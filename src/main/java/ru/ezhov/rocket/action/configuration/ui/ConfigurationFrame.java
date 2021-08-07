package ru.ezhov.rocket.action.configuration.ui;

import ru.ezhov.rocket.action.RocketActionSettingsRepository;
import ru.ezhov.rocket.action.RocketActionSettingsRepositoryException;
import ru.ezhov.rocket.action.RocketActionUiRepository;
import ru.ezhov.rocket.action.api.RocketActionConfiguration;
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.icon.AppIcon;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;
import ru.ezhov.rocket.action.infrastructure.MutableRocketActionSettings;
import ru.ezhov.rocket.action.notification.NotificationFactory;
import ru.ezhov.rocket.action.notification.NotificationType;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;

public class ConfigurationFrame {

    private JDialog dialog;
    private RocketActionConfigurationRepository rocketActionConfigurationRepository;
    private RocketActionUiRepository rocketActionUiRepository;
    private CreateRocketActionSettingsDialog createRocketActionSettingsDialog;
    private RocketActionSettingsRepository rocketActionSettingsRepository;
    private ActionListener updateActionListener;

    public ConfigurationFrame(
            Dialog owner,
            RocketActionConfigurationRepository rocketActionConfigurationRepository,
            RocketActionUiRepository rocketActionUiRepository,
            RocketActionSettingsRepository rocketActionSettingsRepository,
            ActionListener updateActionListener
    ) throws Exception {
        dialog = new JDialog(owner, "Rocket action configuration");

        this.updateActionListener = updateActionListener;
        this.rocketActionConfigurationRepository = rocketActionConfigurationRepository;
        this.rocketActionUiRepository = rocketActionUiRepository;
        this.rocketActionSettingsRepository = rocketActionSettingsRepository;

        JMenuBar menuBar = new JMenuBar();

        JMenuItem menuItemUpdate = new JMenuItem("Update");
        menuItemUpdate.setIcon(IconRepositoryFactory.instance().by(AppIcon.RELOAD));
        menuItemUpdate.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            updateActionListener.actionPerformed(e);
            ConfigurationFrame.this.setVisible(false);
        }));
        menuBar.add(menuItemUpdate);


        final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setSize((int) (size.width * 0.6), (int) (size.height * 0.6));
        dialog.setLocationRelativeTo(null);
        createRocketActionSettingsDialog = new CreateRocketActionSettingsDialog(
                dialog,
                rocketActionConfigurationRepository,
                rocketActionUiRepository
        );
        dialog.add(menuBar, BorderLayout.NORTH);
        dialog.add(panel(), BorderLayout.CENTER);
    }

    private JPanel panel() throws Exception {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tree(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel tree() throws Exception {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(0.5D);
        splitPane.setResizeWeight(0.5D);

        final List<RocketActionSettings> actions = rocketActionSettingsRepository.actions();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);
        fillTreeNodes(actions, root);
        DefaultTreeModel defaultTreeModel = new DefaultTreeModel(root);

        RocketActionSettingsPanel rocketActionSettingsPanel = new RocketActionSettingsPanel();

        JPanel panel = new JPanel(new BorderLayout());
        JTree tree = new JTree(defaultTreeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            final TreePath path = e.getNewLeadSelectionPath();
            if (path == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

            if (node == null) return;

            Object o = node.getUserObject();
            if (o != null) {
                RocketActionSettings settings = (RocketActionSettings) o;
                SwingUtilities.invokeLater(
                        () -> rocketActionSettingsPanel.show(
                                settings,
                                node::setUserObject
                        )
                );
            }
        });
        tree.setCellRenderer(new RocketActionSettingsCellRender());
        tree.setRootVisible(false);

        JPanel panelTree = new JPanel(new BorderLayout());
        panelTree.add(new JScrollPane(tree), BorderLayout.CENTER);
        JPanel panelSaveTree = new JPanel();

        JButton buttonSaveTree = new JButton("Save actions");
        buttonSaveTree.addActionListener(e -> saveSettings(defaultTreeModel));

        panelSaveTree.add(buttonSaveTree);

        panelTree.add(panelSaveTree, BorderLayout.SOUTH);

        splitPane.setLeftComponent(panelTree);
        splitPane.setRightComponent(rocketActionSettingsPanel);

        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new TreeTransferHandler());

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    final TreePath treePath = tree.getClosestPathForLocation(e.getX(), e.getY());
                    if (treePath == null) return;

                    DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();

                    JPopupMenu popupMenu = new JPopupMenu();
                    popupMenu.add(new JMenuItem(new AbstractAction() {
                        {
                            putValue(Action.NAME, "Add new TOP");
                        }

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            createRocketActionSettingsDialog.show(rocketActionSettings -> {
                                SwingUtilities.invokeLater(() -> {
                                    MutableRocketActionSettings newActionSettings = new MutableRocketActionSettings(
                                            rocketActionSettings.id(),
                                            rocketActionSettings.type(),
                                            rocketActionSettings.settings(),
                                            rocketActionSettings.actions()
                                    );

                                    defaultTreeModel.insertNodeInto(
                                            new DefaultMutableTreeNode(newActionSettings, true),
                                            (MutableTreeNode) mutableTreeNode.getParent(),
                                            mutableTreeNode.getParent().getIndex(mutableTreeNode)

                                    );
                                });
                            });
                        }
                    }));
                    popupMenu.add(new JMenuItem(
                            new AbstractAction() {
                                {
                                    putValue(Action.NAME, "Add new DOWN");
                                }

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    createRocketActionSettingsDialog.show(rocketActionSettings -> {
                                        SwingUtilities.invokeLater(() -> {
                                            defaultTreeModel.insertNodeInto(
                                                    new DefaultMutableTreeNode(
                                                            new MutableRocketActionSettings(
                                                                    rocketActionSettings.id(),
                                                                    rocketActionSettings.type(),
                                                                    rocketActionSettings.settings(),
                                                                    rocketActionSettings.actions()
                                                            ),
                                                            true
                                                    ),
                                                    (MutableTreeNode) mutableTreeNode.getParent(),
                                                    mutableTreeNode.getParent().getIndex(mutableTreeNode) + 1
                                            );
                                        });
                                    });
                                }
                            }
                    ));
                    popupMenu.add(new JMenuItem(
                            new AbstractAction() {
                                {
                                    putValue(Action.NAME, "Add new as child");
                                }

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    createRocketActionSettingsDialog.show(rocketActionSettings -> {
                                        SwingUtilities.invokeLater(() -> {
                                            mutableTreeNode.add(
                                                    new DefaultMutableTreeNode(
                                                            new MutableRocketActionSettings(
                                                                    rocketActionSettings.id(),
                                                                    rocketActionSettings.type(),
                                                                    rocketActionSettings.settings(),
                                                                    rocketActionSettings.actions()
                                                            ),
                                                            true
                                                    )
                                            );

                                            defaultTreeModel.reload(mutableTreeNode);
                                        });
                                    });
                                }
                            }
                    ));

                    popupMenu.add(new JMenuItem(
                            new AbstractAction() {
                                {
                                    putValue(Action.NAME, "Delete");
                                }

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    SwingUtilities.invokeLater(() -> {
                                        mutableTreeNode.removeFromParent();
                                        defaultTreeModel.reload();
                                    });
                                }
                            }
                    ));

                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private void fillTreeNodes(List<RocketActionSettings> actions, DefaultMutableTreeNode parent) {
        for (RocketActionSettings rocketActionSettings : actions) {
            DefaultMutableTreeNode current = new DefaultMutableTreeNode(rocketActionSettings, true);
            parent.add(current);
            if (!rocketActionSettings.actions().isEmpty()) {
                final List<RocketActionSettings> childAction = rocketActionSettings.actions();
                fillTreeNodes(childAction, current);
            }
        }
    }

    private void saveSettings(DefaultTreeModel treeModel) {
        List<RocketActionSettings> settings = new ArrayList<>();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            recursiveGetSettings((DefaultMutableTreeNode) root.getChildAt(i), settings, null);
        }

        try {
            rocketActionSettingsRepository.save(settings);

            NotificationFactory.getInstance().show(NotificationType.INFO, "Actions saved");
        } catch (RocketActionSettingsRepositoryException e) {
            e.printStackTrace();

            NotificationFactory.getInstance().show(NotificationType.ERROR, "Error actions saving");
        }
    }

    private void recursiveGetSettings(DefaultMutableTreeNode node, List<RocketActionSettings> settings, MutableRocketActionSettings parent) {
        MutableRocketActionSettings originalActionSettings = (MutableRocketActionSettings) node.getUserObject();
        MutableRocketActionSettings finalActionSettings =
                new MutableRocketActionSettings(
                        originalActionSettings.id(),
                        originalActionSettings.type(),
                        originalActionSettings.settings()
                );
        if (parent == null) {
            settings.add(finalActionSettings);
        } else {
            parent.add(finalActionSettings);
        }
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            recursiveGetSettings((DefaultMutableTreeNode) node.getChildAt(i), settings, finalActionSettings);
        }
    }

    public void setVisible(boolean visible) {
        dialog.setVisible(visible);
    }

    private class RocketActionSettingsCellRender extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (value != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                if (node.getUserObject() instanceof RocketActionSettings) {
                    RocketActionSettings settings = (RocketActionSettings) node.getUserObject();
                    if (settings != null) {
                        final String labelProperty = settings.settings().get("label");
                        if (labelProperty != null && !"".equals(labelProperty)) {
                            label.setText(labelProperty);
                        } else {
                            label.setText(settings.type());
                        }
                    }
                }
            }

            return label;
        }
    }

    private class RocketActionSettingsPanel extends JPanel {
        private final DefaultTableModel tableModel = new DefaultTableModel();
        private final JTable table = new JTable(tableModel);
        private RocketActionSettings currentSettings;
        private SavedRocketActionSettingsPanelCallback callback;
        private JLabel labelType = new JLabel();
        private JLabel labelDescription = new JLabel();

        public RocketActionSettingsPanel() {
            super(new BorderLayout());
            tableModel.addColumn("Name");
            tableModel.addColumn("Value");
            tableModel.addColumn("Description");
            add(top(), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            add(testAndCreate(), BorderLayout.SOUTH);
        }

        private JPanel top() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(labelType, BorderLayout.NORTH);
            panel.add(labelDescription, BorderLayout.CENTER);
            return panel;
        }

        private JPanel testAndCreate() {
            JPanel panel = new JPanel();
            JButton button = new JButton("Save current action");
            button.addActionListener(e -> {
                callback.saved(create());
                NotificationFactory.getInstance().show(NotificationType.INFO, "Current action saved");
            });
            panel.add(button);
            return panel;
        }

        public void show(RocketActionSettings settings, SavedRocketActionSettingsPanelCallback callback) {
            this.currentSettings = settings;

            labelType.setText(settings.type());

            this.callback = callback;
            while (tableModel.getRowCount() != 0) {
                tableModel.removeRow(0);
            }

            final Optional<RocketActionConfiguration> configurationOptional = rocketActionConfigurationRepository.by(settings.type());

            RocketActionConfiguration configuration = null;
            if (configurationOptional.isPresent()) {
                configuration = configurationOptional.get();

                labelDescription.setText(configuration.description());
            }

            final Map<String, String> map = settings.settings();
            RocketActionConfiguration finalConfiguration = configuration;
            map.forEach((k, v) -> {
                Vector<String> row = new Vector<>();
                row.add(k);
                row.add(v);
                if (finalConfiguration != null) {
                    final Optional<RocketActionConfigurationProperty> optional =
                            finalConfiguration
                                    .properties()
                                    .stream()
                                    .filter(p -> p.name().equals(k))
                                    .findFirst();
                    if (optional.isPresent()) {
                        row.add(optional.get().description());
                    } else {
                        row.add("Unregistered property");
                    }
                } else {
                    row.add(null);
                }
                tableModel.addRow(row);
            });

            final List<RocketActionConfigurationProperty> notSetProperty =
                    finalConfiguration
                            .properties()
                            .stream()
                            .filter(p -> !map.containsKey(p.name()))
                            .collect(Collectors.toList());

            notSetProperty.forEach(p -> {
                Vector<String> row = new Vector<>();
                row.add(p.name());
                row.add("");
                row.add(p.description());
                tableModel.addRow(row);
            });
        }

        private RocketActionSettings create() {
            if (currentSettings == null)
                throw new IllegalStateException("Must be set current selected configuration");

            int rowCount = tableModel.getRowCount();
            Map<String, String> map = new TreeMap<>();
            for (int i = 0; i < rowCount; i++) {
                Object name = tableModel.getValueAt(i, 0);
                Object value = tableModel.getValueAt(i, 1);
                map.put(name.toString(), value.toString());
            }

            return new MutableRocketActionSettings(
                    currentSettings.id(),
                    currentSettings.type(),
                    map,
                    currentSettings.actions()
            );
        }
    }
}
