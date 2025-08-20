package io.github.jspinak.brobot.util.string;

import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for TextSelector - stochastic text selection utility.
 * Tests random and similarity-based selection strategies.
 */
@DisplayName("TextSelector Tests")
public class TextSelectorTest extends BrobotTestBase {
    
    private TextSelector textSelector;
    private Text sampleText;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        textSelector = new TextSelector();
        sampleText = new Text();
    }
    
    @Nested
    @DisplayName("Random Selection Method")
    class RandomSelectionMethod {
        
        @Test
        @DisplayName("Random selection from single string")
        public void testRandomSingleString() {
            sampleText.add("only");
            
            String result = textSelector.getString(TextSelector.Method.RANDOM, sampleText);
            
            assertEquals("only", result);
        }
        
        @Test
        @DisplayName("Random selection from multiple strings")
        public void testRandomMultipleStrings() {
            sampleText.add("first");
            sampleText.add("second");
            sampleText.add("third");
            
            String result = textSelector.getString(TextSelector.Method.RANDOM, sampleText);
            
            assertTrue(sampleText.getAll().contains(result));
        }
        
        @RepeatedTest(20)
        @DisplayName("Random distribution over multiple calls")
        public void testRandomDistribution() {
            sampleText.add("A");
            sampleText.add("B");
            sampleText.add("C");
            
            Map<String, Integer> counts = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                String result = textSelector.getString(TextSelector.Method.RANDOM, sampleText);
                counts.merge(result, 1, Integer::sum);
            }
            
            // All options should be selected at least once over 100 iterations
            assertTrue(counts.containsKey("A"));
            assertTrue(counts.containsKey("B"));
            assertTrue(counts.containsKey("C"));
        }
        
        @Test
        @DisplayName("Random from large collection")
        public void testRandomLargeCollection() {
            for (int i = 0; i < 100; i++) {
                sampleText.add("String" + i);
            }
            
            String result = textSelector.getString(TextSelector.Method.RANDOM, sampleText);
            
            assertTrue(result.startsWith("String"));
            assertTrue(sampleText.getAll().contains(result));
        }
    }
    
    @Nested
    @DisplayName("Most Similar Selection Method")
    class MostSimilarSelectionMethod {
        
        @Test
        @DisplayName("Most similar from identical strings")
        public void testMostSimilarIdentical() {
            sampleText.add("same");
            sampleText.add("same");
            sampleText.add("same");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("same", result);
        }
        
        @Test
        @DisplayName("Most similar with clear winner")
        public void testMostSimilarClearWinner() {
            sampleText.add("Submit");
            sampleText.add("Submit");
            sampleText.add("Subrnit"); // OCR error
            sampleText.add("Submit");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("Submit", result);
        }
        
        @Test
        @DisplayName("Most similar with variations")
        public void testMostSimilarVariations() {
            sampleText.add("Button");
            sampleText.add("Butt0n"); // OCR: o -> 0
            sampleText.add("Buttor"); // OCR: n -> r
            sampleText.add("Button");
            sampleText.add("Button");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("Button", result);
        }
        
        @Test
        @DisplayName("Most similar from empty text")
        public void testMostSimilarEmpty() {
            // Empty text
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("", result);
        }
        
        @Test
        @DisplayName("Most similar from single string")
        public void testMostSimilarSingle() {
            sampleText.add("single");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("single", result);
        }
        
        @Test
        @DisplayName("Most similar from two strings")
        public void testMostSimilarTwo() {
            sampleText.add("first");
            sampleText.add("second");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            // With only 2 strings, returns the first one
            assertEquals("first", result);
        }
    }
    
    @Nested
    @DisplayName("OCR Variation Scenarios")
    class OCRVariationScenarios {
        
        @Test
        @DisplayName("Common OCR substitutions")
        public void testCommonOCRSubstitutions() {
            // Common OCR confusion: l/I/1
            sampleText.add("File");
            sampleText.add("FiIe"); // l -> I
            sampleText.add("Fi1e"); // l -> 1
            sampleText.add("File");
            sampleText.add("File");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("File", result);
        }
        
        @Test
        @DisplayName("Number-letter confusion")
        public void testNumberLetterConfusion() {
            // 0/O confusion
            sampleText.add("$100.00");
            sampleText.add("$10O.00"); // 0 -> O
            sampleText.add("$100.00");
            sampleText.add("$1OO.OO"); // multiple errors
            sampleText.add("$100.00");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("$100.00", result);
        }
        
        @Test
        @DisplayName("Punctuation variations")
        public void testPunctuationVariations() {
            sampleText.add("user@example.com");
            sampleText.add("user@examp1e.com"); // l -> 1
            sampleText.add("user@example.com");
            sampleText.add("user@example,com"); // . -> ,
            sampleText.add("user@example.com");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("user@example.com", result);
        }
        
        @Test
        @DisplayName("Case variations")
        public void testCaseVariations() {
            sampleText.add("Click Here");
            sampleText.add("CLICK HERE"); // All caps
            sampleText.add("Click Here");
            sampleText.add("click here"); // All lowercase
            sampleText.add("Click Here");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("Click Here", result);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("All different strings")
        public void testAllDifferentStrings() {
            sampleText.add("alpha");
            sampleText.add("beta");
            sampleText.add("gamma");
            sampleText.add("delta");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            // Should return one of them (deterministic based on similarity scores)
            assertTrue(sampleText.getAll().contains(result));
        }
        
        @Test
        @DisplayName("Very long strings")
        public void testVeryLongStrings() {
            String base = "a".repeat(1000);
            sampleText.add(base + "x");
            sampleText.add(base + "y");
            sampleText.add(base + "x");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals(base + "x", result);
        }
        
        @Test
        @DisplayName("Unicode strings")
        public void testUnicodeStrings() {
            sampleText.add("こんにちは");
            sampleText.add("こんにちは");
            sampleText.add("こんにちわ"); // Variation
            sampleText.add("こんにちは");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("こんにちは", result);
        }
        
        @Test
        @DisplayName("Whitespace variations")
        public void testWhitespaceVariations() {
            sampleText.add("Hello World");
            sampleText.add("Hello  World"); // Extra space
            sampleText.add("Hello World");
            sampleText.add("Hello\tWorld"); // Tab
            sampleText.add("Hello World");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("Hello World", result);
        }
        
        @Test
        @DisplayName("Empty strings in collection")
        public void testEmptyStrings() {
            sampleText.add("");
            sampleText.add("text");
            sampleText.add("");
            sampleText.add("text");
            sampleText.add("text");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("text", result);
        }
    }
    
    @Nested
    @DisplayName("Method Selection")
    class MethodSelection {
        
        @ParameterizedTest
        @EnumSource(TextSelector.Method.class)
        @DisplayName("All methods work with single string")
        public void testAllMethodsSingleString(TextSelector.Method method) {
            sampleText.add("test");
            
            String result = textSelector.getString(method, sampleText);
            
            assertEquals("test", result);
        }
        
        @ParameterizedTest
        @EnumSource(TextSelector.Method.class)
        @DisplayName("All methods handle multiple strings")
        public void testAllMethodsMultipleStrings(TextSelector.Method method) {
            sampleText.add("one");
            sampleText.add("two");
            sampleText.add("three");
            
            String result = textSelector.getString(method, sampleText);
            
            assertNotNull(result);
            assertTrue(sampleText.getAll().contains(result));
        }
    }
    
    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {
        
        @Test
        @DisplayName("Button text variations")
        public void testButtonTextVariations() {
            // Simulate OCR reading "Submit" button multiple times
            sampleText.add("Submit");
            sampleText.add("Subrnit");
            sampleText.add("Submit");
            sampleText.add("5ubmit"); // S -> 5
            sampleText.add("Submit");
            sampleText.add("Submit");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("Submit", result);
        }
        
        @Test
        @DisplayName("Menu item variations")
        public void testMenuItemVariations() {
            // File menu item
            sampleText.add("File");
            sampleText.add("FiIe"); // l -> I
            sampleText.add("File");
            sampleText.add("File");
            sampleText.add("Flle"); // i -> l
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("File", result);
        }
        
        @Test
        @DisplayName("Status message variations")
        public void testStatusMessageVariations() {
            // Status: "Processing..."
            sampleText.add("Processing...");
            sampleText.add("Processing,..");  // . -> ,
            sampleText.add("Processing...");
            sampleText.add("Process1ng..."); // i -> 1
            sampleText.add("Processing...");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("Processing...", result);
        }
        
        @Test
        @DisplayName("Error message variations")
        public void testErrorMessageVariations() {
            // Error message with special characters
            sampleText.add("Error: File not found!");
            sampleText.add("Error: FiIe not found!"); // l -> I
            sampleText.add("Error: File not found!");
            sampleText.add("Error: File not found1"); // ! -> 1
            sampleText.add("Error: File not found!");
            
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals("Error: File not found!", result);
        }
    }
    
    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {
        
        @Test
        @DisplayName("Large collection performance")
        public void testLargeCollectionPerformance() {
            // Add 100 variations with slight differences
            String base = "TestString";
            for (int i = 0; i < 50; i++) {
                sampleText.add(base);
            }
            for (int i = 0; i < 50; i++) {
                sampleText.add(base + i % 10);
            }
            
            long startTime = System.currentTimeMillis();
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            long endTime = System.currentTimeMillis();
            
            assertEquals(base, result);
            assertTrue(endTime - startTime < 1000); // Should complete within 1 second
        }
        
        @Test
        @DisplayName("Consistent results")
        public void testConsistentResults() {
            sampleText.add("test");
            sampleText.add("test");
            sampleText.add("t3st");
            
            String result1 = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            String result2 = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            String result3 = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            
            assertEquals(result1, result2);
            assertEquals(result2, result3);
        }
    }
}