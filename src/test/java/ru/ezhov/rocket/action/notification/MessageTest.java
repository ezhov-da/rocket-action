package ru.ezhov.rocket.action.notification;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Point;

public class MessageTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Message message = new Message(4000, "Test");
            message.showMessage(new Point(300, 200), new Dimension(300, 200));
        });
    }
}