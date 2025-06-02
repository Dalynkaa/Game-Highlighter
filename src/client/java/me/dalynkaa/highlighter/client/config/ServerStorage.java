package me.dalynkaa.highlighter.client.config;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.HighlighterClient;
import net.minecraft.client.network.ServerInfo;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
    public ServerEntry getServerEntry(String serverName) {
        return ServerEntry.read(serverName);
    }

    public List<ServerEntry> getAllServers() {
        return ServerStorage.getAllServerEntries();
    }
    public void saveServerEntry(ServerEntry entry) {
        if (entry == null || entry.getServerName() == null) {
            Highlighter.LOGGER.error("[Configuration] Cannot save null or unnamed server entry.");
            return;
        }
        try {
            entry.save();
        } catch (Exception e) {
            Highlighter.LOGGER.error("[Configuration] Ошибка при сохранении записи сервера: {}", entry.getServerName(), e);
        }
    }



    public static List<ServerEntry> getAllServerEntries() {
        List<ServerEntry> entries = new ArrayList<>();
        File serverDir = serverConfigPath.toFile();

        if (serverDir.exists() && serverDir.isDirectory()) {
            File[] files = serverDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    try {
                        String serverAddress = file.getName().replace(".json", "");
                        ServerEntry entry = ServerEntry.read(serverAddress);
                        if (entry != null) {
                            entries.add(entry);
                        }
                    } catch (Exception e) {
                        Highlighter.LOGGER.error("[Configuration] Ошибка при чтении записи сервера: {}", file.getName(), e);
                    }
                }
            }
        }

        return entries;
    }
}
