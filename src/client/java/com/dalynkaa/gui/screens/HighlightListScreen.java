package com.dalynkaa.gui.screens;

import com.dalynkaa.gui.widget.PlayerListEntryComponent;
import com.dalynkaa.utilities.data.HighlitedPlayer;
import com.dalynkaa.utilities.data.Prefix;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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

@Environment(EnvType.CLIENT)

public class HighlightListScreen extends BaseOwoScreen<FlowLayout> {
    Collection<PlayerListEntry> entries;
    public HighlightListScreen() {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler != null) {
            entries = networkHandler.getPlayerList();
        }

    }
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        LabelComponent playerName = Components.label(Text.literal("????").styled((style) -> style.withColor(TextColor.fromFormatting(Formatting.WHITE))));
        playerName.margins(Insets.of(5,5,5,0));
        rootComponent
                .surface(Surface.blur(15,10))
                .horizontalAlignment(HorizontalAlignment.RIGHT)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(5,5,5,100));
        Component title = Components.label(Text.literal("Player List").styled((style) -> style.withColor(TextColor.fromFormatting(Formatting.BLACK))))
                .margins(Insets.of(5,5,5,0));
        TextBoxComponent searchBox = Components.textBox(Sizing.fill(100), "");
        searchBox.margins(Insets.of(0,5,0,0));
        FlowLayout scrollContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        setTargetPlayer(scrollContainer,entries.stream().toList(),playerName);
        searchBox.onChanged().subscribe(text -> {
            scrollContainer.clearChildren();
            List<PlayerListEntry> filtered = entries.stream().filter(player -> player.getProfile().getName().toLowerCase().contains(text.toLowerCase())).toList();
            if (text.isEmpty()){
                filtered=entries.stream().toList();
            }
            setTargetPlayer(scrollContainer,filtered,playerName);
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

        //playerInfoContainer

        ParentComponent playerInfoContainer = Containers.verticalFlow(Sizing.fixed(200), Sizing.fixed(100))
                .child(playerName)
                .margins(Insets.of(5,5,5,200))
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);


        rootComponent.child(playerInfoContainer);
        rootComponent.child(listContainerMain);
    }
    public void setTargetPlayer(FlowLayout scrollContainer, List<PlayerListEntry> filtered, LabelComponent playerName) {
        for (PlayerListEntry entry : filtered) {
            PlayerListEntryComponent player = new PlayerListEntryComponent(entry);
            player.mouseEnter().subscribe(() ->{
                HighlitedPlayer highlitedPlayer = HighlitedPlayer.getHighlitedPlayer(entry.getProfile().getId());
                Prefix prefix = highlitedPlayer.getPrefix();
                if (prefix == null) {
                    playerName.text(Text.literal(entry.getProfile().getName()).styled((style) -> style.withColor(TextColor.fromFormatting(Formatting.WHITE))));
                    return;
                }
                playerName.text(prefix.getPrefixText(Text.of(entry.getProfile().getName())));
            });
            scrollContainer.child(player);
        }
    }

}
