package io.github.jspinak.brobot.model.state;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.element.Anchor;
import io.github.jspinak.brobot.model.element.Anchors;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a meaningful screen coordinate associated with a specific state in Brobot.
 *
 * <p>StateLocation encapsulates a point on the screen that has contextual significance within a
 * particular state. Unlike simple Location objects, StateLocation maintains state ownership,
 * interaction history, and behavioral properties that make it suitable for sophisticated GUI
 * automation scenarios where specific coordinates have state-dependent meanings and effects.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>State Association</b>: Bound to a specific state for contextual relevance
 *   <li><b>Flexible Positioning</b>: Supports both absolute coordinates and relative positions
 *   <li><b>Interaction Tracking</b>: Records how many times the location has been acted upon
 *   <li><b>Visibility Persistence</b>: Probability of remaining actionable after interaction
 *   <li><b>Existence Probability</b>: Statistical measure of finding actionable content
 *   <li><b>Anchor Support</b>: Can be positioned relative to other screen elements
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Fixed buttons or controls that only function in specific states
 *   <li>Menu items that appear at consistent locations in certain contexts
 *   <li>Click targets that trigger state-specific actions
 *   <li>Reference points for relative positioning of other elements
 *   <li>Hotspots that activate different features based on current state
 * </ul>
 *
 * <p>Behavioral properties:
 *
 * <ul>
 *   <li><b>probabilityStaysVisibleAfterClicked</b>: Likelihood (0-100) the location remains
 *       actionable after being clicked
 *   <li><b>mockFindStochasticModifier</b>: Likelihood (0-100) of finding actionable content at this
 *       location when the state is active
 * </ul>
 *
 * <p>Positioning options:
 *
 * <ul>
 *   <li>Absolute coordinates via Location(x, y)
 *   <li>Relative position within a region
 *   <li>Anchored to other elements for dynamic layouts
 *   <li>Named positions (TOPLEFT, CENTER, etc.) for semantic clarity
 * </ul>
 *
 * <p>In the model-based approach, StateLocation enables precise interaction with GUI elements that
 * may only be meaningful or functional in specific application states. This is essential for
 * handling context-sensitive interfaces where the same screen coordinate may trigger different
 * actions or have different effects depending on the current state.
 *
 * @since 1.0
 * @see StateObject
 * @see Location
 * @see State
 * @see Position
 * @see Anchors
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateLocation implements StateObject {

    private StateObject.Type objectType = StateObject.Type.LOCATION;
    private String name;
    private Location location;
    private String ownerStateName = "null";
    private Long ownerStateId = null;
    private int probabilityStaysVisibleAfterClicked = 100;
    private int mockFindStochasticModifier = 100; // modifier for find probability in mock mode
    private int timesActedOn = 0;
    private Position position;
    private Anchors anchors; // just one, but defined with Anchors b/c it's a StateObject
    private ActionHistory matchHistory = new ActionHistory(); // not used yet

    // Cross-state search region support
    private SearchRegionOnObject searchRegionOnObject;

    public String getIdAsString() {
        return objectType.name() + name + location.getCalculatedX() + location.getCalculatedY();
    }

    public boolean defined() {
        return location != null;
    }

    public void addTimesActedOn() {
        timesActedOn++;
    }

    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder().withLocations(this).build();
    }

    @Override
    public String toString() {
        return "StateLocation:"
                + " name="
                + name
                + " ownerStateName="
                + ownerStateName
                + " location="
                + location
                + " position="
                + position
                + " anchors="
                + anchors;
    }

    public static class Builder {
        private String name = "";
        private Location location;
        private String ownerStateName = "null";
        private Long ownerStateId = 0L;
        private int probabilityStaysVisibleAfterClicked = 100;
        private int mockFindStochasticModifier = 100; // modifier for find probability in mock mode
        private int timesActedOn = 0;
        private Position position = new Position(.5, .5);
        private Anchors anchors = new Anchors();
        private ActionHistory matchHistory = new ActionHistory(); // not used yet
        private SearchRegionOnObject searchRegionOnObject;

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

        public Builder setProbabilityStaysVisibleAfterClicked(
                int probabilityStaysVisibleAfterClicked) {
            this.probabilityStaysVisibleAfterClicked = probabilityStaysVisibleAfterClicked;
            return this;
        }

        public Builder setMockFindStochasticModifier(int mockFindStochasticModifier) {
            this.mockFindStochasticModifier = mockFindStochasticModifier;
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

        public Builder setMatchHistory(ActionHistory matchHistory) {
            this.matchHistory = matchHistory;
            return this;
        }

        public Builder setSearchRegionOnObject(SearchRegionOnObject searchRegionOnObject) {
            this.searchRegionOnObject = searchRegionOnObject;
            return this;
        }

        public StateLocation build() {
            StateLocation stateLocation = new StateLocation();
            stateLocation.name = name;
            stateLocation.location = location;
            stateLocation.ownerStateName = ownerStateName;
            stateLocation.ownerStateId = ownerStateId;
            stateLocation.probabilityStaysVisibleAfterClicked = probabilityStaysVisibleAfterClicked;
            stateLocation.mockFindStochasticModifier = mockFindStochasticModifier;
            stateLocation.timesActedOn = timesActedOn;
            stateLocation.position = position;
            stateLocation.anchors = anchors;
            stateLocation.matchHistory = matchHistory;
            stateLocation.searchRegionOnObject = searchRegionOnObject;
            return stateLocation;
        }
    }

    /**
     * Converts this StateLocation to an ObjectCollection containing only this location. Useful for
     * Action methods that require ObjectCollection parameters.
     *
     * @return ObjectCollection containing this StateLocation
     */
    public ObjectCollection toObjectCollection() {
        return new ObjectCollection.Builder().withLocations(this).build();
    }
}
