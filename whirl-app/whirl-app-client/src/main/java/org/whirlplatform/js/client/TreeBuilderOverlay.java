package org.whirlplatform.js.client;

import org.whirlplatform.component.client.ComponentBuilder;
import org.whirlplatform.component.client.tree.TreeBuilder;

public abstract class TreeBuilderOverlay {

    @Deprecated
    public static TreeBuilder create(TreeBuilder instance) {
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

////    @Deprecated
//    public abstract RowListValue getFieldValue();
//
////    @Deprecated
//    public abstract void setFieldValue(RowListValue value);
//
////    public static RowListValue getDataValue(TreeBuilder instance) {
//        return instance.getFieldValue();
//    }
//
////    public static void setDataValue(TreeBuilder instance, RowListValue value) {
//        instance.setFieldValue(value);
//    }

    public abstract void load();

//    @Deprecated
////    public static void loadParameters(TreeBuilder instance,
//                                      DataValue[] parameters) {
//        instance.load(Arrays.asList(parameters));
//    }
//
////    public static void loadWithParameters(TreeBuilder instance,
//                                          DataValue[] parameters) {
//        instance.load(Arrays.asList(parameters));
//    }

    public static ComponentBuilder getParent(TreeBuilder instance) {
        return instance.getParentBuilder();
    }

    public abstract void focus();

    /**
     * Проверяет валидность заполнения поля
     *
     * @param invalidate - отображать информацию о невалидном значении
     * @return boolean
     */
    public abstract boolean isValid(boolean invalidate);

//    /**
//     * Очищает фильтр поиска по тексту.
//     */
////    public abstract void clearLabelFilter();

}
