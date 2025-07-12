package io.github.jspinak.brobot.runner.ui.execution;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.events.*;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.ExecutionMetrics;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive dashboard for execution control and monitoring.
 * 
 * @deprecated Use {@link RefactoredExecutionDashboardPanel} instead.
 *             This class uses the new LabelManager and UIUpdateManager architecture for better resource management.
 *             Will be removed in version 3.0.
 * 
 * <p>This dashboard combines multiple specialized panels to provide a complete
 * interface for controlling and monitoring automation execution. It includes:
 * <ul>
 *   <li>Execution control buttons (play, pause, stop)</li>
 *   <li>Status display with progress tracking</li>
 *   <li>Performance metrics and charts</li>
 *   <li>State transition history</li>
 *   <li>Action history table</li>
 * </ul>
 * </p>
 * 
 * @see ExecutionControlPanel
 * @see ExecutionStatusPanel
 * @see PerformanceMetricsPanel
 * @see StateTransitionTablePanel
 * @see ActionHistoryTablePanel
 */
@Slf4j
@Getter
@EqualsAndHashCode(callSuper = false)
public class ExecutionDashboardPanel extends BorderPane {
    
    // Core dependencies
    private final EventBus eventBus;
    private final AutomationOrchestrator automationOrchestrator;
    private final StateTransitionStore stateTransitionsRepository;
    private final StateService allStatesInProjectService;

    // UI components
    private ExecutionControlPanel controlPanel;
    private ExecutionStatusPanel statusPanel;
    private PerformanceMetricsPanel performancePanel;
    private StateTransitionTablePanel stateTransitionPanel;
    private ActionHistoryTablePanel actionHistoryPanel;

    // Memory monitoring
    private ScheduledExecutorService memoryMonitor;
    private static final long MEMORY_UPDATE_INTERVAL = 2; // seconds

    /**
     * Creates a new ExecutionDashboardPanel.
     *
     * @param eventBus The event bus for communication
     * @param automationExecutor The automation executor for controlling execution
     * @param stateTransitionsRepository Repository for state transitions
     * @param allStatesInProjectService Service for accessing all states in project
     */
    public ExecutionDashboardPanel(EventBus eventBus,
                                   AutomationOrchestrator automationOrchestrator,
                                   StateTransitionStore stateTransitionsRepository,
                                   StateService allStatesInProjectService) {
        this.eventBus = eventBus;
        this.automationOrchestrator = automationOrchestrator;
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.allStatesInProjectService = allStatesInProjectService;

        setupUI();
        setupEventHandlers();
        setupMemoryMonitoring();
    }

    /**
     * Sets up the UI components of the dashboard.
     */
    private void setupUI() {
        // Main layout structure
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(15));

        // Create specialized panels
        controlPanel = new ExecutionControlPanel(eventBus, automationOrchestrator);
        statusPanel = new ExecutionStatusPanel();
        performancePanel = new PerformanceMetricsPanel();
        stateTransitionPanel = new StateTransitionTablePanel();
        actionHistoryPanel = new ActionHistoryTablePanel();

        // Add components to main layout
        mainLayout.getChildren().addAll(
                controlPanel,
                statusPanel,
                performancePanel,
                stateTransitionPanel,
                actionHistoryPanel
        );

        // Set scroll capability for the entire dashboard
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        setCenter(scrollPane);
    }

    /**
     * Sets up event handlers to respond to execution events.
     */
    private void setupEventHandlers() {
        // Execution state events
        eventBus.subscribe(BrobotEvent.EventType.AUTOMATION_STARTED, this::handleExecutionStarted);
        eventBus.subscribe(BrobotEvent.EventType.AUTOMATION_PROGRESS, this::handleExecutionProgress);
        eventBus.subscribe(BrobotEvent.EventType.AUTOMATION_COMPLETED, this::handleExecutionCompleted);
        eventBus.subscribe(BrobotEvent.EventType.AUTOMATION_FAILED, this::handleExecutionFailed);
        eventBus.subscribe(BrobotEvent.EventType.AUTOMATION_PAUSED, this::handleExecutionPaused);
        eventBus.subscribe(BrobotEvent.EventType.AUTOMATION_RESUMED, this::handleExecutionResumed);
        eventBus.subscribe(BrobotEvent.EventType.AUTOMATION_STOPPED, this::handleExecutionStopped);

        // Log events for action history and metrics
        eventBus.subscribe(BrobotEvent.EventType.LOG_ENTRY, this::handleLogEvent);
    }

    /**
     * Sets up memory monitoring to update memory usage display.
     */
    private void setupMemoryMonitoring() {
        memoryMonitor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "memory-monitor");
            t.setDaemon(true);
            return t;
        });

        memoryMonitor.scheduleAtFixedRate(this::updateMemoryUsage, 0, MEMORY_UPDATE_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Updates memory usage display.
     */
    private void updateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024); // MB
        
        Platform.runLater(() -> performancePanel.updateMemoryUsage(usedMemory));
    }

    private void handleExecutionStarted(BrobotEvent event) {
        Platform.runLater(() -> {
            log.info("Execution started");
            
            // Update UI state
            controlPanel.updateButtonStates(ExecutionState.RUNNING);
            statusPanel.updateStatus(ExecutionState.RUNNING);
            statusPanel.startElapsedTimeUpdater();
            
            // Clear previous data
            performancePanel.reset();
            stateTransitionPanel.clear();
            actionHistoryPanel.clear();
            
            // Update status message
            if (event instanceof ExecutionStatusEvent) {
                ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
                statusPanel.updateCurrentAction(statusEvent.getDetails());
            }
        });
    }

    private void handleExecutionProgress(BrobotEvent event) {
        Platform.runLater(() -> {
            if (event instanceof ExecutionStatusEvent) {
                ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
                ExecutionStatus status = statusEvent.getStatus();
                
                // Update progress
                statusPanel.updateProgress(status.getProgress());
                statusPanel.updateCurrentAction(status.getCurrentAction());
                
                // Update current state if available
                if (status.getCurrentState() != null) {
                    State state = allStatesInProjectService.getAllStates().stream()
                            .filter(s -> s.getName().equals(status.getCurrentState()))
                            .findFirst()
                            .orElse(null);
                    if (state != null) {
                        statusPanel.updateCurrentState(state);
                    }
                }
            }
        });
    }

    private void handleExecutionCompleted(BrobotEvent event) {
        Platform.runLater(() -> {
            log.info("Execution completed");
            
            controlPanel.updateButtonStates(ExecutionState.COMPLETED);
            statusPanel.updateStatus(ExecutionState.COMPLETED);
            statusPanel.stopElapsedTimeUpdater();
            statusPanel.updateProgress(1.0);
            
            if (event instanceof ExecutionStatusEvent) {
                ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
                statusPanel.updateCurrentAction("Completed: " + statusEvent.getDetails());
            }
        });
    }

    private void handleExecutionFailed(BrobotEvent event) {
        Platform.runLater(() -> {
            log.error("Execution failed");
            
            controlPanel.updateButtonStates(ExecutionState.FAILED);
            statusPanel.updateStatus(ExecutionState.FAILED);
            statusPanel.stopElapsedTimeUpdater();
            
            if (event instanceof ExecutionStatusEvent) {
                ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
                statusPanel.updateCurrentAction("Failed: " + statusEvent.getDetails());
            }
        });
    }

    private void handleExecutionPaused(BrobotEvent event) {
        Platform.runLater(() -> {
            log.info("Execution paused");
            
            controlPanel.updateButtonStates(ExecutionState.PAUSED);
            statusPanel.updateStatus(ExecutionState.PAUSED);
            statusPanel.stopElapsedTimeUpdater();
            
            if (event instanceof ExecutionStatusEvent) {
                ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
                statusPanel.updateCurrentAction("Paused: " + statusEvent.getDetails());
            }
        });
    }

    private void handleExecutionResumed(BrobotEvent event) {
        Platform.runLater(() -> {
            log.info("Execution resumed");
            
            controlPanel.updateButtonStates(ExecutionState.RUNNING);
            statusPanel.updateStatus(ExecutionState.RUNNING);
            statusPanel.startElapsedTimeUpdater();
            
            if (event instanceof ExecutionStatusEvent) {
                ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
                statusPanel.updateCurrentAction("Resumed: " + statusEvent.getDetails());
            }
        });
    }

    private void handleExecutionStopped(BrobotEvent event) {
        Platform.runLater(() -> {
            log.info("Execution stopped");
            
            controlPanel.updateButtonStates(ExecutionState.STOPPED);
            statusPanel.updateStatus(ExecutionState.STOPPED);
            statusPanel.stopElapsedTimeUpdater();
            
            if (event instanceof ExecutionStatusEvent) {
                ExecutionStatusEvent statusEvent = (ExecutionStatusEvent) event;
                statusPanel.updateCurrentAction("Stopped: " + statusEvent.getDetails());
            }
        });
    }

    private void handleLogEvent(BrobotEvent event) {
        Platform.runLater(() -> {
            if (event instanceof LogEntryEvent) {
                processLogEntryEvent((LogEntryEvent) event);
            } else if (event instanceof LogEvent) {
                processLogEvent((LogEvent) event);
            }
        });
    }

    private void processLogEntryEvent(LogEntryEvent logEntryEvent) {
        LogData logData = logEntryEvent.getLogEntry();
        
        if (logData == null) return;
        
        // Create adapter to provide expected methods
        LogDataAdapter adapter = new LogDataAdapter(logData);
        
        // Process different types of log entries based on available data
        if (logData.getCurrentStateName() != null) {
            processStateDetectionLogEntry(adapter);
        }
        if (logData.getActionType() != null) {
            processActionLogEntry(adapter);
        }
        if (logData.getErrorMessage() != null) {
            processErrorLogEntry(adapter);
        }
        if (logData.getFromStates() != null && logData.getToStateNames() != null && !logData.getToStateNames().isEmpty()) {
            processTransitionLogEntry(adapter);
        }
        if (logData.getPerformance() != null) {
            processPerformanceLogEntry(adapter);
        }
    }

    private void processStateDetectionLogEntry(LogDataAdapter adapter) {
        // Update current state display
        String stateName = adapter.getDetails().get("state");
        if (stateName != null) {
            State state = allStatesInProjectService.getAllStates().stream()
                    .filter(s -> s.getName().equals(stateName))
                    .findFirst()
                    .orElse(null);
            if (state != null) {
                statusPanel.updateCurrentState(state);
            }
        }
    }

    private void processActionLogEntry(LogDataAdapter adapter) {
        String action = adapter.getAction();
        String target = adapter.getDetails().get("target");
        String result = adapter.getResult();
        Long duration = parseLong(adapter.getDetails().get("duration"));
        String details = adapter.getDetails().get("details");
        
        actionHistoryPanel.addActionRecord(
            action != null ? action : "Unknown",
            target,
            result != null ? result : "UNKNOWN",
            duration != null ? duration : 0,
            details
        );
        
        // Update performance metrics
        if (duration != null) {
            Long matchTime = parseLong(adapter.getDetails().get("matchTime"));
            boolean successful = "SUCCESS".equals(result);
            
            performancePanel.addPerformanceMetric(
                actionHistoryPanel.getActionCount(),
                duration,
                matchTime != null ? matchTime : 0,
                successful
            );
        }
    }

    private void processErrorLogEntry(LogDataAdapter adapter) {
        String errorType = adapter.getDetails().get("errorType");
        String errorMessage = adapter.getMessage();
        String stackTrace = adapter.getDetails().get("stackTrace");
        
        String details = errorType + ": " + errorMessage;
        if (stackTrace != null) {
            details += " (see logs for stack trace)";
        }
        
        actionHistoryPanel.addActionRecord(
            "ERROR",
            errorType,
            "FAILURE",
            0,
            details
        );
    }

    private void processTransitionLogEntry(LogDataAdapter adapter) {
        String fromState = adapter.getDetails().get("fromState");
        String toState = adapter.getDetails().get("toState");
        String trigger = adapter.getDetails().get("trigger");
        Long duration = parseLong(adapter.getDetails().get("duration"));
        
        if (fromState != null && toState != null) {
            stateTransitionPanel.addStateTransition(
                fromState,
                toState,
                trigger != null ? trigger : "Unknown",
                duration != null ? duration : 0
            );
        }
    }

    private void processPerformanceLogEntry(LogDataAdapter adapter) {
        LogDataAdapter.ExecutionMetricsAdapter metrics = adapter.getPerformanceMetrics();
        if (metrics == null) return;
        
        // Process performance metrics
        Long totalDuration = metrics.getTotalDuration();
        Long matchDuration = metrics.getMatchDuration();
        
        if (totalDuration != null) {
            performancePanel.addPerformanceMetric(
                actionHistoryPanel.getActionCount(),
                totalDuration,
                matchDuration != null ? matchDuration : 0,
                true
            );
        }
    }

    private void processLogEvent(LogEvent logEvent) {
        // Handle simple log events
        String message = logEvent.getMessage();
        
        // Extract action information from log message if possible
        if (message.contains("Executing action:")) {
            statusPanel.updateCurrentAction(message.substring(message.indexOf(":") + 1).trim());
        } else if (message.contains("State transition:")) {
            // Parse state transition from log message
            // This is a fallback for when we don't get structured log data
        }
    }

    private Long parseLong(String value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Cleans up resources when the dashboard is closed.
     */
    public void cleanup() {
        if (memoryMonitor != null) {
            memoryMonitor.shutdown();
        }
        statusPanel.stopElapsedTimeUpdater();
    }
}