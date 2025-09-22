package io.github.jspinak.brobot.model.state;

import java.util.*;

import org.bytedeco.opencv.opencv_core.Mat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.analysis.color.kmeans.KmeansProfilesAllSchemas;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.element.Anchor;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.match.Match;

import lombok.Getter;
import lombok.Setter;

/**
 * Visual pattern identifier for States in the Brobot model-based GUI automation framework.
 *
 * <p>StateImage is a fundamental building block of the state structure (Ω), serving as the primary
 * means of identifying and verifying GUI states. It encapsulates one or more Pattern objects (image
 * templates) that, when found on screen, indicate the presence of a specific state configuration.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li><b>Pattern Collection</b>: Contains multiple Patterns for robust state identification
 *       (handling variations in appearance, different resolutions, etc.)
 *   <li><b>State Ownership</b>: Belongs to a specific State, though can be marked as "shared" when
 *       the same visual element appears across multiple states
 *   <li><b>Color Analysis</b>: Supports advanced color-based matching through k-means clustering
 *       and color profiles for more sophisticated pattern recognition
 *   <li><b>Dynamic Images</b>: Can be marked as dynamic when the visual content changes frequently,
 *       requiring special handling
 * </ul>
 *
 * <p>In the model-based approach, StateImages serve multiple critical functions:
 *
 * <ul>
 *   <li>State identification: Determining which state is currently active
 *   <li>Action targets: Providing clickable/interactive elements within states
 *   <li>Transition triggers: Visual elements that initiate state transitions
 *   <li>Verification points: Confirming successful navigation or action completion
 * </ul>
 *
 * <p>The class supports both traditional pattern matching and advanced color-based analysis, making
 * it adaptable to various GUI automation challenges including dynamic content, theme variations,
 * and cross-platform differences.
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * // Create StateImage with patterns
 * StateImage submitButton = new StateImage.Builder()
 *     .setName("submitButton")
 *     .addPatterns("submit.png", "submit-hover.png")  // Multiple variations
 *     .build();
 *
 * // Find and click
 * action.click(submitButton);
 *
 * // Find with custom options
 * PatternFindOptions options = new PatternFindOptions.Builder()
 *     .setSimilarity(0.85)
 *     .setStrategy(PatternFindOptions.Strategy.BEST)
 *     .build();
 * ActionResult result = action.perform(options, submitButton.asObjectCollection());
 *
 * // Using conditional chains (recommended)
 * ConditionalActionChain
 *     .find(new PatternFindOptions.Builder().build())
 *     .ifFoundClick()
 *     .ifNotFoundLog("Submit button not found")
 *     .perform(action, submitButton.asObjectCollection());
 *
 * // Configure search regions
 * StateImage logo = new StateImage.Builder()
 *     .addPattern("logo.png")
 *     .setSearchRegionForAllPatterns(new Region(0, 0, 400, 200))  // Top area only
 *     .build();
 *
 * // Set click position offset
 * StateImage checkbox = new StateImage.Builder()
 *     .addPattern("checkbox.png")
 *     .setOffsetForAllPatterns(-20, 0)  // Click 20 pixels to the left
 *     .build();
 *
 * // Process multiple patterns
 * StateImage multiPattern = new StateImage.Builder()
 *     .addPatterns("option1.png", "option2.png", "option3.png")
 *     .build();
 *
 * ActionResult matches = action.find(multiPattern);
 * for (Match match : matches.getMatchList()) {
 *     System.out.println("Found pattern at: " + match.getLocation());
 *     action.highlight(match);
 * }
 *
 * // Use as part of ObjectCollection
 * ObjectCollection objects = new ObjectCollection.Builder()
 *     .withImages(submitButton, checkbox)
 *     .withStrings("Form submitted")
 *     .build();
 * }</pre>
 *
 * @since 1.0
 * @see Pattern
 * @see State
 * @see StateObject
 * @see ColorCluster
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateImage implements StateObject {

    private Long id; // set by the StateImageEntity in the App module and important for creating
    // StateImageLogs.
    private Long projectId = 0L;
    private StateObject.Type objectType = StateObject.Type.IMAGE;
    private String name = "";
    private List<Pattern> patterns = new ArrayList<>();
    private String ownerStateName =
            "null"; // ownerStateName is set by the State when the object is added
    private Long ownerStateId = null; // set by the State when the object is added
    private int timesActedOn = 0;
    private boolean shared = false; // shared means also found in other states

    // for color analysis and illustration
    private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
    private ColorCluster colorCluster = new ColorCluster();
    private int index; // a unique identifier. used for classification matrices.
    private boolean dynamic = false; // dynamic images cannot be found using pattern matching

    /*
    Persisting Mat objects with JPA is more complex than persisting BufferedImage objects. It requires creating a
    wrapper object that includes information about the Mat size and type. These Mat objects do not contain unique
    data: they are here for convenience and can be recreated with the patterns' BufferedImage objects.
     */
    @JsonIgnore private Mat oneColumnBGRMat; // initialized when program is run
    @JsonIgnore private Mat oneColumnHSVMat; // initialized when program is run

    @JsonIgnore
    private Mat imagesMat; // initialized when program is run, shows the images in the StateImage

    @JsonIgnore
    private Mat profilesMat; // initialized when program is run, shows the color profiles in the

    // StateImage

    /*
    This stores the ids of transitions to other states for which this StateImage is involved.
    Transition ids are first recorded when transitions are saved to the database.
     */
    private Set<Long> involvedTransitionIds = new HashSet<>();

    // Cross-state search region configuration
    private SearchRegionOnObject searchRegionOnObject;

    // Custom highlight color for this StateImage (e.g., "#00FF00" for green, "#0000FF" for blue)
    private String highlightColor;

    // Last matches found for this StateImage - used for resolving SearchRegionOnObject dependencies
    // This is the single source of truth for where this StateImage was last found
    @JsonIgnore private List<Match> lastMatchesFound = new ArrayList<>();

    /**
     * Returns a unique string identifier for this StateImage.
     * Combines object type, name, and pattern information.
     *
     * @return concatenated string of type, name, and patterns
     */
    public String getIdAsString() {
        return objectType.name() + name + patterns.toString();
    }

    /**
     * Sets the Position for each Pattern in the Image.
     *
     * @param position the Position to use for each Pattern.
     */
    public void setPositionForAllPatterns(Position position) {
        patterns.forEach(pattern -> pattern.setTargetPosition(position));
    }

    /**
     * Sets the target offset for all patterns in this StateImage.
     * The offset adjusts the click/action point relative to the pattern match.
     *
     * @param offset the Location offset to apply to all patterns
     */
    public void setOffsetForAllPatterns(Location offset) {
        patterns.forEach(pattern -> pattern.setTargetOffset(offset));
    }

    /**
     * Sets the Anchor objects for each Pattern in the Image. Existing Anchor objects will be
     * deleted.
     *
     * @param anchors the Anchor objects to use for each Pattern.
     */
    public void setAnchors(Anchor... anchors) {
        patterns.forEach(pattern -> pattern.getAnchors().setAnchorList(List.of(anchors)));
    }

    /**
     * Increments the counter tracking how many times this StateImage has been acted upon.
     * Useful for tracking usage frequency and interaction statistics.
     */
    public void addTimesActedOn() {
        this.timesActedOn++;
    }

    /**
     * Returns the combined match history from all patterns in this StateImage.
     * Merges individual pattern histories into a single ActionHistory.
     *
     * @return ActionHistory containing all pattern match records
     */
    public ActionHistory getMatchHistory() {
        ActionHistory matchHistory = new ActionHistory();
        patterns.forEach(p -> matchHistory.merge(p.getMatchHistory()));
        return matchHistory;
    }

    /**
     * Sets the search regions for each Pattern in the Image. Existing search regions will be
     * deleted.
     *
     * @param regions the regions to set for each Pattern.
     */
    public void setSearchRegions(Region... regions) {
        patterns.forEach(pattern -> pattern.setSearchRegionsTo(regions));
    }

    /**
     * Sets the fixed search region for all patterns in this StateImage.
     *
     * @param region the fixed region to set
     */
    public void setFixedSearchRegion(Region region) {
        patterns.forEach(
                pattern -> {
                    pattern.getSearchRegions().setFixedRegion(region);
                    pattern.setFixed(true);
                });
    }

    /**
     * Finds snapshots from all patterns.
     *
     * @return a list of snapshots
     */
    public List<ActionRecord> getAllMatchSnapshots() {
        List<ActionRecord> matchSnapshots = new ArrayList<>();
        patterns.forEach(
                pattern -> matchSnapshots.addAll(pattern.getMatchHistory().getSnapshots()));
        return matchSnapshots;
    }

    /**
     * Returns a random snapshot from patterns that match the given action configuration.
     * Useful for mock mode and testing scenarios.
     *
     * @param actionConfig configuration to filter similar snapshots
     * @return Optional containing a random ActionRecord, or empty if no snapshots match
     */
    public Optional<ActionRecord> getRandomSnapshot(ActionConfig actionConfig) {
        List<ActionRecord> snapshots = new ArrayList<>();
        patterns.forEach(
                pattern ->
                        snapshots.addAll(
                                pattern.getMatchHistory().getSimilarSnapshots(actionConfig)));
        if (snapshots.isEmpty()) return Optional.empty();
        return Optional.of(snapshots.get(new Random().nextInt(snapshots.size())));
    }

    /**
     * Checks if this StateImage contains any patterns.
     *
     * @return true if no patterns are present, false otherwise
     */
    public boolean isEmpty() {
        return patterns.isEmpty();
    }

    /**
     * When a Pattern is fixed, it's defined when the fixed region is defined. When it's not fixed,
     * it's defined when at least one of its search regions is defined. The StateImage is defined if
     * at least one Pattern is defined.
     *
     * @return true if defined
     */
    public boolean isDefined() {
        for (Pattern pattern : patterns) {
            if (pattern.isDefined()) return true;
        }
        return false;
    }

    /**
     * Checks if this StateImage has a defined search region. A search region is defined if any
     * pattern has a fixed region or defined search regions.
     *
     * @return true if any pattern has a defined search region
     */
    public boolean hasDefinedSearchRegion() {
        for (Pattern pattern : patterns) {
            // Check if fixed region is set and defined
            Region fixedRegion = pattern.getSearchRegions().getFixedRegion();
            if (fixedRegion != null && fixedRegion.isDefined()) return true;
            // Check if any search regions are defined
            if (!pattern.getSearchRegions().getRegions().isEmpty()) {
                for (Region region : pattern.getSearchRegions().getRegions()) {
                    if (region.isDefined()) return true;
                }
            }
        }
        return false;
    }

    /**
     * If a pattern has a defined, fixed region, it is included. Otherwise, all search regions are
     * included.
     *
     * @return search regions for all patterns.
     */
    public List<Region> getAllSearchRegions() {
        List<Region> regions = new ArrayList<>();
        patterns.forEach(p -> regions.addAll(p.getRegions()));
        return regions;
    }

    /**
     * Returns all defined fixed regions from patterns in this StateImage.
     * Fixed regions are specific search areas that don't change.
     *
     * @return list of defined fixed regions
     */
    @JsonIgnore
    public List<Region> getDefinedFixedRegions() {
        List<Region> definedFixed = new ArrayList<>();
        for (Pattern pattern : patterns) {
            Region fixedRegion = pattern.getSearchRegions().getFixedRegion();
            if (fixedRegion != null && fixedRegion.isDefined()) definedFixed.add(fixedRegion);
        }
        return definedFixed;
    }

    /**
     * Returns the largest defined fixed region among all patterns,
     * or a new empty Region if none exist.
     *
     * @return largest fixed region or new Region instance
     */
    @JsonIgnore
    public Region getLargestDefinedFixedRegionOrNewRegion() {
        return getDefinedFixedRegions().stream()
                .max(Comparator.comparingInt(Region::size))
                .orElse(new Region());
    }

    /**
     * Converts this StateImage to an ObjectCollection containing only this image.
     * Useful for Action methods that require ObjectCollection parameters.
     *
     * @return ObjectCollection containing this StateImage
     */
    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder().withImages(this).build();
    }

    /**
     * Calculates the average width of all patterns in this StateImage.
     *
     * @return average width in pixels, or 0 if no patterns exist
     */
    public double getAverageWidth() {
        double sum = 0;
        for (Pattern p : patterns) sum += p.w();
        return sum / patterns.size();
    }

    /**
     * Calculates the average height of all patterns in this StateImage.
     *
     * @return average height in pixels, or 0 if no patterns exist
     */
    public double getAverageHeight() {
        double sum = 0;
        for (Pattern p : patterns) sum += p.h();
        return sum / patterns.size();
    }

    /**
     * Finds the maximum width among all patterns in this StateImage.
     *
     * @return maximum width in pixels, or 0 if no patterns exist
     */
    public int getMaxWidth() {
        if (isEmpty()) return 0;
        int max = patterns.get(0).w();
        for (int i = 1; i < patterns.size(); i++) {
            max = Math.max(max, patterns.get(i).w());
        }
        return max;
    }

    /**
     * Finds the maximum height among all patterns in this StateImage.
     *
     * @return maximum height in pixels, or 0 if no patterns exist
     */
    public int getMaxHeight() {
        if (isEmpty()) return 0;
        int max = patterns.get(0).h();
        for (int i = 1; i < patterns.size(); i++) {
            max = Math.max(max, patterns.get(i).h());
        }
        return max;
    }

    /**
     * Finds the minimum size (area) among all patterns in this StateImage.
     *
     * @return minimum size in pixels (width * height), or 0 if no patterns exist
     */
    public int getMinSize() {
        if (patterns.isEmpty()) return 0;
        int minSize = patterns.get(0).size();
        for (int i = 1; i < patterns.size(); i++) {
            minSize = Math.min(minSize, patterns.get(i).size());
        }
        return minSize;
    }

    /**
     * Finds the maximum size (area) among all patterns in this StateImage.
     *
     * @return maximum size in pixels (width * height), or 0 if no patterns exist
     */
    public int getMaxSize() {
        if (patterns.isEmpty()) return 0;
        int maxSize = patterns.get(0).size();
        for (int i = 1; i < patterns.size(); i++) {
            maxSize = Math.max(maxSize, patterns.get(i).size());
        }
        return maxSize;
    }

    /**
     * Returns a string representation of this StateImage.
     * Includes name, id, owner state, search regions, and pattern sizes.
     *
     * @return detailed string representation
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("StateImage:");
        stringBuilder.append(" name=").append(name);
        stringBuilder.append(" id=").append(id);
        stringBuilder.append(" ownerState=").append(ownerStateName);
        stringBuilder.append(" searchRegions=");
        getAllSearchRegions().forEach(stringBuilder::append);
        stringBuilder
                .append(" fixedSearchRegion=")
                .append(getLargestDefinedFixedRegionOrNewRegion());
        stringBuilder.append(" snapshotRegions=");
        getAllMatchSnapshots()
                .forEach(snapshot -> snapshot.getMatchList().forEach(stringBuilder::append));
        if (!patterns.isEmpty()) stringBuilder.append(" pattern sizes =");
        patterns.forEach(p -> stringBuilder.append(" ").append(p.size()));
        return stringBuilder.toString();
    }

    /**
     * Adds patterns from image files specified by their filenames.
     *
     * @param filenames variable number of image filenames to create patterns from
     */
    public void addPatterns(String... filenames) {
        for (String name : filenames) patterns.add(new Pattern(name));
    }

    /**
     * Adds existing Pattern objects to this StateImage.
     *
     * @param patterns variable number of Pattern objects to add
     */
    public void addPatterns(Pattern... patterns) {
        Collections.addAll(this.patterns, patterns);
    }

    public static class Builder {
        private String name = "";
        private List<Pattern> patterns = new ArrayList<>();
        private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
        private int index;
        private String ownerStateName = "null";
        private Long ownerStateId = null;
        private Position positionForAllPatterns;
        private Location offsetForAllPatterns;
        private Region searchRegionForAllPatterns;
        private SearchRegionOnObject searchRegionOnObject;
        private boolean fixedForAllPatterns = false;
        private ActionHistory actionHistoryForAllPatterns;
        private String highlightColor;

        /**
         * Sets the name for the StateImage being built.
         *
         * @param name the name to assign
         * @return this builder for method chaining
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the complete list of patterns for the StateImage.
         * If no name has been set, uses the first pattern's name.
         *
         * @param patterns list of Pattern objects
         * @return this builder for method chaining
         */
        public Builder setPatterns(List<Pattern> patterns) {
            this.patterns = patterns;
            if (!patterns.isEmpty()) setNameFromPatternIfEmpty(patterns.get(0));
            return this;
        }

        /**
         * Adds a single Pattern to the StateImage being built.
         * If no name has been set, uses this pattern's name.
         *
         * @param pattern the Pattern to add
         * @return this builder for method chaining
         */
        public Builder addPattern(Pattern pattern) {
            this.patterns.add(pattern);
            setNameFromPatternIfEmpty(pattern);
            return this;
        }

        /**
         * Creates and adds a Pattern from an image filename.
         * If no name has been set, uses this pattern's name.
         *
         * @param filename the image filename to create a pattern from
         * @return this builder for method chaining
         */
        public Builder addPattern(String filename) {
            Pattern pattern = new Pattern(filename);
            this.patterns.add(pattern);
            setNameFromPatternIfEmpty(pattern);
            return this;
        }

        /**
         * Creates and adds multiple Patterns from image filenames.
         *
         * @param imageNames variable number of image filenames
         * @return this builder for method chaining
         */
        public Builder addPatterns(String... imageNames) {
            for (String imageName : imageNames) {
                addPattern(imageName);
            }
            return this;
        }

        /**
         * Sets the k-means color profiles for advanced color-based matching.
         *
         * @param kmeansProfilesAllSchemas the k-means profiles to use
         * @return this builder for method chaining
         */
        public Builder setKmeansProfilesAllSchemas(
                KmeansProfilesAllSchemas kmeansProfilesAllSchemas) {
            this.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
            return this;
        }

        /**
         * Sets the unique index identifier for classification purposes.
         *
         * @param index the unique index value
         * @return this builder for method chaining
         */
        public Builder setIndex(int index) {
            this.index = index;
            return this;
        }

        /**
         * Sets the name of the State that owns this StateImage.
         *
         * @param ownerStateName the owner state's name
         * @return this builder for method chaining
         */
        public Builder setOwnerStateName(String ownerStateName) {
            this.ownerStateName = ownerStateName;
            return this;
        }

        /**
         * Sets a Position to be applied to all patterns in the StateImage.
         * The Position determines the click/action point relative to the pattern.
         *
         * @param position the Position to apply
         * @return this builder for method chaining
         */
        public Builder setPositionForAllPatterns(Position position) {
            this.positionForAllPatterns = position;
            return this;
        }

        /**
         * Sets a Position for all patterns using percentage coordinates.
         *
         * @param percentOfWidth percentage of pattern width (0-100)
         * @param percentOfHeight percentage of pattern height (0-100)
         * @return this builder for method chaining
         */
        public Builder setPositionForAllPatterns(int percentOfWidth, int percentOfHeight) {
            this.positionForAllPatterns = new Position(percentOfWidth, percentOfHeight);
            return this;
        }

        /**
         * Sets a Location offset to be applied to all patterns.
         * The offset adjusts the click/action point in pixels.
         *
         * @param offset the Location offset to apply
         * @return this builder for method chaining
         */
        public Builder setOffsetForAllPatterns(Location offset) {
            this.offsetForAllPatterns = offset;
            return this;
        }

        /**
         * Sets a pixel offset for all patterns using x,y coordinates.
         *
         * @param xOffset horizontal offset in pixels
         * @param yOffset vertical offset in pixels
         * @return this builder for method chaining
         */
        public Builder setOffsetForAllPatterns(int xOffset, int yOffset) {
            this.offsetForAllPatterns = new Location(xOffset, yOffset);
            return this;
        }

        /**
         * Sets a search region to be applied to all patterns.
         * Patterns will only be searched within this region.
         *
         * @param searchRegion the Region to search within
         * @return this builder for method chaining
         */
        public Builder setSearchRegionForAllPatterns(Region searchRegion) {
            this.searchRegionForAllPatterns = searchRegion;
            return this;
        }

        /**
         * Sets the search region configuration relative to another object.
         * Enables dynamic search regions based on other StateImages.
         *
         * @param searchRegionOnObject the cross-object search configuration
         * @return this builder for method chaining
         */
        public Builder setSearchRegionOnObject(SearchRegionOnObject searchRegionOnObject) {
            this.searchRegionOnObject = searchRegionOnObject;
            return this;
        }

        /**
         * Sets whether all patterns should use fixed search regions.
         * Fixed regions don't adapt based on screen content.
         *
         * @param fixed true to use fixed regions, false for adaptive
         * @return this builder for method chaining
         */
        public Builder setFixedForAllPatterns(boolean fixed) {
            this.fixedForAllPatterns = fixed;
            return this;
        }

        /**
         * Sets a custom highlight color for this StateImage. When this image is found, it will be
         * highlighted with this color.
         *
         * @param color the color in hex format (e.g., "#00FF00" for green, "#0000FF" for blue)
         * @return this builder for method chaining
         */
        public Builder setHighlightColor(String color) {
            this.highlightColor = color;
            return this;
        }

        /**
         * Sets ActionHistory for all patterns in this StateImage. This is required for mock mode
         * finds to work.
         *
         * @param actionHistory the ActionHistory to apply to all patterns
         * @return this builder for method chaining
         */
        public Builder withActionHistory(ActionHistory actionHistory) {
            this.actionHistoryForAllPatterns = actionHistory;
            return this;
        }

        /**
         * Sets ActionHistory for all patterns using a supplier function. Useful for lazy
         * initialization or conditional creation.
         *
         * @param historySupplier supplier that creates the ActionHistory
         * @return this builder for method chaining
         */
        public Builder withActionHistory(
                java.util.function.Supplier<ActionHistory> historySupplier) {
            this.actionHistoryForAllPatterns = historySupplier.get();
            return this;
        }

        /**
         * Sets ActionHistory for all patterns using a single ActionRecord. Useful for simple test
         * scenarios.
         *
         * @param record the ActionRecord to add as a single snapshot
         * @return this builder for method chaining
         */
        public Builder withActionHistory(ActionRecord record) {
            ActionHistory history = new ActionHistory();
            history.addSnapshot(record);
            this.actionHistoryForAllPatterns = history;
            return this;
        }

        /**
         * Returns a string representation of this builder's current state.
         * Shows the StateImage name and pattern names.
         *
         * @return string representation of the builder
         */
        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("StateImage\n");
            stringBuilder.append("images\n");
            stringBuilder.append(this.name).append("\n");
            patterns.forEach(pattern -> stringBuilder.append(pattern.getName()).append(" "));
            return stringBuilder.toString();
        }

        /**
         * Sets the StateImage name from a pattern if no name has been set.
         * Used internally to auto-populate names from pattern filenames.
         *
         * @param pattern the Pattern to potentially get the name from
         */
        private void setNameFromPatternIfEmpty(Pattern pattern) {
            // This condition now safely handles cases where 'name' is null or empty.
            if ((name == null || name.isEmpty()) && pattern != null && pattern.getName() != null) {
                name = pattern.getName();
            }
        }

        /**
         * Builds the StateImage with all configured properties.
         * Applies all pattern-level settings that were configured for all patterns.
         *
         * @return the constructed StateImage instance
         */
        public StateImage build() {
            StateImage stateImage = new StateImage();
            stateImage.name = name;
            stateImage.patterns = patterns;
            stateImage.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
            stateImage.index = index;
            stateImage.ownerStateName = ownerStateName;
            stateImage.ownerStateId = ownerStateId;
            stateImage.searchRegionOnObject = searchRegionOnObject;
            stateImage.highlightColor = highlightColor;
            if (positionForAllPatterns != null)
                stateImage
                        .getPatterns()
                        .forEach(pattern -> pattern.setTargetPosition(positionForAllPatterns));
            if (offsetForAllPatterns != null)
                stateImage
                        .getPatterns()
                        .forEach(pattern -> pattern.setTargetOffset(offsetForAllPatterns));
            if (searchRegionForAllPatterns != null)
                stateImage
                        .getPatterns()
                        .forEach(pattern -> pattern.addSearchRegion(searchRegionForAllPatterns));
            if (fixedForAllPatterns)
                stateImage.getPatterns().forEach(pattern -> pattern.setFixed(true));
            if (actionHistoryForAllPatterns != null)
                stateImage
                        .getPatterns()
                        .forEach(pattern -> pattern.setMatchHistory(actionHistoryForAllPatterns));
            return stateImage;
        }

        /**
         * Creates a generic StateImage with default values.
         * Used for testing or placeholder purposes.
         *
         * @return a generic StateImage instance
         */
        public StateImage generic() {
            StateImage stateImage = new StateImage();
            stateImage.name = "generic";
            stateImage.ownerStateName = "null";
            stateImage.ownerStateId = null;
            return stateImage;
        }
    }

    /**
     * Adds ActionRecord snapshots to all patterns in this StateImage. This is useful for setting up
     * mock data for testing or providing historical match data for the mock framework.
     *
     * @param snapshots The ActionRecord snapshots to add to all patterns
     * @return This StateImage instance for method chaining
     */
    public StateImage addActionSnapshotsToAllPatterns(ActionRecord... snapshots) {
        if (snapshots == null || snapshots.length == 0) {
            return this;
        }

        // Create a shared ActionHistory with all snapshots
        ActionHistory history = new ActionHistory();
        for (ActionRecord snapshot : snapshots) {
            history.addSnapshot(snapshot);
        }

        // Apply the history to all patterns
        for (Pattern pattern : patterns) {
            // If pattern already has history, merge with existing
            if (pattern.getMatchHistory() != null
                    && !pattern.getMatchHistory().getSnapshots().isEmpty()) {
                ActionHistory existingHistory = pattern.getMatchHistory();
                for (ActionRecord snapshot : snapshots) {
                    existingHistory.addSnapshot(snapshot);
                }
            } else {
                // Set new history
                pattern.setMatchHistory(history);
            }
        }

        return this;
    }

    /**
     * Converts this StateImage to an ObjectCollection containing only this image. Useful for Action
     * methods that require ObjectCollection parameters.
     *
     * @return ObjectCollection containing this StateImage
     */
    public ObjectCollection toObjectCollection() {
        return new ObjectCollection.Builder().withImages(this).build();
    }
}
