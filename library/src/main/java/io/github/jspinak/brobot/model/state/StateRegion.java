package io.github.jspinak.brobot.model.state;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions.DefineAs;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Anchor;
import io.github.jspinak.brobot.model.element.Anchors;
import io.github.jspinak.brobot.model.element.CrossStateAnchor;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;

import lombok.Data;

/**
 * Represents a meaningful screen area associated with a specific state in Brobot.
 *
 * <p>StateRegion encapsulates a rectangular area that has contextual significance within a
 * particular state. Unlike simple Region objects, StateRegion maintains state ownership,
 * interaction history, and behavioral properties that make it suitable for sophisticated GUI
 * automation scenarios where screen areas have state-specific meanings and behaviors.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>State Association</b>: Bound to a specific state for contextual relevance
 *   <li><b>Interaction Tracking</b>: Records how many times the region has been acted upon
 *   <li><b>Click Positioning</b>: Configurable target position within the region (default center)
 *   <li><b>Visibility Persistence</b>: Tracks likelihood of remaining visible after interaction
 *   <li><b>Existence Probability</b>: Statistical measure of finding actionable content
 *   <li><b>Anchor Support</b>: Relative positioning based on other screen elements
 *   <li><b>Mock Text</b>: Simulated text content for testing and mocking
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Text input fields that appear only in specific states
 *   <li>Dynamic content areas with state-specific behavior
 *   <li>Button or control regions that change meaning by state
 *   <li>Areas for text extraction or verification
 *   <li>Regions that trigger state transitions when clicked
 * </ul>
 *
 * <p>Behavioral properties:
 *
 * <ul>
 *   <li><b>staysVisibleAfterClicked</b>: Probability (0-100) the region remains after interaction
 *   <li><b>probabilityExists</b>: Likelihood (0-100) of finding actionable content
 *   <li><b>position</b>: Target point within region for clicks (0-1 scale)
 * </ul>
 *
 * <p>Integration features:
 *
 * <ul>
 *   <li>Maintains MatchHistory for learning and mocking
 *   <li>Converts to ObjectCollection for action execution
 *   <li>Supports anchoring for dynamic positioning
 *   <li>Provides convenience methods for coordinate access
 * </ul>
 *
 * <p>In the model-based approach, StateRegion enables fine-grained control over screen areas that
 * have different meanings in different states. This is essential for handling complex GUIs where
 * the same screen location may serve different purposes depending on the application's current
 * state, such as multi-purpose panels or context-sensitive areas.
 *
 * @since 1.0
 * @see StateObject
 * @see Region
 * @see State
 * @see Anchors
 * @see ActionHistory
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateRegion implements StateObject {

    private StateObject.Type objectType = StateObject.Type.REGION;
    private String name = "";
    private Region searchRegion = new Region();
    private String ownerStateName = "null";
    private Long ownerStateId = 0L;
    private int staysVisibleAfterClicked = 100;
    private int probabilityExists = 100; // probability something can be acted on in this region
    private int timesActedOn = 0;
    private Position position = new Position(.5, .5); // click position within region
    private Anchors anchors = new Anchors();
    private String mockText = "mock text";
    private ActionHistory matchHistory = new ActionHistory();

    // Cross-state anchor support
    private List<CrossStateAnchor> crossStateAnchors = new ArrayList<>();
    private DefineAs defineStrategy = DefineAs.OUTSIDE_ANCHORS;

    public String getIdAsString() {
        return objectType.name()
                + name
                + searchRegion.x()
                + searchRegion.y()
                + searchRegion.w()
                + searchRegion.h();
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

    public void addSnapshot(ActionRecord matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder().withRegions(this).build();
    }

    public static class Builder {
        private String name = "";
        private Region searchRegion = new Region();
        private String ownerStateName = "null";
        private Long ownerStateId = 0L;
        private int staysVisibleAfterClicked = 100;
        private int probabilityExists = 100;
        private int timesActedOn = 0;
        private Position position = new Position(.5, .5);
        private Anchors anchors = new Anchors();
        private String mockText = "mock text";
        private ActionHistory matchHistory = new ActionHistory();
        private List<CrossStateAnchor> crossStateAnchors = new ArrayList<>();
        private DefineAs defineStrategy = DefineAs.OUTSIDE_ANCHORS;

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
            this.probabilityExists = probabilityExists;
            return this;
        }

        public Builder setTimesActedOn(int timesActedOn) {
            this.timesActedOn = timesActedOn;
            return this;
        }

        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        public Builder addAnchor(
                Positions.Name definedRegionBorder, Positions.Name positionInThisRegion) {
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

        public Builder addSnapshot(ActionRecord matchSnapshot) {
            this.matchHistory.addSnapshot(matchSnapshot);
            return this;
        }

        public Builder setMockText(String mockText) {
            this.mockText = mockText;
            return this;
        }

        public Builder setMatchHistory(ActionHistory matchHistory) {
            this.matchHistory = matchHistory;
            return this;
        }

        public Builder addCrossStateAnchor(CrossStateAnchor anchor) {
            this.crossStateAnchors.add(anchor);
            return this;
        }

        public Builder setCrossStateAnchors(List<CrossStateAnchor> anchors) {
            this.crossStateAnchors = anchors;
            return this;
        }

        public Builder setDefineStrategy(DefineAs strategy) {
            this.defineStrategy = strategy;
            return this;
        }

        public StateRegion build() {
            StateRegion stateRegion = new StateRegion();
            stateRegion.name = name;
            stateRegion.searchRegion = searchRegion;
            stateRegion.ownerStateName = ownerStateName;
            stateRegion.ownerStateId = ownerStateId;
            stateRegion.staysVisibleAfterClicked = staysVisibleAfterClicked;
            stateRegion.probabilityExists = probabilityExists;
            stateRegion.timesActedOn = timesActedOn;
            stateRegion.position = position;
            stateRegion.anchors = anchors;
            stateRegion.mockText = mockText;
            stateRegion.matchHistory = matchHistory;
            stateRegion.crossStateAnchors = crossStateAnchors;
            stateRegion.defineStrategy = defineStrategy;
            return stateRegion;
        }
    }

    /**
     * Converts this StateRegion to an ObjectCollection containing only this region. Useful for
     * Action methods that require ObjectCollection parameters.
     *
     * @return ObjectCollection containing this StateRegion
     */
    public ObjectCollection toObjectCollection() {
        return new ObjectCollection.Builder().withRegions(this).build();
    }
}
