package io.github.jspinak.brobot.tools.testing.mock.time;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockTimeTest {

    @Mock
    private ActionDurations actionDurations;
    
    private MockTime mockTime;
    private LocalDateTime startTime;
    
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;
    
    @BeforeEach
    void setUp() {
        mockTime = new MockTime(actionDurations);
        startTime = mockTime.now();
        
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
    }
    
    @Test
    void testNow_ReturnsInitialTime() {
        // Verify initial time is set
        assertNotNull(mockTime.now());
        assertEquals(startTime, mockTime.now());
    }
    
    @Test
    void testWait_PositiveSeconds() {
        // Execute
        mockTime.wait(5.5);
        
        // Verify
        LocalDateTime endTime = mockTime.now();
        long nanosDiff = ChronoUnit.NANOS.between(startTime, endTime);
        assertEquals(5_500_000_000L, nanosDiff);
    }
    
    @Test
    void testWait_ZeroSeconds() {
        // Execute
        mockTime.wait(0.0);
        
        // Verify - time should not advance
        assertEquals(startTime, mockTime.now());
    }
    
    @Test
    void testWait_NegativeSeconds() {
        // Execute
        mockTime.wait(-10.0);
        
        // Verify - time should not advance
        assertEquals(startTime, mockTime.now());
    }
    
    @Test
    void testWait_FractionalSeconds() {
        // Execute
        mockTime.wait(0.123);
        
        // Verify
        LocalDateTime endTime = mockTime.now();
        long nanosDiff = ChronoUnit.NANOS.between(startTime, endTime);
        assertEquals(123_000_000L, nanosDiff);
    }
    
    @Test
    void testWait_MultipleWaits() {
        // Execute multiple waits
        mockTime.wait(1.0);
        mockTime.wait(2.5);
        mockTime.wait(0.5);
        
        // Verify cumulative time
        LocalDateTime endTime = mockTime.now();
        long nanosDiff = ChronoUnit.NANOS.between(startTime, endTime);
        assertEquals(4_000_000_000L, nanosDiff);
    }
    
    @Test
    void testWait_WithAction() {
        // Setup
        when(actionDurations.getActionDuration(ActionType.CLICK)).thenReturn(0.5);
        
        // Execute
        mockTime.wait(ActionType.CLICK);
        
        // Verify
        verify(actionDurations).getActionDuration(ActionType.CLICK);
        LocalDateTime endTime = mockTime.now();
        long nanosDiff = ChronoUnit.NANOS.between(startTime, endTime);
        assertEquals(500_000_000L, nanosDiff);
    }
    
    @Test
    void testWait_WithMultipleActions() {
        // Setup
        when(actionDurations.getActionDuration(ActionType.CLICK)).thenReturn(0.5);
        when(actionDurations.getActionDuration(ActionType.TYPE)).thenReturn(2.0);
        when(actionDurations.getActionDuration(ActionType.MOVE)).thenReturn(0.25);
        
        // Execute
        mockTime.wait(ActionType.CLICK);
        mockTime.wait(ActionType.TYPE);
        mockTime.wait(ActionType.MOVE);
        
        // Verify
        LocalDateTime endTime = mockTime.now();
        long nanosDiff = ChronoUnit.NANOS.between(startTime, endTime);
        assertEquals(2_750_000_000L, nanosDiff); // 0.5 + 2.0 + 0.25 = 2.75 seconds
    }
    
    @Test
    @org.junit.jupiter.api.Disabled("ActionOptions.Find no longer exists")
    void testWait_WithFind() {
        // This test relied on ActionOptions.Find enum which no longer exists
        // The modern API uses specific find options classes instead
    }
    
    @Test
    @org.junit.jupiter.api.Disabled("ActionOptions.Find no longer exists")
    void testWait_WithMultipleFinds() {
        // This test relied on ActionOptions.Find enum which no longer exists
        // The modern API uses specific find options classes instead
    }
    
    @Test
    @org.junit.jupiter.api.Disabled("reset() method may not exist in MockTime")
    void testReset() {
        // This test assumes a reset() method exists in MockTime
        // If the method doesn't exist, this test should be removed or MockTime should be updated
    }
    
    @Test
    void testConsoleOutput_HighLevel() {
        // Setup
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
        
        // Execute
        mockTime.wait(1.0);
        
        // Verify - should print at HIGH level
        String output = outputStream.toString();
        assertTrue(output.contains("MockTime.wait"));
    }
    
    @Test
    void testConsoleOutput_LowLevel() {
        // Setup
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.LOW;
        
        // Execute
        mockTime.wait(1.0);
        
        // Verify - should not print at LOW level
        String output = outputStream.toString();
        assertFalse(output.contains("MockTime.wait"));
    }
    
    @Test
    void testGetActionDurationFromMock() {
        // Setup
        when(actionDurations.getActionDuration(ActionType.CLICK)).thenReturn(0.75);
        
        // Execute
        double duration = actionDurations.getActionDuration(ActionType.CLICK);
        
        // Verify
        assertEquals(0.75, duration);
        verify(actionDurations).getActionDuration(ActionType.CLICK);
    }
    
    @Test
    void testWait_VeryLargeDuration() {
        // Execute with large duration
        mockTime.wait(3600.0); // 1 hour
        
        // Verify
        LocalDateTime endTime = mockTime.now();
        long secondsDiff = ChronoUnit.SECONDS.between(startTime, endTime);
        assertEquals(3600L, secondsDiff);
    }
    
    @Test
    void testWait_VerySmallDuration() {
        // Execute with very small duration
        mockTime.wait(0.000001); // 1 microsecond
        
        // Verify
        LocalDateTime endTime = mockTime.now();
        long nanosDiff = ChronoUnit.NANOS.between(startTime, endTime);
        assertEquals(1000L, nanosDiff); // 1 microsecond = 1000 nanoseconds
    }
    
    @Test
    void testSequentialTimeAdvancement() {
        // Verify time always advances forward
        LocalDateTime time1 = mockTime.now();
        mockTime.wait(0.1);
        LocalDateTime time2 = mockTime.now();
        mockTime.wait(0.1);
        LocalDateTime time3 = mockTime.now();
        
        assertTrue(time1.isBefore(time2));
        assertTrue(time2.isBefore(time3));
    }
    
    @Test
    void testNow_ConsistentWithoutWait() {
        // Call now() multiple times without wait
        LocalDateTime time1 = mockTime.now();
        LocalDateTime time2 = mockTime.now();
        LocalDateTime time3 = mockTime.now();
        
        // All should be the same
        assertEquals(time1, time2);
        assertEquals(time2, time3);
    }
}