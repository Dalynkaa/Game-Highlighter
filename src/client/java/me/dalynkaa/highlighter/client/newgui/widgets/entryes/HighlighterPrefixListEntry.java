package me.dalynkaa.highlighter.client.newgui.widgets.entryes;

import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import lombok.Getter;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.newgui.HighlightScreen;
import me.dalynkaa.highlighter.client.utilities.data.HighlightPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class HighlighterPrefixListEntry extends ElementListWidget.Entry<HighlighterPrefixListEntry>{
    public static final int GRAY_COLOR;
    public static final int DARK_GRAY_COLOR;
    public static final int WHITE_COLOR;
    public static final int LIGHT_GRAY_COLOR;
    private static final Identifier HIGHLIGHT_ICON = Identifier.of(Highlighter.MOD_ID,"textures/gui/icons.png");


    @Nullable
    private ButtonWidget highlightButton;

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

        }, Text.translatable("gui.gamehighlighter.un_highlighted"));
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY) && button == 0) {
            // Открываем префикс для редактирования при клике на его элемент в списке
            if (this.parent != null) {
                this.parent.setCurrentPrefix(this.prefix);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

        boolean isMouseOver = this.isMouseOver(mouseX, mouseY);
        context.fill(x-2, y, x + entryWidth-2, y + entryHeight, isMouseOver ? LIGHT_GRAY_COLOR : GRAY_COLOR);
        
        // Отрисовка символа префикса (значка) слева
        float scale = 2.0f; // Увеличиваем масштаб для большей иконки
        int prefixIconSize = (int)(12 * scale);
        
        // Лучше центрируем по вертикали
        int iconCenterY = (int) (y + (entryHeight / 2) - (this.client.textRenderer.fontHeight * scale) / 2);
        int iconX = x + 10; // Немного отступаем от края
        
        // Отображаем символ префикса (getPrefixChar) с его цветом
        int prefixColor = WHITE_COLOR;
        try {
            prefixColor = ColorHelper.Argb.getArgb(255, 
                Integer.parseInt(this.prefix.getPrefixColor().substring(1, 3), 16),
                Integer.parseInt(this.prefix.getPrefixColor().substring(3, 5), 16),
                Integer.parseInt(this.prefix.getPrefixColor().substring(5, 7), 16));
        } catch (Exception e) {
            // Используем белый цвет по умолчанию при ошибке парсинга
        }
        
        // Сохраняем текущее состояние матрицы трансформации
        context.getMatrices().push();
        
        // Применяем масштабирование вокруг правильной точки
        context.getMatrices().translate(iconX, iconCenterY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-iconX / scale, -iconCenterY / scale, 0);
        
        // Отрисовка символа префикса в увеличенном масштабе
        context.drawText(this.client.textRenderer, this.prefix.getPrefixChar(),
            (int)(iconX / scale), (int)(iconCenterY / scale)+1, prefixColor, false);
            
        // Восстанавливаем матрицу трансформации
        context.getMatrices().pop();
        int width = this.client.textRenderer.getWidth(this.prefix.getPrefixChar());
        
        // Отрисовка названия префикса справа от символа
        int nameX = (int) (iconX + (width*scale)); // Отступ от символа
        int nameY = y + (entryHeight - 8) / 2; // центрирование по вертикали
        context.drawTextWithShadow(this.client.textRenderer, this.prefix.getPrefixTag(), 
            nameX, nameY, WHITE_COLOR);
        
        // Позиционирование кнопки

        if (this.highlightButton != null) {
            this.highlightButton.setX(x + (entryWidth - this.highlightButton.getWidth() - 4) - 4);
            this.highlightButton.setY(y + (entryHeight - this.highlightButton.getHeight()) / 2);
            this.highlightButton.render(context, mouseX, mouseY, tickDelta);
        }
    }

    public String getName() {
        if (this.prefix == null) return "";
        return this.prefix.getPrefixTag();
    }
    
    public boolean isMouseOver(double mouseX, double mouseY) {
        return buttons.stream().anyMatch(button -> button.isMouseOver(mouseX, mouseY)) || 
               parent != null && 
               mouseX >= 0 && mouseY >= 0 && 
               mouseX < parent.width && mouseY < parent.height;
    }

    static {
        GRAY_COLOR = ColorHelper.Abgr.getAbgr(255, 74, 74, 74);
        DARK_GRAY_COLOR = ColorHelper.Abgr.getAbgr(255, 48, 48, 48);
        WHITE_COLOR = ColorHelper.Abgr.getAbgr(255, 255, 255, 255);
        LIGHT_GRAY_COLOR = ColorHelper.Abgr.getAbgr(100, 255, 255, 255);
    }
}
