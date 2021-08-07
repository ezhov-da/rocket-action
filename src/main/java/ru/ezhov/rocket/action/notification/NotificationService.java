package ru.ezhov.rocket.action.notification;

public interface NotificationService {
    void show(NotificationType type, String text);
}
