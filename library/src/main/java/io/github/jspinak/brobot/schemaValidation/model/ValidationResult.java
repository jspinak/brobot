package io.github.jspinak.brobot.schemaValidation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains the results of a validation operation.
 */
public class ValidationResult {
    private final List<ValidationError> errors = new ArrayList<>();

    /**
     * Adds an error to the validation result.
     *
     * @param error The error to add
     */
    public void addError(ValidationError error) {
        if (error != null) {
            this.errors.add(error);
        }
    }

    /**
     * Adds an error with a specific code, message, and severity.
     *
     * @param errorCode The error code
     * @param message   The error message
     * @param severity  The severity of the error
     */
    public void addError(String errorCode, String message, ValidationSeverity severity) {
        this.errors.add(new ValidationError(errorCode, message, severity));
    }

    /**
     * Merges another validation result into this one.
     *
     * @param otherResult The other validation result to merge
     */
    public void merge(ValidationResult otherResult) {
        if (otherResult != null && otherResult.getErrors() != null) {
            this.errors.addAll(otherResult.getErrors());
        }
    }

    /**
     * Gets all validation errors.
     *
     * @return List of all errors
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
                .filter(e -> e.severity() == ValidationSeverity.ERROR ||
                        e.severity() == ValidationSeverity.CRITICAL)
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
                .anyMatch(e -> e.severity() == ValidationSeverity.ERROR ||
                        e.severity() == ValidationSeverity.CRITICAL);
    }

    /**
     * Checks if there are any errors of CRITICAL severity.
     *
     * @return true if there are any critical errors
     */
    public boolean hasCriticalErrors() {
        return errors.stream()
                .anyMatch(e -> e.severity() == ValidationSeverity.CRITICAL);
    }

    /**
     * Checks if there are any errors of WARNING severity.
     *
     * @return true if there are any warnings
     */
    public boolean hasWarnings() {
        return errors.stream()
                .anyMatch(e -> e.severity() == ValidationSeverity.WARNING);
    }

    /**
     * Returns a formatted string of all errors, grouped by severity.
     *
     * @return formatted error string
     */
    public String getFormattedErrors() {
        StringBuilder sb = new StringBuilder();

        List<ValidationError> criticalErrors = getCriticalErrors();
        if (!criticalErrors.isEmpty()) {
            sb.append("CRITICAL ERRORS:\n");
            criticalErrors.forEach(e -> sb.append("- ").append(e.message()).append("\n"));
            sb.append("\n");
        }

        List<ValidationError> regularErrors = errors.stream()
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
     * Checks if the validation result is valid (i.e., no errors).
     *
     * @return true if valid
     */
    public boolean isValid() {
        return !hasSevereErrors();
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "isValid=" + isValid() +
                ", errors=" + errors +
                '}';
    }

}
