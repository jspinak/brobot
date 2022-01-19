package actions.actionExecution;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;

/**
 * Actions that are run from the Action class need to follow this interface.
 */
public interface ActionInterface {

    Matches perform(
            ActionOptions actionOptions,
            ObjectCollection... objectCollections);
}
