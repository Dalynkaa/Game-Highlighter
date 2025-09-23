package me.dalynkaa.highlighter.client.translations;

import me.dalynkaa.highlighter.Highlighter;
import net.minecraft.client.MinecraftClient;

public class LanguageChangeListener {
    private static String lastKnownLanguage = null;
    private static boolean isMonitoring = false;
    
    /**
     * Начинает мониторинг смены языка
     */
    public static void startMonitoring() {
        if (isMonitoring) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getLanguageManager() != null) {
            lastKnownLanguage = client.getLanguageManager().getLanguage();
            isMonitoring = true;
            
            Highlighter.LOGGER.info("[LanguageChangeListener] Started monitoring language changes, current: {}", 
                lastKnownLanguage);
        } else {
            Highlighter.LOGGER.debug("[LanguageChangeListener] Cannot start monitoring - LanguageManager not ready");
        }
    }
    
    /**
     * Проверяет изменился ли язык (вызывается из тика клиента)
     */
    public static void checkLanguageChange() {
        if (!isMonitoring) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getLanguageManager() == null) return;
        
        // Если мониторинг не был инициализирован из-за раннего вызова, попробуем снова
        if (!isMonitoring && lastKnownLanguage == null) {
            startMonitoring();
            return;
        }
        
        String currentLanguage = client.getLanguageManager().getLanguage();
        
        if (lastKnownLanguage != null && !lastKnownLanguage.equals(currentLanguage)) {
            Highlighter.LOGGER.info("[LanguageChangeListener] Language changed from {} to {}", 
                lastKnownLanguage, currentLanguage);
            
            // Обновляем переводы
            DynamicTranslationManager.updateLanguage(currentLanguage);
            
            lastKnownLanguage = currentLanguage;
        }
    }
    
    /**
     * Останавливает мониторинг
     */
    public static void stopMonitoring() {
        isMonitoring = false;
        lastKnownLanguage = null;
        Highlighter.LOGGER.info("[LanguageChangeListener] Stopped monitoring language changes");
    }
}