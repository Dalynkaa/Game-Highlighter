package me.dalynkaa.highlighter.client.newgui.widgets;

import com.google.common.collect.Lists;
import lombok.Setter;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.newgui.HighlightScreen;
import me.dalynkaa.highlighter.client.newgui.widgets.entryes.HighlighterPrefixListEntry;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HighlighterPrefixListWidget  extends ElementListWidget<HighlighterPrefixListEntry> {
    private final HighlightScreen parent;

    @Setter
    @Nullable
    private String currentSearch;
    private final List<HighlighterPrefixListEntry> prefixListEntries = Lists.newArrayList();

    public HighlighterPrefixListWidget(HighlightScreen parent, MinecraftClient minecraftClient, int width, int height, int y, int itemHeight) {
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

    public void update(Collection<Prefix> prefixes, double scrollAmount) {
        Highlighter.LOGGER.info("Prefixes: {}", prefixes);
        if (prefixes == null) return;
        Map<UUID, HighlighterPrefixListEntry> map = new HashMap<>();
        this.setPrefixes(prefixes, map);
        this.refresh(map.values(), scrollAmount);
    }

    private void setPrefixes(Collection<Prefix> prefixes, Map<UUID, HighlighterPrefixListEntry> entriesByUuids) {
        for(Prefix prefix : prefixes) {
            entriesByUuids.put(prefix.getPrefixId(), new HighlighterPrefixListEntry(this.client, this.parent, prefix));
        }

    }
    public boolean isEmpty() {
        return this.prefixListEntries.isEmpty();
    }


    private void sortPlayers() {

    }


    private void refresh(Collection<HighlighterPrefixListEntry> prefixListEntries, double scrollAmount) {
        this.prefixListEntries.clear();
        this.prefixListEntries.addAll(prefixListEntries);
        this.sortPlayers();
        this.filterPlayers();
        this.replaceEntries(this.prefixListEntries);
        this.setScrollAmount(scrollAmount);
    }

    private void filterPlayers() {
        if (this.currentSearch != null) {
            this.prefixListEntries.removeIf((player) -> !player.getName().toLowerCase(Locale.ROOT).contains(this.currentSearch));
            this.replaceEntries(this.prefixListEntries);
        }
    }
}
