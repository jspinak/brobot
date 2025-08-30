package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.find.NonImageObjectConverter;
import io.github.jspinak.brobot.action.internal.find.OffsetMatchCreator;
import io.github.jspinak.brobot.action.internal.find.match.MatchRegionAdjuster;
import io.github.jspinak.brobot.action.internal.find.match.MatchContentExtractor;
import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import io.github.jspinak.brobot.action.internal.region.SearchRegionDependencyRegistry;
import io.github.jspinak.brobot.action.internal.utility.ActionSuccessCriteria;
import io.github.jspinak.brobot.analysis.color.profiles.ProfileSetBuilder;
import io.github.jspinak.brobot.analysis.match.MatchFusion;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import io.github.jspinak.brobot.util.string.TextSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 */
@Component
@Slf4j
public class FindPipeline {
    
    private final ProfileSetBuilder profileSetBuilder;
    private final OffsetMatchCreator offsetLocationManager;
    private final MatchFusion matchFusion;
    private final MatchRegionAdjuster matchAdjuster;
    private final MatchContentExtractor contentExtractor;
    private final NonImageObjectConverter nonImageObjectConverter;
    private final StateMemory stateMemory;
    private final TextSelector textSelector;
    private final DynamicRegionResolver dynamicRegionResolver;
    private final HighlightManager highlightManager;
    private final VisualFeedbackConfig visualFeedbackConfig;
    private final ModernFindStrategyRegistry findStrategyRegistry;
    private final ActionSuccessCriteria actionSuccessCriteria;
    
    @Value("${brobot.highlighting.enabled:false}")
    private boolean highlightEnabled;

    @Autowired
    public FindPipeline(ProfileSetBuilder profileSetBuilder,
                       OffsetMatchCreator offsetLocationManager,
                       MatchFusion matchFusion,
                       MatchRegionAdjuster matchAdjuster,
                       MatchContentExtractor contentExtractor,
                       NonImageObjectConverter nonImageObjectConverter,
                       StateMemory stateMemory,
                       TextSelector textSelector,
                       DynamicRegionResolver dynamicRegionResolver,
                       HighlightManager highlightManager,
                       VisualFeedbackConfig visualFeedbackConfig,
                       ModernFindStrategyRegistry findStrategyRegistry,
                       ActionSuccessCriteria actionSuccessCriteria) {
        this.profileSetBuilder = profileSetBuilder;
        this.offsetLocationManager = offsetLocationManager;
        this.matchFusion = matchFusion;
        this.matchAdjuster = matchAdjuster;
        this.contentExtractor = contentExtractor;
        this.nonImageObjectConverter = nonImageObjectConverter;
        this.stateMemory = stateMemory;
        this.textSelector = textSelector;
        this.dynamicRegionResolver = dynamicRegionResolver;
        this.highlightManager = highlightManager;
        this.visualFeedbackConfig = visualFeedbackConfig;
        this.findStrategyRegistry = findStrategyRegistry;
        this.actionSuccessCriteria = actionSuccessCriteria;
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
    
    /**
     * Orders StateImages based on dependencies to ensure that images that other images
     * depend on are searched first. This enables proper declarative region resolution.
     * 
     * @param stateImages The list of state images to order
     * @return A new list with images ordered by dependencies
     */
    private List<StateImage> orderByDependencies(List<StateImage> stateImages) {
        List<StateImage> ordered = new ArrayList<>();
        List<StateImage> withDependencies = new ArrayList<>();
        List<StateImage> noDependencies = new ArrayList<>();
        
        // Separate images with and without dependencies
        for (StateImage image : stateImages) {
            if (image.getSearchRegionOnObject() != null) {
                withDependencies.add(image);
            } else {
                noDependencies.add(image);
            }
        }
        
        // Add images without dependencies first (they can be found anywhere)
        ordered.addAll(noDependencies);
        
        // Then add images with dependencies
        // For now, just add them after - a more sophisticated ordering could be implemented
        // to handle complex dependency chains
        ordered.addAll(withDependencies);
        
        log.debug("Ordered {} StateImages: {} without dependencies, {} with dependencies",
                stateImages.size(), noDependencies.size(), withDependencies.size());
        
        // Log the order for debugging
        if (log.isDebugEnabled()) {
            for (int i = 0; i < ordered.size(); i++) {
                StateImage img = ordered.get(i);
                if (img.getSearchRegionOnObject() != null) {
                    log.debug("  [{}] {} depends on {}.{}",
                            i, img.getName(),
                            img.getSearchRegionOnObject().getTargetStateName(),
                            img.getSearchRegionOnObject().getTargetObjectName());
                } else {
                    log.debug("  [{}] {} (no dependencies)", i, img.getName());
                }
            }
        }
        
        return ordered;
    }

    /**
     * Executes the complete find pipeline with the provided options and collections.
     * 
     * @param findOptions The configuration options for the find operation
     * @param matches The action result to populate with matches
     * @param objectCollections The collections of objects to search for
     */
    public void execute(BaseFindOptions findOptions, ActionResult matches, ObjectCollection... objectCollections) {
        // CRITICAL: Order StateImages by dependencies BEFORE searching
        // This ensures that images without dependencies are searched first,
        // and their locations can be used to constrain searches for dependent images
        for (ObjectCollection collection : objectCollections) {
            List<StateImage> originalOrder = new ArrayList<>(collection.getStateImages());
            List<StateImage> orderedImages = orderByDependencies(originalOrder);
            
            // Replace the collection's state images with the ordered list
            collection.getStateImages().clear();
            collection.getStateImages().addAll(orderedImages);
            
            log.debug("Reordered {} StateImages in collection for dependency resolution", 
                    orderedImages.size());
        }
        
        // Note: Dependencies should be registered when states are built, not here
        // For now, we'll check if search regions need updating based on previous matches
        updateCrossStateSearchRegions(matches, objectCollections);
        
        // Highlight search regions if enabled
        if (shouldHighlightSearchRegions()) {
            highlightSearchRegions(objectCollections);
        }
        
        // Convert non-image objects and delegate to find strategies
        ActionResult nonImageMatches = nonImageObjectConverter.getOtherObjectsDirectlyAsMatchObjects(objectCollections[0]);
        matches.addAllResults(nonImageMatches);
        
        // Execute the appropriate find strategy for image objects
        FindStrategy strategy = findOptions.getFindStrategy();
        if (strategy != null && findStrategyRegistry != null) {
            findStrategyRegistry.runFindStrategy(strategy, matches, objectCollections);
        }
        
        // Post-process matches: fusion, adjustment, content extraction
        matchFusion.setFusedMatches(matches);
        matchAdjuster.adjustAll(matches, findOptions.getMatchAdjustmentOptions());
        contentExtractor.set(matches);
        
        // Update search regions for objects that depend on what we just found
        if (!matches.isEmpty()) {
            log.debug("FindPipeline: Found {} matches, updating dependent search regions", matches.size());
            dynamicRegionResolver.updateDependentSearchRegions(matches);
        } else {
            log.debug("FindPipeline: No matches found, skipping dependent search region update");
        }
        
        // Highlight found matches if enabled
        if (shouldHighlightFinds() && !matches.isEmpty()) {
            highlightManager.highlightMatches(matches.getMatchList());
        }
        
        // Set success criteria based on the action configuration
        // This ensures success is evaluated before the ActionLifecycleAspect logs the result
        if (matches.getActionConfig() != null) {
            actionSuccessCriteria.set(matches.getActionConfig(), matches);
        }
    }

    /**
     * Checks if search regions should be highlighted based on configuration.
     */
    private boolean shouldHighlightSearchRegions() {
        return highlightEnabled && 
               highlightManager != null && 
               visualFeedbackConfig != null && 
               visualFeedbackConfig.isEnabled() && 
               visualFeedbackConfig.isAutoHighlightSearchRegions();
    }
    
    /**
     * Checks if found matches should be highlighted based on configuration.
     */
    private boolean shouldHighlightFinds() {
        return highlightEnabled && 
               highlightManager != null && 
               visualFeedbackConfig != null && 
               visualFeedbackConfig.isEnabled() && 
               visualFeedbackConfig.isAutoHighlightFinds();
    }
    
    /**
     * Highlights the search regions for all objects in the collections.
     * 
     * @param collections The object collections containing search regions to highlight
     */
    private void highlightSearchRegions(ObjectCollection... collections) {
        if (highlightManager == null) return;
        
        List<HighlightManager.RegionWithContext> regionsWithContext = new ArrayList<>();
        
        for (ObjectCollection collection : collections) {
            // Extract regions from StateRegions
            for (StateRegion stateRegion : collection.getStateRegions()) {
                if (stateRegion.getSearchRegion() != null) {
                    regionsWithContext.add(new HighlightManager.RegionWithContext(
                        stateRegion.getSearchRegion(),
                        stateRegion.getOwnerStateName(),
                        stateRegion.getName()
                    ));
                }
            }
            
            // Extract search regions from StateImages
            for (StateImage stateImage : collection.getStateImages()) {
                for (var pattern : stateImage.getPatterns()) {
                    if (pattern.getSearchRegions() != null && 
                        !pattern.getSearchRegions().getAllRegions().isEmpty()) {
                        for (Region region : pattern.getSearchRegions().getAllRegions()) {
                            regionsWithContext.add(new HighlightManager.RegionWithContext(
                                region,
                                stateImage.getOwnerStateName(),
                                stateImage.getName()
                            ));
                        }
                    }
                }
            }
        }
        
        // If no specific search regions found, don't highlight anything
        // (avoid highlighting the entire screen as a fallback)
        if (!regionsWithContext.isEmpty()) {
            highlightManager.highlightSearchRegionsWithContext(regionsWithContext);
        }
    }
    
}
