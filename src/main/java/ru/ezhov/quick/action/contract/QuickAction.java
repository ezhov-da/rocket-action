package ru.ezhov.quick.action.contract;

import java.awt.Component;
import java.util.Map;

public interface QuickAction {
    /**
     * Создание компонента должно происходит только при вызове этого метода
     *
     * @return компонент для отображения
     */
    Component create(Map<String, Object> configuration);
}
