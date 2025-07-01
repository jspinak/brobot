package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.analysis.color.profiles.ProfileSetBuilder;
import io.github.jspinak.brobot.action.internal.find.NonImageObjectConverter;
import io.github.jspinak.brobot.action.internal.find.OffsetLocationManagerV2;
import io.github.jspinak.brobot.action.internal.find.match.MatchAdjusterV2;
import io.github.jspinak.brobot.action.internal.find.match.MatchContentExtractor;
import io.github.jspinak.brobot.analysis.match.MatchFusion;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.util.string.TextSelector;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Core pattern matching action that locates GUI elements on the screen.
 * 
 * <p>Find is the fundamental action in Brobot's visual GUI automation, implementing various 
 * pattern matching strategies to locate GUI elements. It embodies the visual recognition 
 * capability that enables the framework to interact with any GUI regardless of the underlying 
 * technology.</p>
 * 
 * <p>Find strategies supported:
 * <ul>
 *   <li><b>FIRST</b>: Returns the first match found, optimized for speed</li>
 *   <li><b>BEST</b>: Returns the highest-scoring match from all possibilities</li>
 *   <li><b>EACH</b>: Returns one match per StateImage/Pattern</li>
 *   <li><b>ALL</b>: Returns all matches found, useful for lists and grids</li>
 *   <li><b>CUSTOM</b>: User-defined find strategies for special cases</li>
 * </ul>
 * </p>
 * 
 * <p>Advanced features:
 * <ul>
 *   <li>Multi-pattern matching with StateImages containing multiple templates</li>
 *   <li>Color-based matching using k-means profiles</li>
 *   <li>Text extraction from matched regions (OCR integration)</li>
 *   <li>Match fusion for combining overlapping results</li>
 *   <li>Dynamic offset adjustments for precise targeting</li>
 * </ul>
 * </p>
 * 
 * <p>Find operations also handle non-image objects in ObjectCollections:
 * <ul>
 *   <li>Existing Matches can be reused without re-searching</li>
 *   <li>Regions are converted to matches for consistent handling</li>
 *   <li>Locations provide direct targeting without pattern matching</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Find operations are context-aware through integration 
 * with StateMemory, automatically adjusting active states based on what is found. This 
 * enables the framework to maintain an accurate understanding of the current GUI state.</p>
 * 
 * @since 1.0
 * @see FindStrategy
 * @see BaseFindOptions
 * @see StateImage
 * @see ActionResult
 * @see Pattern
 * @author Joshua Spinak
 */
@Component
public class Find implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.FIND;
    }

    private final FindStrategyRegistryV2 findFunctions;
    private final StateMemory stateMemory;
    private final NonImageObjectConverter addNonImageObjects;
    private final MatchAdjusterV2 adjustMatches;
    private final ProfileSetBuilder setAllProfiles;
    private final OffsetLocationManagerV2 offsetOps;
    private final MatchFusion matchFusion;
    private final MatchContentExtractor matchContentExtractor;
    private final TextSelector textSelector;

    public Find(FindStrategyRegistryV2 findFunctions,
                StateMemory stateMemory, NonImageObjectConverter addNonImageObjects, 
                MatchAdjusterV2 adjustMatches,
                ProfileSetBuilder setAllProfiles, 
                OffsetLocationManagerV2 offsetOps, MatchFusion matchFusion,
                MatchContentExtractor matchContentExtractor, TextSelector textSelector) {
        this.findFunctions = findFunctions;
        this.stateMemory = stateMemory;
        this.addNonImageObjects = addNonImageObjects;
        this.adjustMatches = adjustMatches;
        this.setAllProfiles = setAllProfiles;
        this.offsetOps = offsetOps;
        this.matchFusion = matchFusion;
        this.matchContentExtractor = matchContentExtractor;
        this.textSelector = textSelector;
    }

    /**
     * Executes the find operation to locate GUI elements on screen.
     * <p>
     * This method orchestrates the complete find process, including pattern matching,
     * state management, match fusion, and post-processing adjustments. When called
     * directly (rather than through Action.perform), certain lifecycle operations are
     * bypassed to avoid redundant processing:
     * <ul>
     *   <li>Wait.pauseBeforeBegin - Pre-action delays</li>
     *   <li>Matches.setSuccess - Success flag setting</li>
     *   <li>Matches.setDuration - Timing measurements</li>
     *   <li>Matches.saveSnapshots - Screenshot capturing</li>
     *   <li>Wait.pauseAfterEnd - Post-action delays</li>
     * </ul>
     * 
     * <p>The method performs the following operations in sequence:
     * <ol>
     *   <li>Creates color profiles if COLOR find strategy is specified</li>
     *   <li>Sets maximum matches limit from action options</li>
     *   <li>Adds offset matches if specified</li>
     *   <li>Executes the appropriate find function based on strategy</li>
     *   <li>Updates active states based on found matches</li>
     *   <li>Adds non-image objects (regions, locations) as matches</li>
     *   <li>Performs match fusion to combine overlapping results</li>
     *   <li>Applies position/size adjustments to all matches</li>
     *   <li>Adds final offset match if specified</li>
     *   <li>Removes matches smaller than minimum area</li>
     *   <li>Extracts text from matched regions if configured</li>
     *   <li>Selects most similar text from extracted results</li>
     * </ol>
     * 
     * @param matches The ActionResult to populate with found matches. This object is
     *                extensively modified during execution, receiving all match results
     *                and extracted text. Must contain valid ActionOptions.
     * @param objectCollections The collections containing patterns, regions, and other
     *                         objects to find. At least one collection must be provided.
     *                         If two collections are given, both are searched for
     *                         StateImages when creating color profiles.
     */
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - expecting BaseFindOptions or its subclasses
        if (!(matches.getActionConfig() instanceof BaseFindOptions)) {
            throw new IllegalArgumentException("Find requires BaseFindOptions configuration");
        }
        BaseFindOptions findOptions = (BaseFindOptions) matches.getActionConfig();
        
        //int actionId = actionLifecycleManagement.newActionLifecycle(actionOptions);
        createColorProfilesWhenNecessary(findOptions, objectCollections);
        matches.setMaxMatches(findOptions.getMaxMatchesToActOn());
        // Use offset manager with match adjustment options
        if (findOptions.getMatchAdjustmentOptions() != null) {
            offsetOps.addOffsetAsOnlyMatch(List.of(objectCollections), matches, 
                findOptions.getMatchAdjustmentOptions(), true);
        }
        
        // Execute the find strategy
        executeFindStrategy(findOptions, matches, objectCollections);
        
        stateMemory.adjustActiveStatesWithMatches(matches);
        ActionResult nonImageMatches = addNonImageObjects.getOtherObjectsDirectlyAsMatchObjects(objectCollections[0]);
        matches.addMatchObjects(nonImageMatches);
        matchFusion.setFusedMatches(matches);
        // Use match adjuster with match adjustment options
        if (findOptions.getMatchAdjustmentOptions() != null) {
            adjustMatches.adjustAll(matches, findOptions.getMatchAdjustmentOptions());
        }
        
        // Filter matches by minimum area if specified
        filterMatchesByArea(matches, findOptions);
        
        matchContentExtractor.set(matches);
        matches.setSelectedText(textSelector.getString(TextSelector.Method.MOST_SIMILAR, matches.getText()));
    }
    
    private void executeFindStrategy(BaseFindOptions findOptions, ActionResult matches, ObjectCollection... objectCollections) {
        // Use find strategy registry that works with BaseFindOptions
        var findFunction = findFunctions.get(findOptions);
        if (findFunction != null) {
            findFunction.accept(matches, List.of(objectCollections));
        } else {
            throw new IllegalStateException("No find function registered for strategy: " + findOptions.getFindStrategy());
        }
    }
    
    private void filterMatchesByArea(ActionResult matches, BaseFindOptions findOptions) {
        // TODO: Add minArea to BaseFindOptions if needed
        // For now, skip area filtering
        // List<Match> mutableMatchList = new ArrayList<>(matches.getMatchList());
        // mutableMatchList.removeIf(match -> match.size() < findOptions.getMinArea());
        // matches.setMatchList(mutableMatchList);
    }

    
    private void createColorProfilesWhenNecessary(BaseFindOptions findOptions, ObjectCollection... objectCollections) {
        // Only create color profiles if using color-based find strategy
        if (findOptions.getFindStrategy() != FindStrategy.COLOR) {
            return;
        }
        
        List<StateImage> imgs = new ArrayList<>();
        if (objectCollections.length >= 1) imgs.addAll(objectCollections[0].getStateImages());
        if (objectCollections.length >= 2) imgs.addAll(objectCollections[1].getStateImages());
        List<StateImage> imagesWithoutColorProfiles = new ArrayList<>();
        for (StateImage img : imgs) {
            if (img.getKmeansProfilesAllSchemas() == null) {
                imagesWithoutColorProfiles.add(img);
            }
        }
        imagesWithoutColorProfiles.forEach(setAllProfiles::setMatsAndColorProfiles);
    }

}