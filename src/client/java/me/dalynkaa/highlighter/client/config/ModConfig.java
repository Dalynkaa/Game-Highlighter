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
    ChatSettings chatSettings = new ChatSettings();
    @ConfigEntry.Gui.CollapsibleObject
    public
    PlayerHider playerHiderSettings = new PlayerHider();
    public List<PlayerHider> playerHider = new ArrayList<PlayerHider>();

    public static class TabSettings {
        public boolean useExtendedTab = false;
    }
    public static class ChatSettings {
        @ConfigEntry.Gui.Tooltip()
        public String globalChatRegex = "\\[СП5\\] (?<nickname>[a-zA-Z0-9,\\.\\_]\\w*): (?<message>[а-яА-Яa-zA-Z0-9,\\.\\_].*)";
        @ConfigEntry.Gui.Tooltip()
        public String globalChatMessage = "<dark_blue><b><nickname></b></dark_blue> <black><b>-></b></black> <blue><message></blue>";
        @ConfigEntry.Gui.Tooltip()
        public String localChatRegex = "\\[СП5\\] (?<nickname>[a-zA-Z0-9,\\.\\_]\\w*): (?<message>[а-яА-Яa-zA-Z0-9,\\.\\_].*)";

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
