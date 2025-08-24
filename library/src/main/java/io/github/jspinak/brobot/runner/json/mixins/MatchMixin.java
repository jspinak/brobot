package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * Jackson mixin for Brobot's Match class to control JSON serialization.
 * <p>
 * This mixin prevents serialization of heavyweight objects and fields that
 * could cause null reference exceptions or memory issues during JSON processing.
 * Match objects often contain references to image data and OpenCV Mat objects
 * that should not be directly serialized.
 * <p>
 * Properties ignored:
 * <ul>
 * <li>mat - OpenCV Mat object containing raw image data</li>
 * <li>image - BufferedImage representation</li>
 * <li>matBGR - BGR color space Mat object</li>
 * <li>matHSV - HSV color space Mat object</li>
 * <li>sikuli - Reference to Sikuli Match object</li>
 * </ul>
 *
 * @see io.github.jspinak.brobot.model.match.Match
 * @see org.bytedeco.opencv.opencv_core.Mat
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({"mat", "image", "matBGR", "matHSV", "sikuli"})
public abstract class MatchMixin {
    @JsonIgnore
    abstract public Mat getMat();

}