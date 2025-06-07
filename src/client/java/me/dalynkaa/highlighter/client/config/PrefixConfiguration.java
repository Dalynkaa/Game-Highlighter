package me.dalynkaa.highlighter.client.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.dalynkaa.highlighter.client.utilities.data.HighlightedPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;

import java.util.ArrayList;
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


}
