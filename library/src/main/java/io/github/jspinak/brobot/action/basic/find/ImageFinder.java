package io.github.jspinak.brobot.action.basic.find;

import java.util.*;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.color.SceneProvider;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.action.internal.find.DefinedRegionConverter;
import io.github.jspinak.brobot.action.internal.find.IterativePatternFinder;

/**
 * V2 component for finding images on screen using various search strategies.
 *
 * <p>This V2 class works with BaseFindOptions instead of ActionConfig. It implements the primary
 * image matching functionality in Brobot, supporting multiple find strategies including finding all
 * matches, finding the best match, finding one match per state object, and finding matches per
 * scene. It integrates with the action lifecycle management to handle iterative searches and exit
 * conditions.
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
 * @see FindStrategy
 * @see IterativePatternFinder
 * @see ActionLifecycleManagement
 * @see DefinedRegionConverter
 */
@Component
public class ImageFinder {

    private final DefinedRegionConverter useDefinedRegion;
    private final ActionLifecycleManagement actionLifecycleManagement;
    private final SceneProvider getScenes;
    private final IterativePatternFinder findPatternsIteration;
    private final FindImage legacyFindImage;

    public ImageFinder(
            DefinedRegionConverter useDefinedRegion,
            ActionLifecycleManagement actionLifecycleManagement,
            SceneProvider getScenes,
            IterativePatternFinder findPatternsIteration,
            FindImage legacyFindImage) {
        this.useDefinedRegion = useDefinedRegion;
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.getScenes = getScenes;
        this.findPatternsIteration = findPatternsIteration;
        this.legacyFindImage = legacyFindImage;
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
        // For now, delegate to legacy implementation
        // TODO: Update when ActionResult is migrated to use ActionConfig instead of ActionConfig
        legacyFindImage.findAll(matches, objectCollections);
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
        // For now, delegate to legacy implementation
        legacyFindImage.findBest(matches, objectCollections);
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
        // For now, delegate to legacy implementation
        legacyFindImage.findEachStateObject(matches, objectCollections);
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
        // For now, delegate to legacy implementation
        legacyFindImage.findEachScene(matches, objectCollections);
    }
}
