package com.dalynkaa.utilities;

import com.dalynkaa.GameHighlighterClient;
import com.dalynkaa.gui.screens.HighlightListScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class KeyBindManager {
    public static final KeyBinding TOGGLEHIDE_KEYBIND = new KeyBinding(
            "key.game_highlighter.togglehide",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_H,
            "key.category.GameHighlighter"
    );
    public static final KeyBinding HIDE_KEYBIND = new KeyBinding(
            "key.game_highlighter.hide",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.category.GameHighlighter"
    );
    public static final KeyBinding HIDETYPE_KEYBIND = new KeyBinding(
            "key.game_highlighter.hidetype",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_T,
            "key.category.GameHighlighter"
    );
    public static final KeyBinding TABKEY_KEYBIND = new KeyBinding(
            "key.game_highlighter.tabkey",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_J,
            "key.category.GameHighlighter"
    );
    public static final KeyBinding OPEN_HIGHLIGHTS_KEYBIND = new KeyBinding(
            "key.game_highlighter.open_highlights",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_K,
            "key.category.GameHighlighter"
    );
    public static void registerKeyBindings() {
        registerKeyBinding(TOGGLEHIDE_KEYBIND);
        registerKeyBinding(HIDE_KEYBIND);
        registerKeyBinding(HIDETYPE_KEYBIND);
        registerKeyBinding(TABKEY_KEYBIND);
        registerKeyBinding(OPEN_HIGHLIGHTS_KEYBIND);
    }

    private static void registerKeyBinding(KeyBinding keyBinding) {
        KeyBindingHelper.registerKeyBinding(keyBinding);
    }
    public static void initKeysListeners() {
        ModConfig config = GameHighlighterClient.config;
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
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_HIGHLIGHTS_KEYBIND.isPressed()) {
                client.setScreen(new HighlightListScreen());
            }
        });
    }
}
