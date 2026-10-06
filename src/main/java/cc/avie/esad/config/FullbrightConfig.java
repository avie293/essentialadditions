package cc.avie.esad.config;

import cc.avie.esad.feature.fullbright.Fullbright;
import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.IntOption;
import me.avie29.tabbylib.api.option.KeyBindOption;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public final class FullbrightConfig implements FeatureConfig {
	public static final FullbrightConfig INSTANCE = new FullbrightConfig();

	/** Off by default, it is meant to be switched on with its key when needed. */
	public static final BooleanOption ENABLED = BooleanOption.builder("fullbrightEnabled", false).hidden().build();
	public static final KeyBindOption KEY = KeyBindOption.builder("fullbrightKey", Fullbright.KEY).build();
	public static final IntOption STRENGTH = IntOption.builder("fullbrightStrength", 100)
		.slider(10, 100, 5).formatter(value -> Component.literal(value + "%")).build();
	public static final BooleanOption MESSAGE = BooleanOption.builder("fullbrightMessage", true).build();

	private FullbrightConfig() {
	}

	@Override
	public String key() {
		return "fullbright";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/fullbright")
			.add(ENABLED, KEY, STRENGTH, MESSAGE);
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.GLOWSTONE, "block/glowstone");
	}
}
