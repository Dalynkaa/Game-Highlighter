package me.dalynkaa.highlighter.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.utilities.data.HighlightedPlayer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import static me.dalynkaa.highlighter.Highlighter.MOD_VERSION;

public class ServerEntry {
    @SerializedName("highlightedPlayers")
    @Expose
    HashSet<HighlightedPlayer> highlightedPlayers;

    @SerializedName("chatRegex")
    @Expose
    private String[] chatRegex;

    @SerializedName("useChatHighlighter")
    @Expose
    private boolean useChatHighlighter;

    @SerializedName("version")
    @Expose
    private String version;

    @Expose private String serverName;


    @Expose
    private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public ServerEntry(HashSet<HighlightedPlayer> highlightedPlayers, String[] chatRegex, Boolean useChatHighlighter, String version, String serverName) {
        this.version = version;
        this.highlightedPlayers = highlightedPlayers;
        this.chatRegex = chatRegex;
        this.useChatHighlighter = useChatHighlighter;
        this.serverName = serverName;
    }


    public static ServerEntry read(String serverName) {
        File configFile = ServerStorage.serverConfigPath.resolve(serverName+".json").toFile();
        if (configFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
                try {
                    ServerEntry co = gson.fromJson(reader, ServerEntry.class);
                    return Objects.requireNonNullElseGet(co, () -> new ServerEntry(new HashSet<>(),new String[0],true, MOD_VERSION, serverName));
                } catch (JsonSyntaxException j) {
                    boolean isDeleted = configFile.delete();
                    if (isDeleted) {
                        System.out.println("[Configuration] Deleted Corrupted File!");
                    } else {
                        System.out.println("[Configuration] Failed to Delete Corrupted File!");
                    }
                }
            } catch (FileNotFoundException ignored) {
            }
        }
        ServerEntry serverEntry = new ServerEntry(new HashSet<>(),new String[0],true, MOD_VERSION,serverName);
        serverEntry.save();
        return serverEntry;
    }

    public void save() {
        File configFile = ServerStorage.serverConfigPath.resolve(serverName+".json").toFile();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)){
            writer.write(gson.toJson(this));
            Highlighter.LOGGER.info("[Configuration] ServerEntry успешно сохранен в {}", configFile.getAbsolutePath());
            Highlighter.LOGGER.info("[Configuration] {}", gson.toJson(this));
        } catch (Exception ignored) {
        }
    }

    public boolean isHighlighted(UUID id){
        if (this.highlightedPlayers == null || this.highlightedPlayers.isEmpty()){
            return false;
        }
        for (HighlightedPlayer player: this.highlightedPlayers){
            if (player.getUuid().equals(id) && player.isHighlighted()){
                return true;
            }
        }
        return false;
    }

    public void addPlayer(HighlightedPlayer player) {
        if (highlightedPlayers == null){
            highlightedPlayers = new HashSet<>();
        }
        if (containsPlayer(player.getUuid())){
            return;
        }
        this.highlightedPlayers.add(player);
        save();
    }

    public void setPlayer(HighlightedPlayer player) {
        if (!containsPlayer(player.getUuid())){
            addPlayer(player);
            return;
        }

        for (HighlightedPlayer p: this.highlightedPlayers){
            if (p.getUuid().equals(player.getUuid())){
                this.highlightedPlayers.remove(p);
                break;
            }
        }
        this.highlightedPlayers.add(player);
        save();
    }

    public boolean containsPlayer(UUID uuid) {
        for (HighlightedPlayer player: this.highlightedPlayers){
            if (player.getUuid().equals(uuid)){
                return true;
            }
        }
        return false;
    }

    public void removePlayer(UUID uuid) {
        for (HighlightedPlayer player: this.highlightedPlayers){
            if (player.getUuid().equals(uuid)){
                this.highlightedPlayers.remove(player);
                break;
            }
        }

        save();
    }

    public HighlightedPlayer getHighlitedPlayer(UUID uuid) {
        for (HighlightedPlayer player: this.highlightedPlayers){
            if (player.getUuid().equals(uuid)){
                return player;
            }
        }
        return new HighlightedPlayer(uuid);
    }

    public HashSet<HighlightedPlayer> getAll() {
        return this.highlightedPlayers;
    }

    public HashSet<UUID> getAllHighlitedUUID() {
        HashSet<UUID> uuids = new HashSet<>();
        if (this.highlightedPlayers == null || this.highlightedPlayers.isEmpty()){
            return uuids;
        }
        for (HighlightedPlayer player: this.highlightedPlayers){
            if (player.isHighlighted()){
                uuids.add(player.getUuid());
            }
        }
        return uuids;
    }
}
