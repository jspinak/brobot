// ConfigValidationException.java
package io.github.jspinak.brobot.json.schemaValidation.exception;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationError;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationSeverity;
import lombok.Getter;

import java.util.List;

/**
 * Exception thrown when configuration validation fails.
 * This exception contains detailed validation results that can be used
 * to report specific issues with the configuration.
 */
@Getter
public class ConfigValidationException extends RuntimeException {
    /**
     * -- GETTER --
     *  Gets the validation result associated with this exception.
     *
     * @return Validation result
     */
    private final ValidationResult validationResult;

    /**
     * Creates a new exception with the given message.
     *
     * @param message Error message
     */
    public ConfigValidationException(String message) {
        super(message);
        this.validationResult = new ValidationResult();
    }

    /**
     * Creates a new exception with the given validation result.
     *
     * @param validationResult Result containing validation errors
     */
    public ConfigValidationException(ValidationResult validationResult) {
        super(createMessageFromResult(validationResult));
        this.validationResult = validationResult;
    }

    /**
     * Creates a new exception with the given message and validation result.
     *
     * @param message Error message
     * @param validationResult Result containing validation errors
     */
    public ConfigValidationException(String message, ValidationResult validationResult) {
        super(message + "\n" + createMessageFromResult(validationResult));
        this.validationResult = validationResult;
    }

    /**
     * Creates a new exception with a cause.
     *
     * @param message Error message
     * @param cause Exception that caused the validation failure
     */
    public ConfigValidationException(String message, Throwable cause) {
        super(message, cause);
        this.validationResult = new ValidationResult();
    }

    /**
     * Creates a new exception with a cause and validation result.
     *
     * @param message Error message
     * @param cause Exception that caused the validation failure
     * @param validationResult Result containing validation errors
     */
    public ConfigValidationException(String message, Throwable cause, ValidationResult validationResult) {
        super(message + "\n" + createMessageFromResult(validationResult), cause);
        this.validationResult = validationResult;
    }

    /**
     * Creates a formatted message from a validation result.
     *
     * @param result Validation result
     * @return Formatted message string
     */
    private static String createMessageFromResult(ValidationResult result) {
        if (result == null || !result.hasErrors()) {
            return "No validation errors";
        }

        StringBuilder sb = new StringBuilder();

        // Critical errors
        List<ValidationError> criticalErrors = result.getCriticalErrors();
        if (!criticalErrors.isEmpty()) {
            sb.append("CRITICAL ERRORS:\n");
            criticalErrors.forEach(e -> sb.append("- [").append(e.errorCode()).append("] ").append(e.message()).append("\n"));
            sb.append("\n");
        }

        // Regular errors
        List<ValidationError> regularErrors = result.getErrors().stream()
                .filter(e -> e.severity() == ValidationSeverity.ERROR)
                .toList();

        if (!regularErrors.isEmpty()) {
            sb.append("ERRORS:\n");
            regularErrors.forEach(e -> sb.append("- [").append(e.errorCode()).append("] ").append(e.message()).append("\n"));
            sb.append("\n");
        }

        // Warnings
        List<ValidationError> warnings = result.getWarnings();
        if (!warnings.isEmpty()) {
            sb.append("WARNINGS:\n");
            warnings.forEach(e -> sb.append("- [").append(e.errorCode()).append("] ").append(e.message()).append("\n"));
        }

        return sb.toString();
    }
}