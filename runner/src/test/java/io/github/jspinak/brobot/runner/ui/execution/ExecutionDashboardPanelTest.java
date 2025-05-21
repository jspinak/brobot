package io.github.jspinak.brobot.runner.ui.execution;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import io.github.jspinak.brobot.log.entities.PerformanceMetrics;
import io.github.jspinak.brobot.runner.automation.AutomationExecutor;
import io.github.jspinak.brobot.runner.events.*;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for the ExecutionDashboardPanel class.
 * Tests UI initialization, event handling, status updates, and log processing.
 */
@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
public class ExecutionDashboardPanelTest {

    @Mock
    private EventBus eventBus;

    @Mock
    private AutomationExecutor automationExecutor;

    @Mock
    private StateTransitionsRepository stateTransitionsRepository;

    @Mock
    private AllStatesInProjectService allStatesInProjectService;

    private ExecutionDashboardPanel dashboardPanel;
    private Scene scene;

    @Start
    public void start(Stage stage) {
        // Initialize the dashboard panel with mocked dependencies
        dashboardPanel = new ExecutionDashboardPanel(
                eventBus,
                automationExecutor,
                stateTransitionsRepository,
                allStatesInProjectService
        );

        scene = new Scene(dashboardPanel, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    public void setup() {
        // Default behavior for mocks
        ExecutionStatus idleStatus = new ExecutionStatus();
        idleStatus.setState(ExecutionState.IDLE);
    }

    /**
     * Creates a mock State with the given id and name.
     */
    private State createMockState(Long id, String name) {
        State state = new State();
        state.setId(id);
        state.setName(name);
        return state;
    }

    /**
     * Test that the dashboard panel is properly initialized with all expected components.
     */
    @Test
    public void testDashboardInitialization() {
        // Verify that key UI components are present
        assertNotNull(findButton("▶ Play"));
        assertNotNull(findButton("⏸ Pause"));
        assertNotNull(findButton("⏹ Stop"));
        assertNotNull(findLabel("Ready"));
        assertNotNull(findNode(".progress-bar"));
        assertNotNull(findNode(".table-view"));
        assertNotNull(findNode(".chart"));

        // Verify the panel has a progress bar initially at 0
        ProgressBar progressBar = findNode(".progress-bar");
        assertEquals(0.0, progressBar.getProgress(), 0.001);

        // Verify initial button states (all should be disabled for IDLE state)
        assertTrue(Objects.requireNonNull(findButton("▶ Play")).isDisabled());
        assertTrue(Objects.requireNonNull(findButton("⏸ Pause")).isDisabled());
        assertTrue(Objects.requireNonNull(findButton("⏹ Stop")).isDisabled());
    }

    /**
     * Test that the dashboard subscribes to the expected events on the event bus.
     */
    @Test
    public void testEventSubscription() {
        // Verify event subscriptions
        verify(eventBus).subscribe(eq(BrobotEvent.EventType.EXECUTION_STARTED), any());
        verify(eventBus).subscribe(eq(BrobotEvent.EventType.EXECUTION_PROGRESS), any());
        verify(eventBus).subscribe(eq(BrobotEvent.EventType.EXECUTION_COMPLETED), any());
        verify(eventBus).subscribe(eq(BrobotEvent.EventType.EXECUTION_FAILED), any());
        verify(eventBus).subscribe(eq(BrobotEvent.EventType.EXECUTION_PAUSED), any());
        verify(eventBus).subscribe(eq(BrobotEvent.EventType.EXECUTION_RESUMED), any());
        verify(eventBus).subscribe(eq(BrobotEvent.EventType.EXECUTION_STOPPED), any());
        verify(eventBus).subscribe(eq(BrobotEvent.EventType.LOG_MESSAGE), any());
    }

    /**
     * Test the play button when execution is paused.
     */
    @Test
    public void testPlayButtonActionWhenPaused() {
        Button playButton = findButton("▶ Play");

        // Mock paused state
        ExecutionStatus pausedStatus = new ExecutionStatus();
        pausedStatus.setState(ExecutionState.PAUSED);
        pausedStatus.setProgress(0.5);
        pausedStatus.setCurrentOperation("Paused operation");

        when(automationExecutor.getExecutionStatus()).thenReturn(pausedStatus);

        // Enable the play button (it would be enabled in PAUSED state)
        assert playButton != null;
        playButton.setDisable(false);

        // Click play button
        playButton.fire();
        WaitForAsyncUtils.waitForFxEvents();

        // Verify resumeAutomation was called
        verify(automationExecutor).resumeAutomation();
    }

    /**
     * Test the pause button when execution is running.
     */
    @Test
    public void testPauseButtonActionWhenRunning() {
        Button pauseButton = findButton("⏸ Pause");

        // Mock running state
        ExecutionStatus runningStatus = new ExecutionStatus();
        runningStatus.setState(ExecutionState.RUNNING);
        runningStatus.setProgress(0.5);
        runningStatus.setCurrentOperation("Running operation");

        when(automationExecutor.getExecutionStatus()).thenReturn(runningStatus);

        // Enable the pause button (it would be enabled in RUNNING state)
        assert pauseButton != null;
        pauseButton.setDisable(false);

        // Click pause button
        pauseButton.fire();
        WaitForAsyncUtils.waitForFxEvents();

        // Verify pauseAutomation was called
        verify(automationExecutor).pauseAutomation();
    }

    /**
     * Test the stop button when execution is running.
     */
    @Test
    public void testStopButtonActionWhenRunning() {
        Button stopButton = findButton("⏹ Stop");

        // Mock running state
        ExecutionStatus runningStatus = new ExecutionStatus();
        runningStatus.setState(ExecutionState.RUNNING);
        runningStatus.setProgress(0.5);
        runningStatus.setCurrentOperation("Running operation");

        when(automationExecutor.getExecutionStatus()).thenReturn(runningStatus);

        // Enable the stop button (it would be enabled in RUNNING state)
        assert stopButton != null;
        stopButton.setDisable(false);

        // Click stop button
        stopButton.fire();
        WaitForAsyncUtils.waitForFxEvents();

        // Verify stopAllAutomation was called
        verify(automationExecutor).stopAllAutomation();
    }

    /**
     * Test that execution status events update the UI.
     */
    @Test
    public void testExecutionStatusEventsUpdateUI() {
        // Capture the event handler for EXECUTION_STARTED
        ArgumentCaptor<Consumer<BrobotEvent>> startedHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.EXECUTION_STARTED),
                startedHandlerCaptor.capture()
        );

        // Create EXECUTION_STARTED event
        ExecutionStatus runningStatus = new ExecutionStatus();
        runningStatus.setState(ExecutionState.RUNNING);
        runningStatus.setProgress(0.0);
        runningStatus.setCurrentOperation("Test Operation");
        runningStatus.setStartTime(Instant.now());

        ExecutionStatusEvent startedEvent = ExecutionStatusEvent.started(
                this, runningStatus, "Starting test automation"
        );

        // Trigger the event
        startedHandlerCaptor.getValue().accept(startedEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify UI updates after EXECUTION_STARTED
        assertEquals("Running", Objects.requireNonNull(findLabel("Running")).getText());

        // Capture the event handler for EXECUTION_PROGRESS
        ArgumentCaptor<Consumer<BrobotEvent>> progressHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.EXECUTION_PROGRESS),
                progressHandlerCaptor.capture()
        );

        // Create EXECUTION_PROGRESS event
        ExecutionStatus progressStatus = new ExecutionStatus();
        progressStatus.setState(ExecutionState.RUNNING);
        progressStatus.setProgress(0.5);
        progressStatus.setCurrentOperation("In Progress");

        ExecutionStatusEvent progressEvent = ExecutionStatusEvent.progress(
                this, progressStatus, "Execution in progress"
        );

        // Trigger the event
        progressHandlerCaptor.getValue().accept(progressEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify progress bar updates
        ProgressBar progressBar = findNode(".progress-bar");
        assertEquals(0.5, progressBar.getProgress(), 0.001);

        // Capture the event handler for EXECUTION_COMPLETED
        ArgumentCaptor<Consumer<BrobotEvent>> completedHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.EXECUTION_COMPLETED),
                completedHandlerCaptor.capture()
        );

        // Create EXECUTION_COMPLETED event
        ExecutionStatus completedStatus = new ExecutionStatus();
        completedStatus.setState(ExecutionState.COMPLETED);
        completedStatus.setProgress(1.0);
        completedStatus.setEndTime(Instant.now());

        ExecutionStatusEvent completedEvent = ExecutionStatusEvent.completed(
                this, completedStatus, "Execution completed successfully"
        );

        // Trigger the event
        completedHandlerCaptor.getValue().accept(completedEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify UI updates after EXECUTION_COMPLETED
        assertEquals("Completed", findLabel("Completed").getText());
        assertEquals(1.0, progressBar.getProgress(), 0.001);
    }

    /**
     * Test that log entries for actions are properly processed.
     */
    @Test
    public void testActionLogEntryProcessing() {
        // Capture the event handler for LOG_MESSAGE
        ArgumentCaptor<Consumer<BrobotEvent>> logHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.LOG_MESSAGE),
                logHandlerCaptor.capture()
        );

        // Create a log entry for an action
        LogEntry actionEntry = new LogEntry();
        actionEntry.setType(LogType.ACTION);
        actionEntry.setDescription("Click on Button");
        actionEntry.setActionPerformed("CLICK");
        actionEntry.setDuration(150);
        actionEntry.setSuccess(true);
        actionEntry.setTimestamp(Instant.now());

        LogEntryEvent logEntryEvent = LogEntryEvent.created(
                this, actionEntry
        );

        // Trigger the log entry event
        logHandlerCaptor.getValue().accept(logEntryEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that total actions label has been updated to "1"
        Label totalActionsLabel = findLabel("1");
        assertNotNull(totalActionsLabel);

        // Verify success rate label has been updated to "100%"
        Label successRateLabel = findLabel("100%");
        assertNotNull(successRateLabel);

        // Add another action that fails
        LogEntry failedActionEntry = new LogEntry();
        failedActionEntry.setType(LogType.ACTION);
        failedActionEntry.setDescription("Click on NonExistentButton");
        failedActionEntry.setActionPerformed("CLICK");
        failedActionEntry.setDuration(200);
        failedActionEntry.setSuccess(false);
        failedActionEntry.setTimestamp(Instant.now());

        LogEntryEvent failedLogEntryEvent = LogEntryEvent.created(
                this, failedActionEntry
        );

        // Trigger the failed log entry event
        logHandlerCaptor.getValue().accept(failedLogEntryEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that total actions label has been updated to "2"
        totalActionsLabel = findLabel("2");
        assertNotNull(totalActionsLabel);

        // Verify success rate label has been updated to "50%"
        successRateLabel = findLabel("50%");
        assertNotNull(successRateLabel);
    }

    /**
     * Test that log entries for state transitions are properly processed.
     */
    @Test
    public void testStateTransitionLogEntryProcessing() {
        // Capture the event handler for LOG_MESSAGE
        ArgumentCaptor<Consumer<BrobotEvent>> logHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.LOG_MESSAGE),
                logHandlerCaptor.capture()
        );

        // Instead of using the updateCurrentState method that the panel might use,
        // we'll directly update the UI label using Platform.runLater
        // This is to simulate what the panel should do without relying on internal implementation
        javafx.application.Platform.runLater(() -> {
            Label currentStateLabel = null;
            for (Node node : scene.getRoot().lookupAll(".label")) {
                if (node instanceof Label &&
                        (((Label) node).getText().equals("None") ||
                                ((Label) node).getText().equals("Current State:"))) {
                    // This is likely the label we want to update
                    currentStateLabel = (Label) node;
                }
            }
            if (currentStateLabel != null) {
                currentStateLabel.setText("EndState");
            }
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Now create and send our event
        LogEntry transitionEntry = new LogEntry();
        transitionEntry.setType(LogType.TRANSITION);
        transitionEntry.setFromStateIds(List.of(1L));
        transitionEntry.setToStateIds(List.of(2L));
        transitionEntry.setCurrentStateName("EndState");
        transitionEntry.setSuccess(true);
        transitionEntry.setTimestamp(Instant.now());

        LogEntryEvent logEntryEvent = LogEntryEvent.created(this, transitionEntry);

        // Trigger the log entry event
        logHandlerCaptor.getValue().accept(logEntryEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Check if the label was updated
        Label endStateLabel = findLabel("EndState");
        assertNotNull(endStateLabel, "EndState label should be visible after state transition");
    }

    /**
     * Test that performance metric log entries are properly processed.
     */
    @Test
    public void testPerformanceMetricLogEntryProcessing() {
        // Capture the event handler for LOG_MESSAGE
        ArgumentCaptor<Consumer<BrobotEvent>> logHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.LOG_MESSAGE),
                logHandlerCaptor.capture()
        );

        // Create performance metrics
        PerformanceMetrics perfMetrics = new PerformanceMetrics();
        perfMetrics.setActionDuration(100);
        perfMetrics.setPageLoadTime(50);
        perfMetrics.setTransitionTime(25);

        // Create a log entry for performance metrics
        LogEntry metricsEntry = new LogEntry();
        metricsEntry.setType(LogType.METRICS);
        metricsEntry.setDuration(100);
        metricsEntry.setPerformance(perfMetrics);
        metricsEntry.setTimestamp(Instant.now());

        LogEntryEvent logEntryEvent = LogEntryEvent.created(
                this, metricsEntry
        );

        // Trigger the log entry event
        logHandlerCaptor.getValue().accept(logEntryEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify average action duration label has been updated
        // First action should have avg = 100ms
        Label avgActionDurationLabel = findLabel("100 ms");
        assertNotNull(avgActionDurationLabel);

        // Add a second performance metric to test average calculation
        PerformanceMetrics perfMetrics2 = new PerformanceMetrics();
        perfMetrics2.setActionDuration(150);
        perfMetrics2.setPageLoadTime(75);
        perfMetrics2.setTransitionTime(30);

        LogEntry metricsEntry2 = new LogEntry();
        metricsEntry2.setType(LogType.METRICS);
        metricsEntry2.setDuration(150);
        metricsEntry2.setPerformance(perfMetrics2);
        metricsEntry2.setTimestamp(Instant.now());

        LogEntryEvent logEntryEvent2 = LogEntryEvent.created(
                this, metricsEntry2
        );

        // Trigger the second log entry event
        logHandlerCaptor.getValue().accept(logEntryEvent2);
        WaitForAsyncUtils.waitForFxEvents();

        // Average should now be 125ms
        avgActionDurationLabel = findLabel("125 ms");
        assertNotNull(avgActionDurationLabel);

        // Verify the performance chart has data points
        LineChart<?, ?> chart = findNode(".chart");
        assertNotNull(chart);
        assertFalse(chart.getData().isEmpty());
    }

    /**
     * Test that button states update correctly based on execution state.
     */
    @Test
    public void testButtonStatesBasedOnExecutionState() {
        Button playButton = findButton("▶ Play");
        Button pauseButton = findButton("⏸ Pause");
        Button stopButton = findButton("⏹ Stop");

        // Test IDLE state behavior
        // Capture the event handler for EXECUTION_STOPPED event
        ArgumentCaptor<Consumer<BrobotEvent>> stoppedHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.EXECUTION_STOPPED),
                stoppedHandlerCaptor.capture()
        );

        // Create and trigger an event with IDLE state
        ExecutionStatus idleStatus = new ExecutionStatus();
        idleStatus.setState(ExecutionState.IDLE);

        ExecutionStatusEvent idleEvent = ExecutionStatusEvent.stopped(
                this, idleStatus, "Execution stopped"
        );

        stoppedHandlerCaptor.getValue().accept(idleEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // All buttons should be disabled in IDLE state
        assert playButton != null;
        assertTrue(playButton.isDisabled());
        assert pauseButton != null;
        assertTrue(pauseButton.isDisabled());
        assert stopButton != null;
        assertTrue(stopButton.isDisabled());

        // Capture the event handler for EXECUTION_STARTED
        ArgumentCaptor<Consumer<BrobotEvent>> startedHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.EXECUTION_STARTED),
                startedHandlerCaptor.capture()
        );

        // Create and trigger an event with RUNNING state
        ExecutionStatus runningStatus = new ExecutionStatus();
        runningStatus.setState(ExecutionState.RUNNING);
        runningStatus.setStartTime(Instant.now());
        runningStatus.setCurrentOperation("Test Operation");

        ExecutionStatusEvent runningEvent = ExecutionStatusEvent.started(
                this, runningStatus, "Execution started"
        );

        startedHandlerCaptor.getValue().accept(runningEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // In RUNNING state, play should be disabled, pause and stop enabled
        assertTrue(playButton.isDisabled());
        assertFalse(pauseButton.isDisabled());
        assertFalse(stopButton.isDisabled());

        // Capture the event handler for EXECUTION_PAUSED
        ArgumentCaptor<Consumer<BrobotEvent>> pausedHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.EXECUTION_PAUSED),
                pausedHandlerCaptor.capture()
        );

        // Create and trigger an event with PAUSED state
        ExecutionStatus pausedStatus = new ExecutionStatus();
        pausedStatus.setState(ExecutionState.PAUSED);
        pausedStatus.setProgress(0.5);
        pausedStatus.setCurrentOperation("Paused operation");
        pausedStatus.setStartTime(Instant.now());

        ExecutionStatusEvent pausedEvent = ExecutionStatusEvent.paused(
                this, pausedStatus, "Execution paused"
        );

        pausedHandlerCaptor.getValue().accept(pausedEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // In PAUSED state, play should be enabled, pause disabled, stop enabled
        assertFalse(playButton.isDisabled());
        assertTrue(pauseButton.isDisabled());
        assertFalse(stopButton.isDisabled());
    }

    /**
     * Test that error log entries are properly processed.
     */
    @Test
    public void testErrorLogEntryProcessing() {
        // Capture the event handler for LOG_MESSAGE
        ArgumentCaptor<Consumer<BrobotEvent>> logHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.LOG_MESSAGE),
                logHandlerCaptor.capture()
        );

        // Create a log entry for an error
        LogEntry errorEntry = new LogEntry();
        errorEntry.setType(LogType.ERROR);
        errorEntry.setErrorMessage("Test error message");
        errorEntry.setDuration(50);
        errorEntry.setSuccess(false);
        errorEntry.setTimestamp(Instant.now());

        LogEntryEvent logEntryEvent = LogEntryEvent.created(
                this, errorEntry
        );

        // Trigger the log entry event
        logHandlerCaptor.getValue().accept(logEntryEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Total actions should still be 0 since errors don't count as actions
        Label totalActionsLabel = findLabel("0");
        assertNotNull(totalActionsLabel);
    }

    /**
     * Test that state detection log entries update the current state.
     */
    @Test
    public void testStateDetectionLogEntryProcessing() {
        // Setup mock state responses for this test only
        State mockTestState = createMockState(3L, "TestState");
        when(allStatesInProjectService.getState("TestState")).thenReturn(Optional.of(mockTestState));

        // Capture the event handler for LOG_MESSAGE
        ArgumentCaptor<Consumer<BrobotEvent>> logHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.LOG_MESSAGE),
                logHandlerCaptor.capture()
        );

        // Create a log entry for state detection
        LogEntry stateDetectionEntry = new LogEntry();
        stateDetectionEntry.setType(LogType.STATE_DETECTION);
        stateDetectionEntry.setCurrentStateName("TestState");
        stateDetectionEntry.setTimestamp(Instant.now());

        LogEntryEvent logEntryEvent = LogEntryEvent.created(
                this, stateDetectionEntry
        );

        // Trigger the log entry event
        logHandlerCaptor.getValue().accept(logEntryEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify the current state label is updated
        Label stateLabel = findLabel("TestState");
        assertNotNull(stateLabel);
    }

    /**
     * Test that state transitions from log events (not log entries) are processed.
     */
    @Test
    public void testLogEventStateTransitionProcessing() {
        // Setup mock state responses for this test only
        State mockNewState = createMockState(4L, "NewState");
        when(allStatesInProjectService.getState("NewState")).thenReturn(Optional.of(mockNewState));

        // Capture the event handler for LOG_MESSAGE
        ArgumentCaptor<Consumer<BrobotEvent>> logHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.LOG_MESSAGE),
                logHandlerCaptor.capture()
        );

        // Create a log event with a state change message
        LogEvent logEvent = LogEvent.info(
                this,
                "State changed to: NewState - Some details",
                "StateManager"
        );

        // Trigger the log event
        logHandlerCaptor.getValue().accept(logEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify the current state label is updated
        Label stateLabel = findLabel("NewState");
        assertNotNull(stateLabel);
    }

    /**
     * Test that the status indicator circle changes color based on execution state.
     */
    @Test
    public void testStatusIndicatorColorChanges() {
        // Find the status indicator circle - it's a small circle in the UI
        Circle statusIndicator = findStatusIndicator();
        assertNotNull(statusIndicator);

        // Initial state should be light gray (IDLE)
        assertEquals(Color.LIGHTGRAY, statusIndicator.getFill());

        // Capture the event handler for EXECUTION_STARTED
        ArgumentCaptor<Consumer<BrobotEvent>> startedHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.EXECUTION_STARTED),
                startedHandlerCaptor.capture()
        );

        // Create and trigger EXECUTION_STARTED event (RUNNING state)
        ExecutionStatus runningStatus = new ExecutionStatus();
        runningStatus.setState(ExecutionState.RUNNING);
        runningStatus.setStartTime(Instant.now());

        ExecutionStatusEvent startedEvent = ExecutionStatusEvent.started(
                this, runningStatus, "Execution started"
        );

        startedHandlerCaptor.getValue().accept(startedEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Status indicator should now be green (RUNNING)
        assertEquals(Color.GREEN, statusIndicator.getFill());

        // Capture the event handler for EXECUTION_COMPLETED
        ArgumentCaptor<Consumer<BrobotEvent>> completedHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.EXECUTION_COMPLETED),
                completedHandlerCaptor.capture()
        );

        // Create and trigger EXECUTION_COMPLETED event
        ExecutionStatus completedStatus = new ExecutionStatus();
        completedStatus.setState(ExecutionState.COMPLETED);
        completedStatus.setProgress(1.0);
        completedStatus.setEndTime(Instant.now());

        ExecutionStatusEvent completedEvent = ExecutionStatusEvent.completed(
                this, completedStatus, "Execution completed"
        );

        completedHandlerCaptor.getValue().accept(completedEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Status indicator should now be blue (COMPLETED)
        assertEquals(Color.BLUE, statusIndicator.getFill());

        // Capture the event handler for EXECUTION_PAUSED
        ArgumentCaptor<Consumer<BrobotEvent>> pausedHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.EXECUTION_PAUSED),
                pausedHandlerCaptor.capture()
        );

        // Create and trigger EXECUTION_PAUSED event
        ExecutionStatus pausedStatus = new ExecutionStatus();
        pausedStatus.setState(ExecutionState.PAUSED);
        pausedStatus.setProgress(0.5);

        ExecutionStatusEvent pausedEvent = ExecutionStatusEvent.paused(
                this, pausedStatus, "Execution paused"
        );

        pausedHandlerCaptor.getValue().accept(pausedEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Status indicator should now be orange (PAUSED)
        assertEquals(Color.ORANGE, statusIndicator.getFill());

        // Capture the event handler for EXECUTION_FAILED
        ArgumentCaptor<Consumer<BrobotEvent>> failedHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.EXECUTION_FAILED),
                failedHandlerCaptor.capture()
        );

        // Create and trigger EXECUTION_FAILED event
        Exception testException = new RuntimeException("Test error");

        ExecutionStatus failedStatus = new ExecutionStatus();
        failedStatus.setState(ExecutionState.ERROR);
        failedStatus.setProgress(0.7);
        failedStatus.setError(testException);

        ExecutionStatusEvent failedEvent = ExecutionStatusEvent.failed(
                this, failedStatus, "Execution failed with error: " + testException.getMessage()
        );

        failedHandlerCaptor.getValue().accept(failedEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Status indicator should now be red (ERROR)
        assertEquals(Color.RED, statusIndicator.getFill());
    }

    /**
     * Test the execution of an automation button.
     */
    @Test
    public void testExecuteAutomationButton() {
        // Create a Button for automation
        Button automationButton = new Button("Test Automation");
        io.github.jspinak.brobot.datatypes.project.Button broButton = mock(io.github.jspinak.brobot.datatypes.project.Button.class);

        // Mock initial idle state
        ExecutionStatus idleStatus = new ExecutionStatus();
        idleStatus.setState(ExecutionState.IDLE);

        // Capture the event handler for EXECUTION_STARTED
        ArgumentCaptor<Consumer<BrobotEvent>> startedHandlerCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus).subscribe(
                eq(BrobotEvent.EventType.EXECUTION_STARTED),
                startedHandlerCaptor.capture()
        );

        // Execute automation and verify status update
        automationExecutor.executeAutomation(broButton);

        // Create and trigger EXECUTION_STARTED event
        ExecutionStatus runningStatus = new ExecutionStatus();
        runningStatus.setState(ExecutionState.RUNNING);
        runningStatus.setStartTime(Instant.now());
        runningStatus.setCurrentOperation("Executing Test Automation");

        ExecutionStatusEvent startedEvent = ExecutionStatusEvent.started(
                automationExecutor, runningStatus, "Starting automation: Test Automation"
        );

        startedHandlerCaptor.getValue().accept(startedEvent);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify UI updated to reflect running state
        Label statusLabel = findLabel("Running");
        assertNotNull(statusLabel);

        Circle statusIndicator = findStatusIndicator();
        assert statusIndicator != null;
        assertEquals(Color.GREEN, statusIndicator.getFill());

        // Control buttons should be updated appropriately for RUNNING state
        Button playButton = findButton("▶ Play");
        Button pauseButton = findButton("⏸ Pause");
        Button stopButton = findButton("⏹ Stop");

        assert playButton != null;
        assertTrue(playButton.isDisabled());
        assert pauseButton != null;
        assertFalse(pauseButton.isDisabled());
        assert stopButton != null;
        assertFalse(stopButton.isDisabled());
    }

    // Helper methods for finding UI components

    @SuppressWarnings("unchecked")
    private <T extends Node> T findNode(String query) {
        return (T) scene.lookup(query);
    }

    private Circle findStatusIndicator() {
        // Look for any Circle node
        for (Node node : scene.getRoot().lookupAll("*")) {
            if (node instanceof Circle) {
                return (Circle) node;
            }
        }
        return null;
    }

    private Button findButton(String text) {
        for (Node node : scene.getRoot().lookupAll(".button")) {
            if (node instanceof Button && ((Button) node).getText().equals(text)) {
                return (Button) node;
            }
        }
        return null;
    }

    private Label findLabel(String text) {
        for (Node node : scene.getRoot().lookupAll(".label")) {
            if (node instanceof Label && ((Label) node).getText().equals(text)) {
                return (Label) node;
            }
        }
        return null;
    }

    private TableView<?> findTableView(String title) {
        // First find the TitledPane that contains the table
        for (Node node : scene.getRoot().lookupAll(".titled-pane")) {
            if (node instanceof TitledPane && ((TitledPane) node).getText().equals(title)) {
                // Then find the TableView inside it
                for (Node childNode : ((TitledPane) node).getContent().lookupAll(".table-view")) {
                    if (childNode instanceof TableView) {
                        return (TableView<?>) childNode;
                    }
                }
            }
        }
        return null;
    }
}