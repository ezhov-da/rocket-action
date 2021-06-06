package ru.ezhov.rocket.action.types;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.svg.JSVGComponent;
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.caching.CacheFactory;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShowSvgImageRocketActionUi extends AbstractRocketAction {
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
        return "SHOW_SVG_IMAGE";
    }

    @Override
    public String description() {
        return "SVG image show (beta)";
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
            final Optional<File> optionalFile = CacheFactory.getInstance().fromCache(new URL(url));
            Image image = null;
            if (optionalFile.isPresent()) {
                cachedImage = optionalFile.get();
                image = ImageIO.read(optionalFile.get());
            }

            return image;
        }

        @Override
        protected void done() {
            menu.setIcon(IconRepositoryFactory.getInstance().by("image-2x").get());
            try {
                Component component;
                if (settings.settings().containsKey(IMAGE_URL)) {
                    component = new ImagePanel(cachedImage);
                } else {
                    JPanel panel = new JPanel();
                    panel.add(new JLabel(ConfigurationUtil.getValue(settings.settings(), IMAGE_URL)));
                    component = panel;
                }
                menu.add(component);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ImagePanel extends JPanel {
        public ImagePanel(File cachedImage) {
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
                        frame.add(new ShowSvgImageRocketActionUi.ImagePanel(cachedImage));
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

            JPanel panelImage = new JPanel();

            JSVGCanvas svgCanvas = new JSVGCanvas();
            svgCanvas.setDocumentState(JSVGComponent.ALWAYS_DYNAMIC);
            svgCanvas.setURI(cachedImage.toURI().toString());
            panelImage.add(new JSVGScrollPane(svgCanvas));

            add(
                    toolBar,
                    BorderLayout.NORTH
            );
            add(
                    new JScrollPane(panelImage),
                    BorderLayout.CENTER
            );

            //TODO scale
            //JSlider slider = new JSlider(1, 100, 100);
            //toolBar.add(slider);
            //slider.addChangeListener(e -> {
            //imageView.setScale(slider.getValue() / 100D);
            //});

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
