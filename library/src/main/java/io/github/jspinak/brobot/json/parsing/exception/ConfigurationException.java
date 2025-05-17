package io.github.jspinak.brobot.json.parsing.exception;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import lombok.Getter;

/**
 * Exception thrown when there is an error in processing Brobot Runner configurations.
 * This includes errors with parsing, validation, or other configuration-related problems.
 */
@Getter
public class ConfigurationException extends Exception {

    private final ValidationResult validationResult;

    /**
     * Create a new configuration exception with a message
     *
     * @param message The error message
     */
    public ConfigurationException(String message) {
        super(message);
        this.validationResult = new ValidationResult();
    }

    /**
     * Create a new configuration exception with a message and cause
     *
     * @param message The error message
     * @param cause The cause of the exception
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.validationResult = new ValidationResult();
    }

    /**
     * Create a new configuration exception with a message and validation result
     *
     * @param message The error message
     * @param validationResult The validation result
     */
    public ConfigurationException(String message, ValidationResult validationResult) {
        super(message);
        this.validationResult = validationResult != null ? validationResult : new ValidationResult();
    }

    /**
     * Create a new configuration exception with a message, cause and validation result
     *
     * @param message The error message
     * @param cause The cause of the exception
     * @param validationResult The validation result
     */
    public ConfigurationException(String message, Throwable cause, ValidationResult validationResult) {
        super(message, cause);
        this.validationResult = validationResult != null ? validationResult : new ValidationResult();
    }

    /**
     * Create a new configuration exception with a formatted message
     *
     * @param format The format string
     * @param args The format arguments
     */
    public static ConfigurationException formatted(String format, Object... args) {
        return new ConfigurationException(String.format(format, args));
    }
}