package cc.avie.esad.feature.tooltips;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Remembers the content of the ender chest. The client only knows it while the ender chest is open, so it is
 * copied when the screen closes and shown in the tooltip of ender chest items until you leave the world.
 */
public final class EnderChestCache {
	private static @Nullable List<ItemStack> contents;

	private EnderChestCache() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (screen instanceof ContainerScreen container && isEnderChest(container)) {
				ScreenEvents.remove(screen).register(closed -> remember(container.getMenu().getContainer()));
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.level == null) {
				contents = null;
			}
		});
	}

	private static boolean isEnderChest(ContainerScreen screen) {
		return screen.getTitle().getContents() instanceof TranslatableContents translatable
			&& translatable.getKey().equals("container.enderchest");
	}

	private static void remember(Container container) {
		List<ItemStack> items = new ArrayList<>();
		for (int i = 0; i < container.getContainerSize(); i++) {
			items.add(container.getItem(i).copy());
		}
		contents = items;
	}

	/** The last seen content, or null when the ender chest was not opened since joining. */
	public static @Nullable List<ItemStack> contents() {
		return contents;
	}
}
