package io.github.jspinak.brobot.tools.testing.exploration;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;

/**
 * Executes individual test runs with comprehensive logging and monitoring capabilities.
 *
 * <p>ExplorationSessionRunner is responsible for executing specific test scenarios, typically
 * involving navigation to a particular state and verification of the transition. It provides:
 *
 * <ul>
 *   <li>Session-based test execution with unique identifiers
 *   <li>Comprehensive logging of test steps and results
 *   <li>Performance metrics collection
 *   <li>Error handling with screenshot capture on failures
 *   <li>Video recording of test sessions
 * </ul>
 *
 * <h2>Test Execution Flow</h2>
 *
 * <ol>
 *   <li>Initialize test session with unique ID
 *   <li>Start video recording for visual documentation
 *   <li>Log initial application state
 *   <li>Execute state transition to target destination
 *   <li>Capture performance metrics
 *   <li>Log results and any errors
 *   <li>Stop recording and finalize session
 * </ol>
 *
 * <h2>Error Handling</h2>
 *
 * <p>The runner implements robust error handling:
 *
 * <ul>
 *   <li>Captures screenshots on transition failures
 *   <li>Logs detailed error information with context
 *   <li>Ensures video recording is stopped even on exceptions
 *   <li>Records performance metrics regardless of success/failure
 * </ul>
 *
 * <h2>Performance Monitoring</h2>
 *
 * <p>Tracks and logs:
 *
 * <ul>
 *   <li>Individual transition duration
 *   <li>Total test execution time
 *   <li>Success/failure rates
 * </ul>
 *
 * <h2>Integration with Test Framework</h2>
 *
 * <p>Works as part of the larger testing exploration framework:
 *
 * <ul>
 *   <li>{@link ExplorationOrchestrator} orchestrates multiple test runs
 *   <li>{@link StateTraversalService} uses this for individual state visits
 *   <li>{@link ActionLogger} provides the logging infrastructure
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // Execute a test to navigate to the login state
 * explorationSessionRunner.runTest("LoginState");
 *
 * // The runner will:
 * // 1. Start recording
 * // 2. Log current state
 * // 3. Attempt transition to LoginState
 * // 4. Capture results and metrics
 * // 5. Stop recording
 * }</pre>
 *
 * @see ExplorationOrchestrator for test orchestration
 * @see StateTraversalService for comprehensive state exploration
 * @see ActionLogger for logging capabilities
 * @see StateNavigator for state navigation
 * @author jspinak
 */
@Component
public class ExplorationSessionRunner {

    private static final Logger logger = LoggerFactory.getLogger(ExplorationSessionRunner.class);

    private final StateNavigator stateTransitionsManagement;
    private final StateMemory stateMemory;
    private final StateService allStatesInProjectService;
    private final ActionLogger actionLogger;
    private final ScreenshotCapture captureScreenshot;
    private final Action action;

    public ExplorationSessionRunner(
            StateNavigator stateTransitionsManagement,
            StateMemory stateMemory,
            StateService allStatesInProjectService,
            ActionLogger actionLogger,
            ScreenshotCapture captureScreenshot,
            Action action) {
        this.stateTransitionsManagement = stateTransitionsManagement;
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
        this.actionLogger = actionLogger;
        this.captureScreenshot = captureScreenshot;
        this.action = action;
    }

    /**
     * Executes a test run to navigate to a specified destination state.
     *
     * <p>This method orchestrates a complete test execution cycle including:
     *
     * <ol>
     *   <li>Session initialization with unique identifier
     *   <li>Video recording for visual documentation
     *   <li>Initial state capture and logging
     *   <li>State transition execution with timing
     *   <li>Result verification and error handling
     *   <li>Performance metrics collection
     *   <li>Session cleanup and finalization
     * </ol>
     *
     * <p>The test execution is fully instrumented with logging at each step, providing complete
     * visibility into the test process. Screenshots are automatically captured on failures for
     * debugging purposes.
     *
     * <p>Performance metrics tracked include:
     *
     * <ul>
     *   <li>Transition duration - time to navigate to destination
     *   <li>Total test duration - complete execution time
     * </ul>
     *
     * @param destination the name of the target state to navigate to. Must be a valid state name
     *     registered in the state service.
     * @throws RuntimeException if the destination state is not found in the project
     * @see StateNavigator#openState(Long) for state navigation
     * @see ActionLogger for comprehensive logging capabilities
     */
    public void runTest(String destination) {
        String sessionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();

        try {
            actionLogger.startVideoRecording(sessionId);

            logger.info("Test started at {}", startTime);
            actionLogger.logObservation(sessionId, "TEST_START", "Test started", "INFO");

            // Log initial state
            actionLogger.logObservation(
                    sessionId,
                    "INITIAL_STATE",
                    "Initial states: " + stateMemory.getActiveStates(),
                    "INFO");

            // Perform the state transition
            Long destinationId =
                    allStatesInProjectService
                            .getState(destination)
                            .orElseThrow(() -> new RuntimeException("Destination state not found"))
                            .getId();

            Instant transitionStart = Instant.now();
            boolean transitionSuccess = stateTransitionsManagement.openState(destinationId);
            long transitionDuration = Duration.between(transitionStart, Instant.now()).toMillis();

            // Log the transition
            actionLogger.logStateTransition(
                    sessionId,
                    null,
                    allStatesInProjectService.findSetById(stateMemory.getActiveStates()),
                    Collections.singleton(allStatesInProjectService.getState(destination).get()),
                    transitionSuccess,
                    transitionDuration);

            // If transition failed, capture a screenshot
            if (!transitionSuccess) {
                String screenshotPath =
                        captureScreenshot.captureScreenshot("transition_failed_" + sessionId);
                actionLogger.logError(
                        sessionId, "Failed to transition to state: " + destination, screenshotPath);
            }

            // Log final state
            actionLogger.logObservation(
                    sessionId,
                    "FINAL_STATE",
                    "Final states: " + stateMemory.getActiveStates(),
                    "INFO");

            // Log performance metrics
            Instant endTime = Instant.now();
            long totalDuration = Duration.between(startTime, endTime).toMillis();
            actionLogger.logPerformanceMetrics(sessionId, transitionDuration, 0, totalDuration);

        } catch (Exception e) {
            logger.error("Error during test execution", e);
            String screenshotPath = captureScreenshot.captureScreenshot("error_" + sessionId);
            actionLogger.logError(
                    sessionId, "Error during test execution: " + e.getMessage(), screenshotPath);
        } finally {
            try {
                actionLogger.stopVideoRecording(sessionId);
            } catch (Exception e) {
                logger.error("Error stopping video recording", e);
            }

            Instant endTime = Instant.now();
            long totalDuration = Duration.between(startTime, endTime).toMillis();
            logger.info("Test ended at {}. Total duration: {} ms", endTime, totalDuration);
            actionLogger.logObservation(
                    sessionId,
                    "TEST_END",
                    "Test ended. Total duration: " + totalDuration + " ms",
                    "INFO");
        }
    }

    /**
     * Helper method to execute and log individual actions during test execution.
     *
     * <p>This method provides a consistent way to:
     *
     * <ul>
     *   <li>Execute actions on UI elements
     *   <li>Log action results with full context
     *   <li>Capture screenshots on action failures
     *   <li>Maintain session consistency across actions
     * </ul>
     *
     * <p>Failed actions automatically trigger screenshot capture for debugging, with the screenshot
     * path included in error logs.
     *
     * @param sessionId the unique test session identifier for correlation
     * @param actionConfig configuration for the action to perform (e.g., CLICK, FIND)
     * @param objectCollections the UI elements to perform the action on
     * @see Action#perform(ActionConfig, ObjectCollection...) for action execution
     * @see ActionLogger#logAction(String, ActionResult, ObjectCollection) for logging
     */
    private void logAction(
            String sessionId, ActionConfig actionConfig, ObjectCollection... objectCollections) {
        ActionResult results = performAction(actionConfig, objectCollections);
        actionLogger.logAction(sessionId, results, objectCollections[0]);
        if (!results.isSuccess()) {
            String screenshotPath =
                    captureScreenshot.captureScreenshot("action_failed_" + sessionId);
            actionLogger.logError(
                    sessionId,
                    "Action failed: " + actionConfig.getClass().getSimpleName(),
                    screenshotPath);
        }
    }

    /**
     * Performs the specified action on the given UI elements.
     *
     * <p>This method delegates to the injected Action service to execute UI interactions. It serves
     * as a wrapper to maintain consistency and potentially add additional logic in the future.
     *
     * @param actionConfig the action configuration specifying what to do
     * @param objectCollections the target UI elements for the action
     * @return ActionResult containing success status and action details
     * @see Action#perform(ActionConfig, ObjectCollection...) for the underlying implementation
     */
    private ActionResult performAction(
            ActionConfig actionConfig, ObjectCollection... objectCollections) {
        return action.perform(actionConfig, objectCollections);
    }
}
