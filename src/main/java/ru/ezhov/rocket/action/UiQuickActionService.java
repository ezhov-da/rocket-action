package ru.ezhov.rocket.action;

import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.api.RocketActionUi;
import ru.ezhov.rocket.action.configuration.ui.ConfigurationFrame;
import ru.ezhov.rocket.action.configuration.ui.RocketActionConfigurationRepository;
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionConfigurationRepository;
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionUiRepository;
import ru.ezhov.rocket.action.infrastructure.YmlRocketActionSettingsRepository;

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
import java.net.URI;
import java.util.List;
import java.util.Optional;

public class UiQuickActionService {

    private RocketActionConfigurationRepository rocketActionConfigurationRepository;
    private RocketActionSettingsRepository rocketActionSettingsRepository;
    private RocketActionUiRepository rocketActionUiRepository;
    private final String userPathToAction;
    private ConfigurationFrame configurationFrame;
    private JDialog dialog;

    public UiQuickActionService(
            String userPathToAction
    ) {
        this.userPathToAction = userPathToAction;
        this.rocketActionConfigurationRepository = new ReflectionRocketActionConfigurationRepository();
        this.rocketActionUiRepository = new ReflectionRocketActionUiRepository();
    }

    public JMenuBar createMenu(JDialog dialog) throws UiQuickActionServiceException {
        this.dialog = dialog;
        try {
            List<RocketActionSettings> actionSettings = rocketActionSettings();

            JMenu menu = new JMenu();
            menu.setIcon(new ImageIcon(App.class.getResource("/rocket_16x16.png")));

            for (RocketActionSettings rocketActionSettings : actionSettings) {
                final Optional<RocketActionUi> actionUiOptional = rocketActionUiRepository.by(rocketActionSettings.type());
                if (actionUiOptional.isPresent()) {
                    menu.add(actionUiOptional.get().create(rocketActionSettings));
                }
            }

            menu.add(createTools(dialog));

            JMenuBar menuBar = new JMenuBar();
            menuBar.add(menu);
            menuBar.add(createMoveComponent(dialog));

            return menuBar;
        } catch (Exception e) {
            throw new UiQuickActionServiceException("Error", e);
        }
    }

    private List<RocketActionSettings> rocketActionSettings() throws Exception {
        URI uri = null;
        if (userPathToAction != null) {
            uri = new File(userPathToAction).toURI();
        } else {
            System.out.println("Use absolute path to `action.xml` file as argument");
            uri = App.class.getResource("/actions.yml").toURI();
        }

        rocketActionSettingsRepository = new YmlRocketActionSettingsRepository(uri);
        return rocketActionSettingsRepository.actions();
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

        JMenuItem menuItemEditor = new JMenuItem("Editor");
        menuItemEditor.setIcon(new ImageIcon(App.class.getResource("/editor_16x16.png")));

        menuItemEditor.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (configurationFrame == null) {
                try {
                    configurationFrame = new ConfigurationFrame(
                            dialog,
                            rocketActionConfigurationRepository,
                            rocketActionUiRepository,
                            rocketActionSettingsRepository
                    );
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            if (configurationFrame != null) {
                configurationFrame.setVisible(true);
            }
        }));
        menuTools.add(menuItemEditor);

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
