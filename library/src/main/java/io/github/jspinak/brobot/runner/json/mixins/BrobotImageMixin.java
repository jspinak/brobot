package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;

/**
 * Jackson mixin for Brobot's Image class to control JSON serialization.
 * <p>
 * This mixin prevents serialization of heavyweight image objects and internal
 * representations that are not suitable for JSON format. BrobotImage contains
 * multiple representations of the same image data (BufferedImage, OpenCV Mat
 * objects, Sikuli Image) which would cause redundancy and memory issues if
 * serialized directly.
 * <p>
 * Properties ignored:
 * <ul>
 * <li>bufferedImage - Java AWT image representation</li>
 * <li>matBGR - OpenCV Mat in BGR color space</li>
 * <li>matHSV - OpenCV Mat in HSV color space</li>
 * <li>sikuli - Sikuli framework image wrapper</li>
 * </ul>
 *
 * @see io.github.jspinak.brobot.datatypes.primitives.image.Image
 * @see java.awt.image.BufferedImage
 * @see org.bytedeco.opencv.opencv_core.Mat
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({"bufferedImage", "matBGR", "matHSV", "sikuli"})
public abstract class BrobotImageMixin {
    @JsonIgnore
    abstract public BufferedImage getBufferedImage();

    @JsonIgnore
    abstract public Mat getMatBGR();

    @JsonIgnore
    abstract public Mat getMatHSV();

    @JsonIgnore
    abstract public org.sikuli.script.Image sikuli();
}
