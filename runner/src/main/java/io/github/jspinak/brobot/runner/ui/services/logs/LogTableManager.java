package io.github.jspinak.brobot.runner.ui.services.logs;

import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the log table view configuration and behavior.
 */
@Slf4j
@Getter
public class LogTableManager {
    
    private final TableView<LogEntryViewModel> logTable;
    private final Map<String, Color> levelColors = new ConcurrentHashMap<>();
    
    public LogTableManager() {
        this.logTable = new TableView<>();
        initializeLevelColors();
        setupTable();
    }
    
    private void initializeLevelColors() {
        levelColors.put("ERROR", Color.web("#dc3545"));
        levelColors.put("WARNING", Color.web("#ffc107"));
        levelColors.put("INFO", Color.web("#17a2b8"));
        levelColors.put("DEBUG", Color.web("#6c757d"));
        levelColors.put("SUCCESS", Color.web("#28a745"));
    }
    
    private void setupTable() {
        logTable.getStyleClass().add("log-table");
        logTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Create columns
        TableColumn<LogEntryViewModel, String> timeColumn = createTimeColumn();
        TableColumn<LogEntryViewModel, String> levelColumn = createLevelColumn();
        TableColumn<LogEntryViewModel, String> typeColumn = createTypeColumn();
        TableColumn<LogEntryViewModel, String> messageColumn = createMessageColumn();
        
        // Add columns
        logTable.getColumns().addAll(timeColumn, levelColumn, typeColumn, messageColumn);
        
        // Configure sizing
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private TableColumn<LogEntryViewModel, String> createTimeColumn() {
        TableColumn<LogEntryViewModel, String> column = new TableColumn<>("Time");
        column.setCellValueFactory(new PropertyValueFactory<>("time"));
        column.setPrefWidth(80);
        column.setMinWidth(80);
        column.setMaxWidth(100);
        column.getStyleClass().add("time-column");
        return column;
    }
    
    private TableColumn<LogEntryViewModel, String> createLevelColumn() {
        TableColumn<LogEntryViewModel, String> column = new TableColumn<>("Level");
        column.setCellValueFactory(new PropertyValueFactory<>("level"));
        column.setPrefWidth(80);
        column.setMinWidth(60);
        column.setMaxWidth(100);
        column.getStyleClass().add("level-column");
        
        // Custom cell factory for level indicators
        column.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final Circle indicator = new Circle(4);
            
            @Override
            protected void updateItem(String level, boolean empty) {
                super.updateItem(level, empty);
                
                if (empty || level == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(level);
                    indicator.setFill(levelColors.getOrDefault(level, Color.GRAY));
                    setGraphic(indicator);
                    setGraphicTextGap(8);
                }
            }
        });
        
        return column;
    }
    
    private TableColumn<LogEntryViewModel, String> createTypeColumn() {
        TableColumn<LogEntryViewModel, String> column = new TableColumn<>("Type");
        column.setCellValueFactory(new PropertyValueFactory<>("type"));
        column.setPrefWidth(120);
        column.setMinWidth(100);
        column.setMaxWidth(150);
        column.getStyleClass().add("type-column");
        return column;
    }
    
    private TableColumn<LogEntryViewModel, String> createMessageColumn() {
        TableColumn<LogEntryViewModel, String> column = new TableColumn<>("Message");
        column.setCellValueFactory(new PropertyValueFactory<>("message"));
        column.setMinWidth(200);
        column.getStyleClass().add("message-column");
        return column;
    }
    
    /**
     * Sets the items for the table.
     */
    public void setItems(ObservableList<LogEntryViewModel> items) {
        logTable.setItems(items);
    }
    
    /**
     * Gets the selected log entry.
     */
    public LogEntryViewModel getSelectedItem() {
        return logTable.getSelectionModel().getSelectedItem();
    }
    
    /**
     * Scrolls to the top of the table.
     */
    public void scrollToTop() {
        if (logTable.getItems().size() > 0) {
            logTable.scrollTo(0);
        }
    }
    
    /**
     * Scrolls to the bottom of the table.
     */
    public void scrollToBottom() {
        int size = logTable.getItems().size();
        if (size > 0) {
            logTable.scrollTo(size - 1);
        }
    }
    
    /**
     * Clears the selection.
     */
    public void clearSelection() {
        logTable.getSelectionModel().clearSelection();
    }
    
    /**
     * Refreshes the table view.
     */
    public void refresh() {
        logTable.refresh();
    }
}