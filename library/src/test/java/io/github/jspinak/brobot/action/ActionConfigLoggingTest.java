package io.github.jspinak.brobot.action;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

/** Tests for the logging functionality added to ActionConfig. */
public class ActionConfigLoggingTest {

    @Test
    public void testLoggingMethodsInPatternFindOptions() {
        PatternFindOptions options =
                new PatternFindOptions.Builder()
                        .withBeforeActionLog("Starting pattern search...")
                        .withAfterActionLog("Pattern search completed")
                        .withSuccessLog("Pattern found successfully!")
                        .withFailureLog("Pattern not found")
                        .build();

        assertNotNull(options.getBeforeActionLog());
        assertEquals("Starting pattern search...", options.getBeforeActionLog());
        assertEquals("Pattern search completed", options.getAfterActionLog());
        assertEquals("Pattern found successfully!", options.getSuccessLog());
        assertEquals("Pattern not found", options.getFailureLog());
    }

    @Test
    public void testLoggingMethodsInClickOptions() {
        ClickOptions options =
                new ClickOptions.Builder()
                        .withBeforeActionLog("Preparing to click...")
                        .withAfterActionLog("Click action completed")
                        .withSuccessLog("Click successful!")
                        .withFailureLog("Click failed")
                        .setNumberOfClicks(2)
                        .build();

        assertNotNull(options.getBeforeActionLog());
        assertEquals("Preparing to click...", options.getBeforeActionLog());
        assertEquals("Click action completed", options.getAfterActionLog());
        assertEquals("Click successful!", options.getSuccessLog());
        assertEquals("Click failed", options.getFailureLog());
        assertEquals(2, options.getNumberOfClicks());
    }

    @Test
    public void testChainedLoggingMethods() {
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .withBeforeActionLog("Looking for save button...")
                        .withSuccessLog("Save button found")
                        .build();

        ClickOptions clickOptions =
                new ClickOptions.Builder()
                        .withBeforeActionLog("Clicking save button...")
                        .withSuccessLog("Successfully saved!")
                        .withFailureLog("Save operation failed")
                        .build();

        // Verify that both configs have their logging set up
        assertEquals("Looking for save button...", findOptions.getBeforeActionLog());
        assertEquals("Save button found", findOptions.getSuccessLog());

        assertEquals("Clicking save button...", clickOptions.getBeforeActionLog());
        assertEquals("Successfully saved!", clickOptions.getSuccessLog());
        assertEquals("Save operation failed", clickOptions.getFailureLog());
    }

    @Test
    public void testOptionalLoggingFields() {
        // Test that logging fields are optional (can be null)
        PatternFindOptions options = new PatternFindOptions.Builder().build();

        assertNull(options.getBeforeActionLog());
        assertNull(options.getAfterActionLog());
        assertNull(options.getSuccessLog());
        assertNull(options.getFailureLog());
    }
}
