package me.dalynkaa.highlighter.client.newgui.widgets;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.adapters.ColorAdapter;
import me.dalynkaa.highlighter.client.adapters.GuiAdapter;
import me.dalynkaa.highlighter.client.newgui.widgets.helper.AbstractNestedWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Текстовое поле для выбора цвета, 
 * отображает цвет в формате HEX и показывает образец цвета
 */
public class ColorTextFieldWidget extends AbstractNestedWidget {
    private String currentColor = "#FFFFFF";
    private ColorPickerWidget colorPicker;
    private final MinecraftClient client;
    private final Consumer<String> colorChangeConsumer;
    private final TextFieldWidget textField;

    // Флаги для улучшенной обработки событий перетаскивания
    private boolean colorPickerDragging = false;
    private int dragButton = 0;

    private final List<Element> nestedWidgets = new ArrayList<>();

    public ColorTextFieldWidget(MinecraftClient client, int x, int y, int width, int height, Text placeholder, Consumer<String> colorChangeConsumer) {
        super(x, y, width, height);
        this.client = client;
        this.colorChangeConsumer = colorChangeConsumer;
        
        // Создаем текстовое поле
        this.textField = new TextFieldWidget(client.textRenderer, x, y, width, height, placeholder);
        this.textField.setMaxLength(7);
        this.textField.setText("#FFFFFF");

        // Добавляем текстовое поле как вложенный виджет
        this.nestedWidgets.add(textField);
        this.nestedWidgetsVisibility.put(textField, true);

        // Проверяем вводимый текст на соответствие формату hex цвета
        this.textField.setChangedListener(this::onTextFieldChanged);
    }

    private void onTextFieldChanged(String text) {
        // Проверяем, является ли ввод допустимым hex-цветом
        if (ColorAdapter.isValidHexColor(text)) {
            currentColor = text;
            if (colorChangeConsumer != null) {
                colorChangeConsumer.accept(text);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Если нажали правой кнопкой на текстовое поле, открываем пикер
        if (this.textField.isMouseOver(mouseX, mouseY) && button == 1) {
            openColorPicker();
            return true;
        }

        // Если пикер видим и кликнули по нему, то делегируем событие и запоминаем состояние для перетаскивания
        if (isNestedWidgetVisible(colorPicker) && colorPicker.isExtendedHitbox(mouseX, mouseY)) {
            boolean handled = colorPicker.mouseClicked(mouseX, mouseY, button);
            if (handled) {
                colorPickerDragging = true;
                dragButton = button;
                Highlighter.LOGGER.info("Начато перетаскивание в ColorPicker");
                return true;
            }
        }

        // Если пикер видим и кликнули вне его, закрываем его
        if (isNestedWidgetVisible(colorPicker) && !colorPicker.isExtendedHitbox(mouseX, mouseY) && button == 0) {
            setActiveNestedWidget(colorPicker, false);
        }

        // Делегируем клик базовому классу
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // Если пикер цветов активен и мы начали перетаскивание в нём
        if (isNestedWidgetVisible(colorPicker) && colorPickerDragging && button == dragButton) {
            boolean handled = colorPicker.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            if (handled) {
                Highlighter.LOGGER.info("Перетаскивание в ColorPicker: x={}, y={}, deltaX={}, deltaY={}",
                    mouseX, mouseY, deltaX, deltaY);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Если пикер цветов активен и мы завершаем перетаскивание
        if (isNestedWidgetVisible(colorPicker) && colorPickerDragging && button == dragButton) {
            colorPickerDragging = false;
            boolean handled = colorPicker.mouseReleased(mouseX, mouseY, button);
            if (handled) {
                Highlighter.LOGGER.info("Завершено перетаскивание в ColorPicker");
                return true;
            }
        }
        colorPickerDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void openColorPicker() {
        if (colorPicker == null) {
            // Определяем позицию для colorPicker рядом с полем
            int pickerX = this.getX() + this.getWidth() + 10;
            int windowWidth = client.getWindow().getScaledWidth();

            // Если picker выходит за пределы экрана, отображаем его слева
            if (pickerX + 200 > windowWidth) {
                pickerX = this.getX() - 210; // 200 = ширина picker + немного отступа
            }

            colorPicker = new ColorPickerWidget(
                client,
                pickerX,
                this.getY() - 100,
                currentColor,
                this::onColorSelected
            );

            // Добавляем пикер в список вложенных виджетов
            this.nestedWidgets.add(colorPicker);
            this.nestedWidgetsVisibility.put(colorPicker, false);
        } else {
            colorPicker.setHexColor(currentColor);
        }

        // Активируем пикер
        setActiveNestedWidget(colorPicker, true);
    }

    private void onColorSelected(String hexColor) {
        currentColor = hexColor;
        textField.setText(hexColor);

        // Скрываем пикер
        setActiveNestedWidget(colorPicker, false);

        if (colorChangeConsumer != null) {
            colorChangeConsumer.accept(hexColor);
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Отрисовываем базовые элементы
        super.renderWidget(context, mouseX, mouseY, delta);

        // Отображаем предпросмотр цвета
        renderColorPreview(context);
    }

    /**
     * Отображает цветной квадрат для предпросмотра выбранного цвета
     */
    private void renderColorPreview(DrawContext context) {
        // Рисуем цветной квадрат для предпросмотра цвета
        int colorPreviewSize = this.getHeight() - 4;
        int colorPreviewX = this.getX() + this.getWidth() - colorPreviewSize - 4;
        int colorPreviewY = this.getY() + 2;

        try {
            // Используем адаптер для получения цвета
            int color = ColorAdapter.fromHexString(currentColor, 255);

            // Заливка цветом
            GuiAdapter.fillRect(
                context,
                colorPreviewX,
                colorPreviewY,
                colorPreviewX + colorPreviewSize,
                colorPreviewY + colorPreviewSize,
                color
            );

            // Рамка вокруг образца цвета
            GuiAdapter.drawBorder(
                context,
                colorPreviewX,
                colorPreviewY,
                colorPreviewSize,
                colorPreviewSize,
                ColorAdapter.getArgb(255, 0, 0, 0)
            );
        } catch (Exception e) {
            // Ошибка парсинга цвета, отображаем чёрный квадрат с красной рамкой
            GuiAdapter.fillRect(
                context,
                colorPreviewX,
                colorPreviewY,
                colorPreviewX + colorPreviewSize,
                colorPreviewY + colorPreviewSize,
                ColorAdapter.getArgb(255, 0, 0, 0)
            );

            GuiAdapter.drawBorder(
                context,
                colorPreviewX,
                colorPreviewY,
                colorPreviewSize,
                colorPreviewSize,
                ColorAdapter.getArgb(255, 255, 0, 0)
            );
        }
    }

    /**
     * Сбрасывает состояние виджета, скрывая цветовой пикер
     */
    public void reset() {
        if (colorPicker != null) {
            setActiveNestedWidget(colorPicker, false);
            colorPickerDragging = false;
        }
    }

    /**
     * Устанавливает цвет в виде hex строки
     */
    public void setColor(String hexColor) {
        if (ColorAdapter.isValidHexColor(hexColor)) {
            currentColor = hexColor;
            textField.setText(hexColor);

            if (colorPicker != null) {
                colorPicker.setHexColor(hexColor);
            }
        }
    }

    /**
     * Возвращает текущее значение цвета в HEX-формате
     * @return строка с HEX-кодом цвета
     */
    public String getText() {
        return currentColor;
    }

    /**
     * Возвращает текстовое поле, содержащее HEX-код цвета
     * @return текстовое поле
     */
    public TextFieldWidget getTextField() {
        return textField;
    }

    @Override
    public List<Element> getNestedWidgets() {
        return nestedWidgets;
    }

    /**
     * Проверяет, находится ли точка в пределах расширенной области виджета.
     * Включает как само текстовое поле, так и область цветового пикера (если он открыт).
     *
     * @param mouseX X-координата мыши
     * @param mouseY Y-координата мыши
     * @return true, если точка находится в пределах расширенной хитбокса
     */
    @Override
    public boolean isExtendedHitbox(double mouseX, double mouseY) {
        // Проверяем, попадает ли точка в область текстового поля
        if (this.textField.isMouseOver(mouseX, mouseY)) {
            return true;
        }

        // Если цветовой пикер видим, проверяем и его область
        if (colorPicker != null && isNestedWidgetVisible(colorPicker) && colorPicker.isExtendedHitbox(mouseX, mouseY)) {
            return true;
        }

        return false;
    }
}
