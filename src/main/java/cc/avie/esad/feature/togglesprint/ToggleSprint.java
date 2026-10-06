package cc.avie.esad.feature.togglesprint;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.compat.ScreenCompat;
import cc.avie.esad.config.EsadConfig;
import cc.avie.esad.config.HudStyle;
import cc.avie.esad.config.ToggleSprintConfig;
import com.mojang.blaze3d.platform.InputConstants;
import me.avie29.tabbylib.api.HudPreview;
import me.avie29.tabbylib.api.TabbyLibApi;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Toggle sprint and toggle sneak with own keys: one press keeps sprinting or sneaking until the next press.
 * Works by holding the vanilla sprint / sneak key down, so servers see normal movement. Optional status text.
 */
public final class ToggleSprint {
	/** Unbound by default, so they do not take keys other mods or players already use. */
	public static final KeyMapping SPRINT_KEY = new KeyMapping("key.esad.toggle_sprint", InputConstants.UNKNOWN.getValue(), EssentialAdditions.KEY_CATEGORY);
	public static final KeyMapping SNEAK_KEY = new KeyMapping("key.esad.toggle_sneak", InputConstants.UNKNOWN.getValue(), EssentialAdditions.KEY_CATEGORY);

	private static final int PADDING = 3;

	private static boolean sprintToggled;
	private static boolean sneakToggled;
	private static boolean holding;
	private static boolean loaded;

	private ToggleSprint() {
	}

	public static void register() {
		KeyMappingHelper.registerKeyMapping(SPRINT_KEY);
		KeyMappingHelper.registerKeyMapping(SNEAK_KEY);
		ClientTickEvents.END_CLIENT_TICK.register(ToggleSprint::tick);
		HudElementRegistry.addLast(EssentialAdditions.id("toggle_sprint"), (graphics, deltaTracker) -> render(graphics));
	}

	private static void tick(Minecraft minecraft) {
		if (!loaded && EsadConfig.CONFIG != null) {
			loaded = true;
			if (ToggleSprintConfig.REMEMBER.get()) {
				sprintToggled = ToggleSprintConfig.SPRINT_STATE.get();
				sneakToggled = ToggleSprintConfig.SNEAK_STATE.get();
			}
		}
		boolean changed = false;
		while (SPRINT_KEY.consumeClick()) {
			sprintToggled = !sprintToggled;
			changed = true;
		}
		while (SNEAK_KEY.consumeClick()) {
			sneakToggled = !sneakToggled;
			changed = true;
		}
		if (changed && ToggleSprintConfig.REMEMBER.get()) {
			ToggleSprintConfig.SPRINT_STATE.set(sprintToggled);
			ToggleSprintConfig.SNEAK_STATE.set(sneakToggled);
			EsadConfig.CONFIG.save();
		}

		boolean active = ToggleSprintConfig.ENABLED.get() && minecraft.player != null && ScreenCompat.currentScreen() == null;
		boolean sprint = active && (sprintToggled || ToggleSprintConfig.ALWAYS_SPRINT.get());
		boolean sneak = active && sneakToggled;
		if (sprint) {
			minecraft.options.keySprint.setDown(true);
		}
		if (sneak) {
			minecraft.options.keyShift.setDown(true);
		}
		boolean nowHolding = sprint || sneak;
		if (holding && !nowHolding && ScreenCompat.currentScreen() == null) {
			// Back to the real state of the physical keys
			KeyMapping.setAll();
		}
		holding = nowHolding;
	}

	// ---------------------------------------------------------------- status text

	private static void render(GuiGraphicsExtractor graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!ToggleSprintConfig.ENABLED.get() || !ToggleSprintConfig.HUD.get() || minecraft.player == null || TabbyLibApi.isHudEditorOpen()) {
			return;
		}
		List<Component> lines = lines(minecraft.player, false);
		if (lines.isEmpty()) {
			return;
		}
		int[] size = size(minecraft.font, lines);
		ToggleSprintConfig.STYLE.push(graphics, size[0], size[1], false);
		draw(graphics, lines, size, false);
		graphics.pose().popMatrix();
	}

	public static HudPreview preview() {
		return ToggleSprintConfig.STYLE.preview(() -> size(Minecraft.getInstance().font, previewLines()),
			(graphics, pending) -> {
				List<Component> lines = previewLines();
				draw(graphics, lines, size(Minecraft.getInstance().font, lines), true);
			});
	}

	private static List<Component> previewLines() {
		return List.of(Component.translatable("hud.esad.toggle_sprint.sprint_toggled"));
	}

	private static List<Component> lines(LocalPlayer player, boolean pending) {
		List<Component> lines = new ArrayList<>();
		if (sprintToggled) {
			lines.add(Component.translatable("hud.esad.toggle_sprint.sprint_toggled"));
		} else if (HudStyle.value(ToggleSprintConfig.ALWAYS_SPRINT, pending)) {
			lines.add(Component.translatable("hud.esad.toggle_sprint.sprint_always"));
		} else if (HudStyle.value(ToggleSprintConfig.HUD_VANILLA, pending) && player.isSprinting()) {
			lines.add(Component.translatable("hud.esad.toggle_sprint.sprint"));
		}
		if (sneakToggled) {
			lines.add(Component.translatable("hud.esad.toggle_sprint.sneak_toggled"));
		} else if (HudStyle.value(ToggleSprintConfig.HUD_VANILLA, pending) && player.isShiftKeyDown()) {
			lines.add(Component.translatable("hud.esad.toggle_sprint.sneak"));
		}
		return lines;
	}

	private static int[] size(Font font, List<Component> lines) {
		int width = 0;
		for (Component line : lines) {
			width = Math.max(width, font.width(line));
		}
		return new int[] {width + PADDING * 2, lines.size() * 10 + PADDING * 2 - 1};
	}

	private static void draw(GuiGraphicsExtractor graphics, List<Component> lines, int[] size, boolean pending) {
		Font font = Minecraft.getInstance().font;
		ToggleSprintConfig.STYLE.drawBackground(graphics, size[0], size[1], pending);
		int color = HudStyle.value(ToggleSprintConfig.HUD_COLOR, pending);
		int y = PADDING + 1;
		for (Component line : lines) {
			graphics.text(font, line, PADDING, y, color, ToggleSprintConfig.STYLE.shadow(pending));
			y += 10;
		}
	}
}
