package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Jackson mixin for java.awt.image.BufferedImage to control JSON serialization.
 * <p>
 * This mixin prevents serialization of BufferedImage's internal implementation
 * details and heavyweight objects that are not suitable for JSON representation.
 * These properties are typically reconstructed from pixel data rather than
 * serialized directly.
 * <p>
 * Properties ignored include:
 * <ul>
 * <li>raster - Pixel data storage (handled separately)</li>
 * <li>colorModel - Color space information</li>
 * <li>data - Raw image data</li>
 * <li>properties - Image property map</li>
 * <li>propertyNames - Property name array</li>
 * <li>graphics - Graphics context</li>
 * <li>accelerationPriority - Hardware acceleration hint</li>
 * </ul>
 *
 * @see java.awt.image.BufferedImage
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
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
