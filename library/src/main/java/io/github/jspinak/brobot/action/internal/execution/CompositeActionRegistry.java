package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.composite.drag.Drag;
import io.github.jspinak.brobot.action.composite.drag.SimpleDrag;
import io.github.jspinak.brobot.action.composite.multiple.finds.MultipleFinds;
import io.github.jspinak.brobot.action.composite.repeat.ClickUntil;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for composite actions that orchestrate multiple basic actions.
 * <p>
 * CompositeActions represent higher-level GUI automation operations built by
 * combining multiple {@link BasicActionRegistry} primitives. These actions typically
 * involve complex workflows such as:
 * <ul>
 * <li>Multiple find operations to locate different elements</li>
 * <li>Sequential actions with intermediate validation</li>
 * <li>Conditional execution based on GUI state</li>
 * <li>Retry logic with termination conditions</li>
 * </ul>
 * <p>
 * This registry pattern allows the framework to treat composite actions
 * identically to basic actions through the common {@link ActionInterface},
 * supporting the model-based GUI automation approach where complex behaviors
 * emerge from composing simpler primitives.
 * <p>
 * Currently registered composite actions:
 * <ul>
 * <li>{@link Drag}: Click-and-drag operations between two points</li>
 * <li>{@link ClickUntil}: Repeated clicking until a condition is met</li>
 * <li>{@link MultipleFinds}: Finding multiple patterns in sequence</li>
 * </ul>
 *
 * @see BasicActionRegistry
 * @see ActionInterface
 * @see ActionExecution
 */
@Component
public class CompositeActionRegistry {

    /**
     * Registry mapping composite action types to their implementations.
     * Provides consistent lookup interface matching BasicAction's design.
     */
    private Map<ActionOptions.Action, ActionInterface> actions = new HashMap<>();

    /**
     * Constructs the CompositeAction registry with available composite implementations.
     * <p>
     * Uses dependency injection to register composite action implementations that
     * can be resolved at runtime based on {@link ActionOptions} configuration.
     * Each composite action encapsulates a multi-step workflow.
     *
     * @param drag Performs click-and-drag operations by combining mouse down,
     *             move, and mouse up actions with find operations
     * @param clickUntil Repeatedly clicks until a specified condition is met,
     *                   useful for dismissing dialogs or waiting for state changes
     * @param multipleFinds Executes find operations for multiple patterns,
     *                      supporting complex multi-element interactions
     */
    public CompositeActionRegistry(SimpleDrag drag, ClickUntil clickUntil, MultipleFinds multipleFinds) {
        actions.put(ActionOptions.Action.DRAG, drag);
        actions.put(ActionOptions.Action.CLICK_UNTIL, clickUntil);
        actions.put(ActionOptions.Action.FIND, multipleFinds);
    }

    /**
     * Retrieves the composite action implementation for the specified type.
     * <p>
     * This factory method mirrors {@link BasicActionRegistry#getAction}, providing
     * a unified interface for action resolution across both basic and composite
     * actions. Returns an empty Optional if the action type is not registered
     * as a composite action.
     * <p>
     * Note that some action types (like FIND) may be registered in both
     * BasicAction and CompositeAction registries with different implementations
     * based on the complexity of the operation.
     *
     * @param action The composite action type to retrieve
     * @return Optional containing the action implementation, or empty if not found
     * @see ActionInterface
     * @see BasicActionRegistry#getAction
     */
    public Optional<ActionInterface> getAction(ActionOptions.Action action) {
        return Optional.ofNullable(actions.get(action));
    }
}
