package io.github.jspinak.brobot.action.basic.find;
import io.github.jspinak.brobot.action.ActionType;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
     * <p>This method serves as a facade, delegating the entire find process to the 
     * FindPipeline. The pipeline handles all orchestration including pattern matching,
     * state management, match fusion, and post-processing adjustments.</p>
     * 
     * <p>When called directly (rather than through Action.perform), certain lifecycle 
     * operations are bypassed to avoid redundant processing:
     * <ul>
     *   <li>Wait.pauseBeforeBegin - Pre-action delays</li>
     *   <li>Matches.setSuccess - Success flag setting</li>
     *   <li>Matches.setDuration - Timing measurements</li>
     *   <li>Matches.saveSnapshots - Screenshot capturing</li>
     *   <li>Wait.pauseAfterEnd - Post-action delays</li>
     * </ul>
     * </p>
     * 
     * @param matches The ActionResult to populate with found matches. Must contain
     *                valid BaseFindOptions configuration.
     * @param objectCollections The collections containing patterns, regions, and other
     *                         objects to find. At least one collection must be provided.
     * @throws IllegalArgumentException if matches does not contain BaseFindOptions configuration
     * @see FindPipeline#execute(BaseFindOptions, ActionResult, ObjectCollection...)
     */
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        log.info("=== Find.perform() CALLED ===");
        log.info("  ObjectCollections: {}", objectCollections.length);
        log.info("  ActionConfig class: {}", 
                matches.getActionConfig() != null ? matches.getActionConfig().getClass().getName() : "null");
        
        // Validate configuration
        if (!(matches.getActionConfig() instanceof BaseFindOptions)) {
            log.error("Find requires BaseFindOptions configuration, got: {}", 
                    matches.getActionConfig() != null ? matches.getActionConfig().getClass() : "null");
            throw new IllegalArgumentException("Find requires BaseFindOptions configuration");
        }
        
        BaseFindOptions findOptions = (BaseFindOptions) matches.getActionConfig();
        log.info("  FindOptions strategy: {}", findOptions.getFindStrategy());
        
        // Delegate entire orchestration to the pipeline
        log.info("  Calling findPipeline.execute()...");
        findPipeline.execute(findOptions, matches, objectCollections);
        log.info("  findPipeline.execute() completed with {} matches", matches.size());
    }

}