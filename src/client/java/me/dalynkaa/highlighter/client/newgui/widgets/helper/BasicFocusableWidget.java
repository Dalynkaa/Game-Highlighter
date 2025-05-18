package me.dalynkaa.highlighter.client.newgui.widgets.helper;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.newgui.focus.FocusManager;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

/**
 * Базовый класс для простых виджетов, которые могут получать фокус
 * и не требуют вложенной иерархии виджетов
 */
public abstract class BasicFocusableWidget extends ClickableWidget implements FocusableElement {

    protected boolean hasExtendedHitbox = false;

    public BasicFocusableWidget(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    public BasicFocusableWidget(int x, int y, int width, int height) {
        super(x, y, width, height, null);
    }

    @Override
    public boolean isExtendedHitbox(double mouseX, double mouseY) {
        // По умолчанию используем стандартную область хитбокса
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public void onFocusChanged(boolean focused) {
        setFocused(focused);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Отладочная информация о клике
        String widgetName = this.getClass().getSimpleName();
        String message = getMessage() != null ? getMessage().getString() : "null";
        Highlighter.LOGGER.info("[DEBUG] mouseClicked в {} ({}): x={}, y={}, button={}, isMouseOver={}, isExtendedHitbox={}",
            widgetName, message, mouseX, mouseY, button, isMouseOver(mouseX, mouseY), isExtendedHitbox(mouseX, mouseY));

        if (isExtendedHitbox(mouseX, mouseY)) {
            // Установка фокуса при клике
            if (canReceiveFocus()) {
                Highlighter.LOGGER.info("[DEBUG] Устанавливаю фокус на {} ({}) через FocusManager", widgetName, message);
                FocusManager.getInstance().setFocus(this);
            }
            boolean result = super.mouseClicked(mouseX, mouseY, button);
            Highlighter.LOGGER.info("[DEBUG] Результат super.mouseClicked для {} ({}): {}", widgetName, message, result);
            return result;
        }

        Highlighter.LOGGER.info("[DEBUG] Клик вне области виджета {} ({}), игнорирую", widgetName, message);
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        String widgetName = this.getClass().getSimpleName();
        String message = getMessage() != null ? getMessage().getString() : "null";

        Highlighter.LOGGER.info("[DEBUG] mouseDragged в {} ({}): x={}, y={}, button={}, deltaX={}, deltaY={}",
            widgetName, message, mouseX, mouseY, button, deltaX, deltaY);

        boolean result = super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        Highlighter.LOGGER.info("[DEBUG] Результат super.mouseDragged для {} ({}): {}", widgetName, message, result);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        String widgetName = this.getClass().getSimpleName();
        String message = getMessage() != null ? getMessage().getString() : "null";

        Highlighter.LOGGER.info("[DEBUG] mouseReleased в {} ({}): x={}, y={}, button={}",
            widgetName, message, mouseX, mouseY, button);

        boolean result = super.mouseReleased(mouseX, mouseY, button);
        Highlighter.LOGGER.info("[DEBUG] Результат super.mouseReleased для {} ({}): {}", widgetName, message, result);
        return result;
    }

    /**
     * Включает или выключает расширенный хитбокс для виджета
     */
    public void setHasExtendedHitbox(boolean hasExtendedHitbox) {
        this.hasExtendedHitbox = hasExtendedHitbox;
    }

    /**
     * Проверяет, имеет ли виджет расширенный хитбокс
     */
    public boolean hasExtendedHitbox() {
        return hasExtendedHitbox;
    }
}
