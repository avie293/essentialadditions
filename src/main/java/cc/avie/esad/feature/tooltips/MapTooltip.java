package cc.avie.esad.feature.tooltips;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.saveddata.maps.MapId;

/** Tooltip image data for a filled map, turned into a {@link ClientMapTooltip} for drawing. */
public record MapTooltip(MapId mapId) implements TooltipComponent {
}
