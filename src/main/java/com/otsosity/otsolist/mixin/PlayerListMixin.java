package com.otsohelper.mixin.client;

import com.otsohelper.utils.DataClasses.RequesrData;
import com.otsohelper.utils.NetworkUtils;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("otsohelper");
    @Inject(method = "getPlayerName",at = @At("RETURN"),cancellable = true)
    public void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir){
        Text displayname = cir.getReturnValue();
        RequesrData s = NetworkUtils.getOtsoUser(entry.getProfile().getId());
        LOGGER.info(s.status.toString());
        if (s.status){
            MutableText mutableText = Text.literal("");

            mutableText.append(StringEscapeUtils.unescapeJava(s.data.prefix)).styled(style -> style.withColor(TextColor.parse(s.data.prefix_color)));
            mutableText.append(displayname.copy().styled(style -> style.withColor(TextColor.parse(s.data.display_color))));
            cir.setReturnValue(mutableText);
        }

    }
}
