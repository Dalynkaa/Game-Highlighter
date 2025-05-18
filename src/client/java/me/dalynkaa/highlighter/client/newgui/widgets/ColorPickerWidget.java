package me.dalynkaa.highlighter.client.newgui.widgets;

import lombok.Getter;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.adapters.ColorAdapter;
import me.dalynkaa.highlighter.client.adapters.GuiAdapter;
import me.dalynkaa.highlighter.client.newgui.widgets.helper.BasicFocusableWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Виджет выбора цвета с палитрой и слайдером оттенка
 */
public class ColorPickerWidget extends BasicFocusableWidget {
    // Константы размеров
    private static final int PICKER_WIDTH = 200;
    private static final int PICKER_HEIGHT = 220;
    private static final int COLOR_PALETTE_SIZE = 150;
    private static final int HUE_SLIDER_HEIGHT = 15;
    private static final int PREVIEW_SIZE = 15;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 20;
    private static final int BUTTON_WIDTH = 70;
    private static final int FIELD_HEIGHT = 15;

    // Состояние виджета
    private boolean visible = true;
    private final List<Element> children = new ArrayList<>();

    // Core компоненты
    private final MinecraftClient client;

    // Дублируем x и y, так как они приватные в ClickableWidget
    private int pickerX;
    private int pickerY;

    // Компоненты цвета
    private int red, green, blue;
    private float h = 0, s = 1, v = 1;

    // Состояние взаимодействия
    private boolean draggingHue = false, draggingColor = false;
    private boolean disposed = false; // Флаг освобождения ресурсов

    // UI элементы
    private TextFieldWidget hexField;
    private ButtonWidget cancelButton, applyButton;

    @Getter
    private String hexColor = "#FFFFFF";
    private final Consumer<String> colorChangeConsumer;

    // Кеш текстур для оптимизации рендеринга
    private NativeImage paletteImage = null;
    private NativeImage hueSliderImage = null;
    private float lastPaletteHue = -1f;
    private NativeImageBackedTexture paletteTexture;
    private Identifier paletteTextureId;
    private NativeImageBackedTexture hueTexture;
    private Identifier hueTextureId;

    /**
     * Создает виджет выбора цвета
     * @param client клиент Minecraft
     * @param x X-координата верхнего левого угла
     * @param y Y-координата верхнего левого угла
     * @param initialColor начальный цвет в формате #RRGGBB
     * @param colorChangeConsumer функция для обработки выбранного цвета
     */
    public ColorPickerWidget(MinecraftClient client, int x, int y, String initialColor, Consumer<String> colorChangeConsumer) {
        super(x, y, PICKER_WIDTH, PICKER_HEIGHT + 50, Text.literal("Color Picker"));
        this.client = client;
        this.colorChangeConsumer = colorChangeConsumer;

        // Устанавливаем начальный цвет
        setHexColor(initialColor);

        // Устанавливаем локальные координаты
        this.pickerX = x;
        this.pickerY = y;

        // Создаем поле для ввода HEX-кода цвета
        this.hexField = new TextFieldWidget(
            client.textRenderer,
            GuiAdapter.centerX(x, PICKER_WIDTH, 60) - 3,
            y + PICKER_HEIGHT - 5,
            60,
            FIELD_HEIGHT,
            Text.literal("HEX")
        );
        this.hexField.setText(this.hexColor);
        this.hexField.setChangedListener(this::onHexChanged);

        // Создаем кнопки управления
        this.cancelButton = ButtonWidget.builder(Text.literal("Отмена"), button -> {
            this.visible = false;
        }).dimensions(x + BUTTON_SPACING, y + PICKER_HEIGHT + BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT).build();

        this.applyButton = ButtonWidget.builder(Text.literal("Применить"), button -> {
            if (this.colorChangeConsumer != null) {
                this.colorChangeConsumer.accept(this.hexColor);
            }
            this.visible = false;
        }).dimensions(x + PICKER_WIDTH - BUTTON_WIDTH - BUTTON_SPACING, y + PICKER_HEIGHT + BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT).build();

        // Добавляем элементы в список дочерних элементов
        this.children.add(this.hexField);
        this.children.add(this.cancelButton);
        this.children.add(this.applyButton);

        // Инициализируем текстуры
        initializeTextures();
    }

    /**
     * Инициализация и создание текстур для палитры и слайдера оттенка
     */
    private void initializeTextures() {
        updatePaletteImage();
        updateHueSliderImage();

        if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.VERBOSE.ordinal()) {
            Highlighter.LOGGER.info("ColorPicker textures initialized");
        }
    }

    /**
     * Обработчик изменения HEX-кода цвета вручную
     */
    private void onHexChanged(String newHex) {
        try {
            if (newHex.startsWith("#") && newHex.length() == 7) {
                int r = Integer.parseInt(newHex.substring(1, 3), 16);
                int g = Integer.parseInt(newHex.substring(3, 5), 16);
                int b = Integer.parseInt(newHex.substring(5, 7), 16);

                this.red = r;
                this.green = g;
                this.blue = b;

                float[] hsv = ColorAdapter.rgbToHsv(r, g, b);
                this.h = hsv[0];
                this.s = hsv[1];
                this.v = hsv[2];

                this.hexColor = newHex;
                updatePaletteImage();
            }
        } catch (Exception e) {
            if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.MINIMAL.ordinal()) {
                Highlighter.LOGGER.error("Error parsing hex color: {}", newHex, e);
            }
        }
    }

    /**
     * Устанавливает HEX-код цвета
     */
    public void setHexColor(String hexColor) {
        try {
            if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
                hexColor = "#FFFFFF";
            }

            this.hexColor = hexColor;
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);

            this.red = r;
            this.green = g;
            this.blue = b;

            float[] hsv = ColorAdapter.rgbToHsv(r, g, b);
            this.h = hsv[0];
            this.s = hsv[1];
            this.v = hsv[2];

            if (this.hexField != null) {
                this.hexField.setText(hexColor);
            }
            updatePaletteImage();
        } catch (Exception e) {
            this.red = 255;
            this.green = 255;
            this.blue = 255;
            this.hexColor = "#FFFFFF";
            this.h = 0;
            this.s = 0;
            this.v = 1;

            if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.MINIMAL.ordinal()) {
                Highlighter.LOGGER.error("Error setting hex color: {}", hexColor, e);
            }
        }
    }

    /**
     * Обновляет текстуру палитры цветов на основе текущего оттенка (h)
     */
    private void updatePaletteImage() {
        // Если текстура уже существует и оттенок не изменился, не обновляем
        if (paletteTexture != null && Math.abs(lastPaletteHue - h) < 0.001) return;

        // Освобождаем предыдущие ресурсы, если они существуют
        cleanupPaletteTexture();

        try {
            // Создаем новую текстуру палитры
            NativeImage image = new NativeImage(COLOR_PALETTE_SIZE, COLOR_PALETTE_SIZE, false);

            // Заполняем палитру цветами на основе текущего оттенка (h)
            for (int i = 0; i < COLOR_PALETTE_SIZE; i++) {
                for (int j = 0; j < COLOR_PALETTE_SIZE; j++) {
                    float saturation = (float) i / (COLOR_PALETTE_SIZE - 1);
                    float value = 1.0f - (float) j / (COLOR_PALETTE_SIZE - 1);
                    int[] rgb = ColorAdapter.hsvToRgb(h, saturation, value);
                    // ARGB формат: 0xFF - полная непрозрачность
                    image.setColor(i, j, 0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2]);
                }
            }

            paletteTexture = new NativeImageBackedTexture(image);
            paletteTextureId = MinecraftClient.getInstance().getTextureManager()
                    .registerDynamicTexture("color_picker_palette", paletteTexture);
            lastPaletteHue = h;
        } catch (Exception e) {
            if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.MINIMAL.ordinal()) {
                Highlighter.LOGGER.error("Error updating palette image", e);
            }
        }
    }

    /**
     * Освобождает ресурсы текстуры палитры
     */
    private void cleanupPaletteTexture() {
        try {
            if (paletteTexture != null) {
                paletteTexture.close();
                paletteTexture = null;
            }

            if (paletteTextureId != null) {
                MinecraftClient.getInstance().getTextureManager().destroyTexture(paletteTextureId);
                paletteTextureId = null;
            }

            if (paletteImage != null) {
                paletteImage.close();
                paletteImage = null;
            }
        } catch (Exception e) {
            if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.MINIMAL.ordinal()) {
                Highlighter.LOGGER.error("Error cleaning up palette texture", e);
            }
        }
    }

    /**
     * Обновляет текстуру слайдера оттенков
     */
    private void updateHueSliderImage() {
        // Освобождаем предыдущие ресурсы, если они существуют
        cleanupHueTexture();

        try {
            // Создаем новую текстуру для слайдера оттенков
            NativeImage image = new NativeImage(COLOR_PALETTE_SIZE, HUE_SLIDER_HEIGHT, false);

            // Создаём плавный переход по всем оттенкам
            for (int i = 0; i < COLOR_PALETTE_SIZE; i++) {
                float hue = (float)i / (COLOR_PALETTE_SIZE - 1);
                int[] rgb = ColorAdapter.hsvToRgb(hue, 1.0f, 1.0f);
                int color = 0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];

                // Заполняем вертикальную полосу одним цветом
                for (int j = 0; j < HUE_SLIDER_HEIGHT; j++) {
                    image.setColor(i, j, color);
                }
            }

            hueTexture = new NativeImageBackedTexture(image);
            hueTextureId = MinecraftClient.getInstance().getTextureManager()
                    .registerDynamicTexture("color_picker_hue", hueTexture);
        } catch (Exception e) {
            if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.MINIMAL.ordinal()) {
                Highlighter.LOGGER.error("Error updating hue slider image", e);
            }
        }
    }

    /**
     * Освобождает ресурсы текстуры слайдера оттенков
     */
    private void cleanupHueTexture() {
        try {
            if (hueTexture != null) {
                hueTexture.close();
                hueTexture = null;
            }

            if (hueTextureId != null) {
                MinecraftClient.getInstance().getTextureManager().destroyTexture(hueTextureId);
                hueTextureId = null;
            }

            if (hueSliderImage != null) {
                hueSliderImage.close();
                hueSliderImage = null;
            }
        } catch (Exception e) {
            if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.MINIMAL.ordinal()) {
                Highlighter.LOGGER.error("Error cleaning up hue texture", e);
            }
        }
    }

    /**
     * Реализация абстрактного метода renderWidget из ClickableWidget
     */
    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // Проверка, были ли ресурсы освобождены и требуют пересоздания
        if (disposed) {
            initializeTextures();
            disposed = false;
        }

        // Обновляем палитру только если изменился оттенок
        if (Math.abs(h - lastPaletteHue) > 0.001) {
            updatePaletteImage();
        }

        // Полупрозрачное затемнение фона за пикером
        GuiAdapter.fillRect(context, 0, 0, client.getWindow().getScaledWidth(),
            client.getWindow().getScaledHeight(), ColorHelper.Argb.getArgb(160, 0, 0, 0));

        // Фон пикера
        GuiAdapter.fillRect(context, pickerX, pickerY, pickerX + getWidth(), pickerY + getHeight(), ColorHelper.Argb.getArgb(255, 40, 40, 40));
        GuiAdapter.drawBorder(context, pickerX, pickerY, getWidth(), getHeight(), ColorHelper.Argb.getArgb(255, 60, 60, 60));

        // Заголовок
        GuiAdapter.drawCenteredTextWithShadow(context, client, Text.literal("Выбор цвета"),
            pickerX + getWidth() / 2, pickerY + 10, ColorHelper.Argb.getArgb(255, 255, 255, 255));

        // Координаты палитры цветов
        int colorPaletteX = GuiAdapter.centerX(pickerX, getWidth(), COLOR_PALETTE_SIZE);
        int colorPaletteY = pickerY + 30;

        // Отрисовка палитры цветов
        if (paletteTextureId != null) {
            context.drawTexture(
                    paletteTextureId,
                    colorPaletteX, colorPaletteY,
                    0f, 0f, // u, v
                    COLOR_PALETTE_SIZE, COLOR_PALETTE_SIZE,
                    COLOR_PALETTE_SIZE, COLOR_PALETTE_SIZE
            );
        }
        GuiAdapter.drawBorder(context, colorPaletteX, colorPaletteY,
            COLOR_PALETTE_SIZE, COLOR_PALETTE_SIZE, ColorHelper.Argb.getArgb(255, 0, 0, 0));

        // Координаты слайдера оттенков
        int hueSliderX = colorPaletteX;
        int hueSliderY = colorPaletteY + COLOR_PALETTE_SIZE + 10;

        // Отрисовка слайдера оттенков
        if (hueTextureId != null) {
            context.drawTexture(
                    hueTextureId,
                    hueSliderX, hueSliderY,
                    0f, 0f,
                    COLOR_PALETTE_SIZE, HUE_SLIDER_HEIGHT,
                    COLOR_PALETTE_SIZE, HUE_SLIDER_HEIGHT
            );
        }
        GuiAdapter.drawBorder(context, hueSliderX, hueSliderY,
            COLOR_PALETTE_SIZE, HUE_SLIDER_HEIGHT, ColorHelper.Argb.getArgb(255, 0, 0, 0));

        // Индикатор выбора на палитре
        int sPos = (int) (s * (COLOR_PALETTE_SIZE - 1));
        int vPos = (int) ((1 - v) * (COLOR_PALETTE_SIZE - 1));
        GuiAdapter.drawBorder(context, colorPaletteX + sPos - 3, colorPaletteY + vPos - 3,
            7, 7, ColorHelper.Argb.getArgb(255, 255, 255, 255));

        // Индикатор выбора на слайдере оттенков
        int hPos = (int) (h * (COLOR_PALETTE_SIZE - 1));
        GuiAdapter.drawBorder(context, hueSliderX + hPos - 3, hueSliderY - 3,
            7, HUE_SLIDER_HEIGHT + 6, ColorHelper.Argb.getArgb(255, 255, 255, 255));

        // Предпросмотр выбранного цвета
        int previewX = this.hexField.getX() - PREVIEW_SIZE - 5;
        int previewY = this.hexField.getY();

        // Отрисовка превью цвета с использованием адаптера
        GuiAdapter.renderColorPreview(context, previewX, previewY,
            PREVIEW_SIZE, ColorHelper.Argb.getArgb(255, red, green, blue));

        // Отрисовка элементов управления
        this.hexField.render(context, mouseX, mouseY, delta);
        GuiAdapter.renderButton(context, this.cancelButton, mouseX, mouseY, delta);
        GuiAdapter.renderButton(context, this.applyButton, mouseX, mouseY, delta);
    }

    /**
     * Метод для соответствия интерфейсу FocusableElement
     */
    @Override
    public boolean isExtendedHitbox(double mouseX, double mouseY) {
        return visible && (super.isMouseOver(mouseX, mouseY) || isPointInColorPicker(mouseX, mouseY));
    }

    /**
     * Проверяет, находится ли точка в пределах всей области виджета выбора цвета,
     * включая все его элементы
     */
    private boolean isPointInColorPicker(double mouseX, double mouseY) {
        if (!visible) return false;

        // Проверяем основную область пикера
        if (mouseX >= pickerX && mouseX <= pickerX + getWidth() && mouseY >= pickerY && mouseY <= pickerY + getHeight()) {
            return true;
        }

        // Координаты палитры цветов
        int colorPaletteX = GuiAdapter.centerX(pickerX, getWidth(), COLOR_PALETTE_SIZE);
        int colorPaletteY = pickerY + 30;

        // Проверяем область палитры
        if (GuiAdapter.isPointInRect(mouseX, mouseY, colorPaletteX, colorPaletteY, COLOR_PALETTE_SIZE, COLOR_PALETTE_SIZE)) {
            return true;
        }

        // Координаты слайдера оттенков
        int hueSliderY = colorPaletteY + COLOR_PALETTE_SIZE + 10;

        // Проверяем область слайдера
        if (GuiAdapter.isPointInRect(mouseX, mouseY, colorPaletteX, hueSliderY, COLOR_PALETTE_SIZE, HUE_SLIDER_HEIGHT)) {
            return true;
        }

        return false;
    }

    /**
     * Переопределение appendClickableNarrations вместо appendNarrations,
     * так как последний метод финальный в ClickableWidget
     */
    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, Text.literal("Выбор цвета"));
    }

    public boolean isVisible() { return visible; }

    /**
     * Устанавливает видимость виджета с логированием
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.NORMAL.ordinal()) {
            Highlighter.LOGGER.info("Color picker " + (visible ? "shown" : "hidden"));
        }
    }

    /**
     * Перемещает виджет и все дочерние элементы
     */
    public void setPosition(int x, int y) {
        int dx = x - this.pickerX, dy = y - this.pickerY;
        this.pickerX = x; this.pickerY = y;

        // Обновляем положение дочерних элементов
        if (this.hexField != null) {
            this.hexField.setX(this.hexField.getX() + dx);
            this.hexField.setY(this.hexField.getY() + dy);
        }

        if (this.cancelButton != null) {
            this.cancelButton.setX(this.cancelButton.getX() + dx);
            this.cancelButton.setY(this.cancelButton.getY() + dy);
        }

        if (this.applyButton != null) {
            this.applyButton.setX(this.applyButton.getX() + dx);
            this.applyButton.setY(this.applyButton.getY() + dy);
        }
    }

    /**
     * Устанавливает X-координату пикера цветов
     * @param newX новая X-координата
     */
    public void setX(int newX) {
        this.pickerX = newX;

        // Обновляем позиции дочерних элементов управления, если они есть
        updateChildPositions();
    }

    /**
     * Устанавливает Y-координату пикера цветов
     * @param newY новая Y-координата
     */
    public void setY(int newY) {
        this.pickerY = newY;

        // Обновляем позиции дочерних элементов управления, если они есть
        updateChildPositions();
    }

    /**
     * Обновляет позиции всех дочерних элементов управления
     * после изменения координат пикера
     */
    private void updateChildPositions() {
        // Обновляем позицию текстового поля, если оно есть
        if (hexField != null) {
            hexField.setX(pickerX + 10);
            hexField.setY(pickerY + COLOR_PALETTE_SIZE + HUE_SLIDER_HEIGHT + 20);
        }

        // Обновляем позицию кнопок, если они есть
        if (applyButton != null) {
            applyButton.setX(pickerX + 10);
            applyButton.setY(pickerY + PICKER_HEIGHT - BUTTON_HEIGHT - 10);
        }

        if (cancelButton != null) {
            cancelButton.setX(pickerX + getWidth() - BUTTON_WIDTH - 10);
            cancelButton.setY(pickerY + PICKER_HEIGHT - BUTTON_HEIGHT - 10);
        }
    }

    /**
     * Освобождает ресурсы виджета
     */
    public void dispose() {
        cleanupPaletteTexture();
        cleanupHueTexture();
        disposed = true;

        if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.VERBOSE.ordinal()) {
            Highlighter.LOGGER.info("ColorPicker resources disposed");
        }
    }

    /**
     * Обрабатывает нажатие кнопки мыши
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Highlighter.LOGGER.info("[DEBUG-CP] mouseClicked в ColorPickerWidget: visible={}, x={}, y={}, button={}",
            visible, mouseX, mouseY, button);

        if (!visible) {
            Highlighter.LOGGER.info("[DEBUG-CP] ColorPickerWidget невидим, игнорирую клик");
            return false;
        }

        // Проверка клика для дочерних элементов
        for (Element child : children) {
            if (child instanceof ClickableWidget widget && widget.isMouseOver(mouseX, mouseY)) {
                boolean handled = widget.mouseClicked(mouseX, mouseY, button);
                if (handled) {
                    Highlighter.LOGGER.info("[DEBUG-CP] Клик обработан дочерним элементом: {}", widget.getClass().getSimpleName());
                    return true;
                }
            }
        }

        // Координаты палитры цветов
        int colorPaletteX = GuiAdapter.centerX(pickerX, getWidth(), COLOR_PALETTE_SIZE);
        int colorPaletteY = pickerY + 30;

        // Проверяем клик по палитре цветов
        if (GuiAdapter.isPointInRect(mouseX, mouseY, colorPaletteX, colorPaletteY,
                COLOR_PALETTE_SIZE, COLOR_PALETTE_SIZE)) {
            draggingColor = true;
            Highlighter.LOGGER.info("[DEBUG-CP] Начало перетаскивания на цветовой палитре");
            updateColorFromPosition(mouseX, mouseY);
            return true;
        }

        // Координаты слайдера оттенков
        int hueSliderX = colorPaletteX;
        int hueSliderY = colorPaletteY + COLOR_PALETTE_SIZE + 10;

        // Проверяем клик по слайдеру оттенков
        if (GuiAdapter.isPointInRect(mouseX, mouseY, hueSliderX, hueSliderY,
                COLOR_PALETTE_SIZE, HUE_SLIDER_HEIGHT)) {
            draggingHue = true;
            Highlighter.LOGGER.info("[DEBUG-CP] Начало перетаскивания на слайдере оттенков");
            updateHueFromPosition(mouseX, hueSliderX);
            return true;
        }

        boolean result = super.mouseClicked(mouseX, mouseY, button);
        Highlighter.LOGGER.info("[DEBUG-CP] Результат обработки клика: {}", result);
        return result;
    }

    /**
     * Обрабатывает перемещение мыши с зажатой кнопкой (перетаскивание)
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!visible) return false;

        // Если идет перетаскивание на палитре цветов
        if (draggingColor) {
            updateColorFromPosition(mouseX, mouseY);
            return true;
        }

        // Если идет перетаскивание на слайдере оттенков
        if (draggingHue) {
            // Получаем координаты палитры для определения положения слайдера
            int colorPaletteX = GuiAdapter.centerX(pickerX, getWidth(), COLOR_PALETTE_SIZE);
            updateHueFromPosition(mouseX, colorPaletteX);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    /**
     * Обрабатывает отпускание кнопки мыши
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        if (draggingColor) {
            draggingColor = false;
            Highlighter.LOGGER.info("[DEBUG-CP] Завершено перетаскивание на цветовой палитре");
            return true;
        }

        if (draggingHue) {
            draggingHue = false;
            Highlighter.LOGGER.info("[DEBUG-CP] Завершено перетаскивание на слайдере оттенков");
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Обновляет цвет на основе позиции указателя мыши на палитре цветов
     * @param mouseX X-координата мыши
     * @param mouseY Y-координата мыши
     */
    private void updateColorFromPosition(double mouseX, double mouseY) {
        // Получаем координаты палитры
        int colorPaletteX = GuiAdapter.centerX(pickerX, getWidth(), COLOR_PALETTE_SIZE);
        int colorPaletteY = pickerY + 30;

        // Вычисляем относительные координаты внутри палитры
        int relativeX = (int)(mouseX - colorPaletteX);
        int relativeY = (int)(mouseY - colorPaletteY);

        // Ограничиваем значения пределами палитры
        relativeX = Math.max(0, Math.min(COLOR_PALETTE_SIZE - 1, relativeX));
        relativeY = Math.max(0, Math.min(COLOR_PALETTE_SIZE - 1, relativeY));

        // Вычисляем насыщенность и яркость на основе координат
        this.s = (float) relativeX / (COLOR_PALETTE_SIZE - 1);
        this.v = 1.0f - (float) relativeY / (COLOR_PALETTE_SIZE - 1);

        // Конвертируем HSV в RGB
        int[] rgb = ColorAdapter.hsvToRgb(this.h, this.s, this.v);
        this.red = rgb[0];
        this.green = rgb[1];
        this.blue = rgb[2];

        // Обновляем HEX-код
        this.hexColor = String.format("#%02X%02X%02X", red, green, blue);

        // Обновляем текстовое поле, если оно инициализировано
        if (this.hexField != null) {
            this.hexField.setText(this.hexColor);
        }

        if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.VERBOSE.ordinal()) {
            Highlighter.LOGGER.info("[DEBUG-CP] Цвет обновлен: hsv({}, {}, {}), rgb({}, {}, {}), hex={}",
                h, s, v, red, green, blue, hexColor);
        }
    }

    /**
     * Обновляет оттенок на основе X-позиции указателя мыши на слайдере
     * @param mouseX X-координата мыши
     * @param hueSliderX X-координата начала слайдера оттенков
     */
    private void updateHueFromPosition(double mouseX, int hueSliderX) {
        // Вычисляем относительную X-координату внутри слайдера
        int relativeX = (int)(mouseX - hueSliderX);

        // Ограничиваем значение пределами слайдера
        relativeX = Math.max(0, Math.min(COLOR_PALETTE_SIZE - 1, relativeX));

        // Вычисляем новое значение оттенка
        this.h = (float) relativeX / (COLOR_PALETTE_SIZE - 1);

        // Обновляем палитру, если изменился оттенок
        updatePaletteImage();

        // Конвертируем HSV в RGB
        int[] rgb = ColorAdapter.hsvToRgb(this.h, this.s, this.v);
        this.red = rgb[0];
        this.green = rgb[1];
        this.blue = rgb[2];

        // Обновляем HEX-код
        this.hexColor = String.format("#%02X%02X%02X", red, green, blue);

        // Обновляем текстовое поле, если оно инициализировано
        if (this.hexField != null) {
            this.hexField.setText(this.hexColor);
        }

        if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.VERBOSE.ordinal()) {
            Highlighter.LOGGER.info("[DEBUG-CP] Оттенок обновлен: h={}, hex={}", h, hexColor);
        }
    }
}
