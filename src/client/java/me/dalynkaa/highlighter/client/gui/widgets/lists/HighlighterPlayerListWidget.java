package me.dalynkaa.highlighter.client.gui.widgets.lists;

import com.google.common.collect.Lists;
import lombok.Setter;
import me.dalynkaa.highlighter.client.gui.widgets.lists.entryes.HighlighterPlayerListEntry;
import me.dalynkaa.highlighter.client.gui.HighlightScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HighlighterPlayerListWidget  extends ElementListWidget<HighlighterPlayerListEntry> {
    private final HighlightScreen parent;
    private static final Identifier MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_list_background.png");

    @Setter
    @Nullable
    private String currentSearch;
    private final List<HighlighterPlayerListEntry> players = Lists.newArrayList();
    public HighlighterPlayerListWidget(HighlightScreen parent, MinecraftClient minecraftClient, int width, int height, int y, int itemHeight) {
        super(minecraftClient, width, height, y, itemHeight);
        this.parent = parent;
    }

    @Override
    protected void drawMenuListBackground(DrawContext context) {
    }

    @Override
    protected void drawHeaderAndFooterSeparators(DrawContext context) {
    }
    @Override
    protected void enableScissor(DrawContext context) {
        context.enableScissor(this.getX(), this.getY() + 4, this.getRight(), this.getBottom());
    }
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.getX() && mouseX <= this.getRight() && mouseY >= this.getY() && mouseY <= this.getBottom();
    }

    public void update(Collection<UUID> uuids, double scrollAmount, boolean includeOffline) {
        Map<UUID, HighlighterPlayerListEntry> map = new HashMap<>();
        this.setPlayers(uuids, map);
        this.refresh(map.values(), scrollAmount);
    }

    private void setPlayers(Collection<UUID> playerUuids, Map<UUID, HighlighterPlayerListEntry> entriesByUuids) {
        assert this.client.player != null;
        ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.player.networkHandler;

        for(UUID uUID : playerUuids) {
            PlayerListEntry playerListEntry = clientPlayNetworkHandler.getPlayerListEntry(uUID);
            if (playerListEntry != null) {
                String name = playerListEntry.getProfile().getName();
                Objects.requireNonNull(playerListEntry);
                entriesByUuids.put(uUID, new HighlighterPlayerListEntry(this.client, this.parent, uUID, name, playerListEntry::getSkinTextures, true));
            }
        }

    }
    public boolean isEmpty() {
        return this.players.isEmpty();
    }


    private void sortPlayers() {

    }


    private void refresh(Collection<HighlighterPlayerListEntry> players, double scrollAmount) {
        this.players.clear();
        this.players.addAll(players);
        this.sortPlayers();
        this.filterPlayers();
        this.replaceEntries(this.players);
        //? <=1.21.2 {
        /*this.setScrollAmount(scrollAmount);
        *///?} else {
        this.setScrollY(scrollAmount);
        //?}
    }

    private void filterPlayers() {
        if (this.currentSearch != null) {
            this.players.removeIf((player) -> !player.getName().toLowerCase(Locale.ROOT).contains(this.currentSearch));
            this.replaceEntries(this.players);
        }
    }
}
