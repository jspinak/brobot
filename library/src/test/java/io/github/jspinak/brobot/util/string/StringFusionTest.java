package io.github.jspinak.brobot.util.string;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EmptySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for StringFusion - string combination utility.
 * Tests fusion algorithm, common prefix detection, and edge cases.
 */
@DisplayName("StringFusion Tests")
public class StringFusionTest extends BrobotTestBase {
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }
    
    @Nested
    @DisplayName("Basic Fusion Operations")
    class BasicFusionOperations {
        
        @Test
        @DisplayName("Fuse strings with common prefix")
        public void testFuseWithCommonPrefix() {
            String result = StringFusion.fuse("hello", "helloWorld");
            assertEquals("hello-World", result);
        }
        
        @Test
        @DisplayName("Fuse strings with no common prefix")
        public void testFuseNoCommonPrefix() {
            String result = StringFusion.fuse("abc", "xyz");
            assertEquals("abc-xyz", result);
        }
        
        @Test
        @DisplayName("Fuse identical strings")
        public void testFuseIdenticalStrings() {
            String result = StringFusion.fuse("same", "same");
            assertEquals("same-", result);
        }
        
        @Test
        @DisplayName("First string is prefix of second")
        public void testFirstIsPrefixOfSecond() {
            String result = StringFusion.fuse("test", "testing");
            assertEquals("test-ing", result);
        }
        
        @Test
        @DisplayName("Second string is prefix of first")
        public void testSecondIsPrefixOfFirst() {
            String result = StringFusion.fuse("testing", "test");
            assertEquals("testing-", result);
        }
    }
    
    @Nested
    @DisplayName("Parameterized Fusion Tests")
    class ParameterizedFusionTests {
        
        @ParameterizedTest
        @CsvSource({
            "user_name, user_email, user_name-email",
            "com.example, com.example.app, com.example-app",
            "file, filename, file-name",
            "app, application, app-lication",
            "main, maintenance, main-tenance",
            "doc, document, doc-ument",
            "pre, prefix, pre-fix",
            "post, postfix, post-fix"
        })
        @DisplayName("Common fusion scenarios")
        public void testCommonScenarios(String a, String b, String expected) {
            String result = StringFusion.fuse(a, b);
            assertEquals(expected, result);
        }
        
        @ParameterizedTest
        @CsvSource({
            "a, abc, a-bc",
            "ab, abc, ab-c",
            "abc, abc, abc-",
            "abcd, abc, abcd-",
            "abc, abcd, abc-d"
        })
        @DisplayName("Progressive prefix lengths")
        public void testProgressivePrefixes(String a, String b, String expected) {
            String result = StringFusion.fuse(a, b);
            assertEquals(expected, result);
        }
        
        @ParameterizedTest
        @CsvSource({
            "test, test123, test-123",
            "test1, test123, test1-23",
            "test12, test123, test12-3",
            "test123, test123, test123-"
        })
        @DisplayName("Numeric suffix handling")
        public void testNumericSuffixes(String a, String b, String expected) {
            String result = StringFusion.fuse(a, b);
            assertEquals(expected, result);
        }
    }
    
    @Nested
    @DisplayName("Empty String Cases")
    class EmptyStringCases {
        
        @Test
        @DisplayName("First string empty")
        public void testFirstStringEmpty() {
            String result = StringFusion.fuse("", "test");
            assertEquals("-test", result);
        }
        
        @Test
        @DisplayName("Second string empty")
        public void testSecondStringEmpty() {
            String result = StringFusion.fuse("test", "");
            assertEquals("test-", result);
        }
        
        @Test
        @DisplayName("Both strings empty")
        public void testBothStringsEmpty() {
            String result = StringFusion.fuse("", "");
            assertEquals("-", result);
        }
    }
    
    @Nested
    @DisplayName("Special Characters")
    class SpecialCharacters {
        
        @Test
        @DisplayName("Strings with spaces")
        public void testStringsWithSpaces() {
            String result = StringFusion.fuse("hello world", "hello universe");
            assertEquals("hello world-universe", result);
        }
        
        @Test
        @DisplayName("Strings with underscores")
        public void testStringsWithUnderscores() {
            String result = StringFusion.fuse("test_case", "test_case_one");
            assertEquals("test_case-_one", result);
        }
        
        @Test
        @DisplayName("Strings with dots")
        public void testStringsWithDots() {
            String result = StringFusion.fuse("com.example", "com.example.test");
            assertEquals("com.example-.test", result);
        }
        
        @Test
        @DisplayName("Strings with hyphens")
        public void testStringsWithHyphens() {
            String result = StringFusion.fuse("my-file", "my-file-backup");
            assertEquals("my-file--backup", result);
        }
        
        @Test
        @DisplayName("Unicode characters")
        public void testUnicodeCharacters() {
            String result = StringFusion.fuse("こんにちは", "こんにちは世界");
            assertEquals("こんにちは-世界", result);
        }
        
        @Test
        @DisplayName("Mixed special characters")
        public void testMixedSpecialCharacters() {
            String result = StringFusion.fuse("test@123", "test@456");
            assertEquals("test@123-456", result);
        }
    }
    
    @Nested
    @DisplayName("Case Sensitivity")
    class CaseSensitivity {
        
        @Test
        @DisplayName("Case sensitive comparison")
        public void testCaseSensitive() {
            String result = StringFusion.fuse("Test", "test");
            assertEquals("Test-test", result); // No common prefix due to case difference
        }
        
        @Test
        @DisplayName("Mixed case with common prefix")
        public void testMixedCase() {
            String result = StringFusion.fuse("TestCase", "TestCaseOne");
            assertEquals("TestCase-One", result);
        }
        
        @Test
        @DisplayName("All uppercase")
        public void testAllUppercase() {
            String result = StringFusion.fuse("HELLO", "HELLOWORLD");
            assertEquals("HELLO-WORLD", result);
        }
        
        @Test
        @DisplayName("All lowercase")
        public void testAllLowercase() {
            String result = StringFusion.fuse("hello", "helloworld");
            assertEquals("hello-world", result);
        }
    }
    
    @Nested
    @DisplayName("Real-World Use Cases")
    class RealWorldUseCases {
        
        @Test
        @DisplayName("File path fusion")
        public void testFilePathFusion() {
            String result = StringFusion.fuse("/home/user/documents", "/home/user/downloads");
            assertEquals("/home/user/documents-downloads", result);
        }
        
        @Test
        @DisplayName("Package name fusion")
        public void testPackageNameFusion() {
            String result = StringFusion.fuse("io.github.jspinak", "io.github.jspinak.brobot");
            assertEquals("io.github.jspinak-.brobot", result);
        }
        
        @Test
        @DisplayName("URL fusion")
        public void testUrlFusion() {
            String result = StringFusion.fuse("https://example.com", "https://example.com/api");
            assertEquals("https://example.com-/api", result);
        }
        
        @Test
        @DisplayName("Method name fusion")
        public void testMethodNameFusion() {
            String result = StringFusion.fuse("getValue", "getValueOrDefault");
            assertEquals("getValue-OrDefault", result);
        }
        
        @Test
        @DisplayName("Database table fusion")
        public void testDatabaseTableFusion() {
            String result = StringFusion.fuse("user_profile", "user_profile_settings");
            assertEquals("user_profile-_settings", result);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Single character strings")
        public void testSingleCharacters() {
            String result = StringFusion.fuse("a", "a");
            assertEquals("a-", result);
            
            result = StringFusion.fuse("a", "b");
            assertEquals("a-b", result);
        }
        
        @Test
        @DisplayName("Very long common prefix")
        public void testVeryLongCommonPrefix() {
            String longPrefix = "a".repeat(1000);
            String a = longPrefix + "x";
            String b = longPrefix + "y";
            
            String result = StringFusion.fuse(a, b);
            assertEquals(a + "-y", result);
        }
        
        @Test
        @DisplayName("Very long strings")
        public void testVeryLongStrings() {
            String a = "x".repeat(10000);
            String b = "y".repeat(10000);
            
            String result = StringFusion.fuse(a, b);
            assertEquals(a + "-" + b, result); // No common prefix
        }
        
        @Test
        @DisplayName("Strings with only whitespace")
        public void testWhitespaceStrings() {
            String result = StringFusion.fuse("   ", "   ");
            assertEquals("   -", result);
            
            result = StringFusion.fuse("  ", "   ");
            assertEquals("  - ", result);
        }
        
        @Test
        @DisplayName("Null handling")
        public void testNullHandling() {
            // Method doesn't handle nulls - should throw NPE
            assertThrows(NullPointerException.class, () -> {
                StringFusion.fuse(null, "test");
            });
            
            assertThrows(NullPointerException.class, () -> {
                StringFusion.fuse("test", null);
            });
            
            assertThrows(NullPointerException.class, () -> {
                StringFusion.fuse(null, null);
            });
        }
    }
    
    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {
        
        @Test
        @DisplayName("Consistent results for same input")
        public void testConsistentResults() {
            String a = "consistent";
            String b = "consistency";
            
            String result1 = StringFusion.fuse(a, b);
            String result2 = StringFusion.fuse(a, b);
            String result3 = StringFusion.fuse(a, b);
            
            assertEquals(result1, result2);
            assertEquals(result2, result3);
            assertEquals("consistent-cy", result1);
        }
        
        @Test
        @DisplayName("Order matters")
        public void testOrderMatters() {
            String result1 = StringFusion.fuse("abc", "abcd");
            String result2 = StringFusion.fuse("abcd", "abc");
            
            assertNotEquals(result1, result2);
            assertEquals("abc-d", result1);
            assertEquals("abcd-", result2);
        }
        
        @Test
        @DisplayName("Performance with increasing prefix length")
        public void testIncreasingPrefixPerformance() {
            for (int prefixLen = 0; prefixLen <= 100; prefixLen++) {
                String prefix = "x".repeat(prefixLen);
                String a = prefix + "a";
                String b = prefix + "b";
                
                String result = StringFusion.fuse(a, b);
                assertEquals(a + "-b", result);
            }
        }
    }
    
    @Nested
    @DisplayName("Pattern Recognition")
    class PatternRecognition {
        
        @ParameterizedTest
        @CsvSource({
            "Button, ButtonClick, Button-Click",
            "Label, LabelText, Label-Text",
            "Input, InputField, Input-Field",
            "Check, CheckBox, Check-Box",
            "Radio, RadioButton, Radio-Button"
        })
        @DisplayName("UI element patterns")
        public void testUIElementPatterns(String base, String extended, String expected) {
            String result = StringFusion.fuse(base, extended);
            assertEquals(expected, result);
        }
        
        @ParameterizedTest
        @CsvSource({
            "get, getValue, get-Value",
            "set, setValue, set-Value",
            "is, isEnabled, is-Enabled",
            "has, hasChildren, has-Children",
            "add, addElement, add-Element"
        })
        @DisplayName("Method prefix patterns")
        public void testMethodPrefixPatterns(String prefix, String method, String expected) {
            String result = StringFusion.fuse(prefix, method);
            assertEquals(expected, result);
        }
        
        @ParameterizedTest
        @CsvSource({
            "State, StateImage, State-Image",
            "State, StateLocation, State-Location",
            "State, StateRegion, State-Region",
            "Action, ActionResult, Action-Result",
            "Action, ActionConfig, Action-Config"
        })
        @DisplayName("Class hierarchy patterns")
        public void testClassHierarchyPatterns(String base, String derived, String expected) {
            String result = StringFusion.fuse(base, derived);
            assertEquals(expected, result);
        }
    }
}