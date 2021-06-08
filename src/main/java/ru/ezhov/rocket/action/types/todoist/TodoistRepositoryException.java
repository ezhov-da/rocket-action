package ru.ezhov.rocket.action.types.todoist;

public class TodoistRepositoryException extends Exception {
    public TodoistRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public TodoistRepositoryException(String message) {
        super(message);
    }
}
