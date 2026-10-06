package cc.avie.esad.mixin;

import cc.avie.esad.feature.zoom.Zoom;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {
	@ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
	private float esad$applyZoom(float fov) {
		return Zoom.modifyFov(fov);
	}
}
