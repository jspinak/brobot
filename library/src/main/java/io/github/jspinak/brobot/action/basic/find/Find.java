package io.github.jspinak.brobot.action.basic.find;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;
import io.github.jspinak.brobot.logging.events.ActionEvent;
import io.github.jspinak.brobot.model.element.Location;
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
    private final BrobotLogger brobotLogger;

    public Find(FindPipeline findPipeline, BrobotLogger brobotLogger) {
        this.findPipeline = findPipeline;
        this.brobotLogger = brobotLogger;
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
        // Start timing
        long startTime = System.currentTimeMillis();

        // Validate configuration
        if (!(matches.getActionConfig() instanceof BaseFindOptions)) {
            brobotLogger.error(
                    LogCategory.ACTIONS,
                    String.format(
                            "Find requires BaseFindOptions configuration, got: %s",
                            matches.getActionConfig() != null
                                    ? matches.getActionConfig().getClass()
                                    : "null"));
            throw new IllegalArgumentException("Find requires BaseFindOptions configuration");
        }

        BaseFindOptions findOptions = (BaseFindOptions) matches.getActionConfig();

        // Handle null or empty object collections
        if (objectCollections == null
                || objectCollections.length == 0
                || (objectCollections.length == 1 && objectCollections[0] == null)) {
            brobotLogger.warn(LogCategory.ACTIONS, "No valid object collections provided");
            matches.setSuccess(false);
            return;
        }

        // Extract target for logging
        String target = extractTarget(objectCollections);

        // Log action start at appropriate level
        if (brobotLogger.isLoggingEnabled(LogCategory.ACTIONS, LogLevel.DEBUG)) {
            brobotLogger.log(
                    LogCategory.ACTIONS,
                    LogLevel.DEBUG,
                    String.format("FIND %s (strategy: %s)", target, findOptions.getFindStrategy()));
        }

        // Delegate entire orchestration to the pipeline
        findPipeline.execute(findOptions, matches, objectCollections);

        // Calculate duration
        long duration = System.currentTimeMillis() - startTime;
        matches.setDuration(java.time.Duration.ofMillis(duration));

        // Match count and confidence are now tracked internally by ActionResult components

        // Create and log action event
        ActionEvent.ActionEventBuilder eventBuilder =
                ActionEvent.builder()
                        .actionType("FIND")
                        .target(target)
                        .success(matches.isSuccess())
                        .duration(matches.getDuration());

        if (matches.isSuccess() && !matches.getMatchList().isEmpty()) {
            Match firstMatch = matches.getMatchList().get(0);
            eventBuilder.location(
                    new Location(firstMatch.getTarget().getX(), firstMatch.getTarget().getY()));
            eventBuilder.similarity(firstMatch.getScore());
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("matches_found", matches.getMatchList().size());
            eventBuilder.metadata(metadata);
        }

        ActionEvent event = eventBuilder.build();
        brobotLogger.logAction(event);
    }

    /** Extracts target description from object collections for logging. */
    private String extractTarget(ObjectCollection... collections) {
        if (collections == null || collections.length == 0) {
            return "unknown";
        }

        ObjectCollection first = collections[0];
        if (!first.getStateImages().isEmpty()) {
            return first.getStateImages().get(0).getName();
        } else if (!first.getStateLocations().isEmpty()) {
            return "location:" + first.getStateLocations().get(0).toString();
        } else if (!first.getStateRegions().isEmpty()) {
            return "region:" + first.getStateRegions().get(0).toString();
        }

        return "unknown";
    }
}
