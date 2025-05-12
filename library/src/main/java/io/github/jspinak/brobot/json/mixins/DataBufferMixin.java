package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mixin for DataBuffer to prevent serializing internal classes.
 */
@JsonIgnoreProperties({"data", "bankData", "offsets", "size", "dataType"})
public abstract class DataBufferMixin {
}
