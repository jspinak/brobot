package io.github.jspinak.brobot.runner.ui.illustration.export;

import io.github.jspinak.brobot.runner.persistence.entities.IllustrationEntity;
import io.github.jspinak.brobot.runner.ui.illustration.gallery.IllustrationGalleryService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UI panel for exporting and sharing illustrations.
 * <p>
 * This panel provides a user-friendly interface for:
 * <ul>
 * <li>Selecting illustrations to export</li>
 * <li>Choosing export format and options</li>
 * <li>Configuring export settings</li>
 * <li>Monitoring export progress</li>
 * </ul>
 *
 * @see IllustrationExportService
 * @see IllustrationGalleryService
 */
@Component
public class IllustrationExportPanel extends BorderPane {
    
    private final IllustrationExportService exportService;
    private final IllustrationGalleryService galleryService;
    
    // UI Components
    private ComboBox<String> sessionCombo;
    private ListView<IllustrationItem> illustrationList;
    private ComboBox<IllustrationExportService.ExportFormat> formatCombo;
    private TextField outputPathField;
    private CheckBox includeMetadataCheck;
    private CheckBox includeImagesCheck;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Button exportButton;
    
    @Autowired
    public IllustrationExportPanel(IllustrationExportService exportService,
                                  IllustrationGalleryService galleryService) {
        this.exportService = exportService;
        this.galleryService = galleryService;
        
        setupUI();
        loadSessions();
    }
    
    /**
     * Sets up the UI layout.
     */
    private void setupUI() {
        // Header
        Label headerLabel = new Label("Export Illustrations");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(15));
        topSection.getChildren().addAll(
            headerLabel,
            new Separator(),
            createSessionSelector(),
            createIllustrationSelector()
        );
        
        VBox bottomSection = new VBox(10);
        bottomSection.setPadding(new Insets(15));
        bottomSection.getChildren().addAll(
            createExportOptions(),
            createProgressSection(),
            createActionButtons()
        );
        
        setTop(topSection);
        setCenter(bottomSection);
        
        getStyleClass().add("export-panel");
    }
    
    /**
     * Creates the session selector section.
     */
    private HBox createSessionSelector() {
        HBox selector = new HBox(10);
        selector.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label("Session:");
        sessionCombo = new ComboBox<>();
        sessionCombo.setPrefWidth(300);
        sessionCombo.setOnAction(e -> loadIllustrations());
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadSessions());
        
        selector.getChildren().addAll(label, sessionCombo, refreshButton);
        
        return selector;
    }
    
    /**
     * Creates the illustration selector section.
     */
    private VBox createIllustrationSelector() {
        VBox selector = new VBox(5);
        
        Label label = new Label("Select Illustrations:");
        
        illustrationList = new ListView<>();
        illustrationList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        illustrationList.setCellFactory(lv -> new IllustrationCell());
        illustrationList.setPrefHeight(200);
        
        HBox controls = new HBox(10);
        Button selectAllButton = new Button("Select All");
        selectAllButton.setOnAction(e -> illustrationList.getSelectionModel().selectAll());
        
        Button deselectAllButton = new Button("Deselect All");
        deselectAllButton.setOnAction(e -> illustrationList.getSelectionModel().clearSelection());
        
        Label selectionLabel = new Label("0 selected");
        illustrationList.getSelectionModel().getSelectedItems().addListener(
            (javafx.collections.ListChangeListener<IllustrationItem>) change -> {
                selectionLabel.setText(illustrationList.getSelectionModel().getSelectedItems().size() + " selected");
                updateExportButton();
            }
        );
        
        controls.getChildren().addAll(selectAllButton, deselectAllButton, selectionLabel);
        
        selector.getChildren().addAll(label, illustrationList, controls);
        
        return selector;
    }
    
    /**
     * Creates the export options section.
     */
    private GridPane createExportOptions() {
        GridPane options = new GridPane();
        options.setHgap(10);
        options.setVgap(10);
        
        // Format selection
        Label formatLabel = new Label("Export Format:");
        formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll(IllustrationExportService.ExportFormat.values());
        formatCombo.setValue(IllustrationExportService.ExportFormat.ZIP);
        formatCombo.setOnAction(e -> updateExportOptions());
        
        // Output path
        Label pathLabel = new Label("Output Path:");
        outputPathField = new TextField();
        outputPathField.setPrefWidth(300);
        
        Button browseButton = new Button("Browse...");
        browseButton.setOnAction(e -> browseOutputPath());
        
        HBox pathBox = new HBox(5);
        pathBox.getChildren().addAll(outputPathField, browseButton);
        
        // Options
        includeMetadataCheck = new CheckBox("Include Metadata");
        includeMetadataCheck.setSelected(true);
        
        includeImagesCheck = new CheckBox("Embed Images");
        includeImagesCheck.setSelected(true);
        
        // Layout
        options.add(formatLabel, 0, 0);
        options.add(formatCombo, 1, 0);
        options.add(pathLabel, 0, 1);
        options.add(pathBox, 1, 1);
        options.add(includeMetadataCheck, 0, 2);
        options.add(includeImagesCheck, 1, 2);
        
        return options;
    }
    
    /**
     * Creates the progress section.
     */
    private VBox createProgressSection() {
        VBox progress = new VBox(5);
        
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        
        statusLabel = new Label("Ready to export");
        statusLabel.setStyle("-fx-text-fill: #666;");
        
        progress.getChildren().addAll(progressBar, statusLabel);
        
        return progress;
    }
    
    /**
     * Creates the action buttons.
     */
    private HBox createActionButtons() {
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        exportButton = new Button("Export");
        exportButton.setDefaultButton(true);
        exportButton.setDisable(true);
        exportButton.setOnAction(e -> performExport());
        
        Button shareButton = new Button("Share");
        shareButton.setOnAction(e -> shareIllustrations());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> getScene().getWindow().hide());
        
        buttons.getChildren().addAll(exportButton, shareButton, cancelButton);
        
        return buttons;
    }
    
    /**
     * Loads available sessions.
     */
    private void loadSessions() {
        // In a real implementation, this would query unique sessions
        sessionCombo.getItems().clear();
        sessionCombo.getItems().add("Current Session");
        // Add more sessions from database
    }
    
    /**
     * Loads illustrations for the selected session.
     */
    private void loadIllustrations() {
        String session = sessionCombo.getValue();
        if (session == null) return;
        
        List<IllustrationEntity> illustrations = galleryService.getSessionIllustrations(session);
        
        illustrationList.getItems().clear();
        illustrations.forEach(ill -> 
            illustrationList.getItems().add(new IllustrationItem(ill))
        );
    }
    
    /**
     * Updates export options based on selected format.
     */
    private void updateExportOptions() {
        IllustrationExportService.ExportFormat format = formatCombo.getValue();
        
        if (format == null) return;
        
        switch (format) {
            case MARKDOWN:
                includeImagesCheck.setDisable(false);
                includeImagesCheck.setText("Embed Images");
                break;
                
            case PDF:
                includeImagesCheck.setDisable(false);
                includeImagesCheck.setText("Include Images");
                break;
                
            case ZIP:
                includeImagesCheck.setDisable(true);
                includeMetadataCheck.setDisable(false);
                break;
                
            default:
                includeImagesCheck.setDisable(true);
                includeMetadataCheck.setDisable(true);
        }
    }
    
    /**
     * Updates export button state.
     */
    private void updateExportButton() {
        boolean hasSelection = !illustrationList.getSelectionModel().getSelectedItems().isEmpty();
        boolean hasPath = outputPathField.getText() != null && !outputPathField.getText().trim().isEmpty();
        
        exportButton.setDisable(!hasSelection || !hasPath);
    }
    
    /**
     * Browses for output path.
     */
    private void browseOutputPath() {
        IllustrationExportService.ExportFormat format = formatCombo.getValue();
        
        if (format == IllustrationExportService.ExportFormat.ZIP ||
            format == IllustrationExportService.ExportFormat.PDF ||
            format == IllustrationExportService.ExportFormat.MARKDOWN) {
            
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Export Location");
            chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                    format.getDescription(),
                    "*" + format.getExtension()
                )
            );
            
            File file = chooser.showSaveDialog(getScene().getWindow());
            if (file != null) {
                outputPathField.setText(file.getAbsolutePath());
                updateExportButton();
            }
        } else {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Export Directory");
            
            File dir = chooser.showDialog(getScene().getWindow());
            if (dir != null) {
                outputPathField.setText(dir.getAbsolutePath());
                updateExportButton();
            }
        }
    }
    
    /**
     * Performs the export operation.
     */
    private void performExport() {
        List<IllustrationItem> selected = illustrationList.getSelectionModel().getSelectedItems();
        String outputPath = outputPathField.getText();
        IllustrationExportService.ExportFormat format = formatCombo.getValue();
        
        if (selected.isEmpty() || outputPath == null || format == null) {
            return;
        }
        
        // Create export task
        Task<Path> exportTask = new Task<>() {
            @Override
            protected Path call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Starting export...");
                
                List<Long> ids = selected.stream()
                    .map(item -> item.getEntity().getId())
                    .collect(Collectors.toList());
                
                Path result = null;
                
                switch (format) {
                    case ZIP:
                        result = exportService.exportBatch(ids, outputPath);
                        break;
                        
                    case MARKDOWN:
                        String sessionId = sessionCombo.getValue();
                        result = exportService.exportAsMarkdown(
                            sessionId, outputPath, includeImagesCheck.isSelected()
                        );
                        break;
                        
                    case PDF:
                        List<IllustrationEntity> entities = selected.stream()
                            .map(IllustrationItem::getEntity)
                            .collect(Collectors.toList());
                        result = exportService.exportAsPdf(entities, Paths.get(outputPath));
                        break;
                        
                    default:
                        if (selected.size() == 1) {
                            result = exportService.exportSingle(
                                selected.get(0).getEntity().getId(),
                                format,
                                outputPath
                            );
                        }
                }
                
                updateProgress(1, 1);
                updateMessage("Export completed!");
                
                return result;
            }
        };
        
        // Bind UI to task
        progressBar.progressProperty().bind(exportTask.progressProperty());
        statusLabel.textProperty().bind(exportTask.messageProperty());
        progressBar.setVisible(true);
        exportButton.setDisable(true);
        
        // Handle completion
        exportTask.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            exportButton.setDisable(false);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Complete");
            alert.setHeaderText("Export Successful");
            alert.setContentText("Exported to: " + exportTask.getValue());
            alert.showAndWait();
        });
        
        exportTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            exportButton.setDisable(false);
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText("Export Error");
            alert.setContentText("Failed to export: " + exportTask.getException().getMessage());
            alert.showAndWait();
        });
        
        // Start task
        new Thread(exportTask).start();
    }
    
    /**
     * Shares selected illustrations.
     */
    private void shareIllustrations() {
        String sessionId = sessionCombo.getValue();
        if (sessionId == null) return;
        
        String shareLink = exportService.createShareableLink(sessionId);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Share Illustrations");
        alert.setHeaderText("Shareable Link Created");
        alert.setContentText("Share this link:\n" + shareLink);
        
        // Add copy button
        ButtonType copyButton = new ButtonType("Copy Link");
        alert.getButtonTypes().add(copyButton);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == copyButton) {
                // Copy to clipboard
                javafx.scene.input.Clipboard clipboard = 
                    javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = 
                    new javafx.scene.input.ClipboardContent();
                content.putString(shareLink);
                clipboard.setContent(content);
            }
        });
    }
    
    /**
     * Wrapper for illustration list items.
     */
    private static class IllustrationItem {
        private final IllustrationEntity entity;
        
        public IllustrationItem(IllustrationEntity entity) {
            this.entity = entity;
        }
        
        public IllustrationEntity getEntity() {
            return entity;
        }
        
        @Override
        public String toString() {
            return String.format("%s - %s (%s)",
                entity.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                entity.getActionType(),
                entity.isSuccess() ? "Success" : "Failed"
            );
        }
    }
    
    /**
     * Custom cell for illustration items.
     */
    private static class IllustrationCell extends ListCell<IllustrationItem> {
        @Override
        protected void updateItem(IllustrationItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.toString());
                setTextFill(item.getEntity().isSuccess() ? 
                    javafx.scene.paint.Color.GREEN : 
                    javafx.scene.paint.Color.RED);
            }
        }
    }
}