package me.dalynkaa.gamehighlighter.client.utilities.data;

import me.dalynkaa.gamehighlighter.client.GameHighlighterClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.UUID;

public class Prefix {
    private final UUID prefixId;
    private String prefixTag;
    private boolean chatHighlightingEnabled;
    private String chatTemplate;
    private String chatSound;
    private String prefixContent;
    private String playerColor;
    private String prefixColor;

    public Prefix(UUID prefixId, String prefixTag,String chatTemplate,String chatSound,String prefixContent, String playerColor, String prefixColor) {
        this.prefixId = prefixId;
        this.prefixTag = prefixTag;
        this.chatHighlightingEnabled = false;
        this.chatTemplate = chatTemplate;
        this.chatSound = chatSound;
        this.prefixContent = prefixContent;
        this.playerColor = playerColor;
        this.prefixColor = prefixColor;
    }

    public UUID getPrefixId() {
        return this.prefixId;
    }

    public String getPrefixTag() {
        return this.prefixTag;
    }

    public void setPrefixTag(String prefixTag) {
        this.prefixTag = prefixTag;
    }

    public String getPrefixChar() {
        return this.prefixContent;
    }

    public void setPrefixChar(String prefix) {
        this.prefixContent = prefix;
    }

    public String getPlayerColor() {
        return this.playerColor;
    }

    public String getChatTemplate() {
        return this.chatTemplate;
    }
    public boolean isChatHighlightingEnabled() {
        return this.chatHighlightingEnabled;
    }
    public void setChatHighlightingEnabled(boolean chatHighlightingEnabled) {
        this.chatHighlightingEnabled = chatHighlightingEnabled;
    }
    public void setChatTemplate(String chatTemplate) {
        this.chatTemplate = chatTemplate;
    }
    public String getChatSound() {
        return this.chatSound;
    }
    public void setChatSound(String chatSound) {
        this.chatSound = chatSound;
    }

    public void setPlayerColor(String color) {
        this.playerColor = color;
    }

    public String getPrefixColor() {
        return this.prefixColor;
    }

    public void setPrefixColor(String prefix_color) {
        this.prefixColor = prefix_color;
    }

    public static Prefix getPrefix(UUID prefix_id) {
        return GameHighlighterClient.getClientConfig().getPrefix(prefix_id);
    }
    public MutableText getPrefixText(Text displayname) {
        MutableText mutableText = Text.literal("");
        mutableText.append(getPrefixChar()).styled(style -> style.withColor(TextColor.parse(getPrefixColor()).getOrThrow()));
        mutableText.append(displayname.copy().styled(style -> style.withColor(TextColor.parse(getPlayerColor()).getOrThrow())));
        return mutableText;
    }

    @Override
    public String toString() {
        return "Prefix{" +
                "prefixId=" + this.prefixId +
                ", prefixTag='" + this.prefixTag + '\'' +
                ", prefixContent='" + this.prefixContent + '\'' +
                ", playerColor='" + this.playerColor + '\'' +
                ", prefixColor='" + this.prefixColor + '\'' +
                ", chatTemplate='" + this.chatTemplate + '\'' +
                '}';
    }
}
