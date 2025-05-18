package me.dalynkaa.highlighter.client.newgui.widgets;

import lombok.Getter;
import lombok.Setter;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.newgui.HighlightScreen;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import me.dalynkaa.highlighter.client.newgui.widgets.helper.NestedGuiGroup;
import me.dalynkaa.highlighter.client.adapters.GuiAdapter;
import me.dalynkaa.highlighter.client.newgui.focus.FocusManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class HighlighterPrefixCreateEditWidget extends NestedGuiGroup implements Drawable {

    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("social_interactions/background");

    // Константы для размеров и позиционирования
    private static final int FIELD_HEIGHT = 20;
    private static final int LABEL_SPACING = 10;
    private static final int VERTICAL_SPACING = 35;
    private static final int HORIZONTAL_PADDING = 10;
    private static final int TITLE_Y_OFFSET = -12;

    // Данные
    private Prefix prefix;
    private final TextRenderer textRenderer;
    private final MinecraftClient client;

    // Размеры и положение
    private int x;
    private int y;
    private int width;
    private int height;

    // Элементы интерфейса
    private final List<Element> guiElements = new ArrayList<>();
    private final List<ClickableWidget> fields = new ArrayList<>();

    @Getter
    private final TextFieldWidget tagField;
    @Getter
    private final TextFieldWidget contentField;
    @Getter
    private final TextFieldWidget chatTemplateField;
    @Getter
    private final TextFieldWidget chatSoundField;
    private final ColorTextFieldWidget playerColorField, prefixColorField;
    private final ButtonWidget saveButton;
    private final HighlightScreen parent;

    // Состояние фокуса для этого виджета
    private boolean focused = false;

    public HighlighterPrefixCreateEditWidget(int x, int y, int width, int height, HighlightScreen parent) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.client = MinecraftClient.getInstance();
        this.textRenderer = client.textRenderer;
        this.parent = parent;

        int startY = y + HORIZONTAL_PADDING;

        // Создание полей ввода с равным интервалом
        this.tagField = createField("Tag", startY);
        this.contentField = createField("Content", startY + VERTICAL_SPACING);
        this.playerColorField = createColorField("Player Color", startY + VERTICAL_SPACING * 2, color -> {});
        this.prefixColorField = createColorField("Prefix Color", startY + VERTICAL_SPACING * 3, color -> {});
        this.chatTemplateField = createField("Chat Template", startY + VERTICAL_SPACING * 4);
        this.chatSoundField = createField("Chat Sound", startY + VERTICAL_SPACING * 5);

        // Кнопка сохранения внизу формы
        this.saveButton = ButtonWidget.builder(Text.literal("Save"), button -> {
            savePrefix();
        }).dimensions(
            x + HORIZONTAL_PADDING,
            y + HORIZONTAL_PADDING + VERTICAL_SPACING * 6 + LABEL_SPACING,
            width - HORIZONTAL_PADDING * 2,
            FIELD_HEIGHT
        ).build();

        // Добавление полей в списки для легкого управления
        fields.add(tagField);
        fields.add(contentField);
        fields.add(playerColorField);
        fields.add(prefixColorField);
        fields.add(chatTemplateField);
        fields.add(chatSoundField);

        guiElements.addAll(fields);
        guiElements.add(saveButton);

        // Регистрируем этот контейнер в FocusManager
        registerWithFocusManager();
    }

    @Override
    public SelectionType getType() {
        return SelectionType.FOCUSED;
    }

    public void setX(int newX) {
        int deltaX = newX - this.x;
        this.x = newX;

        // Перепозиционируем все дочерние элементы с сохранением относительного положения
        for (Element element : guiElements) {
            if (element instanceof ClickableWidget widget) {
                widget.setX(widget.getX() + deltaX);
            }
        }
        updatePositionAndSize(x, y, width, height);
    }

    public void setY(int newY) {
        int deltaY = newY - this.y;
        this.y = newY;

        // Перепозиционируем все дочерние элементы с сохранением относительного положения
        for (Element element : guiElements) {
            if (element instanceof ClickableWidget widget) {
                widget.setY(widget.getY() + deltaY);
            }
        }
        updatePositionAndSize(x, y, width, height);
    }

    public void setWidth(int newWidth) {
        int deltaWidth = newWidth - this.width;
        this.width = newWidth;

        // Обновляем ширину полей ввода
        for (Element element : guiElements) {
            if (element instanceof ClickableWidget widget) {
                // Если это кнопка или поле ввода, изменяем его ширину пропорционально
                // При этом сохраняем отступы с обеих сторон
                if (widget == saveButton || fields.contains(widget)) {
                    widget.setWidth(widget.getWidth() + deltaWidth);
                }
            }
        }
        updatePositionAndSize(x, y, width, height);
    }

    public void setHeight(int newHeight) {
        // Сохраняем новую высоту, но не меняем высоту дочерних элементов,
        // так как нам нужно только перерисовать фон с новой высотой
        this.height = newHeight;
    }

    /**
     * Полностью обновляет позицию и размеры виджета и всех его дочерних элементов
     */
    public void updatePositionAndSize(int newX, int newY, int newWidth, int newHeight) {
        // Сначала обновляем базовые параметры
        int oldX = this.x;
        int oldY = this.y;
        int oldWidth = this.width;
        int oldHeight = this.height;

        this.x = newX;
        this.y = newY;
        this.width = newWidth;
        this.height = newHeight;

        // Теперь пересоздаем и перепозиционируем все элементы
        int startY = newY + HORIZONTAL_PADDING;

        // Обновляем положение и размеры каждого поля
        for (int i = 0; i < fields.size(); i++) {
            ClickableWidget field = fields.get(i);
            field.setX(newX + HORIZONTAL_PADDING);
            field.setY(startY + VERTICAL_SPACING * i + LABEL_SPACING);
            field.setWidth(newWidth - HORIZONTAL_PADDING * 2);
        }

        // Обновляем кнопку сохранения
        if (saveButton != null) {
            saveButton.setX(newX + HORIZONTAL_PADDING);
            saveButton.setY(newY + HORIZONTAL_PADDING + VERTICAL_SPACING * 6 + LABEL_SPACING);
            saveButton.setWidth(newWidth - HORIZONTAL_PADDING * 2);
        }
    }

    /**
     * Сохраняет данные префикса
     */
    private void savePrefix() {
        String name = getTagField().getText();
        String prefixContent = getContentField().getText();
        String playerColor = getPlayerColorField().getText();
        String prefixColor = getPrefixColorField().getText();
        String chatTemplate = getChatTemplateField().getText();
        String chatSound = getChatSoundField().getText();

        // Проверка заполненности всех полей
        if (name.isEmpty() || prefixContent.isEmpty() || playerColor.isEmpty() ||
                prefixColor.isEmpty() || chatTemplate.isEmpty() || chatSound.isEmpty()) {
            Highlighter.LOGGER.warn("Cannot save prefix with empty fields");

            // Проверяем и подсвечиваем пустые поля
            highlightEmptyFields();
            return;
        }

        if (prefix != null) {
            // Обновляем существующий префикс
            prefix.setPrefixTag(name);
            prefix.setPrefixChar(prefixContent);
            prefix.setPlayerColor(playerColor);
            prefix.setPrefixColor(prefixColor);
            prefix.setChatTemplate(chatTemplate);
            prefix.setChatSound(chatSound);
            HighlighterClient.STORAGE_MANAGER.getPrefixStorage().setPrefix(prefix);

            if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.MINIMAL.ordinal()) {
                Highlighter.LOGGER.info("Updated prefix: {}", name);
            }
        } else {
            // Создаем новый префикс
            Highlighter.LOGGER.info("Creating new prefix with name: {}, content: {}, playerColor: {}, prefixColor: {}, chatTemplate: {}, chatSound: {}",
                    name, prefixContent, playerColor, prefixColor, chatTemplate, chatSound);

            Prefix newPrefix = new Prefix(UUID.randomUUID(), name, chatTemplate, chatSound, prefixContent, playerColor, prefixColor);
            HighlighterClient.STORAGE_MANAGER.getPrefixStorage().addPrefix(newPrefix);
        }

        parent.updatePrefixes();
    }

    /**
     * Подсвечивает пустые поля красной рамкой
     */
    private void highlightEmptyFields() {
        for (ClickableWidget field : fields) {
            if (field instanceof TextFieldWidget textField && textField.getText().isEmpty()) {
                // Отметим пустое поле (в будущих версиях можно добавить визуальное выделение)
                if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.MINIMAL.ordinal()) {
                    Highlighter.LOGGER.warn("Empty field: {}", textField.getMessage().getString());
                }
            }
        }
    }

    /**
     * Устанавливает данные префикса в поля формы
     */
    public void setPrefix(Prefix prefix) {
        this.prefix = prefix;
        if (prefix != null) {
            tagField.setText(prefix.getPrefixTag());
            contentField.setText(prefix.getPrefixChar());
            playerColorField.setColor(prefix.getPlayerColor());
            prefixColorField.setColor(prefix.getPrefixColor());
            chatTemplateField.setText(prefix.getChatTemplate());
            chatSoundField.setText(prefix.getChatSound());
        } else {
            tagField.setText("");
            contentField.setText("");
            playerColorField.setColor("#FFFFFF");
            prefixColorField.setColor("#FFFFFF");
            chatTemplateField.setText("");
            chatSoundField.setText("");
        }
    }

    /**
     * Создает стандартное текстовое поле
     */
    private TextFieldWidget createField(String placeholder, int y) {
        TextFieldWidget field = new TextFieldWidget(
                textRenderer,
                x + HORIZONTAL_PADDING,
                y + LABEL_SPACING,
                width - HORIZONTAL_PADDING * 2,
                FIELD_HEIGHT,
                Text.literal(placeholder)
        );

        field.setMaxLength(128);
        return field;
    }

    /**
     * Создает поле выбора цвета
     */
    private ColorTextFieldWidget createColorField(String placeholder, int y, Consumer<String> onChange) {
        ColorTextFieldWidget field = new ColorTextFieldWidget(
                client,
                x + HORIZONTAL_PADDING,
                y + LABEL_SPACING,
                width - HORIZONTAL_PADDING * 2,
                FIELD_HEIGHT,
                Text.literal(placeholder),
                onChange
        );
        field.setColor("#FFFFFF");
        return field;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        renderTitle(context);

        // Отрисовка полей и их лейблов
        for (int i = 0; i < fields.size(); i++) {
            ClickableWidget field = fields.get(i);
            String label = field instanceof TextFieldWidget textField ? textField.getMessage().getString() : "";
            int labelY = y + HORIZONTAL_PADDING + VERTICAL_SPACING * i;

            // Рендер лейбла с тенью
            context.drawTextWithShadow(
                    textRenderer,
                    label,
                    x + HORIZONTAL_PADDING,
                    labelY,
                    0xFFFFFF
            );

            // Рендер текстового поля с дебагом фокуса
            field.render(context, mouseX, mouseY, delta);

            // Если поле в фокусе и включен дебаг, выводим информацию
            Element focusedElement = FocusManager.getInstance().getFocusedElement();
            if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.VERBOSE.ordinal() &&
                focusedElement == field) {
                GuiAdapter.drawTextWithShadow(
                    context,
                    client,
                    "[FOCUS]",
                    field.getX() + field.getWidth() + 5,
                    field.getY() + 5,
                    0x00FF00
                );
            }

            // Для цветовых полей добавляем предпросмотр, если не используется встроенный
            if (field instanceof ColorTextFieldWidget colorField) {
                try {
                    int color = Integer.parseInt(colorField.getText().substring(1), 16) | 0xFF000000;
                    GuiAdapter.renderColorPreview(
                        context,
                        field.getX() + field.getWidth() + 5,
                        field.getY(),
                        15,
                        color
                    );
                } catch (Exception ignored) {}
            }
        }

        // Рендер кнопки сохранения
        GuiAdapter.renderButton(context, saveButton, mouseX, mouseY, delta);
    }

    /**
     * Отрисовка фона виджета
     */
    private void renderBackground(DrawContext context) {
        GuiAdapter.drawGuiTexture(context, BACKGROUND_TEXTURE, x, y, width, height);
    }

    /**
     * Отрисовка заголовка виджета
     */
    private void renderTitle(DrawContext context) {
        Text title = prefix != null ?
                Text.literal("Edit Prefix: " + prefix.getPrefixTag()) :
                Text.literal("Create Prefix");

        GuiAdapter.drawCenteredTextWithShadow(
            context,
            client,
            title,
            GuiAdapter.centerX(x, width, textRenderer.getWidth(title)),
            y + TITLE_Y_OFFSET,
            0xFFFFFF
        );
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return GuiAdapter.isPointInRect(mouseX, mouseY, x, y, width, height);
    }

    @Override
    protected List<? extends Element> getGuiChildren() {
        return guiElements;
    }

    public ColorTextFieldWidget getPlayerColorField() { return playerColorField; }
    public ColorTextFieldWidget getPrefixColorField() { return prefixColorField; }

    /**
     * Закрывает все открытые цветовые пикеры
     */
    public void clearColorPickers() {
        if (playerColorField != null) {
            playerColorField.reset();
        }
        if (prefixColorField != null) {
            prefixColorField.reset();
        }
    }

    @Override
    public void clearFocus() {
        super.clearFocus();
        clearColorPickers();
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return this.focused;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // TODO: Реализовать поддержку нарраций для доступности
        builder.put(NarrationPart.TITLE, Text.literal(prefix != null ?
                "Edit Prefix Form" : "Create Prefix Form"));
    }

    @Override
    public boolean isPointInside(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
