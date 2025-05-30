package me.dalynkaa.highlighter.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@Config(name = "highlighter")
public class ModConfig implements ConfigData {
    boolean tab_enabled = true;

    public boolean player_hider = false;


    @ConfigEntry.Gui.CollapsibleObject
    public
    TabSettings tabSettings = new TabSettings();

    @ConfigEntry.Gui.CollapsibleObject
    public
    PlayerHider playerHiderSettings = new PlayerHider();

    public static class TabSettings {
        public boolean useExtendedTab = false;
    }

    public static class PlayerHider {

        public HideType hideType = HideType.ALL;

        @ConfigEntry.BoundedDiscrete(min = 0, max = 40)
        public Integer radius = 0;

    }

    public Boolean hasTabEnable(){
        return this.tab_enabled;
    }

    public Boolean hasPlayerHiderEnable(){
        return this.player_hider;
    }
    public enum HideType{
        RADIUS,
        ALL,
        HIDDEN

    }
}
