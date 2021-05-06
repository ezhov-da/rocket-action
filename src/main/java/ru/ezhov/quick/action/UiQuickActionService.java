package ru.ezhov.quick.action;

import ru.ezhov.quick.action.contract.QuickAction;
import ru.ezhov.quick.action.infrastructure.YmlQuickActionRepository;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UiQuickActionService {

    private final String userPathToAction;

    public UiQuickActionService(String userPathToAction) {
        this.userPathToAction = userPathToAction;
    }

    public JMenuBar createMenu(JDialog dialog) throws UiQuickActionServiceException {
        try {
            List<Component> actions = quickActions();

            JMenu menu = new JMenu();
            menu.setIcon(new ImageIcon(App.class.getResource("/rocket_16x16.png")));
            actions.forEach(menu::add);
            menu.add(createTools(dialog));

            JMenuBar menuBar = new JMenuBar();
            menuBar.add(menu);
            menuBar.add(createMoveComponent(dialog));

            return menuBar;
        } catch (Exception e) {
            throw new UiQuickActionServiceException("Error", e);
        }
    }

    private List<Component> quickActions() throws Exception {
        InputStream inputStream = null;

        try {
            if (userPathToAction != null) {
                inputStream = new FileInputStream(new File(userPathToAction));
            } else {
                System.out.println("Use absolute path to `action.xml` file as argument");
                inputStream = App.class.getResourceAsStream("/actions.yml");
            }

            QuickActionRepository quickActionRepository = new YmlQuickActionRepository(inputStream);
            return quickActionRepository.actions();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private JMenu createTools(JDialog dialog) {
        JMenu menuTools = new JMenu("Tools");
        menuTools.setIcon(new ImageIcon(App.class.getResource("/tools_16x16.png")));

        JMenuItem menuItemUpdate = new JMenuItem("Update");
        menuItemUpdate.setIcon(new ImageIcon(App.class.getResource("/update_16x16.png")));
        menuItemUpdate.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            JMenuBar newMenuBar = null;
            try {
                newMenuBar = createMenu(dialog);
            } catch (UiQuickActionServiceException ex) {
                ex.printStackTrace();
            }
            if (newMenuBar != null) {
                // пока костыль, но мы то знаем это "пока" :)
                dialog.getJMenuBar().removeAll();
                dialog.setJMenuBar(newMenuBar);
                dialog.revalidate();
                dialog.repaint();
            }
        }));
        menuTools.add(menuItemUpdate);

        JMenuItem menuItemClose = new JMenuItem("Close");
        menuItemClose.setIcon(new ImageIcon(App.class.getResource("/close_16x16.png")));
        menuItemClose.addActionListener(e -> SwingUtilities.invokeLater(dialog::dispose));
        menuTools.add(menuItemClose);

        return menuTools;
    }

    private Component createMoveComponent(JDialog dialog) {
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

                Point framePoint = dialog.getLocation();

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

                    dialog.setLocation(new Point(mousePoint.x - x, mousePoint.y - y));
                }
            }
        };

        label.addMouseListener(mouseAdapter);
        label.addMouseMotionListener(mouseAdapter);

        return label;
    }
}
