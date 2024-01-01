package fun.dalynkaa.gamehighlighter.mixin;

import com.google.common.collect.ImmutableList;
import fun.dalynkaa.gamehighlighter.client.GameHighlighterClient;
import fun.dalynkaa.gamehighlighter.gui.PlayerEditScreen;
import fun.dalynkaa.gamehighlighter.utils.data.HighlightPlayer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
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
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
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
    @Final
    private String name;
    @Shadow
    @Final
    private Supplier<Identifier> skinTexture;

    @Shadow
    protected abstract Text getStatusText();

    @Shadow
    private boolean offline;
    @Shadow
    @Final
    private List<ClickableWidget> buttons;
    private ButtonWidget muteShowButton;
    //private ButtonWidget muteHideButton;

    @Shadow
    abstract MutableText getNarrationMessage(MutableText text);

    @Shadow public abstract boolean isOffline();

    private List<ClickableWidget> customButtons;
    private static Identifier HIGHLIGHT_ICON = new Identifier("game_highlighter","textures/gui/icons.png");
    private static final Text HIGHLIGHTED_TOOLTIP = Text.translatable("gui.socialInteractions.tooltip.highlighted");
    private static final Text UN_HIGHLIGHTED_TOOLTIP = Text.translatable("gui.socialInteractions.tooltip.un_highlighted");

    private static final Text HIDDEN = (Text.translatable("gui.socialInteractions.status_hidden")).formatted(Formatting.ITALIC);
    private static final Text HIGHLIGHTED = (Text.translatable("gui.game_highlighter.highlighted")).formatted(Formatting.ITALIC);
    private static final Text BLOCKED = (Text.translatable("gui.socialInteractions.status_blocked")).formatted(Formatting.ITALIC);
    private static final Text OFFLINE = (Text.translatable("gui.socialInteractions.status_offline")).formatted(Formatting.ITALIC);
    private static final Text HIDDEN_OFFLINE = (Text.translatable("gui.socialInteractions.status_hidden_offline")).formatted(Formatting.ITALIC);
    private static final Text BLOCKED_OFFLINE = (Text.translatable("gui.socialInteractions.status_blocked_offline")).formatted(Formatting.ITALIC);



    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(MinecraftClient client, SocialInteractionsScreen parent, UUID uuid, String name, Supplier<Identifier> skinTexture, boolean reportable,CallbackInfo ci) {
        SocialInteractionsManager socialInteractionsManager = client.getSocialInteractionsManager();

        if (!client.player.getUuid().equals(uuid) &&
                !socialInteractionsManager.isPlayerBlocked(uuid)) {

            this.muteShowButton = new TexturedButtonWidget(0, 0, 20, 20, 0, 32, 20, HIGHLIGHT_ICON, 256, 256,button -> {
                client.setScreen(new PlayerEditScreen(Text.literal("123"), new HighlightPlayer(uuid, name, skinTexture), parent));
            },Text.translatable("gui.game_highlighter.un_highlighted"));
            this.customButtons = new ArrayList<ClickableWidget>();
            this.customButtons.add(this.muteShowButton);
            setHiglightButtonVisible(GameHighlighterClient.clientConfig.isHighlighted(uuid));
        }else {
            this.customButtons = ImmutableList.of();
        }

    }


    @Inject(method = "render", at = @At(value = "TAIL"))
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (this.muteShowButton != null) {
            this.muteShowButton.setX(x + (entryWidth - this.muteShowButton.getWidth() - 52));
            this.muteShowButton.setY(y + (entryHeight - this.muteShowButton.getHeight()) / 2);
            this.muteShowButton.render(context, mouseX, mouseY, tickDelta);
    }
}
    @Inject(method = "getStatusText", at = @At(value = "RETURN"), cancellable = true)
    private void getStatusText(CallbackInfoReturnable<Text> cir) {
        boolean bl = this.client.getSocialInteractionsManager().isPlayerHidden(this.uuid);
        boolean bl2 = this.client.getSocialInteractionsManager().isPlayerBlocked(this.uuid);
        boolean bl3 = GameHighlighterClient.clientConfig.isHighlighted(uuid);
        if (bl2 && this.isOffline()) {
            cir.setReturnValue(BLOCKED_OFFLINE);
        } else if (bl && this.isOffline()) {
            cir.setReturnValue(HIDDEN_OFFLINE);
        } else if (bl2) {
            cir.setReturnValue(BLOCKED);
        } else if (bl) {
            cir.setReturnValue(HIDDEN);
        }else if (bl3) {
            cir.setReturnValue(HIGHLIGHTED);
        } else {
            if (this.isOffline()) {
                cir.setReturnValue(OFFLINE);
            } else {
                cir.setReturnValue(Text.empty());
            }
        }
    }



    private void setHiglightButtonVisible(boolean showButtonVisible) {

        this.muteShowButton.visible = true;
        this.customButtons.set(0, this.muteShowButton);
    }


    @Inject(method = "children", at = @At(value = "RETURN"), cancellable = true)
    public void children(CallbackInfoReturnable<List<? extends Element>> cir) {
        cir.setReturnValue(Stream.concat(this.buttons.stream(), this.customButtons.stream())
                .collect(Collectors.toList()));
    }
}
