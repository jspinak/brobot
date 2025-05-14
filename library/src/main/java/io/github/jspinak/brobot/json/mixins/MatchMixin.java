package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * Mixin for Match class to prevent serializing fields that could cause null reference issues.
 */
@JsonIgnoreProperties({"mat", "image", "matBGR", "matHSV", "sikuli"})
public abstract class MatchMixin {
    @JsonIgnore
    abstract public Mat getMat();

}