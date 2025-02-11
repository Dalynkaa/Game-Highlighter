package me.dalynkaa.gamehighlighter.client;

import me.dalynkaa.gamehighlighter.client.listeners.OnChatMessage;
import me.dalynkaa.gamehighlighter.client.utilities.HighlightConfig;
import lombok.Getter;
import me.dalynkaa.gamehighlighter.client.utilities.KeyBindManager;
import me.dalynkaa.gamehighlighter.client.utilities.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GameHighlighterClient implements ClientModInitializer {
	public static final String MOD_ID = "gamehighlighter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().getFriendlyString();
	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Getter
    public static HighlightConfig clientConfig;
	public static ModConfig config;

	@Override
	public void onInitializeClient() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
		clientConfig = HighlightConfig.read();
		KeyBindManager.registerKeyBindings();
		KeyBindManager.initKeysListeners();
		new OnChatMessage();
	}
	public static void updateConfig(){
		clientConfig.save();
		clientConfig = HighlightConfig.read();
	}
}
