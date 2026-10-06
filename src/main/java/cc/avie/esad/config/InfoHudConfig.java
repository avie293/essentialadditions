package cc.avie.esad.config;

import cc.avie.esad.feature.hud.InfoHud;
import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.ConfigCategory;
import me.avie29.tabbylib.api.HudPosition;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.ColorOption;
import net.minecraft.world.item.Items;

public final class InfoHudConfig implements FeatureConfig {
	public static final InfoHudConfig INSTANCE = new InfoHudConfig();

	public static final BooleanOption ENABLED = BooleanOption.builder("infoHudEnabled", false).hidden().build();

	public static final BooleanOption COORDINATES = BooleanOption.builder("infoHudCoordinates", true).build();
	public static final BooleanOption DECIMALS = BooleanOption.builder("infoHudDecimals", false).dependsOn(COORDINATES).build();
	public static final BooleanOption OTHER_DIMENSION = BooleanOption.builder("infoHudOtherDimension", true).build();
	public static final BooleanOption FACING = BooleanOption.builder("infoHudFacing", true).build();
	public static final BooleanOption BIOME = BooleanOption.builder("infoHudBiome", true).build();
	public static final BooleanOption LIGHT = BooleanOption.builder("infoHudLight", false).build();
	public static final BooleanOption FPS = BooleanOption.builder("infoHudFps", true).build();
	public static final BooleanOption PING = BooleanOption.builder("infoHudPing", true).build();
	public static final BooleanOption CLOCK = BooleanOption.builder("infoHudClock", false).build();
	public static final BooleanOption TWENTY_FOUR_HOURS = BooleanOption.builder("infoHudTwentyFourHours", true).dependsOn(CLOCK).build();

	public static final ColorOption LABEL_COLOR = ColorOption.builder("infoHudLabelColor", 0xFFFFAA00).build();
	public static final ColorOption VALUE_COLOR = ColorOption.builder("infoHudValueColor", 0xFFFFFFFF).build();
	public static final HudStyle STYLE = new HudStyle("infoHud", HudPosition.of(HudPosition.START, HudPosition.START, 4, 4), InfoHud::preview);

	private InfoHudConfig() {
	}

	@Override
	public String key() {
		return "infoHud";
	}

	@Override
	public ConfigCategory category() {
		return new ConfigCategory(this.key()).file("esad/infohud")
			.add(ENABLED)
			.group("infoHudLines", group -> group.add(COORDINATES, DECIMALS, OTHER_DIMENSION, FACING, BIOME, LIGHT, FPS, PING,
				CLOCK, TWENTY_FOUR_HOURS))
			.group("infoHudAppearance", group -> {
				group.add(LABEL_COLOR, VALUE_COLOR);
				STYLE.addTo(group);
			});
	}

	@Override
	public BooleanOption toggle() {
		return ENABLED;
	}

	@Override
	public FeatureIcon icon() {
		return FeatureIcon.of(Items.FILLED_MAP);
	}
}
