package me.dalynkaa.gamehighlighter.client.gui.screens;


import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import me.dalynkaa.gamehighlighter.client.GameHighlighterClient;
import me.dalynkaa.gamehighlighter.client.gui.widget.ScrollDropdownComponent;
import me.dalynkaa.gamehighlighter.client.utilities.data.HighlightPlayer;
import me.dalynkaa.gamehighlighter.client.utilities.data.HighlitedPlayer;
import me.dalynkaa.gamehighlighter.client.utilities.data.Prefix;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;


@Environment(EnvType.CLIENT)
public class PlayerEditScreen extends BaseOwoScreen<FlowLayout> {
    private static final Identifier HIGHLIGHT_ICON = Identifier.of("gamehighlighter","textures/gui/icons.png");

    public static final int SKIN_TEXTURE_WIDTH = 8;
    public static final int SKIN_TEXTURE_HEIGHT = 8;
    public static final int u = 8;
    public static final int v = 8;
    private static final Logger LOGGER = LoggerFactory.getLogger("gamehighlighter");
    private boolean isHighlited;
    private HashSet<Prefix> prefixes;

    public PlayerEditScreen(HighlightPlayer highlightPlayer, Screen parent) {
        super(Text.translatable("gui.playerEdit.title", highlightPlayer.name()));
        this.highlightPlayer = highlightPlayer;
        this.client = MinecraftClient.getInstance();
        this.parent = parent;
        this.isHighlited = GameHighlighterClient.getClientConfig().isHighlighted(highlightPlayer.uuid());
        this.prefixes = GameHighlighterClient.getClientConfig().getAllPrefixes();

    }
    private final HighlightPlayer highlightPlayer;
    private final Screen parent;
    private final MinecraftClient client;

    private String highlightedText() {
        this.isHighlited = GameHighlighterClient.getClientConfig().isHighlighted(highlightPlayer.uuid());
        if (isHighlited) {
            Prefix prefix = HighlitedPlayer.getHighlitedPlayer(highlightPlayer.uuid()).getPrefix();
            if (prefix!=null) {
                return prefix.getPrefixTag() + " (" + prefix.getPrefixChar() + ")";
            }else {
                return Text.translatable("gui.playerEdit.label.highlight").getString();
            }
        } else {
            return Text.translatable("gui.playerEdit.label.highlight").getString();
        }
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        this.prefixes = GameHighlighterClient.getClientConfig().getAllPrefixes();
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
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
        ParentComponent playerInfo = Containers.horizontalFlow(Sizing.fixed(276-32), Sizing.content())
                .child(player_head)
                .child(nickname);
        ScrollDropdownComponent scrollDropdownComponent = new ScrollDropdownComponent(Sizing.fixed(150), Sizing.content(), Text.literal(highlightedText()), false);

        for (Prefix prefix : prefixes) {
            scrollDropdownComponent.button(Text.literal(prefix.getPrefixTag()+" ("+ prefix.getPrefixChar() +")"), button -> {
                HighlitedPlayer HPlayer = HighlitedPlayer.getHighlitedPlayer(highlightPlayer.uuid());
                HPlayer.highlight(prefix.getPrefixId());
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
        ButtonComponent edit = Components.button(Text.translatable("gui.playerEdit.button.edit"), (comp) -> {
            Prefix prefix = HighlitedPlayer.getHighlitedPlayer(highlightPlayer.uuid()).getPrefix();
            if (prefix!=null) {
                client.setScreen(new PrefixEditScreen(Text.translatable("gui.playerEdit.button.edit"), this, prefix));
            }
        }).active(isHighlited);
        ButtonComponent save = Components.button(Text.translatable("gui.playerEdit.button.save"), (comp) -> {
            client.setScreen(parent);
        });
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
        ParentComponent editContainer = Containers.horizontalFlow(Sizing.fixed(276), Sizing.content())
                .child(scrollDropdown)
                .child(edit.margins(Insets.of(5,5,0,0)).sizing(Sizing.fixed(51), Sizing.fixed(20)))
                .child(save.margins(Insets.of(5,5,5,0)).sizing(Sizing.fixed(51), Sizing.fixed(20)))
//                .child(checkbox.margins(Insets.of(5,5,5,5)))
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .padding(Insets.of(2,2,2,2))
                .surface(Surface.DARK_PANEL);
        ParentComponent titleContainer = Containers.horizontalFlow(Sizing.fixed(276), Sizing.fixed(30))
                .child(playerInfo)
                .child(buttonWidget)
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .padding(Insets.of(5,5,5,5))
                .surface(Surface.DARK_PANEL);
        ParentComponent container = Containers.verticalFlow(Sizing.fixed(276), Sizing.content())
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
