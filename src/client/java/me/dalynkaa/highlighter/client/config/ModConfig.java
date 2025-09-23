package me.dalynkaa.highlighter.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@Config(name = "highlighter")
public class ModConfig implements ConfigData {
    boolean tab_enabled = true;

    @ConfigEntry.Gui.CollapsibleObject
    public TabSettings tabSettings = new TabSettings();

    @ConfigEntry.Gui.CollapsibleObject
    public BackendSettings backendSettings = new BackendSettings();

    public static class TabSettings {
        public boolean useExtendedTab = false;
    }

    public static class BackendSettings {
        @ConfigEntry.Gui.Tooltip
        public String apiBaseUrl = "https://nexbit.dev/";
        
        @ConfigEntry.Gui.Tooltip
        public boolean autoLoadEnabled = true;
        
        @ConfigEntry.Gui.Tooltip
        public String manualSlug = "";
        
        @ConfigEntry.Gui.Tooltip
        public boolean preferManualSlug = false;
    }

    public Boolean hasTabEnable(){
        return this.tab_enabled;
    }

}
