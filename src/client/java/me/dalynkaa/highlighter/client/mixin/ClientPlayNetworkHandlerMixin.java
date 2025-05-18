package me.dalynkaa.highlighter.client.mixin;


import me.dalynkaa.highlighter.client.customEvents.ChatMessageEvent;
import me.dalynkaa.highlighter.client.customEvents.data.ChatMessage;
import me.dalynkaa.highlighter.client.customEvents.data.ChatMessageType;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin  {

    @Shadow @Final private static Logger LOGGER;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER ), method = "onGameMessage", cancellable = true)
    public void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        ChatMessage chatMessage = new ChatMessage(packet.content(),packet.overlay() ? ChatMessageType.SYSTEM : ChatMessageType.CHAT);
        Boolean isCancelled = ChatMessageEvent.EVENT.invoker().onChatMessage(chatMessage);
        if (isCancelled) {
            ci.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "onChatMessage")
    public void onChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
    }

    @Inject(at = @At("TAIL"), method = "onGameJoin")
    private void onJoin(GameJoinS2CPacket packet, CallbackInfo ci) {

    }
}
