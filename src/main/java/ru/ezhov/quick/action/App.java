package ru.ezhov.quick.action;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.time.Instant;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable ex) {
                //
            }

            String path = null;

            if (args.length > 0) {
                path = args[0];
            }

            try {
                UiQuickActionService actionService = new UiQuickActionService(path);
                JDialog dialog = new JDialog();
                dialog.setJMenuBar(actionService.createMenu(dialog));
                dialog.setUndecorated(true);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
            } catch (UiQuickActionServiceException e) {
                e.printStackTrace();
            }
        });
    }
}
