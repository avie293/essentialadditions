package cc.avie.esad.mixin;

import cc.avie.esad.feature.tooltips.AdvancedTooltips;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@ModifyReturnValue(method = "getTooltipImage", at = @At("RETURN"))
	private Optional<TooltipComponent> esad$addMapPreview(Optional<TooltipComponent> original) {
		return AdvancedTooltips.mapTooltip((ItemStack) (Object) this, original);
	}
}
