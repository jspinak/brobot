package io.github.jspinak.brobot.model.state;

import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Scene;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a distinct configuration of the GUI in the Brobot model-based automation framework.
 *
 * <p>A State is the fundamental building block of the model-based approach, representing a
 * recognizable and meaningful configuration of the user interface. States form the nodes in the
 * state structure (Ω), which models the GUI environment as a navigable graph.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li><b>Identification</b>: States are identified by their StateImages - visual patterns that
 *       uniquely define this GUI configuration
 *   <li><b>Navigation</b>: States are connected by transitions, allowing Brobot to navigate between
 *       them like pages in a website
 *   <li><b>Recovery</b>: If Brobot gets lost, it can identify the current State and find a new path
 *       to the target State
 *   <li><b>Hierarchy</b>: States can hide other States (like menus covering content) and can be
 *       blocking (requiring interaction before accessing other States)
 * </ul>
 *
 * <p>State components:
 *
 * <ul>
 *   <li><b>StateImages</b>: Visual patterns that identify this State (some may be shared across
 *       States)
 *   <li><b>StateRegions</b>: Clickable or hoverable areas that can trigger State changes
 *   <li><b>StateStrings</b>: Text input fields that affect State transitions
 *   <li><b>StateLocations</b>: Specific points for precise interactions
 *   <li><b>StateText</b>: Text that appears in this State (used for faster preliminary searches)
 * </ul>
 *
 * <p>This class embodies the core principle of model-based GUI automation: transforming implicit
 * knowledge about GUI structure into an explicit, navigable model that enables robust and
 * maintainable automation.
 *
 * @since 1.0
 * @see StateImage
 * @see StateRegion
 * @see StateTransitions
 * @see PathFinder
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class State {

    private Long id = null; // set when the state is saved
    private String name = "";

    /**
     * StateText is text that appears on the screen and is a clue to look for images in this state.
     * Text search is a lot faster than image search, but cannot be used without an image search to
     * identify a state.
     */
    private Set<String> stateText = new HashSet<>();

    private Set<StateImage> stateImages = new HashSet<>();

    /**
     * StateStrings can change the expected state. They have associated regions where typing the
     * string has an effect.
     */
    private Set<StateString> stateStrings = new HashSet<>();

    /**
     * StateRegions can change the expected state when clicked or hovered over. They can also
     * perform text retrieval.
     */
    private Set<StateRegion> stateRegions = new HashSet<>();

    private Set<StateLocation> stateLocations = new HashSet<>();

    /**
     * When true, this State needs to be acted on before accessing other States. In effect, it makes
     * all other States inactive, even though they may be visible. When a blocking State is exited,
     * the other active States become accessible again.
     */
    private boolean blocking = false;

    /**
     * Holds the States that this State can hide when it becomes active. If one of the 'canHide'
     * States is active when this State becomes active, these now hidden States are added to the
     * 'hidden' set.
     */
    private Set<String> canHide = new HashSet<>();

    private Set<Long> canHideIds = new HashSet<>();

    /**
     * Hiding a State means that this State covers the hidden State, and when this State is exited
     * the hidden State becomes visible again. For example, opening a menu might completely block
     * another State, but when the menu is closed the other State is visible again. When this State
     * becomes active, any active 'canHide' States are deactivated and placed into this State's
     * hidden set. When the State is exited and the hidden States reappear, they are removed from
     * the hidden set. StateTransitions uses the hidden set when there is a PREVIOUS Transition
     * (PREVIOUS is a variable StateEnum).
     */
    private Set<String> hiddenStateNames = new HashSet<>(); // used when initializing states in code

    private Set<Long> hiddenStateIds = new HashSet<>(); // used at runtime

    /**
     * Path-finding cost for this state. The total cost of a path is the sum of all state costs and
     * transition costs. Lower costs are preferred in pathfinding. Default is 1 for normal states.
     */
    private int pathCost = 1;

    /*
    LocalDateTime, to be persisted with JPA, requires the @Converter annotation and code to convert the value
    to a format for the database and back to a Java entity.
     */
    private LocalDateTime lastAccessed;

    /**
     * The base modifier for find operations in mock mode. This value modifies the probability of
     * finding state elements during mock test runs. A value less than 100 will introduce realistic
     * failures into mock tests, simulating environmental uncertainties like network issues,
     * rendering problems, or unexpected UI overlays.
     */
    private int baseMockFindStochasticModifier = 100;

    private int mockFindStochasticModifier = 0; // modifier for find probability in mock mode
    private int timesVisited = 0;

    /**
     * Screenshots where the state is found. These can be used for realistic simulations or for
     * illustrating the state to display it visually. Not all StateImages need to be present. The
     * usable area is the region used to find images. A pattern's fixed region gives its location in
     * the usable area.
     */
    private List<Scene> scenes = new ArrayList<>();

    private Region usableArea = new Region();

    /**
     * Some actions take place without an associated Pattern or StateImage. These actions are stored
     * in their corresponding state.
     */
    private ActionHistory matchHistory = new ActionHistory();

    public void addStateImage(StateImage stateImage) {
        stateImage.setOwnerStateName(name);
        stateImages.add(stateImage);
    }

    public void addStateRegion(StateRegion stateRegion) {
        stateRegion.setOwnerStateName(name);
        stateRegions.add(stateRegion);
    }

    public void addStateLocation(StateLocation stateLocation) {
        stateLocation.setOwnerStateName(name);
        stateLocations.add(stateLocation);
    }

    public void addStateString(StateString stateString) {
        stateString.setOwnerStateName(name);
        stateStrings.add(stateString);
    }

    public void addStateText(String stateText) {
        this.stateText.add(stateText);
    }

    public void setSearchRegionForAllImages(Region searchRegion) {
        stateImages.forEach(imageObj -> imageObj.setSearchRegions(searchRegion));
    }

    public void setProbabilityToBaseProbability() {
        mockFindStochasticModifier = baseMockFindStochasticModifier;
    }

    public void addHiddenState(Long stateId) {
        hiddenStateIds.add(stateId);
    }

    public void resetHidden() {
        hiddenStateNames = new HashSet<>();
    }

    public void addVisit() {
        timesVisited++;
    }

    /**
     * Get the boundaries of the state using StateRegion, StateImage, and StateLocation objects.
     * Snapshots and SearchRegion(s) are used for StateImages.
     *
     * @return the boundaries of the state.
     */
    public Region getBoundaries() {
        List<Region> imageRegions = new ArrayList<>();
        for (StateImage stateImage : stateImages.toArray(new StateImage[0])) {
            // if the image has a fixed location and has been found, add this location
            stateImage
                    .getPatterns()
                    .forEach(
                            pattern -> {
                                Region fixedRegion = pattern.getSearchRegions().getFixedRegion();
                                if (fixedRegion != null && fixedRegion.isDefined())
                                    imageRegions.add(fixedRegion);
                            });
            // otherwise, add the snapshot locations
            List<ActionRecord> snapshots = stateImage.getAllMatchSnapshots();
            for (ActionRecord snapshot : snapshots) {
                snapshot.getMatchList().forEach(match -> imageRegions.add(match.getRegion()));
            }
        }
        for (StateRegion stateRegion : stateRegions) {
            imageRegions.add(stateRegion.getSearchRegion());
        }
        for (StateLocation stateLocation : stateLocations) {
            Location loc = stateLocation.getLocation();
            imageRegions.add(new Region(loc.getCalculatedX(), loc.getCalculatedY(), 0, 0));
        }
        if (imageRegions.isEmpty()) return new Region(); // the region is not defined
        Region union = imageRegions.get(0);
        for (int i = 1; i < imageRegions.size(); i++) {
            union = union.getUnion(imageRegions.get(i));
        }
        return union;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("State: ").append(name).append("\n");
        stringBuilder.append("Images=").append(stateImages.size()).append("\t");
        stateImages.forEach(stringBuilder::append);
        stringBuilder.append("\n");
        stringBuilder.append("Regions=").append(stateRegions.size()).append("\t");
        stateRegions.forEach(stringBuilder::append);
        stringBuilder.append("\n");
        stringBuilder.append("Locations=").append(stateLocations.size()).append("\t");
        stateLocations.forEach(stringBuilder::append);
        stringBuilder.append("\n");
        stringBuilder.append("Strings=").append(stateStrings.size()).append("\t");
        stateStrings.forEach(stringBuilder::append);
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public static class Builder {

        private final String name;
        private final Set<String> stateText = new HashSet<>();
        private final Set<StateImage> stateImages = new HashSet<>();
        private final Set<StateString> stateStrings = new HashSet<>();
        private final Set<StateRegion> stateRegions = new HashSet<>();
        private final Set<StateLocation> stateLocations = new HashSet<>();
        private boolean blocking = false;
        private final Set<String> canHide = new HashSet<>();
        private final Set<String> hidden = new HashSet<>();
        private int pathCost = 1;
        private LocalDateTime lastAccessed;
        private int baseMockFindStochasticModifier = 100;
        private final List<Scene> scenes = new ArrayList<>();
        private Region usableArea = new Region();

        public Builder(String stateName) {
            this.name = stateName;
        }

        public Builder(StateEnum stateEnum) {
            this.name = stateEnum.toString();
        }

        public Builder withText(String... stateText) {
            Collections.addAll(this.stateText, stateText);
            return this;
        }

        public Builder withImages(StateImage... stateImages) {
            Collections.addAll(this.stateImages, stateImages);
            return this;
        }

        public Builder withImages(List<StateImage> stateImages) {
            return withImages(stateImages.toArray(new StateImage[0]));
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

        public Builder canHide(String... stateNames) {
            this.canHide.addAll(List.of(stateNames));
            return this;
        }

        public Builder setPathCost(int cost) {
            this.pathCost = cost;
            return this;
        }

        public Builder setBaseMockFindStochasticModifier(int mockFindStochasticModifier) {
            this.baseMockFindStochasticModifier = mockFindStochasticModifier;
            return this;
        }

        public Builder withScenes(List<Scene> scenes) {
            this.scenes.addAll(scenes);
            return this;
        }

        public Builder addScenes(Scene... scenes) {
            this.scenes.addAll(Arrays.asList(scenes));
            return this;
        }

        public Builder setUsableArea(Region usableArea) {
            this.usableArea = usableArea;
            return this;
        }

        public State build() {
            State state = new State();
            state.name = name;
            state.stateText = stateText;
            for (StateImage image : stateImages) image.setOwnerStateName(name);
            for (StateLocation location : stateLocations) location.setOwnerStateName(name);
            for (StateString string : stateStrings) string.setOwnerStateName(name);
            for (StateRegion region : stateRegions) region.setOwnerStateName(name);
            state.stateImages = stateImages;
            state.stateStrings = stateStrings;
            state.stateRegions = stateRegions;
            state.stateLocations = stateLocations;
            state.blocking = blocking;
            state.canHide = canHide;
            state.hiddenStateNames = hidden;
            state.pathCost = pathCost;
            state.baseMockFindStochasticModifier = baseMockFindStochasticModifier;
            state.scenes = scenes;
            state.usableArea = usableArea;
            return state;
        }
    }
}
