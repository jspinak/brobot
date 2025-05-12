package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mixin for SearchRegions to prevent circular references.
 */
@JsonIgnoreProperties({"regions", "deepCopy"})
public abstract class SearchRegionsMixin {
}
