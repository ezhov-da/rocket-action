package ru.ezhov.rocket.action.notification;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

class PopupNotificationService implements NotificationService {
    private final LinkedList<Message> messages = new LinkedList<>();
    private static final int SPACE_BETWEEN_MESSAGES = 15;

    @Override
    public void show(NotificationType type, String text) {
        SwingUtilities.invokeLater(() -> createAndShowMessage(type, text));
    }

    private void createAndShowMessage(NotificationType type, String text) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension messageDimension = new Dimension(
                (int) (screenSize.width * 0.1),
                (int) (screenSize.height * 0.07)
        );
        Message message = new Message(type, 3000, text);
        Point point;
        if (messages.isEmpty()) {
            point = new Point(screenSize.width - SPACE_BETWEEN_MESSAGES - messageDimension.width,
                    screenSize.height - SPACE_BETWEEN_MESSAGES - messageDimension.height
            );
        } else {
            final Message messageLast = messages.getLast();
            point = new Point(screenSize.width - SPACE_BETWEEN_MESSAGES - messageDimension.width,
                    messageLast.getY() - SPACE_BETWEEN_MESSAGES - messageDimension.height
            );
        }

        message.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                messages.remove(e.getSource());
            }
        });
        messages.addLast(message);
        message.showMessage(point, messageDimension);
    }
}
