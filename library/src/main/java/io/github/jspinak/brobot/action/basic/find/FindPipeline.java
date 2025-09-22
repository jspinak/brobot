package io.github.jspinak.brobot.action.basic.find;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.find.NonImageObjectConverter;
import io.github.jspinak.brobot.action.internal.find.OffsetMatchCreator;
import io.github.jspinak.brobot.action.internal.find.match.MatchContentExtractor;
import io.github.jspinak.brobot.action.internal.find.match.MatchRegionAdjuster;
import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import io.github.jspinak.brobot.action.internal.utility.ActionSuccessCriteria;
import io.github.jspinak.brobot.analysis.color.profiles.ProfileSetBuilder;
import io.github.jspinak.brobot.analysis.match.MatchFusion;
import io.github.jspinak.brobot.logging.ConciseFindLogger;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.statemanagement.StateMemoryUpdater;
// Removed old visual logging imports that no longer exist:// 
// import io.github.jspinak.brobot.tools.logging.visual.HighlightManager; // HighlightManager removed
// import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import io.github.jspinak.brobot.util.string.TextSelector;

import lombok.extern.slf4j.Slf4j;

/**
 * Encapsulates the Find operation pipeline, orchestrating all steps of the pattern matching
 * process.
 *
 * <p>This class implements a clear separation of concerns by organizing the Find operation into
 * three distinct phases:
 *
 * <ul>
 *   <li><b>Pre-processing</b>: Color profile creation and initial offset setup
 *   <li><b>Strategy execution</b>: Running the selected find strategy
 *   <li><b>Post-processing</b>: State management, match fusion, adjustments, and text extraction
 * </ul>
 *
 * <p>By extracting the orchestration logic from the Find class, this pipeline:
 *
 * <ul>
 *   <li>Makes the find process more testable and maintainable
 *   <li>Provides clear extension points for customization
 *   <li>Enables easier debugging and monitoring of each phase
 *   <li>Allows for future pipeline variations without changing the Find facade
 * </ul>
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
    // Removed old visual logging dependencies that no longer exist:
  //  //  // private final HighlightManager highlightManager; // HighlightManager removed // highlightManager removed
    // private final VisualFeedbackConfig visualFeedbackConfig;
    private final ModernFindStrategyRegistry findStrategyRegistry;
    private final ActionSuccessCriteria actionSuccessCriteria;
    private final ConciseFindLogger conciseFindLogger;
    private final StateMemoryUpdater stateMemoryUpdater;

    @Value("${brobot.highlighting.enabled:false}")
    private boolean highlightEnabled;

    @Autowired
    public FindPipeline(
            ProfileSetBuilder profileSetBuilder,
            OffsetMatchCreator offsetLocationManager,
            MatchFusion matchFusion,
            MatchRegionAdjuster matchAdjuster,
            MatchContentExtractor contentExtractor,
            NonImageObjectConverter nonImageObjectConverter,
            StateMemory stateMemory,
            TextSelector textSelector,
            DynamicRegionResolver dynamicRegionResolver,
            // Removed missing parameters:
          //  //  // HighlightManager highlightManager, // HighlightManager removed // highlightManager removed
            // VisualFeedbackConfig visualFeedbackConfig,
            ModernFindStrategyRegistry findStrategyRegistry,
            ActionSuccessCriteria actionSuccessCriteria,
            StateMemoryUpdater stateMemoryUpdater,
            @Autowired(required = false) ConciseFindLogger conciseFindLogger) {
        this.profileSetBuilder = profileSetBuilder;
        this.offsetLocationManager = offsetLocationManager;
        this.matchFusion = matchFusion;
        this.matchAdjuster = matchAdjuster;
        this.contentExtractor = contentExtractor;
        this.nonImageObjectConverter = nonImageObjectConverter;
        this.stateMemory = stateMemory;
        this.textSelector = textSelector;
        this.dynamicRegionResolver = dynamicRegionResolver;
        // Removed initialization of missing classes:
       //  // this.highlightManager = highlightManager; // highlightManager removed
        // this.visualFeedbackConfig = visualFeedbackConfig;
        this.findStrategyRegistry = findStrategyRegistry;
        this.actionSuccessCriteria = actionSuccessCriteria;
        this.stateMemoryUpdater = stateMemoryUpdater;
        this.conciseFindLogger = conciseFindLogger;
    }

    /**
     * Updates search regions for objects that have cross-state search region configurations.
     *
     * <p>This method processes all state objects in the collections and updates their search
     * regions based on matches from other state objects, enabling dynamic search area definition.
     */
    private void updateCrossStateSearchRegions(
            ActionResult matches, ObjectCollection... collections) {
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
     * Orders StateImages based on dependencies to ensure that images that other images depend on
     * are searched first. This enables proper declarative region resolution.
     *
     * @param stateImages The list of state images to order
     * @return A new list with images ordered by dependencies
     */
    private List<StateImage> orderByDependencies(List<StateImage> stateImages) {
        log.info("[ORDERING] Ordering {} StateImages by dependencies", stateImages.size());

        // Create a map to track which images are depended upon
        Map<String, List<StateImage>> dependencyMap = new HashMap<>();
        List<StateImage> noDependencies = new ArrayList<>();
        List<StateImage> withDependencies = new ArrayList<>();

        // First pass: categorize images and build dependency map
        for (StateImage image : stateImages) {
            if (image.getSearchRegionOnObject() != null) {
                withDependencies.add(image);
                String targetKey = image.getSearchRegionOnObject().getTargetObjectName();
                log.info("[ORDERING]   {} depends on {}", image.getName(), targetKey);
            } else {
                noDependencies.add(image);
                log.info("[ORDERING]   {} has no dependencies", image.getName());
            }
        }

        // Build a set of all target names that are dependencies
        Set<String> targetNames = new HashSet<>();
        for (StateImage dependent : withDependencies) {
            targetNames.add(dependent.getSearchRegionOnObject().getTargetObjectName());
        }

        // Now separate the no-dependency list into targets and non-targets
        List<StateImage> targets = new ArrayList<>();
        List<StateImage> nonTargets = new ArrayList<>();

        for (StateImage image : noDependencies) {
            if (targetNames.contains(image.getName())) {
                targets.add(image);
                log.info(
                        "[ORDERING]   {} is a TARGET (other images depend on it)", image.getName());
            } else {
                nonTargets.add(image);
            }
        }

        // Build the ordered list:
        // 1. First add targets (images that others depend on)
        // 2. Then add non-targets without dependencies
        // 3. Finally add images with dependencies
        List<StateImage> ordered = new ArrayList<>();
        ordered.addAll(targets); // These must be found first
        ordered.addAll(nonTargets); // These can be found anytime
        ordered.addAll(withDependencies); // These need their dependencies found first

        log.info("[ORDERING] Final order:");
        for (int i = 0; i < ordered.size(); i++) {
            StateImage img = ordered.get(i);
            String type =
                    targets.contains(img)
                            ? "TARGET"
                            : withDependencies.contains(img) ? "DEPENDENT" : "INDEPENDENT";
            log.info("[ORDERING]   [{}] {} ({})", i, img.getName(), type);
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
    public void execute(
            BaseFindOptions findOptions,
            ActionResult matches,
            ObjectCollection... objectCollections) {
        log.info("FindPipeline.execute() called with {} collections", objectCollections.length);

        // Start a new find session for concise logging
        String sessionId = "find-" + System.currentTimeMillis();
        if (conciseFindLogger != null) {
            conciseFindLogger.startSearchSession(sessionId);
        }

        // CRITICAL: Order StateImages by dependencies BEFORE searching
        // This ensures that images without dependencies are searched first,
        // and their locations can be used to constrain searches for dependent images
        for (ObjectCollection collection : objectCollections) {
            List<StateImage> originalOrder = new ArrayList<>(collection.getStateImages());
            List<StateImage> orderedImages = orderByDependencies(originalOrder);

            // Replace the collection's state images with the ordered list
            collection.getStateImages().clear();
            collection.getStateImages().addAll(orderedImages);

            log.debug(
                    "Reordered {} StateImages in collection for dependency resolution",
                    orderedImages.size());
        }

        // Note: Dependencies should be registered when states are built, not here
        // For now, we'll check if search regions need updating based on previous matches
        log.info("[PIPELINE_DEBUG] About to call updateCrossStateSearchRegions");
        log.info("[PIPELINE_DEBUG]   - Current matches size: {}", matches.size());
        log.info("[PIPELINE_DEBUG]   - Collections count: {}", objectCollections.length);
        for (ObjectCollection collection : objectCollections) {
            log.info(
                    "[PIPELINE_DEBUG]   - Collection has {} StateImages",
                    collection.getStateImages().size());
            for (StateImage img : collection.getStateImages()) {
                log.info(
                        "[PIPELINE_DEBUG]     - StateImage: {}, SearchRegionOnObject: {}",
                        img.getName(),
                        img.getSearchRegionOnObject() != null);
                if (img.getSearchRegionOnObject() != null) {
                    log.info(
                            "[PIPELINE_DEBUG]       -> Depends on: {}.{}",
                            img.getSearchRegionOnObject().getTargetStateName(),
                            img.getSearchRegionOnObject().getTargetObjectName());
                }
            }
        }
        updateCrossStateSearchRegions(matches, objectCollections);

        // Highlight search regions if enabled - DISABLED: visual logging classes removed
        // if (shouldHighlightSearchRegions()) {
        //     highlightSearchRegions(objectCollections);
        // }

        // Convert non-image objects and delegate to find strategies
        ActionResult nonImageMatches =
                nonImageObjectConverter.getOtherObjectsDirectlyAsMatchObjects(objectCollections[0]);
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

        // Save matches to their corresponding StateImages for future dependency resolution
        log.info("About to call saveMatchesToStateImages with {} matches", matches.size());
        saveMatchesToStateImages(matches, objectCollections);
        log.info("Finished saveMatchesToStateImages");

        // Update StateMemory based on found matches - when an image is found,
        // the state it belongs to should be set as active
        if (!matches.isEmpty()) {
            stateMemoryUpdater.updateFromActionResult(matches);
        }

        // Update search regions for objects that depend on what we just found
        if (!matches.isEmpty()) {
            log.debug(
                    "FindPipeline: Found {} matches, updating dependent search regions",
                    matches.size());
            dynamicRegionResolver.updateDependentSearchRegions(matches);
        } else {
            log.debug("FindPipeline: No matches found, skipping dependent search region update");
        }

        // Highlight found matches if enabled - DISABLED: visual logging classes removed
        // if (shouldHighlightFinds() && !matches.isEmpty()) {
       //  //     highlightManager.highlightMatches(matches.getMatchList()); // highlightManager removed
        // }

        // Set success criteria based on the action configuration
        // This ensures success is evaluated before the ActionLifecycleAspect logs the result
        if (matches.getActionConfig() != null) {
            actionSuccessCriteria.set(matches.getActionConfig(), matches);
        }

        // End the find session for concise logging
        if (conciseFindLogger != null) {
            conciseFindLogger.endSearchSession(sessionId, matches.isSuccess(), matches.size());
        }
    }

    /** Checks if search regions should be highlighted based on configuration. */
    private boolean shouldHighlightSearchRegions() {
        // Visual feedback functionality disabled - missing classes
        return false;
        // return highlightEnabled
       //  //         && highlightManager != null // highlightManager removed
        //         && visualFeedbackConfig != null
        //         && visualFeedbackConfig.isEnabled()
        //         && visualFeedbackConfig.isAutoHighlightSearchRegions();
    }

    /** Checks if found matches should be highlighted based on configuration. */
    private boolean shouldHighlightFinds() {
        // Visual feedback functionality disabled - missing classes
        return false;
        // return highlightEnabled
       //  //         && highlightManager != null // highlightManager removed
        //         && visualFeedbackConfig != null
        //         && visualFeedbackConfig.isEnabled()
        //         && visualFeedbackConfig.isAutoHighlightFinds();
    }

    /**
     * Highlights the search regions for all objects in the collections.
     *
     * @param collections The object collections containing search regions to highlight
     */
    private void highlightSearchRegions(ObjectCollection... collections) {
       //  // HighlightManager removed - visual highlighting temporarily disabled // HighlightManager removed
        // This functionality will be restored when the visualization system is refactored
    }

    /**
     * Saves matches to their corresponding StateImages' lastMatchesFound field. This is the single
     * source of truth for where each StateImage was last found, used by DynamicRegionResolver for
     * SearchRegionOnObject dependencies.
     *
     * @param matches The action result containing all matches
     * @param collections The object collections that were searched
     */
    private void saveMatchesToStateImages(ActionResult matches, ObjectCollection... collections) {
        log.info(
                "[SAVE_MATCHES] saveMatchesToStateImages called with {} matches and {} collections",
                matches.getMatchList().size(),
                collections.length);

        // Log details about what we're processing
        log.info("[SAVE_MATCHES] Match details:");
        for (Match match : matches.getMatchList()) {
            if (match.getStateObjectData() != null) {
                log.info(
                        "[SAVE_MATCHES]   - Match has StateObjectData: name={}, ownerState={}",
                        match.getStateObjectData().getStateObjectName(),
                        match.getStateObjectData().getOwnerStateName());
            } else {
                log.info("[SAVE_MATCHES]   - Match has NO StateObjectData!");
            }
        }

        if (matches.getMatchList().isEmpty()) {
            log.info("No matches to save, clearing lastMatchesFound for all StateImages");
            // Clear lastMatchesFound for all StateImages that didn't match
            for (ObjectCollection collection : collections) {
                for (StateImage stateImage : collection.getStateImages()) {
                    boolean hasMatch =
                            matches.getMatchList().stream()
                                    .anyMatch(
                                            m ->
                                                    m.getStateObjectData() != null
                                                            && stateImage
                                                                    .getName()
                                                                    .equals(
                                                                            m.getStateObjectData()
                                                                                    .getStateObjectName()));
                    if (!hasMatch) {
                        stateImage.getLastMatchesFound().clear();
                        log.debug(
                                "Cleared lastMatchesFound for '{}' (no matches)",
                                stateImage.getName());
                    }
                }
            }
            return;
        }

        // Group matches by StateImage name
        Map<String, List<Match>> matchesByStateImage = new HashMap<>();
        for (Match match : matches.getMatchList()) {
            if (match.getStateObjectData() != null
                    && match.getStateObjectData().getStateObjectName() != null) {
                String imageName = match.getStateObjectData().getStateObjectName();
                matchesByStateImage.computeIfAbsent(imageName, k -> new ArrayList<>()).add(match);
            }
        }

        // Update each StateImage with its matches
        log.info(
                "[SAVE_MATCHES] Processing {} collections to update StateImages...",
                collections.length);
        for (ObjectCollection collection : collections) {
            log.info(
                    "[SAVE_MATCHES] Collection has {} StateImages",
                    collection.getStateImages().size());
            for (StateImage stateImage : collection.getStateImages()) {
                log.info(
                        "[SAVE_MATCHES] Checking StateImage '{}' (instance: {})",
                        stateImage.getName(),
                        System.identityHashCode(stateImage));
                List<Match> imageMatches = matchesByStateImage.get(stateImage.getName());
                if (imageMatches != null && !imageMatches.isEmpty()) {
                    // Replace the entire list with new matches
                    log.info(
                            "[SAVE_MATCHES] Found {} matches for '{}', updating"
                                    + " lastMatchesFound...",
                            imageMatches.size(),
                            stateImage.getName());
                    stateImage.getLastMatchesFound().clear();
                    stateImage.getLastMatchesFound().addAll(imageMatches);
                    log.info(
                            "[SAVE_MATCHES] ✓ SAVED {} matches to '{}' lastMatchesFound (StateImage"
                                    + " instance: {})",
                            imageMatches.size(),
                            stateImage.getName(),
                            System.identityHashCode(stateImage));
                } else {
                    // No matches for this image, clear its lastMatchesFound
                    log.info(
                            "[SAVE_MATCHES] No matches found for '{}', clearing lastMatchesFound",
                            stateImage.getName());
                    stateImage.getLastMatchesFound().clear();
                    log.info(
                            "[SAVE_MATCHES] Cleared lastMatchesFound for '{}' (searched but not"
                                    + " found, StateImage instance: {})",
                            stateImage.getName(),
                            System.identityHashCode(stateImage));
                }
            }
        }
    }
}
