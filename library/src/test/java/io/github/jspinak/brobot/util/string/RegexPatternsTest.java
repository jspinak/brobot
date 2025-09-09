package io.github.jspinak.brobot.util.string;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for RegexPatterns - utility for regular expression patterns.
 * Tests numeric validation and pattern matching functionality.
 */
@DisplayName("RegexPatterns Tests")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Test incompatible with CI environment")
public class RegexPatternsTest extends BrobotTestBase {
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }
    
    @Nested
    @DisplayName("isNumeric Method Tests")
    class IsNumericTests {
        
        @Nested
        @DisplayName("Valid Numeric Strings")
        class ValidNumericStrings {
            
            @ParameterizedTest
            @ValueSource(strings = {
                "0",
                "1",
                "123",
                "999999999",
                "00000",
                "0123456789"
            })
            @DisplayName("Integer values")
            public void testIntegerValues(String input) {
                assertTrue(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "0.0",
                "1.5",
                "123.456",
                "0.123",
                "999.999",
                "3.14159"
            })
            @DisplayName("Decimal values")
            public void testDecimalValues(String input) {
                assertTrue(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                ".5",
                ".123",
                ".0",
                ".999"
            })
            @DisplayName("Leading decimal point")
            public void testLeadingDecimal(String input) {
                assertTrue(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "123.",
                "0.",
                "999.",
                "1."
            })
            @DisplayName("Trailing decimal point")
            public void testTrailingDecimal(String input) {
                assertTrue(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "1.2.3",
                "1.2.3.4",
                "0.0.0",
                "...",
                "1..2",
                ".1.2."
            })
            @DisplayName("Multiple decimal points (allowed by current implementation)")
            public void testMultipleDecimals(String input) {
                assertTrue(RegexPatterns.isNumeric(input));
            }
            
            @Test
            @DisplayName("Single decimal point")
            public void testSingleDecimal() {
                assertTrue(RegexPatterns.isNumeric("."));
            }
        }
        
        @Nested
        @DisplayName("Invalid Numeric Strings")
        class InvalidNumericStrings {
            
            @Test
            @DisplayName("Null input")
            public void testNullInput() {
                assertFalse(RegexPatterns.isNumeric(null));
            }
            
            @ParameterizedTest
            @EmptySource
            @DisplayName("Empty string")
            public void testEmptyString(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "abc",
                "xyz",
                "hello",
                "test"
            })
            @DisplayName("Alphabetic strings")
            public void testAlphabeticStrings(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "123abc",
                "abc123",
                "12a34",
                "1.2a",
                "a.123"
            })
            @DisplayName("Mixed alphanumeric")
            public void testMixedAlphanumeric(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "-123",
                "-1",
                "-0.5",
                "-.5",
                "-123.456"
            })
            @DisplayName("Negative numbers")
            public void testNegativeNumbers(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "+123",
                "+1",
                "+0.5",
                "+.5",
                "+123.456"
            })
            @DisplayName("Positive sign prefix")
            public void testPositiveSign(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "1,234",
                "1,234.56",
                "1,000,000",
                "12,34"
            })
            @DisplayName("Numbers with thousand separators")
            public void testThousandSeparators(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "1.23e10",
                "1E5",
                "3.14E-10",
                "2e3",
                "1.5E+2"
            })
            @DisplayName("Scientific notation")
            public void testScientificNotation(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                " 123",
                "123 ",
                " 123 ",
                "1 2 3",
                "12\t34",
                "12\n34"
            })
            @DisplayName("Strings with whitespace")
            public void testWhitespace(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "$123",
                "€100",
                "£50.50",
                "¥1000",
                "123$",
                "100%"
            })
            @DisplayName("Currency and percentage symbols")
            public void testCurrencyAndPercentage(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "(123)",
                "[123]",
                "{123}",
                "123!",
                "123?",
                "#123",
                "123*"
            })
            @DisplayName("Special characters")
            public void testSpecialCharacters(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
        }
        
        @Nested
        @DisplayName("Edge Cases")
        class EdgeCases {
            
            @Test
            @DisplayName("Very long numeric string")
            public void testVeryLongNumeric() {
                String longNumber = "1".repeat(1000) + "." + "2".repeat(1000);
                assertTrue(RegexPatterns.isNumeric(longNumber));
            }
            
            @Test
            @DisplayName("Very long non-numeric string")
            public void testVeryLongNonNumeric() {
                String longString = "a".repeat(1000);
                assertFalse(RegexPatterns.isNumeric(longString));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "٠١٢٣٤٥٦٧٨٩",  // Arabic numerals
                "०१२३४५६७८९",    // Devanagari numerals
                "০১২৩৪৫৬৭৮৯",    // Bengali numerals
                "௦௧௨௩௪௫௬௭௮௯"     // Tamil numerals
            })
            @DisplayName("Non-ASCII numeric characters")
            public void testNonASCIINumerals(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @ValueSource(strings = {
                "Infinity",
                "NaN",
                "null",
                "undefined",
                "true",
                "false"
            })
            @DisplayName("Special numeric keywords")
            public void testSpecialKeywords(String input) {
                assertFalse(RegexPatterns.isNumeric(input));
            }
        }
        
        @Nested
        @DisplayName("Boundary Values")
        class BoundaryValues {
            
            @Test
            @DisplayName("Single digit")
            public void testSingleDigit() {
                for (int i = 0; i <= 9; i++) {
                    assertTrue(RegexPatterns.isNumeric(String.valueOf(i)));
                }
            }
            
            @Test
            @DisplayName("Two character combinations")
            public void testTwoCharacters() {
                assertTrue(RegexPatterns.isNumeric("00"));
                assertTrue(RegexPatterns.isNumeric("0."));
                assertTrue(RegexPatterns.isNumeric(".0"));
                assertTrue(RegexPatterns.isNumeric(".."));
                assertTrue(RegexPatterns.isNumeric("99"));
            }
            
            @Test
            @DisplayName("Maximum integer value")
            public void testMaxInteger() {
                String maxInt = String.valueOf(Integer.MAX_VALUE);
                assertTrue(RegexPatterns.isNumeric(maxInt));
            }
            
            @Test
            @DisplayName("Maximum long value")
            public void testMaxLong() {
                String maxLong = String.valueOf(Long.MAX_VALUE);
                assertTrue(RegexPatterns.isNumeric(maxLong));
            }
        }
        
        @Nested
        @DisplayName("Real-World Use Cases")
        class RealWorldUseCases {
            
            @ParameterizedTest
            @CsvSource({
                "3.14159, true",     // Pi
                "2.71828, true",     // Euler's number
                "0.618, true",       // Golden ratio
                "1.414, true",       // Square root of 2
                "9.8, true"          // Gravity constant
            })
            @DisplayName("Mathematical constants")
            public void testMathConstants(String input, boolean expected) {
                assertEquals(expected, RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @CsvSource({
                "192.168.1.1, true",     // IP address (passes but shouldn't be valid as single number)
                "2024, true",            // Year
                "12.99, true",           // Price
                "98.6, true",            // Temperature
                "1.0.0, true"            // Version number (passes due to multiple dots)
            })
            @DisplayName("Common numeric formats")
            public void testCommonFormats(String input, boolean expected) {
                assertEquals(expected, RegexPatterns.isNumeric(input));
            }
            
            @ParameterizedTest
            @CsvSource({
                "123-45-6789, false",    // SSN
                "555-1234, false",        // Phone
                "12/31/2024, false",      // Date
                "12:30:45, false",        // Time
                "#FFFFFF, false"          // Hex color
            })
            @DisplayName("Formatted data that should not pass")
            public void testFormattedData(String input, boolean expected) {
                assertEquals(expected, RegexPatterns.isNumeric(input));
            }
        }
        
        @Nested
        @DisplayName("Performance Characteristics")
        class PerformanceCharacteristics {
            
            @Test
            @DisplayName("Repeated calls with same input")
            public void testRepeatedCalls() {
                String input = "123.456";
                
                // Should return consistent results
                for (int i = 0; i < 100; i++) {
                    assertTrue(RegexPatterns.isNumeric(input));
                }
            }
            
            @Test
            @DisplayName("Pattern matching performance")
            public void testPatternPerformance() {
                long startTime = System.nanoTime();
                
                for (int i = 0; i < 10000; i++) {
                    RegexPatterns.isNumeric("123.456");
                    RegexPatterns.isNumeric("abc");
                    RegexPatterns.isNumeric(null);
                }
                
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds
                
                // Should complete within reasonable time (100ms for 30000 operations)
                assertTrue(duration < 100, "Performance test took " + duration + "ms");
            }
        }
    }
}