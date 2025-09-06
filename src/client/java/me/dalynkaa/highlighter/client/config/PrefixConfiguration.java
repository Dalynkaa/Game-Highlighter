package me.dalynkaa.highlighter.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.utilities.data.HighlightedPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Класс для представления полной конфигурации префиксов и игроков
 */
public class PrefixConfiguration {
    // Список префиксов
    @Expose
    @SerializedName("prefixes")
    private List<Prefix> prefixes = new ArrayList<>();

    // Список полных данных игроков
    @Expose
    @SerializedName("players")
    private List<HighlightedPlayer> players = new ArrayList<>();

    @Expose
    @SerializedName("serverSettings")
    private OnlineServerConfiguration serverSettings;

    // Название/описание конфигурации
    @Expose
    @SerializedName("configName")
    private String configName;

    // Автор конфигурации
    @Expose
    @SerializedName("author")
    private String author;

    // Версия конфигурации
    @Expose
    @SerializedName("version")
    private String version;

    public List<Prefix> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    public List<HighlightedPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<HighlightedPlayer> players) {
        this.players = players;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public OnlineServerConfiguration getServerSettings() {
        return serverSettings;
    }

    public void setServerSettings(OnlineServerConfiguration serverSettings) {
        this.serverSettings = serverSettings;
    }

    /**
     * Экспортирует конфигурацию в JSON формат
     *
     * @return JSON строка конфигурации
     */
    public String exportToJson() {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
        return gson.toJson(this);
    }

    /**
     * Экспортирует конфигурацию в Base64 формат
     *
     * @return Base64 закодированная строка конфигурации
     */
    public String exportToBase64() {
        String json = exportToJson();
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Генерирует URL для создания конфигурации на backend
     *
     * @param baseUrl базовый URL backend сервера
     * @return полный URL для создания конфигурации
     */
    public String generateBackendUrl(String baseUrl) {
        String base64Config = exportToBase64();
        return baseUrl + "highlighter/configurations/create?config=" + base64Config;
    }

    /**
     * Генерирует URL для создания конфигурации на backend с дефолтным URL
     *
     * @return полный URL для создания конфигурации
     */
    public String generateBackendUrl() {
        return generateBackendUrl(""); // можно заменить на дефолтный URL если нужно
    }

    /**
     * Генерирует URL для создания конфигурации на backend, используя URL из конфига
     *
     * @return полный URL для создания конфигурации
     */
    public String generateBackendUrlFromConfig() {
        String baseUrl = HighlighterClient.CONFIG.backendSettings.apiBaseUrl;
        return generateBackendUrl(baseUrl);
    }

    /**
     * Отправляет конфигурацию в чат игры в виде Base64 строки
     */
    public void sendToChat() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String base64Config = exportToBase64();
            String message = "Highlighter Configuration: " + base64Config;

            // Отправляем сообщение в чат
            client.player.networkHandler.sendChatMessage(message);
        }
    }

    /**
     * Отправляет URL конфигурации в чат игры
     */
    public void sendUrlToChat() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String configUrl = generateBackendUrlFromConfig();
            String message = "Highlighter Config URL: " + configUrl;

            // Отправляем сообщение в чат
            client.player.networkHandler.sendChatMessage(message);
        }
    }

    /**
     * Отправляет сообщение в чат игрока (локально, не в сетевой чат)
     *
     * @param message сообщение для отправки
     */
    public void sendLocalMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message), false);
        }
    }

    /**
     * Отправляет информацию о конфигурации в локальный чат игрока
     */
    public void sendConfigInfoToLocalChat() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String base64Config = exportToBase64();
            String configUrl = generateBackendUrlFromConfig();

            // Заголовок
            client.player.sendMessage(Text.literal("§6[Highlighter] Configuration exported:"), false);

            // Base64 с возможностью копирования
            Text base64Text = Text.literal("§7Base64: ")
                    .append(Text.literal("§f[Копировать]")
                            .styled(style -> style.withColor(Formatting.AQUA)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, base64Config))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.literal("§7Нажмите, чтобы скопировать Base64")))));
            client.player.sendMessage(base64Text, false);

            // URL с кликабельной ссылкой
            Text urlText = Text.literal("§7URL: ")
                    .append(Text.literal("§a[Открыть в браузере]")
                            .styled(style -> style.withColor(Formatting.GREEN)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, configUrl))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.literal("§7Нажмите, чтобы открыть в браузере\n§8" + configUrl)))));
            client.player.sendMessage(urlText, false);
        }
    }

    /**
     * Отправляет кликабельный URL конфигурации в чат игры
     */
    public void sendClickableUrlToChat() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String configUrl = generateBackendUrlFromConfig();

            // Создаем текстовый компонент с кликабельной ссылкой
            Text message = Text.literal("§6[Highlighter] Нажмите здесь, чтобы открыть конфигурацию")
                    .styled(style -> style.withColor(Formatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, configUrl))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§7Щелкните, чтобы открыть URL"))));

            // Отправляем сообщение в чат
            client.player.sendMessage(message, false);
        }
    }
}
