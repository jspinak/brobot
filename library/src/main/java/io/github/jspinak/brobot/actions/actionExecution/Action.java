package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.report.Report;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Entry point for executing GUI automation actions in the Brobot model-based framework.
 * 
 * <p>The Action class serves as the central dispatcher for all GUI operations, implementing 
 * the Action Model (α) described in the theoretical foundations. It processes 
 * ActionOptions to determine what operation to perform and delegates execution to the 
 * appropriate action implementation using the action function f_α.</p>
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Parse ActionOptions to identify the requested action type</li>
 *   <li>Route execution to Basic or Composite action implementations</li>
 *   <li>Manage the action lifecycle and error handling</li>
 *   <li>Return comprehensive results via Matches objects</li>
 * </ul>
 * </p>
 * 
 * <p>Action types supported:
 * <ul>
 *   <li><b>Basic Actions</b>: Atomic operations like Find, Click, Type, Drag</li>
 *   <li><b>Composite Actions</b>: Complex operations that combine multiple basic actions</li>
 * </ul>
 * </p>
 * 
 * <p>This class abstracts the complexity of GUI interaction, allowing automation code to 
 * focus on what to do rather than how to do it. The model-based approach ensures actions 
 * are executed in the context of the current State, making them more reliable and robust.</p>
 * 
 * @since 1.0
 * @see ActionOptions
 * @see ActionInterface
 * @see BasicAction
 * @see CompositeAction
 * @see Matches
 * @author Joshua Spinak
 */
@Component
public class Action {

    private final ActionExecution actionExecution;
    private final ActionService actionService;

    public Action(ActionExecution actionExecution, ActionService actionService) {
        this.actionExecution = actionExecution;
        this.actionService = actionService;
    }

    /**
     * Executes a GUI automation action with the specified options and target objects.
     * 
     * <p>This is the primary method for performing GUI operations in Brobot. It processes
     * the action configuration and executes it against the provided GUI elements.</p>
     * 
     * @param actionOptions     configuration specifying the action type and parameters
     * @param objectCollections target GUI elements to act upon (images, regions, locations, etc.)
     * @return a Matches object containing all results from the action execution
     */
    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        return perform("", actionOptions, objectCollections);
    }

    /**
     * Executes a GUI automation action with a descriptive label for logging and debugging.
     * 
     * <p>This method adds a human-readable description to the action execution, which is
     * valuable for debugging, logging, and understanding automation flow. The description
     * appears in reports and illustrated histories.</p>
     * 
     * @param actionDescription human-readable description of what this action accomplishes
     * @param actionOptions     configuration specifying the action type and parameters
     * @param objectCollections target GUI elements to act upon
     * @return a Matches object containing all results from the action execution
     */
    public Matches perform(String actionDescription, ActionOptions actionOptions, ObjectCollection... objectCollections) {
        for (ObjectCollection objColl : objectCollections) objColl.resetTimesActedOn();
        Optional<ActionInterface> action = actionService.getAction(actionOptions);
        if (action.isEmpty()) {
            Report.println("Not a valid Action.");
            return new Matches();
        }
        return actionExecution.perform(action.get(), actionDescription, actionOptions, objectCollections);
    }

    /**
     * The default ActionOptions is a Find Action.
     * @param stateImages the images to include in the ObjectCollection
     * @return the results of the Find Action
     */
    public Matches find(StateImage... stateImages) {
        return perform(new ActionOptions(), stateImages);
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
     * All StateImages are placed in the first ObjectCollection.
     * @param actionOptions holds the configuration of the action.
     * @param stateImages these will be added to a new ObjectCollection.
     * @return a Matches object with the results of the operation.
     */
    public Matches perform(ActionOptions actionOptions, StateImage... stateImages) {
        return perform(actionOptions, new ObjectCollection.Builder().withImages(stateImages).build());
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
     * All StateImages are placed in the first ObjectCollection.
     * @param action the action to perform
     * @param stateImages these will be added to a new ObjectCollection.
     * @return a Matches object with the results of the operation.
     */
    public Matches perform(ActionOptions.Action action, StateImage... stateImages) {
        return perform(new ActionOptions.Builder().setAction(action).build(), stateImages);
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

    public Matches perform(ActionOptions.Action action, Region... regions) {
        ObjectCollection strColl = new ObjectCollection.Builder().withRegions(regions).build();
        return perform(action, strColl);
    }

}
