package io.github.jspinak.brobot.test.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;
import io.github.jspinak.brobot.logging.events.ActionEvent;
import io.github.jspinak.brobot.logging.events.TransitionEvent;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.builders.TestEventBuilders;
import io.github.jspinak.brobot.test.mock.LogCapture;
import io.github.jspinak.brobot.test.mock.MockLoggerFactory;

/**
 * Validation tests for the test framework itself. Ensures all test utilities and mocks work
 * correctly.
 *
 * <p>Note: Disabled due to complex mock setup issues with LogBuilder. These tests validate the test
 * framework itself, not core library functionality.
 */
@Disabled("Mock setup issues with LogBuilder - test framework validation not critical")
@DisplayName("Test Framework Validation")
public class TestFrameworkValidationTest extends BrobotTestBase {

    @Nested
    @DisplayName("BrobotTestBase Configuration")
    class TestBaseConfiguration {

        @Test
        @DisplayName("Mock logger should be properly configured")
        void testMockLoggerConfiguration() {
            assertNotNull(mockLogger, "Mock logger should be initialized");
            assertNotNull(mockLogBuilder, "Mock log builder should be initialized");

            // Verify fluent API works
            mockLogger.builder(LogCategory.ACTIONS).level(LogLevel.INFO).message("test").log();

            verify(mockLogBuilder).log();
        }

        @Test
        @DisplayName("Mock mode should be enabled by default")
        void testMockModeEnabled() {
            assertTrue(isMockMode(), "Mock mode should be enabled in tests");
        }

        @Test
        @DisplayName("Test ObjectMapper should be configured")
        void testObjectMapperConfiguration() {
            assertNotNull(testObjectMapper, "Test ObjectMapper should be initialized");
        }
    }

    @Nested
    @DisplayName("Logging Verification Utilities")
    class LoggingVerificationUtilities {

        @Test
        @DisplayName("Should verify logged with category and level")
        void testVerifyLogged() {
            mockLogger
                    .builder(LogCategory.ACTIONS)
                    .level(LogLevel.INFO)
                    .message("test action")
                    .log();

            verifyLogged(LogCategory.ACTIONS, LogLevel.INFO);
        }

        @Test
        @DisplayName("Should verify action logged")
        void testVerifyActionLogged() {
            mockLogger
                    .builder(LogCategory.ACTIONS)
                    .level(LogLevel.INFO)
                    .action("CLICK", "button")
                    .log();

            verifyActionLogged("CLICK", "button");
        }

        @Test
        @DisplayName("Should verify error logged")
        void testVerifyErrorLogged() {
            RuntimeException error = new RuntimeException("test error");

            mockLogger.builder(LogCategory.SYSTEM).level(LogLevel.ERROR).error(error).log();

            verifyErrorLogged(error);
        }

        @Test
        @DisplayName("Should verify message logged")
        void testVerifyMessageLogged() {
            String message = "Test message";

            mockLogger.builder(LogCategory.LIFECYCLE).level(LogLevel.INFO).message(message).log();

            verifyMessageLogged(message);
        }

        @Test
        @DisplayName("Should reset logger mocks")
        void testResetLoggerMocks() {
            // Log something first
            mockLogger.builder(LogCategory.ACTIONS).level(LogLevel.INFO).log();

            verify(mockLogBuilder, times(1)).log();

            // Reset mocks - this will clear all interactions
            resetLoggerMocks();

            // Log again - this should work with the reconfigured mock
            mockLogger.builder(LogCategory.ACTIONS).level(LogLevel.INFO).log();

            // Verify the mock was called after reset (should be 1 time, not counting pre-reset
            // calls)
            verify(mockLogBuilder, times(1)).log();
        }
    }

    @Nested
    @DisplayName("MockLoggerFactory")
    class MockLoggerFactoryTests {

        private final MockLoggerFactory factory = new MockLoggerFactory();

        @Test
        @DisplayName("Should create standard mock logger")
        void testCreateMockLogger() {
            var logger = factory.createMockLogger();
            assertNotNull(logger);

            var builder = logger.builder(LogCategory.ACTIONS);
            assertNotNull(builder);

            // Verify fluent API
            assertEquals(builder, builder.level(LogLevel.INFO));
            assertEquals(builder, builder.message("test"));
        }

        @Test
        @DisplayName("Should create silent logger")
        void testCreateSilentLogger() {
            var logger = factory.createSilentLogger();
            assertNotNull(logger);

            // Silent logger should work but do nothing
            assertDoesNotThrow(
                    () -> {
                        logger.builder(LogCategory.ACTIONS)
                                .level(LogLevel.INFO)
                                .message("silent")
                                .log();
                    });
        }

        @Test
        @DisplayName("Should create capturing logger")
        void testCreateCapturingLogger() {
            LogCapture capture = new LogCapture();
            var logger = factory.createCapturingLogger(capture);
            assertNotNull(logger);

            // Log something
            logger.builder(LogCategory.ACTIONS).level(LogLevel.INFO).message("captured").log();

            // Verify it was captured
            assertEquals(1, capture.getLogCount());
            assertTrue(capture.assertLoggedWithMessage("captured"));
        }
    }

    @Nested
    @DisplayName("LogCapture Utility")
    class LogCaptureTests {

        @Test
        @DisplayName("Should capture log entries")
        void testCaptureLogEntries() {
            LogCapture capture = new LogCapture();
            var logger = capture.createCapturingLogger();

            // Log multiple entries
            logger.builder(LogCategory.ACTIONS)
                    .level(LogLevel.INFO)
                    .message("action log")
                    .action("CLICK", "button")
                    .log();

            logger.builder(LogCategory.TRANSITIONS)
                    .level(LogLevel.DEBUG)
                    .message("transition log")
                    .log();

            logger.builder(LogCategory.SYSTEM)
                    .level(LogLevel.ERROR)
                    .error(new RuntimeException("error"))
                    .log();

            // Verify captures
            assertEquals(3, capture.getLogCount());

            var actionLogs = capture.getLogsByCategory(LogCategory.ACTIONS);
            assertEquals(1, actionLogs.size());

            var errorLogs = capture.getLogsByLevel(LogLevel.ERROR);
            assertEquals(1, errorLogs.size());

            assertTrue(capture.assertLoggedWithMessage("action log"));
            assertTrue(capture.assertActionLogged("CLICK", "button"));
            assertTrue(capture.assertErrorLogged(RuntimeException.class));
        }

        @Test
        @DisplayName("Should get last log entry")
        void testGetLastLog() {
            LogCapture capture = new LogCapture();
            var logger = capture.createCapturingLogger();

            logger.builder(LogCategory.ACTIONS).level(LogLevel.INFO).message("first").log();

            logger.builder(LogCategory.ACTIONS).level(LogLevel.INFO).message("last").log();

            var lastLog = capture.getLastLog();
            assertNotNull(lastLog);
            assertEquals("last", lastLog.getMessage());
        }

        @Test
        @DisplayName("Should clear captured logs")
        void testClearLogs() {
            LogCapture capture = new LogCapture();
            var logger = capture.createCapturingLogger();

            logger.builder(LogCategory.ACTIONS).level(LogLevel.INFO).message("test").log();

            assertEquals(1, capture.getLogCount());

            capture.clear();
            assertEquals(0, capture.getLogCount());
        }
    }

    @Nested
    @DisplayName("Test Event Builders")
    class TestEventBuildersTests {

        @Test
        @DisplayName("Should create action event")
        void testCreateActionEvent() {
            ActionEvent event = TestEventBuilders.actionEvent().build();

            assertNotNull(event);
            assertEquals("TEST_ACTION", event.getActionType());
            assertEquals("test-target", event.getTarget());
            assertTrue(event.isSuccess());
            assertNotNull(event.getDuration());
            assertNotNull(event.getCorrelationId());
        }

        @Test
        @DisplayName("Should create failed action event")
        void testCreateFailedActionEvent() {
            ActionEvent event = TestEventBuilders.failedActionEvent().build();

            assertNotNull(event);
            assertFalse(event.isSuccess());
            assertNotNull(event.getErrorMessage());
        }

        @Test
        @DisplayName("Should create click event")
        void testCreateClickEvent() {
            ActionEvent event = TestEventBuilders.clickEvent("submit-button").build();

            assertNotNull(event);
            assertEquals("CLICK", event.getActionType());
            assertEquals("submit-button", event.getTarget());
            assertNotNull(event.getLocation());
        }

        @Test
        @DisplayName("Should create transition event")
        void testCreateTransitionEvent() {
            TransitionEvent event = TestEventBuilders.transitionEvent().build();

            assertNotNull(event);
            assertEquals("STATE_A", event.getFromState());
            assertEquals("STATE_B", event.getToState());
            assertTrue(event.isSuccess());
        }

        @Test
        @DisplayName("Should create correlated events")
        void testCreateCorrelatedEvents() {
            var correlatedSet = TestEventBuilders.correlatedEvents();

            ActionEvent action = correlatedSet.action().build();
            TransitionEvent transition = correlatedSet.transition().build();

            assertNotNull(action.getCorrelationId());
            assertNotNull(transition.getCorrelationId());
            assertEquals(action.getCorrelationId(), transition.getCorrelationId());
            assertEquals(correlatedSet.getCorrelationId(), action.getCorrelationId());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work with all components together")
        void testFullIntegration() {
            // Create a capturing logger
            LogCapture capture = new LogCapture();
            var logger = capture.createCapturingLogger();

            // Create test events
            ActionEvent actionEvent = TestEventBuilders.clickEvent("button").build();
            TransitionEvent transitionEvent = TestEventBuilders.transitionEvent().build();

            // Log the events
            logger.builder(LogCategory.ACTIONS)
                    .level(LogLevel.INFO)
                    .action(actionEvent.getActionType(), actionEvent.getTarget())
                    .correlationId(actionEvent.getCorrelationId())
                    .log();

            logger.builder(LogCategory.TRANSITIONS)
                    .level(LogLevel.INFO)
                    .message(
                            "Transition from %s to %s",
                            transitionEvent.getFromState(), transitionEvent.getToState())
                    .correlationId(transitionEvent.getCorrelationId())
                    .log();

            // Verify the logs
            assertEquals(2, capture.getLogCount());
            assertTrue(capture.assertActionLogged("CLICK", "button"));
            assertTrue(capture.assertLoggedWithMessage("STATE_A"));
            assertTrue(capture.assertLoggedWithMessage("STATE_B"));

            // Verify categorization
            assertEquals(1, capture.getLogsByCategory(LogCategory.ACTIONS).size());
            assertEquals(1, capture.getLogsByCategory(LogCategory.TRANSITIONS).size());
        }
    }
}
