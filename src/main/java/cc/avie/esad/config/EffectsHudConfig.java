package cc.avie.esad.config;

import cc.avie.esad.feature.hud.EffectsHud;
import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.HudPosition;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.EnumOption;
import me.avie29.tabbylib.api.option.IntOption;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public final class EffectsHudConfig implements FeatureConfig {
	public static final EffectsHudConfig INSTANCE = new EffectsHudConfig();

	public enum Sorting {
		/** Like the effects were added. */
		NONE,
		/** Shortest remaining time first. */
		DURATION,
		/** Good effects first, then bad ones. */
		CATEGORY
	}

	public static final BooleanOption ENABLED = BooleanOption.builder("effectsHudEnabled", false).hidden().build();

	public static final BooleanOption NAMES = BooleanOption.builder("effectsHudNames", true).build();
	public static final BooleanOption DURATION = BooleanOption.builder("effectsHudDuration", true).build();
	public static final EnumOption<Sorting> SORTING = EnumOption.builder("effectsHudSorting", Sorting.DURATION).build();
	public static final BooleanOption CATEGORY_COLORS = BooleanOption.builder("effectsHudCategoryColors", true).build();
	public static final IntOption BLINK_SECONDS = IntOption.builder("effectsHudBlinkSeconds", 10)
		.slider(0, 30, 1).formatter(value -> Component.literal(value + "s")).build();
	public static final BooleanOption HIDE_VANILLA = BooleanOption.builder("effectsHudHideVanilla", true).build();

	public static final HudStyle STYLE = new HudStyle("effectsHud", HudPosition.of(HudPosition.END, HudPosition.START, -4, 4), EffectsHud::preview);

	private EffectsHudConfig() {
	}

	@Override
	public String key() {
		return "effectsHud";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/effectshud")
			.add(ENABLED, NAMES, DURATION, SORTING, CATEGORY_COLORS, BLINK_SECONDS, HIDE_VANILLA)
			.group("effectsHudAppearance", STYLE::addTo);
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.POTION);
	}
}
