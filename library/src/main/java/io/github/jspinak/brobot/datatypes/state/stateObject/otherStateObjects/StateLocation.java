package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.datatypes.primitives.location.*;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Getter;
import lombok.Setter;

/**
 * A StateLocation belongs to a State and usually has a Location that
 * has a special meaning for its owner State. For example, clicking on
 * this Location has an effect in the owner State but not in other States.
 */
@Getter
@Setter
public class StateLocation implements StateObject {

    private StateObject.Type objectType = StateObject.Type.LOCATION;
    private String name;
    private Location location;
    private String ownerStateName = "null";
    private int staysVisibleAfterClicked = 100;
    private int probabilityExists = 100; // probability something can be acted on at this location
    private int timesActedOn = 0;
    private Position position;
    private Anchors anchors; // just one, but defined with Anchors b/c it's a StateObject
    private MatchHistory matchHistory = new MatchHistory(); // not used yet


    public String getId() {
        return objectType.name() + name + location.getX() + location.getY();
    }

    public boolean defined() {
        return location != null;
    }

    public void addTimesActedOn() {
        timesActedOn++;
    }

    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder()
                .withLocations(this)
                .build();
    }

    @Override
    public String toString() {
        return "StateLocation:" +
                " name=" + name +
                " ownerStateName=" + ownerStateName +
                " location=" + location +
                " position=" + position +
                " anchors=" + anchors;
    }

    public static class Builder {
        private String name = "";
        private Location location;
        private String ownerStateName = "null";
        private int staysVisibleAfterClicked = 100;
        private int probabilityExists = 100; // probability something can be acted on at this location
        private int timesActedOn = 0;
        private Position position = new Position(.5, .5);
        private Anchors anchors = new Anchors();
        private MatchHistory matchHistory = new MatchHistory(); // not used yet

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setLocation(Location location) {
            this.location = location;
            return this;
        }

        public Builder setLocation(int x, int y) {
            this.location = new Location(x, y);
            return this;
        }

        public Builder setOwnerStateName(String stateName) {
            this.ownerStateName = stateName;
            return this;
        }

        public Builder setStaysVisibleAfterClicked(int staysVisibleAfterClicked) {
            this.staysVisibleAfterClicked = staysVisibleAfterClicked;
            return this;
        }

        public Builder setProbabilityExists(int probabilityExists) {
            this.probabilityExists = probabilityExists;
            return this;
        }

        public Builder setTimesActedOn(int timesActedOn) {
            this.timesActedOn = timesActedOn;
            return this;
        }

        public Builder setPosition(Positions.Name locationPosition) {
            this.position = new Position(locationPosition);
            return this;
        }

        public Builder setPosition(int percentW, int percentH) {
            this.position = new Position(percentW, percentH);
            return this;
        }

        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        public Builder setAnchor(Positions.Name cornerOfRegionToDefine) {
            this.anchors.add(new Anchor(cornerOfRegionToDefine, position));
            return this;
        }

        public Builder setAnchors(Anchors anchors) {
            this.anchors = anchors;
            return this;
        }

        public Builder setMatchHistory(MatchHistory matchHistory) {
            this.matchHistory = matchHistory;
            return this;
        }

        public StateLocation build() {
            StateLocation stateLocation = new StateLocation();
            stateLocation.name = name;
            stateLocation.location = location;
            stateLocation.ownerStateName = ownerStateName;
            stateLocation.staysVisibleAfterClicked = staysVisibleAfterClicked;
            stateLocation.probabilityExists = probabilityExists;
            stateLocation.timesActedOn = timesActedOn;
            stateLocation.position = position;
            stateLocation.anchors = anchors;
            stateLocation.matchHistory = matchHistory;
            return stateLocation;
        }
    }
}
