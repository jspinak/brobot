package io.github.jspinak.brobot.model.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.analysis.color.kmeans.KmeansProfilesAllSchemas;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Anchor;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import java.util.*;

/**
 * Visual pattern identifier for States in the Brobot model-based GUI automation framework.
 * 
 * <p>StateImage is a fundamental building block of the state structure (Ω), serving as the 
 * primary means of identifying and verifying GUI states. It encapsulates one or more Pattern 
 * objects (image templates) that, when found on screen, indicate the presence of a specific 
 * state configuration.</p>
 * 
 * <p>Key concepts:
 * <ul>
 *   <li><b>Pattern Collection</b>: Contains multiple Patterns for robust state identification 
 *       (handling variations in appearance, different resolutions, etc.)</li>
 *   <li><b>State Ownership</b>: Belongs to a specific State, though can be marked as "shared" 
 *       when the same visual element appears across multiple states</li>
 *   <li><b>Color Analysis</b>: Supports advanced color-based matching through k-means clustering 
 *       and color profiles for more sophisticated pattern recognition</li>
 *   <li><b>Dynamic Images</b>: Can be marked as dynamic when the visual content changes frequently, 
 *       requiring special handling</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateImages serve multiple critical functions:
 * <ul>
 *   <li>State identification: Determining which state is currently active</li>
 *   <li>Action targets: Providing clickable/interactive elements within states</li>
 *   <li>Transition triggers: Visual elements that initiate state transitions</li>
 *   <li>Verification points: Confirming successful navigation or action completion</li>
 * </ul>
 * </p>
 * 
 * <p>The class supports both traditional pattern matching and advanced color-based analysis,
 * making it adaptable to various GUI automation challenges including dynamic content,
 * theme variations, and cross-platform differences.</p>
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

    private Long id; // set by the StateImageEntity in the App module and important for creating StateImageLogs.
    private Long projectId = 0L;
    private StateObject.Type objectType = StateObject.Type.IMAGE;
    private String name = "";
    private List<Pattern> patterns = new ArrayList<>();
    private String ownerStateName = "null"; // ownerStateName is set by the State when the object is added
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
    @JsonIgnore
    private Mat oneColumnBGRMat; // initialized when program is run
    @JsonIgnore
    private Mat oneColumnHSVMat; // initialized when program is run
    @JsonIgnore
    private Mat imagesMat; // initialized when program is run, shows the images in the StateImage
    @JsonIgnore
    private Mat profilesMat; // initialized when program is run, shows the color profiles in the StateImage

    /*
    This stores the ids of transitions to other states for which this StateImage is involved.
    Transition ids are first recorded when transitions are saved to the database.
     */
    private Set<Long> involvedTransitionIds = new HashSet<>();

    // Cross-state search region configuration
    private SearchRegionOnObject searchRegionOnObject;

    public String getIdAsString() {
        return objectType.name() + name + patterns.toString();
    }

    /**
     * Sets the Position for each Pattern in the Image.
     * @param position the Position to use for each Pattern.
     */
    public void setPositionForAllPatterns(Position position) {
        patterns.forEach(pattern -> pattern.setTargetPosition(position));
    }

    public void setOffsetForAllPatterns(Location offset) {
        patterns.forEach(pattern -> pattern.setTargetOffset(offset));
    }

    /**
     * Sets the Anchor objects for each Pattern in the Image. Existing Anchor objects will be deleted.
     * @param anchors the Anchor objects to use for each Pattern.
     */
    public void setAnchors(Anchor... anchors) {
        patterns.forEach(pattern -> pattern.getAnchors().setAnchorList(List.of(anchors)));
    }

    public void addTimesActedOn() {
        this.timesActedOn++;
    }

    public ActionHistory getMatchHistory() {
        ActionHistory matchHistory = new ActionHistory();
        patterns.forEach(p -> matchHistory.merge(p.getMatchHistory()));
        return matchHistory;
    }

    /**
     * Sets the search regions for each Pattern in the Image. Existing search regions will be deleted.
     * @param regions the regions to set for each Pattern.
     */
    public void setSearchRegions(Region... regions) {
        patterns.forEach(pattern -> pattern.setSearchRegionsTo(regions));
    }

    /**
     * Sets the fixed search region for all patterns in this StateImage.
     * @param region the fixed region to set
     */
    public void setFixedSearchRegion(Region region) {
        patterns.forEach(pattern -> {
            pattern.getSearchRegions().setFixedRegion(region);
            pattern.setFixed(true);
        });
    }

    /**
     * Finds snapshots from all patterns.
     * @return a list of snapshots
     */
    public List<ActionRecord> getAllMatchSnapshots() {
        List<ActionRecord> matchSnapshots = new ArrayList<>();
        patterns.forEach(pattern -> matchSnapshots.addAll(pattern.getMatchHistory().getSnapshots()));
        return matchSnapshots;
    }

    public Optional<ActionRecord> getRandomSnapshot(ActionOptions actionOptions) {
        List<ActionRecord> snapshots = new ArrayList<>();
        patterns.forEach(pattern -> snapshots.addAll(pattern.getMatchHistory().getSimilarSnapshots(actionOptions)));
        if (snapshots.isEmpty()) return Optional.empty();
        return Optional.of(snapshots.get(new Random().nextInt(snapshots.size())));
    }

    public boolean isEmpty() {
        return patterns.isEmpty();
    }

    /**
     * When a Pattern is fixed, it's defined when the fixed region is defined.
     * When it's not fixed, it's defined when at least one of its search regions is defined.
     * The StateImage is defined if at least one Pattern is defined.
     * @return true if defined
     */
    public boolean isDefined() {
        for (Pattern pattern : patterns) {
            if (pattern.isDefined()) return true;
        }
        return false;
    }

    /**
     * Checks if this StateImage has a defined search region.
     * A search region is defined if any pattern has a fixed region or defined search regions.
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
     * If a pattern has a defined, fixed region, it is included. Otherwise, all search regions are included.
     * @return search regions for all patterns.
     */
    public List<Region> getAllSearchRegions() {
        List<Region> regions = new ArrayList<>();
        patterns.forEach(p -> regions.addAll(p.getRegions()));
        return regions;
    }

    @JsonIgnore
    public List<Region> getDefinedFixedRegions() {
        List<Region> definedFixed = new ArrayList<>();
        for (Pattern pattern : patterns) {
            Region fixedRegion = pattern.getSearchRegions().getFixedRegion();
            if (fixedRegion.isDefined()) definedFixed.add(fixedRegion);
        }
        return definedFixed;
    }

    @JsonIgnore
    public Region getLargestDefinedFixedRegionOrNewRegion() {
        return getDefinedFixedRegions().stream()
                .max(Comparator.comparingInt(Region::size))
                .orElse(new Region());
    }

    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder()
                .withImages(this)
                .build();
    }

    public double getAverageWidth() {
        double sum = 0;
        for (Pattern p : patterns) sum += p.w();
        return sum / patterns.size();
    }

    public double getAverageHeight() {
        double sum = 0;
        for (Pattern p : patterns) sum += p.h();
        return sum / patterns.size();
    }

    public int getMaxWidth() {
        if (isEmpty()) return 0;
        int max = patterns.get(0).w();
        for (int i=1; i<patterns.size(); i++) {
            max = Math.max(max, patterns.get(i).w());
        }
        return max;
    }

    public int getMaxHeight() {
        if (isEmpty()) return 0;
        int max = patterns.get(0).h();
        for (int i=1; i<patterns.size(); i++) {
            max = Math.max(max, patterns.get(i).h());
        }
        return max;
    }

    public int getMinSize() {
        if (patterns.isEmpty()) return 0;
        int minSize = patterns.get(0).size();
        for (int i=1; i<patterns.size(); i++) {
            minSize = Math.min(minSize, patterns.get(i).size());
        }
        return minSize;
    }

    public int getMaxSize() {
        if (patterns.isEmpty()) return 0;
        int maxSize = patterns.get(0).size();
        for (int i=1; i<patterns.size(); i++) {
            maxSize = Math.max(maxSize, patterns.get(i).size());
        }
        return maxSize;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("StateImage:");
        stringBuilder.append(" name=").append(name);
        stringBuilder.append(" id=").append(id);
        stringBuilder.append(" ownerState=").append(ownerStateName);
        stringBuilder.append(" searchRegions=");
        getAllSearchRegions().forEach(stringBuilder::append);
        stringBuilder.append(" fixedSearchRegion=").append(getLargestDefinedFixedRegionOrNewRegion());
        stringBuilder.append(" snapshotRegions=");
        getAllMatchSnapshots().forEach(snapshot ->
                snapshot.getMatchList().forEach(stringBuilder::append));
        if (!patterns.isEmpty()) stringBuilder.append(" pattern sizes =");
        patterns.forEach(p -> stringBuilder.append(" ").append(p.size()));
        return stringBuilder.toString();
    }

    public void addPatterns(String... filenames) {
        for (String name : filenames) patterns.add(new Pattern(name));
    }

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

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPatterns(List<Pattern> patterns) {
            this.patterns = patterns;
            if (!patterns.isEmpty()) setNameFromPatternIfEmpty(patterns.get(0));
            return this;
        }

        public Builder addPattern(Pattern pattern) {
            this.patterns.add(pattern);
            setNameFromPatternIfEmpty(pattern);
            return this;
        }

        public Builder addPattern(String filename) {
            Pattern pattern = new Pattern(filename);
            this.patterns.add(pattern);
            setNameFromPatternIfEmpty(pattern);
            return this;
        }

        public Builder addPatterns(String... imageNames) {
            for (String imageName : imageNames) {
                addPattern(imageName);
            }
            return this;
        }

        public Builder setKmeansProfilesAllSchemas(KmeansProfilesAllSchemas kmeansProfilesAllSchemas) {
            this.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
            return this;
        }

        public Builder setIndex(int index) {
            this.index = index;
            return this;
        }

        public Builder setOwnerStateName(String ownerStateName) {
            this.ownerStateName = ownerStateName;
            return this;
        }

        public Builder setPositionForAllPatterns(Position position) {
            this.positionForAllPatterns = position;
            return this;
        }

        public Builder setPositionForAllPatterns(int percentOfWidth, int percentOfHeight) {
            this.positionForAllPatterns = new Position(percentOfWidth, percentOfHeight);
            return this;
        }

        public Builder setOffsetForAllPatterns(Location offset) {
            this.offsetForAllPatterns = offset;
            return this;
        }

        public Builder setOffsetForAllPatterns(int xOffset, int yOffset) {
            this.offsetForAllPatterns = new Location(xOffset, yOffset);
            return this;
        }

        public Builder setSearchRegionForAllPatterns(Region searchRegion) {
            this.searchRegionForAllPatterns = searchRegion;
            return this;
        }

        public Builder setSearchRegionOnObject(SearchRegionOnObject searchRegionOnObject) {
            this.searchRegionOnObject = searchRegionOnObject;
            return this;
        }

        public Builder setFixedForAllPatterns(boolean fixed) {
            this.fixedForAllPatterns = fixed;
            return this;
        }

        /**
         * Sets ActionHistory for all patterns in this StateImage.
         * This is required for mock mode finds to work.
         * 
         * @param actionHistory the ActionHistory to apply to all patterns
         * @return this builder for method chaining
         */
        public Builder withActionHistory(ActionHistory actionHistory) {
            this.actionHistoryForAllPatterns = actionHistory;
            return this;
        }

        /**
         * Sets ActionHistory for all patterns using a supplier function.
         * Useful for lazy initialization or conditional creation.
         * 
         * @param historySupplier supplier that creates the ActionHistory
         * @return this builder for method chaining
         */
        public Builder withActionHistory(java.util.function.Supplier<ActionHistory> historySupplier) {
            this.actionHistoryForAllPatterns = historySupplier.get();
            return this;
        }

        /**
         * Sets ActionHistory for all patterns using a single ActionRecord.
         * Useful for simple test scenarios.
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

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("StateImage\n");
            stringBuilder.append("images\n");
            stringBuilder.append(this.name).append("\n");
            patterns.forEach(pattern -> stringBuilder.append(pattern.getName()).append(" "));
            return stringBuilder.toString();
        }

        private void setNameFromPatternIfEmpty(Pattern pattern) {
            // This condition now safely handles cases where 'name' is null or empty.
            if ((name == null || name.isEmpty()) && pattern != null && pattern.getName() != null) {
                name = pattern.getName();
            }
        }

        public StateImage build() {
            StateImage stateImage = new StateImage();
            stateImage.name = name;
            stateImage.patterns = patterns;
            stateImage.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
            stateImage.index = index;
            stateImage.ownerStateName = ownerStateName;
            stateImage.ownerStateId = ownerStateId;
            stateImage.searchRegionOnObject = searchRegionOnObject;
            if (positionForAllPatterns != null) stateImage.getPatterns().forEach(pattern ->
                    pattern.setTargetPosition(positionForAllPatterns));
            if (offsetForAllPatterns != null) stateImage.getPatterns().forEach(pattern ->
                    pattern.setTargetOffset(offsetForAllPatterns));
            if (searchRegionForAllPatterns != null) stateImage.getPatterns().forEach(pattern ->
                    pattern.addSearchRegion(searchRegionForAllPatterns));
            if (fixedForAllPatterns) stateImage.getPatterns().forEach(pattern ->
                    pattern.setFixed(true));
            if (actionHistoryForAllPatterns != null) stateImage.getPatterns().forEach(pattern ->
                    pattern.setMatchHistory(actionHistoryForAllPatterns));
            return stateImage;
        }

        public StateImage generic() {
            StateImage stateImage = new StateImage();
            stateImage.name = "generic";
            stateImage.ownerStateName = "null";
            stateImage.ownerStateId = null;
            return stateImage;
        }
    }
}
