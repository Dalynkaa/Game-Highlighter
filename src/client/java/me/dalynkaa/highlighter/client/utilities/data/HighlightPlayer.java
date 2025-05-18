package me.dalynkaa.highlighter.client.utilities.data;

import me.dalynkaa.highlighter.client.HighlighterClient;
import net.minecraft.client.util.SkinTextures;

import java.util.UUID;

public record HighlightPlayer(UUID uuid, String name, SkinTextures skinTexture) {
    public HighlightedPlayer toHighlitedPlayer() {
        return HighlighterClient.getServerEntry().getHighlitedPlayer(uuid);
    }
}
