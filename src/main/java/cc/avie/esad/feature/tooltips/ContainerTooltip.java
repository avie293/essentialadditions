package cc.avie.esad.feature.tooltips;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Tooltip image data for the content of a shulker box, another filled container or the ender chest.
 *
 * @param items slots in order, empty stacks for empty slots
 * @param color tint of the slot background (ARGB), 0 for the plain gray of vanilla slots
 */
public record ContainerTooltip(List<ItemStack> items, int color) implements TooltipComponent {
}
