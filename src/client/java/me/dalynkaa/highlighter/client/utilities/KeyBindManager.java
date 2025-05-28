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

    public static final KeyBinding TOGGLEHIDE_KEYBIND = new KeyBinding(
            "key.gamehighlighter.togglehide",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_H,
            "key.category.GameHighlighter"
    );

    public static final KeyBinding HIDE_KEYBIND = new KeyBinding(
            "key.gamehighlighter.hide",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.category.GameHighlighter"
    );

    public static final KeyBinding HIDETYPE_KEYBIND = new KeyBinding(
            "key.gamehighlighter.hidetype",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_Y,
            "key.category.GameHighlighter"
    );

    public static final KeyBinding TABKEY_KEYBIND = new KeyBinding(
            "key.gamehighlighter.tabkey",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_J,
            "key.category.GameHighlighter"
    );

    public static final KeyBinding OPEN_HIGHLIGHTS_KEYBIND = new KeyBinding(
            "key.gamehighlighter.open_highlights",
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
        ModConfig config = HighlighterClient.CONFIG;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLEHIDE_KEYBIND.wasPressed()) {
                if (config.player_hider){
                    config.player_hider = false;
                    assert client.player != null;
                    client.player.sendMessage(Text.translatable("messages.gamehighlighter.player_hider.disabled"),true);
                }else {
                    config.player_hider = true;
                    assert client.player != null;
                    client.player.sendMessage(Text.translatable("messages.gamehighlighter.player_hider.enabled"),true);
                }

            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (HIDETYPE_KEYBIND.wasPressed()) {
                if (config.playerHiderSettings.hideType == ModConfig.HideType.ALL){
                    config.playerHiderSettings.hideType = ModConfig.HideType.HIDDEN;
                    assert client.player != null;
                    client.player.sendMessage(Text.translatable("messages.gamehighlighter.player_hider.type").append(Text.translatable("messages.gamehighlighter.player_hider.type.minecraft")), true);
                }else if (config.playerHiderSettings.hideType == ModConfig.HideType.HIDDEN){
                    config.playerHiderSettings.hideType = ModConfig.HideType.RADIUS;
                    assert client.player != null;
                    client.player.sendMessage(Text.translatable("messages.gamehighlighter.player_hider.type").append(Text.translatable("messages.gamehighlighter.player_hider.type.radius")), true);
                }else if (config.playerHiderSettings.hideType == ModConfig.HideType.RADIUS){
                    config.playerHiderSettings.hideType = ModConfig.HideType.ALL;
                    assert client.player != null;
                    client.player.sendMessage(Text.translatable("messages.gamehighlighter.player_hider.type").append(Text.translatable("messages.gamehighlighter.player_hider.type.all")),true);
                }

            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (HIDE_KEYBIND.wasPressed()) {
                HitResult result = client.crosshairTarget;
                if (result instanceof EntityHitResult entityResult) {
                    Entity entity = entityResult.getEntity();
                    if (entity instanceof PlayerEntity player) {
                        MinecraftClient.getInstance().getSocialInteractionsManager().hidePlayer(player.getUuid());
                        MutableText mutableText = Text.literal("");
                        mutableText.append("Player ").styled(style -> style.withColor(TextColor.parse("#00b894").getOrThrow()));
                        mutableText.append(player.getDisplayName()).styled(style -> style.withColor(TextColor.parse("#55efc4").getOrThrow()));
                        mutableText.append(" hided").styled(style -> style.withColor(TextColor.parse("#00b894").getOrThrow()));
                        assert client.player != null;
                        client.player.sendMessage(mutableText,true);


                    }
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_HIGHLIGHTS_KEYBIND.isPressed()) {
                client.setScreen(new HighlightScreen());
            }
        });
    }
}
