package com.otsosity.otsolist.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.UUID;


public class HiglightConfig {
    HashSet<UUID> mutedClients;
    public HiglightConfig(HashSet<UUID> mutedClients){
        this.mutedClients = mutedClients;
    }


    public static HiglightConfig read() {
        Gson gson = new Gson();
        File dataFile = new File("config/HighlightMod/data.json");
        if (dataFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new FileReader(dataFile));
                try {
                    return gson.fromJson(reader, HiglightConfig.class);
                } catch (JsonSyntaxException j) {
                    j.printStackTrace();
                    dataFile.delete();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            new Thread(() -> {
                File configDir = new File("config/HighlightMod");
                configDir.mkdirs();
            });
        }

        return new HiglightConfig(new HashSet<>());
    }
    public void save() {
        Gson gson = new Gson();
        // async write
        new Thread(() -> {
            File configDir = new File("config/HighlightMod");
            configDir.mkdirs();

            try {
                try (Writer w = new FileWriter("config/HighlightMod/data.json")) {
                    w.write(gson.toJson(this));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
}
