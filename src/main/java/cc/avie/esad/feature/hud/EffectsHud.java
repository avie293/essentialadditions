package cc.avie.esad.feature.hud;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.config.EffectsHudConfig;
import cc.avie.esad.config.HudStyle;
import me.avie29.tabbylib.api.HudPreview;
import me.avie29.tabbylib.api.TabbyLibApi;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Active status effects as a list with icon, name, level and remaining time. */
public final class EffectsHud {
	private static final int PADDING = 3;
	private static final int ICON = 18;
	private static final int ROW = 20;

	private EffectsHud() {
	}

	public static void register() {
		HudElementRegistry.addLast(EssentialAdditions.id("effects_hud"), (graphics, deltaTracker) -> render(graphics));
	}

	/** Whether the vanilla effect icons at the top right are hidden, read by the version specific mixin. */
	public static boolean hidesVanillaIcons() {
		return EffectsHudConfig.ENABLED.get() && EffectsHudConfig.HIDE_VANILLA.get();
	}

	private static void render(GuiGraphicsExtractor graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!EffectsHudConfig.ENABLED.get() || minecraft.player == null || TabbyLibApi.isHudEditorOpen()) {
			return;
		}
		List<MobEffectInstance> effects = effects(minecraft.player, false);
		if (effects.isEmpty()) {
			return;
		}
		int[] size = size(minecraft.font, effects, false);
		EffectsHudConfig.STYLE.push(graphics, size[0], size[1], false);
		draw(graphics, effects, size, false);
		graphics.pose().popMatrix();
	}

	public static HudPreview preview() {
		return EffectsHudConfig.STYLE.preview(() -> size(Minecraft.getInstance().font, previewEffects(), true),
			(graphics, pending) -> {
				List<MobEffectInstance> effects = previewEffects();
				draw(graphics, effects, size(Minecraft.getInstance().font, effects, true), true);
			});
	}

	private static List<MobEffectInstance> previewEffects() {
		LocalPlayer player = Minecraft.getInstance().player;
		List<MobEffectInstance> effects = player != null ? effects(player, true) : List.of();
		if (!effects.isEmpty()) {
			return effects;
		}
		return List.of(new MobEffectInstance(MobEffects.SPEED, 20 * 95, 1), new MobEffectInstance(MobEffects.POISON, 20 * 8));
	}

	private static List<MobEffectInstance> effects(LocalPlayer player, boolean pending) {
		List<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());
		effects.removeIf(effect -> !effect.showIcon());
		switch (HudStyle.value(EffectsHudConfig.SORTING, pending)) {
			case DURATION -> effects.sort(Comparator.comparingInt(effect -> effect.isInfiniteDuration() ? Integer.MAX_VALUE : effect.getDuration()));
			case CATEGORY -> effects.sort(Comparator.comparingInt(effect -> effect.getEffect().value().isBeneficial() ? 0 : 1));
			case NONE -> {
			}
		}
		return effects;
	}

	private static Component text(MobEffectInstance effect, boolean pending) {
		MutableComponent text = Component.empty();
		if (HudStyle.value(EffectsHudConfig.NAMES, pending)) {
			text.append(effect.getEffect().value().getDisplayName());
			if (effect.getAmplifier() > 0) {
				text.append(" ").append(Component.translatable("potion.potency." + effect.getAmplifier()));
			}
		}
		if (HudStyle.value(EffectsHudConfig.DURATION, pending)) {
			if (!text.getString().isEmpty()) {
				text.append(" ");
			}
			Minecraft minecraft = Minecraft.getInstance();
			float tickRate = minecraft.level != null ? minecraft.level.tickRateManager().tickrate() : 20;
			text.append(MobEffectUtil.formatDuration(effect, 1.0f, tickRate));
		}
		return text;
	}

	private static int[] size(Font font, List<MobEffectInstance> effects, boolean pending) {
		int width = 0;
		for (MobEffectInstance effect : effects) {
			width = Math.max(width, font.width(text(effect, pending)));
		}
		int textWidth = width > 0 ? width + 4 : 0;
		return new int[] {ICON + textWidth + PADDING * 2, effects.size() * ROW - (ROW - ICON) + PADDING * 2};
	}

	private static void draw(GuiGraphicsExtractor graphics, List<MobEffectInstance> effects, int[] size, boolean pending) {
		Font font = Minecraft.getInstance().font;
		EffectsHudConfig.STYLE.drawBackground(graphics, size[0], size[1], pending);
		boolean shadow = EffectsHudConfig.STYLE.shadow(pending);
		boolean categoryColors = HudStyle.value(EffectsHudConfig.CATEGORY_COLORS, pending);
		int blinkTicks = HudStyle.value(EffectsHudConfig.BLINK_SECONDS, pending) * 20;
		boolean blinkOff = Util.getMillis() / 250 % 2 == 0;

		int y = PADDING;
		for (MobEffectInstance effect : effects) {
			boolean endingSoon = !effect.isInfiniteDuration() && effect.getDuration() <= blinkTicks;
			if (!(endingSoon && blinkOff)) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite(effect.getEffect()), PADDING, y, ICON, ICON);
			}
			int color = 0xFFFFFFFF;
			if (categoryColors) {
				MobEffectCategory category = effect.getEffect().value().getCategory();
				color = category == MobEffectCategory.HARMFUL ? 0xFFFF6B6B : category == MobEffectCategory.BENEFICIAL ? 0xFF8CE68C : 0xFFFFFFFF;
			}
			graphics.text(font, text(effect, pending), PADDING + ICON + 4, y + 5, color, shadow);
			y += ROW;
		}
	}

	/** The vanilla icon of an effect, e.g. "minecraft:mob_effect/speed". */
	private static Identifier sprite(Holder<MobEffect> effect) {
		Identifier id = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
		return id != null ? id.withPrefix("mob_effect/") : Identifier.withDefaultNamespace("mob_effect/speed");
	}
}
