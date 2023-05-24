package fun.dalynkaa.gamehighlighter.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.struct.SourceMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;


public class HiglightConfig {
    HashSet<UUID> mutedClients;
    public HiglightConfig(HashSet<UUID> mutedClients){
        this.mutedClients = mutedClients;
    }



    public static HiglightConfig read() {
        Gson gson = new Gson();
        Path saveDir = FabricLoader.getInstance().getConfigDir();
        File configFile = saveDir.resolve("HighlightMod").resolve("data.json").toFile();
        if (configFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new FileReader(configFile));
                try {
                    HiglightConfig co = gson.fromJson(reader, HiglightConfig.class);
                    return Objects.requireNonNullElseGet(co, () -> new HiglightConfig(new HashSet<>()));
                } catch (JsonSyntaxException j) {
                    j.printStackTrace();
                    configFile.delete();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new HiglightConfig(new HashSet<>());
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
        try (FileWriter writer = new FileWriter(configFile)){
            writer.write(gson.toJson(this));
            System.out.println("[Configuration] Saved New File!");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public boolean isHighlighted(UUID id){
        return this.mutedClients.contains(id);
    }
    public void highlight(UUID uuid) {
        this.mutedClients.add(uuid);
        save();
    }
    public void unhighlight(UUID uuid) {
        this.mutedClients.remove(uuid);
        save();
    }
    public HashSet<UUID> getAll() {
        return this.mutedClients;
    }
}
