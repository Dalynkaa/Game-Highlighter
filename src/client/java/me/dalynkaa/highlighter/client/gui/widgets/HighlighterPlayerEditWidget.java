package me.dalynkaa.highlighter.client.gui.widgets;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import lombok.Setter;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.adapters.ColorAdapter;
import me.dalynkaa.highlighter.client.adapters.GuiAdapter;
import me.dalynkaa.highlighter.client.gui.widgets.dropdown.HighlighterScrollDropdownComponent;
import me.dalynkaa.highlighter.client.utilities.data.HighlightPlayer;
import me.dalynkaa.highlighter.client.utilities.data.HighlightedPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.HashSet;

public class HighlighterPlayerEditWidget extends FlowLayout {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("social_interactions/background");

    @Setter
    private HighlightPlayer highlightPlayer;

    private final TextRenderer textRenderer;

    private int x;
    private int y;
    private int width;
    private int height;

    private final HighlighterScrollDropdownComponent dropdown;

    public HighlighterPlayerEditWidget(int x, int y, int width, int height, HighlightPlayer highlightPlayer) {
        super(Sizing.fixed(width), Sizing.fill(),Algorithm.VERTICAL);
        this.highlightPlayer = highlightPlayer;
        HighlightedPlayer highlightedPlayer = HighlighterClient.getServerEntry().getHighlitedPlayer(highlightPlayer.uuid());
        this.positioning(Positioning.absolute(x, y));
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.dropdown = new HighlighterScrollDropdownComponent(Sizing.fixed(width-16), Sizing.content(), highlightedPlayer.getPrefix() == null ? Text.translatable("gui.highlighter.menu.player_edit.prefix_select.title"):Text.literal(highlightedPlayer.getPrefix().getPrefixTag()), false);
        this.dropdown.tooltip(Text.translatable("gui.highlighter.menu.player_edit.prefix_select.tooltip"));
        HashSet<Prefix> prefixes = HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefixes();
        for (Prefix prefix : prefixes) {
            dropdown.button(Text.literal(prefix.getPrefixTag()), (comp) -> {
                highlightedPlayer.highlight(prefix.getPrefixId());
                HighlighterClient.getServerEntry().setPlayer(highlightedPlayer);
                this.dropdown.title(Text.literal(prefix.getPrefixTag()));
            });
        }
        this.child(dropdown.positioning(Positioning.absolute(8,42)));
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        renderBackground(context);
        String text = Text.translatable("gui.highlighter.menu.player_edit.title", highlightPlayer.name()).getString();
        context.drawCenteredTextWithShadow(textRenderer, text, this.x + width/2, this.y - 12, 0xFFFFFF);
        context.fill(this.x + 8, this.y + 8, this.x + this.width - 8, this.y + 40, 0x80000000);
        context.drawHorizontalLine(this.x + 7, this.x + this.width - 8, this.y + 40, ColorAdapter.getArgb(255, 198, 198, 198));

        int i = x + 12;
        int j = y + 8 + 4;
        if (highlightPlayer != null) {
            PlayerSkinDrawer.draw(context, this.highlightPlayer.skinTexture(), i, j, 23);
            context.drawText(textRenderer, this.highlightPlayer.name(), i + 24 + 4, j + 4, 0xFFFFFF, true);
        }
    }
    private void renderBackground(DrawContext context) {
        GuiAdapter.drawGuiTexture(context,BACKGROUND_TEXTURE, this.x, this.y, this.width, this.height);
    }
}
