package ru.ezhov.rocket.action;

import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.api.RocketActionUi;
import ru.ezhov.rocket.action.configuration.ui.ConfigurationFrame;
import ru.ezhov.rocket.action.configuration.ui.RocketActionConfigurationRepository;
import ru.ezhov.rocket.action.icon.AppIcon;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionConfigurationRepository;
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionUiRepository;
import ru.ezhov.rocket.action.infrastructure.YmlRocketActionSettingsRepository;
import ru.ezhov.rocket.action.notification.NotificationFactory;
import ru.ezhov.rocket.action.notification.NotificationType;
import ru.ezhov.rocket.action.properties.GeneralPropertiesRepository;
import ru.ezhov.rocket.action.properties.ResourceGeneralPropertiesRepository;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UiQuickActionService {
    private static final Logger LOGGER = Logger.getLogger(UiQuickActionService.class.getName());
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
            JMenuBar menuBar = new JMenuBar();
            JMenu menu = new JMenu();
            menu.setIcon(new ImageIcon(App.class.getResource("/load_16x16.gif")));

            menuBar.add(menu);
            //TODO: избранное
            //menuBar.add(createFavoriteComponent());
            menuBar.add(createMoveComponent(dialog));

            new CreateMenuWorker(menu).execute();
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
            LOGGER.log(Level.INFO, "Use absolute path to `action.xml` file as argument");
            uri = App.class.getResource("/actions.yml").toURI();
        }

        rocketActionSettingsRepository = new YmlRocketActionSettingsRepository(uri);
        return rocketActionSettingsRepository.actions();
    }

    private JMenu createTools(JDialog dialog) {
        JMenu menuTools = new JMenu("Tools");
        menuTools.setIcon(IconRepositoryFactory.instance().by(AppIcon.WRENCH));

        ActionListener updateActionListener = e -> SwingUtilities.invokeLater(() -> {
            JMenuBar newMenuBar = null;
            try {
                newMenuBar = createMenu(dialog);
            } catch (UiQuickActionServiceException ex) {
                ex.printStackTrace();
                NotificationFactory.getInstance().show(NotificationType.ERROR, "Tools menu created error");
            }
            if (newMenuBar != null) {
                // пока костыль, но мы то знаем это "пока" :)
                dialog.getJMenuBar().removeAll();
                dialog.setJMenuBar(newMenuBar);
                dialog.revalidate();
                dialog.repaint();
            }
        });


        JMenuItem menuItemUpdate = new JMenuItem("Update");
        menuItemUpdate.setIcon(IconRepositoryFactory.instance().by(AppIcon.RELOAD));
        menuItemUpdate.addActionListener(updateActionListener);
        menuTools.add(menuItemUpdate);

        JMenuItem menuItemEditor = new JMenuItem("Editor");
        menuItemEditor.setIcon(IconRepositoryFactory.instance().by(AppIcon.PENCIL));

        menuItemEditor.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (configurationFrame == null) {
                try {
                    configurationFrame = new ConfigurationFrame(
                            dialog,
                            rocketActionConfigurationRepository,
                            rocketActionUiRepository,
                            rocketActionSettingsRepository,
                            updateActionListener
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                    NotificationFactory.getInstance().show(NotificationType.ERROR, "Editor menu created error");
                }
            }

            if (configurationFrame != null) {
                configurationFrame.setVisible(true);
            }
        }));
        menuTools.add(menuItemEditor);

        JMenu menuInfo = new JMenu("Info");
        menuInfo.setIcon(IconRepositoryFactory.instance().by(AppIcon.INFO));


        GeneralPropertiesRepository repository = new ResourceGeneralPropertiesRepository();
        menuInfo.add(new JLabel(repository.all().getProperty("version", "not found")));

        String info = "undefined";
        try (BufferedInputStream is = new BufferedInputStream(this.getClass().getResourceAsStream("/info.html"))) {
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            info = new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            NotificationFactory.getInstance().show(NotificationType.ERROR, "Info menu created error");
        }
        JLabel label = new JLabel("<html>" + info);
        menuInfo.add(label);


        menuTools.add(menuInfo);

        JMenuItem menuItemClose = new JMenuItem("Exit");
        menuItemClose.setIcon(IconRepositoryFactory.instance().by(AppIcon.X));
        menuItemClose.addActionListener(e -> SwingUtilities.invokeLater(dialog::dispose));
        menuTools.add(menuItemClose);

        return menuTools;
    }

    private Component createMoveComponent(JDialog dialog) {
        JLabel label = new JLabel(IconRepositoryFactory.instance().by(AppIcon.MOVE));
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

    private Component createFavoriteComponent() {
        JMenu menu = new JMenu();
        menu.setIcon(IconRepositoryFactory.instance().by(AppIcon.STAR));

        menu.setDropTarget(new DropTarget(
                menu,
                new DropTargetAdapter() {

                    @Override
                    public void drop(DropTargetDropEvent dtde) {
                        try {
                            dtde.acceptDrop(DnDConstants.ACTION_COPY);
                            String text = (String) dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
                            menu.add(new JLabel(text));
                        } catch (UnsupportedFlavorException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ));

        return menu;
    }

    private class CreateMenuWorker extends SwingWorker<String, String> {
        private JMenu menu;

        public CreateMenuWorker(JMenu menu) {
            this.menu = menu;
        }

        @Override
        protected String doInBackground() throws Exception {
            List<RocketActionSettings> actionSettings = rocketActionSettings();
            for (RocketActionSettings rocketActionSettings : actionSettings) {
                final Optional<RocketActionUi> actionUiOptional = rocketActionUiRepository.by(rocketActionSettings.type());
                if (actionUiOptional.isPresent()) {
                    menu.add(actionUiOptional.get().create(rocketActionSettings));
                }
            }
            menu.add(createTools(dialog));
            return null;
        }

        @Override
        protected void done() {
            menu.setIcon(new ImageIcon(App.class.getResource("/rocket_16x16.png")));
        }
    }
}
