package cc.avie.esad.mixin;

import cc.avie.esad.feature.tooltips.AdvancedTooltips;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemContainerContents.class)
public class ContainerContentsMixin {
	/** The preview shows the content, the vanilla text list would only repeat it. */
	@Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
	private void esad$hideListWithPreview(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag,
										  DataComponentGetter components, CallbackInfo ci) {
		if (AdvancedTooltips.hidesContainerList(components)) {
			ci.cancel();
		}
	}
}
