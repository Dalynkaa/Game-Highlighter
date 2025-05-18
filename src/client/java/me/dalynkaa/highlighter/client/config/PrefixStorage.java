package me.dalynkaa.highlighter.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import lombok.Builder;
import lombok.Getter;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import static me.dalynkaa.highlighter.Highlighter.MOD_VERSION;
import static me.dalynkaa.highlighter.client.config.StorageManager.mainConfigPath;

public class PrefixStorage {
    @Getter
    @Expose
    @SerializedName("prefixes")
    HashSet<Prefix> prefixes;
    
    @Expose
    @SerializedName("version")
    private String version;

    @Expose(serialize = false)
    private static String fileName = "prefixes.json";

    @Expose(serialize = false)
    private static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public PrefixStorage(HashSet<Prefix> prefixes, String version) {
        this.version = version;
        this.prefixes = prefixes;
    }

    public static PrefixStorage read() {
        File configFile = mainConfigPath.resolve(fileName).toFile();
        if (configFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
                try {
                    PrefixStorage co = gson.fromJson(reader, PrefixStorage.class);
                    return Objects.requireNonNullElseGet(co, () -> new PrefixStorage(new HashSet<>(), MOD_VERSION));
                } catch (JsonSyntaxException j) {
                    boolean isDeleted = configFile.delete();
                    if (isDeleted) {
                        Highlighter.LOGGER.info("[Configuration] Deleted Corrupted File!");
                    } else {
                        Highlighter.LOGGER.info("[Configuration] Failed to Delete Corrupted File!");
                    }
                }
            } catch (FileNotFoundException ignored) {
            }
        }
        PrefixStorage prefixStorage = new PrefixStorage(new HashSet<>(), MOD_VERSION);
        prefixStorage.save();
        return prefixStorage;
    }
    public void save() {
        Highlighter.LOGGER.info("[Configuration] Saving PrefixStorage...");
        File configFile = mainConfigPath.resolve(fileName).toFile();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)){
            String json = gson.toJson(this);
            Highlighter.LOGGER.info("Saving PrefixStorage: {}", json);
            writer.write(json);
            Highlighter.LOGGER.info("[Configuration] PrefixStorage успешно сохранен в {}", configFile.getAbsolutePath());
        } catch (Exception exception) {
            exception.printStackTrace();
            Highlighter.LOGGER.info("[Configuration] Failed to save PrefixStorage: {}", exception.getMessage());
        }
    }

    public void addPrefix(Prefix prefix) {
        if (prefixes == null){
            prefixes = new HashSet<>();
        }
        this.prefixes.add(prefix);
        save();
    }
    public void setPrefix(Prefix prefix) {
        if (!containsPrefix(prefix.getPrefixId())){
            addPrefix(prefix);
            return;
        }
        for (Prefix p: this.prefixes){
            if (p.getPrefixId().equals(prefix.getPrefixId())){
                this.prefixes.remove(p);
                this.prefixes.add(prefix);
                break;
            }
        }
        save();
    }
    public boolean containsPrefix(UUID prefix_id) {
        for (Prefix prefix: this.prefixes){
            if (prefix.getPrefixId().equals(prefix_id)){
                return true;
            }
        }
        return false;
    }
    public void removePrefix(UUID prefix_id) {
        for (Prefix prefix: this.prefixes){
            if (prefix.getPrefixId().equals(prefix_id)){
                this.prefixes.remove(prefix);
                break;
            }
        }
        save();
    }
    public Prefix getPrefix(UUID prefix_id) {
        for (Prefix prefix: this.prefixes){
            if (prefix.getPrefixId().equals(prefix_id)){
                return prefix;
            }
        }
        return null;
    }
}
