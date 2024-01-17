package io.github.jspinak.brobot.actions.methods.basicactions.textOps;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.text.GetTextWrapper;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.text.Text;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.stringUtils.TextSelector;
import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.GetTextUntil.TEXT_APPEARS;
import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.GetTextUntil.TEXT_VANISHES;

/**
 * Replaced by Find with text search
 * TODO: text appears, disappears. check code to make sure you don't need it and then delete unused classes.
 *
 * GetText retrieves text from a Region.
 * Options include:
 *   Waiting for text to appear: the action terminates when all Match regions have text.
 *   Waiting for text to disappear: the action terminates when no Match regions have text.
 * If one of these conditions is not set, GetText will continue to find and save Strings in
 *   its MatchObjects until it reaches the time or iteration limits set in the ActionOptions.
 * These Strings can be retrieved with the class TextSelector, which makes decisions about which
 *   saved String most likely captures the real text.
 */
@Component
public class GetText implements ActionInterface {

    private final Find find;
    private final TextSelector textSelector;
    private final Time time;
    private final GetTextWrapper getTextWrapper;
    private final ActionLifecycleManagement actionLifecycleManagement;

    public GetText(Find find, TextSelector textSelector, Time time,
                   GetTextWrapper getTextWrapper, ActionLifecycleManagement actionLifecycleManagement) {
        this.find = find;
        this.textSelector = textSelector;
        this.time = time;
        this.getTextWrapper = getTextWrapper;
        this.actionLifecycleManagement = actionLifecycleManagement;
    }

    /**
     * Failure of the GetText Action may be a result of failure in the Find operation or failure
     * in the text retrieval executed by the GetTextWrapper. If the Find operation is the cause
     * of failure, we need to search again for matches. If text retrieval is the problem, we should
     * keep the MatchObjects and look again for text.
     *
     * @param matches has the ActionOptions and existing matches
     * @param objectCollections holds the objects to act on
     */
    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        find.perform(matches, objectCollections);
        getTextWrapper.getAllText(matches);
        Text text;
        int repetitions = 0;
        while (actionLifecycleManagement.isOkToContinueAction(matches, objectCollections[0].getStateImages().size())) {
            repetitions++;
            if (exitIterations(repetitions, actionOptions, matches)) break;
            time.wait(actionOptions.getPauseBetweenIndividualActions());
            text = matches.getText(); // save the text found until this point
            /*
              If not all images are found, create a new Matches object and
              populate it with the new results. Here, we assume that the main problem
              is the Find operation and not text retrieval.
             */
            if (!matches.getDanglingSnapshots().allImagesFound()) {
                find.perform(matches, objectCollections);
                /*
                  Ideally, you should merge the old MatchObjects and Snapshots.
                  Duplicate MatchObjects and found Strings should not be copied.
                */
                matches.getText().addAll(text); // add all text from previous iterations. this is a temp solution.
            }
            /*
              Text retrieval adds text to the MatchObjects, the Text field of Matches, and the Snapshots.
             */
            else getTextWrapper.getAllText(matches);
        }
        matches.setSelectedText(textSelector.getString(TextSelector.Method.MOST_SIMILAR, matches.getText()));
    }

    private boolean exitIterations(int repetitions, ActionOptions actionOptions, Matches matches) {
        ActionOptions.GetTextUntil condition = actionOptions.getGetTextUntil();
        if (repetitions == actionOptions.getTimesToRepeatIndividualAction()) return true;
        return (condition == TEXT_VANISHES && noMatchesHaveText(matches)) ||
                (condition == TEXT_APPEARS && allMatchesHaveText(matches));
    }

    private boolean allMatchesHaveText(Matches matches) {
        for (Match match : matches.getMatchList()) {
            if (match.getText().isEmpty()) return false;
        }
        return true;
    }

    private boolean noMatchesHaveText(Matches matches) {
        for (Match match : matches.getMatchList()) {
            if (!match.getText().isEmpty()) return false;
        }
        return true;
    }
    
}
