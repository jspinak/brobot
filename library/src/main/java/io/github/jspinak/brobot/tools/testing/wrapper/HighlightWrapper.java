package io.github.jspinak.brobot.tools.testing.wrapper;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.config.FrameworkSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Wrapper for highlight operations that breaks circular dependency with Action.
 * 
 * <p>This wrapper provides a stable interface for highlight operations while preventing
 * circular dependencies between HighlightManager and Action. The wrapper pattern allows
 * HighlightManager to depend on this wrapper instead of directly on Action, breaking
 * the circular dependency chain.</p>
 * 
 * <h2>Circular Dependency Resolution:</h2>
 * <p>Original circular dependency chain:
 * HighlightManager → Action → ActionExecution → ... → HighlightManager
 * 
 * With wrapper pattern:
 * HighlightManager → HighlightWrapper
 * Action → ActionExecution → ... → HighlightManager
 * HighlightWrapper → Action (no back reference)</p>
 * 
 * <h2>Design Benefits:</h2>
 * <ul>
 *   <li>Eliminates need for @Lazy annotation</li>
 *   <li>All dependencies resolved at startup</li>
 *   <li>Clear separation of concerns</li>
 *   <li>Testable in isolation</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.tools.logging.visual.HighlightManager
 * @see Action
 * @since 1.0
 */
@Slf4j
@Component
public class HighlightWrapper {
    
    private final Action action;
    
    /**
     * Constructs a HighlightWrapper with the Action dependency.
     * 
     * Since this wrapper doesn't participate in any circular dependency chains,
     * it can safely depend on Action without needing @Lazy annotation.
     * 
     * @param action the Action instance for performing highlight operations
     */
    @Autowired
    public HighlightWrapper(Action action) {
        this.action = action;
    }
    
    /**
     * Performs a highlight action with the given options and regions.
     * 
     * <p>This method wraps the Action.perform call for highlight operations,
     * providing a stable API that HighlightManager can depend on without
     * creating circular dependencies.</p>
     * 
     * <h3>Mock Mode Behavior:</h3>
     * <p>In mock mode (when FrameworkSettings.mock is true), highlight operations
     * are simulated without actual screen interaction. The method returns a
     * successful ActionResult to maintain consistent behavior in tests.</p>
     * 
     * <h3>Live Mode Behavior:</h3>
     * <p>In live mode, this method performs actual screen highlighting using
     * the provided options and regions. The highlight will be visible on screen
     * for the specified duration.</p>
     * 
     * @param highlightOptions configuration for the highlight (color, duration, etc.)
     * @param objectCollection collection containing regions to highlight
     * @return ActionResult indicating success/failure of the highlight operation
     */
    public ActionResult performHighlight(HighlightOptions highlightOptions, ObjectCollection objectCollection) {
        if (FrameworkSettings.mock) {
            log.debug("Mock mode: Simulating highlight for regions");
            // In mock mode, return success without actual highlighting
            ActionResult mockResult = new ActionResult();
            mockResult.setSuccess(true);
            return mockResult;
        }
        
        log.debug("Performing highlight action for regions");
        return action.perform(highlightOptions, objectCollection);
    }
    
    /**
     * Checks if the Action component is available for highlighting.
     * 
     * <p>This method allows HighlightManager to verify that highlighting
     * capabilities are available before attempting to use them.</p>
     * 
     * @return true if Action is available, false otherwise
     */
    public boolean isAvailable() {
        return action != null;
    }
    
    /**
     * Gets a string representation of the wrapper state for debugging.
     * 
     * @return string describing the wrapper state
     */
    @Override
    public String toString() {
        return String.format("HighlightWrapper[actionAvailable=%s, mockMode=%s]",
            isAvailable(), FrameworkSettings.mock);
    }
}