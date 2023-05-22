package com.otsosity.otsolist.listeners;

import com.otsosity.otsolist.client.OtsoListClient;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class PlayerDisconectToServer {
    MinecraftClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger("otsohelper");
    public List<String> whitelist = Arrays.asList("sp.spworlds.ru","127.0.0.1");
    public PlayerDisconectToServer(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient client){
        this.client = client;
        PlayerJoinToServer.taskRunning = true;
        PlayerJoinToServer.delayCounter = 0;

    }

}
