package io.github.jspinak.brobot.testingAUTs;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.imageUtils.CaptureScreenshot;
import io.github.jspinak.brobot.logging.ActionLogger;
import io.github.jspinak.brobot.manageStates.StateMemory;
import io.github.jspinak.brobot.manageStates.StateTransitionsManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class TestRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    private final StateTransitionsManagement stateTransitionsManagement;
    private final StateMemory stateMemory;
    private final AllStatesInProjectService allStatesInProjectService;
    private final ActionLogger actionLogger;
    private final CaptureScreenshot captureScreenshot;
    private final Action action;

    public TestRunner(StateTransitionsManagement stateTransitionsManagement,
                      StateMemory stateMemory,
                      AllStatesInProjectService allStatesInProjectService,
                      ActionLogger actionLogger,
                      CaptureScreenshot captureScreenshot, Action action) {
        this.stateTransitionsManagement = stateTransitionsManagement;
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
        this.actionLogger = actionLogger;
        this.captureScreenshot = captureScreenshot;
        this.action = action;
    }

    public void runTest(String destination) {
        String sessionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();

        try {
            actionLogger.startVideoRecording(sessionId);

            logger.info("Test started at {}", startTime);
            actionLogger.logObservation(sessionId, "TEST_START", "Test started", "INFO");

            // Log initial state
            actionLogger.logObservation(sessionId, "INITIAL_STATE", "Initial states: " + stateMemory.getActiveStates(), "INFO");

            // Perform the state transition
            Long destinationId = allStatesInProjectService.getState(destination)
                    .orElseThrow(() -> new RuntimeException("Destination state not found"))
                    .getId();

            Instant transitionStart = Instant.now();
            boolean transitionSuccess = stateTransitionsManagement.openState(destinationId);
            long transitionDuration = Duration.between(transitionStart, Instant.now()).toMillis();

            // Log the transition
            actionLogger.logStateTransition(sessionId, stateMemory.getActiveStates().toString(), destination,
                    transitionSuccess, transitionDuration);

            // If transition failed, capture a screenshot
            if (!transitionSuccess) {
                String screenshotPath = captureScreenshot.captureScreenshot("transition_failed_" + sessionId);
                actionLogger.logError(sessionId, "Failed to transition to state: " + destination, screenshotPath);
            }

            // Log final state
            actionLogger.logObservation(sessionId, "FINAL_STATE", "Final states: " + stateMemory.getActiveStates(), "INFO");

            // Log performance metrics
            Instant endTime = Instant.now();
            long totalDuration = Duration.between(startTime, endTime).toMillis();
            actionLogger.logPerformanceMetrics(sessionId, transitionDuration, 0, totalDuration);

        } catch (Exception e) {
            logger.error("Error during test execution", e);
            String screenshotPath = captureScreenshot.captureScreenshot("error_" + sessionId);
            actionLogger.logError(sessionId, "Error during test execution: " + e.getMessage(), screenshotPath);
        } finally {
            try {
                actionLogger.stopVideoRecording(sessionId);
            } catch (Exception e) {
                logger.error("Error stopping video recording", e);
            }

            Instant endTime = Instant.now();
            long totalDuration = Duration.between(startTime, endTime).toMillis();
            logger.info("Test ended at {}. Total duration: {} ms", endTime, totalDuration);
            actionLogger.logObservation(sessionId, "TEST_END", "Test ended. Total duration: " + totalDuration + " ms", "INFO");
        }
    }

    // Helper method to log individual actions
    private void logAction(String sessionId, ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches results = performAction(actionOptions, objectCollections);
        actionLogger.logAction(sessionId, results, objectCollections[0]);
        if (!results.isSuccess()) {
            String screenshotPath = captureScreenshot.captureScreenshot("action_failed_" + sessionId);
            actionLogger.logError(sessionId, "Action failed: " + actionOptions.getAction(), screenshotPath);
        }
    }

    // This method should be implemented to perform the actual action
    private Matches performAction(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        return action.perform(actionOptions, objectCollections);
    }
}