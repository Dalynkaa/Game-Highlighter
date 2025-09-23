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
import me.dalynkaa.highlighter.client.gui.ServerConfigConfirmationScreen;
import me.dalynkaa.highlighter.client.translations.DynamicTranslationManager;
import me.dalynkaa.highlighter.client.translations.LanguageChangeListener;
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

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (!HighlighterClient.isMultiplayerServer(client)) {
				resetConnectionState();
				Highlighter.LOGGER.debug("[HighlighterClient] Skipping backend configuration - not a multiplayer server");
				return;
			}
			
			cacheCurrentServer(client);
			
			String serverIp = client.getCurrentServerEntry() != null ?
							 client.getCurrentServerEntry().address : null;
			
			if (serverIp != null && !serverIp.equals(currentServerIp)) {
				currentServerIp = serverIp;
				hasUpdatedServerConfig = false;
				hasAppliedServerConfig = false;
				
				// Проверяем нужно ли показать диалог подтверждения
				ServerEntry serverEntry = HighlighterClient.getServerEntry();
				if (serverEntry != null && !serverEntry.isServerConfigDialogShown()) {
					ConfigurationManager.updateServerConfiguration()
						.thenAccept(success -> {
							if (success) {
								showServerConfigDialog(serverEntry, serverIp);
							} else {
								Highlighter.LOGGER.info("[HighlighterClient] No server configuration found, dialog will be shown when config appears");
							}
						});
				} else {
					updateServerConfiguration(serverIp);
				}
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			resetConnectionState();
		});

		BackendConfigurationLoader.init();
		
		// Инициализируем систему динамических переводов
		DynamicTranslationManager.initialize();
		LanguageChangeListener.startMonitoring();
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Пытаемся инициализировать переводы если еще не готовы
			if (!DynamicTranslationManager.isInitialized() && client.getLanguageManager() != null) {
				DynamicTranslationManager.initialize();
				LanguageChangeListener.startMonitoring();
			}
			
			// Проверяем изменение языка
			LanguageChangeListener.checkLanguageChange();
			
			if (client.world != null && client.player != null &&
				hasUpdatedServerConfig && !hasAppliedServerConfig &&
				HighlighterClient.isMultiplayerServer(client)) {
				
				hasAppliedServerConfig = true;
				
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
	
	/**
	 * Проверяет, подключен ли игрок к реальному мультиплеерному серверу
	 * (исключает одиночную игру и локальные серверы)
	 */
	public static boolean isMultiplayerServer(MinecraftClient client) {
		if (client.getServer() != null) {
			return false;
		}
		
		if (client.getCurrentServerEntry() == null) {
			return false;
		}
		
		if (client.getNetworkHandler() == null) {
			return false;
		}
		
		String address = client.getCurrentServerEntry().address;
		if (address != null) {
			String lowerAddress = address.toLowerCase();
			if (lowerAddress.startsWith("localhost") || 
				lowerAddress.startsWith("127.0.0.1") ||
				lowerAddress.startsWith("192.168.") ||
				lowerAddress.startsWith("10.") ||
				lowerAddress.startsWith("172.")) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Показывает диалог выбора использования серверной конфигурации
	 */
	private static void showServerConfigDialog(ServerEntry serverEntry, String serverIp) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null) return;
		
		String serverName = serverEntry.getServerName() != null ? serverEntry.getServerName() : serverIp;
		
		client.execute(() -> {
			ServerConfigConfirmationScreen dialog = new ServerConfigConfirmationScreen(
				client.currentScreen, 
				serverName,
				(choice) -> {
					serverEntry.setServerConfigDialogShown(true);
					
					if (choice == null) {
						Highlighter.LOGGER.info("[HighlighterClient] User chose to ask later for server config");
						serverEntry.setServerConfigDialogShown(false);
					} else if (choice) {
						Highlighter.LOGGER.info("[HighlighterClient] User agreed to use server configuration");
						serverEntry.setUseServerSettings(true);
						updateServerConfiguration(serverIp);
					} else {
						Highlighter.LOGGER.info("[HighlighterClient] User declined server configuration");
						serverEntry.setUseServerSettings(false);
						serverEntry.setConfigurationSlug(null);
						hasUpdatedServerConfig = true;
					}
					serverEntry.save();
				}
			);
			client.setScreen(dialog);
		});
	}
	
	/**
	 * Обновляет конфигурацию сервера (стандартная логика)
	 */
	private static void updateServerConfiguration(String serverIp) {
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
}
