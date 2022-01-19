package io.github.jspinak.brobot.database.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.database.primitives.location.Anchor;
import io.github.jspinak.brobot.database.primitives.location.Anchors;
import io.github.jspinak.brobot.database.primitives.location.Location;
import io.github.jspinak.brobot.database.primitives.location.Position;
import io.github.jspinak.brobot.database.primitives.match.MatchHistory;
import io.github.jspinak.brobot.database.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.database.state.stateObject.StateObject;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
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
    private StateEnum ownerStateName;
    private int staysVisibleAfterClicked = 100;
    private int probabilityExists = 100; // probability something can be acted on at this location
    private int timesActedOn = 0;
    private Position position;
    private Anchors anchors; // just one, but defined with Anchors b/c it's a StateObject
    private MatchHistory matchHistory = new MatchHistory(); // not used yet

    private StateLocation() {
    }

    public boolean defined() {
        return location != null;
    }

    public void addTimesActedOn() {
        timesActedOn++;
    }

    public void addSnapshot(MatchSnapshot matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    public static class Builder {
        private String name;
        private Location location;
        private StateEnum ownerStateName;
        private Position position = new Position(0, 0);
        private Anchors anchors = new Anchors();

        public Builder called(String name) {
            this.name = name;
            return this;
        }

        public Builder withLocation(Location location) {
            this.location = location;
            return this;
        }

        public Builder inState(StateEnum stateName) {
            this.ownerStateName = stateName;
            return this;
        }

        public Builder setAnchor(Position.Name cornerOfRegionToDefine) {
            this.anchors.add(new Anchor(cornerOfRegionToDefine, position));
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
