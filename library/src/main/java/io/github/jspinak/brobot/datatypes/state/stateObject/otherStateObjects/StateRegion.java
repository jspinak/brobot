package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Data;

/**
 * A StateRegion belongs to a State and usually has a Region that
 * has a special meaning for its owner State. For example, there
 * may be text in this Region that doesn't appear in any other State.
 */
@Data
public class StateRegion implements StateObject {

    private StateObject.Type objectType = StateObject.Type.REGION;
    private String name = "";
    private Region searchRegion = new Region();
    private String ownerStateName = "null";
    private int staysVisibleAfterClicked = 100;
    private int probabilityExists = 100; // probability something can be acted on in this region
    private int timesActedOn = 0;
    private Position position = new Position(.5, .5); // click position within region
    private Anchors anchors = new Anchors();
    private String mockText = "mock text";
    private MatchHistory matchHistory = new MatchHistory();

    public String getId() {
        return objectType.name() + name + searchRegion.getX() + searchRegion.getY() + searchRegion.getW() + searchRegion.getY();
    }

    public int x() {
        return searchRegion.x();
    }

    public int y() {
        return searchRegion.y();
    }

    public int w() {
        return searchRegion.w();
    }

    public int h() {
        return searchRegion.h();
    }

    public boolean defined() {
        return getSearchRegion().isDefined();
    }

    public void addTimesActedOn() {
        timesActedOn++;
    }

    public void addSnapshot(MatchSnapshot matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder()
                .withRegions(this)
                .build();
    }

    public static class Builder {
        private String name = "";
        private Region searchRegion = new Region();
        private String ownerStateName = "null";
        private int staysVisibleAfterClicked = 100;
        private int probabilityExists = 100;
        private int timesActedOn = 0;
        private Position position = new Position(.5, .5);
        private Anchors anchors = new Anchors();
        private String mockText = "mock text";
        private MatchHistory matchHistory = new MatchHistory();

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSearchRegion(Region searchRegion) {
            this.searchRegion = searchRegion;
            return this;
        }

        public Builder setSearchRegion(int x, int y, int w, int h) {
            this.searchRegion = new Region(x, y, w, h);
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
            this.setProbabilityExists(probabilityExists);
            return this;
        }

        public Builder setTimesActedOn(int timesActedOn) {
            this.setTimesActedOn(timesActedOn);
            return this;
        }

        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        public Builder addAnchor(Positions.Name definedRegionBorder, Positions.Name positionInThisRegion) {
            this.anchors.add(new Anchor(definedRegionBorder, new Position(positionInThisRegion)));
            return this;
        }

        public Builder addAnchor(Positions.Name definedRegionBorder, Position location) {
            this.anchors.add(new Anchor(definedRegionBorder, location));
            return this;
        }

        public Builder setAnchors(Anchors anchors) {
            this.anchors = anchors;
            return this;
        }

        public Builder addSnapshot(MatchSnapshot matchSnapshot) {
            this.matchHistory.addSnapshot(matchSnapshot);
            return this;
        }

        public Builder setMockText(String mockText) {
            this.mockText = mockText;
            return this;
        }

        public Builder setMatchHistory(MatchHistory matchHistory) {
            this.matchHistory = matchHistory;
            return this;
        }

        public StateRegion build() {
            StateRegion stateRegion = new StateRegion();
            stateRegion.name = name;
            stateRegion.searchRegion = searchRegion;
            stateRegion.ownerStateName = ownerStateName;
            stateRegion.staysVisibleAfterClicked = staysVisibleAfterClicked;
            stateRegion.probabilityExists = probabilityExists;
            stateRegion.timesActedOn = timesActedOn;
            stateRegion.position = position;
            stateRegion.anchors = anchors;
            stateRegion.mockText = mockText;
            stateRegion.matchHistory = matchHistory;
            return stateRegion;
        }
    }
}
