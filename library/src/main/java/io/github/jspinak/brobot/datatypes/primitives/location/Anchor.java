package io.github.jspinak.brobot.datatypes.primitives.location;

import lombok.Getter;
import lombok.Setter;

/**
 * Anchors define
 * 1) A Position in the Image Match or Region
 * 2) A Position in the Region to define
 *
 * A point defined with Position #1 is used to define part of a Region.
 * Position #2 gives us the part of the Region to define.
 * If Position #2 is a 'middle' Position, it defines 1 border of the Region.
 * If Position #2 is a 'corner' Position, it defines 2 borders of the Region.
 * For example, for Position #2:
 *   MIDDLELEFT defines the left border of the Region
 *   TOPLEFT defines the left and top borders of the Region
 */
@Getter
@Setter
public class Anchor {

    private Positions.Name anchorInNewDefinedRegion; // the border of the region to define
    private Position positionInMatch; // the location in the match to use as a defining point

    // JPA requires an empty constructor
    public Anchor() {}

    public Anchor(Positions.Name anchorInNewDefinedRegion, Position positionInMatch) {
        this.anchorInNewDefinedRegion = anchorInNewDefinedRegion;
        this.positionInMatch = positionInMatch;
    }

    @Override
    public String toString() {
        return "Anchor:" +
                " anchorInNewDefinedRegion=" + anchorInNewDefinedRegion +
                " positionInMatch=" + positionInMatch;
    }

    public boolean equals(Anchor anchor) {
        return anchorInNewDefinedRegion == anchor.getAnchorInNewDefinedRegion() &&
                positionInMatch.equals(anchor.getPositionInMatch());
    }

}
