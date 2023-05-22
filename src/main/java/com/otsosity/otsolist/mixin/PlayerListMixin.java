package com.otsosity.otsolist.mixin;


import com.otsosity.otsolist.client.OtsoListClient;
import com.otsosity.otsolist.utils.DataClasses.ResultTab;
import com.otsosity.otsolist.utils.ModConfig;
import com.otsosity.otsolist.utils.PlayerListAccersor;
import com.otsosity.otsolist.utils.NetworkUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(PlayerListHud.class)
public class PlayerListMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("otsohelper");
    private static final Comparator<PlayerListEntry> ENTRY_ORDERING = Comparator.comparingInt(entry -> NetworkUtils.getOnlineOtso().containsKey(entry.getProfile().getId())? 0 : 1);

    @Shadow
    @Final
    private MinecraftClient client;
    ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    @Inject(method = "getPlayerName",at = @At("RETURN"),cancellable = true)
    public void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir){
        Text displayname = cir.getReturnValue();
        ResultTab s = NetworkUtils.getOnlineOtso().get(entry.getProfile().getId());
        if (s != null){
            if (s.getStatus() && OtsoListClient.whitelisted_server && config.hasTabEnable()){
                MutableText mutableText = Text.literal("");
                mutableText.append(StringEscapeUtils.unescapeJava(s.getData().getPrefix())).styled(style -> style.withColor(TextColor.parse(s.getData().getPrefixColor())));
                mutableText.append(displayname.copy().styled(style -> style.withColor(TextColor.parse(s.getData().getDisplayColor()))));
                cir.setReturnValue(mutableText);
            }
        }
    }
    @Inject(method = "collectPlayerEntries", at = @At("RETURN"), cancellable = true)
    public void collect(CallbackInfoReturnable<List<PlayerListEntry>> cir){
        List<PlayerListEntry> entries = this.client.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).toList();
        ArrayList<UUID> need = new ArrayList<>(NetworkUtils.getOnlineOtso().keySet());
        List<PlayerListEntry> entries1 = new ArrayList<>();
        Comparator<PlayerListEntry> customComparator = Comparator.comparingInt(value -> Arrays.asList(need).contains(value.getProfile().getId()) ? 0 : 1);
        if (InputUtil.isKeyPressed(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
            for (PlayerListEntry entry: entries){
                if (need.contains(entry.getProfile().getId())){
                    entries1.add(entry);
                }
            }
            cir.setReturnValue(entries1.stream().limit(80L).toList());
            return;
        }
        cir.setReturnValue(entries.stream().sorted(customComparator).limit(80L).toList());
    }

}
