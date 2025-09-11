package io.github.jspinak.brobot.action.basic.find;

import java.util.*;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.color.SceneProvider;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.action.internal.find.DefinedRegionConverter;
import io.github.jspinak.brobot.action.internal.find.IterativePatternFinder;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.extern.slf4j.Slf4j;

/**
 * Core component for finding images on screen using various search strategies.
 *
 * <p>This class implements the primary image matching functionality in Brobot, supporting multiple
 * find strategies including finding all matches, finding the best match, finding one match per
 * state object, and finding matches per scene. It integrates with the action lifecycle management
 * to handle iterative searches and exit conditions.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Multiple find strategies (ALL, BEST, EACH, EACH_SCENE)
 *   <li>Support for defined regions to constrain searches
 *   <li>Integration with scene analysis for offline processing
 *   <li>Lifecycle management for iterative searches (e.g., wait until vanish)
 * </ul>
 *
 * @see PatternFindOptions
 * @see IterativePatternFinder
 * @see ActionLifecycleManagement
 * @see DefinedRegionConverter
 */
@Slf4j
@Component
public class FindImage {

    private final DefinedRegionConverter useDefinedRegion;
    private final ActionLifecycleManagement actionLifecycleManagement;
    private final SceneProvider getScenes;
    private final IterativePatternFinder findPatternsIteration;

    public FindImage(
            DefinedRegionConverter useDefinedRegion,
            ActionLifecycleManagement actionLifecycleManagement,
            SceneProvider getScenes,
            IterativePatternFinder findPatternsIteration) {
        this.useDefinedRegion = useDefinedRegion;
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.getScenes = getScenes;
        this.findPatternsIteration = findPatternsIteration;
    }

    /**
     * Finds all matches for the provided images without filtering.
     *
     * <p>This method searches for all occurrences of the target images and returns every match
     * found, regardless of score or position.
     *
     * @param matches The ActionResult to populate with all found matches. This object is modified
     *     by adding all discovered matches.
     * @param objectCollections Collections containing the images to search for
     */
    void findAll(ActionResult matches, List<ObjectCollection> objectCollections) {
        getImageMatches(matches, objectCollections);
    }

    /**
     * Finds only the single best match across all images.
     *
     * <p>This method searches for all matches but returns only the one with the highest similarity
     * score. Useful when you need to interact with the most likely match among multiple
     * possibilities.
     *
     * @param matches The ActionResult to populate with the best match. The match list is replaced
     *     with a single-element list containing only the best match.
     * @param objectCollections Collections containing the images to search for
     */
    void findBest(ActionResult matches, List<ObjectCollection> objectCollections) {
        getImageMatches(matches, objectCollections);
        matches.getBestMatch().ifPresent(match -> matches.setMatchList(List.of(match)));
    }

    /**
     * Finds the best match for each unique state object.
     *
     * <p>This method ensures that each state image gets at most one match - the one with the
     * highest score. This is useful when you have multiple images and want to find the best
     * instance of each, rather than all instances or just the single best overall.
     *
     * @param matches The ActionResult to populate with the best match per state object. The match
     *     list is replaced with the filtered results.
     * @param objectCollections Collections containing the images to search for
     */
    void findEachStateObject(ActionResult matches, List<ObjectCollection> objectCollections) {
        getImageMatches(matches, objectCollections);
        List<Match> bestMatchPerStateObject = new ArrayList<>();
        Set<String> imageIds = matches.getUniqueImageIds();
        for (String id : imageIds) {
            List<Match> singleObjectMatchList = matches.getMatchObjectsWithTargetStateObject(id);
            Optional<Match> matchWithHighestScore =
                    singleObjectMatchList.stream()
                            .max(java.util.Comparator.comparingDouble(Match::getScore));
            matchWithHighestScore.ifPresent(bestMatchPerStateObject::add);
        }
        matches.setMatchList(bestMatchPerStateObject);
    }

    /**
     * Finds the best match per scene when analyzing multiple screenshots.
     *
     * <p>This method is useful for scene-based analysis where you want to find the best match in
     * each scene separately. The final result contains one match per scene, each being the
     * highest-scoring match within that scene.
     *
     * @param matches The ActionResult to populate with the best match from each scene. The match
     *     list is replaced with the per-scene results.
     * @param objectCollections Collections containing the images and scenes to analyze
     */
    void findEachScene(ActionResult matches, List<ObjectCollection> objectCollections) {
        getImageMatches(matches, objectCollections);
        matches.setMatchList(new ArrayList<>());
        matches.getSceneAnalysisCollection()
                .getSceneAnalyses()
                .forEach(
                        sceneAnalysis -> {
                            sceneAnalysis.getMatchList().stream()
                                    .max(Comparator.comparingDouble(Match::getScore))
                                    .ifPresent(matches::add);
                        });
    }

    /**
     * Core method that performs the actual image matching with lifecycle management.
     *
     * <p>This method handles the iterative search process, respecting action options such as
     * repetitions, wait times, and exit conditions. It supports both standard searches and searches
     * using defined regions. The search continues until the lifecycle management determines the
     * exit condition is met.
     *
     * <p>Special behaviors:
     *
     * <ul>
     *   <li>Returns early if no images are provided
     *   <li>Uses defined regions if specified in action options
     *   <li>Continues searching based on lifecycle conditions (e.g., VANISH waits until images
     *       disappear)
     *   <li>Increments repetition count after each search iteration
     * </ul>
     *
     * @param matches Contains ActionConfig and accumulates all matches found. This object is
     *     modified throughout the search process.
     * @param objectCollections Collections containing the images to search for
     */
    void getImageMatches(ActionResult matches, List<ObjectCollection> objectCollections) {
        if (objectCollections.isEmpty()) return; // no images to search for

        ActionConfig actionConfig = matches.getActionConfig();

        // Check if we should use defined region based on config
        if (actionConfig != null && actionConfig instanceof PatternFindOptions) {
            PatternFindOptions findOptions = (PatternFindOptions) actionConfig;
            if (findOptions.isUseDefinedRegion()) {
                matches.addAllResults(
                        useDefinedRegion.useRegion(matches, objectCollections.get(0)));
                return;
            }
        }
        /*
         * Execute the find until the exit condition is achieved. For example, a
         * Find.VANISH will execute until
         * the images are no longer found. The results for each execution are added to
         * the Matches object.
         */
        List<StateImage> stateImages = objectCollections.getFirst().getStateImages();
        log.debug("[FIND_IMAGE] Starting find operation with {} state images", stateImages.size());

        // Add safety check to prevent infinite loops
        int maxIterations = FrameworkSettings.mock ? 1 : 100; // In mock mode, only iterate once
        int iterations = 0;

        // Critical fix: Check mock mode BEFORE entering loop
        if (FrameworkSettings.mock) {
            log.debug("[FIND_IMAGE] Mock mode detected, limiting to single iteration");
        }

        while (iterations < maxIterations
                && actionLifecycleManagement.isOkToContinueAction(matches, stateImages.size())) {
            List<Scene> scenes;
            // Use ActionConfig if available, otherwise use default
            if (actionConfig != null) {
                scenes = getScenes.getScenes(actionConfig, objectCollections, 1, 0);
            } else {
                // Use default PatternFindOptions if no config provided
                PatternFindOptions defaultOptions = new PatternFindOptions.Builder().build();
                scenes = getScenes.getScenes(defaultOptions, objectCollections, 1, 0);
            }
            log.debug("[FIND_IMAGE] Got {} scenes from SceneProvider", scenes.size());

            if (scenes.isEmpty()) {
                log.warn("[FIND_IMAGE] No scenes available for illustration!");
            }

            findPatternsIteration.find(matches, stateImages, scenes);
            actionLifecycleManagement.incrementCompletedRepetitions(matches);
            iterations++;

            // Additional safety check for mock mode - break immediately after first
            // iteration
            if (FrameworkSettings.mock) {
                log.debug("[FIND_IMAGE] Mock mode - breaking after iteration {}", iterations);
                break;
            }
        }

        if (iterations >= maxIterations) {
            log.warn(
                    "[FIND_IMAGE] Find operation reached maximum iterations limit ({})",
                    maxIterations);
        }

        log.debug(
                "[FIND_IMAGE] Find operation complete. SceneAnalysisCollection size: {}",
                matches.getSceneAnalysisCollection().getSceneAnalyses().size());
    }
}
