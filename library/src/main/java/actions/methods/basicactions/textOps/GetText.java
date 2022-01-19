package actions.methods.basicactions.textOps;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.basicactions.find.Find;
import com.brobot.multimodule.actions.methods.sikuliWrappers.Wait;
import com.brobot.multimodule.actions.methods.sikuliWrappers.text.GetTextWrapper;
import com.brobot.multimodule.actions.methods.time.Time;
import com.brobot.multimodule.database.primitives.match.MatchObject;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.primitives.text.Text;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.stringUtils.TextSelector;
import org.springframework.stereotype.Component;

import static com.brobot.multimodule.actions.actionOptions.ActionOptions.GetTextUntil.TEXT_APPEARS;
import static com.brobot.multimodule.actions.actionOptions.ActionOptions.GetTextUntil.TEXT_VANISHES;
import static com.brobot.multimodule.stringUtils.TextSelector.Method.MOST_SIMILAR;

/**
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

    private Find find;
    private Wait wait;
    private TextSelector textSelector;
    private Time time;
    private GetTextWrapper getTextWrapper;

    public GetText(Find find, Wait wait, TextSelector textSelector, Time time,
                   GetTextWrapper getTextWrapper) {
        this.find = find;
        this.wait = wait;
        this.textSelector = textSelector;
        this.time = time;
        this.getTextWrapper = getTextWrapper;
    }

    /**
     * Failure of the GetText Action may be a result of failure in the Find operation or failure
     * in the text retrieval executed by the GetTextWrapper. If the Find operation is the cause
     * of failure, we need to search again for matches. If text retrieval is the problem, we should
     * keep the MatchObjects and look again for text.
     *
     * @param actionOptions
     * @param objectCollections
     * @return a Matches object with MatchObjects and Text
     */
    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = find.perform(actionOptions, objectCollections);
        getTextWrapper.getAllText(matches);
        Text text;
        int repetitions = 0;
        while (!time.expired(actionOptions.getAction(), actionOptions.getMaxWait())) {
            repetitions++;
            if (exitIterations(repetitions, actionOptions, matches)) break;
            wait.wait(actionOptions.getPauseBetweenIndividualActions());
            text = matches.getText(); // save the text found until this point
            /*
              If not all images are found, create a new Matches object and
              populate it with the new results. Here, we assume that the main problem
              is the Find operation and not text retrieval.
             */
            if (!matches.getDanglingSnapshots().allImagesFound()) {
                matches = find.perform(actionOptions, objectCollections);
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
        matches.setSelectedText(textSelector.getString(MOST_SIMILAR, matches.getText()));
        return matches;
    }

    private boolean exitIterations(int repetitions, ActionOptions actionOptions, Matches matches) {
        ActionOptions.GetTextUntil condition = actionOptions.getGetTextUntil();
        if (repetitions == actionOptions.getTimesToRepeatIndividualAction()) return true;
        return (condition == TEXT_VANISHES && noMatchesHaveText(matches)) ||
                (condition == TEXT_APPEARS && allMatchesHaveText(matches));
    }

    private boolean allMatchesHaveText(Matches matches) {
        for (MatchObject matchObject : matches.getMatchObjects()) {
            if (matchObject.getText().isEmpty()) return false;
        }
        return true;
    }

    private boolean noMatchesHaveText(Matches matches) {
        for (MatchObject matchObject : matches.getMatchObjects()) {
            if (!matchObject.getText().isEmpty()) return false;
        }
        return true;
    }
    
}
