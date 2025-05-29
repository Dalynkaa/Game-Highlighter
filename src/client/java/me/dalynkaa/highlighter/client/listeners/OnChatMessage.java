package me.dalynkaa.highlighter.client.listeners;

import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.customEvents.ChatMessageEvent;
import me.dalynkaa.highlighter.client.customEvents.data.ChatMessage;
import me.dalynkaa.highlighter.client.config.ModConfig;
import me.dalynkaa.highlighter.client.utilities.data.HighlightedPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
//import net.kyori.adventure.audience.Audience;
//import net.kyori.adventure.text.Component;
//import net.kyori.adventure.text.format.TextColor;
//import net.kyori.adventure.text.minimessage.MiniMessage;
//import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.session.report.log.ChatLog;
import net.minecraft.client.session.report.log.ReceivedMessage;
import net.minecraft.sound.SoundEvents;


import java.time.Instant;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OnChatMessage {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ModConfig config;

    public OnChatMessage() {
        this.config = HighlighterClient.CONFIG;
        ChatMessageEvent.EVENT.register(this::onChatMessage);
    }

    public boolean onChatMessage(ChatMessage chatMessage) {
        String[] patterns = HighlighterClient.getServerEntry().getChatRegex();
        if (patterns == null || patterns.length == 0) {
            return false;
        }
        if (!HighlighterClient.getServerEntry().isEnabled() || !HighlighterClient.getServerEntry().isUseChatHighlighter()) {
            return false;
        }

        String messageString = chatMessage.getMessage().getString();
        String nickname = null;
        String message = null;

        for (String patternStr : patterns) {
            try {
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(messageString);

                if (matcher.matches()) {
                    if (matcher.groupCount() == 2 &&
                            matcher.namedGroups().containsKey("nickname") &&
                            matcher.namedGroups().containsKey("message")) {

                        nickname = matcher.group("nickname");
                        message = matcher.group("message");
                        break;
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }

        if (nickname == null || message == null) {
            return false;
        }

        ChatLog chatLog = this.client.getAbuseReportContext().getChatLog();
        PlayerListEntry playerListEntry = Objects.requireNonNull(client.getNetworkHandler()).getPlayerListEntry(nickname);
        if (playerListEntry == null) {
            return false;
        }

        boolean isHighlighted = HighlighterClient.getServerEntry().isHighlighted(playerListEntry.getProfile().getId());

        if (isHighlighted) {
            HighlightedPlayer highlightedPlayer = HighlighterClient.getServerEntry().getHighlitedPlayer(playerListEntry.getProfile().getId());
            Prefix prefix = highlightedPlayer.getPrefix();
            String template = prefix.getChatTemplate() == null ? config.chatSettings.globalChatMessage : prefix.getChatTemplate();
            Component formattedMessage = MiniMessage.miniMessage()
                    .deserialize(template,
                            Placeholder.unparsed("nickname", nickname),
                            Placeholder.unparsed("message", message),
                            Placeholder.component("prefix", Component.text(prefix.getPrefixChar())),
                            Placeholder.styling("name_color", Objects.requireNonNull(TextColor.fromCSSHexString(prefix.getPlayerColor()))),
                            Placeholder.styling("prefix_color", Objects.requireNonNull(TextColor.fromCSSHexString(prefix.getPrefixColor()))));
            ClientPlayerEntity player = this.client.player;
            if (player != null) {
                player.sendMessage(formattedMessage);
                client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
                chatLog.add(ReceivedMessage.of(chatMessage.getMessage(), Instant.now()));
                this.client.getNarratorManager().narrateSystemMessage(chatMessage.getMessage());
                return true;
            }
        }

        return false;
    }
}
