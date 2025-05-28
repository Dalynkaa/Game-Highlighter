package me.dalynkaa.highlighter.client.gui.widgets.lists.entryes;

import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import lombok.Getter;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.gui.HighlightScreen;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HighlighterPrefixListEntry extends ElementListWidget.Entry<HighlighterPrefixListEntry>{
    public static final int GRAY_COLOR;
    public static final int DARK_GRAY_COLOR;
    public static final int WHITE_COLOR;
    public static final int LIGHT_GRAY_COLOR;
    private static final Identifier HIGHLIGHT_ICON = Identifier.of(Highlighter.MOD_ID,"textures/gui/icons.png");


    @Nullable
    private final ButtonWidget highlightButton;
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
        this.highlightButton = new LegacyTexturedButtonWidget(0, 0, 20, 20, 0, 32, 20, HIGHLIGHT_ICON, 256, 256, button -> {
            parent.setCurrentPrefix(prefix);
        }, Text.translatable("gui.gamehighlighter.un_highlighted"));
        this.upButton = new LegacyTexturedButtonWidget(0, 0, 10, 10, 0, 12, 10, HIGHLIGHT_ICON, 256, 256, button -> {
            prefix.movePrefixTop();
            parent.updatePrefixList();
        }, Text.translatable("gui.gamehighlighter.un_highlighted"));
        this.downButton = new LegacyTexturedButtonWidget(0, 0, 10, 10, 10, 12, 10, HIGHLIGHT_ICON, 256, 256, button -> {
            prefix.movePrefixDown();
            parent.updatePrefixList();
        }, Text.translatable("gui.gamehighlighter.un_highlighted"));
        this.buttons.add(this.highlightButton);
        this.buttons.add(this.upButton);
        this.buttons.add(this.downButton);
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
            prefixColor = ColorHelper.Argb.getArgb(255, 
                Integer.parseInt(this.prefix.getPrefixColor().substring(1, 3), 16),
                Integer.parseInt(this.prefix.getPrefixColor().substring(3, 5), 16),
                Integer.parseInt(this.prefix.getPrefixColor().substring(5, 7), 16));
        } catch (Exception e) {
            // Используем белый цвет по умолчанию при ошибке парсинга
        }
        
        context.getMatrices().push();
        
        context.getMatrices().translate(iconX, iconCenterY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-iconX / scale, -iconCenterY / scale, 0);
        
        context.drawText(this.client.textRenderer, this.prefix.getPrefixChar(),
            (int)(iconX / scale), (int)(iconCenterY / scale)+1, prefixColor, false);
            
        context.getMatrices().pop();
        
        int nameX = iconX + 50;
        int nameY = y + (entryHeight - 8) / 2;
        context.drawTextWithShadow(this.client.textRenderer, this.prefix.getPrefixTag(), 
            nameX, nameY, WHITE_COLOR);

        if (this.highlightButton != null) {
            this.highlightButton.setX(x + (entryWidth - this.highlightButton.getWidth() - 4) - 14);
            this.highlightButton.setY(y + (entryHeight - this.highlightButton.getHeight()) / 2);
            this.highlightButton.render(context, mouseX, mouseY, tickDelta);
        }
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

    public String getName() {
        if (this.prefix == null) return "";
        return this.prefix.getPrefixTag();
    }

    static {
        GRAY_COLOR = ColorHelper.Abgr.getAbgr(255, 74, 74, 74);
        DARK_GRAY_COLOR = ColorHelper.Abgr.getAbgr(255, 48, 48, 48);
        WHITE_COLOR = ColorHelper.Abgr.getAbgr(255, 255, 255, 255);
        LIGHT_GRAY_COLOR = ColorHelper.Abgr.getAbgr(100, 255, 255, 255);
    }
}
