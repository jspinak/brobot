package io.github.jspinak.brobot.tools.logging;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.tools.logging.model.LogData;

import java.awt.*;
import java.io.IOException;
import java.util.Set;

/**
 * Defines the contract for logging automation actions and events in the Brobot framework.
 * <p>
 * This interface provides a comprehensive logging API for capturing various aspects of
 * automation execution including actions performed, state transitions, performance metrics,
 * errors, and video recordings. Implementations can store logs in different formats and
 * destinations such as files, databases, or external logging services.
 * <p>
 * Key logging categories:
 * <ul>
 * <li>Action logs - Captures details of automation actions performed</li>
 * <li>Observations - Records observations about the application state</li>
 * <li>State transitions - Tracks navigation between application states</li>
 * <li>Performance metrics - Measures execution times and performance</li>
 * <li>Errors - Captures error conditions with screenshots</li>
 * <li>Video recording - Controls recording of automation sessions</li>
 * </ul>
 * <p>
 * All methods provide default no-op implementations, allowing implementations to
 * selectively override only the logging methods they need. This design supports
 * flexible logging configurations from minimal to comprehensive logging.
 *
 * @see ActionResult
 * @see LogData
 * @see io.github.jspinak.brobot.tools.logging.implementation.ActionLoggerImpl
 */
public interface ActionLogger {
    /**
     * Logs the execution of an automation action with its results.
     * <p>
     * This method captures comprehensive information about an action execution including
     * what was searched for, what was found, and the outcome. The logged data can be
     * used for debugging, auditing, and analyzing automation behavior.
     *
     * @param sessionId The unique identifier for the current automation session
     * @param results The result of the action execution containing matches, success status,
     *                and timing information. This object is not modified.
     * @param objectCollection The collection of objects that were searched for during the action.
     *                        This provides context about what the action was trying to find.
     * @return LogData containing the logged information, or null for no-op implementations
     */
    default LogData logAction(String sessionId, ActionResult results, ObjectCollection objectCollection) {
        // No-op implementation
        return null;
    }

    /**
     * Logs an observation about the application state or automation execution.
     * <p>
     * Observations are general-purpose log entries for recording noteworthy events
     * or conditions that don't fit into other specific categories. Examples include
     * unexpected UI states, performance observations, or business logic validations.
     *
     * @param sessionId The unique identifier for the current automation session
     * @param observationType A categorization of the observation (e.g., "UI_STATE", "PERFORMANCE", "VALIDATION")
     * @param description A detailed description of what was observed
     * @param severity The importance level of the observation (e.g., "INFO", "WARNING", "ERROR")
     * @return LogData containing the logged observation, or null for no-op implementations
     */
    default LogData logObservation(String sessionId, String observationType, String description, String severity) {
        // No-op implementation
        return null;
    }

    /**
     * Logs a state transition in the application under test.
     * <p>
     * State transitions represent navigation between different screens or states in the
     * application. This method captures the complete context of a transition including
     * the starting states, target states, actual states before transition, success status,
     * and timing. This information is crucial for understanding navigation patterns and
     * debugging transition failures.
     *
     * @param sessionId The unique identifier for the current automation session
     * @param fromStates The set of states the application was in before the transition.
     *                   Multiple states indicate the application was in an ambiguous state.
     * @param toStates The set of target states the transition was attempting to reach
     * @param beforeStates The actual states detected before executing the transition.
     *                     May differ from fromStates if state detection was re-run.
     * @param success Whether the transition successfully reached one of the target states
     * @param transitionTime The time taken to complete the transition in milliseconds
     * @return LogData containing the transition details, or null for no-op implementations
     */
    default LogData logStateTransition(String sessionId, Set<State> fromStates, Set<State> toStates,
                                       Set<State> beforeStates, boolean success, long transitionTime) {
        // No-op implementation
        return null;
    }

    /**
     * Logs performance metrics for automation execution.
     * <p>
     * Performance metrics help identify bottlenecks and track execution efficiency.
     * This method captures key timing measurements that can be used for performance
     * analysis, optimization, and SLA monitoring.
     *
     * @param sessionId The unique identifier for the current automation session
     * @param actionDuration The time taken to execute the last action in milliseconds
     * @param pageLoadTime The time taken for the page/screen to load in milliseconds.
     *                     May be 0 if not applicable or not measured.
     * @param totalTestDuration The total elapsed time of the test execution in milliseconds
     * @return LogData containing the performance metrics, or null for no-op implementations
     */
    default LogData logPerformanceMetrics(String sessionId, long actionDuration, long pageLoadTime, long totalTestDuration) {
        // No-op implementation
        return null;
    }

    /**
     * Logs an error condition with an optional screenshot for debugging.
     * <p>
     * Error logs capture failure conditions during automation execution. The screenshot
     * provides visual context of the application state at the time of the error, which
     * is invaluable for debugging intermittent failures or understanding error conditions.
     *
     * @param sessionId The unique identifier for the current automation session
     * @param errorMessage A detailed description of the error that occurred
     * @param screenshotPath The file path to a screenshot taken at the time of the error.
     *                       May be null if screenshot capture failed or was not attempted.
     * @return LogData containing the error information, or null for no-op implementations
     */
    default LogData logError(String sessionId, String errorMessage, String screenshotPath) {
        // No-op implementation
        return null;
    }

    /**
     * Starts video recording of the automation session.
     * <p>
     * Video recordings provide a complete visual record of automation execution,
     * useful for debugging complex scenarios, demonstrating test execution, or
     * compliance documentation. The recording captures the entire screen or
     * application window depending on the implementation.
     *
     * @param sessionId The unique identifier for the current automation session
     * @return LogData containing recording start information including the output file path,
     *         or null for no-op implementations
     * @throws IOException if there's an error accessing the file system or creating the recording file
     * @throws AWTException if there's an error accessing the screen for recording
     */
    default LogData startVideoRecording(String sessionId) throws IOException, AWTException {
        // No-op implementation
        return null;
    }

    /**
     * Stops video recording of the automation session.
     * <p>
     * This method finalizes the video recording started by {@link #startVideoRecording(String)}.
     * The implementation should ensure the video file is properly closed and saved.
     * The returned LogData typically includes the final file path and recording duration.
     *
     * @param sessionId The unique identifier for the current automation session.
     *                  Must match the sessionId used in startVideoRecording.
     * @return LogData containing recording completion information including final file path
     *         and duration, or null for no-op implementations
     * @throws IOException if there's an error finalizing or saving the video file
     */
    default LogData stopVideoRecording(String sessionId) throws IOException {
        // No-op implementation
        return null;
    }
}