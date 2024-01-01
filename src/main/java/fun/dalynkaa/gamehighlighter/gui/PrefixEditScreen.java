package fun.dalynkaa.gamehighlighter.gui;

import fun.dalynkaa.gamehighlighter.client.GameHighlighterClient;
import fun.dalynkaa.gamehighlighter.utils.data.Prefix;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


@Environment(EnvType.CLIENT)
public class PrefixEditScreen extends BaseOwoScreen<FlowLayout> {
    private static final Logger LOGGER = LoggerFactory.getLogger("otsohelper");

    public PrefixEditScreen(Text title, Screen parent) {
        super(Text.translatable("gui.prefixEdit.titleNew"));
        this.client = MinecraftClient.getInstance();
        this.parent = parent;
        this.prefix = null;
    }
    public PrefixEditScreen(Text title, Screen parent, Prefix prefix) {
        super(Text.translatable("gui.prefixEdit.titleEdit", prefix.getPrefix_tag()));
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
                .margins(Insets.of(5,0,0,0));

        TextBoxComponent prefix_tag;
        TextBoxComponent prefix_char;
        TextBoxComponent prefix_player_color;
        TextBoxComponent prefix_prefix_color;
        ButtonComponent save_button;
        if (prefix != null){
            prefix_tag = Components.textBox(Sizing.fixed(206), prefix.getPrefix_tag());
            prefix_char = Components.textBox(Sizing.fixed(206), prefix.getPrefixChar());
            prefix_player_color = Components.textBox(Sizing.fixed(206), prefix.getColor());
            prefix_prefix_color = Components.textBox(Sizing.fixed(206), prefix.getPrefix_color());
            save_button = Components.button(Text.translatable("gui.prefixEdit.button.edit"), button -> {
                if (!prefix_tag.getText().isEmpty() && !prefix_char.getText().isEmpty() && !prefix_player_color.getText().isEmpty() && !prefix_prefix_color.getText().isEmpty()) {
                    GameHighlighterClient.getClientConfig().setPrefix(new Prefix(prefix.getPrefix_id(), prefix_tag.getText(), prefix_char.getText(), prefix_player_color.getText(), prefix_prefix_color.getText()));
                    client.setScreen(new SocialInteractionsScreen());
                }
            });
        }else {
            prefix_tag = Components.textBox(Sizing.fixed(206), "");
            prefix_char = Components.textBox(Sizing.fixed(206), "");
            prefix_player_color = Components.textBox(Sizing.fixed(206), "#a29bfe");
            prefix_prefix_color = Components.textBox(Sizing.fixed(206), "#6c5ce7");
            save_button = Components.button(Text.translatable("gui.prefixEdit.button.create"), button -> {
                if (!prefix_tag.getText().isEmpty() && !prefix_char.getText().isEmpty() && !prefix_player_color.getText().isEmpty() && !prefix_prefix_color.getText().isEmpty()) {
                    GameHighlighterClient.getClientConfig().addPrefix(new Prefix(UUID.randomUUID(), prefix_tag.getText(), prefix_char.getText(), prefix_player_color.getText(), prefix_prefix_color.getText()));
                    client.setScreen(new SocialInteractionsScreen());
                }
            });
        }
        prefix_tag.setMaxLength(16);
        prefix_char.setMaxLength(4);
        prefix_player_color.setMaxLength(7);
        prefix_prefix_color.setMaxLength(7);

        ParentComponent tag_container = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.translatable("gui.prefixEdit.label.tag")).margins(Insets.of(0,5,0,0)))
                .child(prefix_tag.margins(Insets.of(0,5,0,0)));
        ParentComponent char_container = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.translatable("gui.prefixEdit.label.symbol")).margins(Insets.of(0,5,0,0)))
                .child(prefix_char.margins(Insets.of(0,5,0,0)));
        ParentComponent color_container = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.translatable("gui.prefixEdit.label.nameColor")).margins(Insets.of(0,5,0,0)))
                .child(prefix_player_color.margins(Insets.of(0,5,0,0)));
        ParentComponent prefix_color_container = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.translatable("gui.prefixEdit.label.prefixColor")).margins(Insets.of(0,5,0,0)))
                .child(prefix_prefix_color.margins(Insets.of(0,5,0,0)));


        ParentComponent editContainer = Containers.verticalFlow(Sizing.fixed(236), Sizing.content())
                .child(tag_container.margins(Insets.of(0,5,5,0)))
                .child(char_container.margins(Insets.of(0,5,5,0)))
                .child(color_container.margins(Insets.of(0,5,5,0)))
                .child(prefix_color_container.margins(Insets.of(0,5,5,0)))
                .child(save_button.margins(Insets.of(0,5,5,0)))
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .padding(Insets.of(5,5,7,5))
                .surface(Surface.DARK_PANEL);
        ParentComponent titleContainer = Containers.horizontalFlow(Sizing.fixed(236), Sizing.fixed(30))
                .child(title)
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .padding(Insets.of(5,5,15,15))
                .surface(Surface.DARK_PANEL);
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
    @Override
    public void close() {
        client.setScreen(parent);
    }


}
