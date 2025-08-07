package io.github.jspinak.brobot.model.conditional;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import lombok.extern.slf4j.Slf4j;

/**
 * ConditionalActionChain provides a fluent API for building complex action sequences with conditional execution.
 * 
 * Code from: docs/docs/01-getting-started/pure-actions-quickstart.md lines 34-38, 57-62, 74-76, 85-88, 102-106
 * Code from: docs/docs/03-core-library/action-config/15-conditional-chains-examples.md lines 13-17, 23-31
 */
@Slf4j
public class ConditionalActionChain {
    
    private PatternFindOptions findOptions;
    private ClickOptions clickOptions;
    private TypeOptions typeOptions;
    private HighlightOptions highlightOptions;
    private String logMessage;
    private Runnable customAction;
    
    private ConditionalActionChain() {
        // Private constructor to enforce builder pattern
    }
    
    /**
     * Start a new conditional action chain with a find operation
     * From: docs/docs/01-getting-started/pure-actions-quickstart.md line 34
     */
    public static ConditionalActionChain find(PatternFindOptions findOptions) {
        ConditionalActionChain chain = new ConditionalActionChain();
        chain.findOptions = findOptions;
        return chain;
    }
    
    /**
     * Add an action to perform if the find operation succeeds
     * From: docs/docs/01-getting-started/pure-actions-quickstart.md line 35
     */
    public ConditionalActionChain ifFound(ClickOptions clickOptions) {
        this.clickOptions = clickOptions;
        return this;
    }
    
    /**
     * Add an action to perform if the find operation fails
     * From: docs/docs/01-getting-started/pure-actions-quickstart.md line 36
     */
    public ConditionalActionChain ifNotFound(String logMessage) {
        this.logMessage = logMessage;
        return this;
    }
    
    /**
     * Add a logging action for when element is not found
     * From: docs/docs/03-core-library/action-config/15-conditional-chains-examples.md line 25
     */
    public ConditionalActionChain ifNotFoundLog(String message) {
        this.logMessage = message;
        return this;
    }
    
    /**
     * Add a custom action to perform if find operation fails
     * From: docs/docs/03-core-library/action-config/15-conditional-chains-examples.md lines 26-31
     */
    public ConditionalActionChain ifNotFoundDo(Runnable action) {
        this.customAction = action;
        return this;
    }
    
    /**
     * Chain a type action after a click
     * From: docs/docs/01-getting-started/pure-actions-quickstart.md line 87
     */
    public ConditionalActionChain then(TypeOptions typeOptions) {
        this.typeOptions = typeOptions;
        return this;
    }
    
    /**
     * Execute the conditional action chain
     * From: docs/docs/01-getting-started/pure-actions-quickstart.md lines 57-62
     */
    public ActionResult perform(Action action, ObjectCollection objectCollection) {
        log.info("Executing ConditionalActionChain");
        
        // This is a simplified implementation for demonstration
        // In a real implementation, this would execute the full chain logic
        ActionResult result = action.perform(findOptions, objectCollection);
        
        if (result.isSuccess()) {
            if (clickOptions != null) {
                return action.perform(clickOptions, new ObjectCollection.Builder()
                    .withMatches(result)
                    .build());
            }
        } else {
            if (logMessage != null) {
                log.warn(logMessage);
            }
            if (customAction != null) {
                customAction.run();
            }
        }
        
        return result;
    }
    
    // Convenience methods for creating common options
    public static ClickOptions click() {
        return new ClickOptions.Builder().build();
    }
    
    public static String log(String message) {
        return message;
    }
}