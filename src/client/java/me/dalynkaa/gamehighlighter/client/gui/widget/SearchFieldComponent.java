package me.dalynkaa.gamehighlighter.client.gui.widget;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SearchFieldComponent extends FlowLayout {
    public SearchFieldComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        TextFieldWidget textFieldWidget = new TextFieldWidget(textRenderer, 0, 0, 200, 15, Text.literal(""));
        super.child(Components.wrapVanillaWidget(textFieldWidget));
    }
}
