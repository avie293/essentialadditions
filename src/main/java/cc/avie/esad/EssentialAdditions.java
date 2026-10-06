package cc.avie.esad;

import cc.avie.esad.config.EsadConfig;
import cc.avie.esad.feature.daycounter.DayCounter;
import cc.avie.esad.feature.dynamiclights.DynamicLights;
import cc.avie.esad.feature.freelook.Freelook;
import cc.avie.esad.feature.fullbright.Fullbright;
import cc.avie.esad.feature.hud.ArmorHud;
import cc.avie.esad.feature.hud.EffectsHud;
import cc.avie.esad.feature.hud.InfoHud;
import cc.avie.esad.feature.hud.Keystrokes;
import cc.avie.esad.feature.hud.PickupNotifier;
import cc.avie.esad.feature.sleep.SleepReminder;
import cc.avie.esad.feature.sort.InventorySort;
import cc.avie.esad.feature.togglesprint.ToggleSprint;
import cc.avie.esad.feature.tooltips.AdvancedTooltips;
import cc.avie.esad.feature.zoom.Zoom;
import cc.avie.esad.gui.EsadConfigScreen;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

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
		Fullbright.register();
		InfoHud.register();
		ArmorHud.register();
		EffectsHud.register();
		Keystrokes.register();
		ToggleSprint.register();
		SleepReminder.register();
		PickupNotifier.register();
		Freelook.register();
		InventorySort.register();
		EsadConfig.init();
		DynamicLights.register();
		AdvancedTooltips.register();
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
			LiteralArgumentBuilder.<FabricClientCommandSource>literal(MOD_ID).executes(context -> {
				EsadConfigScreen.open(null);
				return 1;
			})));
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
