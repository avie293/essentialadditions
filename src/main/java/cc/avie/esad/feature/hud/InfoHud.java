package cc.avie.esad.feature.hud;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.config.HudStyle;
import cc.avie.esad.config.InfoHudConfig;
import me.avie29.tabbylib.api.HudPreview;
import me.avie29.tabbylib.api.TabbyLibApi;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Movable box with coordinates, facing, biome, FPS, ping and more. Every line can be turned off. */
public final class InfoHud {
	private static final int PADDING = 4;
	private static final int LINE_HEIGHT = 10;
	private static final DateTimeFormatter CLOCK_24 = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter CLOCK_12 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

	private InfoHud() {
	}

	public static void register() {
		HudElementRegistry.addLast(EssentialAdditions.id("info_hud"), (graphics, deltaTracker) -> render(graphics));
	}

	private static void render(GuiGraphicsExtractor graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!InfoHudConfig.ENABLED.get() || minecraft.player == null || TabbyLibApi.isHudEditorOpen()) {
			return;
		}
		List<Line> lines = lines(false);
		if (lines.isEmpty()) {
			return;
		}
		int[] size = size(minecraft.font, lines);
		InfoHudConfig.STYLE.push(graphics, size[0], size[1], false);
		draw(graphics, lines, size, false);
		graphics.pose().popMatrix();
	}

	public static HudPreview preview() {
		return InfoHudConfig.STYLE.preview(() -> size(Minecraft.getInstance().font, lines(true)),
			(graphics, pending) -> {
				List<Line> lines = lines(true);
				draw(graphics, lines, size(Minecraft.getInstance().font, lines), true);
			});
	}

	private static void draw(GuiGraphicsExtractor graphics, List<Line> lines, int[] size, boolean pending) {
		Font font = Minecraft.getInstance().font;
		InfoHudConfig.STYLE.drawBackground(graphics, size[0], size[1], pending);
		boolean shadow = InfoHudConfig.STYLE.shadow(pending);
		int labelColor = HudStyle.value(InfoHudConfig.LABEL_COLOR, pending);
		int valueColor = HudStyle.value(InfoHudConfig.VALUE_COLOR, pending);
		int y = PADDING + 1;
		for (Line line : lines) {
			graphics.text(font, line.label(), PADDING, y, labelColor, shadow);
			graphics.text(font, line.value(), PADDING + font.width(line.label()) + 4, y, valueColor, shadow);
			y += LINE_HEIGHT;
		}
	}

	private static int[] size(Font font, List<Line> lines) {
		int width = 0;
		for (Line line : lines) {
			width = Math.max(width, font.width(line.label()) + 4 + font.width(line.value()));
		}
		return new int[] {width + PADDING * 2, lines.size() * LINE_HEIGHT + PADDING * 2 - 1};
	}

	private static List<Line> lines(boolean pending) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		List<Line> lines = new ArrayList<>();
		if (player == null) {
			// Preview in the menu without a world
			lines.add(new Line(Component.translatable("hud.esad.info.coordinates"), Component.literal("128 / 64 / -256")));
			return lines;
		}
		Level level = player.level();
		BlockPos pos = player.blockPosition();

		if (HudStyle.value(InfoHudConfig.COORDINATES, pending)) {
			String coordinates = HudStyle.value(InfoHudConfig.DECIMALS, pending)
				? String.format(Locale.ROOT, "%.1f / %.1f / %.1f", player.getX(), player.getY(), player.getZ())
				: pos.getX() + " / " + pos.getY() + " / " + pos.getZ();
			lines.add(new Line(Component.translatable("hud.esad.info.coordinates"), Component.literal(coordinates)));
		}
		if (HudStyle.value(InfoHudConfig.OTHER_DIMENSION, pending)) {
			if (level.dimension() == Level.OVERWORLD) {
				lines.add(new Line(Component.translatable("hud.esad.info.nether"),
					Component.literal(Math.floorDiv(pos.getX(), 8) + " / " + Math.floorDiv(pos.getZ(), 8))));
			} else if (level.dimension() == Level.NETHER) {
				lines.add(new Line(Component.translatable("hud.esad.info.overworld"),
					Component.literal(pos.getX() * 8 + " / " + pos.getZ() * 8)));
			}
		}
		if (HudStyle.value(InfoHudConfig.FACING, pending)) {
			Direction direction = player.getDirection();
			String axis = switch (direction) {
				case NORTH -> "-Z";
				case SOUTH -> "+Z";
				case WEST -> "-X";
				case EAST -> "+X";
				default -> "";
			};
			lines.add(new Line(Component.translatable("hud.esad.info.facing"),
				Component.translatable("hud.esad.info.direction." + direction.getName()).append(" (" + axis + ")")));
		}
		if (HudStyle.value(InfoHudConfig.BIOME, pending)) {
			Component biome = level.getBiome(pos).unwrapKey()
				.map(key -> (Component) Component.translatable(key.identifier().toLanguageKey("biome")))
				.orElse(Component.literal("?"));
			lines.add(new Line(Component.translatable("hud.esad.info.biome"), biome));
		}
		if (HudStyle.value(InfoHudConfig.LIGHT, pending)) {
			int block = level.getBrightness(LightLayer.BLOCK, pos);
			int sky = level.getBrightness(LightLayer.SKY, pos);
			lines.add(new Line(Component.translatable("hud.esad.info.light"),
				Component.translatable("hud.esad.info.light.value", block, sky)));
		}
		if (HudStyle.value(InfoHudConfig.FPS, pending)) {
			lines.add(new Line(Component.translatable("hud.esad.info.fps"), Component.literal(Integer.toString(minecraft.getFps()))));
		}
		if (HudStyle.value(InfoHudConfig.PING, pending) && minecraft.getConnection() != null) {
			PlayerInfo info = minecraft.getConnection().getPlayerInfo(player.getUUID());
			int ping = info != null ? info.getLatency() : 0;
			lines.add(new Line(Component.translatable("hud.esad.info.ping"), Component.literal(ping + "ms")));
		}
		if (HudStyle.value(InfoHudConfig.CLOCK, pending)) {
			DateTimeFormatter format = HudStyle.value(InfoHudConfig.TWENTY_FOUR_HOURS, pending) ? CLOCK_24 : CLOCK_12;
			lines.add(new Line(Component.translatable("hud.esad.info.clock"), Component.literal(LocalTime.now().format(format))));
		}
		return lines;
	}

	private record Line(Component label, Component value) {
	}
}
