package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.FindFunctions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Returns the corresponding Action class as specified in ActionOptions.
 */
@Component
public class ActionService {

    private final BasicAction basicAction;
    private final CompositeAction compositeAction;
    private final FindFunctions findFunctions;

    public ActionService(BasicAction basicAction, CompositeAction compositeAction,
                         FindFunctions findFunctions) {
        this.basicAction = basicAction;
        this.compositeAction = compositeAction;
        this.findFunctions = findFunctions;
    }

    public Optional<ActionInterface> getAction(ActionOptions actionOptions) {
        if (actionOptions.getFindActions().size() > 1)
            return compositeAction.getAction(ActionOptions.Action.FIND);
        Optional<ActionInterface> actOpt = basicAction.getAction(actionOptions.getAction());
        if (actOpt.isPresent()) return actOpt;
        return compositeAction.getAction(actionOptions.getAction());
    }

    public void setCustomFind(BiFunction<ActionOptions, List<ObjectCollection>, Matches> customFind) {
        findFunctions.addCustomFind(customFind);
    }

}
