package com.dalynkaa.gui.screens;

import com.dalynkaa.GameHighlighterClient;
import com.dalynkaa.gui.widget.ScrollDropdownComponent;
import com.dalynkaa.utilities.data.HighlightPlayer;
import com.dalynkaa.utilities.data.HighlitedPlayer;
import com.dalynkaa.utilities.data.Prefix;
import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;


@Environment(EnvType.CLIENT)
public class PlayerEditScreen extends BaseOwoScreen<FlowLayout> {
    private static final Identifier HIGHLIGHT_ICON = new Identifier("gamehighlighter","textures/gui/icons.png");

    public static final int SKIN_TEXTURE_WIDTH = 8;
    public static final int SKIN_TEXTURE_HEIGHT = 8;
    public static final int u = 8;
    public static final int v = 8;
    private static final Logger LOGGER = LoggerFactory.getLogger("otsohelper");
    private boolean isHighlited;

    public PlayerEditScreen(HighlightPlayer highlightPlayer, Screen parent) {
        super(Text.translatable("gui.playerEdit.title", highlightPlayer.name()));
        this.highlightPlayer = highlightPlayer;
        this.client = MinecraftClient.getInstance();
        this.parent = parent;
        this.isHighlited = GameHighlighterClient.getClientConfig().isHighlighted(highlightPlayer.uuid());

    }
    private final HighlightPlayer highlightPlayer;
    private final Screen parent;
    private final MinecraftClient client;
    private String highlitedText() {
        this.isHighlited = GameHighlighterClient.getClientConfig().isHighlighted(highlightPlayer.uuid());
        if (isHighlited) {
            Prefix prefix = HighlitedPlayer.getHighlitedPlayer(highlightPlayer.uuid()).getPrefix();
            LOGGER.info(prefix.toString());
            return prefix.getPrefix_tag() + " (" + prefix.getPrefixChar() + ")";
        } else {
            return Text.translatable("gui.playerEdit.label.highlight").getString();
        }
    }
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);
        Component nickname = Components.label(Text.translatable("gui.playerEdit.title", highlightPlayer.name()))
                .margins(Insets.of(5,0,0,0));
        Component player_head = Components.texture(highlightPlayer.skinTexture(),u,v,SKIN_TEXTURE_WIDTH,SKIN_TEXTURE_HEIGHT,64,64)
                .sizing(Sizing.fixed(20), Sizing.fixed(20))
                .margins(Insets.of(0,0,0,5));
        ParentComponent playerInfo = Containers.horizontalFlow(Sizing.fill(90), Sizing.content())
                .child(player_head)
                .child(nickname);
        ScrollDropdownComponent scrollDropdownComponent = new ScrollDropdownComponent(Sizing.fixed(150), Sizing.content(), Text.literal(highlitedText()), false);
        HashSet<Prefix> prefixes = GameHighlighterClient.getClientConfig().getAllPrefixes();
        for (Prefix prefix : prefixes) {
            scrollDropdownComponent.button(Text.literal(prefix.getPrefix_tag()+" ("+ prefix.getPrefixChar() +")"), button -> {
                HighlitedPlayer HPlayer = HighlitedPlayer.getHighlitedPlayer(highlightPlayer.uuid());
                HPlayer.highlight(prefix.getPrefix_id());
                client.setScreen(new PlayerEditScreen(highlightPlayer, new HighlightListScreen()));
            });
        }
        if (isHighlited) {
            scrollDropdownComponent.button(Text.translatable("gui.playerEdit.dropdown.removeHighlight"), button -> {
                HighlitedPlayer HPlayer = HighlitedPlayer.getHighlitedPlayer(highlightPlayer.uuid());
                HPlayer.unhighlight();
                client.setScreen(new PlayerEditScreen(highlightPlayer, new HighlightListScreen()));
            });
        }
        scrollDropdownComponent.button(Text.translatable("gui.playerEdit.button.newPrefix"), button -> {
            LOGGER.info("Новый префикс");
            client.setScreen(new PrefixEditScreen(Text.translatable("gui.playerEdit.button.newPrefix"), this));
        });
        ParentComponent scrollDropdown = scrollDropdownComponent.margins(Insets.of(5,5,5,5));
        ButtonComponent button = Components.button(Text.translatable("gui.playerEdit.button.edit"), (comp) -> {
            Prefix prefix = HighlitedPlayer.getHighlitedPlayer(highlightPlayer.uuid()).getPrefix();
            client.setScreen(new PrefixEditScreen(null, this, prefix));
        }).active(isHighlited);
        ButtonWidget buttonComponent;
        HighlitedPlayer highlitedPlayer = HighlitedPlayer.getHighlitedPlayer(highlightPlayer.uuid());
        boolean isHiden = highlitedPlayer.isHiden();

        if (isHiden) {
            buttonComponent = new LegacyTexturedButtonWidget(0, 0, 20, 20, 20, 32, 20, HIGHLIGHT_ICON, 256, 256, click -> {
                LOGGER.info("unhided");
                HighlitedPlayer.getHighlitedPlayer(highlightPlayer.uuid()).setHiden(false);
                client.setScreen(new PlayerEditScreen(highlightPlayer, new HighlightListScreen()));
            },Text.translatable("gui.gamehighlighter.un_highlighted"));
        }else {
            buttonComponent = new LegacyTexturedButtonWidget(0, 0, 20, 20, 0, 32, 20, HIGHLIGHT_ICON, 256, 256, click -> {
                LOGGER.info("hided");
                HighlitedPlayer.getHighlitedPlayer(highlightPlayer.uuid()).setHiden(true);
                client.setScreen(new PlayerEditScreen(highlightPlayer, new HighlightListScreen()));
            },Text.translatable("gui.gamehighlighter.un_highlighted"));
        }
        VanillaWidgetComponent buttonWidget = Components.wrapVanillaWidget(buttonComponent);
//        SmallCheckboxComponent checkbox = Components.smallCheckbox(Text.literal("Скрыт")).checked(HPlayer.isHiden());
        ParentComponent editContainer = Containers.horizontalFlow(Sizing.fixed(236), Sizing.content())
                .child(scrollDropdown)
                .child(button.margins(Insets.of(5,5,5,5)).sizing(Sizing.fixed(50), Sizing.fixed(20)))
//                .child(checkbox.margins(Insets.of(5,5,5,5)))
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .padding(Insets.of(5,5,5,5))
                .surface(Surface.DARK_PANEL);
        ParentComponent titleContainer = Containers.horizontalFlow(Sizing.fixed(236), Sizing.fixed(30))
                .child(playerInfo)
                .child(buttonWidget)
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .padding(Insets.of(5,5,5,5))
                .surface(Surface.DARK_PANEL);
        ParentComponent container = Containers.verticalFlow(Sizing.fixed(236), Sizing.content())
                .child(titleContainer)
                .child(editContainer)
                .padding(Insets.of(0))
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.LEFT);
        rootComponent.child(
                container
        );
    }


    @Override
    public void close() {
        client.setScreen(parent);
    }


}
