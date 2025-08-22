package io.github.jspinak.brobot.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import lombok.Getter;
import lombok.Setter;

import static io.github.jspinak.brobot.model.element.Positions.Name.TOPLEFT;

import java.util.*;

/**
 * Container for GUI elements that serve as targets for automation actions in Brobot.
 * 
 * <p>ObjectCollection is a fundamental data structure in the model-based approach that 
 * aggregates different types of GUI elements that can be acted upon. It provides a 
 * unified way to pass multiple heterogeneous targets to actions, supporting the 
 * framework's flexibility in handling various GUI interaction scenarios.</p>
 * 
 * <p>Supported element types:
 * <ul>
 *   <li><b>StateImages</b>: Visual patterns to find and interact with</li>
 *   <li><b>StateLocations</b>: Specific points for precise interactions</li>
 *   <li><b>StateRegions</b>: Rectangular areas for spatial operations</li>
 *   <li><b>StateStrings</b>: Text strings for keyboard input</li>
 *   <li><b>ActionResult</b>: Results from previous find operations that can be reused</li>
 *   <li><b>Scenes</b>: Screenshots for offline processing instead of live screen capture</li>
 * </ul>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Supports multiple objects of each type for batch operations</li>
 *   <li>Tracks interaction counts for each object (timesActedOn)</li>
 *   <li>Enables offline automation using stored screenshots (Scenes)</li>
 *   <li>Provides builder pattern for convenient construction</li>
 * </ul>
 * </p>
 * 
 * <p>This design allows actions to be polymorphic - the same action (e.g., Click) can 
 * operate on images, regions, locations, or previous matches, making automation code 
 * more flexible and reusable.</p>
 * 
 * @since 1.0
 * @see StateImage
 * @see StateRegion
 * @see StateLocation
 * @see ActionResult
 * @see Action
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectCollection {
    private List<StateLocation> stateLocations = new ArrayList<>();
    private List<StateImage> stateImages = new ArrayList<>();
    private List<StateRegion> stateRegions = new ArrayList<>();
    private List<StateString> stateStrings = new ArrayList<>();
    private List<ActionResult> matches = new ArrayList<>();
    private List<Scene> scenes = new ArrayList<>();

    public ObjectCollection() {} // public for mapping

    public boolean isEmpty() {
        return stateLocations.isEmpty()
                && stateImages.isEmpty()
                && stateRegions.isEmpty()
                && stateStrings.isEmpty()
                && matches.isEmpty()
                && scenes.isEmpty();
    }

    /**
     * Sets the timesActedOn variable to 0 for all objects, including those
     * found in the ActionResult variable. Knowing how many times an object Match
     * was acted on is valuable for understanding the actual automation as
     * well as for performing mocks.
     */
    public void resetTimesActedOn() {
        stateImages.forEach(sio -> sio.setTimesActedOn(0));
        stateLocations.forEach(sio -> sio.setTimesActedOn(0));
        stateRegions.forEach(sio -> sio.setTimesActedOn(0));
        stateStrings.forEach(sio -> sio.setTimesActedOn(0));
        matches.forEach(m -> m.setTimesActedOn(0));
    }

    public String getFirstObjectName() {
        if (!stateImages.isEmpty()) {
            if (!stateImages.get(0).getName().isEmpty()) return stateImages.get(0).getName();
            else if (!stateImages.get(0).getPatterns().get(0).getImgpath().isEmpty())
                return stateImages.get(0).getPatterns().get(0).getImgpath();
        }
        if (!stateLocations.isEmpty() && !stateLocations.get(0).getName().isEmpty())
            return stateLocations.get(0).getName();
        if (!stateRegions.isEmpty() && !stateRegions.get(0).getName().isEmpty())
            return stateRegions.get(0).getName();
        if (!stateStrings.isEmpty()) return stateStrings.get(0).getString();
        return "";
    }

    public boolean contains(StateImage stateImage) {
        for (StateImage si : stateImages) {
            if (stateImage.equals(si)) return true;
        }
        return false;
    }

    public boolean contains(StateRegion stateRegion) {
        return stateRegions.contains(stateRegion);
    }

    public boolean contains(StateLocation stateLocation) {
        return stateLocations.contains(stateLocation);
    }

    public boolean contains(StateString stateString) {
        return stateStrings.contains(stateString);
    }

    public boolean contains(ActionResult m) {
        return matches.contains(m);
    }

    public boolean contains(Scene sc) { return scenes.contains(sc); }

    public boolean equals(ObjectCollection objectCollection) {
        for (StateImage si : stateImages) {
            if (!objectCollection.contains(si)) {
                //Report.println(si.getName()+" is not in the objectCollection. ");
                return false;
            }
        }
        for (StateRegion sr : stateRegions) {
            if (!objectCollection.contains(sr)) {
                //Report.println(" regions different ");
                return false;
            }
        }
        for (StateLocation sl : stateLocations) {
            if (!objectCollection.contains(sl)) {
                //Report.println(" locations different ");
                return false;
            }
        }
        for (StateString ss : stateStrings) {
            if (!objectCollection.contains(ss)) {
                //Report.println(" strings different ");
                return false;
            }
        }
        for (ActionResult m : matches) {
            if (!objectCollection.contains(m)) {
                //Report.println(" matches different ");
                return false;
            }
        }
        for (Scene sc : scenes) {
            if (!objectCollection.contains(sc)) {
                //Report.println(" matches different ");
                return false;
            }
        }
        return true;
    }

    public Set<String> getAllImageFilenames() {
        Set<String> filenames = new HashSet<>();
        stateImages.forEach(sI -> sI.getPatterns().forEach(p -> filenames.add(p.getImgpath())));
        return filenames;
    }

    public Set<String> getAllOwnerStates() {
        Set<String> states = new HashSet<>();
        stateImages.forEach(si -> states.add(si.getOwnerStateName()));
        stateLocations.forEach(si -> states.add(si.getOwnerStateName()));
        stateRegions.forEach(si -> states.add(si.getOwnerStateName()));
        stateStrings.forEach(si -> states.add(si.getOwnerStateName()));
        return states;
    }

    @Override
    public String toString() {
        return "ObjectCollection{" +
                "stateLocations=" + stateLocations.size() +
                ", stateImages=" + stateImages.size() +
                ", stateRegions=" + stateRegions.size() +
                ", stateStrings=" + stateStrings.size() +
                ", matches=" + matches.size() +
                ", scenes=" + scenes.size() +
                '}';
    }

    public static class Builder {
        //private int lastId = 0; // currently id is given only to images
        private List<StateLocation> stateLocations = new ArrayList<>();
        private List<StateImage> stateImages = new ArrayList<>();
        private List<StateRegion> stateRegions = new ArrayList<>();
        private List<StateString> stateStrings = new ArrayList<>();
        private List<ActionResult> matches = new ArrayList<>();
        private List<Scene> scenes = new ArrayList<>();

        public Builder withLocations(Location... locations) {
            for (Location location : locations) {
                StateLocation stateLocation = location.asStateLocationInNullState();
                stateLocation.setPosition(new Position(TOPLEFT));
                this.stateLocations.add(stateLocation);
            }
            return this;
        }

        public Builder withLocations(StateLocation... locations) {
            Collections.addAll(this.stateLocations, locations);
            return this;
        }

        public Builder setLocations(List<StateLocation> locations) {
            this.stateLocations = locations;
            return this;
        }

        //private Builder addImage(StateImage img) {
        //    img.
        //    this.stateImages.add()
        //}

        public Builder withImages(StateImage... stateImages) {
            this.stateImages.addAll(Arrays.asList(stateImages));
            return this;
        }

        public Builder withImages(List<StateImage> stateImages) {
            this.stateImages.addAll(stateImages);
            return this;
        }

        public Builder setImages(List<StateImage> stateImages) {
            this.stateImages = stateImages;
            return this;
        }

        public Builder withPatterns(Pattern... patterns) {
            for (Pattern pattern : patterns) this.stateImages.add(pattern.inNullState());
            return this;
        }

        public Builder withPatterns(List<Pattern> patterns) {
            for (Pattern pattern : patterns) this.stateImages.add(pattern.inNullState());
            return this;
        }

        public Builder withAllStateImages(State state) {
            if (state == null) {
                ConsoleReporter.print("null state passed| ");
                return this;
            } else stateImages.addAll(state.getStateImages());
            return this;
        }

        public Builder withNonSharedImages(State state) {
            if (state == null) {
                ConsoleReporter.print("null state passed| ");
                return this;
            }
            for (StateImage stateImage : state.getStateImages()) {
                if (!stateImage.isShared()) stateImages.add(stateImage);
            }
            return this;
        }

        public Builder withRegions(Region... regions) {
            for (Region region : regions) this.stateRegions.add(region.inNullState());
            return this;
        }

        public Builder withRegions(StateRegion... regions) {
            Collections.addAll(this.stateRegions, regions);
            return this;
        }

        public Builder setRegions(List<StateRegion> regions) {
            this.stateRegions = regions;
            return this;
        }

        public Builder withGridSubregions(int rows, int columns, Region... regions) {
            for (Region region : regions) {
                for (Region gridRegion : region.getGridRegions(rows, columns))
                    this.stateRegions.add(gridRegion.inNullState());
            }
            return this;
        }

        // should the State info be kept if it's a subregion? this is not clear, so we're sticking with NullState for now
        public Builder withGridSubregions(int rows, int columns, StateRegion... regions) {
            for (StateRegion region : regions) {
                for (Region gridRegion : region.getSearchRegion().getGridRegions(rows, columns))
                    this.stateRegions.add(gridRegion.inNullState());
            }
            return this;
        }

        public Builder withStrings(String... strings) {
            for (String string : strings) this.stateStrings.add(new StateString.InNullState().withString(string));
            return this;
        }

        public Builder withStrings(StateString... strings) {
            Collections.addAll(this.stateStrings, strings);
            return this;
        }

        public Builder setStrings(List<StateString> strings) {
            this.stateStrings = strings;
            return this;
        }

        public Builder withMatches(ActionResult... matches) {
            Collections.addAll(this.matches, matches);
            return this;
        }

        public Builder setMatches(List<ActionResult> matches) {
            this.matches = matches;
            return this;
        }

        public Builder withMatchObjectsAsRegions(Match... matches) {
            for (Match match : matches) {
                this.stateRegions.add(new StateRegion.Builder()
                        .setSearchRegion(match.getRegion())
                        .setOwnerStateName("null")
                        .build());
            }
            return this;
        }

        public Builder withMatchObjectsAsStateImages(Match... matches) {
            for (Match match : matches) {
                this.stateImages.add(match.toStateImage());
            }
            return this;
        }

        public Builder withScenes(String... strings) {
            for (String string : strings) this.scenes.add(new Scene(string));
            return this;
        }

        public Builder withScenes(Pattern... patterns) {
            for (Pattern pattern : patterns) this.scenes.add(new Scene(pattern));
            return this;
        }

        public Builder withScenes(Scene... scenes) {
            this.scenes.addAll(Arrays.asList(scenes));
            return this;
        }

        public Builder withScenes(List<Scene> scenes) {
            this.scenes = scenes;
            return this;
        }

        public ObjectCollection build() {
            ObjectCollection objectCollection = new ObjectCollection();
            objectCollection.stateImages = stateImages;
            objectCollection.stateLocations = stateLocations;
            objectCollection.stateRegions = stateRegions;
            objectCollection.stateStrings = stateStrings;
            objectCollection.matches = matches;
            objectCollection.scenes = scenes;
            return objectCollection;
        }
    }
}
