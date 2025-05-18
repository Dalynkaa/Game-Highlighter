package me.dalynkaa.highlighter.client.utilities.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.config.StorageManager;

import java.util.Objects;
import java.util.UUID;

public class HighlightedPlayer {
    @Getter
    @SerializedName("uuid")
    @Expose
    private final UUID uuid;
    @SerializedName("prefix")
    @Expose
    private UUID prefix;
    @SerializedName("isHidden")
    @Expose
    private boolean isHidden;
    @SerializedName("isHighlighted")
    @Expose
    private boolean isHighlighted;

    public HighlightedPlayer(UUID uuid, UUID prefix, boolean isHidden, boolean isHighlighted) {
        this.uuid = uuid;
        this.prefix = prefix;
        this.isHidden = isHidden;
        this.isHighlighted = isHighlighted;
    }

    public HighlightedPlayer(UUID uuid) {
        this.uuid = uuid;
        this.prefix = null;
        this.isHidden = false;
        this.isHighlighted = false;
    }

    public Prefix getPrefix() {
        return HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefix(prefix);
    }
    public HighlightedPlayer setPrefix(UUID prefix_id) {
        this.prefix = prefix_id;
        return this;
    }
    public boolean isHidden() {
        return isHidden;
    }
    public void setHidden(boolean hidden) {
        isHidden = hidden;
        HighlighterClient.getServerEntry().setPlayer(this);
    }
    public boolean isHighlighted() {
        return isHighlighted;
    }
    public HighlightedPlayer setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
        return this;
    }
    public void highlight(UUID prefix_id) {
        this.isHighlighted = true;
        this.prefix = prefix_id;
        HighlighterClient.getServerEntry().setPlayer(this);
    }
    public void unhighlight() {
        this.isHighlighted = false;
        this.prefix = null;
        HighlighterClient.getServerEntry().setPlayer(this);
    }


}
