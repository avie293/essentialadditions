package cc.avie.esad.mixin;

import cc.avie.esad.feature.freelook.Freelook;
import cc.avie.esad.feature.zoom.Zoom;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private void esad$zoomSensitivity(LocalPlayer player, double yaw, double pitch, Operation<Void> original) {
		double multiplier = Zoom.sensitivityMultiplier();
		if (Freelook.isActive()) {
			// Freelook turns only the camera, the player keeps its direction
			Freelook.turn(yaw * multiplier, pitch * multiplier);
			return;
		}
		original.call(player, yaw * multiplier, pitch * multiplier);
	}

	@ModifyExpressionValue(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;smoothCamera:Z"))
	private boolean esad$zoomCinematicCamera(boolean smoothCamera) {
		return smoothCamera || Zoom.useCinematicCamera();
	}

	// Only reached in game (no screen open), so scrolling in menus is not affected
	@WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;onMouseScroll(DD)Lorg/joml/Vector2i;"))
	private Vector2i esad$scrollZoom(ScrollWheelHandler handler, double scrollX, double scrollY, Operation<Vector2i> original) {
		Vector2i steps = original.call(handler, scrollX, scrollY);
		// Zero steps make vanilla return early instead of changing the hotbar slot
		return Zoom.onScroll(steps.y) ? new Vector2i() : steps;
	}
}
