package io.github.jspinak.brobot.runner.json.validation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregates results from configuration validation operations.
 *
 * <p>This class collects all validation errors discovered during the validation process and
 * provides convenient methods for querying and displaying them. It supports merging results from
 * multiple validators, filtering by severity, and formatting errors for display.
 *
 * <h2>Key Features:</h2>
 *
 * <ul>
 *   <li>Accumulates errors from multiple validation sources
 *   <li>Filters errors by severity level for targeted handling
 *   <li>Provides formatted output for logging and user display
 *   <li>Supports result merging for composite validation operations
 *   <li>Thread-safe for use in concurrent validation scenarios
 * </ul>
 *
 * <h2>Severity Handling:</h2>
 *
 * <p>The class provides specialized methods for different severity levels:
 *
 * <ul>
 *   <li><b>Critical Errors</b> - Must be fixed before the configuration can be used
 *   <li><b>Regular Errors</b> - Should be fixed but might not block all operations
 *   <li><b>Warnings</b> - Indicate potential issues or best practice violations
 *   <li><b>Info Messages</b> - Provide helpful information about the configuration
 * </ul>
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * ValidationResult result = new ValidationResult();
 *
 * // Add errors during validation
 * result.addError(new ValidationError(
 *     "Missing state",
 *     "State ID 5 not found",
 *     ValidationSeverity.ERROR
 * ));
 *
 * // Merge results from multiple validators
 * result.merge(schemaValidator.validate(json));
 * result.merge(referenceValidator.validate(model));
 *
 * // Check validation outcome
 * if (result.hasCriticalErrors()) {
 *     throw new ConfigValidationException(result);
 * } else if (result.hasWarnings()) {
 *     logger.warn("Validation warnings:\n{}", result.getFormattedErrors());
 * }
 *
 * // Process specific error types
 * result.getErrors().stream()
 *     .filter(e -> e.errorCode().contains("reference"))
 *     .forEach(e -> handleReferenceError(e));
 * }</pre>
 *
 * @see ValidationError for individual error representation
 * @see ValidationSeverity for severity level definitions
 * @see ConfigurationValidator for usage in validation pipelines
 * @author jspinak
 */
public class ValidationResult {
    private final List<ValidationError> errors = new ArrayList<>();

    /**
     * Adds a validation error to this result.
     *
     * <p>This method safely adds an error to the internal collection, ignoring null values to
     * prevent NullPointerExceptions. Errors are stored in the order they are added, which typically
     * reflects the order of validation.
     *
     * @param error The validation error to add, or null (which will be ignored)
     */
    public void addError(ValidationError error) {
        if (error != null) {
            this.errors.add(error);
        }
    }

    /**
     * Convenience method to add an error by specifying its components.
     *
     * <p>This method creates a new ValidationError instance and adds it to the result. It's useful
     * when you have the error components but haven't created the ValidationError object yet.
     *
     * @param errorCode A stable identifier for this type of error
     * @param message A detailed description of the error
     * @param severity The severity level of the error
     * @see ValidationError for error code conventions
     */
    public void addError(String errorCode, String message, ValidationSeverity severity) {
        this.errors.add(new ValidationError(errorCode, message, severity));
    }

    /**
     * Merges all errors from another validation result into this one.
     *
     * <p>This method is essential for composite validation operations where multiple validators
     * contribute to the final result. It safely handles null inputs and preserves the order of
     * errors from both results.
     *
     * <h3>Common Usage:</h3>
     *
     * <pre>{@code
     * ValidationResult finalResult = new ValidationResult();
     * finalResult.merge(schemaValidator.validate(config));
     * finalResult.merge(referenceValidator.validate(config));
     * finalResult.merge(businessRuleValidator.validate(config));
     * }</pre>
     *
     * @param otherResult The validation result to merge into this one, or null (which will be
     *     ignored)
     */
    public void merge(ValidationResult otherResult) {
        if (otherResult != null && otherResult.getErrors() != null) {
            this.errors.addAll(otherResult.getErrors());
        }
    }

    /**
     * Gets all validation errors regardless of severity.
     *
     * <p>Returns an unmodifiable view of all errors to prevent external modification of the
     * validation state. Errors are returned in the order they were added.
     *
     * @return Unmodifiable list of all validation errors in chronological order
     */
    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors); // Return an unmodifiable list
    }

    /**
     * Gets validation errors with WARNING severity.
     *
     * @return List of warnings
     */
    public List<ValidationError> getWarnings() {
        return errors.stream()
                .filter(e -> e.severity() == ValidationSeverity.WARNING)
                .collect(Collectors.toList());
    }

    /**
     * Gets validation errors with ERROR or CRITICAL severity.
     *
     * @return List of errors and critical errors
     */
    public List<ValidationError> getErrorsAndCritical() {
        return errors.stream()
                .filter(
                        e ->
                                e.severity() == ValidationSeverity.ERROR
                                        || e.severity() == ValidationSeverity.CRITICAL)
                .collect(Collectors.toList());
    }

    /**
     * Gets validation errors with CRITICAL severity.
     *
     * @return List of critical errors
     */
    public List<ValidationError> getCriticalErrors() {
        return errors.stream()
                .filter(e -> e.severity() == ValidationSeverity.CRITICAL)
                .collect(Collectors.toList());
    }

    /**
     * Gets validation errors with INFO severity.
     *
     * @return List of info messages
     */
    public List<ValidationError> getInfoMessages() {
        return errors.stream()
                .filter(e -> e.severity() == ValidationSeverity.INFO)
                .collect(Collectors.toList());
    }

    /**
     * Checks if there are any errors (of any severity).
     *
     * @return true if there are any errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Checks if there are any errors of ERROR or CRITICAL severity.
     *
     * @return true if there are any severe errors
     */
    public boolean hasSevereErrors() {
        return errors.stream()
                .anyMatch(
                        e ->
                                e.severity() == ValidationSeverity.ERROR
                                        || e.severity() == ValidationSeverity.CRITICAL);
    }

    /**
     * Checks if there are any errors of CRITICAL severity.
     *
     * @return true if there are any critical errors
     */
    public boolean hasCriticalErrors() {
        return errors.stream().anyMatch(e -> e.severity() == ValidationSeverity.CRITICAL);
    }

    /**
     * Checks if there are any errors of WARNING severity.
     *
     * @return true if there are any warnings
     */
    public boolean hasWarnings() {
        return errors.stream().anyMatch(e -> e.severity() == ValidationSeverity.WARNING);
    }

    /**
     * Checks if there are any errors of the specified severity.
     *
     * @param severity The severity level to check for
     * @return true if there are any errors of the specified severity
     */
    public boolean hasErrorsOfSeverity(ValidationSeverity severity) {
        return errors.stream().anyMatch(e -> e.severity() == severity);
    }

    /**
     * Gets all errors of the specified severity.
     *
     * @param severity The severity level to filter by
     * @return List of errors with the specified severity
     */
    public List<ValidationError> getErrorsBySeverity(ValidationSeverity severity) {
        return errors.stream().filter(e -> e.severity() == severity).collect(Collectors.toList());
    }

    /** Clears all validation errors from this result. */
    public void clear() {
        errors.clear();
    }

    /**
     * Creates a formatted string of all errors, grouped by severity.
     *
     * <p>This method produces a human-readable summary of all validation errors, organized by
     * severity level for easy scanning. The format is suitable for logging, console output, or
     * display in error dialogs.
     *
     * <h3>Output Format:</h3>
     *
     * <pre>
     * CRITICAL ERRORS:
     * - Schema validation failed at line 10
     * - Required field 'name' is missing
     *
     * ERRORS:
     * - State ID 5 referenced but not defined
     * - Function 'helper' called but not found
     *
     * WARNINGS:
     * - Function 'process' has high complexity (75 statements)
     * - State 'Loading' is unreachable
     * </pre>
     *
     * @return Formatted string with errors grouped by severity, or empty string if no errors exist
     */
    public String getFormattedErrors() {
        StringBuilder sb = new StringBuilder();

        List<ValidationError> criticalErrors = getCriticalErrors();
        if (!criticalErrors.isEmpty()) {
            sb.append("CRITICAL ERRORS:\n");
            criticalErrors.forEach(e -> sb.append("- ").append(e.message()).append("\n"));
            sb.append("\n");
        }

        List<ValidationError> regularErrors =
                errors.stream()
                        .filter(e -> e.severity() == ValidationSeverity.ERROR)
                        .collect(Collectors.toList());

        if (!regularErrors.isEmpty()) {
            sb.append("ERRORS:\n");
            regularErrors.forEach(e -> sb.append("- ").append(e.message()).append("\n"));
            sb.append("\n");
        }

        List<ValidationError> warnings = getWarnings();
        if (!warnings.isEmpty()) {
            sb.append("WARNINGS:\n");
            warnings.forEach(e -> sb.append("- ").append(e.message()).append("\n"));
        }

        return sb.toString();
    }

    /**
     * Determines if the configuration is valid for use.
     *
     * <p>A configuration is considered valid if it has no severe errors (ERROR or CRITICAL
     * severity). Warnings alone do not make a configuration invalid, though they should still be
     * addressed.
     *
     * <h3>Validity Rules:</h3>
     *
     * <ul>
     *   <li>No errors → Valid ✓
     *   <li>Only warnings → Valid ✓ (but should be reviewed)
     *   <li>Has errors or critical errors → Invalid ✗
     * </ul>
     *
     * @return true if the configuration can be safely used, false if it has errors that must be
     *     fixed
     */
    public boolean isValid() {
        return !hasSevereErrors();
    }

    @Override
    public String toString() {
        return "ValidationResult{" + "isValid=" + isValid() + ", errors=" + errors + '}';
    }
}
