package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.action.internal.utility.ActionSuccessCriteria;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.control.ExecutionController;
import io.github.jspinak.brobot.control.ExecutionStoppedException;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ExecutionSession;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.ml.dataset.DatasetManager;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ExecutionController executionController;
    private final BrobotLogger brobotLogger;
    private final StateMemory stateMemory;

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
     * @param executionController Controls execution flow with pause/resume/stop functionality
     */
    public ActionExecution(TimeProvider time, IllustrationController illustrateScreenshot, SearchRegionResolver selectRegions,
                           ActionLifecycleManagement actionLifecycleManagement, DatasetManager datasetManager,
                           ActionSuccessCriteria success, ActionResultFactory matchesInitializer, ActionLogger actionLogger,
                           ScreenshotCapture captureScreenshot, ExecutionSession automationSession,
                           @Autowired(required = false) ExecutionController executionController,
                           BrobotLogger brobotLogger, StateMemory stateMemory) {
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
        this.executionController = executionController;
        this.brobotLogger = brobotLogger;
        this.stateMemory = stateMemory;
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
        
        try {
            // Check pause point before starting
            checkPausePointSafely();
            
            time.wait(actionOptions.getPauseBeforeBegin());
            
            while (actionLifecycleManagement.isMoreSequencesAllowed(matches)) {
                // Check pause point before each sequence
                checkPausePointSafely();
                
                actionMethod.perform(matches, objectCollections);
                actionLifecycleManagement.incrementCompletedSequences(matches);
            }
            
            success.set(actionOptions, matches);
            illustrateScreenshot.illustrateWhenAllowed(matches,
                    selectRegions.getRegionsForAllImages(actionOptions, objectCollections),
                    actionOptions, objectCollections);
            time.wait(actionOptions.getPauseAfterEnd());
            
        } catch (ExecutionStoppedException e) {
            log.info("Action execution stopped: {}", actionDescription);
            matches.setSuccess(false);
            // Rethrow to propagate the stop signal
            throw e;
        }
        
        Duration duration = actionLifecycleManagement.getCurrentDuration(matches);
        matches.setDuration(duration);
        if (FrameworkSettings.buildDataset) datasetManager.addSetOfData(matches);
        // Removed direct console output - logging is handled by ActionLifecycleAspect which respects QUIET mode
        // ConsoleReporter.println(actionOptions.getAction() + " " + matches.getOutputText() + " " + matches.getSuccessSymbol());
        if (objectCollections.length > 0) {
            LogData logData = actionLogger.logAction(sessionId, matches, objectCollections[0]);
        }
        if (!matches.isSuccess()) {
            //String screenshotPath = captureScreenshot.captureScreenshot("action_failed_" + sessionId);
            //actionLogger.logError(sessionId, "Action failed: " + actionOptions.getAction(), screenshotPath);
        }
        
        // Automatically update state memory when patterns are found
        // This ensures states are activated when their patterns are detected
        if (matches.isSuccess() && !matches.getMatchList().isEmpty()) {
            stateMemory.adjustActiveStatesWithMatches(matches);
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
        // Disabled direct console output - logging is handled by ActionLifecycleAspect which respects QUIET mode
        // Legacy direct console printing interferes with structured logging and QUIET mode
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
        
        try {
            // Check pause point before starting
            checkPausePointSafely();
            
            // Handle before action logging
            handleBeforeActionLogging(actionConfig, objectCollections);
            
            time.wait(actionConfig.getPauseBeforeBegin());
            
            while (actionLifecycleManagement.isMoreSequencesAllowed(matches)) {
                // Check pause point before each sequence
                checkPausePointSafely();
                
                actionMethod.perform(matches, objectCollections);
                actionLifecycleManagement.incrementCompletedSequences(matches);
            }
            
            success.set(actionConfig, matches);
            
            // Handle automatic logging based on ActionConfig logging options
            handleAutomaticLogging(actionConfig, matches, objectCollections);
            
            illustrateScreenshot.illustrateWhenAllowed(matches,
                    selectRegions.getRegionsForAllImages(actionConfig, objectCollections),
                    actionConfig, objectCollections);
            time.wait(actionConfig.getPauseAfterEnd());
            
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
        if (FrameworkSettings.buildDataset) datasetManager.addSetOfData(matches);
        // Removed direct console output - logging is handled by ActionLifecycleAspect which respects QUIET mode
        // ConsoleReporter.println(actionConfig.getClass().getSimpleName() + " " + matches.getOutputText() + " " + matches.getSuccessSymbol());
        if (objectCollections.length > 0) {
            LogData logData = actionLogger.logAction(sessionId, matches, objectCollections[0]);
        }
        
        // Automatically update state memory when patterns are found
        // This ensures states are activated when their patterns are detected
        if (matches.isSuccess() && !matches.getMatchList().isEmpty()) {
            stateMemory.adjustActiveStatesWithMatches(matches);
        }
        
        return matches;
    }

    /**
     * Prints action details for ActionConfig-based actions.
     */
    private void printActionConfig(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        // Disabled direct console output - logging is handled by ActionLifecycleAspect which respects QUIET mode
        // Legacy direct console printing interferes with structured logging and QUIET mode
    }
    
    /**
     * Safely checks for pause points when an ExecutionController is available.
     * <p>
     * This method provides a null-safe way to check pause points. If no
     * ExecutionController is configured (for backward compatibility), the
     * method returns immediately without checking.
     * </p>
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
    private void handleBeforeActionLogging(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        if (actionConfig.getLoggingOptions() == null || 
            !actionConfig.getLoggingOptions().isLogBeforeAction() ||
            actionConfig.getLoggingOptions().getBeforeActionMessage() == null) {
            return;
        }
        
        String message = formatLogMessage(actionConfig.getLoggingOptions().getBeforeActionMessage(), 
                                        null, objectCollections);
        logMessage(message, actionConfig.getLoggingOptions().getBeforeActionLevel(), 
                  actionConfig, null, objectCollections);
    }
    
    /**
     * Handles logging after action execution completes.
     * 
     * @param actionConfig The action configuration containing logging options
     * @param actionResult The result of the action execution
     * @param objectCollections The object collections involved in the action
     */
    private void handleAfterActionLogging(ActionConfig actionConfig, ActionResult actionResult,
                                         ObjectCollection... objectCollections) {
        if (actionConfig.getLoggingOptions() == null || 
            !actionConfig.getLoggingOptions().isLogAfterAction() ||
            actionConfig.getLoggingOptions().getAfterActionMessage() == null) {
            return;
        }
        
        String message = formatLogMessage(actionConfig.getLoggingOptions().getAfterActionMessage(), 
                                        actionResult, objectCollections);
        logMessage(message, actionConfig.getLoggingOptions().getAfterActionLevel(), 
                  actionConfig, actionResult, objectCollections);
    }

    /**
     * Handles automatic logging based on ActionConfig logging options.
     * <p>
     * This method checks the logging configuration and logs success or failure
     * messages automatically, reducing boilerplate in application code.
     * </p>
     * 
     * @param actionConfig The action configuration containing logging options
     * @param actionResult The result of the action execution
     * @param objectCollections The object collections involved in the action
     */
    private void handleAutomaticLogging(ActionConfig actionConfig, ActionResult actionResult, 
                                       ObjectCollection... objectCollections) {
        if (actionConfig.getLoggingOptions() == null) {
            return; // No logging configured
        }
        
        ActionConfig.LoggingOptions loggingOptions = actionConfig.getLoggingOptions();
        
        if (actionResult.isSuccess() && loggingOptions.isLogOnSuccess() && 
            loggingOptions.getSuccessMessage() != null) {
            // Log success message
            String message = formatLogMessage(loggingOptions.getSuccessMessage(), 
                                            actionResult, objectCollections);
            logMessage(message, loggingOptions.getSuccessLevel(), actionConfig, 
                      actionResult, objectCollections);
        } else if (!actionResult.isSuccess() && loggingOptions.isLogOnFailure() && 
                   loggingOptions.getFailureMessage() != null) {
            // Log failure message
            String message = formatLogMessage(loggingOptions.getFailureMessage(), 
                                            actionResult, objectCollections);
            logMessage(message, loggingOptions.getFailureLevel(), actionConfig, 
                      actionResult, objectCollections);
        }
    }
    
    /**
     * Formats a log message with placeholders replaced by actual values.
     * Supports placeholders like {matchCount}, {duration}, {target}, etc.
     */
    private String formatLogMessage(String template, ActionResult actionResult, 
                                   ObjectCollection... objectCollections) {
        String message = template;
        
        // Replace action result placeholders if result is available
        if (actionResult != null) {
            message = message.replace("{matchCount}", String.valueOf(actionResult.getMatchList().size()));
            if (actionResult.getDuration() != null) {
                message = message.replace("{duration}", String.valueOf(actionResult.getDuration().toMillis()));
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
    
    /**
     * Extracts action type from the class name or template.
     */
    private String getActionType(String template) {
        // This is a placeholder - in practice, you'd extract from the ActionConfig class name
        return "action";
    }
    
    /**
     * Logs a message through the unified logging system.
     */
    private void logMessage(String message, io.github.jspinak.brobot.tools.logging.model.LogEventType logType,
                           ActionConfig actionConfig, ActionResult actionResult, 
                           ObjectCollection... objectCollections) {
        // Determine the primary target for logging
        String targetName = "unknown";
        if (objectCollections.length > 0 && !objectCollections[0].getStateImages().isEmpty()) {
            targetName = objectCollections[0].getStateImages().get(0).getName();
        }
        
        // Build log entry
        var logBuilder = brobotLogger.log()
            .type(mapLogEventType(logType))
            .action(actionConfig.getClass().getSimpleName().replace("Options", ""))
            .target(targetName)
            .observation(message);
            
        // Add result-specific data if available
        if (actionResult != null) {
            logBuilder.success(actionResult.isSuccess());
            if (actionResult.getDuration() != null) {
                logBuilder.duration(actionResult.getDuration().toMillis());
            }
            logBuilder.metadata("matchCount", actionResult.getMatchList().size());
        }
        
        logBuilder.log();
    }
    
    /**
     * Maps LogEventType to unified LogEvent.Type
     */
    private io.github.jspinak.brobot.logging.unified.LogEvent.Type mapLogEventType(
            io.github.jspinak.brobot.tools.logging.model.LogEventType logEventType) {
        switch (logEventType) {
            case ACTION:
                return io.github.jspinak.brobot.logging.unified.LogEvent.Type.ACTION;
            case ERROR:
                return io.github.jspinak.brobot.logging.unified.LogEvent.Type.ERROR;
            case TRANSITION:
                return io.github.jspinak.brobot.logging.unified.LogEvent.Type.TRANSITION;
            default:
                return io.github.jspinak.brobot.logging.unified.LogEvent.Type.OBSERVATION;
        }
    }

}
