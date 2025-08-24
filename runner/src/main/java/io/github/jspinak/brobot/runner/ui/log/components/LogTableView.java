package io.github.jspinak.brobot.runner.ui.log.components;

import atlantafx.base.theme.Styles;
import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Custom TableView for displaying log entries with advanced features.
 * Includes filtering, sorting, and custom cell rendering.
 */
@Getter
public class LogTableView extends TableView<LogEntry> {
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final int MAX_MESSAGE_LENGTH = 200;
    
    private final ObservableList<LogEntry> allEntries = FXCollections.observableArrayList();
    private final FilteredList<LogEntry> filteredEntries;
    private final SortedList<LogEntry> sortedEntries;
    
    public LogTableView() {
        // Initialize filtered and sorted lists
        this.filteredEntries = new FilteredList<>(allEntries, p -> true);
        this.sortedEntries = new SortedList<>(filteredEntries);
        sortedEntries.comparatorProperty().bind(comparatorProperty());
        
        setItems(sortedEntries);
        setupColumns();
        setupStyling();
        setupBehavior();
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupColumns() {
        // Time column
        TableColumn<LogEntry, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getTimestamp().format(TIME_FORMAT)));
        timeColumn.setPrefWidth(100);
        timeColumn.setStyle("-fx-font-family: monospace;");
        
        // Level column with custom cell factory
        TableColumn<LogEntry, LogEntry.LogLevel> levelColumn = new TableColumn<>("Level");
        levelColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>(data.getValue().getLevel()));
        levelColumn.setCellFactory(createLevelCellFactory());
        levelColumn.setPrefWidth(80);
        
        // Type column
        TableColumn<LogEntry, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getType()));
        typeColumn.setPrefWidth(100);
        
        // Source column
        TableColumn<LogEntry, String> sourceColumn = new TableColumn<>("Source");
        sourceColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getSource()));
        sourceColumn.setPrefWidth(120);
        
        // Message column
        TableColumn<LogEntry, String> messageColumn = new TableColumn<>("Message");
        messageColumn.setCellValueFactory(data -> {
            String message = data.getValue().getMessage();
            if (message.length() > MAX_MESSAGE_LENGTH) {
                message = message.substring(0, MAX_MESSAGE_LENGTH) + "...";
            }
            return new SimpleStringProperty(message);
        });
        messageColumn.setPrefWidth(400);
        
        // Add all columns
        getColumns().addAll(timeColumn, levelColumn, typeColumn, sourceColumn, messageColumn);
        
        // Make message column grow
        messageColumn.prefWidthProperty().bind(
            widthProperty()
                .subtract(timeColumn.widthProperty())
                .subtract(levelColumn.widthProperty())
                .subtract(typeColumn.widthProperty())
                .subtract(sourceColumn.widthProperty())
                .subtract(20) // Scrollbar width
        );
    }
    
    /**
     * Creates a custom cell factory for the level column.
     */
    private Callback<TableColumn<LogEntry, LogEntry.LogLevel>, TableCell<LogEntry, LogEntry.LogLevel>> createLevelCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(LogEntry.LogLevel level, boolean empty) {
                super.updateItem(level, empty);
                
                if (empty || level == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Create level display with icon and color
                    HBox content = new HBox(4);
                    content.setAlignment(Pos.CENTER_LEFT);
                    
                    Label iconLabel = new Label(level.getIcon());
                    Label textLabel = new Label(level.getDisplayName());
                    
                    content.getChildren().addAll(iconLabel, textLabel);
                    
                    // Apply color based on level
                    String textColor = getLevelColor(level);
                    content.setStyle("-fx-text-fill: " + textColor + ";");
                    
                    setGraphic(content);
                    setText(null);
                }
            }
        };
    }
    
    /**
     * Gets the color for a log level.
     */
    private String getLevelColor(LogEntry.LogLevel level) {
        switch (level) {
            case TRACE:
                return "#9E9E9E"; // Gray
            case DEBUG:
                return "#757575"; // Dark Gray
            case INFO:
                return "#1976D2"; // Blue
            case WARNING:
                return "#F57C00"; // Orange
            case ERROR:
                return "#D32F2F"; // Red
            case FATAL:
                return "#B71C1C"; // Dark Red
            default:
                return "#000000"; // Black
        }
    }
    
    /**
     * Sets up table styling.
     */
    private void setupStyling() {
        getStyleClass().addAll(Styles.STRIPED, Styles.BORDERED);
        
        // Custom row factory for highlighting
        setRowFactory(tv -> {
            TableRow<LogEntry> row = new TableRow<>() {
                @Override
                protected void updateItem(LogEntry item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        // Highlight rows based on level
                        switch (item.getLevel()) {
                            case ERROR:
                            case FATAL:
                                setStyle("-fx-background-color: #ffebee;"); // Light red
                                break;
                            case WARNING:
                                setStyle("-fx-background-color: #fff3e0;"); // Light orange
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            };
            
            // Add context menu
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    showContextMenu(row.getItem(), event.getScreenX(), event.getScreenY());
                }
            });
            
            return row;
        });
    }
    
    /**
     * Sets up table behavior.
     */
    private void setupBehavior() {
        // Enable multiple selection
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Sort by timestamp descending by default
        getSortOrder().add(getColumns().get(0)); // Time column
        getColumns().get(0).setSortType(TableColumn.SortType.DESCENDING);
    }
    
    /**
     * Shows context menu for a log entry.
     */
    private void showContextMenu(LogEntry entry, double x, double y) {
        ContextMenu menu = new ContextMenu();
        
        MenuItem copyMessage = new MenuItem("Copy Message");
        copyMessage.setOnAction(e -> copyToClipboard(entry.getMessage()));
        
        MenuItem copyRow = new MenuItem("Copy Row");
        copyRow.setOnAction(e -> copyToClipboard(formatEntryAsText(entry)));
        
        MenuItem viewDetails = new MenuItem("View Details");
        viewDetails.setOnAction(e -> showDetailsDialog(entry));
        
        menu.getItems().addAll(copyMessage, copyRow, new SeparatorMenuItem(), viewDetails);
        
        if (entry.hasException()) {
            MenuItem copyStackTrace = new MenuItem("Copy Stack Trace");
            copyStackTrace.setOnAction(e -> copyToClipboard(entry.getExceptionStackTrace()));
            menu.getItems().add(copyStackTrace);
        }
        
        menu.show(this, x, y);
    }
    
    /**
     * Copies text to clipboard.
     */
    private void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }
    
    /**
     * Formats a log entry as text.
     */
    private String formatEntryAsText(LogEntry entry) {
        return String.format("[%s] [%s] [%s] [%s] %s",
            entry.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            entry.getLevel(),
            entry.getType(),
            entry.getSource(),
            entry.getMessage()
        );
    }
    
    /**
     * Shows a details dialog for a log entry.
     */
    private void showDetailsDialog(LogEntry entry) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Log Entry Details");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setPrefRowCount(20);
        detailsArea.setPrefColumnCount(80);
        
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(entry.getId()).append("\n");
        details.append("Timestamp: ").append(entry.getTimestamp()).append("\n");
        details.append("Level: ").append(entry.getLevel()).append("\n");
        details.append("Type: ").append(entry.getType()).append("\n");
        details.append("Source: ").append(entry.getSource()).append("\n");
        details.append("Message: ").append(entry.getMessage()).append("\n");
        
        if (entry.getDetails() != null) {
            details.append("\nDetails:\n").append(entry.getDetails()).append("\n");
        }
        
        if (entry.hasException()) {
            details.append("\nException Stack Trace:\n").append(entry.getExceptionStackTrace()).append("\n");
        }
        
        if (entry.hasMetadata()) {
            details.append("\nMetadata:\n");
            entry.getMetadata().forEach((key, value) -> 
                details.append("  ").append(key).append(": ").append(value).append("\n"));
        }
        
        detailsArea.setText(details.toString());
        dialog.getDialogPane().setContent(detailsArea);
        dialog.showAndWait();
    }
    
    /**
     * Adds a new log entry to the table.
     */
    public void addEntry(LogEntry entry) {
        allEntries.add(entry);
        
        // Auto-scroll to new entry if at bottom
        if (getSelectionModel().getSelectedItems().isEmpty()) {
            scrollTo(entry);
        }
    }
    
    /**
     * Adds multiple log entries.
     */
    public void addEntries(List<LogEntry> entries) {
        allEntries.addAll(entries);
    }
    
    /**
     * Clears all log entries.
     */
    public void clearEntries() {
        allEntries.clear();
    }
    
    /**
     * Sets the filter predicate.
     */
    public void setFilter(Predicate<LogEntry> filter) {
        filteredEntries.setPredicate(filter);
    }
    
    /**
     * Gets the current filter predicate.
     */
    public Predicate<? super LogEntry> getFilter() {
        return filteredEntries.getPredicate();
    }
    
    /**
     * Gets the total number of entries (before filtering).
     */
    public int getTotalEntries() {
        return allEntries.size();
    }
    
    /**
     * Gets the number of filtered entries.
     */
    public int getFilteredEntries() {
        return filteredEntries.size();
    }
    
    /**
     * Gets all entries (unfiltered).
     */
    public ObservableList<LogEntry> getAllEntries() {
        return FXCollections.unmodifiableObservableList(allEntries);
    }
    
    /**
     * Scrolls to a specific entry.
     */
    public void scrollToEntry(LogEntry entry) {
        int index = sortedEntries.indexOf(entry);
        if (index >= 0) {
            scrollTo(index);
            getSelectionModel().select(index);
        }
    }
}