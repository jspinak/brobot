package io.github.jspinak.brobot.runner.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

/**
 * Utility class for error handling. Provides methods for logging errors and displaying error
 * information to the user.
 */
@Data
public class ErrorUtils {
    private static final Logger logger = LoggerFactory.getLogger(ErrorUtils.class);

    /** Private constructor to prevent instantiation. */
    private ErrorUtils() {
        // Utility class
    }

    /**
     * Handles an exception by logging it and optionally displaying it to the user.
     *
     * @param exception The exception to handle
     * @param message A descriptive message about the error
     * @param displayToUser Whether to display the error to the user
     */
    public static void handleException(Exception exception, String message, boolean displayToUser) {
        // Log the error
        logger.error(message, exception);

        // Display to user if requested
        if (displayToUser) {
            FxThreadUtils.runAsync(() -> showErrorDialog(message, exception));
        }
    }

    /**
     * Handles an exception by logging it and optionally displaying it to the user.
     *
     * @param exception The exception to handle
     * @param message A descriptive message about the error
     * @param displayToUser Whether to display the error to the user
     * @param onErrorDisplayed Callback to execute after error is displayed to user
     */
    public static void handleException(
            Exception exception,
            String message,
            boolean displayToUser,
            Consumer<Exception> onErrorDisplayed) {
        // Log the error
        logger.error(message, exception);

        // Display to user if requested
        if (displayToUser) {
            FxThreadUtils.runAsync(
                    () -> {
                        showErrorDialog(message, exception);
                        if (onErrorDisplayed != null) {
                            onErrorDisplayed.accept(exception);
                        }
                    });
        } else if (onErrorDisplayed != null) {
            onErrorDisplayed.accept(exception);
        }
    }

    /**
     * Shows an error dialog to the user.
     *
     * @param message The error message
     * @param exception The exception that occurred
     */
    public static void showErrorDialog(String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);

        if (exception != null) {
            // Create expandable Exception section
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("Exception stacktrace:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);
        }

        alert.showAndWait();
    }

    /**
     * Gets a formatted string representation of an exception.
     *
     * @param exception The exception
     * @return A string representation with message and stack trace
     */
    public static String formatException(Exception exception) {
        if (exception == null) {
            return "No exception information available";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Exception: ").append(exception.getClass().getName()).append("\n");
        sb.append("Message: ").append(exception.getMessage()).append("\n\n");
        sb.append("Stack trace:\n");

        // Get the stack trace
        StackTraceElement[] stackTrace = exception.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            sb.append("    at ").append(element).append("\n");
        }

        // Handle cause if present
        Throwable cause = exception.getCause();
        if (cause != null) {
            sb.append("\nCaused by: ").append(cause.getClass().getName()).append("\n");
            sb.append("Message: ").append(cause.getMessage()).append("\n\n");
            sb.append("Stack trace:\n");

            // Get the cause stack trace
            StackTraceElement[] causeTrace = cause.getStackTrace();
            for (StackTraceElement element : causeTrace) {
                sb.append("    at ").append(element).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Gets a summary of an exception (just class name and message).
     *
     * @param exception The exception
     * @return A short summary of the exception
     */
    public static String getExceptionSummary(Exception exception) {
        if (exception == null) {
            return "No exception";
        }

        return exception.getClass().getSimpleName() + ": " + exception.getMessage();
    }

    /**
     * Creates an error dialog from a generic error description.
     *
     * @param title The dialog title
     * @param header The dialog header
     * @param content The error details
     */
    public static void showErrorAlert(String title, String header, String content) {
        FxThreadUtils.runAsync(
                () -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(title);
                    alert.setHeaderText(header);
                    alert.setContentText(content);
                    alert.showAndWait();
                });
    }

    /**
     * Checks if an exception is of a specific type or has a cause of that type.
     *
     * @param exception The exception to check
     * @param exceptionType The exception type to look for
     * @return true if the exception is of the specified type or has a cause of that type
     */
    public static boolean isExceptionOfType(
            Throwable exception, Class<? extends Throwable> exceptionType) {
        if (exception == null || exceptionType == null) {
            return false;
        }

        if (exceptionType.isInstance(exception)) {
            return true;
        }

        Throwable cause = exception.getCause();
        return isExceptionOfType(cause, exceptionType);
    }

    /**
     * Gets the root cause of an exception.
     *
     * @param exception The exception
     * @return The root cause exception
     */
    public static Throwable getRootCause(Throwable exception) {
        if (exception == null) {
            return null;
        }

        Throwable cause = exception;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }

        return cause;
    }

    /**
     * Gets a list of all causes in the exception chain.
     *
     * @param exception The exception
     * @return An array of all exceptions in the cause chain
     */
    public static Throwable[] getCauseChain(Throwable exception) {
        if (exception == null) {
            return new Throwable[0];
        }

        java.util.List<Throwable> chain = new java.util.ArrayList<>();
        Throwable current = exception;

        while (current != null) {
            chain.add(current);
            if (current.getCause() == null || current.getCause() == current) {
                break;
            }
            current = current.getCause();
        }

        return chain.toArray(new Throwable[0]);
    }
}
