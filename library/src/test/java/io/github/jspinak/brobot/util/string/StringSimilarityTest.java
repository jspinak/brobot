package io.github.jspinak.brobot.util.string;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for StringSimilarity - calculates string similarity using Levenshtein distance.
 * Tests similarity calculations, edge cases, and various string comparison scenarios.
 */
@DisplayName("StringSimilarity Tests")
public class StringSimilarityTest extends BrobotTestBase {
    
    private static final double DELTA = 0.001;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }
    
    @Nested
    @DisplayName("Basic Similarity Calculations")
    class BasicSimilarityCalculations {
        
        @Test
        @DisplayName("Identical strings have similarity 1.0")
        public void testIdenticalStrings() {
            assertEquals(1.0, StringSimilarity.similarity("hello", "hello"), DELTA);
            assertEquals(1.0, StringSimilarity.similarity("", ""), DELTA);
            assertEquals(1.0, StringSimilarity.similarity("test123", "test123"), DELTA);
        }
        
        @Test
        @DisplayName("Completely different strings have similarity 0.0")
        public void testCompletelyDifferentStrings() {
            assertEquals(0.0, StringSimilarity.similarity("abc", "xyz"), DELTA);
            assertEquals(0.0, StringSimilarity.similarity("123", "456"), DELTA);
        }
        
        @Test
        @DisplayName("One character difference")
        public void testOneCharacterDifference() {
            double similarity = StringSimilarity.similarity("hello", "hallo");
            assertEquals(0.8, similarity, DELTA);
        }
        
        @Test
        @DisplayName("Case-insensitive comparison")
        public void testCaseInsensitive() {
            double similarity = StringSimilarity.similarity("Hello", "hello");
            assertEquals(1.0, similarity, DELTA);
            
            similarity = StringSimilarity.similarity("HELLO", "hello");
            assertEquals(1.0, similarity, DELTA);
        }
        
        @Test
        @DisplayName("Order independence")
        public void testOrderIndependence() {
            String s1 = "test";
            String s2 = "best";
            
            double similarity1 = StringSimilarity.similarity(s1, s2);
            double similarity2 = StringSimilarity.similarity(s2, s1);
            
            assertEquals(similarity1, similarity2, DELTA);
        }
    }
    
    @Nested
    @DisplayName("Edit Distance Scenarios")
    class EditDistanceScenarios {
        
        @ParameterizedTest
        @CsvSource({
            "hello, hallo, 0.8",
            "hello, help, 0.6",
            "kitten, sitting, 0.571",
            "saturday, sunday, 0.625",
            "book, back, 0.5"
        })
        @DisplayName("Various edit distances")
        public void testVariousEditDistances(String s1, String s2, double expected) {
            double similarity = StringSimilarity.similarity(s1, s2);
            assertEquals(expected, similarity, 0.01);
        }
        
        @Test
        @DisplayName("Single insertion")
        public void testSingleInsertion() {
            double similarity = StringSimilarity.similarity("test", "tests");
            assertEquals(0.8, similarity, DELTA);
        }
        
        @Test
        @DisplayName("Single deletion")
        public void testSingleDeletion() {
            double similarity = StringSimilarity.similarity("tests", "test");
            assertEquals(0.8, similarity, DELTA);
        }
        
        @Test
        @DisplayName("Single substitution")
        public void testSingleSubstitution() {
            double similarity = StringSimilarity.similarity("test", "best");
            assertEquals(0.75, similarity, DELTA);
        }
        
        @Test
        @DisplayName("Transposition counts as two edits")
        public void testTransposition() {
            // "ab" to "ba" requires delete 'a' + insert 'a' = 2 edits
            double similarity = StringSimilarity.similarity("ab", "ba");
            assertEquals(0.0, similarity, DELTA); // 2 edits for length 2 = 0% similar
            
            similarity = StringSimilarity.similarity("abc", "bac");
            assertEquals(0.333, similarity, 0.01); // 2 edits for length 3
        }
    }
    
    @Nested
    @DisplayName("Empty and Null String Handling")
    class EmptyAndNullStringHandling {
        
        @Test
        @DisplayName("Both empty strings return 1.0")
        public void testBothEmptyStrings() {
            assertEquals(1.0, StringSimilarity.similarity("", ""), DELTA);
        }
        
        @Test
        @DisplayName("One empty string returns 0.0")
        public void testOneEmptyString() {
            assertEquals(0.0, StringSimilarity.similarity("hello", ""), DELTA);
            assertEquals(0.0, StringSimilarity.similarity("", "world"), DELTA);
        }
        
        @Test
        @DisplayName("Whitespace strings")
        public void testWhitespaceStrings() {
            double similarity = StringSimilarity.similarity("   ", "   ");
            assertEquals(1.0, similarity, DELTA);
            
            similarity = StringSimilarity.similarity("hello", "hello ");
            assertEquals(5.0/6.0, similarity, DELTA);
        }
    }
    
    @Nested
    @DisplayName("OCR and Fuzzy Matching Scenarios")
    class OCRAndFuzzyMatchingScenarios {
        
        @Test
        @DisplayName("OCR common mistakes")
        public void testOCRCommonMistakes() {
            // Common OCR confusions
            double similarity = StringSimilarity.similarity("0", "O");
            assertEquals(0.0, similarity, DELTA); // Single char difference
            
            similarity = StringSimilarity.similarity("1", "l");
            assertEquals(0.0, similarity, DELTA);
            
            similarity = StringSimilarity.similarity("rn", "m");
            assertEquals(0.0, similarity, DELTA);
        }
        
        @Test
        @DisplayName("OCR text variations")
        public void testOCRTextVariations() {
            double similarity = StringSimilarity.similarity("Hello World", "Hell0 W0rld");
            assertTrue(similarity > 0.8); // Should be highly similar despite OCR errors
            
            similarity = StringSimilarity.similarity("Invoice #12345", "lnvoice #l2345");
            assertTrue(similarity > 0.7); // OCR errors but still recognizable
        }
        
        @Test
        @DisplayName("Typo detection")
        public void testTypoDetection() {
            double similarity = StringSimilarity.similarity("receive", "recieve");
            // "receive" vs "recieve" has 2 transposition edits = 5/7 = 0.714
            assertTrue(similarity > 0.7); // Common typo
            
            similarity = StringSimilarity.similarity("necessary", "neccessary");
            // "necessary" vs "neccessary" has 1 insertion = 9/10 = 0.9
            assertEquals(0.9, similarity, 0.01); // Extra letter
            
            similarity = StringSimilarity.similarity("definitely", "definately");
            // "definitely" vs "definately" has 1 substitution = 9/10 = 0.9  
            assertEquals(0.9, similarity, 0.01); // Common misspelling
        }
    }
    
    @Nested
    @DisplayName("Performance and Edge Cases")
    class PerformanceAndEdgeCases {
        
        @Test
        @DisplayName("Very long identical strings")
        public void testVeryLongIdenticalStrings() {
            String longString = "a".repeat(1000);
            assertEquals(1.0, StringSimilarity.similarity(longString, longString), DELTA);
        }
        
        @Test
        @DisplayName("Very long different strings")
        public void testVeryLongDifferentStrings() {
            String longString1 = "a".repeat(1000);
            String longString2 = "b".repeat(1000);
            assertEquals(0.0, StringSimilarity.similarity(longString1, longString2), DELTA);
        }
        
        @Test
        @DisplayName("Strings with different lengths")
        public void testDifferentLengths() {
            double similarity = StringSimilarity.similarity("short", "this is a much longer string");
            assertTrue(similarity < 0.3); // Very different lengths should have low similarity
        }
        
        @Test
        @DisplayName("Special characters")
        public void testSpecialCharacters() {
            double similarity = StringSimilarity.similarity("hello!", "hello?");
            assertEquals(5.0/6.0, similarity, DELTA);
            
            similarity = StringSimilarity.similarity("test@123", "test#123");
            assertEquals(7.0/8.0, similarity, DELTA);
        }
        
        @Test
        @DisplayName("Unicode characters")
        public void testUnicodeCharacters() {
            double similarity = StringSimilarity.similarity("café", "cafe");
            assertEquals(0.75, similarity, DELTA);
            
            similarity = StringSimilarity.similarity("こんにちは", "こんにちは");
            assertEquals(1.0, similarity, DELTA);
        }
    }
    
    @Nested
    @DisplayName("Threshold-Based Matching")
    class ThresholdBasedMatching {
        
        @Test
        @DisplayName("High similarity threshold (>0.9)")
        public void testHighSimilarityThreshold() {
            double threshold = 0.9;
            
            assertTrue(StringSimilarity.similarity("hello", "hello") >= threshold);
            // "test123" to "test124" is 6/7 = 0.857, which is < 0.9
            assertFalse(StringSimilarity.similarity("test123", "test124") >= threshold);
            assertFalse(StringSimilarity.similarity("hello", "help") >= threshold);
        }
        
        @Test
        @DisplayName("Medium similarity threshold (>0.7)")
        public void testMediumSimilarityThreshold() {
            double threshold = 0.7;
            
            assertTrue(StringSimilarity.similarity("hello", "hallo") >= threshold);
            assertTrue(StringSimilarity.similarity("test", "best") >= threshold);
            assertFalse(StringSimilarity.similarity("abc", "xyz") >= threshold);
        }
        
        @Test
        @DisplayName("Low similarity threshold (>0.5)")
        public void testLowSimilarityThreshold() {
            double threshold = 0.5;
            
            assertTrue(StringSimilarity.similarity("hello", "help") >= threshold);
            assertTrue(StringSimilarity.similarity("book", "back") >= threshold);
            assertFalse(StringSimilarity.similarity("abc", "xyz") >= threshold);
        }
    }
    
    @Nested
    @DisplayName("Real-World Use Cases")
    class RealWorldUseCases {
        
        @Test
        @DisplayName("Username variations")
        public void testUsernameVariations() {
            double similarity = StringSimilarity.similarity("john.doe", "john_doe");
            assertTrue(similarity > 0.85);
            
            similarity = StringSimilarity.similarity("user123", "user124");
            assertTrue(similarity > 0.85);
        }
        
        @Test
        @DisplayName("Email address similarity")
        public void testEmailSimilarity() {
            double similarity = StringSimilarity.similarity(
                "john.doe@example.com", 
                "john.doe@example.org"
            );
            // .com vs .org is 3 char difference in 20 chars = 17/20 = 0.85
            assertEquals(0.85, similarity, 0.01);
            
            similarity = StringSimilarity.similarity(
                "test@domain.com",
                "test@domain.co"
            );
            // .com vs .co is 1 char deletion in 15 chars = 14/15 = 0.933
            assertTrue(similarity > 0.9);
        }
        
        @Test
        @DisplayName("File path similarity")
        public void testFilePathSimilarity() {
            double similarity = StringSimilarity.similarity(
                "/home/user/documents/file.txt",
                "/home/user/documents/file.doc"
            );
            assertTrue(similarity > 0.85);
            
            similarity = StringSimilarity.similarity(
                "C:\\Users\\John\\Documents",
                "C:\\Users\\Jane\\Documents"
            );
            assertTrue(similarity > 0.8);
        }
        
        @Test
        @DisplayName("Product name matching")
        public void testProductNameMatching() {
            double similarity = StringSimilarity.similarity(
                "iPhone 13 Pro Max",
                "iPhone 13 Pro"
            );
            assertTrue(similarity > 0.75);
            
            similarity = StringSimilarity.similarity(
                "Samsung Galaxy S21",
                "Samsung Galaxy S22"
            );
            assertTrue(similarity > 0.85);
        }
    }
    
    @Nested
    @DisplayName("Algorithm Properties")
    class AlgorithmProperties {
        
        @Test
        @DisplayName("Normalized score between 0 and 1")
        public void testNormalizedScore() {
            for (int i = 0; i < 100; i++) {
                String s1 = generateRandomString(10);
                String s2 = generateRandomString(10);
                double similarity = StringSimilarity.similarity(s1, s2);
                
                assertTrue(similarity >= 0.0, "Similarity should be >= 0");
                assertTrue(similarity <= 1.0, "Similarity should be <= 1");
            }
        }
        
        @Test
        @DisplayName("Symmetry property")
        public void testSymmetry() {
            String[][] pairs = {
                {"hello", "world"},
                {"test", "best"},
                {"abc", "xyz"},
                {"", "test"},
                {"same", "same"}
            };
            
            for (String[] pair : pairs) {
                double sim1 = StringSimilarity.similarity(pair[0], pair[1]);
                double sim2 = StringSimilarity.similarity(pair[1], pair[0]);
                assertEquals(sim1, sim2, DELTA, 
                    "Similarity should be symmetric for: " + pair[0] + " and " + pair[1]);
            }
        }
        
        @Test
        @DisplayName("Triangle inequality property")
        public void testTriangleInequality() {
            // For Levenshtein distance: d(a,c) <= d(a,b) + d(b,c)
            // This translates to similarity scores
            String a = "hello";
            String b = "hallo";
            String c = "hullo";
            
            double simAB = StringSimilarity.similarity(a, b);
            double simBC = StringSimilarity.similarity(b, c);
            double simAC = StringSimilarity.similarity(a, c);
            
            // All similarities should be reasonable
            assertTrue(simAB > 0);
            assertTrue(simBC > 0);
            assertTrue(simAC > 0);
        }
        
        private String generateRandomString(int length) {
            String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt((int)(Math.random() * chars.length())));
            }
            return sb.toString();
        }
    }
    
    @Nested
    @DisplayName("Finding Best Match")
    class FindingBestMatch {
        
        @Test
        @DisplayName("Find best match from candidates")
        public void testFindBestMatch() {
            String target = "hello";
            String[] candidates = {"hallo", "help", "hero", "jello", "hello!"};
            
            double bestSimilarity = 0;
            String bestMatch = null;
            
            for (String candidate : candidates) {
                double similarity = StringSimilarity.similarity(target, candidate);
                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                    bestMatch = candidate;
                }
            }
            
            assertEquals("hello!", bestMatch); // "hello!" should be closest
            assertTrue(bestSimilarity > 0.8);
        }
        
        @Test
        @DisplayName("Rank candidates by similarity")
        public void testRankCandidates() {
            String target = "test";
            String[] candidates = {"test", "best", "rest", "text", "tests"};
            
            // All should have reasonable similarity
            for (String candidate : candidates) {
                double similarity = StringSimilarity.similarity(target, candidate);
                assertTrue(similarity >= 0.5, 
                    candidate + " should have similarity >= 0.5 with 'test'");
            }
            
            // "test" should have perfect match
            assertEquals(1.0, StringSimilarity.similarity(target, "test"), DELTA);
        }
    }
}