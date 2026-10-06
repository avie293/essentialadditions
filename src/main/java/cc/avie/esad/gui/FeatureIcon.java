package cc.avie.esad.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Icon of a feature card. Item stacks can only be created after the item components are bound, which happens
 * when a world is loaded. Before that (e.g. when the settings are opened from the main menu) the flat item
 * texture is drawn instead.
 *
 * @param item    the item drawn in a world
 * @param texture the texture drawn before, e.g. "minecraft:textures/item/spyglass.png"
 */
public record FeatureIcon(Item item, Identifier texture) {
	/** Uses the item texture with the same name as the item. */
	public static FeatureIcon of(Item item) {
		return new FeatureIcon(item, BuiltInRegistries.ITEM.getKey(item).withPath(path -> "textures/item/" + path + ".png"));
	}

	/** Uses another texture, e.g. "block/glowstone" for block items or "item/clock_00" for animated items. */
	public static FeatureIcon of(Item item, String texture) {
		return new FeatureIcon(item, Identifier.withDefaultNamespace("textures/" + texture + ".png"));
	}

	/** Whether item stacks can be created already, false in the main menu before the first world. */
	public static boolean itemsReady() {
		return Items.STONE.builtInRegistryHolder().areComponentsBound();
	}

	public void draw(GuiGraphicsExtractor graphics, int x, int y, int size) {
		if (itemsReady()) {
			graphics.pose().pushMatrix();
			graphics.pose().translate(x, y);
			graphics.pose().scale(size / 16f, size / 16f);
			graphics.item(new ItemStack(this.item), 0, 0);
			graphics.pose().popMatrix();
		} else {
			graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, x, y, 0, 0, size, size, 16, 16, 16, 16);
		}
	}
}
