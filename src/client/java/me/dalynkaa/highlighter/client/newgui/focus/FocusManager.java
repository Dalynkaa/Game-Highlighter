package me.dalynkaa.highlighter.client.newgui.focus;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.adapters.GuiAdapter;
import me.dalynkaa.highlighter.client.newgui.widgets.HighlighterDropdownWidget;
import me.dalynkaa.highlighter.client.newgui.widgets.helper.FocusableElement;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Менеджер фокуса для управления фокусом виджетов в иерархии GUI.
 * Заменяет встроенный механизм фокуса Minecraft для предотвращения конфликтов.
 */
public class FocusManager {
    // Singleton инстанс
    private static FocusManager instance;

    // Текущий элемент с фокусом
    private Element focusedElement;

    // Флаг, чтобы предотвратить рекурсивные вызовы
    private boolean isSettingFocus = false;

    // Список всех элементов, которые могут получать фокус в текущем экране
    private final List<Element> focusableElements = new ArrayList<>();

    // Родительский элемент текущего фокусированного элемента
    private FocusableContainer currentContainer;

    private FocusManager() {
        // Приватный конструктор для Singleton
    }

    /**
     * Получить экземпляр менеджера фокуса
     */
    public static FocusManager getInstance() {
        if (instance == null) {
            instance = new FocusManager();
        }
        return instance;
    }

    /**
     * Регистрирует контейнер с элементами, которые могут получать фокус
     */
    public void registerContainer(FocusableContainer container) {
        if (container == null) return;

        // Если это первый или текущий контейнер, делаем его активным
        if (currentContainer == null) {
            currentContainer = container;
            focusableElements.clear();
            focusableElements.addAll(container.getFocusableElements());
        }
    }

    /**
     * Устанавливает активный контейнер
     */
    public void setActiveContainer(FocusableContainer container) {
        if (container == currentContainer) return;

        clearFocus();
        currentContainer = container;
        focusableElements.clear();

        if (container != null) {
            focusableElements.addAll(container.getFocusableElements());
        }
    }

    /**
     * Устанавливает фокус на элемент
     */
    public void setFocus(Element element) {
        // Предотвращаем рекурсивные вызовы
        if (isSettingFocus || element == focusedElement) {
            return;
        }

        isSettingFocus = true;

        try {
            // Снимаем фокус с текущего элемента
            if (focusedElement != null && focusedElement != element) {
                if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.NORMAL.ordinal()) {
                    GuiAdapter.debugFocusChange(focusedElement, false);
                }

                if (focusedElement instanceof TextFieldWidget field) {
                    field.setFocused(false);
                } else if (focusedElement instanceof ClickableWidget widget) {
                    widget.setFocused(false);
                }
            }

            // Устанавливаем новый фокус
            focusedElement = element;

            if (element != null) {
                if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.MINIMAL.ordinal()) {
                    GuiAdapter.debugFocusChange(element, true);
                }

                if (element instanceof TextFieldWidget field) {
                    field.setFocused(true);
                } else if (element instanceof ClickableWidget widget) {
                    widget.setFocused(true);
                }

                // Убеждаемся, что все остальные элементы контейнера не имеют фокуса
                if (currentContainer != null) {
                    for (Element other : currentContainer.getFocusableElements()) {
                        if (other != element && other instanceof TextFieldWidget otherField) {
                            otherField.setFocused(false);
                        }
                    }
                }
            }
        } finally {
            isSettingFocus = false;
        }
    }

    /**
     * Очищает фокус со всех элементов
     */
    public void clearFocus() {
        setFocus(null);
    }

    /**
     * Получить текущий элемент с фокусом
     */
    public Element getFocusedElement() {
        return focusedElement;
    }

    /**
     * Обработка клика для смены фокуса
     */
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (currentContainer == null) return false;

        List<Element> elements = currentContainer.getFocusableElements();
        boolean handled = false;

        // Проверяем клик по элементам контейнера
        for (Element element : elements) {
            if (isClickableAt(element, mouseX, mouseY)) {
                // Если это вложенный контейнер, сначала пытаемся обработать клик внутри него
                if (element instanceof FocusableContainer nestedContainer) {
                    boolean nestedHandled = handleNestedContainerClick(nestedContainer, mouseX, mouseY, button);
                    if (nestedHandled) {
                        return true;
                    }
                }

                // Если это кнопка или другой кликабельный виджет (не поле ввода),
                // сначала дадим ему обработать клик, прежде чем менять фокус
                if (element instanceof net.minecraft.client.gui.widget.ButtonWidget ||
                    (element instanceof ClickableWidget && !(element instanceof TextFieldWidget))) {

                    if (element instanceof ClickableWidget widget) {
                        // Кнопка обрабатывает клик по-своему
                        if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.VERBOSE.ordinal()) {
                            Highlighter.LOGGER.info("Делегирование клика элементу: {}", element.getClass().getSimpleName());
                        }

                        // Даем элементу обработать клик и запоминаем результат
                        handled = widget.mouseClicked(mouseX, mouseY, button);
                    }
                }

                // Установим фокус на элемент, если клик не был обработан кнопкой или это текстовое поле
                if (!handled || element instanceof TextFieldWidget) {
                    setFocus(element);
                    handled = true;
                }

                break;
            }
        }

        // Если клик был не по фокусируемым элементам, очищаем фокус
        if (!handled && isClickInContainer(currentContainer, mouseX, mouseY)) {
            clearFocus();
            handled = true;
        }

        return handled;
    }

    /**
     * Обрабатывает клик внутри вложенного контейнера
     */
    private boolean handleNestedContainerClick(FocusableContainer container, double mouseX, double mouseY, int button) {
        return handleNestedContainerClick(container, mouseX, mouseY, button, new HashSet<>());
    }

    /**
     * Обрабатывает клик внутри вложенного контейнера с защитой от циклических вложений
     */
    private boolean handleNestedContainerClick(FocusableContainer container, double mouseX, double mouseY, int button,
                                              Set<FocusableContainer> visitedContainers) {
        if (container == null) return false;

        // Проверка на циклическую рекурсию
        if (visitedContainers.contains(container)) {
            return false;
        }

        // Добавляем текущий контейнер в набор посещенных
        visitedContainers.add(container);

        // Проверяем, находится ли клик внутри контейнера
        if (!container.isPointInside(mouseX, mouseY)) {
            return false;
        }

        // Получаем элементы, доступные для фокуса внутри вложенного контейнера
        List<Element> nestedElements = container.getFocusableElements();

        // Сначала проверяем клики по самым верхним специальным виджетам (например, раскрытые выпадающие списки)
        for (Element nestedElement : nestedElements) {
            // Специальная обработка для выпадающего списка
            if (nestedElement instanceof HighlighterDropdownWidget dropdown) {
                if (dropdown.isMouseOver(mouseX, mouseY)) {
                    if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.VERBOSE.ordinal()) {
                        Highlighter.LOGGER.info("Обработка клика по выпадающему списку: {}", dropdown.getClass().getSimpleName());
                    }
                    boolean handled = dropdown.mouseClicked(mouseX, mouseY, button);
                    if (handled) {
                        setFocus(dropdown);
                        return true;
                    }
                }
            }
        }

        // Затем проверяем клик по вложенным контейнерам
        for (Element nestedElement : nestedElements) {
            if (nestedElement instanceof FocusableContainer nestedContainer) {
                if (nestedContainer.isPointInside(mouseX, mouseY)) {
                    if (handleNestedContainerClick(nestedContainer, mouseX, mouseY, button, visitedContainers)) {
                        return true;
                    }
                }
            }
        }

        // Затем проверяем клики по простым виджетам
        for (Element nestedElement : nestedElements) {
            // Проверяем, является ли элемент кликабельным виджетом и находится ли точка над ним
            if (nestedElement instanceof ClickableWidget widget &&
                widget.isMouseOver(mouseX, mouseY)) {

                // Обработка выпадающих списков (они могут иметь специальный интерфейс)
                if (widget.getClass().getSimpleName().contains("Dropdown") ||
                    widget.getClass().getSimpleName().contains("SelectWidget") ||
                    widget.getClass().getSimpleName().contains("ComboBox")) {

                    setFocus(widget);
                    boolean handled = widget.mouseClicked(mouseX, mouseY, button);
                    if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.VERBOSE.ordinal()) {
                        Highlighter.LOGGER.info("Обработан клик по выпадающему списку: {}", widget.getClass().getSimpleName());
                    }
                    return true; // Всегда считаем обработанным, так как это специальный элемент
                }
                // Если это поле ввода, устанавливаем на него фокус
                else if (widget instanceof TextFieldWidget) {
                    setFocus(widget);
                    boolean handled = widget.mouseClicked(mouseX, mouseY, button);
                    if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.VERBOSE.ordinal()) {
                        Highlighter.LOGGER.info("Обработан клик по текстовому полю: {}", widget.getClass().getSimpleName());
                    }
                    return true;
                }
                // Если это кнопка, даем ей обработать клик
                else if (widget instanceof ButtonWidget) {
                    boolean handled = widget.mouseClicked(mouseX, mouseY, button);
                    if (handled) {
                        return true;
                    }
                }
                // Обрабатываем любой другой кликабельный виджет
                else {
                    setFocus(widget);
                    boolean handled = widget.mouseClicked(mouseX, mouseY, button);
                    if (handled) {
                        if (GuiAdapter.DEBUG_LEVEL.ordinal() >= GuiAdapter.DebugLevel.VERBOSE.ordinal()) {
                            Highlighter.LOGGER.info("Обработан клик по виджету: {}", widget.getClass().getSimpleName());
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Проверяет, находится ли элемент в точке клика
     * с учетом расширенной области для FocusableElement
     */
    private boolean isClickableAt(Element element, double mouseX, double mouseY) {
        // Для FocusableElement используем расширенную проверку области клика
        if (element instanceof FocusableElement focusableElement) {
            return focusableElement.isExtendedHitbox(mouseX, mouseY);
        }
        // Для обычных виджетов используем стандартную логику
        else if (element instanceof ClickableWidget widget) {
            return widget.isMouseOver(mouseX, mouseY);
        } else if (element instanceof FocusableContainer container) {
            return container.isPointInside(mouseX, mouseY);
        }
        return false;
    }

    /**
     * Проверяет, находится ли клик внутри контейнера
     */
    private boolean isClickInContainer(FocusableContainer container, double mouseX, double mouseY) {
        if (container == null) return false;
        return container.isPointInside(mouseX, mouseY);
    }

    /**
     * Переключает фокус на следующий элемент в последовательности
     * @param reverse true для обратного направления (Shift+Tab)
     * @return true если фокус был переключен
     */
    public boolean cycleFocus(boolean reverse) {
        if (currentContainer == null || focusableElements.isEmpty()) {
            return false;
        }

        // Находим текущий индекс элемента с фокусом
        int currentIndex = -1;
        if (focusedElement != null) {
            currentIndex = focusableElements.indexOf(focusedElement);
        }

        // Рассчитываем индекс следующего элемента
        int nextIndex;
        if (reverse) {
            // Движемся назад с учетом кольцевого перехода
            nextIndex = (currentIndex - 1 + focusableElements.size()) % focusableElements.size();
        } else {
            // Движемся вперед с учетом кольцевого перехода
            nextIndex = (currentIndex + 1) % focusableElements.size();
        }

        // Устанавливаем фокус на следующий элемент
        Element nextElement = focusableElements.get(nextIndex);

        // Проверяем, является ли элемент видимым и активным
        boolean isActive = true;
        if (nextElement instanceof FocusableElement focusElement) {
            isActive = focusElement.canReceiveFocus();
        } else if (nextElement instanceof ClickableWidget widget) {
            isActive = widget.visible && widget.active;
        }

        if (isActive) {
            setFocus(nextElement);
            return true;
        } else {
            // Рекурсивно вызываем метод для пропуска неактивных элементов
            // Но ограничиваем глубину рекурсии, чтобы избежать зацикливания
            return cycleFocusInternal(reverse, 1);
        }
    }

    /**
     * Переключает фокус на следующий элемент с контролем глубины рекурсии
     * @param reverse true для обратного направления (Shift+Tab)
     * @param depth текущая глубина рекурсии для предотвращения бесконечного цикла
     * @return true если фокус был переключен
     */
    private boolean cycleFocusInternal(boolean reverse, int depth) {
        // Предотвращаем слишком глубокую рекурсию
        if (depth >= focusableElements.size()) {
            return false;
        }

        if (currentContainer == null || focusableElements.isEmpty()) {
            return false;
        }

        // Находим текущий индекс элемента с фокусом
        int currentIndex = -1;
        if (focusedElement != null) {
            currentIndex = focusableElements.indexOf(focusedElement);
        }

        // Рассчитываем индекс следующего элемента
        int nextIndex;
        if (reverse) {
            nextIndex = (currentIndex - 1 + focusableElements.size()) % focusableElements.size();
        } else {
            nextIndex = (currentIndex + 1) % focusableElements.size();
        }

        // Получаем следующий элемент
        Element nextElement = focusableElements.get(nextIndex);

        // Проверяем, является ли элемент видимым и активным
        boolean isActive = true;
        if (nextElement instanceof FocusableElement focusElement) {
            isActive = focusElement.canReceiveFocus();
        } else if (nextElement instanceof ClickableWidget widget) {
            isActive = widget.visible && widget.active;
        }

        if (isActive) {
            setFocus(nextElement);
            return true;
        } else {
            // Продолжаем рекурсивный поиск следующего активного элемента
            // с увеличением глубины рекурсии
            return cycleFocusInternal(reverse, depth + 1);
        }
    }
}
