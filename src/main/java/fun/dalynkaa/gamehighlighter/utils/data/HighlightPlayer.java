package fun.dalynkaa.gamehighlighter.utils.data;

import lombok.Getter;
import net.minecraft.util.Identifier;

import java.util.UUID;
import java.util.function.Supplier;

@Getter
public record HighlightPlayer(UUID uuid, String name, Supplier<Identifier> skinTexture) {

}
