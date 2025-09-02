package io.github.jspinak.brobot.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.action.result.*;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycle;
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
 * <p>Version 2.0 introduces a component-based architecture where responsibilities are
 * delegated to specialized classes while maintaining backward compatibility.</p>
 * 
 * @since 1.0
 * @version 2.0
 * @see Match
 * @see Action
 * @see ActionConfig
 * @see ActionLifecycle
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionResult {
    /**
     * Human-readable description of the action performed.
     */
    private String actionDescription = "";
    
    /**
     * Indicates whether the action achieved its intended goal.
     */
    private boolean success = false;
    
    /**
     * Configuration used for this action execution.
     */
    @JsonIgnore
    private ActionConfig actionConfig;
    
    /**
     * Lifecycle tracking data for this action execution.
     */
    @JsonIgnore
    private ActionLifecycle actionLifecycle;
    
    /**
     * Formatted text output for reporting and logging.
     */
    private String outputText = "";
    
    // Component-based architecture (Version 2.0)
    private final MatchCollection matchCollection = new MatchCollection();
    private final TimingData timingData = new TimingData();
    private final TextExtractionResult textResult = new TextExtractionResult();
    private final StateTracker stateTracker = new StateTracker();
    private final RegionManager regionManager = new RegionManager();
    private final MovementTracker movementTracker = new MovementTracker();
    private final ActionAnalysis actionAnalysis = new ActionAnalysis();
    private final ExecutionHistory executionHistory = new ExecutionHistory();
    
    // Fields maintained for test compatibility
    @JsonIgnore
    private ActionMetrics actionMetrics;
    @JsonIgnore
    private ActionExecutionContext executionContext;
    @JsonIgnore
    private static volatile EnvironmentSnapshot environmentSnapshot;
    
    /**
     * Creates an empty ActionResult with default values.
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
    
    // =====================================================
    // Match Management Methods (delegated to MatchCollection)
    // =====================================================
    
    /**
     * Adds one or more matches to the result set.
     * Also extracts and records any state information from the matches.
     *
     * @param matches Variable number of Match objects to add
     */
    public void add(Match... matches) {
        for (Match m : matches) {
            matchCollection.add(m);
            stateTracker.processMatch(m);
        }
    }
    
    /**
     * Gets the list of all matches found during action execution.
     * 
     * @return List of matches
     */
    public List<Match> getMatchList() {
        return matchCollection.getMatches();
    }
    
    /**
     * Sets the match list directly.
     * 
     * @param matches List of matches to set
     */
    public void setMatchList(List<Match> matches) {
        matchCollection.clear();
        if (matches != null) {
            for (Match m : matches) {
                add(m);
            }
        }
    }
    
    /**
     * Gets the initial matches before any filtering or processing.
     * 
     * @return List of initial matches
     */
    public List<Match> getInitialMatchList() {
        return matchCollection.getInitialMatches();
    }
    
    /**
     * Sets the initial match list.
     * 
     * @param matches List of initial matches
     */
    public void setInitialMatchList(List<Match> matches) {
        if (matches != null) {
            matchCollection.setInitialMatches(new ArrayList<>(matches));
        }
    }
    
    /**
     * Gets the maximum number of matches to return.
     * 
     * @return Maximum matches limit (-1 for unlimited)
     */
    public int getMaxMatches() {
        return matchCollection.getMaxMatches();
    }
    
    /**
     * Sets the maximum number of matches to return.
     * 
     * @param maxMatches Maximum matches limit (-1 for unlimited)
     */
    public void setMaxMatches(int maxMatches) {
        matchCollection.setMaxMatches(maxMatches);
    }
    
    /**
     * Sorts matches by similarity score in ascending order.
     */
    public void sortMatchObjects() {
        matchCollection.sortByScore();
    }
    
    /**
     * Sorts matches by similarity score in descending order.
     */
    public void sortMatchObjectsDescending() {
        matchCollection.sortByScoreDescending();
    }
    
    /**
     * @deprecated Use {@link #sortMatchObjects()} for ascending or
     *             {@link #sortMatchObjectsDescending()} for descending
     */
    @Deprecated
    public void sortByMatchScoreDecending() {
        sortMatchObjects();
    }
    
    /**
     * Sorts the match list by match score in descending order.
     */
    public void sortByMatchScoreDescending() {
        sortMatchObjectsDescending();
    }
    
    /**
     * @deprecated Use {@link #sortBySizeDescending()} instead (typo fix)
     */
    @Deprecated  
    public void sortBySizeDecending() {
        sortBySizeDescending();
    }
    
    /**
     * Sorts matches by area in descending order.
     */
    public void sortBySizeDescending() {
        matchCollection.sortBySizeDescending();
    }
    
    /**
     * Extracts regions from all matches.
     * 
     * @return List of regions corresponding to match locations
     */
    public List<Region> getMatchRegions() {
        return matchCollection.getRegions();
    }
    
    /**
     * Extracts target locations from all matches.
     * 
     * @return List of target locations for all matches
     */
    public List<Location> getMatchLocations() {
        return matchCollection.getLocations();
    }
    
    /**
     * Gets the target location of the best scoring match.
     *
     * @return Optional containing the location, or empty if no matches
     */
    public Optional<Location> getBestLocation() {
        return matchCollection.getBest()
                .map(Match::getTarget);
    }
    
    /**
     * Finds the match with the highest similarity score.
     *
     * @return Optional containing the best match, or empty if no matches
     */
    public Optional<Match> getBestMatch() {
        return matchCollection.getBest();
    }
    
    /**
     * Checks if the best match score is below a threshold.
     *
     * @param similarity Threshold to compare against (0.0-1.0)
     * @return true if best score is less than threshold or no matches exist
     */
    public boolean bestMatchSimilarityLessThan(double similarity) {
        Optional<Match> matchOpt = getBestMatch();
        double score = matchOpt.map(Match::getScore).orElse(0.0);
        return score < similarity;
    }
    
    /**
     * Gets the number of matches found.
     *
     * @return Count of matches in the result
     */
    public int size() {
        return matchCollection.size();
    }
    
    /**
     * Checks if the action found any matches.
     *
     * @return true if no matches were found
     */
    public boolean isEmpty() {
        return matchCollection.isEmpty();
    }
    
    /**
     * Updates the action count for all matches.
     *
     * @param timesActedOn The count to set for all matches
     */
    public void setTimesActedOn(int timesActedOn) {
        matchCollection.setTimesActedOn(timesActedOn);
    }
    
    /**
     * Calculates the median region from all matches.
     * 
     * @return Optional containing the median region, or empty if no matches
     */
    public Optional<Region> getMedian() {
        return matchCollection.getStatistics().getMedianRegion();
    }
    
    /**
     * Gets the center point of the median region.
     *
     * @return Optional containing the median center location, or empty if no matches
     */
    public Optional<Location> getMedianLocation() {
        return matchCollection.getStatistics().getMedianLocation();
    }
    
    /**
     * Finds the match closest to a specified location.
     *
     * @param location Target location to measure distance from
     * @return Optional containing the closest match, or empty if no matches
     */
    public Optional<Match> getClosestTo(Location location) {
        return matchCollection.getClosestTo(location);
    }
    
    /**
     * Performs set subtraction on match collections.
     *
     * @param matches The ActionResult containing matches to exclude
     * @return New ActionResult with non-overlapping matches
     */
    public ActionResult minus(ActionResult matches) {
        ActionResult result = new ActionResult();
        MatchCollection remaining = matchCollection.minus(matches.matchCollection);
        result.matchCollection.addAll(remaining.getMatches());
        return result;
    }
    
    /**
     * Checks if this result contains a specific match.
     *
     * @param match The match to search for
     * @return true if an equal match exists in this result
     */
    public boolean containsMatch(Match match) {
        return matchCollection.contains(match);
    }
    
    /**
     * Filters matches that contain other matches within their bounds.
     *
     * @param insideMatches Matches that must be found inside returned matches
     * @return New ActionResult with only confirmed container matches
     */
    public ActionResult getConfirmedMatches(ActionResult insideMatches) {
        ActionResult result = new ActionResult();
        List<Match> confirmed = MatchFilter.byPredicate(
            matchCollection.getMatches(),
            m -> insideMatches.containsMatch(m)
        );
        result.matchCollection.addAll(confirmed);
        return result;
    }
    
    /**
     * Removes matches not confirmed by the inside matches.
     *
     * @param insideMatches Matches that confirm which to keep
     */
    public void removeNonConfirmedMatches(ActionResult insideMatches) {
        List<Match> confirmed = MatchFilter.byPredicate(
            matchCollection.getMatches(),
            m -> insideMatches.containsMatch(m)
        );
        matchCollection.clear();
        matchCollection.addAll(confirmed);
    }
    
    /**
     * Retains only matches confirmed by the inside matches.
     *
     * @param insideMatches Matches used for confirmation
     */
    public void keepOnlyConfirmedMatches(ActionResult insideMatches) {
        removeNonConfirmedMatches(insideMatches);
    }
    
    /**
     * Adds all matches from a list to this result.
     *
     * @param newMatches List of matches to add
     */
    public void addAll(List<Match> newMatches) {
        if (newMatches != null) {
            for (Match m : newMatches) {
                add(m);
            }
        }
    }
    
    /**
     * Gets unique state object IDs from all matches.
     *
     * @return Set of unique state object identifiers
     */
    public Set<String> getUniqueImageIds() {
        return matchCollection.getUniqueStateObjectIds();
    }
    
    /**
     * Filters matches by state object ID.
     *
     * @param id The state object ID to filter by
     * @return List of matches from the specified state object
     */
    public List<Match> getMatchObjectsWithTargetStateObject(String id) {
        return matchCollection.getByStateObject(id);
    }
    
    /**
     * Converts matches back to StateImage objects.
     *
     * @return List of StateImages created from matches
     */
    public List<StateImage> getMatchListAsStateImages() {
        return matchCollection.getMatches().stream()
                .map(Match::toStateImage)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets unique owner state names from all matches.
     * 
     * @return Set of unique owner state names
     */
    public Set<String> getOwnerStateNames() {
        return matchCollection.getUniqueOwnerStates();
    }
    
    /**
     * Gets matches from a specific owner state.
     * 
     * @param ownerStateName The owner state name
     * @return List of matches from that state
     */
    public List<Match> getMatchObjectsWithOwnerState(String ownerStateName) {
        return matchCollection.getByOwnerState(ownerStateName);
    }
    
    // =====================================================
    // Text Management Methods (delegated to TextExtractionResult)
    // =====================================================
    
    /**
     * Gets accumulated text content from all matches.
     * 
     * @return Text object containing all extracted text
     */
    public Text getText() {
        return textResult.getAccumulatedText();
    }
    
    /**
     * Sets the accumulated text content.
     * 
     * @param text Text object to set
     */
    public void setText(Text text) {
        textResult.setAccumulatedText(text);
    }
    
    /**
     * Gets specific text selected or highlighted during the action.
     * 
     * @return The selected text
     */
    public String getSelectedText() {
        return textResult.getSelectedText();
    }
    
    /**
     * Sets the selected text.
     * 
     * @param selectedText The text to set as selected
     */
    public void setSelectedText(String selectedText) {
        textResult.setSelectedText(selectedText);
    }
    
    /**
     * Adds a text string to the accumulated text results.
     *
     * @param str Text to add to the results
     */
    public void addString(String str) {
        textResult.addText(str);
    }
    
    // =====================================================
    // State Management Methods (delegated to StateTracker)
    // =====================================================
    
    /**
     * Gets names of states identified as active during action execution.
     * 
     * @return Set of active state names
     */
    public Set<String> getActiveStates() {
        return stateTracker.getActiveStates();
    }
    
    /**
     * Sets the active states.
     * 
     * @param activeStates Set of active state names
     */
    public void setActiveStates(Set<String> activeStates) {
        stateTracker.clear();
        if (activeStates != null) {
            activeStates.forEach(stateTracker::recordActiveState);
        }
    }
    
    // =====================================================
    // Timing Methods (delegated to TimingData)
    // =====================================================
    
    /**
     * Gets the total time taken for action execution.
     * 
     * @return Duration of the action
     */
    public Duration getDuration() {
        return timingData.getElapsed();
    }
    
    /**
     * Sets the duration.
     * 
     * @param duration Duration to set
     */
    public void setDuration(Duration duration) {
        timingData.setTotalDuration(duration != null ? duration : Duration.ZERO);
    }
    
    /**
     * Gets the timestamp when action execution began.
     * 
     * @return Start time
     */
    public LocalDateTime getStartTime() {
        return timingData.getStartTime();
    }
    
    /**
     * Sets the start time.
     * 
     * @param startTime Start time to set
     */
    public void setStartTime(LocalDateTime startTime) {
        timingData.setStartTime(startTime);
    }
    
    /**
     * Gets the timestamp when action execution completed.
     * 
     * @return End time
     */
    public LocalDateTime getEndTime() {
        return timingData.getEndTime();
    }
    
    /**
     * Sets the end time.
     * 
     * @param endTime End time to set
     */
    public void setEndTime(LocalDateTime endTime) {
        timingData.setEndTime(endTime);
        timingData.stop();
    }
    
    // =====================================================
    // Region Management Methods (delegated to RegionManager)
    // =====================================================
    
    /**
     * Gets regions created or captured by DEFINE actions.
     * 
     * @return List of defined regions
     */
    public List<Region> getDefinedRegions() {
        return regionManager.getAllRegions();
    }
    
    /**
     * Sets the defined regions.
     * 
     * @param definedRegions List of regions to set
     */
    public void setDefinedRegions(List<Region> definedRegions) {
        regionManager.clear();
        if (definedRegions != null) {
            definedRegions.forEach(regionManager::defineRegion);
        }
    }
    
    /**
     * Adds a region to the defined regions collection.
     *
     * @param region The region to add
     */
    public void addDefinedRegion(Region region) {
        regionManager.defineRegion(region);
    }
    
    /**
     * Gets the primary region defined by this action.
     *
     * @return The first defined region or an empty region
     */
    public Region getDefinedRegion() {
        return regionManager.getPrimaryRegion();
    }
    
    // =====================================================
    // Movement Management Methods (delegated to MovementTracker)
    // =====================================================
    
    /**
     * Gets list of movements performed during action execution.
     * 
     * @return List of movements
     */
    public List<Movement> getMovements() {
        return movementTracker.getMovementSequence();
    }
    
    /**
     * Sets the movements list.
     * 
     * @param movements List of movements to set
     */
    public void setMovements(List<Movement> movements) {
        movementTracker.clear();
        if (movements != null) {
            movements.forEach(movementTracker::recordMovement);
        }
    }
    
    /**
     * Adds a movement to the result.
     * 
     * @param movement The movement to add
     */
    public void addMovement(Movement movement) {
        movementTracker.recordMovement(movement);
    }
    
    /**
     * Returns an Optional containing the first movement from the action.
     *
     * @return An Optional containing the first Movement if one exists
     */
    @JsonIgnore
    public Optional<Movement> getMovement() {
        return movementTracker.getFirstMovement();
    }
    
    // =====================================================
    // Analysis Methods (delegated to ActionAnalysis)
    // =====================================================
    
    /**
     * Gets collection of scene analysis results.
     * 
     * @return SceneAnalyses collection
     */
    @JsonIgnore
    public SceneAnalyses getSceneAnalysisCollection() {
        return actionAnalysis.getSceneAnalyses();
    }
    
    /**
     * Sets the scene analysis collection.
     * 
     * @param sceneAnalyses SceneAnalyses to set
     */
    public void setSceneAnalysisCollection(SceneAnalyses sceneAnalyses) {
        actionAnalysis.setSceneAnalyses(sceneAnalyses != null ? sceneAnalyses : new SceneAnalyses());
    }
    
    /**
     * Adds scene analysis data from color or motion detection.
     *
     * @param sceneAnalysis Analysis results to incorporate
     */
    public void addSceneAnalysis(SceneAnalysis sceneAnalysis) {
        actionAnalysis.addSceneAnalysis(sceneAnalysis);
    }
    
    /**
     * Gets binary mask indicating regions of interest or motion.
     * 
     * @return OpenCV Mat mask
     */
    @JsonIgnore
    public Mat getMask() {
        return actionAnalysis.getMask();
    }
    
    /**
     * Sets the binary mask.
     * 
     * @param mask OpenCV Mat mask to set
     */
    public void setMask(Mat mask) {
        actionAnalysis.setMask(mask);
    }
    
    // =====================================================
    // Execution History Methods (delegated to ExecutionHistory)
    // =====================================================
    
    /**
     * Gets ordered history of action execution steps.
     * 
     * @return List of action records
     */
    public List<ActionRecord> getExecutionHistory() {
        return executionHistory.getHistory();
    }
    
    /**
     * Sets the execution history.
     * 
     * @param history List of action records to set
     */
    public void setExecutionHistory(List<ActionRecord> history) {
        executionHistory.clear();
        if (history != null) {
            history.forEach(executionHistory::recordStep);
        }
    }
    
    /**
     * Adds an action record to the execution history.
     * 
     * @param record The action record to add
     */
    public void addExecutionRecord(ActionRecord record) {
        executionHistory.recordStep(record);
    }
    
    // =====================================================
    // Merge Methods
    // =====================================================
    
    /**
     * Merges match objects from another ActionResult.
     *
     * @param matches Source ActionResult containing matches to add
     */
    public void addMatchObjects(ActionResult matches) {
        if (matches != null) {
            for (Match match : matches.getMatchList()) {
                add(match);
            }
        }
    }
    
    /**
     * Merges all data from another ActionResult.
     *
     * @param matches Source ActionResult to merge completely
     */
    public void addAllResults(ActionResult matches) {
        if (matches != null) {
            addMatchObjects(matches);
            addNonMatchResults(matches);
        }
    }
    
    /**
     * Merges non-match data from another ActionResult.
     *
     * @param matches Source ActionResult containing data to merge
     */
    public void addNonMatchResults(ActionResult matches) {
        if (matches != null) {
            textResult.merge(matches.textResult);
            stateTracker.merge(matches.stateTracker);
            timingData.merge(matches.timingData);
            actionAnalysis.merge(matches.actionAnalysis);
            regionManager.merge(matches.regionManager);
            movementTracker.merge(matches.movementTracker);
            executionHistory.merge(matches.executionHistory);
        }
    }
    
    // =====================================================
    // Utility Methods
    // =====================================================
    
    /**
     * Converts this result into an ObjectCollection.
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
     */
    public void print() {
        matchCollection.getMatches().forEach(System.out::println);
    }
    
    /**
     * Gets a visual symbol representing action success or failure.
     *
     * @return Unicode symbol indicating success (✓) or failure (✗)
     */
    public String getSuccessSymbol() {
        return success ? MessageFormatter.check : MessageFormatter.fail;
    }
    
    /**
     * Formats matches as a temporary state structure visualization.
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
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ActionResult: ").append("size=").append(size()).append(" ");
        for (Match match : getMatchList()) {
            stringBuilder.append(match).append(" ");
        }
        return stringBuilder.toString();
    }
    
    // =====================================================
    // Logging Methods
    // =====================================================
    
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        if (actionConfig != null) {
            summary.append("Action: ").append(actionConfig.getClass().getSimpleName()).append("\n");
        }
        summary.append("Success: ").append(success).append("\n");
        summary.append("Number of matches: ").append(matchCollection.size()).append("\n");
        summary.append("Active states: ").append(String.join(", ", stateTracker.getActiveStates())).append("\n");
        if (textResult.hasText()) {
            summary.append("Extracted text: ").append(textResult.getCombinedText()).append("\n");
        }
        return summary.toString();
    }
    
    public List<StateImageData> getStateImageData() {
        return matchCollection.getMatches().stream()
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
                    -1,
                    -1
            );
        }
    }
    
    // ===============================
    // Enhanced Logging Data Structures (maintained for test compatibility)
    // ===============================
    
    @Data
    public static class ActionExecutionContext {
        private String actionType;
        private List<StateImage> targetImages = new ArrayList<>();
        private List<String> targetStrings = new ArrayList<>();
        private List<Region> targetRegions = new ArrayList<>();
        private String primaryTargetName;
        private boolean success;
        private Duration executionDuration = Duration.ZERO;
        private Instant startTime;
        private Instant endTime;
        private List<Match> resultMatches = new ArrayList<>();
        private Throwable executionError;
        private String executingThread;
        private String actionId;
    }
    
    @Data
    public static class ActionMetrics {
        private long executionTimeMs;
        private int matchCount;
        private double bestMatchConfidence = 0.0;
        private String threadName;
        private String actionId;
        private int retryCount = 0;
        private long retryTimeMs = 0;
    }
    
    @Data
    public static class EnvironmentSnapshot {
        private List<MonitorInfo> monitors = new ArrayList<>();
        private String osName;
        private String javaVersion;
        private boolean headlessMode;
        private Instant captureTime;
        
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
        
        private static EnvironmentSnapshot captureEnvironment() {
            EnvironmentSnapshot snapshot = new EnvironmentSnapshot();
            snapshot.setOsName(System.getProperty("os.name", "unknown"));
            snapshot.setJavaVersion(System.getProperty("java.version", "unknown"));
            snapshot.setHeadlessMode("true".equals(System.getProperty("java.awt.headless")));
            snapshot.setCaptureTime(Instant.now());
            snapshot.setMonitors(new ArrayList<>());
            return snapshot;
        }
    }
    
    @Data
    public static class MonitorInfo {
        private int monitorId;
        private int width;
        private int height;
        private int x;
        private int y;
        private boolean primary;
    }
    
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
    
    public String getLogTargetName() {
        if (executionContext != null && executionContext.getPrimaryTargetName() != null) {
            return executionContext.getPrimaryTargetName();
        }
        
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
    
    public long getExecutionTimeMs() {
        if (actionMetrics != null) {
            return actionMetrics.getExecutionTimeMs();
        }
        return timingData.getExecutionTimeMs();
    }
    
    public boolean isLoggable() {
        return executionContext != null && 
               executionContext.getEndTime() != null;
    }
}