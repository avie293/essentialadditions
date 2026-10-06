package cc.avie.esad.feature.hud;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.config.HudStyle;
import cc.avie.esad.config.KeystrokesConfig;
import me.avie29.tabbylib.api.HudPreview;
import me.avie29.tabbylib.api.TabbyLibApi;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Keystrokes overlay: movement keys, jump and mouse buttons with clicks per second. Shows the keys the
 * player really bound, so it also works with other key layouts.
 */
public final class Keystrokes {
	private static final int KEY = 22;
	private static final int GAP = 2;
	private static final int WIDTH = KEY * 3 + GAP * 2;
	private static final int SPACE_HEIGHT = 10;
	private static final int MOUSE_HEIGHT = 18;

	private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
	private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();
	private static boolean leftWasDown;
	private static boolean rightWasDown;

	private Keystrokes() {
	}

	public static void register() {
		HudElementRegistry.addLast(EssentialAdditions.id("keystrokes"), (graphics, deltaTracker) -> render(graphics));
	}

	private static void render(GuiGraphicsExtractor graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		trackClicks(minecraft.options);
		if (!KeystrokesConfig.ENABLED.get() || minecraft.player == null || TabbyLibApi.isHudEditorOpen()) {
			return;
		}
		int[] size = size(false);
		KeystrokesConfig.STYLE.push(graphics, size[0], size[1], false);
		draw(graphics, size, false);
		graphics.pose().popMatrix();
	}

	public static HudPreview preview() {
		return KeystrokesConfig.STYLE.preview(() -> size(true), (graphics, pending) -> draw(graphics, size(true), true));
	}

	/** Counts presses once per frame, which catches every click at normal frame rates. */
	private static void trackClicks(Options options) {
		long now = Util.getMillis();
		boolean left = options.keyAttack.isDown();
		boolean right = options.keyUse.isDown();
		if (left && !leftWasDown) {
			LEFT_CLICKS.addLast(now);
		}
		if (right && !rightWasDown) {
			RIGHT_CLICKS.addLast(now);
		}
		leftWasDown = left;
		rightWasDown = right;
		while (!LEFT_CLICKS.isEmpty() && now - LEFT_CLICKS.peekFirst() > 1000) {
			LEFT_CLICKS.removeFirst();
		}
		while (!RIGHT_CLICKS.isEmpty() && now - RIGHT_CLICKS.peekFirst() > 1000) {
			RIGHT_CLICKS.removeFirst();
		}
	}

	private static int[] size(boolean pending) {
		int height = KEY * 2 + GAP;
		if (HudStyle.value(KeystrokesConfig.MOUSE, pending)) {
			height += GAP + MOUSE_HEIGHT;
		}
		if (HudStyle.value(KeystrokesConfig.SPACE, pending)) {
			height += GAP + SPACE_HEIGHT;
		}
		return new int[] {WIDTH + 4, height + 4};
	}

	private static void draw(GuiGraphicsExtractor graphics, int[] size, boolean pending) {
		Options options = Minecraft.getInstance().options;
		KeystrokesConfig.STYLE.drawBackground(graphics, size[0], size[1], pending);
		int x = 2;
		int y = 2;
		key(graphics, options.keyUp, x + KEY + GAP, y, KEY, KEY, null, pending);
		y += KEY + GAP;
		key(graphics, options.keyLeft, x, y, KEY, KEY, null, pending);
		key(graphics, options.keyDown, x + KEY + GAP, y, KEY, KEY, null, pending);
		key(graphics, options.keyRight, x + (KEY + GAP) * 2, y, KEY, KEY, null, pending);
		y += KEY + GAP;
		if (HudStyle.value(KeystrokesConfig.MOUSE, pending)) {
			int half = (WIDTH - GAP) / 2;
			boolean cps = HudStyle.value(KeystrokesConfig.CPS, pending);
			key(graphics, options.keyAttack, x, y, half, MOUSE_HEIGHT,
				cps ? Component.translatable("hud.esad.keystrokes.cps", LEFT_CLICKS.size()) : Component.translatable("hud.esad.keystrokes.lmb"), pending);
			key(graphics, options.keyUse, x + half + GAP, y, WIDTH - half - GAP, MOUSE_HEIGHT,
				cps ? Component.translatable("hud.esad.keystrokes.cps", RIGHT_CLICKS.size()) : Component.translatable("hud.esad.keystrokes.rmb"), pending);
			y += MOUSE_HEIGHT + GAP;
		}
		if (HudStyle.value(KeystrokesConfig.SPACE, pending)) {
			boolean down = options.keyJump.isDown();
			HudStyle.box(graphics, x, y, WIDTH, SPACE_HEIGHT, HudStyle.value(down ? KeystrokesConfig.PRESSED_COLOR : KeystrokesConfig.KEY_COLOR, pending));
			int line = HudStyle.value(down ? KeystrokesConfig.PRESSED_TEXT_COLOR : KeystrokesConfig.TEXT_COLOR, pending);
			graphics.fill(x + WIDTH / 2 - 12, y + SPACE_HEIGHT / 2, x + WIDTH / 2 + 12, y + SPACE_HEIGHT / 2 + 1, line);
		}
	}

	private static void key(GuiGraphicsExtractor graphics, KeyMapping mapping, int x, int y, int width, int height,
							@Nullable Component label, boolean pending) {
		Font font = Minecraft.getInstance().font;
		boolean down = mapping.isDown();
		HudStyle.box(graphics, x, y, width, height, HudStyle.value(down ? KeystrokesConfig.PRESSED_COLOR : KeystrokesConfig.KEY_COLOR, pending));
		Component text = label != null ? label : mapping.getTranslatedKeyMessage();
		String plain = font.plainSubstrByWidth(text.getString(), width - 2);
		int color = HudStyle.value(down ? KeystrokesConfig.PRESSED_TEXT_COLOR : KeystrokesConfig.TEXT_COLOR, pending);
		graphics.text(font, plain, x + (width - font.width(plain)) / 2, y + (height - 8) / 2, color,
			KeystrokesConfig.STYLE.shadow(pending) && !down);
	}
}
