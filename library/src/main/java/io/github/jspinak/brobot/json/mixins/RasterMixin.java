package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mixin for Raster to prevent serializing internal classes.
 */
@JsonIgnoreProperties({"dataElements", "dataBuffer", "numBands", "numDataElements", "parent", "sampleModel", "dataBuffer", "numDataElements"})
public abstract class RasterMixin {
}
