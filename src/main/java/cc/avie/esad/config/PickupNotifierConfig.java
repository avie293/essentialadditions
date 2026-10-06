package cc.avie.esad.config;

import cc.avie.esad.feature.hud.PickupNotifier;
import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.HudPosition;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.DoubleOption;
import me.avie29.tabbylib.api.option.IntOption;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.Locale;

public final class PickupNotifierConfig implements FeatureConfig {
	public static final PickupNotifierConfig INSTANCE = new PickupNotifierConfig();

	public static final BooleanOption ENABLED = BooleanOption.builder("pickupNotifierEnabled", false).hidden().build();

	public static final DoubleOption DURATION = DoubleOption.builder("pickupNotifierDuration", 3.0)
		.slider(1.0, 10.0, 0.5).formatter(value -> Component.literal(String.format(Locale.ROOT, "%.1fs", value))).build();
	public static final IntOption MAX_ENTRIES = IntOption.builder("pickupNotifierMaxEntries", 5).slider(1, 10, 1).build();
	public static final BooleanOption ICONS = BooleanOption.builder("pickupNotifierIcons", true).build();
	public static final BooleanOption RARITY_COLORS = BooleanOption.builder("pickupNotifierRarityColors", true).build();
	public static final BooleanOption TOTAL = BooleanOption.builder("pickupNotifierTotal", false).build();

	public static final HudStyle STYLE = new HudStyle("pickupNotifier", HudPosition.of(HudPosition.END, HudPosition.CENTER, -4, 20),
		PickupNotifier::preview, false);

	private PickupNotifierConfig() {
	}

	@Override
	public String key() {
		return "pickupNotifier";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/pickupnotifier")
			.add(ENABLED, DURATION, MAX_ENTRIES, ICONS, RARITY_COLORS, TOTAL)
			.group("pickupNotifierAppearance", STYLE::addTo);
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.HOPPER);
	}
}
