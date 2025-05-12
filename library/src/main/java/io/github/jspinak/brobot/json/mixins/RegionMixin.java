package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mixin for Region class to prevent circular references.
 */
@JsonIgnoreProperties({"location", "sikuli", "javaRect"})
public abstract class RegionMixin {
    @JsonIgnore
    abstract public org.sikuli.script.Region sikuli();

    @JsonIgnore
    abstract public org.sikuli.script.Location getLocation();
}