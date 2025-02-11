package me.dalynkaa.gamehighlighter.client.utilities.data;


import lombok.Getter;
import lombok.Setter;
import me.dalynkaa.gamehighlighter.client.GameHighlighterClient;
import me.dalynkaa.gamehighlighter.client.utilities.HighlightConfig;

public class ServerConfig {
    @Getter @Setter
    private String serverName;
    @Getter @Setter
    private String serverIp;
    @Getter @Setter
    private String globalChatRegex;
    @Getter @Setter
    private String localChatRegex;

    public ServerConfig(String serverName, String serverIp, String globalChatRegex, String localChatRegex) {
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.globalChatRegex = globalChatRegex;
        this.localChatRegex = localChatRegex;
    }
    public static ServerConfig getOrCreateServerConfig(String serverName,String serverIp) {
        HighlightConfig config = GameHighlighterClient.getClientConfig();
        ServerConfig serverConfig = config.getServerConfig(serverIp);
        if (serverConfig == null) {
            serverConfig = new ServerConfig(serverName, serverIp, ".*", ".*");
            config.addServerToConfig(serverConfig);
            config.save();
        }
        return serverConfig;
    }


}
