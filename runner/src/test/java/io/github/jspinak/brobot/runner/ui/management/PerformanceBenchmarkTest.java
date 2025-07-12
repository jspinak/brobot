package io.github.jspinak.brobot.runner.ui.management;

import io.github.jspinak.brobot.runner.testutils.JavaFXTestBase;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance benchmark tests comparing the old approach vs new architecture.
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "JavaFX tests require display")
class PerformanceBenchmarkTest extends JavaFXTestBase {
    
    private LabelManager labelManager;
    private UIUpdateManager uiUpdateManager;
    
    @BeforeEach
    void setUp() {
        labelManager = new LabelManager();
        uiUpdateManager = new UIUpdateManager();
        uiUpdateManager.initialize();
    }
    
    @Test
    void benchmarkLabelCreationOldVsNew() throws InterruptedException {
        final int LABEL_COUNT = 1000;
        
        // Benchmark old approach - direct label creation
        AtomicLong oldApproachTime = new AtomicLong();
        List<Label> oldLabels = new ArrayList<>();
        
        runAndWait(() -> {
            long start = System.nanoTime();
            for (int i = 0; i < LABEL_COUNT; i++) {
                Label label = new Label("Label " + i);
                label.setId("label-" + i);
                oldLabels.add(label);
            }
            oldApproachTime.set(System.nanoTime() - start);
        });
        
        // Benchmark new approach - using LabelManager
        AtomicLong newApproachTime = new AtomicLong();
        
        runAndWait(() -> {
            long start = System.nanoTime();
            for (int i = 0; i < LABEL_COUNT; i++) {
                labelManager.getOrCreateLabel("label-" + i, "Label " + i);
            }
            newApproachTime.set(System.nanoTime() - start);
        });
        
        // Log results
        System.out.println("=== Label Creation Benchmark ===");
        System.out.println("Old approach (direct creation): " + (oldApproachTime.get() / 1_000_000) + " ms");
        System.out.println("New approach (LabelManager): " + (newApproachTime.get() / 1_000_000) + " ms");
        System.out.println("LabelManager prevented duplicates: " + (LABEL_COUNT - labelManager.getLabelCount()) + " labels");
        
        // LabelManager should be efficient
        assertTrue(newApproachTime.get() < oldApproachTime.get() * 2, 
                "LabelManager should not be significantly slower than direct creation");
    }
    
    @Test
    void benchmarkUIUpdatesOldVsNew() throws InterruptedException {
        final int UPDATE_COUNT = 100;
        CountDownLatch oldLatch = new CountDownLatch(UPDATE_COUNT);
        CountDownLatch newLatch = new CountDownLatch(UPDATE_COUNT);
        
        // Create a test label
        runAndWait(() -> {
            labelManager.getOrCreateLabel("test-label", "Initial");
        });
        
        // Benchmark old approach - direct Platform.runLater
        long oldStart = System.nanoTime();
        for (int i = 0; i < UPDATE_COUNT; i++) {
            final int index = i;
            Platform.runLater(() -> {
                Label label = labelManager.getOrCreateLabel("test-label", "");
                label.setText("Update " + index);
                oldLatch.countDown();
            });
        }
        assertTrue(oldLatch.await(2, TimeUnit.SECONDS));
        long oldTime = System.nanoTime() - oldStart;
        
        // Benchmark new approach - using UIUpdateManager
        long newStart = System.nanoTime();
        for (int i = 0; i < UPDATE_COUNT; i++) {
            final int index = i;
            uiUpdateManager.queueUpdate("benchmark-update", () -> {
                labelManager.updateLabel("test-label", "Update " + index);
                newLatch.countDown();
            });
        }
        assertTrue(newLatch.await(2, TimeUnit.SECONDS));
        long newTime = System.nanoTime() - newStart;
        
        // Get metrics
        UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics("benchmark-update");
        
        // Log results
        System.out.println("\n=== UI Update Benchmark ===");
        System.out.println("Old approach (Platform.runLater): " + (oldTime / 1_000_000) + " ms");
        System.out.println("New approach (UIUpdateManager): " + (newTime / 1_000_000) + " ms");
        System.out.println("Average update time: " + String.format("%.2f", metrics.getAverageDurationMs()) + " ms");
        System.out.println("Min update time: " + String.format("%.2f", metrics.getMinDurationMs()) + " ms");
        System.out.println("Max update time: " + String.format("%.2f", metrics.getMaxDurationMs()) + " ms");
        
        // UIUpdateManager provides better metrics and control
        assertNotNull(metrics);
        assertEquals(UPDATE_COUNT, metrics.getTotalUpdates());
    }
    
    @Test
    void benchmarkDuplicateLabelPrevention() throws InterruptedException {
        final int DUPLICATE_ATTEMPTS = 1000;
        final String LABEL_ID = "duplicate-test";
        
        // Benchmark without deduplication (old approach)
        List<Label> oldLabels = new ArrayList<>();
        AtomicLong oldMemoryBefore = new AtomicLong();
        AtomicLong oldMemoryAfter = new AtomicLong();
        
        runAndWait(() -> {
            System.gc();
            oldMemoryBefore.set(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            
            for (int i = 0; i < DUPLICATE_ATTEMPTS; i++) {
                Label label = new Label("Duplicate Label");
                label.setId(LABEL_ID);
                oldLabels.add(label);
            }
            
            System.gc();
            oldMemoryAfter.set(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        });
        
        // Benchmark with deduplication (new approach)
        AtomicLong newMemoryBefore = new AtomicLong();
        AtomicLong newMemoryAfter = new AtomicLong();
        
        runAndWait(() -> {
            System.gc();
            newMemoryBefore.set(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            
            for (int i = 0; i < DUPLICATE_ATTEMPTS; i++) {
                labelManager.getOrCreateLabel(LABEL_ID, "Duplicate Label");
            }
            
            System.gc();
            newMemoryAfter.set(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        });
        
        // Log results
        System.out.println("\n=== Duplicate Prevention Benchmark ===");
        System.out.println("Old approach created: " + oldLabels.size() + " labels");
        System.out.println("New approach created: " + labelManager.getLabelCount() + " labels");
        System.out.println("Memory saved: ~" + 
                ((oldMemoryAfter.get() - oldMemoryBefore.get()) - (newMemoryAfter.get() - newMemoryBefore.get())) / 1024 
                + " KB");
        
        // LabelManager should create only one label
        assertEquals(1, labelManager.getLabelCount());
        assertEquals(DUPLICATE_ATTEMPTS, oldLabels.size());
    }
    
    @Test
    void benchmarkScheduledUpdates() throws InterruptedException {
        final int SCHEDULED_TASKS = 10;
        final long UPDATE_INTERVAL = 50; // ms
        final int UPDATES_PER_TASK = 5;
        
        CountDownLatch completionLatch = new CountDownLatch(SCHEDULED_TASKS * UPDATES_PER_TASK);
        List<String> taskIds = new ArrayList<>();
        
        // Schedule multiple periodic tasks
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < SCHEDULED_TASKS; i++) {
            String taskId = "scheduled-task-" + i;
            taskIds.add(taskId);
            
            final int taskIndex = i;
            uiUpdateManager.schedulePeriodicUpdate(
                taskId,
                () -> {
                    labelManager.updateLabel("scheduled-label-" + taskIndex, 
                            "Update at " + System.currentTimeMillis());
                    completionLatch.countDown();
                },
                0,
                UPDATE_INTERVAL,
                TimeUnit.MILLISECONDS
            );
        }
        
        // Wait for updates to complete
        assertTrue(completionLatch.await(5, TimeUnit.SECONDS));
        long totalTime = System.currentTimeMillis() - startTime;
        
        // Cancel all tasks
        taskIds.forEach(uiUpdateManager::cancelScheduledUpdate);
        
        // Collect metrics
        System.out.println("\n=== Scheduled Updates Benchmark ===");
        System.out.println("Total scheduled tasks: " + SCHEDULED_TASKS);
        System.out.println("Updates per task: " + UPDATES_PER_TASK);
        System.out.println("Total time: " + totalTime + " ms");
        
        for (String taskId : taskIds) {
            UIUpdateManager.UpdateMetrics metrics = uiUpdateManager.getMetrics(taskId);
            if (metrics != null) {
                System.out.println(taskId + " - Avg time: " + 
                        String.format("%.2f", metrics.getAverageDurationMs()) + " ms");
            }
        }
        
        // All tasks should have metrics
        for (String taskId : taskIds) {
            assertNotNull(uiUpdateManager.getMetrics(taskId));
        }
    }
    
    @Test
    void benchmarkComponentCleanup() throws InterruptedException {
        final int COMPONENT_COUNT = 100;
        final int LABELS_PER_COMPONENT = 10;
        
        List<Object> components = new ArrayList<>();
        
        // Create many components with labels
        long createStart = System.nanoTime();
        runAndWait(() -> {
            for (int i = 0; i < COMPONENT_COUNT; i++) {
                Object component = new Object();
                components.add(component);
                
                for (int j = 0; j < LABELS_PER_COMPONENT; j++) {
                    labelManager.getOrCreateLabel(component, "label-" + j, "Label " + j);
                }
            }
        });
        long createTime = System.nanoTime() - createStart;
        
        int totalLabels = labelManager.getLabelCount();
        assertEquals(COMPONENT_COUNT * LABELS_PER_COMPONENT, totalLabels);
        
        // Cleanup components
        long cleanupStart = System.nanoTime();
        runAndWait(() -> {
            for (Object component : components) {
                labelManager.removeComponentLabels(component);
            }
        });
        long cleanupTime = System.nanoTime() - cleanupStart;
        
        // Log results
        System.out.println("\n=== Component Cleanup Benchmark ===");
        System.out.println("Components created: " + COMPONENT_COUNT);
        System.out.println("Labels per component: " + LABELS_PER_COMPONENT);
        System.out.println("Total labels: " + totalLabels);
        System.out.println("Creation time: " + (createTime / 1_000_000) + " ms");
        System.out.println("Cleanup time: " + (cleanupTime / 1_000_000) + " ms");
        
        // All labels should be cleaned up
        assertEquals(0, labelManager.getLabelCount());
        assertEquals(0, labelManager.getComponentCount());
        
        // Cleanup should be efficient
        assertTrue(cleanupTime < createTime, "Cleanup should be faster than creation");
    }
    
    @Test
    void benchmarkConcurrentAccess() throws InterruptedException {
        final int THREAD_COUNT = 10;
        final int OPERATIONS_PER_THREAD = 100;
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(THREAD_COUNT);
        
        List<Thread> threads = new ArrayList<>();
        AtomicLong totalOperationTime = new AtomicLong();
        
        // Create threads that will perform concurrent operations
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadIndex = i;
            Thread thread = new Thread(() -> {
                try {
                    startLatch.await();
                    
                    long threadStart = System.nanoTime();
                    
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        final int opIndex = j;
                        
                        // Mix of operations
                        if (j % 3 == 0) {
                            // Create label
                            uiUpdateManager.executeUpdate("thread-" + threadIndex, () -> {
                                labelManager.getOrCreateLabel(
                                    "thread-" + threadIndex + "-label-" + opIndex,
                                    "Label " + opIndex
                                );
                            });
                        } else if (j % 3 == 1) {
                            // Update label
                            uiUpdateManager.queueUpdate("thread-" + threadIndex, () -> {
                                labelManager.updateLabel(
                                    "thread-" + threadIndex + "-label-0",
                                    "Updated " + opIndex
                                );
                            });
                        } else {
                            // Read label
                            Label label = labelManager.getOrCreateLabel(
                                "thread-" + threadIndex + "-label-0", ""
                            );
                            assertNotNull(label);
                        }
                    }
                    
                    long threadTime = System.nanoTime() - threadStart;
                    totalOperationTime.addAndGet(threadTime);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        // Start all threads simultaneously
        long overallStart = System.nanoTime();
        startLatch.countDown();
        
        // Wait for completion
        assertTrue(completeLatch.await(10, TimeUnit.SECONDS));
        long overallTime = System.nanoTime() - overallStart;
        
        // Log results
        System.out.println("\n=== Concurrent Access Benchmark ===");
        System.out.println("Thread count: " + THREAD_COUNT);
        System.out.println("Operations per thread: " + OPERATIONS_PER_THREAD);
        System.out.println("Total operations: " + (THREAD_COUNT * OPERATIONS_PER_THREAD));
        System.out.println("Overall time: " + (overallTime / 1_000_000) + " ms");
        System.out.println("Average thread time: " + (totalOperationTime.get() / THREAD_COUNT / 1_000_000) + " ms");
        System.out.println("Throughput: " + 
                ((THREAD_COUNT * OPERATIONS_PER_THREAD) * 1000.0 / (overallTime / 1_000_000)) + " ops/sec");
        
        // Verify thread safety - no exceptions should have occurred
        assertTrue(labelManager.getLabelCount() > 0);
        assertTrue(uiUpdateManager.getAllMetrics().size() >= THREAD_COUNT);
    }
}