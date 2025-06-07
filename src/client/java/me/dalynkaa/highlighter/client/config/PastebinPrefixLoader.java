package me.dalynkaa.highlighter.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.utilities.data.HighlightedPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import me.dalynkaa.highlighter.client.utilities.data.PrefixSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Класс для загрузки префиксов и конфигураций игроков из Pastebin-подобного сервиса
 */
public class PastebinPrefixLoader {
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();
    private static final Executor NETWORK_EXECUTOR = Executors.newSingleThreadExecutor();

    // Последний использованный URL конфигурации
    private static String lastConfigUrl = null;

    // Последняя загруженная конфигурация
    private static PrefixConfiguration lastConfiguration = null;

    /**
     * Загружает конфигурацию префиксов из указанного URL
     *
     * @param url URL с JSON конфигурацией префиксов и игроков
     * @return CompletableFuture с результатом загрузки
     */
    public static CompletableFuture<Boolean> loadConfigurationFromUrl(String url) {
        return CompletableFuture.supplyAsync(() -> {
            Highlighter.LOGGER.info("[PastebinLoader] Loading configuration from URL: {}", url);

            HttpURLConnection connection = null;
            try {
                // Создаем соединение
                URL configUrl = new URL(url);
                connection = (HttpURLConnection) configUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                // Проверяем статус ответа
                int status = connection.getResponseCode();
                if (status != 200) {
                    Highlighter.LOGGER.error("[PastebinLoader] HTTP error: {}", status);
                    return false;
                }

                // Читаем содержимое ответа
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Парсим JSON
                String jsonContent = response.toString();

                Highlighter.LOGGER.info("[PastebinLoader] Raw JSON content: {}", jsonContent);

                // Пробуем парсить как полную конфигурацию
                try {
                    PrefixConfiguration config = GSON.fromJson(jsonContent, PrefixConfiguration.class);
                    if (config != null && config.getPrefixes() != null && !config.getPrefixes().isEmpty()) {
                        // Применяем загруженную конфигурацию в главном потоке
                        CompletableFuture<Void> processResult = CompletableFuture.runAsync(() -> {
                            applyLoadedConfiguration(config, url);
                        }, runnable -> MinecraftClient.getInstance().execute(runnable));

                        processResult.join(); // Ждем завершения применения
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    Highlighter.LOGGER.warn("[PastebinLoader] Failed to parse as full configuration, trying as prefixes list");
                    return false;
                }
            } catch (IOException e) {
                Highlighter.LOGGER.error("[PastebinLoader] Error loading configuration: {}", e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }, NETWORK_EXECUTOR);
    }

    /**
     * Применяет загруженную полную конфигурацию (префиксы и игроки)
     */
    private static void applyLoadedConfiguration(PrefixConfiguration config, String sourceUrl) {
        // Если URL изменился, удаляем все предыдущие серверные префиксы
        if (lastConfigUrl != null && !lastConfigUrl.equals(sourceUrl)) {
            HighlighterClient.STORAGE_MANAGER.getPrefixStorage().removeAllServerPrefixes();
        }

        lastConfigUrl = sourceUrl;
        lastConfiguration = config;

        // Маркируем все префиксы как серверные
        List<Prefix> prefixes = config.getPrefixes();
        if (prefixes != null && !prefixes.isEmpty()) {
            for (Prefix prefix : prefixes) {
                prefix.setSource(PrefixSource.SERVER);
            }

            // Объединяем с существующими префиксами
            HighlighterClient.STORAGE_MANAGER.getPrefixStorage().mergeServerPrefixes(prefixes);

            Highlighter.LOGGER.info("[PastebinLoader] Successfully loaded {} prefixes from configuration", prefixes.size());
        }

        // Обрабатываем данные игроков
        if (config.getPlayers() != null && !config.getPlayers().isEmpty()) {
            applyPlayerConfiguration(config.getPlayers());
        }

        // Логируем информацию о загруженной конфигурации
        Highlighter.LOGGER.info("[PastebinLoader] Loaded configuration: {}",
                                config.getConfigName() != null ? config.getConfigName() : "Unnamed");
        if (config.getAuthor() != null) {
            Highlighter.LOGGER.info("[PastebinLoader] Configuration author: {}", config.getAuthor());
        }
        if (config.getVersion() != null) {
            Highlighter.LOGGER.info("[PastebinLoader] Configuration version: {}", config.getVersion());
        }
    }

    /**
     * Применяет полную конфигурацию игроков
     */
    private static void applyPlayerConfiguration(List<HighlightedPlayer> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        // Получаем текущий сервер
        ServerEntry currentServer = HighlighterClient.getServerEntry();
        if (currentServer == null) {
            Highlighter.LOGGER.warn("[PastebinLoader] Cannot apply player configuration - no current server");
            return;
        }

        // Подсчитываем добавленных и обновленных игроков
        int added = 0;
        int updated = 0;

        for (HighlightedPlayer player : players) {
            if (player.getUuid() == null) {
                continue;
            }

            // Проверяем, существует ли префикс
            if (player.getPrefixId() != null &&
                HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefix(player.getPrefixId()) == null) {
                Highlighter.LOGGER.warn("[PastebinLoader] Skipping player {} - prefix {} not found",
                                       player.getUuid(), player.getPrefixId());
                continue;
            }

            HighlightedPlayer existingPlayer = currentServer.getHighlitedPlayer(player.getUuid());
            if (existingPlayer == null) {
                currentServer.addPlayer(player);
                added++;
            } else {
                if (existingPlayer.getPrefixId() == null ||
                    (player.getPrefixId() != null &&
                     getPlayerPrefixPriority(existingPlayer) <= getPlayerPrefixPriority(player))) {
                    currentServer.removePlayer(existingPlayer.getUuid());
                    currentServer.addPlayer(player);
                    updated++;
                }
            }
        }

        // Сохраняем изменения
        currentServer.save();

        Highlighter.LOGGER.info("[PastebinLoader] Applied player configuration: {} added, {} updated",
                               added, updated);
    }

    /**
     * Получает приоритет префикса игрока
     */
    private static int getPlayerPrefixPriority(HighlightedPlayer player) {
        if (player.getPrefixId() == null) {
            return -1;
        }

        Prefix prefix = HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefix(player.getPrefixId());
        return prefix != null ? prefix.getPriority() : 0;
    }

    /**
     * Экспортирует текущую конфигурацию в JSON формат
     */
    public static String exportCurrentConfiguration(String configName, String author, String serverName) {
        PrefixConfiguration config = new PrefixConfiguration();

        // Устанавливаем метаданные
        config.setConfigName(configName);
        config.setAuthor(author);
        config.setVersion(Highlighter.MOD_VERSION);

        // Добавляем все локальные префиксы
        config.setPrefixes(HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getLocalPrefixes());

        // Добавляем игроков из текущего сервера
        ServerEntry currentServer = HighlighterClient.STORAGE_MANAGER.getServerStorage().getServerEntry(serverName);
        if (currentServer != null) {
            config.setPlayers(new ArrayList<>(currentServer.getAll()));
            config.setServerSettings(new OnlineServerConfiguration(currentServer.isEnabled(),
                                                                                                                    currentServer.isUseTabHighlighter(),
                                                                                                                    currentServer.isUseChatHighlighter(),
                                                                                                                    Arrays.asList(currentServer.getChatRegex())
                    )
            );
        }

        // Преобразуем в JSON
        return GSON.toJson(config);
    }

    /**
     * Перезагружает префиксы из последнего использованного URL
     */
    public static CompletableFuture<Boolean> reloadLastConfig() {
        if (lastConfigUrl == null) {
            Highlighter.LOGGER.warn("[PastebinLoader] No previous URL to reload");
            return CompletableFuture.completedFuture(false);
        }

        return loadConfigurationFromUrl(lastConfigUrl);
    }

    /**
     * Очищает все загруженные префиксы
     */
    public static void clearLoadedPrefixes() {
        HighlighterClient.STORAGE_MANAGER.getPrefixStorage().removeAllServerPrefixes();
        lastConfigUrl = null;
        lastConfiguration = null;
        Highlighter.LOGGER.info("[PastebinLoader] Cleared all loaded prefixes");
    }

    /**
     * Инициализирует обработчики событий
     */
    public static void init() {
        // При отключении от сервера очищаем загруженные префиксы
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            clearLoadedPrefixes();
        });
    }
}



