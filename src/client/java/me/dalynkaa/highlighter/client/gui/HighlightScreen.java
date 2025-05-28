package me.dalynkaa.highlighter.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.gui.widgets.HighlighterPlayerEditWidget;
import me.dalynkaa.highlighter.client.gui.widgets.HighlighterPrefixEditWidget;
import me.dalynkaa.highlighter.client.gui.widgets.lists.HighlighterPlayerListWidget;
import me.dalynkaa.highlighter.client.gui.widgets.lists.HighlighterPrefixListWidget;
import me.dalynkaa.highlighter.client.utilities.data.HighlightPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

public class HighlightScreen extends BaseOwoScreen<FlowLayout> {

    private static final Text PLAYERS_TAB_TITLE;
    private static final Text PREFIXES_TAB_TITLE;
    private static final Text SELECTED_PLAYERS_TAB_TITLE;
    private static final Text SELECTED_PREFIXES_TAB_TITLE;
    private static final Text SEARCH_TEXT;
    static final Text EMPTY_SEARCH_TEXT;

    private static final int SCREEN_WIDTH = 236;
    //textures
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("social_interactions/background");
    private static final Identifier SEARCH_ICON_TEXTURE = Identifier.ofVanilla("icon/search");
    private static final Identifier INWORLD_MENU_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_background.png");


    FlowLayout mainLayout;

    HighlighterPlayerListWidget playerList;
    HighlighterPrefixListWidget prefixList;

    private ButtonWidget playersTabButton;
    private ButtonWidget prefixesTabButton;
    private TextFieldWidget searchBox;
    private String currentSearch;

    private Tab currentTab = Tab.PLAYERS;

    private HighlightPlayer currentPlayer = null;
    private Prefix currentPrefix = null;

    HighlighterPlayerEditWidget highlighterPlayerEditWidget;
    HighlighterPrefixEditWidget highlighterPrefixCreateEditWidget;

    private int getScreenHeight() {
        return Math.max(52, this.height - 128 - 16);
    }

    private int getPlayerListBottom() {
        return 80 + this.getScreenHeight() - 8;
    }

    private int getScreenStartX() {
        return (this.width - SCREEN_WIDTH - 64);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    protected void build(FlowLayout flowLayout) {
        this.mainLayout = flowLayout;
        // --- playerList ---
        this.playerList = new HighlighterPlayerListWidget(this, this.client, SCREEN_WIDTH, this.getPlayerListBottom()-88, 90, 35);
        this.playerList.setPosition(getScreenStartX(), 90);
        this.playerList = (HighlighterPlayerListWidget) this.playerList.positioning(Positioning.absolute(getScreenStartX(), 90));

        // --- prefixList ---
        this.prefixList = new HighlighterPrefixListWidget(this, this.client, SCREEN_WIDTH, this.getPlayerListBottom()-120, 90, 35);
        this.prefixList.setPosition(getScreenStartX(), 90);
        this.prefixList = (HighlighterPrefixListWidget) this.prefixList.positioning(Positioning.absolute(getScreenStartX(), 90));

        int buttonsWidth = SCREEN_WIDTH / 2 - 2;

        this.playersTabButton = this.addDrawableChild(ButtonWidget.builder(PLAYERS_TAB_TITLE, (button) -> this.setCurrentTab(Tab.PLAYERS)).dimensions(getScreenStartX()+1, 42, buttonsWidth, 20).build());
        this.prefixesTabButton = this.addDrawableChild(ButtonWidget.builder(PREFIXES_TAB_TITLE, (button) -> this.setCurrentTab(Tab.PREFIXES)).dimensions(getScreenStartX()+1+buttonsWidth, 42, buttonsWidth, 20).build());

        this.playersTabButton = (ButtonWidget) this.playersTabButton.positioning(Positioning.absolute(getScreenStartX()+1, 42));
        this.prefixesTabButton = (ButtonWidget) this.prefixesTabButton.positioning(Positioning.absolute(getScreenStartX()+1+buttonsWidth, 42));

        // --- search bar ---
        // --- searchBox ---
        String string = this.searchBox != null ? this.searchBox.getText() : "";
        this.searchBox = new TextFieldWidget(this.textRenderer, this.getScreenStartX() + 26, 74, 200, 15, SEARCH_TEXT);
        this.searchBox.setMaxLength(16);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(-1);
        this.searchBox.setText(string);
        this.searchBox.setPlaceholder(SEARCH_TEXT);
        this.searchBox.setChangedListener(this::onSearchChange);
        this.searchBox.positioning(Positioning.absolute(getScreenStartX()+26, 74));

        int i1 = Math.max(100, this.height - 600);

        // --- add components to mainLayout ---
        this.highlighterPlayerEditWidget = null;
        this.highlighterPrefixCreateEditWidget = null;
        //flowLayout.child(highlighterPlayerEditWidget);

        // --- add components to flowLayout ---
        flowLayout.child(this.playersTabButton);
        flowLayout.child(this.prefixesTabButton);
        flowLayout.child(this.searchBox);
        flowLayout.child(playerList);
        setCurrentTab(Tab.PLAYERS);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        this.applyBlur(delta);
        BaseOwoScreen.renderBackgroundTexture(context, INWORLD_MENU_BACKGROUND_TEXTURE, 0, 0,0.0F, 0.0F, this.width, this.height);
        context.drawGuiTexture(BACKGROUND_TEXTURE, getScreenStartX(), 64, SCREEN_WIDTH, this.getScreenHeight() + 16);
        context.drawGuiTexture(SEARCH_ICON_TEXTURE, getScreenStartX() + 10, 76, 12, 12);
    }
    protected void applyBlur(float delta) {
        this.client.gameRenderer.renderBlur(delta);
        this.client.getFramebuffer().beginWrite(false);
    }

    public void setCurrentPlayer(@Nullable HighlightPlayer player) {
        Highlighter.LOGGER.info("setCurrentPlayer: {}", player);
        if (this.highlighterPrefixCreateEditWidget != null) {
            this.mainLayout.removeChild(this.highlighterPrefixCreateEditWidget);
            this.highlighterPrefixCreateEditWidget = null;
        }
        if (player == null) {
            this.currentPlayer = null;
            if (this.highlighterPlayerEditWidget != null) {
                this.mainLayout.removeChild(this.highlighterPlayerEditWidget);
                this.highlighterPlayerEditWidget = null;
            }
            return;
        }
        if (this.highlighterPlayerEditWidget != null) {
            this.mainLayout.removeChild(this.highlighterPlayerEditWidget);
        }
        int editWidgetHeight = Math.max(71, 0);
        int editWidgetX = 64;
        int editWidgetY = (this.height / 2) - (editWidgetHeight / 2);
        this.highlighterPlayerEditWidget = new HighlighterPlayerEditWidget(editWidgetX, editWidgetY, SCREEN_WIDTH , editWidgetHeight, player);
        this.currentPlayer = player;
        this.mainLayout.child(highlighterPlayerEditWidget);
    }
    public void setCurrentPrefix(@Nullable Prefix prefix) {
        Highlighter.LOGGER.info("setCurrentPrefix: {}", prefix);
        if (this.highlighterPlayerEditWidget != null) {
            this.mainLayout.removeChild(this.highlighterPlayerEditWidget);
            this.highlighterPlayerEditWidget = null;
        }
        if (prefix == null) {
            this.currentPrefix = null;
            if (this.highlighterPrefixCreateEditWidget != null) {
                this.mainLayout.removeChild(this.highlighterPrefixCreateEditWidget);
                this.highlighterPrefixCreateEditWidget = null;
            }
            return;
        }
        if (this.highlighterPrefixCreateEditWidget != null) {
            this.mainLayout.removeChild(this.highlighterPrefixCreateEditWidget);
        }
        int editWidgetHeight = Math.max(208, 0);
        int editWidgetX = 64;
        int editWidgetY = (this.height / 2) - (editWidgetHeight / 2);
        this.highlighterPrefixCreateEditWidget = new HighlighterPrefixEditWidget( this,editWidgetX, editWidgetY, SCREEN_WIDTH , editWidgetHeight, prefix);
        this.currentPrefix = prefix;
        this.mainLayout.child(highlighterPrefixCreateEditWidget);
    }
    public void updatePrefixList() {
        Collection<Prefix> prefixes = HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefixes();
        this.prefixList.update(prefixes, this.prefixList.getScrollAmount());
    }

    private void setCurrentTab(Tab tab) {
        this.currentTab = tab;
        playersTabButton.setMessage(PLAYERS_TAB_TITLE);
        prefixesTabButton.setMessage(PREFIXES_TAB_TITLE);
        this.mainLayout.removeChild(this.playerList);
        this.mainLayout.removeChild(this.prefixList);
        // Обновляем сообщения на кнопках вкладок
        switch (tab) {
            case PLAYERS -> {
                this.mainLayout.child(this.playerList);
                playersTabButton.setMessage(SELECTED_PLAYERS_TAB_TITLE);
                Collection<UUID> collection = this.client.player.networkHandler.getPlayerUuids();
                playerList.update(collection, this.playerList.getScrollAmount(), false);
            }
            case PREFIXES -> {
                this.mainLayout.child(this.prefixList);
                prefixesTabButton.setMessage(SELECTED_PREFIXES_TAB_TITLE);
                updatePrefixList();
            }
        }
    }

    private void onSearchChange(String currentSearch) {
        currentSearch = currentSearch.toLowerCase(Locale.ROOT);
        if (!currentSearch.equals(this.currentSearch)) {
            this.currentSearch = currentSearch;
            if (currentTab == Tab.PLAYERS) {
                this.playerList.setCurrentSearch(currentSearch);
                Collection<UUID> collection = this.client.player.networkHandler.getPlayerUuids();
                playerList.update(collection, this.playerList.getScrollAmount(), false);
            } else if (currentTab == Tab.PREFIXES) {
                this.prefixList.setCurrentSearch(currentSearch);
                updatePrefixList();
            }
        }
    }

    public enum Tab{
        PLAYERS,
        PREFIXES
    }

    static {
        PLAYERS_TAB_TITLE = Text.translatable("gui.gamehighlighter.menu.tab.players");
        PREFIXES_TAB_TITLE = Text.translatable("gui.gamehighlighter.menu.tab.prefixes");
        SELECTED_PREFIXES_TAB_TITLE = PREFIXES_TAB_TITLE.copyContentOnly().formatted(Formatting.UNDERLINE);
        SELECTED_PLAYERS_TAB_TITLE = PLAYERS_TAB_TITLE.copyContentOnly().formatted(Formatting.UNDERLINE);
        EMPTY_SEARCH_TEXT = Text.translatable("gui.gamehighlighter.menu.search_empty").formatted(Formatting.GRAY);
        SEARCH_TEXT = Text.translatable("gui.gamehighlighter.menu.search_hint").formatted(Formatting.GRAY);
    }
}
