package me.dalynkaa.gamehighlighter.client.utilities.data;

import net.minecraft.util.Identifier;

import java.util.UUID;

public record HighlightPlayer(UUID uuid, String name, Identifier skinTexture) {

}
