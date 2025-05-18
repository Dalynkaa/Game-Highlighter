package me.dalynkaa.highlighter.client.newgui;

import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.config.PrefixStorage;
import me.dalynkaa.highlighter.client.newgui.focus.FocusManager;
import me.dalynkaa.highlighter.client.newgui.focus.FocusableContainer;
import me.dalynkaa.highlighter.client.newgui.widgets.HighlighterPlayerEditWidget;
import me.dalynkaa.highlighter.client.newgui.widgets.HighlighterPlayerListWidget;
import me.dalynkaa.highlighter.client.newgui.widgets.HighlighterPrefixCreateEditWidget;
import me.dalynkaa.highlighter.client.newgui.widgets.HighlighterPrefixListWidget;
import me.dalynkaa.highlighter.client.newgui.widgets.helper.NestedGuiGroup;
import me.dalynkaa.highlighter.client.utilities.data.HighlightPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HighlightScreen extends Screen implements FocusableContainer {
    //constants
    private static final int SCREEN_WIDTH = 236;

    //translations
    private static final Text PLAYERS_TAB_TITLE;
    private static final Text PREFIXES_TAB_TITLE;
    private static final Text SELECTED_PLAYERS_TAB_TITLE;
    private static final Text SELECTED_PREFIXES_TAB_TITLE;
    private static final Text SEARCH_TEXT;
    static final Text EMPTY_SEARCH_TEXT;

    //textures
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("social_interactions/background");
    private static final Identifier SEARCH_ICON_TEXTURE = Identifier.ofVanilla("icon/search");

    // Делегат для обработки событий вложенных элементов
    private final ScreenNestedGuiDelegate guiDelegate;

    private ButtonWidget playersTabButton;
    private ButtonWidget prefixesTabButton;
    private TextFieldWidget searchBox;
    private String currentSearch;

    HighlighterPlayerListWidget playerList;
    HighlighterPrefixListWidget prefixList;

    ButtonWidget createPrefixButton;

    HighlighterPlayerEditWidget highlighterPlayerEditWidget;
    HighlighterPrefixCreateEditWidget highlighterPrefixCreateEditWidget;

    private Tab currentTab = Tab.PLAYERS;

    public HighlightScreen() {
        super(Text.literal("Highlighter"));
        this.guiDelegate = new ScreenNestedGuiDelegate();
    }

    // Внутренний класс, который расширяет NestedGuiGroup и делегирует ему управление вложенными элементами
    private class ScreenNestedGuiDelegate extends NestedGuiGroup {
        private boolean focused = false;

        @Override
        protected List<? extends Element> getGuiChildren() {
            return HighlightScreen.this.getGuiChildren();
        }

        @Override
        public boolean isPointInside(double x, double y) {
            return true; // Экран всегда получает клики
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean isFocused() {
            return this.focused;
        }

        @Override
        public SelectionType getType() {
            return SelectionType.NONE; // Делегат сам по себе не является выбираемым элементом
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {
            // Делегат не имеет собственной нарративной информации
            // Каждый элемент в getGuiChildren() уже добавляет свои собственные нарративные сообщения
            builder.put(NarrationPart.TITLE, Text.literal("Highlighter Screen"));
        }
    }

    // Возвращает все элементы интерфейса, которым делегируются события
    protected List<? extends Element> getGuiChildren() {
        List<Element> children = new ArrayList<>();

        // Добавляем общие элементы (всегда доступны)
        children.add(searchBox);
        children.add(playersTabButton);
        children.add(prefixesTabButton);

        // Добавляем элементы в зависимости от текущей вкладки
        if (currentTab == Tab.PLAYERS) {
            children.add(playerList);
            children.add(highlighterPlayerEditWidget);
        } else if (currentTab == Tab.PREFIXES) {
            children.add(prefixList);
            children.add(highlighterPrefixCreateEditWidget);
            children.add(createPrefixButton);
        }

        return children;
    }

    /**
     * Возвращает элементы, которые могут получать фокус в текущей вкладке
     */
    @Override
    public List<Element> getFocusableElements() {
        return guiDelegate.getFocusableElements();
    }

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
    protected void init() {
        super.init();

        // --- playerList ---
        this.playerList = new HighlighterPlayerListWidget(this, this.client, SCREEN_WIDTH, this.getPlayerListBottom()-88, 90, 35);
        this.playerList.setPosition(getScreenStartX(), 90);

        // --- prefixList ---
        this.prefixList = new HighlighterPrefixListWidget(this, this.client, SCREEN_WIDTH, this.getPlayerListBottom()-120, 90, 35);
        this.prefixList.setPosition(getScreenStartX(), 90);
        this.createPrefixButton = ButtonWidget.builder(Text.literal("Создать префикс"), (button) -> {
            this.highlighterPrefixCreateEditWidget.setPrefix(null);
        }).build();
        createPrefixButton.setPosition(getScreenStartX() + 13, getPlayerListBottom()-25);
        createPrefixButton.setWidth(210);

        // --- Tabs button ---
        int buttonsWidth = 236/2-2;
        this.playersTabButton = this.addDrawableChild(ButtonWidget.builder(PLAYERS_TAB_TITLE, (button) -> this.setCurrentTab(Tab.PLAYERS)).dimensions(getScreenStartX()+1, 42, buttonsWidth, 20).build());
        this.prefixesTabButton = this.addDrawableChild(ButtonWidget.builder(PREFIXES_TAB_TITLE, (button) -> this.setCurrentTab(Tab.PREFIXES)).dimensions(getScreenStartX()+1+buttonsWidth, 42, buttonsWidth, 20).build());

        // --- searchBox ---
        String string = this.searchBox != null ? this.searchBox.getText() : "";
        this.searchBox = new TextFieldWidget(this.textRenderer, this.getScreenStartX() + 26, 74, 200, 15, SEARCH_TEXT);
        this.searchBox.setMaxLength(16);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(-1);
        this.searchBox.setText(string);
        this.searchBox.setPlaceholder(SEARCH_TEXT);
        this.searchBox.setChangedListener(this::onSearchChange);
        this.addDrawableChild(this.searchBox);
        int i1 = Math.max(100, this.height - 600);

        // --- highlighterPlayerEditWidget ---
        this.highlighterPlayerEditWidget = new HighlighterPlayerEditWidget((int) Math.max(Math.abs(this.width*0.1), 50), (this.height/2)-(i1/2), SCREEN_WIDTH, i1);
        PlayerListEntry playerListEntry = this.client.player.networkHandler.getPlayerListEntry(this.client.player.getUuid());
        if (playerListEntry != null) {
            this.highlighterPlayerEditWidget.setHighlightPlayer(new HighlightPlayer(playerListEntry.getProfile().getId(), playerListEntry.getProfile().getName(), playerListEntry.getSkinTextures()));
        }

        this.highlighterPrefixCreateEditWidget = new HighlighterPrefixCreateEditWidget((int) Math.max(Math.abs(this.width*0.1), 50), (this.height/2)-(i1/2), SCREEN_WIDTH, this.height-300,this);

        // Зарегистрируем экран в FocusManager
        FocusManager.getInstance().registerContainer(this);

        // --- setup ---
        setCurrentTab(Tab.PLAYERS);
        this.updateLayout();
    }

    public void updatePrefixes(){
        PrefixStorage.read();
        Collection<Prefix> prefixes = HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefixes();
        this.prefixList.update(prefixes, this.prefixList.getScrollAmount());
        this.prefixList.setCurrentSearch(this.currentSearch);
        this.setCurrentTab(Tab.PREFIXES);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Используем делегирование для проверки кликов по всем вложенным элементам
        if (guiDelegate.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Если клик не был обработан, снимаем фокус
        FocusManager.getInstance().clearFocus();

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // Используем делегирование для проверки перетаскивания по всем вложенным элементам
        if (guiDelegate.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        this.updateLayout();
    }

    private void updateLayout() {
        int screenStartX = getScreenStartX();

        // --- playerList ---
        int playerListHeight = this.getPlayerListBottom() - 88;
        this.playerList.setPosition(screenStartX, 90);
        this.playerList.setWidth(SCREEN_WIDTH);
        this.playerList.setHeight(playerListHeight);

        // --- prefixList ---
        int prefixListHeight = this.getPlayerListBottom() - 120;
        this.prefixList.setPosition(screenStartX, 90);
        this.prefixList.setWidth(SCREEN_WIDTH);
        this.prefixList.setHeight(prefixListHeight);
        this.createPrefixButton.setPosition(screenStartX + 13, getPlayerListBottom()-25);

        // --- searchBox ---
        this.searchBox.setX(screenStartX + 26);
        this.searchBox.setY(74);
        this.searchBox.setWidth(200);

        // --- highlighterPlayerEditWidget  ---
        int editWidgetHeight = Math.max(100, 0);
        int editWidgetX = 64;
        int editWidgetY = (this.height / 2) - (editWidgetHeight / 2);
        this.highlighterPlayerEditWidget.setX(editWidgetX);
        this.highlighterPlayerEditWidget.setY(editWidgetY);
        this.highlighterPlayerEditWidget.setWidth(SCREEN_WIDTH);
        this.highlighterPlayerEditWidget.setHeight(editWidgetHeight);

        // --- highlighterPrefixCreateEditWidget  ---
        int editPrefixWidgetHeight = Math.max(225, 0);
        int editPrefixWidgetX = 64;
        int editPrefixWidgetY = (this.height / 2) - (editPrefixWidgetHeight / 2);
        this.highlighterPrefixCreateEditWidget.setX(editPrefixWidgetX);
        this.highlighterPrefixCreateEditWidget.setY(editPrefixWidgetY);
        this.highlighterPrefixCreateEditWidget.setWidth(SCREEN_WIDTH);
        this.highlighterPrefixCreateEditWidget.setHeight(editPrefixWidgetHeight);

        // --- Tabs button ---
        int buttonsWidth = SCREEN_WIDTH / 2 - 2;
        this.playersTabButton.setX(screenStartX + 1);
        this.prefixesTabButton.setX(screenStartX + 1 + buttonsWidth);
        this.playersTabButton.setWidth(buttonsWidth);
        this.prefixesTabButton.setWidth(buttonsWidth);
    }

    public void setCurrentPlayer(@Nullable HighlightPlayer player) {
        this.highlighterPlayerEditWidget.setHighlightPlayer(player);
    }
    public void setCurrentPrefix(@Nullable Prefix prefix) {
        this.highlighterPrefixCreateEditWidget.setPrefix(prefix);
    }

    /**
     * Проверяет, находится ли точка внутри контейнера
     * @param x Координата X
     * @param y Координата Y
     * @return true, если точка внутри контейнера
     */
    @Override
    public boolean isPointInside(double x, double y) {
        return true; // Экран всегда получает клики
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 16, 16777215);

        if (currentTab == Tab.PLAYERS) {
            if (!this.playerList.isEmpty()) {
                this.playerList.render(context, mouseX, mouseY, delta);
            } else {
                context.drawCenteredTextWithShadow(this.textRenderer, EMPTY_SEARCH_TEXT, this.getScreenStartX() + SCREEN_WIDTH / 2, 90 + this.getScreenHeight() / 2 - 4, Formatting.GRAY.getColorValue());
            }
            this.highlighterPlayerEditWidget.render(context, mouseX, mouseY, delta);
        } else if (currentTab == Tab.PREFIXES) {
            if (!this.prefixList.isEmpty()) {
                this.prefixList.render(context, mouseX, mouseY, delta);
            } else {
                context.drawCenteredTextWithShadow(this.textRenderer, EMPTY_SEARCH_TEXT, this.getScreenStartX() + SCREEN_WIDTH / 2, 90 + this.getScreenHeight() / 2 - 4, Formatting.GRAY.getColorValue());
            }
            this.highlighterPrefixCreateEditWidget.render(context, mouseX, mouseY, delta);
            this.createPrefixButton.render(context, mouseX, mouseY, delta);
        }

        this.searchBox.render(context, mouseX, mouseY, delta);
        this.playersTabButton.render(context, mouseX, mouseY, delta);
        this.prefixesTabButton.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Используем делегирование для проверки прокрутки по всем вложенным элементам
        if (guiDelegate.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Обработка Tab для циклического переключения между полями
        if (keyCode == 258) { // Tab key
            return FocusManager.getInstance().cycleFocus(hasShiftDown());
        }

        // Проверяем нажатие клавиши закрытия
        if (!this.searchBox.isFocused() && this.client.options.socialInteractionsKey.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }

        // Получаем текущий элемент с фокусом
        Element focusedElement = FocusManager.getInstance().getFocusedElement();

        // Если фокус на поле поиска или другом элементе, передаем ему обработку клавиш
        if (focusedElement != null && focusedElement.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // Получаем текущий элемент с фокусом
        Element focusedElement = FocusManager.getInstance().getFocusedElement();

        // Если есть элемент с фокусом, передаем ему ввод символа
        if (focusedElement != null && focusedElement.charTyped(codePoint, modifiers)) {
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.drawGuiTexture(BACKGROUND_TEXTURE, getScreenStartX(), 64, SCREEN_WIDTH, this.getScreenHeight() + 16);
        context.drawGuiTexture(SEARCH_ICON_TEXTURE, getScreenStartX() + 10, 76, 12, 12);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.searchBox);
    }

    private void setCurrentTab(Tab tab) {
        // Сбрасываем фокус при смене вкладки
        FocusManager.getInstance().clearFocus();

        this.currentTab = tab;
        playersTabButton.setMessage(PLAYERS_TAB_TITLE);
        prefixesTabButton.setMessage(PREFIXES_TAB_TITLE);

        // Закрываем цветовые пикеры при переключении вкладок
        if (highlighterPrefixCreateEditWidget != null) {
            highlighterPrefixCreateEditWidget.clearColorPickers();
        }
    
        // Обновляем сообщения на кнопках вкладок
        switch (tab) {
            case PLAYERS -> {
                playersTabButton.setMessage(SELECTED_PLAYERS_TAB_TITLE);
                Collection<UUID> collection = this.client.player.networkHandler.getPlayerUuids();
                playerList.update(collection, this.playerList.getScrollAmount(), false);
                // Обновляем контейнер фокуса для активной вкладки
                FocusManager.getInstance().setActiveContainer(this);
            }
            case PREFIXES -> {
                prefixesTabButton.setMessage(SELECTED_PREFIXES_TAB_TITLE);
                Collection<Prefix> prefixes = HighlighterClient.STORAGE_MANAGER.getPrefixStorage().getPrefixes();
                prefixList.update(prefixes, this.prefixList.getScrollAmount());
                // Обновляем контейнер фокуса для активной вкладки
                FocusManager.getInstance().setActiveContainer(this);
            }
        }
    }

    private void onSearchChange(String currentSearch) {
        currentSearch = currentSearch.toLowerCase(Locale.ROOT);
        if (!currentSearch.equals(this.currentSearch)) {
            this.currentSearch = currentSearch;
            if (currentTab == Tab.PLAYERS) {
                this.playerList.setCurrentSearch(currentSearch);
            } else if (currentTab == Tab.PREFIXES) {
                this.prefixList.setCurrentSearch(currentSearch);
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

