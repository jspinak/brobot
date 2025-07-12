package io.github.jspinak.brobot.runner.ui.execution;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.events.*;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.testutils.JavaFXTestBase;
import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import io.github.jspinak.brobot.runner.ui.management.UIUpdateManager;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "JavaFX tests require display")
class RefactoredExecutionDashboardPanelTest extends JavaFXTestBase {
    
    @Mock
    private EventBus eventBus;
    
    @Mock
    private AutomationOrchestrator automationOrchestrator;
    
    @Mock
    private StateTransitionStore stateTransitionsRepository;
    
    @Mock
    private StateService allStatesInProjectService;
    
    private LabelManager labelManager;
    private UIUpdateManager uiUpdateManager;
    private RefactoredExecutionDashboardPanel dashboard;
    
    @BeforeEach
    void setUp() throws InterruptedException {
        MockitoAnnotations.openMocks(this);
        
        labelManager = new LabelManager();
        uiUpdateManager = new UIUpdateManager();
        uiUpdateManager.initialize();
        
        runAndWait(() -> {
            dashboard = new RefactoredExecutionDashboardPanel(
                eventBus,
                automationOrchestrator,
                stateTransitionsRepository,
                allStatesInProjectService,
                labelManager,
                uiUpdateManager
            );
            dashboard.postConstruct();
        });
    }
    
    @Test
    void testInitialization() throws InterruptedException {
        runAndWait(() -> {
            // Verify dashboard structure
            assertNotNull(dashboard.getCenter());
            
            // Verify event subscriptions
            verify(eventBus).subscribe(BrobotEvent.EventType.AUTOMATION_STARTED, any());
            verify(eventBus).subscribe(BrobotEvent.EventType.AUTOMATION_PROGRESS, any());
            verify(eventBus).subscribe(BrobotEvent.EventType.AUTOMATION_COMPLETED, any());
            verify(eventBus).subscribe(BrobotEvent.EventType.AUTOMATION_FAILED, any());
            verify(eventBus).subscribe(BrobotEvent.EventType.AUTOMATION_PAUSED, any());
            verify(eventBus).subscribe(BrobotEvent.EventType.AUTOMATION_RESUMED, any());
            verify(eventBus).subscribe(BrobotEvent.EventType.AUTOMATION_STOPPED, any());
            verify(eventBus).subscribe(BrobotEvent.EventType.LOG_ENTRY, any());
        });
    }
    
    @Test
    void testMemoryMonitoringScheduled() {
        // Verify memory monitoring was scheduled
        UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("execution-memory-update");
        assertNotNull(metrics);
    }
    
    @Test
    void testExecutionStartedEvent() throws InterruptedException {
        // Create test event
        ExecutionStatusEvent event = new ExecutionStatusEvent(
            this,
            BrobotEvent.EventType.AUTOMATION_STARTED,
            new ExecutionStatus(ExecutionState.RUNNING, "Starting automation", 0.0, "InitialState")
        );
        
        CountDownLatch latch = new CountDownLatch(1);
        
        // Manually trigger the handler since we can't easily trigger through mocked eventBus
        runAndWait(() -> {
            dashboard.handleExecutionStarted(event);
            latch.countDown();
        });
        
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        
        // Verify UI update was executed
        UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("execution-status-update");
        assertNotNull(metrics);
        assertTrue(metrics.getTotalUpdates() > 0);
    }
    
    @Test
    void testExecutionProgressEvent() throws InterruptedException {
        // Create test event
        ExecutionStatus status = new ExecutionStatus(ExecutionState.RUNNING, "Processing", 0.5, "CurrentState");
        ExecutionStatusEvent event = new ExecutionStatusEvent(
            this,
            BrobotEvent.EventType.AUTOMATION_PROGRESS,
            status
        );
        
        // Mock state service response
        when(allStatesInProjectService.getAllStates()).thenReturn(java.util.Collections.emptyList());
        
        runAndWait(() -> dashboard.handleExecutionProgress(event));
        
        // Verify update was queued
        UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("execution-status-update");
        assertNotNull(metrics);
    }
    
    @Test
    void testReset() throws InterruptedException {
        runAndWait(() -> dashboard.reset());
        
        // Verify reset was executed through UIUpdateManager
        UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("execution-status-update");
        assertNotNull(metrics);
        assertTrue(metrics.getTotalUpdates() > 0);
    }
    
    @Test
    void testPerformanceSummary() throws InterruptedException {
        // Trigger some updates
        runAndWait(() -> dashboard.reset());
        
        String summary = dashboard.getPerformanceSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("Execution Dashboard Performance"));
        assertTrue(summary.contains("Memory Updates"));
        assertTrue(summary.contains("Status Updates"));
    }
    
    @Test
    void testCleanup() throws InterruptedException {
        // Verify initial state
        UIUpdateManager.UpdateMetrics memoryMetrics = uiUpdateManager.getMetrics("execution-memory-update");
        assertNotNull(memoryMetrics);
        
        runAndWait(() -> dashboard.preDestroy());
        
        // Verify scheduled update was cancelled
        assertFalse(uiUpdateManager.cancelScheduledUpdate("execution-memory-update"));
        
        // Verify labels were cleaned up
        assertEquals(0, labelManager.getLabelCount());
    }
    
    @Test
    void testMultipleEventHandling() throws InterruptedException {
        // Test handling multiple events in sequence
        ExecutionStatus runningStatus = new ExecutionStatus(ExecutionState.RUNNING, "Running", 0.25, "State1");
        ExecutionStatus progressStatus = new ExecutionStatus(ExecutionState.RUNNING, "Processing", 0.75, "State2");
        ExecutionStatus completedStatus = new ExecutionStatus(ExecutionState.COMPLETED, "Done", 1.0, "FinalState");
        
        runAndWait(() -> {
            dashboard.handleExecutionStarted(new ExecutionStatusEvent(
                this, BrobotEvent.EventType.AUTOMATION_STARTED, runningStatus));
            dashboard.handleExecutionProgress(new ExecutionStatusEvent(
                this, BrobotEvent.EventType.AUTOMATION_PROGRESS, progressStatus));
            dashboard.handleExecutionCompleted(new ExecutionStatusEvent(
                this, BrobotEvent.EventType.AUTOMATION_COMPLETED, completedStatus));
        });
        
        // Verify multiple updates were processed
        UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("execution-status-update");
        assertNotNull(metrics);
        assertTrue(metrics.getTotalUpdates() >= 3);
    }
    
    @Test
    void testLogEventHandling() throws InterruptedException {
        // Create a log event
        LogEvent logEvent = LogEvent.info(this, "Test log message", "Test");
        
        runAndWait(() -> dashboard.handleLogEvent(logEvent));
        
        // Verify log was processed
        UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("execution-status-update");
        assertNotNull(metrics);
        assertTrue(metrics.getTotalUpdates() > 0);
    }
}