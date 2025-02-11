package me.dalynkaa.gamehighlighter.client.listeners;

import me.dalynkaa.gamehighlighter.client.GameHighlighterClient;
import me.dalynkaa.gamehighlighter.client.customEvents.ChatMessageEvent;
import me.dalynkaa.gamehighlighter.client.customEvents.data.ChatMessage;
import me.dalynkaa.gamehighlighter.client.utilities.ModConfig;
import me.dalynkaa.gamehighlighter.client.utilities.data.HighlitedPlayer;
import me.dalynkaa.gamehighlighter.client.utilities.data.Prefix;
import net.kyori.adventure.audience.Audience;
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
        this.config = GameHighlighterClient.config;
        ChatMessageEvent.EVENT.register(this::onChatMessage);
    }

    public boolean onChatMessage(ChatMessage chatMessage) {
        Pattern pattern = Pattern.compile(config.chatSettings.globalChatRegex);
        Matcher matcher = pattern.matcher(chatMessage.getMessage().getString());
        ChatLog chatLog = this.client.getAbuseReportContext().getChatLog();

        if (!matcher.matches()) {
            return false;
        }
        if (matcher.groupCount() != 2) {
            return false;
        }
        if (!matcher.namedGroups().containsKey("nickname") && !matcher.namedGroups().containsKey("message")) {
            return false;
        }
        String nickname = matcher.group("nickname");
        String message = matcher.group("message");

        PlayerListEntry playerListEntry = Objects.requireNonNull(client.getNetworkHandler()).getPlayerListEntry(nickname);
        if (playerListEntry == null) {
            return false;
        }
        boolean isHighlighted = GameHighlighterClient.clientConfig.isHighlighted(playerListEntry.getProfile().getId());

        if (isHighlighted) {
            HighlitedPlayer highlitedPlayer = HighlitedPlayer.getHighlitedPlayer(playerListEntry.getProfile().getId());
            Prefix prefix = highlitedPlayer.getPrefix();
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
                ((Audience) player).sendMessage(formattedMessage);
                client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
                chatLog.add(ReceivedMessage.of(chatMessage.getMessage(), Instant.now()));
                this.client.getNarratorManager().narrateSystemMessage(chatMessage.getMessage());
                return true;
            }

        }

        return false;
    }
}
