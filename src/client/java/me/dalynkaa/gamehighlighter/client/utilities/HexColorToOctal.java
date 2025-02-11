package me.dalynkaa.gamehighlighter.client.utilities;

public class HexColorToOctal {
    public static String convert(String hexColor) {
        String hex = hexColor.substring(1); // Remove the #
        int decimal = Integer.parseInt(hex, 16);
        return Integer.toOctalString(decimal);
    }
}
