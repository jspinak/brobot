package io.github.jspinak.brobot.runner.json.validation.model;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ValidationResult
 * Testing error aggregation, severity filtering, and result merging
 */
@DisplayName("ValidationResult Tests")
class ValidationResultTest extends BrobotTestBase {

    private ValidationResult result;
    
    @BeforeEach
    void setUp() {
        result = new ValidationResult();
    }

    @Nested
    @DisplayName("Adding Errors Tests")
    class AddingErrorsTests {

        @Test
        @DisplayName("Should add single error with object")
        void shouldAddSingleError() {
            // Given
            ValidationError error = new ValidationError("TEST001", "Test error message", ValidationSeverity.ERROR);
            
            // When
            result.addError(error);
            
            // Then
            assertEquals(1, result.getErrors().size());
            assertEquals(error, result.getErrors().get(0));
            assertTrue(result.hasErrors());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should add error using components")
        void shouldAddErrorUsingComponents() {
            // When
            result.addError("TEST002", "Component error", ValidationSeverity.WARNING);
            
            // Then
            assertEquals(1, result.getErrors().size());
            ValidationError addedError = result.getErrors().get(0);
            assertEquals("TEST002", addedError.errorCode());
            assertEquals("Component error", addedError.message());
            assertEquals(ValidationSeverity.WARNING, addedError.severity());
        }

        @Test
        @DisplayName("Should handle null error gracefully")
        void shouldHandleNullError() {
            // When
            result.addError(null);
            
            // Then
            assertEquals(0, result.getErrors().size());
            assertFalse(result.hasErrors());
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should maintain error order")
        void shouldMaintainErrorOrder() {
            // Given
            ValidationError error1 = new ValidationError("ERR1", "First", ValidationSeverity.ERROR);
            ValidationError error2 = new ValidationError("ERR2", "Second", ValidationSeverity.WARNING);
            ValidationError error3 = new ValidationError("ERR3", "Third", ValidationSeverity.CRITICAL);
            
            // When
            result.addError(error1);
            result.addError(error2);
            result.addError(error3);
            
            // Then
            List<ValidationError> errors = result.getErrors();
            assertEquals(3, errors.size());
            assertEquals("First", errors.get(0).message());
            assertEquals("Second", errors.get(1).message());
            assertEquals("Third", errors.get(2).message());
        }

        @Test
        @DisplayName("Should return unmodifiable error list")
        void shouldReturnUnmodifiableErrorList() {
            // Given
            result.addError("TEST", "Error", ValidationSeverity.ERROR);
            
            // When/Then
            List<ValidationError> errors = result.getErrors();
            assertThrows(UnsupportedOperationException.class, () -> 
                errors.add(new ValidationError("NEW", "New error", ValidationSeverity.ERROR))
            );
        }
    }

    @Nested
    @DisplayName("Merging Results Tests")
    class MergingResultsTests {

        @Test
        @DisplayName("Should merge two validation results")
        void shouldMergeTwoResults() {
            // Given
            result.addError("ERR1", "Error 1", ValidationSeverity.ERROR);
            
            ValidationResult other = new ValidationResult();
            other.addError("ERR2", "Error 2", ValidationSeverity.WARNING);
            other.addError("ERR3", "Error 3", ValidationSeverity.CRITICAL);
            
            // When
            result.merge(other);
            
            // Then
            assertEquals(3, result.getErrors().size());
            assertTrue(result.hasCriticalErrors());
            assertTrue(result.hasWarnings());
        }

        @Test
        @DisplayName("Should handle merging null result")
        void shouldHandleMergingNullResult() {
            // Given
            result.addError("ERR1", "Error 1", ValidationSeverity.ERROR);
            
            // When
            result.merge(null);
            
            // Then
            assertEquals(1, result.getErrors().size());
        }

        @Test
        @DisplayName("Should handle merging empty result")
        void shouldHandleMergingEmptyResult() {
            // Given
            result.addError("ERR1", "Error 1", ValidationSeverity.ERROR);
            ValidationResult empty = new ValidationResult();
            
            // When
            result.merge(empty);
            
            // Then
            assertEquals(1, result.getErrors().size());
        }

        @Test
        @DisplayName("Should preserve order when merging")
        void shouldPreserveOrderWhenMerging() {
            // Given
            result.addError("A1", "First set - 1", ValidationSeverity.ERROR);
            result.addError("A2", "First set - 2", ValidationSeverity.WARNING);
            
            ValidationResult other = new ValidationResult();
            other.addError("B1", "Second set - 1", ValidationSeverity.CRITICAL);
            other.addError("B2", "Second set - 2", ValidationSeverity.INFO);
            
            // When
            result.merge(other);
            
            // Then
            List<ValidationError> errors = result.getErrors();
            assertEquals("First set - 1", errors.get(0).message());
            assertEquals("First set - 2", errors.get(1).message());
            assertEquals("Second set - 1", errors.get(2).message());
            assertEquals("Second set - 2", errors.get(3).message());
        }
    }

    @Nested
    @DisplayName("Severity Filtering Tests")
    class SeverityFilteringTests {

        @BeforeEach
        void setupMixedSeverities() {
            result.addError("CRIT1", "Critical 1", ValidationSeverity.CRITICAL);
            result.addError("CRIT2", "Critical 2", ValidationSeverity.CRITICAL);
            result.addError("ERR1", "Error 1", ValidationSeverity.ERROR);
            result.addError("ERR2", "Error 2", ValidationSeverity.ERROR);
            result.addError("ERR3", "Error 3", ValidationSeverity.ERROR);
            result.addError("WARN1", "Warning 1", ValidationSeverity.WARNING);
            result.addError("WARN2", "Warning 2", ValidationSeverity.WARNING);
            result.addError("INFO1", "Info 1", ValidationSeverity.INFO);
        }

        @Test
        @DisplayName("Should get warnings only")
        void shouldGetWarningsOnly() {
            // When
            List<ValidationError> warnings = result.getWarnings();
            
            // Then
            assertEquals(2, warnings.size());
            assertTrue(warnings.stream().allMatch(e -> e.severity() == ValidationSeverity.WARNING));
        }

        @Test
        @DisplayName("Should get critical errors only")
        void shouldGetCriticalErrorsOnly() {
            // When
            List<ValidationError> critical = result.getCriticalErrors();
            
            // Then
            assertEquals(2, critical.size());
            assertTrue(critical.stream().allMatch(e -> e.severity() == ValidationSeverity.CRITICAL));
        }

        @Test
        @DisplayName("Should get errors and critical combined")
        void shouldGetErrorsAndCritical() {
            // When
            List<ValidationError> severe = result.getErrorsAndCritical();
            
            // Then
            assertEquals(5, severe.size()); // 2 critical + 3 errors
            assertTrue(severe.stream().allMatch(e -> 
                e.severity() == ValidationSeverity.ERROR || 
                e.severity() == ValidationSeverity.CRITICAL
            ));
        }

        @Test
        @DisplayName("Should get info messages only")
        void shouldGetInfoMessagesOnly() {
            // When
            List<ValidationError> info = result.getInfoMessages();
            
            // Then
            assertEquals(1, info.size());
            assertEquals(ValidationSeverity.INFO, info.get(0).severity());
        }

        @ParameterizedTest
        @EnumSource(ValidationSeverity.class)
        @DisplayName("Should filter by specific severity")
        void shouldFilterBySpecificSeverity(ValidationSeverity severity) {
            // When
            List<ValidationError> filtered = result.getErrorsBySeverity(severity);
            
            // Then
            assertTrue(filtered.stream().allMatch(e -> e.severity() == severity));
        }
    }

    @Nested
    @DisplayName("Validation State Tests")
    class ValidationStateTests {

        @Test
        @DisplayName("Should be valid when no errors")
        void shouldBeValidWhenNoErrors() {
            assertTrue(result.isValid());
            assertFalse(result.hasErrors());
            assertFalse(result.hasSevereErrors());
            assertFalse(result.hasCriticalErrors());
            assertFalse(result.hasWarnings());
        }

        @Test
        @DisplayName("Should be valid with only warnings")
        void shouldBeValidWithOnlyWarnings() {
            // Given
            result.addError("W1", "Warning", ValidationSeverity.WARNING);
            result.addError("I1", "Info", ValidationSeverity.INFO);
            
            // Then
            assertTrue(result.isValid());
            assertTrue(result.hasErrors());
            assertTrue(result.hasWarnings());
            assertFalse(result.hasSevereErrors());
            assertFalse(result.hasCriticalErrors());
        }

        @Test
        @DisplayName("Should be invalid with errors")
        void shouldBeInvalidWithErrors() {
            // Given
            result.addError("E1", "Error", ValidationSeverity.ERROR);
            
            // Then
            assertFalse(result.isValid());
            assertTrue(result.hasErrors());
            assertTrue(result.hasSevereErrors());
            assertFalse(result.hasCriticalErrors());
        }

        @Test
        @DisplayName("Should be invalid with critical errors")
        void shouldBeInvalidWithCriticalErrors() {
            // Given
            result.addError("C1", "Critical", ValidationSeverity.CRITICAL);
            
            // Then
            assertFalse(result.isValid());
            assertTrue(result.hasErrors());
            assertTrue(result.hasSevereErrors());
            assertTrue(result.hasCriticalErrors());
        }

        @ParameterizedTest
        @EnumSource(ValidationSeverity.class)
        @DisplayName("Should detect errors of specific severity")
        void shouldDetectErrorsOfSeverity(ValidationSeverity severity) {
            // Given
            result.addError("TEST", "Test error", severity);
            
            // Then
            assertTrue(result.hasErrorsOfSeverity(severity));
        }

        @Test
        @DisplayName("Should handle mixed severities correctly")
        void shouldHandleMixedSeverities() {
            // Given
            result.addError("C1", "Critical", ValidationSeverity.CRITICAL);
            result.addError("E1", "Error", ValidationSeverity.ERROR);
            result.addError("W1", "Warning", ValidationSeverity.WARNING);
            result.addError("I1", "Info", ValidationSeverity.INFO);
            
            // Then
            assertFalse(result.isValid());
            assertTrue(result.hasErrors());
            assertTrue(result.hasSevereErrors());
            assertTrue(result.hasCriticalErrors());
            assertTrue(result.hasWarnings());
            assertEquals(4, result.getErrors().size());
        }
    }

    @Nested
    @DisplayName("Clear Operation Tests")
    class ClearOperationTests {

        @Test
        @DisplayName("Should clear all errors")
        void shouldClearAllErrors() {
            // Given
            result.addError("E1", "Error 1", ValidationSeverity.ERROR);
            result.addError("E2", "Error 2", ValidationSeverity.WARNING);
            result.addError("E3", "Error 3", ValidationSeverity.CRITICAL);
            assertEquals(3, result.getErrors().size());
            
            // When
            result.clear();
            
            // Then
            assertEquals(0, result.getErrors().size());
            assertTrue(result.isValid());
            assertFalse(result.hasErrors());
        }

        @Test
        @DisplayName("Should be reusable after clear")
        void shouldBeReusableAfterClear() {
            // Given
            result.addError("E1", "Error 1", ValidationSeverity.ERROR);
            result.clear();
            
            // When
            result.addError("E2", "Error 2", ValidationSeverity.WARNING);
            
            // Then
            assertEquals(1, result.getErrors().size());
            assertEquals("E2", result.getErrors().get(0).errorCode());
        }
    }

    @Nested
    @DisplayName("Formatted Output Tests")
    class FormattedOutputTests {

        @Test
        @DisplayName("Should format errors by severity")
        void shouldFormatErrorsBySeverity() {
            // Given
            result.addError("C1", "Critical error 1", ValidationSeverity.CRITICAL);
            result.addError("C2", "Critical error 2", ValidationSeverity.CRITICAL);
            result.addError("E1", "Regular error 1", ValidationSeverity.ERROR);
            result.addError("E2", "Regular error 2", ValidationSeverity.ERROR);
            result.addError("W1", "Warning 1", ValidationSeverity.WARNING);
            
            // When
            String formatted = result.getFormattedErrors();
            
            // Then
            assertTrue(formatted.contains("CRITICAL ERRORS:"));
            assertTrue(formatted.contains("Critical error 1"));
            assertTrue(formatted.contains("Critical error 2"));
            assertTrue(formatted.contains("ERRORS:"));
            assertTrue(formatted.contains("Regular error 1"));
            assertTrue(formatted.contains("Regular error 2"));
            assertTrue(formatted.contains("WARNINGS:"));
            assertTrue(formatted.contains("Warning 1"));
            
            // Verify order - critical should come before errors, errors before warnings
            int criticalIndex = formatted.indexOf("CRITICAL ERRORS:");
            int errorIndex = formatted.indexOf("ERRORS:");
            int warningIndex = formatted.indexOf("WARNINGS:");
            assertTrue(criticalIndex < errorIndex);
            assertTrue(errorIndex < warningIndex);
        }

        @Test
        @DisplayName("Should handle empty result formatting")
        void shouldHandleEmptyResultFormatting() {
            // When
            String formatted = result.getFormattedErrors();
            
            // Then
            assertEquals("", formatted);
        }

        @Test
        @DisplayName("Should format only present severity levels")
        void shouldFormatOnlyPresentSeverityLevels() {
            // Given - only warnings
            result.addError("W1", "Warning 1", ValidationSeverity.WARNING);
            result.addError("W2", "Warning 2", ValidationSeverity.WARNING);
            
            // When
            String formatted = result.getFormattedErrors();
            
            // Then
            assertFalse(formatted.contains("CRITICAL ERRORS:"));
            assertFalse(formatted.contains("ERRORS:"));
            assertTrue(formatted.contains("WARNINGS:"));
            assertTrue(formatted.contains("Warning 1"));
            assertTrue(formatted.contains("Warning 2"));
        }

        @Test
        @DisplayName("Should use dash prefix for error items")
        void shouldUseDashPrefixForErrorItems() {
            // Given
            result.addError("E1", "Error message", ValidationSeverity.ERROR);
            
            // When
            String formatted = result.getFormattedErrors();
            
            // Then
            assertTrue(formatted.contains("- Error message"));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should provide meaningful toString")
        void shouldProvideMeaningfulToString() {
            // Given
            result.addError("E1", "Error 1", ValidationSeverity.ERROR);
            result.addError("W1", "Warning 1", ValidationSeverity.WARNING);
            
            // When
            String string = result.toString();
            
            // Then
            assertTrue(string.contains("ValidationResult"));
            assertTrue(string.contains("isValid=false"));
            assertTrue(string.contains("errors="));
        }

        @Test
        @DisplayName("Should show valid state in toString")
        void shouldShowValidStateInToString() {
            // When
            String string = result.toString();
            
            // Then
            assertTrue(string.contains("isValid=true"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle large number of errors")
        void shouldHandleLargeNumberOfErrors() {
            // Given
            for (int i = 0; i < 1000; i++) {
                result.addError("ERR" + i, "Error " + i, ValidationSeverity.ERROR);
            }
            
            // Then
            assertEquals(1000, result.getErrors().size());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should handle duplicate errors")
        void shouldHandleDuplicateErrors() {
            // Given
            ValidationError error = new ValidationError("DUP", "Duplicate", ValidationSeverity.ERROR);
            
            // When
            result.addError(error);
            result.addError(error);
            result.addError(error);
            
            // Then
            assertEquals(3, result.getErrors().size());
            // All references should be to the same object
            assertTrue(result.getErrors().stream().allMatch(e -> e == error));
        }

        @ParameterizedTest
        @CsvSource({
            "CRITICAL, false",
            "ERROR, false",
            "WARNING, true",
            "INFO, true"
        })
        @DisplayName("Should determine validity based on severity")
        void shouldDetermineValidityBasedOnSeverity(ValidationSeverity severity, boolean expectedValid) {
            // Given
            result.addError("TEST", "Test error", severity);
            
            // Then
            assertEquals(expectedValid, result.isValid());
        }
    }
}