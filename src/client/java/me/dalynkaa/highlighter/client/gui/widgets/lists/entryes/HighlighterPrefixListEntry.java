package me.dalynkaa.highlighter.client.gui.widgets.lists.entryes;

import lombok.Getter;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.adapters.ColorAdapter;
import me.dalynkaa.highlighter.client.gui.HighlightScreen;
import me.dalynkaa.highlighter.client.utilities.data.HighlightPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import me.dalynkaa.highlighter.client.utilities.data.PrefixSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class HighlighterPrefixListEntry extends ElementListWidget.Entry<HighlighterPrefixListEntry>{
    public static final int GRAY_COLOR;
    public static final int DARK_GRAY_COLOR;
    public static final int WHITE_COLOR;
    public static final int LIGHT_GRAY_COLOR;
    private static final Identifier HIGHLIGHT_ICON = Identifier.of(Highlighter.MOD_ID,"edit");
    private static final Identifier HIGHLIGHT_ICON_FOCUSED = Identifier.of(Highlighter.MOD_ID,"edit-hovered");
    private static final Identifier HIGHLIGHT_ICON_DISABLED = Identifier.of(Highlighter.MOD_ID,"edit-disabled");
    private static final Identifier DELETE_ICON = Identifier.of(Highlighter.MOD_ID,"delete");
    private static final Identifier DELETE_ICON_FOCUSED = Identifier.of(Highlighter.MOD_ID,"delete-hovered");
    private static final ButtonTextures HIGHLIGHT_BUTTON_ICON = new ButtonTextures(
            HIGHLIGHT_ICON,
            HIGHLIGHT_ICON_DISABLED,
            HIGHLIGHT_ICON_FOCUSED
    );
    private static final ButtonTextures DELETE_BUTTON_ICON = new ButtonTextures(
            DELETE_ICON,
            DELETE_ICON_FOCUSED
    );
    private static final ButtonTextures HIGHLIGHT_ICON_UP = new ButtonTextures(
            Identifier.of(Highlighter.MOD_ID,"up"),
            Identifier.of(Highlighter.MOD_ID,"upf")
    );
    private static final ButtonTextures HIGHLIGHT_ICON_DOWN = new ButtonTextures(
            Identifier.of(Highlighter.MOD_ID,"down"),
            Identifier.of(Highlighter.MOD_ID,"downf")
    );



    @Nullable
    private final ButtonWidget highlightButton;
    @Nullable
    private final ButtonWidget deleteButton;
    @Nullable
    private final ButtonWidget upButton;
    @Nullable
    private final ButtonWidget downButton;

    @Getter
    private Prefix prefix;
    private final MinecraftClient client;
    private final HighlightScreen parent;
    private final List<ClickableWidget> buttons;

    public HighlighterPrefixListEntry(MinecraftClient client, HighlightScreen parent, Prefix prefix) {
        this.client = client;
        this.parent = parent;
        this.buttons = new ArrayList<>();
        this.prefix = prefix;
        this.highlightButton = new TexturedButtonWidget(0, 0, 20, 20,HIGHLIGHT_BUTTON_ICON, button -> {
            parent.setCurrentPrefix(prefix);
        }, Text.translatable("gui.highlighter.menu.button.edit"));
        this.highlightButton.setTooltip(Tooltip.of(Text.translatable("gui.highlighter.menu.button.edit.tooltip")));
        this.deleteButton = new TexturedButtonWidget(0, 0, 20, 20, DELETE_BUTTON_ICON, button -> {
            parent.deletePrefix(prefix);
        }, Text.translatable("gui.highlighter.menu.button.delete"));
        this.upButton = new TexturedButtonWidget(0, 0, 10, 10,HIGHLIGHT_ICON_UP, button -> {
            prefix.movePrefixTop();
            parent.updatePrefixList();
        }, Text.translatable("gui.highlighter.menu.button.move_up"));
        this.upButton.setTooltip(Tooltip.of(Text.translatable("gui.highlighter.menu.button.move_up.tooltip")));
        this.downButton = new TexturedButtonWidget(0, 0, 10, 10, HIGHLIGHT_ICON_DOWN, button -> {
            prefix.movePrefixDown();
            parent.updatePrefixList();
        }, Text.translatable("gui.highlighter.menu.button.move_down"));
        this.downButton.setTooltip(Tooltip.of(Text.translatable("gui.highlighter.menu.button.move_down.tooltip")));
        if (prefix.getSource().equals(PrefixSource.LOCAL)){
            this.buttons.add(this.highlightButton);
            this.buttons.add(this.deleteButton);
            this.buttons.add(this.upButton);
            this.buttons.add(this.downButton);
        }


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

        boolean isMouseOver = this.isMouseOver(mouseX, mouseY);
        context.fill(x-2, y, x + entryWidth-2, y + entryHeight, isMouseOver ? LIGHT_GRAY_COLOR : GRAY_COLOR);
        float scale = 2.0f;
        int iconCenterY = (int) (y + ((float) entryHeight / 2) - (this.client.textRenderer.fontHeight * scale) / 2);
        int iconX = x + 10;
        
        int prefixColor = WHITE_COLOR;
        try {
            prefixColor = ColorAdapter.getArgb(255,
                Integer.parseInt(this.prefix.getPrefixColor().substring(1, 3), 16),
                Integer.parseInt(this.prefix.getPrefixColor().substring(3, 5), 16),
                Integer.parseInt(this.prefix.getPrefixColor().substring(5, 7), 16));
        } catch (Exception e) {
            // Используем белый цвет по умолчанию при ошибке парсинга
        }
        
        //? if >=1.21.6 {
        /*context.getMatrices().pushMatrix();
        context.getMatrices().translate(iconX, iconCenterY);
        context.getMatrices().scale(scale, scale);
        context.getMatrices().translate(-iconX / scale, -iconCenterY / scale);
        context.drawText(this.client.textRenderer, this.prefix.getPrefixChar(),
                (int)(iconX / scale), (int)(iconCenterY / scale)+1, prefixColor, false);

        context.getMatrices().popMatrix();
        *///?} else {
        context.getMatrices().push();
        
        context.getMatrices().translate(iconX, iconCenterY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-iconX / scale, -iconCenterY / scale, 0);
        
        context.drawText(this.client.textRenderer, this.prefix.getPrefixChar(),
            (int)(iconX / scale), (int)(iconCenterY / scale)+1, prefixColor, false);
            
        context.getMatrices().pop();
        //?}
        
//        int nameX = iconX + 100;
//        int nameY = y + (entryHeight - 8) / 2;
//        context.drawTextWithShadow(this.client.textRenderer, this.prefix.getPrefixTag(),
//            nameX, nameY, WHITE_COLOR);
        int offset = parent.isPrefixAlone() ? 4 : 14;
        if (prefix.getSource().equals(PrefixSource.LOCAL)){
            if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(),InputUtil.GLFW_KEY_LEFT_SHIFT)){
                if (highlightButton!=null){
                    highlightButton.active = false;
                }
                if (this.deleteButton != null) {
                    this.deleteButton.setX(x + (entryWidth - this.deleteButton.getWidth()- 4) - offset);
                    this.deleteButton.setY(y + (entryHeight - this.deleteButton.getHeight()) / 2);
                    this.deleteButton.render(context, mouseX, mouseY, tickDelta);
                }
            }else {
                if (this.highlightButton != null) {
                    highlightButton.active = true;
                    this.highlightButton.setX(x + (entryWidth - this.highlightButton.getWidth() - 4) - offset);
                    this.highlightButton.setY(y + (entryHeight - this.highlightButton.getHeight()) / 2);
                    this.highlightButton.render(context, mouseX, mouseY, tickDelta);
                }
            }

            if (!parent.isPrefixAlone()) {
                if (this.upButton != null && !prefix.isFirstPrefix()) {
                    this.upButton.setX(x + (entryWidth - this.upButton.getWidth() - 4)-2);
                    this.upButton.setY(y + ((entryHeight - this.upButton.getHeight()) / 2)-upButton.getHeight()/2);
                    this.upButton.render(context, mouseX, mouseY, tickDelta);
                }
                if (this.downButton != null && !prefix.isLatestPrefix()) {
                    this.downButton.setX(x + (entryWidth - this.downButton.getWidth() - 4)-2);
                    this.downButton.setY(y + ((entryHeight - this.downButton.getHeight()) / 2)+downButton.getHeight()/2);
                    this.downButton.render(context, mouseX, mouseY, tickDelta);
                }
            }
        }
    }

    public String getName() {
        if (this.prefix == null) return "";
        return this.prefix.getPrefixTag();
    }

    static {
        GRAY_COLOR = ColorAdapter.getArgb(255, 74, 74, 74);
        DARK_GRAY_COLOR = ColorAdapter.getArgb(255, 48, 48, 48);
        WHITE_COLOR = ColorAdapter.getArgb(255, 255, 255, 255);
        LIGHT_GRAY_COLOR = ColorAdapter.getArgb(100, 255, 255, 255);
    }
}
