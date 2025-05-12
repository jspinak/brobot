package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mixin for SikuliLocation class to prevent circular references.
 */
@JsonIgnoreProperties({"screen", "monitor", "offset", "targetOffset"})
public abstract class SikuliLocationMixin {
}
