package me.dalynkaa.highlighter.client.gui.widgets;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import lombok.Setter;
import me.dalynkaa.highlighter.Highlighter;

import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.adapters.ColorAdapter;
import me.dalynkaa.highlighter.client.adapters.GuiAdapter;
import me.dalynkaa.highlighter.client.gui.HighlightScreen;
import me.dalynkaa.highlighter.client.gui.widgets.colorPicker.ColorPickerFieldWidget;
import me.dalynkaa.highlighter.client.gui.widgets.dropdown.HighlighterScrollDropdownComponent;
import me.dalynkaa.highlighter.client.utilities.CustomNotificationEffects;
import me.dalynkaa.highlighter.client.utilities.SoundUtils;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.logging.Logger;


public class HighlighterPrefixEditWidget extends FlowLayout {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("social_interactions/background");
    private HighlightScreen highlightScreen;
    @Setter
    private @Nullable Prefix prefix;

    private final TextRenderer textRenderer;

    private ColorPickerFieldWidget currentColorField;

    private int x;
    private int y;
    private int width;
    private int height;
    TextBoxComponent name, tag, chatPattern;
    ColorPickerFieldWidget nameColorField, tagColorField;
    HighlighterScrollDropdownComponent chatSoundDropdown;
    ButtonComponent saveButton, cancelButton;
    String mainChatSound = null;

    // Флаг, указывающий, открыт ли выпадающий список
    private boolean isDropdownExpanded = false;

    public HighlighterPrefixEditWidget(HighlightScreen parent, int x, int y, int width, int height, @Nullable Prefix prefix) {
        super(Sizing.fill(), Sizing.fill(),Algorithm.VERTICAL);
        this.highlightScreen = parent;
        this.prefix = prefix;
        this.positioning(Positioning.absolute(x, y));
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;


        FlowLayout nameLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());
        this.name = Components.textBox(Sizing.fill(),prefix == null ? "": prefix.getPrefixTag());
        this.name.setMaxLength(20);
        this.name.setPlaceholder(Text.translatable("gui.highlighter.menu.prefix_edit.form.prefix_name.placeholder"));
        this.name.tooltip(Text.translatable("gui.highlighter.menu.prefix_edit.form.prefix_name.tooltip"));
        LabelComponent nameLabel = Components.label(Text.translatable("gui.highlighter.menu.prefix_edit.form.prefix_name.label"));
        nameLayout.child(nameLabel).child(this.name);

        FlowLayout tagLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());
        this.tag = Components.textBox(Sizing.fill(), prefix == null ? "": prefix.getPrefixChar());
        this.tag.setMaxLength(12);
        this.tag.setPlaceholder(Text.translatable("gui.highlighter.menu.prefix_edit.form.prefix_tag.placeholder"));
        this.tag.tooltip(Text.translatable("gui.highlighter.menu.prefix_edit.form.prefix_tag.tooltip"));
        LabelComponent tagLabel = Components.label(Text.translatable("gui.highlighter.menu.prefix_edit.form.prefix_tag.label"));
        tagLayout.child(tagLabel).child(this.tag);

        this.nameColorField = new ColorPickerFieldWidget(0, 0, width-16, prefix == null ? 0xFF0000 : ColorAdapter.fromHexString(prefix.getPlayerColor()), (color)->{
            return;
        });
        this.nameColorField.zIndex(1000);
        this.nameColorField.onPopupOpenEvent((colorPicker) -> {
            if(currentColorField != null) {
                currentColorField.closePopup();
            }
            this.currentColorField = colorPicker;
        });
        this.nameColorField.onPopupClosedEvent((colorPicker) -> {
            if (this.currentColorField != null && this.currentColorField == colorPicker) {
                this.currentColorField = null;
            }
        });
        //this.nameColorField.setTooltip(Tooltip.of(Text.translatable("gui.highlighter.menu.prefix_edit.form.player_name_color.tooltip")));
        if (prefix!= null) {
            int color = ColorAdapter.fromHexString(prefix.getPlayerColor());
            Highlighter.LOGGER.info("Setting name color: {}", color);
            this.nameColorField.setColor(color);
        }
        LabelComponent nameColorLabel = Components.label(Text.translatable("gui.highlighter.menu.prefix_edit.form.player_name_color.label"));
        nameColorLabel.sizing(Sizing.content(), Sizing.fixed(10));

        this.tagColorField = new ColorPickerFieldWidget(0, 0, width-16, prefix == null ? 0xFF0000 : ColorAdapter.fromHexString(prefix.getPrefixColor()), (color)->{
            return;
        });
        this.tagColorField.zIndex(999);
        this.tagColorField.onPopupOpenEvent((colorPicker) -> {
            if(currentColorField != null) {
                currentColorField.closePopup();
            }
            this.currentColorField = colorPicker;
        });
        this.tagColorField.onPopupClosedEvent((colorPicker) -> {
            if (this.currentColorField != null && this.currentColorField == colorPicker) {
                this.currentColorField = null;
            }
        });
        //this.tagColorField.setTooltip(Tooltip.of(Text.translatable("gui.highlighter.menu.prefix_edit.form.prefix_color.tooltip")).);
        if (prefix!= null) {
            this.tagColorField.setColor(ColorAdapter.fromHexString(prefix.getPrefixColor()));
        }
        LabelComponent tagColorLabel = Components.label(Text.translatable("gui.highlighter.menu.prefix_edit.form.prefix_color.label"));
        Text chatSoundInitial = prefix == null ? Text.literal("Chat sound") : Text.literal(prefix.getChatSound() == null ? "None" : prefix.getChatSound());

        // Установка повышенного z-index и настройка обработчиков для выпадающего списка
        this.chatSoundDropdown = new HighlighterScrollDropdownComponent(Sizing.fill(), Sizing.content(), chatSoundInitial, false);

        // Отслеживаем изменение состояния выпадающего списка
        this.chatSoundDropdown.onExpandStateChanged(expanded -> {
            Highlighter.LOGGER.info("Setting chat sound: {}", expanded);
            this.isDropdownExpanded = expanded;
            this.saveButton.active = !expanded;
            this.chatPattern.active = !expanded;
        });
        if (prefix!=null && prefix.getChatSound()!= null) {
            this.mainChatSound = prefix.getChatSound();
            this.chatSoundDropdown.title(Text.literal(prefix.getChatSound()));
        }

        this.chatSoundDropdown.button(Text.literal("None"), (comp) -> {
            this.chatSoundDropdown.title(Text.literal("None"));
            this.mainChatSound = null;
        });
        for (CustomNotificationEffects effects : CustomNotificationEffects.values()) {
            if (effects.getSoundEvent() != null) {
                this.chatSoundDropdown.button(Text.literal(effects.getName()), (comp) -> {
                    this.chatSoundDropdown.title(Text.literal(effects.getName()));
                    this.mainChatSound = effects.getName();
                    SoundUtils.playSound(effects.getSoundEvent());
                });
            }
        }
        //this.chatSoundDropdown.tooltip(Text.translatable("gui.highlighter.menu.prefix_edit.form.chat_sound.tooltip"));
        LabelComponent chatSoundLabel = Components.label(Text.translatable("gui.highlighter.menu.prefix_edit.form.chat_sound.label"));
        chatSoundLabel.sizing(Sizing.content(), Sizing.fixed(10));

        FlowLayout chatPatternLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());
        this.chatPattern = Components.textBox(Sizing.fill());
        this.chatPattern.setMaxLength(1000);
        if (prefix!=null){
            this.chatPattern.text(prefix.getChatTemplate());
        }
        this.chatPattern.setPlaceholder(Text.translatable("gui.highlighter.menu.prefix_edit.form.chat_pattern.placeholder"));
        this.chatPattern.tooltip(Text.translatable("gui.highlighter.menu.prefix_edit.form.chat_pattern.tooltip"));
        LabelComponent chatPatternLabel = Components.label(Text.translatable("gui.highlighter.menu.prefix_edit.form.chat_pattern.label"));
        chatPatternLayout.child(chatPatternLabel).child(this.chatPattern);
        this.saveButton = Components.button(Text.translatable("gui.highlighter.menu.prefix_edit.button.save"), (button) -> {

            String prefixName = this.name.getText().trim();
            String prefixTag = this.tag.getText().trim();
            String playerColor = ColorAdapter.rgbToHex(this.nameColorField.getColor());
            String prefixColor = ColorAdapter.rgbToHex(this.tagColorField.getColor());
            String chatSound = this.mainChatSound;
            String chatTemplate = this.chatPattern.getText().trim();

            if (prefixName.isEmpty() || prefixTag.isEmpty() || playerColor.isEmpty() || prefixColor.isEmpty()) {
                Highlighter.LOGGER.warn("Prefix creation failed: All fields must be filled.");
                return;
            }
            if (chatSound == null || chatSound.isEmpty() || chatSound.equals("None")) {
                chatSound = null;
            }
            if (chatTemplate.isEmpty()) {
                chatTemplate = null;
            }

            if (this.prefix == null) {
                this.prefix = new Prefix(UUID.randomUUID(), prefixName, chatTemplate, chatSound, prefixTag, playerColor, prefixColor);
                HighlighterClient.STORAGE_MANAGER.getPrefixStorage().addPrefix(this.prefix);
            } else {
                this.prefix.setPrefixTag(prefixName);
                this.prefix.setPrefixChar(prefixTag);
                this.prefix.setPlayerColor(playerColor);
                this.prefix.setPrefixColor(prefixColor);
                this.prefix.setChatSound(chatSound);
                this.prefix.setChatTemplate(chatTemplate);
                HighlighterClient.STORAGE_MANAGER.getPrefixStorage().setPrefix(this.prefix);
            }
            highlightScreen.updatePrefixList();
            highlightScreen.setCurrentPrefix(null);
        });
        this.saveButton.tooltip(Text.translatable("gui.highlighter.menu.prefix_edit.button.save.tooltip"));

        int startX = 8;
        int startY = 8;
        int spacing = 32;
        int labelHeight = 10;

        this
                .child(this.chatSoundDropdown.allowOverflow(true).positioning(Positioning.absolute(startX, startY + spacing * 4 + labelHeight)).sizing(Sizing.fixed(width-15), Sizing.fixed(20)))
                .child(nameLayout.positioning(Positioning.absolute(startX, startY)).sizing(Sizing.fixed(width-14), Sizing.fixed(30)))
                .child(tagLayout.positioning(Positioning.absolute(startX,startY+spacing)).sizing(Sizing.fixed(width-14), Sizing.fixed(30)))
                .child(nameColorLabel.positioning(Positioning.absolute(startX,startY+spacing*2)))
                .child(this.nameColorField.positioning(Positioning.absolute(startX, startY+spacing*2+labelHeight)))
                .child(tagColorLabel.positioning(Positioning.absolute(startX, startY+spacing*3)))
                .child(this.tagColorField.positioning(Positioning.absolute(startX, startY+spacing*3+labelHeight)))
                .child(chatSoundLabel.positioning(Positioning.absolute(startX, startY+spacing*4)))
                .child(chatPatternLayout.positioning(Positioning.absolute(startX, startY + spacing * 5)).sizing(Sizing.fixed(width-14), Sizing.fixed(30)))
                .child(this.saveButton.positioning(Positioning.absolute(startX, startY + spacing * 6 + 10)).sizing(Sizing.fixed(width-14), Sizing.fixed(20)));
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        renderBackground(context);
    }

    private void renderBackground(DrawContext context) {
        GuiAdapter.drawGuiTexture(context,BACKGROUND_TEXTURE, this.x, this.y, this.width+2, this.height);
        String text = prefix == null ? Text.translatable("gui.highlighter.menu.prefix_edit.create.title").getString() : Text.translatable("gui.highlighter.menu.prefix_edit.edit.title", prefix.getPrefixTag()).getString();
        context.drawCenteredTextWithShadow(textRenderer, text, this.x + width/2, this.y - 12, 0xFFFFFF);
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (isDropdownExpanded && isMouseOverDropdown(mouseX, mouseY)) {
            return super.onMouseDown(mouseX, mouseY, button);
        }
        return super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        if (isDropdownExpanded && isMouseOverDropdown(x, y)) {
            return true;
        }else {
            return super.isInBoundingBox(x, y);
        }
    }

    private boolean isMouseOverDropdown(double mouseX, double mouseY) {
        if (!isDropdownExpanded) return false;
        return chatSoundDropdown.isInBoundingBox(mouseX, mouseY);
    }
}