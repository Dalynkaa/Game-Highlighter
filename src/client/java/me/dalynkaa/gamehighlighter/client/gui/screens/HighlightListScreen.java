package me.dalynkaa.gamehighlighter.client.gui.screens;


import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import me.dalynkaa.gamehighlighter.client.GameHighlighterClient;
import me.dalynkaa.gamehighlighter.client.gui.widget.PlayerListEntryComponent;
import me.dalynkaa.gamehighlighter.client.utilities.data.HighlitedPlayer;
import me.dalynkaa.gamehighlighter.client.utilities.data.Prefix;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;


import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Environment(EnvType.CLIENT)

public class HighlightListScreen extends BaseOwoScreen<FlowLayout> {

    Comparator<PlayerListEntry> ENTRY_ORDERING = Comparator.comparingInt(value -> GameHighlighterClient.clientConfig.getAllHighlitedUUID().contains(value.getProfile().getId()) ? 0 : 1);
    Collection<PlayerListEntry> entries;
    TextBoxComponent searchBox;

    private static final Identifier SEARCH_ICON_TEXTURE = Identifier.of("gamehighlighter","textures/gui/search.png");


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
        LabelComponent playerName = Components.label(Text.literal("Unknown").styled((style) -> style.withColor(TextColor.fromFormatting(Formatting.WHITE))));
        playerName.margins(Insets.of(5,5,5,0));
        rootComponent
                .surface(Surface.blur(15,10))
                .horizontalAlignment(HorizontalAlignment.RIGHT)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(5,5,5,100));
        Component title = Components.label(Text.literal("Player List").styled((style) -> style.withColor(TextColor.fromFormatting(Formatting.WHITE))))
                .margins(Insets.of(5,5,5,0));
        searchBox = Components.textBox(Sizing.fill(99), "");

        searchBox.margins(Insets.of(0,5,0,5));
        searchBox.setPlaceholder(Text.translatable("gui.gamehighlighter.search"));
        FlowLayout searchContainer = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20))
                .child(createSearchIcon().margins(Insets.of(0,0,2,2)))
                .child(searchBox);
        searchContainer.verticalAlignment(VerticalAlignment.CENTER);


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
        ParentComponent listContainerMain = Containers.verticalFlow(Sizing.fixed(236), Sizing.fixed(360))
                .child(searchContainer.margins(Insets.of(2,2,0,3)))
                .child(listContainer)
                .surface(Surface.PANEL)
                .padding(Insets.of(5,5,5,5));

        ParentComponent test = Containers.verticalFlow(Sizing.fixed(246), Sizing.fixed(400))
                .child(title)
                .child(listContainerMain)
                .padding(Insets.of(5,5,5,5));

        //playerInfoContainer

        ParentComponent playerInfoContainer = Containers.verticalFlow(Sizing.fixed(200), Sizing.fixed(100))
                .child(playerName)
                .margins(Insets.of(5,5,5,200))
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);


        rootComponent.child(playerInfoContainer);
        rootComponent.child(test);
    }
    public void setTargetPlayer(FlowLayout scrollContainer, List<PlayerListEntry> filtered, LabelComponent playerName) {
        List<PlayerListEntry> filtered1 = filtered.stream().sorted(ENTRY_ORDERING).toList();
        for (PlayerListEntry entry : filtered1) {
            HighlitedPlayer highlitedPlayer = HighlitedPlayer.getHighlitedPlayer(entry.getProfile().getId());
            Prefix prefix = highlitedPlayer.getPrefix();
            PlayerListEntryComponent player = new PlayerListEntryComponent(entry, prefix);
            player.mouseEnter().subscribe(() ->{
                if (prefix == null) {
                    playerName.text(Text.literal(entry.getProfile().getName()).styled((style) -> style.withColor(TextColor.fromFormatting(Formatting.WHITE))));
                    return;
                }
                playerName.text(prefix.getPrefixText(Text.of(entry.getProfile().getName())));
            });
            scrollContainer.child(player);
        }
    }
    private Component createSearchIcon() {
        TextureComponent searchIcon = Components.texture(SEARCH_ICON_TEXTURE, 0,0,12,12,12,12);
        return searchIcon.sizing(Sizing.fixed(15), Sizing.fixed(15));
    }

}
