package io.github.jspinak.brobot.tools.testing.mock.time;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
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
        when(actionDurations.getActionDuration(ActionOptions.Action.CLICK)).thenReturn(0.5);
        
        // Execute
        mockTime.wait(ActionOptions.Action.CLICK);
        
        // Verify
        verify(actionDurations).getActionDuration(ActionOptions.Action.CLICK);
        LocalDateTime endTime = mockTime.now();
        long nanosDiff = ChronoUnit.NANOS.between(startTime, endTime);
        assertEquals(500_000_000L, nanosDiff);
    }
    
    @Test
    void testWait_WithMultipleActions() {
        // Setup
        when(actionDurations.getActionDuration(ActionOptions.Action.CLICK)).thenReturn(0.5);
        when(actionDurations.getActionDuration(ActionOptions.Action.TYPE)).thenReturn(2.0);
        when(actionDurations.getActionDuration(ActionOptions.Action.MOVE)).thenReturn(0.25);
        
        // Execute
        mockTime.wait(ActionOptions.Action.CLICK);
        mockTime.wait(ActionOptions.Action.TYPE);
        mockTime.wait(ActionOptions.Action.MOVE);
        
        // Verify
        LocalDateTime endTime = mockTime.now();
        long nanosDiff = ChronoUnit.NANOS.between(startTime, endTime);
        assertEquals(2_750_000_000L, nanosDiff); // 0.5 + 2.0 + 0.25 = 2.75 seconds
    }
    
    @Test
    void testWait_WithFind() {
        // Setup
        when(actionDurations.getFindDuration(ActionOptions.Find.UNIVERSAL)).thenReturn(1.5);
        
        // Execute
        mockTime.wait(ActionOptions.Find.UNIVERSAL);
        
        // Verify
        verify(actionDurations).getFindDuration(ActionOptions.Find.UNIVERSAL);
        LocalDateTime endTime = mockTime.now();
        long nanosDiff = ChronoUnit.NANOS.between(startTime, endTime);
        assertEquals(1_500_000_000L, nanosDiff);
    }
    
    @Test
    void testWait_ConsoleOutput_HighLevel() {
        // Setup
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
        
        // Execute
        mockTime.wait(3.7);
        
        // Verify console output
        String output = outputStream.toString();
        assertTrue(output.contains("wait-3.7"));
    }
    
    @Test
    void testWait_ConsoleOutput_LowLevel() {
        // Setup
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.LOW;
        
        // Execute
        mockTime.wait(3.7);
        
        // Verify no console output at LOW level
        String output = outputStream.toString();
        assertTrue(output.isEmpty());
    }
    
    @Test
    void testWait_LargeTimeValue() {
        // Execute - wait for 1 hour
        mockTime.wait(3600.0);
        
        // Verify
        LocalDateTime endTime = mockTime.now();
        long hoursDiff = ChronoUnit.HOURS.between(startTime, endTime);
        assertEquals(1, hoursDiff);
    }
    
    @Test
    void testNow_Immutability() {
        // Get reference to current time
        LocalDateTime time1 = mockTime.now();
        
        // Wait to advance time
        mockTime.wait(1.0);
        
        // Get new reference
        LocalDateTime time2 = mockTime.now();
        
        // Verify time1 hasn't changed (immutability)
        assertNotEquals(time1, time2);
        assertEquals(1_000_000_000L, ChronoUnit.NANOS.between(time1, time2));
    }
    
    @Test
    void testWait_VerySmallTime() {
        // Execute - wait for 1 nanosecond
        mockTime.wait(0.000000001);
        
        // Verify
        LocalDateTime endTime = mockTime.now();
        long nanosDiff = ChronoUnit.NANOS.between(startTime, endTime);
        assertEquals(1L, nanosDiff);
    }
}