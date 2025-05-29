package me.dalynkaa.highlighter.client.adapters;

import me.dalynkaa.highlighter.Highlighter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
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
        //? if =1.21.1 {
        context.drawGuiTexture(texture, x, y, width, height);
        //?} else {
        /*context.drawGuiTexture(RenderLayer::getGuiTextured,texture, x, y, width, height);
        *///?}
    }

    public static void drawTexture(DrawContext context, Identifier texture, int x, int y,int u,int v,int width,int height, int textureWidth, int textureHeight) {
        //? if =1.21.1 {
        context.drawTexture(texture,
                x , y,
                u, v,
                width, height,
                textureWidth, textureHeight
        );
        //?} else {
        /*context.drawTexture(RenderLayer::getGuiTextured, texture,
                x, y,
                u, v,
                width, height,
                textureWidth, textureHeight
        );
        *///?}
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
