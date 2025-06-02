package me.dalynkaa.highlighter;

import me.dalynkaa.highlighter.util.LogUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public class Highlighter implements ModInitializer {
	public static final String MOD_ID = "highlighter";
	public static final LogUtil LOGGER = new LogUtil(LoggerFactory.getLogger(MOD_ID));
	public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().getFriendlyString();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Highlighter version {}", MOD_VERSION);
	}
}
