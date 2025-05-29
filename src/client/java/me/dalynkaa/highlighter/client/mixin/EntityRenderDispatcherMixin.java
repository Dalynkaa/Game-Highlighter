package me.dalynkaa.highlighter.client.mixin;

import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.config.ModConfig;
import me.dalynkaa.highlighter.client.utilities.data.HighlightedPlayer;
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
    //? if =1.21.1 {
    private <E extends Entity> void render(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
    //?} else {
    /*private <E extends Entity> void render(E entity, double x, double y, double z,float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
     *///? }
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
        }else if (config.playerHiderSettings.hideType.equals(ModConfig.HideType.HIDDEN)){
            if(entity instanceof PlayerEntity player && !player.isMainPlayer() && config.hasPlayerHiderEnable()) {
                HighlightedPlayer highlightedPlayer = HighlighterClient.getServerEntry().getHighlitedPlayer(player.getUuid());
                if (highlightedPlayer.isHidden()){
                    ci.cancel();
                }

            }
        }

    }
}
