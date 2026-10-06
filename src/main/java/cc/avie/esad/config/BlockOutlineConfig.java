package cc.avie.esad.config;

import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.ColorOption;
import me.avie29.tabbylib.api.option.DoubleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.Locale;

public final class BlockOutlineConfig implements FeatureConfig {
	public static final BlockOutlineConfig INSTANCE = new BlockOutlineConfig();

	public static final BooleanOption ENABLED = BooleanOption.builder("blockOutlineEnabled", false).hidden().build();

	public static final ColorOption COLOR = ColorOption.builder("blockOutlineColor", 0x66000000).alpha().build();
	public static final BooleanOption RAINBOW = BooleanOption.builder("blockOutlineRainbow", false).build();
	public static final DoubleOption RAINBOW_SPEED = DoubleOption.builder("blockOutlineRainbowSpeed", 1.0)
		.slider(0.25, 4.0, 0.25).formatter(value -> Component.literal(String.format(Locale.ROOT, "%.2fx", value)))
		.dependsOn(RAINBOW).build();
	public static final DoubleOption WIDTH = DoubleOption.builder("blockOutlineWidth", 1.0)
		.slider(0.5, 5.0, 0.25).formatter(value -> Component.literal(String.format(Locale.ROOT, "%.2fx", value))).build();

	private BlockOutlineConfig() {
	}

	@Override
	public String key() {
		return "blockOutline";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/blockoutline")
			.add(ENABLED, COLOR, RAINBOW, RAINBOW_SPEED, WIDTH);
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.GLASS, "block/glass");
	}
}
