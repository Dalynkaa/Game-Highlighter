package me.dalynkaa.highlighter.client.gui.widgets.colorPicker;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.adapters.ColorAdapter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ColorPickerFieldWidget extends TextFieldWidget {
    private static final int COLOR_PREVIEW_SIZE = 16;
    private static final int PADDING = 4;
    private static final Pattern HEX_PATTERN = Pattern.compile("^#?[0-9A-Fa-f]{0,6}$");

    private int selectedColor;
    private Consumer<Integer> colorChangeCallback;
    private ColorPickerPopup popup;
    private boolean popupOpen = false;
    private boolean textChangedByUser = false;
    private Consumer<ColorPickerFieldWidget> onPopupClosedCallback;
    private Consumer<ColorPickerFieldWidget> onPopupOpenCallback;

    public ColorPickerFieldWidget(int x, int y, int width, int height, Text text, int color,
                                  Consumer<Integer> colorChangeCallback) {
        super(MinecraftClient.getInstance().textRenderer, x, y, width, height, text);
        setX(x);
        setY(y);
        this.selectedColor = color;
        this.colorChangeCallback = colorChangeCallback;

        // Setup text field for hex input
        this.setMaxLength(7); // #RRGGBB
        this.setText(String.format("#%06X", color & 0xFFFFFF));

        // Set text change listener
        this.setChangedListener(this::onTextChanged);
    }

    public ColorPickerFieldWidget(int x, int y, int width, int color, Consumer<Integer> colorChangeCallback) {
        this(x, y, width, 20, Text.empty(), color, colorChangeCallback);
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        if (popupOpen) {
            return popup.isMouseOver(x, y) || super.isInBoundingBox(x, y);
        }
        return super.isInBoundingBox(x, y);
    }

    // Global mouse event handling for click-outside detection
    public boolean handleGlobalMouseClick(double mouseX, double mouseY, int button) {
        if (popupOpen && button == 0) {
            // Если клик по попапу или полю, НЕ закрываем
            if (popup.isMouseOver(mouseX, mouseY) || isMouseOverField(mouseX, mouseY)) {
                return false; // Не обрабатываем глобально, пусть обработает сам виджет
            }
            // Только если клик вне попапа и поля - закрываем
            closePopup();
            return true;
        }
        return false;
    }

    public String getText() {
        return ColorAdapter.rgbToHex(selectedColor);
    }


    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render the base text field
        super.renderWidget(context, mouseX, mouseY, delta);

        // Draw color preview on the left side
        int x = getX();
        int y = getY();
        int height = getHeight();

        int previewX = x + width - COLOR_PREVIEW_SIZE - PADDING;
        int previewY = y + (height - COLOR_PREVIEW_SIZE) / 2;

        // Color preview background (checkerboard for transparency visibility)
        drawCheckerboard(context, previewX, previewY, COLOR_PREVIEW_SIZE, COLOR_PREVIEW_SIZE);

        // Color preview
        context.fill(previewX, previewY, previewX + COLOR_PREVIEW_SIZE,
                previewY + COLOR_PREVIEW_SIZE, selectedColor);

        // Color preview border
        context.drawBorder(previewX, previewY, COLOR_PREVIEW_SIZE, COLOR_PREVIEW_SIZE, 0xFF000000);

        // Draw dropdown arrow on the right
        int arrowX = x + getWidth() - 28;
        int arrowY = y + height / 2;
        drawDropdownArrow(context, arrowX, arrowY, isFocused() ? 0xFFFFFFFF : 0xFFAAAAAA);

        // Render popup if open
        if (popup != null && popupOpen) {
            popup.render(context, mouseX, mouseY, delta);
        }
    }

    private void drawCheckerboard(DrawContext context, int x, int y, int width, int height) {
        int checkSize = 4;
        for (int cx = 0; cx < width; cx += checkSize) {
            for (int cy = 0; cy < height; cy += checkSize) {
                boolean isLight = ((cx / checkSize) + (cy / checkSize)) % 2 == 0;
                int color = isLight ? 0xFFCCCCCC : 0xFF999999;
                context.fill(x + cx, y + cy,
                        x + Math.min(cx + checkSize, width),
                        y + Math.min(cy + checkSize, height), color);
            }
        }
    }

    private void drawDropdownArrow(DrawContext context, int x, int y, int color) {
        // Draw down arrow
        if (popupOpen) {
            for (int i = 0; i < 4; i++) {
                context.fill(x - i, y - 2 + i, x + i + 1, y - 1 + i, color);
            }
        } else {
            for (int i = 0; i < 4; i++) {
                context.fill(x - i, y + 2 - i, x + i + 1, y + 1 - i, color);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (popup != null && popupOpen) {
            // Если клик по попапу, обрабатываем его
            if (popup.isMouseOver(mouseX, mouseY)) {
                return popup.mouseClicked(mouseX, mouseY, button);
            }
        }
        
        if (button == 0) {
            int relativeX = (int) (mouseX - getX());
            if (relativeX >= getWidth() - COLOR_PREVIEW_SIZE - PADDING ||
                    relativeX >= getWidth() - 28) {
                if (popupOpen) {
                    closePopup();
                } else {
                    openPopup();
                }
                return true;
            }
        }

        // Возвращаем управление родительскому классу только для обработки клика по текстовому полю
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOverField(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getWidth() &&
               mouseY >= getY() && mouseY <= getY() + getHeight();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (popup != null && popupOpen) {
            if (popup.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (popup != null && popupOpen) {
            if (popup.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public int getInnerWidth() {
        // Reduce inner width to account for color preview and arrow
        return Math.max(0, super.getInnerWidth() - COLOR_PREVIEW_SIZE - PADDING - 20);
    }

    // Override to adjust text rendering position
    protected int getTextStartX() {
        return getX() + COLOR_PREVIEW_SIZE + PADDING * 2;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
    }

    private void onTextChanged(String text) {
        if (!textChangedByUser) return;

        // Validate and parse hex color
        String cleanText = text.trim();
        if (cleanText.startsWith("#")) {
            cleanText = cleanText.substring(1);
        }

        if (HEX_PATTERN.matcher("#" + cleanText).matches() && cleanText.length() == 6) {
            try {
                int newColor = Integer.parseInt(cleanText, 16) | 0xFF000000;
                if (newColor != selectedColor) {
                    selectedColor = newColor;
                    if (colorChangeCallback != null) {
                        colorChangeCallback.accept(selectedColor);
                    }
                }
            } catch (NumberFormatException ignored) {
                // Invalid hex, ignore
            }
        }
    }

    @Override
    public void write(String text) {
        textChangedByUser = true;
        super.write(text);
        textChangedByUser = false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        textChangedByUser = true;
        boolean result = super.charTyped(chr, modifiers);
        textChangedByUser = false;
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            if (popupOpen) {
                closePopup();
                return true;
            }
        }

        textChangedByUser = true;
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        textChangedByUser = false;
        return result;
    }

    // Method to check if popup is open for parent containers
    public boolean isPopupOpen() {
        return popupOpen;
    }

    public void openPopup() {
        if (popup != null) {
            closePopup();
        }

        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen != null) {
            // Position popup below the field, but adjust if it would go off screen
            int popupX = getX() + getWidth() + 8;
            int popupY = getY() + getHeight() + 2;

            // Adjust position if popup would go off screen
            if (popupX + ColorPickerPopup.POPUP_WIDTH > currentScreen.width) {
                popupX = currentScreen.width - ColorPickerPopup.POPUP_WIDTH - 10;
            }
            if (popupY + ColorPickerPopup.POPUP_HEIGHT > currentScreen.height) {
                popupY = getY() - ColorPickerPopup.POPUP_HEIGHT - 2;
            }

            // Обязательно передаем текущий выбранный цвет, чтобы он не сбрасывался
            popup = new ColorPickerPopup(popupX, popupY, selectedColor,
                    this::onColorSelected, this::closePopup);
            popupOpen = true;
            if (onPopupOpenCallback != null) {
                onPopupOpenCallback.accept(this);
            }
        }
    }

    public void closePopup() {
        if (popup != null) {
            popup.close();
            popup = null;
        }
        popupOpen = false;
        if (onPopupClosedCallback != null) {
            onPopupClosedCallback.accept(this);
        }
    }

    public boolean  isPopupOpened(){
        return popupOpen;
    }

    public void onPopupClosedEvent(Consumer<ColorPickerFieldWidget> callback) {
        this.onPopupClosedCallback = callback;
    }

    public void onPopupOpenEvent(Consumer<ColorPickerFieldWidget> callback) {
        this.onPopupOpenCallback = callback;
    }

    private void onColorSelected(int color) {
        selectedColor = color;

        // Update text field without triggering change event
        textChangedByUser = false;
        setText(String.format("#%06X", color & 0xFFFFFF));
        textChangedByUser = true;

        if (colorChangeCallback != null) {
            colorChangeCallback.accept(color);
        }
    }

    public void setColor(int color) {
        selectedColor = color;
        textChangedByUser = false;
        setText(String.format("#%06X", color & 0xFFFFFF));
        textChangedByUser = true;
    }

    public int getColor() {
        return selectedColor;
    }

    // Inner popup class
    private static class ColorPickerPopup {
        public static final int POPUP_WIDTH = 258;
        public static final int POPUP_HEIGHT = 166;
        private static final int PRESET_SIZE = 18;
        private static final int PRESET_SPACING = 3;
        private static final float SCALE_FACTOR = 0.75f; // Определяем масштаб как константу

        // Массив предустановленных цветов
        private static final int[] COLOR_PRESETS = {
            // Первый столбец (основные цвета)
            0xFFFF0000, // Красный
            0xFF00FF00, // Зеленый
            0xFF0000FF, // Синий
            0xFFFFFF00, // Желтый
            0xFFFF00FF, // Пурпурный
            0xFF00FFFF, // Голубой
            0xFFFFFFFF, // Белый
            // Второй столбец (оттенки)
            0xFFFF8080, // Светло-красный
            0xFF80FF80, // Светло-зеленый
            0xFF8080FF, // Светло-синий
            0xFFFFFF80, // Светло-желтый
            0xFFFF80FF, // Светло-пурпурный
            0xFF80FFFF, // Светло-голубой
            0xFFC0C0C0, // Светло-серый
            // Третий столбец (дополнительные)
            0xFF800000, // Темно-красный
            0xFF008000, // Темно-зеленый
            0xFF000080, // Темно-синий
            0xFF808000, // Оливковый
            0xFF800080, // Фиолетовый
            0xFF008080, // Сине-зеленый
            0xFF404040 // Темно-серый
        };

        private static final int PRESET_COLUMNS = 3;
        private static final int PRESET_ROWS = 7;
        private static final int PRESET_PANEL_WIDTH = PRESET_COLUMNS * (PRESET_SIZE + PRESET_SPACING) + PRESET_SPACING;

        private final int x, y;
        private final OptimizedColorPickerWidget colorPicker;
        private final Consumer<Integer> onColorSelected;
        private final Runnable onClose;

        public ColorPickerPopup(int x, int y, int initialColor, Consumer<Integer> onColorSelected, Runnable onClose) {
            this.x = x;
            this.y = y;
            this.onColorSelected = onColorSelected;
            this.onClose = onClose;

            // Рассчитываем позицию пикера с учетом масштаба
            float scaledX = (x + 8) / SCALE_FACTOR;
            float scaledY = (y + 8) / SCALE_FACTOR;

            // Create color picker with correct scaled position
            this.colorPicker = new OptimizedColorPickerWidget((int)scaledX, (int)scaledY, color -> {
                onColorSelected.accept(color);
            });

            colorPicker.setColor(initialColor);
        }

        // Метод для преобразования координат с учетом масштаба
        private double scaleCoordinate(double coordinate) {
            return coordinate / SCALE_FACTOR;
        }

        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            // Draw popup background with shadow
            context.fill(x - 2, y - 2, x + POPUP_WIDTH + 2, y + POPUP_HEIGHT + 2, 0x80000000);
            context.fill(x, y, x + POPUP_WIDTH, y + POPUP_HEIGHT, 0xF0202020);
            context.drawBorder(x, y, POPUP_WIDTH, POPUP_HEIGHT, 0xFF404040);

            //? if >=1.21.6 {
            context.getMatrices().pushMatrix();
            context.getMatrices().scale(SCALE_FACTOR, SCALE_FACTOR);
            //?} else {
            /*context.getMatrices().push();
            context.getMatrices().scale(SCALE_FACTOR, SCALE_FACTOR, 1.0f);
            *///?}

            float scaledX = (x + 8) / SCALE_FACTOR;
            float scaledY = (y + 8) / SCALE_FACTOR;
            float scaledMouseX = mouseX / SCALE_FACTOR;
            float scaledMouseY = mouseY / SCALE_FACTOR;

            colorPicker.renderWidget(context, (int) scaledMouseX, (int) scaledMouseY, delta);

            //? if >=1.21.6 {
            context.getMatrices().popMatrix();
            //?} else {
            /*context.getMatrices().pop();
            *///?}

            int presetsStartX = x + POPUP_WIDTH - PRESET_PANEL_WIDTH - 4;
            int presetsStartY = y + 8;

            context.fill(presetsStartX, y+8, x + POPUP_WIDTH - 4, y + POPUP_HEIGHT - 8, 0x40000000);

            for (int i = 0; i < COLOR_PRESETS.length; i++) {
                int col = i / PRESET_ROWS;
                int row = i % PRESET_ROWS;

                int presetX = presetsStartX + col * (PRESET_SIZE + PRESET_SPACING) + PRESET_SPACING;
                int presetY = presetsStartY + row * (PRESET_SIZE + PRESET_SPACING) + PRESET_SPACING;

                drawCheckerboard(context, presetX, presetY, PRESET_SIZE, PRESET_SIZE);

                context.fill(presetX, presetY, presetX + PRESET_SIZE,
                        presetY + PRESET_SIZE, COLOR_PRESETS[i]);

                boolean hovered = mouseX >= presetX && mouseX < presetX + PRESET_SIZE &&
                        mouseY >= presetY && mouseY < presetY + PRESET_SIZE;
                int borderColor = hovered ? 0xFFFFFFFF : 0xFF606060;
                context.drawBorder(presetX, presetY, PRESET_SIZE, PRESET_SIZE, borderColor);

                if (COLOR_PRESETS[i] == colorPicker.getColor()) {
                    context.drawBorder(presetX - 1, presetY - 1, PRESET_SIZE + 2, PRESET_SIZE + 2, 0xFFFFFF00);
                }
            }
        }

        private void drawCheckerboard(DrawContext context, int x, int y, int width, int height) {
            int checkSize = 3;
            for (int cx = 0; cx < width; cx += checkSize) {
                for (int cy = 0; cy < height; cy += checkSize) {
                    boolean isLight = ((cx / checkSize) + (cy / checkSize)) % 2 == 0;
                    int color = isLight ? 0xFFCCCCCC : 0xFF999999;
                    context.fill(x + cx, y + cy,
                            x + Math.min(cx + checkSize, width),
                            y + Math.min(cy + checkSize, height), color);
                }
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return false;
            int presetsStartX = x + POPUP_WIDTH - PRESET_PANEL_WIDTH - 4;
            int presetsStartY = y + 8;

            for (int i = 0; i < COLOR_PRESETS.length; i++) {
                int col = i / PRESET_ROWS;
                int row = i % PRESET_ROWS;

                int presetX = presetsStartX + col * (PRESET_SIZE + PRESET_SPACING) + PRESET_SPACING;
                int presetY = presetsStartY + row * (PRESET_SIZE + PRESET_SPACING) + PRESET_SPACING;

                if (mouseX >= presetX && mouseX < presetX + PRESET_SIZE &&
                        mouseY >= presetY && mouseY < presetY + PRESET_SIZE) {
                    colorPicker.setColor(COLOR_PRESETS[i]);
                    onColorSelected.accept(COLOR_PRESETS[i]);
                    return true;
                }
            }

            double scaledX = scaleCoordinate(mouseX);
            double scaledY = scaleCoordinate(mouseY);

            return colorPicker.mouseClicked(scaledX, scaledY, button) || true;
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            double scaledX = scaleCoordinate(mouseX);
            double scaledY = scaleCoordinate(mouseY);
            double scaledDeltaX = scaleCoordinate(deltaX);
            double scaledDeltaY = scaleCoordinate(deltaY);

            return colorPicker.mouseDragged(scaledX, scaledY, button, scaledDeltaX, scaledDeltaY);
        }

        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            double scaledX = scaleCoordinate(mouseX);
            double scaledY = scaleCoordinate(mouseY);

            return colorPicker.mouseReleased(scaledX, scaledY, button);
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + POPUP_WIDTH &&
                   mouseY >= y && mouseY < y + POPUP_HEIGHT;
        }

        public void close() {
            if (colorPicker != null) {
                colorPicker.cleanup();
            }
        }
    }
}

