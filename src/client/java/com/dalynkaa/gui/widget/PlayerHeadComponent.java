package com.dalynkaa.gui.widget;

import com.dalynkaa.utilities.data.HighlightPlayer;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.util.Identifier;

public class PlayerHeadComponent extends FlowLayout {
    public static final int u = 8;
    public static final int v = 8;
    public static final int SKIN_TEXTURE_WIDTH = 8;
    public static final int SKIN_TEXTURE_HEIGHT = 8;
    public PlayerHeadComponent(Sizing horizontalSizing, Sizing verticalSizing, Identifier skinTexture) {
        super(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL);
        TextureComponent component = Components.texture(skinTexture,u,v,SKIN_TEXTURE_WIDTH,SKIN_TEXTURE_HEIGHT,64,64);
        component.sizing(horizontalSizing,verticalSizing);
        super.child(component);
    }
}
