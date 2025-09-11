package io.github.jspinak.brobot.runner.json.mixins;

import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for java.awt.image.BufferedImage to control JSON serialization.
 *
 * <p>This mixin prevents serialization of BufferedImage's internal implementation details and
 * heavyweight objects that are not suitable for JSON representation. These properties are typically
 * reconstructed from pixel data rather than serialized directly.
 *
 * <p>Properties ignored include:
 *
 * <ul>
 *   <li>raster - Pixel data storage (handled separately)
 *   <li>colorModel - Color space information
 *   <li>data - Raw image data
 *   <li>properties - Image property map
 *   <li>propertyNames - Property name array
 *   <li>graphics - Graphics context
 *   <li>accelerationPriority - Hardware acceleration hint
 *   <li>source - Image source (OffScreenImageSource) that causes serialization issues
 * </ul>
 *
 * @see java.awt.image.BufferedImage
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({
    "raster",
    "colorModel",
    "data",
    "properties",
    "propertyNames",
    "graphics",
    "accelerationPriority",
    "source"
})
public abstract class BufferedImageMixin {
    @JsonIgnore
    public abstract WritableRaster getRaster();

    @JsonIgnore
    public abstract ColorModel getColorModel();

    @JsonIgnore
    public abstract Raster getData();
}
