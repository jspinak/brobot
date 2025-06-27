package io.github.jspinak.brobot.action.composite.select;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import lombok.Getter;

/**
 * Data container for configuring and tracking the state of a Select operation.
 * <p>
 * This class encapsulates all the configuration needed to perform a selection operation,
 * including the swipe parameters, find options, click settings, and optional confirmation
 * verification. It also tracks the operation's results and state during execution.
 * <p>
 * A SelectActionObject contains:
 * <ul>
 * <li>Configuration for swipe operations (from/to locations, action options)</li>
 * <li>Configuration for finding target images</li>
 * <li>Configuration for clicking on found matches</li>
 * <li>Optional configuration for confirmation verification</li>
 * <li>Runtime state tracking (swipe count, matches found, success status)</li>
 * </ul>
 * <p>
 * This class uses the Builder pattern for construction to handle its many configuration
 * parameters. The runtime state fields are modified by the {@link Select} class during
 * operation execution.
 *
 * @see Select
 * @see CommonSelect
 */
@Getter
public class SelectActionObject {

    //build
    private int maxSwipes = 10;
    private ObjectCollection swipeFromObjColl;
    private ObjectCollection swipeToObjColl;
    private ActionOptions swipeActionOptions;
    private ObjectCollection findObjectCollection;
    private ActionOptions findActionOptions;
    private ActionOptions clickActionOptions;
    private ObjectCollection clickMatches; // defined after find operation
    private ObjectCollection confirmationObjectCollection; //when null don't confirm
    private ActionOptions confirmActionOptions;

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
     * This method is called by {@link Select} to store the results of each
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
     * Builder class for constructing SelectActionObject instances.
     * <p>
     * This builder provides a fluent interface for setting the numerous
     * configuration parameters needed for a select operation. All setter
     * methods return the builder instance for method chaining.
     */
    public static class Builder {
        private int maxSwipes = 10;
        private ObjectCollection swipeFromObjColl;
        private ObjectCollection swipeToObjColl;
        private ActionOptions swipeActionOptions;
        private ObjectCollection findObjectCollection;
        private ActionOptions findActionOptions;
        private ActionOptions clickActionOptions;
        private ObjectCollection confirmationObjectCollection; //when null don't confirm
        private ActionOptions confirmActionOptions;

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

        public Builder setSwipeActionOptions(ActionOptions swipeActionOptions) {
            this.swipeActionOptions = swipeActionOptions;
            return this;
        }

        public Builder setFindObjectCollection(ObjectCollection findObjectCollection) {
            this.findObjectCollection = findObjectCollection;
            return this;
        }

        public Builder setFindActionOptions(ActionOptions findActionOptions) {
            this.findActionOptions = findActionOptions;
            return this;
        }

        public Builder setClickActionOptions(ActionOptions clickActionOptions) {
            this.clickActionOptions = clickActionOptions;
            return this;
        }

        public Builder setConfirmationObjectCollection(ObjectCollection confirmationObjectCollection) {
            this.confirmationObjectCollection = confirmationObjectCollection;
            return this;
        }

        public Builder setConfirmActionOptions(ActionOptions confirmActionOptions) {
            this.confirmActionOptions = confirmActionOptions;
            return this;
        }

        public SelectActionObject build() {
            SelectActionObject selectActionObject = new SelectActionObject();
            selectActionObject.maxSwipes = maxSwipes;
            selectActionObject.swipeFromObjColl = swipeFromObjColl;
            selectActionObject.swipeToObjColl = swipeToObjColl;
            selectActionObject.swipeActionOptions = swipeActionOptions;
            selectActionObject.findObjectCollection = findObjectCollection;
            selectActionObject.findActionOptions = findActionOptions;
            selectActionObject.clickActionOptions = clickActionOptions;
            selectActionObject.confirmationObjectCollection = confirmationObjectCollection;
            selectActionObject.confirmActionOptions = confirmActionOptions;
            return selectActionObject;
        }
    }
}
