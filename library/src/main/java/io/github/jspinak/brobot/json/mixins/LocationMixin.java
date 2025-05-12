package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;

import java.util.Optional;

/**
 * Jackson mixin class for Location to prevent serialization of methods that could cause circular references.
 */
@JsonIgnoreProperties({"opposite", "oppositeToLocation", "region", "sikuli"})
public abstract class LocationMixin {

    @JsonIgnore
    abstract public org.sikuli.script.Location sikuli();

    @JsonIgnore
    abstract public Optional<Double> getPercentOfW();

    @JsonIgnore
    abstract public Optional<Double> getPercentOfH();

    @JsonIgnore
    abstract public Location getOpposite();

    @JsonIgnore
    abstract public Location getOppositeTo(Location location);

    @JsonIgnore
    abstract public boolean defined();

    @JsonIgnore
    abstract public Match toMatch();

    @JsonIgnore
    abstract public StateLocation asStateLocationInNullState();

    @JsonIgnore
    abstract public ObjectCollection asObjectCollection();

    @JsonIgnore
    abstract public boolean isDefinedByXY();

    @JsonIgnore
    abstract public boolean isDefinedWithRegion();
}