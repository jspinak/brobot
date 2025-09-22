package io.github.jspinak.brobot.action.internal.execution;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.action.internal.utility.ActionSuccessCriteria;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.control.ExecutionController;
import io.github.jspinak.brobot.control.ExecutionStoppedException;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.tools.ml.dataset.DatasetManager;
import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;

import lombok.extern.slf4j.Slf4j;

/**
 * Central orchestrator for executing GUI automation actions with full lifecycle management.
 *
 * <p>This class implements the action execution layer of the model-based GUI automation framework,
 * handling the complete lifecycle of an action from initialization through completion. It provides
 * a consistent execution environment for both {@link BasicActionRegistry} and {@link
 * CompositeActionRegistry} operations while managing critical cross-cutting concerns.
 *
 * <p><strong>Key responsibilities:</strong>
 *
 * <ul>
 *   <li>Action timing: Records start time, duration, and enforces configured pauses
 *   <li>Lifecycle management: Controls action sequences, repetitions, and termination conditions
 *   <li>Success determination: Evaluates action outcomes based on configured criteria
 *   <li>Logging and reporting: Captures execution details for debugging and analysis
 *   <li>Screenshot illustration: Documents visual state before/after actions when enabled
 *   <li>Dataset collection: Captures training data for machine learning when enabled
 * </ul>
 *
 * <p>The execution flow follows these steps:
 *
 * <ol>
 *   <li>Initialize {@link ActionResult} with action parameters
 *   <li>Apply pre-action pause if configured
 *   <li>Execute action sequences until lifecycle conditions are met
 *   <li>Determine success based on results and criteria
 *   <li>Capture illustrations if enabled
 *   <li>Apply post-action pause if configured
 *   <li>Log results and collect training data if applicable
 * </ol>
 *
 * <p><strong>Design note:</strong> Any action executed outside this class must implement equivalent
 * lifecycle management to ensure consistent behavior across the framework.
 *
 * @see ActionInterface
 * @see ActionLifecycleManagement
 * @see ActionConfig
 * @see ActionResult
 */
@Component
@Slf4j
public class ActionExecution {

    private final BrobotProperties brobotProperties;
    private final TimeWrapper timeWrapper;
    private final IllustrationController illustrateScreenshot;
    private final SearchRegionResolver selectRegions;
    private final ActionLifecycleManagement actionLifecycleManagement;
    private final DatasetManager datasetManager;
    private final ActionSuccessCriteria success;
    private final ActionResultFactory matchesInitializer;
    private final ScreenshotCapture captureScreenshot;
    private final ExecutionController executionController;
    private final BrobotLogger brobotLogger;
    private final StateMemory stateMemory;

    /**
     * Constructs an ActionExecution instance with all required dependencies.
     *
     * <p>This constructor uses dependency injection to wire together the various components needed
     * for comprehensive action execution. Each dependency handles a specific aspect of the
     * execution lifecycle.
     *
     * @param brobotProperties Configuration properties for the framework
     * @param timeWrapper Provides timing services including wait operations and duration tracking
     * @param illustrateScreenshot Captures and annotates screenshots for visual documentation
     * @param selectRegions Identifies screen regions relevant to the action
     * @param actionLifecycleManagement Controls action repetition, sequences, and termination
     * @param datasetManager Collects training data when machine learning mode is enabled
     * @param success Evaluates and sets success criteria for completed actions
     * @param matchesInitializer Creates and configures ActionResult instances
     * @param captureScreenshot Captures raw screenshots for error documentation
     * @param executionController Controls execution flow with pause/resume/stop functionality
     * @param brobotLogger Logger for framework-level events
     * @param stateMemory State management and history
     */
    public ActionExecution(
            BrobotProperties brobotProperties,
            TimeWrapper timeWrapper,
            IllustrationController illustrateScreenshot,
            SearchRegionResolver selectRegions,
            ActionLifecycleManagement actionLifecycleManagement,
            DatasetManager datasetManager,
            ActionSuccessCriteria success,
            ActionResultFactory matchesInitializer,
            ScreenshotCapture captureScreenshot,
            @Autowired(required = false) ExecutionController executionController,
            BrobotLogger brobotLogger,
            StateMemory stateMemory) {
        this.brobotProperties = brobotProperties;
        this.timeWrapper = timeWrapper;
        this.illustrateScreenshot = illustrateScreenshot;
        this.selectRegions = selectRegions;
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.datasetManager = datasetManager;
        this.success = success;
        this.matchesInitializer = matchesInitializer;
        this.captureScreenshot = captureScreenshot;
        this.executionController = executionController;
        this.brobotLogger = brobotLogger;
        this.stateMemory = stateMemory;
    }

    /**
     * Executes an action with complete lifecycle management and maintenance operations.
     *
     * <p>This method serves as the primary entry point for all action execution in the framework.
     * It orchestrates the entire action lifecycle, from initialization through completion, while
     * handling cross-cutting concerns like timing, logging, and data collection.
     *
     * <p><strong>Execution sequence:</strong>
     *
     * <ol>
     *   <li>Logs action initiation and target objects
     *   <li>Initializes {@link ActionResult} with action parameters
     *   <li>Applies pre-action pause (if configured)
     *   <li>Executes action sequences via {@link ActionLifecycleManagement#isMoreSequencesAllowed}
     *   <li>Determines success based on results and {@link ActionConfig} criteria
     *   <li>Captures illustrated screenshots (if enabled)
     *   <li>Applies post-action pause (if configured)
     *   <li>Records duration and logs results
     *   <li>Collects training data (if {@link BrobotProperties} is enabled)
     * </ol>
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Modifies the GUI through the provided actionMethod
     *   <li>Updates the ActionResult with execution details
     *   <li>Logs action details to the automation session
     *   <li>May capture screenshots based on configuration
     *   <li>May save training data when in dataset building mode
     *   <li>Prints action results to the report output
     * </ul>
     *
     * @param actionMethod The {@link ActionInterface} implementation containing action-specific
     *     logic
     * @param actionDescription Human-readable description of the action for logging and ML training
     * @param actionConfig Configuration parameters controlling action behavior and success criteria
     * @param objectCollections Variable number of {@link ObjectCollection} containing target GUI
     *     elements. The first collection (if present) is used for primary logging.
     * @return {@link ActionResult} containing matches found, success status, duration, and
     *     execution details
     * @see ActionInterface#perform
     * @see ActionLifecycleManagement
     * @see DatasetManager#addSetOfData
     */
    // The ActionConfig version is available below
    /*
     * public ActionResult perform(ActionInterface actionMethod, String
     * actionDescription, ActionConfig actionConfig,
     * ObjectCollection... objectCollections) {
     * String sessionId = automationSession.getCurrentSessionId();
     * printAction(actionConfig, objectCollections);
     * ActionResult matches = matchesInitializer.init(actionConfig,
     * actionDescription, objectCollections);
     *
     * try {
     * // Check pause point before starting
     * checkPausePointSafely();
     *
     * time.wait(actionConfig.getPauseBeforeBegin());
     *
     * while (actionLifecycleManagement.isMoreSequencesAllowed(matches)) {
     * // Check pause point before each sequence
     * checkPausePointSafely();
     *
     * actionMethod.perform(matches, objectCollections);
     * actionLifecycleManagement.incrementCompletedSequences(matches);
     * }
     *
     * success.set(actionConfig, matches);
     * illustrateScreenshot.illustrateWhenAllowed(matches,
     * selectRegions.getRegionsForAllImages(actionConfig, objectCollections),
     * actionConfig, objectCollections);
     * time.wait(actionConfig.getPauseAfterEnd());
     *
     * } catch (ExecutionStoppedException e) {
     * log.info("Action execution stopped: {}", actionDescription);
     * matches.setSuccess(false);
     * // Rethrow to propagate the stop signal
     * throw e;
     * }
     *
     * Duration duration = actionLifecycleManagement.getCurrentDuration(matches);
     * matches.setDuration(duration);
     * if (brobotProperties.getDataset().isEnabled()) datasetManager.addSetOfData(matches);
     * ActionLifecycleAspect which respects QUIET mode
     * //  + " " +
     * matches.getOutputText() + " " + matches.getSuccessSymbol());
     * if (objectCollections.length > 0) {
     * LogData logData = actionLogger.logAction(sessionId, matches,
     * objectCollections[0]);
     * }
     * if (!matches.isSuccess()) {
     * //String screenshotPath =
     * captureScreenshot.captureScreenshot("action_failed_" + sessionId);
     * //actionLogger.logError(sessionId, "Action failed: " +
     * actionConfig.getAction(), screenshotPath);
     * }
     *
     * // Automatically update state memory when patterns are found
     * // This ensures states are activated when their patterns are detected
     * if (matches.isSuccess() && !matches.getMatchList().isEmpty()) {
     * stateMemory.adjustActiveStatesWithMatches(matches);
     * }
     *
     * return matches;
     * }
     */

    /**
     * Prints action details to the report output for debugging and monitoring.
     *
     * <p>This method formats and outputs a concise representation of the action being executed,
     * including the action type and target state images. Output is only generated when the
     * reporting level is set to {@link  {
     * ActionLifecycleAspect which respects QUIET mode
     * QUIET mode
     * }
     */

    /**
     * Executes an action with complete lifecycle management using ActionConfig.
     *
     * <p>This method is the new primary entry point that accepts ActionConfig instead of
     * ActionConfig. It provides the same comprehensive lifecycle management as the legacy method
     * but uses the modern configuration approach.
     *
     * @param actionMethod The {@link ActionInterface} implementation containing action-specific
     *     logic
     * @param actionDescription Human-readable description of the action for logging and ML training
     * @param actionConfig Configuration parameters controlling action behavior and success criteria
     * @param objectCollections Variable number of {@link ObjectCollection} containing target GUI
     *     elements
     * @return {@link ActionResult} containing matches found, success status, duration, and
     *     execution details
     */
    public ActionResult perform(
            ActionInterface actionMethod,
            String actionDescription,
            ActionConfig actionConfig,
            ObjectCollection... objectCollections) {
        printActionConfig(actionConfig, objectCollections);
        ActionResult matches =
                matchesInitializer.init(actionConfig, actionDescription, objectCollections);

        try {
            // Check pause point before starting
            checkPausePointSafely();

            // Handle before action logging
            handleBeforeActionLogging(actionConfig, objectCollections);

            timeWrapper.wait(actionConfig.getPauseBeforeBegin());

            while (actionLifecycleManagement.isMoreSequencesAllowed(matches)) {
                // Check pause point before each sequence
                checkPausePointSafely();

                actionMethod.perform(matches, objectCollections);
                actionLifecycleManagement.incrementCompletedSequences(matches);
            }

            success.set(actionConfig, matches);

            // Handle automatic logging based on ActionConfig logging options
            handleAutomaticLogging(actionConfig, matches, objectCollections);

            illustrateScreenshot.illustrateWhenAllowed(
                    matches,
                    selectRegions.getRegionsForAllImages(actionConfig, objectCollections),
                    actionConfig,
                    objectCollections);
            timeWrapper.wait(actionConfig.getPauseAfterEnd());

            // Handle after action logging
            handleAfterActionLogging(actionConfig, matches, objectCollections);

        } catch (ExecutionStoppedException e) {
            log.info("Action execution stopped: {}", actionDescription);
            matches.setSuccess(false);
            // Rethrow to propagate the stop signal
            throw e;
        }

        Duration duration = actionLifecycleManagement.getCurrentDuration(matches);
        matches.setDuration(duration);
        if (brobotProperties.getDataset().isBuild()) datasetManager.addSetOfData(matches);
        // matches.getOutputText() + " " + matches.getSuccessSymbol());
        // if (objectCollections.length > 0) {
        //     LogData logData = actionLogger.logAction(sessionId, matches, objectCollections[0]);
        // }

        // Automatically update state memory when patterns are found
        // This ensures states are activated when their patterns are detected
        if (matches.isSuccess() && !matches.getMatchList().isEmpty()) {
            stateMemory.adjustActiveStatesWithMatches(matches);
        }

        return matches;
    }

    /** Prints action details for ActionConfig-based actions. */
    private void printActionConfig(
            ActionConfig actionConfig, ObjectCollection... objectCollections) {}

    /**
     * Safely checks for pause points when an ExecutionController is available.
     *
     * <p>This method provides a null-safe way to check pause points. If no ExecutionController is
     * configured (for backward compatibility), the method returns immediately without checking.
     *
     * @throws ExecutionStoppedException if the execution has been stopped
     */
    private void checkPausePointSafely() throws ExecutionStoppedException {
        if (executionController != null) {
            try {
                executionController.checkPausePoint();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExecutionStoppedException("Action execution interrupted", e);
            }
        }
    }

    /**
     * Handles logging before action execution begins.
     *
     * @param actionConfig The action configuration containing logging options
     * @param objectCollections The object collections involved in the action
     */
    private void handleBeforeActionLogging(
            ActionConfig actionConfig, ObjectCollection... objectCollections) {}

    /**
     * Handles logging after action execution completes.
     *
     * @param actionConfig The action configuration containing logging options
     * @param actionResult The result of the action execution
     * @param objectCollections The object collections involved in the action
     */
    private void handleAfterActionLogging(
            ActionConfig actionConfig,
            ActionResult actionResult,
            ObjectCollection... objectCollections) {}

    /**
     * Handles automatic logging based on ActionConfig logging options.
     *
     * <p>This method checks the logging configuration and logs success or failure messages
     * automatically, reducing boilerplate in application code.
     *
     * @param actionConfig The action configuration containing logging options
     * @param actionResult The result of the action execution
     * @param objectCollections The object collections involved in the action
     */
    private void handleAutomaticLogging(
            ActionConfig actionConfig,
            ActionResult actionResult,
            ObjectCollection... objectCollections) {}

    /**
     * Formats a log message with placeholders replaced by actual values. Supports placeholders like
     * {matchCount}, {duration}, {target}, etc.
     */
    private String formatLogMessage(
            String template, ActionResult actionResult, ObjectCollection... objectCollections) {
        String message = template;

        // Replace action result placeholders if result is available
        if (actionResult != null) {
            message =
                    message.replace(
                            "{matchCount}", String.valueOf(actionResult.getMatchList().size()));
            if (actionResult.getDuration() != null) {
                message =
                        message.replace(
                                "{duration}",
                                String.valueOf(actionResult.getDuration().toMillis()));
            }
            message = message.replace("{success}", String.valueOf(actionResult.isSuccess()));
        }

        // Add target name if available
        if (objectCollections.length > 0 && !objectCollections[0].getStateImages().isEmpty()) {
            String targetName = objectCollections[0].getStateImages().get(0).getName();
            message = message.replace("{target}", targetName);
        }

        // Add action type
        message = message.replace("{action}", getActionType(template));

        return message;
    }

    /** Extracts action type from the class name or template. */
    private String getActionType(String template) {
        // This is a placeholder - in practice, you'd extract from the ActionConfig
        // class name
        return "action";
    }
}
