package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.basic.find.FindAll;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.ActionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Orchestrates iterative pattern finding across multiple scenes and state images.
 * <p>
 * This component manages the iteration process for finding patterns, coordinating
 * searches across multiple scenes and state images. It supports various find strategies
 * (FIRST, EACH, ALL, BEST) and manages the action lifecycle during the search process.
 * The component intelligently routes searches based on pattern variability, using either
 * standard image finding or region-in-pattern (RIP) techniques as appropriate.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Iterates through multiple scenes and state images systematically</li>
 *   <li>Manages early termination for FIRST strategy when matches are found</li>
 *   <li>Aggregates matches both globally and per-scene for analysis</li>
 *   <li>Monitors action lifecycle to handle interruptions or failures</li>
 * </ul>
 * 
 * @see FindAll
 * @see ActionLifecycleManagement
 * @see SceneAnalysis
 */
@Slf4j
@Component
public class IterativePatternFinder {

    private final ActionLifecycleManagement actionLifecycleManagement;
    private final FindAll findAll;
    private final DynamicRegionResolver dynamicRegionResolver;

    /**
     * Creates a new IterativePatternFinder instance with required dependencies.
     * 
     * @param actionLifecycleManagement Service for managing action lifecycle and monitoring execution
     * @param findAll Service for performing exhaustive pattern matching
     * @param dynamicRegionResolver Service for updating search regions based on found matches
     */
    public IterativePatternFinder(ActionLifecycleManagement actionLifecycleManagement, 
                                 FindAll findAll,
                                 DynamicRegionResolver dynamicRegionResolver) {
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.findAll = findAll;
        this.dynamicRegionResolver = dynamicRegionResolver;
    }

    /**
     * Performs iterative pattern finding across multiple scenes and state images.
     * <p>
     * This method systematically searches for patterns from each state image within each
     * provided scene. The search behavior adapts based on the find strategy:
     * <ul>
     *   <li>For FIRST strategy: Returns immediately upon finding sufficient matches</li>
     *   <li>For other strategies: Continues searching all scenes and images exhaustively</li>
     * </ul>
     * 
     * <p>The method maintains two levels of match aggregation:
     * <ol>
     *   <li>Global matches - All matches across all scenes (stored in the ActionResult)</li>
     *   <li>Per-scene matches - Matches grouped by scene (stored as SceneAnalysis)</li>
     * </ol>
     * 
     * <p>Action lifecycle management ensures the search can be interrupted if:
     * <ul>
     *   <li>Sufficient matches are found (for FIRST strategy)</li>
     *   <li>The action is cancelled or times out</li>
     *   <li>An error condition occurs</li>
     * </ul>
     * 
     * @param matches The ActionResult to populate with found matches and scene analyses.
     *                This object is modified throughout execution.
     * @param stateImages The list of state images containing patterns to search for.
     *                    Must not be null or empty.
     * @param scenes The list of scenes (screenshots) to search within. Must not be null or empty.
     */
    public void find(ActionResult matches, List<StateImage> stateImages, List<Scene> scenes) {
        // Add null checks for defensive programming
        if (matches == null || stateImages == null || scenes == null) {
            return;
        }
        
        actionLifecycleManagement.printActionOnce(matches);
        for (Scene scene : scenes) {
            List<Match> singleSceneMatchList = new ArrayList<>(); // holds finds for a specific scene
            for (int i=0; i<stateImages.size(); i++) { // run for each StateImage
                StateImage currentImage = stateImages.get(i);
                log.info("[ITERATIVE] Processing StateImage {} of {}: '{}'", 
                        i+1, stateImages.size(), currentImage.getName());
                log.info("[ITERATIVE]   Has SearchRegionOnObject: {}", 
                        currentImage.getSearchRegionOnObject() != null);
                if (currentImage.getSearchRegionOnObject() != null) {
                    log.info("[ITERATIVE]   Depends on: {}.{}",
                            currentImage.getSearchRegionOnObject().getTargetStateName(),
                            currentImage.getSearchRegionOnObject().getTargetObjectName());
                }
                
                // Log current pattern search regions BEFORE searching
                log.info("[ITERATIVE]   Pattern search regions BEFORE find:");
                for (int p = 0; p < currentImage.getPatterns().size(); p++) {
                    var pattern = currentImage.getPatterns().get(p);
                    log.info("[ITERATIVE]     Pattern {}: {} regions",
                            p, pattern.getSearchRegions() != null ? 
                            pattern.getSearchRegions().getAllRegions().size() : 0);
                    if (pattern.getSearchRegions() != null && !pattern.getSearchRegions().getAllRegions().isEmpty()) {
                        for (var region : pattern.getSearchRegions().getAllRegions()) {
                            log.info("[ITERATIVE]       - {}", region);
                        }
                    }
                }
                
                List<Match> newMatches = findAll.find(currentImage, scene, matches.getActionConfig());
                singleSceneMatchList.addAll(newMatches);
                matches.addAll(newMatches); // holds all matches found
                
                log.info("[ITERATIVE]   Found {} matches for '{}'", 
                        newMatches.size(), currentImage.getName());
                
                // CRITICAL: Update dependent search regions immediately after finding matches
                // This ensures that if ImageA is found, ImageB's search regions are updated
                // before ImageB is searched for, enabling proper declarative region resolution
                if (!newMatches.isEmpty()) {
                    log.info("[ITERATIVE] Found {} matches for {}, updating dependent search regions", 
                            newMatches.size(), currentImage.getName());
                    dynamicRegionResolver.updateDependentSearchRegions(matches);
                    
                    // Also update search regions for remaining state images in this iteration
                    // This ensures that if ImageA is found, ImageB (which might be later in the list)
                    // gets its search regions updated before being searched
                    List<StateImage> remainingImages = stateImages.subList(i + 1, stateImages.size());
                    if (!remainingImages.isEmpty()) {
                        log.info("[ITERATIVE] Updating search regions for {} remaining state images", remainingImages.size());
                        for (StateImage img : remainingImages) {
                            log.info("[ITERATIVE]   - Will update: '{}' (has dependency: {})",
                                    img.getName(), img.getSearchRegionOnObject() != null);
                        }
                        dynamicRegionResolver.updateSearchRegionsForObjects(
                            new ArrayList<>(remainingImages), matches);
                    }
                }
                
                if (!actionLifecycleManagement.isOkToContinueAction(matches, stateImages.size())) return;
            }
            SceneAnalysis sceneAnalysis = new SceneAnalysis(scene);
            sceneAnalysis.setMatchList(singleSceneMatchList);
            matches.getSceneAnalysisCollection().add(sceneAnalysis);
        }
    }

}
