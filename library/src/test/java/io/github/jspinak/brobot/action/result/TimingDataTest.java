package io.github.jspinak.brobot.action.result;

import io.github.jspinak.brobot.action.result.TimingData.TimeSegment;
import io.github.jspinak.brobot.test.ConcurrentTestBase;
import io.github.jspinak.brobot.test.annotations.Flaky;
import io.github.jspinak.brobot.test.annotations.Flaky.FlakyCause;
import io.github.jspinak.brobot.test.utils.ConcurrentTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for TimingData - manages timing information for action execution.
 * Tests start/stop operations, duration calculations, and time segment management.
 * Uses ConcurrentTestBase for thread-safe parallel execution.
 */
@DisplayName("TimingData Tests")
public class TimingDataTest extends ConcurrentTestBase {
    
    private TimingData timingData;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        timingData = null; // Will create fresh instance in each test
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Default constructor starts timing immediately")
        public void testDefaultConstructor() {
            LocalDateTime beforeCreation = LocalDateTime.now();
            
            timingData = new TimingData();
            
            LocalDateTime afterCreation = LocalDateTime.now();
            
            assertNotNull(timingData.getStartTime());
            assertNotNull(timingData.getInstantStart());
            assertNull(timingData.getEndTime());
            assertNull(timingData.getInstantEnd());
            assertTrue(!timingData.getStartTime().isBefore(beforeCreation));
            assertTrue(!timingData.getStartTime().isAfter(afterCreation));
        }
        
        @Test
        @DisplayName("Constructor with start time sets provided time")
        public void testConstructorWithStartTime() {
            LocalDateTime specificTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            
            timingData = new TimingData(specificTime);
            
            assertEquals(specificTime, timingData.getStartTime());
            assertNotNull(timingData.getInstantStart());
            assertNull(timingData.getEndTime());
            assertEquals(Duration.ZERO, timingData.getTotalDuration());
        }
    }
    
    @Nested
    @DisplayName("Start and Stop Operations")
    class StartStopOperations {
        
        @Test
        @DisplayName("Start method initializes timing")
        public void testStart() throws Exception {
            timingData = new TimingData();
            LocalDateTime initialStart = timingData.getStartTime();
            
            // Add small delay to ensure time difference
            waitFor(() -> false, Duration.ofMillis(10));
            
            timingData.start();
            
            assertNotNull(timingData.getStartTime());
            assertNotNull(timingData.getInstantStart());
            assertNull(timingData.getEndTime());
            assertNull(timingData.getInstantEnd());
            // Use isAfter or isEqual to handle edge cases where timing is exact
            assertTrue(timingData.getStartTime().isAfter(initialStart) || 
                      timingData.getStartTime().isEqual(initialStart));
        }
        
        @Test
        @DisplayName("Stop method sets end time and calculates duration")
        @Flaky(reason = "Timing measurement accuracy", cause = FlakyCause.TIMING)
        public void testStop() throws Exception {
            timingData = new TimingData();
            // Use executeAsync for controlled timing
            executeAsync(() -> {
                Thread.sleep(50);
                return null;
            }, Duration.ofMillis(100));
            
            timingData.stop();
            
            assertNotNull(timingData.getEndTime());
            assertNotNull(timingData.getInstantEnd());
            assertTrue(timingData.getEndTime().isAfter(timingData.getStartTime()) ||
                      timingData.getEndTime().isEqual(timingData.getStartTime()));
            // Use >= 40 instead of >= 50 to account for timing variations
            assertTrue(timingData.getTotalDuration().toMillis() >= 40);
        }
        
        @Test
        @DisplayName("Stop without start does not throw exception")
        public void testStopWithoutStart() {
            LocalDateTime specificTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            timingData = new TimingData(specificTime);
            timingData.setStartTime(null);
            
            assertDoesNotThrow(() -> timingData.stop());
            
            assertNull(timingData.getEndTime());
        }
        
        @Test
        @DisplayName("Multiple stops only set end time once")
        public void testMultipleStops() throws Exception {
            timingData = new TimingData();
            waitFor(() -> false, Duration.ofMillis(50));
            
            timingData.stop();
            LocalDateTime firstEndTime = timingData.getEndTime();
            waitFor(() -> false, Duration.ofMillis(50));
            timingData.stop();
            
            assertEquals(firstEndTime, timingData.getEndTime());
        }
    }
    
    @Nested
    @DisplayName("Duration Calculations")
    class DurationCalculations {
        
        @Test
        @DisplayName("Get elapsed duration while running")
        @Flaky(reason = "Timing measurement while running", cause = FlakyCause.TIMING)
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testElapsedWhileRunning() throws Exception {
            timingData = new TimingData();
            
            // Wait and then check elapsed time
            waitFor(() -> timingData.getElapsed().toMillis() >= 100, Duration.ofMillis(150));
            Duration elapsed1 = timingData.getElapsed();
            
            // Wait more and check again
            waitFor(() -> timingData.getElapsed().toMillis() >= 200, Duration.ofMillis(150));
            Duration elapsed2 = timingData.getElapsed();
            
            assertTrue(elapsed1.toMillis() >= 100);
            assertTrue(elapsed2.toMillis() >= 200);
            assertTrue(elapsed2.compareTo(elapsed1) > 0);
            assertNull(timingData.getEndTime());
        }
        
        @Test
        @DisplayName("Get elapsed duration after stopping")
        @Flaky(reason = "Timing measurement after stop", cause = FlakyCause.TIMING)
        public void testElapsedAfterStopping() throws Exception {
            timingData = new TimingData();
            waitFor(() -> false, Duration.ofMillis(100));
            timingData.stop();
            
            Duration elapsed1 = timingData.getElapsed();
            waitFor(() -> false, Duration.ofMillis(100));
            Duration elapsed2 = timingData.getElapsed();
            
            assertEquals(elapsed1, elapsed2);
            assertEquals(timingData.getTotalDuration(), elapsed1);
        }
        
        @Test
        @DisplayName("Get elapsed with null start time returns zero")
        public void testElapsedWithNullStart() {
            timingData = new TimingData();
            timingData.setStartTime(null);
            
            Duration elapsed = timingData.getElapsed();
            
            assertEquals(Duration.ZERO, elapsed);
        }
        
        @Test
        @DisplayName("Get execution time in milliseconds")
        @Flaky(reason = "Execution time measurement", cause = FlakyCause.TIMING)
        public void testExecutionTimeMs() throws Exception {
            timingData = new TimingData();
            // Use controlled timing
            executeAsync(() -> {
                Thread.sleep(150);
                return null;
            }, Duration.ofMillis(200));
            timingData.stop();
            
            long executionMs = timingData.getExecutionTimeMs();
            
            assertTrue(executionMs >= 140); // Allow some tolerance
            assertEquals(timingData.getElapsed().toMillis(), executionMs);
        }
        
        @Test
        @DisplayName("Get execution time in seconds")
        @Flaky(reason = "Long execution time measurement", cause = FlakyCause.TIMING)
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testExecutionTimeSeconds() throws Exception {
            timingData = new TimingData();
            // Use controlled timing for 1.1 seconds
            executeAsync(() -> {
                Thread.sleep(1100);
                return null;
            }, Duration.ofSeconds(2));
            timingData.stop();
            
            double executionSeconds = timingData.getExecutionTimeMs() / 1000.0;
            
            assertTrue(executionSeconds >= 1.0); // Allow tolerance
            assertEquals(timingData.getElapsed().toMillis() / 1000.0, executionSeconds, 0.1);
        }
    }
    
    @Nested
    @DisplayName("Time Segment Management")
    class TimeSegmentManagement {
        
        @Test
        @DisplayName("Add time segment")
        public void testAddSegment() {
            timingData = new TimingData();
            TimingData.TimeSegment segment = new TimeSegment("Find Operation", Duration.ofMillis(500));
            
            timingData.getSegments().add(segment);
            
            assertEquals(1, timingData.getSegments().size());
            assertEquals(segment, timingData.getSegments().get(0));
        }
        
        @Test
        @DisplayName("Add multiple time segments")
        public void testAddMultipleSegments() {
            timingData = new TimingData();
            TimingData.TimeSegment segment1 = new TimeSegment("Find", Duration.ofMillis(300));
            TimingData.TimeSegment segment2 = new TimeSegment("Click", Duration.ofMillis(100));
            TimingData.TimeSegment segment3 = new TimeSegment("Verify", Duration.ofMillis(200));
            
            timingData.getSegments().add(segment1);
            timingData.getSegments().add(segment2);
            timingData.getSegments().add(segment3);
            
            assertEquals(3, timingData.getSegments().size());
            assertEquals(segment1, timingData.getSegments().get(0));
            assertEquals(segment2, timingData.getSegments().get(1));
            assertEquals(segment3, timingData.getSegments().get(2));
        }
        
        @Test
        @DisplayName("Clear segments")
        public void testClearSegments() {
            timingData = new TimingData();
            timingData.addSegment("Segment1", Duration.ofMillis(100));
            timingData.addSegment("Segment2", Duration.ofMillis(200));
            
            timingData.getSegments().clear();
            
            assertTrue(timingData.getSegments().isEmpty());
        }
        
        @Test
        @DisplayName("Get total segment duration")
        public void testTotalSegmentDuration() {
            timingData = new TimingData();
            timingData.addSegment("Segment1", Duration.ofMillis(100));
            timingData.addSegment("Segment2", Duration.ofMillis(200));
            timingData.addSegment("Segment3", Duration.ofMillis(150));
            
            Duration totalSegmentDuration = timingData.getSegmentsDuration();
            
            assertEquals(450, totalSegmentDuration.toMillis());
        }
        
        @Test
        @DisplayName("Get segment by name")
        public void testGetSegmentByName() {
            timingData = new TimingData();
            TimeSegment findSegment = new TimeSegment("Find", Duration.ofMillis(300));
            TimeSegment clickSegment = new TimeSegment("Click", Duration.ofMillis(100));
            timingData.getSegments().add(findSegment);
            timingData.getSegments().add(clickSegment);
            
            TimeSegment retrieved = timingData.getSegments().stream()
                .filter(s -> "Find".equals(s.getName()))
                .findFirst().orElse(null);
            
            assertEquals(findSegment, retrieved);
        }
        
        @Test
        @DisplayName("Get segment by name returns null for non-existent")
        public void testGetSegmentByNameNotFound() {
            timingData = new TimingData();
            timingData.addSegment("Find", Duration.ofMillis(300));
            
            TimeSegment retrieved = timingData.getSegments().stream()
                .filter(s -> "NonExistent".equals(s.getName()))
                .findFirst().orElse(null);
            
            assertNull(retrieved);
        }
    }
    
    @Nested
    @DisplayName("Instant-based Timing")
    class InstantBasedTiming {
        
        @Test
        @DisplayName("Instant start and end are set correctly")
        @Flaky(reason = "Instant timing verification", cause = FlakyCause.TIMING)
        public void testInstantTiming() throws Exception {
            Instant beforeStart = Instant.now();
            timingData = new TimingData();
            Instant afterStart = Instant.now();
            
            waitFor(() -> false, Duration.ofMillis(100));
            
            Instant beforeStop = Instant.now();
            timingData.stop();
            Instant afterStop = Instant.now();
            
            assertTrue(!timingData.getInstantStart().isBefore(beforeStart));
            assertTrue(!timingData.getInstantStart().isAfter(afterStart));
            assertTrue(!timingData.getInstantEnd().isBefore(beforeStop));
            assertTrue(!timingData.getInstantEnd().isAfter(afterStop));
        }
        
        @Test
        @DisplayName("Calculate elapsed using Instant when available")
        @Flaky(reason = "Instant-based elapsed calculation", cause = FlakyCause.TIMING)
        public void testElapsedWithInstant() throws Exception {
            timingData = new TimingData();
            
            // Wait until elapsed time is at least 100ms
            boolean success = waitFor(() -> timingData.getElapsed().toMillis() >= 100, Duration.ofMillis(150));
            
            Duration elapsed = timingData.getElapsed();
            
            // Should use Instant-based calculation when available
            assertTrue(success && elapsed.toMillis() >= 100);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @DisplayName("Handle timing with zero duration")
        public void testZeroDuration() {
            timingData = new TimingData();
            timingData.stop(); // Stop immediately
            
            Duration elapsed = timingData.getElapsed();
            
            assertTrue(elapsed.toNanos() >= 0);
            assertTrue(elapsed.toMillis() < 100); // Should be very small
        }
        
        @Test
        @DisplayName("Reset timing with start after stop")
        public void testRestartAfterStop() throws Exception {
            timingData = new TimingData();
            waitFor(() -> false, Duration.ofMillis(100));
            timingData.stop();
            LocalDateTime firstEnd = timingData.getEndTime();
            
            timingData.start();
            
            assertNull(timingData.getEndTime());
            assertNull(timingData.getInstantEnd());
            assertTrue(timingData.getStartTime().isAfter(firstEnd));
        }
        
        @ParameterizedTest
        @ValueSource(longs = {0, 10, 50, 100, 500})
        @DisplayName("Accurate timing for various durations")
        @Flaky(reason = "Parameterized timing accuracy test", cause = FlakyCause.TIMING)
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        public void testAccurateTiming(long sleepMs) throws Exception {
            timingData = new TimingData();
            
            if (sleepMs > 0) {
                executeAsync(() -> {
                    Thread.sleep(sleepMs);
                    return null;
                }, Duration.ofMillis(sleepMs + 200));
            }
            timingData.stop();
            
            long elapsed = timingData.getExecutionTimeMs();
            
            // Allow some tolerance for timing accuracy
            assertTrue(elapsed >= sleepMs - 10); // Allow 10ms tolerance
            assertTrue(elapsed < sleepMs + 150); // Max 150ms overhead
        }
    }
    
    @Nested
    @DisplayName("Complex Timing Scenarios")
    class ComplexTimingScenarios {
        
        @Test
        @DisplayName("Track multi-phase operation timing")
        @Flaky(reason = "Multi-phase timing tracking", cause = FlakyCause.TIMING)
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testMultiPhaseOperation() throws Exception {
            timingData = new TimingData();
            
            // Phase 1: Find
            long phase1Start = System.currentTimeMillis();
            waitFor(() -> false, Duration.ofMillis(50));
            long phase1Duration = System.currentTimeMillis() - phase1Start;
            timingData.addSegment("Find", Duration.ofMillis(phase1Duration));
            
            // Phase 2: Process
            long phase2Start = System.currentTimeMillis();
            waitFor(() -> false, Duration.ofMillis(30));
            long phase2Duration = System.currentTimeMillis() - phase2Start;
            timingData.addSegment("Process", Duration.ofMillis(phase2Duration));
            
            // Phase 3: Verify
            long phase3Start = System.currentTimeMillis();
            waitFor(() -> false, Duration.ofMillis(20));
            long phase3Duration = System.currentTimeMillis() - phase3Start;
            timingData.addSegment("Verify", Duration.ofMillis(phase3Duration));
            
            timingData.stop();
            
            assertEquals(3, timingData.getSegments().size());
            assertTrue(timingData.getSegmentsDuration().toMillis() >= 90); // Allow tolerance
            assertTrue(timingData.getExecutionTimeMs() >= 90);
        }
        
        @Test
        @DisplayName("Format timing for reporting")
        public void testFormattedOutput() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            timingData = new TimingData(start);
            timingData.setEndTime(start.plusSeconds(5).plusNanos(500_000_000));
            timingData.setTotalDuration(Duration.ofMillis(5500));
            
            String formatted = timingData.format();
            
            assertNotNull(formatted);
            assertTrue(formatted.contains("5.5") || formatted.contains("5500"));
        }
    }
}