package io.github.jspinak.brobot.runner.errorhandling.ui;

import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.UserFriendlyErrorMessages.ErrorMessage;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Dialog for displaying errors to users in a friendly way.
 */
@Slf4j
@Getter
@EqualsAndHashCode(callSuper = false)
public class ErrorDialog extends Dialog<ButtonType> {

    private static final String ERROR_ICON_PATH = "/icons/error.png";
    private static final String WARNING_ICON_PATH = "/icons/warning.png";
    private static final String INFO_ICON_PATH = "/icons/info.png";
    
    private final ErrorMessage errorMessage;
    private final String technicalDetails;
    private final ErrorContext context;
    
    public ErrorDialog(ErrorMessage errorMessage, String technicalDetails, ErrorContext context) {
        this.errorMessage = errorMessage;
        this.technicalDetails = technicalDetails;
        this.context = context;
        
        initializeDialog();
    }
    
    private void initializeDialog() {
        setTitle(errorMessage.title());
        setHeaderText(null);
        
        // Set dialog modality
        initModality(Modality.APPLICATION_MODAL);
        
        // Create content
        VBox content = createContent();
        getDialogPane().setContent(content);
        
        // Add buttons
        addButtons();
        
        // Style the dialog
        styleDialog();
        
        // Set dialog icon
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        if (stage != null) {
            stage.getIcons().add(loadIcon());
        }
    }
    
    private VBox createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        
        // Error icon and message
        HBox messageBox = new HBox(15);
        messageBox.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        
        ImageView icon = new ImageView(loadIcon());
        icon.setFitWidth(48);
        icon.setFitHeight(48);
        icon.setPreserveRatio(true);
        
        VBox messageContent = new VBox(10);
        messageContent.setFillWidth(true);
        HBox.setHgrow(messageContent, Priority.ALWAYS);
        
        // Main message
        Label descriptionLabel = new Label(errorMessage.description());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 14px;");
        messageContent.getChildren().add(descriptionLabel);
        
        // Suggestion
        if (errorMessage.suggestion() != null && !errorMessage.suggestion().isEmpty()) {
            Label suggestionLabel = new Label(errorMessage.suggestion());
            suggestionLabel.setWrapText(true);
            suggestionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
            messageContent.getChildren().add(suggestionLabel);
        }
        
        messageBox.getChildren().addAll(icon, messageContent);
        content.getChildren().add(messageBox);
        
        // Add separator
        content.getChildren().add(new Separator());
        
        // Error details (collapsible)
        if (technicalDetails != null || context != null) {
            TitledPane detailsPane = createDetailsPane();
            content.getChildren().add(detailsPane);
        }
        
        // Error ID for critical errors
        if (errorMessage.severity() == ErrorContext.ErrorSeverity.CRITICAL) {
            Label errorIdLabel = new Label("Error ID: " + errorMessage.errorId());
            errorIdLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
            content.getChildren().add(errorIdLabel);
        }
        
        return content;
    }
    
    private TitledPane createDetailsPane() {
        VBox detailsContent = new VBox(10);
        detailsContent.setPadding(new Insets(10));
        
        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefRowCount(8);
        detailsArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        
        StringBuilder details = new StringBuilder();
        
        // Add context information
        if (context != null) {
            details.append("Error Information:\n");
            details.append("  Time: ").append(
                context.getTimestamp().atZone(java.time.ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ).append("\n");
            details.append("  Category: ").append(context.getCategory().getDisplayName()).append("\n");
            details.append("  Operation: ").append(context.getOperation()).append("\n");
            if (context.getComponent() != null) {
                details.append("  Component: ").append(context.getComponent()).append("\n");
            }
            details.append("\n");
        }
        
        // Add technical details
        if (technicalDetails != null && !technicalDetails.isEmpty()) {
            details.append("Technical Details:\n");
            details.append(technicalDetails);
        }
        
        detailsArea.setText(details.toString());
        detailsContent.getChildren().add(detailsArea);
        
        // Copy button
        Button copyButton = new Button("Copy Details");
        copyButton.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(detailsArea.getText());
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
            
            copyButton.setText("Copied!");
            copyButton.setDisable(true);
            
            // Reset button after 2 seconds
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                javafx.util.Duration.seconds(2)
            );
            pause.setOnFinished(event -> {
                copyButton.setText("Copy Details");
                copyButton.setDisable(false);
            });
            pause.play();
        });
        
        detailsContent.getChildren().add(copyButton);
        
        TitledPane detailsPane = new TitledPane("Technical Details", detailsContent);
        detailsPane.setExpanded(false);
        
        return detailsPane;
    }
    
    private void addButtons() {
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(closeButton);
        
        if (errorMessage.recoverable()) {
            ButtonType retryButton = new ButtonType("Retry", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().add(retryButton);
            
            // Make retry the default button
            Button retryBtn = (Button) getDialogPane().lookupButton(retryButton);
            retryBtn.setDefaultButton(true);
        }
        
        // Add report button for critical errors
        if (errorMessage.severity() == ErrorContext.ErrorSeverity.CRITICAL) {
            ButtonType reportButton = new ButtonType("Report Issue", ButtonBar.ButtonData.HELP);
            getDialogPane().getButtonTypes().add(reportButton);
        }
    }
    
    private void styleDialog() {
        DialogPane dialogPane = getDialogPane();
        
        // Add CSS class based on severity
        String severityClass = switch (errorMessage.severity()) {
            case CRITICAL -> "error-dialog-critical";
            case HIGH -> "error-dialog-high";
            case MEDIUM -> "error-dialog-medium";
            case LOW -> "error-dialog-low";
        };
        
        dialogPane.getStyleClass().add(severityClass);
        dialogPane.getStyleClass().add("error-dialog");
        
        // Apply custom CSS if available
        java.net.URL css = getClass().getResource("/css/error-dialog.css");
        if (css != null) {
            dialogPane.getStylesheets().add(css.toExternalForm());
        }
    }
    
    private Image loadIcon() {
        String iconPath = switch (errorMessage.severity()) {
            case CRITICAL, HIGH -> ERROR_ICON_PATH;
            case MEDIUM -> WARNING_ICON_PATH;
            case LOW -> INFO_ICON_PATH;
        };
        
        try {
            return new Image(getClass().getResourceAsStream(iconPath));
        } catch (Exception e) {
            // Return a default icon or null
            return null;
        }
    }
    
    /**
     * Show an error dialog and wait for user response.
     */
    public static Optional<ButtonType> showError(ErrorMessage errorMessage, 
                                                 String technicalDetails, 
                                                 ErrorContext context) {
        ErrorDialog dialog = new ErrorDialog(errorMessage, technicalDetails, context);
        return dialog.showAndWait();
    }
    
    /**
     * Show a simple error dialog with minimal information.
     */
    public static void showSimpleError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}