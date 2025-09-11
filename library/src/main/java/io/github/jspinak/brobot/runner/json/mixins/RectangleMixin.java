package io.github.jspinak.brobot.runner.json.mixins;

import java.awt.geom.Rectangle2D;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for java.awt.Rectangle to control JSON serialization.
 *
 * <p>This mixin prevents circular reference issues specific to Rectangle objects by ignoring the
 * bounds2D property. Rectangle extends Rectangle2D and the getBounds2D() method can create
 * serialization loops when Jackson attempts to serialize the full object graph. The bounds
 * information is already captured through Rectangle's x, y, width, and height properties.
 *
 * <p>Properties/Methods ignored:
 *
 * <ul>
 *   <li>bounds2D - Rectangle2D representation of bounds (redundant)
 *   <li>getBounds2D() - Method returning Rectangle2D bounds
 * </ul>
 *
 * @see java.awt.Rectangle
 * @see java.awt.geom.Rectangle2D
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({"bounds2D"})
public abstract class RectangleMixin {
    @JsonIgnore
    public abstract Rectangle2D getBounds2D();
}
