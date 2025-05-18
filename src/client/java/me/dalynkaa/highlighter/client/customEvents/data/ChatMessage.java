package me.dalynkaa.highlighter.client.customEvents.data;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.text.Text;

@Setter
@Getter
public class ChatMessage extends CanselableEvent {
    private Text message;
    private ChatMessageType type;

    public ChatMessage(Text message, ChatMessageType type) {
        this.message = message;
        this.type = type;
    }


}

