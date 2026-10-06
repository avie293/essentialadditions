package cc.avie.esad.feature.daycounter;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.config.EsadConfig;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.avie29.tabbylib.api.TabbyLibApi;
import me.avie29.tabbylib.api.option.BooleanOption;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Day counter from the Day Counter mod: shows the current Minecraft day in a HUD that can be moved
 * with the TabbyLib HUD editor. Toggled with a key or the {@code /dc} command.
 */
public final class DayCounter {
	public static final KeyMapping TOGGLE_KEY = new KeyMapping("key.esad.toggle_day_counter", InputConstants.KEY_H, EssentialAdditions.KEY_CATEGORY);

	private static int debugTicks;

	private DayCounter() {
	}

	/** False when the standalone Day Counter mod is installed, so the day is not shown twice. */
	public static boolean isActive() {
		return !FabricLoader.getInstance().isModLoaded("day-counter");
	}

	public static void register() {
		KeyMappingHelper.registerKeyMapping(TOGGLE_KEY);
		if (!isActive()) {
			EssentialAdditions.LOGGER.info("Day Counter is installed, the day counter of EssentialAdditions is turned off");
			return;
		}
		HudElementRegistry.addLast(EssentialAdditions.id("day_counter"), (graphics, deltaTracker) -> DayCounterHud.render(graphics));
		ClientTickEvents.END_CLIENT_TICK.register(DayCounter::tick);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(command()));
	}

	/** The current day, counted from 0. */
	public static long currentDay() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			return 0;
		}
		return Math.max(0, minecraft.level.getOverworldClockTime() / 24000L);
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> command() {
		return LiteralArgumentBuilder.<FabricClientCommandSource>literal("dc")
			.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("config").executes(context -> {
				TabbyLibApi.openScreen(EssentialAdditions.MOD_ID);
				return 1;
			}))
			.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("position").executes(context -> {
				TabbyLibApi.openHudEditor(EsadConfig.DAY_COUNTER_POSITION);
				return 1;
			}))
			.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("bgtoggle").executes(context -> {
				boolean enabled = toggle(EsadConfig.DAY_COUNTER_BACKGROUND);
				context.getSource().sendFeedback(Component.translatable(enabled
					? "message.esad.day_counter.background_enabled" : "message.esad.day_counter.background_disabled"));
				return 1;
			}))
			.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("debug").executes(context -> {
				boolean enabled = toggle(EsadConfig.DAY_COUNTER_DEBUG);
				context.getSource().sendFeedback(Component.translatable(enabled
					? "message.esad.day_counter.debug_enabled" : "message.esad.day_counter.debug_disabled"));
				return 1;
			}))
			.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("help").executes(context -> {
				for (String key : new String[] {"help_title", "help_config", "help_position", "help_bgtoggle", "help_debug"}) {
					context.getSource().sendFeedback(Component.translatable("message.esad.day_counter." + key));
				}
				return 1;
			}));
	}

	private static void tick(Minecraft minecraft) {
		if (minecraft.player == null) {
			return;
		}
		while (TOGGLE_KEY.consumeClick()) {
			boolean visible = toggle(EsadConfig.DAY_COUNTER_VISIBLE);
			minecraft.player.sendSystemMessage(Component.translatable(visible
				? "message.esad.day_counter.hud_enabled" : "message.esad.day_counter.hud_disabled"));
		}
		if (EsadConfig.DAY_COUNTER_DEBUG.get() && minecraft.level != null && ++debugTicks >= 100) {
			debugTicks = 0;
			EssentialAdditions.LOGGER.info("[Day Counter] day={}, dayTime={}, gameTime={}, dimension={}, player=({}, {}, {})",
				currentDay(), minecraft.level.getOverworldClockTime(), minecraft.level.getGameTime(), minecraft.level.dimension(),
				String.format(Locale.ROOT, "%.2f", minecraft.player.getX()),
				String.format(Locale.ROOT, "%.2f", minecraft.player.getY()),
				String.format(Locale.ROOT, "%.2f", minecraft.player.getZ()));
		}
	}

	private static boolean toggle(BooleanOption option) {
		option.set(!option.get());
		EsadConfig.CONFIG.save();
		return option.get();
	}
}
