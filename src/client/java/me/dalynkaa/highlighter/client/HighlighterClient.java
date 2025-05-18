package me.dalynkaa.highlighter.client;

import me.dalynkaa.highlighter.client.config.ServerEntry;
import me.dalynkaa.highlighter.client.config.StorageManager;
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

		KeyBindManager.registerKeyBindings();
		KeyBindManager.initKeysListeners();
		new OnChatMessage();

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			cacheCurrentServer(client);
		});
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
