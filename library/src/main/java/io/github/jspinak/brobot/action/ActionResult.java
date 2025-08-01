package io.github.jspinak.brobot.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycle;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.internal.options.ActionOptionsAdapter;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Movement;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.tools.logging.MessageFormatter;
import lombok.Data;
import org.bytedeco.opencv.opencv_core.Mat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive results container for all action executions in the Brobot framework.
 * 
 * <p>ActionResult serves as the universal return type for all actions, encapsulating not just 
 * pattern matching results but all information generated during action execution. This 
 * unified approach simplifies the API and provides consistent access to action outcomes 
 * regardless of the action type.</p>
 * 
 * <p>Key information contained:
 * <ul>
 *   <li><b>Match List</b>: Collection of successful pattern matches found during execution</li>
 *   <li><b>Success Status</b>: Boolean indicating whether the action achieved its goal</li>
 *   <li><b>Timing Data</b>: Start time, end time, and duration for performance analysis</li>
 *   <li><b>Text Results</b>: Any text extracted or read during the action</li>
 *   <li><b>Active States</b>: States identified as active during execution</li>
 *   <li><b>Defined Regions</b>: Regions created or modified by the action</li>
 *   <li><b>Analysis Data</b>: Advanced results like color analysis, motion detection, etc.</li>
 * </ul>
 * </p>
 * 
 * <p>Design benefits:
 * <ul>
 *   <li>Single return type for all actions simplifies the API</li>
 *   <li>Rich metadata supports debugging and performance optimization</li>
 *   <li>Chainable results enable complex composite actions</li>
 *   <li>Comprehensive logging data for illustrated histories</li>
 * </ul>
 * </p>
 * 
 * <p>The ActionResult object is central to Brobot's approach of making automation testable 
 * and debuggable by providing complete visibility into what happened during execution.</p>
 * 
 * @since 1.0
 * @see Match
 * @see Action
 * @see ActionOptions
 * @see ActionLifecycle
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionResult {
    /**
     * Human-readable description of the action performed.
     * Used for logging, debugging, and report generation.
     */
    private String actionDescription = "";
    
    /**
     * List of all matches found during action execution.
     * Contains successful pattern matches with their locations, scores, and metadata.
     */
    private List<Match> matchList = new ArrayList<>();
    
    /**
     * Original matches before any filtering or processing.
     * Preserved for comparison and debugging purposes.
     */
    private List<Match> initialMatchList = new ArrayList<>();

    /**
     * Configuration used for this action execution.
     * Contains all parameters that controlled the action behavior.
     * JsonIgnore due to potential non-serializable fields like function references.
     */
    @JsonIgnore
    private ActionConfig actionConfig;

    /**
     * Legacy ActionOptions for backward compatibility during migration.
     * This field supports the gradual migration from ActionOptions to ActionConfig.
     * @deprecated Use actionConfig instead. This field will be removed in a future version.
     */
    @JsonIgnore
    @Deprecated
    private ActionOptions actionOptions;

    /**
     * Names of states identified as active during action execution.
     * Populated from StateObjectData of successful matches.
     */
    private Set<String> activeStates = new HashSet<>();
    
    /**
     * Accumulated text content from all matches.
     * Contains OCR results and text extracted during the action.
     */
    private Text text = new Text();
    
    /**
     * Specific text selected or highlighted during the action.
     * Used for text-based operations like copy or verification.
     */
    private String selectedText = "";
    
    /**
     * Total time taken for action execution.
     * Calculated from start to end of the action lifecycle.
     */
    private Duration duration = Duration.ZERO;
    
    /**
     * Timestamp when action execution began.
     * Used for duration calculation and event logging.
     */
    private LocalDateTime startTime = LocalDateTime.now();
    
    /**
     * Timestamp when action execution completed.
     * Null until action finishes.
     */
    private LocalDateTime endTime;
    
    /**
     * Indicates whether the action achieved its intended goal.
     * Set based on success criteria defined in ActionOptions.
     */
    private boolean success = false;
    
    /**
     * Regions created or captured by DEFINE actions.
     * Used to pass region definitions between actions.
     */
    private List<Region> definedRegions = new ArrayList<>();
    
    /**
     * Maximum number of matches to return.
     * -1 indicates no limit.
     */
    private int maxMatches = -1;

    /**
     * Collection of scene analysis results from color and motion detection.
     * Contains histogram data, color statistics, and pixel analysis.
     * JsonIgnore due to complex OpenCV objects.
     */
    @JsonIgnore
    private SceneAnalyses sceneAnalysisCollection = new SceneAnalyses();

    /**
     * Binary mask indicating regions of interest or motion.
     * Used by FIXED_PIXELS, DYNAMIC_PIXELS, and motion detection.
     * JsonIgnore because OpenCV Mat objects aren't JSON-serializable.
     */
    @JsonIgnore
    private Mat mask;

    /**
     * Formatted text output for reporting and logging.
     * Contains human-readable summary of action results.
     */
    private String outputText = "";

    /**
     * Lifecycle tracking data for this action execution.
     * Contains timing, repetition counts, and execution state.
     * JsonIgnore to avoid circular references in serialization.
     */
    @JsonIgnore
    private ActionLifecycle actionLifecycle;
    
    /**
     * List of movements performed during action execution.
     * Used by drag operations and other movement-based actions.
     */
    private List<Movement> movements = new ArrayList<>();
    
    /**
     * Ordered history of action execution steps.
     * Contains ActionRecord for each step in a chained action sequence.
     * Empty for single-step actions.
     */
    private List<ActionRecord> executionHistory = new ArrayList<>();

    /**
     * Creates an empty ActionResult with default values.
     * Used when action execution hasn't started or for error cases.
     */
    public ActionResult() {}

    /**
     * Creates an ActionResult configured with specific action configuration.
     * 
     * @param actionConfig Configuration that will control the action execution
     */
    public ActionResult(ActionConfig actionConfig) {
        this.actionConfig = actionConfig;
    }

    /**
     * Creates an ActionResult configured with legacy ActionOptions.
     * @deprecated Use ActionResult(ActionConfig) instead
     * 
     * @param actionOptions Legacy configuration object
     */
    @Deprecated
    public ActionResult(ActionOptions actionOptions) {
        // This constructor is kept for backward compatibility
        // In a full migration, this would convert ActionOptions to ActionConfig
    }

    /**
     * Adds one or more matches to the result set.
     * <p>
     * Also extracts and records any state information from the matches.
     * This is the primary method for populating results during action execution.
     *
     * @param matches Variable number of Match objects to add
     */
    public void add(Match... matches) {
        for (Match m : matches) {
            matchList.add(m);
            addActiveState(m);
        }
    }

    /**
     * Adds scene analysis data from color or motion detection.
     *
     * @param sceneAnalysis Analysis results to incorporate
     */
    public void addSceneAnalysis(SceneAnalysis sceneAnalysis) {
        sceneAnalysisCollection.add(sceneAnalysis);
    }

    /**
     * Merges match objects from another ActionResult.
     * <p>
     * Copies only the matches, preserving state associations.
     * Useful for combining results from multiple search operations.
     *
     * @param matches Source ActionResult containing matches to add
     */
    public void addMatchObjects(ActionResult matches) {
        for (Match match : matches.getMatchList()) {
            add(match);
        }
    }

    /**
     * Merges all data from another ActionResult.
     * <p>
     * Combines matches, text, states, timing, and analysis data.
     * Used by composite actions to aggregate results from sub-actions.
     *
     * @param matches Source ActionResult to merge completely
     */
    public void addAllResults(ActionResult matches) {
        addMatchObjects(matches);
        addNonMatchResults(matches);
    }

    /**
     * Merges non-match data from another ActionResult.
     * <p>
     * Includes text, active states, duration, scene analysis, and masks.
     * Preserves match lists unchanged while combining metadata.
     *
     * @param matches Source ActionResult containing data to merge
     */
    public void addNonMatchResults(ActionResult matches) {
        text.addAll(matches.text);
        activeStates.addAll(matches.activeStates);
        duration = duration.plus(matches.duration);
        sceneAnalysisCollection.merge(matches.sceneAnalysisCollection);
        mask = matches.mask;
    }

    /**
     * Adds a text string to the accumulated text results.
     * <p>
     * Used to collect text from OCR operations or user input.
     *
     * @param str Text to add to the results
     */
    public void addString(String str) {
        text.add(str);
    }

    /**
     * Adds a region to the defined regions collection.
     * <p>
     * Used by DEFINE actions to store captured regions for later use.
     *
     * @param region The region to add
     */
    public void addDefinedRegion(Region region) {
        definedRegions.add(region);
    }
    
    /**
     * Adds a movement to the result.
     * Used by drag and other movement-based actions.
     * 
     * @param movement The movement to add
     */
    public void addMovement(Movement movement) {
        movements.add(movement);
    }
    
    /**
     * Returns an Optional containing the first movement from the action.
     * <p>
     * This is a convenience method for simple, single-segment actions like a standard
     * DRAG. It provides easy access to the result without needing to handle the list.
     *
     * @return An Optional containing the first Movement if one exists, otherwise an empty Optional
     */
    @JsonIgnore
    public Optional<Movement> getMovement() {
        if (movements == null || movements.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(movements.get(0));
    }
    
    /**
     * Adds an action record to the execution history.
     * Used by chained actions to track intermediate results.
     * 
     * @param record The action record to add
     */
    public void addExecutionRecord(ActionRecord record) {
        executionHistory.add(record);
    }

    /**
     * Sorts matches by similarity score in ascending order.
     * <p>
     * Places lowest scoring matches first. Useful for filtering
     * or removing poor quality matches.
     */
    public void sortMatchObjects() {
        matchList.sort(Comparator.comparingDouble(Match::getScore));
    }

    /**
     * Sorts matches by similarity score in descending order.
     * <p>
     * Places best matches first. Standard sorting for most use cases
     * where high-quality matches are prioritized.
     */
    public void sortMatchObjectsDescending() {
        matchList.sort(Comparator.comparingDouble(Match::getScore).reversed());
    }

    /**
     * Extracts regions from all matches.
     * <p>
     * Converts the match list to a simple list of regions,
     * useful for region-based operations that don't need full match data.
     * 
     * @return List of regions corresponding to match locations
     */
    public List<Region> getMatchRegions() {
        List<Region> regions = new ArrayList<>();
        matchList.forEach(mO -> regions.add(mO.getRegion()));
        return regions;
    }

    /**
     * Extracts target locations from all matches.
     * <p>
     * Returns the click/action points for all matches,
     * typically the center or a positioned offset within each match.
     * 
     * @return List of target locations for all matches
     */
    public List<Location> getMatchLocations() {
        List<Location> locations = new ArrayList<>();
        matchList.forEach(mO -> locations.add(mO.getTarget()));
        return locations;
    }

    /**
     * Gets the target location of the best scoring match.
     *
     * @return Optional containing the location, or empty if no matches
     */
    public Optional<Location> getBestLocation() {
        if (getBestMatch().isEmpty()) return Optional.empty();
        return Optional.of(getBestMatch().get().getTarget());
    }

    /**
     * Finds the match with the highest similarity score.
     * <p>
     * Useful for Find.BEST operations or when only the most
     * confident match is needed.
     *
     * @return Optional containing the best match, or empty if no matches
     */
    public Optional<Match> getBestMatch() {
        return matchList.stream()
                .max(Comparator.comparingDouble(Match::getScore));
    }

    /**
     * Checks if the best match score is below a threshold.
     * <p>
     * Used to validate match quality or determine if better
     * matches should be sought.
     *
     * @param similarity Threshold to compare against (0.0-1.0)
     * @return true if best score is less than threshold or no matches exist
     */
    public boolean bestMatchSimilarityLessThan(double similarity) {
        Optional<Match> matchOpt = getBestMatch();
        double score = 0.0;
        if (matchOpt.isPresent()) score = matchOpt.get().getScore();
        return score < similarity;
    }

    /**
     * Extracts and records state information from a match.
     * <p>
     * Called internally when matches are added to maintain
     * the set of active states discovered during execution.
     *
     * @param newMatch Match potentially containing state data
     */
    private void addActiveState(Match newMatch) {
        if (newMatch.getStateObjectData() != null)
            activeStates.add(newMatch.getStateObjectData().getOwnerStateName());
    }

    /**
     * Gets the primary region defined by this action.
     * <p>
     * Returns the first defined region, typically from a DEFINE action.
     * Returns an empty region if no regions were defined.
     *
     * @return The first defined region or an empty region
     */
    public Region getDefinedRegion() {
        if (definedRegions.isEmpty()) return new Region();
        return definedRegions.getFirst();
    }

    /**
     * Gets the number of matches found.
     *
     * @return Count of matches in the result
     */
    public int size() {
        return matchList.size();
    }

    /**
     * Checks if the action found any matches.
     *
     * @return true if no matches were found
     */
    public boolean isEmpty() {
        return matchList.isEmpty();
    }

    /**
     * Updates the action count for all matches.
     * <p>
     * Used to track how many times each match has been acted upon
     * across multiple action iterations.
     *
     * @param timesActedOn The count to set for all matches
     */
    public void setTimesActedOn(int timesActedOn) {
        matchList.forEach(m -> m.setTimesActedOn(timesActedOn));
    }

    /**
     * Sorts matches by score in ascending order.
     * <p>
     * Note: Despite the method name suggesting descending order,
     * this actually sorts ascending (lowest scores first).
     * 
     * @deprecated Use {@link #sortMatchObjects()} for ascending or
     *             {@link #sortMatchObjectsDescending()} for descending
     */
    @Deprecated
    public void sortByMatchScoreDecending() {
        matchList.sort(Comparator.comparingDouble(Match::getScore));
    }

    /**
     * Sorts matches by area in descending order.
     * <p>
     * Places larger matches first. Useful when larger UI elements
     * are more likely to be the intended targets.
     */
    public void sortBySizeDecending() {
        matchList.sort(Comparator.comparing(Match::size).reversed());
    }

    /**
     * Converts this result into an ObjectCollection.
     * <p>
     * Enables using action results as input for subsequent actions,
     * supporting action chaining and composite operations.
     *
     * @return New ObjectCollection containing these results
     */
    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder()
                .withMatches(this)
                .build();
    }

    /**
     * Prints all matches to standard output.
     * <p>
     * Debugging utility that outputs match details to console.
     */
    public void print() {
        matchList.forEach(System.out::println);
    }

    /**
     * Calculates the median region from all matches.
     * <p>
     * Creates a new region positioned at the average x,y coordinates
     * with average width and height of all matches. Useful for finding
     * the central tendency when multiple similar objects are detected.
     * 
     * @return Optional containing the median region, or empty if no matches
     */
    public Optional<Region> getMedian() {
        if (matchList.isEmpty()) return Optional.empty();
        int cumX = 0, cumY = 0, cumW = 0, cumH = 0;
        for (Match m : matchList) {
            cumX += m.x();
            cumY += m.y();
            cumW += m.w();
            cumH += m.h();
        }
        int size = matchList.size();
        return Optional.of(new Region(cumX/size, cumY/size, cumW/size, cumH/size));
    }

    /**
     * Gets the center point of the median region.
     * <p>
     * Calculates the median of all match regions and returns
     * its center point. Useful for clicking in the middle of
     * a group of similar objects.
     *
     * @return Optional containing the median center location, or empty if no matches
     */
    public Optional<Location> getMedianLocation() {
        Optional<Region> regOpt = getMedian();
        return regOpt.map(region -> new Location(region, Positions.Name.MIDDLEMIDDLE));
    }

    /**
     * Finds the match closest to a specified location.
     * <p>
     * Uses Euclidean distance to find the match whose center
     * is nearest to the target location. Useful for selecting
     * among multiple matches based on proximity.
     *
     * @param location Target location to measure distance from
     * @return Optional containing the closest match, or empty if no matches
     */
    public Optional<Match> getClosestTo(Location location) {
        if (matchList.isEmpty()) return Optional.empty();
        double closest = getDist(matchList.get(0), location);
        Match closestMO = matchList.get(0);
        for (Match mO : matchList) {
            double dist = getDist(mO, location);
            if (dist <= closest) {
                closest = dist;
                closestMO = mO;
            }
        }
        return Optional.of(closestMO);
    }

    /**
     * Calculates squared Euclidean distance between match and location.
     * <p>
     * Returns squared distance to avoid sqrt calculation when only
     * relative distances matter.
     *
     * @param match Match to measure from
     * @param location Target location
     * @return Squared distance between match center and location
     */
    private double getDist(Match match, Location location) {
        int xDist = match.x() - location.getCalculatedX();
        int yDist = match.y() - location.getCalculatedY();
        return Math.pow(xDist, 2) + Math.pow(yDist, 2);
    }

    /**
     * Performs set subtraction on match collections.
     * <p>
     * Returns matches that exist in this result but not in the parameter.
     * Useful for finding unique matches or filtering out duplicates.
     *
     * @param matches The ActionResult containing matches to exclude
     * @return New ActionResult with non-overlapping matches
     */
    public ActionResult minus(ActionResult matches) {
        ActionResult rest = new ActionResult();
        matchList.forEach(match -> {
            if (!matches.containsMatch(match)) rest.add(match);
        });
        return rest;
    }

    /**
     * Checks if this result contains a specific match.
     * <p>
     * Uses Match.equals() for comparison, which typically
     * compares location and image identity.
     *
     * @param match The match to search for
     * @return true if an equal match exists in this result
     */
    public boolean containsMatch(Match match) {
        for (Match m : matchList) {
            if (match.equals(m)) return true;
        }
        return false;
    }

    /**
     * Filters matches that contain other matches within their bounds.
     * <p>
     * Returns only matches from this result that contain at least one
     * match from the parameter collection. Used for hierarchical matching
     * where outer elements must contain specific inner elements.
     *
     * @param insideMatches Matches that must be found inside returned matches
     * @return New ActionResult with only confirmed container matches
     */
    public ActionResult getConfirmedMatches(ActionResult insideMatches) {
        ActionResult matches = new ActionResult();
        matchList.forEach(m -> {
            if (insideMatches.getMatchList().contains(m)) matches.add(m);
        });
        return matches;
    }

    /**
     * Removes matches not confirmed by the inside matches.
     * <p>
     * Modifies this result to keep only matches that exist
     * in the confirmation set.
     *
     * @param insideMatches Matches that confirm which to keep
     */
    public void removeNonConfirmedMatches(ActionResult insideMatches) {
        matchList.removeIf(m -> !insideMatches.getMatchList().contains(m));
    }

    /**
     * Retains only matches confirmed by the inside matches.
     * <p>
     * Alternative to removeNonConfirmedMatches that replaces
     * the entire match list with confirmed matches only.
     *
     * @param insideMatches Matches used for confirmation
     */
    public void keepOnlyConfirmedMatches(ActionResult insideMatches) {
        ActionResult confirmedMatches = getConfirmedMatches(insideMatches);
        matchList = confirmedMatches.getMatchList();
    }

    /**
     * Adds all matches from a list to this result.
     * <p>
     * Bulk addition without processing state information.
     * Use add(Match...) to properly extract state data.
     *
     * @param newMatches List of matches to add
     */
    public void addAll(List<Match> newMatches) {
        matchList.addAll(newMatches);
    }

    /**
     * Gets unique state object IDs from all matches.
     * <p>
     * Extracts the set of distinct state objects that produced matches.
     * Useful for determining which UI elements were successfully found.
     *
     * @return Set of unique state object identifiers
     */
    public Set<String> getUniqueImageIds() {
        return matchList.stream()
                .map(match -> match.getStateObjectData().getStateObjectId())
                .collect(Collectors.toSet());
    }

    /**
     * Filters matches by state object ID.
     * <p>
     * Returns all matches that originated from a specific state object.
     * Useful for processing results from multi-image searches.
     *
     * @param id The state object ID to filter by
     * @return List of matches from the specified state object
     */
    public List<Match> getMatchObjectsWithTargetStateObject(String id) {
        return matchList.stream()
                .filter(match -> Objects.equals(match.getStateObjectData().getStateObjectId(), id))
                .collect(Collectors.toList());
    }

    /**
     * Converts matches back to StateImage objects.
     * <p>
     * Reconstructs StateImage instances from match data,
     * useful for using found elements in subsequent searches.
     *
     * @return List of StateImages created from matches
     */
    public List<StateImage> getMatchListAsStateImages() {
        List<StateImage> stateImages = new ArrayList<>();
        matchList.forEach(match -> stateImages.add(match.toStateImage()));
        return stateImages;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ActionResult: ").append("size=").append(size()).append(" ");
        for (Match match : matchList) {
            stringBuilder.append(match).append(" ");
        }
        return stringBuilder.toString();
    }

    public Set<String> getOwnerStateNames() {
        Set<String> uniqueOwnerStateNames = new HashSet<>();
        matchList.forEach(match -> uniqueOwnerStateNames.add(match.getOwnerStateName()));
        return uniqueOwnerStateNames;
    }

    public List<Match> getMatchObjectsWithOwnerState(String ownerStateName) {
        return matchList.stream()
                .filter(match -> ownerStateName.equals(match.getOwnerStateName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets a visual symbol representing action success or failure.
     * <p>
     * Returns a checkmark for successful actions or an X for failures.
     * Used in reports and console output for quick visual scanning.
     *
     * @return Unicode symbol indicating success (✓) or failure (✗)
     */
    public String getSuccessSymbol() {
        if (success) return MessageFormatter.check;
        return MessageFormatter.fail;
    }

    /**
     * Formats matches as a temporary state structure visualization.
     * <p>
     * When using automation to discover state structures, matches represent
     * potential state images. This method groups matches by owner state and
     * formats them for analysis of state relationships and transitions.
     *
     * @return String representation showing states and their associated matches
     */
    public String toStringAsTempStates() {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> uniqueStates = getOwnerStateNames();
        stringBuilder.append("State Structure: #states=").append(uniqueStates.size()).append("\n");
        uniqueStates.forEach(ownerStateName -> {
            List<Match> stateMatchList = getMatchObjectsWithOwnerState(ownerStateName);
            stringBuilder.append(ownerStateName).append(": size=").append(stateMatchList.size()).append(" ");
            stateMatchList.forEach(stringBuilder::append);
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

    /*
    The following methods deal with logging.
     */

    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        if (actionConfig != null) {
            summary.append("Action: ").append(actionConfig.getClass().getSimpleName()).append("\n");
        }
        summary.append("Success: ").append(success).append("\n");
        summary.append("Number of matches: ").append(matchList.size()).append("\n");
        summary.append("Active states: ").append(String.join(", ", activeStates)).append("\n");
        if (!text.isEmpty()) {
            summary.append("Extracted text: ").append(text).append("\n");
        }
        return summary.toString();
    }

    public List<StateImageData> getStateImageData() {
        return matchList.stream()
                .map(StateImageData::fromMatch)
                .collect(Collectors.toList());
    }

    public static class StateImageData {
        public final String name;
        public final String stateObjectName;
        public final boolean found;
        public final int x;
        public final int y;
        public final int expectedX;
        public final int expectedY;

        private StateImageData(String name, String stateObjectName, boolean found, int x, int y, int expectedX, int expectedY) {
            this.name = name;
            this.stateObjectName = stateObjectName;
            this.found = found;
            this.x = x;
            this.y = y;
            this.expectedX = expectedX;
            this.expectedY = expectedY;
        }

        public static StateImageData fromMatch(Match match) {
            return new StateImageData(
                    match.getName(),
                    match.getStateObjectData() != null ? match.getStateObjectData().getStateObjectName() : "",
                    true,
                    match.x(),
                    match.y(),
                    -1, // or calculate expected X if available
                    -1  // or calculate expected Y if available
            );
        }
    }

    /**
     * Gets the ActionConfig for this result.
     * This is the preferred method for accessing action configuration.
     * 
     * @return the ActionConfig used for this action execution
     */
    public ActionConfig getActionConfig() {
        return actionConfig;
    }

    /**
     * Sets the ActionConfig for this result.
     * Also updates the legacy actionOptions field for backward compatibility.
     * 
     * @param actionConfig the ActionConfig to set
     */
    public void setActionConfig(ActionConfig actionConfig) {
        this.actionConfig = actionConfig;
        // Clear actionOptions since we can't convert back from ActionConfig
        // This encourages migration to the new API
        this.actionOptions = null;
    }

    /**
     * Gets the ActionOptions for this result.
     * This method is provided for backward compatibility during the migration
     * from ActionOptions to ActionConfig.
     * 
     * @deprecated Use {@link #getActionConfig()} instead. This method will be 
     *             removed in a future version.
     * @return the ActionOptions if available, otherwise a new default instance
     */
    @Deprecated
    public ActionOptions getActionOptions() {
        // Return the cached actionOptions if available
        if (actionOptions != null) {
            return actionOptions;
        }
        // Create a default ActionOptions to prevent NPEs in legacy code
        // This ensures backward compatibility even when using new ActionConfig
        ActionOptions defaultOptions = new ActionOptions();
        // Try to set some basic properties from ActionConfig if available
        if (actionConfig != null) {
            // Set the action description if available
            if (this.actionDescription != null && !this.actionDescription.isEmpty()) {
                // The description might help legacy code understand what happened
            }
        }
        return defaultOptions;
    }

    /**
     * Sets the ActionOptions for this result.
     * This method is provided for backward compatibility during the migration.
     * Also converts and sets the ActionConfig using the adapter.
     * 
     * @deprecated Use {@link #setActionConfig(ActionConfig)} instead. This method 
     *             will be removed in a future version.
     * @param actionOptions the ActionOptions to set
     */
    @Deprecated
    public void setActionOptions(ActionOptions actionOptions) {
        this.actionOptions = actionOptions;
        // Convert to ActionConfig for the new API
        if (actionOptions != null) {
            ActionOptionsAdapter adapter = new ActionOptionsAdapter();
            this.actionConfig = adapter.convert(actionOptions);
        } else {
            this.actionConfig = null;
        }
    }
    
    // ===============================
    // Enhanced Logging Data Structures
    // ===============================
    
    /**
     * Enhanced execution context for modular logging architecture.
     * This field will be populated by ActionLifecycleAspect and used by formatters.
     */
    @JsonIgnore
    private ActionExecutionContext executionContext;
    
    /**
     * Action metrics for logging and performance analysis.
     */
    @JsonIgnore
    private ActionMetrics actionMetrics;
    
    /**
     * Environment snapshot captured during action execution.
     */
    @JsonIgnore
    private static volatile EnvironmentSnapshot environmentSnapshot;
    
    /**
     * Context information about the action execution, optimized for logging.
     * Contains all information needed to generate comprehensive log messages.
     */
    @Data
    public static class ActionExecutionContext {
        /** Type of action being performed (FIND, CLICK, TYPE, etc.) */
        private String actionType;
        
        /** StateImages that were targeted by this action */
        private List<StateImage> targetImages = new ArrayList<>();
        
        /** StateStrings that were targeted by this action */
        private List<String> targetStrings = new ArrayList<>();
        
        /** StateRegions that were targeted by this action */
        private List<Region> targetRegions = new ArrayList<>();
        
        /** Primary target name in "State.Object" format for logging */
        private String primaryTargetName;
        
        /** Whether the action completed successfully */
        private boolean success;
        
        /** Duration of action execution */
        private Duration executionDuration = Duration.ZERO;
        
        /** When the action started */
        private Instant startTime;
        
        /** When the action completed */
        private Instant endTime;
        
        /** Matches found by the action */
        private List<Match> resultMatches = new ArrayList<>();
        
        /** Exception that occurred during execution, if any */
        private Throwable executionError;
        
        /** Thread that executed the action */
        private String executingThread;
        
        /** Unique identifier for this action execution */
        private String actionId;
    }
    
    /**
     * Metrics and performance data for the action execution.
     */
    @Data
    public static class ActionMetrics {
        /** Total execution time in milliseconds */
        private long executionTimeMs;
        
        /** Number of matches found */
        private int matchCount;
        
        /** Confidence score of the best match (if applicable) */
        private double bestMatchConfidence = 0.0;
        
        /** Name of thread that executed the action */
        private String threadName;
        
        /** Unique identifier for this action execution */
        private String actionId;
        
        /** Number of retries attempted */
        private int retryCount = 0;
        
        /** Total time spent in retries */
        private long retryTimeMs = 0;
    }
    
    /**
     * Environment information captured once and shared across all actions.
     * This avoids repeatedly collecting the same environmental data.
     */
    @Data
    public static class EnvironmentSnapshot {
        /** Information about available monitors */
        private List<MonitorInfo> monitors = new ArrayList<>();
        
        /** Operating system name */
        private String osName;
        
        /** Java version */
        private String javaVersion;
        
        /** Whether running in headless mode */
        private boolean headlessMode;
        
        /** When this snapshot was created */
        private Instant captureTime;
        
        /** Get or create the singleton environment snapshot */
        public static EnvironmentSnapshot getInstance() {
            if (environmentSnapshot == null) {
                synchronized (EnvironmentSnapshot.class) {
                    if (environmentSnapshot == null) {
                        environmentSnapshot = captureEnvironment();
                    }
                }
            }
            return environmentSnapshot;
        }
        
        /** Capture current environment information */
        private static EnvironmentSnapshot captureEnvironment() {
            EnvironmentSnapshot snapshot = new EnvironmentSnapshot();
            snapshot.setOsName(System.getProperty("os.name", "unknown"));
            snapshot.setJavaVersion(System.getProperty("java.version", "unknown"));
            snapshot.setHeadlessMode("true".equals(System.getProperty("java.awt.headless")));
            snapshot.setCaptureTime(Instant.now());
            
            // Monitor information would be collected here
            // This is a placeholder - actual implementation would use AWT/Swing
            snapshot.setMonitors(new ArrayList<>());
            
            return snapshot;
        }
    }
    
    /**
     * Information about a display monitor.
     */
    @Data
    public static class MonitorInfo {
        private int monitorId;
        private int width;
        private int height;
        private int x;
        private int y;
        private boolean primary;
    }
    
    // Getters and setters for new fields
    public ActionExecutionContext getExecutionContext() {
        return executionContext;
    }
    
    public void setExecutionContext(ActionExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
    
    public ActionMetrics getActionMetrics() {
        return actionMetrics;
    }
    
    public void setActionMetrics(ActionMetrics actionMetrics) {
        this.actionMetrics = actionMetrics;
    }
    
    public EnvironmentSnapshot getEnvironmentSnapshot() {
        return EnvironmentSnapshot.getInstance();
    }
    
    /**
     * Convenience method to get primary target name for logging.
     * Builds the target name from various sources in priority order.
     */
    public String getLogTargetName() {
        if (executionContext != null && executionContext.getPrimaryTargetName() != null) {
            return executionContext.getPrimaryTargetName();
        }
        
        // Fallback: try to build from existing match data
        if (!getMatchList().isEmpty()) {
            Match firstMatch = getMatchList().get(0);
            if (firstMatch.getStateObjectData() != null) {
                String stateName = firstMatch.getStateObjectData().getOwnerStateName();
                String objectName = firstMatch.getStateObjectData().getStateObjectName();
                if (stateName != null && objectName != null) {
                    return stateName + "." + objectName;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Convenience method to get execution time in milliseconds.
     */
    public long getExecutionTimeMs() {
        if (actionMetrics != null) {
            return actionMetrics.getExecutionTimeMs();
        }
        // Fallback to existing duration field
        return duration != null ? duration.toMillis() : 0;
    }
    
    /**
     * Convenience method to check if this action should be logged.
     * An action is loggable if it has been completed (has end time).
     */
    public boolean isLoggable() {
        return executionContext != null && 
               executionContext.getEndTime() != null;
    }
}