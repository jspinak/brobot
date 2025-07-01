package io.github.jspinak.brobot.exception;

import io.github.jspinak.brobot.action.ActionInterface;

/**
 * Thrown when an action fails during execution.
 * <p>
 * This exception indicates that a specific action (Click, Find, Type, etc.) 
 * could not complete successfully. It provides context about which action failed
 * and why, enabling the framework to make intelligent decisions about recovery
 * strategies.
 * </p>
 * 
 * @since 1.0
 */
public class ActionFailedException extends BrobotRuntimeException {
    
    private final ActionInterface.Type actionType;
    private final String actionDetails;
    
    /**
     * Constructs a new action failed exception with details about the failed action.
     *
     * @param actionType the type of action that failed
     * @param message a description of why the action failed
     */
    public ActionFailedException(ActionInterface.Type actionType, String message) {
        super(String.format("Action %s failed: %s", actionType, message));
        this.actionType = actionType;
        this.actionDetails = message;
    }
    
    /**
     * Constructs a new action failed exception with details and underlying cause.
     *
     * @param actionType the type of action that failed
     * @param message a description of why the action failed
     * @param cause the underlying cause of the failure
     */
    public ActionFailedException(ActionInterface.Type actionType, String message, Throwable cause) {
        super(String.format("Action %s failed: %s", actionType, message), cause);
        this.actionType = actionType;
        this.actionDetails = message;
    }
    
    /**
     * Gets the type of action that failed.
     *
     * @return the action type
     */
    public ActionInterface.Type getActionType() {
        return actionType;
    }
    
    /**
     * Gets detailed information about why the action failed.
     *
     * @return the failure details
     */
    public String getActionDetails() {
        return actionDetails;
    }
}