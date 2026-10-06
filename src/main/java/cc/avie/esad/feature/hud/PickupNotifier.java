package cc.avie.esad.feature.hud;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.config.HudStyle;
import cc.avie.esad.config.PickupNotifierConfig;
import cc.avie.esad.gui.FeatureIcon;
import me.avie29.tabbylib.api.HudPreview;
import me.avie29.tabbylib.api.TabbyLibApi;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * "+12 Coal" notifications for picked up items. Pickups of the same item are added up while the entry is shown,
 * old entries fade out.
 */
public final class PickupNotifier {
	private static final int PADDING = 3;
	private static final int ROW = 18;
	private static final long FADE_MILLIS = 500;

	private static final List<Entry> ENTRIES = new ArrayList<>();

	private PickupNotifier() {
	}

	public static void register() {
		HudElementRegistry.addLast(EssentialAdditions.id("pickup_notifier"), (graphics, deltaTracker) -> render(graphics));
	}

	/** Called by the mixin when the server says the local player picked up an item. */
	public static void onPickup(ItemStack stack, int amount) {
		if (!PickupNotifierConfig.ENABLED.get() || stack.isEmpty() || amount <= 0) {
			return;
		}
		long now = Util.getMillis();
		for (Entry entry : ENTRIES) {
			if (ItemStack.isSameItemSameComponents(entry.stack, stack)) {
				entry.count += amount;
				entry.time = now;
				ENTRIES.remove(entry);
				ENTRIES.addFirst(entry);
				return;
			}
		}
		ENTRIES.addFirst(new Entry(stack.copyWithCount(1), amount, now));
		while (ENTRIES.size() > 10) {
			ENTRIES.removeLast();
		}
	}

	private static void render(GuiGraphicsExtractor graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		long now = Util.getMillis();
		long duration = (long) (PickupNotifierConfig.DURATION.get() * 1000);
		ENTRIES.removeIf(entry -> now - entry.time > duration);
		if (!PickupNotifierConfig.ENABLED.get() || minecraft.player == null || ENTRIES.isEmpty() || TabbyLibApi.isHudEditorOpen()) {
			return;
		}
		List<Entry> shown = ENTRIES.subList(0, Math.min(ENTRIES.size(), PickupNotifierConfig.MAX_ENTRIES.get()));
		int[] size = size(minecraft.font, shown, false);
		PickupNotifierConfig.STYLE.push(graphics, size[0], size[1], false);
		draw(graphics, shown, size, now, duration, false);
		graphics.pose().popMatrix();
	}

	public static HudPreview preview() {
		return PickupNotifierConfig.STYLE.preview(() -> size(Minecraft.getInstance().font, previewEntries(), true),
			(graphics, pending) -> {
				List<Entry> entries = previewEntries();
				draw(graphics, entries, size(Minecraft.getInstance().font, entries, true), Util.getMillis(), Long.MAX_VALUE, true);
			});
	}

	private static List<Entry> previewEntries() {
		if (!FeatureIcon.itemsReady()) {
			// Without a world no item stacks can be created
			return List.of();
		}
		long now = Util.getMillis();
		return List.of(new Entry(new ItemStack(Items.COAL), 12, now), new Entry(new ItemStack(Items.DIAMOND), 2, now),
			new Entry(new ItemStack(Items.OAK_LOG), 32, now));
	}

	private static Component text(Entry entry, boolean pending) {
		Component name = entry.stack.getHoverName();
		LocalPlayer player = Minecraft.getInstance().player;
		if (HudStyle.value(PickupNotifierConfig.TOTAL, pending) && player != null) {
			int total = 0;
			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				ItemStack other = player.getInventory().getItem(i);
				if (ItemStack.isSameItemSameComponents(entry.stack, other)) {
					total += other.getCount();
				}
			}
			return Component.translatable("hud.esad.pickup.total", entry.count, name, total);
		}
		return Component.translatable("hud.esad.pickup", entry.count, name);
	}

	private static int[] size(Font font, List<Entry> entries, boolean pending) {
		int width = 0;
		for (Entry entry : entries) {
			width = Math.max(width, font.width(text(entry, pending)));
		}
		int icon = HudStyle.value(PickupNotifierConfig.ICONS, pending) ? 18 : 0;
		return new int[] {icon + width + PADDING * 2, entries.size() * ROW - 2 + PADDING * 2};
	}

	private static void draw(GuiGraphicsExtractor graphics, List<Entry> entries, int[] size, long now, long duration, boolean pending) {
		Font font = Minecraft.getInstance().font;
		PickupNotifierConfig.STYLE.drawBackground(graphics, size[0], size[1], pending);
		boolean icons = HudStyle.value(PickupNotifierConfig.ICONS, pending);
		boolean rarity = HudStyle.value(PickupNotifierConfig.RARITY_COLORS, pending);
		int y = PADDING;
		for (Entry entry : entries) {
			long left = duration - (now - entry.time);
			int alpha = left >= FADE_MILLIS ? 255 : (int) Math.max(16, 255 * left / FADE_MILLIS);
			// Entries are right aligned, so the HUD can sit at the right edge of the screen
			Component text = text(entry, pending);
			int textX = size[0] - PADDING - font.width(text);
			if (icons) {
				graphics.item(entry.stack, textX - 18, y);
			}
			TextColor rarityColor = rarity ? TextColor.fromLegacyFormat(entry.stack.getRarity().color()) : null;
			int color = (rarityColor != null ? rarityColor.getValue() : 0xFFFFFF) | alpha << 24;
			graphics.text(font, text, textX, y + 4, color, PickupNotifierConfig.STYLE.shadow(pending));
			y += ROW;
		}
	}

	private static final class Entry {
		private final ItemStack stack;
		private int count;
		private long time;

		Entry(ItemStack stack, int count, long time) {
			this.stack = stack;
			this.count = count;
			this.time = time;
		}
	}
}
