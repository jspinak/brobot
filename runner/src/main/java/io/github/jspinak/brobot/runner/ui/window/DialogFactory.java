package io.github.jspinak.brobot.runner.ui.window;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** Factory for creating common dialog types. */
@Slf4j
public class DialogFactory {

    /**
     * Creates a message dialog.
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param message The message to display
     */
    public static void createMessageDialog(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        setupDialog(alert, owner, title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Creates a confirmation dialog.
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param message The message to display
     * @return true if confirmed, false otherwise
     */
    public static boolean createConfirmDialog(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setupDialog(alert, owner, title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Creates an error dialog.
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param message The error message
     * @param exception The exception that caused the error, or null if none
     */
    public static void createErrorDialog(
            Stage owner, String title, String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setupDialog(alert, owner, title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if (exception != null) {
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
     * Creates an input dialog.
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param message The message to display
     * @param defaultValue The default input value
     * @return The input value, or empty if cancelled
     */
    public static Optional<String> createInputDialog(
            Stage owner, String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        setupDialog(dialog, owner, title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        return dialog.showAndWait();
    }

    /**
     * Creates a progress dialog.
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param message The message to display
     * @return The progress dialog
     */
    public static ProgressDialog createProgressDialog(Stage owner, String title, String message) {
        ProgressDialog dialog = new ProgressDialog(owner, title, message);
        dialog.show();
        return dialog;
    }

    /**
     * Sets up common dialog properties.
     *
     * @param dialog The dialog
     * @param owner The owner window
     * @param title The dialog title
     */
    private static void setupDialog(Dialog<?> dialog, Stage owner, String title) {
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);

        if (owner != null) {
            dialog.initOwner(owner);
        }
    }

    /** A custom progress dialog. */
    public static class ProgressDialog {
        /** The dialog stage. */
        @Getter private final Stage dialogStage;

        private final ProgressBar progressBar;
        private final Label messageLabel;

        /**
         * Creates a new progress dialog.
         *
         * @param owner The owner window
         * @param title The dialog title
         * @param message The message to display
         */
        public ProgressDialog(Stage owner, String title, String message) {
            dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            if (owner != null) {
                dialogStage.initOwner(owner);
            }

            messageLabel = new Label(message);
            progressBar = new ProgressBar();
            progressBar.setPrefWidth(300);

            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(e -> dialogStage.close());

            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(20));
            vbox.getChildren().addAll(messageLabel, progressBar, cancelButton);

            dialogStage.setScene(new javafx.scene.Scene(vbox));
        }

        /** Shows the dialog. */
        public void show() {
            dialogStage.show();
        }

        /** Closes the dialog. */
        public void close() {
            dialogStage.close();
        }

        /**
         * Sets the progress value.
         *
         * @param progress The progress value (0.0 to 1.0)
         */
        public void setProgress(double progress) {
            Platform.runLater(() -> progressBar.setProgress(progress));
        }

        /**
         * Sets the message.
         *
         * @param message The message to display
         */
        public void setMessage(String message) {
            Platform.runLater(() -> messageLabel.setText(message));
        }
    }
}
