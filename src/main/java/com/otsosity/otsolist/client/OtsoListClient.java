package com.otsosity.otsolist.client;

import com.otsosity.otsolist.listeners.AnotherPlayerJoinToServer;
import com.otsosity.otsolist.listeners.PlayerDisconectToServer;
import com.otsosity.otsolist.listeners.PlayerJoinToServer;
import com.otsosity.otsolist.utils.HiglightConfig;
import lombok.Getter;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import com.otsosity.otsolist.utils.ModConfig;
import com.otsosity.otsolist.utils.NetworkUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.Resource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


public class OtsoListClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Getter
    public static HiglightConfig clientConfig;
    public static Boolean whitelisted_server = false;
    private static final Executor DOWNLOAD_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final KeyBinding UPDATE_KEYBIND = new KeyBinding(
            "key.otso.listupdate",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.category.OtsoList"
    );
    private static final KeyBinding TOGGLEHIDE_KEYBIND = new KeyBinding(
            "key.otso.togglehide",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.category.OtsoList"
    );
    private static final KeyBinding HIDE_KEYBIND = new KeyBinding(
            "key.otso.hide",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.category.OtsoList"
    );
    private static final KeyBinding HIDETYPE_KEYBIND = new KeyBinding(
            "key.otso.hidetype",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.category.OtsoList"
    );
    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        clientConfig = HiglightConfig.read();

        ClientPlayConnectionEvents.JOIN.register(PlayerJoinToServer::new);
        ClientPlayConnectionEvents.DISCONNECT.register(PlayerDisconectToServer::new);

        ClientEntityEvents.ENTITY_LOAD.register(AnotherPlayerJoinToServer::new);
        KeyBindingHelper.registerKeyBinding(UPDATE_KEYBIND);
        KeyBindingHelper.registerKeyBinding(TOGGLEHIDE_KEYBIND);
        KeyBindingHelper.registerKeyBinding(HIDE_KEYBIND);
        KeyBindingHelper.registerKeyBinding(HIDETYPE_KEYBIND);


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (UPDATE_KEYBIND.wasPressed()) {
                CompletableFuture.runAsync(() -> NetworkUtils.getOnlineOtsoInit(MinecraftClient.getInstance()), DOWNLOAD_EXECUTOR);
                client.player.sendMessage(Text.literal("updated"));
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLEHIDE_KEYBIND.wasPressed()) {
                if (config.player_hider){
                    config.player_hider = false;
                    client.player.sendMessage(Text.literal("Скрытие игроков выключено"));
                }else {
                    config.player_hider = true;
                    client.player.sendMessage(Text.literal("Скрытие игроков включено"));
                }

            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (HIDETYPE_KEYBIND.wasPressed()) {
                if (config.playerHider_setttings.hideType == ModConfig.HideType.ALL){
                    config.playerHider_setttings.hideType = ModConfig.HideType.HIDEN;
                    client.player.sendMessage(Text.literal("Режим скрытия Майнкрафт"));
                }else if (config.playerHider_setttings.hideType == ModConfig.HideType.HIDEN){
                    config.playerHider_setttings.hideType = ModConfig.HideType.RADIUS;
                    client.player.sendMessage(Text.literal("Режим скрытия Радиус"));
                }else if (config.playerHider_setttings.hideType == ModConfig.HideType.RADIUS){
                    config.playerHider_setttings.hideType = ModConfig.HideType.ALL;
                    client.player.sendMessage(Text.literal("Режим скрытия Все"));
                }

            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (HIDE_KEYBIND.wasPressed()) {
                HitResult result = client.crosshairTarget;
                if (result instanceof EntityHitResult) {
                    EntityHitResult entityResult = (EntityHitResult) result;
                    Entity entity = entityResult.getEntity();
                    if (entity instanceof PlayerEntity) {
                        PlayerEntity player = (PlayerEntity) entity;
                        MinecraftClient.getInstance().getSocialInteractionsManager().hidePlayer(player.getUuid());
                        MutableText mutableText = Text.literal("[OtsoHider] ").styled(style -> style.withColor(TextColor.parse("#6c5ce7")));
                        mutableText.append("Игрок ").styled(style -> style.withColor(TextColor.parse("#00b894")));
                        mutableText.append(player.getDisplayName()).styled(style -> style.withColor(TextColor.parse("#55efc4")));
                        mutableText.append(" скрыт").styled(style -> style.withColor(TextColor.parse("#00b894")));
                        client.player.sendMessage(mutableText);


                    }
                }
            }
        });

    }
}
