package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;

/**
 * Mixin for Brobot Image class to prevent serializing BufferedImage.
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
