package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mixin for ColorModel to prevent serializing internal classes.
 */
@JsonIgnoreProperties({"colorSpace", "components", "bits", "componentSize", "transparency"})
public abstract class ColorModelMixin {
}
