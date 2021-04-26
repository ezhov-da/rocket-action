package ru.ezhov.quick.action;

public class QuickActionRepositoryException extends Exception {
    public QuickActionRepositoryException(String message) {
        super(message);
    }

    public QuickActionRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
