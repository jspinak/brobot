package io.github.jspinak.brobot.actions.methods.basicactions;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.HighlightMatch;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.illustratedHistory.IllustrateScreenshot;
import org.springframework.stereotype.Component;

/**
 * Highlight all Matches at once, or one at a time.
 */
@Component
public class Highlight implements ActionInterface {

    private Find find;
    private HighlightMatch highlightMatch;
    private Wait wait;
    private IllustrateScreenshot illustrateScreenshot;

    public Highlight(Find find, HighlightMatch highlightMatch, Wait wait,
                     IllustrateScreenshot illustrateScreenshot) {
        this.find = find;
        this.highlightMatch = highlightMatch;
        this.wait = wait;
        this.illustrateScreenshot = illustrateScreenshot;
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
