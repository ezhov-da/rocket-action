package ru.ezhov.quick.action;

import ru.ezhov.quick.action.contract.QuickAction;

import java.util.List;

public interface QuickActionRepository {
    List<QuickAction> actions() throws QuickActionRepositoryException;
}
