package io.github.jspinak.brobot.action.logging;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.logging.LogLevel;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for ActionLoggingEnhancer. Tests fluent API for adding logging
 * capabilities to ActionConfig objects.
 */
@DisplayName("ActionLoggingEnhancer Tests")
public class ActionLoggingEnhancerTest extends BrobotTestBase {

    private ActionLoggingEnhancer enhancer;
    private ActionConfig actionConfig;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        enhancer = new ActionLoggingEnhancer();
        actionConfig = new PatternFindOptions.Builder().build();
    }

    @Nested
    @DisplayName("Logging Configuration")
    class LoggingConfigurationTests {

        @Test
        @DisplayName("Should create default configuration")
        public void testDefaultConfiguration() {
            // Act
            ActionLoggingEnhancer.LoggingConfiguration config =
                    ActionLoggingEnhancer.LoggingConfiguration.defaultConfig();

            // Assert
            assertNotNull(config);
            assertEquals(LogLevel.INFO, config.getLogLevel());
            assertTrue(config.isLogOnSuccess());
            assertTrue(config.isLogOnFailure());
            assertNull(config.getBeforeActionLog());
            assertNull(config.getAfterActionLog());
            assertNull(config.getMessageTemplate());
        }

        @Test
        @DisplayName("Should build custom configuration")
        public void testCustomConfiguration() {
            // Arrange
            Consumer<ActionConfig> beforeLog = config -> {};
            BiConsumer<ActionConfig, ActionResult> afterLog = (config, result) -> {};

            // Act
            ActionLoggingEnhancer.LoggingConfiguration config =
                    ActionLoggingEnhancer.LoggingConfiguration.builder()
                            .beforeActionLog(beforeLog)
                            .afterActionLog(afterLog)
                            .logLevel(LogLevel.DEBUG)
                            .logOnSuccess(false)
                            .logOnFailure(true)
                            .messageTemplate("Custom: {action}")
                            .build();

            // Assert
            assertSame(beforeLog, config.getBeforeActionLog());
            assertSame(afterLog, config.getAfterActionLog());
            assertEquals(LogLevel.DEBUG, config.getLogLevel());
            assertFalse(config.isLogOnSuccess());
            assertTrue(config.isLogOnFailure());
            assertEquals("Custom: {action}", config.getMessageTemplate());
        }
    }

    @Nested
    @DisplayName("Before Action Logging")
    class BeforeActionLoggingTests {

        @Test
        @DisplayName("Should add before action log callback")
        public void testAddBeforeActionLog() {
            // Arrange
            AtomicBoolean callbackExecuted = new AtomicBoolean(false);
            Consumer<ActionConfig> beforeLog = config -> callbackExecuted.set(true);

            // Act
            ActionConfig result = enhancer.withBeforeActionLog(actionConfig, beforeLog);

            // Assert
            assertSame(actionConfig, result); // Returns same config

            // Execute callback to verify it was stored
            enhancer.executeBeforeLogging(actionConfig);
            assertTrue(callbackExecuted.get());
        }

        @Test
        @DisplayName("Should handle multiple before callbacks")
        public void testMultipleBeforeCallbacks() {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            Consumer<ActionConfig> firstLog = config -> counter.incrementAndGet();
            Consumer<ActionConfig> secondLog = config -> counter.addAndGet(10);

            // Act
            enhancer.withBeforeActionLog(actionConfig, firstLog);
            enhancer.withBeforeActionLog(actionConfig, secondLog); // Overwrites

            // Execute
            enhancer.executeBeforeLogging(actionConfig);

            // Assert - Only second callback executed (overwrites first)
            assertEquals(10, counter.get());
        }

        @Test
        @DisplayName("Should handle null before callback")
        public void testNullBeforeCallback() {
            // Act & Assert - Should not throw
            assertDoesNotThrow(
                    () -> {
                        enhancer.withBeforeActionLog(actionConfig, null);
                        enhancer.executeBeforeLogging(actionConfig);
                    });
        }

        @Test
        @DisplayName("Should not execute when no callback configured")
        public void testNoBeforeCallback() {
            // Act & Assert - Should not throw
            assertDoesNotThrow(() -> enhancer.executeBeforeLogging(actionConfig));
        }
    }

    @Nested
    @DisplayName("After Action Logging")
    class AfterActionLoggingTests {

        @Test
        @DisplayName("Should add after action log callback")
        public void testAddAfterActionLog() {
            // Arrange
            AtomicBoolean callbackExecuted = new AtomicBoolean(false);
            BiConsumer<ActionConfig, ActionResult> afterLog =
                    (config, result) -> callbackExecuted.set(true);

            // Act
            ActionConfig result = enhancer.withAfterActionLog(actionConfig, afterLog);

            // Assert
            assertSame(actionConfig, result);

            // Execute callback
            ActionResult actionResult = new ActionResult();
            actionResult.setSuccess(true);
            enhancer.executeAfterLogging(actionConfig, actionResult);
            assertTrue(callbackExecuted.get());
        }

        @Test
        @DisplayName("Should pass correct parameters to after callback")
        public void testAfterCallbackParameters() {
            // Arrange
            AtomicBoolean correctParams = new AtomicBoolean(false);
            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(true);

            BiConsumer<ActionConfig, ActionResult> afterLog =
                    (config, result) -> {
                        correctParams.set(config == actionConfig && result == expectedResult);
                    };

            // Act
            enhancer.withAfterActionLog(actionConfig, afterLog);
            enhancer.executeAfterLogging(actionConfig, expectedResult);

            // Assert
            assertTrue(correctParams.get());
        }

        @Test
        @DisplayName("Should respect log on success flag")
        public void testLogOnSuccessFlag() {
            // Arrange
            AtomicInteger callCount = new AtomicInteger(0);
            BiConsumer<ActionConfig, ActionResult> afterLog =
                    (config, result) -> callCount.incrementAndGet();

            enhancer.withAfterActionLog(actionConfig, afterLog);
            enhancer.withLogOnSuccess(actionConfig, false);

            ActionResult successResult = new ActionResult();
            successResult.setSuccess(true);

            // Act
            enhancer.executeAfterLogging(actionConfig, successResult);

            // Assert - Should not execute because logOnSuccess is false
            assertEquals(0, callCount.get());
        }

        @Test
        @DisplayName("Should respect log on failure flag")
        public void testLogOnFailureFlag() {
            // Arrange
            AtomicInteger callCount = new AtomicInteger(0);
            BiConsumer<ActionConfig, ActionResult> afterLog =
                    (config, result) -> callCount.incrementAndGet();

            enhancer.withAfterActionLog(actionConfig, afterLog);
            enhancer.withLogOnFailure(actionConfig, false);

            ActionResult failureResult = new ActionResult();
            failureResult.setSuccess(false);

            // Act
            enhancer.executeAfterLogging(actionConfig, failureResult);

            // Assert - Should not execute because logOnFailure is false
            assertEquals(0, callCount.get());
        }
    }

    @Nested
    @DisplayName("Log Level Configuration")
    class LogLevelConfigurationTests {

        @Test
        @DisplayName("Should set log level")
        public void testSetLogLevel() {
            // Act
            ActionConfig result = enhancer.withLogLevel(actionConfig, LogLevel.DEBUG);

            // Assert
            assertSame(actionConfig, result);
            ActionLoggingEnhancer.LoggingConfiguration config =
                    enhancer.getLoggingConfiguration(actionConfig);
            assertNotNull(config);
            assertEquals(LogLevel.DEBUG, config.getLogLevel());
        }

        @Test
        @DisplayName("Should default to INFO level")
        public void testDefaultLogLevel() {
            // Arrange - Trigger default config creation
            enhancer.withLogOnSuccess(actionConfig, true);

            // Act
            ActionLoggingEnhancer.LoggingConfiguration config =
                    enhancer.getLoggingConfiguration(actionConfig);

            // Assert
            assertEquals(LogLevel.INFO, config.getLogLevel());
        }

        @Test
        @DisplayName("Should handle all log levels")
        public void testAllLogLevels() {
            for (LogLevel level : LogLevel.values()) {
                // Arrange
                ActionConfig config = new PatternFindOptions.Builder().build();

                // Act
                enhancer.withLogLevel(config, level);

                // Assert
                ActionLoggingEnhancer.LoggingConfiguration loggingConfig =
                        enhancer.getLoggingConfiguration(config);
                assertEquals(level, loggingConfig.getLogLevel());
            }
        }
    }

    @Nested
    @DisplayName("Conditional Logging")
    class ConditionalLoggingTests {

        @Test
        @DisplayName("Should configure log on success")
        public void testLogOnSuccess() {
            // Act
            ActionConfig result = enhancer.withLogOnSuccess(actionConfig, false);

            // Assert
            assertSame(actionConfig, result);
            ActionLoggingEnhancer.LoggingConfiguration config =
                    enhancer.getLoggingConfiguration(actionConfig);
            assertFalse(config.isLogOnSuccess());

            // Change back to true
            enhancer.withLogOnSuccess(actionConfig, true);
            assertTrue(config.isLogOnSuccess());
        }

        @Test
        @DisplayName("Should configure log on failure")
        public void testLogOnFailure() {
            // Act
            ActionConfig result = enhancer.withLogOnFailure(actionConfig, false);

            // Assert
            assertSame(actionConfig, result);
            ActionLoggingEnhancer.LoggingConfiguration config =
                    enhancer.getLoggingConfiguration(actionConfig);
            assertFalse(config.isLogOnFailure());

            // Change back to true
            enhancer.withLogOnFailure(actionConfig, true);
            assertTrue(config.isLogOnFailure());
        }

        @Test
        @DisplayName("Should default to logging both success and failure")
        public void testDefaultConditionalLogging() {
            // Arrange - Trigger default config creation
            enhancer.withMessageTemplate(actionConfig, "test");

            // Act
            ActionLoggingEnhancer.LoggingConfiguration config =
                    enhancer.getLoggingConfiguration(actionConfig);

            // Assert
            assertTrue(config.isLogOnSuccess());
            assertTrue(config.isLogOnFailure());
        }
    }

    @Nested
    @DisplayName("Message Template")
    class MessageTemplateTests {

        @Test
        @DisplayName("Should set message template")
        public void testSetMessageTemplate() {
            // Act
            ActionConfig result =
                    enhancer.withMessageTemplate(
                            actionConfig, "Action {name} completed in {duration}ms");

            // Assert
            assertSame(actionConfig, result);
            ActionLoggingEnhancer.LoggingConfiguration config =
                    enhancer.getLoggingConfiguration(actionConfig);
            assertEquals("Action {name} completed in {duration}ms", config.getMessageTemplate());
        }

        @Test
        @DisplayName("Should handle null template")
        public void testNullTemplate() {
            // Act
            enhancer.withMessageTemplate(actionConfig, null);

            // Assert
            ActionLoggingEnhancer.LoggingConfiguration config =
                    enhancer.getLoggingConfiguration(actionConfig);
            assertNull(config.getMessageTemplate());
        }
    }

    @Nested
    @DisplayName("Fluent API Chaining")
    class FluentAPIChainingTests {

        @Test
        @DisplayName("Should support method chaining")
        public void testMethodChaining() {
            // Arrange
            Consumer<ActionConfig> beforeLog = config -> {};
            BiConsumer<ActionConfig, ActionResult> afterLog = (config, result) -> {};

            // Act - Chain multiple configurations
            ActionConfig result = enhancer.withBeforeActionLog(actionConfig, beforeLog);

            result = enhancer.withAfterActionLog(result, afterLog);
            result = enhancer.withLogLevel(result, LogLevel.DEBUG);
            result = enhancer.withLogOnSuccess(result, false);
            result = enhancer.withLogOnFailure(result, true);
            result = enhancer.withMessageTemplate(result, "Custom template");

            // Assert
            assertSame(actionConfig, result);
            ActionLoggingEnhancer.LoggingConfiguration config =
                    enhancer.getLoggingConfiguration(actionConfig);
            assertEquals(LogLevel.DEBUG, config.getLogLevel());
            assertFalse(config.isLogOnSuccess());
            assertTrue(config.isLogOnFailure());
            assertEquals("Custom template", config.getMessageTemplate());
        }
    }

    @Nested
    @DisplayName("Configuration Management")
    class ConfigurationManagementTests {

        @Test
        @DisplayName("Should maintain separate configs for different ActionConfigs")
        public void testSeparateConfigurations() {
            // Arrange
            ActionConfig config1 = new PatternFindOptions.Builder().build();
            ActionConfig config2 = new PatternFindOptions.Builder().build();

            // Act
            enhancer.withLogLevel(config1, LogLevel.DEBUG);
            enhancer.withLogLevel(config2, LogLevel.ERROR);
            enhancer.withLogOnSuccess(config1, false);
            enhancer.withLogOnSuccess(config2, true);

            // Assert
            ActionLoggingEnhancer.LoggingConfiguration loggingConfig1 =
                    enhancer.getLoggingConfiguration(config1);
            ActionLoggingEnhancer.LoggingConfiguration loggingConfig2 =
                    enhancer.getLoggingConfiguration(config2);

            assertEquals(LogLevel.DEBUG, loggingConfig1.getLogLevel());
            assertEquals(LogLevel.ERROR, loggingConfig2.getLogLevel());
            assertFalse(loggingConfig1.isLogOnSuccess());
            assertTrue(loggingConfig2.isLogOnSuccess());
        }

        @Test
        @DisplayName("Should clear configuration")
        public void testClearConfiguration() {
            // Arrange
            enhancer.withLogLevel(actionConfig, LogLevel.DEBUG);
            enhancer.withLogOnSuccess(actionConfig, false);

            // Act
            enhancer.clearLoggingConfiguration(actionConfig);

            // Assert
            assertNull(enhancer.getLoggingConfiguration(actionConfig));
        }

        @Test
        @DisplayName("Should handle getting non-existent configuration")
        public void testGetNonExistentConfiguration() {
            // Act
            ActionLoggingEnhancer.LoggingConfiguration config =
                    enhancer.getLoggingConfiguration(actionConfig);

            // Assert
            assertNull(config);
        }
    }
}
