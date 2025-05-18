package me.dalynkaa.highlighter.client.newgui.widgets.helper;

import me.dalynkaa.highlighter.client.newgui.focus.FocusableContainer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.List;

/**
 * Интерфейс для виджетов, которые содержат вложенные виджеты
 * и могут делегировать им события
 */
public interface NestedWidgetContainer extends FocusableContainer {

    /**
     * Получить список всех дочерних виджетов
     */
    List<? extends Element> getNestedWidgets();

    /**
     * Проверить, виден ли вложенный виджет
     */
    boolean isNestedWidgetVisible(Element widget);

    /**
     * Проверить, находится ли точка над вложенным виджетом
     */
    boolean isPointOverNestedWidget(double mouseX, double mouseY);

    /**
     * Получить текущий активный вложенный виджет или null, если такого нет
     */
    Element getActiveNestedWidget();

    /**
     * Установить текущий активный вложенный виджет
     */
    void setActiveNestedWidget(Element widget, boolean visible);

    /**
     * Скрыть все вложенные виджеты
     */
    void hideAllNestedWidgets();
}
