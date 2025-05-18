package me.dalynkaa.highlighter.client.customEvents;

import me.dalynkaa.highlighter.client.customEvents.data.ChatMessage;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

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
