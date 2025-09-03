package io.github.jspinak.brobot.runner.json.validation.model;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ValidationError record
 * Testing immutability, equality, and toString formatting
 */
@DisplayName("ValidationError Tests")
class ValidationErrorTest extends BrobotTestBase {

    @Nested
    @DisplayName("Record Creation Tests")
    class RecordCreationTests {

        @Test
        @DisplayName("Should create error with all components")
        void shouldCreateErrorWithAllComponents() {
            // Given
            String errorCode = "TEST001";
            String message = "Test error message";
            ValidationSeverity severity = ValidationSeverity.ERROR;
            
            // When
            ValidationError error = new ValidationError(errorCode, message, severity);
            
            // Then
            assertEquals(errorCode, error.errorCode());
            assertEquals(message, error.message());
            assertEquals(severity, error.severity());
        }

        @ParameterizedTest
        @EnumSource(ValidationSeverity.class)
        @DisplayName("Should support all severity levels")
        void shouldSupportAllSeverityLevels(ValidationSeverity severity) {
            // When
            ValidationError error = new ValidationError("CODE", "Message", severity);
            
            // Then
            assertEquals(severity, error.severity());
        }

        @Test
        @DisplayName("Should handle null components")
        void shouldHandleNullComponents() {
            // When
            ValidationError error = new ValidationError(null, null, null);
            
            // Then
            assertNull(error.errorCode());
            assertNull(error.message());
            assertNull(error.severity());
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // When
            ValidationError error = new ValidationError("", "", ValidationSeverity.WARNING);
            
            // Then
            assertEquals("", error.errorCode());
            assertEquals("", error.message());
            assertEquals(ValidationSeverity.WARNING, error.severity());
        }

        @ParameterizedTest
        @CsvSource({
            "Missing image resource, Image login_button.png not found in images directory, ERROR",
            "Duplicate function name, Function 'processData' defined multiple times, WARNING",
            "Unreachable state, State 'HiddenMenu' has no incoming transitions, WARNING",
            "Invalid state reference, Transition #5 references non-existent state ID 99, CRITICAL",
            "Schema violation, JSON does not conform to schema at line 42, ERROR"
        })
        @DisplayName("Should handle various error code conventions")
        void shouldHandleVariousErrorCodeConventions(String code, String msg, ValidationSeverity sev) {
            // When
            ValidationError error = new ValidationError(code, msg, sev);
            
            // Then
            assertEquals(code, error.errorCode());
            assertEquals(msg, error.message());
            assertEquals(sev, error.severity());
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal for identical values")
        void shouldBeEqualForIdenticalValues() {
            // Given
            ValidationError error1 = new ValidationError("CODE", "Message", ValidationSeverity.ERROR);
            ValidationError error2 = new ValidationError("CODE", "Message", ValidationSeverity.ERROR);
            
            // Then
            assertEquals(error1, error2);
            assertEquals(error1.hashCode(), error2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different error codes")
        void shouldNotBeEqualForDifferentErrorCodes() {
            // Given
            ValidationError error1 = new ValidationError("CODE1", "Message", ValidationSeverity.ERROR);
            ValidationError error2 = new ValidationError("CODE2", "Message", ValidationSeverity.ERROR);
            
            // Then
            assertNotEquals(error1, error2);
        }

        @Test
        @DisplayName("Should not be equal for different messages")
        void shouldNotBeEqualForDifferentMessages() {
            // Given
            ValidationError error1 = new ValidationError("CODE", "Message1", ValidationSeverity.ERROR);
            ValidationError error2 = new ValidationError("CODE", "Message2", ValidationSeverity.ERROR);
            
            // Then
            assertNotEquals(error1, error2);
        }

        @Test
        @DisplayName("Should not be equal for different severities")
        void shouldNotBeEqualForDifferentSeverities() {
            // Given
            ValidationError error1 = new ValidationError("CODE", "Message", ValidationSeverity.ERROR);
            ValidationError error2 = new ValidationError("CODE", "Message", ValidationSeverity.WARNING);
            
            // Then
            assertNotEquals(error1, error2);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            ValidationError error = new ValidationError("CODE", "Message", ValidationSeverity.ERROR);
            
            // Then
            assertEquals(error, error);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            ValidationError error = new ValidationError("CODE", "Message", ValidationSeverity.ERROR);
            
            // Then
            assertNotEquals(null, error);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            ValidationError error = new ValidationError("CODE", "Message", ValidationSeverity.ERROR);
            String notAnError = "Not an error";
            
            // Then
            assertNotEquals(error, notAnError);
        }

        @Test
        @DisplayName("Should handle null fields in equality")
        void shouldHandleNullFieldsInEquality() {
            // Given
            ValidationError error1 = new ValidationError(null, null, null);
            ValidationError error2 = new ValidationError(null, null, null);
            ValidationError error3 = new ValidationError("CODE", null, null);
            
            // Then
            assertEquals(error1, error2);
            assertNotEquals(error1, error3);
        }
    }

    @Nested
    @DisplayName("ToString Formatting Tests")
    class ToStringFormattingTests {

        @Test
        @DisplayName("Should format as [SEVERITY] code: message")
        void shouldFormatCorrectly() {
            // Given
            ValidationError error = new ValidationError(
                "Invalid reference",
                "State ID 42 not found",
                ValidationSeverity.ERROR
            );
            
            // When
            String result = error.toString();
            
            // Then
            assertEquals("[ERROR] Invalid reference: State ID 42 not found", result);
        }

        @ParameterizedTest
        @EnumSource(ValidationSeverity.class)
        @DisplayName("Should include severity in toString")
        void shouldIncludeSeverityInToString(ValidationSeverity severity) {
            // Given
            ValidationError error = new ValidationError("CODE", "Message", severity);
            
            // When
            String result = error.toString();
            
            // Then
            assertTrue(result.contains("[" + severity + "]"));
            assertTrue(result.contains("CODE"));
            assertTrue(result.contains("Message"));
        }

        @Test
        @DisplayName("Should handle null severity in toString")
        void shouldHandleNullSeverityInToString() {
            // Given
            ValidationError error = new ValidationError("CODE", "Message", null);
            
            // When
            String result = error.toString();
            
            // Then
            assertTrue(result.contains("[null]"));
            assertTrue(result.contains("CODE"));
            assertTrue(result.contains("Message"));
        }

        @Test
        @DisplayName("Should handle null fields in toString")
        void shouldHandleNullFieldsInToString() {
            // Given
            ValidationError error = new ValidationError(null, null, ValidationSeverity.WARNING);
            
            // When
            String result = error.toString();
            
            // Then
            assertEquals("[WARNING] null: null", result);
        }

        @Test
        @DisplayName("Should handle empty strings in toString")
        void shouldHandleEmptyStringsInToString() {
            // Given
            ValidationError error = new ValidationError("", "", ValidationSeverity.INFO);
            
            // When
            String result = error.toString();
            
            // Then
            assertEquals("[INFO] : ", result);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable record")
        void shouldBeImmutableRecord() {
            // Given
            ValidationError error = new ValidationError("CODE", "Message", ValidationSeverity.ERROR);
            
            // Then - Record components are final and cannot be modified
            assertEquals("CODE", error.errorCode());
            assertEquals("Message", error.message());
            assertEquals(ValidationSeverity.ERROR, error.severity());
            
            // Record fields are implicitly final - no setters exist
        }

        @Test
        @DisplayName("Should create defensive copies if needed")
        void shouldCreateDefensiveCopiesIfNeeded() {
            // Given
            String code = "MUTABLE";
            String message = "Test";
            ValidationSeverity severity = ValidationSeverity.ERROR;
            
            // When
            ValidationError error1 = new ValidationError(code, message, severity);
            ValidationError error2 = new ValidationError(code, message, severity);
            
            // Then - Same values but different instances
            assertEquals(error1, error2);
            assertNotSame(error1, error2);
        }
    }

    @Nested
    @DisplayName("Use Case Tests")
    class UseCaseTests {

        @Test
        @DisplayName("Should work in collections")
        void shouldWorkInCollections() {
            // Given
            ValidationError error1 = new ValidationError("E1", "Error 1", ValidationSeverity.ERROR);
            ValidationError error2 = new ValidationError("E2", "Error 2", ValidationSeverity.WARNING);
            ValidationError duplicate = new ValidationError("E1", "Error 1", ValidationSeverity.ERROR);
            
            // When
            var set = new java.util.HashSet<ValidationError>();
            set.add(error1);
            set.add(error2);
            set.add(duplicate); // Should not add duplicate
            
            // Then
            assertEquals(2, set.size());
            assertTrue(set.contains(error1));
            assertTrue(set.contains(error2));
            assertTrue(set.contains(duplicate)); // Equal to error1
        }

        @Test
        @DisplayName("Should support pattern matching")
        void shouldSupportPatternMatching() {
            // Given
            ValidationError error = new ValidationError("TEST", "Message", ValidationSeverity.CRITICAL);
            
            // When/Then - Pattern matching with records
            String result = switch (error.severity()) {
                case CRITICAL -> "Critical: " + error.message();
                case ERROR -> "Error: " + error.message();
                case WARNING -> "Warning: " + error.message();
                case INFO -> "Info: " + error.message();
                case null -> "Unknown severity";
            };
            
            assertEquals("Critical: Message", result);
        }

        @Test
        @DisplayName("Should work with stream operations")
        void shouldWorkWithStreamOperations() {
            // Given
            var errors = java.util.List.of(
                new ValidationError("E1", "Critical", ValidationSeverity.CRITICAL),
                new ValidationError("E2", "Error", ValidationSeverity.ERROR),
                new ValidationError("E3", "Warning", ValidationSeverity.WARNING),
                new ValidationError("E4", "Info", ValidationSeverity.INFO)
            );
            
            // When
            var criticalCount = errors.stream()
                .filter(e -> e.severity() == ValidationSeverity.CRITICAL)
                .count();
            
            var errorCodes = errors.stream()
                .map(ValidationError::errorCode)
                .toList();
            
            // Then
            assertEquals(1, criticalCount);
            assertEquals(java.util.List.of("E1", "E2", "E3", "E4"), errorCodes);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "",
            " ",
            "\n",
            "\t",
            "Very long error code that exceeds typical length expectations and contains many words",
            "Code with special chars !@#$%^&*()",
            "Code\nwith\nnewlines",
            "Code\twith\ttabs"
        })
        @DisplayName("Should handle various string formats")
        void shouldHandleVariousStringFormats(String text) {
            // When
            ValidationError error = new ValidationError(text, text, ValidationSeverity.ERROR);
            
            // Then
            assertEquals(text, error.errorCode());
            assertEquals(text, error.message());
            assertNotNull(error.toString());
        }

        @Test
        @DisplayName("Should handle very long messages")
        void shouldHandleVeryLongMessages() {
            // Given
            String longMessage = "A".repeat(10000);
            
            // When
            ValidationError error = new ValidationError("CODE", longMessage, ValidationSeverity.ERROR);
            
            // Then
            assertEquals(longMessage, error.message());
            assertTrue(error.toString().length() > 10000);
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            // Given
            String unicode = "Error: 日本語 🚀 éñ";
            
            // When
            ValidationError error = new ValidationError(unicode, unicode, ValidationSeverity.WARNING);
            
            // Then
            assertEquals(unicode, error.errorCode());
            assertEquals(unicode, error.message());
            assertTrue(error.toString().contains(unicode));
        }
    }
}