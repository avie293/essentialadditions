package cc.avie.esad.mixin;

import cc.avie.esad.feature.outline.BlockOutline;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Version specific: called submitHitOutline since 26.2
@Mixin(LevelRenderer.class)
public class BlockOutlineMixin {
	@ModifyVariable(method = "renderHitOutline", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private int esad$outlineColor(int color) {
		return BlockOutline.color(color);
	}

	@ModifyVariable(method = "renderHitOutline", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float esad$outlineWidth(float width) {
		return BlockOutline.width(width);
	}
}
