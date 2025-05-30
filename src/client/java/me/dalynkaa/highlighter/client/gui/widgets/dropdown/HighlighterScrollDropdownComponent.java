package me.dalynkaa.highlighter.client.gui.widgets.dropdown;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class HighlighterScrollDropdownComponent extends FlowLayout {
    private Consumer<Boolean> expandStateChangeListener;
    private static final String EXPANDED_DROPDOWN_CHAR = "⏶ ";
    private static final String UNEXPANDED_DROPDOWN_CHAR = "⏷ ";
    public final HighlighterDropdownComponent expandableDropdown;
    protected final int TITLE_OUTLINE_COLOR = 0xffa0a0a0;
    protected final int OPTIONS_OUTLINE_COLOR = 0xff666666;
    protected final int BG_COLOR = 0xff000000;
    protected final Insets PADDING = Insets.of(5);
    protected final ScrollContainer<FlowLayout> expandableDropdownScroll;
    protected final FlowLayout contentLayout;
    protected final HighlighterDropdownComponent titleDropdown;
    protected final Text title;
    protected final LabelComponent arrowLabel;
    protected final LabelComponent titleLabel;
    protected boolean expanded;
    // Новые константы для улучшения скролла
    protected final int SCROLL_BAR_THICKNESS = 4;
    protected final int MAX_VISIBLE_ITEMS = 10;
    protected final int DEFAULT_ITEM_HEIGHT = 16;

    public HighlighterScrollDropdownComponent(Sizing horizontalSizing, Sizing verticalSizing, Text title, boolean expanded) {
        super(horizontalSizing, Sizing.content(), Algorithm.HORIZONTAL);

        this.title = title;
        this.expanded = expanded;

        contentLayout = Containers.verticalFlow(horizontalSizing, Sizing.content());

        expandableDropdown = new HighlighterDropdownComponent(horizontalSizing);
        expandableDropdown.surface(Surface.flat(BG_COLOR));
        expandableDropdown.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        expandableDropdown.margins(Insets.top(-2));
        expandableDropdown.padding(Insets.of(0));

        // Вычисляем высоту скролла для обеспечения оптимального отображения
        if (verticalSizing.isContent()) {
            expandableDropdownScroll = Containers.verticalScroll(horizontalSizing, Sizing.fixed(MAX_VISIBLE_ITEMS * DEFAULT_ITEM_HEIGHT), expandableDropdown);
        } else {
            expandableDropdownScroll = Containers.verticalScroll(horizontalSizing, verticalSizing, expandableDropdown);
        }


        titleDropdown = new HighlighterDropdownComponent(horizontalSizing);
        titleDropdown.zIndex(110);
        titleDropdown.surface(Surface.flat(BG_COLOR));
        titleDropdown.horizontalSizing(horizontalSizing);
        titleDropdown.button(Text.literal(title.getString() + "  "), (comp) -> {
            this.expanded = !this.expanded;
            updateExpandableDropdown();
        });

        FlowLayout dropdownLayout = ((FlowLayout) titleDropdown.children().get(0));

        Component child = dropdownLayout.children().get(dropdownLayout.children().size() - 1);
        titleLabel = (LabelComponent) child;
        child.margins(PADDING);
        dropdownLayout.removeChild(child);

        FlowLayout layout = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());

        if (horizontalSizing.isContent()) {
            layout = Containers.horizontalFlow(horizontalSizing, Sizing.content());
        }

        layout.child(child);
        child.horizontalSizing(horizontalSizing);
        layout.surface(Surface.outline(TITLE_OUTLINE_COLOR));

        dropdownLayout.child(layout);

        // Arrow
        arrowLabel = Components.label(Text.literal(""));
        arrowLabel.positioning(Positioning.relative(95, 50));
        arrowLabel.zIndex(110);
        titleDropdown.child(arrowLabel);

        contentLayout.child(titleDropdown);
        contentLayout.allowOverflow(true);
        contentLayout.zIndex(105);

        updateExpandableDropdown();

        super.child(contentLayout);
        super.allowOverflow(true);
    }

    public HighlighterScrollDropdownComponent title(Text text) {
        titleLabel.text(text);
        return this;
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        if (expanded) {
            // Проверяем, находится ли точка в области выпадающего списка
            double dropdownX = this.x + this.width / 2 - expandableDropdownScroll.width() / 2;
            double dropdownY = this.y + this.height;
            return (x >= dropdownX && x <= dropdownX + expandableDropdownScroll.width() &&
                   y >= dropdownY && y <= dropdownY + expandableDropdownScroll.height()) ||
                    (x >= this.x && x <= this.x + this.width &&
                    y >= this.y && y <= this.y + this.height);
        }else {
            // Если не развернут, проверяем только область заголовка
            return x >= this.x && x <= this.x + this.width &&
                   y >= this.y && y <= this.y + this.height;
        }
    }

    public void onExpandStateChanged(Consumer<Boolean> listener) {
        this.expandStateChangeListener = listener;
    }

    public HighlighterScrollDropdownComponent button(Text text, Consumer<HighlighterDropdownComponent> onClick) {
        expandableDropdown.button(text, dropdown -> {
            this.expanded(false);
            onClick.accept(dropdown);
        });

        FlowLayout dropdownLayout = ((FlowLayout) expandableDropdown.children().get(0));

        Component child = dropdownLayout.children().get(dropdownLayout.children().size() - 1);
        child.margins(PADDING);
        dropdownLayout.removeChild(child);

        FlowLayout layout = Containers.verticalFlow(Sizing.content(), Sizing.content());
        layout.child(child);
        layout.padding(Insets.of(1));
        layout.margins(Insets.top(-1));
        child.horizontalSizing(Sizing.fill(100));
        layout.surface(Surface.outline(OPTIONS_OUTLINE_COLOR));

        dropdownLayout.child(layout);

        return this;
    }

    public void removeEntries() {
        FlowLayout dropdownLayout = ((FlowLayout) expandableDropdown.children().get(0));
        dropdownLayout.clearChildren();
    }

    public boolean expanded() {
        return expanded;
    }

    public void expanded(boolean value) {
        expanded = value;
        updateExpandableDropdown();

        // Вызываем слушателя при изменении состояния
        if (expandStateChangeListener != null) {
            expandStateChangeListener.accept(value);
        }
    }

    protected void updateExpandableDropdown() {
        expandableDropdown.horizontalSizing(Sizing.fixed(this.width()));
        if (this.expandStateChangeListener !=null){
            this.expandStateChangeListener.accept(expanded);
        }
        if (!expanded) {
            contentLayout.removeChild(expandableDropdownScroll);
        } else {
            contentLayout.child(expandableDropdownScroll);
            expandableDropdownScroll.positioning(Positioning.layout());
        }
    }

    @Override
    protected void drawChildren(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta, List<? extends Component> children) {
        if (!expanded) {
            arrowLabel.text(Text.literal(UNEXPANDED_DROPDOWN_CHAR));
        } else {
            arrowLabel.text(Text.literal(EXPANDED_DROPDOWN_CHAR));
        }
        super.drawChildren(context, mouseX, mouseY, partialTicks, delta, children);
    }

    /**
     * Проверяет, должен ли компонент перехватывать все события ввода.
     * Возвращает true, когда dropdown активен (развернут), что позволяет
     * предотвратить обработку кликов по другим виджетам.
     *
     * @return true если dropdown активен и должен захватить все клики
     */
    public boolean shouldCaptureAllInputs() {
        return expanded;
    }

}
