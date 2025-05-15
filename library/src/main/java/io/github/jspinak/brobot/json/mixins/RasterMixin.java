package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mixin for Raster to prevent serializing internal classes.
 */
@JsonIgnoreProperties({"raster", "colorModel", "data", "properties", "propertyNames",
        "graphics", "accelerationPriority", "dataElements", "dataOffsets",
        "dataBuffer", "dataOffset", "scanlineStride", "pixelStride"})
public abstract class RasterMixin {
}
