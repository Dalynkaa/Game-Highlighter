package me.dalynkaa.highlighter.client.utilities;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

/**
 * Utility class for showing toast notifications in the game
 */
public class ToastNotification {
    
    /**
     * Shows a system toast notification
     * @param title The title of the notification
     * @param description The description text
     */
    public static void show(Text title, Text description) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getToastManager() != null) {
            client.getToastManager().add(SystemToast.create(client, SystemToast.Type.NARRATOR_TOGGLE, title, description));
        }
    }
    
    /**
     * Shows a system toast notification with string parameters
     * @param title The title of the notification
     * @param description The description text
     */
    public static void show(String title, String description) {
        show(Text.literal(title), Text.literal(description));
    }
    
    /**
     * Shows a system toast notification using translation keys
     * @param titleKey The translation key for the title
     * @param descriptionKey The translation key for the description
     */
    public static void showTranslated(String titleKey, String descriptionKey) {
        show(Text.translatable(titleKey), Text.translatable(descriptionKey));
    }
    
    /**
     * Shows a system toast notification with formatted description
     * @param titleKey The translation key for the title
     * @param descriptionKey The translation key for the description
     * @param args Arguments for the description formatting
     */
    public static void showTranslated(String titleKey, String descriptionKey, Object... args) {
        show(Text.translatable(titleKey), Text.translatable(descriptionKey, args));
    }
    
    /**
     * Shows a Highlighter-specific notification
     * @param messageKey The translation key for the message
     * @param args Arguments for message formatting
     */
    public static void showHighlighter(String messageKey, Object... args) {
        show(Text.literal("Highlighter"), Text.translatable(messageKey, args));
    }
}