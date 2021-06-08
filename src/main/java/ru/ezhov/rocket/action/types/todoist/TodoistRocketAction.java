package ru.ezhov.rocket.action.types.todoist;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;
import ru.ezhov.rocket.action.notification.NotificationFactory;
import ru.ezhov.rocket.action.types.AbstractRocketAction;
import ru.ezhov.rocket.action.types.ConfigurationUtil;
import ru.ezhov.rocket.action.types.todoist.model.Project;
import ru.ezhov.rocket.action.types.todoist.model.Task;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TodoistRocketAction extends AbstractRocketAction {
    public static final String LABEL = "label";
    public static final String TOKEN = "todoistToken";
    public static final String BASE_GIST_URL = "baseGistUrl";
    private JMenu menu;

    @Override
    public String description() {
        return "Simple todoist client";
    }

    @Override
    public List<RocketActionConfigurationProperty> properties() {
        return Arrays.asList(
                createRocketActionProperty(LABEL, "Label", true),
                createRocketActionProperty(
                        TOKEN,
                        "Use this or -D" + TodoistProjectRepository.TOKEN_PROPERTY,
                        false
                )
        );
    }

    @Override
    public Component create(RocketActionSettings settings) {
        menu = new JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL));
        new TodoistWorker(settings).execute();
        return menu;
    }

    @Override
    public String type() {
        return "TODOIST";
    }

    private class TodoistWorker extends SwingWorker<TodoistPanel, String> {
        private RocketActionSettings settings;

        public TodoistWorker(RocketActionSettings settings) {
            menu.removeAll();
            menu.setIcon(new ImageIcon(this.getClass().getResource("/load_16x16.gif")));
            this.settings = settings;
        }

        @Override
        protected TodoistPanel doInBackground() throws Exception {
            return new TodoistPanel(null, settings.settings().get(BASE_GIST_URL), settings);
        }

        @Override
        protected void done() {
            menu.setIcon(IconRepositoryFactory.getInstance().by("bookmark-2x").get());
            try {
                menu.removeAll();
                menu.add(this.get());

                NotificationFactory.getInstance().show("Todoist loaded");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private class TodoistPanel extends JPanel {
        private TodoistTaskRepository todoistTaskRepository = new TodoistTaskRepository();
        private DefaultListModel<Project> projectListModel = new DefaultListModel<>();
        private DefaultListModel<Task> taskListModel = new DefaultListModel<>();
        private JPanelTaskInfo panelTaskInfo = new JPanelTaskInfo();

        public TodoistPanel(List<Project> gists, String gistUrl, RocketActionSettings settings) {
            super(new BorderLayout());
            try {
                final List<Task> tasks = todoistTaskRepository.tasks(settings);
                taskListModel.removeAllElements();
                for (Task task : tasks) {
                    taskListModel.addElement(task);
                }
            } catch (TodoistRepositoryException e) {
                e.printStackTrace();
            }
            JList<Task> taskJList = new JList(taskListModel);
            taskJList.setFixedCellWidth(500);
            taskJList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value != null) {
                        label.setText(((Task) value).getContent());
                    }
                    return label;
                }
            });
            taskJList.addListSelectionListener(e -> {
                Task task = taskJList.getSelectedValue();
                if (task != null) {
                    panelTaskInfo.setTask(task);
                }
            });
            final JScrollPane scrollPane = new JScrollPane(taskJList);
            add(scrollPane, BorderLayout.CENTER);
            add(panelTaskInfo, BorderLayout.SOUTH);
        }

        private class JPanelTaskInfo extends JPanel {
            private JLabel labelId = new JLabel();
            private JLabel labelCreated = new JLabel();
            private JLabel labelUrl = new JLabel();
            private JTextPane textPaneContent = new JTextPane();

            public JPanelTaskInfo() {
                super(new BorderLayout());

                labelUrl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if(!"".equals(labelUrl.getText())){
                            if(Desktop.isDesktopSupported()){
                                try {
                                    Desktop.getDesktop().browse(new URI(labelUrl.getText()));
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                } catch (URISyntaxException uriSyntaxException) {
                                    uriSyntaxException.printStackTrace();
                                }
                            }
                        }
                    }
                });

                add(new JScrollPane(textPaneContent), BorderLayout.NORTH);
                add(labelUrl, BorderLayout.CENTER);
            }

            public void setTask(Task task) {
                labelUrl.setText(task.getUrl());
                textPaneContent.setText(task.getContent());
            }
        }
    }
}
