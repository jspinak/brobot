package io.github.jspinak.brobot.model.element;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for Text - represents OCR-extracted text with variability. Tests
 * stochastic OCR results, multiple readings, and collection operations.
 */
@DisplayName("Text Tests")
public class TextTest extends BrobotTestBase {

    private Text text;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        text = new Text();
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Default constructor creates empty text")
        public void testDefaultConstructor() {
            Text newText = new Text();

            assertNotNull(newText);
            assertNotNull(newText.getStrings());
            assertTrue(newText.isEmpty());
            assertEquals(0, newText.size());
        }

        @Test
        @DisplayName("Add single string")
        public void testAddSingleString() {
            text.add("Hello");

            assertEquals(1, text.size());
            assertFalse(text.isEmpty());
            assertEquals("Hello", text.get(0));
        }

        @Test
        @DisplayName("Add multiple strings")
        public void testAddMultipleStrings() {
            text.add("First");
            text.add("Second");
            text.add("Third");

            assertEquals(3, text.size());
            assertEquals("First", text.get(0));
            assertEquals("Second", text.get(1));
            assertEquals("Third", text.get(2));
        }

        @Test
        @DisplayName("Add null string")
        public void testAddNullString() {
            text.add(null);

            assertEquals(1, text.size());
            assertNull(text.get(0));
        }

        @Test
        @DisplayName("Add empty string")
        public void testAddEmptyString() {
            text.add("");

            assertEquals(1, text.size());
            assertEquals("", text.get(0));
        }
    }

    @Nested
    @DisplayName("OCR Variability Simulation")
    class OCRVariability {

        @Test
        @DisplayName("Store multiple OCR readings of same text")
        public void testMultipleOCRReadings() {
            // Simulating OCR variations of "Submit"
            text.add("Submit");
            text.add("Subrnit"); // Common OCR error: m→rn
            text.add("Submit");
            text.add("5ubmit"); // Common OCR error: S→5

            assertEquals(4, text.size());

            // Most common reading appears twice
            List<String> readings = text.getAll();
            long submitCount = readings.stream().filter(s -> "Submit".equals(s)).count();
            assertEquals(2, submitCount);
        }

        @Test
        @DisplayName("Track currency OCR variations")
        public void testCurrencyOCRVariations() {
            // Common OCR variations for "$100.00"
            text.add("$100.00");
            text.add("$100,00"); // Period vs comma confusion
            text.add("S100.00"); // $ vs S confusion
            text.add("$100.00");

            assertEquals(4, text.size());
            assertTrue(text.getAll().contains("$100.00"));
            assertTrue(text.getAll().contains("$100,00"));
        }

        @Test
        @DisplayName("Track special character OCR variations")
        public void testSpecialCharacterVariations() {
            // Common OCR variations for "I/O Error"
            text.add("I/O Error");
            text.add("l/O Error"); // I vs l confusion
            text.add("I/0 Error"); // O vs 0 confusion
            text.add("I/O Error");

            assertEquals(4, text.size());

            // Check for common variations
            List<String> allReadings = text.getAll();
            assertTrue(allReadings.contains("I/O Error"));
            assertTrue(allReadings.contains("l/O Error"));
            assertTrue(allReadings.contains("I/0 Error"));
        }

        @Test
        @DisplayName("Case sensitivity variations")
        public void testCaseSensitivityVariations() {
            text.add("LOGIN");
            text.add("Login");
            text.add("login");
            text.add("LOGIN");

            assertEquals(4, text.size());

            // All variations preserved
            List<String> readings = text.getAll();
            assertTrue(readings.contains("LOGIN"));
            assertTrue(readings.contains("Login"));
            assertTrue(readings.contains("login"));
        }
    }

    @Nested
    @DisplayName("Collection Operations")
    class CollectionOperations {

        @Test
        @DisplayName("GetAll returns all strings")
        public void testGetAll() {
            text.add("One");
            text.add("Two");
            text.add("Three");

            List<String> all = text.getAll();

            assertEquals(3, all.size());
            assertEquals(Arrays.asList("One", "Two", "Three"), all);
        }

        @Test
        @DisplayName("GetAll on empty text")
        public void testGetAllEmpty() {
            List<String> all = text.getAll();

            assertNotNull(all);
            assertTrue(all.isEmpty());
        }

        @Test
        @DisplayName("AddAll from another Text")
        public void testAddAll() {
            text.add("First");
            text.add("Second");

            Text otherText = new Text();
            otherText.add("Third");
            otherText.add("Fourth");

            text.addAll(otherText);

            assertEquals(4, text.size());
            assertEquals("First", text.get(0));
            assertEquals("Second", text.get(1));
            assertEquals("Third", text.get(2));
            assertEquals("Fourth", text.get(3));
        }

        @Test
        @DisplayName("AddAll from empty Text")
        public void testAddAllEmpty() {
            text.add("Original");

            Text emptyText = new Text();
            text.addAll(emptyText);

            assertEquals(1, text.size());
            assertEquals("Original", text.get(0));
        }

        @Test
        @DisplayName("AddAll to empty Text")
        public void testAddAllToEmpty() {
            Text sourceText = new Text();
            sourceText.add("Source1");
            sourceText.add("Source2");

            text.addAll(sourceText);

            assertEquals(2, text.size());
            assertEquals("Source1", text.get(0));
            assertEquals("Source2", text.get(1));
        }
    }

    @Nested
    @DisplayName("Size and Empty Checks")
    class SizeAndEmptyChecks {

        @Test
        @DisplayName("Size of empty text")
        public void testSizeEmpty() {
            assertEquals(0, text.size());
        }

        @Test
        @DisplayName("Size after additions")
        public void testSizeAfterAdditions() {
            assertEquals(0, text.size());

            text.add("One");
            assertEquals(1, text.size());

            text.add("Two");
            assertEquals(2, text.size());

            text.add("Three");
            assertEquals(3, text.size());
        }

        @Test
        @DisplayName("IsEmpty on new text")
        public void testIsEmptyNew() {
            assertTrue(text.isEmpty());
        }

        @Test
        @DisplayName("IsEmpty after adding string")
        public void testIsEmptyAfterAdd() {
            text.add("Not empty");
            assertFalse(text.isEmpty());
        }

        @Test
        @DisplayName("IsEmpty after adding null")
        public void testIsEmptyAfterAddNull() {
            text.add(null);
            assertFalse(text.isEmpty());
            assertEquals(1, text.size());
        }

        @Test
        @DisplayName("IsEmpty after adding empty string")
        public void testIsEmptyAfterAddEmptyString() {
            text.add("");
            assertFalse(text.isEmpty());
            assertEquals(1, text.size());
        }
    }

    @Nested
    @DisplayName("Indexed Access")
    class IndexedAccess {

        @BeforeEach
        public void setupIndexedText() {
            text.add("Zero");
            text.add("One");
            text.add("Two");
            text.add("Three");
        }

        @Test
        @DisplayName("Get by valid index")
        public void testGetValidIndex() {
            assertEquals("Zero", text.get(0));
            assertEquals("One", text.get(1));
            assertEquals("Two", text.get(2));
            assertEquals("Three", text.get(3));
        }

        @Test
        @DisplayName("Get by negative index throws exception")
        public void testGetNegativeIndex() {
            assertThrows(
                    IndexOutOfBoundsException.class,
                    () -> {
                        text.get(-1);
                    });
        }

        @Test
        @DisplayName("Get by out of bounds index throws exception")
        public void testGetOutOfBoundsIndex() {
            assertThrows(
                    IndexOutOfBoundsException.class,
                    () -> {
                        text.get(4);
                    });
        }

        @Test
        @DisplayName("Get from empty text throws exception")
        public void testGetFromEmpty() {
            Text emptyText = new Text();
            assertThrows(
                    IndexOutOfBoundsException.class,
                    () -> {
                        emptyText.get(0);
                    });
        }
    }

    @Nested
    @DisplayName("Real-World OCR Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Button text variations")
        public void testButtonTextVariations() {
            // Common button text OCR results
            text.add("OK");
            text.add("0K"); // O vs 0
            text.add("OK");
            text.add("Ok"); // Case variation

            assertEquals(4, text.size());

            // Most common reading
            long okCount = text.getAll().stream().filter(s -> "OK".equals(s)).count();
            assertEquals(2, okCount);
        }

        @Test
        @DisplayName("Date format variations")
        public void testDateFormatVariations() {
            // OCR reading dates
            text.add("01/15/2025");
            text.add("01/l5/2025"); // 1 vs l
            text.add("01/15/2025");
            text.add("0l/15/2025"); // 1 vs l in different position

            assertEquals(4, text.size());

            // Check for most common correct reading
            assertTrue(text.getAll().contains("01/15/2025"));
        }

        @Test
        @DisplayName("Email address variations")
        public void testEmailVariations() {
            // OCR reading email addresses
            text.add("user@example.com");
            text.add("user@examp1e.com"); // l vs 1
            text.add("user@example.com");
            text.add("user@example,com"); // . vs ,

            assertEquals(4, text.size());

            // Correct email appears multiple times
            long correctCount =
                    text.getAll().stream().filter(s -> "user@example.com".equals(s)).count();
            assertEquals(2, correctCount);
        }

        @Test
        @DisplayName("Phone number variations")
        public void testPhoneNumberVariations() {
            // OCR reading phone numbers
            text.add("(555) 123-4567");
            text.add("[555] 123-4567"); // Bracket confusion
            text.add("(555) l23-4567"); // 1 vs l
            text.add("(555) 123-4567");

            assertEquals(4, text.size());

            // Check variations exist
            List<String> readings = text.getAll();
            assertTrue(readings.contains("(555) 123-4567"));
            assertTrue(readings.contains("[555] 123-4567"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Large number of readings")
        public void testLargeNumberOfReadings() {
            // Simulate many OCR attempts
            for (int i = 0; i < 1000; i++) {
                text.add("Reading " + i);
            }

            assertEquals(1000, text.size());
            assertEquals("Reading 0", text.get(0));
            assertEquals("Reading 999", text.get(999));
        }

        @Test
        @DisplayName("Very long strings")
        public void testVeryLongStrings() {
            StringBuilder longString = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                longString.append("a");
            }

            text.add(longString.toString());

            assertEquals(1, text.size());
            assertEquals(10000, text.get(0).length());
        }

        @Test
        @DisplayName("Mixed null and non-null strings")
        public void testMixedNullAndNonNull() {
            text.add("Valid");
            text.add(null);
            text.add("Another");
            text.add(null);
            text.add("Last");

            assertEquals(5, text.size());
            assertEquals("Valid", text.get(0));
            assertNull(text.get(1));
            assertEquals("Another", text.get(2));
            assertNull(text.get(3));
            assertEquals("Last", text.get(4));
        }

        @ParameterizedTest
        @DisplayName("Special characters in strings")
        @ValueSource(strings = {"\n", "\t", "\r", "\\", "\"", "'", "\u0000"})
        public void testSpecialCharacters(String special) {
            text.add(special);

            assertEquals(1, text.size());
            assertEquals(special, text.get(0));
        }
    }

    @Nested
    @DisplayName("Statistical Analysis")
    class StatisticalAnalysis {

        @Test
        @DisplayName("Find most common reading")
        public void testFindMostCommonReading() {
            // Simulate multiple OCR attempts with variations
            text.add("Submit");
            text.add("Submit");
            text.add("Subrnit");
            text.add("Submit");
            text.add("5ubmit");

            // Count occurrences
            String mostCommon = null;
            int maxCount = 0;

            for (String str : text.getAll()) {
                int count = 0;
                for (String compare : text.getAll()) {
                    if (str != null && str.equals(compare)) {
                        count++;
                    }
                }
                if (count > maxCount) {
                    maxCount = count;
                    mostCommon = str;
                }
            }

            assertEquals("Submit", mostCommon);
            assertEquals(3, maxCount);
        }

        @Test
        @DisplayName("Calculate variation rate")
        public void testCalculateVariationRate() {
            // Add OCR readings
            text.add("Login");
            text.add("Login");
            text.add("Logln"); // Variation
            text.add("Login");
            text.add("L0gin"); // Variation

            // Count unique values
            long uniqueCount = text.getAll().stream().distinct().count();

            // Variation rate
            double variationRate = (double) uniqueCount / text.size();

            assertEquals(3, uniqueCount); // "Login", "Logln", "L0gin"
            assertEquals(0.6, variationRate, 0.01); // 3/5 = 0.6
        }
    }
}
