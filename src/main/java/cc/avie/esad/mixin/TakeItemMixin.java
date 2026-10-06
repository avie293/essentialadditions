package cc.avie.esad.mixin;

import cc.avie.esad.feature.hud.PickupNotifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class TakeItemMixin {
	/** After the switch to the client thread, while the item entity still exists. */
	@Inject(method = "handleTakeItemEntity", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
		shift = At.Shift.AFTER))
	private void esad$notifyPickup(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null || packet.getPlayerId() != minecraft.player.getId()) {
			return;
		}
		Entity entity = minecraft.level.getEntity(packet.getItemId());
		if (entity instanceof ItemEntity item) {
			PickupNotifier.onPickup(item.getItem(), packet.getAmount());
		}
	}
}
