package com.dalynkaa.gui.screens;

import com.dalynkaa.gui.widget.PlayerListEntryComponent;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)

public class HighlightListScreen extends BaseOwoScreen<FlowLayout> {
    private static final Logger LOGGER = LoggerFactory.getLogger("gamehighlighter");
    private ClientPlayNetworkHandler networkHandler;
    Collection<PlayerListEntry> entries;
    public HighlightListScreen() {
        networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        entries = networkHandler.getPlayerList();

    }
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.RIGHT)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(5,5,5,100));
        Component title = Components.label(Text.literal("Player List").styled((style) -> style.withColor(TextColor.fromFormatting(Formatting.BLACK))))
                .margins(Insets.of(5,5,5,0));
        TextBoxComponent searchBox = Components.textBox(Sizing.fill(100), "");
        searchBox.margins(Insets.of(0,5,0,0));
        FlowLayout scrollContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        for (PlayerListEntry entry : entries) {
            PlayerListEntryComponent player = new PlayerListEntryComponent(entry);
            scrollContainer.child(player);
        }
        searchBox.onChanged().subscribe(text -> {
            scrollContainer.clearChildren();
            List<PlayerListEntry> filtered = entries.stream().filter(player -> player.getProfile().getName().toLowerCase().contains(text.toLowerCase())).toList();
            if (text.isEmpty()){
                filtered=entries.stream().toList();
            }
            for (PlayerListEntry entry : filtered) {
                PlayerListEntryComponent player = new PlayerListEntryComponent(entry);
                scrollContainer.child(player);
            }
        });
        ScrollContainer<FlowLayout> listContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fixed(400),scrollContainer);
        listContainer.scrollbarThiccness(0);
        listContainer.scrollbar(ScrollContainer.Scrollbar.vanilla());
        listContainer.verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.LEFT);
        ParentComponent listContainerMain = Containers.verticalFlow(Sizing.fixed(236), Sizing.fixed(400))
                .child(title)
                .child(searchBox)
                .child(listContainer)
                .padding(Insets.of(5,5,5,5))
                .surface(Surface.PANEL);
        rootComponent.child(listContainerMain);
    }

}
