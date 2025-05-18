package me.dalynkaa.highlighter.client.newgui.widgets.helper;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.adapters.GuiAdapter;
import me.dalynkaa.highlighter.client.newgui.HighlightScreen;
import me.dalynkaa.highlighter.client.newgui.focus.FocusManager;
import me.dalynkaa.highlighter.client.newgui.focus.FocusableContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Упрощённая абстракция для вложенных GUI-элементов с поддержкой делегирования событий и фокуса
 */
public abstract class NestedGuiGroup implements Element, Selectable, FocusableContainer {

    // Флаг, указывающий, что мы обрабатываем клик
    private boolean processingClick = false;
    // Добавляем флаг для отслеживания перетаскивания
    private boolean processingDrag = false;

    /**
     * Все вложенные элементы, которым делегируются события
     */
    protected abstract List<? extends Element> getGuiChildren();

    /**
     * Элементы, которые могут получать фокус
     */
    @Override
    public List<Element> getFocusableElements() {
        List<Element> focusable = new ArrayList<>();

        for (Element element : getGuiChildren()) {
            if (element instanceof TextFieldWidget ||
                element instanceof ButtonWidget ||
                element instanceof FocusableContainer) {
                focusable.add(element);
            }
        }

        return focusable;
    }

    /**
     * Регистрирует этот контейнер в менеджере фокуса
     */
    protected void registerWithFocusManager() {
        FocusManager.getInstance().registerContainer(this);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Предотвращаем рекурсивные вызовы
        if (processingClick) return false;

        processingClick = true;
        try {
            // Делегируем обработку кликов FocusManager
            if (FocusManager.getInstance().handleMouseClick(mouseX, mouseY, button)) {
                return true;
            }

            // Если FocusManager не обработал клик, проверяем все дочерние элементы
            List<? extends Element> children = getGuiChildren();
            for (Element child : children) {
                // Добавляем логирование для отладки
                Highlighter.LOGGER.info("Checking child element: {}", child.getClass().getSimpleName());

                // Обрабатываем клики для всех типов элементов
                if (child instanceof BasicFocusableWidget widget) {
                    if (widget.isMouseOver(mouseX, mouseY) && widget.mouseClicked(mouseX, mouseY, button)) {
                        Highlighter.LOGGER.info("Click handled by BasicFocusableWidget: {}", widget.getClass().getSimpleName());
                        return true;
                    }
                } else if (child instanceof ClickableWidget widget) {
                    if (widget.isMouseOver(mouseX, mouseY) && widget.mouseClicked(mouseX, mouseY, button)) {
                        Highlighter.LOGGER.info("Click handled by ClickableWidget: {}", widget.getClass().getSimpleName());
                        return true;
                    }
                } else if (child instanceof Element element) {
                    if (element.mouseClicked(mouseX, mouseY, button)) {
                        Highlighter.LOGGER.info("Click handled by Element: {}", element.getClass().getSimpleName());
                        return true;
                    }
                }
            }

            return false;
        } finally {
            processingClick = false;
        }
    }

    /**
     * Очищает фокус у всех дочерних элементов
     * Предназначен для переопределения в дочерних классах
     */
    public void clearFocus() {
        // Снимаем фокус со всех дочерних элементов
        for (Element child : getGuiChildren()) {
            if (child instanceof ClickableWidget widget) {
                widget.setFocused(false);
            } else if (child instanceof FocusableElement focusable) {
                focusable.setFocused(false);
            }
        }
    }
}
