package io.github.jspinak.brobot.navigation.transition;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import io.github.jspinak.brobot.test.BrobotTestBase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Test class demonstrating how to test SLF4J logging in Brobot.
 *
 * <p>This is a simplified example showing the pattern for verifying log output using Logback's
 * ListAppender to capture log events.
 */
@DisplayName("TransitionExecutor Logging Tests")
public class TransitionExecutorLoggingTest extends BrobotTestBase {

    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Setup logging capture for TransitionExecutor
        logger = (Logger) LoggerFactory.getLogger(TransitionExecutor.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
        logger.setLevel(Level.TRACE);
    }

    @AfterEach
    public void tearDown() {
        if (logger != null && logAppender != null) {
            logger.detachAppender(logAppender);
        }
    }

    @Test
    @DisplayName("Example: Verify TRACE level logging")
    public void testTraceLogging() {
        // When code in TransitionExecutor logs at TRACE level:
        // log.trace("Executing IncomingTransitions for additional activated states: {}", states);

        // Simulate the log statement
        logger.trace(
                "Executing IncomingTransitions for additional activated states: {}", "TestState");

        // Verify the log was captured
        List<ILoggingEvent> logEvents = logAppender.list;

        boolean foundTraceLog =
                logEvents.stream()
                        .anyMatch(
                                event ->
                                        event.getLevel() == Level.TRACE
                                                && event.getFormattedMessage()
                                                        .contains("Executing IncomingTransitions")
                                                && event.getFormattedMessage()
                                                        .contains("TestState"));

        assertTrue(foundTraceLog, "Should log trace message for IncomingTransitions");
    }

    @Test
    @DisplayName("Example: Verify WARN level logging")
    public void testWarnLogging() {
        // When code in TransitionExecutor logs at WARN level:
        // log.warn("IncomingTransition failed for state {} ({})", stateId, stateName);

        // Simulate the log statement
        logger.warn("IncomingTransition failed for state {} ({})", 1L, "FailedState");

        // Verify the log was captured
        List<ILoggingEvent> logEvents = logAppender.list;

        boolean foundWarnLog =
                logEvents.stream()
                        .anyMatch(
                                event ->
                                        event.getLevel() == Level.WARN
                                                && event.getFormattedMessage()
                                                        .contains("IncomingTransition failed")
                                                && event.getFormattedMessage()
                                                        .contains("FailedState"));

        assertTrue(foundWarnLog, "Should log warning for failed IncomingTransition");
    }

    @Test
    @DisplayName("Example: Verify console output with System.out")
    public void testConsoleOutput() {
        // Some code still uses System.out.println for debug output
        // Example from TransitionExecutor:
        // System.out.println("=== TRANSITION DEBUG: Transition failed - target state " + to + "(" +
        // toStateName + ") is not active after transition");

        // Capture System.out
        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outContent));

        try {
            // Simulate the console output
            System.out.println(
                    "=== TRANSITION DEBUG: Transition failed - target state 2(TargetState) is not"
                            + " active after transition");

            // Verify the output
            String output = outContent.toString();
            assertTrue(output.contains("=== TRANSITION DEBUG"), "Should print debug header");
            assertTrue(output.contains("Transition failed"), "Should indicate failure");
            assertTrue(output.contains("target state 2(TargetState)"), "Should include state info");
            assertTrue(
                    output.contains("is not active after transition"), "Should explain the issue");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("Example: Verify multiple log levels are captured")
    public void testMultipleLogLevels() {
        // Clear any existing log events from setup
        logAppender.list.clear();

        // Log at different levels
        logger.error("Critical error occurred");
        logger.warn("Warning: Potential issue");
        logger.info("Information: Process completed");
        logger.debug("Debug: Detailed information");
        logger.trace("Trace: Method entry/exit");

        // Verify all logs were captured
        List<ILoggingEvent> logEvents = logAppender.list;

        assertTrue(logEvents.size() >= 5, "Should capture at least 5 log events");

        // Verify each level
        assertTrue(
                logEvents.stream()
                        .anyMatch(
                                e ->
                                        e.getLevel() == Level.ERROR
                                                && e.getMessage().contains("Critical error")));
        assertTrue(
                logEvents.stream()
                        .anyMatch(
                                e ->
                                        e.getLevel() == Level.WARN
                                                && e.getMessage().contains("Warning")));
        assertTrue(
                logEvents.stream()
                        .anyMatch(
                                e ->
                                        e.getLevel() == Level.INFO
                                                && e.getMessage().contains("Information")));
        assertTrue(
                logEvents.stream()
                        .anyMatch(
                                e ->
                                        e.getLevel() == Level.DEBUG
                                                && e.getMessage().contains("Debug")));
        assertTrue(
                logEvents.stream()
                        .anyMatch(
                                e ->
                                        e.getLevel() == Level.TRACE
                                                && e.getMessage().contains("Trace")));
    }

    @Test
    @DisplayName("Example: Verify log message parameters")
    public void testLogMessageParameters() {
        // Clear any existing log events from setup
        logAppender.list.clear();

        // Log with parameters
        String stateName = "LoginPage";
        Long stateId = 42L;
        int attemptCount = 3;

        logger.info(
                "Transition to state {} ({}) succeeded after {} attempts",
                stateName,
                stateId,
                attemptCount);

        // Verify the formatted message
        List<ILoggingEvent> logEvents = logAppender.list;
        assertTrue(logEvents.size() >= 1, "Should have at least one log event");

        // Find the specific log event we're looking for
        ILoggingEvent targetEvent =
                logEvents.stream()
                        .filter(e -> e.getMessage().contains("Transition to state"))
                        .findFirst()
                        .orElse(null);

        assertNotNull(targetEvent, "Should find the transition log message");
        assertEquals(
                "Transition to state LoginPage (42) succeeded after 3 attempts",
                targetEvent.getFormattedMessage());
    }
}
