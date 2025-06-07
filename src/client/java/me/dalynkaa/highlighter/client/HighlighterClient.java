package me.dalynkaa.highlighter.client;

import io.wispforest.owo.Owo;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.config.PastebinPrefixLoader;
import me.dalynkaa.highlighter.client.config.ServerEntry;
import me.dalynkaa.highlighter.client.config.StorageManager;
import me.dalynkaa.highlighter.client.config.migrations.MigrationManager;
import me.dalynkaa.highlighter.client.listeners.OnChatMessage;
import me.dalynkaa.highlighter.client.utilities.KeyBindManager;
import me.dalynkaa.highlighter.client.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;


public class HighlighterClient implements ClientModInitializer {
	public static ModConfig CONFIG;
	public static StorageManager STORAGE_MANAGER;
	private static ServerEntry cachedServerEntry;

	@Override
	public void onInitializeClient() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		STORAGE_MANAGER = new StorageManager();
		STORAGE_MANAGER.initialize();
		STORAGE_MANAGER.getPrefixStorage().removeAllServerPrefixes();

		MigrationManager migrationManager = new MigrationManager(STORAGE_MANAGER);
		migrationManager.runMigrations();

		KeyBindManager.registerKeyBindings();
		KeyBindManager.initKeysListeners();
		new OnChatMessage();

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (client.getServer() == null || client.getNetworkHandler() == null) {
				cachedServerEntry = null;
				return;
			}
			if (client.getServer().isDedicated()){
				cachedServerEntry = null;
				return;
			}
			cacheCurrentServer(client);
			PastebinPrefixLoader.loadConfigurationFromUrl("https://pastebin.com/raw/rWSdgqc8");
		});

		PastebinPrefixLoader.init();
	}

	public static ServerEntry getServerEntry() {
		return cachedServerEntry;
	}

	private static void cacheCurrentServer(MinecraftClient client) {
		if (client.getNetworkHandler() != null && client.getNetworkHandler().getServerInfo() != null) {
			ServerInfo serverInfo = client.getNetworkHandler().getServerInfo();
			cachedServerEntry = STORAGE_MANAGER.getServerStorage().getOrCreateServerEntry(serverInfo);
			System.out.println("Cached server: " + serverInfo);
		}
	}
}
