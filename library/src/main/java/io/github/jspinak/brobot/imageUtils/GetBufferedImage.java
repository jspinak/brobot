package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.database.primitives.region.Region;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Component
public class GetBufferedImage {

    private ImageUtils imageUtils;

    public GetBufferedImage(ImageUtils imageUtils) {
        this.imageUtils = imageUtils;
    }

    public BufferedImage getBufferedImage(String path) throws IOException {
        File f = new File(path);
        return ImageIO.read(Objects.requireNonNull(f));
    }
}
