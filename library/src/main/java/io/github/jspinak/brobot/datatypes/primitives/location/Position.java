package io.github.jspinak.brobot.datatypes.primitives.location;

import io.github.jspinak.brobot.primatives.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Position is defined by (%w, %h) and can be used to get a location in a Region.
 */
public class Position {

    private double percentW;
    private double percentH;

    public enum Name {
        TOPLEFT, TOPMIDDLE, TOPRIGHT, MIDDLELEFT, MIDDLEMIDDLE, MIDDLERIGHT, BOTTOMLEFT, BOTTOMMIDDLE, BOTTOMRIGHT
    }

    private final Map<Name, Pair<Double,Double>> positions = new HashMap<>();
    {
        positions.put(Name.TOPLEFT, Pair.of(0.0,0.0));
        positions.put(Name.TOPMIDDLE, Pair.of(.5, 0.0));
        positions.put(Name.TOPRIGHT, Pair.of(1.0, 0.0));
        positions.put(Name.MIDDLELEFT, Pair.of(0.0, .5));
        positions.put(Name.MIDDLEMIDDLE, Pair.of(.5, .5));
        positions.put(Name.MIDDLERIGHT, Pair.of(1.0, .5));
        positions.put(Name.BOTTOMLEFT, Pair.of(0.0, 1.0));
        positions.put(Name.BOTTOMMIDDLE, Pair.of(.5, 1.0));
        positions.put(Name.BOTTOMRIGHT, Pair.of(1.0, 1.0));
    }

    public Position(double percentW, double percentH) {
        this.percentW = percentW;
        this.percentH = percentH;
    }

    public Position(Name positionName) {
        this.percentW = positions.get(positionName).getKey();
        this.percentH = positions.get(positionName).getValue();
    }

    public Position(Name positionName, double addPercentW, double addPercentH) {
        this.percentW = positions.get(positionName).getKey() + addPercentW;
        this.percentH = positions.get(positionName).getValue() + addPercentH;
    }

    public double getPercentW() {
        return percentW;
    }

    public double getPercentH() {
        return percentH;
    }

    public void addPercentW(double addW) {
        percentW += addW;
    }

    public void addPercentH(double addH) {
        percentH += addH;
    }

    public void multiplyPercentW(double mult) {
        percentW = percentW * mult;
    }

    public void multiplyPercentH(double mult) {
        percentH = percentH * mult;
    }

    @Override
    public String toString() {
        return String.format("P[%.1f.%.1f]", percentW, percentH);
    }

}
