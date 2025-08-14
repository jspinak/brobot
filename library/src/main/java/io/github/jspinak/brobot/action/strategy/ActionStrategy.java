package io.github.jspinak.brobot.action.strategy;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;

/**
 * Strategy interface for action execution.
 * Implementations can be profile-specific (mock, live, etc.)
 */
public interface ActionStrategy {
    
    /**
     * Execute an action with the given configuration and targets.
     * 
     * @param actionOptions The action configuration options
     * @param targets The target objects
     * @return The result of the action execution
     */
    ActionResult execute(ActionOptions actionOptions, ObjectCollection targets);
    
    /**
     * Check if this strategy can handle the given action configuration.
     * 
     * @param actionOptions The action configuration options
     * @return true if this strategy can handle the action
     */
    boolean canHandle(ActionOptions actionOptions);
    
    /**
     * Get the name of this strategy for logging/debugging.
     * 
     * @return The strategy name
     */
    String getName();
}