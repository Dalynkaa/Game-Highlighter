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
    public
    TabSettings tabSettings = new TabSettings();

    public static class TabSettings {
        public boolean useExtendedTab = false;
    }

    public Boolean hasTabEnable(){
        return this.tab_enabled;
    }

}
