package io.github.jspinak.brobot.datatypes.state.state;

import io.github.jspinak.brobot.illustratedHistory.StateIllustration;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

/**
 * States give a Brobot application structure. They are organized in a similar way
 * to html pages in a website. Brobot can find its way to a State by finding a Path to
 * the State and following StateTransitions. If it gets lost, it will try to find its
 * way by identifying its current State and following a new Path to the desired State.
 *
 * States are identified by their StateImages. Some StateImages contain
 * Images that are found in other States. These StateImages identify themselves
 * as 'shared' and are not including in searches for the current State when Brobot is lost.
 * The shared Images are used for all other State searches.
 *
 */
@Getter
@Setter
public class State {

    private Long projectId = 0L;
    private String name = "";
    /**
     * StateText is text that appears on the screen and is a clue to look for images in this state.
     * Text search is a lot faster than image search, but cannot be used without an image search to identify a state.
     */
    private Set<String> stateText = new HashSet<>();
    private Set<StateImage> stateImages = new HashSet<>();
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
    private Set<String> canHide = new HashSet<>();
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
    private Set<String> hidden = new HashSet<>();
    private int pathScore = 1; // larger path scores discourage taking a path with this state
    /*
    LocalDateTime, to be persisted with JPA, requires the @Converter annotation and code to convert the value
    to a format for the database and back to a Java entity.
     */
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
    private int timesVisited = 0;
    /**
     * Screenshots where the state is found. These can be used for realistic simulations or for
     * illustrating the state to display it visually. Not all StateImages need to be present.
     */
    private List<Image> scenes = new ArrayList<>();
    private List<StateIllustration> illustrations = new ArrayList<>();
    /**
     * Some actions take place without an associated Pattern or StateImage. These actions are stored in their
     * corresponding state.
     */
    private MatchHistory matchHistory = new MatchHistory();

    public void setSearchRegionForAllImages(Region searchRegion) {
        stateImages.forEach(imageObj -> imageObj.setSearchRegions(searchRegion));
    }

    public void setProbabilityToBaseProbability() {
        probabilityExists = baseProbabilityExists;
    }

    public void addHiddenState(String stateName) {
        hidden.add(stateName);
    }

    public void resetHidden() {
        hidden = new HashSet<>();
    }

    public void addVisit() {
        timesVisited++;
    }

    /**
     * Get the boundaries of the state using StateRegion, StateImage, and StateLocation objects.
     * Snapshots and SearchRegion(s) are used for StateImages.
     * @return the boundaries of the state.
     */
    public Region getBoundaries() {
        List<Region> imageRegions = new ArrayList<>();
        for (StateImage stateImage : stateImages.toArray(new StateImage[0])) {
            // if the image has a fixed location and has been found, add this location
            stateImage.getPatterns().forEach(pattern -> {
                        Region fixedRegion = pattern.getSearchRegions().getFixedRegion();
                        if (fixedRegion.isDefined()) imageRegions.add(fixedRegion);
                    });
            // otherwise, add the snapshot locations
            List<MatchSnapshot> snapshots = stateImage.getAllMatchSnapshots();
            for (MatchSnapshot snapshot : snapshots) {
                snapshot.getMatchList().forEach(match -> imageRegions.add(match.getRegion()));
            }
        }
        for (StateRegion stateRegion : stateRegions) {
            imageRegions.add(stateRegion.getSearchRegion());
        }
        for (StateLocation stateLocation : stateLocations) {
            Location loc = stateLocation.getLocation();
            imageRegions.add(new Region(loc.getX(), loc.getY(), 0, 0));
        }
        if (imageRegions.isEmpty()) return new Region(); // the region is not defined
        Region union = imageRegions.get(0);
        for (int i=1; i<imageRegions.size(); i++) {
            union = union.getUnion(imageRegions.get(i));
        }
        return union;
    }

    public void addIllustrations(StateIllustration... stateIllustrations) {
        illustrations.addAll(List.of(stateIllustrations));
    }

    public void addIllustrations(List<StateIllustration> stateIllustrations) {
        illustrations.addAll(stateIllustrations);
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
        private int pathScore = 1;
        private LocalDateTime lastAccessed;
        private int baseProbabilityExists = 100;
        private final List<Image> scenes = new ArrayList<>();
        private final List<StateIllustration> illustrations = new ArrayList<>();

        public Builder(String stateName) {
            this.name = stateName;
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

        public Builder setPathScore(int score) {
            this.pathScore = score;
            return this;
        }

        public Builder setBaseProbabilityExists(int probabilityExists) {
            this.baseProbabilityExists = probabilityExists;
            return this;
        }

        public Builder withScenes(List<Image> scenes) {
            this.scenes.addAll(scenes);
            return this;
        }

        public Builder addScenes(Image... scenes) {
            this.scenes.addAll(List.of(scenes));
            return this;
        }

        public Builder addIllustrations(StateIllustration... stateIllustrations) {
            this.illustrations.addAll(List.of(stateIllustrations));
            return this;
        }

        public State build() {
            State state = new State();
            state.name = name;
            state.stateText = stateText;
            for (StateImage image : stateImages) image.setOwnerStateName(name);
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
            state.scenes = scenes;
            state.illustrations = illustrations;
            return state;
        }
    }
}


