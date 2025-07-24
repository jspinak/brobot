package io.github.jspinak.brobot.action.composite.repeat;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.composite.multiple.actions.MultipleActionsObject;
import io.github.jspinak.brobot.action.composite.multiple.actions.MultipleBasicActions;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static io.github.jspinak.brobot.action.internal.options.ActionOptions.Action.*;

/**
 * Repeatedly clicks elements until a specified condition is met or timeout occurs.
 * <p>
 * ClickUntil implements a conditional clicking pattern that continues clicking target
 * elements until either objects appear or vanish from the screen. This is essential
 * for handling dynamic UI elements that require multiple clicks to dismiss, activate,
 * or reveal other elements. The action combines clicking behavior with conditional
 * monitoring to create robust interaction patterns.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Continuous clicking until condition is satisfied</li>
 *   <li>Two termination conditions: OBJECTS_APPEAR or OBJECTS_VANISH</li>
 *   <li>Configurable timeout to prevent infinite loops</li>
 *   <li>Flexible target specification (same or different objects)</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Dismissing multiple popup dialogs or notifications</li>
 *   <li>Clicking "Next" until a specific screen appears</li>
 *   <li>Clearing items from a list until empty</li>
 *   <li>Clicking buttons until they become disabled/disappear</li>
 *   <li>Navigating through wizards or multi-step processes</li>
 * </ul>
 * 
 * <p><b>ObjectCollection usage patterns:</b></p>
 * <ul>
 *   <li><b>1 collection:</b> Click objects until they disappear (e.g., dismiss popups)</li>
 *   <li><b>2 collections:</b> Click collection 1 until collection 2 appears/disappears
 *       (e.g., click "Next" until "Finish" appears)</li>
 * </ul>
 * 
 * @deprecated Use {@link RepeatUntilConfig} with appropriate action configurations instead.
 *             RepeatUntilConfig provides more flexibility by allowing any action type to be
 *             repeated with custom termination conditions.
 * 
 * <p>The action internally converts ClickUntil conditions to basic actions:
 * OBJECTS_VANISH → VANISH action, OBJECTS_APPEAR → FIND action. This design
 * allows reuse of existing action implementations while providing a cleaner API.</p>
 * 
 * <p>For more complex scenarios requiring different ActionOptions for click and
 * monitoring actions, use {@link RepeatUntilConfig} directly.</p>
 * 
 * @see RepeatUntilConfig
 * @see MultipleBasicActions
 * @see ActionOptions.ClickUntil
 */
@Component
@Deprecated
public class ClickUntil implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.CLICK_UNTIL;
    }

    private final MultipleBasicActions multipleBasicActions;

    private Map<ActionOptions.ClickUntil, ActionOptions.Action> clickUntilConversion = new HashMap<>();
    {
        clickUntilConversion.put(ActionOptions.ClickUntil.OBJECTS_VANISH, VANISH);
        clickUntilConversion.put(ActionOptions.ClickUntil.OBJECTS_APPEAR, FIND);
    }

    public ClickUntil(MultipleBasicActions multipleBasicActions) {
        this.multipleBasicActions = multipleBasicActions;
    }

    /**
     * Executes repeated clicks until the specified condition is met or timeout occurs.
     * <p>
     * This method orchestrates a click-and-check loop by creating a composite action
     * that alternates between clicking target elements and checking for the termination
     * condition. The loop continues until either the condition is satisfied or the
     * operation times out (based on ActionOptions.maxWait).
     * 
     * <p><b>Execution flow:</b></p>
     * <ol>
     *   <li>Extracts click targets (collection 1) and condition targets (collection 2 or 1)</li>
     *   <li>Creates separate ActionOptions for CLICK and condition check actions</li>
     *   <li>Converts ClickUntil condition to corresponding action (VANISH or FIND)</li>
     *   <li>Builds MultipleActionsObject with click-check sequence</li>
     *   <li>Delegates execution to MultipleBasicActions</li>
     * </ol>
     * 
     * <p><b>ObjectCollection behavior:</b></p>
     * <ul>
     *   <li><b>1 collection:</b> Both click and condition check use same objects.
     *       Example: Click popup close buttons until they disappear</li>
     *   <li><b>2 collections:</b> Click collection 1, check condition on collection 2.
     *       Example: Click "Load More" until specific content appears</li>
     * </ul>
     * 
     * <p>The method shares ActionOptions between click and condition actions, which
     * means timing, search regions, and other settings apply to both. For different
     * settings per action, use DoUntilActionObject directly.</p>
     * 
     * @param matches The ActionResult containing action configuration. The ActionOptions
     *                within this object control behavior for both click and condition actions.
     * @param objectCollections 1 or 2 collections: [0] = click targets, [1] = condition
     *                          targets (if provided, otherwise [0] is used for both)
     */
    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - expecting ClickUntilOptions
        ActionConfig config = matches.getActionConfig();
        
        // Extract the condition
        ClickUntilOptions.Condition condition = ClickUntilOptions.Condition.OBJECTS_APPEAR;
        if (config instanceof ClickUntilOptions) {
            ClickUntilOptions clickUntilOptions = (ClickUntilOptions) config;
            condition = clickUntilOptions.getCondition();
        }
        
        ObjectCollection coll1 = objectCollections[0]; //the 'click' collection
        ObjectCollection coll2 = objectCollections[0]; //the 'until' collection
        if (objectCollections.length > 1) coll2 = objectCollections[1];
        
        // TODO: Update MultipleBasicActions to accept ActionConfig
        // For now, create temporary ActionOptions
        ActionOptions tempOptions = createTemporaryActionOptions(config);
        
        ActionOptions click = new ActionOptions.Builder(tempOptions).build();
        click.setAction(CLICK);
        
        ActionOptions untilAction = new ActionOptions.Builder(tempOptions).build();
        // Map condition to action
        ActionOptions.ClickUntil clickUntilEnum = condition == ClickUntilOptions.Condition.OBJECTS_VANISH 
            ? ActionOptions.ClickUntil.OBJECTS_VANISH 
            : ActionOptions.ClickUntil.OBJECTS_APPEAR;
        untilAction.setAction(clickUntilConversion.get(clickUntilEnum));
        
        System.out.print(", until action is "+untilAction.getAction());
        MultipleActionsObject mao = new MultipleActionsObject();
        mao.addActionOptionsObjectCollectionPair(click, coll1);
        mao.addActionOptionsObjectCollectionPair(untilAction, coll2);
        multipleBasicActions.perform(mao);
    }
    
    private ActionOptions createTemporaryActionOptions(ActionConfig config) {
        ActionOptions tempOptions = new ActionOptions();
        tempOptions.setPauseBeforeBegin(config.getPauseBeforeBegin());
        tempOptions.setPauseAfterEnd(config.getPauseAfterEnd());
        return tempOptions;
    }

    /**
     * Configures success criteria based on the ClickUntil condition type.
     * <p>
     * This unused method demonstrates an alternative approach to setting success criteria
     * dynamically within the action implementation rather than relying on external
     * configuration. It provides more flexibility for complex scenarios where success
     * conditions might depend on runtime state or the specific objects being acted upon.
     * 
     * <p>The method sets appropriate success criteria lambdas based on the ClickUntil type:</p>
     * <ul>
     *   <li><b>OBJECTS_APPEAR:</b> Success when matches are found (!isEmpty)</li>
     *   <li><b>OBJECTS_VANISH:</b> Success when no matches found (isEmpty)</li>
     *   <li><b>No collections:</b> Always fails as there's nothing to check</li>
     * </ul>
     * 
     * <p><b>Note:</b> This method is currently unused but retained as it demonstrates
     * a valuable pattern for dynamic success criteria configuration that could be
     * useful for more sophisticated composite actions.</p>
     * 
     * @param actionOptions The action configuration to modify with success criteria.
     *                      This parameter is modified by setting its success criteria lambda.
     * @param objectCollections The objects being acted upon, used to validate that
     *                          there are targets available for the operation
     */
    private void setSuccessEvaluation(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (objectCollections.length < 1) {
            actionOptions.setSuccessCriteria(matches -> false);
        }
        if (actionOptions.getClickUntil() == ActionOptions.ClickUntil.OBJECTS_APPEAR)
            actionOptions.setSuccessCriteria(matches -> !matches.isEmpty());
        if (actionOptions.getClickUntil() == ActionOptions.ClickUntil.OBJECTS_VANISH)
            actionOptions.setSuccessCriteria(ActionResult::isEmpty);
    }
}
