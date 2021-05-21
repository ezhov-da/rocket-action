package ru.ezhov.rocket.action.configuration.ui;

import ru.ezhov.rocket.action.RocketActionUiRepository;
import ru.ezhov.rocket.action.api.RocketActionConfiguration;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.api.RocketActionUi;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;

public class CreateRocketActionSettingsDialog {
    private JDialog dialog;
    private RocketActionConfigurationRepository rocketActionConfigurationRepository;
    private JComboBox<RocketActionConfiguration> comboBox;
    private RocketActionSettingsPanel actionSettingsPanel = new RocketActionSettingsPanel();
    private CreatedRocketActionSettingsCallback currentCallback;
    private RocketActionUiRepository rocketActionUiRepository;
    private TestPanel testPanel = new TestPanel();

    public CreateRocketActionSettingsDialog(
            Dialog owner,
            RocketActionConfigurationRepository rocketActionConfigurationRepository,
            RocketActionUiRepository rocketActionUiRepository
    ) throws Exception {
        dialog = new JDialog(owner, "Create rocket action");
        dialog.setSize(500, 400);
        this.rocketActionConfigurationRepository = rocketActionConfigurationRepository;
        this.rocketActionUiRepository = rocketActionUiRepository;

        dialog.add(panelComboBox(), BorderLayout.NORTH);

        actionSettingsPanel.setRocketActionConfiguration((RocketActionConfiguration) comboBox.getSelectedItem());
        dialog.add(actionSettingsPanel, BorderLayout.CENTER);
        dialog.add(createTestAndSaveDialog(), BorderLayout.SOUTH);

        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        dialog.setLocationRelativeTo(owner);
    }

    private JPanel panelComboBox() throws Exception {
        DefaultComboBoxModel<RocketActionConfiguration> comboBoxModel = new DefaultComboBoxModel<>();
        final List<RocketActionConfiguration> all = rocketActionConfigurationRepository.all();
        List<RocketActionConfiguration> sortedAll = all
                .stream()
                .sorted(Comparator.comparing(RocketActionConfiguration::type))
                .collect(Collectors.toList());
        sortedAll.forEach(comboBoxModel::addElement);

        JPanel panel = new JPanel(new BorderLayout());

        comboBox = new JComboBox<>(comboBoxModel);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    RocketActionConfiguration configuration = (RocketActionConfiguration) value;
                    label.setText(configuration.type() + " - " + configuration.description());
                    label.setToolTipText(configuration.description());
                }
                return label;
            }
        });

        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                SwingUtilities.invokeLater(() -> {
                    actionSettingsPanel.setRocketActionConfiguration((RocketActionConfiguration) e.getItem());
                    CreateRocketActionSettingsDialog.this.testPanel.clearTest();
                });
            }
        });

        panel.add(comboBox, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTestAndSaveDialog() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelCreateButton = new JPanel();

        JButton buttonCreate = new JButton("Create");
        panelCreateButton.add(buttonCreate);
        buttonCreate.addActionListener(e -> {
            final RocketActionSettings settings = actionSettingsPanel.create();
            currentCallback.create(settings);
            dialog.setVisible(false);
        });

        panel.add(testPanel, BorderLayout.NORTH);
        panel.add(panelCreateButton, BorderLayout.CENTER);

        return panel;
    }

    public void show(CreatedRocketActionSettingsCallback callback) {
        this.currentCallback = callback;
        dialog.setVisible(true);
    }

    private class TestPanel extends JPanel {
        private JPanel panelTest;

        public TestPanel() {
            super(new BorderLayout());

            JPanel panel = new JPanel();
            JButton buttonTest = new JButton("Test");
            buttonTest.addActionListener(e -> SwingUtilities.invokeLater(() -> createTest(actionSettingsPanel.create())));
            panel.add(buttonTest);
            add(panel, BorderLayout.SOUTH);
        }

        public void createTest(RocketActionSettings settings) {
            JPanel panel;
            final Optional<RocketActionUi> actionUiOptional = rocketActionUiRepository.by(settings.type());
            if (actionUiOptional.isPresent()) {
                final RocketActionUi actionUi = actionUiOptional.get();
                panel = new JPanel(new BorderLayout());
                JMenuBar menuBar = new JMenuBar();
                final Component component = actionUi.create(settings);
                menuBar.add(component);
                panel.add(menuBar, BorderLayout.CENTER);
            } else {
                panel = new JPanel(new BorderLayout());
                panel.add(new JLabel("Not found rocket action for type '" + settings.type() + "'"));
            }

            if (panelTest != null) {
                clearTest();
            }
            panelTest = panel;
            add(panel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        public void clearTest() {
            if (panelTest != null) {
                this.remove(panelTest);
                this.revalidate();
                this.repaint();
            }
        }
    }

    private class RocketActionSettingsPanel extends JPanel {
        private final DefaultTableModel tableModel = new DefaultTableModel();
        private final JTable table = new JTable(tableModel);
        private RocketActionConfiguration currentConfiguration;

        public RocketActionSettingsPanel() {
            super(new BorderLayout());
            tableModel.addColumn("Name");
            tableModel.addColumn("Value");
            tableModel.addColumn("Description");
            tableModel.addColumn("Required");
            add(new JScrollPane(table));
        }

        public void setRocketActionConfiguration(RocketActionConfiguration configuration) {
            this.currentConfiguration = configuration;
            while (tableModel.getRowCount() != 0) {
                tableModel.removeRow(0);
            }

            configuration.properties().forEach(c -> {
                Vector<String> row = new Vector<>();
                row.add(c.name());
                row.add("");
                row.add(c.description());
                row.add(Boolean.toString(c.isRequired()));
                tableModel.addRow(row);
            });
        }

        public RocketActionSettings create() {
            if (currentConfiguration == null)
                throw new IllegalStateException("Must be set current selected configuration");

            int rowCount = tableModel.getRowCount();
            Map<String, String> map = new TreeMap<>();
            for (int i = 0; i < rowCount; i++) {
                Object name = tableModel.getValueAt(i, 0);
                Object value = tableModel.getValueAt(i, 1);
                map.put(name.toString(), value.toString());
            }

            return new NewRocketActionSettings(currentConfiguration.type(), map, Collections.emptyList());
        }
    }
}
