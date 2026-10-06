package cc.avie.esad.mixin;

import cc.avie.esad.feature.pingdisplay.PingDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	/** Makes room for the ping text, the signal bars are 13 pixels wide. */
	@ModifyConstant(method = "extractRenderState", constant = @Constant(intValue = 13))
	private int esad$widenPingArea(int original) {
		return PingDisplay.showInTabList() ? original + 23 : original;
	}

	@Inject(method = "extractPingIcon(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V",
		at = @At("HEAD"), cancellable = true)
	private void esad$replacePingIconWithText(GuiGraphicsExtractor graphics, int slotWidth, int x, int y, PlayerInfo info, CallbackInfo ci) {
		if (PingDisplay.showInTabList()) {
			PingDisplay.renderPingText(this.minecraft, graphics, slotWidth, x, y, info);
			ci.cancel();
		}
	}
}
