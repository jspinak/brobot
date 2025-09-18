package io.github.jspinak.brobot.action.composite.repeat;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import lombok.Getter;

/**
 * Configuration and result container for do-until composite actions using ActionConfig.
 *
 * <p>RepeatUntilConfigV2 is the modern replacement for RepeatUntilConfig, using the new
 * ActionConfig hierarchy instead of ActionConfig. It encapsulates the complete configuration needed
 * to execute a pattern where one action is repeatedly performed until a condition is met. It
 * separates the configuration of the primary action (do) from the termination condition (until),
 * providing more flexibility than simpler composite actions. This separation allows different
 * ActionConfig implementations for each phase of the operation.
 *
 * <p>Key components:
 *
 * <ul>
 *   <li><b>Action configuration:</b> Separate ActionConfig and ObjectCollections for both the
 *       repeated action and the termination condition
 *   <li><b>Execution control:</b> Maximum action limit to prevent infinite loops
 *   <li><b>Result tracking:</b> Captures results from both action and condition evaluations for
 *       analysis
 *   <li><b>Success metrics:</b> Tracks successful action count and overall success
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Complex clicking patterns with custom termination conditions
 *   <li>Typing until specific feedback appears
 *   <li>Dragging until an object reaches a target position
 *   <li>Any repetitive action with sophisticated stop conditions
 * </ul>
 *
 * <p>This class follows the Builder pattern for construction, ensuring all configurations are set
 * before the object is used. The mutable result fields allow the executing action to update
 * outcomes during execution.
 *
 * <p><b>Example usage:</b>
 *
 * <pre>{@code
 * RepeatUntilConfigV2 config = new RepeatUntilConfigV2.Builder()
 *     .setDoAction(new ClickOptions.Builder().build())
 *     .setActionObjectCollection(buttons)
 *     .setUntilAction(new PatternFindOptions.Builder().build())
 *     .setConditionObjectCollection(successMessage)
 *     .setMaxActions(10)
 *     .build();
 * }</pre>
 *
 * @see RepeatUntilConfig
 * @see ClickUntil
 */
@Getter
public class ModernRepeatUntilConfig {

    private ObjectCollection actionObjectCollection;
    private ObjectCollection conditionObjectCollection;
    private ActionConfig doAction;
    private ActionConfig untilAction;
    private int maxActions = 3;

    // results
    private int totalSuccessfulActions = 0;
    private ActionResult actionMatches = new ActionResult();
    private ActionResult conditionMatches = new ActionResult();
    private boolean success = false;

    /**
     * Resets the action counters for the action's target objects.
     *
     * <p>This method clears the internal counters that track how many times each object in the
     * action collection has been acted upon. This is important for actions that have limits on how
     * many times they can act on the same object. Only the action collection is reset, not the
     * condition collection, as conditions are typically evaluated without modification.
     *
     * <p>Common scenarios for using this method:
     *
     * <ul>
     *   <li>Reusing a RepeatUntilConfigV2 for multiple executions
     *   <li>Resetting state after error recovery
     *   <li>Starting fresh after configuration changes
     * </ul>
     */
    public void resetTimesActedOn() {
        actionObjectCollection.resetTimesActedOn();
    }

    /**
     * Sets the result of the total successful actions.
     *
     * @param totalSuccessfulActions The number of successful actions performed
     */
    public void setTotalSuccessfulActions(int totalSuccessfulActions) {
        this.totalSuccessfulActions = totalSuccessfulActions;
    }

    /**
     * Sets the action matches result.
     *
     * @param actionMatches The result of the repeated action
     */
    public void setActionMatches(ActionResult actionMatches) {
        this.actionMatches = actionMatches;
    }

    /**
     * Sets the condition matches result.
     *
     * @param conditionMatches The result of the condition check
     */
    public void setConditionMatches(ActionResult conditionMatches) {
        this.conditionMatches = conditionMatches;
    }

    /**
     * Sets the overall success status.
     *
     * @param success True if the operation succeeded, false otherwise
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Builder for constructing RepeatUntilConfigV2 instances with proper configuration.
     *
     * <p>This builder ensures that RepeatUntilConfigV2 instances are created with all necessary
     * configuration before use. It provides a fluent interface for setting the various components
     * needed for do-until operations, with sensible defaults where appropriate.
     *
     * <p>The builder separates concerns between:
     *
     * <ul>
     *   <li>Action configuration (what to do repeatedly)
     *   <li>Condition configuration (when to stop)
     *   <li>Execution limits (maximum repetitions)
     * </ul>
     *
     * <p>Default values:
     *
     * <ul>
     *   <li>maxActions: 3 (prevents infinite loops while allowing reasonable retries)
     * </ul>
     */
    public static class Builder {
        private ObjectCollection actionObjectCollection;
        private ObjectCollection conditionObjectCollection;
        private ActionConfig doAction;
        private ActionConfig untilAction;
        private int maxActions = 3;

        public Builder() {}

        /**
         * Sets the objects to be acted upon by the primary action.
         *
         * <p>These are the targets for the repeated action (e.g., buttons to click, fields to type
         * in). The action will be performed on these objects until the termination condition is
         * met.
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
         *
         * <p>These objects are checked by the until action to determine when to stop the primary
         * action. They may be different from the action objects (e.g., click button A until message
         * B appears).
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
         *
         * <p>This ActionConfig defines what action is executed in the loop (e.g., CLICK, TYPE,
         * DRAG). It includes all timing, search, and behavior settings for the repeated action.
         *
         * @param actionConfig The configuration for the primary action
         * @return This builder instance for method chaining
         */
        public Builder setDoAction(ActionConfig actionConfig) {
            this.doAction = actionConfig;
            return this;
        }

        /**
         * Configures the condition check that determines when to stop.
         *
         * <p>This ActionConfig defines how to check the termination condition (e.g., FIND to wait
         * for appearance, VANISH to wait for disappearance). The success criteria in these options
         * determine when the loop terminates.
         *
         * @param actionConfig The configuration for condition checking
         * @return This builder instance for method chaining
         */
        public Builder setUntilAction(ActionConfig actionConfig) {
            this.untilAction = actionConfig;
            return this;
        }

        /**
         * Sets the maximum number of times the primary action can be performed.
         *
         * <p>This safety limit prevents infinite loops if the termination condition is never met.
         * Once this limit is reached, the operation stops regardless of the condition state.
         * Default is 3 if not set.
         *
         * @param maxActions The maximum number of action repetitions allowed
         * @return This builder instance for method chaining
         */
        public Builder setMaxActions(int maxActions) {
            this.maxActions = maxActions;
            return this;
        }

        /**
         * Creates a configured RepeatUntilConfigV2 instance.
         *
         * <p>This method constructs the final object with all configured values. No validation is
         * performed - callers should ensure all necessary components are set before building.
         * Result tracking fields are initialized to their default states.
         *
         * @return A new RepeatUntilConfigV2 with the configured settings
         */
        public ModernRepeatUntilConfig build() {
            ModernRepeatUntilConfig repeatUntilConfig = new ModernRepeatUntilConfig();
            repeatUntilConfig.actionObjectCollection = actionObjectCollection;
            repeatUntilConfig.conditionObjectCollection = conditionObjectCollection;
            repeatUntilConfig.doAction = doAction;
            repeatUntilConfig.untilAction = untilAction;
            repeatUntilConfig.maxActions = maxActions;
            return repeatUntilConfig;
        }
    }
}
