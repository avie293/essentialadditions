package cc.avie.esad.feature.sort;

import cc.avie.esad.EssentialAdditions;
import cc.avie.esad.config.InventorySortConfig;
import cc.avie.esad.mixin.ContainerScreenAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sorts the inventory or the container under the mouse with a key or a middle click. Everything happens with
 * normal inventory clicks, exactly like a player would do it by hand, so it works on every server.
 */
public final class InventorySort {
	public static final KeyMapping KEY = new KeyMapping("key.esad.sort_inventory", InputConstants.KEY_R, EssentialAdditions.KEY_CATEGORY);

	/** Slots 0 - 8 of the player inventory are the hotbar, 9 - 35 the main inventory. */
	private static final int HOTBAR_SIZE = 9;
	private static final int MAIN_END = 36;

	private InventorySort() {
	}

	public static void register() {
		KeyMappingHelper.registerKeyMapping(KEY);
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (!(screen instanceof AbstractContainerScreen<?> container) || screen instanceof CreativeModeInventoryScreen) {
				return;
			}
			ScreenMouseEvents.allowMouseClick(screen).register((current, event) -> {
				if (InventorySortConfig.ENABLED.get() && InventorySortConfig.MIDDLE_CLICK.get()
					&& event.button() == InputConstants.MOUSE_BUTTON_MIDDLE && hoveredSlot(container) != null) {
					sort(container);
					return false;
				}
				return true;
			});
		});
	}

	/**
	 * Called by the mixin at the start of AbstractContainerScreen.keyPressed. Text fields like the recipe book
	 * search get the key before, so typing there does not sort.
	 */
	public static boolean onKeyPressed(AbstractContainerScreen<?> screen, KeyEvent event) {
		if (!InventorySortConfig.ENABLED.get() || !KEY.matches(event)) {
			return false;
		}
		if (screen instanceof CreativeModeInventoryScreen creative) {
			// Only on the inventory tab, the other tabs are item lists and the search needs the key for typing
			if (!creative.isInventoryOpen()) {
				return false;
			}
			sortCreative();
			return true;
		}
		sort(screen);
		return true;
	}

	/**
	 * In creative mode the client may set its inventory slots directly, inventory clicks work differently there.
	 * So the sorted inventory is computed first and every changed slot is sent like the creative inventory does.
	 */
	private static void sortCreative() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.gameMode == null || !player.isCreative()) {
			return;
		}
		Inventory inventory = player.getInventory();
		List<Integer> indexes = new ArrayList<>();
		for (int i = InventorySortConfig.HOTBAR.get() ? 0 : HOTBAR_SIZE; i < MAIN_END; i++) {
			indexes.add(i);
		}

		List<ItemStack> stacks = new ArrayList<>();
		for (int index : indexes) {
			ItemStack stack = inventory.getItem(index);
			if (!stack.isEmpty()) {
				stacks.add(stack.copy());
			}
		}
		if (InventorySortConfig.MERGE.get()) {
			stacks = mergeStacks(stacks);
		}
		stacks.sort(comparator());

		for (int i = 0; i < indexes.size(); i++) {
			int index = indexes.get(i);
			ItemStack wanted = i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY;
			if (ItemStack.matches(inventory.getItem(index), wanted)) {
				continue;
			}
			inventory.setItem(index, wanted.copy());
			// Inventory slots 9 - 35 have the same number in the player menu, the hotbar is 36 - 44 there
			int menuSlot = index < HOTBAR_SIZE ? index + MAIN_END : index;
			minecraft.gameMode.handleCreativeModeItemAdd(wanted.copy(), menuSlot);
		}
		inventory.setChanged();
	}

	/** Combines stacks of the same item, as full as possible. */
	private static List<ItemStack> mergeStacks(List<ItemStack> stacks) {
		List<ItemStack> merged = new ArrayList<>();
		for (ItemStack stack : stacks) {
			int left = stack.getCount();
			for (ItemStack target : merged) {
				if (left <= 0) {
					break;
				}
				if (ItemStack.isSameItemSameComponents(target, stack) && target.getCount() < target.getMaxStackSize()) {
					int moved = Math.min(left, target.getMaxStackSize() - target.getCount());
					target.grow(moved);
					left -= moved;
				}
			}
			if (left > 0) {
				merged.add(stack.copyWithCount(left));
			}
		}
		return merged;
	}

	private static @Nullable Slot hoveredSlot(AbstractContainerScreen<?> screen) {
		return ((ContainerScreenAccessor) screen).esad$getHoveredSlot();
	}

	private static void sort(AbstractContainerScreen<?> screen) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		AbstractContainerMenu menu = screen.getMenu();
		// Only with an empty cursor, otherwise the clicks would move the held stack around
		if (player == null || minecraft.gameMode == null || !menu.getCarried().isEmpty()) {
			return;
		}
		List<Slot> slots = slotsToSort(menu, hoveredSlot(screen), player.getInventory());
		if (slots.size() < 2) {
			return;
		}
		Clicker clicker = slot -> minecraft.gameMode.handleContainerInput(menu.containerId, slot.index, 0, ContainerInput.PICKUP, player);
		if (InventorySortConfig.MERGE.get()) {
			merge(slots, clicker);
		}
		order(slots, clicker);
		// Something could not be placed (e.g. a slot that refuses the item): put it back into a free slot
		if (!menu.getCarried().isEmpty()) {
			for (Slot slot : slots) {
				if (!slot.hasItem()) {
					clicker.click(slot);
					break;
				}
			}
		}
	}

	/**
	 * The slots of the container under the mouse: the player inventory (without hotbar unless enabled) when the
	 * mouse is over it or nothing, otherwise all slots of the other container.
	 */
	private static List<Slot> slotsToSort(AbstractContainerMenu menu, @Nullable Slot hovered, Inventory inventory) {
		Container target = hovered != null && isSortable(menu, hovered.container, inventory) ? hovered.container : null;
		if (target == null && hovered == null) {
			// Nothing hovered: the opened container if there is one
			for (Slot slot : menu.slots) {
				if (slot.container != inventory && isSortable(menu, slot.container, inventory)) {
					target = slot.container;
					break;
				}
			}
		}
		if (target == null) {
			target = inventory;
		}
		List<Slot> slots = new ArrayList<>();
		for (Slot slot : menu.slots) {
			if (slot.container != target) {
				continue;
			}
			if (target == inventory) {
				int index = slot.getContainerSlot();
				boolean main = index >= HOTBAR_SIZE && index < MAIN_END;
				boolean hotbar = index < HOTBAR_SIZE && InventorySortConfig.HOTBAR.get();
				if (!main && !hotbar) {
					// Armor, off hand and crafting slots stay as they are
					continue;
				}
			}
			slots.add(slot);
		}
		return slots;
	}

	/**
	 * Only real storage is sorted: the player inventory and containers with at least one row (chests, barrels,
	 * shulker boxes, ...). Crafting grids, result slots and furnace slots are left alone.
	 */
	private static boolean isSortable(AbstractContainerMenu menu, Container container, Inventory inventory) {
		if (container == inventory) {
			return true;
		}
		int count = 0;
		for (Slot slot : menu.slots) {
			if (slot.container == container) {
				count++;
			}
		}
		return count >= 9 && !(container instanceof CraftingContainer);
	}

	/** Fills stacks that are not full with items of the same kind from later slots. */
	private static void merge(List<Slot> slots, Clicker clicker) {
		for (int i = 0; i < slots.size(); i++) {
			Slot target = slots.get(i);
			for (int j = i + 1; j < slots.size(); j++) {
				ItemStack stack = target.getItem();
				if (stack.isEmpty() || stack.getCount() >= stack.getMaxStackSize()) {
					break;
				}
				Slot source = slots.get(j);
				if (ItemStack.isSameItemSameComponents(stack, source.getItem())) {
					// Pick up, drop onto the target (the rest stays on the cursor), put the rest back
					clicker.click(source);
					clicker.click(target);
					clicker.click(source);
				}
			}
		}
	}

	/** Puts the stacks into the wanted order with swaps, one slot after the other. */
	private static void order(List<Slot> slots, Clicker clicker) {
		List<ItemStack> wanted = new ArrayList<>();
		for (Slot slot : slots) {
			if (slot.hasItem()) {
				wanted.add(slot.getItem().copy());
			}
		}
		wanted.sort(comparator());

		for (int i = 0; i < wanted.size(); i++) {
			ItemStack goal = wanted.get(i);
			Slot target = slots.get(i);
			if (matches(target.getItem(), goal)) {
				continue;
			}
			for (int j = i + 1; j < slots.size(); j++) {
				Slot source = slots.get(j);
				if (matches(source.getItem(), goal)) {
					// Pick up the wanted stack, swap it with the target, put the old target stack where it came from
					clicker.click(source);
					clicker.click(target);
					clicker.click(source);
					break;
				}
			}
		}
	}

	private static boolean matches(ItemStack stack, ItemStack goal) {
		return ItemStack.isSameItemSameComponents(stack, goal) && stack.getCount() == goal.getCount();
	}

	private static Comparator<ItemStack> comparator() {
		Comparator<ItemStack> byId = Comparator.comparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
		Comparator<ItemStack> base = switch (InventorySortConfig.ORDER.get()) {
			case ID -> byId;
			case NAME -> Comparator.<ItemStack, String>comparing(stack -> stack.getHoverName().getString()).thenComparing(byId);
			case COUNT -> Comparator.<ItemStack>comparingInt(ItemStack::getCount).reversed().thenComparing(byId);
		};
		// Same items: full stacks first, then by name (renamed items, different enchantments) to keep them stable
		return base.thenComparing(Comparator.<ItemStack>comparingInt(ItemStack::getCount).reversed())
			.thenComparing(stack -> stack.getHoverName().getString());
	}

	@FunctionalInterface
	private interface Clicker {
		void click(Slot slot);
	}
}
