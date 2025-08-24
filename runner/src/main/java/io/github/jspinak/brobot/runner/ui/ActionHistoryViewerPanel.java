package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.persistence.entity.ActionRecordEntity;
import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity;
import io.github.jspinak.brobot.runner.persistence.repository.ActionRecordRepository;
import io.github.jspinak.brobot.runner.service.ActionHistoryExportService;
import io.github.jspinak.brobot.runner.service.ActionRecordingService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

/**
 * JavaFX panel for viewing and managing action history.
 */
@Component
@FxmlView
@Slf4j
public class ActionHistoryViewerPanel extends VBox {
    
    @Autowired
    private ActionRecordingService recordingService;
    
    @Autowired
    private ActionRecordRepository recordRepository;
    
    @Autowired
    private ActionHistoryExportService exportService;
    
    // UI Components
    private ComboBox<RecordingSessionEntity> sessionSelector;
    private TableView<ActionRecordEntity> historyTable;
    private TextField filterField;
    private CheckBox successOnlyCheckbox;
    private Label sessionInfoLabel;
    private Button exportButton;
    private Button deleteButton;
    private Button refreshButton;
    
    // Data
    private ObservableList<ActionRecordEntity> tableData;
    private FilteredList<ActionRecordEntity> filteredData;
    
    public ActionHistoryViewerPanel() {
        initialize();
    }
    
    private void initialize() {
        setPadding(new Insets(10));
        setSpacing(10);
        
        // Title
        Label titleLabel = new Label("Action History Viewer");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Session selector
        HBox sessionBox = new HBox(10);
        sessionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label sessionLabel = new Label("Session:");
        sessionSelector = new ComboBox<>();
        sessionSelector.setConverter(new StringConverter<RecordingSessionEntity>() {
            @Override
            public String toString(RecordingSessionEntity session) {
                if (session == null) return "";
                return String.format("%s (%s)", session.getName(), 
                    session.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            
            @Override
            public RecordingSessionEntity fromString(String string) {
                return null;
            }
        });
        sessionSelector.setOnAction(e -> loadSession());
        
        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshSessions());
        
        sessionBox.getChildren().addAll(sessionLabel, sessionSelector, refreshButton);
        
        // Session info
        sessionInfoLabel = new Label("No session selected");
        sessionInfoLabel.setStyle("-fx-font-style: italic;");
        
        // Filter controls
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter:");
        filterField = new TextField();
        filterField.setPromptText("Type to filter...");
        filterField.textProperty().addListener((obs, old, text) -> applyFilters());
        
        successOnlyCheckbox = new CheckBox("Success only");
        successOnlyCheckbox.setOnAction(e -> applyFilters());
        
        filterBox.getChildren().addAll(filterLabel, filterField, successOnlyCheckbox);
        
        // History table
        historyTable = createHistoryTable();
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        
        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        exportButton = new Button("Export Session");
        exportButton.setDisable(true);
        exportButton.setOnAction(e -> exportSession());
        
        deleteButton = new Button("Delete Session");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> deleteSession());
        
        Button importButton = new Button("Import Session");
        importButton.setOnAction(e -> importSession());
        
        actionBox.getChildren().addAll(exportButton, deleteButton, importButton);
        
        // Add all components
        getChildren().addAll(
            titleLabel,
            new Separator(),
            sessionBox,
            sessionInfoLabel,
            new Separator(),
            filterBox,
            historyTable,
            new Separator(),
            actionBox
        );
        
        // Initialize data
        tableData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(tableData, p -> true);
        historyTable.setItems(filteredData);
        
        // Load sessions on startup
        Platform.runLater(this::refreshSessions);
    }
    
    private TableView<ActionRecordEntity> createHistoryTable() {
        TableView<ActionRecordEntity> table = new TableView<>();
        
        // Timestamp column
        TableColumn<ActionRecordEntity, String> timestampCol = new TableColumn<>("Time");
        timestampCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTimestamp()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))));
        timestampCol.setPrefWidth(100);
        
        // Action type column
        TableColumn<ActionRecordEntity, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("actionConfigType"));
        actionCol.setPrefWidth(150);
        
        // Success column
        TableColumn<ActionRecordEntity, Boolean> successCol = new TableColumn<>("Success");
        successCol.setCellValueFactory(new PropertyValueFactory<>("actionSuccess"));
        successCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean success, boolean empty) {
                super.updateItem(success, empty);
                if (empty || success == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(success ? "✓" : "✗");
                    setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
        successCol.setPrefWidth(60);
        
        // Duration column
        TableColumn<ActionRecordEntity, Long> durationCol = new TableColumn<>("Duration (ms)");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationCol.setPrefWidth(100);
        
        // State column
        TableColumn<ActionRecordEntity, String> stateCol = new TableColumn<>("State");
        stateCol.setCellValueFactory(new PropertyValueFactory<>("stateName"));
        stateCol.setPrefWidth(150);
        
        // Object column
        TableColumn<ActionRecordEntity, String> objectCol = new TableColumn<>("Object");
        objectCol.setCellValueFactory(new PropertyValueFactory<>("objectName"));
        objectCol.setPrefWidth(150);
        
        // Text column
        TableColumn<ActionRecordEntity, String> textCol = new TableColumn<>("Text");
        textCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        textCol.setPrefWidth(200);
        
        table.getColumns().addAll(timestampCol, actionCol, successCol, 
                                  durationCol, stateCol, objectCol, textCol);
        
        // Enable selection
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Add context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem viewDetailsItem = new MenuItem("View Details");
        viewDetailsItem.setOnAction(e -> viewRecordDetails());
        contextMenu.getItems().add(viewDetailsItem);
        table.setContextMenu(contextMenu);
        
        return table;
    }
    
    private void refreshSessions() {
        List<RecordingSessionEntity> sessions = recordingService.getAllSessions();
        sessionSelector.setItems(FXCollections.observableArrayList(sessions));
        
        if (!sessions.isEmpty()) {
            sessionSelector.getSelectionModel().selectFirst();
            loadSession();
        }
    }
    
    private void loadSession() {
        RecordingSessionEntity selected = sessionSelector.getValue();
        
        if (selected == null) {
            tableData.clear();
            sessionInfoLabel.setText("No session selected");
            exportButton.setDisable(true);
            deleteButton.setDisable(true);
            return;
        }
        
        // Update session info
        sessionInfoLabel.setText(String.format(
            "Session: %s | Actions: %d | Success Rate: %.1f%% | Duration: %s",
            selected.getName(),
            selected.getTotalActions(),
            selected.getSuccessRate(),
            formatDuration(selected.getDuration())
        ));
        
        // Load records
        List<ActionRecordEntity> records = recordRepository.findBySessionId(selected.getId());
        tableData.clear();
        tableData.addAll(records);
        
        // Enable buttons
        exportButton.setDisable(false);
        deleteButton.setDisable(false);
        
        log.info("Loaded {} records for session: {}", records.size(), selected.getName());
    }
    
    private void applyFilters() {
        Predicate<ActionRecordEntity> filter = record -> true;
        
        // Text filter
        String filterText = filterField.getText();
        if (filterText != null && !filterText.isEmpty()) {
            String lowerFilter = filterText.toLowerCase();
            filter = filter.and(record -> 
                (record.getStateName() != null && record.getStateName().toLowerCase().contains(lowerFilter)) ||
                (record.getObjectName() != null && record.getObjectName().toLowerCase().contains(lowerFilter)) ||
                (record.getActionConfigType() != null && record.getActionConfigType().toLowerCase().contains(lowerFilter)) ||
                (record.getText() != null && record.getText().toLowerCase().contains(lowerFilter))
            );
        }
        
        // Success filter
        if (successOnlyCheckbox.isSelected()) {
            filter = filter.and(ActionRecordEntity::isActionSuccess);
        }
        
        filteredData.setPredicate(filter);
    }
    
    private void exportSession() {
        RecordingSessionEntity selected = sessionSelector.getValue();
        if (selected == null) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Session");
        fileChooser.setInitialFileName(selected.getName() + ".json");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        
        if (file != null) {
            try {
                ActionHistoryExportService.ExportFormat format = 
                    file.getName().endsWith(".csv") ? 
                    ActionHistoryExportService.ExportFormat.CSV : 
                    ActionHistoryExportService.ExportFormat.JSON;
                
                exportService.exportSessionToFile(selected.getId(), file, format);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Complete");
                alert.setHeaderText("Session exported successfully");
                alert.setContentText("File: " + file.getAbsolutePath());
                alert.showAndWait();
                
            } catch (IOException e) {
                log.error("Failed to export session", e);
                showError("Export failed: " + e.getMessage());
            }
        }
    }
    
    private void deleteSession() {
        RecordingSessionEntity selected = sessionSelector.getValue();
        if (selected == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Session");
        confirm.setHeaderText("Delete session: " + selected.getName());
        confirm.setContentText("This will permanently delete the session and all its records. Continue?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                recordingService.deleteSession(selected.getId());
                refreshSessions();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Session Deleted");
                alert.setHeaderText("Session deleted successfully");
                alert.showAndWait();
            }
        });
    }
    
    private void importSession() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Session");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        
        if (file != null) {
            try {
                String sessionName = "Imported_" + System.currentTimeMillis();
                RecordingSessionEntity imported = exportService.importToSession(file, sessionName);
                
                refreshSessions();
                
                // Select the imported session
                sessionSelector.setValue(imported);
                loadSession();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Import Complete");
                alert.setHeaderText("Session imported successfully");
                alert.setContentText("Session: " + imported.getName());
                alert.showAndWait();
                
            } catch (IOException e) {
                log.error("Failed to import session", e);
                showError("Import failed: " + e.getMessage());
            }
        }
    }
    
    private void viewRecordDetails() {
        ActionRecordEntity selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Action Record Details");
        alert.setHeaderText("Details for Action #" + selected.getId());
        
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setText(formatRecordDetails(selected));
        textArea.setPrefRowCount(15);
        textArea.setPrefColumnCount(50);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
    
    private String formatRecordDetails(ActionRecordEntity record) {
        StringBuilder sb = new StringBuilder();
        sb.append("Timestamp: ").append(record.getTimestamp()).append("\n");
        sb.append("Action Type: ").append(record.getActionConfigType()).append("\n");
        sb.append("Success: ").append(record.isActionSuccess()).append("\n");
        sb.append("Duration: ").append(record.getDuration()).append(" ms\n");
        sb.append("State: ").append(record.getStateName()).append("\n");
        sb.append("Object: ").append(record.getObjectName()).append("\n");
        
        if (record.getText() != null && !record.getText().isEmpty()) {
            sb.append("Text: ").append(record.getText()).append("\n");
        }
        
        if (record.getActionConfigJson() != null) {
            sb.append("\nAction Configuration:\n");
            sb.append(record.getActionConfigJson());
        }
        
        if (record.getMatches() != null && !record.getMatches().isEmpty()) {
            sb.append("\n\nMatches: ").append(record.getMatches().size()).append("\n");
            record.getMatches().forEach(match -> {
                sb.append(String.format("  - [%d,%d,%dx%d] Score: %.2f\n",
                    match.getX(), match.getY(), match.getWidth(), match.getHeight(),
                    match.getSimScore()));
            });
        }
        
        return sb.toString();
    }
    
    private String formatDuration(java.time.Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
}