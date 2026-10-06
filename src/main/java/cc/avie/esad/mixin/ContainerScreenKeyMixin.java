package cc.avie.esad.mixin;

import cc.avie.esad.feature.sort.InventorySort;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class ContainerScreenKeyMixin {
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void esad$sortKey(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (InventorySort.onKeyPressed((AbstractContainerScreen<?>) (Object) this, event)) {
			cir.setReturnValue(true);
		}
	}
}
