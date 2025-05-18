package me.dalynkaa.highlighter.client.newgui.widgets.helper;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.newgui.focus.FocusManager;
import me.dalynkaa.highlighter.client.newgui.widgets.ColorPickerWidget;
import me.dalynkaa.highlighter.client.newgui.widgets.HighlighterDropdownWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Абстрактный класс для виджетов, содержащих вложенные виджеты
 * Обеспечивает базовую реализацию для делегирования событий вложенным виджетам
 */
public abstract class AbstractNestedWidget extends ClickableWidget implements NestedWidgetContainer, FocusableElement {

    // Карта видимости для вложенных виджетов
    protected final Map<Element, Boolean> nestedWidgetsVisibility = new HashMap<>();

    // Текущий активный вложенный виджет
    protected Element activeNestedWidget = null;

    // Флаг для предотвращения рекурсивных вызовов
    private boolean processingEvent = false;

    public AbstractNestedWidget(int x, int y, int width, int height) {
        super(x, y, width, height, null);
    }

    @Override
    public boolean isNestedWidgetVisible(Element widget) {
        return nestedWidgetsVisibility.getOrDefault(widget, false);
    }

    @Override
    public boolean isPointOverNestedWidget(double mouseX, double mouseY) {
        for (Element widget : getNestedWidgets()) {
            if (isNestedWidgetVisible(widget) && isMouseOverElement(widget, mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Element getActiveNestedWidget() {
        return activeNestedWidget;
    }

    @Override
    public void setActiveNestedWidget(Element widget, boolean visible) {
        // Если виджет уже активный и режим видимости совпадает, ничего не делаем
        if (activeNestedWidget == widget && isNestedWidgetVisible(widget) == visible) {
            return;
        }

        // Скрываем предыдущий активный виджет
        if (activeNestedWidget != null && activeNestedWidget != widget) {
            nestedWidgetsVisibility.put(activeNestedWidget, false);
        }

        activeNestedWidget = widget;
        if (widget != null) {
            nestedWidgetsVisibility.put(widget, visible);

            // Если виджет становится видимым, устанавливаем на него фокус
            if (visible && widget instanceof ClickableWidget) {
                FocusManager.getInstance().setFocus(widget);
            }
        }
    }

    @Override
    public void hideAllNestedWidgets() {
        for (Element widget : getNestedWidgets()) {
            nestedWidgetsVisibility.put(widget, false);
        }
        activeNestedWidget = null;
    }

    @Override
    public List<Element> getFocusableElements() {
        List<Element> focusable = new ArrayList<>();

        // Добавляем сам виджет как фокусируемый
        focusable.add(this);

        // Добавляем видимые вложенные виджеты как фокусируемые
        for (Element widget : getNestedWidgets()) {
            if (isNestedWidgetVisible(widget)) {
                focusable.add(widget);
            }
        }

        return focusable;
    }

    @Override
    public boolean isPointInside(double x, double y) {
        return isMouseOver(x, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Предотвращаем рекурсивные вызовы
        if (processingEvent) return super.mouseClicked(mouseX, mouseY, button);

        processingEvent = true;
        try {
            // Проверяем клики по видимым вложенным виджетам
            for (Element widget : getNestedWidgets()) {
                if (isNestedWidgetVisible(widget) && isMouseOverElement(widget, mouseX, mouseY)) {
                    boolean handled = widget.mouseClicked(mouseX, mouseY, button);
                    if (handled) {
                        // Если виджет обработал клик, фокусируемся на нем
                        if (widget instanceof ClickableWidget) {
                            FocusManager.getInstance().setFocus(widget);
                        }
                        return true;
                    }
                }
            }

            // Если клик не обработан вложенными виджетами, вызываем стандартный обработчик
            return super.mouseClicked(mouseX, mouseY, button);
        } finally {
            processingEvent = false;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // Предотвращаем рекурсивные вызовы
        if (processingEvent) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

        String widgetName = this.getClass().getSimpleName();
        Highlighter.LOGGER.info("[DEBUG-ANW] mouseDragged в {} (хэш={}): x={}, y={}, button={}, activeNestedWidget={}",
            widgetName, this.hashCode(), mouseX, mouseY, button,
            (activeNestedWidget != null ? activeNestedWidget.getClass().getSimpleName() : "null"));

        processingEvent = true;
        try {
            // Проверяем перетаскивание для активного вложенного виджета
            if (activeNestedWidget != null && isNestedWidgetVisible(activeNestedWidget)) {
                // Для активного виджета или пикера цвета перетаскивание может быть вне его области
                boolean isColorPicker = activeNestedWidget instanceof ColorPickerWidget;
                boolean isDropdown = activeNestedWidget instanceof HighlighterDropdownWidget;

                // Дополнительная отладка
                if (isColorPicker) {
                    ColorPickerWidget picker = (ColorPickerWidget)activeNestedWidget;
                    Highlighter.LOGGER.info("[DEBUG-ANW] Активный виджет - ColorPicker: видимость={}, x/y={}/{}, размеры={}x{}",
                        picker.isVisible(), picker.getX(), picker.getY(), picker.getWidth(), picker.getHeight());
                }

                // Если это особый виджет или мышь над виджетом - делегируем событие
                if (isColorPicker || isDropdown || isMouseOverElement(activeNestedWidget, mouseX, mouseY)) {
                    Highlighter.LOGGER.info("[DEBUG-ANW] Делегирую mouseDragged активному виджету {}",
                        activeNestedWidget.getClass().getSimpleName());
                    boolean handled = activeNestedWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                    if (handled) {
                        Highlighter.LOGGER.info("[DEBUG-ANW] Событие обработано активным виджетом");
                        return true;
                    } else {
                        Highlighter.LOGGER.info("[DEBUG-ANW] Активный виджет вернул false");
                    }
                } else {
                    Highlighter.LOGGER.info("[DEBUG-ANW] Курсор не над активным виджетом, пропускаем");
                }
            } else if (activeNestedWidget != null) {
                Highlighter.LOGGER.info("[DEBUG-ANW] Активный виджет невидим, пропускаем");
            }

            // Проверяем все видимые вложенные виджеты
            for (Element widget : getNestedWidgets()) {
                if (isNestedWidgetVisible(widget) && widget != activeNestedWidget &&
                    isMouseOverElement(widget, mouseX, mouseY)) {
                    Highlighter.LOGGER.info("[DEBUG-ANW] Делегирую mouseDragged виджету {}", widget.getClass().getSimpleName());
                    boolean handled = widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                    if (handled) {
                        Highlighter.LOGGER.info("[DEBUG-ANW] Событие обработано виджетом");
                        return true;
                    }
                }
            }

            Highlighter.LOGGER.info("[DEBUG-ANW] Вызываю super.mouseDragged");
            boolean result = super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            Highlighter.LOGGER.info("[DEBUG-ANW] Результат super.mouseDragged: {}", result);
            return result;
        } finally {
            processingEvent = false;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Предотвращаем рекурсивные вызовы
        if (processingEvent) return super.mouseReleased(mouseX, mouseY, button);

        processingEvent = true;
        try {
            // Проверяем отпускание для активного вложенного виджета
            if (activeNestedWidget != null && isNestedWidgetVisible(activeNestedWidget)) {
                boolean handled = activeNestedWidget.mouseReleased(mouseX, mouseY, button);
                if (handled) {
                    return true;
                }
            }

            // Проверяем все видимые вложенные виджеты
            for (Element widget : getNestedWidgets()) {
                if (isNestedWidgetVisible(widget) && widget != activeNestedWidget) {
                    if (widget.mouseReleased(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }

            return super.mouseReleased(mouseX, mouseY, button);
        } finally {
            processingEvent = false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Предотвращаем рекурсивные вызовы
        if (processingEvent) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        processingEvent = true;
        try {
            // Проверяем сначала активный вложенный виджет
            if (activeNestedWidget != null && isNestedWidgetVisible(activeNestedWidget) &&
                isMouseOverElement(activeNestedWidget, mouseX, mouseY)) {
                boolean handled = activeNestedWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
                if (handled) {
                    return true;
                }
            }

            // Проверяем прокрутку для всех видимых вложенных виджетов, над которыми находится указатель
            for (Element widget : getNestedWidgets()) {
                if (isNestedWidgetVisible(widget) && widget != activeNestedWidget &&
                    isMouseOverElement(widget, mouseX, mouseY)) {
                    if (widget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                        return true;
                    }
                }
            }

            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        } finally {
            processingEvent = false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Предотвращаем рекурсивные вызовы
        if (processingEvent) return super.keyPressed(keyCode, scanCode, modifiers);

        processingEvent = true;
        try {
            // Проверяем нажатие клавиши для активного вложенного виджета
            if (activeNestedWidget != null && isNestedWidgetVisible(activeNestedWidget)) {
                boolean handled = activeNestedWidget.keyPressed(keyCode, scanCode, modifiers);
                if (handled) {
                    return true;
                }
            }

            return super.keyPressed(keyCode, scanCode, modifiers);
        } finally {
            processingEvent = false;
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        // Предотвращаем рекурсивные вызовы
        if (processingEvent) return super.charTyped(chr, modifiers);

        processingEvent = true;
        try {
            // Проверяем ввод символа для активного вложенного виджета
            if (activeNestedWidget != null && isNestedWidgetVisible(activeNestedWidget)) {
                boolean handled = activeNestedWidget.charTyped(chr, modifiers);
                if (handled) {
                    return true;
                }
            }

            return super.charTyped(chr, modifiers);
        } finally {
            processingEvent = false;
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Рендерим все видимые вложенные виджеты
        for (Element widget : getNestedWidgets()) {
            if (isNestedWidgetVisible(widget) && widget instanceof ClickableWidget clickable) {
                clickable.render(context, mouseX, mouseY, delta);
            }
        }
    }

    /**
     * Проверяет, находится ли указатель мыши над элементом
     */
    protected boolean isMouseOverElement(Element element, double mouseX, double mouseY) {
        if (element instanceof ClickableWidget widget) {
            return widget.isMouseOver(mouseX, mouseY);
        } else if (element instanceof NestedWidgetContainer container) {
            return container.isPointInside(mouseX, mouseY);
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        // Базовая реализация для нарраций - можно переопределить в дочерних классах
        // для более подробных описаний для screen readers
        if (this.active && this.visible) {
            if (this.isFocused()) {
                builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, "Nested widget with focus");
            } else {
                builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, "Nested widget");
            }
        }
    }
}
