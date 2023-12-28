package io.github.jspinak.brobot.actions.methods.basicactions;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.time.Time;
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

    private Find find;
    private Time time;

    public WaitVanish(Find find, Time time) {
        this.find = find;
        this.time = time;
    }

    public void perform(Matches matches, ObjectCollection[] objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        actionOptions.setFind(ActionOptions.Find.EACH);
        time.setStartTime(ActionOptions.Action.VANISH); // this method shouldn't be called directly, but just in case...
        while (!time.expired(ActionOptions.Action.VANISH, actionOptions.getMaxWait()) && !matches.isEmpty()) {
            find.perform(matches, objectCollections[0]);
        }
    }

}