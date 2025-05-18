package io.github.jspinak.brobot.runner.services;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.ui.window.DialogFactory;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Service for displaying dialogs and managing UI interactions.
 * Handles both synchronous and asynchronous dialog operations.
 */
@Service
public class DialogService {
    private static final Logger logger = LoggerFactory.getLogger(DialogService.class);

    private final EventBus eventBus;
    /**
     *  The primary stage for displaying dialogs.
     */
    @Setter
    private Stage primaryStage;

    @Autowired
    public DialogService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Shows an information dialog.
     *
     * @param title The dialog title
     * @param message The message to display
     */
    public void showInformation(String title, String message) {
        Platform.runLater(() -> {
            DialogFactory.createMessageDialog(primaryStage, title, message);
            eventBus.publish(LogEvent.info(this, "Dialog shown: " + title, "UI"));
        });
    }

    /**
     * Shows an error dialog.
     *
     * @param title The dialog title
     * @param message The error message
     */
    public void showError(String title, String message) {
        Platform.runLater(() -> {
            DialogFactory.createErrorDialog(primaryStage, title, message, null);
            eventBus.publish(LogEvent.error(this, "Error dialog shown: " + title + " - " + message, "UI", null));
        });
    }

    /**
     * Shows an error dialog with an exception.
     *
     * @param title The dialog title
     * @param message The error message
     * @param exception The exception that caused the error
     */
    public void showError(String title, String message, Exception exception) {
        Platform.runLater(() -> {
            DialogFactory.createErrorDialog(primaryStage, title, message, exception);
            eventBus.publish(LogEvent.error(this, "Error dialog shown: " + title + " - " + message, "UI", exception));
        });
    }

    /**
     * Shows a confirmation dialog and returns the result.
     *
     * @param title The dialog title
     * @param message The message to display
     * @return true if confirmed, false otherwise
     */
    public boolean showConfirmation(String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            // If called from a non-JavaFX thread, use the async version and wait for the result
            try {
                return showConfirmationAsync(title, message).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error showing confirmation dialog", e);
                return false;
            }
        }

        boolean result = DialogFactory.createConfirmDialog(primaryStage, title, message);
        eventBus.publish(LogEvent.info(this, "Confirmation dialog shown: " + title + ", result: " + result, "UI"));
        return result;
    }

    /**
     * Shows a confirmation dialog asynchronously.
     *
     * @param title The dialog title
     * @param message The message to display
     * @return A CompletableFuture that will resolve to the dialog result
     */
    public CompletableFuture<Boolean> showConfirmationAsync(String title, String message) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            boolean result = DialogFactory.createConfirmDialog(primaryStage, title, message);
            eventBus.publish(LogEvent.info(this, "Async confirmation dialog shown: " + title + ", result: " + result, "UI"));
            future.complete(result);
        });

        return future;
    }

    /**
     * Shows an input dialog and returns the result.
     *
     * @param title The dialog title
     * @param message The message to display
     * @param defaultValue The default input value
     * @return An Optional containing the input, or empty if cancelled
     */
    public Optional<String> showInputDialog(String title, String message, String defaultValue) {
        if (!Platform.isFxApplicationThread()) {
            // If called from a non-JavaFX thread, use the async version and wait for the result
            try {
                return showInputDialogAsync(title, message, defaultValue).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error showing input dialog", e);
                return Optional.empty();
            }
        }

        Optional<String> result = DialogFactory.createInputDialog(primaryStage, title, message, defaultValue);
        eventBus.publish(LogEvent.info(this, "Input dialog shown: " + title + ", has result: " + result.isPresent(), "UI"));
        return result;
    }

    /**
     * Shows an input dialog asynchronously.
     *
     * @param title The dialog title
     * @param message The message to display
     * @param defaultValue The default input value
     * @return A CompletableFuture that will resolve to the dialog result
     */
    public CompletableFuture<Optional<String>> showInputDialogAsync(String title, String message, String defaultValue) {
        CompletableFuture<Optional<String>> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            Optional<String> result = DialogFactory.createInputDialog(primaryStage, title, message, defaultValue);
            eventBus.publish(LogEvent.info(this, "Async input dialog shown: " + title + ", has result: " + result.isPresent(), "UI"));
            future.complete(result);
        });

        return future;
    }

    /**
     * Shows a progress dialog.
     *
     * @param title The dialog title
     * @param message The message to display
     * @return The progress dialog
     */
    public DialogFactory.ProgressDialog showProgressDialog(String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            logger.warn("Progress dialog should be created on the JavaFX application thread");
        }

        DialogFactory.ProgressDialog dialog = DialogFactory.createProgressDialog(primaryStage, title, message);
        eventBus.publish(LogEvent.info(this, "Progress dialog shown: " + title, "UI"));
        return dialog;
    }

    /**
     * Shows a custom alert dialog.
     *
     * @param alertType The alert type
     * @param title The dialog title
     * @param headerText The header text
     * @param contentText The content text
     * @return The result of the dialog (button clicked)
     */
    public Optional<ButtonType> showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        if (!Platform.isFxApplicationThread()) {
            // If called from a non-JavaFX thread, use the async version and wait for the result
            try {
                return showAlertAsync(alertType, title, headerText, contentText).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error showing alert dialog", e);
                return Optional.empty();
            }
        }

        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        if (primaryStage != null) {
            alert.initOwner(primaryStage);
        }

        Optional<ButtonType> result = alert.showAndWait();
        eventBus.publish(LogEvent.info(this, "Alert dialog shown: " + title + ", type: " + alertType, "UI"));
        return result;
    }

    /**
     * Shows a custom alert dialog asynchronously.
     *
     * @param alertType The alert type
     * @param title The dialog title
     * @param headerText The header text
     * @param contentText The content text
     * @return A CompletableFuture that will resolve to the dialog result
     */
    public CompletableFuture<Optional<ButtonType>> showAlertAsync(Alert.AlertType alertType, String title, String headerText, String contentText) {
        CompletableFuture<Optional<ButtonType>> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(headerText);
            alert.setContentText(contentText);

            if (primaryStage != null) {
                alert.initOwner(primaryStage);
            }

            Optional<ButtonType> result = alert.showAndWait();
            eventBus.publish(LogEvent.info(this, "Async alert dialog shown: " + title + ", type: " + alertType, "UI"));
            future.complete(result);
        });

        return future;
    }
}