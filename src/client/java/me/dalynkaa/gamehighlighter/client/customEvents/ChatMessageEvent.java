package me.dalynkaa.gamehighlighter.client.customEvents;

import me.dalynkaa.gamehighlighter.client.customEvents.data.ChatMessage;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface ChatMessageEvent {

    Event<ChatMessageEvent> EVENT = EventFactory.createArrayBacked(ChatMessageEvent.class,
            (listeners) -> ( chatMessage ) -> {
                for (ChatMessageEvent event : listeners) {
                    return event.onChatMessage(chatMessage);
                }
                return false;
            }
    );

    Boolean onChatMessage(ChatMessage chatMessage);


}
