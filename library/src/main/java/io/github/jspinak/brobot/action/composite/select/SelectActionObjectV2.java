package io.github.jspinak.brobot.action.composite.select;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import lombok.Getter;

/**
 * Data container for configuring and tracking the state of a Select operation using ActionConfig.
 * <p>
 * SelectActionObjectV2 is the modern replacement for SelectActionObject, using the new
 * ActionConfig hierarchy instead of ActionOptions. It encapsulates all the configuration 
 * needed to perform a selection operation, including the swipe parameters, find options, 
 * click settings, and optional confirmation verification. It also tracks the operation's 
 * results and state during execution.
 * 
 * @deprecated Complex select operations should be built using {@link io.github.jspinak.brobot.action.ActionChainOptions}.
 *             For simpler cases, use a custom action chain with find, click, and swipe actions.
 *             The action chain approach provides better modularity and reusability.
 * <p>
 * A SelectActionObjectV2 contains:
 * <ul>
 * <li>Configuration for swipe operations (from/to locations, action config)</li>
 * <li>Configuration for finding target images</li>
 * <li>Configuration for clicking on found matches</li>
 * <li>Optional configuration for confirmation verification</li>
 * <li>Runtime state tracking (swipe count, matches found, success status)</li>
 * </ul>
 * <p>
 * This class uses the Builder pattern for construction to handle its many configuration
 * parameters. The runtime state fields are modified by the Select class during
 * operation execution.
 * <p>
 * <b>Example usage:</b>
 * <pre>{@code
 * SelectActionObjectV2 config = new SelectActionObjectV2.Builder()
 *     .setMaxSwipes(5)
 *     .setSwipeActionConfig(new DragOptions.Builder().build())
 *     .setSwipeFromObjColl(fromRegion)
 *     .setSwipeToObjColl(toRegion)
 *     .setFindActionConfig(new PatternFindOptions.Builder().build())
 *     .setFindObjectCollection(targetImages)
 *     .setClickActionConfig(new ClickOptions.Builder().build())
 *     .build();
 * }</pre>
 *
 * @see SelectActionObject
 * @see Select
 * @see CommonSelect
 */
@Deprecated
@Getter
public class SelectActionObjectV2 {

    //build
    private int maxSwipes = 10;
    private ObjectCollection swipeFromObjColl;
    private ObjectCollection swipeToObjColl;
    private ActionConfig swipeActionConfig;
    private ObjectCollection findObjectCollection;
    private ActionConfig findActionConfig;
    private ActionConfig clickActionConfig;
    private ObjectCollection clickMatches; // defined after find operation
    private ObjectCollection confirmationObjectCollection; //when null don't confirm
    private ActionConfig confirmActionConfig;

    //results
    private int totalSwipes = 0;
    private ActionResult foundMatches = new ActionResult();
    private ActionResult foundConfirmations = new ActionResult();
    private boolean success = false;

    //setters
    /**
     * Resets the swipe counter to zero.
     * <p>
     * This method is typically called at the beginning of a select operation
     * to ensure accurate tracking of swipe attempts.
     */
    public void resetTotalSwipes() {
        totalSwipes = 0;
    }
    
    /**
     * Increments the swipe counter by one.
     * <p>
     * Called after each swipe operation to track the total number of swipes
     * performed during the selection process.
     */
    public void addSwipe() {
        totalSwipes++;
    }
    
    /**
     * Sets the matches found during the find operation.
     * <p>
     * This method is called by Select to store the results of each
     * find attempt during the selection process.
     *
     * @param matches The ActionResult containing found matches from the find operation
     */
    public void setFoundMatches(ActionResult matches) {
        foundMatches = matches;
    }
    
    /**
     * Sets the confirmation matches found after clicking.
     * <p>
     * This method stores the results of the confirmation find operation,
     * which verifies that the click action had the desired effect.
     *
     * @param matches The ActionResult containing confirmation matches
     */
    public void setFoundConfirmations(ActionResult matches) {
        foundConfirmations = matches;
    }
    
    /**
     * Sets the overall success status of the select operation.
     * <p>
     * Set to true when the operation completes successfully (target found,
     * clicked, and optionally confirmed). Set to false otherwise.
     *
     * @param success true if the operation succeeded, false otherwise
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Builder class for constructing SelectActionObjectV2 instances.
     * <p>
     * This builder provides a fluent interface for setting the numerous
     * configuration parameters needed for a select operation. All setter
     * methods return the builder instance for method chaining.
     */
    public static class Builder {
        private int maxSwipes = 10;
        private ObjectCollection swipeFromObjColl;
        private ObjectCollection swipeToObjColl;
        private ActionConfig swipeActionConfig;
        private ObjectCollection findObjectCollection;
        private ActionConfig findActionConfig;
        private ActionConfig clickActionConfig;
        private ObjectCollection confirmationObjectCollection; //when null don't confirm
        private ActionConfig confirmActionConfig;

        public Builder() {}

        /**
         * Sets the maximum number of swipe attempts.
         * @param maxSwipes Maximum swipes before giving up
         * @return This builder instance for chaining
         */
        public Builder setMaxSwipes(int maxSwipes) {
            this.maxSwipes = maxSwipes;
            return this;
        }

        /**
         * Sets the starting location/region for swipe operations.
         * @param swipeFromObjColl Collection containing the swipe start point
         * @return This builder instance for chaining
         */
        public Builder setSwipeFromObjColl(ObjectCollection swipeFromObjColl) {
            this.swipeFromObjColl = swipeFromObjColl;
            return this;
        }

        /**
         * Sets the ending location/region for swipe operations.
         * @param swipeToObjColl Collection containing the swipe end point
         * @return This builder instance for chaining
         */
        public Builder setSwipeToObjColl(ObjectCollection swipeToObjColl) {
            this.swipeToObjColl = swipeToObjColl;
            return this;
        }

        /**
         * Sets the configuration for swipe operations.
         * @param swipeActionConfig ActionConfig for swipe (typically DragOptions)
         * @return This builder instance for chaining
         */
        public Builder setSwipeActionConfig(ActionConfig swipeActionConfig) {
            this.swipeActionConfig = swipeActionConfig;
            return this;
        }

        /**
         * Sets the objects to find during the selection process.
         * @param findObjectCollection Target images/regions to find
         * @return This builder instance for chaining
         */
        public Builder setFindObjectCollection(ObjectCollection findObjectCollection) {
            this.findObjectCollection = findObjectCollection;
            return this;
        }

        /**
         * Sets the configuration for find operations.
         * @param findActionConfig ActionConfig for finding (typically PatternFindOptions)
         * @return This builder instance for chaining
         */
        public Builder setFindActionConfig(ActionConfig findActionConfig) {
            this.findActionConfig = findActionConfig;
            return this;
        }

        /**
         * Sets the configuration for click operations.
         * @param clickActionConfig ActionConfig for clicking (typically ClickOptions)
         * @return This builder instance for chaining
         */
        public Builder setClickActionConfig(ActionConfig clickActionConfig) {
            this.clickActionConfig = clickActionConfig;
            return this;
        }

        /**
         * Sets the objects to find for confirmation after clicking.
         * @param confirmationObjectCollection Images/regions that should appear after clicking
         * @return This builder instance for chaining
         */
        public Builder setConfirmationObjectCollection(ObjectCollection confirmationObjectCollection) {
            this.confirmationObjectCollection = confirmationObjectCollection;
            return this;
        }

        /**
         * Sets the configuration for confirmation find operations.
         * @param confirmActionConfig ActionConfig for confirmation (typically PatternFindOptions)
         * @return This builder instance for chaining
         */
        public Builder setConfirmActionConfig(ActionConfig confirmActionConfig) {
            this.confirmActionConfig = confirmActionConfig;
            return this;
        }

        /**
         * Builds the immutable SelectActionObjectV2 instance.
         * @return A new SelectActionObjectV2 with the configured settings
         */
        public SelectActionObjectV2 build() {
            SelectActionObjectV2 selectActionObject = new SelectActionObjectV2();
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