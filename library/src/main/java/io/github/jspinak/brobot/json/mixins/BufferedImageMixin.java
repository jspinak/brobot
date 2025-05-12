package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Mixin for BufferedImage to prevent serializing internal classes.
 */
@JsonIgnoreProperties({"raster", "colorModel", "data", "properties", "propertyNames", "graphics", "accelerationPriority"})
public abstract class BufferedImageMixin {
    @JsonIgnore
    abstract public WritableRaster getRaster();

    @JsonIgnore
    abstract public ColorModel getColorModel();

    @JsonIgnore
    abstract public Raster getData();
}
