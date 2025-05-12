package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.awt.geom.Rectangle2D;

/**
 * Mixin for java.awt.geom.Rectangle2D to prevent possible circular references.
 */
public abstract class Rectangle2DMixin {
    @JsonIgnore
    abstract public Rectangle2D getBounds2D();
}
