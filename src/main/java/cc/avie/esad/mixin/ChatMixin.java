package cc.avie.esad.mixin;

import cc.avie.esad.feature.chat.ChatTweaks;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatMixin {
	@Shadow
	@Final
	private List<GuiMessage> allMessages;

	@Shadow
	protected abstract void refreshTrimmedMessages();

	@ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
		at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private Component esad$decorateMessage(Component message) {
		if (ChatTweaks.isRepeat(message) && !this.allMessages.isEmpty()) {
			// The newest message is the first one, it is replaced by the stacked line
			this.allMessages.removeFirst();
			this.refreshTrimmedMessages();
		}
		return ChatTweaks.decorate(message);
	}

	@ModifyConstant(method = {"addMessageToDisplayQueue", "addMessageToQueue"}, constant = @Constant(intValue = 100))
	private int esad$longerHistory(int original) {
		return ChatTweaks.history(original);
	}

	@Inject(method = "clearMessages", at = @At("HEAD"))
	private void esad$forgetLastMessage(boolean clearHistory, CallbackInfo ci) {
		ChatTweaks.reset();
	}
}
