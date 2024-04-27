package com.dalynkaa;

import com.dalynkaa.utilities.HiglightConfig;
import com.dalynkaa.utilities.KeyBindManager;
import com.dalynkaa.utilities.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;


public class GameHighlighterClient implements ClientModInitializer {
	/**
	 * Runs the mod initializer on the client environment.
	 */
	public static HiglightConfig clientConfig;

	public static ModConfig config;

	@Override
	public void onInitializeClient() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
		clientConfig = HiglightConfig.read();
		KeyBindManager.registerKeyBindings();
		KeyBindManager.initKeysListeners();
	}
	public static HiglightConfig getClientConfig() {
		return clientConfig;
	}
}
