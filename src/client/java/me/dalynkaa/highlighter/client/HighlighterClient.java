package me.dalynkaa.highlighter.client;

import io.wispforest.owo.Owo;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.config.BackendConfigurationLoader;
import me.dalynkaa.highlighter.client.config.ConfigurationManager;
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
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;


public class HighlighterClient implements ClientModInitializer {
	public static ModConfig CONFIG;
	public static StorageManager STORAGE_MANAGER;
	private static ServerEntry cachedServerEntry;
	
	// Флаги для отслеживания состояния подключения
	private static boolean hasUpdatedServerConfig = false;
	private static boolean hasAppliedServerConfig = false;
	private static String currentServerIp = null;

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

		// При подключении к серверу - обновляем конфигурацию сервера
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {

			
			cacheCurrentServer(client);
			
			// Получаем IP сервера и обновляем конфигурацию сервера
			String serverIp = client.getCurrentServerEntry() != null ? 
							 client.getCurrentServerEntry().address : null;
			
			if (serverIp != null && !serverIp.equals(currentServerIp)) {
				currentServerIp = serverIp;
				hasUpdatedServerConfig = false;
				hasAppliedServerConfig = false;
				
				// Этап 1: Обновляем конфигурацию сервера (получаем слаг)
				ConfigurationManager.updateServerConfiguration()
					.thenAccept(success -> {
						hasUpdatedServerConfig = true;
						if (success) {
							Highlighter.LOGGER.info("[HighlighterClient] Server configuration updated successfully");
						} else {
							Highlighter.LOGGER.debug("[HighlighterClient] No server configuration update needed");
						}
					})
					.exceptionally(throwable -> {
						hasUpdatedServerConfig = true;
						Highlighter.LOGGER.error("[HighlighterClient] Failed to update server configuration", throwable);
						return null;
					});
			}
		});

		// При отключении - сбрасываем состояние
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			resetConnectionState();
		});

		BackendConfigurationLoader.init();
		
		// Слушаем тики клиента для применения конфигурации после загрузки мира
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Применяем конфигурацию префиксов когда мир полностью загрузился
			if (client.world != null && client.player != null && 
				hasUpdatedServerConfig && !hasAppliedServerConfig) {
				
				hasAppliedServerConfig = true;
				
				// Этап 2: Применяем только конфигурацию префиксов по слагу
				ConfigurationManager.applyPrefixConfiguration()
					.thenAccept(success -> {
						if (success) {
							Highlighter.LOGGER.info("[HighlighterClient] Prefix configuration applied successfully");
						} else {
							Highlighter.LOGGER.debug("[HighlighterClient] No prefix configuration to apply");
						}
					})
					.exceptionally(throwable -> {
						Highlighter.LOGGER.error("[HighlighterClient] Failed to apply prefix configuration", throwable);
						return null;
					});
			}
		});
	}

	public static ServerEntry getServerEntry() {
		return cachedServerEntry;
	}

	private static void cacheCurrentServer(MinecraftClient client) {
        Highlighter.LOGGER.info("[HighlighterClient] caching server entry");
		if (client.getNetworkHandler() != null && client.getNetworkHandler().getServerInfo() != null) {
			ServerInfo serverInfo = client.getNetworkHandler().getServerInfo();
			cachedServerEntry = STORAGE_MANAGER.getServerStorage().getOrCreateServerEntry(serverInfo);
			Highlighter.LOGGER.debug("[HighlighterClient] Cached server: {}", serverInfo.address);
		}
	}
	
	private static void resetConnectionState() {
		cachedServerEntry = null;
		hasUpdatedServerConfig = false;
		hasAppliedServerConfig = false;
		currentServerIp = null;
		Highlighter.LOGGER.debug("[HighlighterClient] Connection state reset");
	}
}
