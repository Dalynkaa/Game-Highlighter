package me.dalynkaa.highlighter.client.config;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.HighlighterClient;
import net.minecraft.client.network.ServerInfo;

import java.nio.file.Path;

public class ServerStorage {
    public static final Path serverConfigPath = StorageManager.mainConfigPath.resolve("servers");

    public ServerStorage() {}

    public void initialize() {
        if (!serverConfigPath.toFile().exists()) {
            boolean created = serverConfigPath.toFile().mkdirs();
            if (created) {
                System.out.println("[Configuration] Created CONFIG directory: " + serverConfigPath);
            } else {
                System.out.println("[Configuration] Failed to create CONFIG directory: " + serverConfigPath);
                throw new RuntimeException("Failed to create CONFIG directory: " + serverConfigPath);
            }
        }
    }

    public ServerEntry getOrCreateServerEntry(ServerInfo serverInfo) {
        Highlighter.LOGGER.info("Loading server: {}", serverInfo.address);
        return ServerEntry.read(serverInfo.address);
    }
}
