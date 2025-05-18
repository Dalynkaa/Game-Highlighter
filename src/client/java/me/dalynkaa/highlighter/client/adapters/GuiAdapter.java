package me.dalynkaa.highlighter.client.adapters;

import me.dalynkaa.highlighter.Highlighter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Адаптер для работы с GUI элементами Minecraft
 * Содержит методы, которые могут изменяться при обновлении Minecraft
 */
public class GuiAdapter {
    
    /**
     * Уровни детализации отладки GUI
     */
    public enum DebugLevel {
        NONE,       // Отладка отключена
        MINIMAL,    // Минимум информации (только критически важные события)
        NORMAL,     // Стандартный уровень (фокус + основные события)
        VERBOSE     // Подробная информация (все события, включая мышь и клавиатуру)
    }

    /**
     * Текущий уровень отладки GUI
     */
    public static DebugLevel DEBUG_LEVEL = DebugLevel.VERBOSE;

    /**
     * Включает/выключает дебаг GUI в консоль (помимо чата)
     */
    public static boolean DEBUG_CONSOLE = false;


    /**
     * Кэш для оптимизации повторяющихся текстовых сообщений
     */
    private static final Map<String, Long> DEBUG_MESSAGE_CACHE = new HashMap<>();
    private static final long DEBUG_MESSAGE_COOLDOWN = 1000; // миллисекунды

    /**
     * Отрисовывает прямоугольник с заливкой цветом
     */
    public static void fillRect(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y2, color);
    }
    
    /**
     * Отрисовывает границу прямоугольника
     */
    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.drawBorder(x, y, width, height, color);
    }
    
    /**
     * Отрисовывает горизонтальную линию
     */
    public static void drawHorizontalLine(DrawContext context, int x1, int x2, int y, int color) {
        context.drawHorizontalLine(x1, x2, y, color);
    }
    
    /**
     * Отрисовывает вертикальную линию
     */
    public static void drawVerticalLine(DrawContext context, int x, int y1, int y2, int color) {
        context.drawVerticalLine(x, y1, y2, color);
    }

    /**
     * Отрисовывает текстуру GUI
     */
    public static void drawGuiTexture(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        context.drawGuiTexture(texture, x, y, width, height);
    }
    
    /**
     * Отрисовывает текст с тенью
     */
    public static void drawTextWithShadow(DrawContext context, MinecraftClient client, String text, int x, int y, int color) {
        context.drawTextWithShadow(client.textRenderer, text, x, y, color);
    }
    
    /**
     * Отрисовывает текст с тенью используя Text объект
     */
    public static void drawTextWithShadow(DrawContext context, MinecraftClient client, Text text, int x, int y, int color) {
        context.drawTextWithShadow(client.textRenderer, text, x, y, color);
    }
    
    /**
     * Отрисовывает центрированный текст с тенью
     */
    public static void drawCenteredTextWithShadow(DrawContext context, MinecraftClient client, String text, int x, int y, int color) {
        context.drawCenteredTextWithShadow(client.textRenderer, text, x, y, color);
    }
    
    /**
     * Отрисовывает центрированный текст с тенью используя Text объект
     */
    public static void drawCenteredTextWithShadow(DrawContext context, MinecraftClient client, Text text, int x, int y, int color) {
        context.drawCenteredTextWithShadow(client.textRenderer, text, x, y, color);
    }
    
    /**
     * Включает обрезку (scissor) области рисования
     */
    public static void enableScissor(DrawContext context, int x, int y, int right, int bottom) {
        context.enableScissor(x, y, right, bottom);
    }
    
    /**
     * Отключает обрезку (scissor) области рисования
     */
    public static void disableScissor(DrawContext context) {
        context.disableScissor();
    }
    
    /**
     * Проверяет, нажата ли клавиша Shift
     */
    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }
    
    /**
     * Проверяет, нажата ли клавиша Control
     */
    public static boolean hasControlDown() {
        return Screen.hasControlDown();
    }

    /**
     * Проверяет, нажата ли клавиша Alt
     */
    public static boolean hasAltDown() {
        return Screen.hasAltDown();
    }

    /**
     * Рендерит текстовое поле с логированием фокуса
     */
    public static void renderTextField(DrawContext context, TextFieldWidget field, int mouseX, int mouseY, float delta) {
        field.render(context, mouseX, mouseY, delta);

        if (DEBUG_LEVEL.ordinal() >= DebugLevel.NORMAL.ordinal() && field.isFocused()) {
            logDebug("[GUI] Поле с фокусом: " + field.getMessage().getString());
        }
    }

    /**
     * Рендерит кнопку с дополнительными возможностями
     */
    public static void renderButton(DrawContext context, ClickableWidget button, int mouseX, int mouseY, float delta) {
        button.render(context, mouseX, mouseY, delta);

        if (DEBUG_LEVEL.ordinal() >= DebugLevel.VERBOSE.ordinal() && button.isHovered()) {
            String message = button instanceof net.minecraft.client.gui.widget.ButtonWidget bw ?
                bw.getMessage().getString() : button.getClass().getSimpleName();
            logDebug("[GUI] Кнопка под курсором: " + message);
        }
    }

    /**
     * Логирует смену фокуса для любого элемента с дополнительной информацией
     */
    public static void debugFocusChange(Element element, boolean focused) {
        if (DEBUG_LEVEL == DebugLevel.NONE || element == null) return;

        StringBuilder debugInfo = new StringBuilder();
        try {
            // Базовая информация об элементе
            String elementType = element.getClass().getSimpleName();
            String elementName = "Неизвестно";

            // Обработка известных типов элементов
            if (element instanceof TextFieldWidget field) {
                elementName = field.getMessage().getString();
                debugInfo.append("Текст: '").append(field.getText()).append("', ");
                debugInfo.append("Активен: ").append(field.isActive()).append(", ");
                debugInfo.append("Видим: ").append(field.visible).append(", ");
                debugInfo.append("X/Y: ").append(field.getX()).append("/").append(field.getY());
            } else if (element instanceof net.minecraft.client.gui.widget.ButtonWidget button) {
                elementName = button.getMessage().getString();
                debugInfo.append("X/Y: ").append(button.getX()).append("/").append(button.getY()).append(", ");
                debugInfo.append("Ширина/Высота: ").append(button.getWidth()).append("/").append(button.getHeight());
            } else if (element instanceof ClickableWidget widget) {
                elementName = widget.getMessage().getString();
                debugInfo.append("X/Y: ").append(widget.getX()).append("/").append(widget.getY()).append(", ");
                debugInfo.append("Размеры: ").append(widget.getWidth()).append("x").append(widget.getHeight());
            } else {
                debugInfo.append("Тип: ").append(elementType);
                debugInfo.append(", Хэш: ").append(element.hashCode());
            }

            // Сообщение для отладки
            String debugMessage = String.format("[GUI] %s: %s (%s) - %s",
                focused ? "Фокус" : "Потеря фокуса",
                elementName,
                elementType,
                debugInfo);

            // Выводим сообщение в зависимости от уровня отладки
            if ((focused && DEBUG_LEVEL.ordinal() >= DebugLevel.MINIMAL.ordinal()) ||
                (!focused && DEBUG_LEVEL.ordinal() >= DebugLevel.NORMAL.ordinal())) {

                logDebug(debugMessage);
            }

        } catch (Exception e) {
            // В случае ошибки выводим базовую информацию
            if (DEBUG_LEVEL.ordinal() >= DebugLevel.MINIMAL.ordinal()) {
                logDebug("[GUI] " + (focused ? "Фокус" : "Потеря фокуса") + ": " +
                    element.getClass().getSimpleName() + " (ошибка получения деталей: " + e.getMessage() + ")");
            }
        }
    }

    /**
     * Рендер предпросмотра цвета с улучшенным отображением
     */
    public static void renderColorPreview(DrawContext context, int x, int y, int size, int color) {
        // Основной цвет
        fillRect(context, x, y, x + size, y + size, color);

        // Рамка с градиентом для лучшей видимости на любом фоне
        int borderColor = ColorAdapter.getArgb(200, 0, 0, 0); // полупрозрачный черный
        drawBorder(context, x, y, size, size, borderColor);

        // Угловые маркеры для лучшей видимости на любом фоне
        int markerSize = Math.max(2, size / 8);
        int whiteCorner = ColorAdapter.getArgb(180, 255, 255, 255);

        // Верхний левый угол (белый)
        fillRect(context, x, y, x + markerSize, y + markerSize, whiteCorner);

        // Нижний правый угол (белый)
        fillRect(context, x + size - markerSize, y + size - markerSize, x + size, y + size, whiteCorner);
    }

    /**
     * Логирует отладочное сообщение в чат и/или консоль
     */
    private static void logDebug(String message) {
        // Проверка на кэш частых сообщений
        long now = System.currentTimeMillis();
        Long lastLog = DEBUG_MESSAGE_CACHE.get(message);

        if (lastLog != null && now - lastLog < DEBUG_MESSAGE_COOLDOWN) {
            return; // Игнорируем слишком частые повторы одинаковых сообщений
        }

        // Обновляем кэш
        DEBUG_MESSAGE_CACHE.put(message, now);

        // Вывод в чат
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
            Text.literal(message)
        );

        // Вывод в консоль если включено
        if (DEBUG_CONSOLE) {
            Highlighter.LOGGER.info(message);
        }
    }

    /**
     * Очищает кэш отладочных сообщений
     */
    public static void clearDebugCache() {
        DEBUG_MESSAGE_CACHE.clear();
    }

    /**
     * Форматирует размеры для более удобного отображения в GUI
     * @param width ширина
     * @param height высота
     * @return отформатированная строка (например, "100×50")
     */
    public static String formatDimensions(int width, int height) {
        return width + "×" + height;
    }

    /**
     * Проверяет, находится ли точка внутри прямоугольника
     */
    public static boolean isPointInRect(double x, double y, int rectX, int rectY, int rectWidth, int rectHeight) {
        return x >= rectX && x <= rectX + rectWidth && y >= rectY && y <= rectY + rectHeight;
    }

    /**
     * Расчет центрированного X-координата для элемента относительно контейнера
     */
    public static int centerX(int containerX, int containerWidth, int elementWidth) {
        return containerX + (containerWidth - elementWidth) / 2;
    }

    /**
     * Расчет центрированного Y-координата для элемента относительно контейнера
     */
    public static int centerY(int containerY, int containerHeight, int elementHeight) {
        return containerY + (containerHeight - elementHeight) / 2;
    }
}
