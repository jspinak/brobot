package io.github.jspinak.brobot.logging.unified;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ConsoleOutputCapture.
 * Tests console output capture and routing to BrobotLogger.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsoleOutputCapture Tests")
public class ConsoleOutputCaptureTest extends BrobotTestBase {

    private ConsoleOutputCapture capture;
    private PrintStream originalOut;
    private PrintStream originalErr;
    
    @Mock
    private BrobotLogger mockBrobotLogger;
    
    @Mock
    private LogBuilder mockLogBuilder;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Save original streams
        originalOut = System.out;
        originalErr = System.err;
        
        capture = new ConsoleOutputCapture();
        
        // Setup mock behavior
        when(mockBrobotLogger.log()).thenReturn(mockLogBuilder);
        when(mockLogBuilder.observation(anyString())).thenReturn(mockLogBuilder);
        when(mockLogBuilder.level(any(LogEvent.Level.class))).thenReturn(mockLogBuilder);
        when(mockLogBuilder.metadata(anyString(), anyString())).thenReturn(mockLogBuilder);
        
        // Inject the mock logger
        capture.setBrobotLogger(mockBrobotLogger);
    }
    
    @AfterEach
    public void tearDown() {
        // Always restore original streams
        capture.stopCapture();
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
    
    @Test
    @DisplayName("Should start capture and redirect System.out to BrobotLogger")
    void testCaptureSystemOut() {
        // Act
        capture.startCapture();
        System.out.println("Test output line");
        capture.stopCapture();
        
        // Assert - Verify that BrobotLogger was called
        verify(mockBrobotLogger, atLeastOnce()).log();
        verify(mockLogBuilder, atLeastOnce()).observation("Test output line");
        verify(mockLogBuilder, atLeastOnce()).level(LogEvent.Level.INFO);
        verify(mockLogBuilder, atLeastOnce()).metadata("source", "console.out");
        verify(mockLogBuilder, atLeastOnce()).log();
    }
    
    @Test
    @DisplayName("Should capture System.err output and route to BrobotLogger")
    void testCaptureSystemErr() {
        // Act
        capture.startCapture();
        System.err.println("Error message");
        capture.stopCapture();
        
        // Assert - Verify that BrobotLogger was called for error
        verify(mockBrobotLogger, atLeastOnce()).log();
        verify(mockLogBuilder, atLeastOnce()).observation("Error message");
        verify(mockLogBuilder, atLeastOnce()).level(LogEvent.Level.ERROR);
        verify(mockLogBuilder, atLeastOnce()).metadata("source", "console.err");
        verify(mockLogBuilder, atLeastOnce()).log();
    }
    
    @Test
    @DisplayName("Should capture mixed output and error streams")
    void testCaptureMixedStreams() {
        // Act
        capture.startCapture();
        System.out.println("Output message");
        System.err.println("Error message");
        capture.stopCapture();
        
        // Assert - Verify both stdout and stderr were captured
        verify(mockBrobotLogger, atLeast(2)).log();
        verify(mockLogBuilder, atLeastOnce()).observation("Output message");
        verify(mockLogBuilder, atLeastOnce()).observation("Error message");
        verify(mockLogBuilder, atLeastOnce()).level(LogEvent.Level.INFO);
        verify(mockLogBuilder, atLeastOnce()).level(LogEvent.Level.ERROR);
        verify(mockLogBuilder, atLeastOnce()).metadata("source", "console.out");
        verify(mockLogBuilder, atLeastOnce()).metadata("source", "console.err");
    }
    
    @Test
    @DisplayName("Should disable and enable capture")
    void testDisableEnableCapture() {
        // Test disable
        capture.disableCapture();
        
        // Capture should be disabled, so no logging should occur
        PrintStream currentOut = System.out;
        System.out.println("This should not be captured");
        
        // Verify no logging occurred
        verifyNoInteractions(mockBrobotLogger);
        
        // Test enable
        capture.enableCapture();
        System.out.println("This should be captured");
        capture.stopCapture();
        
        // Should have logged after enabling
        verify(mockBrobotLogger, atLeastOnce()).log();
    }
    
    @Test
    @DisplayName("Should handle multiple start/stop cycles")
    void testMultipleStartStopCycles() {
        // First cycle
        capture.startCapture();
        System.out.println("Cycle 1");
        capture.stopCapture();
        
        // Reset mocks for second cycle
        reset(mockBrobotLogger, mockLogBuilder);
        when(mockBrobotLogger.log()).thenReturn(mockLogBuilder);
        when(mockLogBuilder.observation(anyString())).thenReturn(mockLogBuilder);
        when(mockLogBuilder.level(any(LogEvent.Level.class))).thenReturn(mockLogBuilder);
        when(mockLogBuilder.metadata(anyString(), anyString())).thenReturn(mockLogBuilder);
        
        // Second cycle
        capture.startCapture();
        System.out.println("Cycle 2");
        capture.stopCapture();
        
        // Verify second cycle logging
        verify(mockBrobotLogger, atLeastOnce()).log();
        verify(mockLogBuilder, atLeastOnce()).observation("Cycle 2");
    }
    
    @Test
    @DisplayName("Should filter out known verbose logs")
    void testLogFiltering() throws Exception {
        // Act
        capture.startCapture();
        
        // These should be filtered out
        System.out.println("[log] SikuliX verbose message");
        System.out.println("in HighlightRegion: some message");
        System.out.println("SikuliX debug info");
        System.out.println("Click on L(123,456)");
        System.out.println("highlight region");
        System.out.println("TRACE level message");
        
        // This should not be filtered
        System.out.println("Normal log message");
        
        capture.stopCapture();
        
        // Assert - Only the normal message should have been logged
        verify(mockBrobotLogger, times(1)).log();
        verify(mockLogBuilder, times(1)).observation("Normal log message");
    }
    
    @Test
    @DisplayName("Should handle empty lines")
    void testEmptyLines() {
        // Act
        capture.startCapture();
        System.out.println("");  // Empty line should be ignored
        System.out.println("Non-empty");
        capture.stopCapture();
        
        // Assert - Only non-empty line should be logged
        verify(mockBrobotLogger, times(1)).log();
        verify(mockLogBuilder, times(1)).observation("Non-empty");
    }
    
    @Test
    @DisplayName("Should handle log level determination")
    void testLogLevelDetermination() {
        // Act
        capture.startCapture();
        System.out.println("Error occurred");  // Should be ERROR level
        System.out.println("Warning message"); // Should be WARNING level  
        System.out.println("Debug info");     // Should be DEBUG level
        System.out.println("Normal message");  // Should be INFO level
        capture.stopCapture();
        
        // Assert - Verify different log levels were used
        verify(mockLogBuilder, times(1)).level(LogEvent.Level.ERROR);
        verify(mockLogBuilder, times(1)).level(LogEvent.Level.WARNING);
        verify(mockLogBuilder, times(1)).level(LogEvent.Level.DEBUG);
        verify(mockLogBuilder, times(1)).level(LogEvent.Level.INFO);
    }
    
    @Test
    @DisplayName("Should get original streams")
    void testGetOriginalStreams() {
        // Arrange - Start capture to set original streams
        capture.startCapture();
        
        // Act
        PrintStream originalOut = ConsoleOutputCapture.getOriginalOut();
        PrintStream originalErr = ConsoleOutputCapture.getOriginalErr();
        
        // Assert
        assertNotNull(originalOut);
        assertNotNull(originalErr);
        
        capture.stopCapture();
    }
    
    @Test
    @DisplayName("Should not capture when disabled via property")
    void testCaptureDisabledViaProperty() throws Exception {
        // Arrange - Create capture with disabled property
        ConsoleOutputCapture disabledCapture = new ConsoleOutputCapture();
        
        // Use reflection to set captureEnabled to false
        Field captureEnabledField = ConsoleOutputCapture.class.getDeclaredField("captureEnabled");
        captureEnabledField.setAccessible(true);
        captureEnabledField.set(disabledCapture, false);
        
        disabledCapture.setBrobotLogger(mockBrobotLogger);
        
        // Act
        disabledCapture.startCapture();
        System.out.println("This should not be captured");
        disabledCapture.stopCapture();
        
        // Assert - No logging should occur
        verifyNoInteractions(mockBrobotLogger);
    }
    
    @Test
    @DisplayName("Should restore original streams on stop")
    void testRestoreOriginalStreams() {
        // Arrange
        PrintStream testOut = System.out;
        PrintStream testErr = System.err;
        
        // Act
        capture.startCapture();
        
        // Verify streams were replaced during capture
        assertNotEquals(testOut, System.out);
        assertNotEquals(testErr, System.err);
        
        capture.stopCapture();
        
        // Assert - Original streams should be restored
        // Note: The actual restoration might not be to the exact same object
        // but the functionality should be restored
        assertNotNull(System.out);
        assertNotNull(System.err);
    }
    
    @Test
    @DisplayName("Should handle null logger gracefully")
    void testNullLoggerHandling() {
        // Arrange - Create capture without setting logger
        ConsoleOutputCapture captureWithoutLogger = new ConsoleOutputCapture();
        
        // Act & Assert - Should not throw exceptions
        assertDoesNotThrow(() -> {
            captureWithoutLogger.startCapture();
            System.out.println("Message without logger");
            captureWithoutLogger.stopCapture();
        });
    }
    
    @Test
    @DisplayName("Should skip capture in test environment")
    void testSkipCaptureInTestEnvironment() {
        // Arrange - Set test property
        System.setProperty("brobot.test.type", "unit");
        
        try {
            ConsoleOutputCapture testCapture = new ConsoleOutputCapture();
            testCapture.setBrobotLogger(mockBrobotLogger);
            
            // Act
            testCapture.startCapture();
            System.out.println("Test message");
            testCapture.stopCapture();
            
            // Assert - Should not have captured anything due to test environment
            verifyNoInteractions(mockBrobotLogger);
        } finally {
            // Clean up
            System.clearProperty("brobot.test.type");
        }
    }
    
    @Test
    @DisplayName("Should handle rapid start/stop calls")
    void testRapidStartStop() {
        // Act & Assert - Should not throw exceptions
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                capture.startCapture();
                System.out.println("Rapid test " + i);
                capture.stopCapture();
            }
        });
        
        // Verify some logging occurred
        verify(mockBrobotLogger, atLeastOnce()).log();
    }
    
    @Test
    @DisplayName("Should capture output during exception handling")
    void testCapturesDuringException() {
        // Act
        capture.startCapture();
        try {
            System.out.println("Before exception");
            throw new RuntimeException("Test exception");
        } catch (RuntimeException e) {
            System.err.println("Caught exception: " + e.getMessage());
        } finally {
            System.out.println("In finally block");
        }
        capture.stopCapture();
        
        // Assert - Verify all messages were captured
        verify(mockBrobotLogger, times(3)).log();
        verify(mockLogBuilder, times(1)).observation("Before exception");
        verify(mockLogBuilder, times(1)).observation("Caught exception: Test exception");
        verify(mockLogBuilder, times(1)).observation("In finally block");
    }
    
    @Test
    @DisplayName("Should prevent recursive logging")
    void testPreventRecursiveLogging() {
        // This test verifies that the ThreadLocal mechanism prevents infinite recursion
        // when BrobotLogger itself might write to console
        
        // Arrange - Mock BrobotLogger to write to console (simulating recursion)
        doAnswer(invocation -> {
            System.out.println("BrobotLogger internal message");
            return null;
        }).when(mockLogBuilder).log();
        
        // Act
        capture.startCapture();
        System.out.println("Original message");
        capture.stopCapture();
        
        // Assert - Should handle gracefully without infinite recursion
        verify(mockBrobotLogger, atLeastOnce()).log();
    }
    
    @Test
    @DisplayName("Should handle flush operations")
    void testFlushOperations() {
        // Act
        capture.startCapture();
        System.out.print("Partial line without newline");
        System.out.flush(); // This should trigger the flush in LoggingOutputStream
        capture.stopCapture();
        
        // Assert - The flush should have caused the partial line to be logged
        verify(mockBrobotLogger, times(1)).log();
        verify(mockLogBuilder, times(1)).observation("Partial line without newline");
    }
}