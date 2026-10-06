package cc.avie.esad.feature.sleep;

import cc.avie.esad.config.SleepReminderConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Reminds you when you can sleep and warns before phantoms spawn.
 * <p>
 * The server keeps the real "time since rest", the client does not know it. So the nights without sleep are
 * counted here while the game runs: each new night you did not sleep in counts one up, sleeping resets it.
 */
public final class SleepReminder {
	/** First tick of the day in which beds can be used in clear weather. */
	private static final long SLEEP_START = 12542;
	private static final long SLEEP_END = 23460;

	private static boolean wasNight;
	private static boolean wasThundering;
	private static boolean sleptThisNight;
	private static int nightsWithoutSleep;
	private static @Nullable ClientLevel lastLevel;

	private SleepReminder() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(SleepReminder::tick);
	}

	private static void tick(Minecraft minecraft) {
		ClientLevel level = minecraft.level;
		LocalPlayer player = minecraft.player;
		if (level == null || player == null) {
			lastLevel = null;
			return;
		}
		if (level != lastLevel) {
			// New world or dimension: start fresh without a reminder for the night that is already running
			lastLevel = level;
			wasNight = isNight(level);
			wasThundering = level.isThundering();
			return;
		}
		if (player.isSleeping()) {
			sleptThisNight = true;
			nightsWithoutSleep = 0;
		}
		// Beds only work in the Overworld
		if (!SleepReminderConfig.ENABLED.get() || level.dimension() != Level.OVERWORLD) {
			wasNight = isNight(level);
			wasThundering = level.isThundering();
			return;
		}

		boolean night = isNight(level);
		boolean thundering = level.isThundering();
		if (night && !wasNight) {
			if (!sleptThisNight) {
				nightsWithoutSleep++;
			}
			sleptThisNight = false;
			if (SleepReminderConfig.PHANTOMS.get() && nightsWithoutSleep >= SleepReminderConfig.PHANTOM_NIGHTS.get()) {
				remind(minecraft, Component.translatable("message.esad.sleep.phantoms", nightsWithoutSleep).withStyle(ChatFormatting.RED));
			} else if (SleepReminderConfig.NIGHT.get()) {
				remind(minecraft, Component.translatable("message.esad.sleep.night").withStyle(ChatFormatting.GOLD));
			}
		} else if (thundering && !wasThundering && !night && SleepReminderConfig.THUNDER.get()) {
			remind(minecraft, Component.translatable("message.esad.sleep.thunder").withStyle(ChatFormatting.GOLD));
		}
		wasNight = night;
		wasThundering = thundering;
	}

	private static boolean isNight(ClientLevel level) {
		long time = Math.floorMod(level.getOverworldClockTime(), 24000L);
		return time >= SLEEP_START && time < SLEEP_END;
	}

	private static void remind(Minecraft minecraft, Component message) {
		if (minecraft.player == null) {
			return;
		}
		if (SleepReminderConfig.CHAT.get()) {
			minecraft.player.sendSystemMessage(message);
		} else {
			minecraft.player.sendOverlayMessage(message);
		}
		if (SleepReminderConfig.SOUND.get()) {
			minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BELL, 1.0f));
		}
	}
}
