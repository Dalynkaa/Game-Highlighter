package fun.dalynkaa.gamehighlighter.mixin;


import fun.dalynkaa.gamehighlighter.client.GameHighlighterClient;
import fun.dalynkaa.gamehighlighter.utils.ModConfig;
import fun.dalynkaa.gamehighlighter.utils.data.HighlitedPlayer;
import fun.dalynkaa.gamehighlighter.utils.data.Prefix;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static fun.dalynkaa.gamehighlighter.client.GameHighlighterClient.TABKEY_KEYBIND;

@Mixin(PlayerListHud.class)
public class PlayerListMixin {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("otsohelper");
    @Unique
    private static final Comparator<PlayerListEntry> ENTRY_ORDERING = Comparator.comparingInt(entry -> GameHighlighterClient.clientConfig.getAllHighlitedUUID().contains(entry.getProfile().getId())? 0 : 1);

    @Shadow
    @Final
    private MinecraftClient client;
    @Inject(method = "getPlayerName",at = @At("RETURN"),cancellable = true)
    public void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir){
        Text displayname = cir.getReturnValue();
        if (GameHighlighterClient.clientConfig.isHighlighted(entry.getProfile().getId()) && GameHighlighterClient.config.hasTabEnable()){
            HighlitedPlayer highlitedPlayer = GameHighlighterClient.getClientConfig().getHighlitedPlayer(entry.getProfile().getId());
            Prefix prefix = highlitedPlayer.getPrefix();
            cir.setReturnValue(prefix.getPrefixText(displayname));
        }
    }
    @Inject(method = "collectPlayerEntries", at = @At("RETURN"), cancellable = true)
    public void collect(CallbackInfoReturnable<List<PlayerListEntry>> cir){
        List<PlayerListEntry> entries = this.client.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).toList();
        List<UUID> need = GameHighlighterClient.clientConfig.getAllHighlitedUUID().stream().toList();
        List<PlayerListEntry> entries1 = new ArrayList<>();
        Comparator<PlayerListEntry> customComparator = Comparator.comparingInt(value -> Arrays.asList(need).contains(value.getProfile().getId()) ? 0 : 1);
        if (TABKEY_KEYBIND.isPressed()) {
            for (PlayerListEntry entry: entries){
                if (need.contains(entry.getProfile().getId())){
                    entries1.add(entry);
                }
            }
            cir.setReturnValue(entries1.stream().limit(80L).toList());
            return;
        }
        cir.setReturnValue(entries.stream().sorted(customComparator).limit(80L).toList());
    }

}
