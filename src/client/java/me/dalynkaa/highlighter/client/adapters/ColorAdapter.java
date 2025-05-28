package me.dalynkaa.highlighter.client.adapters;

import io.wispforest.owo.ui.component.Components;
import net.minecraft.util.math.ColorHelper;

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
        return ColorHelper.Argb.getArgb(alpha, red, green, blue);
    }

    public static int getRgb(int red, int green, int blue) {
        return red << 16 | green << 8 | blue;
    }
    
    /**
     * Создает ABGR цвет из компонентов
     * @param alpha Альфа-канал (0-255)
     * @param blue Синий компонент (0-255)
     * @param green Зеленый компонент (0-255)
     * @param red Красный компонент (0-255)
     * @return целочисленное представление цвета
     */
    public static int getAbgr(int alpha, int red, int green, int blue) {
        return ColorHelper.Abgr.getAbgr(alpha, red, green, blue);
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

    /**
     * Конвертирует RGB в HSV
     * @param r Красный компонент (0-255)
     * @param g Зеленый компонент (0-255)
     * @param b Синий компонент (0-255)
     * @return Массив [H, S, V] где каждый компонент в диапазоне 0-1
     */
    public static float[] rgbToHsv(int r, int g, int b) {
        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        // Расчет оттенка (Hue)
        float h = 0;
        if (delta != 0) {
            if (max == rf) {
                h = ((gf - bf) / delta) % 6;
            } else if (max == gf) {
                h = ((bf - rf) / delta) + 2;
            } else { // max == bf
                h = ((rf - gf) / delta) + 4;
            }

            h /= 6;
            if (h < 0) h += 1;
        }

        // Расчет насыщенности (Saturation)
        float s = (max == 0) ? 0 : (delta / max);

        // Значение (Value)
        float v = max;

        return new float[]{h, s, v};
    }

    /**
     * Конвертирует HSV в RGB
     * @param h Оттенок (0-1)
     * @param s Насыщенность (0-1)
     * @param v Яркость (0-1)
     * @return Массив [R, G, B] где каждый компонент в диапазоне 0-255
     */

    public static int[] hsvToRgb(float h, float s, float v) {
        // Нормализуем оттенок до диапазона 0-1
        h = h - (float)Math.floor(h);

        int i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        float r, g, b;
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            case 5 -> { r = v; g = p; b = q; }
            default -> { r = v; g = t; b = p; } // Не должно происходить, но на всякий случай
        }

        return new int[]{
                Math.round(r * 255),
                Math.round(g * 255),
                Math.round(b * 255)
        };
    }

    public static String rgbToHex(int rgb) {
        int b = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int r = rgb & 0xFF;
        return String.format("#%02X%02X%02X", r, g, b);
    }
}
