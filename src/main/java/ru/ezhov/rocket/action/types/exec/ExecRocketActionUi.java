package ru.ezhov.rocket.action.types.exec;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;
import ru.ezhov.rocket.action.icon.IconService;
import ru.ezhov.rocket.action.notification.NotificationFactory;
import ru.ezhov.rocket.action.types.AbstractRocketAction;
import ru.ezhov.rocket.action.types.ConfigurationUtil;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileSystemView;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ExecRocketActionUi extends AbstractRocketAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String COMMAND = "command";
    private static final String WORKING_DIR = "workingDirectory";
    private static final String ICON_URL = "iconUrl";

    public Component create(RocketActionSettings settings) {
        JMenuItem menuItem = new JMenuItem(ConfigurationUtil.getValue(settings.settings(), LABEL));

        String command = ConfigurationUtil.getValue(settings.settings(), COMMAND);

        Icon icon = IconService.load(
                Optional.ofNullable("".equals(settings.settings().get(ICON_URL)) ? null : ICON_URL),
                IconRepositoryFactory.getInstance().by("fire-2x").get()
        );
        try {
            File file = new File(command);
            if (file.exists()) {
                icon = FileSystemView.getFileSystemView().getSystemIcon(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        menuItem.setIcon(icon);
        menuItem.setToolTipText(ConfigurationUtil.getValue(settings.settings(), DESCRIPTION));

        menuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
                    Clipboard clipboard = defaultToolkit.getSystemClipboard();
                    clipboard.setContents(new StringSelection(ConfigurationUtil.getValue(settings.settings(), COMMAND)), null);

                    NotificationFactory.getInstance().show("Command '" + command + "' copy to clipboard");
                } else if (e.getButton() == MouseEvent.BUTTON1) {
                    try {
                        String workingDir = ConfigurationUtil.getValue(settings.settings(), WORKING_DIR);
                        if (workingDir == null || "".equals(workingDir)) {
                            Runtime.getRuntime().exec(ConfigurationUtil.getValue(settings.settings(), COMMAND));
                        } else {
                            Runtime.getRuntime().exec(
                                    ConfigurationUtil.getValue(settings.settings(), COMMAND),
                                    null,
                                    new File(workingDir)
                            );
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        return menuItem;
    }

    @Override
    public String type() {
        return "EXEC";
    }

    @Override
    public String description() {
        return "description";
    }

    @Override
    public List<RocketActionConfigurationProperty> properties() {
        return Arrays.asList(
                createRocketActionProperty(LABEL, "TEST", true),
                createRocketActionProperty(COMMAND, "TEST", true),
                createRocketActionProperty(WORKING_DIR, "TEST", true),
                createRocketActionProperty(DESCRIPTION, "TEST", false),
                createRocketActionProperty(ICON_URL, "Icon URL", false)
        );
    }
}
