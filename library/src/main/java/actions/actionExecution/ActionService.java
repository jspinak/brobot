package actions.actionExecution;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.basicactions.find.FindFunctions;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.stateObject.stateImageObject.StateImageObject;
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

    public Optional<ActionInterface> getAction(ActionOptions.Action action) {
        Optional<ActionInterface> actOpt = basicAction.getAction(action);
        if (actOpt.isPresent()) return actOpt;
        return compositeAction.getAction(action);
    }

    public void setCustomFind(BiFunction<ActionOptions, List<StateImageObject>, Matches> customFind) {
        findFunctions.addCustomFind(customFind);
    }

}
