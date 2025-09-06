package me.dalynkaa.highlighter.client.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * Модель ответа для API эндпоинта configurations/find/raw
 */
@Getter
@Setter
public class ConfigurationResponse {
    @Expose
    @SerializedName("content")
    private String content; // Base64 encoded JSON
}