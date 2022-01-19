package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.state.ObjectCollection;

/**
 * Actions that are run from the Action class need to follow this interface.
 */
public interface ActionInterface {

    Matches perform(
            ActionOptions actionOptions,
            ObjectCollection... objectCollections);
}
