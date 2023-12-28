package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;

/**
 * Actions that are run from the Action class need to follow this interface.
 */
public interface ActionInterface {

    void perform(Matches matches, ObjectCollection... objectCollections);
}
