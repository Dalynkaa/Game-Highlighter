package me.dalynkaa.highlighter.client.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class OnlineServerConfiguration {
    @Expose
    @SerializedName("enabled")
    @Getter @Setter
    private Boolean enabled;

    @Expose
    @SerializedName("tabEnabled")
    @Getter @Setter
    private Boolean tabEnabled;

    @Expose
    @SerializedName("chatEnabled")
    @Getter @Setter
    private Boolean chatEnabled;

    @Expose
    private List<String> regexes;

    public OnlineServerConfiguration(boolean enabled, boolean tabEnabled, boolean chatEnabled, List<String> regexes) {
        this.enabled = enabled;
        this.tabEnabled = tabEnabled;
        this.chatEnabled = chatEnabled;
        this.regexes = regexes;
    }

    public List<String> getRegexes() {
        return regexes;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public Boolean isTabEnabled() {
        return tabEnabled;
    }

    public Boolean isChatEnabled() {
        return chatEnabled;
    }

}
