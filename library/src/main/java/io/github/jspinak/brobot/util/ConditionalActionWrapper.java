package io.github.jspinak.brobot.util;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.model.state.StateLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Spring-friendly wrapper around ConditionalActionChain that simplifies
 * conditional action execution in Brobot applications.
 * 
 * <p>This wrapper provides:
 * <ul>
 *   <li>Convenient methods for common patterns</li>
 *   <li>Integration with StateObject types</li>
 *   <li>Pre-configured action builders</li>
 *   <li>Simplified error handling</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private ConditionalActionWrapper actions;
 * 
 * // Simple find and click
 * ActionResult result = actions.findAndClick(submitButton);
 * 
 * // Using the chain builder
 * ActionResult result = actions.createChain()
 *     .find(searchField)
 *     .ifFound(click())
 *     .ifFound(type("search query"))
 *     .execute();
 * }</pre>
 */
@Component
@Slf4j
public class ConditionalActionWrapper {
    
    private final Action action;
    
    public ConditionalActionWrapper(Action action) {
        this.action = action;
    }
    
    /**
     * Simple find and click operation.
     * 
     * @param stateObject the object to find and click
     * @return the action result
     */
    public ActionResult findAndClick(StateObject stateObject) {
        log.debug("Finding and clicking: {}", stateObject.getName());
        
        // Create a find-click chain
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        
        ConditionalActionChain chain = ConditionalActionChain.find(findOptions)
            .ifFound(clickOptions);
            
        return executeChain(chain, stateObject);
    }
    
    /**
     * Find and type operation.
     * 
     * @param target where to click before typing
     * @param text what to type
     * @return the action result
     */
    public ActionResult findAndType(StateObject target, String text) {
        log.debug("Finding {} and typing: {}", target.getName(), text);
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        TypeOptions typeOptions = new TypeOptions.Builder().build();
        
        // Note: TypeOptions doesn't have a text setter in the builder,
        // the text is passed via StateString in the ObjectCollection
        StateString textToType = new StateString();
        textToType.setString(text);
        
        ConditionalActionChain chain = ConditionalActionChain.find(findOptions)
            .ifFound(clickOptions)
            .ifFound(typeOptions);
            
        ObjectCollection collection = createCollection(target);
        collection.getStateStrings().add(textToType);
        
        return chain.perform(action, collection);
    }
    
    /**
     * Creates a new chain builder for complex conditional logic.
     * 
     * @return a new ChainBuilder instance
     */
    public ChainBuilder createChain() {
        return new ChainBuilder();
    }
    
    /**
     * Helper method to execute a chain with a single StateObject.
     */
    private ActionResult executeChain(ConditionalActionChain chain, StateObject stateObject) {
        ObjectCollection collection = createCollection(stateObject);
        return chain.perform(action, collection);
    }
    
    /**
     * Helper method to create an ObjectCollection from a StateObject.
     */
    private ObjectCollection createCollection(StateObject stateObject) {
        ObjectCollection.Builder builder = new ObjectCollection.Builder();
        
        if (stateObject instanceof StateImage) {
            builder.withImages((StateImage) stateObject);
        } else if (stateObject instanceof StateRegion) {
            builder.withRegions((StateRegion) stateObject);
        } else if (stateObject instanceof StateLocation) {
            builder.withLocations((StateLocation) stateObject);
        } else if (stateObject instanceof StateString) {
            builder.withStrings((StateString) stateObject);
        }
        
        return builder.build();
    }
    
    /**
     * Static factory methods for common action configurations.
     */
    public static ClickOptions click() {
        return new ClickOptions.Builder().build();
    }
    
    public static TypeOptions type() {
        return new TypeOptions.Builder().build();
    }
    
    public static PatternFindOptions find() {
        return new PatternFindOptions.Builder().build();
    }
    
    /**
     * Fluent builder for creating conditional action chains.
     */
    public class ChainBuilder {
        private ConditionalActionChain currentChain;
        private StateObject primaryObject;
        
        /**
         * Starts the chain with a find operation.
         */
        public ChainBuilder find(StateObject stateObject) {
            this.primaryObject = stateObject;
            this.currentChain = ConditionalActionChain.find(
                new PatternFindOptions.Builder().build()
            );
            return this;
        }
        
        /**
         * Adds an action to execute if the previous action succeeded.
         */
        public ChainBuilder ifFound(ActionConfig actionConfig) {
            if (currentChain == null) {
                throw new IllegalStateException("Chain not started. Call find() first.");
            }
            currentChain.ifFound(actionConfig);
            return this;
        }
        
        /**
         * Adds an action to execute if the previous action failed.
         */
        public ChainBuilder ifNotFound(ActionConfig actionConfig) {
            if (currentChain == null) {
                throw new IllegalStateException("Chain not started. Call find() first.");
            }
            currentChain.ifNotFound(actionConfig);
            return this;
        }
        
        /**
         * Adds an action that always executes.
         */
        public ChainBuilder always(ActionConfig actionConfig) {
            if (currentChain == null) {
                throw new IllegalStateException("Chain not started. Call find() first.");
            }
            currentChain.always(actionConfig);
            return this;
        }
        
        /**
         * Logs a message if the previous action succeeded.
         */
        public ChainBuilder ifFoundLog(String message) {
            if (currentChain == null) {
                throw new IllegalStateException("Chain not started. Call find() first.");
            }
            currentChain.ifFoundLog(message);
            return this;
        }
        
        /**
         * Logs a message if the previous action failed.
         */
        public ChainBuilder ifNotFoundLog(String message) {
            if (currentChain == null) {
                throw new IllegalStateException("Chain not started. Call find() first.");
            }
            currentChain.ifNotFoundLog(message);
            return this;
        }
        
        /**
         * Executes the chain.
         * 
         * @return the final ActionResult
         */
        public ActionResult execute() {
            if (currentChain == null || primaryObject == null) {
                throw new IllegalStateException("Chain not properly initialized");
            }
            
            return executeChain(currentChain, primaryObject);
        }
        
        /**
         * Executes the chain with additional object collections.
         * 
         * @param additionalCollections extra collections to include
         * @return the final ActionResult
         */
        public ActionResult execute(ObjectCollection... additionalCollections) {
            if (currentChain == null || primaryObject == null) {
                throw new IllegalStateException("Chain not properly initialized");
            }
            
            ObjectCollection primaryCollection = createCollection(primaryObject);
            ObjectCollection[] allCollections = new ObjectCollection[additionalCollections.length + 1];
            allCollections[0] = primaryCollection;
            System.arraycopy(additionalCollections, 0, allCollections, 1, additionalCollections.length);
            
            return currentChain.perform(action, allCollections);
        }
    }
}