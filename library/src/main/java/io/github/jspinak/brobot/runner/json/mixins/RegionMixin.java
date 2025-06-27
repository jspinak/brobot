package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for Brobot's Region class to control JSON serialization.
 * <p>
 * This mixin prevents circular reference issues and infinite recursion during
 * JSON serialization by ignoring properties that would create object graph cycles.
 * It specifically excludes Sikuli-related objects and location references that
 * could lead to serialization failures.
 * <p>
 * Properties handled:
 * <ul>
 * <li>Ignored via @JsonIgnoreProperties: location, sikuli, javaRect</li>
 * <li>Explicitly ignored methods: sikuli(), getLocation()</li>
 * </ul>
 *
 * @see io.github.jspinak.brobot.model.element.Region
 * @see org.sikuli.script.Region
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({"location", "sikuli", "javaRect"})
public abstract class RegionMixin {
    @JsonIgnore
    abstract public org.sikuli.script.Region sikuli();

    @JsonIgnore
    abstract public org.sikuli.script.Location getLocation();
}