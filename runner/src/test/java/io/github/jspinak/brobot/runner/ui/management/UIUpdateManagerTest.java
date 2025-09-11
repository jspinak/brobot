package io.github.jspinak.brobot.runner.ui.management;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UIUpdateManagerTest {

    private UIUpdateManager updateManager;

    @BeforeEach
    void setUp() {
        updateManager = new UIUpdateManager();
        updateManager.initialize();
    }

    @AfterEach
    void tearDown() {
        updateManager.shutdown();
    }

    @Test
    void testExecuteUpdate() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(0);

        // Act
        updateManager.executeUpdate(
                "testUpdate",
                () -> {
                    counter.incrementAndGet();
                    latch.countDown();
                });

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(1, counter.get());
    }

    @Test
    void testQueueUpdate() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger counter = new AtomicInteger(0);

        // Act - Queue multiple updates
        for (int i = 0; i < 3; i++) {
            updateManager.queueUpdate(
                    "queuedUpdate",
                    () -> {
                        counter.incrementAndGet();
                        latch.countDown();
                    });
        }

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(3, counter.get());
    }

    @Test
    void testSchedulePeriodicUpdate() throws InterruptedException {
        // Arrange
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);

        // Act
        boolean scheduled =
                updateManager.schedulePeriodicUpdate(
                        "periodicTask",
                        () -> {
                            counter.incrementAndGet();
                            latch.countDown();
                        },
                        0,
                        100,
                        TimeUnit.MILLISECONDS);

        // Assert
        assertTrue(scheduled);
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(counter.get() >= 3);

        // Cleanup
        updateManager.cancelScheduledUpdate("periodicTask");
    }

    @Test
    void testSchedulePeriodicUpdate_DuplicateId() {
        // Arrange
        updateManager.schedulePeriodicUpdate(
                "duplicateTask", () -> {}, 0, 100, TimeUnit.MILLISECONDS);

        // Act
        boolean scheduled =
                updateManager.schedulePeriodicUpdate(
                        "duplicateTask", () -> {}, 0, 100, TimeUnit.MILLISECONDS);

        // Assert
        assertFalse(scheduled);

        // Cleanup
        updateManager.cancelScheduledUpdate("duplicateTask");
    }

    @Test
    void testScheduleDelayedUpdate() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        // Act
        boolean scheduled =
                updateManager.scheduleDelayedUpdate(
                        "delayedTask",
                        () -> {
                            counter.incrementAndGet();
                            latch.countDown();
                        },
                        200,
                        TimeUnit.MILLISECONDS);

        // Assert
        assertTrue(scheduled);
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(1, counter.get());

        // Verify delay
        long elapsed = System.currentTimeMillis() - startTime;
        assertTrue(elapsed >= 200, "Task executed too early");
    }

    @Test
    void testCancelScheduledUpdate() throws InterruptedException {
        // Arrange
        AtomicInteger counter = new AtomicInteger(0);
        updateManager.schedulePeriodicUpdate(
                "cancelableTask", counter::incrementAndGet, 100, 50, TimeUnit.MILLISECONDS);

        // Let it run a bit
        Thread.sleep(150);

        // Act
        boolean cancelled = updateManager.cancelScheduledUpdate("cancelableTask");
        int countBeforeCancel = counter.get();
        Thread.sleep(150);

        // Assert
        assertTrue(cancelled);
        assertEquals(countBeforeCancel, counter.get(), "Task continued after cancellation");
    }

    @Test
    void testMetrics() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(5);

        // Act - Execute multiple updates
        for (int i = 0; i < 5; i++) {
            updateManager.executeUpdate(
                    "metricsTest",
                    () -> {
                        try {
                            Thread.sleep(10); // Simulate work
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        latch.countDown();
                    });
        }

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        // Assert
        UIUpdateManager.UpdateMetrics metrics = updateManager.getMetrics("metricsTest");
        assertNotNull(metrics);
        assertEquals(5, metrics.getTotalUpdates());
        assertTrue(metrics.getAverageDurationMs() > 0);
        assertTrue(metrics.getMaxDurationMs() >= metrics.getAverageDurationMs());
        assertTrue(metrics.getMinDurationMs() <= metrics.getAverageDurationMs());
    }

    @Test
    void testClearMetrics() {
        // Arrange
        updateManager.executeUpdate("clearTest", () -> {});

        // Act
        updateManager.clearMetrics("clearTest");

        // Assert
        assertNull(updateManager.getMetrics("clearTest"));
    }

    @Test
    void testGetAllMetrics() {
        // Arrange
        updateManager.executeUpdate("task1", () -> {});
        updateManager.executeUpdate("task2", () -> {});
        updateManager.executeUpdate("task3", () -> {});

        // Act
        var allMetrics = updateManager.getAllMetrics();

        // Assert
        assertEquals(3, allMetrics.size());
        assertTrue(allMetrics.containsKey("task1"));
        assertTrue(allMetrics.containsKey("task2"));
        assertTrue(allMetrics.containsKey("task3"));
    }
}
