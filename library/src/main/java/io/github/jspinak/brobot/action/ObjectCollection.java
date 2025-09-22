package io.github.jspinak.brobot.action;

import static io.github.jspinak.brobot.model.element.Positions.Name.TOPLEFT;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
// Removed old logging import: 
import lombok.Getter;
import lombok.Setter;

/**
 * Container for GUI elements that serve as targets for automation actions in Brobot.
 *
 * <p>ObjectCollection is a fundamental data structure in the model-based approach that aggregates
 * different types of GUI elements that can be acted upon. It provides a unified way to pass
 * multiple heterogeneous targets to actions, supporting the framework's flexibility in handling
 * various GUI interaction scenarios.
 *
 * <p>Supported element types:
 *
 * <ul>
 *   <li><b>StateImages</b>: Visual patterns to find and interact with
 *   <li><b>StateLocations</b>: Specific points for precise interactions
 *   <li><b>StateRegions</b>: Rectangular areas for spatial operations
 *   <li><b>StateStrings</b>: Text strings for keyboard input
 *   <li><b>ActionResult</b>: Results from previous find operations that can be reused
 *   <li><b>Scenes</b>: Screenshots for offline processing instead of live screen capture
 * </ul>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Supports multiple objects of each type for batch operations
 *   <li>Tracks interaction counts for each object (timesActedOn)
 *   <li>Enables offline automation using stored screenshots (Scenes)
 *   <li>Provides builder pattern for convenient construction
 * </ul>
 *
 * <p>This design allows actions to be polymorphic - the same action (e.g., Click) can operate on
 * images, regions, locations, or previous matches, making automation code more flexible and
 * reusable.
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

    /**
     * Default constructor for Jackson JSON mapping.
     */
    public ObjectCollection() {} // public for mapping

    /**
     * Checks if this collection contains no objects.
     *
     * @return true if all object lists are empty, false otherwise
     */
    public boolean isEmpty() {
        return stateLocations.isEmpty()
                && stateImages.isEmpty()
                && stateRegions.isEmpty()
                && stateStrings.isEmpty()
                && matches.isEmpty()
                && scenes.isEmpty();
    }

    // ===== Convenience Factory Methods =====

    /**
     * Creates an ObjectCollection containing a single Location.
     *
     * <p>This is a convenience method for the common case of acting on a single location.
     *
     * @param location the location to include
     * @return a new ObjectCollection containing the location
     */
    public static ObjectCollection withLocations(Location... locations) {
        return new Builder().withLocations(locations).build();
    }

    /**
     * Creates an ObjectCollection containing StateImages.
     *
     * <p>This is a convenience method for the common case of acting on state images.
     *
     * @param stateImages the state images to include
     * @return a new ObjectCollection containing the state images
     */
    public static ObjectCollection withStateImages(StateImage... stateImages) {
        return new Builder().withImages(stateImages).build();
    }

    /**
     * Creates an ObjectCollection containing Regions.
     *
     * <p>This is a convenience method for the common case of acting on regions.
     *
     * @param regions the regions to include
     * @return a new ObjectCollection containing the regions
     */
    public static ObjectCollection withRegions(Region... regions) {
        return new Builder().withRegions(regions).build();
    }

    /**
     * Creates an ObjectCollection containing StateRegions.
     *
     * <p>This is a convenience method for the common case of acting on state regions.
     *
     * @param stateRegions the state regions to include
     * @return a new ObjectCollection containing the state regions
     */
    public static ObjectCollection withStateRegions(StateRegion... stateRegions) {
        return new Builder().withRegions(stateRegions).build();
    }

    /**
     * Creates an ObjectCollection containing Strings.
     *
     * <p>This is a convenience method for the common case of typing strings.
     *
     * @param strings the strings to include
     * @return a new ObjectCollection containing the strings
     */
    public static ObjectCollection withStrings(String... strings) {
        return new Builder().withStrings(strings).build();
    }

    /**
     * Creates an ObjectCollection from an ActionResult.
     *
     * <p>This is a convenience method for using results from a previous find operation.
     *
     * @param actionResult the action result to include
     * @return a new ObjectCollection containing the action result
     */
    public static ObjectCollection fromActionResult(ActionResult actionResult) {
        return new Builder().withMatches(actionResult).build();
    }

    /**
     * Sets the timesActedOn variable to 0 for all objects, including those found in the
     * ActionResult variable. Knowing how many times an object Match was acted on is valuable for
     * understanding the actual automation as well as for performing mocks.
     */
    public void resetTimesActedOn() {
        stateImages.forEach(sio -> sio.setTimesActedOn(0));
        stateLocations.forEach(sio -> sio.setTimesActedOn(0));
        stateRegions.forEach(sio -> sio.setTimesActedOn(0));
        stateStrings.forEach(sio -> sio.setTimesActedOn(0));
        matches.forEach(m -> m.setTimesActedOn(0));
    }

    /**
     * Gets the name of the first object in the collection.
     * Searches in order: StateImages, StateLocations, StateRegions, StateStrings.
     *
     * @return the name of the first object, or empty string if no objects or names
     */
    public String getFirstObjectName() {
        if (!stateImages.isEmpty()) {
            if (!stateImages.get(0).getName().isEmpty()) return stateImages.get(0).getName();
            else if (!stateImages.get(0).getPatterns().isEmpty()
                    && !stateImages.get(0).getPatterns().get(0).getImgpath().isEmpty())
                return stateImages.get(0).getPatterns().get(0).getImgpath();
        }
        if (!stateLocations.isEmpty() && !stateLocations.get(0).getName().isEmpty())
            return stateLocations.get(0).getName();
        if (!stateRegions.isEmpty() && !stateRegions.get(0).getName().isEmpty())
            return stateRegions.get(0).getName();
        if (!stateStrings.isEmpty()) return stateStrings.get(0).getString();
        return "";
    }

    /**
     * Checks if this collection contains the specified StateImage.
     *
     * @param stateImage the StateImage to check for
     * @return true if the StateImage is in this collection
     */
    public boolean contains(StateImage stateImage) {
        for (StateImage si : stateImages) {
            if (stateImage.equals(si)) return true;
        }
        return false;
    }

    /**
     * Checks if this collection contains the specified StateRegion.
     *
     * @param stateRegion the StateRegion to check for
     * @return true if the StateRegion is in this collection
     */
    public boolean contains(StateRegion stateRegion) {
        return stateRegions.contains(stateRegion);
    }

    /**
     * Checks if this collection contains the specified StateLocation.
     *
     * @param stateLocation the StateLocation to check for
     * @return true if the StateLocation is in this collection
     */
    public boolean contains(StateLocation stateLocation) {
        return stateLocations.contains(stateLocation);
    }

    /**
     * Checks if this collection contains the specified StateString.
     *
     * @param stateString the StateString to check for
     * @return true if the StateString is in this collection
     */
    public boolean contains(StateString stateString) {
        return stateStrings.contains(stateString);
    }

    /**
     * Checks if this collection contains the specified ActionResult.
     *
     * @param m the ActionResult to check for
     * @return true if the ActionResult is in this collection
     */
    public boolean contains(ActionResult m) {
        return matches.contains(m);
    }

    /**
     * Checks if this collection contains the specified Scene.
     *
     * @param sc the Scene to check for
     * @return true if the Scene is in this collection
     */
    public boolean contains(Scene sc) {
        return scenes.contains(sc);
    }

    /**
     * Checks if this collection equals another ObjectCollection.
     * Collections are equal if they contain the same objects in all categories.
     *
     * @param objectCollection the ObjectCollection to compare with
     * @return true if the collections contain the same objects
     */
    public boolean equals(ObjectCollection objectCollection) {
        for (StateImage si : stateImages) {
            if (!objectCollection.contains(si)) {
                // Report.println(si.getName()+" is not in the objectCollection. ");
                return false;
            }
        }
        for (StateRegion sr : stateRegions) {
            if (!objectCollection.contains(sr)) {
                // Report.println(" regions different ");
                return false;
            }
        }
        for (StateLocation sl : stateLocations) {
            if (!objectCollection.contains(sl)) {
                // Report.println(" locations different ");
                return false;
            }
        }
        for (StateString ss : stateStrings) {
            if (!objectCollection.contains(ss)) {
                // Report.println(" strings different ");
                return false;
            }
        }
        for (ActionResult m : matches) {
            if (!objectCollection.contains(m)) {
                // Report.println(" matches different ");
                return false;
            }
        }
        for (Scene sc : scenes) {
            if (!objectCollection.contains(sc)) {
                // Report.println(" matches different ");
                return false;
            }
        }
        return true;
    }

    /**
     * Gets all unique image filenames from StateImages in this collection.
     *
     * @return set of all image file paths
     */
    public Set<String> getAllImageFilenames() {
        Set<String> filenames = new HashSet<>();
        stateImages.forEach(sI -> sI.getPatterns().forEach(p -> filenames.add(p.getImgpath())));
        return filenames;
    }

    /**
     * Gets all unique owner state names from objects in this collection.
     *
     * @return set of all owner state names
     */
    public Set<String> getAllOwnerStates() {
        Set<String> states = new HashSet<>();
        stateImages.forEach(si -> states.add(si.getOwnerStateName()));
        stateLocations.forEach(si -> states.add(si.getOwnerStateName()));
        stateRegions.forEach(si -> states.add(si.getOwnerStateName()));
        stateStrings.forEach(si -> states.add(si.getOwnerStateName()));
        return states;
    }

    /**
     * Returns a string representation of this collection.
     *
     * @return string containing all object lists and their sizes
     */
    @Override
    public String toString() {
        return "ObjectCollection{"
                + "stateLocations="
                + stateLocations.size()
                + ", stateImages="
                + stateImages.size()
                + ", stateRegions="
                + stateRegions.size()
                + ", stateStrings="
                + stateStrings.size()
                + ", matches="
                + matches.size()
                + ", scenes="
                + scenes.size()
                + '}';
    }

    /**
     * Builder class for constructing ObjectCollection instances with a fluent API.
     */
    public static class Builder {
        // private int lastId = 0; // currently id is given only to images
        private List<StateLocation> stateLocations = new ArrayList<>();
        private List<StateImage> stateImages = new ArrayList<>();
        private List<StateRegion> stateRegions = new ArrayList<>();
        private List<StateString> stateStrings = new ArrayList<>();
        private List<ActionResult> matches = new ArrayList<>();
        private List<Scene> scenes = new ArrayList<>();

        /**
         * Adds Locations to the collection, converting them to StateLocations.
         *
         * @param locations the Locations to add
         * @return this builder for method chaining
         */
        public Builder withLocations(Location... locations) {
            for (Location location : locations) {
                StateLocation stateLocation = location.asStateLocationInNullState();
                stateLocation.setPosition(new Position(TOPLEFT));
                this.stateLocations.add(stateLocation);
            }
            return this;
        }

        /**
         * Adds StateLocations to the collection.
         *
         * @param locations the StateLocations to add
         * @return this builder for method chaining
         */
        public Builder withLocations(StateLocation... locations) {
            Collections.addAll(this.stateLocations, locations);
            return this;
        }

        /**
         * Sets the StateLocation list, replacing any existing locations.
         *
         * @param locations the list of StateLocations
         * @return this builder for method chaining
         */
        public Builder setLocations(List<StateLocation> locations) {
            this.stateLocations = locations;
            return this;
        }

        // private Builder addImage(StateImage img) {
        //    img.
        //    this.stateImages.add()
        // }

        /**
         * Adds StateImages to the collection.
         *
         * @param stateImages the StateImages to add
         * @return this builder for method chaining
         */
        public Builder withImages(StateImage... stateImages) {
            this.stateImages.addAll(Arrays.asList(stateImages));
            return this;
        }

        /**
         * Adds a list of StateImages to the collection.
         *
         * @param stateImages the list of StateImages to add
         * @return this builder for method chaining
         */
        public Builder withImages(List<StateImage> stateImages) {
            this.stateImages.addAll(stateImages);
            return this;
        }

        /**
         * Sets the StateImage list, replacing any existing images.
         *
         * @param stateImages the list of StateImages
         * @return this builder for method chaining
         */
        public Builder setImages(List<StateImage> stateImages) {
            this.stateImages = stateImages;
            return this;
        }

        /**
         * Adds Patterns to the collection, converting them to StateImages.
         *
         * @param patterns the Patterns to add
         * @return this builder for method chaining
         */
        public Builder withPatterns(Pattern... patterns) {
            for (Pattern pattern : patterns) this.stateImages.add(pattern.inNullState());
            return this;
        }

        /**
         * Adds a list of Patterns to the collection, converting them to StateImages.
         *
         * @param patterns the list of Patterns to add
         * @return this builder for method chaining
         */
        public Builder withPatterns(List<Pattern> patterns) {
            for (Pattern pattern : patterns) this.stateImages.add(pattern.inNullState());
            return this;
        }

        /**
         * Adds all StateImages from a State to the collection.
         *
         * @param state the State containing StateImages
         * @return this builder for method chaining
         */
        public Builder withAllStateImages(State state) {
            if (state == null) {
                return this;
            } else stateImages.addAll(state.getStateImages());
            return this;
        }

        /**
         * Adds only non-shared StateImages from a State to the collection.
         *
         * @param state the State containing StateImages
         * @return this builder for method chaining
         */
        public Builder withNonSharedImages(State state) {
            if (state == null) {
                return this;
            }
            for (StateImage stateImage : state.getStateImages()) {
                if (!stateImage.isShared()) stateImages.add(stateImage);
            }
            return this;
        }

        /**
         * Adds Regions to the collection, converting them to StateRegions.
         *
         * @param regions the Regions to add
         * @return this builder for method chaining
         */
        public Builder withRegions(Region... regions) {
            for (Region region : regions) this.stateRegions.add(region.inNullState());
            return this;
        }

        /**
         * Adds StateRegions to the collection.
         *
         * @param regions the StateRegions to add
         * @return this builder for method chaining
         */
        public Builder withRegions(StateRegion... regions) {
            Collections.addAll(this.stateRegions, regions);
            return this;
        }

        /**
         * Sets the StateRegion list, replacing any existing regions.
         *
         * @param regions the list of StateRegions
         * @return this builder for method chaining
         */
        public Builder setRegions(List<StateRegion> regions) {
            this.stateRegions = regions;
            return this;
        }

        /**
         * Divides Regions into grid subregions and adds them to the collection.
         *
         * @param rows the number of rows in the grid
         * @param columns the number of columns in the grid
         * @param regions the Regions to divide
         * @return this builder for method chaining
         */
        public Builder withGridSubregions(int rows, int columns, Region... regions) {
            for (Region region : regions) {
                for (Region gridRegion : region.getGridRegions(rows, columns))
                    this.stateRegions.add(gridRegion.inNullState());
            }
            return this;
        }

        /**
         * Divides StateRegions into grid subregions and adds them to the collection.
         * Note: Subregions are created as NullState regions.
         *
         * @param rows the number of rows in the grid
         * @param columns the number of columns in the grid
         * @param regions the StateRegions to divide
         * @return this builder for method chaining
         */
        public Builder withGridSubregions(int rows, int columns, StateRegion... regions) {
            for (StateRegion region : regions) {
                for (Region gridRegion : region.getSearchRegion().getGridRegions(rows, columns))
                    this.stateRegions.add(gridRegion.inNullState());
            }
            return this;
        }

        /**
         * Adds Strings to the collection, converting them to StateStrings.
         *
         * @param strings the Strings to add
         * @return this builder for method chaining
         */
        public Builder withStrings(String... strings) {
            for (String string : strings)
                this.stateStrings.add(new StateString.InNullState().withString(string));
            return this;
        }

        /**
         * Adds StateStrings to the collection.
         *
         * @param strings the StateStrings to add
         * @return this builder for method chaining
         */
        public Builder withStrings(StateString... strings) {
            Collections.addAll(this.stateStrings, strings);
            return this;
        }

        /**
         * Sets the StateString list, replacing any existing strings.
         *
         * @param strings the list of StateStrings
         * @return this builder for method chaining
         */
        public Builder setStrings(List<StateString> strings) {
            this.stateStrings = strings;
            return this;
        }

        /**
         * Adds ActionResults to the collection.
         *
         * @param matches the ActionResults to add
         * @return this builder for method chaining
         */
        public Builder withMatches(ActionResult... matches) {
            Collections.addAll(this.matches, matches);
            return this;
        }

        /**
         * Sets the ActionResult list, replacing any existing matches.
         *
         * @param matches the list of ActionResults
         * @return this builder for method chaining
         */
        public Builder setMatches(List<ActionResult> matches) {
            this.matches = matches;
            return this;
        }

        /**
         * Converts Matches to StateRegions and adds them to the collection.
         *
         * @param matches the Matches to convert to regions
         * @return this builder for method chaining
         */
        public Builder withMatchObjectsAsRegions(Match... matches) {
            for (Match match : matches) {
                this.stateRegions.add(
                        new StateRegion.Builder()
                                .setSearchRegion(match.getRegion())
                                .setOwnerStateName("null")
                                .build());
            }
            return this;
        }

        /**
         * Converts Matches to StateImages and adds them to the collection.
         *
         * @param matches the Matches to convert to images
         * @return this builder for method chaining
         */
        public Builder withMatchObjectsAsStateImages(Match... matches) {
            for (Match match : matches) {
                this.stateImages.add(match.toStateImage());
            }
            return this;
        }

        /**
         * Adds Scenes created from image filenames.
         *
         * @param strings the image filenames for creating scenes
         * @return this builder for method chaining
         */
        public Builder withScenes(String... strings) {
            for (String string : strings) this.scenes.add(new Scene(string));
            return this;
        }

        /**
         * Adds Scenes created from Patterns.
         *
         * @param patterns the Patterns for creating scenes
         * @return this builder for method chaining
         */
        public Builder withScenes(Pattern... patterns) {
            for (Pattern pattern : patterns) this.scenes.add(new Scene(pattern));
            return this;
        }

        /**
         * Adds Scenes to the collection.
         *
         * @param scenes the Scenes to add
         * @return this builder for method chaining
         */
        public Builder withScenes(Scene... scenes) {
            this.scenes.addAll(Arrays.asList(scenes));
            return this;
        }

        /**
         * Sets the Scene list, replacing any existing scenes.
         *
         * @param scenes the list of Scenes
         * @return this builder for method chaining
         */
        public Builder withScenes(List<Scene> scenes) {
            this.scenes = scenes;
            return this;
        }

        /**
         * Builds and returns a new ObjectCollection with the configured objects.
         *
         * @return a new ObjectCollection instance
         */
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
