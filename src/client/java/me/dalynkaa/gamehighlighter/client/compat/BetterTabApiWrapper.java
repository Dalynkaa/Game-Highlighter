package me.dalynkaa.gamehighlighter.client.compat;

import net.fabricmc.loader.api.FabricLoader;

public class BetterTabApiWrapper {
    public static  boolean isBetterTabAvailable() {
        return FabricLoader.getInstance().isModLoaded("better-tab");
    }
}
