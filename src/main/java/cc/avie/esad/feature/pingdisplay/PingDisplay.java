package cc.avie.esad.feature.pingdisplay;

import cc.avie.esad.config.EsadConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * Ping display from the Ping Display mod: the ping as colored number in the tab list instead of the
 * signal bars, and optionally behind the name tags of players.
 */
public final class PingDisplay {
	/** Cached, the mod list does not change while the game is running. */
	private static final boolean ACTIVE = !FabricLoader.getInstance().isModLoaded("ping-display");

	private PingDisplay() {
	}

	/** False when the standalone Ping Display mod is installed, so the ping is not shown twice. */
	public static boolean isActive() {
		return ACTIVE;
	}

	public static boolean showInTabList() {
		return ACTIVE && EsadConfig.PING_ENABLED.get() && EsadConfig.PING_TAB_LIST.get();
	}

	/** Draws the ping right aligned in the space of the vanilla signal bars. */
	public static void renderPingText(Minecraft minecraft, GuiGraphicsExtractor graphics, int width, int x, int y, PlayerInfo info) {
		int ping = info.getLatency();
		String text = pingText(ping);
		Font font = minecraft.font;
		graphics.text(font, text, x + width - font.width(text), y, pingColor(ping), EsadConfig.PING_TEXT_SHADOW.get());
	}

	public static String pingText(int ping) {
		if (ping < 0) {
			return EsadConfig.PING_UNKNOWN_TEXT.get();
		}
		return EsadConfig.PING_SHOW_MS.get() ? ping + "ms" : String.valueOf(ping);
	}

	public static int pingColor(int ping) {
		if (ping < 0) {
			return EsadConfig.PING_COLOR_UNKNOWN.get();
		}
		int good = EsadConfig.PING_GOOD_BELOW.get();
		int ok = Math.max(good + 1, EsadConfig.PING_OK_BELOW.get());
		int bad = Math.max(ok + 1, EsadConfig.PING_BAD_BELOW.get());

		if (EsadConfig.PING_COLOR_MODE.get() == PingColorMode.GRADIENT) {
			if (ping <= good) {
				return EsadConfig.PING_COLOR_GOOD.get();
			}
			if (ping <= ok) {
				return lerp(ping, good, ok, EsadConfig.PING_COLOR_GOOD.get(), EsadConfig.PING_COLOR_OK.get());
			}
			if (ping <= bad) {
				return lerp(ping, ok, bad, EsadConfig.PING_COLOR_OK.get(), EsadConfig.PING_COLOR_BAD.get());
			}
			return lerp(ping, bad, bad * 2, EsadConfig.PING_COLOR_BAD.get(), EsadConfig.PING_COLOR_TERRIBLE.get());
		}

		if (ping < good) {
			return EsadConfig.PING_COLOR_GOOD.get();
		}
		if (ping < ok) {
			return EsadConfig.PING_COLOR_OK.get();
		}
		if (ping < bad) {
			return EsadConfig.PING_COLOR_BAD.get();
		}
		return EsadConfig.PING_COLOR_TERRIBLE.get();
	}

	/** Blends every ARGB channel separately. */
	private static int lerp(int ping, int from, int to, int fromColor, int toColor) {
		float delta = Math.min(1f, (ping - from) / (float) (to - from));
		int color = 0;
		for (int shift = 0; shift <= 24; shift += 8) {
			int a = fromColor >>> shift & 0xFF;
			int b = toColor >>> shift & 0xFF;
			color |= Math.round(a + (b - a) * delta) << shift;
		}
		return color;
	}

	/** The name tag with " (42ms)" behind it, or null to keep the original. */
	public static @Nullable Component nametagWithPing(Entity entity, @Nullable Component nameTag) {
		if (!ACTIVE || !EsadConfig.PING_ENABLED.get() || !EsadConfig.PING_NAMETAG.get()
			|| !(entity instanceof AbstractClientPlayer player) || nameTag == null) {
			return null;
		}
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) {
			return null;
		}
		PlayerInfo info = connection.getPlayerInfo(player.getUUID());
		if (info == null) {
			return null;
		}

		int ping = info.getLatency();
		int color = pingColor(ping) & 0xFFFFFF;
		int bracketColor = EsadConfig.PING_BRACKET_COLOR.get() & 0xFFFFFF;
		return nameTag.copy()
			.append(Component.literal(" (").withStyle(style -> style.withColor(bracketColor)))
			.append(Component.literal(pingText(ping)).withStyle(style -> style.withColor(color)))
			.append(Component.literal(")").withStyle(style -> style.withColor(bracketColor)));
	}
}
