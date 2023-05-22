package com.otsosity.otsolist.mixin;

import com.google.common.collect.ImmutableList;
import com.otsosity.otsolist.client.OtsoListClient;
import com.otsosity.otsolist.utils.HiglightConfig;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Mixin(SocialInteractionsPlayerListEntry.class)
public abstract class PlayerEntryMixin {

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    @Final
    private UUID uuid;

    @Shadow
    protected abstract Text getStatusText();

    @Shadow
    private boolean offline;
    @Shadow
    @Final
    private List<ClickableWidget> buttons;
    private ButtonWidget muteShowButton;
    private ButtonWidget muteHideButton;

    @Shadow
    abstract MutableText getNarrationMessage(MutableText text);

    @Shadow public abstract boolean isOffline();

    private List<ClickableWidget> customButtons;
    private static Identifier HIGLIGHT_ICON = new Identifier("otsolist","textures/gui/icons.png");


    private static final Text HIDDEN = (Text.translatable("gui.socialInteractions.status_hidden")).formatted(Formatting.ITALIC);
    private static final Text BLOCKED = (Text.translatable("gui.socialInteractions.status_blocked")).formatted(Formatting.ITALIC);
    private static final Text OFFLINE = (Text.translatable("gui.socialInteractions.status_offline")).formatted(Formatting.ITALIC);
    private static final Text HIDDEN_OFFLINE = (Text.translatable("gui.socialInteractions.status_hidden_offline")).formatted(Formatting.ITALIC);
    private static final Text BLOCKED_OFFLINE = (Text.translatable("gui.socialInteractions.status_blocked_offline")).formatted(Formatting.ITALIC);



    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        SocialInteractionsManager socialInteractionsManager = client.getSocialInteractionsManager();

        if (!client.player.getUuid().equals(uuid) &&
                !socialInteractionsManager.isPlayerBlocked(uuid)) {

            this.muteShowButton = new TexturedButtonWidget(0, 0, 20, 20, 0, 32, 20, HIGLIGHT_ICON, 256, 256,button -> {
                // действие
                OtsoListClient.clientConfig.unhighlight(uuid);
                client.player.sendMessage(Text.literal("1"));
                setHiglightButtonVisible(false);
            },Text.translatable("gui.socialInteractions.show"));

            this.muteHideButton = new TexturedButtonWidget(0, 0, 20, 20, 20, 32, 20, HIGLIGHT_ICON, 256, 256,button -> {
                // действие
                OtsoListClient.clientConfig.highlight(uuid);
                client.player.sendMessage(Text.literal("0"));
                setHiglightButtonVisible(true);
            },Text.translatable("gui.socialInteractions.hide"));
            this.customButtons = new ArrayList<ClickableWidget>();
            this.customButtons.add(this.muteHideButton);
            this.customButtons.add(this.muteShowButton);
            setHiglightButtonVisible(OtsoListClient.clientConfig.isHighlighted(uuid));
        }else {
            this.customButtons = ImmutableList.of();
        }

    }


    @Inject(method = "render", at = @At(value = "TAIL"))
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
                       int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (this.muteHideButton != null && this.muteShowButton != null) {
            this.muteHideButton.setX(x + (entryWidth - this.muteHideButton.getWidth() - 52));
            this.muteHideButton.setY(y + (entryHeight - this.muteHideButton.getHeight()) / 2);
            this.muteHideButton.render(matrices, mouseX, mouseY, tickDelta);

            this.muteShowButton.setX(x + (entryWidth - this.muteShowButton.getWidth() - 52));
            this.muteShowButton.setY(y + (entryHeight - this.muteShowButton.getHeight()) / 2);
            this.muteShowButton.render(matrices, mouseX, mouseY, tickDelta);
    }}
    @Inject(method = "getStatusText", at = @At(value = "RETURN"), cancellable = true)
    private void getStatusText(CallbackInfoReturnable<Text> cir) {
        boolean bl = this.client.getSocialInteractionsManager().isPlayerHidden(this.uuid) ||
                OtsoListClient.clientConfig.isHighlighted(uuid);
        boolean bl2 = this.client.getSocialInteractionsManager().isPlayerBlocked(this.uuid);
        if (bl2 && this.isOffline()) {
            cir.setReturnValue(BLOCKED_OFFLINE);
        } else if (bl && this.isOffline()) {
            cir.setReturnValue(HIDDEN_OFFLINE);
        } else if (bl2) {
            cir.setReturnValue(BLOCKED);
        } else if (bl) {
            cir.setReturnValue(HIDDEN);
        } else {
            if (this.isOffline()) {
                cir.setReturnValue(OFFLINE);
            } else {
                cir.setReturnValue(Text.empty());
            }
        }
    }



    private void setHiglightButtonVisible(boolean showButtonVisible) {
        this.muteShowButton.visible = showButtonVisible;
        this.muteHideButton.visible = !showButtonVisible;
        this.customButtons.set(0, showButtonVisible ?  this.muteShowButton:this.muteHideButton);
    }


    @Inject(method = "children", at = @At(value = "RETURN"), cancellable = true)
    public void children(CallbackInfoReturnable<List<? extends Element>> cir) {
        cir.setReturnValue(Stream.concat(this.buttons.stream(), this.customButtons.stream())
                .collect(Collectors.toList()));
    }
}
