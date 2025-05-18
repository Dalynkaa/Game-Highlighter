package me.dalynkaa.highlighter.client.newgui.widgets;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.newgui.widgets.helper.BasicFocusableWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HighlighterDropdownWidget extends BasicFocusableWidget {
    private final Map<String, String> options;
    private String selectedId;
    private boolean expanded = false;
    private final Text label;

    private final int optionHeight = 20;
    private int scrollOffset = 0;
    private final int maxVisibleOptions = 5;
    private final Consumer<String> selectChangeConsumer;

    public HighlighterDropdownWidget(int x, int y, int width, int height, Map<String, String> options, String initialId, Text label, Consumer<String> selectChangeConsume) {
        super(x, y, width, height, Text.literal(options.getOrDefault(initialId, "Select")));
        this.options = new LinkedHashMap<>(options);
        this.selectedId = initialId;
        this.label = label;
        this.selectChangeConsumer = selectChangeConsume;
    }

    public String getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(String id) {
        this.selectedId = id;
        this.setMessage(Text.literal(options.getOrDefault(id, "Select")));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            expanded = !expanded;
            playDownSound(MinecraftClient.getInstance().getSoundManager());
            return true;
        }

        if (!expanded) {
            return false;
        }

        // Проверка клика по раскрытому списку опций
        int yOffset = getY() + getHeight();
        List<Map.Entry<String, String>> entryList = options.entrySet().stream().toList();
        int visible = Math.min(maxVisibleOptions, entryList.size());
        int totalDropdownHeight = visible * optionHeight;

        // Если клик в пределах раскрытого списка, обрабатываем его
        if (mouseX >= getX() && mouseX <= getX() + getWidth() &&
            mouseY >= yOffset && mouseY <= yOffset + totalDropdownHeight) {

            for (int i = 0; i < visible; i++) {
                int index = scrollOffset + i;
                if (index >= entryList.size()) break;

                int entryY = yOffset + i * optionHeight;
                if (mouseY >= entryY && mouseY <= entryY + optionHeight) {
                    setSelectedId(entryList.get(index).getKey());
                    selectChangeConsumer.accept(entryList.get(index).getKey());
                    expanded = false;
                    playDownSound(MinecraftClient.getInstance().getSoundManager());
                    return true;
                }
            }

            // Клик внутри раскрытого списка, но не по конкретной опции (например, по полосе прокрутки)
            return true;
        }

        // Клик вне виджета - скрываем список
        expanded = false;
        return false;
    }

    /**
     * Проверяет, находится ли точка внутри раскрытого списка
     */
    public boolean isExpandedDropdownHovered(double mouseX, double mouseY) {
        if (!expanded) return false;

        int yOffset = getY() + getHeight();
        int visible = Math.min(maxVisibleOptions, options.size());
        int totalHeight = visible * optionHeight;

        return mouseX >= getX() && mouseX <= getX() + getWidth() &&
               mouseY >= yOffset && mouseY <= yOffset + totalHeight;
    }

    /**
     * Проверяет, находится ли точка над виджетом, включая раскрытый список
     */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // Используем базовую проверку только для основного виджета, без раскрытого списка
        return super.isMouseOver(mouseX, mouseY);
    }

    /**
     * Проверяет, находится ли точка над виджетом или его раскрытым списком
     */
    @Override
    public boolean isExtendedHitbox(double mouseX, double mouseY) {
        boolean overMainWidget = super.isMouseOver(mouseX, mouseY);
        boolean overExpandedList = isExpandedDropdownHovered(mouseX, mouseY);

        Highlighter.LOGGER.debug("Dropdown hitbox check: regular={}, expanded={}, result={}",
                overMainWidget, overExpandedList, (overMainWidget || overExpandedList));

        return overMainWidget || overExpandedList;
    }

    // This is the method Fabric 1.21.1 is using
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Debug log to verify this method is called
        Highlighter.LOGGER.info("Dropdown mouseScrolled called: x=" + mouseX + ", y=" + mouseY + ", h=" + horizontalAmount + ", v=" + verticalAmount);

        if (!expanded) {
            return false;
        }

        // Expanded dropdown coordinates
        int expandedTop = getY() + getHeight();
        int expandedBottom = expandedTop + (optionHeight * Math.min(maxVisibleOptions, options.size()));

        // If mouse not over expanded part - don't process
        if (!(mouseX >= getX() && mouseX <= getX() + getWidth() &&
                mouseY >= expandedTop && mouseY <= expandedBottom)) {
            return false;
        }

        int maxScroll = Math.max(0, options.size() - maxVisibleOptions);

        // Invert verticalAmount to match expected scroll direction
        if (verticalAmount < 0 && scrollOffset < maxScroll) {
            scrollOffset++;
            Highlighter.LOGGER.info("Scroll down: offset=" + scrollOffset);
            return true;
        } else if (verticalAmount > 0 && scrollOffset > 0) {
            scrollOffset--;
            Highlighter.LOGGER.info("Scroll up: offset=" + scrollOffset);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!expanded || button != 0) {
            return false;
        }

        // Если пользователь перетаскивает мышь в пределах раскрытого списка
        int yOffset = getY() + getHeight();
        int visible = Math.min(maxVisibleOptions, options.size());
        int totalHeight = visible * optionHeight;

        // Области перетаскивания
        boolean isOverDropdown = mouseX >= getX() && mouseX <= getX() + getWidth() &&
                                mouseY >= yOffset && mouseY <= yOffset + totalHeight;

        boolean isOverScrollbar = mouseX >= getX() + getWidth() - 4 && mouseX <= getX() + getWidth() &&
                                 mouseY >= yOffset && mouseY <= yOffset + totalHeight;

        // Если мышь над полосой прокрутки, обрабатываем перетаскивание для прокрутки
        if (isOverScrollbar || isOverDropdown) {
            int maxScroll = Math.max(0, options.size() - maxVisibleOptions);

            // Перемещение вниз (положительное deltaY) увеличивает scrollOffset
            if (deltaY > 0 && scrollOffset < maxScroll) {
                scrollOffset = Math.min(scrollOffset + 1, maxScroll);
                Highlighter.LOGGER.info("Drag down: scrollOffset=" + scrollOffset);
                return true;
            }
            // Перемещение вверх (отрицательное deltaY) уменьшает scrollOffset
            else if (deltaY < 0 && scrollOffset > 0) {
                scrollOffset = Math.max(scrollOffset - 1, 0);
                Highlighter.LOGGER.info("Drag up: scrollOffset=" + scrollOffset);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Возможно, здесь нам нужно сбросить какие-то флаги перетаскивания, если они будут добавлены позже
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        // Main block
        int backgroundColor = isHovered() ? 0xFF666666 : 0xFF444444;

        // Draw label
        context.drawText(textRenderer, label, getX() + 4, getY() - 12, 0xFFFFFF, false);

        // Draw dropdown
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), backgroundColor);
        context.drawBorder(getX(), getY(), getWidth(), getHeight(), 0xFF888888);
        context.drawText(textRenderer, getMessage(), getX() + 4, getY() + 6, 0xFFFFFF, false);
        context.drawText(textRenderer, expanded ? "▲" : "▼", getX() + getWidth() - 10, getY() + 6, 0xFFFFFF, false);

        if (expanded) {
            List<Map.Entry<String, String>> entryList = options.entrySet().stream().toList();

            int yOffset = getY() + getHeight();
            int visible = Math.min(maxVisibleOptions, entryList.size());

            // Draw dropdown options
            for (int i = 0; i < visible; i++) {
                int index = scrollOffset + i;
                if (index >= entryList.size()) break;

                Map.Entry<String, String> entry = entryList.get(index);
                boolean hovered = mouseX >= getX() && mouseX <= getX() + getWidth()
                        && mouseY >= yOffset && mouseY <= yOffset + optionHeight;

                int optionColor = hovered ? 0xFF888888 : 0xFF333333;
                context.fill(getX(), yOffset, getX() + getWidth(), yOffset + optionHeight, optionColor);
                context.drawText(textRenderer, Text.literal(entry.getValue()), getX() + 4, yOffset + 6, 0xFFFFFF, false);

                yOffset += optionHeight;
            }

            // Simple scrollbar
            int totalHeight = Math.min(options.size(), maxVisibleOptions) * optionHeight;
            int barHeight = Math.max(10, totalHeight * visible / Math.max(1, options.size()));
            int barY = getY() + getHeight() + (scrollOffset * (totalHeight - barHeight)) / Math.max(1, options.size() - visible);

            // Draw scrollbar track and thumb
            context.fill(getX() + getWidth() - 4, getY() + getHeight(), getX() + getWidth(), getY() + getHeight() + totalHeight, 0xFF222222);
            context.fill(getX() + getWidth() - 4, barY, getX() + getWidth(), barY + barHeight, 0xFF999999);
        }
    }
}

