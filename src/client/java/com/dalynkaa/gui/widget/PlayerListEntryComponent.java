package com.dalynkaa.gui.widget;

import com.dalynkaa.gui.screens.HighlightListScreen;
import com.dalynkaa.gui.screens.PlayerEditScreen;
import com.dalynkaa.utilities.data.HighlightPlayer;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class PlayerListEntryComponent extends FlowLayout {
    private static final Identifier HIGHLIGHT_ICON = new Identifier("gamehighlighter","textures/gui/icons.png");
    private final MinecraftClient client = MinecraftClient.getInstance();


    public PlayerListEntryComponent(PlayerListEntry entry) {
        super(Sizing.fill(99), Sizing.fixed(30), Algorithm.HORIZONTAL);
        super.padding(Insets.of(5,5,5,5));
        PlayerHeadComponent head = new PlayerHeadComponent(Sizing.fixed(20), Sizing.fixed(20), entry.getSkinTexture());
        Component player = Containers.horizontalFlow(Sizing.fixed(167), Sizing.fixed(35))
                .child(Components.label(Text.literal(entry.getProfile().getName()).styled((style) -> style.withColor(TextColor.fromFormatting(Formatting.WHITE)))));
        player.margins(Insets.of(5,0,5,0));
        ButtonWidget buttonComponent = new TexturedButtonWidget(0, 0, 20, 20, 0, 32, 20, HIGHLIGHT_ICON, 256, 256, button -> client.setScreen(new PlayerEditScreen(new HighlightPlayer(entry.getProfile().getId(), entry.getProfile().getName(),entry.getSkinTexture()), new HighlightListScreen())),Text.translatable("gui.gamehighlighter.un_highlighted"));
        VanillaWidgetComponent buttonWidget = Components.wrapVanillaWidget(buttonComponent);
        super.surface(Surface.DARK_PANEL);
        super.margins(Insets.of(0,0,0,0));
        super.child(head);
        super.child(player);
        super.child(buttonWidget);

    }
}
