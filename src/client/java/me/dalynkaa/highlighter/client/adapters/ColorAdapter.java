package me.dalynkaa.highlighter.client.adapters;

import io.wispforest.owo.ui.component.Components;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

/**
 * Адаптер для работы с цветами
 * Содержит методы, которые могут изменяться при обновлении Minecraft
 */
public class ColorAdapter {
    
    /**
     * Создает ARGB цвет из компонентов
     * @param alpha Альфа-канал (0-255)
     * @param red Красный компонент (0-255)
     * @param green Зеленый компонент (0-255)
     * @param blue Синий компонент (0-255)
     * @return целочисленное представление цвета
     */
    public static int getArgb(int alpha, int red, int green, int blue) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    public static int getRgb(int red, int green, int blue) {
        return red << 16 | green << 8 | blue;
    }
    
    /**
     * Преобразует hex-строку в цвет ARGB
     * @param hexColor Строка цвета в формате "#RRGGBB"
     * @param alpha Альфа-канал (0-255)
     * @return целочисленное представление цвета
     */
    public static int fromHexString(String hexColor, int alpha) {
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            return getArgb(alpha, r, g, b);
        } catch (Exception e) {
            return getArgb(alpha, 0, 0, 0); // Черный цвет по умолчанию
        }
    }

    public static int fromHexString(String hexColor) {
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            return getRgb( r, g, b);
        } catch (Exception e) {
            return getRgb(0, 0, 0); // Черный цвет по умолчанию
        }
    }
    
    /**
     * Проверяет, является ли строка допустимым hex цветом в формате #RRGGBB
     */
    public static boolean isValidHexColor(String color) {
        if (color == null || color.length() != 7 || !color.startsWith("#")) {
            return false;
        }
        
        try {
            // Пытаемся преобразовать части строки в числа
            Integer.parseInt(color.substring(1, 3), 16);
            Integer.parseInt(color.substring(3, 5), 16);
            Integer.parseInt(color.substring(5, 7), 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int[] hsbToRgb(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;

        if (saturation == 0) {
            r = g = b = Math.round(brightness * 255.0f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));

            switch ((int) h) {
                case 0:
                    r = Math.round(brightness * 255.0f);
                    g = Math.round(t * 255.0f);
                    b = Math.round(p * 255.0f);
                    break;
                case 1:
                    r = Math.round(q * 255.0f);
                    g = Math.round(brightness * 255.0f);
                    b = Math.round(p * 255.0f);
                    break;
                case 2:
                    r = Math.round(p * 255.0f);
                    g = Math.round(brightness * 255.0f);
                    b = Math.round(t * 255.0f);
                    break;
                case 3:
                    r = Math.round(p * 255.0f);
                    g = Math.round(q * 255.0f);
                    b = Math.round(brightness * 255.0f);
                    break;
                case 4:
                    r = Math.round(t * 255.0f);
                    g = Math.round(p * 255.0f);
                    b = Math.round(brightness * 255.0f);
                    break;
                case 5:
                    r = Math.round(brightness * 255.0f);
                    g = Math.round(p * 255.0f);
                    b = Math.round(q * 255.0f);
                    break;
            }
        }

        return new int[]{
                MathHelper.clamp(r, 0, 255),
                MathHelper.clamp(g, 0, 255),
                MathHelper.clamp(b, 0, 255)
        };
    }

    public static float[] rgbToHsb(int r, int g, int b) {
        float[] hsbvals = new float[3];
        int cmax = Math.max(r, Math.max(g, b));
        int cmin = Math.min(r, Math.min(g, b));

        float brightness = ((float) cmax) / 255.0f;
        float saturation = (cmax != 0) ? ((float) (cmax - cmin)) / ((float) cmax) : 0;
        float hue = 0;

        if (saturation != 0) {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));

            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;

            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }

        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    public static String rgbToHex(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return String.format("#%02X%02X%02X", r, g, b);
    }
}
