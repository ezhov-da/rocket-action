package ru.ezhov.rocket.action.notification;

public class NotificationFactory {
    private static NotificationService service;

    public static NotificationService getInstance(){
        if(service == null){
            service = new PopupNotificationService();
        }

        return service;
    }
}
