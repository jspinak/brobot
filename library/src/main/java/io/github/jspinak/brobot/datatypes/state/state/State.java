package io.github.jspinak.brobot.datatypes.state.state;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * States give a Brobot application structure. They are organized in a similar way
 * to html pages in a website. Brobot can find its way to a State by finding a Path to
 * the State and following StateTransitions. If it gets lost, it will try to find its
 * way by identifying its current State and following a new Path to the desired State.
 *
 * States are identified by their StateImageObjects. Some StateImageObjects contain
 * Images that are found in other States. These StateImageObjects identify themselves
 * as 'shared' and are not including in searches for the current State when Brobot is lost.
 * The shared Images are used for all other State searches.
 *
 */
@Getter
@Setter
public class State {

    private StateEnum name;
    /**
     * StateText is text that appears on the screen and is a clue to look for images in this state.
     * Text search is a lot faster than image search, but cannot be used without an image search to identify a state.
     */
    private Set<String> stateText = new HashSet<>();
    private Set<StateImageObject> stateImages = new HashSet<>();
    /**
     * StateStrings can change the expected state.
     * They have associated regions where typing the string has an effect.
     */
    private Set<StateString> stateStrings = new HashSet<>();
    /**
     * StateRegions can change the expected state when clicked or hovered over.
     * They can also perform text retrieval.
     */
    private Set<StateRegion> stateRegions = new HashSet<>();
    private Set<StateLocation> stateLocations = new HashSet<>();
    /**
     * When true, this State needs to be acted on before accessing other States.
     * In effect, it makes all other States inactive, even though they may be visible.
     * When a blocking State is exited, the other active States become accessible again.
     */
    private boolean blocking = false;
    /**
     * Holds the States that this State can hide when it becomes active. If one of the
     * 'canHide' States is active when this State becomes active, these now hidden
     * States are added to the 'hidden' set.
     */
    private Set<StateEnum> canHide = new HashSet<>();
    /**
     * Hiding a State means that this State covers the hidden State, and when this State is exited
     * the hidden State becomes visible again. For example, opening a menu
     * might completely block another State, but when the menu is closed the other State is visible again.
     * When this State becomes active,
     * any active 'canHide' States are deactivated and placed into this State's hidden set.
     * When the State is exited and the hidden States reappear, they are removed from the hidden set.
     * The hidden set is used by StateTransitions when there is a PREVIOUS Transition (PREVIOUS is
     * a variable StateEnum).
     */
    private Set<StateEnum> hidden = new HashSet<>();
    private int pathScore = 1; // larger path scores prohibit taking a path with this state
    private LocalDateTime lastAccessed;
    /**
     * The base probability that a State exists is transfered to the active variable 'probabilityExists',
     * which is used by Find functions. ProbabilityExists is set to 0 when a State exits, and is set
     * to the base probability when it should be found. Having a baseProbability of less than 100 will
     * introduce unexpected behavior into a program (States may not appear when expected to), which
     * can be useful for testing a program with built-in stochasticity.
     */
    private int baseProbabilityExists = 100;
    private int probabilityExists = 0; // probability that the state exists. used for mocks.

    public void setSearchRegionForAllImages(Region searchRegion) {
        stateImages.forEach(imageObj -> imageObj.setSearchRegion(searchRegion));
    }

    public void setProbabilitiesForAllImages(int probabilityExists) {
        stateImages.forEach(imageObj -> imageObj.setProbabilityExists(probabilityExists));
    }

    public void setProbabilitiesForAllImages() {
        stateImages.forEach(imageObj -> imageObj.setProbabilityExists(this.probabilityExists));
    }

    public void setProbabilityToBaseProbability() {
        probabilityExists = baseProbabilityExists;
    }

    public void addHiddenState(StateEnum stateEnum) {
        hidden.add(stateEnum);
    }

    public void resetHidden() {
        hidden = new HashSet<>();
    }

    public static class Builder {

        private StateEnum name;
        private Set<String> stateText = new HashSet<>();
        private Set<StateImageObject> stateImages = new HashSet<>();
        private Set<StateString> stateStrings = new HashSet<>();
        private Set<StateRegion> stateRegions = new HashSet<>();
        private Set<StateLocation> stateLocations = new HashSet<>();
        private boolean blocking = false;
        private Set<StateEnum> canHide = new HashSet<>();
        private Set<StateEnum> hidden = new HashSet<>();
        private int pathScore = 1;
        private LocalDateTime lastAccessed;
        private int baseProbabilityExists = 100;

        public Builder(StateEnum stateEnum) {
            this.name = stateEnum;
        }

        public Builder withText(String... stateText) {
            Collections.addAll(this.stateText, stateText);
            return this;
        }

        public Builder withImages(StateImageObject... stateImageObjects) {
            Collections.addAll(this.stateImages, stateImageObjects);
            return this;
        }

        public Builder withStrings(StateString... stateStrings) {
            Collections.addAll(this.stateStrings, stateStrings);
            return this;
        }

        public Builder withRegions(StateRegion... stateRegions) {
            Collections.addAll(this.stateRegions, stateRegions);
            return this;
        }

        public Builder withLocations(StateLocation... stateLocations) {
            Collections.addAll(this.stateLocations, stateLocations);
            return this;
        }

        public Builder setBlocking(boolean blocking) {
            this.blocking = blocking;
            return this;
        }

        public Builder canHide(StateEnum... stateEnums) {
            this.canHide.addAll(List.of(stateEnums));
            return this;
        }

        public Builder setPathScore(int score) {
            this.pathScore = score;
            return this;
        }

        public Builder setBaseProbabilityExists(int probabilityExists) {
            this.baseProbabilityExists = probabilityExists;
            return this;
        }

        public State build() {
            State state = new State();
            state.name = name;
            state.stateText = stateText;
            for (StateImageObject image : stateImages) image.setOwnerStateName(name);
            for (StateString string : stateStrings) string.setOwnerStateName(name);
            for (StateRegion region : stateRegions) region.setOwnerStateName(name);
            state.stateImages = stateImages;
            state.stateStrings = stateStrings;
            state.stateRegions = stateRegions;
            state.stateLocations = stateLocations;
            state.blocking = blocking;
            state.canHide = canHide;
            state.hidden = hidden;
            state.pathScore = pathScore;
            state.baseProbabilityExists = baseProbabilityExists;
            return state;
        }
    }
}


