package me.dalynkaa.gamehighlighter.client.mixin;

import me.dalynkaa.gamehighlighter.client.utilities.ModConfig;
import me.dalynkaa.gamehighlighter.client.utilities.data.HighlitedPlayer;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Unique
    ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void render(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (config.playerHiderSettings.hideType.equals(ModConfig.HideType.RADIUS)){
            if (MinecraftClient.getInstance().player!=null){
                if(entity instanceof PlayerEntity player && !player.isMainPlayer() && config.hasPlayerHiderEnable() && MinecraftClient.getInstance().player.distanceTo(player) > config.playerHiderSettings.radius) {
                    ci.cancel();
                }
            }

        }else if (config.playerHiderSettings.hideType.equals(ModConfig.HideType.ALL)){
            if(entity instanceof PlayerEntity player && !player.isMainPlayer() && config.hasPlayerHiderEnable()) {
                ci.cancel();
            }
        }else if (config.playerHiderSettings.hideType.equals(ModConfig.HideType.HIDEN)){
            if(entity instanceof PlayerEntity player && !player.isMainPlayer() && config.hasPlayerHiderEnable()) {
                HighlitedPlayer highlitedPlayer = HighlitedPlayer.getHighlitedPlayer(player.getUuid());
                if (highlitedPlayer.isHiden()){
                    ci.cancel();
                }

            }
        }

    }
}
