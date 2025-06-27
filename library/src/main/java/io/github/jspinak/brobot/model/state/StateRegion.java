package io.github.jspinak.brobot.model.state;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Anchor;
import io.github.jspinak.brobot.model.element.Anchors;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateObject;
import lombok.Data;

/**
 * Represents a meaningful screen area associated with a specific state in Brobot.
 * 
 * <p>StateRegion encapsulates a rectangular area that has contextual significance within 
 * a particular state. Unlike simple Region objects, StateRegion maintains state ownership, 
 * interaction history, and behavioral properties that make it suitable for sophisticated 
 * GUI automation scenarios where screen areas have state-specific meanings and behaviors.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>State Association</b>: Bound to a specific state for contextual relevance</li>
 *   <li><b>Interaction Tracking</b>: Records how many times the region has been acted upon</li>
 *   <li><b>Click Positioning</b>: Configurable target position within the region (default center)</li>
 *   <li><b>Visibility Persistence</b>: Tracks likelihood of remaining visible after interaction</li>
 *   <li><b>Existence Probability</b>: Statistical measure of finding actionable content</li>
 *   <li><b>Anchor Support</b>: Relative positioning based on other screen elements</li>
 *   <li><b>Mock Text</b>: Simulated text content for testing and mocking</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Text input fields that appear only in specific states</li>
 *   <li>Dynamic content areas with state-specific behavior</li>
 *   <li>Button or control regions that change meaning by state</li>
 *   <li>Areas for text extraction or verification</li>
 *   <li>Regions that trigger state transitions when clicked</li>
 * </ul>
 * </p>
 * 
 * <p>Behavioral properties:
 * <ul>
 *   <li><b>staysVisibleAfterClicked</b>: Probability (0-100) the region remains after interaction</li>
 *   <li><b>probabilityExists</b>: Likelihood (0-100) of finding actionable content</li>
 *   <li><b>position</b>: Target point within region for clicks (0-1 scale)</li>
 * </ul>
 * </p>
 * 
 * <p>Integration features:
 * <ul>
 *   <li>Maintains MatchHistory for learning and mocking</li>
 *   <li>Converts to ObjectCollection for action execution</li>
 *   <li>Supports anchoring for dynamic positioning</li>
 *   <li>Provides convenience methods for coordinate access</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateRegion enables fine-grained control over screen 
 * areas that have different meanings in different states. This is essential for handling 
 * complex GUIs where the same screen location may serve different purposes depending on 
 * the application's current state, such as multi-purpose panels or context-sensitive areas.</p>
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

    public String getIdAsString() {
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

    public void addSnapshot(ActionRecord matchSnapshot) {
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
        private Long ownerStateId = 0L;
        private int staysVisibleAfterClicked = 100;
        private int probabilityExists = 100;
        private int timesActedOn = 0;
        private Position position = new Position(.5, .5);
        private Anchors anchors = new Anchors();
        private String mockText = "mock text";
        private ActionHistory matchHistory = new ActionHistory();

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
            return stateRegion;
        }
    }
}
