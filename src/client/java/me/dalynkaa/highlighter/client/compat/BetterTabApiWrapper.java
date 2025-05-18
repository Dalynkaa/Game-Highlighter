package me.dalynkaa.highlighter.client.compat;

import net.fabricmc.loader.api.FabricLoader;

public class BetterTabApiWrapper {
    public static  boolean isBetterTabAvailable() {
        return FabricLoader.getInstance().isModLoaded("better-tab");
    }
}
