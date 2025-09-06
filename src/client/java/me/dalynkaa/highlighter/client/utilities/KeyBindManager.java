package me.dalynkaa.highlighter.client.utilities;

import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.config.ModConfig;
import me.dalynkaa.highlighter.client.gui.HighlightScreen;
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


    public static final KeyBinding TABKEY_KEYBIND = new KeyBinding(
            "key.highlighter.tabkey",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_J,
            "key.category.highlighter"
    );

    public static final KeyBinding OPEN_HIGHLIGHTS_KEYBIND = new KeyBinding(
            "key.highlighter.open_menu",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_K,
            "key.category.highlighter"
    );


    public static void registerKeyBindings() {

        registerKeyBinding(TABKEY_KEYBIND);
        registerKeyBinding(OPEN_HIGHLIGHTS_KEYBIND);
    }

    private static void registerKeyBinding(KeyBinding keyBinding) {
        KeyBindingHelper.registerKeyBinding(keyBinding);
    }

    public static void initKeysListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_HIGHLIGHTS_KEYBIND.isPressed()) {
                // Проверяем что мы на мультиплеерном сервере
                if (!HighlighterClient.isMultiplayerServer(client)) {
                    return;
                }
                client.setScreen(new HighlightScreen());
            }
        });
    }
}
