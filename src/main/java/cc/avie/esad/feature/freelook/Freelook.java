package cc.avie.esad.feature.freelook;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.config.FreelookConfig;
import cc.avie.esad.feature.zoom.ZoomMode;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Freelook (like the Perspective mod): while the key is active, the mouse turns only the camera, the player
 * keeps walking and looking in the old direction. By default the camera switches to third person meanwhile.
 */
public final class Freelook {
	public static final KeyMapping KEY = new KeyMapping("key.esad.freelook", InputConstants.KEY_LALT, EssentialAdditions.KEY_CATEGORY);

	private static boolean active;
	private static float yaw;
	private static float pitch;
	/** The camera type before freelook switched to third person, null when it did not switch. */
	private static @Nullable CameraType previousCamera;

	private Freelook() {
	}

	public static void register() {
		KeyMappingHelper.registerKeyMapping(KEY);
		ClientTickEvents.END_CLIENT_TICK.register(Freelook::tick);
	}

	private static void tick(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		boolean wanted;
		if (!FreelookConfig.ENABLED.get() || player == null) {
			while (KEY.consumeClick()) {
				// Clicks are only used in toggle mode
			}
			wanted = false;
		} else if (FreelookConfig.MODE.get() == ZoomMode.TOGGLE) {
			wanted = active;
			while (KEY.consumeClick()) {
				wanted = !wanted;
			}
		} else {
			while (KEY.consumeClick()) {
				// Clicks are only used in toggle mode
			}
			wanted = KEY.isDown();
		}

		if (wanted && !active && player != null) {
			start(minecraft, player);
		} else if (!wanted && active) {
			stop(minecraft);
		}
	}

	private static void start(Minecraft minecraft, LocalPlayer player) {
		active = true;
		yaw = player.getYRot();
		pitch = player.getXRot();
		if (FreelookConfig.THIRD_PERSON.get() && minecraft.options.getCameraType().isFirstPerson()) {
			previousCamera = minecraft.options.getCameraType();
			minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
		}
	}

	private static void stop(Minecraft minecraft) {
		active = false;
		if (previousCamera != null) {
			minecraft.options.setCameraType(previousCamera);
			previousCamera = null;
		}
	}

	public static boolean isActive() {
		return active;
	}

	/** Mouse movement while active, with the same factor vanilla uses for the player rotation. */
	public static void turn(double yawChange, double pitchChange) {
		yaw += (float) yawChange * 0.15f;
		float pitchDelta = (float) pitchChange * 0.15f;
		pitch = Mth.clamp(pitch + (FreelookConfig.INVERT_PITCH.get() ? -pitchDelta : pitchDelta), -90, 90);
	}

	public static float yaw() {
		return yaw;
	}

	public static float pitch() {
		return pitch;
	}
}
