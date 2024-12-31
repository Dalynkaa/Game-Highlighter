package com.dalynkaa.customEvents;

import com.dalynkaa.customEvents.data.ChatMessage;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface ChatMessageEvent {

    Event<ChatMessageEvent> EVENT = EventFactory.createArrayBacked(ChatMessageEvent.class,
            (listeners) -> ( chatMessage ) -> {
                for (ChatMessageEvent event : listeners) {
                    ActionResult actionResult = event.onChatMessage(chatMessage);
                    if (actionResult!=ActionResult.PASS){
                        return actionResult;
                    }
                }
                return ActionResult.PASS;
            }
    );

    ActionResult onChatMessage(ChatMessage chatMessage);
}
