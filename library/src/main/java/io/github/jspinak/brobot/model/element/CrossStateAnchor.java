package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.model.state.StateObject;
import lombok.Getter;
import lombok.Setter;

/**
 * Enhanced anchor that can reference matches from other states.
 * This allows regions to be defined using reference points from objects
 * in different states, enabling more flexible and dynamic region definitions.
 */
@Getter
@Setter
public class CrossStateAnchor extends Anchor {
    
    private String sourceStateName;
    private String sourceObjectName;
    private StateObject.Type sourceType;
    private SearchRegionOnObject.AdjustOptions adjustments;

    public CrossStateAnchor() {
        super();
        this.adjustments = new SearchRegionOnObject.AdjustOptions();
    }

    public CrossStateAnchor(Positions.Name anchorInNewDefinedRegion, Positions.Name positionInMatch) {
        super(anchorInNewDefinedRegion, new Position(positionInMatch));
        this.adjustments = new SearchRegionOnObject.AdjustOptions();
    }

    /**
     * Creates a Location from a match, applying adjustments.
     * @param matchLocation The location of the matched object
     * @param matchRegion The region of the matched object
     * @return The adjusted location for this anchor
     */
    public Location getAdjustedLocation(Location matchLocation, Region matchRegion) {
        // Calculate the anchor location based on position within the match
        double x = matchRegion.getX() + (matchRegion.getW() * getPositionInMatch().getPercentW());
        double y = matchRegion.getY() + (matchRegion.getH() * getPositionInMatch().getPercentH());
        
        // Apply adjustments
        return new Location(
            (int)(x + adjustments.getXAdjust()),
            (int)(y + adjustments.getYAdjust())
        );
    }

    public static class Builder {
        private final CrossStateAnchor anchor = new CrossStateAnchor();

        public Builder anchorInNewDefinedRegion(Positions.Name position) {
            anchor.setAnchorInNewDefinedRegion(position);
            return this;
        }

        public Builder positionInMatch(Positions.Name position) {
            anchor.setPositionInMatch(new Position(position));
            return this;
        }

        public Builder sourceState(String stateName) {
            anchor.sourceStateName = stateName;
            return this;
        }

        public Builder sourceObject(String objectName) {
            anchor.sourceObjectName = objectName;
            return this;
        }

        public Builder sourceType(StateObject.Type type) {
            anchor.sourceType = type;
            return this;
        }

        public Builder adjustX(int x) {
            anchor.adjustments.setXAdjust(x);
            return this;
        }

        public Builder adjustY(int y) {
            anchor.adjustments.setYAdjust(y);
            return this;
        }

        public Builder adjustments(int x, int y) {
            anchor.adjustments = new SearchRegionOnObject.AdjustOptions(x, y, 0, 0);
            return this;
        }

        public CrossStateAnchor build() {
            // Validate required fields
            if (anchor.getAnchorInNewDefinedRegion() == null) {
                anchor.setAnchorInNewDefinedRegion(Positions.Name.MIDDLEMIDDLE);
            }
            if (anchor.getPositionInMatch() == null) {
                anchor.setPositionInMatch(new Position(Positions.Name.MIDDLEMIDDLE));
            }
            return anchor;
        }
    }
}