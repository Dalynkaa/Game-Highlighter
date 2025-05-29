package me.dalynkaa.highlighter.client.mixin;



import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.compat.BetterTabApiWrapper;
import me.dalynkaa.highlighter.client.config.ModConfig;
import me.dalynkaa.highlighter.client.config.ServerEntry;
import me.dalynkaa.highlighter.client.utilities.data.HighlightedPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
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

import static me.dalynkaa.highlighter.client.utilities.KeyBindManager.TABKEY_KEYBIND;

@Mixin(value = PlayerListHud.class,priority = 800)
public class PlayerListMixin {
    @Unique
    ModConfig config = HighlighterClient.CONFIG;
    @Shadow
    @Final
    private static Comparator<PlayerListEntry> ENTRY_ORDERING;
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "getPlayerName",at = @At("RETURN"),cancellable = true)
    public void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir){
        if (!HighlighterClient.getServerEntry().isEnabled() || !HighlighterClient.getServerEntry().isUseTabHighlighter()) {
            cir.setReturnValue(cir.getReturnValue());
            return;
        }
        ServerEntry serverEntry = HighlighterClient.getServerEntry();
        Text displayName = cir.getReturnValue();
        if (serverEntry.isHighlighted(entry.getProfile().getId()) && HighlighterClient.CONFIG.hasTabEnable()){
            HighlightedPlayer highlightedPlayer = serverEntry.getHighlitedPlayer(entry.getProfile().getId());
            Prefix prefix = highlightedPlayer.getPrefix();
            if (prefix!=null) {
                cir.setReturnValue(prefix.getPrefixText(displayName));
            }
        }
    }
    @Inject(method = "collectPlayerEntries", at = @At("HEAD"), cancellable = true)
    public void collect(CallbackInfoReturnable<List<PlayerListEntry>> cir){
        if (!HighlighterClient.getServerEntry().isEnabled() || !HighlighterClient.getServerEntry().isUseTabHighlighter()) {
            cir.setReturnValue(cir.getReturnValue());
            return;
        }
        ServerEntry serverEntry = HighlighterClient.getServerEntry();
        long PLAYER_LIST_ENTRY_LIMIT = config.tabSettings.useExtendedTab ? 200L : 80L;
        if (this.client.player!=null){
            List<PlayerListEntry> entries = this.client.player.networkHandler.getListedPlayerListEntries()
                    .stream()
                    .sorted(ENTRY_ORDERING)
                    .toList();

            // Получаем список выделенных UUID и создаем список для хранения отсортированных записей
            List<UUID> highlightedUUIDs = serverEntry.getAllHighlitedUUID().stream().toList();
            List<PlayerListEntry> sortedEntries = new ArrayList<>();

            // Создаем временный список для выделенных игроков
            List<PlayerListEntry> highlightedEntries = new ArrayList<>();
            // Создаем временный список для обычных игроков
            List<PlayerListEntry> regularEntries = new ArrayList<>();

            // Разделяем игроков на выделенных и обычных
            for (PlayerListEntry entry : entries) {
                UUID playerId = entry.getProfile().getId();
                if (highlightedUUIDs.contains(playerId)) {
                    highlightedEntries.add(entry);
                } else {
                    regularEntries.add(entry);
                }
            }

            // Сортируем выделенных игроков по индексу префикса
            highlightedEntries.sort((entry1, entry2) -> {
                UUID id1 = entry1.getProfile().getId();
                UUID id2 = entry2.getProfile().getId();

                HighlightedPlayer player1 = serverEntry.getHighlitedPlayer(id1);
                HighlightedPlayer player2 = serverEntry.getHighlitedPlayer(id2);

                Prefix prefix1 = player1 != null ? player1.getPrefix() : null;
                Prefix prefix2 = player2 != null ? player2.getPrefix() : null;

                // Если у обоих есть префиксы, сортируем по индексу
                if (prefix1 != null && prefix2 != null) {
                    return Integer.compare(prefix1.getIndex(), prefix2.getIndex());
                }
                // Если только у одного есть префикс, он идет первым
                else if (prefix1 != null) {
                    return -1;
                }
                else if (prefix2 != null) {
                    return 1;
                }
                // Если ни у кого нет префикса, сохраняем исходный порядок
                return 0;
            });

            // Сначала добавляем выделенных игроков, затем обычных
            sortedEntries.addAll(highlightedEntries);
            sortedEntries.addAll(regularEntries);

            // Проверяем, нажата ли клавиша TAB для показа только выделенных игроков
            if (TABKEY_KEYBIND.isPressed()) {
                cir.setReturnValue(highlightedEntries);
                return;
            }

            // BetterTab compatibility
            if (BetterTabApiWrapper.isBetterTabAvailable()){
                cir.setReturnValue(sortedEntries);
            } else {
                cir.setReturnValue(sortedEntries.stream().limit(PLAYER_LIST_ENTRY_LIMIT).toList());
            }
        }
    }

}
