package io.github.jspinak.brobot.action.composite.select;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import lombok.Getter;

/**
 * Data container for configuring and tracking the state of a Select operation.
 *
 * <p>This class encapsulates all the configuration needed to perform a selection operation,
 * including the swipe parameters, find options, click settings, and optional confirmation
 * verification. It also tracks the operation's results and state during execution.
 *
 * <p>A SelectActionObject contains:
 *
 * <ul>
 *   <li>Configuration for swipe operations (from/to locations, action options)
 *   <li>Configuration for finding target images
 *   <li>Configuration for clicking on found matches
 *   <li>Optional configuration for confirmation verification
 *   <li>Runtime state tracking (swipe count, matches found, success status)
 * </ul>
 *
 * <p>This class uses the Builder pattern for construction to handle its many configuration
 * parameters. The runtime state fields are modified by the {@link Select} class during operation
 * execution.
 *
 * <p><b>Migration Note:</b> This class now supports both ActionOptions (legacy) and ActionConfig
 * (modern) APIs. New code should use the ActionConfig setters. The getters intelligently return the
 * appropriate configuration based on which was set.
 *
 * @see Select
 * @see CommonSelect
 */
@Getter
public class SelectActionObject {

    // build
    private int maxSwipes = 10;
    private ObjectCollection swipeFromObjColl;
    private ObjectCollection swipeToObjColl;
    private ActionConfig swipeActionConfig; // Modern API
    private ObjectCollection findObjectCollection;
    private ActionConfig findActionConfig; // Modern API
    private ActionConfig clickActionConfig; // Modern API
    private ObjectCollection clickMatches; // defined after find operation
    private ObjectCollection confirmationObjectCollection; // when null don't confirm
    private ActionConfig confirmActionConfig; // Modern API

    // results
    private int totalSwipes = 0;
    private ActionResult foundMatches = new ActionResult();
    private ActionResult foundConfirmations = new ActionResult();
    private boolean success = false;

    // setters
    /**
     * Resets the swipe counter to zero.
     *
     * <p>This method is typically called at the beginning of a select operation to ensure accurate
     * tracking of swipe attempts.
     */
    public void resetTotalSwipes() {
        totalSwipes = 0;
    }

    /**
     * Increments the swipe counter by one.
     *
     * <p>Called after each swipe operation to track the total number of swipes performed during the
     * selection process.
     */
    public void addSwipe() {
        totalSwipes++;
    }

    /**
     * Sets the matches found during the find operation.
     *
     * <p>This method is called by {@link Select} to store the results of each find attempt during
     * the selection process.
     *
     * @param matches The ActionResult containing found matches from the find operation
     */
    public void setFoundMatches(ActionResult matches) {
        foundMatches = matches;
    }

    /**
     * Sets the confirmation matches found after clicking.
     *
     * <p>This method stores the results of the confirmation find operation, which verifies that the
     * click action had the desired effect.
     *
     * @param matches The ActionResult containing confirmation matches
     */
    public void setFoundConfirmations(ActionResult matches) {
        foundConfirmations = matches;
    }

    /**
     * Sets the overall success status of the select operation.
     *
     * <p>Set to true when the operation completes successfully (target found, clicked, and
     * optionally confirmed). Set to false otherwise.
     *
     * @param success true if the operation succeeded, false otherwise
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    // Custom getters that return either ActionConfig or ActionOptions based on what was set

    /**
     * Gets the swipe configuration, preferring ActionConfig if set.
     *
     * @return ActionConfig if set, otherwise ActionOptions, or null if neither set
     */
    public ActionConfig getSwipeConfiguration() {
        return swipeActionConfig;
    }

    /**
     * Gets the find configuration, preferring ActionConfig if set.
     *
     * @return ActionConfig if set, otherwise ActionOptions, or null if neither set
     */
    public ActionConfig getFindConfiguration() {
        return findActionConfig;
    }

    /**
     * Gets the click configuration, preferring ActionConfig if set.
     *
     * @return ActionConfig if set, otherwise ActionOptions, or null if neither set
     */
    public ActionConfig getClickConfiguration() {
        return clickActionConfig;
    }

    /**
     * Gets the confirmation configuration, preferring ActionConfig if set.
     *
     * @return ActionConfig if set, otherwise ActionOptions, or null if neither set
     */
    public ActionConfig getConfirmConfiguration() {
        return confirmActionConfig;
    }

    /**
     * Builder class for constructing SelectActionObject instances.
     *
     * <p>This builder provides a fluent interface for setting the numerous configuration parameters
     * needed for a select operation. All setter methods return the builder instance for method
     * chaining.
     *
     * <p><b>Migration Note:</b> The builder now supports both ActionOptions (legacy) and
     * ActionConfig (modern) APIs. New code should use the ActionConfig setters.
     */
    public static class Builder {
        private int maxSwipes = 10;
        private ObjectCollection swipeFromObjColl;
        private ObjectCollection swipeToObjColl;
        private ActionConfig swipeActionConfig; // Modern API
        private ObjectCollection findObjectCollection;
        private ActionConfig findActionConfig; // Modern API
        private ActionConfig clickActionConfig; // Modern API
        private ObjectCollection confirmationObjectCollection; // when null don't confirm
        private ActionConfig confirmActionConfig; // Modern API

        public Builder() {}

        public Builder setMaxSwipes(int maxSwipes) {
            this.maxSwipes = maxSwipes;
            return this;
        }

        public Builder setSwipeFromObjColl(ObjectCollection swipeFromObjColl) {
            this.swipeFromObjColl = swipeFromObjColl;
            return this;
        }

        public Builder setSwipeToObjColl(ObjectCollection swipeToObjColl) {
            this.swipeToObjColl = swipeToObjColl;
            return this;
        }

        // Removed deprecated setSwipeActionOptions method

        /**
         * Sets the swipe configuration using ActionConfig (modern API).
         *
         * @param swipeActionConfig The ActionConfig for swipe operations
         * @return this Builder instance
         */
        public Builder setSwipeActionConfig(ActionConfig swipeActionConfig) {
            this.swipeActionConfig = swipeActionConfig;
            return this;
        }

        public Builder setFindObjectCollection(ObjectCollection findObjectCollection) {
            this.findObjectCollection = findObjectCollection;
            return this;
        }

        // Removed deprecated setFindActionOptions method

        /**
         * Sets the find configuration using ActionConfig (modern API).
         *
         * @param findActionConfig The ActionConfig for find operations
         * @return this Builder instance
         */
        public Builder setFindActionConfig(ActionConfig findActionConfig) {
            this.findActionConfig = findActionConfig;
            return this;
        }

        // Removed deprecated setClickActionOptions method

        /**
         * Sets the click configuration using ActionConfig (modern API).
         *
         * @param clickActionConfig The ActionConfig for click operations
         * @return this Builder instance
         */
        public Builder setClickActionConfig(ActionConfig clickActionConfig) {
            this.clickActionConfig = clickActionConfig;
            return this;
        }

        public Builder setConfirmationObjectCollection(
                ObjectCollection confirmationObjectCollection) {
            this.confirmationObjectCollection = confirmationObjectCollection;
            return this;
        }

        // Removed deprecated setConfirmActionOptions method

        /**
         * Sets the confirmation configuration using ActionConfig (modern API).
         *
         * @param confirmActionConfig The ActionConfig for confirmation operations
         * @return this Builder instance
         */
        public Builder setConfirmActionConfig(ActionConfig confirmActionConfig) {
            this.confirmActionConfig = confirmActionConfig;
            return this;
        }

        public SelectActionObject build() {
            SelectActionObject selectActionObject = new SelectActionObject();
            selectActionObject.maxSwipes = maxSwipes;
            selectActionObject.swipeFromObjColl = swipeFromObjColl;
            selectActionObject.swipeToObjColl = swipeToObjColl;
            selectActionObject.swipeActionConfig = swipeActionConfig;
            selectActionObject.findObjectCollection = findObjectCollection;
            selectActionObject.findActionConfig = findActionConfig;
            selectActionObject.clickActionConfig = clickActionConfig;
            selectActionObject.confirmationObjectCollection = confirmationObjectCollection;
            selectActionObject.confirmActionConfig = confirmActionConfig;
            return selectActionObject;
        }
    }
}
