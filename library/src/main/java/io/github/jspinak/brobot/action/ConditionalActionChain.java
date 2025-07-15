package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A fluent API for building action chains with conditional execution support.
 * <p>
 * ConditionalActionChain extends the existing ActionChain functionality by adding
 * conditional execution patterns like ifFound(), ifNotFound(), and always().
 * This enables more flexible action composition where subsequent actions can be
 * executed based on the success or failure of previous actions.
 * </p>
 * 
 * <p>Example usage:
 * <pre>{@code
 * ConditionalActionChain.find(findOptions)
 *     .ifFound(click())
 *     .ifNotFound(log("Element not found"))
 *     .always(takeScreenshot())
 *     .perform(objectCollection);
 * }</pre>
 * </p>
 * 
 * @since 2.0
 */
public class ConditionalActionChain {
    
    /**
     * Represents a conditional action in the chain.
     */
    private static class ConditionalAction {
        final ActionConfig action;
        final Condition condition;
        final Supplier<String> logMessage;
        final Consumer<ActionResult> customHandler;
        
        ConditionalAction(ActionConfig action, Condition condition) {
            this(action, condition, null, null);
        }
        
        ConditionalAction(ActionConfig action, Condition condition, 
                         Supplier<String> logMessage, Consumer<ActionResult> customHandler) {
            this.action = action;
            this.condition = condition;
            this.logMessage = logMessage;
            this.customHandler = customHandler;
        }
    }
    
    /**
     * Conditions for executing actions in the chain.
     */
    private enum Condition {
        ALWAYS,      // Execute regardless of previous result
        IF_FOUND,    // Execute only if previous action succeeded
        IF_NOT_FOUND // Execute only if previous action failed
    }
    
    private final List<ConditionalAction> actions = new ArrayList<>();
    private ActionResult previousResult;
    
    /**
     * Private constructor to enforce fluent API usage.
     */
    private ConditionalActionChain() {}
    
    /**
     * Creates a new ConditionalActionChain starting with a Find action.
     * 
     * @param findOptions the find configuration
     * @return a new ConditionalActionChain instance
     */
    public static ConditionalActionChain find(PatternFindOptions findOptions) {
        ConditionalActionChain chain = new ConditionalActionChain();
        chain.actions.add(new ConditionalAction(findOptions, Condition.ALWAYS));
        return chain;
    }
    
    /**
     * Creates a new ConditionalActionChain starting with any action.
     * 
     * @param action the initial action configuration
     * @return a new ConditionalActionChain instance
     */
    public static ConditionalActionChain start(ActionConfig action) {
        ConditionalActionChain chain = new ConditionalActionChain();
        chain.actions.add(new ConditionalAction(action, Condition.ALWAYS));
        return chain;
    }
    
    /**
     * Adds an action that executes only if the previous action succeeded.
     * 
     * @param action the action to execute if found
     * @return this chain for fluent API
     */
    public ConditionalActionChain ifFound(ActionConfig action) {
        actions.add(new ConditionalAction(action, Condition.IF_FOUND));
        return this;
    }
    
    /**
     * Adds an action that executes only if the previous action failed.
     * 
     * @param action the action to execute if not found
     * @return this chain for fluent API
     */
    public ConditionalActionChain ifNotFound(ActionConfig action) {
        actions.add(new ConditionalAction(action, Condition.IF_NOT_FOUND));
        return this;
    }
    
    /**
     * Adds an action that executes regardless of the previous result.
     * 
     * @param action the action to always execute
     * @return this chain for fluent API
     */
    public ConditionalActionChain always(ActionConfig action) {
        actions.add(new ConditionalAction(action, Condition.ALWAYS));
        return this;
    }
    
    /**
     * Adds a log message that executes if the previous action succeeded.
     * 
     * @param message the message to log
     * @return this chain for fluent API
     */
    public ConditionalActionChain ifFoundLog(String message) {
        actions.add(new ConditionalAction(null, Condition.IF_FOUND, () -> message, null));
        return this;
    }
    
    /**
     * Adds a log message that executes if the previous action failed.
     * 
     * @param message the message to log
     * @return this chain for fluent API
     */
    public ConditionalActionChain ifNotFoundLog(String message) {
        actions.add(new ConditionalAction(null, Condition.IF_NOT_FOUND, () -> message, null));
        return this;
    }
    
    /**
     * Adds a custom handler that executes if the previous action succeeded.
     * 
     * @param handler the custom handler to execute
     * @return this chain for fluent API
     */
    public ConditionalActionChain ifFoundDo(Consumer<ActionResult> handler) {
        actions.add(new ConditionalAction(null, Condition.IF_FOUND, null, handler));
        return this;
    }
    
    /**
     * Adds a custom handler that executes if the previous action failed.
     * 
     * @param handler the custom handler to execute
     * @return this chain for fluent API
     */
    public ConditionalActionChain ifNotFoundDo(Consumer<ActionResult> handler) {
        actions.add(new ConditionalAction(null, Condition.IF_NOT_FOUND, null, handler));
        return this;
    }
    
    /**
     * Converts this ConditionalActionChain to a standard ActionChainOptions.
     * This allows integration with the existing ActionChain infrastructure.
     * 
     * @return ActionChainOptions configured for conditional execution
     */
    public ActionChainOptions toActionChainOptions() {
        if (actions.isEmpty()) {
            throw new IllegalStateException("Cannot create ActionChainOptions from empty chain");
        }
        
        // The first action is always executed
        ConditionalAction first = actions.get(0);
        ActionChainOptions.Builder builder = new ActionChainOptions.Builder(first.action);
        
        // For now, we'll add all ALWAYS actions to the chain
        // TODO: Implement full conditional logic in ActionChainExecutor
        for (int i = 1; i < actions.size(); i++) {
            ConditionalAction conditionalAction = actions.get(i);
            if (conditionalAction.condition == Condition.ALWAYS && conditionalAction.action != null) {
                builder.then(conditionalAction.action);
            }
        }
        
        return builder.build();
    }
    
    /**
     * Executes the conditional action chain.
     * 
     * @param action the Action instance to use for execution
     * @param objectCollections the object collections to act upon
     * @return the final ActionResult
     */
    public ActionResult perform(Action action, ObjectCollection... objectCollections) {
        // For now, delegate to the existing ActionChain infrastructure
        // TODO: Implement full conditional execution logic
        return action.perform(toActionChainOptions(), objectCollections);
    }
    
    /**
     * Static factory method for convenience.
     * Creates a Find-based chain starting with the provided object collections.
     * 
     * @param objectCollections the collections to find
     * @return a new ConditionalActionChain configured for finding
     */
    public static ConditionalActionChain findIn(ObjectCollection... objectCollections) {
        return find(new PatternFindOptions.Builder().build());
    }
}