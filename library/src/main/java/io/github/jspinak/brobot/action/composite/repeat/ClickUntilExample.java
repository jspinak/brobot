package io.github.jspinak.brobot.action.composite.repeat;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.stereotype.Component;

/**
 * Example implementation showing how to use RepeatUntilConfig to replace ClickUntil functionality.
 * <p>
 * This example demonstrates the modern approach to implementing click-until patterns using
 * RepeatUntilConfig instead of the deprecated ClickUntil action. It shows how to:
 * <ul>
 *   <li>Configure repeated clicking with a maximum limit</li>
 *   <li>Set up termination conditions based on image appearance</li>
 *   <li>Handle both the action and verification phases</li>
 * </ul>
 * 
 * <p>This pattern is common in GUI automation for scenarios like:</p>
 * <ul>
 *   <li>Clicking "Next" until a specific screen appears</li>
 *   <li>Dismissing popups until they're all gone</li>
 *   <li>Clicking "Load More" until desired content is visible</li>
 * </ul>
 * 
 * @see RepeatUntilConfig
 * @see ClickUntil
 */
@Component
public class ClickUntilExample {
    
    private final Action action;
    
    public ClickUntilExample(Action action) {
        this.action = action;
    }
    
    /**
     * Clicks on a button until a specific image appears, with a maximum of 10 clicks.
     * <p>
     * This method demonstrates the recommended pattern for click-until operations using
     * RepeatUntilConfig. It separates the click action from the verification action,
     * providing more flexibility than the older ClickUntil approach.
     * 
     * @param buttonToClick The button/element to repeatedly click
     * @param imageToAppear The image that should appear to stop clicking
     * @return true if the image appeared within 10 clicks, false otherwise
     */
    public boolean clickUntilImageAppears(StateImage buttonToClick, StateImage imageToAppear) {
        // Create the click action configuration
        ActionOptions clickOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.CLICK)
            .setPauseAfterEnd(0.5) // Small pause between clicks
            .build();
        
        // Create the verification action configuration
        ActionOptions findOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setMaxWait(2.0) // Wait up to 2 seconds for image to appear
            .build();
        
        // Build the RepeatUntilConfig
        RepeatUntilConfig config = new RepeatUntilConfig.Builder()
            .setDoAction(clickOptions)
            .setActionObjectCollection(buttonToClick.asObjectCollection())
            .setUntilAction(findOptions)
            .setConditionObjectCollection(imageToAppear.asObjectCollection())
            .setMaxActions(10) // Maximum 10 clicks
            .build();
        
        // Execute the repeat-until operation
        return executeRepeatUntil(config);
    }
    
    /**
     * Clicks on elements until they all disappear, with a maximum of 10 clicks.
     * <p>
     * This example shows how to implement the "click until vanish" pattern, where
     * we keep clicking elements until they're no longer visible on screen.
     * 
     * @param elementsToClick The elements to click until they disappear
     * @return true if all elements vanished within 10 clicks, false otherwise
     */
    public boolean clickUntilElementsVanish(ObjectCollection elementsToClick) {
        // Create the click action configuration
        ActionOptions clickOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.CLICK)
            .setPauseAfterEnd(0.5)
            .build();
        
        // Create the vanish verification configuration
        ActionOptions vanishOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.VANISH)
            .setMaxWait(2.0)
            .build();
        
        // Build the RepeatUntilConfig
        RepeatUntilConfig config = new RepeatUntilConfig.Builder()
            .setDoAction(clickOptions)
            .setActionObjectCollection(elementsToClick)
            .setUntilAction(vanishOptions)
            .setConditionObjectCollection(elementsToClick) // Same elements for both
            .setMaxActions(10)
            .build();
        
        // Execute the repeat-until operation
        return executeRepeatUntil(config);
    }
    
    /**
     * A more complex example: Click "Next" button until "Finish" button appears.
     * <p>
     * This demonstrates clicking one element repeatedly while monitoring for a
     * different element to appear - a common pattern in wizard-style interfaces.
     * 
     * @param nextButton The "Next" button to click
     * @param finishButton The "Finish" button we're waiting to see
     * @return true if the finish button appeared within 10 clicks, false otherwise
     */
    public boolean clickNextUntilFinishAppears(StateImage nextButton, StateImage finishButton) {
        // Configure with more specific settings
        ActionOptions clickOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.CLICK)
            .setPauseAfterEnd(1.0) // Longer pause for page transitions
            .setMinSimilarity(0.8) // Higher similarity for button matching
            .build();
        
        ActionOptions findOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setMaxWait(3.0) // More time for page loads
            .setMinSimilarity(0.85) // High similarity for finish button
            .build();
        
        RepeatUntilConfig config = new RepeatUntilConfig.Builder()
            .setDoAction(clickOptions)
            .setActionObjectCollection(nextButton.asObjectCollection())
            .setUntilAction(findOptions)
            .setConditionObjectCollection(finishButton.asObjectCollection())
            .setMaxActions(10)
            .build();
        
        return executeRepeatUntil(config);
    }
    
    /**
     * Executes a RepeatUntilConfig using the recommended pattern.
     * <p>
     * Note: In actual usage, you would typically use an existing framework component
     * that handles RepeatUntilConfig execution (like MultipleBasicActions or a dedicated
     * RepeatUntilExecutor). This simplified example shows the conceptual flow.
     * 
     * <p>The actual execution would be handled by the framework, which would:
     * <ol>
     *   <li>Execute the primary action (e.g., click)</li>
     *   <li>Check the condition (e.g., find image)</li>
     *   <li>Repeat until condition is met or max actions reached</li>
     *   <li>Update the config's result fields internally</li>
     * </ol>
     * 
     * @param config The repeat-until configuration
     * @return true if the condition was met, false if max actions was reached
     */
    private boolean executeRepeatUntil(RepeatUntilConfig config) {
        // In practice, you would use the framework's execution component:
        // return repeatUntilExecutor.execute(config);
        
        // For this example, we'll show a simplified execution flow
        int actionsPerformed = 0;
        
        while (actionsPerformed < config.getMaxActions()) {
            // Perform the primary action
            ActionResult actionResult = action.perform(
                config.getDoAction(), 
                config.getActionObjectCollection()
            );
            
            // If the action failed, we might want to stop
            if (!actionResult.isSuccess()) {
                return false;
            }
            
            actionsPerformed++;
            
            // Check the termination condition
            ActionResult conditionResult = action.perform(
                config.getUntilAction(),
                config.getConditionObjectCollection()
            );
            
            // If condition is met, we're done
            if (conditionResult.isSuccess()) {
                return true;
            }
            
            // Reset the action counter if needed
            if (config.getDoAction().getMaxMatchesToActOn() > 0) {
                config.resetTimesActedOn();
            }
        }
        
        // Max actions reached without meeting condition
        return false;
    }
}