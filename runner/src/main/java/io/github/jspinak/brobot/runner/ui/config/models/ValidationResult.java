package io.github.jspinak.brobot.runner.ui.config.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Represents the result of a validation operation.
 * Can be success, warning, or error with an optional message.
 */
@Data
@RequiredArgsConstructor
public class ValidationResult {
    
    public enum Severity {
        SUCCESS,
        WARNING,
        ERROR
    }
    
    private final Severity severity;
    private final String message;
    
    /**
     * Creates a successful validation result.
     */
    public static ValidationResult success() {
        return new ValidationResult(Severity.SUCCESS, null);
    }
    
    /**
     * Creates a successful validation result with a message.
     */
    public static ValidationResult success(String message) {
        return new ValidationResult(Severity.SUCCESS, message);
    }
    
    /**
     * Creates a warning validation result.
     */
    public static ValidationResult warning(String message) {
        return new ValidationResult(Severity.WARNING, message);
    }
    
    /**
     * Creates an error validation result.
     */
    public static ValidationResult error(String message) {
        return new ValidationResult(Severity.ERROR, message);
    }
    
    /**
     * Checks if the validation was successful (no errors).
     * Warnings are considered valid.
     */
    public boolean isValid() {
        return severity != Severity.ERROR;
    }
    
    /**
     * Checks if this is a success result.
     */
    public boolean isSuccess() {
        return severity == Severity.SUCCESS;
    }
    
    /**
     * Checks if this is a warning result.
     */
    public boolean isWarning() {
        return severity == Severity.WARNING;
    }
    
    /**
     * Checks if this is an error result.
     */
    public boolean isError() {
        return severity == Severity.ERROR;
    }
    
    /**
     * Gets a formatted message including the severity.
     */
    public String getFormattedMessage() {
        if (message == null || message.isEmpty()) {
            return severity.toString();
        }
        return String.format("[%s] %s", severity, message);
    }
    
    @Override
    public String toString() {
        return getFormattedMessage();
    }
}