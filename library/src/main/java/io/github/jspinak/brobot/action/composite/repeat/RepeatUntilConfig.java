package io.github.jspinak.brobot.action.composite.repeat;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import lombok.Getter;

/**
 * Configuration and result container for do-until composite actions.
 * <p>
 * DoUntilActionObject encapsulates the complete configuration needed to execute a
 * pattern where one action is repeatedly performed until a condition is met. It
 * separates the configuration of the primary action (do) from the termination
 * condition (until), providing more flexibility than simpler composite actions.
 * This separation allows different ActionOptions for each phase of the operation.
 * 
 * <p>Key components:</p>
 * <ul>
 *   <li><b>Action configuration:</b> Separate ActionOptions and ObjectCollections
 *       for both the repeated action and the termination condition</li>
 *   <li><b>Execution control:</b> Maximum action limit to prevent infinite loops</li>
 *   <li><b>Result tracking:</b> Captures results from both action and condition
 *       evaluations for analysis</li>
 *   <li><b>Success metrics:</b> Tracks successful action count and overall success</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Complex clicking patterns with custom termination conditions</li>
 *   <li>Typing until specific feedback appears</li>
 *   <li>Dragging until an object reaches a target position</li>
 *   <li>Any repetitive action with sophisticated stop conditions</li>
 * </ul>
 * 
 * <p>This class follows the Builder pattern for construction, ensuring all
 * configurations are set before the object is used. The mutable result fields
 * allow the executing action to update outcomes during execution.</p>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * DoUntilActionObject config = new DoUntilActionObject.Builder()
 *     .setDoAction(clickOptions)
 *     .setActionObjectCollection(buttons)
 *     .setUntilAction(findOptions)
 *     .setConditionObjectCollection(successMessage)
 *     .setMaxActions(10)
 *     .build();
 * }</pre>
 * 
 * @see ClickUntil
 */
@Getter
public class RepeatUntilConfig {

    private ObjectCollection actionObjectCollection;
    private ObjectCollection conditionObjectCollection;
    private ActionOptions doAction;
    private ActionOptions untilAction;
    private int maxActions = 3;

    //results
    private int totalSuccessfulActions = 0;
    private ActionResult actionMatches = new ActionResult();
    private ActionResult conditionMatches = new ActionResult();
    private boolean success = false;

    /**
     * Resets the action counters for the action's target objects.
     * <p>
     * This method clears the internal counters that track how many times each object
     * in the action collection has been acted upon. This is important for actions that
     * have limits on how many times they can act on the same object (controlled by
     * ActionOptions.maxMatchesToActOn). Only the action collection is reset, not the
     * condition collection, as conditions are typically evaluated without modification.
     * 
     * <p>Common scenarios for using this method:</p>
     * <ul>
     *   <li>Reusing a DoUntilActionObject for multiple executions</li>
     *   <li>Resetting state after error recovery</li>
     *   <li>Starting fresh after configuration changes</li>
     * </ul>
     */
    public void resetTimesActedOn() {
        actionObjectCollection.resetTimesActedOn();
    }

    /**
     * Builder for constructing DoUntilActionObject instances with proper configuration.
     * <p>
     * This builder ensures that DoUntilActionObject instances are created with all
     * necessary configuration before use. It provides a fluent interface for setting
     * the various components needed for do-until operations, with sensible defaults
     * where appropriate.
     * 
     * <p>The builder separates concerns between:</p>
     * <ul>
     *   <li>Action configuration (what to do repeatedly)</li>
     *   <li>Condition configuration (when to stop)</li>
     *   <li>Execution limits (maximum repetitions)</li>
     * </ul>
     * 
     * <p>Default values:</p>
     * <ul>
     *   <li>maxActions: 3 (prevents infinite loops while allowing reasonable retries)</li>
     * </ul>
     */
    public static class Builder {
        private ObjectCollection actionObjectCollection;
        private ObjectCollection conditionObjectCollection;
        private ActionOptions doAction;
        private ActionOptions untilAction;
        private int maxActions = 3;

        public Builder() {
        }

        /**
         * Sets the objects to be acted upon by the primary action.
         * <p>
         * These are the targets for the repeated action (e.g., buttons to click,
         * fields to type in). The action will be performed on these objects until
         * the termination condition is met.
         * 
         * @param objectCollection The collection of objects for the primary action
         * @return This builder instance for method chaining
         */
        public Builder setActionObjectCollection(ObjectCollection objectCollection) {
            this.actionObjectCollection = objectCollection;
            return this;
        }

        /**
         * Sets the objects to monitor for the termination condition.
         * <p>
         * These objects are checked by the until action to determine when to stop
         * the primary action. They may be different from the action objects (e.g.,
         * click button A until message B appears).
         * 
         * @param objectCollection The collection of objects for condition checking
         * @return This builder instance for method chaining
         */
        public Builder setConditionObjectCollection(ObjectCollection objectCollection) {
            this.conditionObjectCollection = objectCollection;
            return this;
        }

        /**
         * Configures the primary action to be performed repeatedly.
         * <p>
         * This ActionOptions defines what action is executed in the loop (e.g.,
         * CLICK, TYPE, DRAG). It includes all timing, search, and behavior settings
         * for the repeated action.
         * 
         * @param actionOptions The configuration for the primary action
         * @return This builder instance for method chaining
         */
        public Builder setDoAction(ActionOptions actionOptions) {
            this.doAction = actionOptions;
            return this;
        }

        /**
         * Configures the condition check that determines when to stop.
         * <p>
         * This ActionOptions defines how to check the termination condition (e.g.,
         * FIND to wait for appearance, VANISH to wait for disappearance). The
         * success criteria in these options determine when the loop terminates.
         * 
         * @param actionOptions The configuration for condition checking
         * @return This builder instance for method chaining
         */
        public Builder setUntilAction(ActionOptions actionOptions) {
            this.untilAction = actionOptions;
            return this;
        }

        /**
         * Sets the maximum number of times the primary action can be performed.
         * <p>
         * This safety limit prevents infinite loops if the termination condition
         * is never met. Once this limit is reached, the operation stops regardless
         * of the condition state. Default is 3 if not set.
         * 
         * @param maxActions The maximum number of action repetitions allowed
         * @return This builder instance for method chaining
         */
        public Builder setMaxActions(int maxActions) {
            this.maxActions = maxActions;
            return this;
        }

        /**
         * Creates a configured DoUntilActionObject instance.
         * <p>
         * This method constructs the final object with all configured values.
         * No validation is performed - callers should ensure all necessary
         * components are set before building. Result tracking fields are
         * initialized to their default states.
         * 
         * @return A new DoUntilActionObject with the configured settings
         */
        public RepeatUntilConfig build() {
            RepeatUntilConfig doUntilActionObject = new RepeatUntilConfig();
            doUntilActionObject.actionObjectCollection = actionObjectCollection;
            doUntilActionObject.conditionObjectCollection = conditionObjectCollection;
            doUntilActionObject.doAction = doAction;
            doUntilActionObject.untilAction = untilAction;
            doUntilActionObject.maxActions = maxActions;
            return doUntilActionObject;
        }
    }
}
