package actions.methods.basicactions;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.basicactions.find.Find;
import com.brobot.multimodule.actions.methods.time.Time;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
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

    public Matches perform(ActionOptions actionOptions, ObjectCollection[] objectCollections) {
        actionOptions.setFind(ActionOptions.Find.EACH);
        Matches matches = new Matches();
        time.setStartTime(ActionOptions.Action.VANISH); // this method shouldn't be called directly, but just in case...
        while (!time.expired(ActionOptions.Action.VANISH, actionOptions.getMaxWait()) && !matches.isEmpty()) {
            matches = find.perform(actionOptions, objectCollections[0]);
        }
        return matches;
    }

}