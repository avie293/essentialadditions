package cc.avie.esad.config;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.feature.daycounter.DayCounter;
import cc.avie.esad.feature.daycounter.DayCounterFormat;
import cc.avie.esad.feature.daycounter.DayCounterHud;
import cc.avie.esad.feature.dynamiclights.UpdateRate;
import cc.avie.esad.feature.pingdisplay.PingColorMode;
import cc.avie.esad.feature.pingdisplay.PingDisplay;
import cc.avie.esad.feature.tooltips.DurabilityColor;
import cc.avie.esad.feature.tooltips.DurabilityStyle;
import cc.avie.esad.feature.tooltips.TooltipMode;
import cc.avie.esad.feature.zoom.Zoom;
import cc.avie.esad.feature.zoom.ZoomEasing;
import cc.avie.esad.feature.zoom.ZoomMode;
import me.avie29.tabbylib.api.HudPosition;
import me.avie29.tabbylib.api.TabbyConfig;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.ColorOption;
import me.avie29.tabbylib.api.option.DoubleOption;
import me.avie29.tabbylib.api.option.EnumOption;
import me.avie29.tabbylib.api.option.HudPositionOption;
import me.avie29.tabbylib.api.option.IntOption;
import me.avie29.tabbylib.api.option.KeyBindOption;
import me.avie29.tabbylib.api.option.StringOption;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** All settings of the mod, shown in the TabbyLib config screen and saved in config/esad.json. */
public final class EsadConfig {
	// ---------------------------------------------------------------- zoom
	public static final BooleanOption ZOOM_ENABLED = BooleanOption.builder("zoomEnabled", true).build();
	public static final KeyBindOption ZOOM_KEY = KeyBindOption.builder("zoomKey", Zoom.KEY).build();
	public static final EnumOption<ZoomMode> ZOOM_MODE = EnumOption.builder("zoomMode", ZoomMode.HOLD)
		.dependsOn(ZOOM_ENABLED).build();
	public static final DoubleOption ZOOM_LEVEL = DoubleOption.builder("zoomLevel", 4.0)
		.slider(1.5, 20.0, 0.5).formatter(EsadConfig::multiplier).dependsOn(ZOOM_ENABLED).build();

	public static final BooleanOption SCROLL_ZOOM = BooleanOption.builder("scrollZoom", true)
		.dependsOn(ZOOM_ENABLED).build();
	public static final DoubleOption SCROLL_STEP = DoubleOption.builder("scrollStep", 1.25)
		.slider(1.05, 2.0, 0.05).formatter(EsadConfig::multiplier).dependsOn(SCROLL_ZOOM).build();
	public static final DoubleOption MAX_ZOOM = DoubleOption.builder("maxZoom", 50.0)
		.slider(2.0, 100.0, 1.0).formatter(EsadConfig::multiplier).dependsOn(SCROLL_ZOOM).build();
	public static final BooleanOption RETAIN_SCROLL_ZOOM = BooleanOption.builder("retainScrollZoom", false)
		.dependsOn(SCROLL_ZOOM).build();

	public static final DoubleOption ZOOM_IN_TIME = DoubleOption.builder("zoomInTime", 0.25)
		.slider(0.0, 2.0, 0.05).formatter(EsadConfig::seconds).dependsOn(ZOOM_ENABLED).build();
	public static final DoubleOption ZOOM_OUT_TIME = DoubleOption.builder("zoomOutTime", 0.2)
		.slider(0.0, 2.0, 0.05).formatter(EsadConfig::seconds).dependsOn(ZOOM_ENABLED).build();
	public static final EnumOption<ZoomEasing> ZOOM_EASING = EnumOption.builder("zoomEasing", ZoomEasing.SINE)
		.dependsOn(ZOOM_ENABLED).build();
	public static final BooleanOption SMOOTH_SCROLL = BooleanOption.builder("smoothScroll", true)
		.dependsOn(SCROLL_ZOOM).build();

	public static final IntOption RELATIVE_SENSITIVITY = IntOption.builder("relativeSensitivity", 100)
		.slider(0, 100, 5).formatter(value -> Component.literal(value + "%")).dependsOn(ZOOM_ENABLED).build();
	public static final BooleanOption CINEMATIC_CAMERA = BooleanOption.builder("cinematicCamera", false)
		.dependsOn(ZOOM_ENABLED).build();
	public static final BooleanOption HIDE_HAND = BooleanOption.builder("hideHand", true)
		.dependsOn(ZOOM_ENABLED).build();

	// ---------------------------------------------------------------- dynamic lights
	public static final BooleanOption DYNAMIC_LIGHTS_ENABLED = BooleanOption.builder("dynamicLightsEnabled", true).build();
	public static final EnumOption<UpdateRate> DYNAMIC_LIGHTS_UPDATE_RATE = EnumOption.builder("dynamicLightsUpdateRate", UpdateRate.REALTIME)
		.dependsOn(DYNAMIC_LIGHTS_ENABLED).build();
	public static final IntOption DYNAMIC_LIGHTS_RANGE = IntOption.builder("dynamicLightsRange", 64)
		.slider(16, 128, 8).formatter(value -> Component.translatable("config.esad.unit.blocks", value)).dependsOn(DYNAMIC_LIGHTS_ENABLED).build();

	public static final BooleanOption DYNAMIC_LIGHTS_SELF = BooleanOption.builder("dynamicLightsSelf", true)
		.dependsOn(DYNAMIC_LIGHTS_ENABLED).build();
	public static final BooleanOption DYNAMIC_LIGHTS_HELD_ITEMS = BooleanOption.builder("dynamicLightsHeldItems", true)
		.dependsOn(DYNAMIC_LIGHTS_ENABLED).build();
	public static final BooleanOption DYNAMIC_LIGHTS_DROPPED_ITEMS = BooleanOption.builder("dynamicLightsDroppedItems", true)
		.dependsOn(DYNAMIC_LIGHTS_ENABLED).build();
	public static final BooleanOption DYNAMIC_LIGHTS_BURNING = BooleanOption.builder("dynamicLightsBurning", true)
		.dependsOn(DYNAMIC_LIGHTS_ENABLED).build();
	public static final BooleanOption DYNAMIC_LIGHTS_ENTITIES = BooleanOption.builder("dynamicLightsEntities", true)
		.dependsOn(DYNAMIC_LIGHTS_ENABLED).build();
	public static final BooleanOption DYNAMIC_LIGHTS_WATER_SENSITIVE = BooleanOption.builder("dynamicLightsWaterSensitive", true)
		.dependsOn(DYNAMIC_LIGHTS_ENABLED).build();

	// ---------------------------------------------------------------- tooltips
	public static final BooleanOption TOOLTIPS_ENABLED = BooleanOption.builder("tooltipsEnabled", true).build();
	public static final BooleanOption TOOLTIP_SHIFT_HINT = BooleanOption.builder("tooltipShiftHint", true)
		.dependsOn(TOOLTIPS_ENABLED).build();

	public static final EnumOption<TooltipMode> TOOLTIP_DURABILITY = EnumOption.builder("tooltipDurability", TooltipMode.ALWAYS)
		.dependsOn(TOOLTIPS_ENABLED).build();
	public static final EnumOption<DurabilityStyle> TOOLTIP_DURABILITY_STYLE = EnumOption.builder("tooltipDurabilityStyle", DurabilityStyle.NUMBERS)
		.dependsOn(TOOLTIPS_ENABLED).build();
	public static final EnumOption<DurabilityColor> TOOLTIP_DURABILITY_COLOR = EnumOption.builder("tooltipDurabilityColor", DurabilityColor.VARYING)
		.dependsOn(TOOLTIPS_ENABLED).build();
	public static final BooleanOption TOOLTIP_DURABILITY_LABEL = BooleanOption.builder("tooltipDurabilityLabel", true)
		.dependsOn(TOOLTIPS_ENABLED).build();
	public static final BooleanOption TOOLTIP_DURABILITY_WHEN_FULL = BooleanOption.builder("tooltipDurabilityWhenFull", true)
		.dependsOn(TOOLTIPS_ENABLED).build();

	public static final EnumOption<TooltipMode> TOOLTIP_FOOD = EnumOption.builder("tooltipFood", TooltipMode.ALWAYS)
		.dependsOn(TOOLTIPS_ENABLED).build();
	public static final EnumOption<TooltipMode> TOOLTIP_FUEL = EnumOption.builder("tooltipFuel", TooltipMode.SHIFT)
		.dependsOn(TOOLTIPS_ENABLED).build();
	public static final EnumOption<TooltipMode> TOOLTIP_REPAIR_COST = EnumOption.builder("tooltipRepairCost", TooltipMode.SHIFT)
		.dependsOn(TOOLTIPS_ENABLED).build();
	public static final EnumOption<TooltipMode> TOOLTIP_ENCHANTMENTS = EnumOption.builder("tooltipEnchantments", TooltipMode.SHIFT)
		.dependsOn(TOOLTIPS_ENABLED).build();

	public static final EnumOption<TooltipMode> TOOLTIP_MOD_NAME = EnumOption.builder("tooltipModName", TooltipMode.ALWAYS)
		.dependsOn(TOOLTIPS_ENABLED).build();
	public static final EnumOption<TooltipMode> TOOLTIP_ITEM_ID = EnumOption.builder("tooltipItemId", TooltipMode.SHIFT)
		.dependsOn(TOOLTIPS_ENABLED).build();

	public static final EnumOption<TooltipMode> TOOLTIP_MAP = EnumOption.builder("tooltipMap", TooltipMode.ALWAYS)
		.dependsOn(TOOLTIPS_ENABLED).build();
	public static final IntOption TOOLTIP_MAP_SIZE = IntOption.builder("tooltipMapSize", 64)
		.slider(32, 128, 16).formatter(value -> Component.literal(value + " px")).dependsOn(TOOLTIPS_ENABLED).build();

	// ---------------------------------------------------------------- day counter
	public static final BooleanOption DAY_COUNTER_VISIBLE = BooleanOption.builder("dayCounterVisible", true).build();
	public static final KeyBindOption DAY_COUNTER_TOGGLE_KEY = KeyBindOption.builder("dayCounterToggleKey", DayCounter.TOGGLE_KEY).build();
	public static final HudPositionOption DAY_COUNTER_POSITION = HudPositionOption.builder("dayCounterPosition",
		HudPosition.of(HudPosition.CENTER, HudPosition.END, 0, -50), DayCounterHud::preview).build();

	public static final EnumOption<DayCounterFormat> DAY_COUNTER_FORMAT = EnumOption.builder("dayCounterFormat", DayCounterFormat.DAY).build();
	public static final StringOption DAY_COUNTER_CUSTOM_TEXT = StringOption.builder("dayCounterCustomText", "Day {day}")
		.maxLength(64)
		.hint(Component.literal("Day {day} - {time}"))
		.visibleWhen(() -> DAY_COUNTER_FORMAT.getPending() == DayCounterFormat.CUSTOM)
		.build();
	public static final BooleanOption DAY_COUNTER_TWELVE_HOUR = BooleanOption.builder("dayCounterTwelveHour", false)
		.visibleWhen(() -> DAY_COUNTER_FORMAT.getPending() == DayCounterFormat.DAY_AND_TIME || DAY_COUNTER_FORMAT.getPending() == DayCounterFormat.CUSTOM)
		.build();

	public static final ColorOption DAY_COUNTER_TEXT_COLOR = ColorOption.builder("dayCounterTextColor", 0xFFFFFFFF).build();
	public static final BooleanOption DAY_COUNTER_TEXT_SHADOW = BooleanOption.builder("dayCounterTextShadow", true).build();
	public static final DoubleOption DAY_COUNTER_SCALE = DoubleOption.builder("dayCounterScale", 1.0)
		.slider(0.5, 3.0, 0.25).formatter(EsadConfig::multiplier).build();
	public static final BooleanOption DAY_COUNTER_BACKGROUND = BooleanOption.builder("dayCounterBackground", true).build();
	public static final ColorOption DAY_COUNTER_BACKGROUND_COLOR = ColorOption.builder("dayCounterBackgroundColor", 0x90000000)
		.alpha().dependsOn(DAY_COUNTER_BACKGROUND).build();
	public static final BooleanOption DAY_COUNTER_BORDER = BooleanOption.builder("dayCounterBorder", false).build();
	public static final ColorOption DAY_COUNTER_BORDER_COLOR = ColorOption.builder("dayCounterBorderColor", 0xFFFFFF00)
		.alpha().dependsOn(DAY_COUNTER_BORDER).build();
	public static final BooleanOption DAY_COUNTER_DEBUG = BooleanOption.builder("dayCounterDebug", false).build();

	// ---------------------------------------------------------------- ping display
	public static final BooleanOption PING_TAB_LIST = BooleanOption.builder("pingTabList", true).build();
	public static final BooleanOption PING_SHOW_MS = BooleanOption.builder("pingShowMs", true).build();
	public static final BooleanOption PING_TEXT_SHADOW = BooleanOption.builder("pingTextShadow", true).build();
	public static final StringOption PING_UNKNOWN_TEXT = StringOption.builder("pingUnknownText", "N/A").maxLength(8).build();

	public static final BooleanOption PING_NAMETAG = BooleanOption.builder("pingNametag", false).build();
	public static final ColorOption PING_BRACKET_COLOR = ColorOption.builder("pingBracketColor", 0xFFAAAAAA)
		.dependsOn(PING_NAMETAG).build();

	public static final EnumOption<PingColorMode> PING_COLOR_MODE = EnumOption.builder("pingColorMode", PingColorMode.STEPS).build();
	public static final ColorOption PING_COLOR_GOOD = ColorOption.builder("pingColorGood", 0xFF00E676).build();
	public static final ColorOption PING_COLOR_OK = ColorOption.builder("pingColorOk", 0xFFD6CD30).build();
	public static final ColorOption PING_COLOR_BAD = ColorOption.builder("pingColorBad", 0xFFF59042).build();
	public static final ColorOption PING_COLOR_TERRIBLE = ColorOption.builder("pingColorTerrible", 0xFFE53935).build();
	public static final ColorOption PING_COLOR_UNKNOWN = ColorOption.builder("pingColorUnknown", 0xFF535353).build();

	public static final IntOption PING_GOOD_BELOW = IntOption.builder("pingGoodBelow", 50)
		.slider(10, 300, 5).formatter(EsadConfig::milliseconds).build();
	public static final IntOption PING_OK_BELOW = IntOption.builder("pingOkBelow", 100)
		.slider(20, 500, 5).formatter(EsadConfig::milliseconds).build();
	public static final IntOption PING_BAD_BELOW = IntOption.builder("pingBadBelow", 200)
		.slider(50, 1000, 10).formatter(EsadConfig::milliseconds).build();

	public static TabbyConfig CONFIG;

	private EsadConfig() {
	}

	public static void init() {
		TabbyConfig.Builder builder = TabbyConfig.builder(EssentialAdditions.MOD_ID)
			.category("zoom", category -> category
				.add(ZOOM_ENABLED, ZOOM_KEY, ZOOM_MODE, ZOOM_LEVEL)
				.group("scroll", group -> group.add(SCROLL_ZOOM, SCROLL_STEP, MAX_ZOOM, RETAIN_SCROLL_ZOOM))
				.group("animation", group -> group.add(ZOOM_IN_TIME, ZOOM_OUT_TIME, ZOOM_EASING, SMOOTH_SCROLL))
				.group("camera", group -> group.add(RELATIVE_SENSITIVITY, CINEMATIC_CAMERA, HIDE_HAND)))
			.category("dynamicLights", category -> category
				.add(DYNAMIC_LIGHTS_ENABLED, DYNAMIC_LIGHTS_UPDATE_RATE, DYNAMIC_LIGHTS_RANGE)
				.group("lightSources", group -> group.add(DYNAMIC_LIGHTS_SELF, DYNAMIC_LIGHTS_HELD_ITEMS,
					DYNAMIC_LIGHTS_DROPPED_ITEMS, DYNAMIC_LIGHTS_BURNING, DYNAMIC_LIGHTS_ENTITIES, DYNAMIC_LIGHTS_WATER_SENSITIVE)))
			.category("tooltips", category -> category
				.add(TOOLTIPS_ENABLED, TOOLTIP_SHIFT_HINT)
				.group("durability", group -> group.add(TOOLTIP_DURABILITY, TOOLTIP_DURABILITY_STYLE, TOOLTIP_DURABILITY_COLOR,
					TOOLTIP_DURABILITY_LABEL, TOOLTIP_DURABILITY_WHEN_FULL))
				.group("itemInfo", group -> group.add(TOOLTIP_FOOD, TOOLTIP_FUEL, TOOLTIP_REPAIR_COST, TOOLTIP_ENCHANTMENTS))
				.group("itemOrigin", group -> group.add(TOOLTIP_MOD_NAME, TOOLTIP_ITEM_ID))
				.group("mapPreview", group -> group.add(TOOLTIP_MAP, TOOLTIP_MAP_SIZE)));

		// The tabs are left out when the standalone mods are installed, their own settings apply then
		if (DayCounter.isActive()) {
			builder.category("dayCounter", category -> category
				.add(DAY_COUNTER_VISIBLE, DAY_COUNTER_TOGGLE_KEY, DAY_COUNTER_POSITION)
				.group("dayCounterText", group -> group.add(DAY_COUNTER_FORMAT, DAY_COUNTER_CUSTOM_TEXT, DAY_COUNTER_TWELVE_HOUR))
				.group("dayCounterAppearance", group -> group.add(DAY_COUNTER_TEXT_COLOR, DAY_COUNTER_TEXT_SHADOW, DAY_COUNTER_SCALE,
					DAY_COUNTER_BACKGROUND, DAY_COUNTER_BACKGROUND_COLOR, DAY_COUNTER_BORDER, DAY_COUNTER_BORDER_COLOR))
				.group("dayCounterAdvanced", group -> group.add(DAY_COUNTER_DEBUG).collapsed()));
		}
		if (PingDisplay.isActive()) {
			builder.category("pingDisplay", category -> category
				.group("pingTabList", group -> group.add(PING_TAB_LIST, PING_SHOW_MS, PING_TEXT_SHADOW, PING_UNKNOWN_TEXT))
				.group("pingNametags", group -> group.add(PING_NAMETAG, PING_BRACKET_COLOR))
				.group("pingColors", group -> group.add(PING_COLOR_MODE, PING_COLOR_GOOD, PING_COLOR_OK, PING_COLOR_BAD,
					PING_COLOR_TERRIBLE, PING_COLOR_UNKNOWN))
				.group("pingRanges", group -> group
					.label(Component.translatable("config.esad.pingRanges.description"))
					.add(PING_GOOD_BELOW, PING_OK_BELOW, PING_BAD_BELOW)));
		}
		CONFIG = builder.build();
	}

	private static Component multiplier(double value) {
		return Component.literal(format(value) + "x");
	}

	private static Component milliseconds(int value) {
		return Component.literal(value + " ms");
	}

	private static Component seconds(double value) {
		return Component.literal(format(value) + "s");
	}

	private static String format(double value) {
		return value == Math.rint(value) ? Long.toString(Math.round(value)) : String.format(Locale.ROOT, "%.2f", value);
	}
}
