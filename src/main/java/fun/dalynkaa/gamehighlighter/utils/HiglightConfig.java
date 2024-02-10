package fun.dalynkaa.gamehighlighter.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fun.dalynkaa.gamehighlighter.utils.data.HighlitedPlayer;
import fun.dalynkaa.gamehighlighter.utils.data.Prefix;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.struct.SourceMap;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;


public class HiglightConfig {
    @SerializedName("version")
    private String version;
    @SerializedName("playersHighlighted")
    HashSet<HighlitedPlayer> highlitedPlayers;
    @SerializedName("prefixes")
    HashSet<Prefix> prefixes;
    HashSet<UUID> mutedClients;
    public HiglightConfig(String version,HashSet<HighlitedPlayer> highlitedPlayers, HashSet<Prefix> prefixes, HashSet<UUID> mutedClients){
        this.version = version;
        this.highlitedPlayers = highlitedPlayers;
        this.prefixes = prefixes;
        this.mutedClients = mutedClients;

    }


    private void migration() {
        if (version==null){
            this.version = "1.4";
            if (mutedClients!=null && !mutedClients.isEmpty()){
                for (UUID uuid: mutedClients){
                    addPlayer(new HighlitedPlayer(uuid, UUID.fromString("b4c9349b-2bf1-4af6-b8b7-a585797d56a4"), false, true));
                }
            }
            if (prefixes == null || prefixes.isEmpty()){
                addPrefix(new Prefix(UUID.fromString("b4c9349b-2bf1-4af6-b8b7-a585797d56a4"), "Example prefix", "* ", "#e84393", "#fd79a8"));
            }
        }
        if (Objects.equals(version, "1.4")){
            this.version = "1.5";
            if (getPrefix(UUID.fromString("b4c9349b-2bf1-4af6-b8b7-a585797d56a4"))!=null){
                setPrefix(new Prefix(UUID.fromString("b4c9349b-2bf1-4af6-b8b7-a585797d56a4"), "Example prefix", "* ", "#e84393", "#fd79a8"));
            }
        }
    }
    public static HiglightConfig read() {
        Gson gson = new Gson();
        Path saveDir = FabricLoader.getInstance().getConfigDir();
        File configFile = saveDir.resolve("HighlightMod").resolve("data.json").toFile();
        if (configFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
                try {
                    HiglightConfig co = gson.fromJson(reader, HiglightConfig.class);
                    co.migration();
                    return Objects.requireNonNullElseGet(co, () -> new HiglightConfig("1.4",new HashSet<>(), new HashSet<>(), new HashSet<>()));
                } catch (JsonSyntaxException j) {
                    j.printStackTrace();
                    configFile.delete();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new HiglightConfig("1.4",new HashSet<>(), new HashSet<>(), new HashSet<>());
    }
    public void save() {
        Gson gson = new Gson();
        Path saveDir = FabricLoader.getInstance().getConfigDir();
        System.out.println("SAVE DIR: " + saveDir.toString());
        System.out.println("SAVE DIR: " + saveDir.resolve("HighlightMod").toString());
        File configFile = saveDir.resolve("HighlightMod").resolve("data.json").toFile();
        if (!Files.exists(saveDir.resolve("HighlightMod"))){
            try {
                Files.createDirectory(saveDir.resolve("HighlightMod"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)){
            writer.write(gson.toJson(this));
            System.out.println("[Configuration] Saved New File!");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getVersion() {
        return version;
    }

    public boolean isHighlighted(UUID id){
        if (this.highlitedPlayers == null || this.highlitedPlayers.isEmpty()){
            return false;
        }
        for (HighlitedPlayer player: this.highlitedPlayers){
            if (player.getUuid().equals(id) && player.isHighlighted()){
                return true;
            }
        }
        return false;
    }
    public void addPlayer(HighlitedPlayer player) {
        if (highlitedPlayers == null){
            highlitedPlayers = new HashSet<>();
        }
        this.highlitedPlayers.add(player);
        save();
    }
    public void setPlayer(HighlitedPlayer player) {
        if (!containsPlayer(player.getUuid())){
            addPlayer(player);
            return;
        }
        for (HighlitedPlayer p: this.highlitedPlayers){
            if (p.getUuid().equals(player.getUuid())){
                this.highlitedPlayers.remove(p);
                this.highlitedPlayers.add(player);
                break;
            }
        }
        save();
    }
    public boolean containsPlayer(UUID uuid) {
        for (HighlitedPlayer player: this.highlitedPlayers){
            if (player.getUuid().equals(uuid)){
                return true;
            }
        }
        return false;
    }
    public void removePlayer(UUID uuid) {
        for (HighlitedPlayer player: this.highlitedPlayers){
            if (player.getUuid().equals(uuid)){
                this.highlitedPlayers.remove(player);
                break;
            }
        }

        save();
    }
    public HighlitedPlayer getHighlitedPlayer(UUID uuid) {
        for (HighlitedPlayer player: this.highlitedPlayers){
            if (player.getUuid().equals(uuid) && player.isHighlighted()){
                return player;
            }
        }
        return null;
    }
    public HashSet<HighlitedPlayer> getAll() {
        return this.highlitedPlayers;
    }
    public HashSet<UUID> getAllHighlitedUUID() {
        HashSet<UUID> uuids = new HashSet<>();
        for (HighlitedPlayer player: this.highlitedPlayers){
            if (player.isHighlighted()){
                uuids.add(player.getUuid());
            }
        }
        return uuids;
    }
    public HashSet<Prefix> getAllPrefixes() {
        return this.prefixes;
    }
    public void addPrefix(Prefix prefix) {
        if (prefixes == null){
            prefixes = new HashSet<>();
        }
        this.prefixes.add(prefix);
        save();
    }
    public void setPrefix(Prefix prefix) {
        if (!containsPrefix(prefix.getPrefix_id())){
            addPrefix(prefix);
            return;
        }
        for (Prefix p: this.prefixes){
            if (p.getPrefix_id().equals(prefix.getPrefix_id())){
                this.prefixes.remove(p);
                this.prefixes.add(prefix);
                break;
            }
        }
        save();
    }
    public boolean containsPrefix(UUID prefix_id) {
        for (Prefix prefix: this.prefixes){
            if (prefix.getPrefix_id().equals(prefix_id)){
                return true;
            }
        }
        return false;
    }
    public void removePrefix(UUID prefix_id) {
        for (Prefix prefix: this.prefixes){
            if (prefix.getPrefix_id().equals(prefix_id)){
                this.prefixes.remove(prefix);
                break;
            }
        }
        save();
    }
    public Prefix getPrefix(UUID prefix_id) {
        for (Prefix prefix: this.prefixes){
            if (prefix.getPrefix_id().equals(prefix_id)){
                return prefix;
            }
        }
        return null;
    }

}
