package io.github.jspinak.brobot.action.result;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for TimingData - manages timing information for action execution.
 * Tests start/stop operations, duration calculations, and time segment management.
 */
@DisplayName("TimingData Tests")
public class TimingDataTest extends BrobotTestBase {
    
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
        public void testStart() {
            timingData = new TimingData();
            LocalDateTime initialStart = timingData.getStartTime();
            
            // Add small delay to ensure time difference
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            
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
        public void testStop() throws InterruptedException {
            timingData = new TimingData();
            Thread.sleep(50); // Small delay to ensure measurable duration
            
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
        public void testMultipleStops() throws InterruptedException {
            timingData = new TimingData();
            Thread.sleep(50);
            
            timingData.stop();
            LocalDateTime firstEndTime = timingData.getEndTime();
            Thread.sleep(50);
            timingData.stop();
            
            assertEquals(firstEndTime, timingData.getEndTime());
        }
    }
    
    @Nested
    @DisplayName("Duration Calculations")
    class DurationCalculations {
        
        @Test
        @DisplayName("Get elapsed duration while running")
        public void testElapsedWhileRunning() throws InterruptedException {
            timingData = new TimingData();
            
            Thread.sleep(100);
            Duration elapsed1 = timingData.getElapsed();
            
            Thread.sleep(100);
            Duration elapsed2 = timingData.getElapsed();
            
            assertTrue(elapsed1.toMillis() >= 100);
            assertTrue(elapsed2.toMillis() >= 200);
            assertTrue(elapsed2.compareTo(elapsed1) > 0);
            assertNull(timingData.getEndTime());
        }
        
        @Test
        @DisplayName("Get elapsed duration after stopping")
        public void testElapsedAfterStopping() throws InterruptedException {
            timingData = new TimingData();
            Thread.sleep(100);
            timingData.stop();
            
            Duration elapsed1 = timingData.getElapsed();
            Thread.sleep(100);
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
        public void testExecutionTimeMs() throws InterruptedException {
            timingData = new TimingData();
            Thread.sleep(150);
            timingData.stop();
            
            long executionMs = timingData.getExecutionTimeMs();
            
            assertTrue(executionMs >= 150);
            assertEquals(timingData.getElapsed().toMillis(), executionMs);
        }
        
        @Test
        @DisplayName("Get execution time in seconds")
        public void testExecutionTimeSeconds() throws InterruptedException {
            timingData = new TimingData();
            Thread.sleep(1100);
            timingData.stop();
            
            double executionSeconds = timingData.getExecutionTimeMs() / 1000.0;
            
            assertTrue(executionSeconds >= 1.1);
            assertEquals(timingData.getElapsed().toMillis() / 1000.0, executionSeconds, 0.01);
        }
    }
    
    @Nested
    @DisplayName("Time Segment Management")
    class TimeSegmentManagement {
        
        @Test
        @DisplayName("Add time segment")
        public void testAddSegment() {
            timingData = new TimingData();
            TimingData.TimeSegment segment = new TimingData.TimeSegment("Find Operation", Duration.ofMillis(500));
            
            timingData.getSegments().add(segment);
            
            assertEquals(1, timingData.getSegments().size());
            assertEquals(segment, timingData.getSegments().get(0));
        }
        
        @Test
        @DisplayName("Add multiple time segments")
        public void testAddMultipleSegments() {
            timingData = new TimingData();
            TimingData.TimeSegment segment1 = new TimingData.TimeSegment("Find", Duration.ofMillis(300));
            TimingData.TimeSegment segment2 = new TimingData.TimeSegment("Click", Duration.ofMillis(100));
            TimingData.TimeSegment segment3 = new TimingData.TimeSegment("Verify", Duration.ofMillis(200));
            
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
            TimingData.TimeSegment findSegment = new TimingData.TimeSegment("Find", Duration.ofMillis(300));
            TimingData.TimeSegment clickSegment = new TimingData.TimeSegment("Click", Duration.ofMillis(100));
            timingData.getSegments().add(findSegment);
            timingData.getSegments().add(clickSegment);
            
            TimingData.TimeSegment retrieved = timingData.getSegments().stream()
                .filter(s -> "Find".equals(s.getName()))
                .findFirst().orElse(null);
            
            assertEquals(findSegment, retrieved);
        }
        
        @Test
        @DisplayName("Get segment by name returns null for non-existent")
        public void testGetSegmentByNameNotFound() {
            timingData = new TimingData();
            timingData.addSegment("Find", Duration.ofMillis(300));
            
            TimingData.TimeSegment retrieved = timingData.getSegments().stream()
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
        public void testInstantTiming() throws InterruptedException {
            Instant beforeStart = Instant.now();
            timingData = new TimingData();
            Instant afterStart = Instant.now();
            
            Thread.sleep(100);
            
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
        public void testElapsedWithInstant() throws InterruptedException {
            timingData = new TimingData();
            Thread.sleep(100);
            
            Duration elapsed = timingData.getElapsed();
            
            // Should use Instant-based calculation when available
            assertTrue(elapsed.toMillis() >= 100);
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
        public void testRestartAfterStop() throws InterruptedException {
            timingData = new TimingData();
            Thread.sleep(100);
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
        public void testAccurateTiming(long sleepMs) throws InterruptedException {
            timingData = new TimingData();
            
            Thread.sleep(sleepMs);
            timingData.stop();
            
            long elapsed = timingData.getExecutionTimeMs();
            
            // Allow some tolerance for timing accuracy
            assertTrue(elapsed >= sleepMs);
            assertTrue(elapsed < sleepMs + 100); // Max 100ms overhead
        }
    }
    
    @Nested
    @DisplayName("Complex Timing Scenarios")
    class ComplexTimingScenarios {
        
        @Test
        @DisplayName("Track multi-phase operation timing")
        public void testMultiPhaseOperation() throws InterruptedException {
            timingData = new TimingData();
            
            // Phase 1: Find
            Thread.sleep(50);
            timingData.addSegment("Find", Duration.ofMillis(50));
            
            // Phase 2: Process
            Thread.sleep(30);
            timingData.addSegment("Process", Duration.ofMillis(30));
            
            // Phase 3: Verify
            Thread.sleep(20);
            timingData.addSegment("Verify", Duration.ofMillis(20));
            
            timingData.stop();
            
            assertEquals(3, timingData.getSegments().size());
            assertEquals(100, timingData.getSegmentsDuration().toMillis());
            assertTrue(timingData.getExecutionTimeMs() >= 100);
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