package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionConfigurations.Success;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionExecution.manageTrainingData.DatasetManager;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.SelectRegions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.illustratedHistory.IllustrateScreenshot;
import io.github.jspinak.brobot.reports.Output;
import io.github.jspinak.brobot.reports.Report;
import io.github.jspinak.brobot.testingAUTs.ActionLog;
import io.github.jspinak.brobot.testingAUTs.ActionLogCreator;
import io.github.jspinak.brobot.testingAUTs.LogListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
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
@Slf4j
public class ActionExecution {
    private final Time time;
    private final IllustrateScreenshot illustrateScreenshot;
    private final SelectRegions selectRegions;
    private final ActionLifecycleManagement actionLifecycleManagement;
    private final DatasetManager datasetManager;
    private final ActionLogCreator actionLogCreator;
    private final Success success;
    private final MatchesInitializer matchesInitializer;
    private final LogListener logListener;

    public ActionExecution(Time time, IllustrateScreenshot illustrateScreenshot, SelectRegions selectRegions,
                           ActionLifecycleManagement actionLifecycleManagement, DatasetManager datasetManager,
                           ActionLogCreator actionLogCreator, Success success,
                           MatchesInitializer matchesInitializer, LogListener logListener) {
        this.time = time;
        this.illustrateScreenshot = illustrateScreenshot;
        this.selectRegions = selectRegions;
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.datasetManager = datasetManager;
        this.actionLogCreator = actionLogCreator;
        this.success = success;
        this.matchesInitializer = matchesInitializer;
        this.logListener = logListener;
    }

    /**
     * Performs the Action and certain maintenance functions.
     * @param actionMethod has Action-specific code.
     * @param actionDescription a description of the action. can be used for training a neural net.
     * @param actionOptions contains information about the Action to be performed.
     * @param objectCollections contains the objects on which to perform the Action.
     * @return Matches
     */
    public Matches perform(ActionInterface actionMethod, String actionDescription, ActionOptions actionOptions,
                           ObjectCollection... objectCollections) {
        printAction(actionOptions, objectCollections);
        Matches matches = matchesInitializer.init(actionOptions, actionDescription, objectCollections);
        time.wait(actionOptions.getPauseBeforeBegin());
        while (actionLifecycleManagement.isMoreSequencesAllowed(matches)) {
            actionMethod.perform(matches, objectCollections);
            actionLifecycleManagement.incrementCompletedSequences(matches);
        }
        success.set(actionOptions, matches);
        illustrateScreenshot.illustrateWhenAllowed(matches,
                selectRegions.getRegionsForAllImages(actionOptions, objectCollections),
                actionOptions, objectCollections);
        time.wait(actionOptions.getPauseAfterEnd());
        Duration duration = actionLifecycleManagement.getCurrentDuration(matches);
        matches.setDuration(duration); //time.getDuration(actionOptions.getAction()));
        sendActionLog(matches, actionOptions, objectCollections);
        if (BrobotSettings.buildDataset) datasetManager.addSetOfData(matches); // for the neural net training dataset
        //log.info("Logged ActionLogInfo: {}", actionLog.toJson());
        Report.println(actionOptions.getAction() + " " + matches.getOutputText() + " " + matches.getSuccessSymbol());
        return matches;
    }

    private void sendActionLog(Matches matches, ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (!BrobotSettings.sendLogsToElasticContainer) return;
        ActionLog actionLog = actionLogCreator.create(
                //time.getStartTime(actionOptions.getAction()),
                //time.getEndTime(actionOptions.getAction()),
                matches, actionOptions, objectCollections);
        logListener.logEvent(actionLog);
    }

    private void printAction(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (Report.minReportingLevel(Report.OutputLevel.LOW)) {
            Report.format("|%s ", actionOptions.getAction());
            if (objectCollections.length == 0) return;
            List<StateImage> stImgs = objectCollections[0].getStateImages();
            int lastIndex = stImgs.size() - 1;
            for (StateImage sio : objectCollections[0].getStateImages()) {
                Report.format("%s.%s", sio.getOwnerStateName(), sio.getName());
                String ending = stImgs.indexOf(sio) != lastIndex? "," : "|";
                Report.print(ending+" ");
            }
        }
    }

}
