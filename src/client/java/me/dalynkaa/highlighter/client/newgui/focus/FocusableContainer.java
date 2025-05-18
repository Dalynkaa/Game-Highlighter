package me.dalynkaa.highlighter.client.newgui.focus;

import net.minecraft.client.gui.Element;
import java.util.List;

/**
 * Интерфейс для контейнеров элементов, которые могут получать фокус
 */
public interface FocusableContainer {
    /**
     * Возвращает список элементов, которые могут получать фокус в этом контейнере
     */
    List<Element> getFocusableElements();

    /**
     * Проверяет, находится ли точка внутри этого контейнера
     */
    boolean isPointInside(double x, double y);
}
