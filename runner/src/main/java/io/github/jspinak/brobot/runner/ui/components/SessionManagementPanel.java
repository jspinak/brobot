package io.github.jspinak.brobot.runner.ui.components;

import atlantafx.base.theme.Styles;
import io.github.jspinak.brobot.runner.session.SessionManager;
import io.github.jspinak.brobot.runner.session.SessionSummary;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing sessions.
 * Extracted from ResourceMonitorPanel to improve modularity.
 */
public class SessionManagementPanel extends VBox {
    
    private final SessionManager sessionManager;
    private Label sessionStatusLabel;
    private TableView<SessionSummary> sessionsTable;
    private final ObservableList<SessionSummary> sessionsList = FXCollections.observableArrayList();
    private boolean initialized = false;
    
    public SessionManagementPanel(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        initialize();
    }
    
    private void initialize() {
        setSpacing(12);
        setPadding(new Insets(0));
        getStyleClass().add("session-management-panel");
        
        // Session status label
        sessionStatusLabel = new Label("No active session");
        sessionStatusLabel.getStyleClass().add(Styles.TEXT_MUTED);
        
        // Sessions table
        sessionsTable = createSessionsTable();
        
        // Control buttons
        HBox sessionButtons = createControlButtons();
        
        getChildren().addAll(sessionStatusLabel, sessionsTable, sessionButtons);
        
        // Load initial data only if sessionManager is available
        if (sessionManager != null) {
            refreshSessions();
        }
        initialized = true;
    }
    
    private TableView<SessionSummary> createSessionsTable() {
        TableView<SessionSummary> table = new TableView<>();
        table.setItems(sessionsList);
        table.setPrefHeight(200);
        table.getStyleClass().addAll("sessions-table", Styles.STRIPED);
        
        // ID Column
        TableColumn<SessionSummary, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getId().substring(0, 8) + "...")
        );
        idColumn.setPrefWidth(80);
        
        // Project Column
        TableColumn<SessionSummary, String> projectColumn = new TableColumn<>("Project");
        projectColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getProjectName())
        );
        projectColumn.setPrefWidth(120);
        
        // Start Time Column
        TableColumn<SessionSummary, String> startTimeColumn = new TableColumn<>("Start Time");
        startTimeColumn.setCellValueFactory(data -> {
            var startTime = data.getValue().getStartTime();
            String timeStr = startTime != null ?
                    startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) :
                    "Unknown";
            return new SimpleStringProperty(timeStr);
        });
        startTimeColumn.setPrefWidth(140);
        
        // Duration Column
        TableColumn<SessionSummary, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getFormattedDuration())
        );
        durationColumn.setPrefWidth(80);
        
        // Status Column
        TableColumn<SessionSummary, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStatus())
        );
        statusColumn.setPrefWidth(80);
        
        // Add all columns
        table.getColumns().addAll(idColumn, projectColumn, startTimeColumn, durationColumn, statusColumn);
        
        return table;
    }
    
    private HBox createControlButtons() {
        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_LEFT);
        
        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        refreshButton.setOnAction(e -> refreshSessions());
        
        Button restoreButton = new Button("Restore Selected");
        restoreButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        restoreButton.setOnAction(e -> restoreSelectedSession());
        restoreButton.disableProperty().bind(
            sessionsTable.getSelectionModel().selectedItemProperty().isNull()
        );
        
        Button deleteButton = new Button("Delete Selected");
        deleteButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
        deleteButton.setOnAction(e -> deleteSelectedSession());
        deleteButton.disableProperty().bind(
            sessionsTable.getSelectionModel().selectedItemProperty().isNull()
        );
        
        buttons.getChildren().addAll(refreshButton, restoreButton, deleteButton);
        
        return buttons;
    }
    
    /**
     * Update the session status display.
     */
    public void updateSessionStatus(String status) {
        sessionStatusLabel.setText(status);
    }
    
    /**
     * Refresh the sessions list from the session manager.
     */
    public void refreshSessions() {
        if (sessionManager == null) {
            return;
        }
        List<SessionSummary> sessions = sessionManager.getAllSessionSummaries();
        sessionsList.clear();
        sessionsList.addAll(sessions);
    }
    
    private void restoreSelectedSession() {
        SessionSummary selected = sessionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restore Session");
        alert.setHeaderText("Restore session " + selected.getId() + "?");
        alert.setContentText("This will end the current session and restore the selected one.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = sessionManager.restoreSession(selected.getId());
                if (success) {
                    showInfoDialog("Session Restored",
                            "Session " + selected.getId() + " has been restored successfully.");
                    refreshSessions();
                } else {
                    showErrorDialog("Restore Failed",
                            "Failed to restore session " + selected.getId());
                }
            }
        });
    }
    
    private void deleteSelectedSession() {
        SessionSummary selected = sessionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Session");
        alert.setHeaderText("Delete session " + selected.getId() + "?");
        alert.setContentText("This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = sessionManager.deleteSession(selected.getId());
                if (success) {
                    showInfoDialog("Session Deleted",
                            "Session " + selected.getId() + " has been deleted.");
                    refreshSessions();
                } else {
                    showErrorDialog("Delete Failed",
                            "Failed to delete session " + selected.getId());
                }
            }
        });
    }
    
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}