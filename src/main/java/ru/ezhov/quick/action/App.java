package ru.ezhov.quick.action;

import ru.ezhov.quick.action.infrastructure.XmlQuickActionRepository;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable ex) {
                //
            }

            List<QuickAction> actions = new ArrayList<>();
            InputStream inputStream = null;

            try {
                if (args.length > 0) {
                    String pathToActions = args[0];
                    inputStream = new FileInputStream(new File(pathToActions));
                } else {
                    System.out.println("Use absolute path to `action.xml` file as argument");
                    inputStream = App.class.getResourceAsStream("/actions.xml");
                }

                QuickActionRepository quickActionRepository = new XmlQuickActionRepository(inputStream);
                actions = quickActionRepository.actions();

            } catch (IOException | QuickActionRepositoryException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            JDialog frame = new JDialog();
            frame.add(createMenu(frame, actions));
            frame.setUndecorated(true);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setAlwaysOnTop(true);
            frame.setVisible(true);
        });
    }

    private static JMenuBar createMenu(JDialog frame, List<QuickAction> actions) {
        JMenu menu = new JMenu();
        menu.setIcon(new ImageIcon(App.class.getResource("/rocket_16x16.png")));

        actions.forEach(a -> menu.add(a.create()));

        JMenuItem menuItemClose = new JMenuItem("Закрыть");
        menuItemClose.setIcon(new ImageIcon(App.class.getResource("/close_16x16.png")));
        menuItemClose.addActionListener(e -> SwingUtilities.invokeLater(frame::dispose));
        menu.add(menuItemClose);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);

        JLabel label = new JLabel(new ImageIcon(App.class.getResource("/cursor_drag_arrow_16x16.png")));
        MouseAdapter mouseAdapter = new MouseAdapter() {
            boolean pressed = false;
            int x = 0;
            int y = 0;

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                Point mousePoint = e.getPoint();
                SwingUtilities.convertPointToScreen(mousePoint, label);

                Point framePoint = frame.getLocation();

                x = mousePoint.x - framePoint.x;
                y = mousePoint.y - framePoint.y;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (pressed) {
                    Point mousePoint = e.getPoint();
                    SwingUtilities.convertPointToScreen(mousePoint, label);

                    frame.setLocation(new Point(mousePoint.x - x, mousePoint.y - y));
                }
            }
        };

        label.addMouseListener(mouseAdapter);
        label.addMouseMotionListener(mouseAdapter);
        menuBar.add(label);

        return menuBar;
    }
}
