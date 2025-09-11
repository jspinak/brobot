package io.github.jspinak.brobot.runner.errorhandling;

import java.util.Map;

import lombok.Getter;

/**
 * Base exception class for all application-specific exceptions. Provides structured error
 * information and context.
 */
@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorContext context;
    private final String userMessage;
    private final String technicalDetails;

    public ApplicationException(String message, ErrorContext context) {
        super(message);
        this.context = context;
        this.userMessage = message;
        this.technicalDetails = null;
    }

    public ApplicationException(String userMessage, String technicalDetails, ErrorContext context) {
        super(userMessage);
        this.context = context;
        this.userMessage = userMessage;
        this.technicalDetails = technicalDetails;
    }

    public ApplicationException(String message, Throwable cause, ErrorContext context) {
        super(message, cause);
        this.context = context;
        this.userMessage = message;
        this.technicalDetails = cause != null ? cause.getMessage() : null;
    }

    public ApplicationException(
            String userMessage, String technicalDetails, Throwable cause, ErrorContext context) {
        super(userMessage, cause);
        this.context = context;
        this.userMessage = userMessage;
        this.technicalDetails = technicalDetails;
    }

    /** Get a display-friendly error message for users. */
    public String getDisplayMessage() {
        if (context.isRecoverable() && context.getRecoveryHint() != null) {
            return userMessage + "\n\nSuggestion: " + context.getRecoveryHint();
        }
        return userMessage;
    }

    /** Get the error ID for reference and tracking. */
    public String getErrorId() {
        return context.getErrorId();
    }

    /** Check if this error is recoverable. */
    public boolean isRecoverable() {
        return context.isRecoverable();
    }

    /** Factory method for configuration errors. */
    public static ApplicationException configurationError(String message, String details) {
        ErrorContext context =
                ErrorContext.builder()
                        .category(ErrorContext.ErrorCategory.CONFIGURATION)
                        .severity(ErrorContext.ErrorSeverity.HIGH)
                        .operation("Configuration Loading")
                        .recoverable(false)
                        .build();

        return new ApplicationException(message, details, context);
    }

    /** Factory method for validation errors. */
    public static ApplicationException validationError(String message, String field) {
        ErrorContext context =
                ErrorContext.builder()
                        .category(ErrorContext.ErrorCategory.VALIDATION)
                        .severity(ErrorContext.ErrorSeverity.MEDIUM)
                        .operation("Data Validation")
                        .recoverable(true)
                        .recoveryHint("Please check the " + field + " field and try again.")
                        .build();

        return new ApplicationException(message, context);
    }

    /** Factory method for file I/O errors. */
    public static ApplicationException fileError(String message, String filePath, Throwable cause) {
        ErrorContext context =
                ErrorContext.builder()
                        .category(ErrorContext.ErrorCategory.FILE_IO)
                        .severity(ErrorContext.ErrorSeverity.MEDIUM)
                        .operation("File Operation")
                        .recoverable(true)
                        .recoveryHint("Check if the file exists and you have proper permissions.")
                        .additionalData(Map.of("filePath", filePath))
                        .build();

        return new ApplicationException(message, cause, context);
    }

    /** Factory method for automation errors. */
    public static ApplicationException automationError(String message, String actionName) {
        ErrorContext context =
                ErrorContext.builder()
                        .category(ErrorContext.ErrorCategory.AUTOMATION)
                        .severity(ErrorContext.ErrorSeverity.MEDIUM)
                        .operation("Automation Execution")
                        .recoverable(true)
                        .recoveryHint(
                                "Try running the automation again or check the configuration.")
                        .additionalData(Map.of("actionName", actionName))
                        .build();

        return new ApplicationException(message, context);
    }
}
