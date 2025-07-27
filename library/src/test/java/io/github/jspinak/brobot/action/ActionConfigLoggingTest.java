package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActionConfig logging functionality.
 * Tests the builder methods and LoggingOptions configuration.
 */
class ActionConfigLoggingTest {

    @Test
    @DisplayName("Should create default LoggingOptions when not configured")
    void testDefaultLoggingOptions() {
        PatternFindOptions options = new PatternFindOptions.Builder()
                .build();

        assertNotNull(options.getLoggingOptions());
        assertNull(options.getLoggingOptions().getBeforeActionMessage());
        assertNull(options.getLoggingOptions().getAfterActionMessage());
        assertNull(options.getLoggingOptions().getSuccessMessage());
        assertNull(options.getLoggingOptions().getFailureMessage());
        assertFalse(options.getLoggingOptions().isLogBeforeAction());
        assertFalse(options.getLoggingOptions().isLogAfterAction());
        assertTrue(options.getLoggingOptions().isLogOnSuccess());
        assertTrue(options.getLoggingOptions().isLogOnFailure());
    }

    @Test
    @DisplayName("Should configure before action logging")
    void testBeforeActionLogging() {
        String beforeMessage = "Starting search operation...";
        
        PatternFindOptions options = new PatternFindOptions.Builder()
                .withBeforeActionLog(beforeMessage)
                .build();

        assertEquals(beforeMessage, options.getLoggingOptions().getBeforeActionMessage());
        assertTrue(options.getLoggingOptions().isLogBeforeAction());
        assertEquals(LogEventType.ACTION, options.getLoggingOptions().getBeforeActionLevel());
    }

    @Test
    @DisplayName("Should configure after action logging")
    void testAfterActionLogging() {
        String afterMessage = "Search completed in {duration}ms";
        
        ClickOptions options = new ClickOptions.Builder()
                .withAfterActionLog(afterMessage)
                .build();

        assertEquals(afterMessage, options.getLoggingOptions().getAfterActionMessage());
        assertTrue(options.getLoggingOptions().isLogAfterAction());
        assertEquals(LogEventType.ACTION, options.getLoggingOptions().getAfterActionLevel());
    }

    @Test
    @DisplayName("Should configure success logging")
    void testSuccessLogging() {
        String successMessage = "Successfully found target at ({x}, {y})";
        
        PatternFindOptions options = new PatternFindOptions.Builder()
                .withSuccessLog(successMessage)
                .build();

        assertEquals(successMessage, options.getLoggingOptions().getSuccessMessage());
        assertTrue(options.getLoggingOptions().isLogOnSuccess());
        assertEquals(LogEventType.ACTION, options.getLoggingOptions().getSuccessLevel());
    }

    @Test
    @DisplayName("Should configure failure logging")
    void testFailureLogging() {
        String failureMessage = "Failed to find target after {duration}ms";
        
        TypeOptions options = new TypeOptions.Builder()
                .withFailureLog(failureMessage)
                .build();

        assertEquals(failureMessage, options.getLoggingOptions().getFailureMessage());
        assertTrue(options.getLoggingOptions().isLogOnFailure());
        assertEquals(LogEventType.ERROR, options.getLoggingOptions().getFailureLevel());
    }

    @Test
    @DisplayName("Should configure comprehensive logging")
    void testComprehensiveLogging() {
        String beforeMsg = "Looking for login button...";
        String afterMsg = "Login search completed";
        String successMsg = "Login button found";
        String failureMsg = "Login button not found";
        
        PatternFindOptions options = new PatternFindOptions.Builder()
                .withLogging(loggingBuilder -> loggingBuilder
                        .beforeActionMessage(beforeMsg)
                        .afterActionMessage(afterMsg)
                        .successMessage(successMsg)
                        .failureMessage(failureMsg)
                        .logBeforeAction(true)
                        .logAfterAction(true)
                        .logOnSuccess(true)
                        .logOnFailure(true))
                .build();

        ActionConfig.LoggingOptions logging = options.getLoggingOptions();
        assertEquals(beforeMsg, logging.getBeforeActionMessage());
        assertEquals(afterMsg, logging.getAfterActionMessage());
        assertEquals(successMsg, logging.getSuccessMessage());
        assertEquals(failureMsg, logging.getFailureMessage());
        assertTrue(logging.isLogBeforeAction());
        assertTrue(logging.isLogAfterAction());
        assertTrue(logging.isLogOnSuccess());
        assertTrue(logging.isLogOnFailure());
    }

    @Test
    @DisplayName("Should disable all logging")
    void testDisableAllLogging() {
        ClickOptions options = new ClickOptions.Builder()
                .withNoLogging()
                .build();

        ActionConfig.LoggingOptions logging = options.getLoggingOptions();
        assertFalse(logging.isLogBeforeAction());
        assertFalse(logging.isLogAfterAction());
        assertFalse(logging.isLogOnSuccess());
        assertFalse(logging.isLogOnFailure());
    }

    @Test
    @DisplayName("Should support method chaining")
    void testMethodChaining() {
        PatternFindOptions options = new PatternFindOptions.Builder()
                .withBeforeActionLog("Starting...")
                .withSuccessLog("Success!")
                .withFailureLog("Failed!")
                .withAfterActionLog("Completed")
                .build();

        assertNotNull(options.getLoggingOptions());
        assertEquals("Starting...", options.getLoggingOptions().getBeforeActionMessage());
        assertEquals("Success!", options.getLoggingOptions().getSuccessMessage());
        assertEquals("Failed!", options.getLoggingOptions().getFailureMessage());
        assertEquals("Completed", options.getLoggingOptions().getAfterActionMessage());
    }

    @Test
    @DisplayName("Should configure custom log levels")
    void testCustomLogLevels() {
        ActionConfig.LoggingOptions loggingOptions = ActionConfig.LoggingOptions.builder()
                .beforeActionMessage("Debug: Starting action")
                .beforeActionLevel(LogEventType.SYSTEM)
                .afterActionMessage("Info: Action completed")
                .afterActionLevel(LogEventType.ACTION)
                .successMessage("Success!")
                .successLevel(LogEventType.ACTION)
                .failureMessage("Critical: Action failed")
                .failureLevel(LogEventType.ERROR)
                .logBeforeAction(true)
                .logAfterAction(true)
                .logOnSuccess(true)
                .logOnFailure(true)
                .build();

        assertEquals(LogEventType.SYSTEM, loggingOptions.getBeforeActionLevel());
        assertEquals(LogEventType.ACTION, loggingOptions.getAfterActionLevel());
        assertEquals(LogEventType.ACTION, loggingOptions.getSuccessLevel());
        assertEquals(LogEventType.ERROR, loggingOptions.getFailureLevel());
    }

    @Test
    @DisplayName("Should handle placeholders in messages")
    void testMessagePlaceholders() {
        String messageWithPlaceholders = "Found {matchCount} matches with {confidence}% confidence in {duration}ms";
        
        PatternFindOptions options = new PatternFindOptions.Builder()
                .withSuccessLog(messageWithPlaceholders)
                .build();

        assertEquals(messageWithPlaceholders, options.getLoggingOptions().getSuccessMessage());
        // Actual placeholder replacement would be tested in integration tests
    }

    @Test
    @DisplayName("Should inherit logging options in subclasses")
    void testLoggingInheritance() {
        // Test that ClickOptions inherits logging from ActionConfig
        ClickOptions clickOptions = new ClickOptions.Builder()
                .withBeforeActionLog("Clicking...")
                .build();

        assertTrue(clickOptions instanceof ActionConfig);
        assertNotNull(clickOptions.getLoggingOptions());
        assertEquals("Clicking...", clickOptions.getLoggingOptions().getBeforeActionMessage());

        // Test that TypeOptions inherits logging from ActionConfig
        TypeOptions typeOptions = new TypeOptions.Builder()
                .withSuccessLog("Text typed successfully")
                .build();

        assertTrue(typeOptions instanceof ActionConfig);
        assertEquals("Text typed successfully", typeOptions.getLoggingOptions().getSuccessMessage());
    }

    @Test
    @DisplayName("Should preserve logging options through builder operations")
    void testLoggingPreservation() {
        PatternFindOptions options = new PatternFindOptions.Builder()
                .withBeforeActionLog("Step 1")
                .withSuccessLog("Found it!")
                .withFailureLog("Not found")
                .build();

        ActionConfig.LoggingOptions logging = options.getLoggingOptions();
        assertEquals("Step 1", logging.getBeforeActionMessage());
        assertEquals("Found it!", logging.getSuccessMessage());
        assertEquals("Not found", logging.getFailureMessage());
    }

    @Test
    @DisplayName("Should handle null messages gracefully")
    void testNullMessages() {
        PatternFindOptions options = new PatternFindOptions.Builder()
                .withBeforeActionLog(null)
                .withSuccessLog(null)
                .build();

        // Should not throw exceptions
        assertNull(options.getLoggingOptions().getBeforeActionMessage());
        assertNull(options.getLoggingOptions().getSuccessMessage());
        // But flags should still be set
        assertTrue(options.getLoggingOptions().isLogBeforeAction());
        assertTrue(options.getLoggingOptions().isLogOnSuccess());
    }

    @Test
    @DisplayName("Should configure logging through then() chaining")
    void testLoggingThroughChaining() {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .withBeforeActionLog("Finding element...")
                .withSuccessLog("Element found")
                .then(new ClickOptions.Builder()
                        .withBeforeActionLog("Clicking element...")
                        .withSuccessLog("Click successful")
                        .build())
                .build();

        // First action logging
        assertEquals("Finding element...", findOptions.getLoggingOptions().getBeforeActionMessage());
        assertEquals("Element found", findOptions.getLoggingOptions().getSuccessMessage());
        
        // Chained action would have its own logging (tested in integration tests)
    }

    @Test
    @DisplayName("Should support conditional logging configuration")
    void testConditionalLogging() {
        boolean debugMode = true;
        
        PatternFindOptions.Builder builder = new PatternFindOptions.Builder();
        
        if (debugMode) {
            builder.withBeforeActionLog("DEBUG: Starting search")
                   .withAfterActionLog("DEBUG: Search completed");
        }
        
        builder.withSuccessLog("Target found")
               .withFailureLog("Target not found");
        
        PatternFindOptions options = builder.build();
        
        assertEquals("DEBUG: Starting search", options.getLoggingOptions().getBeforeActionMessage());
        assertEquals("DEBUG: Search completed", options.getLoggingOptions().getAfterActionMessage());
        assertEquals("Target found", options.getLoggingOptions().getSuccessMessage());
        assertEquals("Target not found", options.getLoggingOptions().getFailureMessage());
    }
}