package fun.dalynkaa.gamehighlighter.gui.widget;

import fun.dalynkaa.gamehighlighter.utils.data.HighlightPlayer;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;

public class PlayerHeadComponent extends FlowLayout {
    HighlightPlayer player;
    public static final int FACE_WIDTH = 8;
    public static final int FACE_HEIGHT = 8;
    public static final int FACE_X = 8;
    public static final int FACE_Y = 8;
    public static final int SKIN_TEXTURE_WIDTH = 64;
    public static final int SKIN_TEXTURE_HEIGHT = 64;
    public PlayerHeadComponent(Sizing horizontalSizing, Sizing verticalSizing, HighlightPlayer player) {
        super(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL);
        this.player = player;
        TextureComponent component = Components.texture(player.skinTexture().get(),FACE_X, FACE_Y, FACE_WIDTH, FACE_HEIGHT, SKIN_TEXTURE_WIDTH, SKIN_TEXTURE_HEIGHT);
        super.child(component);
    }
}
