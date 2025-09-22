package io.github.jspinak.brobot.runner.json.mixins;

import java.awt.image.BufferedImage;

import org.bytedeco.opencv.opencv_core.Mat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Jackson mixin for Brobot's Image class to control JSON serialization.
 *
 * <p>This mixin prevents serialization of heavyweight image objects and internal representations
 * that are not suitable for JSON format. BrobotImage contains multiple representations of the same
 * image data (BufferedImage, OpenCV Mat objects, Sikuli Image) which would cause redundancy and
 * memory issues if serialized directly.
 *
 * <p>Properties ignored:
 *
 * <ul>
 *   <li>bufferedImage - Java AWT image representation
 *   <li>matBGR - OpenCV Mat in BGR color space
 *   <li>matHSV - OpenCV Mat in HSV color space
 *   <li>sikuli - Sikuli framework image wrapper
 * </ul>
 *
 * @see io.github.jspinak.brobot.datatypes.primitives.image.Image
 * @see java.awt.image.BufferedImage
 * @see org.bytedeco.opencv.opencv_core.Mat
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({"matBGR", "matHSV", "sikuli", "empty"})
public abstract class BrobotImageMixin {
    // Override the @JsonIgnore on the getBufferedImage method to allow serialization
    // when using BrobotObjectMapper with custom serializers
    @JsonSerialize(using = io.github.jspinak.brobot.json.serializers.BufferedImageSerializer.class)
    @JsonDeserialize(
            using = io.github.jspinak.brobot.json.serializers.BufferedImageDeserializer.class)
    public abstract BufferedImage getBufferedImage();

    @JsonIgnore
    public abstract Mat getMatBGR();

    @JsonIgnore
    public abstract Mat getMatHSV();

    @JsonIgnore
    public abstract org.sikuli.script.Image sikuli();
}
