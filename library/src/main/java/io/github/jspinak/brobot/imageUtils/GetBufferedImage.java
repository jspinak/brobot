package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Component
public class GetBufferedImage {

    public BufferedImage fromFile(String path) throws IOException {
        File f = new File(path);
        return ImageIO.read(Objects.requireNonNull(f));
    }

    public BufferedImage fromScreen(Region region) {
        return new Screen().capture(region).getImage();
    }
}
