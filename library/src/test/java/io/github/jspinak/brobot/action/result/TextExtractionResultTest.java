package io.github.jspinak.brobot.action.result;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.test.BrobotTestBase;

public class TextExtractionResultTest extends BrobotTestBase {

    @Test
    public void testMergeWithNullAccumulatedText() {
        // Create two TextExtractionResult instances without initializing accumulatedText
        TextExtractionResult result1 = new TextExtractionResult();
        TextExtractionResult result2 = new TextExtractionResult();

        // Add some text to result2
        result2.addText("test text");

        // This should not throw NullPointerException
        assertDoesNotThrow(() -> result1.merge(result2));

        // Verify the merge worked correctly
        assertEquals("test text", result1.getCombinedText());
    }

    @Test
    public void testMergeWithBothNullAccumulatedText() {
        // Create two TextExtractionResult instances without any text
        TextExtractionResult result1 = new TextExtractionResult();
        TextExtractionResult result2 = new TextExtractionResult();

        // This should not throw NullPointerException
        assertDoesNotThrow(() -> result1.merge(result2));

        // Verify result is still empty
        assertEquals("", result1.getCombinedText());
        assertFalse(result1.hasText());
    }

    @Test
    public void testGetCombinedTextWithNullAccumulatedText() {
        TextExtractionResult result = new TextExtractionResult();

        // Should return empty string, not throw NPE
        assertEquals("", result.getCombinedText());
    }

    @Test
    public void testGetTextLinesWithNullAccumulatedText() {
        TextExtractionResult result = new TextExtractionResult();

        // Should return empty list, not throw NPE
        assertNotNull(result.getTextLines());
        assertTrue(result.getTextLines().isEmpty());
    }

    @Test
    public void testHasTextWithNullFields() {
        TextExtractionResult result = new TextExtractionResult();

        // Should return false, not throw NPE
        assertFalse(result.hasText());
    }

    @Test
    public void testHasSelectedTextWithNull() {
        TextExtractionResult result = new TextExtractionResult();

        // Should return false, not throw NPE
        assertFalse(result.hasSelectedText());
    }

    @Test
    public void testGetTextCountWithNullAccumulatedText() {
        TextExtractionResult result = new TextExtractionResult();

        // Should return 0, not throw NPE
        assertEquals(0, result.getTextCount());
    }

    @Test
    public void testFormatWithNullFields() {
        TextExtractionResult result = new TextExtractionResult();

        // Should not throw NPE
        String formatted = result.format();
        assertNotNull(formatted);
        assertEquals("No text extracted", formatted);
    }
}
