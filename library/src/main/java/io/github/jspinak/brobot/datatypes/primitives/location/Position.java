package io.github.jspinak.brobot.datatypes.primitives.location;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import java.io.Serializable;

/**
 * Position is defined by (%w, %h) and can be used to get a location in a Region.
 */
@Embeddable
@Getter
public class Position {

    private double percentW;
    private double percentH;

    public Position() {
        this.percentW = 0.5;
        this.percentH = 0.5;
    }

    public Position(double w, double h) {
        this.percentW = w;
        this.percentH = h;
    }

    public Position(int percentW, int percentH) {
        this.percentW = (double) percentW / 100;
        this.percentH = (double) percentH / 100;
    }

    public Position(Positions.Name positionName) {
        this.percentW = Positions.getCoordinates(positionName).getKey();
        this.percentH = Positions.getCoordinates(positionName).getValue();
    }

    public Position(Positions.Name positionName, double addPercentW, double addPercentH) {
        this.percentW = Positions.getCoordinates(positionName).getKey() + addPercentW;
        this.percentH = Positions.getCoordinates(positionName).getValue() + addPercentH;
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

    public boolean equals(Position position) {
        return percentW == position.getPercentW() && percentH == position.getPercentH();
    }

}
