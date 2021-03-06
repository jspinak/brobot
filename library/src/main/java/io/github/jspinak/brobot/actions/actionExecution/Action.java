package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This class is the entry point for processing operations. It reads the action to be
 * performed in the ActionOptions object and calls the corresponding methods. Currently,
 * there are two main types of Actions, Basic and Composite, each with their own
 * methods. Both Basic and Composite Actions follow the ActionInterface.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class Action {

    private ActionExecution actionExecution;
    private ActionService actionService;

    public Action(ActionExecution actionExecution, ActionService actionService) {
        this.actionExecution = actionExecution;
        this.actionService = actionService;
    }

    /**
     * All other methods in this class return this method.
     *
     * @param actionOptions     contains all information about the action.
     * @param objectCollections contains all objects to be acted on.
     * @return a Matches object with all results of the action.
     */
    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        for (ObjectCollection objColl : objectCollections) objColl.resetTimesActedOn();
        Optional<ActionInterface> action = actionService.getAction(actionOptions);
        if (action.isEmpty()) {
            Report.println("Not a valid Action.");
            return new Matches();
        }
        return actionExecution.perform(action.get(), actionOptions, objectCollections);
    }

    /**
     * The default ActionOptions is a Find Action.
     * @param stateImageObjects the images to include in the ObjectCollection
     * @return the results of the Find Action
     */
    public Matches find(StateImageObject... stateImageObjects) {
        return perform(new ActionOptions(), stateImageObjects);
    }

    /**
     * The default ActionOptions is a Find Action.
     * @param objectCollections the objects to find
     * @return the results of the Find Action on the objectCollections
     */
    public Matches find(ObjectCollection... objectCollections) {
        return perform(new ActionOptions(), objectCollections);
    }

    /**
     * All StateImageObjects are placed in the first ObjectCollection.
     * @param actionOptions holds the configuration of the action.
     * @param stateImageObjects these will be added to a new ObjectCollection.
     * @return a Matches object with the results of the operation.
     */
    public Matches perform(ActionOptions actionOptions, StateImageObject... stateImageObjects) {
        return perform(actionOptions, new ObjectCollection.Builder().withImages(stateImageObjects).build());
    }

    /**
     * Perform an Action on an empty Object Collections. This method is necessary since there
     * are two different methods that take ActionOptions and a varargs parameter.
     * @param actionOptions holds the configuration of the action.
     * @return a Matches object with the results of the operation.
     */
    public Matches perform(ActionOptions actionOptions) {
        return perform(actionOptions, new ObjectCollection.Builder().build());
    }

    /**
     * Perform an Action with default options.
     * @param action the action to perform
     * @param objectCollections contains all objects to be acted on.
     * @return a Matches object with the results of the operation.
     */
    public Matches perform(ActionOptions.Action action, ObjectCollection... objectCollections) {
        return perform(new ActionOptions.Builder().setAction(action).build(), objectCollections);
    }

    /**
     * Perform an Action with default options.
     * All StateImageObjects are placed in the first ObjectCollection.
     * @param action the action to perform
     * @param stateImageObjects these will be added to a new ObjectCollection.
     * @return a Matches object with the results of the operation.
     */
    public Matches perform(ActionOptions.Action action, StateImageObject... stateImageObjects) {
        return perform(new ActionOptions.Builder().setAction(action).build(), stateImageObjects);
    }

    /**
     * Perform an Action with default options and no associated ObjectCollections.
     * @param action the action to perform
     * @return a Matches object with the results of the operation.
     */
    public Matches perform(ActionOptions.Action action) {
        return perform(new ActionOptions.Builder().setAction(action).build(), new ObjectCollection.Builder().build());
    }

    /**
     * Perform an Action with default options.
     * All Strings are placed in the first ObjectCollection.
     * @param action the action to perform
     * @param strings are added to a new ObjectCollection.
     * @return a Matches object with the results of the operation.
     */
    public Matches perform(ActionOptions.Action action, String... strings) {
        ObjectCollection strColl = new ObjectCollection.Builder().withStrings(strings).build();
        return perform(action, strColl);
    }

    public Matches perform(ActionOptions actionOptions, String... strings) {
        ObjectCollection strColl = new ObjectCollection.Builder().withStrings(strings).build();
        return perform(actionOptions, strColl);
    }

}
