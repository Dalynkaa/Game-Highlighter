package me.dalynkaa.highlighter.client.config;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.HighlighterClient;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.CompletableFuture;

/**
 * Менеджер конфигураций с поддержкой двухэтапной загрузки:
 * 1. При подключении к серверу - обновление конфигурации сервера (получение слага)
 * 2. При заходе на сервер - применение конфигурации префиксов по слагу
 */
public class ConfigurationManager {
    
    /**
     * Обновляет конфигурацию сервера при подключении
     * Получает слаг конфигурации из API и сохраняет в настройки сервера
     */
    public static CompletableFuture<Boolean> updateServerConfiguration() {
        ModConfig.BackendSettings settings = HighlighterClient.CONFIG.backendSettings;
        
        if (!settings.autoLoadEnabled) {
            Highlighter.LOGGER.debug("[ConfigurationManager] Auto-load is disabled");
            return CompletableFuture.completedFuture(false);
        }
        
        String serverIp = getCurrentServerIp();
        if (serverIp == null) {
            Highlighter.LOGGER.warn("[ConfigurationManager] Cannot determine server IP");
            return CompletableFuture.completedFuture(false);
        }
        
        // Настраиваем URL API
        BackendConfigurationLoader.setBaseApiUrl(settings.apiBaseUrl);
        
        // Проверяем нужно ли обновлять конфигурацию (с интервалом 1 час)
        return BackendConfigurationLoader.checkAndUpdateServerConfiguration(serverIp)
            .exceptionally(throwable -> {
                Highlighter.LOGGER.error("[ConfigurationManager] Failed to update server configuration", throwable);
                return false;
            });
    }
    
    /**
     * Применяет только конфигурацию префиксов при заходе в игру
     * Серверные настройки НЕ применяются (они уже применены при подключении)
     */
    public static CompletableFuture<Boolean> applyPrefixConfiguration() {
        ModConfig.BackendSettings settings = HighlighterClient.CONFIG.backendSettings;
        
        // Настраиваем URL API
        BackendConfigurationLoader.setBaseApiUrl(settings.apiBaseUrl);
        
        // Проверяем приоритет: ручной слаг > слаг сервера
        if (settings.preferManualSlug && !settings.manualSlug.trim().isEmpty()) {
            // Ручная загрузка по глобальному слагу из настроек
            Highlighter.LOGGER.info("[ConfigurationManager] Applying manual prefix configuration: {}", 
                                  settings.manualSlug.trim());
            return BackendConfigurationLoader.loadConfigurationBySlug(settings.manualSlug.trim());
        } else if (settings.autoLoadEnabled) {
            // Загрузка префиксов по слагу из настроек сервера
            Highlighter.LOGGER.info("[ConfigurationManager] Applying server prefix configuration");
            return BackendConfigurationLoader.applyPrefixConfiguration();
        } else {
            // Загрузка отключена
            Highlighter.LOGGER.debug("[ConfigurationManager] Configuration loading is disabled");
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * @deprecated Используйте applyPrefixConfiguration()
     */
    @Deprecated
    public static CompletableFuture<Boolean> applyServerConfiguration() {
        return applyPrefixConfiguration();
    }

    /**
     * Загружает конфигурацию согласно настройкам пользователя
     * @deprecated Используйте updateServerConfiguration() и applyPrefixConfiguration()
     */
    @Deprecated
    public static CompletableFuture<Boolean> loadConfiguration() {
        return applyPrefixConfiguration();
    }
    
    /**
     * Получает IP текущего сервера
     */
    private static String getCurrentServerIp() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address;
        }
        return null;
    }
    
    /**
     * Загружает конфигурацию по указанному слагу, игнорируя настройки
     */
    public static CompletableFuture<Boolean> loadConfigurationBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            Highlighter.LOGGER.warn("[ConfigurationManager] Cannot load - slug is empty");
            return CompletableFuture.completedFuture(false);
        }
        
        BackendConfigurationLoader.setBaseApiUrl(HighlighterClient.CONFIG.backendSettings.apiBaseUrl);
        return BackendConfigurationLoader.loadConfigurationBySlug(slug.trim());
    }
    
    /**
     * Форсирует загрузку конфигурации по IP текущего сервера
     */
    public static CompletableFuture<Boolean> forceLoadByServerIp() {
        BackendConfigurationLoader.setBaseApiUrl(HighlighterClient.CONFIG.backendSettings.apiBaseUrl);
        return BackendConfigurationLoader.loadConfigurationByServerIp();
    }
    
    /**
     * Перезагружает последнюю успешно загруженную конфигурацию
     */
    public static CompletableFuture<Boolean> reloadConfiguration() {
        BackendConfigurationLoader.setBaseApiUrl(HighlighterClient.CONFIG.backendSettings.apiBaseUrl);
        return BackendConfigurationLoader.reloadLastConfiguration();
    }
    
    /**
     * Очищает всю загруженную конфигурацию
     */
    public static void clearConfiguration() {
        BackendConfigurationLoader.clearLoadedConfiguration();
        Highlighter.LOGGER.info("[ConfigurationManager] Configuration cleared");
    }
    
    /**
     * Возвращает информацию о последней загруженной конфигурации
     */
    public static String getConfigurationInfo() {
        return BackendConfigurationLoader.getLastConfigurationInfo();
    }
    
    /**
     * Проверяет, включена ли загрузка конфигураций
     */
    public static boolean isConfigurationLoadingEnabled() {
        ModConfig.BackendSettings settings = HighlighterClient.CONFIG.backendSettings;
        return settings.autoLoadEnabled || 
               (settings.preferManualSlug && !settings.manualSlug.trim().isEmpty());
    }
}