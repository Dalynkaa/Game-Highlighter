package com.dalynkaa.utilities.data;

import net.minecraft.util.Identifier;

import java.util.UUID;
import java.util.function.Supplier;

public record HighlightPlayer(UUID uuid, String name, Identifier skinTexture) {

}
