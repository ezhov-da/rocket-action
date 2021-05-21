package ru.ezhov.rocket.action.types;

import com.google.common.hash.Hashing;
import org.jdesktop.swingx.JXImageView;
import org.jdesktop.swingx.JXPanel;
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ShowImageRocketActionUi extends AbstractRocketAction {
    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String IMAGE_URL = "imageUrl";

    @Override
    public Component create(RocketActionSettings settings) {
        JMenu menu = new JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL));
        menu.setToolTipText(ConfigurationUtil.getValue(settings.settings(), DESCRIPTION));
        menu.setIcon(
                new ImageIcon(this.getClass().getResource("/load_16x16.gif"))
        );
        new LoadImageWorker(menu, settings).execute();
        return menu;
    }

    @Override
    public String type() {
        return "SHOW_IMAGE";
    }

    @Override
    public String description() {
        return "description";
    }

    @Override
    public List<RocketActionConfigurationProperty> properties() {
        return Arrays.asList(
                createRocketActionProperty(LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, "TEST", true),
                createRocketActionProperty(IMAGE_URL, "TEST", true)
        );
    }

    private class LoadImageWorker extends SwingWorker<Image, String> {
        private String cacheFolder = System.getProperty("java.io.tmpdir") + File.separator;
        private JMenu menu;
        private RocketActionSettings settings;
        private File cachedImage;

        public LoadImageWorker(JMenu menu, RocketActionSettings settings) {
            this.menu = menu;
            this.settings = settings;
        }

        @Override
        protected Image doInBackground() throws Exception {
            String url = ConfigurationUtil.getValue(settings.settings(), IMAGE_URL);
            String sha256hexUrl = Hashing.sha256()
                    .hashString(url, StandardCharsets.UTF_8)
                    .toString();
            File file = new File(cacheFolder + "si-" + sha256hexUrl);
            cachedImage = file;
            if (file.exists()) {
                return ImageIO.read(file);
            } else {
                try (InputStream inputStream = new URL(url).openStream();
                     FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    byte[] buf = new byte[256];
                    int p;
                    while ((p = inputStream.read(buf)) != -1) {
                        fileOutputStream.write(buf, 0, p);
                    }
                    return ImageIO.read(file);
                }
            }
        }

        @Override
        protected void done() {
            menu.setIcon(
                    new ImageIcon(this.getClass().getResource("/image_16x16.png"))
            );
            try {
                Component component;
                if (settings.settings().containsKey(IMAGE_URL)) {
                    component = new ImagePanel(this.get(), cachedImage);
                } else {
                    JPanel panel = new JPanel();
                    panel.add(new JLabel(ConfigurationUtil.getValue(settings.settings(), IMAGE_URL)));
                    component = panel;
                }
                menu.add(component);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private class ImagePanel extends JXPanel {
        public ImagePanel(Image image, File cachedImage) {
            super(new BorderLayout());
            final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
            int widthNew = (int) (dimension.width * 0.5);
            int heightNew = (int) (dimension.height * 0.5);

            Dimension newDimension = new Dimension(widthNew, heightNew);

            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);

            toolBar.add(new AbstractAction() {
                {
                    putValue(Action.NAME, "Open in window");
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        JFrame frame = new JFrame(cachedImage.getAbsolutePath());
                        frame.add(new ImagePanel(image, cachedImage));
                        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                        frame.setSize((int) (dimension.width * 0.8), (int) (dimension.height * 0.8));
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                    });
                }
            });

            setPreferredSize(newDimension);
            setMaximumSize(newDimension);
            setMinimumSize(newDimension);

            JXPanel panelImage = new JXPanel(new BorderLayout());
            JXImageView imageView = new JXImageView();
            imageView.setImage(image);
            imageView.setAutoscrolls(true);
            panelImage.add(
                    imageView,
                    BorderLayout.CENTER
            );

            add(
                    toolBar,
                    BorderLayout.NORTH
            );
            add(
                    panelImage,
                    BorderLayout.CENTER
            );

            JSlider slider = new JSlider(1, 100, 100);
            toolBar.add(slider);
            slider.addChangeListener(e -> {
                imageView.setScale(slider.getValue() / 100D);
            });

            final JLabel cachedLabel = new JLabel("Cached: " + cachedImage.getAbsolutePath());
            cachedLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().open(cachedImage.getParentFile());
                            } catch (IOException ioException) {

                                ioException.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        cachedLabel.setForeground(Color.BLUE);
                    });
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        cachedLabel.setForeground(new JLabel().getForeground());
                    });
                }
            });
            add(
                    cachedLabel,
                    BorderLayout.SOUTH
            );
        }
    }
}
