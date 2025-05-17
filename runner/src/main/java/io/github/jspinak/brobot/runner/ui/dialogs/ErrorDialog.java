package io.github.jspinak.brobot.runner.ui.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.function.Function;

/**
 * Utility class for showing standardized error dialogs
 */
public class ErrorDialog {

    // This field allows tests to replace the alert factory
    public static Function<Alert.AlertType, Alert> alertFactory = Alert::new;

    /**
     * Shows an error dialog with the given title, header, and content
     *
     * @param title The dialog title
     * @param header The header text
     * @param content The detailed content
     */
    public static void show(String title, String header, String content) {
        Alert alert = alertFactory.apply(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);

        // For longer error messages, use a scrollable text area
        if (content != null && content.length() > 100) {
            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 0);

            alert.getDialogPane().setExpandableContent(expContent);
            alert.getDialogPane().setExpanded(true);
        } else {
            alert.setContentText(content);
        }

        alert.showAndWait();
    }
}