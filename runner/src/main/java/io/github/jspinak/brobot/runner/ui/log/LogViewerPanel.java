package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEntryEvent;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * A comprehensive log viewer panel that displays, filters, and allows exporting logs.
 * Includes features for log filtering, searching, level indicators, auto-scroll,
 * state visualization, and image match display.
 */
public class LogViewerPanel extends BorderPane {
    private static final Logger logger = LoggerFactory.getLogger(LogViewerPanel.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MAX_LOG_ENTRIES = 10000; // Maximum number of log entries to keep in memory

    private final EventBus eventBus;
    private final IconRegistry iconRegistry;

    // UI Components
    private TableView<LogEntryViewModel> logTable;
    private FilteredList<LogEntryViewModel> filteredLogs;
    private final ObservableList<LogEntryViewModel> logEntries = FXCollections.observableArrayList();

    private VBox detailPanel;
    private TextArea logDetailTextArea;
    private ImageView matchImageView;
    private StateVisualizationPanel stateVisualizationPanel;

    // Search and filter components
    private TextField searchField;
    private ComboBox<String> logTypeFilter;
    private ComboBox<String> logLevelFilter;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private CheckBox autoScrollCheckBox;

    // State to keep track of the currently selected log entry
    private LogEntryViewModel selectedLogEntry;

    // Cache of loaded images
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    /**
     * Creates a new LogViewerPanel.
     *
     * @param eventBus The event bus for communication
     * @param iconRegistry The icon registry for icons
     */
    public LogViewerPanel(EventBus eventBus, IconRegistry iconRegistry) {
        this.eventBus = eventBus;
        this.iconRegistry = iconRegistry;

        setupUI();
        setupEventListeners();
    }

    /**
     * Sets up the UI components.
     */
    private void setupUI() {
        // Main layout with log table on the left and details on the right
        setStyle("-fx-background-color: #f4f4f4;");
        setPadding(new Insets(10));

        // Setup toolbar with filters and search
        VBox topSection = createTopSection();
        setTop(topSection);

        // Setup log table
        setupLogTable();

        // Create split pane to divide logs and details
        SplitPane splitPane = new SplitPane();
        VBox tableContainer = new VBox(createTableToolbar(), logTable);
        VBox.setVgrow(logTable, Priority.ALWAYS);

        // Setup detail panel
        detailPanel = createDetailPanel();

        // Add to split pane
        splitPane.getItems().addAll(tableContainer, detailPanel);
        splitPane.setDividerPositions(0.6);
        setCenter(splitPane);

        // Setup status bar
        HBox statusBar = createStatusBar();
        setBottom(statusBar);
    }

    /**
     * Creates the top section with filters and search.
     *
     * @return The top section VBox
     */
    private VBox createTopSection() {
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(0, 0, 10, 0));

        // Title
        Label titleLabel = new Label("Log Viewer");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Search and filter bar
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search logs...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Log type filter
        logTypeFilter = new ComboBox<>();
        logTypeFilter.setPromptText("Log Type");
        logTypeFilter.getItems().add("All Types");
        for (LogType type : LogType.values()) {
            logTypeFilter.getItems().add(type.name());
        }
        logTypeFilter.setValue("All Types");
        logTypeFilter.setOnAction(e -> applyFilters());

        // Log level filter
        logLevelFilter = new ComboBox<>();
        logLevelFilter.setPromptText("Log Level");
        logLevelFilter.getItems().addAll("All Levels", "INFO", "WARNING", "ERROR", "DEBUG");
        logLevelFilter.setValue("All Levels");
        logLevelFilter.setOnAction(e -> applyFilters());

        // Date filters
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Auto-scroll checkbox
        autoScrollCheckBox = new CheckBox("Auto-scroll");
        autoScrollCheckBox.setSelected(true);

        // Clear filters button
        Button clearFiltersButton = new Button("Clear Filters");
        clearFiltersButton.setOnAction(e -> clearFilters());

        // Add all components to the filter bar
        filterBar.getChildren().addAll(
                new Label("Search:"), searchField,
                new Label("Type:"), logTypeFilter,
                new Label("Level:"), logLevelFilter,
                new Label("From:"), startDatePicker,
                new Label("To:"), endDatePicker,
                autoScrollCheckBox,
                clearFiltersButton
        );

        topSection.getChildren().addAll(titleLabel, filterBar);
        return topSection;
    }

    /**
     * Creates the toolbar for the log table.
     *
     * @return The table toolbar HBox
     */
    private HBox createTableToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5, 0, 5, 0));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Export logs button
        Button exportButton = new Button("Export Logs");
        exportButton.setOnAction(e -> exportLogs());

        // Clear logs button
        Button clearButton = new Button("Clear Logs");
        clearButton.setOnAction(e -> clearLogs());

        toolbar.getChildren().addAll(exportButton, clearButton);
        return toolbar;
    }

    /**
     * Sets up the log table with columns and selection handling.
     */
    private void setupLogTable() {
        logTable = new TableView<>();
        logTable.setPlaceholder(new Label("No logs available"));

        // Set row factory for custom styling based on log level
        logTable.setRowFactory(tv -> new TableRow<LogEntryViewModel>() {
            @Override
            protected void updateItem(LogEntryViewModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    // Apply style based on log level
                    switch (item.getLevel()) {
                        case "ERROR":
                            setStyle("-fx-background-color: #ffdddd;");
                            break;
                        case "WARNING":
                            setStyle("-fx-background-color: #ffffcc;");
                            break;
                        case "DEBUG":
                            setStyle("-fx-background-color: #f0f0f0;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });

        // Time column
        TableColumn<LogEntryViewModel, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(param -> param.getValue().timeProperty());
        timeColumn.setPrefWidth(180);

        // Level column with icons
        TableColumn<LogEntryViewModel, String> levelColumn = new TableColumn<>("Level");
        levelColumn.setCellValueFactory(param -> param.getValue().levelProperty());
        levelColumn.setCellFactory(column -> new TableCell<LogEntryViewModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);

                    // Add icon based on level
                    ImageView icon = switch (item) {
                        case "ERROR" -> iconRegistry.getIconView("error", 16);
                        case "WARNING" -> iconRegistry.getIconView("warning", 16);
                        case "INFO" -> iconRegistry.getIconView("info", 16);
                        case "DEBUG" -> iconRegistry.getIconView("info", 16);
                        default -> null;
                    };

                    setGraphic(icon);
                }
            }
        });
        levelColumn.setPrefWidth(80);

        // Type column
        TableColumn<LogEntryViewModel, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(param -> param.getValue().typeProperty());
        typeColumn.setPrefWidth(120);

        // Message column
        TableColumn<LogEntryViewModel, String> messageColumn = new TableColumn<>("Message");
        messageColumn.setCellValueFactory(param -> param.getValue().messageProperty());
        messageColumn.setPrefWidth(400);

        // Success column
        TableColumn<LogEntryViewModel, Boolean> successColumn = new TableColumn<>("Success");
        successColumn.setCellValueFactory(param -> param.getValue().successProperty());
        successColumn.setCellFactory(column -> new TableCell<LogEntryViewModel, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item) {
                        setText("✓");
                        setTextFill(Color.GREEN);
                    } else {
                        setText("✗");
                        setTextFill(Color.RED);
                    }
                }
            }
        });
        successColumn.setPrefWidth(70);

        logTable.getColumns().addAll(timeColumn, levelColumn, typeColumn, messageColumn, successColumn);

        // Set up filtered list
        filteredLogs = new FilteredList<>(logEntries);
        logTable.setItems(filteredLogs);

        // Handle selection
        logTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedLogEntry = newVal;
                updateDetailPanel(newVal);
            }
        });

        VBox.setVgrow(logTable, Priority.ALWAYS);
    }

    /**
     * Creates the detail panel for showing detailed log information.
     *
     * @return The detail panel VBox
     */
    private VBox createDetailPanel() {
        VBox detailPanel = new VBox(10);
        detailPanel.setPadding(new Insets(10));

        // Title
        Label titleLabel = new Label("Log Details");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Log detail text area
        logDetailTextArea = new TextArea();
        logDetailTextArea.setEditable(false);
        logDetailTextArea.setWrapText(true);
        logDetailTextArea.setPrefHeight(200);
        VBox.setVgrow(logDetailTextArea, Priority.ALWAYS);

        // Match image section (initially hidden)
        VBox imageBox = new VBox(5);
        Label imageLabel = new Label("Match Image");
        matchImageView = new ImageView();
        matchImageView.setPreserveRatio(true);
        matchImageView.setFitWidth(300);
        imageBox.getChildren().addAll(imageLabel, matchImageView);
        imageBox.setVisible(false);

        // State visualization panel
        stateVisualizationPanel = new StateVisualizationPanel();

        // Add tabs for different detail views
        TabPane detailTabs = new TabPane();
        detailTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab textTab = new Tab("Text");
        textTab.setContent(logDetailTextArea);

        Tab imageTab = new Tab("Image");
        imageTab.setContent(imageBox);

        Tab stateTab = new Tab("State");
        stateTab.setContent(stateVisualizationPanel);

        detailTabs.getTabs().addAll(textTab, imageTab, stateTab);
        VBox.setVgrow(detailTabs, Priority.ALWAYS);

        detailPanel.getChildren().addAll(titleLabel, detailTabs);
        return detailPanel;
    }

    /**
     * Creates the status bar with statistics.
     *
     * @return The status bar HBox
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #eaeaea; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

        Label logCountLabel = new Label("Total Logs: 0");
        Label filteredCountLabel = new Label("Filtered: 0");

        // Keep log count updated
        logEntries.addListener((javafx.collections.ListChangeListener<LogEntryViewModel>) c -> {
            logCountLabel.setText("Total Logs: " + logEntries.size());
            filteredCountLabel.setText("Filtered: " + filteredLogs.size());
        });

        // Keep filtered count updated
        filteredLogs.predicateProperty().addListener((obs, oldVal, newVal) ->
                filteredCountLabel.setText("Filtered: " + filteredLogs.size())
        );

        statusBar.getChildren().addAll(logCountLabel, filteredCountLabel);
        return statusBar;
    }

    /**
     * Sets up event listeners for log events.
     */
    private void setupEventListeners() {
        eventBus.subscribe(LogEntryEvent.EventType.LOG_MESSAGE, this::handleLogEntryEvent);
        eventBus.subscribe(LogEvent.EventType.LOG_MESSAGE, this::handleLogEvent);
        eventBus.subscribe(LogEvent.EventType.LOG_WARNING, this::handleLogEvent);
        eventBus.subscribe(LogEvent.EventType.LOG_ERROR, this::handleLogEvent);
    }

    /**
     * Handles log entry events.
     *
     * @param event The log entry event
     */
    private void handleLogEntryEvent(io.github.jspinak.brobot.runner.events.BrobotEvent event) {
        if (event instanceof LogEntryEvent logEntryEvent) {
            LogEntry logEntry = logEntryEvent.getLogEntry();

            if (logEntry != null) {
                Platform.runLater(() -> {
                    addLogEntry(logEntry);
                });
            }
        }
    }

    /**
     * Handles log events.
     *
     * @param event The log event
     */
    private void handleLogEvent(io.github.jspinak.brobot.runner.events.BrobotEvent event) {
        if (event instanceof LogEvent logEvent) {

            Platform.runLater(() -> {
                LogEntryViewModel viewModel = new LogEntryViewModel();
                viewModel.setTime(LocalDateTime.now().format(TIME_FORMATTER));
                viewModel.setLevel(logEvent.getLevel().toString());
                viewModel.setType(logEvent.getCategory());
                viewModel.setMessage(logEvent.getMessage());
                viewModel.setSuccess(logEvent.getLevel() != LogEvent.LogLevel.ERROR &&
                        logEvent.getLevel() != LogEvent.LogLevel.WARNING);
                viewModel.setRawLogEntry(null); // No raw log entry for this type

                addLogEntryViewModel(viewModel);
            });
        }
    }

    /**
     * Adds a log entry to the table.
     *
     * @param logEntry The log entry to add
     */
    void addLogEntry(LogEntry logEntry) {
        LogEntryViewModel viewModel = new LogEntryViewModel();

        // Convert timestamp to formatted string
        LocalDateTime timestamp = LocalDateTime.ofInstant(
                logEntry.getTimestamp(),
                ZoneId.systemDefault()
        );
        viewModel.setTime(timestamp.format(TIME_FORMATTER));

        // Set log level based on type and success
        if (logEntry.getType() == LogType.ERROR) {
            viewModel.setLevel("ERROR");
        } else if (!logEntry.isSuccess() &&
                (logEntry.getType() == LogType.ACTION || logEntry.getType() == LogType.TRANSITION)) {
            viewModel.setLevel("WARNING");
        } else {
            viewModel.setLevel("INFO");
        }

        viewModel.setType(logEntry.getType() != null ? logEntry.getType().toString() : "UNKNOWN");
        viewModel.setMessage(logEntry.getDescription());
        viewModel.setSuccess(logEntry.isSuccess());
        viewModel.setRawLogEntry(logEntry); // Store the original log entry

        addLogEntryViewModel(viewModel);
    }

    /**
     * Adds a log entry view model to the table.
     *
     * @param viewModel The view model to add
     */
    private void addLogEntryViewModel(LogEntryViewModel viewModel) {
        // Add at the beginning of the list (newest first)
        logEntries.addFirst(viewModel);

        // Limit number of entries to prevent memory issues
        if (logEntries.size() > MAX_LOG_ENTRIES) {
            logEntries.remove(MAX_LOG_ENTRIES, logEntries.size());
        }

        // Auto-scroll to the top (newest) if enabled
        if (autoScrollCheckBox.isSelected()) {
            logTable.scrollTo(0);
        }
    }

    /**
     * Updates the detail panel with the selected log entry.
     *
     * @param viewModel The selected log entry view model
     */
    private void updateDetailPanel(LogEntryViewModel viewModel) {
        if (viewModel == null) {
            logDetailTextArea.clear();
            matchImageView.setImage(null);
            stateVisualizationPanel.clearStates();
            return;
        }

        // Update text details
        StringBuilder detailsBuilder = new StringBuilder();

        detailsBuilder.append("Time: ").append(viewModel.getTime()).append("\n");
        detailsBuilder.append("Level: ").append(viewModel.getLevel()).append("\n");
        detailsBuilder.append("Type: ").append(viewModel.getType()).append("\n");
        detailsBuilder.append("Success: ").append(viewModel.isSuccess() ? "Yes" : "No").append("\n\n");
        detailsBuilder.append("Message: ").append(viewModel.getMessage()).append("\n\n");

        // Add additional details if available
        LogEntry rawLogEntry = viewModel.getRawLogEntry();
        if (rawLogEntry != null) {
            detailsBuilder.append("Additional Details:\n");

            if (rawLogEntry.getActionType() != null) {
                detailsBuilder.append("Action Type: ").append(rawLogEntry.getActionType()).append("\n");
            }

            if (rawLogEntry.getErrorMessage() != null) {
                detailsBuilder.append("Error: ").append(rawLogEntry.getErrorMessage()).append("\n");
            }

            if (rawLogEntry.getCurrentStateName() != null) {
                detailsBuilder.append("Current State: ").append(rawLogEntry.getCurrentStateName()).append("\n");
            }

            if (rawLogEntry.getFromStates() != null) {
                detailsBuilder.append("From States: ").append(rawLogEntry.getFromStates()).append("\n");
            }

            if (rawLogEntry.getToStateNames() != null && !rawLogEntry.getToStateNames().isEmpty()) {
                detailsBuilder.append("To States: ").append(String.join(", ", rawLogEntry.getToStateNames())).append("\n");
            }

            if (rawLogEntry.getScreenshotPath() != null) {
                detailsBuilder.append("Screenshot: ").append(rawLogEntry.getScreenshotPath()).append("\n");
            }

            if (rawLogEntry.getPerformance() != null) {
                detailsBuilder.append("\nPerformance Metrics:\n");
                detailsBuilder.append("Action Duration: ").append(rawLogEntry.getPerformance().getActionDuration()).append(" ms\n");
                detailsBuilder.append("Page Load Time: ").append(rawLogEntry.getPerformance().getPageLoadTime()).append(" ms\n");
                detailsBuilder.append("Transition Time: ").append(rawLogEntry.getPerformance().getTransitionTime()).append(" ms\n");
                detailsBuilder.append("Total Test Duration: ").append(rawLogEntry.getPerformance().getTotalTestDuration()).append(" ms\n");
            }

            // Set state visualization
            updateStateVisualization(rawLogEntry);

            // Try to load image if screenshot path is available
            if (rawLogEntry.getScreenshotPath() != null) {
                loadImage(rawLogEntry.getScreenshotPath());
            }
        }

        logDetailTextArea.setText(detailsBuilder.toString());
    }

    /**
     * Updates the state visualization panel based on the log entry.
     *
     * @param logEntry The log entry
     */
    private void updateStateVisualization(LogEntry logEntry) {
        if (logEntry == null) {
            stateVisualizationPanel.clearStates();
            return;
        }

        // For state transitions
        if (logEntry.getType() == LogType.TRANSITION) {
            List<String> fromStates = new ArrayList<>();
            List<String> toStates = new ArrayList<>();

            // Add from states
            if (logEntry.getFromStates() != null) {
                fromStates.add(logEntry.getFromStates());
            }

            // Add to states
            if (logEntry.getToStateNames() != null) {
                toStates.addAll(logEntry.getToStateNames());
            }

            stateVisualizationPanel.setStates(fromStates, toStates);
        }
        // For current state
        else if (logEntry.getCurrentStateName() != null) {
            stateVisualizationPanel.setCurrentState(logEntry.getCurrentStateName());
        }
        // Otherwise, clear
        else {
            stateVisualizationPanel.clearStates();
        }
    }

    /**
     * Loads an image from the specified path.
     *
     * @param imagePath The path to the image
     */
    private void loadImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            matchImageView.setImage(null);
            return;
        }

        // Check cache first
        if (imageCache.containsKey(imagePath)) {
            matchImageView.setImage(imageCache.get(imagePath));
            return;
        }

        // Try to load image
        try {
            Path path = Paths.get(imagePath);
            if (Files.exists(path)) {
                Image image = new Image(path.toUri().toString());
                imageCache.put(imagePath, image);
                matchImageView.setImage(image);
            } else {
                matchImageView.setImage(null);
            }
        } catch (Exception e) {
            logger.error("Error loading image: " + imagePath, e);
            matchImageView.setImage(null);
        }
    }

    /**
     * Applies filters to the log entries.
     */
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String selectedType = logTypeFilter.getValue();
        String selectedLevel = logLevelFilter.getValue();
        java.time.LocalDate startDate = startDatePicker.getValue();
        java.time.LocalDate endDate = endDatePicker.getValue();

        Predicate<LogEntryViewModel> predicate = entry -> {
            // Apply search text filter
            boolean matchesSearch = searchText.isEmpty() ||
                    entry.getMessage().toLowerCase().contains(searchText) ||
                    entry.getType().toLowerCase().contains(searchText);

            // Apply type filter
            boolean matchesType = "All Types".equals(selectedType) ||
                    entry.getType().equals(selectedType);

            // Apply level filter
            boolean matchesLevel = "All Levels".equals(selectedLevel) ||
                    entry.getLevel().equals(selectedLevel);

            // Apply date filters
            boolean matchesDateRange = true;

            if (startDate != null || endDate != null) {
                // Parse the date from the time field
                try {
                    LocalDateTime entryTime = LocalDateTime.parse(entry.getTime(), TIME_FORMATTER);

                    if (startDate != null) {
                        matchesDateRange = !entryTime.toLocalDate().isBefore(startDate);
                    }

                    if (endDate != null) {
                        matchesDateRange = matchesDateRange &&
                                !entryTime.toLocalDate().isAfter(endDate);
                    }
                } catch (Exception e) {
                    logger.error("Error parsing date for filtering", e);
                }
            }

            return matchesSearch && matchesType && matchesLevel && matchesDateRange;
        };

        filteredLogs.setPredicate(predicate);
    }

    /**
     * Clears all filters.
     */
    private void clearFilters() {
        searchField.clear();
        logTypeFilter.setValue("All Types");
        logLevelFilter.setValue("All Levels");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);

        filteredLogs.setPredicate(null);
    }

    /**
     * Clears all logs.
     */
    private void clearLogs() {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Logs");
        alert.setHeaderText("Clear all logs?");
        alert.setContentText("This will remove all logs from the viewer. This operation cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logEntries.clear();
                imageCache.clear();
                logDetailTextArea.clear();
                matchImageView.setImage(null);
                stateVisualizationPanel.clearStates();
            }
        });
    }

    /**
     * Exports logs to a file.
     */
    protected void exportLogs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Logs");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        fileChooser.setInitialFileName("logs_export_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            // Determine export format based on extension
            String filename = file.getName().toLowerCase();
            if (filename.endsWith(".csv")) {
                exportLogsAsCSV(file);
            } else {
                exportLogsAsText(file);
            }
        }
    }

    /**
     * Exports logs as a text file.
     *
     * @param file The file to export to
     */
    void exportLogsAsText(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            System.out.println("Writing to file: " + file.getAbsolutePath());
            System.out.println("File exists before writing? " + file.exists());
            // Use the current filtered logs or all logs?
            List<LogEntryViewModel> logsToExport = filteredLogs.filtered(item -> true);

            writer.println("Log Export - " + LocalDateTime.now().format(TIME_FORMATTER));
            writer.println("Total Logs: " + logsToExport.size());
            writer.println("--------------------------------------------------------------------------------");
            writer.println();

            for (LogEntryViewModel entry : logsToExport) {
                writer.println("Time: " + entry.getTime());
                writer.println("Level: " + entry.getLevel());
                writer.println("Type: " + entry.getType());
                writer.println("Success: " + (entry.isSuccess() ? "Yes" : "No"));
                writer.println("Message: " + entry.getMessage());

                LogEntry rawLogEntry = entry.getRawLogEntry();
                if (rawLogEntry != null) {
                    if (rawLogEntry.getActionType() != null) {
                        writer.println("Action Type: " + rawLogEntry.getActionType());
                    }

                    if (rawLogEntry.getErrorMessage() != null) {
                        writer.println("Error: " + rawLogEntry.getErrorMessage());
                    }

                    if (rawLogEntry.getCurrentStateName() != null) {
                        writer.println("Current State: " + rawLogEntry.getCurrentStateName());
                    }

                    if (rawLogEntry.getPerformance() != null) {
                        writer.println("Action Duration: " + rawLogEntry.getPerformance().getActionDuration() + " ms");
                    }
                }

                writer.println("--------------------------------------------------------------------------------");
            }

            showExportSuccessMessage(file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error exporting logs as text", e);
            showExportErrorMessage(e.getMessage());
        }
    }

    /**
     * Exports logs as a CSV file.
     *
     * @param file The file to export to
     */
    void exportLogsAsCSV(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Use the current filtered logs or all logs?
            List<LogEntryViewModel> logsToExport = filteredLogs.filtered(item -> true);

            // Write CSV header
            writer.println("Time,Level,Type,Success,Message");

            // Write each log entry
            for (LogEntryViewModel entry : logsToExport) {
                writer.println(
                        csvEscape(entry.getTime()) + "," +
                                csvEscape(entry.getLevel()) + "," +
                                csvEscape(entry.getType()) + "," +
                                (entry.isSuccess() ? "Yes" : "No") + "," +
                                csvEscape(entry.getMessage())
                );
            }

            showExportSuccessMessage(file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error exporting logs as CSV", e);
            showExportErrorMessage(e.getMessage());
        }
    }

    /**
     * Escapes a string for CSV format.
     *
     * @param input The input string
     * @return The escaped string
     */
    private String csvEscape(String input) {
        if (input == null) {
            return "";
        }

        // If the input contains a comma, double quote, or newline, wrap it in quotes
        boolean needsQuoting = input.contains(",") || input.contains("\"") || input.contains("\n") || input.contains("\r");

        if (needsQuoting) {
            // Replace double quotes with two double quotes
            String escaped = input.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }

        return input;
    }

    /**
     * Shows a success message for log export.
     *
     * @param filePath The path to the exported file
     */
    void showExportSuccessMessage(String filePath) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Successful");
        alert.setHeaderText("Logs exported successfully");
        alert.setContentText("Exported to: " + filePath);
        alert.showAndWait();
    }

    /**
     * Shows an error message for log export.
     *
     * @param errorMessage The error message
     */
    void showExportErrorMessage(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Export Error");
        alert.setHeaderText("Failed to export logs");
        alert.setContentText("Error: " + errorMessage);
        alert.showAndWait();
    }

    /**
     * View model for a log entry in the table.
     */
    public static class LogEntryViewModel {
        private final javafx.beans.property.StringProperty time = new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.StringProperty level = new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.StringProperty type = new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.StringProperty message = new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.BooleanProperty success = new SimpleBooleanProperty();
        @Setter
        @Getter
        private LogEntry rawLogEntry;

        public String getTime() { return time.get(); }
        public void setTime(String value) { time.set(value); }
        public javafx.beans.property.StringProperty timeProperty() { return time; }

        public String getLevel() { return level.get(); }
        public void setLevel(String value) { level.set(value); }
        public javafx.beans.property.StringProperty levelProperty() { return level; }

        public String getType() { return type.get(); }
        public void setType(String value) { type.set(value); }
        public javafx.beans.property.StringProperty typeProperty() { return type; }

        public String getMessage() { return message.get(); }
        public void setMessage(String value) { message.set(value); }
        public javafx.beans.property.StringProperty messageProperty() { return message; }

        public boolean isSuccess() { return success.get(); }
        public void setSuccess(boolean value) { success.set(value); }
        public javafx.beans.property.BooleanProperty successProperty() { return success; }

    }

    /**
     * Panel for visualizing state transitions.
     */
    static class StateVisualizationPanel extends VBox {
        private final Pane stateCanvas;
        private final Label titleLabel;

        public StateVisualizationPanel() {
            setPadding(new Insets(10));
            setSpacing(10);

            titleLabel = new Label("State Visualization");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            stateCanvas = new Pane();
            stateCanvas.setMinHeight(200);
            stateCanvas.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-color: white;");

            getChildren().addAll(titleLabel, stateCanvas);
        }

        /**
         * Sets the from and to states for visualization.
         *
         * @param fromStates The from states
         * @param toStates The to states
         */
        public void setStates(List<String> fromStates, List<String> toStates) {
            stateCanvas.getChildren().clear();

            // Show title with transition info
            titleLabel.setText("State Transition: " +
                    String.join(", ", fromStates) + " → " +
                    String.join(", ", toStates));

            double canvasWidth = stateCanvas.getWidth();
            double canvasHeight = stateCanvas.getHeight();

            // Ensure we have minimum dimensions
            if (canvasWidth < 50) canvasWidth = 300;
            if (canvasHeight < 50) canvasHeight = 200;

            // Draw from states on the left
            drawStateGroup(fromStates, 50, canvasHeight / 2, "From States");

            // Draw to states on the right
            drawStateGroup(toStates, canvasWidth - 50, canvasHeight / 2, "To States");

            // Draw arrow
            drawArrow(100, canvasHeight / 2, canvasWidth - 100, canvasHeight / 2);
        }

        /**
         * Sets the current state for visualization.
         *
         * @param stateName The current state name
         */
        public void setCurrentState(String stateName) {
            stateCanvas.getChildren().clear();

            titleLabel.setText("Current State: " + stateName);

            double canvasWidth = stateCanvas.getWidth();
            double canvasHeight = stateCanvas.getHeight();

            // Ensure we have minimum dimensions
            if (canvasWidth < 50) canvasWidth = 300;
            if (canvasHeight < 50) canvasHeight = 200;

            // Draw the state in the center
            drawState(stateName, canvasWidth / 2, canvasHeight / 2);
        }

        /**
         * Clears all states from the visualization.
         */
        public void clearStates() {
            stateCanvas.getChildren().clear();
            titleLabel.setText("State Visualization");
        }

        /**
         * Draws a group of states.
         *
         * @param stateNames The state names
         * @param x The x coordinate
         * @param y The y coordinate
         * @param groupTitle The group title
         */
        private void drawStateGroup(List<String> stateNames, double x, double y, String groupTitle) {
            if (stateNames.isEmpty()) {
                return;
            }

            // Draw group title
            Label title = new Label(groupTitle);
            title.setFont(Font.font("System", FontWeight.BOLD, 12));
            title.setLayoutX(x - 50);
            title.setLayoutY(y - 80);
            stateCanvas.getChildren().add(title);

            // Draw each state
            double spacing = 60;
            double startY = y - ((stateNames.size() - 1) * spacing / 2);

            for (int i = 0; i < stateNames.size(); i++) {
                drawState(stateNames.get(i), x, startY + i * spacing);
            }
        }

        /**
         * Draws a single state.
         *
         * @param stateName The state name
         * @param x The x coordinate
         * @param y The y coordinate
         */
        private void drawState(String stateName, double x, double y) {
            // Draw circle
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(30);
            circle.setFill(Color.LIGHTSKYBLUE);
            circle.setStroke(Color.STEELBLUE);
            circle.setStrokeWidth(2);
            circle.setCenterX(x);
            circle.setCenterY(y);

            // Draw text
            Label label = new Label(stateName);
            label.setFont(Font.font("System", 12));
            label.setTextFill(Color.BLACK);
            double labelWidth = stateName.length() * 7; // Rough estimate of text width
            label.setLayoutX(x - labelWidth / 2);
            label.setLayoutY(y - 10);

            stateCanvas.getChildren().addAll(circle, label);
        }

        /**
         * Draws an arrow between two points.
         *
         * @param startX The start x coordinate
         * @param startY The start y coordinate
         * @param endX The end x coordinate
         * @param endY The end y coordinate
         */
        private void drawArrow(double startX, double startY, double endX, double endY) {
            // Draw line
            javafx.scene.shape.Line line = new javafx.scene.shape.Line(startX, startY, endX, endY);
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(2);

            // Draw arrowhead
            double arrowLength = 15;
            double arrowWidth = 7;
            double angle = Math.atan2(endY - startY, endX - startX);

            javafx.scene.shape.Polygon arrowHead = new javafx.scene.shape.Polygon();
            arrowHead.getPoints().addAll(
                    endX, endY,
                    endX - arrowLength * Math.cos(angle - Math.PI/6), endY - arrowLength * Math.sin(angle - Math.PI/6),
                    endX - arrowLength * Math.cos(angle + Math.PI/6), endY - arrowLength * Math.sin(angle + Math.PI/6)
            );
            arrowHead.setFill(Color.BLACK);

            stateCanvas.getChildren().addAll(line, arrowHead);
        }
    }
}