package io.github.jspinak.brobot.action.basic.find;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.extern.slf4j.Slf4j;

/**
 * Core pattern matching action that locates GUI elements on the screen.
 *
 * <p>Find is the fundamental action in Brobot's visual GUI automation, implementing various pattern
 * matching strategies to locate GUI elements. It embodies the visual recognition capability that
 * enables the framework to interact with any GUI regardless of the underlying technology.
 *
 * <p>Find strategies supported:
 *
 * <ul>
 *   <li><b>FIRST</b>: Returns the first match found, optimized for speed
 *   <li><b>BEST</b>: Returns the highest-scoring match from all possibilities
 *   <li><b>EACH</b>: Returns one match per StateImage/Pattern
 *   <li><b>ALL</b>: Returns all matches found, useful for lists and grids
 *   <li><b>CUSTOM</b>: User-defined find strategies for special cases
 * </ul>
 *
 * <p>Advanced features:
 *
 * <ul>
 *   <li>Multi-pattern matching with StateImages containing multiple templates
 *   <li>Color-based matching using k-means profiles
 *   <li>Text extraction from matched regions (OCR integration)
 *   <li>Match fusion for combining overlapping results
 *   <li>Dynamic offset adjustments for precise targeting
 * </ul>
 *
 * <p>Find operations also handle non-image objects in ObjectCollections:
 *
 * <ul>
 *   <li>Existing Matches can be reused without re-searching
 *   <li>Regions are converted to matches for consistent handling
 *   <li>Locations provide direct targeting without pattern matching
 * </ul>
 *
 * <p>In the model-based approach, Find operations are context-aware through integration with
 * StateMemory, automatically adjusting active states based on what is found. This enables the
 * framework to maintain an accurate understanding of the current GUI state.
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
@Slf4j
public class Find implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.FIND;
    }

    private final FindPipeline findPipeline;

    public Find(FindPipeline findPipeline) {
        this.findPipeline = findPipeline;
    }

    /**
     * Executes the find operation to locate GUI elements on screen.
     *
     * <p>This method serves as a facade, delegating the entire find process to the FindPipeline.
     * The pipeline handles all orchestration including pattern matching, state management, match
     * fusion, and post-processing adjustments.
     *
     * <p>When called directly (rather than through Action.perform), certain lifecycle operations
     * are bypassed to avoid redundant processing:
     *
     * <ul>
     *   <li>Wait.pauseBeforeBegin - Pre-action delays
     *   <li>Matches.setSuccess - Success flag setting
     *   <li>Matches.setDuration - Timing measurements
     *   <li>Matches.saveSnapshots - Screenshot capturing
     *   <li>Wait.pauseAfterEnd - Post-action delays
     * </ul>
     *
     * @param matches The ActionResult to populate with found matches. Must contain valid
     *     BaseFindOptions configuration.
     * @param objectCollections The collections containing patterns, regions, and other objects to
     *     find. At least one collection must be provided.
     * @throws IllegalArgumentException if matches does not contain BaseFindOptions configuration
     * @see FindPipeline#execute(BaseFindOptions, ActionResult, ObjectCollection...)
     */
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        log.info("[FIND] === Find.perform() CALLED ===");

        // Handle null varargs
        if (objectCollections == null) {
            log.warn("[FIND] ObjectCollections is null");
            matches.setSuccess(false);
            return;
        }

        log.info("[FIND]   ObjectCollections: {}", objectCollections.length);
        if (objectCollections.length > 0
                && objectCollections[0] != null
                && !objectCollections[0].getStateImages().isEmpty()) {
            StateImage firstImage = objectCollections[0].getStateImages().get(0);
            log.info(
                    "[FIND]   First StateImage: name={}, instance={}",
                    firstImage.getName(),
                    System.identityHashCode(firstImage));
        }
        log.info(
                "[FIND]   ActionConfig class: {}",
                matches.getActionConfig() != null
                        ? matches.getActionConfig().getClass().getName()
                        : "null");

        // Validate configuration
        if (!(matches.getActionConfig() instanceof BaseFindOptions)) {
            log.error(
                    "Find requires BaseFindOptions configuration, got: {}",
                    matches.getActionConfig() != null
                            ? matches.getActionConfig().getClass()
                            : "null");
            throw new IllegalArgumentException("Find requires BaseFindOptions configuration");
        }

        BaseFindOptions findOptions = (BaseFindOptions) matches.getActionConfig();
        log.info("  FindOptions strategy: {}", findOptions.getFindStrategy());

        // Handle null or empty object collections
        if (objectCollections == null
                || objectCollections.length == 0
                || (objectCollections.length == 1 && objectCollections[0] == null)) {
            log.warn("[FIND] No valid object collections provided");
            matches.setSuccess(false);
            return;
        }

        // Delegate entire orchestration to the pipeline
        log.info("[FIND]   Calling findPipeline.execute()...");
        findPipeline.execute(findOptions, matches, objectCollections);
        log.info("[FIND]   findPipeline.execute() completed with {} matches", matches.size());

        // Debug: Check if matches have StateObjectData
        if (!matches.getMatchList().isEmpty()) {
            log.info(
                    "[FIND]   First match StateObjectData: {}",
                    matches.getMatchList().get(0).getStateObjectData() != null
                            ? matches.getMatchList()
                                    .get(0)
                                    .getStateObjectData()
                                    .getStateObjectName()
                            : "NULL");
        }

        // Update ActionMetrics with match information
        ActionResult.ActionMetrics metrics = matches.getActionMetrics();
        if (metrics == null) {
            metrics = new ActionResult.ActionMetrics();
            matches.setActionMetrics(metrics);
        }

        // Always update match count and best score
        metrics.setMatchCount(matches.size());

        // Set best match confidence if matches exist
        if (!matches.getMatchList().isEmpty()) {
            double bestScore =
                    matches.getMatchList().stream().mapToDouble(Match::getScore).max().orElse(0.0);
            metrics.setBestMatchConfidence(bestScore);
        } else {
            metrics.setBestMatchConfidence(0.0);
        }
    }
}
