package com.dalynkaa.utilities;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
@Config(name = "assets/gamehighlighter")
public class ModConfig implements ConfigData {
    boolean tab_enabled = true;
    public boolean player_hider = false;


    @ConfigEntry.Gui.CollapsibleObject
    public
    TabSettings tab_settings = new TabSettings();
    @ConfigEntry.Gui.CollapsibleObject
    public
    PlayerHider playerHider_setttings = new PlayerHider();

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
        HIDEN

    }
}
