package ru.ezhov.rocket.action.notification;

import ru.ezhov.rocket.action.icon.AppIcon;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

class Message extends JWindow {
    private float opacity = 1F;
    private int delay;

    public Message(NotificationType type, int delay, String text) {
        this.delay = delay;
        JPanel panel = new JPanel(new BorderLayout());
        final JLabel label = new JLabel(text);
        switch (type) {
            case INFO:
                label.setIcon(IconRepositoryFactory.instance().by(AppIcon.INFO));
                break;
            case WARN:
                label.setIcon(IconRepositoryFactory.instance().by(AppIcon.WARNING));
                break;
            case ERROR:
                label.setIcon(IconRepositoryFactory.instance().by(AppIcon.BAN));
                break;
        }

        label.setHorizontalAlignment(JLabel.CENTER);
        panel.add(label, BorderLayout.CENTER);
        add(panel, BorderLayout.CENTER);
        setAlwaysOnTop(true);
    }

    public void showMessage(Point location, Dimension size) {
        Message.this.setSize(size);
        Message.this.setLocation(location);
        Timer timer = new Timer(20, null);
        timer.setInitialDelay(delay);
        timer.addActionListener(e -> {
            opacity = opacity - 0.1F;
            if (opacity <= 0) {
                timer.stop();
                Message.this.setVisible(false);
                Message.this.dispose();
            } else {
                Message.this.setOpacity(opacity);
            }
        });
        timer.start();

        Message.this.setVisible(true);
    }
}
