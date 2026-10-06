package cc.avie.esad.feature.zoom;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.config.EsadConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * Zoom like Zoomify / OptiFine: lowers the FOV while the zoom key is active, with an eased
 * animation, scroll wheel zoom, lower mouse sensitivity and an optional cinematic camera.
 * <p>
 * All state is updated once per frame from {@link #modifyFov}, so the animation runs on real time
 * and not on game ticks.
 */
public final class Zoom {
	public static final KeyMapping KEY = new KeyMapping("key.esad.zoom", InputConstants.KEY_C, EssentialAdditions.KEY_CATEGORY);

	/** How fast the smooth scroll zoom follows the target, higher is faster. */
	private static final double SCROLL_SMOOTHING = 15.0;

	private static boolean active;
	/** Linear animation progress, 0 = not zoomed, 1 = fully zoomed. */
	private static double progress;
	/** Zoom the scroll wheel aims for. */
	private static double targetZoom = 1.0;
	/** Zoom that follows {@link #targetZoom}, smoothed when smooth scrolling is on. */
	private static double currentZoom = 1.0;
	/** The zoom factor of the last frame, the FOV is divided by it. */
	private static double factor = 1.0;
	private static boolean scrolledSinceStart;
	private static long lastFrameNanos;

	private Zoom() {
	}

	public static void register() {
		KeyMappingHelper.registerKeyMapping(KEY);
	}

	/** Called by the camera every frame with the final FOV. */
	public static float modifyFov(float fov) {
		update();
		return factor == 1.0 ? fov : (float) (fov / factor);
	}

	private static void update() {
		long now = System.nanoTime();
		// Capped so a lag spike does not skip the whole animation
		double delta = lastFrameNanos == 0 ? 0 : Math.min((now - lastFrameNanos) / 1_000_000_000.0, 0.1);
		lastFrameNanos = now;

		boolean wasActive = active;
		active = wantsZoom();
		if (active && !wasActive) {
			start();
		}

		double duration = active ? EsadConfig.ZOOM_IN_TIME.get() : EsadConfig.ZOOM_OUT_TIME.get();
		double change = duration <= 0 ? 1 : delta / duration;
		progress = Mth.clamp(progress + (active ? change : -change), 0, 1);

		if (EsadConfig.SMOOTH_SCROLL.get()) {
			currentZoom = targetZoom + (currentZoom - targetZoom) * Math.exp(-SCROLL_SMOOTHING * delta);
		} else {
			currentZoom = targetZoom;
		}

		// Interpolated exponentially, so every step of the animation feels equally fast
		double eased = EsadConfig.ZOOM_EASING.get().apply(progress);
		factor = progress <= 0 ? 1.0 : Math.pow(currentZoom, eased);
	}

	private static boolean wantsZoom() {
		Minecraft minecraft = Minecraft.getInstance();
		if (!EsadConfig.ZOOM_ENABLED.get() || minecraft.player == null) {
			drainClicks();
			return false;
		}
		if (EsadConfig.ZOOM_MODE.get() == ZoomMode.TOGGLE) {
			boolean result = active;
			while (KEY.consumeClick()) {
				result = !result;
			}
			return result;
		}
		drainClicks();
		return KEY.isDown();
	}

	private static void drainClicks() {
		while (KEY.consumeClick()) {
			// Clicks are only used in toggle mode
		}
	}

	private static void start() {
		if (!EsadConfig.RETAIN_SCROLL_ZOOM.get() || !scrolledSinceStart) {
			targetZoom = EsadConfig.ZOOM_LEVEL.get();
			scrolledSinceStart = false;
		}
		// The zoom animation already eases in, the scroll smoothing would only slow it down
		currentZoom = targetZoom;
	}

	/**
	 * Called with the scroll wheel steps while no screen is open.
	 *
	 * @return true when the zoom used the scroll, so the hotbar slot must not change
	 */
	public static boolean onScroll(int steps) {
		if (!active || !EsadConfig.SCROLL_ZOOM.get()) {
			return false;
		}
		if (steps != 0) {
			double max = Math.max(EsadConfig.MAX_ZOOM.get(), EsadConfig.ZOOM_LEVEL.get());
			targetZoom = Mth.clamp(targetZoom * Math.pow(EsadConfig.SCROLL_STEP.get(), steps), 1.0, max);
			scrolledSinceStart = true;
		}
		return true;
	}

	/** Multiplier for the mouse movement, lowers the sensitivity the further you are zoomed in. */
	public static double sensitivityMultiplier() {
		if (factor == 1.0) {
			return 1.0;
		}
		return Math.pow(factor, -EsadConfig.RELATIVE_SENSITIVITY.get() / 100.0);
	}

	public static boolean useCinematicCamera() {
		return active && EsadConfig.CINEMATIC_CAMERA.get();
	}

	public static boolean shouldHideHand() {
		return progress > 0 && EsadConfig.HIDE_HAND.get();
	}
}
