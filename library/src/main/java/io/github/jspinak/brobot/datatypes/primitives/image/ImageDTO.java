package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * The Image Data Transfer Object converts an Image to a byte array when storing in a database,
 * and back to a BufferedImage after retrieving from the database.
 */
@Embeddable
public class ImageDTO {

    @Lob
    private byte[] bytes; // BGR image as a byte array for persistence

    public BufferedImage getBufferedImage() {
        return BufferedImageOps.fromByteArray(bytes);
    }

    public void setBytesWithBufferedImage(BufferedImage bufferedImage) {
        bytes = BufferedImageOps.toByteArray(bufferedImage);
    }

}
