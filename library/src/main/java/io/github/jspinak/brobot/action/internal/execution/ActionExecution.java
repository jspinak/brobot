package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.action.internal.utility.ActionSuccessCriteria;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ExecutionSession;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.ml.dataset.DatasetManager;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Central orchestrator for executing GUI automation actions with full lifecycle management.
 * <p>
 * This class implements the action execution layer of the model-based GUI automation framework,
 * handling the complete lifecycle of an action from initialization through completion. It provides
 * a consistent execution environment for both {@link BasicActionRegistry} and {@link CompositeActionRegistry}
 * operations while managing critical cross-cutting concerns.
 * <p>
 * <strong>Key responsibilities:</strong>
 * <ul>
 * <li>Action timing: Records start time, duration, and enforces configured pauses</li>
 * <li>Lifecycle management: Controls action sequences, repetitions, and termination conditions</li>
 * <li>Success determination: Evaluates action outcomes based on configured criteria</li>
 * <li>Logging and reporting: Captures execution details for debugging and analysis</li>
 * <li>Screenshot illustration: Documents visual state before/after actions when enabled</li>
 * <li>Dataset collection: Captures training data for machine learning when enabled</li>
 * </ul>
 * <p>
 * The execution flow follows these steps:
 * <ol>
 * <li>Initialize {@link ActionResult} with action parameters</li>
 * <li>Apply pre-action pause if configured</li>
 * <li>Execute action sequences until lifecycle conditions are met</li>
 * <li>Determine success based on results and criteria</li>
 * <li>Capture illustrations if enabled</li>
 * <li>Apply post-action pause if configured</li>
 * <li>Log results and collect training data if applicable</li>
 * </ol>
 * <p>
 * <strong>Design note:</strong> Any action executed outside this class must implement
 * equivalent lifecycle management to ensure consistent behavior across the framework.
 *
 * @see ActionInterface
 * @see ActionLifecycleManagement
 * @see ActionOptions
 * @see ActionResult
 */
@Component
@Slf4j
public class ActionExecution {
    private final TimeProvider time;
    private final IllustrationController illustrateScreenshot;
    private final SearchRegionResolver selectRegions;
    private final ActionLifecycleManagement actionLifecycleManagement;
    private final DatasetManager datasetManager;
    private final ActionSuccessCriteria success;
    private final ActionResultFactory matchesInitializer;
    private final ActionLogger actionLogger;
    private final ScreenshotCapture captureScreenshot;
    private final ExecutionSession automationSession;

    /**
     * Constructs an ActionExecution instance with all required dependencies.
     * <p>
     * This constructor uses dependency injection to wire together the various components
     * needed for comprehensive action execution. Each dependency handles a specific aspect
     * of the execution lifecycle.
     *
     * @param time Provides timing services including wait operations and duration tracking
     * @param illustrateScreenshot Captures and annotates screenshots for visual documentation
     * @param selectRegions Identifies screen regions relevant to the action
     * @param actionLifecycleManagement Controls action repetition, sequences, and termination
     * @param datasetManager Collects training data when machine learning mode is enabled
     * @param success Evaluates and sets success criteria for completed actions
     * @param matchesInitializer Creates and configures ActionResult instances
     * @param actionLogger Records action execution details for analysis and debugging
     * @param captureScreenshot Captures raw screenshots for error documentation
     * @param automationSession Manages session context and identifiers
     */
    public ActionExecution(TimeProvider time, IllustrationController illustrateScreenshot, SearchRegionResolver selectRegions,
                           ActionLifecycleManagement actionLifecycleManagement, DatasetManager datasetManager,
                           ActionSuccessCriteria success, ActionResultFactory matchesInitializer, ActionLogger actionLogger,
                           ScreenshotCapture captureScreenshot, ExecutionSession automationSession) {
        this.time = time;
        this.illustrateScreenshot = illustrateScreenshot;
        this.selectRegions = selectRegions;
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.datasetManager = datasetManager;
        this.success = success;
        this.matchesInitializer = matchesInitializer;
        this.actionLogger = actionLogger;
        this.captureScreenshot = captureScreenshot;
        this.automationSession = automationSession;
    }

    /**
     * Executes an action with complete lifecycle management and maintenance operations.
     * <p>
     * This method serves as the primary entry point for all action execution in the framework.
     * It orchestrates the entire action lifecycle, from initialization through completion,
     * while handling cross-cutting concerns like timing, logging, and data collection.
     * <p>
     * <strong>Execution sequence:</strong>
     * <ol>
     * <li>Logs action initiation and target objects</li>
     * <li>Initializes {@link ActionResult} with action parameters</li>
     * <li>Applies pre-action pause (if configured)</li>
     * <li>Executes action sequences via {@link ActionLifecycleManagement#isMoreSequencesAllowed}</li>
     * <li>Determines success based on results and {@link ActionOptions} criteria</li>
     * <li>Captures illustrated screenshots (if enabled)</li>
     * <li>Applies post-action pause (if configured)</li>
     * <li>Records duration and logs results</li>
     * <li>Collects training data (if {@link FrameworkSettings#buildDataset} is enabled)</li>
     * </ol>
     * <p>
     * <strong>Side effects:</strong>
     * <ul>
     * <li>Modifies the GUI through the provided actionMethod</li>
     * <li>Updates the ActionResult with execution details</li>
     * <li>Logs action details to the automation session</li>
     * <li>May capture screenshots based on configuration</li>
     * <li>May save training data when in dataset building mode</li>
     * <li>Prints action results to the report output</li>
     * </ul>
     *
     * @param actionMethod The {@link ActionInterface} implementation containing action-specific logic
     * @param actionDescription Human-readable description of the action for logging and ML training
     * @param actionOptions Configuration parameters controlling action behavior and success criteria
     * @param objectCollections Variable number of {@link ObjectCollection} containing target GUI elements.
     *                         The first collection (if present) is used for primary logging.
     * @return {@link ActionResult} containing matches found, success status, duration, and execution details
     *
     * @see ActionInterface#perform
     * @see ActionLifecycleManagement
     * @see DatasetManager#addSetOfData
     */
    public ActionResult perform(ActionInterface actionMethod, String actionDescription, ActionOptions actionOptions,
                           ObjectCollection... objectCollections) {
        String sessionId = automationSession.getCurrentSessionId();
        printAction(actionOptions, objectCollections);
        ActionResult matches = matchesInitializer.init(actionOptions, actionDescription, objectCollections);
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
        if (FrameworkSettings.buildDataset) datasetManager.addSetOfData(matches); // for the neural net training dataset
        ConsoleReporter.println(actionOptions.getAction() + " " + matches.getOutputText() + " " + matches.getSuccessSymbol());
        if (objectCollections.length > 0) {
            LogData logData = actionLogger.logAction(sessionId, matches, objectCollections[0]); // log the action after it's finished
        }
        if (!matches.isSuccess()) {  // taking a screenshot should be a part of the logError implementation in the App module
            //String screenshotPath = captureScreenshot.captureScreenshot("action_failed_" + sessionId);
            //actionLogger.logError(sessionId, "Action failed: " + actionOptions.getAction(), screenshotPath);
        }
        return matches;
    }

    /**
     * Prints action details to the report output for debugging and monitoring.
     * <p>
     * This method formats and outputs a concise representation of the action being executed,
     * including the action type and target state images. Output is only generated when the
     * reporting level is set to {@link ConsoleReporter.OutputLevel#LOW} or higher.
     * <p>
     * The output format is: {@code |ACTION_TYPE stateName.imageName, stateName.imageName|}
     * <p>
     * Example output: {@code |CLICK MainMenu.fileButton, MainMenu.editButton|}
     *
     * @param actionOptions Contains the action type to be printed
     * @param objectCollections Contains state images to be included in the output.
     *                         Only the first collection is processed if multiple are provided.
     */
    private void printAction(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.LOW)) {
            ConsoleReporter.format("|%s ", actionOptions.getAction());
            if (objectCollections.length == 0) return;
            List<StateImage> stImgs = objectCollections[0].getStateImages();
            int lastIndex = stImgs.size() - 1;
            for (StateImage sio : objectCollections[0].getStateImages()) {
                ConsoleReporter.format("%s.%s", sio.getOwnerStateName(), sio.getName());
                String ending = stImgs.indexOf(sio) != lastIndex? "," : "|";
                ConsoleReporter.print(ending+" ");
            }
        }
    }

    /**
     * Executes an action with complete lifecycle management using ActionConfig.
     * <p>
     * This method is the new primary entry point that accepts ActionConfig instead of ActionOptions.
     * It provides the same comprehensive lifecycle management as the legacy method but uses the
     * modern configuration approach.
     *
     * @param actionMethod The {@link ActionInterface} implementation containing action-specific logic
     * @param actionDescription Human-readable description of the action for logging and ML training
     * @param actionConfig Configuration parameters controlling action behavior and success criteria
     * @param objectCollections Variable number of {@link ObjectCollection} containing target GUI elements
     * @return {@link ActionResult} containing matches found, success status, duration, and execution details
     */
    public ActionResult perform(ActionInterface actionMethod, String actionDescription, ActionConfig actionConfig,
                               ObjectCollection... objectCollections) {
        String sessionId = automationSession.getCurrentSessionId();
        printActionConfig(actionConfig, objectCollections);
        ActionResult matches = matchesInitializer.init(actionConfig, actionDescription, objectCollections);
        time.wait(actionConfig.getPauseBeforeBegin());
        while (actionLifecycleManagement.isMoreSequencesAllowed(matches)) {
            actionMethod.perform(matches, objectCollections);
            actionLifecycleManagement.incrementCompletedSequences(matches);
        }
        success.set(actionConfig, matches);
        illustrateScreenshot.illustrateWhenAllowed(matches,
                selectRegions.getRegionsForAllImages(actionConfig, objectCollections),
                actionConfig, objectCollections);
        time.wait(actionConfig.getPauseAfterEnd());
        Duration duration = actionLifecycleManagement.getCurrentDuration(matches);
        matches.setDuration(duration);
        if (FrameworkSettings.buildDataset) datasetManager.addSetOfData(matches);
        ConsoleReporter.println(actionConfig.getClass().getSimpleName() + " " + matches.getOutputText() + " " + matches.getSuccessSymbol());
        if (objectCollections.length > 0) {
            LogData logData = actionLogger.logAction(sessionId, matches, objectCollections[0]);
        }
        return matches;
    }

    /**
     * Prints action details for ActionConfig-based actions.
     */
    private void printActionConfig(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        if (ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.LOW)) {
            ConsoleReporter.format("|%s ", actionConfig.getClass().getSimpleName());
            if (objectCollections.length == 0) return;
            List<StateImage> stImgs = objectCollections[0].getStateImages();
            int lastIndex = stImgs.size() - 1;
            for (StateImage sio : objectCollections[0].getStateImages()) {
                ConsoleReporter.format("%s.%s", sio.getOwnerStateName(), sio.getName());
                String ending = stImgs.indexOf(sio) != lastIndex? "," : "|";
                ConsoleReporter.print(ending+" ");
            }
        }
    }

}
