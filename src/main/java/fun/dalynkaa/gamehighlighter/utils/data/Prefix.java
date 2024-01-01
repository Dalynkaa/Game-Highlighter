package fun.dalynkaa.gamehighlighter.utils.data;

import fun.dalynkaa.gamehighlighter.client.GameHighlighterClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.UUID;

public class Prefix {
    private UUID prefix_id;
    private String prefix_tag;
    private String prefix;
    private String color;
    private String prefix_color;

    public Prefix(UUID prefix_id, String prefix_tag,String prefix, String color, String prefix_color) {
        this.prefix_id = prefix_id;
        this.prefix_tag = prefix_tag;
        this.prefix = prefix;
        this.color = color;
        this.prefix_color = prefix_color;
    }

    public UUID getPrefix_id() {
        return prefix_id;
    }

    public String getPrefix_tag() {
        return prefix_tag;
    }

    public void setPrefix_tag(String prefix_tag) {
        this.prefix_tag = prefix_tag;
    }

    public String getPrefixChar() {
        return prefix;
    }

    public void setPrefixChar(String prefix) {
        this.prefix = prefix;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPrefix_color() {
        return prefix_color;
    }

    public void setPrefix_color(String prefix_color) {
        this.prefix_color = prefix_color;
    }
    public static Prefix getPrefix(UUID prefix_id) {
        return GameHighlighterClient.getClientConfig().getPrefix(prefix_id);
    }
    public MutableText getPrefixText(Text displayname) {
        MutableText mutableText = Text.literal("");
        mutableText.append(getPrefixChar()).styled(style -> style.withColor(TextColor.parse(getPrefix_color())));
        mutableText.append(displayname.copy().styled(style -> style.withColor(TextColor.parse(getColor()))));
        return mutableText;
    }

    @Override
    public String toString() {
        return "Prefix{" +
                "prefix_id=" + prefix_id +
                ", prefix_tag='" + prefix_tag + '\'' +
                ", prefix='" + prefix + '\'' +
                ", color='" + color + '\'' +
                ", prefix_color='" + prefix_color + '\'' +
                '}';
    }
}
