package me.dalynkaa.highlighter.client.adapters;

import me.dalynkaa.highlighter.client.newgui.widgets.ColorTextFieldWidget;
import me.dalynkaa.highlighter.client.newgui.widgets.HighlighterDropdownWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Фабрика для создания виджетов
 * Содержит методы, которые могут изменяться при обновлении Minecraft
 */
public class WidgetFactory {
    
    /**
     * Создает текстовое поле
     */
    public static TextFieldWidget createTextField(MinecraftClient client, int x, int y, int width, int height, String placeholder) {
        return new TextFieldWidget(client.textRenderer, x, y, width, height, Text.literal(placeholder));
    }
    
    /**
     * Создает текстовое поле для выбора цвета
     */
    public static ColorTextFieldWidget createColorField(MinecraftClient client, int x, int y, int width, int height, String placeholder, Consumer<String> onChange) {
        return new ColorTextFieldWidget(client, x, y, width, height, Text.literal(placeholder), onChange);
    }
    
    /**
     * Создает кнопку
     */
    public static ButtonWidget createButton(int x, int y, int width, int height, String text, ButtonWidget.PressAction onPress) {
        return ButtonWidget.builder(Text.literal(text), onPress)
                .dimensions(x, y, width, height)
                .build();
    }
    
    /**
     * Создает выпадающий список
     */
    public static HighlighterDropdownWidget createDropdown(int x, int y, int width, int height, Map<String, String> options, 
                                                        String initialId, String label, Consumer<String> onSelect) {
        return new HighlighterDropdownWidget(x, y, width, height, options, initialId, Text.literal(label), onSelect);
    }
}
