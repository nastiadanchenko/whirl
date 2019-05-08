package org.whirlplatform.js.client;

import org.whirlplatform.component.client.ComponentBuilder;
import org.whirlplatform.component.client.base.NumberFieldBuilder;

/**
 * Числовое поле ввода
 */
public abstract class NumberFieldBuilderOverlay {

    /**
     * Инициализация числового поля
     *
     * @param instance - NumberFieldBuilder
     * @return NumberFieldBuilder
     */
    @Deprecated
    public static NumberFieldBuilder create(NumberFieldBuilder instance) {
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

    /**
     * Установить код на числовое поле
     *
     * @param name - String, код
     */
    public abstract void setCode(String name);

    /**
     * Получить код числового поля
     *
     * @return String
     */
    public abstract String getCode();

    /**
     * Установить активность числового поля
     *
     * @param enabled - boolean
     */
    public abstract void setEnabled(boolean enabled);

    /**
     * Получить информачию об активности числового поля
     *
     * @return boolean
     */
    public abstract boolean isEnabled();

    /**
     * Установить скрытость числового поля
     *
     * @param hidden - boolean
     */
    public abstract void setHidden(boolean hidden);

    /**
     * Получить информацию о скрытости числового поля
     *
     * @return boolean
     */
    public abstract boolean isHidden();

    /**
     * Установить стиль на числовое поле
     *
     * @param styleName - String, название стиля
     */
    public abstract void setStyleName(String styleName);

    /**
     * Проверка на null значения числового поля
     *
     * @param instance - NumberFieldBuilder
     * @return boolean
     */
    public static boolean isEmpty(NumberFieldBuilder instance) {
        return instance.getValue() == null;
    }

    /**
     * Установка значения числового поля
     *
     * @param instance - NumberFieldBuilder
     * @param value    - double
     */
    public static void setValue(NumberFieldBuilder instance, double value) {
        instance.setValue(value);
    }

    /**
     * Получение значение числового поля
     *
     * @param instance - NumberFieldBuilder
     * @return double
     */
    public static double getValue(NumberFieldBuilder instance) {
        if (instance.getValue() == null) {
            return 0;
        }
        return instance.getValue();
    }

////    public static DataValue getDataValue(NumberFieldBuilder instance) {
//        return instance.getFieldValue();
//    }
//
////    public static void setDataValue(NumberFieldBuilder instance, DataValue value) {
//        instance.setFieldValue(value);
//    }

    public abstract String getText();

    /**
     * Очистка значения числового поля
     */
    public abstract void clear();

    /**
     * Установка свойства "Обязателен для заполнения" для числового поля
     *
     * @param required - boolean
     */
    public abstract void setRequired(boolean required);

    /**
     * Получить информацию о свойстве "Обязателен для заполнения" у числового
     * поля
     *
     * @return boolean
     */
    public abstract boolean isRequired();

    /**
     * Установить формат числового поля
     *
     * @param format - String
     */
    public abstract void setFormat(String format);

    /**
     * Установить маску числового поля
     *
     * @param mask - String
     */
    public abstract void setMask(String mask);

    /**
     * Установить сообщение не валидности числового поля
     *
     * @param message - String
     */
    public abstract void markInvalid(String message);

    /**
     * Очистка валидности числового поля
     */
    public abstract void clearInvalid();

    /**
     * Получение родителя числового поля
     *
     * @param instance - NumberFieldBuilder
     * @return ComponentBuilder
     */
    public static ComponentBuilder getParent(NumberFieldBuilder instance) {
        return instance.getParentBuilder();
    }

    /**
     * Установить фокус на числовое поле
     */
    public abstract void focus();

    /**
     * Проверяет валидность заполнения поля
     *
     * @param invalidate - отображать информацию о невалидном значении
     * @return boolean
     */
    public abstract boolean isValid(boolean invalidate);

    public static void setToolTip(NumberFieldBuilder instance, String toolTip) {
        instance.getComponent().setToolTip(toolTip);
    }
}
