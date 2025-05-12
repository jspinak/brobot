package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.awt.geom.Rectangle2D;

@JsonIgnoreProperties({"bounds2D"})
public abstract class RectangleMixin {
    @JsonIgnore
    abstract public Rectangle2D getBounds2D();
}
