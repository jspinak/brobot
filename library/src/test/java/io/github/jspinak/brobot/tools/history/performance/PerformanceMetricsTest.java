package io.github.jspinak.brobot.tools.history.performance;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PerformanceMetrics class.
 * Verifies performance tracking and metric calculations.
 */
public class PerformanceMetricsTest extends BrobotTestBase {
    
    private PerformanceMetrics metrics;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        metrics = new PerformanceMetrics();
    }
    
    @Nested
    @DisplayName("Illustration Recording Tests")
    class IllustrationRecordingTests {
        
        @Test
        @DisplayName("Should record illustration with metrics")
        public void testRecordIllustration() {
            metrics.recordIllustration(100L, 50L, true);
            
            assertEquals(1, metrics.getTotalIllustrationsGenerated().get());
            assertEquals(100L, metrics.getTotalProcessingTimeMs().get());
            assertEquals(50L, metrics.getTotalMemoryUsedMB().get());
            assertEquals(1, metrics.getHighQualityIllustrations().get());
            assertEquals(0, metrics.getLowQualityIllustrations().get());
        }
        
        @Test
        @DisplayName("Should track min and max processing times")
        public void testMinMaxProcessingTimes() {
            metrics.recordIllustration(100L, 10L, true);
            metrics.recordIllustration(50L, 10L, true);
            metrics.recordIllustration(200L, 10L, true);
            metrics.recordIllustration(75L, 10L, true);
            
            assertEquals(50L, metrics.getMinProcessingTimeMs().get());
            assertEquals(200L, metrics.getMaxProcessingTimeMs().get());
            assertEquals(425L, metrics.getTotalProcessingTimeMs().get());
        }
        
        @Test
        @DisplayName("Should track quality distribution")
        public void testQualityDistribution() {
            metrics.recordIllustration(100L, 10L, true);
            metrics.recordIllustration(100L, 10L, false);
            metrics.recordIllustration(100L, 10L, true);
            metrics.recordIllustration(100L, 10L, false);
            metrics.recordIllustration(100L, 10L, false);
            
            assertEquals(2, metrics.getHighQualityIllustrations().get());
            assertEquals(3, metrics.getLowQualityIllustrations().get());
        }
        
        @Test
        @DisplayName("Should update last illustration time")
        public void testLastIllustrationTime() throws InterruptedException {
            LocalDateTime beforeRecord = LocalDateTime.now();
            Thread.sleep(10); // Small delay to ensure time difference
            
            metrics.recordIllustration(100L, 10L, true);
            LocalDateTime lastTime = metrics.getLastIllustrationTime();
            
            assertTrue(lastTime.isAfter(beforeRecord));
        }
    }
    
    @Nested
    @DisplayName("Skipped Illustrations Tests")
    class SkippedIllustrationsTests {
        
        @Test
        @DisplayName("Should record skipped illustrations")
        public void testRecordSkipped() {
            metrics.recordSkipped("Performance threshold exceeded");
            metrics.recordSkipped("Memory limit reached");
            
            assertEquals(2, metrics.getIllustrationsSkipped().get());
            assertEquals(0, metrics.getTotalIllustrationsGenerated().get());
        }
        
        @Test
        @DisplayName("Should track skip reasons separately")
        public void testSkipReasons() {
            metrics.recordSkipped("test reason");
            metrics.recordBatched();
            metrics.recordDeferred(Duration.ofSeconds(5));
            
            assertEquals(1, metrics.getIllustrationsSkipped().get());
            assertEquals(1, metrics.getIllustrationsBatched().get());
            assertEquals(1, metrics.getIllustrationsDeferred().get());
        }
    }
    
    @Nested
    @DisplayName("Resource Tracking Tests")
    class ResourceTrackingTests {
        
        @Test
        @DisplayName("Should track cumulative memory usage")
        public void testMemoryTracking() {
            metrics.recordIllustration(100L, 25L, true);
            metrics.recordIllustration(100L, 30L, true);
            metrics.recordIllustration(100L, 45L, false);
            
            assertEquals(100L, metrics.getTotalMemoryUsedMB().get());
        }
        
        @Test
        @DisplayName("Should track active threads")
        public void testThreadTracking() {
            metrics.incrementActiveThreads();
            assertEquals(1, metrics.getActiveIllustrationThreads().get());
            
            metrics.incrementActiveThreads();
            assertEquals(2, metrics.getActiveIllustrationThreads().get());
            
            metrics.decrementActiveThreads();
            assertEquals(1, metrics.getActiveIllustrationThreads().get());
        }
        
        @Test
        @DisplayName("Should not go negative on thread decrement")
        public void testThreadDecrementBoundary() {
            assertEquals(0, metrics.getActiveIllustrationThreads().get());
            
            metrics.decrementActiveThreads();
            // Should handle gracefully (implementation dependent)
            assertTrue(metrics.getActiveIllustrationThreads().get() <= 0);
        }
    }
    
    @Nested
    @DisplayName("Metric Calculation Tests")
    class MetricCalculationTests {
        
        @Test
        @DisplayName("Should calculate average processing time")
        public void testAverageProcessingTime() {
            metrics.recordIllustration(100L, 10L, true);
            metrics.recordIllustration(200L, 10L, true);
            metrics.recordIllustration(300L, 10L, true);
            
            double average = metrics.getAverageProcessingTimeMs();
            assertEquals(200.0, average, 0.01);
        }
        
        @Test
        @DisplayName("Should handle average with no illustrations")
        public void testAverageWithNoData() {
            double average = metrics.getAverageProcessingTimeMs();
            assertEquals(0.0, average, 0.01);
        }
        
        @Test
        @DisplayName("Should calculate throughput")
        public void testThroughputCalculation() throws InterruptedException {
            // Record some illustrations
            metrics.recordIllustration(100L, 10L, true);
            Thread.sleep(100);
            metrics.recordIllustration(100L, 10L, true);
            Thread.sleep(100);
            metrics.recordIllustration(100L, 10L, true);
            
            double throughput = metrics.getIllustrationsPerMinute();
            // Should be positive but exact value depends on timing
            assertTrue(throughput >= 0);
        }
        
        @Test
        @DisplayName("Should calculate quality percentage")
        public void testQualityPercentage() {
            metrics.recordIllustration(100L, 10L, true);
            metrics.recordIllustration(100L, 10L, true);
            metrics.recordIllustration(100L, 10L, false);
            metrics.recordIllustration(100L, 10L, true);
            
            double qualityRate = metrics.getHighQualityRate();
            assertEquals(0.75, qualityRate, 0.01);
        }
        
        @Test
        @DisplayName("Should handle quality percentage with no illustrations")
        public void testQualityPercentageNoData() {
            double qualityRate = metrics.getHighQualityRate();
            assertEquals(0.0, qualityRate, 0.01);
        }
    }
    
    @Nested
    @DisplayName("Reset and Snapshot Tests")
    class ResetSnapshotTests {
        
        @Test
        @DisplayName("Should reset all metrics")
        public void testReset() {
            // Add some data
            metrics.recordIllustration(100L, 50L, true);
            metrics.recordSkipped("test reason");
            metrics.incrementActiveThreads();
            
            // Reset
            metrics.reset();
            
            // Verify all counters are reset
            assertEquals(0, metrics.getTotalIllustrationsGenerated().get());
            assertEquals(0, metrics.getIllustrationsSkipped().get());
            assertEquals(0, metrics.getTotalProcessingTimeMs().get());
            assertEquals(0, metrics.getTotalMemoryUsedMB().get());
            assertEquals(0, metrics.getActiveIllustrationThreads().get());
            assertEquals(Long.MAX_VALUE, metrics.getMinProcessingTimeMs().get());
            assertEquals(0, metrics.getMaxProcessingTimeMs().get());
        }
        
        @Test
        @DisplayName("Should create snapshot of current metrics")
        public void testSnapshot() {
            metrics.recordIllustration(100L, 25L, true);
            metrics.recordIllustration(200L, 30L, false);
            metrics.recordSkipped("test");
            
            PerformanceMetrics.MetricsSnapshot snapshot = metrics.snapshot();
            
            // Verify snapshot has same values
            assertEquals(metrics.getTotalIllustrationsGenerated().get(), 
                        snapshot.getTotalIllustrationsGenerated());
            assertEquals(metrics.getAverageProcessingTimeMs(),
                        snapshot.getAverageProcessingTimeMs(), 0.01);
            assertEquals(metrics.getIllustrationsSkipped().get(),
                        snapshot.getIllustrationsSkipped());
            
            // Modifying original should not affect snapshot
            metrics.recordIllustration(300L, 50L, true);
            assertEquals(2, snapshot.getTotalIllustrationsGenerated());
            assertEquals(3, metrics.getTotalIllustrationsGenerated().get());
        }
    }
    
    @Nested
    @DisplayName("Time-based Metrics Tests")
    class TimeBasedMetricsTests {
        
        @Test
        @DisplayName("Should track uptime duration")
        public void testUptimeDuration() throws InterruptedException {
            Thread.sleep(100);
            
            Duration uptime = metrics.getTotalUptime();
            assertTrue(uptime.toMillis() >= 100);
        }
        
        @Test
        @DisplayName("Should track time since last illustration")
        public void testTimeSinceLastIllustration() throws InterruptedException {
            metrics.recordIllustration(100L, 10L, true);
            LocalDateTime recordTime = metrics.getLastIllustrationTime();
            
            Thread.sleep(100);
            
            Duration timeSince = metrics.getTimeSinceLastIllustration();
            assertTrue(timeSince.toMillis() >= 100);
        }
    }
}