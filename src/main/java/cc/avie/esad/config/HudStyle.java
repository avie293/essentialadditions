package cc.avie.esad.config;

import me.avie29.tabbylib.api.HudPosition;
import me.avie29.tabbylib.api.HudPreview;
import me.avie29.tabbylib.api.OptionGroup;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.ColorOption;
import me.avie29.tabbylib.api.option.DoubleOption;
import me.avie29.tabbylib.api.option.HudPositionOption;
import me.avie29.tabbylib.api.option.Option;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * The usual settings of a HUD element: position (moved in the TabbyLib HUD editor), size, background and text
 * shadow. The option keys start with the prefix, e.g. "infoHudScale".
 */
public final class HudStyle {
	public final HudPositionOption position;
	public final DoubleOption scale;
	public final BooleanOption background;
	public final ColorOption backgroundColor;
	public final BooleanOption textShadow;

	public HudStyle(String prefix, HudPosition defaultPosition, Supplier<HudPreview> preview) {
		this(prefix, defaultPosition, preview, true);
	}

	/** With background off by default, for elements that draw their own boxes. */
	public HudStyle(String prefix, HudPosition defaultPosition, Supplier<HudPreview> preview, boolean background) {
		this.position = HudPositionOption.builder(prefix + "Position", defaultPosition, preview).build();
		this.scale = DoubleOption.builder(prefix + "Scale", 1.0)
			.slider(0.5, 3.0, 0.25)
			.formatter(value -> Component.literal(String.format(Locale.ROOT, "%.2fx", value)))
			.build();
		this.background = BooleanOption.builder(prefix + "Background", background).build();
		this.backgroundColor = ColorOption.builder(prefix + "BackgroundColor", 0x90000000).alpha().dependsOn(this.background).build();
		this.textShadow = BooleanOption.builder(prefix + "TextShadow", true).build();
	}

	/** Adds the options as "Appearance" group. */
	public void addTo(OptionGroup group) {
		group.add(this.position, this.scale, this.background, this.backgroundColor, this.textShadow);
	}

	/** The saved value, or the unsaved one of the settings window for the HUD editor preview. */
	public static <T> T value(Option<T> option, boolean pending) {
		return pending ? option.getPending() : option.get();
	}

	public float scale(boolean pending) {
		return value(this.scale, pending).floatValue();
	}

	/**
	 * Moves and scales the pose to the HUD position, for an element with the given unscaled size.
	 * Call {@code graphics.pose().popMatrix()} after drawing.
	 */
	public void push(GuiGraphicsExtractor graphics, int width, int height, boolean pending) {
		Minecraft minecraft = Minecraft.getInstance();
		float scale = this.scale(pending);
		int scaledWidth = Math.round(width * scale);
		int scaledHeight = Math.round(height * scale);
		HudPosition position = value(this.position, pending);
		int x = position.x(minecraft.getWindow().getGuiScaledWidth(), scaledWidth);
		int y = position.y(minecraft.getWindow().getGuiScaledHeight(), scaledHeight);
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale, scale);
	}

	/** Background box with cut corners, like the day counter. */
	public void drawBackground(GuiGraphicsExtractor graphics, int width, int height, boolean pending) {
		if (value(this.background, pending)) {
			box(graphics, 0, 0, width, height, value(this.backgroundColor, pending));
		}
	}

	public boolean shadow(boolean pending) {
		return value(this.textShadow, pending);
	}

	/** Filled box with the four corner pixels left out. */
	public static void box(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x + 1, y, x + width - 1, y + 1, color);
		graphics.fill(x, y + 1, x + width, y + height - 1, color);
		graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
	}

	/** A preview for the HUD editor from a size supplier and a draw call at 0, 0 (pose already moved by the editor). */
	public HudPreview preview(Supplier<int[]> size, Drawer drawer) {
		HudStyle style = this;
		return new HudPreview() {
			@Override
			public int width() {
				return Math.round(size.get()[0] * style.scale(true));
			}

			@Override
			public int height() {
				return Math.round(size.get()[1] * style.scale(true));
			}

			@Override
			public void render(GuiGraphicsExtractor graphics, int x, int y) {
				float scale = style.scale(true);
				graphics.pose().pushMatrix();
				graphics.pose().translate(x, y);
				graphics.pose().scale(scale, scale);
				drawer.draw(graphics, true);
				graphics.pose().popMatrix();
			}
		};
	}

	/** Draws a HUD element at 0, 0 in unscaled coordinates. */
	@FunctionalInterface
	public interface Drawer {
		void draw(GuiGraphicsExtractor graphics, boolean pending);
	}
}
