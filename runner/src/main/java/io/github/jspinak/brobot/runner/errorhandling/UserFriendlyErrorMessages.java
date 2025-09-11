package io.github.jspinak.brobot.runner.errorhandling;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides user-friendly error messages for various error scenarios. Translates technical errors
 * into messages that users can understand and act upon.
 */
@Component
@Slf4j
@Data
public class UserFriendlyErrorMessages {

    private final Map<Pattern, MessageTemplate> errorPatterns = new HashMap<>();
    private final Map<Class<? extends Throwable>, MessageTemplate> errorTypeMessages =
            new HashMap<>();
    private final Map<ErrorContext.ErrorCategory, CategoryMessages> categoryMessages =
            new HashMap<>();

    public UserFriendlyErrorMessages() {
        initializeErrorPatterns();
        initializeErrorTypeMessages();
        initializeCategoryMessages();
    }

    /** Get a user-friendly message for an error. */
    public ErrorMessage getUserMessage(Throwable error, ErrorContext context) {
        // Try specific error type first
        MessageTemplate template = errorTypeMessages.get(error.getClass());
        if (template != null) {
            return template.format(error, context);
        }

        // Try pattern matching on error message
        String errorMessage = error.getMessage();
        if (errorMessage != null) {
            for (Map.Entry<Pattern, MessageTemplate> entry : errorPatterns.entrySet()) {
                if (entry.getKey().matcher(errorMessage).find()) {
                    return entry.getValue().format(error, context);
                }
            }
        }

        // Fall back to category-based message
        CategoryMessages catMessages = categoryMessages.get(context.getCategory());
        if (catMessages != null) {
            return catMessages.getDefaultMessage(error, context);
        }

        // Ultimate fallback
        return getGenericMessage(error, context);
    }

    private void initializeErrorPatterns() {
        // File-related patterns
        errorPatterns.put(
                Pattern.compile("(No such file|File not found)", Pattern.CASE_INSENSITIVE),
                new MessageTemplate(
                        "Cannot find the requested file",
                        "The file you're looking for doesn't exist or has been moved.",
                        "Check the file path and ensure the file exists"));

        errorPatterns.put(
                Pattern.compile("(Permission denied|Access denied)", Pattern.CASE_INSENSITIVE),
                new MessageTemplate(
                        "Access denied",
                        "You don't have permission to access this file or resource.",
                        "Check your permissions or run the application as administrator"));

        errorPatterns.put(
                Pattern.compile("(Connection refused|Cannot connect)", Pattern.CASE_INSENSITIVE),
                new MessageTemplate(
                        "Connection failed",
                        "Unable to connect to the required service.",
                        "Check your network connection and firewall settings"));

        errorPatterns.put(
                Pattern.compile("(Timeout|Timed out)", Pattern.CASE_INSENSITIVE),
                new MessageTemplate(
                        "Operation timed out",
                        "The operation took too long to complete.",
                        "Try again or check if the service is responding"));

        errorPatterns.put(
                Pattern.compile("(Out of memory|heap space)", Pattern.CASE_INSENSITIVE),
                new MessageTemplate(
                        "Not enough memory",
                        "The application has run out of available memory.",
                        "Close other applications or increase the memory allocation"));

        errorPatterns.put(
                Pattern.compile("(Invalid|Illegal) (argument|parameter)", Pattern.CASE_INSENSITIVE),
                new MessageTemplate(
                        "Invalid input",
                        "The provided input is not valid.",
                        "Check your input and try again"));
    }

    private void initializeErrorTypeMessages() {
        // Common Java exceptions
        errorTypeMessages.put(
                NullPointerException.class,
                new MessageTemplate(
                        "Missing information",
                        "A required piece of information is missing.",
                        "Please ensure all required fields are filled"));

        errorTypeMessages.put(
                IllegalArgumentException.class,
                new MessageTemplate(
                        "Invalid input",
                        "The provided input is not acceptable.",
                        "Please check your input and try again"));

        errorTypeMessages.put(
                IllegalStateException.class,
                new MessageTemplate(
                        "Invalid operation",
                        "This operation cannot be performed right now.",
                        "Please complete the current task before proceeding"));

        errorTypeMessages.put(
                java.io.FileNotFoundException.class,
                new MessageTemplate(
                        "File not found",
                        "The requested file could not be located.",
                        "Verify the file path and try again"));

        errorTypeMessages.put(
                java.io.IOException.class,
                new MessageTemplate(
                        "File operation failed",
                        "An error occurred while accessing a file.",
                        "Check file permissions and disk space"));

        errorTypeMessages.put(
                java.net.ConnectException.class,
                new MessageTemplate(
                        "Connection failed",
                        "Could not establish a network connection.",
                        "Check your internet connection and firewall"));

        errorTypeMessages.put(
                java.util.concurrent.TimeoutException.class,
                new MessageTemplate(
                        "Operation timed out",
                        "The operation did not complete in time.",
                        "Please try again or contact support if the problem persists"));
    }

    private void initializeCategoryMessages() {
        // Configuration errors
        categoryMessages.put(
                ErrorContext.ErrorCategory.CONFIGURATION,
                new CategoryMessages(
                        "Configuration Error",
                        "There's a problem with the application configuration.",
                        Map.of(
                                "missing", "Required configuration is missing",
                                "invalid", "Configuration contains invalid values",
                                "corrupt", "Configuration file is corrupted")));

        // File I/O errors
        categoryMessages.put(
                ErrorContext.ErrorCategory.FILE_IO,
                new CategoryMessages(
                        "File Access Error",
                        "Problem accessing files on your system.",
                        Map.of(
                                "read", "Cannot read the file",
                                "write", "Cannot save the file",
                                "delete", "Cannot delete the file",
                                "create", "Cannot create the file")));

        // Network errors
        categoryMessages.put(
                ErrorContext.ErrorCategory.NETWORK,
                new CategoryMessages(
                        "Network Error",
                        "Problem with network connectivity.",
                        Map.of(
                                "offline", "No internet connection available",
                                "slow", "Network connection is too slow",
                                "blocked", "Network access is blocked")));

        // Validation errors
        categoryMessages.put(
                ErrorContext.ErrorCategory.VALIDATION,
                new CategoryMessages(
                        "Validation Error",
                        "The provided data is not valid.",
                        Map.of(
                                "required", "Required fields are missing",
                                "format", "Data is in the wrong format",
                                "range", "Value is outside the allowed range")));

        // Automation errors
        categoryMessages.put(
                ErrorContext.ErrorCategory.AUTOMATION,
                new CategoryMessages(
                        "Automation Error",
                        "Problem executing the automation.",
                        Map.of(
                                "element", "Cannot find the UI element",
                                "timeout", "Automation step timed out",
                                "state", "Application is in unexpected state")));
    }

    private ErrorMessage getGenericMessage(Throwable error, ErrorContext context) {
        String title = context.getCategory().getDisplayName();
        String description = "An unexpected error occurred.";
        String suggestion = "Please try again or contact support if the problem persists.";

        if (context.isRecoverable() && context.getRecoveryHint() != null) {
            suggestion = context.getRecoveryHint();
        }

        return new ErrorMessage(
                title,
                description,
                suggestion,
                context.getErrorId(),
                context.getSeverity(),
                context.isRecoverable());
    }

    /** Template for creating error messages. */
    private static class MessageTemplate {
        private final String title;
        private final String description;
        private final String suggestion;

        MessageTemplate(String title, String description, String suggestion) {
            this.title = title;
            this.description = description;
            this.suggestion = suggestion;
        }

        ErrorMessage format(Throwable error, ErrorContext context) {
            // Can customize based on context
            String formattedDescription = description;
            if (error.getMessage() != null && !error.getMessage().isEmpty()) {
                formattedDescription +=
                        "\n\nDetails: " + cleanupTechnicalMessage(error.getMessage());
            }

            return new ErrorMessage(
                    title,
                    formattedDescription,
                    suggestion,
                    context.getErrorId(),
                    context.getSeverity(),
                    context.isRecoverable());
        }

        private String cleanupTechnicalMessage(String message) {
            // Remove technical jargon and make more user-friendly
            return message.replaceAll("java\\.[\\w.]+Exception:\\s*", "")
                    .replaceAll("\\bat\\s+[\\w.]+\\([\\w.]+:\\d+\\)", "")
                    .replaceAll("\\$", "")
                    .trim();
        }
    }

    /** Category-specific messages. */
    private static class CategoryMessages {
        private final String defaultTitle;
        private final String defaultDescription;
        private final Map<String, String> specificMessages;

        CategoryMessages(
                String defaultTitle,
                String defaultDescription,
                Map<String, String> specificMessages) {
            this.defaultTitle = defaultTitle;
            this.defaultDescription = defaultDescription;
            this.specificMessages = specificMessages;
        }

        ErrorMessage getDefaultMessage(Throwable error, ErrorContext context) {
            String description = defaultDescription;

            // Try to find a more specific message based on error details
            if (error.getMessage() != null) {
                String lowerMessage = error.getMessage().toLowerCase();
                for (Map.Entry<String, String> entry : specificMessages.entrySet()) {
                    if (lowerMessage.contains(entry.getKey())) {
                        description = entry.getValue();
                        break;
                    }
                }
            }

            String suggestion = "Please try again.";
            if (context.isRecoverable() && context.getRecoveryHint() != null) {
                suggestion = context.getRecoveryHint();
            }

            return new ErrorMessage(
                    defaultTitle,
                    description,
                    suggestion,
                    context.getErrorId(),
                    context.getSeverity(),
                    context.isRecoverable());
        }
    }

    /** User-friendly error message. */
    public record ErrorMessage(
            String title,
            String description,
            String suggestion,
            String errorId,
            ErrorContext.ErrorSeverity severity,
            boolean recoverable) {
        /** Get a formatted message suitable for display in a dialog. */
        public String getDialogMessage() {
            StringBuilder message = new StringBuilder();
            message.append(description);

            if (suggestion != null && !suggestion.isEmpty()) {
                message.append("\n\n").append(suggestion);
            }

            if (severity == ErrorContext.ErrorSeverity.CRITICAL) {
                message.append("\n\nError ID: ").append(errorId);
            }

            return message.toString();
        }

        /** Get a short notification message. */
        public String getNotificationMessage() {
            return title + ": " + getFirstSentence(description);
        }

        private String getFirstSentence(String text) {
            int endIndex = text.indexOf('.');
            if (endIndex > 0 && endIndex < 100) {
                return text.substring(0, endIndex + 1);
            }
            return text.length() > 100 ? text.substring(0, 97) + "..." : text;
        }
    }
}
