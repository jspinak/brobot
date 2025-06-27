package io.github.jspinak.brobot.action.internal.service;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategyRegistry;
import io.github.jspinak.brobot.action.internal.execution.BasicActionRegistry;
import io.github.jspinak.brobot.action.internal.execution.CompositeActionRegistry;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Central service for resolving action implementations based on configuration.
 * <p>
 * ActionService acts as a factory that examines {@link ActionOptions} and returns
 * the appropriate {@link ActionInterface} implementation. It intelligently routes
 * between basic and composite actions based on the complexity of the requested operation.
 * <p>
 * <strong>Resolution logic:</strong>
 * <ul>
 * <li>Multiple Find operations → Composite FIND action</li>
 * <li>Single Find operation → Basic FIND action</li>
 * <li>Other actions → Checks basic registry first, then composite</li>
 * </ul>
 * <p>
 * This service encapsulates the complexity of action selection, allowing the
 * framework to transparently handle both simple and complex operations through
 * a uniform interface.
 *
 * @see BasicActionRegistry
 * @see CompositeActionRegistry
 * @see ActionInterface
 */
@Component
public class ActionService {

    private final BasicActionRegistry basicAction;
    private final CompositeActionRegistry compositeAction;
    private final FindStrategyRegistry findFunctions;

    /**
     * Constructs the ActionService with required action registries.
     *
     * @param basicAction Registry of atomic GUI operations
     * @param compositeAction Registry of multi-step operations
     * @param findFunctions Service for custom find implementations
     */
    public ActionService(BasicActionRegistry basicAction, CompositeActionRegistry compositeAction,
                         FindStrategyRegistry findFunctions) {
        this.basicAction = basicAction;
        this.compositeAction = compositeAction;
        this.findFunctions = findFunctions;
    }

    /**
     * Resolves the appropriate action implementation for the given options.
     * <p>
     * Examines the action type and configuration to determine whether a basic
     * or composite action is needed. The resolution follows these rules:
     * <ol>
     * <li>Multiple find actions require the composite Find implementation</li>
     * <li>Single find actions use the basic Find implementation</li>
     * <li>Other actions check basic registry first for performance</li>
     * <li>Falls back to composite registry if not found in basic</li>
     * </ol>
     *
     * @param actionOptions Configuration specifying the desired action
     * @return Optional containing the action implementation, or empty if not found
     */
    public Optional<ActionInterface> getAction(ActionOptions actionOptions) {
        if (actionOptions.getFindActions().size() > 1)
            return compositeAction.getAction(ActionOptions.Action.FIND);
        if (actionOptions.getFindActions().isEmpty() && actionOptions.getAction() == ActionOptions.Action.FIND)
            return basicAction.getAction(ActionOptions.Action.FIND);
        Optional<ActionInterface> actOpt = basicAction.getAction(actionOptions.getAction());
        if (actOpt.isPresent()) return actOpt;
        return compositeAction.getAction(actionOptions.getAction());
    }

    /**
     * Registers a custom Find implementation for one-time use.
     * <p>
     * Allows runtime registration of specialized find logic without modifying
     * the core framework. The custom implementation will be available for
     * actions configured with {@link ActionOptions.Find#CUSTOM}.
     * <p>
     * Custom finds are useful for:
     * <ul>
     * <li>Application-specific pattern matching</li>
     * <li>Complex multi-stage searches</li>
     * <li>Integration with external vision systems</li>
     * </ul>
     *
     * @param customFind BiConsumer that accepts ActionResult to populate and
     *                   ObjectCollections to search within
     */
    public void setCustomFind(BiConsumer<ActionResult, List<ObjectCollection>> customFind) {
        findFunctions.addCustomFind(customFind);
    }

}
