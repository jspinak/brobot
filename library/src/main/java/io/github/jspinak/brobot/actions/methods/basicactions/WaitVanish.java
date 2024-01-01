package io.github.jspinak.brobot.actions.methods.basicactions;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Returns a successful Matches object if at some point no objects are found.
 * Returns an unsuccessful Matches object when at least one object exists for the entire wait period.
 * The Matches object will contain a MatchObject for each Image found the last time Find was successful.
 * Only uses ObjectCollection #1
 */
@Component
public class WaitVanish implements ActionInterface {

    private final Find find;
    private final ActionLifecycleManagement actionLifecycleManagement;

    public WaitVanish(Find find, ActionLifecycleManagement actionLifecycleManagement) {
        this.find = find;
        this.actionLifecycleManagement = actionLifecycleManagement;
    }

    public void perform(Matches matches, ObjectCollection[] objectCollections) {
        matches.getActionOptions().setFind(ActionOptions.Find.EACH);
        while (actionLifecycleManagement.isOkToContinueAction(matches, objectCollections[0].getStateImages().size())) {
            find.perform(matches, objectCollections[0]);
        }
    }

}