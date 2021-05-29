package ru.ezhov.rocket.action.notification;

public class PopupNotificationServiceTest {
    public static void main(String[] args) {
        PopupNotificationService popupNotificationService = new PopupNotificationService();
        for (int i = 0; i < 5; i++) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            popupNotificationService.show("" + i);
        }
    }
}