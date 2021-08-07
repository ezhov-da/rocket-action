package ru.ezhov.rocket.action.types.gist;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.icon.AppIcon;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;
import ru.ezhov.rocket.action.notification.NotificationFactory;
import ru.ezhov.rocket.action.notification.NotificationType;
import ru.ezhov.rocket.action.types.AbstractRocketAction;
import ru.ezhov.rocket.action.types.ConfigurationUtil;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GistRocketAction extends AbstractRocketAction {
    public static final String LABEL = "label";
    public static final String TOKEN = "gistToken";
    public static final String USERNAME = "username";
    public static final String BASE_GIST_URL = "baseGistUrl";
    private JMenu menu;

    @Override
    public String description() {
        return "Github gist loader";
    }

    @Override
    public List<RocketActionConfigurationProperty> properties() {
        return Arrays.asList(
                createRocketActionProperty(LABEL, "Label", true),
                createRocketActionProperty(
                        TOKEN,
                        "Use this or -D" + GistActionService.TOKEN_PROPERTY,
                        false
                ),
                createRocketActionProperty(USERNAME, "", true),
                createRocketActionProperty(
                        BASE_GIST_URL,
                        "Url gists for open",
                        false
                )
        );
    }

    @Override
    public Component create(RocketActionSettings settings) {
        menu = new JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL));
        new GistWorker(settings).execute();
        return menu;
    }

    @Override
    public String type() {
        return "GIST";
    }

    private class GistWorker extends SwingWorker<GistPanel, String> {
        private RocketActionSettings settings;

        public GistWorker(RocketActionSettings settings) {
            menu.removeAll();
            menu.setIcon(new ImageIcon(this.getClass().getResource("/load_16x16.gif")));
            this.settings = settings;
        }

        @Override
        protected GistPanel doInBackground() throws Exception {
            GistActionService gistActionService = new GistActionService();
            final List<Gist> gists = gistActionService.gists(settings);
            return new GistPanel(gists, settings.settings().get(BASE_GIST_URL), settings);
        }

        @Override
        protected void done() {
            menu.setIcon(IconRepositoryFactory.instance().by(AppIcon.BOOKMARK));
            try {
                menu.removeAll();
                menu.add(this.get());

                NotificationFactory.getInstance().show(NotificationType.INFO, "Gists loaded");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private class GistPanel extends JPanel {
        private List<GistListItem> gistItems;
        private JTextField textFieldSearch = new JTextField();
        private DefaultListModel<GistListItem> model = new DefaultListModel<>();

        public GistPanel(List<Gist> gists, String gistUrl, RocketActionSettings settings) {
            super(new BorderLayout());
            this.gistItems = new ArrayList<>();
            for (Gist gist : gists) {
                Map<String, GistFile> fileMap = gist.getFiles();
                for (Map.Entry<String, GistFile> fileEntry : fileMap.entrySet()) {
                    gistItems.add(new GistListItem(fileEntry.getKey(), gist.getHtmlUrl()));
                }
            }

            gistItems.sort(Comparator.comparing(o -> o.name));
            gistItems.forEach(model::addElement);

            JList<GistListItem> list = new JList<>(model);
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    final GistListItem value = list.getSelectedValue();
                    if (value != null && Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(new URI(value.url));
                        } catch (IOException | URISyntaxException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

            JPanel panelSearchAndUpdate = new JPanel(new BorderLayout());
            JButton buttonUpdate = new JButton(IconRepositoryFactory.instance().by(AppIcon.RELOAD));
            buttonUpdate.addActionListener(e -> new GistWorker(settings).execute());
            panelSearchAndUpdate.add(textFieldSearch, BorderLayout.CENTER);
            panelSearchAndUpdate.add(buttonUpdate, BorderLayout.EAST);

            add(panelSearchAndUpdate, BorderLayout.NORTH);
            add(new JScrollPane(list), BorderLayout.CENTER);

            if (gistUrl != null && !"".equals(gistUrl)) {
                JLabel label = new JLabel(gistUrl);
                add(label, BorderLayout.SOUTH);
                label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(new URI(gistUrl));
                            } catch (IOException | URISyntaxException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            }

            textFieldSearch.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> fillAndSetModel(textFieldSearch.getText()));
            });
        }

        private void fillAndSetModel(String searchText) {
            model.removeAllElements();
            if ("".equals(searchText)) {
                gistItems.forEach(model::addElement);
            } else {
                gistItems.forEach(i -> {
                    if (i.name.contains(searchText)) {
                        model.addElement(i);
                    }
                });
            }
        }

        private class GistListItem {
            private String name;
            private String url;

            public GistListItem(String name, String url) {
                this.name = name;
                this.url = url;
            }

            public String getName() {
                return name;
            }

            public String getUrl() {
                return url;
            }

            @Override
            public String toString() {
                return name;
            }
        }
    }
}
