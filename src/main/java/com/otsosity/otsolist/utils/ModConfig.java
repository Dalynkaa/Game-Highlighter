package com.otsosity.otsolist.utils;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
@Config(name = "otsolist")
public class ModConfig implements ConfigData {
    boolean tab_enabled = true;
    public boolean player_hider = false;


    @ConfigEntry.Gui.CollapsibleObject
    TabSettings tab_settings = new TabSettings();
    @ConfigEntry.Gui.CollapsibleObject
    public
    PlayerHider playerHider_setttings = new PlayerHider();

    static class TabSettings {
        String url = "http://132.145.21.132:5000";

    }
    public static class PlayerHider {

        public String url = "http://132.145.21.132:5000";
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
        HIDEN,
        ONLINE

    }
}
