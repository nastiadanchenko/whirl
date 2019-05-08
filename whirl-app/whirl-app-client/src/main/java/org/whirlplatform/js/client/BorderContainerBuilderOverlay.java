package org.whirlplatform.js.client;

import org.whirlplatform.component.client.ComponentBuilder;
import org.whirlplatform.component.client.base.BorderContainerBuilder;
import org.whirlplatform.meta.shared.component.PropertyType;
import org.whirlplatform.meta.shared.data.DataType;
import org.whirlplatform.meta.shared.data.DataValueImpl;


/**
 * Контейнер может содержать вложенные контейнеры, выровненные по его границам
 * (северный-NORTH, южный-SOUTH, западный-WEST, восточный-EAST)
 *
 */
public abstract class BorderContainerBuilderOverlay {

    public static BorderContainerBuilder create(BorderContainerBuilder instance) {
        instance.create();
        return instance;
    }

    /**
     * Устанавливает идентификатор элемента в DOM документа.
     *
     * @param domId
     */
    public abstract void setDomId(String domId);

    /**
     * Возвращает идентификатор элемента в DOM документа.
     *
     */
    public abstract String getDomId();

    public abstract void setCode(String name);

    public abstract String getCode();


    /**
     * Управляет доступностью контейнера и вложенных компонентов для действий пользователя.
     * В случае enabled = false компоненты остаются видимыми, но недоступными для действий пользователя
     */
    public abstract void setEnabled(boolean enabled);

    public abstract boolean isEnabled();

    /**
     * Метод позволяет управлять видимостью компонента
     * Скрытые компоненты не занимают места в DOM-дереве браузера
     *
     * @param hidden Скрыть компонент (hidden = true), либо показать(hidden = false)
     */
    public abstract void setHidden(boolean hidden);

    public abstract boolean isHidden();

    /**
     * Устанавливает значение DOM-атрибута компонента class = "styleName".
     * Пример: setStyleName("wide-container-class no-bordered") -> <div ... class="wide-container-class no-bordered" ... >...</div>
     *
     * @param styleName
     */
    public abstract void setStyleName(String styleName);

    /**
     * Устанавливает компонент(builder) в западную(слева) часть контейнера
     *
     * @param builder билдер компонента, который помещаем в западную часть BorderContainer
     */
    public static void setChildWest(BorderContainerBuilder instance,
                                    ComponentBuilder builder) {
        builder.setProperty(PropertyType.LayoutDataLocation.getCode(), new DataValueImpl(DataType.STRING, "West"));
        instance.addChild(builder);
    }


    /**
     * Устанавливает компонент(builder) в восточную(справа) часть контейнера
     *
     * @param builder билдер компонента, который помещаем в восточную часть BorderContainer
     */
    public static void setChildEast(BorderContainerBuilder instance,
                                    ComponentBuilder builder) {
        builder.setProperty(PropertyType.LayoutDataLocation.getCode(), new DataValueImpl(DataType.STRING, "East"));
        instance.addChild(builder);
    }

    /**
     * Устанавливает компонент(builder) в северную(верхнюю) часть контейнера
     *
     * @param builder билдер компонента, который помещаем в северную часть BorderContainer
     */
    public static void setChildNorth(BorderContainerBuilder instance,
                                     ComponentBuilder builder) {
        builder.setProperty(PropertyType.LayoutDataLocation.getCode(), new DataValueImpl(DataType.STRING, "North"));
        instance.addChild(builder);
    }

    /**
     * Устанавливает компонент(builder) в южную(нижнюю) часть контейнера
     *
     * @param builder билдер компонента, который помещаем в южную часть BorderContainer
     */
    public static void setChildSouth(BorderContainerBuilder instance,
                                     ComponentBuilder builder) {
        builder.setProperty(PropertyType.LayoutDataLocation.getCode(), new DataValueImpl(DataType.STRING, "South"));
        instance.addChild(builder);
    }

    /**
     * Удалить вложенный компонент(builder) из контейнера
     *
     * @param builder удаляемый компонент
     */
    public static void remove(BorderContainerBuilder instance,
                              ComponentBuilder builder) {
        instance.removeChild(builder);
    }

    /**
     * Удаляет все вложенные компоненты из контейнера.
     */
    public static void clear(BorderContainerBuilder instance) {
        instance.clearContainer();
    }

    public abstract ComponentBuilder[] getChildren();


    /**
     * Выполняет принудительную перерисовку контейнера и вложенных компонентов.
     */
    public abstract void forceLayout();

    public static ComponentBuilder getParent(BorderContainerBuilder instance) {
        return instance.getParentBuilder();
    }

    public abstract int getChildrenCount();

    public abstract void focus();

}
