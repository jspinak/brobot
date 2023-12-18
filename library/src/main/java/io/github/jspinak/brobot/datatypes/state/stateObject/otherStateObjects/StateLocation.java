package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Data;

/**
 * A StateLocation belongs to a State and usually has a Location that
 * has a special meaning for its owner State. For example, clicking on
 * this Location has an effect in the owner State but not in other States.
 */
@Data
public class StateLocation implements StateObject {

    private String name;
    private Location location;
    private String ownerStateName;
    private int staysVisibleAfterClicked = 100;
    private int probabilityExists = 100; // probability something can be acted on at this location
    private int timesActedOn = 0;
    private Position position;
    private Anchors anchors; // just one, but defined with Anchors b/c it's a StateObject
    private MatchHistory matchHistory = new MatchHistory(); // not used yet

    private StateLocation() {}

    public boolean defined() {
        return location != null;
    }

    public void addTimesActedOn() {
        timesActedOn++;
    }

    public void addSnapshot(MatchSnapshot matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    public void setPosition(Position.Name positionName) {
        this.position = new Position(positionName);
    }

    public void setPosition(int percentW, int percentH) {
        this.position = new Position(percentW, percentH);
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
        private String ownerStateName;
        private Position position = new Position(.5, .5);
        private Anchors anchors = new Anchors();

        public Builder called(String name) {
            this.name = name;
            return this;
        }

        public Builder withLocation(Location location) {
            this.location = location;
            return this;
        }

        public Builder withLocation(int x, int y) {
            this.location = new Location(x, y);
            return this;
        }

        public Builder inState(String stateName) {
            this.ownerStateName = stateName;
            return this;
        }

        public Builder setAnchor(Position.Name cornerOfRegionToDefine) {
            this.anchors.add(new Anchor(cornerOfRegionToDefine, position));
            return this;
        }

        public Builder setPosition(Position.Name locationPosition) {
            this.position = new Position(locationPosition);
            return this;
        }

        public Builder setPosition(int percentW, int percentH) {
            this.position = new Position(percentW, percentH);
            return this;
        }

        public StateLocation build() {
            StateLocation stateLocation = new StateLocation();
            stateLocation.name = name;
            stateLocation.location = location;
            stateLocation.ownerStateName = ownerStateName;
            stateLocation.position = position;
            stateLocation.anchors = anchors;
            return stateLocation;
        }

    }
}
