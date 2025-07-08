package io.github.jspinak.brobot.runner.ui.managers;

import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UIUpdateManager.
 */
@ExtendWith(ApplicationExtension.class)
class UIUpdateManagerTest {
    
    private UIUpdateManager updateManager;
    
    @BeforeAll
    static void initializeJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @BeforeEach
    void setUp() {
        updateManager = new UIUpdateManager();
    }
    
    @AfterEach
    void tearDown() {
        updateManager.shutdown();
    }
    
    @Test
    void testScheduleUpdate_ExecutesPeriodically() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);
        
        updateManager.scheduleUpdate("test1", () -> {
            counter.incrementAndGet();
            latch.countDown();
        }, 0, 100, TimeUnit.MILLISECONDS);
        
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(counter.get() >= 3);
        
        updateManager.cancelTask("test1");
    }
    
    @Test
    void testScheduleOnce_ExecutesOnlyOnce() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        
        updateManager.scheduleOnce("test2", () -> {
            counter.incrementAndGet();
            latch.countDown();
        }, 100, TimeUnit.MILLISECONDS);
        
        assertTrue(latch.await(500, TimeUnit.MILLISECONDS));
        assertEquals(1, counter.get());
        
        // Wait to ensure it doesn't execute again
        Thread.sleep(200);
        assertEquals(1, counter.get());
    }
    
    @Test
    void testCancelTask_StopsExecution() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        
        updateManager.scheduleUpdate("test3", counter::incrementAndGet, 
            0, 50, TimeUnit.MILLISECONDS);
        
        Thread.sleep(150);
        assertTrue(updateManager.cancelTask("test3"));
        
        int countAfterCancel = counter.get();
        Thread.sleep(150);
        
        assertEquals(countAfterCancel, counter.get());
    }
    
    @Test
    void testIsTaskScheduled() throws Exception {
        assertFalse(updateManager.isTaskScheduled("test4"));
        
        updateManager.scheduleUpdate("test4", () -> {}, 
            0, 1, TimeUnit.SECONDS);
        
        assertTrue(updateManager.isTaskScheduled("test4"));
        
        updateManager.cancelTask("test4");
        
        assertFalse(updateManager.isTaskScheduled("test4"));
    }
    
    @Test
    void testExecuteNow_RunsImmediately() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        
        updateManager.executeNow(() -> {
            counter.incrementAndGet();
            latch.countDown();
        });
        
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
        assertEquals(1, counter.get());
    }
    
    @Test
    void testUpdateTaskBuilder() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        
        updateManager.updateTask("test5")
            .withTask(() -> {
                counter.incrementAndGet();
                latch.countDown();
            })
            .withInitialDelay(50, TimeUnit.MILLISECONDS)
            .oneTime()
            .schedule();
        
        assertTrue(latch.await(200, TimeUnit.MILLISECONDS));
        assertEquals(1, counter.get());
    }
    
    @Test
    void testCancelAllTasks() throws Exception {
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);
        AtomicInteger counter3 = new AtomicInteger(0);
        
        updateManager.scheduleUpdate("task1", counter1::incrementAndGet, 0, 50, TimeUnit.MILLISECONDS);
        updateManager.scheduleUpdate("task2", counter2::incrementAndGet, 0, 50, TimeUnit.MILLISECONDS);
        updateManager.scheduleUpdate("task3", counter3::incrementAndGet, 0, 50, TimeUnit.MILLISECONDS);
        
        Thread.sleep(150);
        
        assertEquals(3, updateManager.getActiveTaskCount());
        
        updateManager.cancelAllTasks();
        
        assertEquals(0, updateManager.getActiveTaskCount());
        
        int total1 = counter1.get();
        int total2 = counter2.get();
        int total3 = counter3.get();
        
        Thread.sleep(150);
        
        // Counters should not increase after cancel
        assertEquals(total1, counter1.get());
        assertEquals(total2, counter2.get());
        assertEquals(total3, counter3.get());
    }
    
    @Test
    void testPerformanceTracking() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        updateManager.scheduleOnce("slowTask", () -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            latch.countDown();
        }, 0, TimeUnit.MILLISECONDS);
        
        assertTrue(latch.await(500, TimeUnit.MILLISECONDS));
        
        Thread.sleep(100); // Give time for metrics to be recorded
        
        Long executionTime = updateManager.getTaskExecutionTimes().get("slowTask");
        assertNotNull(executionTime);
        assertTrue(executionTime >= 150);
    }
    
    @Test
    void testRescheduleTask() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        
        // Schedule initial task
        updateManager.scheduleUpdate("reschedule", counter::incrementAndGet, 
            0, 100, TimeUnit.MILLISECONDS);
        
        Thread.sleep(250);
        int countBefore = counter.get();
        assertTrue(countBefore >= 2);
        
        // Reschedule with different period
        updateManager.scheduleUpdate("reschedule", counter::incrementAndGet, 
            0, 50, TimeUnit.MILLISECONDS);
        
        Thread.sleep(150);
        int countAfter = counter.get();
        
        // Should have more executions with shorter period
        assertTrue(countAfter > countBefore + 2);
    }
}