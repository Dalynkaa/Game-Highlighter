package com.otsosity.otsolist.utils;

import com.google.gson.Gson;

import com.otsosity.otsolist.utils.DataClasses.ResultTab;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class NetworkUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger("otsolist");
    private static ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    private static HashMap<UUID, ResultTab> onlineOtso = new HashMap<>();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public static String getOtsoUsers(String arg){
        Gson g = new Gson();
        Map<Object, Object> data = new HashMap<>();
        data.put("uuid", arg);
        String res = postHTML(config.tab_settings.url+"/api/hasotso",data);
        return res;

    }
    public static void getOnlineOtsoInit(MinecraftClient client){
        ArrayList<String> playerList = new ArrayList<>();
        Gson g = new Gson();
        for (PlayerListEntry l: MinecraftClient.getInstance().player.networkHandler.getPlayerList()){
            playerList.add(l.getProfile().getId().toString());
        }
        String res = NetworkUtils.getOtsoUsers(playerList.toString());
        String[] splited = res.split(";");
        for (String sp:splited){
            String[] uuidandRT = sp.split("%%");
            onlineOtso.put(UUID.fromString(uuidandRT[0]),g.fromJson(uuidandRT[1], ResultTab.class));
        }
    }
    public static HashMap<UUID, ResultTab> getOnlineOtso(){
        return onlineOtso;
    }
    public static String postHTML(String urlToRead, Map<Object,Object> data){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(buildFormDataFromMap(data))
                    .uri(URI.create(urlToRead))
                    .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }catch (Exception e){
            return null;
        }
    }
    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

}
