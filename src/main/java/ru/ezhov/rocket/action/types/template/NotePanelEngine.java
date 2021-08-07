package ru.ezhov.rocket.action.types.template;

import ru.ezhov.rocket.action.notification.NotificationFactory;
import ru.ezhov.rocket.action.notification.NotificationType;
import ru.ezhov.rocket.action.template.domain.Engine;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class NotePanelEngine extends JPanel {
    private final PanelEngine panelEngine;
    private final Engine engine;
    private final String originText;
    private final JLabel labelText;

    public NotePanelEngine(String originText, Engine engine) {
        super(new BorderLayout());
        this.engine = engine;
        this.originText = originText;
        List<String> words = engine.words(originText);
        this.panelEngine = new PanelEngine(words, new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    apply();
                }
            }
        });
        add(panelEngine, BorderLayout.CENTER);

        JButton button = new JButton("apply template (CTRL + ENTER field)");
        JPanel panelButton = new JPanel(new BorderLayout());
        panelButton.add(button, BorderLayout.NORTH);
        labelText = new JLabel(originText);
        panelButton.add(button, BorderLayout.NORTH);
        panelButton.add(labelText, BorderLayout.CENTER);
        add(panelButton, BorderLayout.SOUTH);

        button.addActionListener(e -> apply());
    }

    private void apply() {
        final String finalText = engine.apply(originText, panelEngine.apply());
        SwingUtilities.invokeLater(() -> {
            labelText.setText(finalText);
            Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = defaultToolkit.getSystemClipboard();
            clipboard.setContents(new StringSelection(finalText), null);

            NotificationFactory.getInstance().show(NotificationType.INFO, "Template text copy to clipboard");
        });
    }
}
