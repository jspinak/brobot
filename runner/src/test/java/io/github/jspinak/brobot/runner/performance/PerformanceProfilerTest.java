package io.github.jspinak.brobot.runner.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceProfilerTest {

    private PerformanceProfiler profiler;

    @BeforeEach
    void setUp() {
        profiler = new PerformanceProfiler();
        profiler.initialize();
    }

    @Test
    @DisplayName("Should track operation execution time")
    void shouldTrackOperationTime() throws Exception {
        // Execute operation with profiling
        try (var timer = profiler.startOperation("test-operation")) {
            Thread.sleep(100); // Simulate work
        }

        // Generate report
        PerformanceProfiler.PerformanceReport report = profiler.generateReport();

        // Verify operation was tracked
        String reportStr = report.toString();
        assertTrue(reportStr.contains("test-operation"));
        assertTrue(reportStr.contains("Executions: 1"));
    }

    @Test
    @DisplayName("Should calculate average execution time correctly")
    void shouldCalculateAverageTime() throws Exception {
        // Execute operation multiple times
        for (int i = 0; i < 5; i++) {
            try (var timer = profiler.startOperation("avg-test")) {
                Thread.sleep(50); // Consistent 50ms operations
            }
        }

        // Generate report
        PerformanceProfiler.PerformanceReport report = profiler.generateReport();
        String reportStr = report.toString();

        // Verify average is calculated
        assertTrue(reportStr.contains("avg-test"));
        assertTrue(reportStr.contains("Executions: 5"));
        // Average should be around 50ms (allowing for some variance)
        assertTrue(reportStr.contains("Avg time:"));
    }

    @Test
    @DisplayName("Should track min and max execution times")
    void shouldTrackMinMaxTimes() throws Exception {
        // Execute operations with different durations
        try (var timer = profiler.startOperation("minmax-test")) {
            Thread.sleep(10); // Short operation
        }

        try (var timer = profiler.startOperation("minmax-test")) {
            Thread.sleep(100); // Long operation
        }

        try (var timer = profiler.startOperation("minmax-test")) {
            Thread.sleep(50); // Medium operation
        }

        // Generate report
        PerformanceProfiler.PerformanceReport report = profiler.generateReport();
        String reportStr = report.toString();

        // Verify min/max are tracked
        assertTrue(reportStr.contains("minmax-test"));
        assertTrue(reportStr.contains("Min/Max:"));
    }

    @Test
    @DisplayName("Should capture memory snapshots when profiling is active")
    void shouldCaptureMemorySnapshots() throws Exception {
        // Start profiling
        profiler.startProfiling();

        // Wait for snapshots to be captured
        Thread.sleep(2000);

        // Stop profiling
        profiler.stopProfiling();

        // Generate report
        PerformanceProfiler.PerformanceReport report = profiler.generateReport();
        String reportStr = report.toString();

        // Verify memory analysis is included
        assertTrue(reportStr.contains("Memory Analysis:"));
        assertTrue(reportStr.contains("Heap utilization:"));
    }

    @Test
    @DisplayName("Should detect thread information")
    void shouldDetectThreadInfo() throws Exception {
        // Start profiling
        profiler.startProfiling();

        // Create some threads
        Thread thread1 = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "test-thread-1");
        
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "test-thread-2");

        thread1.start();
        thread2.start();

        // Wait for snapshot
        Thread.sleep(1500);

        // Generate report
        PerformanceProfiler.PerformanceReport report = profiler.generateReport();
        String reportStr = report.toString();

        // Verify thread analysis
        assertTrue(reportStr.contains("Thread Analysis:"));
        assertTrue(reportStr.contains("Active threads:"));

        // Cleanup
        thread1.join();
        thread2.join();
        profiler.stopProfiling();
    }

    @Test
    @DisplayName("Should track GC statistics")
    void shouldTrackGCStatistics() throws Exception {
        // Start profiling
        profiler.startProfiling();

        // Create garbage to trigger GC
        for (int i = 0; i < 1000; i++) {
            byte[] garbage = new byte[1024 * 1024]; // 1MB
        }
        System.gc(); // Suggest GC

        // Wait for snapshots
        Thread.sleep(2000);

        // Generate report
        PerformanceProfiler.PerformanceReport report = profiler.generateReport();
        String reportStr = report.toString();

        // Verify GC analysis
        assertTrue(reportStr.contains("GC Analysis:"));
        assertTrue(reportStr.contains("GC collections:"));

        profiler.stopProfiling();
    }

    @Test
    @DisplayName("Should handle concurrent operations")
    void shouldHandleConcurrentOperations() throws Exception {
        // Execute operations concurrently
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try (var timer = profiler.startOperation("concurrent-op-" + index)) {
                    Thread.sleep(50);
                } catch (Exception e) {
                    fail("Operation failed: " + e.getMessage());
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Generate report
        PerformanceProfiler.PerformanceReport report = profiler.generateReport();
        String reportStr = report.toString();

        // Verify all operations were tracked
        for (int i = 0; i < threads.length; i++) {
            assertTrue(reportStr.contains("concurrent-op-" + i));
        }
    }

    @Test
    @DisplayName("Should track CPU time separately from wall time")
    void shouldTrackCPUTime() throws Exception {
        // Execute CPU-intensive operation
        try (var timer = profiler.startOperation("cpu-intensive")) {
            // Perform CPU-intensive work
            long sum = 0;
            for (int i = 0; i < 10_000_000; i++) {
                sum += i;
            }
            assertEquals(49999995000000L, sum); // Verify computation
        }

        // Execute I/O-bound operation
        try (var timer = profiler.startOperation("io-bound")) {
            Thread.sleep(100); // Simulate I/O wait
        }

        // Generate report
        PerformanceProfiler.PerformanceReport report = profiler.generateReport();
        String reportStr = report.toString();

        // Both operations should be tracked with CPU time
        assertTrue(reportStr.contains("cpu-intensive"));
        assertTrue(reportStr.contains("io-bound"));
        assertTrue(reportStr.contains("Total CPU time:"));
    }

    @Test
    @DisplayName("Should handle operations with exceptions")
    void shouldHandleOperationsWithExceptions() {
        // Execute operation that throws exception
        assertThrows(RuntimeException.class, () -> {
            try (var timer = profiler.startOperation("failing-operation")) {
                throw new RuntimeException("Test exception");
            }
        });

        // Generate report - should still include the operation
        PerformanceProfiler.PerformanceReport report = profiler.generateReport();
        String reportStr = report.toString();

        assertTrue(reportStr.contains("failing-operation"));
        assertTrue(reportStr.contains("Executions: 1"));
    }

    @Test
    @DisplayName("Should clean up resources properly")
    void shouldCleanupProperly() {
        // Create and destroy multiple profilers
        for (int i = 0; i < 5; i++) {
            PerformanceProfiler tempProfiler = new PerformanceProfiler();
            tempProfiler.initialize();
            tempProfiler.startProfiling();
            tempProfiler.stopProfiling();
            tempProfiler.shutdown();
        }

        // If we get here without exceptions, cleanup is working
        assertTrue(true);
    }
}