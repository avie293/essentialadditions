package cc.avie.esad.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

// Version specific: 26.3 replaced FuelValues with the cooking_fuel item component
public final class FuelCompat {
	private FuelCompat() {
	}

	/** Burn time of a furnace fuel in ticks, 0 when it is no fuel or unknown. */
	public static int burnTime(ItemStack stack) {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.level == null ? 0 : minecraft.level.fuelValues().burnDuration(stack);
	}
}
