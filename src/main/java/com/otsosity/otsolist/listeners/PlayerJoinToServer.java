package com.otsosity.otsolist.listeners;

import com.otsosity.otsolist.client.OtsoListClient;
import com.otsosity.otsolist.utils.NetworkUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlayerJoinToServer {
    MinecraftClient Mclient;
    public static boolean taskRunning = false;
    private int tickCounter = 0;
    private final int TICKS_PER_MINUTE = 20 * 30;
    private final int FIRST_DELAY = 20 * 5;
    public static int delayCounter = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger("otsohelper");
    public List<String> whitelist = Arrays.asList("sp.spworlds.ru","127.0.0.1");
    private static final Executor DOWNLOAD_EXECUTOR = Executors.newSingleThreadExecutor();
    public PlayerJoinToServer(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient Mclient){
        this.Mclient = Mclient;
        OtsoListClient.whitelisted_server = whitelist.contains(handler.getServerInfo().address);
        ClientTickEvents.START_WORLD_TICK.register(client -> {
            if (!taskRunning) {
                tickCounter++;
                delayCounter++;
                if (delayCounter >= FIRST_DELAY) {
                    tickCounter = 0;
                    CompletableFuture.runAsync(()-> NetworkUtils.getOnlineOtsoInit(MinecraftClient.getInstance()),DOWNLOAD_EXECUTOR);
                }
                if (tickCounter >= TICKS_PER_MINUTE) {
                    tickCounter = 0;
                    CompletableFuture.runAsync(()-> NetworkUtils.getOnlineOtsoInit(MinecraftClient.getInstance()),DOWNLOAD_EXECUTOR);
                }
            }
        });
    }

}
