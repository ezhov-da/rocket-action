package ru.ezhov.quick.action;

import java.util.List;

public interface QuickActionRepository {
    List<QuickAction> actions() throws QuickActionRepositoryException;
}
