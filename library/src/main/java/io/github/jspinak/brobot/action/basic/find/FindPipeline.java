package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.internal.find.NonImageObjectConverter;
import io.github.jspinak.brobot.action.internal.find.OffsetLocationManagerV2;
import io.github.jspinak.brobot.action.internal.find.match.MatchAdjusterV2;
import io.github.jspinak.brobot.action.internal.find.match.MatchContentExtractor;
import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import io.github.jspinak.brobot.analysis.color.profiles.ProfileSetBuilder;
import io.github.jspinak.brobot.analysis.match.MatchFusion;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.util.string.TextSelector;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the Find operation pipeline, orchestrating all steps of the pattern matching process.
 * 
 * <p>This class implements a clear separation of concerns by organizing the Find operation
 * into three distinct phases:
 * <ul>
 *   <li><b>Pre-processing</b>: Color profile creation and initial offset setup</li>
 *   <li><b>Strategy execution</b>: Running the selected find strategy</li>
 *   <li><b>Post-processing</b>: State management, match fusion, adjustments, and text extraction</li>
 * </ul>
 * </p>
 * 
 * <p>By extracting the orchestration logic from the Find class, this pipeline:
 * <ul>
 *   <li>Makes the find process more testable and maintainable</li>
 *   <li>Provides clear extension points for customization</li>
 *   <li>Enables easier debugging and monitoring of each phase</li>
 *   <li>Allows for future pipeline variations without changing the Find facade</li>
 * </ul>
 * </p>
 * 
 * @since 1.1.0
 * @see Find
 * @see BaseFindOptions
 * @see ActionResult
 */
@Component
public class FindPipeline {
    
    private final FindStrategyRegistryV2 findFunctions;
    private final StateMemory stateMemory;
    private final NonImageObjectConverter addNonImageObjects;
    private final MatchAdjusterV2 adjustMatches;
    private final ProfileSetBuilder setAllProfiles;
    private final OffsetLocationManagerV2 offsetOps;
    private final MatchFusion matchFusion;
    private final MatchContentExtractor matchContentExtractor;
    private final TextSelector textSelector;
    private final DynamicRegionResolver dynamicRegionResolver;
    
    public FindPipeline(FindStrategyRegistryV2 findFunctions,
                        StateMemory stateMemory,
                        NonImageObjectConverter addNonImageObjects,
                        MatchAdjusterV2 adjustMatches,
                        ProfileSetBuilder setAllProfiles,
                        OffsetLocationManagerV2 offsetOps,
                        MatchFusion matchFusion,
                        MatchContentExtractor matchContentExtractor,
                        TextSelector textSelector,
                        DynamicRegionResolver dynamicRegionResolver) {
        this.findFunctions = findFunctions;
        this.stateMemory = stateMemory;
        this.addNonImageObjects = addNonImageObjects;
        this.adjustMatches = adjustMatches;
        this.setAllProfiles = setAllProfiles;
        this.offsetOps = offsetOps;
        this.matchFusion = matchFusion;
        this.matchContentExtractor = matchContentExtractor;
        this.textSelector = textSelector;
        this.dynamicRegionResolver = dynamicRegionResolver;
    }
    
    /**
     * Executes the complete Find pipeline with the given configuration and object collections.
     * 
     * @param options The find configuration options
     * @param matches The ActionResult to populate with found matches
     * @param collections The object collections containing patterns to find
     * @throws IllegalArgumentException if options is not a BaseFindOptions instance
     * @throws IllegalStateException if no find strategy is registered for the given options
     */
    public void execute(BaseFindOptions options, ActionResult matches, ObjectCollection... collections) {
        runPreProcessing(options, matches, collections);
        runStrategy(options, matches, collections);
        runPostProcessing(options, matches, collections);
    }
    
    /**
     * Pre-processing phase: Prepares the environment for pattern matching.
     * 
     * <p>This phase handles:
     * <ul>
     *   <li>Updating cross-state search regions</li>
     *   <li>Creating color profiles for COLOR strategy</li>
     *   <li>Setting maximum matches limit</li>
     *   <li>Adding initial offset matches if configured</li>
     * </ul>
     * </p>
     */
    private void runPreProcessing(BaseFindOptions options, ActionResult matches, ObjectCollection... collections) {
        // Update cross-state search regions for all objects
        updateCrossStateSearchRegions(matches, collections);
        
        // Create color profiles if using COLOR strategy
        createColorProfilesWhenNecessary(options, collections);
        
        // Set max matches limit
        matches.setMaxMatches(options.getMaxMatchesToActOn());
        
        // Add initial offset if configured
        if (options.getMatchAdjustmentOptions() != null) {
            offsetOps.addOffsetAsOnlyMatch(List.of(collections), matches, 
                options.getMatchAdjustmentOptions(), true);
        }
    }
    
    /**
     * Strategy execution phase: Runs the selected find strategy.
     * 
     * <p>This phase delegates to the appropriate find strategy implementation
     * based on the configured FindStrategy (FIRST, BEST, ALL, etc.).</p>
     */
    private void runStrategy(BaseFindOptions options, ActionResult matches, ObjectCollection... collections) {
        var findFunction = findFunctions.get(options);
        if (findFunction != null) {
            findFunction.accept(matches, List.of(collections));
        } else {
            throw new IllegalStateException("No find function registered for strategy: " + options.getFindStrategy());
        }
    }
    
    /**
     * Post-processing phase: Refines and enriches the match results.
     * 
     * <p>This phase handles:
     * <ul>
     *   <li>State memory updates based on found matches</li>
     *   <li>Adding non-image objects as matches</li>
     *   <li>Match fusion for overlapping results</li>
     *   <li>Position and size adjustments</li>
     *   <li>Area-based filtering</li>
     *   <li>Text extraction from matched regions</li>
     *   <li>Text selection from extracted content</li>
     * </ul>
     * </p>
     */
    private void runPostProcessing(BaseFindOptions options, ActionResult matches, ObjectCollection... collections) {
        // Update state memory
        stateMemory.adjustActiveStatesWithMatches(matches);
        
        // Add non-image objects
        ActionResult nonImageMatches = addNonImageObjects.getOtherObjectsDirectlyAsMatchObjects(collections[0]);
        matches.addMatchObjects(nonImageMatches);
        
        // Perform match fusion
        matchFusion.setFusedMatches(matches);
        
        // Apply match adjustments
        if (options.getMatchAdjustmentOptions() != null) {
            adjustMatches.adjustAll(matches, options.getMatchAdjustmentOptions());
        }
        
        // Filter by area if needed
        filterMatchesByArea(matches, options);
        
        // Extract text content
        matchContentExtractor.set(matches);
        
        // Select most similar text
        matches.setSelectedText(textSelector.getString(TextSelector.Method.MOST_SIMILAR, matches.getText()));
    }
    
    /**
     * Creates color profiles for StateImages when using COLOR find strategy.
     */
    private void createColorProfilesWhenNecessary(BaseFindOptions options, ObjectCollection... collections) {
        if (options.getFindStrategy() != FindStrategy.COLOR) {
            return;
        }
        
        List<StateImage> imgs = new ArrayList<>();
        if (collections.length >= 1) imgs.addAll(collections[0].getStateImages());
        if (collections.length >= 2) imgs.addAll(collections[1].getStateImages());
        
        List<StateImage> imagesWithoutColorProfiles = imgs.stream()
            .filter(img -> img.getKmeansProfilesAllSchemas() == null)
            .toList();
            
        imagesWithoutColorProfiles.forEach(setAllProfiles::setMatsAndColorProfiles);
    }
    
    /**
     * Filters matches by minimum area if configured.
     * 
     * <p>This method checks if the find options include area filtering
     * configuration and delegates to MatchAdjusterV2 for efficient filtering.</p>
     */
    private void filterMatchesByArea(ActionResult matches, BaseFindOptions options) {
        // Check if options support area filtering
        if (options instanceof ColorFindOptions) {
            ColorFindOptions colorOptions = (ColorFindOptions) options;
            AreaFilteringOptions areaFilter = colorOptions.getAreaFiltering();
            if (areaFilter != null && areaFilter.getMinArea() > 0) {
                adjustMatches.filterByMinimumArea(matches, areaFilter.getMinArea());
            }
        }
        // Add similar checks for other option types that support area filtering
        // as they are implemented
    }
    
    /**
     * Updates search regions for objects that have cross-state search region configurations.
     * 
     * <p>This method processes all state objects in the collections and updates their
     * search regions based on matches from other state objects, enabling dynamic
     * search area definition.</p>
     */
    private void updateCrossStateSearchRegions(ActionResult matches, ObjectCollection... collections) {
        List<StateObject> allObjects = new ArrayList<>();
        
        // Collect all state objects
        for (ObjectCollection collection : collections) {
            allObjects.addAll(collection.getStateImages());
            allObjects.addAll(collection.getStateRegions());
            allObjects.addAll(collection.getStateLocations());
        }
        
        // Update search regions based on cross-state references
        dynamicRegionResolver.updateSearchRegionsForObjects(allObjects, matches);
    }
}