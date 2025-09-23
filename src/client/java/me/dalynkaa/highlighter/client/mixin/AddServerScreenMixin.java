package me.dalynkaa.highlighter.client.mixin;

import me.dalynkaa.highlighter.client.gui.ServerSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(AddServerScreen.class)
public class AddServerScreenMixin extends Screen {
    @Shadow @Final
    private ServerInfo server;


    @Shadow private TextFieldWidget addressField;
    @Shadow private TextFieldWidget serverNameField;
    @Unique
    private ButtonWidget serverConfigureButton;

    protected AddServerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    protected void init(CallbackInfo ci){
        this.serverConfigureButton = this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.highlighter.menu.server_settings.button"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new ServerSettingsScreen(this, this.server));
                    }
                }
                )
                        .dimensions(this.width / 2 - 100, this.height - 40, 200, 20)
                .build());
    }

    @Inject(method = "updateAddButton" , at = @At("HEAD"), cancellable = false)
    protected void updateAddButton(CallbackInfo ci) {
        if (this.serverConfigureButton != null) {
            this.serverConfigureButton.active = ServerAddress.isValid(this.addressField.getText()) && !this.serverNameField.getText().isEmpty();
        }
    }
}
