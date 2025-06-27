package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.runner.events.BrobotEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEntryEvent;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LogViewerPanel extends BorderPane implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(LogViewerPanel.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MAX_LOG_ENTRIES = 10000;

    private final EventBus eventBus;
    private final IconRegistry iconRegistry;
    private final LogQueryService logQueryService;

    private final ObservableList<LogEntryViewModel> logEntries = FXCollections.observableArrayList();
    private final FilteredList<LogEntryViewModel> filteredLogs;
    private TableView<LogEntryViewModel> logTable;
    private LogEntryViewModel selectedLogEntry;

    private TextArea logDetailTextArea;
    private ImageView matchImageView;
    private StateVisualizationPanel stateVisualizationPanel;

    private TextField searchField;
    @Getter
    private ComboBox<String> logTypeFilter;
    @Getter
    private ComboBox<String> logLevelFilter;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private CheckBox autoScrollCheckBox;

    private final Consumer<BrobotEvent> eventHandler;
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    public LogViewerPanel(LogQueryService logQueryService, EventBus eventBus, IconRegistry iconRegistry) {
        this.logQueryService = logQueryService;
        this.eventBus = eventBus;
        this.iconRegistry = iconRegistry;
        this.filteredLogs = new FilteredList<>(logEntries);
        this.eventHandler = this::handleEvent;
        setupUI();
        setupEventListeners();
        refreshLogs();
    }

    private void setupUI() {
        setPadding(new Insets(10));
        setTop(createTopSection());
        setCenter(createMainContent());
        setBottom(createStatusBar());
    }

    private Node createMainContent() {
        setupLogTable();
        SplitPane splitPane = new SplitPane();
        VBox tableContainer = new VBox(5, createTableToolbar(), logTable);
        VBox.setVgrow(logTable, Priority.ALWAYS);
        splitPane.getItems().addAll(tableContainer, createDetailPanel());
        splitPane.setDividerPositions(0.65);
        return splitPane;
    }

    private VBox createTopSection() {
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(0, 0, 10, 0));
        Label titleLabel = new Label("Log Viewer");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("Search logs...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        HBox.setHgrow(searchField, Priority.ALWAYS);
        logTypeFilter = new ComboBox<>();
        logTypeFilter.getItems().add("All Types");
        for (LogEventType type : LogEventType.values()) {
            logTypeFilter.getItems().add(type.name());
        }
        logTypeFilter.setValue("All Types");
        logTypeFilter.setOnAction(e -> applyFilters());
        logLevelFilter = new ComboBox<>();
        logLevelFilter.getItems().addAll("All Levels", "INFO", "WARNING", "ERROR", "DEBUG");
        logLevelFilter.setValue("All Levels");
        logLevelFilter.setOnAction(e -> applyFilters());
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        autoScrollCheckBox = new CheckBox("Auto-scroll");
        autoScrollCheckBox.setSelected(true);
        Button clearFiltersButton = new Button("Clear Filters");
        clearFiltersButton.setOnAction(e -> clearFilters());
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

    private HBox createTableToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5, 0, 5, 0));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button exportButton = new Button("Export Logs");
        exportButton.setOnAction(e -> exportLogs());
        Button clearButton = new Button("Clear Logs");
        clearButton.setOnAction(e -> clearLogs());
        toolbar.getChildren().addAll(exportButton, clearButton);
        return toolbar;
    }

    private void setupLogTable() {
        logTable = new TableView<>();
        logTable.setPlaceholder(new Label("No logs available"));
        logTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(LogEntryViewModel item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("log-error", "log-warning", "log-debug");
                if (empty || item == null) {
                    setStyle("");
                } else {
                    switch (item.getLevel()) {
                        case "ERROR" -> getStyleClass().add("log-error");
                        case "WARNING" -> getStyleClass().add("log-warning");
                        case "DEBUG" -> getStyleClass().add("log-debug");
                    }
                }
            }
        });

        TableColumn<LogEntryViewModel, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(param -> param.getValue().timeProperty());
        timeColumn.setPrefWidth(180);

        TableColumn<LogEntryViewModel, String> levelColumn = new TableColumn<>("Level");
        levelColumn.setCellValueFactory(param -> param.getValue().levelProperty());
        levelColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    ImageView icon = switch (item) {
                        case "ERROR" -> iconRegistry.getIconView("error", 16);
                        case "WARNING" -> iconRegistry.getIconView("warning", 16);
                        default -> iconRegistry.getIconView("info", 16);
                    };
                    setGraphic(icon);
                }
            }
        });
        levelColumn.setPrefWidth(100);

        TableColumn<LogEntryViewModel, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(param -> param.getValue().typeProperty());
        typeColumn.setPrefWidth(120);

        TableColumn<LogEntryViewModel, String> messageColumn = new TableColumn<>("Message");
        messageColumn.setCellValueFactory(param -> param.getValue().messageProperty());
        messageColumn.setPrefWidth(400);

        logTable.getColumns().addAll(timeColumn, levelColumn, typeColumn, messageColumn);
        logTable.setItems(filteredLogs);

        logTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedLogEntry = newVal;
            updateDetailPanel(newVal);
        });
    }

    private VBox createDetailPanel() {
        VBox detailPanel = new VBox(10);
        detailPanel.setPadding(new Insets(0, 0, 0, 10));
        Label titleLabel = new Label("Log Details");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        logDetailTextArea = new TextArea();
        logDetailTextArea.setEditable(false);
        logDetailTextArea.setWrapText(true);
        VBox.setVgrow(logDetailTextArea, Priority.ALWAYS);
        stateVisualizationPanel = new StateVisualizationPanel();
        TabPane detailTabs = new TabPane();
        detailTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab textTab = new Tab("Details", logDetailTextArea);
        Tab stateTab = new Tab("State Visualization", stateVisualizationPanel);
        detailTabs.getTabs().addAll(textTab, stateTab);
        detailPanel.getChildren().addAll(titleLabel, detailTabs);
        return detailPanel;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        Label logCountLabel = new Label("Total Logs: 0");
        Label filteredCountLabel = new Label("Filtered: 0");
        logEntries.addListener((javafx.collections.ListChangeListener<LogEntryViewModel>) c -> {
            logCountLabel.setText("Total Logs: " + logEntries.size());
            filteredCountLabel.setText("Filtered: " + filteredLogs.size());
        });
        filteredLogs.predicateProperty().addListener((obs, oldVal, newVal) ->
                filteredCountLabel.setText("Filtered: " + filteredLogs.size())
        );
        statusBar.getChildren().addAll(logCountLabel, filteredCountLabel);
        return statusBar;
    }

    private void setupEventListeners() {
        eventBus.subscribe(BrobotEvent.EventType.LOG_MESSAGE, eventHandler);
        eventBus.subscribe(BrobotEvent.EventType.LOG_WARNING, eventHandler);
        eventBus.subscribe(BrobotEvent.EventType.LOG_ERROR, eventHandler);
    }

    private void handleEvent(BrobotEvent event) {
        Platform.runLater(() -> {
            if (event instanceof LogEntryEvent logEntryEvent) {
                if (logEntryEvent.getLogEntry() != null) addLogEntry(logEntryEvent.getLogEntry());
            } else if (event instanceof LogEvent logEvent) {
                addLogEntry(logEvent);
            }
        });
    }

    public void refreshLogs() {
        Platform.runLater(() -> {
            clearLogDisplay();
            List<LogData> recentLogs = logQueryService.getRecentLogs(200);
            List<LogEntryViewModel> newViewModels = new ArrayList<>();
            for (LogData logData : recentLogs) {
                newViewModels.add(new LogEntryViewModel(logData));
            }
            logEntries.addAll(newViewModels);
        });
    }

    private void clearLogDisplay() {
        if (logTable != null && logTable.getSelectionModel() != null) {
            logTable.getSelectionModel().clearSelection();
        }
        selectedLogEntry = null;
        updateDetailPanel(null);
        logEntries.clear();
    }

    void addLogEntry(LogData logData) {
        addLogEntryViewModel(new LogEntryViewModel(logData));
    }

    void addLogEntry(LogEvent logEvent) {
        addLogEntryViewModel(new LogEntryViewModel(logEvent));
    }

    private void addLogEntryViewModel(LogEntryViewModel viewModel) {
        logEntries.addFirst(viewModel);
        if (logEntries.size() > MAX_LOG_ENTRIES) {
            logEntries.removeLast();
        }
        if (autoScrollCheckBox.isSelected()) {
            logTable.scrollTo(0);
        }
    }

    private void updateDetailPanel(LogEntryViewModel viewModel) {
        if (viewModel == null) {
            logDetailTextArea.clear();
            if (stateVisualizationPanel != null) stateVisualizationPanel.clearStates();
            return;
        }
        logDetailTextArea.setText(viewModel.getDetailedText());
        if (stateVisualizationPanel != null) updateStateVisualization(viewModel.getRawLogData());
    }

    private void updateStateVisualization(LogData logData) {
        if (logData == null) {
            stateVisualizationPanel.clearStates();
            return;
        }
        if (logData.getType() == LogEventType.TRANSITION) {
            List<String> fromStates = new ArrayList<>();
            if (logData.getFromStates() != null) fromStates.add(logData.getFromStates());
            stateVisualizationPanel.setStates(fromStates, logData.getToStateNames());
        } else if (logData.getCurrentStateName() != null) {
            stateVisualizationPanel.setCurrentState(logData.getCurrentStateName());
        } else {
            stateVisualizationPanel.clearStates();
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String selectedType = logTypeFilter.getValue();
        String selectedLevel = logLevelFilter.getValue();
        java.time.LocalDate startDate = startDatePicker.getValue();
        java.time.LocalDate endDate = endDatePicker.getValue();

        Predicate<LogEntryViewModel> predicate = entry -> {
            if (!searchText.isEmpty() && !entry.getMessage().toLowerCase().contains(searchText) && !entry.getType().toLowerCase().contains(searchText)) {
                return false;
            }
            if (selectedType != null && !"All Types".equals(selectedType) && !entry.getType().equals(selectedType)) {
                return false;
            }
            if (selectedLevel != null && !"All Levels".equals(selectedLevel) && !entry.getLevel().equals(selectedLevel)) {
                return false;
            }
            try {
                LocalDateTime entryTime = LocalDateTime.parse(entry.getTime(), TIME_FORMATTER);
                if (startDate != null && entryTime.toLocalDate().isBefore(startDate)) {
                    return false;
                }
                if (endDate != null && entryTime.toLocalDate().isAfter(endDate)) {
                    return false;
                }
            } catch (Exception e) {
                return true;
            }
            return true;
        };
        filteredLogs.setPredicate(predicate);
    }

    private void clearFilters() {
        searchField.clear();
        logTypeFilter.setValue("All Types");
        logLevelFilter.setValue("All Levels");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }

    private void clearLogs() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Logs");
        alert.setHeaderText("Clear all logs?");
        alert.setContentText("This will remove all logs from the viewer. This operation cannot be undone.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearLogDisplay();
            }
        });
    }

    protected void exportLogs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Logs");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            if (file.getName().toLowerCase().endsWith(".csv")) {
                exportLogsAsCSV(file);
            } else {
                exportLogsAsText(file);
            }
        }
    }

    void exportLogsAsText(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (LogEntryViewModel entry : filteredLogs) {
                writer.println(entry.getDetailedText());
                writer.println("----------------------------------------");
            }
            showExportSuccessMessage(file.getAbsolutePath());
        } catch (IOException e) {
            showExportErrorMessage(e.getMessage());
        }
    }

    void exportLogsAsCSV(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Time,Level,Type,Success,Message");
            for (LogEntryViewModel entry : filteredLogs) {
                writer.println(
                        "\"" + entry.getTime() + "\"," +
                                "\"" + entry.getLevel() + "\"," +
                                "\"" + entry.getType() + "\"," +
                                (entry.isSuccess() ? "Yes" : "No") + "," +
                                "\"" + entry.getMessage().replace("\"", "\"\"") + "\""
                );
            }
            showExportSuccessMessage(file.getAbsolutePath());
        } catch (IOException e) {
            showExportErrorMessage(e.getMessage());
        }
    }

    protected void showExportSuccessMessage(String filePath) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Logs exported to:\n" + filePath, ButtonType.OK);
        alert.setTitle("Export Successful");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    protected void showExportErrorMessage(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Error exporting logs:\n" + errorMessage, ButtonType.OK);
        alert.setTitle("Export Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @Override
    public void close() {
        eventBus.unsubscribe(BrobotEvent.EventType.LOG_MESSAGE, eventHandler);
        eventBus.unsubscribe(BrobotEvent.EventType.LOG_WARNING, eventHandler);
        eventBus.unsubscribe(BrobotEvent.EventType.LOG_ERROR, eventHandler);
        clearLogDisplay();
        if (logTable != null) logTable.setItems(null);
        if (filteredLogs != null) filteredLogs.setPredicate(null);
        logger.info("LogViewerPanel closed and all resources aggressively released.");
    }

    public static class LogEntryViewModel {
        private final SimpleStringProperty time = new SimpleStringProperty();
        private final SimpleStringProperty level = new SimpleStringProperty();
        private final SimpleStringProperty type = new SimpleStringProperty();
        private final SimpleStringProperty message = new SimpleStringProperty();
        private final SimpleBooleanProperty success = new SimpleBooleanProperty();
        @Getter @Setter
        private LogData rawLogData;

        public LogEntryViewModel() {}

        public LogEntryViewModel(LogData logData) {
            this.rawLogData = logData;
            LocalDateTime timestamp = LocalDateTime.ofInstant(logData.getTimestamp(), ZoneId.systemDefault());
            this.time.set(timestamp.format(TIME_FORMATTER));
            this.type.set(logData.getType() != null ? logData.getType().toString() : "UNKNOWN");
            this.message.set(logData.getDescription());
            this.success.set(logData.isSuccess());
            if (logData.getType() == LogEventType.ERROR) {
                this.level.set("ERROR");
            } else if (!logData.isSuccess()) {
                this.level.set("WARNING");
            } else {
                this.level.set("INFO");
            }
        }

        public LogEntryViewModel(LogEvent logEvent) {
            this.level.set(logEvent.getLevel().name());
            this.success.set(logEvent.getLevel() == LogEvent.LogLevel.INFO || logEvent.getLevel() == LogEvent.LogLevel.DEBUG);
            this.message.set(logEvent.getMessage());
            LogData tempLogData = new LogData();
            tempLogData.setSuccess(isSuccess());
            tempLogData.setDescription(getMessage());
            tempLogData.setTimestamp(logEvent.getTimestamp());
            if (logEvent.getException() != null) {
                tempLogData.setErrorMessage(logEvent.getException().toString());
            }
            this.rawLogData = tempLogData;
            try {
                this.type.set(LogEventType.valueOf(logEvent.getCategory().toUpperCase()).toString());
            } catch (Exception e) {
                this.type.set(LogEventType.SYSTEM.toString());
            }
        }

        public String getTime() { return time.get(); }
        public void setTime(String value) { time.set(value); }
        public SimpleStringProperty timeProperty() { return time; }

        public String getLevel() { return level.get(); }
        public void setLevel(String value) { level.set(value); }
        public SimpleStringProperty levelProperty() { return level; }

        public String getType() { return type.get(); }
        public void setType(String value) { type.set(value); }
        public SimpleStringProperty typeProperty() { return type; }

        public String getMessage() { return message.get(); }
        public void setMessage(String value) { message.set(value); }
        public SimpleStringProperty messageProperty() { return message; }

        public boolean isSuccess() { return success.get(); }
        public void setSuccess(boolean value) { success.set(value); }
        public SimpleBooleanProperty successProperty() { return success; }

        public String getDetailedText() {
            if (rawLogData == null) return "No raw data available.";
            StringBuilder sb = new StringBuilder();
            sb.append("Time: ").append(getTime()).append("\n");
            sb.append("Level: ").append(getLevel()).append("\n");
            sb.append("Type: ").append(getType()).append("\n");
            sb.append("Success: ").append(isSuccess()).append("\n\n");
            sb.append("Message: ").append(getMessage()).append("\n\n");
            if (rawLogData.getActionType() != null) sb.append("Action Type: ").append(rawLogData.getActionType()).append("\n");
            if (rawLogData.getErrorMessage() != null) sb.append("Error: ").append(rawLogData.getErrorMessage()).append("\n");
            if (rawLogData.getCurrentStateName() != null) {
                sb.append("Current State: ").append(rawLogData.getCurrentStateName()).append("\n");
            }
            if (rawLogData.getPerformance() != null && rawLogData.getPerformance().getActionDuration() > 0) {
                sb.append("Action Duration: ").append(rawLogData.getPerformance().getActionDuration()).append(" ms\n");
            }
            return sb.toString();
        }
    }

    static class StateVisualizationPanel extends VBox {
        private final Pane stateCanvas;
        final Label titleLabel;

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

        public void setStates(List<String> fromStates, List<String> toStates) {
            Platform.runLater(() -> {
                stateCanvas.getChildren().clear();
                titleLabel.setText("State Transition: " + String.join(", ", fromStates) + " → " + String.join(", ", toStates));
                double canvasWidth = stateCanvas.getWidth() > 0 ? stateCanvas.getWidth() : 600;
                double canvasHeight = stateCanvas.getHeight() > 0 ? stateCanvas.getHeight() : 200;
                drawStateGroup(fromStates, canvasWidth * 0.2, canvasHeight / 2);
                drawStateGroup(toStates, canvasWidth * 0.8, canvasHeight / 2);
                drawArrow(canvasWidth * 0.2 + 35, canvasHeight / 2, canvasWidth * 0.8 - 35, canvasHeight / 2);
            });
        }

        public void setCurrentState(String stateName) {
            Platform.runLater(() -> {
                stateCanvas.getChildren().clear();
                titleLabel.setText("Current State: " + stateName);
                double canvasWidth = stateCanvas.getWidth() > 0 ? stateCanvas.getWidth() : 600;
                double canvasHeight = stateCanvas.getHeight() > 0 ? stateCanvas.getHeight() : 200;
                drawState(stateName, canvasWidth / 2, canvasHeight / 2);
            });
        }

        public void clearStates() {
            Platform.runLater(() -> {
                stateCanvas.getChildren().clear();
                titleLabel.setText("State Visualization");
            });
        }

        private void drawStateGroup(List<String> stateNames, double x, double y) {
            if (stateNames == null || stateNames.isEmpty()) return;
            double spacing = 60;
            double startY = y - ((stateNames.size() - 1) * spacing / 2);
            for (int i = 0; i < stateNames.size(); i++) {
                drawState(stateNames.get(i), x, startY + i * spacing);
            }
        }

        private void drawState(String stateName, double x, double y) {
            Circle circle = new Circle(30, Color.LIGHTSKYBLUE);
            circle.setStroke(Color.STEELBLUE);
            circle.setStrokeWidth(2);
            Label label = new Label(stateName);
            label.setFont(Font.font("System", 12));
            StackPane stack = new StackPane(circle, label);
            stack.setLayoutX(x - 30);
            stack.setLayoutY(y - 30);
            stateCanvas.getChildren().add(stack);
        }

        private void drawArrow(double startX, double startY, double endX, double endY) {
            Line line = new Line(startX, startY, endX, endY);
            line.setStrokeWidth(2);
            double angle = Math.atan2(endY - startY, endX - startX);
            double arrowHeadSize = 10;
            Polygon arrowHead = new Polygon();
            arrowHead.getPoints().addAll(
                    endX, endY,
                    endX - arrowHeadSize * Math.cos(angle - Math.PI / 6), endY - arrowHeadSize * Math.sin(angle - Math.PI / 6),
                    endX - arrowHeadSize * Math.cos(angle + Math.PI / 6), endY - arrowHeadSize * Math.sin(angle + Math.PI / 6)
            );
            stateCanvas.getChildren().addAll(line, arrowHead);
        }
    }

    private static String csvEscape(String value) {
        if (value == null) return "";
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        if (needsQuotes) {
            return "\"" + escaped + "\"";
        }
        return value;
    }
}