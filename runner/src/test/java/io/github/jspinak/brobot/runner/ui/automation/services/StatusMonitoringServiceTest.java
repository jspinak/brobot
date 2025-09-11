package io.github.jspinak.brobot.runner.ui.automation.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;

@ExtendWith(MockitoExtension.class)
class StatusMonitoringServiceTest {

    @Mock private AutomationOrchestrator automationOrchestrator;

    @Mock private ExecutionStatus executionStatus;

    private StatusMonitoringService service;

    @BeforeEach
    void setUp() {
        service = new StatusMonitoringService(automationOrchestrator);
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    void testStartStopMonitoring() {
        // Initially not monitoring
        assertFalse(service.isMonitoring());

        // Start monitoring
        service.startMonitoring();
        assertTrue(service.isMonitoring());

        // Try to start again (should not create new thread)
        service.startMonitoring();
        assertTrue(service.isMonitoring());

        // Stop monitoring
        service.stopMonitoring();
        assertFalse(service.isMonitoring());

        // Try to stop again (should be safe)
        service.stopMonitoring();
        assertFalse(service.isMonitoring());
    }

    @Test
    void testStatusUpdateNotification() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<StatusMonitoringService.StatusUpdate> updateRef = new AtomicReference<>();

        StatusMonitoringService.StatusUpdateListener listener =
                update -> {
                    updateRef.set(update);
                    latch.countDown();
                };

        service.addStatusListener(listener);

        // Configure for fast updates
        service.setConfiguration(
                StatusMonitoringService.MonitoringConfiguration.builder()
                        .updateIntervalMs(50)
                        .build());

        // Mock status
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getProgress()).thenReturn(0.5);
        when(executionStatus.getCurrentOperation()).thenReturn("Processing");

        // When
        service.startMonitoring();

        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        service.stopMonitoring();

        StatusMonitoringService.StatusUpdate update = updateRef.get();
        assertNotNull(update);
        assertEquals("Status: Processing", update.getStatusMessage());
        assertEquals(0.5, update.getProgress());
        assertEquals("Pause Execution", update.getPauseResumeText());
        assertTrue(update.isPauseResumeEnabled());
    }

    @Test
    void testStatusChangeDetection() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger updateCount = new AtomicInteger(0);

        service.addStatusListener(
                update -> {
                    updateCount.incrementAndGet();
                    latch.countDown();
                });

        service.setConfiguration(
                StatusMonitoringService.MonitoringConfiguration.builder()
                        .updateIntervalMs(50)
                        .build());

        // First status
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getProgress()).thenReturn(0.3);

        // When
        service.startMonitoring();

        // Wait for first update
        Thread.sleep(100);

        // Change status
        when(executionStatus.getProgress()).thenReturn(0.6);

        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        service.stopMonitoring();

        assertTrue(updateCount.get() >= 2);
    }

    @Test
    void testNoUpdateWhenStatusUnchanged() throws InterruptedException {
        // Given
        AtomicInteger updateCount = new AtomicInteger(0);

        service.addStatusListener(update -> updateCount.incrementAndGet());

        service.setConfiguration(
                StatusMonitoringService.MonitoringConfiguration.builder()
                        .updateIntervalMs(50)
                        .build());

        // Static status
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getProgress()).thenReturn(0.0);
        when(executionStatus.getCurrentOperation()).thenReturn(null);

        // When
        service.startMonitoring();

        // Wait for monitoring to run multiple times
        Thread.sleep(300);

        service.stopMonitoring();

        // Then - Should only update once since status doesn't change
        assertEquals(1, updateCount.get());
    }

    @Test
    void testRemoveListener() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger updateCount = new AtomicInteger(0);

        StatusMonitoringService.StatusUpdateListener listener =
                update -> {
                    updateCount.incrementAndGet();
                    latch.countDown();
                };

        service.addStatusListener(listener);

        service.setConfiguration(
                StatusMonitoringService.MonitoringConfiguration.builder()
                        .updateIntervalMs(50)
                        .build());

        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);

        // Start and verify listener works
        service.startMonitoring();
        assertTrue(latch.await(1, TimeUnit.SECONDS));

        // Remove listener
        service.removeStatusListener(listener);

        // Change status
        when(executionStatus.getProgress()).thenReturn(0.9);

        // Wait for potential updates
        Thread.sleep(200);

        service.stopMonitoring();

        // Should still be 1 since listener was removed
        assertEquals(1, updateCount.get());
    }

    @Test
    void testForceUpdate() {
        // Given
        AtomicReference<StatusMonitoringService.StatusUpdate> updateRef = new AtomicReference<>();
        service.addStatusListener(updateRef::set);

        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.PAUSED);
        when(executionStatus.getProgress()).thenReturn(0.75);

        // When
        service.forceUpdate();

        // Then
        StatusMonitoringService.StatusUpdate update = updateRef.get();
        assertNotNull(update);
        assertEquals("Resume Execution", update.getPauseResumeText());
        assertEquals(0.75, update.getProgress());
    }

    @Test
    void testGetLastStatus() {
        // Initially null
        assertNull(service.getLastStatus());

        // After monitoring
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        service.forceUpdate();

        assertEquals(executionStatus, service.getLastStatus());
    }

    @Test
    void testConfiguration() {
        // Given
        StatusMonitoringService.MonitoringConfiguration config =
                StatusMonitoringService.MonitoringConfiguration.builder()
                        .updateIntervalMs(1000)
                        .threadName("TestMonitor")
                        .daemon(false)
                        .build();

        // When
        service.setConfiguration(config);

        // Then - configuration is applied (we can't easily test all aspects)
        // Start monitoring to create thread with new config
        service.startMonitoring();

        // Give thread time to start
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        service.stopMonitoring();
    }

    @Test
    void testStatusUpdateWithDifferentStates() {
        // Given
        AtomicReference<StatusMonitoringService.StatusUpdate> updateRef = new AtomicReference<>();
        service.addStatusListener(updateRef::set);

        // Test PAUSED state
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.PAUSED);
        when(executionStatus.getProgress()).thenReturn(0.5);

        service.forceUpdate();

        StatusMonitoringService.StatusUpdate pausedUpdate = updateRef.get();
        assertEquals("Resume Execution", pausedUpdate.getPauseResumeText());
        assertTrue(pausedUpdate.isPauseResumeEnabled());

        // Test IDLE state
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getCurrentOperation()).thenReturn(null);
        when(executionStatus.getState().getDescription()).thenReturn("Idle");

        service.forceUpdate();

        StatusMonitoringService.StatusUpdate idleUpdate = updateRef.get();
        assertEquals("Status: Idle", idleUpdate.getStatusMessage());
        assertEquals("Pause Execution", idleUpdate.getPauseResumeText());
        assertFalse(idleUpdate.isPauseResumeEnabled());
    }

    @Test
    void testMonitoringWithNullStatus() throws InterruptedException {
        // Given
        AtomicInteger updateCount = new AtomicInteger(0);
        service.addStatusListener(update -> updateCount.incrementAndGet());

        service.setConfiguration(
                StatusMonitoringService.MonitoringConfiguration.builder()
                        .updateIntervalMs(50)
                        .build());

        // Return null status
        when(automationOrchestrator.getExecutionStatus()).thenReturn(null);

        // When
        service.startMonitoring();
        Thread.sleep(200);
        service.stopMonitoring();

        // Then - No updates should occur
        assertEquals(0, updateCount.get());
    }

    @Test
    void testErrorHandlingInMonitoring() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger updateCount = new AtomicInteger(0);

        service.addStatusListener(
                update -> {
                    updateCount.incrementAndGet();
                    latch.countDown();
                    // Throw exception on first update
                    if (updateCount.get() == 1) {
                        throw new RuntimeException("Test exception");
                    }
                });

        service.setConfiguration(
                StatusMonitoringService.MonitoringConfiguration.builder()
                        .updateIntervalMs(50)
                        .build());

        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getProgress()).thenReturn(0.1, 0.2); // Different values

        // When
        service.startMonitoring();

        // Then - Monitoring should continue despite exception
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        service.stopMonitoring();

        assertTrue(updateCount.get() >= 2);
    }

    @Test
    void testOperationChangeDetection() {
        // Given
        AtomicInteger updateCount = new AtomicInteger(0);
        service.addStatusListener(update -> updateCount.incrementAndGet());

        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getProgress()).thenReturn(0.5);
        when(executionStatus.getCurrentOperation()).thenReturn("Step 1");

        // First update
        service.forceUpdate();
        assertEquals(1, updateCount.get());

        // Change operation
        when(executionStatus.getCurrentOperation()).thenReturn("Step 2");
        service.forceUpdate();
        assertEquals(2, updateCount.get());

        // Same operation
        service.forceUpdate();
        assertEquals(2, updateCount.get()); // No change, no update
    }
}
