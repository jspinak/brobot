package io.github.jspinak.brobot.database.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.database.primitives.location.Anchor;
import io.github.jspinak.brobot.database.primitives.location.Anchors;
import io.github.jspinak.brobot.database.primitives.location.Position;
import io.github.jspinak.brobot.database.primitives.match.MatchHistory;
import io.github.jspinak.brobot.database.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.stateObject.StateObject;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.database.state.NullState;
import lombok.Data;

/**
 * A StateRegion belongs to a State and usually has a Region that
 * has a special meaning for its owner State. For example, there
 * may be text in this Region that doesn't appear in any other State.
 */
@Data
public class StateRegion implements StateObject {

    private String name = "";
    private Region searchRegion = new Region();
    private StateEnum ownerStateName = NullState.Enum.NULL;
    private int staysVisibleAfterClicked = 100;
    private int probabilityExists = 100; // probability something can be acted on in this region
    private int timesActedOn = 0;
    private Position position = new Position(50, 50);
    private Anchors anchors = new Anchors();
    private String mockText = "mock text";
    private MatchHistory matchHistory = new MatchHistory();

    //think about deleting this field, it may be confusing and can easily be replaced by a separate region
    //sometimes we need to select a region within the searchRegion and act on it. this Region may change frequently.
    private Region actionableRegion;

    private StateRegion() {}

    public int x() {
        return searchRegion.x;
    }

    public int y() {
        return searchRegion.y;
    }

    public int w() {
        return searchRegion.w;
    }

    public int h() {
        return searchRegion.h;
    }

    public boolean defined() {
        return getSearchRegion().defined();
    }

    public void addTimesActedOn() {
        timesActedOn++;
    }

    public void addSnapshot(MatchSnapshot matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    public static class Builder {
        private String name = "";
        private Region searchRegion = new Region();
        private StateEnum ownerStateName = NullState.Enum.NULL;
        private Position position = new Position(50, 50);

        // Positions.Name: the border of the region to define
        // Position: the location in the region to use as a defining point
        private Anchors anchors = new Anchors();
        private MatchHistory matchHistory = new MatchHistory();

        private Region actionableRegion = new Region();

        public Builder called(String name) {
            this.name = name;
            return this;
        }

        public Builder withSearchRegion(Region searchRegion) {
            this.searchRegion = searchRegion;
            return this;
        }

        public Builder inState(StateEnum stateName) {
            this.ownerStateName = stateName;
            return this;
        }

        public Builder setPointLocation(Position position) {
            this.position = position;
            return this;
        }

        public Builder addAnchor(Position.Name definedRegionBorder, Position location) {
            this.anchors.add(new Anchor(definedRegionBorder, location));
            return this;
        }

        public Builder addSnapshot(MatchSnapshot matchSnapshot) {
            this.matchHistory.addSnapshot(matchSnapshot);
            return this;
        }

        public StateRegion build() {
            StateRegion stateRegion = new StateRegion();
            stateRegion.name = name;
            stateRegion.searchRegion = searchRegion;
            stateRegion.ownerStateName = ownerStateName;
            stateRegion.position = position;
            stateRegion.anchors = anchors;
            stateRegion.actionableRegion = actionableRegion;
            stateRegion.matchHistory = matchHistory;
            return stateRegion;
        }

    }
}
