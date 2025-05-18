package me.dalynkaa.highlighter.client.config;

import lombok.Getter;
import me.dalynkaa.highlighter.Highlighter;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class StorageManager {
    public static Path mainConfigPath = FabricLoader.getInstance().getConfigDir().resolve(Highlighter.MOD_ID);
    private PrefixStorage prefixStorage;
    private ServerStorage serverStorage;
    private Boolean initialized = false;

    public StorageManager() {
    }

    public  void initialize() {
        Path saveDir = FabricLoader.getInstance().getConfigDir();
        Path mainConfigPath = saveDir.resolve(Highlighter.MOD_ID);
        if (!mainConfigPath.toFile().exists()) {
            boolean created = mainConfigPath.toFile().mkdirs();
            if (created) {
                Highlighter.LOGGER.info("Created CONFIG directory: {}", mainConfigPath);
            } else {
                Highlighter.LOGGER.error("Failed to create CONFIG directory: {}", mainConfigPath);
                throw new RuntimeException("Failed to create CONFIG directory: " + mainConfigPath);
            }
        }
        prefixStorage = PrefixStorage.read();
        if (prefixStorage == null) {
            throw new IllegalStateException("StorageManager has not been initialized");
        }
        serverStorage = new ServerStorage();
        serverStorage.initialize();
        initialized = true;
    }

    public Boolean isInitialized() {
        return initialized;
    }

    public PrefixStorage getPrefixStorage() {
        if (!initialized) {
            throw new IllegalStateException("StorageManager has not been initialized");
        }
        return prefixStorage;
    }
    public ServerStorage getServerStorage() {
        if (!initialized) {
            throw new IllegalStateException("StorageManager has not been initialized");
        }
        return serverStorage;
    }
}
