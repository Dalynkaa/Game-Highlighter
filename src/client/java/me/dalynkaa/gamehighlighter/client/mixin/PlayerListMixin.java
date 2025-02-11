package me.dalynkaa.gamehighlighter.client.mixin;



import me.dalynkaa.gamehighlighter.client.GameHighlighterClient;
import me.dalynkaa.gamehighlighter.client.compat.BetterTabApiWrapper;
import me.dalynkaa.gamehighlighter.client.utilities.ModConfig;
import me.dalynkaa.gamehighlighter.client.utilities.data.HighlitedPlayer;
import me.dalynkaa.gamehighlighter.client.utilities.data.Prefix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Stream;

import static me.dalynkaa.gamehighlighter.client.utilities.KeyBindManager.TABKEY_KEYBIND;

@Mixin(value = PlayerListHud.class,priority = 800)
public class PlayerListMixin {
    @Unique
    ModConfig config = GameHighlighterClient.config;



    @Shadow
    @Final
    private static Comparator<PlayerListEntry> ENTRY_ORDERING;

    @Shadow
    @Final
    private MinecraftClient client;
    @Inject(method = "getPlayerName",at = @At("RETURN"),cancellable = true)
    public void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir){
        Text displayName = cir.getReturnValue();
        if (GameHighlighterClient.clientConfig.isHighlighted(entry.getProfile().getId()) && GameHighlighterClient.config.hasTabEnable()){
            HighlitedPlayer highlitedPlayer = HighlitedPlayer.getHighlitedPlayer(entry.getProfile().getId());
            Prefix prefix = highlitedPlayer.getPrefix();
            if (prefix!=null) {
                cir.setReturnValue(prefix.getPrefixText(displayName));
            }
        }
    }
    @Inject(method = "collectPlayerEntries", at = @At("HEAD"), cancellable = true)
    public void collect(CallbackInfoReturnable<List<PlayerListEntry>> cir){
        long PLAYER_LIST_ENTRY_LIMIT = config.tabSettings.useExtendedTab ? 200L : 80L;
        if (this.client.player!=null){
            List<PlayerListEntry> entries = this.client.player.networkHandler.getListedPlayerListEntries()
                    .stream()
                    .sorted(ENTRY_ORDERING.thenComparing(entry -> GameHighlighterClient.clientConfig.getAllHighlitedUUID().contains(entry.getProfile().getId()) ? 0 : 1))
                    .toList();
            List<UUID> need = GameHighlighterClient.clientConfig.getAllHighlitedUUID().stream().toList();
            List<PlayerListEntry> entries1 = new ArrayList<>();
            Comparator<PlayerListEntry> customComparator = Comparator.comparingInt(value -> need.contains(value.getProfile().getId()) ? 0 : 1);
            if (TABKEY_KEYBIND.isPressed()) {
                for (PlayerListEntry entry: entries){
                    if (need.contains(entry.getProfile().getId())){
                        entries1.add(entry);
                    }
                }
                cir.setReturnValue(entries1.stream().toList());
                return;
            }
            Stream<PlayerListEntry> stream = entries.stream().sorted(customComparator);
            if (BetterTabApiWrapper.isBetterTabAvailable()){
                cir.setReturnValue(stream.toList());
            }else {
                cir.setReturnValue(stream.limit(PLAYER_LIST_ENTRY_LIMIT).toList());
            }
        }

    }

}
