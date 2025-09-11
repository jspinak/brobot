package io.github.jspinak.brobot.runner.ui.config;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import lombok.Getter;

/** Panel for displaying configuration validation results. */
public class ValidationResultsPanel extends VBox {
    private static final Logger logger = LoggerFactory.getLogger(ValidationResultsPanel.class);

    private final TreeView<ValidationItem> resultsTree;
    private final TreeItem<ValidationItem> rootItem;

    public ValidationResultsPanel() {
        setPadding(new Insets(10));
        setSpacing(10);

        // Create root tree item
        rootItem =
                new TreeItem<>(
                        new ValidationItem("Validation Results", "", ValidationSeverity.INFO));
        rootItem.setExpanded(true);

        // Create tree view
        resultsTree = new TreeView<>(rootItem);
        resultsTree.setCellFactory(tv -> new ValidationResultsTreeCell());
        resultsTree.setShowRoot(false);

        VBox.setVgrow(resultsTree, Priority.ALWAYS);

        getChildren().add(resultsTree);
    }

    /**
     * Sets the validation result to display.
     *
     * @param result The validation result
     */
    public void setValidationResult(ValidationResult result) {
        // Clear previous results
        rootItem.getChildren().clear();

        if (result == null) {
            return;
        }

        // Add critical errors
        addErrorsToTree("Critical Errors", result.getCriticalErrors(), ValidationSeverity.CRITICAL);

        // Add errors
        addErrorsToTree("Errors", result.getErrors(), ValidationSeverity.ERROR);

        // Add warnings
        addErrorsToTree("Warnings", result.getWarnings(), ValidationSeverity.WARNING);

        // Add info messages
        addErrorsToTree("Information", result.getInfoMessages(), ValidationSeverity.INFO);

        // Expand all items
        expandTreeItems(rootItem);
    }

    private void addErrorsToTree(
            String category, List<ValidationError> errors, ValidationSeverity severity) {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        // Create category item
        TreeItem<ValidationItem> categoryItem =
                new TreeItem<>(
                        new ValidationItem(category + " (" + errors.size() + ")", "", severity));
        categoryItem.setExpanded(true);

        // Add each error as a child item
        for (ValidationError error : errors) {
            TreeItem<ValidationItem> errorItem =
                    new TreeItem<>(
                            new ValidationItem(
                                    error.errorCode(), error.message(), error.severity()));
            categoryItem.getChildren().add(errorItem);
        }

        rootItem.getChildren().add(categoryItem);
    }

    private void expandTreeItems(TreeItem<?> item) {
        if (item != null) {
            item.setExpanded(true);
            for (TreeItem<?> child : item.getChildren()) {
                expandTreeItems(child);
            }
        }
    }

    /** Clears all validation results. */
    public void clearResults() {
        rootItem.getChildren().clear();
    }

    /** Data class for validation items in the tree. */
    @Getter
    public static class ValidationItem {
        private final String message;
        private final String location;
        private final ValidationSeverity severity;

        public ValidationItem(String message, String location, ValidationSeverity severity) {
            this.message = message;
            this.location = location;
            this.severity = severity;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    /** Custom tree cell for validation results. */
    private static class ValidationResultsTreeCell extends TreeCell<ValidationItem> {
        @Override
        protected void updateItem(ValidationItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
                setTooltip(null);
                return;
            }

            setText(item.getMessage());

            // Set style based on severity
            switch (item.getSeverity()) {
                case CRITICAL:
                    setStyle("-fx-text-fill: darkred; -fx-font-weight: bold;");
                    break;
                case ERROR:
                    setStyle("-fx-text-fill: red;");
                    break;
                case WARNING:
                    setStyle("-fx-text-fill: darkorange;");
                    break;
                case INFO:
                    setStyle("-fx-text-fill: green;");
                    break;
                default:
                    setStyle("");
            }

            // Set tooltip with location if available
            if (item.getLocation() != null && !item.getLocation().isEmpty()) {
                setTooltip(new Tooltip("Location: " + item.getLocation()));
            } else {
                setTooltip(null);
            }
        }
    }
}
