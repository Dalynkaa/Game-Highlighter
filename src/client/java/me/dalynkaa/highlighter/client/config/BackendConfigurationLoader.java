package me.dalynkaa.highlighter.client.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.config.ServerEntry;
import me.dalynkaa.highlighter.client.utilities.data.HighlightedPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import me.dalynkaa.highlighter.client.utilities.data.PrefixSource;
import me.dalynkaa.highlighter.client.utilities.ToastNotification;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Загрузчик конфигураций из бекенда:
 * - При подключении к серверу получает конфигурацию по IP и сохраняет слаг в настройки сервера
 * - При заходе на сервер применяет конфигурацию по сохраненному слагу
 * - Поддерживает фоновое обновление конфигураций серверов
 */
public class BackendConfigurationLoader {
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .registerTypeAdapter(UUID.class, new UuidDeserializer())
            .create();
    private static final Executor NETWORK_EXECUTOR = Executors.newSingleThreadExecutor();
    
    // Базовый URL API (может быть настроен в конфиге)
    private static String baseApiUrl = "http://localhost:4000";
    
    // Кеш последней загруженной конфигурации
    private static String lastServerIp = null;
    private static String lastConfigSlug = null;
    private static PrefixConfiguration lastConfiguration = null;
    private static ServerConfigurationResponse lastServerConfig = null;

    /**
     * Устанавливает базовый URL API
     */
    public static void setBaseApiUrl(String url) {
        baseApiUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        Highlighter.LOGGER.info("[BackendLoader] API URL set to: {}", baseApiUrl);
    }

    /**
     * Обновляет конфигурацию сервера из бекенда по IP и сохраняет слаг
     * Используется при первом подключении или обновлении конфигурации сервера
     */
    public static CompletableFuture<Boolean> updateServerConfiguration(String serverIp) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Highlighter.LOGGER.info("[BackendLoader] Updating server configuration for: {}", serverIp);
                
                // Получаем конфигурацию сервера из API
                ServerConfigurationResponse serverConfig = getServerConfiguration(serverIp);
                if (serverConfig == null) {
                    Highlighter.LOGGER.info("[BackendLoader] No backend configuration found for server: {}", serverIp);
                    return false;
                }
                
                // Сохраняем слаг конфигурации в настройки сервера
                MinecraftClient.getInstance().execute(() -> {
                    ServerEntry serverEntry = HighlighterClient.getServerEntry();
                    if (serverEntry != null) {
                        // Применяем серверные настройки только если пользователь включил автоматическую загрузку
                        if (serverEntry.isUseServerSettings()) {
                            serverEntry.updateConfigurationSlug(serverConfig.getDefaultConfigurationSlug());
                            serverEntry.setUseChatHighlighter(serverConfig.isChatEnabled());
                            serverEntry.setUseTabHighlighter(serverConfig.isTabEnabled());
                            
                            if (serverConfig.getChatRegexp() != null && !serverConfig.getChatRegexp().isEmpty()) {
                                String[] regexArray = serverConfig.getChatRegexp().toArray(new String[0]);
                                serverEntry.setChatRegex(regexArray);
                            }
                            
                            Highlighter.LOGGER.info("[BackendLoader] Applied server settings and updated configuration with slug: {}", 
                                                  serverConfig.getDefaultConfigurationSlug());
                            
                            // Show toast notification for server settings update
                            ToastNotification.showHighlighter("highlighter.notification.server_settings_updated");
                        } else {
                            Highlighter.LOGGER.info("[BackendLoader] Saved configuration slug but skipped server settings (manual mode): {}", 
                                                  serverConfig.getDefaultConfigurationSlug());
                        }
                    }
                });
                
                return true;
                
            } catch (Exception e) {
                Highlighter.LOGGER.error("[BackendLoader] Error updating server configuration for {}: {}", 
                                       serverIp, e.getMessage());
                return false;
            }
        }, NETWORK_EXECUTOR);
    }

    /**
     * Применяет только конфигурацию префиксов по слагу из настроек сервера
     * Используется при заходе в игру
     */
    public static CompletableFuture<Boolean> applyPrefixConfiguration() {
        ServerEntry serverEntry = HighlighterClient.getServerEntry();
        if (serverEntry == null) {
            Highlighter.LOGGER.debug("[BackendLoader] No server entry found");
            return CompletableFuture.completedFuture(false);
        }
        
        // Проверяем настройку "использовать серверные настройки"
        if (!serverEntry.isUseServerSettings()) {
            // Пользователь отключил автоматическую загрузку, используем ручной слаг если есть
            if (serverEntry.hasConfigurationSlug()) {
                String manualSlug = serverEntry.getConfigurationSlug();
                Highlighter.LOGGER.info("[BackendLoader] Using manual configuration slug: {}", manualSlug);
                return loadPrefixConfigurationBySlug(manualSlug)
                    .thenApply(success -> {
                        if (success) {
                            Highlighter.LOGGER.info("[BackendLoader] Successfully applied manual prefix configuration");
                        } else {
                            Highlighter.LOGGER.warn("[BackendLoader] Failed to apply manual prefix configuration for slug: {}", manualSlug);
                        }
                        return success;
                    });
            } else {
                Highlighter.LOGGER.debug("[BackendLoader] Server settings disabled and no manual slug provided");
                return CompletableFuture.completedFuture(false);
            }
        }
        
        // Используем автоматическую загрузку по слагу из серверных настроек
        if (!serverEntry.hasConfigurationSlug()) {
            Highlighter.LOGGER.debug("[BackendLoader] No configuration slug found for current server");
            return CompletableFuture.completedFuture(false);
        }
        
        String slug = serverEntry.getConfigurationSlug();
        Highlighter.LOGGER.info("[BackendLoader] Applying server prefix configuration with slug: {}", slug);
        
        return loadPrefixConfigurationBySlug(slug)
            .thenApply(success -> {
                if (success) {
                    Highlighter.LOGGER.info("[BackendLoader] Successfully applied server prefix configuration");
                } else {
                    Highlighter.LOGGER.warn("[BackendLoader] Failed to apply server prefix configuration for slug: {}", slug);
                }
                return success;
            });
    }

    /**
     * @deprecated Используйте applyPrefixConfiguration()
     */
    @Deprecated
    public static CompletableFuture<Boolean> applyServerConfiguration() {
        return applyPrefixConfiguration();
    }

    /**
     * Загружает конфигурацию автоматически по IP текущего сервера
     * @deprecated Используйте updateServerConfiguration() и applyServerConfiguration()
     */
    @Deprecated
    public static CompletableFuture<Boolean> loadConfigurationByServerIp() {
        // Получаем IP текущего сервера
        String serverIp = getCurrentServerIp();
        if (serverIp == null) {
            Highlighter.LOGGER.warn("[BackendLoader] Cannot determine current server IP");
            return CompletableFuture.completedFuture(false);
        }
        
        return loadConfigurationByServerIp(serverIp);
    }

    /**
     * Загружает конфигурацию по указанному IP сервера
     */
    public static CompletableFuture<Boolean> loadConfigurationByServerIp(String serverIp) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Highlighter.LOGGER.info("[BackendLoader] Loading configuration for server: {}", serverIp);
                
                // Получаем конфигурацию сервера
                ServerConfigurationResponse serverConfig = getServerConfiguration(serverIp);
                if (serverConfig == null) {
                    Highlighter.LOGGER.warn("[BackendLoader] No configuration found for server: {}", serverIp);
                    return false;
                }
                
                // Загружаем конфигурацию префиксов по слагу
                String slug = serverConfig.getDefaultConfigurationSlug();
                if (slug == null || slug.trim().isEmpty()) {
                    Highlighter.LOGGER.warn("[BackendLoader] No default configuration slug for server: {}", serverIp);
                    return false;
                }
                
                PrefixConfiguration prefixConfig = getConfigurationBySlug(slug);
                if (prefixConfig == null) {
                    Highlighter.LOGGER.error("[BackendLoader] Failed to load configuration with slug: {}", slug);
                    return false;
                }
                
                // Применяем конфигурацию в главном потоке
                MinecraftClient.getInstance().execute(() -> {
                    applyBackendConfiguration(serverConfig, prefixConfig, serverIp);
                });
                
                return true;
                
            } catch (Exception e) {
                Highlighter.LOGGER.error("[BackendLoader] Error loading configuration for server {}: {}", 
                                       serverIp, e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, NETWORK_EXECUTOR);
    }

    /**
     * Загружает только конфигурацию префиксов по указанному слагу
     * Используется при заходе в игру
     */
    public static CompletableFuture<Boolean> loadPrefixConfigurationBySlug(String slug) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Highlighter.LOGGER.info("[BackendLoader] Loading prefix configuration by slug: {}", slug);
                
                PrefixConfiguration prefixConfig = getConfigurationBySlug(slug);
                if (prefixConfig == null) {
                    Highlighter.LOGGER.error("[BackendLoader] Failed to load configuration with slug: {}", slug);
                    return false;
                }
                
                // Применяем только префиксы и игроков без серверных настроек
                MinecraftClient.getInstance().execute(() -> {
                    applyOnlyPrefixConfiguration(prefixConfig, slug);
                });
                
                return true;
                
            } catch (Exception e) {
                Highlighter.LOGGER.error("[BackendLoader] Error loading prefix configuration by slug {}: {}", 
                                       slug, e.getMessage());
                return false;
            }
        }, NETWORK_EXECUTOR);
    }

    /**
     * Загружает конфигурацию по указанному слагу (ручной режим)
     */
    public static CompletableFuture<Boolean> loadConfigurationBySlug(String slug) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Highlighter.LOGGER.info("[BackendLoader] Loading configuration by slug: {}", slug);
                
                PrefixConfiguration prefixConfig = getConfigurationBySlug(slug);
                if (prefixConfig == null) {
                    Highlighter.LOGGER.error("[BackendLoader] Failed to load configuration with slug: {}", slug);
                    return false;
                }
                
                // В ручном режиме применяем только префиксы и игроков, без серверных настроек
                MinecraftClient.getInstance().execute(() -> {
                    applyManualConfiguration(prefixConfig, slug);
                });
                
                return true;
                
            } catch (Exception e) {
                Highlighter.LOGGER.error("[BackendLoader] Error loading configuration by slug {}: {}", 
                                       slug, e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, NETWORK_EXECUTOR);
    }

    /**
     * Получает конфигурацию сервера по IP
     */
    private static ServerConfigurationResponse getServerConfiguration(String serverIp) throws IOException {
        String url = baseApiUrl + "/api/v1/server-configurations/mod/by-ip/" + serverIp;
        
        HttpURLConnection connection = null;
        try {
            connection = createConnection(url);
            
            int status = connection.getResponseCode();
            if (status != 200) {
                Highlighter.LOGGER.warn("[BackendLoader] Server configuration not found for IP: {} (HTTP {})", 
                                      serverIp, status);
                return null;
            }
            
            String response = readResponse(connection);
            return GSON.fromJson(response, ServerConfigurationResponse.class);
            
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Получает конфигурацию префиксов по слагу
     */
    private static PrefixConfiguration getConfigurationBySlug(String slug) throws IOException {
        String url = baseApiUrl + "/api/v1/configurations/find/raw/" + slug;
        
        HttpURLConnection connection = null;
        try {
            connection = createConnection(url);
            
            int status = connection.getResponseCode();
            if (status != 200) {
                Highlighter.LOGGER.warn("[BackendLoader] Configuration not found for slug: {} (HTTP {})", 
                                      slug, status);
                return null;
            }
            
            String response = readResponse(connection);
            ConfigurationResponse configResponse = GSON.fromJson(response, ConfigurationResponse.class);
            
            if (configResponse == null || configResponse.getContent() == null) {
                Highlighter.LOGGER.error("[BackendLoader] Invalid configuration response for slug: {}", slug);
                return null;
            }
            
            // Декодируем Base64 контент
            String decodedContent = new String(Base64.getDecoder().decode(configResponse.getContent()));
            
            Highlighter.LOGGER.debug("[BackendLoader] Decoded configuration content: {}", decodedContent);
            
            return GSON.fromJson(decodedContent, PrefixConfiguration.class);
            
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Создает HTTP соединение
     */
    private static HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "Highlighter-Mod/" + Highlighter.MOD_VERSION);
        connection.setRequestProperty("Origin", "http://localhost:3000"); // added Origin header

        return connection;
    }

    /**
     * Читает ответ HTTP соединения
     */
    private static String readResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    /**
     * Применяет конфигурацию из бекенда (автоматический режим)
     */
    private static void applyBackendConfiguration(ServerConfigurationResponse serverConfig, 
                                                PrefixConfiguration prefixConfig, 
                                                String serverIp) {
        
        // Очищаем предыдущие серверные данные если изменился сервер
        if (lastServerIp != null && !lastServerIp.equals(serverIp)) {
            clearServerData();
        }
        
        lastServerIp = serverIp;
        lastServerConfig = serverConfig;
        lastConfiguration = prefixConfig;
        lastConfigSlug = serverConfig.getDefaultConfigurationSlug();
        
        // Применяем префиксы
        applyPrefixes(prefixConfig.getPrefixes());
        
        // Применяем конфигурацию игроков
        if (prefixConfig.getPlayers() != null && !prefixConfig.getPlayers().isEmpty()) {
            applyPlayerConfiguration(prefixConfig.getPlayers());
        }
        
        // Применяем серверные настройки
        applyServerSettings(serverConfig);
        
        Highlighter.LOGGER.info("[BackendLoader] Successfully applied backend configuration for server: {}", 
                              serverIp);
    }

    /**
     * Применяет только конфигурацию префиксов без серверных настроек
     * Используется при заходе в игру
     */
    private static void applyOnlyPrefixConfiguration(PrefixConfiguration prefixConfig, String slug) {
        // Очищаем предыдущие серверные данные
        clearServerData();
        
        lastConfigSlug = slug;
        lastConfiguration = prefixConfig;
        
        // Применяем только префиксы
        applyPrefixes(prefixConfig.getPrefixes());
        
        // Применяем конфигурацию игроков
        if (prefixConfig.getPlayers() != null && !prefixConfig.getPlayers().isEmpty()) {
            applyPlayerConfiguration(prefixConfig.getPlayers());
        }
        
        Highlighter.LOGGER.info("[BackendLoader] Successfully applied prefix configuration with slug: {}", slug);
        
        // Show toast notification for prefix configuration loaded
        ToastNotification.showHighlighter("highlighter.notification.prefix_config_loaded", slug);
    }

    /**
     * Применяет конфигурацию в ручном режиме (только префиксы и игроки)
     */
    private static void applyManualConfiguration(PrefixConfiguration prefixConfig, String slug) {
        // Очищаем предыдущие серверные данные
        clearServerData();
        
        lastConfigSlug = slug;
        lastConfiguration = prefixConfig;
        lastServerIp = null; // Указываем что это ручной режим
        lastServerConfig = null;
        
        // Применяем префиксы
        applyPrefixes(prefixConfig.getPrefixes());
        
        // Применяем конфигурацию игроков
        if (prefixConfig.getPlayers() != null && !prefixConfig.getPlayers().isEmpty()) {
            applyPlayerConfiguration(prefixConfig.getPlayers());
        }
        
        Highlighter.LOGGER.info("[BackendLoader] Successfully applied manual configuration with slug: {}", slug);
        
        // Show toast notification for manual configuration loaded
        ToastNotification.showHighlighter("highlighter.notification.manual_config_loaded", slug);
    }

    /**
     * Применяет префиксы к системе
     */
    private static void applyPrefixes(List<Prefix> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) {
            Highlighter.LOGGER.warn("[BackendLoader] No prefixes to apply");
            return;
        }
        
        // Маркируем все префиксы как серверные
        for (Prefix prefix : prefixes) {
            prefix.setSource(PrefixSource.SERVER);
        }
        
        // Объединяем с существующими префиксами
        HighlighterClient.STORAGE_MANAGER.getPrefixStorage().mergeServerPrefixes(prefixes);
        
        Highlighter.LOGGER.info("[BackendLoader] Applied {} prefixes", prefixes.size());
    }

    /**
     * Применяет конфигурацию игроков
     */
    private static void applyPlayerConfiguration(List<HighlightedPlayer> players) {
        ServerEntry currentServer = HighlighterClient.getServerEntry();
        if (currentServer == null) {
            Highlighter.LOGGER.warn("[BackendLoader] Cannot apply player configuration - no current server");
            return;
        }
        
        int added = 0;
        int updated = 0;
        
        for (HighlightedPlayer player : players) {
            if (player.getUuid() == null) {
                continue;
            }
            
            // Проверяем существование префикса
            if (player.getPrefixId() != null && 
                HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefix(player.getPrefixId()) == null) {
                Highlighter.LOGGER.warn("[BackendLoader] Skipping player {} - prefix {} not found", 
                                      player.getUuid(), player.getPrefixId());
                continue;
            }
            
            HighlightedPlayer existingPlayer = currentServer.getHighlitedPlayer(player.getUuid());
            if (existingPlayer == null) {
                currentServer.addPlayer(player);
                added++;
            } else {
                // Обновляем если приоритет выше
                if (shouldUpdatePlayer(existingPlayer, player)) {
                    currentServer.removePlayer(existingPlayer.getUuid());
                    currentServer.addPlayer(player);
                    updated++;
                }
            }
        }
        
        currentServer.save();
        
        Highlighter.LOGGER.info("[BackendLoader] Applied player configuration: {} added, {} updated", 
                              added, updated);
    }

    /**
     * Применяет серверные настройки
     */
    private static void applyServerSettings(ServerConfigurationResponse serverConfig) {
        ServerEntry currentServer = HighlighterClient.getServerEntry();
        if (currentServer == null) {
            return;
        }
        
        // Применяем настройки из серверной конфигурации
        currentServer.setUseChatHighlighter(serverConfig.isChatEnabled());
        currentServer.setUseTabHighlighter(serverConfig.isTabEnabled());
        
        if (serverConfig.getChatRegexp() != null && !serverConfig.getChatRegexp().isEmpty()) {
            String[] regexArray = serverConfig.getChatRegexp().toArray(new String[0]);
            currentServer.setChatRegex(regexArray);
        }
        
        currentServer.save();
        
        Highlighter.LOGGER.info("[BackendLoader] Applied server settings - Chat: {}, Tab: {}, Regex count: {}", 
                              serverConfig.isChatEnabled(), 
                              serverConfig.isTabEnabled(),
                              serverConfig.getChatRegexp() != null ? serverConfig.getChatRegexp().size() : 0);
    }

    /**
     * Определяет нужно ли обновлять игрока
     */
    private static boolean shouldUpdatePlayer(HighlightedPlayer existing, HighlightedPlayer newPlayer) {
        if (existing.getPrefixId() == null) {
            return true;
        }
        
        if (newPlayer.getPrefixId() == null) {
            return false;
        }
        
        Prefix existingPrefix = HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefix(existing.getPrefixId());
        Prefix newPrefix = HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefix(newPlayer.getPrefixId());
        
        int existingPriority = existingPrefix != null ? existingPrefix.getPriority() : 0;
        int newPriority = newPrefix != null ? newPrefix.getPriority() : 0;
        
        return newPriority > existingPriority;
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
     * Очищает серверные данные
     */
    private static void clearServerData() {
        HighlighterClient.STORAGE_MANAGER.getPrefixStorage().removeAllServerPrefixes();
        Highlighter.LOGGER.debug("[BackendLoader] Cleared previous server data");
    }

    /**
     * Перезагружает последнюю конфигурацию
     */
    public static CompletableFuture<Boolean> reloadLastConfiguration() {
        if (lastServerIp != null) {
            // Перезагружаем в автоматическом режиме
            return loadConfigurationByServerIp(lastServerIp);
        } else if (lastConfigSlug != null) {
            // Перезагружаем в ручном режиме
            return loadConfigurationBySlug(lastConfigSlug);
        }
        
        Highlighter.LOGGER.warn("[BackendLoader] No previous configuration to reload");
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Очищает все загруженные конфигурации
     */
    public static void clearLoadedConfiguration() {
        clearServerData();
        lastServerIp = null;
        lastConfigSlug = null;
        lastConfiguration = null;
        lastServerConfig = null;
        Highlighter.LOGGER.info("[BackendLoader] Cleared all loaded configurations");
    }

    /**
     * Проверяет и обновляет конфигурацию сервера если необходимо
     * Интервал обновления по умолчанию: 1 час
     */
    public static CompletableFuture<Boolean> checkAndUpdateServerConfiguration(String serverIp) {
        return checkAndUpdateServerConfiguration(serverIp, 60 * 60 * 1000); // 1 час
    }

    /**
     * Проверяет и обновляет конфигурацию сервера если прошел указанный интервал
     */
    public static CompletableFuture<Boolean> checkAndUpdateServerConfiguration(String serverIp, long updateIntervalMs) {
        ServerEntry serverEntry = HighlighterClient.getServerEntry();
        if (serverEntry == null) {
            return CompletableFuture.completedFuture(false);
        }

        if (!serverEntry.needsConfigurationUpdate(updateIntervalMs)) {
            Highlighter.LOGGER.debug("[BackendLoader] Server configuration is up to date for: {}", serverIp);
            return CompletableFuture.completedFuture(true);
        }

        Highlighter.LOGGER.info("[BackendLoader] Configuration update needed for server: {}", serverIp);
        return updateServerConfiguration(serverIp);
    }

    /**
     * Возвращает информацию о последней загруженной конфигурации
     */
    public static String getLastConfigurationInfo() {
        if (lastConfiguration == null) {
            return "No configuration loaded";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Configuration: ").append(lastConfiguration.getConfigName() != null ? 
                                             lastConfiguration.getConfigName() : "Unnamed");
        
        if (lastServerIp != null) {
            info.append(" (Auto-loaded for ").append(lastServerIp).append(")");
        } else if (lastConfigSlug != null) {
            info.append(" (Manual slug: ").append(lastConfigSlug).append(")");
        }
        
        if (lastConfiguration.getAuthor() != null) {
            info.append(" by ").append(lastConfiguration.getAuthor());
        }
        
        if (lastConfiguration.getVersion() != null) {
            info.append(" v").append(lastConfiguration.getVersion());
        }
        
        return info.toString();
    }

    /**
     * Инициализирует обработчики событий
     */
    public static void init() {
        // При отключении от сервера очищаем загруженные конфигурации
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            clearLoadedConfiguration();
        });
        
        Highlighter.LOGGER.info("[BackendLoader] Initialized with base URL: {}", baseApiUrl);
    }

    /**
     * Custom UUID deserializer to handle both standard format (with dashes)
     * and compact format (without dashes) from API responses
     */
    private static class UuidDeserializer implements JsonDeserializer<UUID> {
        @Override
        public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String uuidString = json.getAsString();
            
            // If it's already in standard format, parse it directly
            if (uuidString.contains("-")) {
                return UUID.fromString(uuidString);
            }
            
            // If it's a compact format (32 characters), add dashes
            if (uuidString.length() == 32) {
                String formatted = uuidString.substring(0, 8) + "-" +
                                 uuidString.substring(8, 12) + "-" +
                                 uuidString.substring(12, 16) + "-" +
                                 uuidString.substring(16, 20) + "-" +
                                 uuidString.substring(20);
                return UUID.fromString(formatted);
            }
            
            throw new JsonParseException("Invalid UUID format: " + uuidString);
        }
    }
}