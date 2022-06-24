package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionConfigurations.ExitSequences;
import io.github.jspinak.brobot.actions.actionConfigurations.Success;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.SelectRegions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.illustratedHistory.IllustrateScreenshot;
import io.github.jspinak.brobot.reports.Output;
import io.github.jspinak.brobot.reports.Report;
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
    private IllustrateScreenshot illustrateScreenshot;
    private SelectRegions selectRegions;

    public ActionExecution(Wait wait, Time time, Success success, ExitSequences exitSequences,
                           IllustrateScreenshot illustrateScreenshot, SelectRegions selectRegions) {
        this.wait = wait;
        this.time = time;
        this.success = success;
        this.exitSequences = exitSequences;
        this.illustrateScreenshot = illustrateScreenshot;
        this.selectRegions = selectRegions;
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
        illustrateScreenshot.illustrateWhenAllowed(matches,
                selectRegions.getRegionsForAllImages(actionOptions, objectCollections),
                actionOptions, objectCollections);
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
