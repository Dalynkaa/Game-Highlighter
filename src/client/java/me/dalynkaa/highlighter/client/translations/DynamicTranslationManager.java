package me.dalynkaa.highlighter.client.translations;

import me.dalynkaa.highlighter.Highlighter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;

import java.util.concurrent.CompletableFuture;

public class DynamicTranslationManager {
    private static String currentLanguage = "en_us";
    private static boolean isInitialized = false;
    private static DynamicLanguageResourcePack resourcePack;
    
    /**
     * Инициализирует менеджер переводов
     */
    public static void initialize() {
        if (isInitialized) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getLanguageManager() != null) {
            currentLanguage = client.getLanguageManager().getLanguage();
            
            // Создаем resource pack для динамических переводов
            resourcePack = new DynamicLanguageResourcePack();
            
            TranslationDownloader.preloadCurrentLanguage();
            isInitialized = true;
            
            Highlighter.LOGGER.info("[DynamicTranslationManager] Initialized with language: {}", currentLanguage);
        } else {
            // Отложенная инициализация - попробуем позже
            Highlighter.LOGGER.debug("[DynamicTranslationManager] Deferred initialization - LanguageManager not ready yet");
        }
    }
    
    /**
     * Обновляет язык и загружает новые переводы
     */
    public static void updateLanguage(String newLanguage) {
        if (!newLanguage.equals(currentLanguage)) {
            currentLanguage = newLanguage;
            
            // Загружаем переводы для нового языка
            TranslationDownloader.downloadTranslation(newLanguage)
                .thenAccept(success -> {
                    if (success) {
                        Highlighter.LOGGER.info("[DynamicTranslationManager] Downloaded translations for new language: {}", newLanguage);
                        // После загрузки Minecraft автоматически подхватит новые файлы
                    }
                });
            
            Highlighter.LOGGER.info("[DynamicTranslationManager] Language updated to: {}", newLanguage);
        }
    }
    
    /**
     * Принудительно обновляет переводы с сервера
     */
    public static CompletableFuture<Void> forceRefresh() {
        return TranslationDownloader.downloadTranslation(currentLanguage)
            .thenAccept(success -> {
                if (success) {
                    Highlighter.LOGGER.info("[DynamicTranslationManager] Force refreshed translations");
                }
            });
    }
    
    /**
     * Получает текущий язык
     */
    public static String getCurrentLanguage() {
        return currentLanguage;
    }
    
    /**
     * Проверяет есть ли кешированные переводы для текущего языка
     */
    public static boolean hasTranslationsForCurrentLanguage() {
        return TranslationDownloader.hasCachedTranslation(currentLanguage);
    }
    
    /**
     * Проверяет инициализирован ли менеджер переводов
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
}