package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.imageUtils.ImageOps;
import jakarta.persistence.*;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;

/**
 * The physical representation of an image, stored as a BufferedImage, sent to the database as a byte array, and
 * retrievable in these forms as well as a JavaCV Mat of BGR or HSV.
 */
@Entity
@Getter
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    @Transient
    private BufferedImage bufferedImage;
    @Embedded
    private ImageDTO imageDTO = new ImageDTO();

    public Image(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public Image(Mat BGRmat) {
        this.bufferedImage = BufferedImageOps.fromMat(BGRmat);
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

    /**
     * Convert BufferedImage to a byte array to store in the database.
     */
    public void setBytesForPersistence() {
        imageDTO.setBytesWithBufferedImage(bufferedImage);
    }

    /**
     * Set BufferedImage after loading object from database.
     */
    public void setBufferedImageFromBytes() {
        bufferedImage = imageDTO.getBufferedImage();
    }

    public int w() {
        return bufferedImage.getWidth();
    }

    public int h() {
        return bufferedImage.getHeight();
    }

}
