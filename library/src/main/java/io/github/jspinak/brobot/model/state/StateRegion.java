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
 *   <li><b>mockFindStochasticModifier</b>: Likelihood (0-100) of finding actionable content
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
    private int mockFindStochasticModifier = 100; // modifier for find probability in mock mode
    private int timesActedOn = 0;
    private Position position = new Position(.5, .5); // click position within region
    private Anchors anchors = new Anchors();
    private String mockText = "mock text";
    private ActionHistory matchHistory = new ActionHistory();

    // Cross-state anchor support
    private List<CrossStateAnchor> crossStateAnchors = new ArrayList<>();
    private DefineAs defineStrategy = DefineAs.OUTSIDE_ANCHORS;

    /**
     * Returns a unique string identifier for this StateRegion. Combines object type, name, and
     * region coordinates.
     *
     * @return concatenated string of type, name, and coordinates
     */
    public String getIdAsString() {
        return objectType.name()
                + name
                + searchRegion.x()
                + searchRegion.y()
                + searchRegion.w()
                + searchRegion.h();
    }

    /**
     * Returns the x-coordinate of this StateRegion.
     *
     * @return x-coordinate in pixels
     */
    public int x() {
        return searchRegion.x();
    }

    /**
     * Returns the y-coordinate of this StateRegion.
     *
     * @return y-coordinate in pixels
     */
    public int y() {
        return searchRegion.y();
    }

    /**
     * Returns the width of this StateRegion.
     *
     * @return width in pixels
     */
    public int w() {
        return searchRegion.w();
    }

    /**
     * Returns the height of this StateRegion.
     *
     * @return height in pixels
     */
    public int h() {
        return searchRegion.h();
    }

    /**
     * Checks if this StateRegion has defined coordinates. A region is defined if it has non-zero
     * width and height.
     *
     * @return true if the region is defined, false otherwise
     */
    public boolean defined() {
        return getSearchRegion().isDefined();
    }

    /**
     * Increments the counter tracking how many times this region has been acted upon. Useful for
     * tracking usage frequency and interaction statistics.
     */
    public void addTimesActedOn() {
        timesActedOn++;
    }

    /**
     * Adds an action record snapshot to this region's match history. Used for tracking historical
     * interactions and supporting mock mode.
     *
     * @param matchSnapshot the ActionRecord to add to history
     */
    public void addSnapshot(ActionRecord matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    /**
     * Converts this StateRegion to an ObjectCollection containing only this region. Useful for
     * Action methods that require ObjectCollection parameters.
     *
     * @return ObjectCollection containing this StateRegion
     */
    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder().withRegions(this).build();
    }

    public static class Builder {
        private String name = "";
        private Region searchRegion = new Region();
        private String ownerStateName = "null";
        private Long ownerStateId = 0L;
        private int staysVisibleAfterClicked = 100;
        private int mockFindStochasticModifier = 100;
        private int timesActedOn = 0;
        private Position position = new Position(.5, .5);
        private Anchors anchors = new Anchors();
        private String mockText = "mock text";
        private ActionHistory matchHistory = new ActionHistory();
        private List<CrossStateAnchor> crossStateAnchors = new ArrayList<>();
        private DefineAs defineStrategy = DefineAs.OUTSIDE_ANCHORS;

        /**
         * Sets the name for the StateRegion being built.
         *
         * @param name the name to assign
         * @return this builder for method chaining
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the search region for this StateRegion.
         *
         * @param searchRegion the Region to use
         * @return this builder for method chaining
         */
        public Builder setSearchRegion(Region searchRegion) {
            this.searchRegion = searchRegion;
            return this;
        }

        /**
         * Sets the search region using coordinates.
         *
         * @param x x-coordinate in pixels
         * @param y y-coordinate in pixels
         * @param w width in pixels
         * @param h height in pixels
         * @return this builder for method chaining
         */
        public Builder setSearchRegion(int x, int y, int w, int h) {
            this.searchRegion = new Region(x, y, w, h);
            return this;
        }

        /**
         * Sets the name of the State that owns this StateRegion.
         *
         * @param stateName the owner state's name
         * @return this builder for method chaining
         */
        public Builder setOwnerStateName(String stateName) {
            this.ownerStateName = stateName;
            return this;
        }

        /**
         * Sets the probability that this region remains visible after being clicked.
         *
         * @param staysVisibleAfterClicked probability (0-100) of staying visible
         * @return this builder for method chaining
         */
        public Builder setStaysVisibleAfterClicked(int staysVisibleAfterClicked) {
            this.staysVisibleAfterClicked = staysVisibleAfterClicked;
            return this;
        }

        /**
         * Sets the probability that actionable content exists in this region.
         *
         * @param mockFindStochasticModifier probability (0-100) of content existence
         * @return this builder for method chaining
         */
        public Builder setMockFindStochasticModifier(int mockFindStochasticModifier) {
            this.mockFindStochasticModifier = mockFindStochasticModifier;
            return this;
        }

        /**
         * Sets the initial count of times this region has been acted upon.
         *
         * @param timesActedOn the initial interaction count
         * @return this builder for method chaining
         */
        public Builder setTimesActedOn(int timesActedOn) {
            this.timesActedOn = timesActedOn;
            return this;
        }

        /**
         * Sets the target position within the region for clicks. Position is relative (0.0-1.0)
         * where 0.5,0.5 is center.
         *
         * @param position the Position for clicks
         * @return this builder for method chaining
         */
        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        /**
         * Adds an anchor relating a border of a defined region to a position in this region.
         *
         * @param definedRegionBorder the border position of the anchor region
         * @param positionInThisRegion the position in this region to anchor to
         * @return this builder for method chaining
         */
        public Builder addAnchor(
                Positions.Name definedRegionBorder, Positions.Name positionInThisRegion) {
            this.anchors.add(new Anchor(definedRegionBorder, new Position(positionInThisRegion)));
            return this;
        }

        /**
         * Adds an anchor with a specific position.
         *
         * @param definedRegionBorder the border position of the anchor region
         * @param location the specific position to anchor to
         * @return this builder for method chaining
         */
        public Builder addAnchor(Positions.Name definedRegionBorder, Position location) {
            this.anchors.add(new Anchor(definedRegionBorder, location));
            return this;
        }

        /**
         * Sets the complete Anchors collection for this region.
         *
         * @param anchors the Anchors object containing all anchors
         * @return this builder for method chaining
         */
        public Builder setAnchors(Anchors anchors) {
            this.anchors = anchors;
            return this;
        }

        /**
         * Adds an action record snapshot to the match history.
         *
         * @param matchSnapshot the ActionRecord to add
         * @return this builder for method chaining
         */
        public Builder addSnapshot(ActionRecord matchSnapshot) {
            this.matchHistory.addSnapshot(matchSnapshot);
            return this;
        }

        /**
         * Sets the mock text for this region. Used in testing and mock mode to simulate text
         * content.
         *
         * @param mockText the text to use in mock scenarios
         * @return this builder for method chaining
         */
        public Builder setMockText(String mockText) {
            this.mockText = mockText;
            return this;
        }

        /**
         * Sets the complete match history for this region. Contains historical interaction data for
         * learning and mocking.
         *
         * @param matchHistory the ActionHistory to use
         * @return this builder for method chaining
         */
        public Builder setMatchHistory(ActionHistory matchHistory) {
            this.matchHistory = matchHistory;
            return this;
        }

        /**
         * Adds a cross-state anchor for positioning relative to elements in other states.
         *
         * @param anchor the CrossStateAnchor to add
         * @return this builder for method chaining
         */
        public Builder addCrossStateAnchor(CrossStateAnchor anchor) {
            this.crossStateAnchors.add(anchor);
            return this;
        }

        /**
         * Sets the complete list of cross-state anchors.
         *
         * @param anchors list of CrossStateAnchor objects
         * @return this builder for method chaining
         */
        public Builder setCrossStateAnchors(List<CrossStateAnchor> anchors) {
            this.crossStateAnchors = anchors;
            return this;
        }

        /**
         * Sets the strategy for defining this region relative to anchors.
         *
         * @param strategy the DefineAs strategy to use
         * @return this builder for method chaining
         */
        public Builder setDefineStrategy(DefineAs strategy) {
            this.defineStrategy = strategy;
            return this;
        }

        /**
         * Builds the StateRegion with all configured properties.
         *
         * @return the constructed StateRegion instance
         */
        public StateRegion build() {
            StateRegion stateRegion = new StateRegion();
            stateRegion.name = name;
            stateRegion.searchRegion = searchRegion;
            stateRegion.ownerStateName = ownerStateName;
            stateRegion.ownerStateId = ownerStateId;
            stateRegion.staysVisibleAfterClicked = staysVisibleAfterClicked;
            stateRegion.mockFindStochasticModifier = mockFindStochasticModifier;
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
