package me.dalynkaa.highlighter.client.utilities.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.utilities.CustomNotificationEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.UUID;

public class Prefix {
    @Expose
    @SerializedName("prefix_id")
    private final UUID prefixId;
    @Expose
    @SerializedName("prefix_tag")
    private String prefixTag;
    @Expose
    @SerializedName("chat_highlighting_enabled")
    private boolean chatHighlightingEnabled;
    @Expose
    @SerializedName("chat_template")
    private String chatTemplate;
    @Expose
    @SerializedName("chat_sound")
    private String chatSound;
    @Expose
    @SerializedName("prefix_content")
    private String prefixContent;
    @Expose
    @SerializedName("player_color")
    private String playerColor;
    @Expose
    @SerializedName("prefix_color")
    private String prefixColor;
    @Expose
    @SerializedName("index")
    private Integer index;

    public Prefix(UUID prefixId, String prefixTag,String chatTemplate,String chatSound,String prefixContent, String playerColor, String prefixColor) {
        this.prefixId = prefixId;
        this.prefixTag = prefixTag;
        this.chatHighlightingEnabled = false;
        this.chatTemplate = chatTemplate;
        this.chatSound = chatSound;
        this.prefixContent = prefixContent;
        this.playerColor = playerColor;
        this.prefixColor = prefixColor;
        this.index = Prefix.gelLatestPrefixIndex()+1;
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

    public Integer getIndex() {
        return this.index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public CustomNotificationEffects getChatSoundEffect() {
        return CustomNotificationEffects.getEffectByName(this.chatSound);
    }

    public static Prefix getPrefix(UUID prefixId) {
        return HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefix(prefixId);
    }
    public MutableText getPrefixText(Text displayName) {
        MutableText mutableText = Text.literal("");
        mutableText.append(getPrefixChar()).styled(style -> style.withColor(TextColor.parse(getPrefixColor()).getOrThrow()));
        mutableText.append(displayName.copy().styled(style -> style.withColor(TextColor.parse(getPlayerColor()).getOrThrow())));
        return mutableText;
    }

    public static Integer gelLatestPrefixIndex() {
        return HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefixes()
                .stream()
                .mapToInt(Prefix::getIndex)
                .max()
                .orElse(0);
    }

    public void movePrefixTop() {
        HighlighterClient.STORAGE_MANAGER.getPrefixStorage().movePrefixTop(this);
    }
    public void movePrefixDown() {
        HighlighterClient.STORAGE_MANAGER.getPrefixStorage().movePrefixDown(this);
    }
    public boolean isLatestPrefix() {
        return this.index.equals(gelLatestPrefixIndex());
    }
    public boolean isFirstPrefix() {
        return this.index.equals(0);
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
