package me.dalynkaa.highlighter.client.gui;

import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.config.ServerEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ServerSettingsScreen extends Screen {
    private final Screen parent;
    private final ServerInfo server;
    private final ServerEntry serverEntry;
    private boolean enabled;
    private boolean tabHighlight;
    private boolean chatHighlight;

    private ButtonWidget enabledButton;
    private ButtonWidget tabHighlightButton;
    private ButtonWidget chatHighlightButton;
    private ButtonWidget addFieldButton;

    private final List<RegexField> regexFields = new ArrayList<>();

    public ServerSettingsScreen(Screen parent, ServerInfo server) {
        super(Text.translatable("gui.highlighter.menu.server_settings.title"));
        this.parent = parent;
        this.server = server;
        this.serverEntry = HighlighterClient.STORAGE_MANAGER.getServerStorage().getOrCreateServerEntry(server);
        this.enabled = serverEntry.isEnabled();
        this.tabHighlight = serverEntry.isUseTabHighlighter();
        this.chatHighlight = serverEntry.isUseChatHighlighter();
    }

    @Override
    protected void init() {
        this.clearChildren();
        regexFields.clear();

        int y = 30;
        int padding = 5;

        // Toggle buttons
        enabledButton = ButtonWidget.builder(settingText("gui.highlighter.menu.server_settings.enabled", enabled), button -> {
            enabled = !enabled;
            button.setMessage(settingText("gui.highlighter.menu.server_settings.enabled", enabled));
        }).position(this.width / 2 - 100, y).size(200, 20).build();
        this.addDrawableChild(enabledButton);
        y += 25 + padding;

        tabHighlightButton = ButtonWidget.builder(settingText("gui.highlighter.menu.server_settings.tab_highlight", tabHighlight), button -> {
            tabHighlight = !tabHighlight;
            button.setMessage(settingText("gui.highlighter.menu.server_settings.tab_highlight", tabHighlight));
        }).position(this.width / 2 - 100, y).size(200, 20).build();
        this.addDrawableChild(tabHighlightButton);
        y += 25 + padding;

        chatHighlightButton = ButtonWidget.builder(settingText("gui.highlighter.menu.server_settings.chat_highlight", chatHighlight), button -> {
            chatHighlight = !chatHighlight;
            button.setMessage(settingText("gui.highlighter.menu.server_settings.chat_highlight", chatHighlight));
        }).position(this.width / 2 - 100, y).size(200, 20).build();
        this.addDrawableChild(chatHighlightButton);
        y += 30 + padding;


        // Load or create regex fields
        String[] initial = serverEntry.getChatRegex();
        if (initial.length == 0) {
            addRegexField("");
        } else {
            for (String value : initial) {
                addRegexField(value);
            }
        }

        // Add "+" button
        addFieldButton = ButtonWidget.builder(Text.literal("+"), b -> {
            addRegexField("");
            updateRegexFieldPositions();
        }).position(this.width / 2 + 105, y).size(20, 20).build();
        addFieldButton.tooltip(Text.translatable("gui.highlighter.menu.server_settings.regex_filter.add.tooltip"));
        this.addDrawableChild(addFieldButton);

        // Save/Cancel buttons
        y = this.height - 30;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.highlighter.menu.server_settings.save"), button -> {
            List<String> regexList = new ArrayList<>();
            for (RegexField field : regexFields) {
                String text = field.input.getText().trim();
                if (!text.isEmpty()) {
                    regexList.add(text);
                }
            }

            serverEntry.setEnabled(enabled);
            serverEntry.setUseTabHighlighter(tabHighlight);
            serverEntry.setUseChatHighlighter(chatHighlight);
            serverEntry.setChatRegex(regexList.toArray(new String[0]));

            this.client.setScreen(parent);
        }).position(this.width / 2 - 100, y).size(95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.highlighter.menu.server_settings.cancel"), button -> {
            this.client.setScreen(parent);
        }).position(this.width / 2 + 5, y).size(95, 20).build());

        updateRegexFieldPositions();
    }

    private void addRegexField(String value) {
        if (regexFields.size() >= 4) return;

        TextFieldWidget input = new TextFieldWidget(this.textRenderer, 0, 0, 200, 20, Text.translatable("gui.highlighter.menu.server_settings.regex_filters.field"));
        input.setText(value);
        this.addDrawableChild(input);

        ButtonWidget removeButton = ButtonWidget.builder(Text.literal("X"), b -> {
            removeRegexField(input);
        }).position(0, 0).size(20, 20).build();
        removeButton.tooltip(Text.translatable("gui.highlighter.menu.server_settings.regex_filter.remove.tooltip"));
        this.addDrawableChild(removeButton);

        regexFields.add(new RegexField(input, removeButton));
    }

    private void removeRegexField(TextFieldWidget input) {
        RegexField target = null;
        for (RegexField field : regexFields) {
            if (field.input == input) {
                target = field;
                break;
            }
        }

        if (target != null) {
            this.remove(target.input);
            this.remove(target.removeButton);
            regexFields.remove(target);
            updateRegexFieldPositions();
        }
    }

    private void updateRegexFieldPositions() {
        int START_Y = 130;
        int y = START_Y;
        boolean showX = regexFields.size() > 1;

        for (RegexField rf : regexFields) {
            rf.input.setX(this.width / 2 - 100);
            rf.input.setY(y);
            rf.removeButton.setX(this.width / 2 + 105);
            rf.removeButton.setY(y);
            rf.removeButton.visible = showX;
            y += 25;
        }

        if (addFieldButton != null) {
            addFieldButton.visible = regexFields.size() < 4;
            if (regexFields.size() ==1){
                addFieldButton.setY(START_Y);
            }else {
                addFieldButton.setY(START_Y + regexFields.size() * 25);
            }
        }
    }

    private Text settingText(String name, boolean value) {
        return Text.translatable(name, value ? Text.translatable("gui.highlighter.menu.server_settings.yes") : Text.translatable("gui.highlighter.menu.server_settings.no"));
    }

    private record RegexField(TextFieldWidget input, ButtonWidget removeButton) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        context.drawText(this.textRenderer, Text.translatable("gui.highlighter.menu.server_settings.regex_filters.label"), this.width / 2 - 100, 120, 0xFFFFFF, false);


    }
}
