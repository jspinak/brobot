package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.imageUtils.ImageOps;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import org.sikuli.script.Image;

import java.awt.image.BufferedImage;

/**
 * The Image Data Transfer Object is needed to centralize the code related to saving images to database and also
 * for retrieving Mat objects from the image's BufferedImage. The reason is that both Pattern and Image use this
 * code. Pattern already contains a SiukuliX Image since it extends the SikuliX Pattern. Having a
 * Brobot Image object would duplicate the stored BufferedImage. There are other uses for the SikuliX Image object
 * outside of Pattern, and the Brobot Image object encapsulates this together with the functions of the ImageDTO.
 */
@Embeddable
public class ImageDTO {

    @Lob
    private byte[] bytes; // BGR image as a byte array for persistence

    public BufferedImage getBufferedImage() {
        return BufferedImageOps.fromByteArray(bytes);
    }

    public void setBytesWithImage(Image image) {
        bytes = ImageOps.getBytes(image);
    }

    public void setBytesWithBufferedImage(BufferedImage bufferedImage) {
        bytes = BufferedImageOps.toByteArray(bufferedImage);
    }

}
