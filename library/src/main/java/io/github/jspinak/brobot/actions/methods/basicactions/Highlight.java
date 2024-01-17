package io.github.jspinak.brobot.actions.methods.basicactions;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.HighlightMatch;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Highlight all Matches at once, or one at a time.
 */
@Component
public class Highlight implements ActionInterface {

    private final Find find;
    private final HighlightMatch highlightMatch;
    private final Time time;

    public Highlight(Find find, HighlightMatch highlightMatch, Time time) {
        this.find = find;
        this.highlightMatch = highlightMatch;
        this.time = time;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        find.perform(matches, objectCollections);
        if (actionOptions.isHighlightAllAtOnce()) highlightAllAtOnce(matches, actionOptions);
        else highlightOneAtATime(matches, actionOptions);
    }

    private void highlightAllAtOnce(Matches matches, ActionOptions actionOptions) {
        matches.getMatchList().forEach(match ->
                highlightMatch.turnOn(match, match.getStateObjectData(), actionOptions));
        time.wait(actionOptions.getHighlightSeconds());
        matches.getMatchList().forEach(highlightMatch::turnOff);
    }

    private void highlightOneAtATime(Matches matches, ActionOptions actionOptions) {
        for (Match match : matches.getMatchList()) {
            highlightMatch.highlight(match, match.getStateObjectData(), actionOptions);
            if (matches.getMatchList().indexOf(match) < matches.getMatchList().size() - 1)
                time.wait(actionOptions.getPauseBetweenIndividualActions());
        }
    }
}
