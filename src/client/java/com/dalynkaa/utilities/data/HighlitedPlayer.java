package com.dalynkaa.utilities.data;

import com.dalynkaa.GameHighlighterClient;

import java.util.UUID;

public class HighlitedPlayer {
    private UUID uuid;
    private UUID prefix;
    private boolean isHiden;
    private boolean isHighlighted;

    public HighlitedPlayer(UUID uuid, UUID prefix, boolean isHiden, boolean isHighlighted) {
        this.uuid = uuid;
        this.prefix = prefix;
        this.isHiden = isHiden;
        this.isHighlighted = isHighlighted;
    }

    public UUID getUuid() {
        return uuid;
    }
    public Prefix getPrefix() {
        return Prefix.getPrefix(prefix);
    }
    public HighlitedPlayer setPrefix(UUID prefix_id) {
        this.prefix = prefix_id;
        return this;
    }
    public boolean isHiden() {
        return isHiden;
    }
    public HighlitedPlayer setHiden(boolean hiden) {
        isHiden = hiden;
        return this;
    }
    public boolean isHighlighted() {
        return isHighlighted;
    }
    public HighlitedPlayer setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
        return this;
    }
    public void highlight(UUID prefix_id) {
        this.isHighlighted = true;
        this.prefix = prefix_id;
        GameHighlighterClient.getClientConfig().setPlayer(this);
    }
    public void unhighlight() {
        this.isHighlighted = false;
        this.prefix = null;
        GameHighlighterClient.getClientConfig().setPlayer(this);
    }
    public static HighlitedPlayer getHighlitedPlayer(UUID uuid) {
        HighlitedPlayer player = GameHighlighterClient.getClientConfig().getHighlitedPlayer(uuid);
        if (player == null) {
            return new HighlitedPlayer(uuid, null, false, false);
        }
        return player;
    }


}
