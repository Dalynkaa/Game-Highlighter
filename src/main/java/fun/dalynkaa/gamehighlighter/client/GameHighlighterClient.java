package fun.dalynkaa.gamehighlighter.client;

import fun.dalynkaa.gamehighlighter.utils.HiglightConfig;
import lombok.Getter;
import fun.dalynkaa.gamehighlighter.utils.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class GameHighlighterClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Getter
    public static HiglightConfig clientConfig;

    public static ModConfig config;
    private static final KeyBinding TOGGLEHIDE_KEYBIND = new KeyBinding(
            "key.game_highlighter.togglehide",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.category.GameHighlighter"
    );
    private static final KeyBinding HIDE_KEYBIND = new KeyBinding(
            "key.game_highlighter.hide",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.category.GameHighlighter"
    );
    private static final KeyBinding HIDETYPE_KEYBIND = new KeyBinding(
            "key.game_highlighter.hidetype",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.category.GameHighlighter"
    );
    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        clientConfig = HiglightConfig.read();

        System.out.println(config.tab_settings.prefix);
        System.out.println(config.tab_settings.hex_color_prefix);
        System.out.println(config.tab_settings.hex_color_display_name);

        KeyBindingHelper.registerKeyBinding(TOGGLEHIDE_KEYBIND);
        KeyBindingHelper.registerKeyBinding(HIDE_KEYBIND);
        KeyBindingHelper.registerKeyBinding(HIDETYPE_KEYBIND);


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLEHIDE_KEYBIND.wasPressed()) {
                if (config.player_hider){
                    config.player_hider = false;
                    client.player.sendMessage(Text.translatable("messages.game_highlighter.player_hider.disabled"),true);
                }else {
                    config.player_hider = true;
                    client.player.sendMessage(Text.translatable("messages.game_highlighter.player_hider.enabled"),true);
                }

            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (HIDETYPE_KEYBIND.wasPressed()) {
                if (config.playerHider_setttings.hideType == ModConfig.HideType.ALL){
                    config.playerHider_setttings.hideType = ModConfig.HideType.HIDEN;
                    client.player.sendMessage(Text.translatable("messages.game_highlighter.player_hider.type").append(Text.translatable("messages.game_highlighter.player_hider.type.minecraft")), true);
                }else if (config.playerHider_setttings.hideType == ModConfig.HideType.HIDEN){
                    config.playerHider_setttings.hideType = ModConfig.HideType.RADIUS;
                    client.player.sendMessage(Text.translatable("messages.game_highlighter.player_hider.type").append(Text.translatable("messages.game_highlighter.player_hider.type.radius")), true);
                }else if (config.playerHider_setttings.hideType == ModConfig.HideType.RADIUS){
                    config.playerHider_setttings.hideType = ModConfig.HideType.ALL;
                    client.player.sendMessage(Text.translatable("messages.game_highlighter.player_hider.type").append(Text.translatable("messages.game_highlighter.player_hider.type.all")),true);
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
                        MutableText mutableText = Text.literal("");
                        mutableText.append("Player ").styled(style -> style.withColor(TextColor.parse("#00b894")));
                        mutableText.append(player.getDisplayName()).styled(style -> style.withColor(TextColor.parse("#55efc4")));
                        mutableText.append(" hided").styled(style -> style.withColor(TextColor.parse("#00b894")));
                        client.player.sendMessage(mutableText,true);


                    }
                }
            }
        });

    }
}
