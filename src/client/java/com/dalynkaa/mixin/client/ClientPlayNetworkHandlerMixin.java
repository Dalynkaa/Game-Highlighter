package com.dalynkaa.mixin.client;

import com.dalynkaa.customEvents.ChatMessageEvent;
import com.dalynkaa.customEvents.data.ChatMessage;
import com.mojang.brigadier.ParseResults;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin  {

    @Shadow protected abstract ParseResults<CommandSource> parse(String command);

    @Inject(at = @At("HEAD"), method = "onGameMessage", cancellable = true)
    public void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        ChatMessage chatMessage = new ChatMessage(packet.content(),packet.overlay());
        ActionResult actionResult = ChatMessageEvent.EVENT.invoker().onChatMessage(chatMessage);
        if (actionResult.equals(ActionResult.FAIL) || actionResult.equals(ActionResult.SUCCESS)) {
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
