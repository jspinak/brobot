package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.imageUtils.ImageOps;
import lombok.Data;
import org.bytedeco.opencv.opencv_core.Mat;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;

/**
 * The physical representation of an image, stored as a BufferedImage, sent to the database as a byte array, and
 * retrievable in these forms as well as a JavaCV Mat of BGR or HSV.
 */
@Data
public class Image {

    private String name;
    private BufferedImage bufferedImage;

    public Image(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public Image(BufferedImage bufferedImage, String name) {
        this.bufferedImage = bufferedImage;
        this.name = name;
    }

    public Image(Mat BGRmat) {
        this.bufferedImage = BufferedImageOps.fromMat(BGRmat);
    }

    public Image(Mat BGRmat, String name) {
        this.bufferedImage = BufferedImageOps.fromMat(BGRmat);
        this.name = name;
    }

    public Image(String filename) {
        this.bufferedImage = BufferedImageOps.getBuffImgFromFile(filename);
        this.name = filename.replaceFirst("[.][^.]+$", ""); // the filename without extension
    }

    public Image(Pattern pattern) {
        this.bufferedImage = pattern.getBImage();
        this.name = pattern.getName();
    }

    public Image(org.sikuli.script.Image image) {
        this.bufferedImage = image.get();
    }

    /**
     * Returns the BGR representation as a JavaCV Mat.
     * If there is a conversion issue, an empty Mat is returned.
     * @return the image as a Mat.
     */
    public Mat getMatBGR() {
        return ImageOps.getMatBGR(bufferedImage);
    }

    /**
     * Returns the HSV representation as a JavaCV Mat.
     * If there is a conversion issue, an empty Mat is returned.
     * @return the image as a Mat.
     */
    public Mat getMatHSV() {
        return ImageOps.getMatHSV(bufferedImage);
    }

    public boolean isEmpty() {
        return bufferedImage == null;
    }

    public org.sikuli.script.Image sikuli() {
        return new org.sikuli.script.Image(bufferedImage);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public int w() {
        return bufferedImage.getWidth();
    }

    public int h() {
        return bufferedImage.getHeight();
    }

    public static Image getEmptyImage() {
        Region r = new Region();
        BufferedImage bufferedImage = new BufferedImage(r.w(), r.h(), TYPE_BYTE_BINARY);
        Image image = new Image(bufferedImage);
        image.name = "empty scene";
        return image;
    }

}
