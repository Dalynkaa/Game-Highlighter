package me.dalynkaa.highlighter.client.newgui.widgets.helper;

import net.minecraft.client.gui.Element;

/**
 * Интерфейс для элементов интерфейса, которые могут получать фокус
 * и обрабатывать события мыши особым образом
 */
public interface FocusableElement extends Element {
    /**
     * Проверяет, находится ли точка внутри элемента, включая
     * потенциально выходящие за границы компоненты (например, раскрытый список)
     */
    boolean isExtendedHitbox(double mouseX, double mouseY);

    /**
     * Вызывается, когда элемент получает или теряет фокус
     */
    default void onFocusChanged(boolean focused) {
        // По умолчанию ничего не делаем
    }

    /**
     * Определяет, может ли элемент получать фокус в текущем состоянии
     */
    default boolean canReceiveFocus() {
        return true;
    }
}
