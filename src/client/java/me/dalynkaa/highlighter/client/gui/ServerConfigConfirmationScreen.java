package me.dalynkaa.highlighter.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Диалог подтверждения использования серверной конфигурации
 */
public class ServerConfigConfirmationScreen extends Screen {
    private final Screen parent;
    private final String serverName;
    private final Consumer<Boolean> callback;
    private final int DIALOG_WIDTH = 300;
    private final int DIALOG_HEIGHT = 120;
    private final List<Drawable> buttons = new ArrayList<>();

    public ServerConfigConfirmationScreen(Screen parent, String serverName, Consumer<Boolean> callback) {
        super(Text.translatable("highlighter.dialog.server_config.title"));
        this.parent = parent;
        this.serverName = serverName;
        this.callback = callback;
    }


    @Override
    protected void init() {
        super.init();
        
        int dialogX = (this.width - DIALOG_WIDTH) / 2;
        int dialogY = (this.height - DIALOG_HEIGHT) / 2;
        
        // Кнопка "Да"
        ButtonWidget yesButton = ButtonWidget.builder(
                Text.translatable("highlighter.dialog.server_config.yes"),
                button -> {
                    this.callback.accept(true);
                    this.close();
                })
                .position(dialogX + 20, dialogY + 80)
                .size(80, 20)
                .build();
        
        // Кнопка "Нет"
        ButtonWidget noButton = ButtonWidget.builder(
                Text.translatable("highlighter.dialog.server_config.no"),
                button -> {
                    this.callback.accept(false);
                    this.close();
                })
                .position(dialogX + 120, dialogY + 80)
                .size(80, 20)
                .build();
        
        // Кнопка "Спросить позже"
        ButtonWidget laterButton = ButtonWidget.builder(
                Text.translatable("highlighter.dialog.server_config.later"),
                button -> {
                    this.callback.accept(null); // null означает "спросить позже"
                    this.close();
                })
                .position(dialogX + 220, dialogY + 80)
                .size(60, 20)
                .build();
        
        buttons.add(yesButton);
        buttons.add(noButton);
        buttons.add(laterButton);
        this.addSelectableChild(yesButton);
        this.addSelectableChild(noButton);
        this.addSelectableChild(laterButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //super.renderBackground(context, mouseX, mouseY, delta);

        int dialogX = (this.width - DIALOG_WIDTH) / 2;
        int dialogY = (this.height - DIALOG_HEIGHT) / 2;
        
        // Рисуем диалог
        context.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xE0000000);
        context.drawBorder(dialogX, dialogY, DIALOG_WIDTH, DIALOG_HEIGHT, 0xFFFFFFFF);
        
        // Заголовок
        Text titleText = Text.translatable("highlighter.dialog.server_config.title");
        int titleWidth = this.textRenderer.getWidth(titleText);
        context.drawText(this.textRenderer, titleText, 
                        dialogX + (DIALOG_WIDTH - titleWidth) / 2, 
                        dialogY + 10, 0xFFFFFF, false);
        
        // Текст сообщения
        Text messageText = Text.translatable("highlighter.dialog.server_config.message", serverName);
        int messageWidth = this.textRenderer.getWidth(messageText);
        
        // Разбиваем длинное сообщение на строки если нужно
        if (messageWidth > DIALOG_WIDTH - 20) {
            String messageString = messageText.getString();
            String[] words = messageString.split(" ");
            StringBuilder line = new StringBuilder();
            int y = dialogY + 35;
            
            for (String word : words) {
                String testLine = line.length() > 0 ? line + " " + word : word;
                if (this.textRenderer.getWidth(testLine) > DIALOG_WIDTH - 20 && line.length() > 0) {
                    context.drawText(this.textRenderer, line.toString(), 
                                   dialogX + (DIALOG_WIDTH - this.textRenderer.getWidth(line.toString())) / 2,
                                   y, 0xFFFFFF, false);
                    line = new StringBuilder(word);
                    y += 12;
                } else {
                    line = new StringBuilder(testLine);
                }
            }
            if (line.length() > 0) {
                context.drawText(this.textRenderer, line.toString(), 
                               dialogX + (DIALOG_WIDTH - this.textRenderer.getWidth(line.toString())) / 2,
                               y, 0xFFFFFF, false);
            }
        } else {
            context.drawText(this.textRenderer, messageText, 
                           dialogX + (DIALOG_WIDTH - messageWidth) / 2, 
                           dialogY + 35, 0xFFFFFF, false);
        }

        for (Drawable drawable : buttons) {
            drawable.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // Предотвращаем закрытие по ESC - пользователь должен сделать выбор
    }
}