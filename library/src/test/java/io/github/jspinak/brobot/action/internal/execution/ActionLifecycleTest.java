package io.github.jspinak.brobot.action.internal.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ActionLifecycleTest {

    private LocalDateTime testStartTime;
    private ActionLifecycle actionLifecycle;
    
    @BeforeEach
    void setUp() {
        testStartTime = LocalDateTime.now();
    }
    
    @Test
    void testConstructor_WithPositiveMaxWait() {
        // Setup
        double maxWait = 5.0; // 5 seconds
        
        // Execute
        actionLifecycle = new ActionLifecycle(testStartTime, maxWait);
        
        // Verify
        assertEquals(testStartTime, actionLifecycle.getStartTime());
        assertNull(actionLifecycle.getEndTime());
        assertEquals(0, actionLifecycle.getCompletedRepetitions());
        assertEquals(0, actionLifecycle.getCompletedSequences());
        assertFalse(actionLifecycle.isPrinted());
        
        // Check allowed end time is correctly calculated
        long expectedNanos = (long) (maxWait * Math.pow(10, 9));
        LocalDateTime expectedEndTime = testStartTime.plusNanos(expectedNanos);
        assertEquals(expectedEndTime, actionLifecycle.getAllowedEndTime());
    }
    
    @Test
    void testConstructor_WithZeroMaxWait() {
        // Setup
        double maxWait = 0.0;
        
        // Execute
        actionLifecycle = new ActionLifecycle(testStartTime, maxWait);
        
        // Verify
        assertEquals(testStartTime, actionLifecycle.getStartTime());
        assertEquals(testStartTime, actionLifecycle.getAllowedEndTime());
    }
    
    @Test
    void testConstructor_WithFractionalMaxWait() {
        // Setup
        double maxWait = 1.5; // 1.5 seconds
        
        // Execute
        actionLifecycle = new ActionLifecycle(testStartTime, maxWait);
        
        // Verify
        long expectedNanos = 1_500_000_000L; // 1.5 seconds in nanoseconds
        LocalDateTime expectedEndTime = testStartTime.plusNanos(expectedNanos);
        assertEquals(expectedEndTime, actionLifecycle.getAllowedEndTime());
    }
    
    @Test
    void testConstructor_WithVerySmallMaxWait() {
        // Setup
        double maxWait = 0.001; // 1 millisecond
        
        // Execute
        actionLifecycle = new ActionLifecycle(testStartTime, maxWait);
        
        // Verify
        long expectedNanos = 1_000_000L; // 1 millisecond in nanoseconds
        LocalDateTime expectedEndTime = testStartTime.plusNanos(expectedNanos);
        assertEquals(expectedEndTime, actionLifecycle.getAllowedEndTime());
    }
    
    @Test
    void testIncrementCompletedRepetitions() {
        // Setup
        actionLifecycle = new ActionLifecycle(testStartTime, 10.0);
        assertEquals(0, actionLifecycle.getCompletedRepetitions());
        
        // Execute & Verify
        actionLifecycle.incrementCompletedRepetitions();
        assertEquals(1, actionLifecycle.getCompletedRepetitions());
        
        actionLifecycle.incrementCompletedRepetitions();
        assertEquals(2, actionLifecycle.getCompletedRepetitions());
        
        actionLifecycle.incrementCompletedRepetitions();
        assertEquals(3, actionLifecycle.getCompletedRepetitions());
    }
    
    @Test
    void testIncrementCompletedSequences() {
        // Setup
        actionLifecycle = new ActionLifecycle(testStartTime, 10.0);
        assertEquals(0, actionLifecycle.getCompletedSequences());
        
        // Execute & Verify
        actionLifecycle.incrementCompletedSequences();
        assertEquals(1, actionLifecycle.getCompletedSequences());
        
        actionLifecycle.incrementCompletedSequences();
        assertEquals(2, actionLifecycle.getCompletedSequences());
    }
    
    @Test
    void testSetEndTime() {
        // Setup
        actionLifecycle = new ActionLifecycle(testStartTime, 10.0);
        LocalDateTime endTime = testStartTime.plusSeconds(3);
        
        // Execute
        actionLifecycle.setEndTime(endTime);
        
        // Verify
        assertEquals(endTime, actionLifecycle.getEndTime());
    }
    
    @Test
    void testSetPrinted() {
        // Setup
        actionLifecycle = new ActionLifecycle(testStartTime, 10.0);
        assertFalse(actionLifecycle.isPrinted());
        
        // Execute
        actionLifecycle.setPrinted(true);
        
        // Verify
        assertTrue(actionLifecycle.isPrinted());
    }
    
    @Test
    void testCompleteLifecycleScenario() {
        // Setup
        double maxWait = 30.0;
        actionLifecycle = new ActionLifecycle(testStartTime, maxWait);
        
        // Simulate action execution
        for (int i = 0; i < 5; i++) {
            actionLifecycle.incrementCompletedRepetitions();
        }
        actionLifecycle.incrementCompletedSequences();
        
        // Simulate another sequence
        for (int i = 0; i < 3; i++) {
            actionLifecycle.incrementCompletedRepetitions();
        }
        actionLifecycle.incrementCompletedSequences();
        
        // Set end time
        LocalDateTime endTime = testStartTime.plusSeconds(15);
        actionLifecycle.setEndTime(endTime);
        actionLifecycle.setPrinted(true);
        
        // Verify final state
        assertEquals(8, actionLifecycle.getCompletedRepetitions());
        assertEquals(2, actionLifecycle.getCompletedSequences());
        assertEquals(endTime, actionLifecycle.getEndTime());
        assertTrue(actionLifecycle.isPrinted());
        
        // Verify end time is before allowed end time
        assertTrue(actionLifecycle.getEndTime().isBefore(actionLifecycle.getAllowedEndTime()));
    }
    
    @Test
    void testPreciseNanoCalculation() {
        // Setup - test with a value that would lose precision with float
        double maxWait = 0.123456789;
        
        // Execute
        actionLifecycle = new ActionLifecycle(testStartTime, maxWait);
        
        // Verify
        long expectedNanos = 123456789L;
        LocalDateTime expectedEndTime = testStartTime.plusNanos(expectedNanos);
        
        // Check the nanosecond precision is maintained
        long actualNanosDiff = ChronoUnit.NANOS.between(testStartTime, actionLifecycle.getAllowedEndTime());
        assertEquals(expectedNanos, actualNanosDiff);
    }
}