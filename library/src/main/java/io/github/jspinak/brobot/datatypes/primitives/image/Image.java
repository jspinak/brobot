package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.imageUtils.ImageOps;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;

/**
 * The physical representation of an image, stored as a BufferedImage, sent to the database as a byte array, and
 * retrievable in these forms as well as a JavaCV Mat of BGR or HSV.
 */
@Embeddable
@Getter
public class Image extends org.sikuli.script.Image {

    @Embedded
    private ImageDTO imageDTO = new ImageDTO();

    public Image(BufferedImage bufferedImage) {
        super(bufferedImage);
    }

    public Image(Mat BGRmat) {
        super(BufferedImageOps.fromMat(BGRmat));
    }

    public Image(String filename) {
        super(BufferedImageOps.getBuffImgFromFile(filename));
        setName(filename.replaceFirst("[.][^.]+$", "")); // the filename without extension
    }

    public Image(Pattern pattern) {
        super(pattern.getBImage());
        setName(pattern.getName());
    }

    /**
     * Returns the BGR representation as a JavaCV Mat.
     * If there is a conversion issue, an empty Mat is returned.
     * @return the image as a Mat.
     */
    public Mat getMatBGR() {
        return ImageOps.getMatBGR(getImage());
    }

    /**
     * Returns the HSV representation as a JavaCV Mat.
     * If there is a conversion issue, an empty Mat is returned.
     * @return the image as a Mat.
     */
    public Mat getMatHSV() {
        return ImageOps.getMatHSV(getImage());
    }

    public boolean isEmpty() {
        return get() == null;
    }

}
