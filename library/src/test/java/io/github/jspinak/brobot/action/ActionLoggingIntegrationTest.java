package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ActionConfig logging functionality.
 * Tests that logging configuration is properly integrated with the action system.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.logging.enabled=true",
    "brobot.logging.console.enabled=true",
    "brobot.logging.console.colored=false"
})
class ActionLoggingIntegrationTest {

    @Test
    @DisplayName("Should create pattern find action with comprehensive logging")
    void testPatternFindWithLogging() {
        // Create a find action with all logging options
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .withBeforeActionLog("Searching for login button...")
                .withAfterActionLog("Search completed in {duration}ms")
                .withSuccessLog("Login button found at ({x}, {y})")
                .withFailureLog("Login button not found after {duration}ms")
                .build();
        
        // Verify all logging options are set
        assertNotNull(findOptions.getLoggingOptions());
        assertEquals("Searching for login button...", findOptions.getLoggingOptions().getBeforeActionMessage());
        assertEquals("Search completed in {duration}ms", findOptions.getLoggingOptions().getAfterActionMessage());
        assertEquals("Login button found at ({x}, {y})", findOptions.getLoggingOptions().getSuccessMessage());
        assertEquals("Login button not found after {duration}ms", findOptions.getLoggingOptions().getFailureMessage());
        
        // Verify logging flags
        assertTrue(findOptions.getLoggingOptions().isLogBeforeAction());
        assertTrue(findOptions.getLoggingOptions().isLogAfterAction());
        assertTrue(findOptions.getLoggingOptions().isLogOnSuccess());
        assertTrue(findOptions.getLoggingOptions().isLogOnFailure());
    }
    
    @Test
    @DisplayName("Should create click action with logging")
    void testClickWithLogging() {
        ClickOptions clickOptions = new ClickOptions.Builder()
                .withBeforeActionLog("Clicking submit button...")
                .withSuccessLog("Button clicked successfully")
                .withFailureLog("Failed to click button")
                .build();
        
        assertEquals("Clicking submit button...", clickOptions.getLoggingOptions().getBeforeActionMessage());
        assertEquals("Button clicked successfully", clickOptions.getLoggingOptions().getSuccessMessage());
        assertEquals("Failed to click button", clickOptions.getLoggingOptions().getFailureMessage());
    }
    
    @Test
    @DisplayName("Should create type action with logging")
    void testTypeWithLogging() {
        TypeOptions typeOptions = new TypeOptions.Builder()
                .withBeforeActionLog("Entering username...")
                .withSuccessLog("Text entered successfully")
                .withFailureLog("Failed to enter text")
                .setTypeDelay(0.05)
                .build();
        
        assertEquals("Entering username...", typeOptions.getLoggingOptions().getBeforeActionMessage());
        assertEquals("Text entered successfully", typeOptions.getLoggingOptions().getSuccessMessage());
        assertEquals("Failed to enter text", typeOptions.getLoggingOptions().getFailureMessage());
        assertEquals(0.05, typeOptions.getTypeDelay(), 0.001);
    }
    
    @Test
    @DisplayName("Should chain actions with individual logging")
    void testActionChainingWithLogging() {
        PatternFindOptions chainedAction = new PatternFindOptions.Builder()
                .withBeforeActionLog("Step 1: Finding element...")
                .withSuccessLog("Step 1: Element found")
                .then(new ClickOptions.Builder()
                        .withBeforeActionLog("Step 2: Clicking element...")
                        .withSuccessLog("Step 2: Click successful")
                        .build())
                .then(new TypeOptions.Builder()
                        .withBeforeActionLog("Step 3: Typing text...")
                        .withSuccessLog("Step 3: Text entered")
                        .build())
                .build();
        
        // Verify first action logging
        assertEquals("Step 1: Finding element...", chainedAction.getLoggingOptions().getBeforeActionMessage());
        assertEquals("Step 1: Element found", chainedAction.getLoggingOptions().getSuccessMessage());
        
        // Verify chained actions exist
        assertFalse(chainedAction.getSubsequentActions().isEmpty());
        assertEquals(2, chainedAction.getSubsequentActions().size());
        
        // Verify chained action logging
        ActionConfig secondAction = chainedAction.getSubsequentActions().get(0);
        assertEquals("Step 2: Clicking element...", secondAction.getLoggingOptions().getBeforeActionMessage());
        assertEquals("Step 2: Click successful", secondAction.getLoggingOptions().getSuccessMessage());
        
        ActionConfig thirdAction = chainedAction.getSubsequentActions().get(1);
        assertEquals("Step 3: Typing text...", thirdAction.getLoggingOptions().getBeforeActionMessage());
        assertEquals("Step 3: Text entered", thirdAction.getLoggingOptions().getSuccessMessage());
    }
    
    @Test
    @DisplayName("Should configure custom logging with builder")
    void testCustomLoggingConfiguration() {
        PatternFindOptions customLogging = new PatternFindOptions.Builder()
                .withLogging(loggingBuilder -> loggingBuilder
                        .beforeActionMessage("Custom before message")
                        .afterActionMessage("Custom after message")
                        .successMessage("Custom success")
                        .failureMessage("Custom failure")
                        .beforeActionLevel(LogEventType.SYSTEM)
                        .afterActionLevel(LogEventType.METRICS)
                        .successLevel(LogEventType.ACTION)
                        .failureLevel(LogEventType.ERROR)
                        .logBeforeAction(true)
                        .logAfterAction(true)
                        .logOnSuccess(true)
                        .logOnFailure(true))
                .build();
        
        ActionConfig.LoggingOptions logging = customLogging.getLoggingOptions();
        assertEquals("Custom before message", logging.getBeforeActionMessage());
        assertEquals("Custom after message", logging.getAfterActionMessage());
        assertEquals("Custom success", logging.getSuccessMessage());
        assertEquals("Custom failure", logging.getFailureMessage());
        assertEquals(LogEventType.SYSTEM, logging.getBeforeActionLevel());
        assertEquals(LogEventType.METRICS, logging.getAfterActionLevel());
        assertEquals(LogEventType.ACTION, logging.getSuccessLevel());
        assertEquals(LogEventType.ERROR, logging.getFailureLevel());
    }
    
    @Test
    @DisplayName("Should disable logging completely")
    void testDisableLogging() {
        PatternFindOptions noLogging = new PatternFindOptions.Builder()
                .withNoLogging()
                .build();
        
        assertFalse(noLogging.getLoggingOptions().isLogBeforeAction());
        assertFalse(noLogging.getLoggingOptions().isLogAfterAction());
        assertFalse(noLogging.getLoggingOptions().isLogOnSuccess());
        assertFalse(noLogging.getLoggingOptions().isLogOnFailure());
    }
    
    @Test
    @DisplayName("Should support partial logging configuration")
    void testPartialLoggingConfiguration() {
        // Only before and success logging
        PatternFindOptions partialLogging = new PatternFindOptions.Builder()
                .withBeforeActionLog("Starting operation...")
                .withSuccessLog("Operation completed")
                .build();
        
        assertTrue(partialLogging.getLoggingOptions().isLogBeforeAction());
        assertTrue(partialLogging.getLoggingOptions().isLogOnSuccess());
        assertFalse(partialLogging.getLoggingOptions().isLogAfterAction());
        assertTrue(partialLogging.getLoggingOptions().isLogOnFailure()); // Default is true
        
        assertEquals("Starting operation...", partialLogging.getLoggingOptions().getBeforeActionMessage());
        assertEquals("Operation completed", partialLogging.getLoggingOptions().getSuccessMessage());
        assertNull(partialLogging.getLoggingOptions().getAfterActionMessage());
        assertNull(partialLogging.getLoggingOptions().getFailureMessage());
    }
    
    @Test
    @DisplayName("Should preserve logging through multiple builder operations")
    void testLoggingPersistenceThroughBuilding() {
        PatternFindOptions complexConfig = new PatternFindOptions.Builder()
                .withBeforeActionLog("Initial log")
                .withSuccessLog("Success log")
                .withFailureLog("Failure log")
                .setPauseAfterEnd(0.5)
                .withAfterActionLog("After log")
                .build();
        
        // Verify all logging persisted through other builder operations
        assertEquals("Initial log", complexConfig.getLoggingOptions().getBeforeActionMessage());
        assertEquals("Success log", complexConfig.getLoggingOptions().getSuccessMessage());
        assertEquals("Failure log", complexConfig.getLoggingOptions().getFailureMessage());
        assertEquals("After log", complexConfig.getLoggingOptions().getAfterActionMessage());
        
        // Verify other settings also work
        assertEquals(0.5, complexConfig.getPauseAfterEnd(), 0.001);
    }
    
    @Test
    @DisplayName("Should handle message placeholders")
    void testMessagePlaceholders() {
        PatternFindOptions placeholderOptions = new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for {target}...")
                .withSuccessLog("Found {matchCount} matches with {confidence}% confidence at ({x}, {y})")
                .withFailureLog("Failed to find {target} after {duration}ms")
                .withAfterActionLog("Action completed in {duration}ms - success: {success}")
                .build();
        
        // Verify placeholders are preserved in configuration
        assertTrue(placeholderOptions.getLoggingOptions().getBeforeActionMessage().contains("{target}"));
        assertTrue(placeholderOptions.getLoggingOptions().getSuccessMessage().contains("{matchCount}"));
        assertTrue(placeholderOptions.getLoggingOptions().getSuccessMessage().contains("{confidence}"));
        assertTrue(placeholderOptions.getLoggingOptions().getSuccessMessage().contains("{x}"));
        assertTrue(placeholderOptions.getLoggingOptions().getSuccessMessage().contains("{y}"));
        assertTrue(placeholderOptions.getLoggingOptions().getFailureMessage().contains("{duration}"));
        assertTrue(placeholderOptions.getLoggingOptions().getAfterActionMessage().contains("{success}"));
    }
}