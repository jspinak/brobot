package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mixin for WritableRaster to prevent serializing internal classes.
 */
@JsonIgnoreProperties({"dataElements", "dataBuffer", "numBands", "numDataElements", "parent", "sampleModel"})
public abstract class WritableRasterMixin {
}