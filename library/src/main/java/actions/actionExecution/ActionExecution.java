package actions.actionExecution;

import com.brobot.multimodule.actions.actionConfigurations.ExitSequences;
import com.brobot.multimodule.actions.actionConfigurations.Success;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.sikuliWrappers.Wait;
import com.brobot.multimodule.actions.methods.time.Time;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.database.state.stateObject.stateImageObject.StateImageObject;
import com.brobot.multimodule.reports.Output;
import com.brobot.multimodule.reports.Report;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Performs the Action and maintenance around the Action such as
 *   - Setting the start time and saving the operation's duration
 *   - Executing pauses before and after the operation
 *   - Determining the success of the operation
 *   - Saving the MatchSnapshots
 *
 * Any Action that is run independently of this class should
 *   have code to perform maintenance around the Action.
 */
@Component
public class ActionExecution {

    private Wait wait;
    private Time time;
    private Success success;
    private ExitSequences exitSequences;

    public ActionExecution(Wait wait, Time time, Success success, ExitSequences exitSequences) {
        this.wait = wait;
        this.time = time;
        this.success = success;
        this.exitSequences = exitSequences;
    }

    /**
     * Performs the Action and certain maintenance functions.
     * @param actionMethod has Action-specific code.
     * @param actionOptions contains information about the Action to be performed.
     * @param objectCollections contains the objects on which to perform the Action.
     * @return Matches
     */
    public Matches perform(ActionInterface actionMethod, ActionOptions actionOptions,
                           ObjectCollection... objectCollections) {
        printAction(actionOptions, objectCollections);
        time.setStartTime(actionOptions.getAction());
        wait.wait(actionOptions.getPauseBeforeBegin());
        Matches matches = new Matches();
        for (int i=0; i<actionOptions.getMaxTimesToRepeatActionSequence(); i++) {
            matches = actionMethod.perform(actionOptions, objectCollections);
            success.set(actionOptions, matches);
            if (exitSequences.okToExit(actionOptions, matches)) break;
        }
        wait.wait(actionOptions.getPauseAfterEnd());
        matches.setDuration(time.getDuration(actionOptions.getAction()));
        matches.saveSnapshots();
        char symbol = matches.isSuccess()? Output.check : Output.fail;
        Report.println(actionOptions.getAction()+" "+symbol);
        return matches;
    }

    private void printAction(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (Report.minReportingLevel(Report.OutputLevel.LOW)) {
            Report.format("|%s ", actionOptions.getAction());
            if (objectCollections.length == 0) return;
            List<StateImageObject> stImgs = objectCollections[0].getStateImages();
            int lastIndex = stImgs.size() - 1;
            for (StateImageObject sio : objectCollections[0].getStateImages()) {
                Report.format("%s.%s", sio.getOwnerStateName(), sio.getName());
                String ending = stImgs.indexOf(sio) != lastIndex? "," : "|";
                Report.print(ending+" ");
            }
        }
    }

}
