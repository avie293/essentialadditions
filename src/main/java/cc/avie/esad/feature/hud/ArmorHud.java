package cc.avie.esad.feature.hud;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.config.ArmorHudConfig;
import cc.avie.esad.config.HudStyle;
import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.HudPreview;
import me.avie29.tabbylib.api.TabbyLibApi;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Armor and held items with their durability. Items below the warning limit are drawn in red and an optional
 * "Low durability!" text blinks next to them.
 */
public final class ArmorHud {
	private static final int PADDING = 3;
	private static final int ICON = 16;
	private static final int GAP = 2;
	private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private ArmorHud() {
	}

	public static void register() {
		HudElementRegistry.addLast(EssentialAdditions.id("armor_hud"), (graphics, deltaTracker) -> render(graphics));
	}

	private static void render(GuiGraphicsExtractor graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!ArmorHudConfig.ENABLED.get() || minecraft.player == null || TabbyLibApi.isHudEditorOpen()) {
			return;
		}
		List<Entry> entries = entries(minecraft.player, false);
		if (entries.isEmpty()) {
			return;
		}
		int[] size = size(minecraft.font, entries, false);
		ArmorHudConfig.STYLE.push(graphics, size[0], size[1], false);
		draw(graphics, entries, size, false);
		graphics.pose().popMatrix();
	}

	public static HudPreview preview() {
		return ArmorHudConfig.STYLE.preview(() -> size(Minecraft.getInstance().font, previewEntries(), true),
			(graphics, pending) -> {
				List<Entry> entries = previewEntries();
				draw(graphics, entries, size(Minecraft.getInstance().font, entries, true), true);
			});
	}

	/** The real items when in a world, example items otherwise (and when nothing is worn). */
	private static List<Entry> previewEntries() {
		LocalPlayer player = Minecraft.getInstance().player;
		List<Entry> entries = player != null ? entries(player, true) : List.of();
		if (!entries.isEmpty() || !FeatureIcon.itemsReady()) {
			// Without a world no item stacks can be created, the preview stays empty
			return entries;
		}
		ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
		helmet.setDamageValue(300);
		return List.of(entry(helmet, 1, true), entry(new ItemStack(Items.IRON_CHESTPLATE), 1, true),
			entry(new ItemStack(Items.IRON_LEGGINGS), 1, true), entry(new ItemStack(Items.IRON_BOOTS), 1, true));
	}

	private static List<Entry> entries(LocalPlayer player, boolean pending) {
		List<Entry> entries = new ArrayList<>();
		for (EquipmentSlot slot : ARMOR) {
			ItemStack stack = player.getItemBySlot(slot);
			if (!stack.isEmpty()) {
				entries.add(entry(stack, 1, pending));
			}
		}
		if (HudStyle.value(ArmorHudConfig.HANDS, pending)) {
			for (ItemStack stack : new ItemStack[] {player.getMainHandItem(), player.getOffhandItem()}) {
				if (!stack.isEmpty()) {
					int count = HudStyle.value(ArmorHudConfig.ITEM_COUNT, pending) && !stack.isDamageableItem()
						? countInInventory(player, stack) : stack.getCount();
					entries.add(entry(stack, count, pending));
				}
			}
		}
		return entries;
	}

	private static int countInInventory(LocalPlayer player, ItemStack stack) {
		int count = 0;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack other = player.getInventory().getItem(i);
			if (ItemStack.isSameItemSameComponents(stack, other)) {
				count += other.getCount();
			}
		}
		return count;
	}

	private static Entry entry(ItemStack stack, int count, boolean pending) {
		Component text = Component.empty();
		boolean low = false;
		if (stack.isDamageableItem()) {
			int max = stack.getMaxDamage();
			int left = max - stack.getDamageValue();
			low = left * 100 < max * HudStyle.value(ArmorHudConfig.WARNING_PERCENT, pending);
			text = switch (HudStyle.value(ArmorHudConfig.DURABILITY, pending)) {
				case NUMBER -> Component.literal(Integer.toString(left));
				case PERCENT -> Component.literal(left * 100 / Math.max(1, max) + "%");
				case NONE -> Component.empty();
			};
		} else if (count > 1) {
			text = Component.literal(Integer.toString(count));
		}
		return new Entry(stack, text, low);
	}

	private static int[] size(Font font, List<Entry> entries, boolean pending) {
		int textWidth = 0;
		for (Entry entry : entries) {
			textWidth = Math.max(textWidth, font.width(entry.text()));
		}
		int cell = textWidth > 0 ? ICON + GAP + textWidth : ICON;
		if (HudStyle.value(ArmorHudConfig.LAYOUT, pending) == ArmorHudConfig.Layout.HORIZONTAL) {
			return new int[] {entries.size() * (cell + 4) - 4 + PADDING * 2, ICON + PADDING * 2};
		}
		return new int[] {cell + PADDING * 2, entries.size() * (ICON + GAP) - GAP + PADDING * 2};
	}

	private static void draw(GuiGraphicsExtractor graphics, List<Entry> entries, int[] size, boolean pending) {
		Font font = Minecraft.getInstance().font;
		ArmorHudConfig.STYLE.drawBackground(graphics, size[0], size[1], pending);
		boolean shadow = ArmorHudConfig.STYLE.shadow(pending);
		boolean horizontal = HudStyle.value(ArmorHudConfig.LAYOUT, pending) == ArmorHudConfig.Layout.HORIZONTAL;
		boolean bar = HudStyle.value(ArmorHudConfig.DURABILITY_BAR, pending);
		// Blinks twice per second
		boolean blink = Util.getMillis() / 500 % 2 == 0;
		boolean anyLow = false;

		int x = PADDING;
		int y = PADDING;
		for (Entry entry : entries) {
			graphics.item(entry.stack(), x, y);
			if (bar && entry.stack().isDamageableItem()) {
				// Only the bar, the count is part of the text
				graphics.itemDecorations(font, entry.stack(), x, y, "");
			}
			int color = entry.low() ? 0xFFFF5555 : 0xFFFFFFFF;
			graphics.text(font, entry.text(), x + ICON + GAP, y + 4, color, shadow);
			anyLow |= entry.low();
			if (horizontal) {
				x += ICON + GAP + font.width(entry.text()) + 4;
			} else {
				y += ICON + GAP;
			}
		}

		if (anyLow && blink && HudStyle.value(ArmorHudConfig.WARNING_TEXT, pending)) {
			Component warning = Component.translatable("hud.esad.armor.low").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
			// Right aligned, the HUD usually sits at the right edge of the screen
			graphics.text(font, warning, size[0] - font.width(warning), -font.lineHeight - 1, 0xFFFF5555, true);
		}
	}

	private record Entry(ItemStack stack, Component text, boolean low) {
	}
}
