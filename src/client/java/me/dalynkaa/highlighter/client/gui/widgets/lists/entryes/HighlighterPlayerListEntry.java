package me.dalynkaa.highlighter.client.gui.widgets.lists.entryes;

import lombok.Getter;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.adapters.ColorAdapter;
import me.dalynkaa.highlighter.client.gui.HighlightScreen;
import me.dalynkaa.highlighter.client.utilities.data.HighlightPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class HighlighterPlayerListEntry extends ElementListWidget.Entry<HighlighterPlayerListEntry>{

    public static final int GRAY_COLOR;
    public static final int DARK_GRAY_COLOR;
    public static final int WHITE_COLOR;
    public static final int LIGHT_GRAY_COLOR;
    private static final Identifier HIGHLIGHT_ICON = Identifier.of(Highlighter.MOD_ID,"highlight");
    private static final Identifier HIGHLIGHT_ICON_FOCUSED = Identifier.of(Highlighter.MOD_ID,"highlightfocus");
    private static final ButtonTextures HIGHLIGHT_BUTTON_ICON = new ButtonTextures(
        HIGHLIGHT_ICON,
        HIGHLIGHT_ICON_FOCUSED
    );


    @Nullable
    private ButtonWidget highlightButton;


    @Getter
    private final UUID uuid;
    @Getter
    private final String name;
    private final MinecraftClient client;
    private final HighlightScreen parent;
    private final Supplier<SkinTextures> skinTexture;
    private final Boolean hideble;
    private final List<ClickableWidget> buttons;
    public HighlighterPlayerListEntry(MinecraftClient client, HighlightScreen parent, UUID uuid, String name, Supplier<SkinTextures> skinTexture, boolean hideble) {
        this.client = client;
        this.parent = parent;
        this.uuid = uuid;
        this.name = name;
        this.skinTexture = skinTexture;
        this.hideble = hideble;
        this.buttons = new ArrayList<>();
        this.highlightButton = new TexturedButtonWidget(0,0,20,20,HIGHLIGHT_BUTTON_ICON, button -> {
            parent.setCurrentPlayer(new HighlightPlayer(uuid,name,skinTexture.get()));
        }, Text.translatable("gui.highlighter.menu.button.edit"));
        //this.highlightButton.tooltip(Text.translatable("gui.highlighter.menu.button.edit.tooltip"));

        this.buttons.add(this.highlightButton);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return this.buttons;
    }

    @Override
    public List<? extends Element> children() {
        return this.buttons;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int i = x+2 ;
        int j = y + (entryHeight - 24) / 2;
        int k = i + 24 + 4;
        int l = y + (entryHeight - 8) / 2;
        boolean isMouseOver = this.isMouseOver(mouseX, mouseY);
        context.fill(x-2, y, x + entryWidth-2, y + entryHeight, isMouseOver ? LIGHT_GRAY_COLOR : GRAY_COLOR);

        PlayerSkinDrawer.draw(context, this.skinTexture.get(), i, j, 24);
        context.drawText(this.client.textRenderer, this.name, k, l, WHITE_COLOR, false);

        if (this.highlightButton != null) {
            this.highlightButton.setX(x + (entryWidth - this.highlightButton.getWidth() - 4) - 4);
            this.highlightButton.setY(y + (entryHeight - this.highlightButton.getHeight()) / 2);
            this.highlightButton.render(context, mouseX, mouseY, tickDelta);
        }
    }

    static {
        GRAY_COLOR = ColorAdapter.getArgb(255, 74, 74, 74);
        DARK_GRAY_COLOR = ColorAdapter.getArgb(255, 48, 48, 48);
        WHITE_COLOR = ColorAdapter.getArgb(255, 255, 255, 255);
        LIGHT_GRAY_COLOR = ColorAdapter.getArgb(100, 255, 255, 255);
    }

}
