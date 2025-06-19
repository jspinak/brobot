package io.github.jspinak.brobot.runner.ui.execution;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.model.PerformanceMetricsData;
import io.github.jspinak.brobot.runner.automation.AutomationExecutor;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.ExecutionStatusEvent;
import io.github.jspinak.brobot.runner.events.LogEntryEvent;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Comprehensive dashboard for execution control and monitoring.
 * Provides controls for execution flow, status visualization, performance metrics,
 * and state transition monitoring.
 */
public class ExecutionDashboardPanel extends BorderPane {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionDashboardPanel.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Core dependencies
    private final EventBus eventBus;
    private final AutomationExecutor automationExecutor;
    private final StateTransitionsRepository stateTransitionsRepository;
    private final AllStatesInProjectService allStatesInProjectService;

    // UI components
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Label currentActionLabel;
    private Label elapsedTimeLabel;
    private Label currentStateLabel;
    private Circle statusIndicator;

    // Charts and data visualizations
    private LineChart<Number, Number> performanceChart;
    private XYChart.Series<Number, Number> actionDurationSeries;
    private XYChart.Series<Number, Number> matchTimeSeries;

    // State transition components
    private TableView<StateTransitionRecord> stateTransitionTable;
    private TableView<ActionRecord> actionHistoryTable;

    // Performance metrics
    private Label totalActionsLabel;
    private Label avgActionDurationLabel;
    private Label successRateLabel;
    private Label peakMemoryLabel;

    // Data tracking
    private final Queue<PerformanceMetric> performanceMetrics = new ConcurrentLinkedQueue<>();
    private final ObservableList<StateTransitionRecord> stateTransitions = FXCollections.observableArrayList();
    private final ObservableList<ActionRecord> actionHistory = FXCollections.observableArrayList();
    private final AtomicLong totalActions = new AtomicLong(0);
    private final AtomicLong successfulActions = new AtomicLong(0);
    private LocalDateTime executionStartTime;
    private Timeline elapsedTimeUpdater;
    private final ObjectProperty<State> currentState = new SimpleObjectProperty<>();

    /**
     * Creates a new ExecutionDashboardPanel.
     *
     * @param eventBus The event bus for communication
     * @param automationExecutor The automation executor for controlling execution
     * @param stateTransitionsRepository Repository for state transitions
     * @param allStatesInProjectService Service for accessing all states in project
     */
    public ExecutionDashboardPanel(EventBus eventBus,
                                   AutomationExecutor automationExecutor,
                                   StateTransitionsRepository stateTransitionsRepository,
                                   AllStatesInProjectService allStatesInProjectService) {
        this.eventBus = eventBus;
        this.automationExecutor = automationExecutor;
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.allStatesInProjectService = allStatesInProjectService;

        setupUI();
        setupEventHandlers();
    }

    /**
     * Sets up the UI components of the dashboard.
     */
    private void setupUI() {
        // Main layout structure
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(15));

        // Control panel section
        HBox controlPanel = createControlPanel();

        // Status panel
        VBox statusPanel = createStatusPanel();

        // Performance metrics panel
        TitledPane performancePane = createPerformanceMetricsPanel();

        // State transition panel
        TitledPane stateTransitionPane = createStateTransitionPanel();

        // Action history panel
        TitledPane actionHistoryPane = createActionHistoryPanel();

        // Add components to main layout
        mainLayout.getChildren().addAll(
                controlPanel,
                statusPanel,
                performancePane,
                stateTransitionPane,
                actionHistoryPane
        );

        // Set scroll capability for the entire dashboard
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        setCenter(scrollPane);
    }

    /**
     * Creates the control panel with play, pause, and stop buttons.
     */
    private HBox createControlPanel() {
        HBox controlPanel = new HBox(10);
        controlPanel.setAlignment(Pos.CENTER_LEFT);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label titleLabel = new Label("Execution Control");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        playButton = new Button("▶ Play");
        playButton.getStyleClass().add("button-primary");
        playButton.setOnAction(e -> resumeExecution());

        pauseButton = new Button("⏸ Pause");
        pauseButton.setOnAction(e -> pauseExecution());

        stopButton = new Button("⏹ Stop");
        stopButton.getStyleClass().add("button-danger");
        stopButton.setOnAction(e -> stopExecution());

        // Initial button states
        updateControlButtonStates(ExecutionState.IDLE);

        controlPanel.getChildren().addAll(titleLabel, spacer, playButton, pauseButton, stopButton);
        return controlPanel;
    }

    /**
     * Creates the status panel with progress bar, status indicator, and current state.
     */
    private VBox createStatusPanel() {
        VBox statusPanel = new VBox(10);
        statusPanel.setPadding(new Insets(10));
        statusPanel.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #ddd; -fx-border-radius: 5;");

        // Status indicator and label
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        statusIndicator = new Circle(8);
        statusIndicator.setFill(Color.LIGHTGRAY);

        statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        elapsedTimeLabel = new Label("00:00:00");
        elapsedTimeLabel.setFont(Font.font("Monospaced", 14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusRow.getChildren().addAll(statusIndicator, statusLabel, spacer, new Label("Elapsed Time:"), elapsedTimeLabel);

        // Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);

        // Current action and state
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(5);

        infoGrid.add(new Label("Current Action:"), 0, 0);
        currentActionLabel = new Label("None");
        infoGrid.add(currentActionLabel, 1, 0);

        infoGrid.add(new Label("Current State:"), 0, 1);
        currentStateLabel = new Label("None");
        infoGrid.add(currentStateLabel, 1, 1);

        GridPane.setHgrow(currentActionLabel, Priority.ALWAYS);
        GridPane.setHgrow(currentStateLabel, Priority.ALWAYS);

        statusPanel.getChildren().addAll(statusRow, progressBar, infoGrid);
        return statusPanel;
    }

    /**
     * Creates the performance metrics panel with charts and statistics.
     */
    private TitledPane createPerformanceMetricsPanel() {
        VBox performancePanel = new VBox(15);
        performancePanel.setPadding(new Insets(10));

        // Performance metrics summary
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(20);
        metricsGrid.setVgap(5);
        metricsGrid.setPadding(new Insets(10));
        metricsGrid.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ddd; -fx-border-radius: 5;");

        metricsGrid.add(new Label("Total Actions:"), 0, 0);
        totalActionsLabel = new Label("0");
        metricsGrid.add(totalActionsLabel, 1, 0);

        metricsGrid.add(new Label("Avg Action Duration:"), 0, 1);
        avgActionDurationLabel = new Label("0 ms");
        metricsGrid.add(avgActionDurationLabel, 1, 1);

        metricsGrid.add(new Label("Success Rate:"), 2, 0);
        successRateLabel = new Label("0%");
        metricsGrid.add(successRateLabel, 3, 0);

        metricsGrid.add(new Label("Peak Memory:"), 2, 1);
        peakMemoryLabel = new Label("0 MB");
        metricsGrid.add(peakMemoryLabel, 3, 1);

        // Performance chart
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        performanceChart = new LineChart<>(xAxis, yAxis);
        performanceChart.setTitle("Performance Metrics");
        performanceChart.setAnimated(false);
        performanceChart.setCreateSymbols(false);
        performanceChart.setPrefHeight(200);

        xAxis.setLabel("Action #");
        yAxis.setLabel("Time (ms)");

        actionDurationSeries = new XYChart.Series<>();
        actionDurationSeries.setName("Action Duration");

        matchTimeSeries = new XYChart.Series<>();
        matchTimeSeries.setName("Match Time");

        performanceChart.getData().addAll(actionDurationSeries, matchTimeSeries);

        performancePanel.getChildren().addAll(metricsGrid, performanceChart);

        TitledPane performancePane = new TitledPane("Performance Metrics", performancePanel);
        performancePane.setCollapsible(true);
        performancePane.setExpanded(true);

        return performancePane;
    }

    /**
     * Creates the state transition panel with a table of state transitions.
     */
    private TitledPane createStateTransitionPanel() {
        VBox transitionPanel = new VBox(10);
        transitionPanel.setPadding(new Insets(10));

        stateTransitionTable = new TableView<>();
        stateTransitionTable.setPlaceholder(new Label("No state transitions recorded"));

        TableColumn<StateTransitionRecord, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime()));

        TableColumn<StateTransitionRecord, String> fromStateColumn = new TableColumn<>("From State");
        fromStateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFromState()));

        TableColumn<StateTransitionRecord, String> toStateColumn = new TableColumn<>("To State");
        toStateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getToState()));

        TableColumn<StateTransitionRecord, String> durationColumn = new TableColumn<>("Duration (ms)");
        durationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuration()));

        TableColumn<StateTransitionRecord, String> successColumn = new TableColumn<>("Success");
        successColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isSuccess() ? "Yes" : "No"));

        stateTransitionTable.getColumns().addAll(timeColumn, fromStateColumn, toStateColumn, durationColumn, successColumn);
        stateTransitionTable.setItems(stateTransitions);

        VBox.setVgrow(stateTransitionTable, Priority.ALWAYS);
        transitionPanel.getChildren().add(stateTransitionTable);

        TitledPane stateTransitionPane = new TitledPane("State Transitions", transitionPanel);
        stateTransitionPane.setCollapsible(true);
        stateTransitionPane.setExpanded(true);

        return stateTransitionPane;
    }

    /**
     * Creates the action history panel with a table of actions.
     */
    private TitledPane createActionHistoryPanel() {
        VBox actionPanel = new VBox(10);
        actionPanel.setPadding(new Insets(10));

        actionHistoryTable = new TableView<>();
        actionHistoryTable.setPlaceholder(new Label("No actions recorded"));

        TableColumn<ActionRecord, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime()));

        TableColumn<ActionRecord, String> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAction()));

        TableColumn<ActionRecord, String> targetColumn = new TableColumn<>("Target");
        targetColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTarget()));

        TableColumn<ActionRecord, String> durationColumn = new TableColumn<>("Duration (ms)");
        durationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuration()));

        TableColumn<ActionRecord, String> resultColumn = new TableColumn<>("Result");
        resultColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResult()));

        actionHistoryTable.getColumns().addAll(timeColumn, actionColumn, targetColumn, durationColumn, resultColumn);
        actionHistoryTable.setItems(actionHistory);

        VBox.setVgrow(actionHistoryTable, Priority.ALWAYS);
        actionPanel.getChildren().add(actionHistoryTable);

        TitledPane actionHistoryPane = new TitledPane("Action History", actionPanel);
        actionHistoryPane.setCollapsible(true);
        actionHistoryPane.setExpanded(true);

        return actionHistoryPane;
    }

    /**
     * Sets up event handlers for execution events and log entries.
     */
    private void setupEventHandlers() {
        // Listen for execution status events
        eventBus.subscribe(ExecutionStatusEvent.EventType.EXECUTION_STARTED, this::handleExecutionStarted);
        eventBus.subscribe(ExecutionStatusEvent.EventType.EXECUTION_PROGRESS, this::handleExecutionProgress);
        eventBus.subscribe(ExecutionStatusEvent.EventType.EXECUTION_COMPLETED, this::handleExecutionCompleted);
        eventBus.subscribe(ExecutionStatusEvent.EventType.EXECUTION_FAILED, this::handleExecutionFailed);
        eventBus.subscribe(ExecutionStatusEvent.EventType.EXECUTION_PAUSED, this::handleExecutionPaused);
        eventBus.subscribe(ExecutionStatusEvent.EventType.EXECUTION_RESUMED, this::handleExecutionResumed);
        eventBus.subscribe(ExecutionStatusEvent.EventType.EXECUTION_STOPPED, this::handleExecutionStopped);

        // Listen for log entry events to track actions and state transitions
        eventBus.subscribe(ExecutionStatusEvent.EventType.LOG_MESSAGE, this::handleLogEvent);

        // Set up periodic updates for memory usage
        setupMemoryMonitoring();
    }

    /**
     * Sets up periodic memory usage monitoring.
     */
    private void setupMemoryMonitoring() {
        Timeline memoryMonitor = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> updateMemoryUsage())
        );
        memoryMonitor.setCycleCount(Timeline.INDEFINITE);
        memoryMonitor.play();
    }

    /**
     * Updates the memory usage display.
     */
    private void updateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        Platform.runLater(() -> {
            peakMemoryLabel.setText(usedMemory + " MB");
        });
    }

    /**
     * Updates control button states based on execution state.
     */
    private void updateControlButtonStates(ExecutionState state) {
        Platform.runLater(() -> {
            switch (state) {
                case IDLE:
                case COMPLETED:
                case ERROR:
                case STOPPED:
                case TIMEOUT:
                    playButton.setDisable(true);
                    pauseButton.setDisable(true);
                    stopButton.setDisable(true);
                    break;

                case STARTING:
                case RUNNING:
                    playButton.setDisable(true);
                    pauseButton.setDisable(false);
                    stopButton.setDisable(false);
                    break;

                case PAUSED:
                    playButton.setDisable(false);
                    pauseButton.setDisable(true);
                    stopButton.setDisable(false);
                    break;

                case STOPPING:
                    playButton.setDisable(true);
                    pauseButton.setDisable(true);
                    stopButton.setDisable(true);
                    break;
            }
        });
    }

    /**
     * Updates the status indicator color based on execution state.
     */
    private void updateStatusIndicator(ExecutionState state) {
        Platform.runLater(() -> {
            switch (state) {
                case IDLE:
                    statusIndicator.setFill(Color.LIGHTGRAY);
                    break;
                case STARTING:
                case RUNNING:
                    statusIndicator.setFill(Color.GREEN);
                    break;
                case PAUSED:
                    statusIndicator.setFill(Color.ORANGE);
                    break;
                case STOPPING:
                    statusIndicator.setFill(Color.ORANGE);
                    break;
                case COMPLETED:
                    statusIndicator.setFill(Color.BLUE);
                    break;
                case ERROR:
                case TIMEOUT:
                    statusIndicator.setFill(Color.RED);
                    break;
                case STOPPED:
                    statusIndicator.setFill(Color.GRAY);
                    break;
            }
        });
    }

    /**
     * Starts the elapsed time updater.
     */
    private void startElapsedTimeUpdater() {
        executionStartTime = LocalDateTime.now();

        if (elapsedTimeUpdater != null) {
            elapsedTimeUpdater.stop();
        }

        elapsedTimeUpdater = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateElapsedTime())
        );
        elapsedTimeUpdater.setCycleCount(Timeline.INDEFINITE);
        elapsedTimeUpdater.play();
    }

    /**
     * Stops the elapsed time updater.
     */
    private void stopElapsedTimeUpdater() {
        if (elapsedTimeUpdater != null) {
            elapsedTimeUpdater.stop();
            elapsedTimeUpdater = null;
        }
    }

    /**
     * Updates the elapsed time display.
     */
    private void updateElapsedTime() {
        if (executionStartTime == null) return;

        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(executionStartTime, now);

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        Platform.runLater(() -> {
            elapsedTimeLabel.setText(timeString);
        });
    }

    /**
     * Resume execution if paused.
     */
    private void resumeExecution() {
        ExecutionStatus status = automationExecutor.getExecutionStatus();
        if (status.getState() == ExecutionState.PAUSED) {
            automationExecutor.resumeAutomation();
            // Event handling for this will update the UI
        }
    }

    /**
     * Pause execution if running.
     */
    private void pauseExecution() {
        ExecutionStatus status = automationExecutor.getExecutionStatus();
        if (status.getState() == ExecutionState.RUNNING || status.getState() == ExecutionState.STARTING) {
            automationExecutor.pauseAutomation();
            // Event handling for this will update the UI
        }
    }

    /**
     * Stop execution if running or paused.
     */
    private void stopExecution() {
        ExecutionStatus status = automationExecutor.getExecutionStatus();
        if (status.getState().isActive()) {
            automationExecutor.stopAllAutomation();
            // Event handling for this will update the UI
        }
    }

    /**
     * Handles execution started events.
     */
    private void handleExecutionStarted(io.github.jspinak.brobot.runner.events.BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        ExecutionStatus status = statusEvent.getStatus();

        Platform.runLater(() -> {
            // Reset state
            performanceMetrics.clear();
            actionDurationSeries.getData().clear();
            matchTimeSeries.getData().clear();
            stateTransitions.clear();
            actionHistory.clear();
            totalActions.set(0);
            successfulActions.set(0);

            // Update UI
            statusLabel.setText("Running");
            progressBar.setProgress(0);
            currentActionLabel.setText("Starting...");
            totalActionsLabel.setText("0");
            avgActionDurationLabel.setText("0 ms");
            successRateLabel.setText("0%");

            updateStatusIndicator(status.getState());
            updateControlButtonStates(status.getState());

            // Start elapsed time tracking
            startElapsedTimeUpdater();
        });
    }

    /**
     * Handles execution progress events.
     */
    private void handleExecutionProgress(io.github.jspinak.brobot.runner.events.BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        ExecutionStatus status = statusEvent.getStatus();

        Platform.runLater(() -> {
            progressBar.setProgress(status.getProgress());

            if (status.getCurrentOperation() != null) {
                currentActionLabel.setText(status.getCurrentOperation());
            }

            updateStatusIndicator(status.getState());
            updateControlButtonStates(status.getState());
        });
    }

    /**
     * Handles execution completed events.
     */
    private void handleExecutionCompleted(io.github.jspinak.brobot.runner.events.BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        ExecutionStatus status = statusEvent.getStatus();

        Platform.runLater(() -> {
            statusLabel.setText("Completed");
            progressBar.setProgress(1.0);
            currentActionLabel.setText("Done");

            updateStatusIndicator(status.getState());
            updateControlButtonStates(status.getState());

            // Stop elapsed time tracking
            stopElapsedTimeUpdater();
        });
    }

    /**
     * Handles execution failed events.
     */
    private void handleExecutionFailed(io.github.jspinak.brobot.runner.events.BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        ExecutionStatus status = statusEvent.getStatus();

        Platform.runLater(() -> {
            statusLabel.setText("Failed: " + (status.getError() != null ? status.getError().getMessage() : "Unknown error"));

            updateStatusIndicator(status.getState());
            updateControlButtonStates(status.getState());

            // Stop elapsed time tracking
            stopElapsedTimeUpdater();
        });
    }

    /**
     * Handles execution paused events.
     */
    private void handleExecutionPaused(io.github.jspinak.brobot.runner.events.BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        ExecutionStatus status = statusEvent.getStatus();

        Platform.runLater(() -> {
            statusLabel.setText("Paused");

            updateStatusIndicator(status.getState());
            updateControlButtonStates(status.getState());

            // Pause elapsed time tracking (but don't stop it)
            if (elapsedTimeUpdater != null) {
                elapsedTimeUpdater.pause();
            }
        });
    }

    /**
     * Handles execution resumed events.
     */
    private void handleExecutionResumed(io.github.jspinak.brobot.runner.events.BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        ExecutionStatus status = statusEvent.getStatus();

        Platform.runLater(() -> {
            statusLabel.setText("Running");

            updateStatusIndicator(status.getState());
            updateControlButtonStates(status.getState());

            // Resume elapsed time tracking
            if (elapsedTimeUpdater != null) {
                elapsedTimeUpdater.play();
            }
        });
    }

    /**
     * Handles execution stopped events.
     */
    private void handleExecutionStopped(io.github.jspinak.brobot.runner.events.BrobotEvent event) {
        ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
        ExecutionStatus status = statusEvent.getStatus();

        Platform.runLater(() -> {
            statusLabel.setText("Stopped");

            updateStatusIndicator(status.getState());
            updateControlButtonStates(status.getState());

            // Stop elapsed time tracking
            stopElapsedTimeUpdater();
        });
    }

    /**
     * Handles log events to extract action and state transition information.
     */
    private void handleLogEvent(io.github.jspinak.brobot.runner.events.BrobotEvent event) {
        if (event instanceof LogEvent logEvent) {
            // Process regular log event
            processLogEvent(logEvent);
        } else if (event instanceof LogEntryEvent logEntryEvent) {
            // Process log entry event (contains more detailed information)
            processLogEntryEvent(logEntryEvent);
        }
    }

    /**
     * Processes a log entry event for detailed action and state transition information.
     */
    private void processLogEntryEvent(LogEntryEvent logEntryEvent) {
        LogData logData = logEntryEvent.getLogEntry();
        if (logData == null) return;

        switch (logData.getType()) {
            case ACTION:
                processActionLogEntry(logData);
                break;

            case TRANSITION:
                processTransitionLogEntry(logData);
                break;

            case METRICS:
                processPerformanceLogEntry(logData);
                break;

            case STATE_DETECTION:
                processStateDetectionLogEntry(logData);
                break;

            case ERROR:
                processErrorLogEntry(logData);
                break;

            default:
                // Other log types not specifically handled
                break;
        }
    }

    /**
     * Processes a state detection log entry.
     */
    private void processStateDetectionLogEntry(LogData logData) {
        // Update the current state label if applicable
        if (logData.getCurrentStateName() != null) {
            Platform.runLater(() -> {
                currentStateLabel.setText(logData.getCurrentStateName());
            });

            // Try to get and set the current state
            allStatesInProjectService.getState(logData.getCurrentStateName())
                    .ifPresent(this::updateCurrentState);
        }
    }

    /**
     * Processes an error log entry.
     */
    private void processErrorLogEntry(LogData logData) {
        Platform.runLater(() -> {
            // Could display in a dedicated error panel or highlight in the action history
            ActionRecord record = new ActionRecord(
                    LocalDateTime.now().format(TIME_FORMATTER),
                    "ERROR",
                    logData.getErrorMessage() != null ? logData.getErrorMessage() : "Unknown error",
                    logData.getDuration() + "",
                    "Failed"
            );

            actionHistory.addFirst(record);

            // Limit history size
            if (actionHistory.size() > 100) {
                actionHistory.removeLast();
            }
        });
    }

    /**
     * Processes a state transition log entry.
     */
    private void processTransitionLogEntry(LogData logData) {
        try {
            String fromStates = "Unknown";
            String toStates = "Unknown";

            // Use structured data from LogEntry instead of parsing from description

            // Handle from states
            if (logData.getFromStates() != null && !logData.getFromStates().isEmpty()) {
                fromStates = logData.getFromStates();
            } else if (logData.getFromStateIds() != null && !logData.getFromStateIds().isEmpty()) {
                List<String> stateNames = new ArrayList<>();
                for (Long stateId : logData.getFromStateIds()) {
                    String stateName = allStatesInProjectService.getStateName(stateId);
                    stateNames.add(Objects.requireNonNullElseGet(stateName, () -> "State ID: " + stateId));
                }
                fromStates = String.join(", ", stateNames);
            }

            // Handle to states
            if (logData.getToStateNames() != null && !logData.getToStateNames().isEmpty()) {
                toStates = String.join(", ", logData.getToStateNames());
            } else if (logData.getToStateIds() != null && !logData.getToStateIds().isEmpty()) {
                List<String> stateNames = new ArrayList<>();
                for (Long stateId : logData.getToStateIds()) {
                    String stateName = allStatesInProjectService.getStateName(stateId);
                    stateNames.add(Objects.requireNonNullElseGet(stateName, () -> "State ID: " + stateId));
                }
                toStates = String.join(", ", stateNames);
            }

            // Also update current state if available
            if (logData.getCurrentStateName() != null) {
                allStatesInProjectService.getState(logData.getCurrentStateName())
                        .ifPresent(this::updateCurrentState);
            }

            final String finalFromStates = fromStates;
            final String finalToStates = toStates;

            Platform.runLater(() -> {
                StateTransitionRecord record = new StateTransitionRecord(
                        LocalDateTime.now().format(TIME_FORMATTER),
                        finalFromStates,
                        finalToStates,
                        logData.getDuration() + "",
                        logData.isSuccess()
                );

                stateTransitions.addFirst(record);

                // Limit history size
                if (stateTransitions.size() > 100) {
                    stateTransitions.removeLast();
                }
            });
        } catch (Exception e) {
            logger.error("Error processing state transition log entry", e);
        }
    }

    /**
     * Processes a performance log entry.
     */
    private void processPerformanceLogEntry(LogData logData) {
        try {
            long actionDuration = logData.getDuration();
            long matchTime = 0;

            // Use PerformanceMetrics if available
            PerformanceMetricsData perfMetrics = logData.getPerformance();
            if (perfMetrics != null) {
                actionDuration = perfMetrics.getActionDuration();
                // There's no direct match time in PerformanceMetrics,
                // but we could use another metric like pageLoadTime
                matchTime = perfMetrics.getPageLoadTime();
            }

            // Store performance metric
            PerformanceMetric metric = new PerformanceMetric(
                    performanceMetrics.size() + 1,
                    actionDuration,
                    matchTime
            );

            performanceMetrics.add(metric);

            // Calculate average action duration
            double avgActionDuration = performanceMetrics.stream()
                    .mapToLong(PerformanceMetric::getActionDuration)
                    .average()
                    .orElse(0);

            Platform.runLater(() -> {
                // Update chart
                actionDurationSeries.getData().add(new XYChart.Data<>(metric.getIndex(), metric.getActionDuration()));
                matchTimeSeries.getData().add(new XYChart.Data<>(metric.getIndex(), metric.getMatchTime()));

                // Limit chart size
                if (actionDurationSeries.getData().size() > 50) {
                    actionDurationSeries.getData().removeFirst();
                    matchTimeSeries.getData().removeFirst();
                }

                // Update metric label
                avgActionDurationLabel.setText(String.format("%.0f ms", avgActionDuration));

                // Could also update additional performance metrics here if UI components exist
                // For example, page load time, transition time, etc.
            });
        } catch (Exception e) {
            logger.error("Error processing performance log entry", e);
        }
    }

    /**
     * Processes a log event for information.
     */
    private void processLogEvent(LogEvent logEvent) {
        String message = logEvent.getMessage();

        // Extract current state information if available
        if (message != null && message.contains("State changed to:")) {
            String stateName = message.substring(message.indexOf("State changed to:") + "State changed to:".length()).trim();
            if (stateName.contains(" - ")) {
                stateName = stateName.substring(0, stateName.indexOf(" - ")).trim();
            }

            // Use the AllStatesInProjectService to find the state by name
            final String finalStateName = stateName;
            allStatesInProjectService.getState(finalStateName).ifPresent(this::updateCurrentState);
        }
    }

    /**
     * Updates the current state display.
     */
    private void updateCurrentState(State state) {
        Platform.runLater(() -> {
            currentState.set(state);
            currentStateLabel.setText(state != null ? state.getName() : "None");

            // Could also update additional state information in the UI
            // For example, display state attributes, images, etc.
        });
    }

    /**
     * Processes an action log entry.
     */
    private void processActionLogEntry(LogData logData) {
        // Update action count statistics
        totalActions.incrementAndGet();
        if (logData.isSuccess()) {
            successfulActions.incrementAndGet();
        }

        // Update success rate
        Platform.runLater(() -> {
            long total = totalActions.get();
            long successful = successfulActions.get();
            int successRate = total > 0 ? (int) ((successful * 100) / total) : 0;

            totalActionsLabel.setText(String.valueOf(total));
            successRateLabel.setText(successRate + "%");

            // Add to action history
            String actionText = logData.getDescription();
            if (actionText == null) actionText = "Unknown action";

            if (actionText.contains(" on ")) {
                String[] parts = actionText.split(" on ", 2);
                String action = parts[0].trim();
                String target = parts[1].trim();

                ActionRecord record = new ActionRecord(
                        LocalDateTime.now().format(TIME_FORMATTER),
                        action,
                        target,
                        logData.getDuration() + "",
                        logData.isSuccess() ? "Success" : "Failed"
                );

                actionHistory.addFirst(record); // Add at the beginning

                // Limit history size
                if (actionHistory.size() > 100) {
                    actionHistory.removeLast();
                }
            }
        });
    }

    /**
     * Record class for state transitions.
     */
    @Getter
    public static class StateTransitionRecord {
        private final String time;
        private final String fromState;
        private final String toState;
        private final String duration;
        private final boolean success;

        public StateTransitionRecord(String time, String fromState, String toState, String duration, boolean success) {
            this.time = time;
            this.fromState = fromState;
            this.toState = toState;
            this.duration = duration;
            this.success = success;
        }

    }

    /**
     * Record class for actions.
     */
    @Getter
    public static class ActionRecord {
        private final String time;
        private final String action;
        private final String target;
        private final String duration;
        private final String result;

        public ActionRecord(String time, String action, String target, String duration, String result) {
            this.time = time;
            this.action = action;
            this.target = target;
            this.duration = duration;
            this.result = result;
        }

    }

    /**
     * Record class for performance metrics.
     */
    @Getter
    private static class PerformanceMetric {
        private final int index;
        private final long actionDuration;
        private final long matchTime;

        public PerformanceMetric(int index, long actionDuration, long matchTime) {
            this.index = index;
            this.actionDuration = actionDuration;
            this.matchTime = matchTime;
        }

    }
}