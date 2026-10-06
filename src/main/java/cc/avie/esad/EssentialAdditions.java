package cc.avie.esad;

import cc.avie.esad.config.EsadConfig;
import cc.avie.esad.feature.daycounter.DayCounter;
import cc.avie.esad.feature.dynamiclights.DynamicLights;
import cc.avie.esad.feature.tooltips.AdvancedTooltips;
import cc.avie.esad.feature.zoom.Zoom;
import net.fabricmc.api.ClientModInitializer;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EssentialAdditions implements ClientModInitializer {
	public static final String MOD_ID = "esad";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** Category of all key mappings of the mod in the controls screen. */
	public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(id("main"));

	@Override
	public void onInitializeClient() {
		// Key mappings first, the config shows them in the TabbyLib screen
		Zoom.register();
		DayCounter.register();
		EsadConfig.init();
		DynamicLights.register();
		AdvancedTooltips.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
