package io.github.jspinak.brobot.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for ConfigurationException. Achieves 100% coverage of all constructors and
 * methods.
 */
@DisplayName("ConfigurationException Tests")
public class BrobotConfigurationExceptionTest extends BrobotTestBase {

    @Test
    @DisplayName("Should create exception with message only")
    void testExceptionWithMessage() {
        // Given
        String message = "Invalid configuration detected";

        // When
        ConfigurationException exception = new ConfigurationException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getConfigurationItem());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with configuration item and message")
    void testExceptionWithItemAndMessage() {
        // Given
        String configItem = "defaultTimeout";
        String message = "Value must be positive";

        // When
        ConfigurationException exception = new ConfigurationException(configItem, message);

        // Then
        assertEquals(configItem, exception.getConfigurationItem());
        assertEquals(
                "Configuration error in 'defaultTimeout': Value must be positive",
                exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void testExceptionWithMessageAndCause() {
        // Given
        String message = "Failed to load configuration file";
        IOException cause = new IOException("File not found");

        // When
        ConfigurationException exception = new ConfigurationException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getConfigurationItem());
        assertEquals(cause, exception.getCause());
        assertSame(cause, exception.getCause());
    }

    @ParameterizedTest
    @CsvSource({
        "similarity, Must be between 0.0 and 1.0, Configuration error in 'similarity': Must be"
                + " between 0.0 and 1.0",
        "timeout, Cannot be negative, Configuration error in 'timeout': Cannot be negative",
        "retryCount, Must be at least 1, Configuration error in 'retryCount': Must be at least 1",
        "clickDelay, Invalid format, Configuration error in 'clickDelay': Invalid format",
        "mockMode, Not a boolean value, Configuration error in 'mockMode': Not a boolean value"
    })
    @DisplayName("Should format message correctly for various configuration items")
    void testMessageFormattingWithItems(
            String configItem, String message, String expectedFullMessage) {
        // When
        ConfigurationException exception = new ConfigurationException(configItem, message);

        // Then
        assertEquals(configItem, exception.getConfigurationItem());
        assertEquals(expectedFullMessage, exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                " ",
                "\t",
                "\n",
                "Simple message",
                "Message with special: @#$%",
                "Very long configuration error message that provides detailed information about"
                        + " what went wrong"
            })
    @DisplayName("Should handle various message formats")
    void testVariousMessageFormats(String message) {
        // When
        ConfigurationException exception = new ConfigurationException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getConfigurationItem());
    }

    @Test
    @DisplayName("Should handle null configuration item")
    void testNullConfigItem() {
        // When
        ConfigurationException exception = new ConfigurationException(null, "Error message");

        // Then
        assertNull(exception.getConfigurationItem());
        assertEquals("Configuration error in 'null': Error message", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null message with configuration item")
    void testNullMessageWithItem() {
        // Given
        String configItem = "testItem";

        // When
        ConfigurationException exception = new ConfigurationException(configItem, (String) null);

        // Then
        assertEquals(configItem, exception.getConfigurationItem());
        assertEquals("Configuration error in 'testItem': null", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null cause")
    void testNullCause() {
        // Given
        String message = "Configuration error";

        // When
        ConfigurationException exception = new ConfigurationException(message, (Throwable) null);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertNull(exception.getConfigurationItem());
    }

    @Test
    @DisplayName("Should inherit from BrobotRuntimeException")
    void testInheritance() {
        // Given
        ConfigurationException exception = new ConfigurationException("Test");

        // Then
        assertTrue(exception instanceof BrobotRuntimeException);
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    @DisplayName("Should be throwable and catchable")
    void testThrowAndCatch() {
        // Given
        String message = "Invalid configuration";

        // When/Then
        ConfigurationException caught =
                assertThrows(
                        ConfigurationException.class,
                        () -> {
                            throw new ConfigurationException(message);
                        });

        assertEquals(message, caught.getMessage());
    }

    @Test
    @DisplayName("Should be catchable as BrobotRuntimeException")
    void testCatchAsBrobotRuntimeException() {
        // When/Then
        BrobotRuntimeException caught =
                assertThrows(
                        BrobotRuntimeException.class,
                        () -> {
                            throw new ConfigurationException("timeout", "Invalid value");
                        });

        assertTrue(caught instanceof ConfigurationException);
    }

    @ParameterizedTest
    @MethodSource("provideRealisticConfigScenarios")
    @DisplayName("Should handle realistic configuration error scenarios")
    void testRealisticScenarios(
            String configItem, String message, Throwable cause, String expectedInMessage) {
        // When
        ConfigurationException exception;
        if (cause != null) {
            exception = new ConfigurationException(message, cause);
        } else if (configItem != null) {
            exception = new ConfigurationException(configItem, message);
        } else {
            exception = new ConfigurationException(message);
        }

        // Then
        assertTrue(exception.getMessage().contains(expectedInMessage));
        if (configItem != null && cause == null) {
            assertEquals(configItem, exception.getConfigurationItem());
        }
        if (cause != null) {
            assertEquals(cause, exception.getCause());
        }
    }

    private static Stream<Arguments> provideRealisticConfigScenarios() {
        return Stream.of(
                Arguments.of(null, "Missing required property", null, "Missing required property"),
                Arguments.of(
                        "defaultSimilarity",
                        "Value 1.5 exceeds maximum",
                        null,
                        "defaultSimilarity"),
                Arguments.of(
                        "screenshotPath",
                        "Directory does not exist",
                        null,
                        "Directory does not exist"),
                Arguments.of(
                        null,
                        "Failed to parse config file",
                        new FileNotFoundException(),
                        "Failed to parse"),
                Arguments.of("threadPoolSize", "Must be positive integer", null, "threadPoolSize"),
                Arguments.of(
                        "actionTimeout", "Cannot exceed global timeout", null, "Cannot exceed"),
                Arguments.of(null, "Circular dependency detected", null, "Circular dependency"),
                Arguments.of("logLevel", "Unknown level: SUPERFINE", null, "Unknown level"));
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void testStackTrace() {
        // When
        ConfigurationException exception = new ConfigurationException("Test error");
        StackTraceElement[] stackTrace = exception.getStackTrace();

        // Then
        assertTrue(stackTrace.length > 0);
        assertEquals(this.getClass().getName(), stackTrace[0].getClassName());
    }

    @Test
    @DisplayName("Should support exception chaining")
    void testExceptionChaining() {
        // Given
        Exception root = new IllegalArgumentException("Invalid value");
        IOException middle = new IOException("File error", root);
        ConfigurationException top = new ConfigurationException("Config load failed", middle);

        // When
        Throwable rootCause = getRootCause(top);

        // Then
        assertEquals(middle, top.getCause());
        assertEquals(root, rootCause);
    }

    @Test
    @DisplayName("Should handle special characters in configuration items")
    void testSpecialCharactersInConfigItem() {
        // Given
        String[] specialItems = {
            "config.item",
            "config-item",
            "config_item",
            "config/item",
            "config\\item",
            "config:item",
            "config[0]",
            "config{key}",
            "config$var",
            "config@annotation",
            "config#id",
            "config%percent",
            "config&and",
            "config*star",
            "config+plus"
        };

        // When/Then
        for (String item : specialItems) {
            ConfigurationException exception = new ConfigurationException(item, "Error");
            assertEquals(item, exception.getConfigurationItem());
            assertTrue(exception.getMessage().contains(item));
        }
    }

    @Test
    @DisplayName("Should handle property path notation")
    void testPropertyPathNotation() {
        // Given
        String[] propertyPaths = {
            "brobot.action.timeout",
            "brobot.mock.enabled",
            "brobot.screenshot.path",
            "brobot.logging.level",
            "brobot.retry.count",
            "brobot.parallel.threads"
        };

        // When/Then
        for (String path : propertyPaths) {
            ConfigurationException exception = new ConfigurationException(path, "Invalid value");
            assertEquals(path, exception.getConfigurationItem());
            assertTrue(exception.getMessage().contains(path));
        }
    }

    @Test
    @DisplayName("Should be immutable after creation")
    void testImmutability() {
        // Given
        String configItem = "immutableItem";
        ConfigurationException exception = new ConfigurationException(configItem, "Test");

        // When
        String retrieved1 = exception.getConfigurationItem();
        String retrieved2 = exception.getConfigurationItem();

        // Then
        assertEquals(configItem, retrieved1);
        assertEquals(configItem, retrieved2);
        assertSame(retrieved1, retrieved2);
    }

    @Test
    @DisplayName("Should handle very long configuration item names")
    void testVeryLongConfigItemName() {
        // Given
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longName.append("very.long.config.path.");
        }
        String configItem = longName.toString();

        // When
        ConfigurationException exception = new ConfigurationException(configItem, "Error");

        // Then
        assertEquals(configItem, exception.getConfigurationItem());
        assertTrue(exception.getMessage().length() > 2000);
    }

    @Test
    @DisplayName("Should handle concurrent access")
    void testConcurrentAccess() throws InterruptedException {
        // Given
        ConfigurationException exception =
                new ConfigurationException("concurrentConfig", "Test error");

        // When - Access from multiple threads
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] =
                    new Thread(
                            () -> {
                                for (int j = 0; j < 1000; j++) {
                                    assertEquals(
                                            "concurrentConfig", exception.getConfigurationItem());
                                    assertTrue(exception.getMessage().contains("concurrentConfig"));
                                }
                            });
            threads[i].start();
        }

        // Then - Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }
    }

    // Helper method
    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        return (cause == null) ? throwable : getRootCause(cause);
    }
}
