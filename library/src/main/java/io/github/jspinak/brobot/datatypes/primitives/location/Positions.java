package io.github.jspinak.brobot.datatypes.primitives.location;

import io.github.jspinak.brobot.primatives.Pair;

import java.util.HashMap;
import java.util.Map;

import static io.github.jspinak.brobot.datatypes.primitives.location.Positions.Name.*;

public class Positions {

    public enum Name {
        TOPLEFT, TOPMIDDLE, TOPRIGHT, MIDDLELEFT, MIDDLEMIDDLE, MIDDLERIGHT, BOTTOMLEFT, BOTTOMMIDDLE, BOTTOMRIGHT
    }

    private final static Map<Positions.Name, Pair<Double,Double>> positions = new HashMap<>();
    static {
        positions.put(TOPLEFT, Pair.of(0.0,0.0));
        positions.put(TOPMIDDLE, Pair.of(.5, 0.0));
        positions.put(TOPRIGHT, Pair.of(1.0, 0.0));
        positions.put(MIDDLELEFT, Pair.of(0.0, .5));
        positions.put(MIDDLEMIDDLE, Pair.of(.5, .5));
        positions.put(MIDDLERIGHT, Pair.of(1.0, .5));
        positions.put(BOTTOMLEFT, Pair.of(0.0, 1.0));
        positions.put(BOTTOMMIDDLE, Pair.of(.5, 1.0));
        positions.put(BOTTOMRIGHT, Pair.of(1.0, 1.0));
    }
    
    public static Pair<Double, Double> getCoordinates(Positions.Name position) {
        return positions.get(position);
    }
    
}
