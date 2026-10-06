package cc.avie.esad.config;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.feature.dynamiclights.UpdateRate;
import cc.avie.esad.feature.zoom.Zoom;
import cc.avie.esad.feature.zoom.ZoomEasing;
import cc.avie.esad.feature.zoom.ZoomMode;
import me.avie29.tabbylib.api.TabbyConfig;
import me.avie29.tabbylib.api.option.BooleanOption;
import me.avie29.tabbylib.api.option.DoubleOption;
import me.avie29.tabbylib.api.option.EnumOption;
import me.avie29.tabbylib.api.option.IntOption;
import me.avie29.tabbylib.api.option.KeyBindOption;
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

	public static TabbyConfig CONFIG;

	private EsadConfig() {
	}

	public static void init() {
		CONFIG = TabbyConfig.builder(EssentialAdditions.MOD_ID)
			.category("zoom", category -> category
				.add(ZOOM_ENABLED, ZOOM_KEY, ZOOM_MODE, ZOOM_LEVEL)
				.group("scroll", group -> group.add(SCROLL_ZOOM, SCROLL_STEP, MAX_ZOOM, RETAIN_SCROLL_ZOOM))
				.group("animation", group -> group.add(ZOOM_IN_TIME, ZOOM_OUT_TIME, ZOOM_EASING, SMOOTH_SCROLL))
				.group("camera", group -> group.add(RELATIVE_SENSITIVITY, CINEMATIC_CAMERA, HIDE_HAND)))
			.category("dynamicLights", category -> category
				.add(DYNAMIC_LIGHTS_ENABLED, DYNAMIC_LIGHTS_UPDATE_RATE, DYNAMIC_LIGHTS_RANGE)
				.group("lightSources", group -> group.add(DYNAMIC_LIGHTS_SELF, DYNAMIC_LIGHTS_HELD_ITEMS,
					DYNAMIC_LIGHTS_DROPPED_ITEMS, DYNAMIC_LIGHTS_BURNING, DYNAMIC_LIGHTS_ENTITIES, DYNAMIC_LIGHTS_WATER_SENSITIVE)))
			.build();
	}

	private static Component multiplier(double value) {
		return Component.literal(format(value) + "x");
	}

	private static Component seconds(double value) {
		return Component.literal(format(value) + "s");
	}

	private static String format(double value) {
		return value == Math.rint(value) ? Long.toString(Math.round(value)) : String.format(Locale.ROOT, "%.2f", value);
	}
}
