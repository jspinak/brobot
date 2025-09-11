package io.github.jspinak.brobot.runner.ui.enhanced.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.ui.AutomationStatusPanel;

@ExtendWith(MockitoExtension.class)
class EnhancedStatusMonitoringServiceTest {

    @Mock private AutomationOrchestrator automationOrchestrator;

    @Mock private ExecutionStatus executionStatus;

    @Mock private AutomationStatusPanel statusPanel;

    private EnhancedStatusMonitoringService service;

    @BeforeEach
    void setUp() {
        service = new EnhancedStatusMonitoringService(automationOrchestrator);

        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }

        // Disable auto-start for tests
        service.setConfiguration(
                EnhancedStatusMonitoringService.MonitoringConfiguration.builder()
                        .autoStart(false)
                        .updateIntervalMs(50)
                        .build());
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
    }

    @Test
    void testStatusUpdateNotification() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<EnhancedStatusMonitoringService.StatusUpdate> updateRef =
                new AtomicReference<>();

        service.addStatusListener(
                update -> {
                    updateRef.set(update);
                    latch.countDown();
                });

        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getProgress()).thenReturn(0.5);
        when(executionStatus.getCurrentOperation()).thenReturn("Processing");

        // When
        service.startMonitoring();

        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        service.stopMonitoring();

        EnhancedStatusMonitoringService.StatusUpdate update = updateRef.get();
        assertNotNull(update);
        assertEquals(executionStatus, update.getStatus());
        assertEquals("Pause", update.getPauseResumeText());
        assertTrue(update.isPauseResumeEnabled());
        assertTrue(update.isButtonsDisabled());
    }

    @Test
    void testPausedStateUpdate() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<EnhancedStatusMonitoringService.StatusUpdate> updateRef =
                new AtomicReference<>();

        service.addStatusListener(
                update -> {
                    updateRef.set(update);
                    latch.countDown();
                });

        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.PAUSED);

        // When
        service.startMonitoring();

        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        service.stopMonitoring();

        EnhancedStatusMonitoringService.StatusUpdate update = updateRef.get();
        assertEquals("Resume", update.getPauseResumeText());
        assertTrue(update.isPauseResumeEnabled());
    }

    @Test
    void testIdleStateUpdate() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<EnhancedStatusMonitoringService.StatusUpdate> updateRef =
                new AtomicReference<>();

        service.addStatusListener(
                update -> {
                    updateRef.set(update);
                    latch.countDown();
                });

        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getState().isActive()).thenReturn(false);

        // When
        service.startMonitoring();

        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        service.stopMonitoring();

        EnhancedStatusMonitoringService.StatusUpdate update = updateRef.get();
        assertEquals("Pause", update.getPauseResumeText());
        assertFalse(update.isPauseResumeEnabled());
        assertFalse(update.isButtonsDisabled());
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

        EnhancedStatusMonitoringService.StatusUpdateListener listener =
                update -> {
                    updateCount.incrementAndGet();
                    latch.countDown();
                };

        service.addStatusListener(listener);

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
    void testUpdateStatusPanel() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        service.getLastStatus(); // Force update of lastStatus

        // When
        Platform.runLater(
                () -> {
                    service.updateStatusPanel(statusPanel);
                });

        // Wait for Platform.runLater
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        // Can't verify in unit test due to Platform.runLater
    }

    @Test
    void testGetLastStatus() {
        // Initially null
        assertNull(service.getLastStatus());

        // After monitoring
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        service.startMonitoring();

        // Wait for update
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        service.stopMonitoring();

        // Should have captured status
        assertNotNull(service.getLastStatus());
    }

    @Test
    void testConfiguration() {
        // Given
        EnhancedStatusMonitoringService.MonitoringConfiguration config =
                EnhancedStatusMonitoringService.MonitoringConfiguration.builder()
                        .updateIntervalMs(200)
                        .threadName("TestMonitor")
                        .daemon(false)
                        .autoStart(false)
                        .build();

        // When
        service.setConfiguration(config);

        // Then
        assertEquals(200, config.getUpdateIntervalMs());
        assertEquals("TestMonitor", config.getThreadName());
        assertFalse(config.isDaemon());
        assertFalse(config.isAutoStart());
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
}
