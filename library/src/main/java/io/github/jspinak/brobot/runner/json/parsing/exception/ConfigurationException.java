package io.github.jspinak.brobot.runner.json.parsing.exception;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;

import lombok.Getter;

/**
 * Exception thrown when configuration processing fails in the Brobot framework.
 *
 * <p>This exception represents various configuration-related failures including:
 *
 * <ul>
 *   <li>JSON parsing errors - Malformed JSON syntax
 *   <li>Schema validation failures - JSON doesn't conform to expected schema
 *   <li>Missing required fields - Required configuration elements are absent
 *   <li>Type mismatches - Values have incorrect types
 *   <li>File I/O errors - Configuration files cannot be read/written
 *   <li>Path navigation errors - Invalid JSON paths or missing nodes
 * </ul>
 *
 * <p>The exception can optionally carry a {@link ValidationResult} containing detailed validation
 * errors when the failure is due to schema validation. This allows for comprehensive error
 * reporting with specific field-level issues.
 *
 * <p>Common usage patterns:
 *
 * <ul>
 *   <li>Simple error: {@code new ConfigurationException("Config file not found")}
 *   <li>With cause: {@code new ConfigurationException("Parse failed", ioException)}
 *   <li>With validation: {@code new ConfigurationException("Invalid config", validationResult)}
 *   <li>Formatted: {@code ConfigurationException.formatted("Missing field: %s", fieldName)}
 * </ul>
 *
 * @see ValidationResult
 * @see io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser
 * @see io.github.jspinak.brobot.runner.json.validation.ConfigurationValidator
 */
@Getter
public class ConfigurationException extends Exception {

    private final ValidationResult validationResult;

    /**
     * Creates a configuration exception with an error message.
     *
     * <p>Use this constructor for simple error cases where no underlying cause or validation
     * details are available.
     *
     * @param message The error message describing what went wrong
     */
    public ConfigurationException(String message) {
        super(message);
        this.validationResult = new ValidationResult();
    }

    /**
     * Creates a configuration exception with an error message and underlying cause.
     *
     * <p>Use this constructor when the configuration error is caused by another exception, such as
     * IOException during file reading or JsonProcessingException during parsing.
     *
     * @param message The error message describing what went wrong
     * @param cause The underlying exception that caused this configuration error
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.validationResult = new ValidationResult();
    }

    /**
     * Creates a configuration exception with validation details.
     *
     * <p>Use this constructor when the error is due to schema validation failure. The
     * ValidationResult provides detailed information about which fields failed validation and why.
     *
     * @param message The high-level error message
     * @param validationResult Detailed validation errors (null-safe, creates empty result if null)
     */
    public ConfigurationException(String message, ValidationResult validationResult) {
        super(message);
        this.validationResult =
                validationResult != null ? validationResult : new ValidationResult();
    }

    /**
     * Creates a configuration exception with full error context.
     *
     * <p>Use this constructor when you have both an underlying cause and validation details. This
     * provides the most comprehensive error information for debugging.
     *
     * @param message The high-level error message
     * @param cause The underlying exception that triggered the validation
     * @param validationResult Detailed validation errors (null-safe, creates empty result if null)
     */
    public ConfigurationException(
            String message, Throwable cause, ValidationResult validationResult) {
        super(message, cause);
        this.validationResult =
                validationResult != null ? validationResult : new ValidationResult();
    }

    /**
     * Creates a configuration exception with a formatted message.
     *
     * <p>This factory method provides a convenient way to create exceptions with parameterized
     * messages using String.format() syntax. It helps avoid string concatenation and makes messages
     * more readable.
     *
     * <p>Example:
     *
     * <pre>{@code
     * throw ConfigurationException.formatted(
     *     "Invalid value '%s' for field '%s'", value, fieldName);
     * }</pre>
     *
     * @param format The format string (as per String.format())
     * @param args The arguments to be formatted into the message
     * @return A new ConfigurationException with the formatted message
     */
    public static ConfigurationException formatted(String format, Object... args) {
        return new ConfigurationException(String.format(format, args));
    }
}
