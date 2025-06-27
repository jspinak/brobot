package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.awt.geom.Rectangle2D;

/**
 * Jackson mixin for java.awt.geom.Rectangle2D to control JSON serialization.
 * <p>
 * This mixin prevents circular reference issues that can occur during serialization
 * of Rectangle2D objects. The getBounds2D() method returns a reference to the
 * rectangle itself or a new Rectangle2D instance representing the same bounds,
 * which can create infinite recursion during JSON serialization when the serializer
 * attempts to serialize the bounds property.
 * <p>
 * Methods ignored:
 * <ul>
 * <li>getBounds2D() - Returns the bounds as a Rectangle2D (self-reference)</li>
 * </ul>
 *
 * @see java.awt.geom.Rectangle2D
 * @see java.awt.geom.RectangularShape
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
public abstract class Rectangle2DMixin {
    @JsonIgnore
    abstract public Rectangle2D getBounds2D();
}
