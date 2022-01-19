package actions.methods.basicactions;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.basicactions.find.Find;
import com.brobot.multimodule.actions.methods.sikuliWrappers.HighlightMatch;
import com.brobot.multimodule.actions.methods.sikuliWrappers.Wait;
import com.brobot.multimodule.database.primitives.match.MatchObject;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Highlight all Matches at once, or one at a time.
 */
@Component
public class Highlight implements ActionInterface {

    private Find find;
    private HighlightMatch highlightMatch;
    private Wait wait;

    public Highlight(Find find, HighlightMatch highlightMatch, Wait wait) {
        this.find = find;
        this.highlightMatch = highlightMatch;
        this.wait = wait;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = find.perform(actionOptions, objectCollections);
        if (actionOptions.isHighlightAllAtOnce()) highlightAllAtOnce(matches, actionOptions);
        else highlightOneAtATime(matches, actionOptions);
        return matches;
    }

    private void highlightAllAtOnce(Matches matches, ActionOptions actionOptions) {
        matches.getMatchObjects().forEach(matchObject ->
                highlightMatch.turnOn(matchObject.getMatch(), matchObject.getStateObject(), actionOptions));
        wait.wait(actionOptions.getHighlightSeconds());
        matches.getMatchObjects().forEach(matchObject ->
                highlightMatch.turnOff(matchObject.getMatch()));
    }

    private void highlightOneAtATime(Matches matches, ActionOptions actionOptions) {
        for (MatchObject matchObject : matches.getMatchObjects()) {
            highlightMatch.highlight(matchObject.getMatch(), matchObject.getStateObject(), actionOptions);
            if (matches.getMatchObjects().indexOf(matchObject) < matches.getMatchObjects().size() - 1)
                wait.wait(actionOptions.getPauseBetweenIndividualActions());
        }
    }
}
