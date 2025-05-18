package me.dalynkaa.highlighter.client.newgui.widgets;

import lombok.Setter;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.newgui.focus.FocusManager;
import me.dalynkaa.highlighter.client.newgui.widgets.helper.NestedGuiGroup;
import me.dalynkaa.highlighter.client.utilities.data.HighlightPlayer;
import me.dalynkaa.highlighter.client.utilities.data.HighlightedPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.*;

public class HighlighterPlayerEditWidget extends NestedGuiGroup implements Drawable {

    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("social_interactions/background");

    @Setter
    private HighlightPlayer highlightPlayer;

    private final TextRenderer textRenderer;

    private int x;
    private int y;
    private int width;
    private int height;

    private final HighlighterDropdownWidget dropdown;

    // Состояние фокуса для этого виджета
    private boolean focused = false;

    public HighlighterPlayerEditWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;

        Map<String, String> manyOptions = new LinkedHashMap<>();
        HashSet<Prefix> prefixes = HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefixes();
        for (Prefix prefix : prefixes) {
            manyOptions.put(prefix.getPrefixId().toString(), prefix.getPrefixTag());
        }

        this.dropdown = new HighlighterDropdownWidget(x + 8, y + 58, width - 16, 20, manyOptions, null, Text.literal("Select prefix"),
                (selectedOption) -> {
                    Highlighter.LOGGER.info("Selected option: {}", selectedOption);
                    if (selectedOption != null) {
                        Prefix selectedPrefix = HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefix(UUID.fromString(selectedOption));
                        Highlighter.LOGGER.info("Selected prefix: {}", selectedPrefix);
                        if (selectedPrefix != null) {
                            HighlightedPlayer highlightedPlayer = HighlighterClient.getServerEntry().getHighlitedPlayer(highlightPlayer.uuid());
                            Highlighter.LOGGER.info("Highlighted player: {}", highlightedPlayer);
                            if (highlightedPlayer != null) {
                                highlightedPlayer.highlight(selectedPrefix.getPrefixId());
                                HighlighterClient.getServerEntry().setPlayer(highlightedPlayer);
                            }
                        }
                    }
                });

        // Регистрируем этот контейнер в системе управления фокусом
        registerWithFocusManager();
    }

    public void setX(int x) {
        this.x = x;
        this.dropdown.setX(x + 8);
    }

    public void setY(int y) {
        this.y = y;
        this.dropdown.setY(y + 58);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, "Player Edit", this.x + width/2, this.y - 12, 0xFFFFFF);
        context.fill(this.x + 8, this.y + 8, this.x + this.width - 8, this.y + 40, 0x80000000);
        context.drawHorizontalLine(this.x + 7, this.x + this.width - 8, this.y + 40, ColorHelper.Abgr.getAbgr(255, 198, 198, 198));

        int i = x + 12;
        int j = y + 8 + 4;
        if (highlightPlayer != null) {
            PlayerSkinDrawer.draw(context, this.highlightPlayer.skinTexture(), i, j, 23);
            context.drawText(textRenderer, this.highlightPlayer.name(), i + 24 + 4, j + 4, 0xFFFFFF, true);
        }

        this.dropdown.render(context, mouseX, mouseY, delta);
    }

    private void renderBackground(DrawContext context) {
        context.drawGuiTexture(BACKGROUND_TEXTURE, this.x, this.y, this.width, this.height);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // Optional accessibility
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    // Override these methods to ensure proper focus handling
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    protected List<? extends Element> getGuiChildren() {
        return List.of(dropdown);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Сначала обрабатываем клик по dropdown независимо от проверки isMouseOver - принудительно проверяем
        if (dropdown != null) {
            // Логируем координаты клика для отладки
            Highlighter.LOGGER.info("Проверяем клик по dropdown: mouseX={}, mouseY={}, isDropdownMouseOver={}, isDropdownExpandedHovered={}",
                mouseX, mouseY,
                dropdown.isMouseOver(mouseX, mouseY),
                dropdown.isExpandedDropdownHovered(mouseX, mouseY));

            // Проверяем, находится ли клик над основным виджетом выпадающего списка
            if (dropdown.isMouseOver(mouseX, mouseY)) {
                Highlighter.LOGGER.info("Клик обнаружен над dropdown, передаем событие");
                boolean handled = dropdown.mouseClicked(mouseX, mouseY, button);
                if (handled) {
                    Highlighter.LOGGER.info("Dropdown обработал клик!");
                    return true;
                }
            }

            // Проверяем, находится ли клик над раскрытым списком
            if (dropdown.isExpandedDropdownHovered(mouseX, mouseY)) {
                Highlighter.LOGGER.info("Клик обнаружен над раскрытым dropdown, передаем событие");
                boolean handled = dropdown.mouseClicked(mouseX, mouseY, button);
                if (handled) {
                    Highlighter.LOGGER.info("Раскрытый dropdown обработал клик!");
                    return true;
                }
            }
        }

        // Если dropdown не обработал клик, используем стандартную логику
        Highlighter.LOGGER.info("Dropdown не обработал клик, вызываем родительский метод");
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Используем родительскую реализацию, которая теперь интегрирована с FocusManager
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPointInside(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return this.focused;
    }
}
