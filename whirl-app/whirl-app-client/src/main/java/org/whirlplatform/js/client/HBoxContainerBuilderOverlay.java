package org.whirlplatform.js.client;

import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import org.whirlplatform.component.client.ComponentBuilder;
import org.whirlplatform.component.client.base.HBoxContainerBuilder;
import org.whirlplatform.meta.shared.component.PropertyType;
import org.whirlplatform.meta.shared.data.DataType;
import org.whirlplatform.meta.shared.data.DataValueImpl;

/**
 * Контейнер, позволяющий располагать элементы один рядом с другим.
 * В отличие от HorizontalContainerBuilder, вложенные компоненты занимают по ширине минимальное место
 */
public abstract class HBoxContainerBuilderOverlay {

    public static HBoxContainerBuilder create(HBoxContainerBuilder instance) {
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

    public abstract void setEnabled(boolean enabled);

    public abstract boolean isEnabled();

    public abstract void setHidden(boolean hidden);

    public abstract void isHidden();

    public abstract void setStyleName(String styleName);

    public static void addChild(HBoxContainerBuilder instance,
                                ComponentBuilder builder, BoxLayoutData data) {
        addChildByIndex(instance, instance.getChildrenCount(), builder, data);
    }

    public static void addChildByIndex(HBoxContainerBuilder instance,
                                       int index, ComponentBuilder builder, BoxLayoutData data) {
        builder.setProperty(PropertyType.LayoutDataIndex.getCode(),
                new DataValueImpl(DataType.NUMBER, index));
        if (data != null) {
            builder.setProperty(PropertyType.LayoutDataMinSize.getCode(),
                    new DataValueImpl(DataType.NUMBER, data.getMinSize()));
            builder.setProperty(PropertyType.LayoutDataMaxSize.getCode(),
                    new DataValueImpl(DataType.NUMBER, data.getMaxSize()));
            builder.setProperty(PropertyType.LayoutDataFlex.getCode(),
                    new DataValueImpl(DataType.NUMBER, data.getFlex()));
            builder.setProperty(PropertyType.LayoutDataMarginTop.getCode(),
                    new DataValueImpl(DataType.NUMBER, data.getMargins().getTop()));
            builder.setProperty(PropertyType.LayoutDataMarginRight.getCode(),
                    new DataValueImpl(DataType.NUMBER, data.getMargins().getRight()));
            builder.setProperty(PropertyType.LayoutDataMarginBottom.getCode(),
                    new DataValueImpl(DataType.NUMBER, data.getMargins().getBottom()));
            builder.setProperty(PropertyType.LayoutDataMarginLeft.getCode(),
                    new DataValueImpl(DataType.NUMBER, data.getMargins().getLeft()));
        }
        instance.addChild(builder);
    }

    public static void clear(HBoxContainerBuilder instance) {
        instance.clearContainer();
    }

    public static void remove(HBoxContainerBuilder instance,
                              ComponentBuilder builder) {
        instance.removeChild(builder);
    }

    public abstract ComponentBuilder[] getChildren();

    public abstract void forceLayout();

    public static ComponentBuilder getParent(HBoxContainerBuilder instance) {
        return instance.getParentBuilder();
    }

    public abstract int getChildrenCount();

    public abstract void focus();

}
