package cc.avie.esad.feature.tooltips;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

/** Draws container content as an inventory grid with 9 slots per row, like the container itself. */
public class ClientContainerTooltip implements ClientTooltipComponent {
	private static final int SLOT = 18;
	private static final int COLUMNS = 9;

	private final ContainerTooltip tooltip;

	public ClientContainerTooltip(ContainerTooltip tooltip) {
		this.tooltip = tooltip;
	}

	private int rows() {
		return (this.tooltip.items().size() + COLUMNS - 1) / COLUMNS;
	}

	@Override
	public int getHeight(Font font) {
		return this.rows() * SLOT + 4;
	}

	@Override
	public int getWidth(Font font) {
		return COLUMNS * SLOT;
	}

	@Override
	public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
		int top = y + 2;
		int color = this.tooltip.color();
		// Frame and slot colors of the vanilla inventory, optionally tinted with the color of the shulker box
		int frame = color != 0 ? darken(color, 0.45f) : 0xFF373737;
		int slot = color != 0 ? darken(color, 0.8f) : 0xFF8B8B8B;
		int light = color != 0 ? color | 0xFF000000 : 0xFFFFFFFF;
		for (int i = 0; i < this.tooltip.items().size(); i++) {
			int slotX = x + i % COLUMNS * SLOT;
			int slotY = top + i / COLUMNS * SLOT;
			graphics.fill(slotX, slotY, slotX + SLOT, slotY + SLOT, frame);
			graphics.fill(slotX + 1, slotY + 1, slotX + SLOT, slotY + SLOT, light);
			graphics.fill(slotX + 1, slotY + 1, slotX + SLOT - 1, slotY + SLOT - 1, slot);
			ItemStack stack = this.tooltip.items().get(i);
			if (!stack.isEmpty()) {
				graphics.item(stack, slotX + 1, slotY + 1);
				graphics.itemDecorations(font, stack, slotX + 1, slotY + 1);
			}
		}
	}

	private static int darken(int color, float factor) {
		int r = (int) ((color >> 16 & 0xFF) * factor);
		int g = (int) ((color >> 8 & 0xFF) * factor);
		int b = (int) ((color & 0xFF) * factor);
		return 0xFF000000 | r << 16 | g << 8 | b;
	}
}
