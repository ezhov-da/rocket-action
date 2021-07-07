package ru.ezhov.rocket.action.icon;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleOp;
import net.sf.image4j.codec.ico.ICODecoder;
import net.sf.image4j.codec.ico.ICOImage;
import ru.ezhov.rocket.action.caching.CacheFactory;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IconService {
    private static final Logger LOG = Logger.getLogger(IconService.class.getName());

    public static Icon load(Optional<String> iconUrl, Icon defaultIcon) {
        Icon resultIcon = defaultIcon;
        if (iconUrl.isPresent()) {
            final String urlAsString = iconUrl.get();
            if ("".equals(urlAsString)) {
                return defaultIcon;
            }
            try {
                final Optional<File> optionalFile = CacheFactory.getInstance().fromCache(new URL(urlAsString));
                if (optionalFile.isPresent()) {
                    File file = optionalFile.get();
                    BufferedImage image;
                    if (urlAsString.endsWith("ico")) {
                        final List<ICOImage> bufferedImages = ICODecoder.readExt(file);
                        image = bufferedImages
                                .get(bufferedImages.size() - 1)
                                .getImage()
                        ;
                    } else {
                        image = ImageIO.read(optionalFile.get());
                    }

                    resultIcon = new ImageIcon(handleICOImage(image));
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, String.format("Exception when load icon url='%s'", urlAsString), e);
            }
        }
        return resultIcon;
    }

    private static BufferedImage handleICOImage(BufferedImage icoImage) throws IOException {
        ResampleOp resampleOp = new ResampleOp(16, 16);
        resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Oversharpened);
        return resampleOp.filter(icoImage, null);
    }
}
