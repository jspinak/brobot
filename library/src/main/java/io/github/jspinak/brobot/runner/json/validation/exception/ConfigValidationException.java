// ConfigValidationException.java
package io.github.jspinak.brobot.runner.json.validation.exception;

import lombok.Getter;

import java.util.List;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

/**
 * Exception thrown when Brobot configuration validation fails.
 * 
 * <p>This exception provides detailed information about validation failures,
 * including the specific errors, their severity levels, and formatted messages
 * for logging or user display. It extends RuntimeException to allow for unchecked
 * exception handling in validation flows.</p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Carries a complete ValidationResult with all discovered issues</li>
 *   <li>Provides formatted error messages grouped by severity</li>
 *   <li>Supports exception chaining for root cause analysis</li>
 *   <li>Integrates with Lombok for automatic getter generation</li>
 * </ul>
 * 
 * <h2>When This Exception Is Thrown:</h2>
 * <ul>
 *   <li>Schema validation encounters critical errors</li>
 *   <li>Cross-reference validation finds invalid references</li>
 *   <li>Business rules are severely violated</li>
 *   <li>Required resources are missing or invalid</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * try {
 *     ValidationResult result = validator.validateConfiguration(
 *         projectJson, dslJson, imagePath);
 *     
 *     if (result.hasCriticalErrors()) {
 *         throw new ConfigValidationException(
 *             "Configuration has critical errors", result);
 *     }
 * } catch (ConfigValidationException e) {
 *     // Log the formatted error message
 *     logger.error("Validation failed: {}", e.getMessage());
 *     
 *     // Access detailed validation results
 *     ValidationResult details = e.getValidationResult();
 *     details.getCriticalErrors().forEach(error -> {
 *         alertUser(error.errorCode(), error.message());
 *     });
 * }
 * }</pre>
 * 
 * @see ValidationResult for understanding validation outcomes
 * @see ConfigurationValidator for the main validation entry point
 * @author jspinak
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
     * Creates a new validation exception with a simple message.
     * 
     * <p>Use this constructor when you have a general validation failure message
     * but no detailed ValidationResult. An empty ValidationResult will be created.</p>
     * 
     * @param message Error message describing the validation failure
     */
    public ConfigValidationException(String message) {
        super(message);
        this.validationResult = new ValidationResult();
    }

    /**
     * Creates a new validation exception from a ValidationResult.
     * 
     * <p>This constructor automatically generates a formatted error message from
     * the validation result, grouping errors by severity. This is useful when
     * you want the exception message to contain all validation details.</p>
     * 
     * @param validationResult Result containing validation errors to include in
     *                         the exception. The result's errors will be formatted
     *                         into a human-readable message
     */
    public ConfigValidationException(ValidationResult validationResult) {
        super(createMessageFromResult(validationResult));
        this.validationResult = validationResult;
    }

    /**
     * Creates a new validation exception with a custom message and detailed results.
     * 
     * <p>This constructor combines a custom message with the formatted validation
     * errors. Use this when you want to provide context about why validation was
     * performed along with the specific errors found.</p>
     * 
     * @param message High-level error message providing context
     * @param validationResult Detailed validation errors to append to the message
     */
    public ConfigValidationException(String message, ValidationResult validationResult) {
        super(message + "\n" + createMessageFromResult(validationResult));
        this.validationResult = validationResult;
    }

    /**
     * Creates a new validation exception with a root cause.
     * 
     * <p>Use this constructor when validation fails due to an underlying exception,
     * such as JSON parsing errors or I/O failures. The cause will be preserved
     * for debugging purposes.</p>
     * 
     * @param message Error message describing the validation failure
     * @param cause The exception that triggered the validation failure, such as
     *              a JSONException or IOException
     */
    public ConfigValidationException(String message, Throwable cause) {
        super(message, cause);
        this.validationResult = new ValidationResult();
    }

    /**
     * Creates a new validation exception with complete error information.
     * 
     * <p>This constructor provides the most complete error information by combining
     * a custom message, root cause exception, and detailed validation results.
     * Use this for comprehensive error reporting in complex validation scenarios.</p>
     * 
     * @param message High-level description of the validation failure
     * @param cause The underlying exception that triggered validation failure
     * @param validationResult Detailed validation errors discovered before the
     *                         exception occurred
     */
    public ConfigValidationException(String message, Throwable cause, ValidationResult validationResult) {
        super(message + "\n" + createMessageFromResult(validationResult), cause);
        this.validationResult = validationResult;
    }

    /**
     * Creates a formatted, human-readable message from validation results.
     * 
     * <p>This method formats validation errors into a structured message grouped
     * by severity level. The format is designed for both logging and user display,
     * with clear sections for critical errors, regular errors, and warnings.</p>
     * 
     * <h3>Message Format:</h3>
     * <pre>
     * CRITICAL ERRORS:
     * - [errorCode] error message
     * 
     * ERRORS:
     * - [errorCode] error message
     * 
     * WARNINGS:
     * - [errorCode] error message
     * </pre>
     * 
     * @param result Validation result containing errors to format
     * @return Formatted string with errors grouped by severity, or
     *         "No validation errors" if the result is null or empty
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