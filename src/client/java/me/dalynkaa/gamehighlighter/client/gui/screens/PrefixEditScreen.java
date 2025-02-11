package me.dalynkaa.gamehighlighter.client.gui.screens;



import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.ColorPickerComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import me.dalynkaa.gamehighlighter.client.GameHighlighterClient;
import me.dalynkaa.gamehighlighter.client.utilities.ModConfig;
import me.dalynkaa.gamehighlighter.client.utilities.data.Prefix;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


@Environment(EnvType.CLIENT)
public class PrefixEditScreen extends BaseOwoScreen<FlowLayout> {
    private final ModConfig CONFIG = GameHighlighterClient.config;

    public PrefixEditScreen(Text title, Screen parent) {
        super(Text.translatable("gui.prefixEdit.titleNew"));
        this.client = MinecraftClient.getInstance();
        this.parent = parent;
        this.prefix = null;
    }
    public PrefixEditScreen(Text title, Screen parent, Prefix prefix) {
        super(Text.translatable("gui.prefixEdit.titleEdit", prefix.getPrefixTag()));
        this.client = MinecraftClient.getInstance();
        this.parent = parent;
        this.prefix = prefix;

    }
    private final Screen parent;
    private final MinecraftClient client;
    private final Prefix prefix;
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);
        Component title = Components.label(getTitle())
                .margins(Insets.of(0,0,0,0));

        TextBoxComponent prefix_tag;
        TextBoxComponent prefix_char;
        TextBoxComponent chat_template;
        TextBoxComponent prefix_player_color;
        TextBoxComponent prefix_prefix_color;


        ButtonComponent save_button;
        if (prefix != null){

            prefix_tag = Components.textBox(Sizing.fixed(110), prefix.getPrefixTag());
            prefix_char = Components.textBox(Sizing.fixed(110), prefix.getPrefixChar());
            chat_template = Components.textBox(Sizing.fixed(220), prefix.getChatTemplate());
            prefix_player_color = Components.textBox(Sizing.fixed(120), prefix.getPlayerColor());
            prefix_prefix_color = Components.textBox(Sizing.fixed(120), prefix.getPrefixColor());
            save_button = Components.button(Text.translatable("gui.prefixEdit.button.edit"), button -> {
                if (!prefix_tag.getText().isEmpty() && !prefix_char.getText().isEmpty() && !prefix_player_color.getText().isEmpty() && !prefix_prefix_color.getText().isEmpty()) {
                    GameHighlighterClient.getClientConfig().setPrefix(new Prefix(prefix.getPrefixId(), prefix_tag.getText(),chat_template.getText(),"", prefix_char.getText(), prefix_player_color.getText(), prefix_prefix_color.getText()));
                    client.setScreen(new HighlightListScreen());
                }
            });
        }else {
            prefix_tag = Components.textBox(Sizing.fixed(110), "");
            prefix_char = Components.textBox(Sizing.fixed(110), "");
            chat_template = Components.textBox(Sizing.fixed(220), CONFIG.chatSettings.globalChatMessage);
            prefix_player_color = Components.textBox(Sizing.fixed(120), "#a29bfe");
            prefix_prefix_color = Components.textBox(Sizing.fixed(120), "#6c5ce7");
            save_button = Components.button(Text.translatable("gui.prefixEdit.button.create"), button -> {
                if (!prefix_tag.getText().isEmpty() && !prefix_char.getText().isEmpty() && !prefix_player_color.getText().isEmpty() && !prefix_prefix_color.getText().isEmpty()) {
                    GameHighlighterClient.getClientConfig().addPrefix(new Prefix(UUID.randomUUID(), prefix_tag.getText(),chat_template.getText(),"", prefix_char.getText(), prefix_player_color.getText(), prefix_prefix_color.getText()));
                    client.setScreen(new HighlightListScreen());
                }
            });
        }

        prefix_tag.setMaxLength(16);
        prefix_tag.tooltip(Text.translatable("gui.prefixEdit.tooltip.tag"));
        prefix_char.setMaxLength(4);
        prefix_char.tooltip(Text.translatable("gui.prefixEdit.tooltip.symbol"));
        prefix_player_color.setMaxLength(7);
        prefix_player_color.tooltip(Text.translatable("gui.prefixEdit.tooltip.color"));
        prefix_prefix_color.setMaxLength(7);
        prefix_prefix_color.tooltip(Text.translatable("gui.prefixEdit.tooltip.prefixColor"));
        chat_template.tooltip(Text.translatable("gui.prefixEdit.tooltip.chatTemplate"));
        chat_template.setMaxLength(500);
        if (prefix != null){
            chat_template.text(prefix.getChatTemplate());
        }


        ParentComponent color_picker_container_player = collorPicker(prefix_player_color,prefix != null ? prefix.getPlayerColor() : "#a29bfe");
        ParentComponent color_picker_container_prefix = collorPicker(prefix_prefix_color,prefix != null ? prefix.getPrefixColor() : "#6c5ce7");

        ParentComponent tag_container = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.translatable("gui.prefixEdit.label.tag")).margins(Insets.of(0,3,4,0)))
                .child(prefix_tag.margins(Insets.of(0,3,0,0)));
        ParentComponent char_container = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.translatable("gui.prefixEdit.label.symbol")).margins(Insets.of(0,3,4,0)))
                .child(prefix_char.margins(Insets.of(0,3,0,0)));
        ParentComponent info_container = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(tag_container)
                .child(char_container);
        ParentComponent chat_container = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.translatable("gui.prefixEdit.label.chatTemplate")).margins(Insets.of(0,3,4,0)))
                .child(chat_template.margins(Insets.of(0,3,0,0)));
        ParentComponent color_container = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.translatable("gui.prefixEdit.label.nameColor")).margins(Insets.of(0,3,4,0)))
                .child(color_picker_container_player.margins(Insets.of(0,3,0,0)));
        ParentComponent prefix_color_container = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.translatable("gui.prefixEdit.label.prefixColor")).margins(Insets.of(0,3,4,0)))
                .child(color_picker_container_prefix.margins(Insets.of(0,3,0,0)));
        CollapsibleContainer collapsibleContainer = Containers.collapsible(Sizing.fill(), Sizing.fixed(20), Text.literal(""), false);
        collapsibleContainer.child(chat_container.margins(Insets.of(0,3,3,0)));

        ParentComponent editContainer = Containers.verticalFlow(Sizing.fill(), Sizing.content())
                .child(info_container.margins(Insets.of(4,3,3,0)))
                .child(color_container.margins(Insets.of(0,3,3,0)))
                .child(prefix_color_container.margins(Insets.of(0,3,3,0)))
                .child(chat_container.margins(Insets.of(0,3,3,0)))
                .child(save_button.margins(Insets.of(0,3,3,0)))
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .padding(Insets.of(3,3,3,3))
                .surface(Surface.DARK_PANEL);
        ParentComponent titleContainer = Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(15))
                .child(title)
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .padding(Insets.of(3,3,5,13));
        ParentComponent container = Containers.verticalFlow(Sizing.fixed(236), Sizing.content())
                .child(titleContainer)
                .child(editContainer)
                .padding(Insets.of(0))
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.LEFT);
        rootComponent.child(
                container
        );
    }

    private ParentComponent collorPicker(TextBoxComponent prefix_prefix_color, String hexColor) {
        ColorPickerComponent colorPickerWidget_prefix = new ColorPickerComponent();
        colorPickerWidget_prefix.selectedColor(Color.ofRgb(hexToRgb(hexColor)));
        colorPickerWidget_prefix.sizing(Sizing.fixed(100), Sizing.fixed(50));
        colorPickerWidget_prefix.onChanged().subscribe(color -> prefix_prefix_color.setText(color.asHexString(false)));
        prefix_prefix_color.onChanged().subscribe(color -> colorPickerWidget_prefix.selectedColor(Color.ofRgb(hexToRgb(color))));

        return Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(prefix_prefix_color.margins(Insets.of(0,3,0,0)))
                .child(colorPickerWidget_prefix);
    }
    private int hexToRgb(String hexColor ) {
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
            return Integer.parseInt(hexColor, 16);
        }else {
            return Integer.parseInt("a29bfe", 16);
        }

    }

    @Override
    public void close() {
        client.setScreen(parent);
    }


}
