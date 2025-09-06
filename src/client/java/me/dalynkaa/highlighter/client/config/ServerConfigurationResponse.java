package me.dalynkaa.highlighter.client.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Модель ответа для API эндпоинта server-configurations/mod/by-ip
 */
@Getter
@Setter
public class ServerConfigurationResponse {
    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("serverIp")
    private String serverIp;

    @Expose
    @SerializedName("defaultConfigurationSlug")
    private String defaultConfigurationSlug;

    @Expose
    @SerializedName("chatRegexp")
    private List<String> chatRegexp;

    @Expose
    @SerializedName("chatEnabled")
    private boolean chatEnabled;

    @Expose
    @SerializedName("tabEnabled")
    private boolean tabEnabled;
}