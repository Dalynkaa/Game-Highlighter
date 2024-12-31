package com.dalynkaa.listeners;

import com.dalynkaa.customEvents.ChatMessageEvent;
import com.dalynkaa.customEvents.data.ChatMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnChatMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger("gamehighlighter");
    private final MessageHandler messageHandler;
    public OnChatMessage() {
        ChatMessageEvent.EVENT.register(this::onChatMessage);
        messageHandler = MinecraftClient.getInstance().getMessageHandler();

    }

    public ActionResult onChatMessage(ChatMessage chatMessage) {
        Pattern pattern = Pattern.compile("\\[СП5\\] (?<nickname>[a-zA-Z0-9,\\.\\_]\\w*): (?<message>[а-яА-Яa-zA-Z0-9,\\.\\_].*)");
        Matcher matcher = pattern.matcher(chatMessage.message().getString());
        if (matcher.find()) {
            PlayerListEntry entry = Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getPlayerListEntry(matcher.group("nickname"));
            Text line = Text.literal("--------------------------------\n");
            if (entry != null) {
                if (messageHandler!=null) {
                    messageHandler.onGameMessage(line, chatMessage.overlay());
                    messageHandler.onGameMessage(chatMessage.message(), chatMessage.overlay());
                    messageHandler.onGameMessage(line, chatMessage.overlay());
                    return ActionResult.SUCCESS;
                }else {
                    return ActionResult.PASS;
                }
            }else{
                return ActionResult.PASS;
            }

        }
        return ActionResult.PASS;
    }
}
