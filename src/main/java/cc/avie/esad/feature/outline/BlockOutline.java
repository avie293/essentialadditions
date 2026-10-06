package cc.avie.esad.feature.outline;

import cc.avie.esad.config.BlockOutlineConfig;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

/** Color and line width of the outline around the block you look at. */
public final class BlockOutline {
	/** The extra thick black outline of the high contrast mode, it is left as it is. */
	private static final int HIGH_CONTRAST_COLOR = 0xFF000000;
	private static final float HIGH_CONTRAST_WIDTH = 7.0f;

	private BlockOutline() {
	}

	public static int color(int original) {
		if (!BlockOutlineConfig.ENABLED.get() || original == HIGH_CONTRAST_COLOR) {
			return original;
		}
		int color = BlockOutlineConfig.COLOR.get();
		if (BlockOutlineConfig.RAINBOW.get()) {
			// One full cycle every 4 seconds at speed 1, keeps the alpha of the chosen color
			float hue = (float) (Util.getMillis() * BlockOutlineConfig.RAINBOW_SPEED.get() % 4000 / 4000.0);
			color = (color & 0xFF000000) | (Mth.hsvToRgb(hue, 0.8f, 1.0f) & 0xFFFFFF);
		}
		return color;
	}

	public static float width(float original) {
		if (!BlockOutlineConfig.ENABLED.get() || original >= HIGH_CONTRAST_WIDTH) {
			return original;
		}
		return (float) (original * BlockOutlineConfig.WIDTH.get());
	}
}
