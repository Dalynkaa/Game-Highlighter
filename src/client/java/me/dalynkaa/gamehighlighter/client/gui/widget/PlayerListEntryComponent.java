package me.dalynkaa.gamehighlighter.client.gui.widget;

import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import me.dalynkaa.gamehighlighter.client.gui.screens.HighlightListScreen;
import me.dalynkaa.gamehighlighter.client.gui.screens.PlayerEditScreen;
import me.dalynkaa.gamehighlighter.client.utilities.data.HighlightPlayer;
import me.dalynkaa.gamehighlighter.client.utilities.data.Prefix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class PlayerListEntryComponent extends FlowLayout {
    private static final Identifier HIGHLIGHT_ICON = Identifier.of("gamehighlighter","textures/gui/icons.png");

    private final MinecraftClient client = MinecraftClient.getInstance();


    public PlayerListEntryComponent(PlayerListEntry entry, Prefix prefix) {

        super(Sizing.fill(99), Sizing.fixed(30), Algorithm.HORIZONTAL);
        super.padding(Insets.of(5,5,5,5));
        PlayerHeadComponent head = new PlayerHeadComponent(Sizing.fixed(20), Sizing.fixed(20), entry.getSkinTextures().texture());
        Component player;
        if (prefix == null){
            player = Containers.horizontalFlow(Sizing.fixed(167), Sizing.fixed(35))
                    .child(Components.label(Text.literal(entry.getProfile().getName()).styled((style) -> style.withColor(TextColor.fromFormatting(Formatting.WHITE)))));
        }else {
            player = Containers.horizontalFlow(Sizing.fixed(167), Sizing.fixed(35))
                    .child(Components.label(prefix.getPrefixText(Text.literal(entry.getProfile().getName()))));
        }
        player.margins(Insets.of(5,0,5,0));
        ButtonWidget buttonComponent = new LegacyTexturedButtonWidget(0, 0, 20, 20, 0, 32, 20, HIGHLIGHT_ICON, 256, 256, button -> client.setScreen(new PlayerEditScreen(new HighlightPlayer(entry.getProfile().getId(), entry.getProfile().getName(),entry.getSkinTextures().texture()), new HighlightListScreen())),Text.translatable("gui.gamehighlighter.un_highlighted"));
        VanillaWidgetComponent buttonWidget = Components.wrapVanillaWidget(buttonComponent);
        super.surface(Surface.DARK_PANEL);
        super.margins(Insets.of(0,0,0,0));
        super.child(head);
        super.child(player);
        super.child(buttonWidget);

    }
}
