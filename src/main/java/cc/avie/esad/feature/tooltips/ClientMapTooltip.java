package cc.avie.esad.feature.tooltips;

import cc.avie.esad.config.EsadConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** Draws the map like the cartography table does, scaled down to the configured size. */
public class ClientMapTooltip implements ClientTooltipComponent {
	/** Maps are drawn 128 units wide. */
	private static final int MAP_SIZE = 128;
	private static final int PADDING = 2;

	private final MapTooltip tooltip;
	private final MapRenderState renderState = new MapRenderState();

	public ClientMapTooltip(MapTooltip tooltip) {
		this.tooltip = tooltip;
	}

	private static int size() {
		return EsadConfig.TOOLTIP_MAP_SIZE.get();
	}

	@Override
	public int getHeight(Font font) {
		return size() + PADDING * 2;
	}

	@Override
	public int getWidth(Font font) {
		return size();
	}

	@Override
	public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			return;
		}
		MapItemSavedData data = MapItem.getSavedData(this.tooltip.mapId(), minecraft.level);
		if (data == null) {
			// The server has not sent the map yet
			return;
		}
		float scale = size() / (float) MAP_SIZE;
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y + PADDING);
		graphics.pose().scale(scale, scale);
		minecraft.getMapRenderer().extractRenderState(this.tooltip.mapId(), data, this.renderState);
		graphics.map(this.renderState);
		graphics.pose().popMatrix();
	}
}
