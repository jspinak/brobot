package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * Mixin for Pattern class to prevent serializing problematic fields.
 */
@JsonIgnoreProperties({"image", "mat", "matHSV", "regions", "matchHistory", "sikuli"})
public abstract class PatternMixin {
    @JsonIgnore
    abstract public Image getImage();

    @JsonIgnore
    abstract public Mat getMat();

    @JsonIgnore
    abstract public Mat getMatHSV();

    @JsonIgnore
    abstract public org.sikuli.script.Pattern sikuli();
}
