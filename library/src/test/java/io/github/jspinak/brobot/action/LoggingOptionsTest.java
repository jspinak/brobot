package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActionConfig.LoggingOptions class.
 * Tests the builder pattern and default values.
 */
class LoggingOptionsTest {

    @Test
    @DisplayName("Should create LoggingOptions with default values")
    void testDefaultValues() {
        ActionConfig.LoggingOptions options = ActionConfig.LoggingOptions.builder()
                .build();

        assertNull(options.getBeforeActionMessage());
        assertNull(options.getAfterActionMessage());
        assertNull(options.getSuccessMessage());
        assertNull(options.getFailureMessage());
        assertFalse(options.isLogBeforeAction());
        assertFalse(options.isLogAfterAction());
        assertTrue(options.isLogOnSuccess());
        assertTrue(options.isLogOnFailure());
        assertEquals(LogEventType.ACTION, options.getBeforeActionLevel());
        assertEquals(LogEventType.ACTION, options.getAfterActionLevel());
        assertEquals(LogEventType.ACTION, options.getSuccessLevel());
        assertEquals(LogEventType.ERROR, options.getFailureLevel());
    }

    @Test
    @DisplayName("Should build LoggingOptions with all fields")
    void testFullConfiguration() {
        ActionConfig.LoggingOptions options = ActionConfig.LoggingOptions.builder()
                .beforeActionMessage("Before action")
                .afterActionMessage("After action")
                .successMessage("Success!")
                .failureMessage("Failed!")
                .logBeforeAction(true)
                .logAfterAction(true)
                .logOnSuccess(true)
                .logOnFailure(true)
                .beforeActionLevel(LogEventType.SYSTEM)
                .afterActionLevel(LogEventType.METRICS)
                .successLevel(LogEventType.ACTION)
                .failureLevel(LogEventType.ERROR)
                .build();

        assertEquals("Before action", options.getBeforeActionMessage());
        assertEquals("After action", options.getAfterActionMessage());
        assertEquals("Success!", options.getSuccessMessage());
        assertEquals("Failed!", options.getFailureMessage());
        assertTrue(options.isLogBeforeAction());
        assertTrue(options.isLogAfterAction());
        assertTrue(options.isLogOnSuccess());
        assertTrue(options.isLogOnFailure());
        assertEquals(LogEventType.SYSTEM, options.getBeforeActionLevel());
        assertEquals(LogEventType.METRICS, options.getAfterActionLevel());
        assertEquals(LogEventType.ACTION, options.getSuccessLevel());
        assertEquals(LogEventType.ERROR, options.getFailureLevel());
    }

    @Test
    @DisplayName("Should handle partial configuration")
    void testPartialConfiguration() {
        ActionConfig.LoggingOptions options = ActionConfig.LoggingOptions.builder()
                .successMessage("Operation successful")
                .logOnSuccess(true)
                .successLevel(LogEventType.ACTION)
                .build();

        assertNull(options.getBeforeActionMessage());
        assertNull(options.getAfterActionMessage());
        assertEquals("Operation successful", options.getSuccessMessage());
        assertNull(options.getFailureMessage());
        assertFalse(options.isLogBeforeAction());
        assertFalse(options.isLogAfterAction());
        assertTrue(options.isLogOnSuccess());
        assertTrue(options.isLogOnFailure()); // Default
        assertEquals(LogEventType.ACTION, options.getSuccessLevel());
    }

    @Test
    @DisplayName("Should allow custom log levels")
    void testCustomLogLevels() {
        ActionConfig.LoggingOptions options = ActionConfig.LoggingOptions.builder()
                .beforeActionLevel(LogEventType.METRICS)
                .afterActionLevel(LogEventType.SYSTEM)
                .successLevel(LogEventType.STATE_DETECTION)
                .failureLevel(LogEventType.ERROR)
                .build();

        assertEquals(LogEventType.METRICS, options.getBeforeActionLevel());
        assertEquals(LogEventType.SYSTEM, options.getAfterActionLevel());
        assertEquals(LogEventType.STATE_DETECTION, options.getSuccessLevel());
        assertEquals(LogEventType.ERROR, options.getFailureLevel());
    }

    @Test
    @DisplayName("Should disable specific logging types")
    void testDisableLogging() {
        ActionConfig.LoggingOptions options = ActionConfig.LoggingOptions.builder()
                .beforeActionMessage("This won't be logged")
                .successMessage("Neither will this")
                .logBeforeAction(false)
                .logOnSuccess(false)
                .logOnFailure(false)
                .build();

        assertEquals("This won't be logged", options.getBeforeActionMessage());
        assertEquals("Neither will this", options.getSuccessMessage());
        assertFalse(options.isLogBeforeAction());
        assertFalse(options.isLogOnSuccess());
        assertFalse(options.isLogOnFailure());
    }

    @Test
    @DisplayName("Should handle placeholder messages")
    void testPlaceholderMessages() {
        ActionConfig.LoggingOptions options = ActionConfig.LoggingOptions.builder()
                .beforeActionMessage("Looking for {target}...")
                .afterActionMessage("Action completed in {duration}ms")
                .successMessage("Found {matchCount} matches at ({x}, {y}) with {confidence}% confidence")
                .failureMessage("Failed to find {target} - {success}")
                .build();

        assertTrue(options.getBeforeActionMessage().contains("{target}"));
        assertTrue(options.getAfterActionMessage().contains("{duration}"));
        assertTrue(options.getSuccessMessage().contains("{matchCount}"));
        assertTrue(options.getSuccessMessage().contains("{x}"));
        assertTrue(options.getSuccessMessage().contains("{y}"));
        assertTrue(options.getSuccessMessage().contains("{confidence}"));
        assertTrue(options.getFailureMessage().contains("{success}"));
    }

    @Test
    @DisplayName("Should build immutable LoggingOptions")
    void testImmutability() {
        ActionConfig.LoggingOptions.LoggingOptionsBuilder builder = ActionConfig.LoggingOptions.builder()
                .beforeActionMessage("Original message");
        
        ActionConfig.LoggingOptions options1 = builder.build();
        
        // Modify builder after first build
        builder.beforeActionMessage("Modified message");
        ActionConfig.LoggingOptions options2 = builder.build();
        
        // First options should remain unchanged
        assertEquals("Original message", options1.getBeforeActionMessage());
        assertEquals("Modified message", options2.getBeforeActionMessage());
    }

    @Test
    @DisplayName("Should handle null messages")
    void testNullMessages() {
        ActionConfig.LoggingOptions options = ActionConfig.LoggingOptions.builder()
                .beforeActionMessage(null)
                .afterActionMessage(null)
                .successMessage(null)
                .failureMessage(null)
                .logBeforeAction(true)
                .logAfterAction(true)
                .build();

        assertNull(options.getBeforeActionMessage());
        assertNull(options.getAfterActionMessage());
        assertNull(options.getSuccessMessage());
        assertNull(options.getFailureMessage());
        assertTrue(options.isLogBeforeAction());
        assertTrue(options.isLogAfterAction());
    }
}