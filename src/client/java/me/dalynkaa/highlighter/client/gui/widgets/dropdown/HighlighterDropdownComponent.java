package me.dalynkaa.highlighter.client.gui.widgets.dropdown;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.UISounds;
import me.dalynkaa.highlighter.client.adapters.GuiAdapter;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class HighlighterDropdownComponent extends FlowLayout {
    protected static final Identifier ICONS_TEXTURE = Identifier.of("owo", "textures/gui/dropdown_icons.png");
    public final FlowLayout entries;
    protected boolean closeWhenNotHovered = false;

    protected HighlighterDropdownComponent(Sizing horizontalSizing) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);

        this.entries = Containers.verticalFlow(horizontalSizing, Sizing.content());
        this.entries.padding(Insets.of(1));
        this.entries.allowOverflow(true);
        this.entries.surface(Surface.flat(0xC7000000).and(Surface.blur(3, 5)).and(Surface.outline(0xFF121212)));

        this.child(this.entries);
    }

    /**
     * Open a context menu at the given location in the given screen,
     * adjusting the position if needed to avoid overflowing screen space
     *
     * @param screen        The screen on which to operate
     * @param rootComponent The root component onto which to mount the dropdown
     * @param mountFunction The mounting function to use
     * @param mouseX        The x-coordinate at which to open the dropdown
     * @param mouseY        The y-coordinate at which to open the dropdown
     * @param builder       A function to add entries to the dropdown
     */
    public static <R extends ParentComponent> HighlighterDropdownComponent openContextMenu(Screen screen, R rootComponent, BiConsumer<R, HighlighterDropdownComponent> mountFunction, double mouseX, double mouseY, Consumer<HighlighterDropdownComponent> builder) {
        var dropdown = new HighlighterDropdownComponent(Sizing.content());
        builder.accept(dropdown);
        ParentComponent topLevelParent = findTopLevelParent(rootComponent);
        if (topLevelParent != null) {
            mountFunction.accept((R) topLevelParent, dropdown);
        } else {
            mountFunction.accept(rootComponent, dropdown);
        }

        int xLocation = (int) mouseX - rootComponent.x();
        int yLocation = (int) mouseY - rootComponent.y();

        // Горизонтальное выравнивание: если дропдаун выходит за пределы экрана справа
        if (xLocation + dropdown.width() > screen.width) {
            xLocation -= xLocation + dropdown.width() - screen.width;
        }

        // Вертикальное выравнивание: всегда размещаем сверху относительно точки клика
        // Если не помещается сверху, пытаемся показать снизу
        if (yLocation - dropdown.height() >= 0) {
            // Достаточно места сверху - размещаем над точкой клика
            yLocation -= dropdown.height();
        } else if (yLocation + dropdown.height() > screen.height) {
            // Не помещается ни сверху, ни снизу - размещаем так, чтобы поместилось на экране
            yLocation = Math.max(0, screen.height - dropdown.height());
        }

        dropdown.positioning(Positioning.absolute(xLocation, yLocation));

        // Установка очень высокого zIndex для отображения на переднем плане поверх всех элементов
        dropdown.zIndex(3000);

        // Явно разрешаем переполнение для дропдауна и его элементов
        dropdown.allowOverflow(true);
        dropdown.entries.allowOverflow(true);

        var dismounted = new MutableBoolean(false);
        ScreenMouseEvents.beforeMouseClick(screen).register((screen_, mouseX_, mouseY_, button) -> {
            if (dismounted.isTrue() || dropdown.isInBoundingBox(mouseX_, mouseY_)) return;

            if (topLevelParent != null) {
                topLevelParent.removeChild(dropdown);
            } else {
                rootComponent.removeChild(dropdown);
            }
            dismounted.setTrue();
        });

        return dropdown;
    }

    /**
     * Находит корневой компонент в иерархии UI
     * @param component Начальный компонент
     * @return Корневой компонент или null, если не найден
     */
    private static ParentComponent findTopLevelParent(ParentComponent component) {
        ParentComponent current = component;
        ParentComponent parent = component.parent();

        // Ищем самый верхний компонент в иерархии
        while (parent != null) {
            current = parent;
            parent = parent.parent();
        }

        return current;
    }

    protected static void drawIconFromTexture(OwoUIDrawContext context, ParentComponent dropdown, int y, int u, int v) {
        GuiAdapter.drawTexture(context,ICONS_TEXTURE,
                dropdown.x() + dropdown.width() - dropdown.padding().get().right() - 10, y,
                u, v,
                9, 9,
                32, 32);
    }

    @Override
    public ParentComponent surface(Surface surface) {
        return this.entries.surface(surface);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        if (this.closeWhenNotHovered && !this.isInBoundingBox(mouseX, mouseY)) {
            this.queue(() -> {
                this.closeWhenNotHovered(false);
                if (this.parent != null) {
                    this.parent.removeChild(this);
                }
            });
        }
    }

    @Override
    public void layout(Size space) {
        super.layout(space);

        var entries = this.entries.children();
        for (Component entry : entries) {
            if (!(entry instanceof ResizeableComponent sizeable)) continue;

            sizeable.setWidth(this.entries.width() - this.entries.padding().get().horizontal() - entry.margins().get().horizontal());
        }
    }

    public HighlighterDropdownComponent divider() {
        this.entries.child(new Divider());
        return this;
    }

    public HighlighterDropdownComponent text(Text text) {
        this.entries.child(Components.label(text).color(Color.ofFormatting(Formatting.GRAY)).margins(Insets.of(2)));
        return this;
    }

    public HighlighterDropdownComponent button(Text text, Consumer<HighlighterDropdownComponent> onClick) {
        this.entries.child(new Button(this, text, onClick).margins(Insets.of(2)));
        return this;
    }

    public HighlighterDropdownComponent checkbox(Text text, boolean state, Consumer<Boolean> onClick) {
        this.entries.child(new Checkbox(this, text, state, onClick).margins(Insets.of(2)));
        return this;
    }

    public HighlighterDropdownComponent nested(Text text, Sizing horizontalSizing, Consumer<HighlighterDropdownComponent> builder) {
        var nested = new HighlighterDropdownComponent(horizontalSizing);
        builder.accept(nested);
        this.entries.child(new NestEntry(this, text, nested).margins(Insets.of(2)));
        return this;
    }

    @Override
    public FlowLayout removeChild(Component child) {
        if (child == this.entries) {
            this.queue(() -> {
                this.closeWhenNotHovered(false);
                if (this.parent != null) {
                    this.parent.removeChild(this);
                }
            });
        }
        return super.removeChild(child);
    }

    public HighlighterDropdownComponent closeWhenNotHovered(boolean closeWhenNotHovered) {
        this.closeWhenNotHovered = closeWhenNotHovered;
        return this;
    }

    public boolean closeWhenNotHovered() {
        return this.closeWhenNotHovered;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "entries", Function.identity(), this::parseAndApplyEntries);
        UIParsing.apply(children, "close-when-not-hovered", UIParsing::parseBool, this::closeWhenNotHovered);
    }

    protected void parseAndApplyEntries(Element container) {
        for (var node : UIParsing.allChildrenOfType(container, Node.ELEMENT_NODE)) {
            var entry = (Element) node;

            switch (entry.getNodeName()) {
                case "divider" -> this.divider();
                case "text" -> this.text(UIParsing.parseText(entry));
                case "button" -> {
                    var children = UIParsing.childElements(entry);
                    UIParsing.expectChildren(entry, children, "text");

                    var text = UIParsing.parseText(children.get("text"));
                    this.button(text, dropdownComponent -> {
                    });
                }
                case "checkbox" -> {
                    var children = UIParsing.childElements(entry);
                    UIParsing.expectChildren(entry, children, "text", "checked");

                    var text = UIParsing.parseText(children.get("text"));
                    var checked = UIParsing.parseBool(children.get("checked"));

                    this.checkbox(text, checked, aBoolean -> {
                    });
                }
                case "nested" -> {
                    var text = entry.getAttribute("translate").equals("true")
                            ? Text.translatable(entry.getAttribute("name"))
                            : Text.literal(entry.getAttribute("name")).setStyle(net.minecraft.text.Style.EMPTY);
                    this.nested(text, Sizing.content(), dropdownComponent -> dropdownComponent.parseAndApplyEntries(entry));
                }
            }
        }
    }

    protected interface ResizeableComponent {
        void setWidth(int width);
    }

    protected static class Divider extends BaseComponent implements ResizeableComponent {

        public Divider() {
            this.sizing(Sizing.fixed(1));
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            var margins = this.margins.get();
            context.fill(
                    this.x - margins.left(),
                    this.y - margins.top(),
                    this.x + this.width + margins.right(),
                    this.y + this.height + margins.bottom(),
                    0xFF121212
            );
        }

        @Override
        public void setWidth(int width) {
            this.width = width;
        }
    }

    protected static class NestEntry extends LabelComponent {

        private final HighlighterDropdownComponent child;

        protected NestEntry(HighlighterDropdownComponent parentDropdown, Text text, HighlighterDropdownComponent child) {
            super(text);
            this.child = child;

            this.mouseEnter().subscribe(() -> {
                child.margins(Insets.top(this.y - parentDropdown.y));

                parentDropdown.queue(() -> {
                    parentDropdown.removeChild(child);
                    parentDropdown.child(child);
                });
            });
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
            if (this.parent != null) {
                drawIconFromTexture(context, this.parent, this.y, 0, 16);
            }

            this.child.closeWhenNotHovered(!PositionedRectangle.of(this.x, this.y, this.parent.width(), this.height).isInBoundingBox(mouseX, mouseY));
        }

        @Override
        protected int determineHorizontalContentSize(Sizing sizing) {
            return super.determineHorizontalContentSize(sizing) + 17;
        }
    }

    protected static class Button extends LabelComponent implements ResizeableComponent {

        protected final int HIGHLIGHT_COLOR = 0x22FFFFFF;
        protected final HighlighterDropdownComponent parentDropdown;
        protected Consumer<HighlighterDropdownComponent> onClick;

        protected Button(HighlighterDropdownComponent parentDropdown, Text text, Consumer<HighlighterDropdownComponent> onClick) {
            super(text);
            this.onClick = onClick;
            this.parentDropdown = parentDropdown;

            this.margins(Insets.vertical(1));
            this.cursorStyle(CursorStyle.HAND);
        }

        public void setWidth(int width) {
            this.width = width;
        }

        @Override
        public boolean onMouseDown(double mouseX, double mouseY, int button) {
            //super.onMouseDown(mouseX, mouseY, button);

            this.onClick.accept(this.parentDropdown);
            this.playInteractionSound();

            return true;
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            if (this.isInBoundingBox(mouseX, mouseY)) {
                var margins = this.margins.get();
                context.fill(
                        this.x - margins.left(),
                        this.y - margins.top(),
                        this.x + this.width + margins.right(),
                        this.y + this.height + margins.bottom(),
                        HIGHLIGHT_COLOR
                );
            }

            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }

        protected void playInteractionSound() {
            UISounds.playButtonSound();
        }
    }

    protected static class Checkbox extends Button {

        protected boolean state;

        public Checkbox(HighlighterDropdownComponent parentDropdown, Text text, boolean state, Consumer<Boolean> onClick) {
            super(parentDropdown, text, dropdownComponent -> {
            });

            this.state = state;
            this.onClick = dropdownComponent -> {
                this.state = !this.state;
                onClick.accept(this.state);
            };
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
            if (this.parent != null) {
                drawIconFromTexture(context, this.parent, this.y, this.state ? 16 : 0, 0);
            }
        }

        @Override
        protected int determineHorizontalContentSize(Sizing sizing) {
            return super.determineHorizontalContentSize(sizing) + 17;
        }

        @Override
        protected void playInteractionSound() {
            UISounds.playInteractionSound();
        }
    }
}
