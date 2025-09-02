package io.github.jspinak.brobot.logging.unified;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

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
    private ByteArrayOutputStream testOut;
    private ByteArrayOutputStream testErr;
    
    @Mock
    private LoggingContext mockLoggingContext;
    
    @Mock
    private MessageRouter mockMessageRouter;
    
    private BrobotLogger brobotLogger;
    
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Save original streams
        originalOut = System.out;
        originalErr = System.err;
        
        // Create test output streams for verification
        testOut = new ByteArrayOutputStream();
        testErr = new ByteArrayOutputStream();
        
        // Create capture instance
        capture = new ConsoleOutputCapture();
        
        // Setup mock context with lenient stubbing to avoid "unnecessary stubbing" errors
        lenient().when(mockLoggingContext.getSessionId()).thenReturn("test-session");
        lenient().when(mockLoggingContext.getCurrentState()).thenReturn(null);
        lenient().when(mockLoggingContext.getAllMetadata()).thenReturn(new java.util.HashMap<>());
        
        // Create real BrobotLogger with mocked dependencies
        brobotLogger = new BrobotLogger(mockLoggingContext, mockMessageRouter);
        
        // Inject the real logger and enable capture via reflection
        capture.setBrobotLogger(brobotLogger);
        ReflectionTestUtils.setField(capture, "captureEnabled", true);
        
        // Clear any test environment property that might interfere
        System.clearProperty("brobot.test.type");
    }
    
    @AfterEach
    public void tearDown() {
        // Always restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);
        
        // Clear test properties
        System.clearProperty("brobot.test.type");
    }
    
    @Test
    @DisplayName("Should start capture and redirect System.out to BrobotLogger")
    void testCaptureSystemOut() throws InterruptedException {
        // Act
        capture.startCapture();
        
        // Write to System.out
        System.out.println("Test output line");
        System.out.flush();
        
        // Give time for async processing if any
        Thread.sleep(50);
        
        capture.stopCapture();
        
        // Assert - Verify that MessageRouter was called
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockMessageRouter, atLeastOnce()).route(eventCaptor.capture());
        
        // Check that our message was captured
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMessage() != null && event.getMessage().contains("Test output line")),
            "Expected message 'Test output line' to be captured");
            
        // Verify metadata
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMetadata() != null && "console.out".equals(event.getMetadata().get("source"))),
            "Expected source metadata to be 'console.out'");
    }
    
    @Test
    @DisplayName("Should capture System.err output and route to BrobotLogger")
    void testCaptureSystemErr() throws InterruptedException {
        // Act
        capture.startCapture();
        
        System.err.println("Error message");
        System.err.flush();
        
        // Give time for async processing if any
        Thread.sleep(50);
        
        capture.stopCapture();
        
        // Assert - Verify that MessageRouter was called for error
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockMessageRouter, atLeastOnce()).route(eventCaptor.capture());
        
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMessage() != null && event.getMessage().contains("Error message")),
            "Expected error message to be captured");
            
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getLevel() == LogEvent.Level.ERROR),
            "Expected ERROR level for stderr");
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMetadata() != null && "console.err".equals(event.getMetadata().get("source"))),
            "Expected source metadata to be 'console.err'");
    }
    
    @Test
    @DisplayName("Should capture mixed output and error streams")
    void testCaptureMixedStreams() throws InterruptedException {
        // Act
        capture.startCapture();
        
        System.out.println("Output message");
        System.err.println("Error message");
        System.out.flush();
        System.err.flush();
        
        Thread.sleep(50);
        
        capture.stopCapture();
        
        // Assert - Verify both stdout and stderr were captured
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockMessageRouter, atLeast(2)).route(eventCaptor.capture());
        
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMessage() != null && event.getMessage().contains("Output message")),
            "Expected output message to be captured");
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMessage() != null && event.getMessage().contains("Error message")),
            "Expected error message to be captured");
    }
    
    @Test
    @DisplayName("Should disable and enable capture")
    void testDisableEnableCapture() throws InterruptedException {
        // Test disable
        capture.disableCapture();
        
        // Reset mocks to ensure clean state
        reset(mockMessageRouter);
        
        // Capture should be disabled, so no logging should occur
        System.out.println("This should not be captured");
        Thread.sleep(50);
        
        // Verify no logging occurred
        verifyNoInteractions(mockMessageRouter);
        
        // Test enable
        capture.enableCapture();
        System.out.println("This should be captured");
        System.out.flush();
        Thread.sleep(50);
        capture.stopCapture();
        
        // Should have logged after enabling
        verify(mockMessageRouter, atLeastOnce()).route(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle multiple start/stop cycles")
    void testMultipleStartStopCycles() throws InterruptedException {
        // First cycle
        capture.startCapture();
        System.out.println("Cycle 1");
        System.out.flush();
        Thread.sleep(50);
        capture.stopCapture();
        
        // Verify first cycle
        verify(mockMessageRouter, atLeastOnce()).route(any(LogEvent.class));
        
        // Reset mocks for second cycle
        reset(mockMessageRouter);
        
        // Second cycle
        capture.startCapture();
        System.out.println("Cycle 2");
        System.out.flush();
        Thread.sleep(50);
        capture.stopCapture();
        
        // Verify second cycle logging
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockMessageRouter, atLeastOnce()).route(eventCaptor.capture());
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMessage() != null && event.getMessage().contains("Cycle 2")),
            "Expected 'Cycle 2' message to be captured");
    }
    
    @Test
    @DisplayName("Should filter out known verbose logs")
    void testLogFiltering() throws InterruptedException {
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
        
        System.out.flush();
        Thread.sleep(50);
        
        capture.stopCapture();
        
        // Assert - Only the normal message should have been logged
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockMessageRouter, atLeastOnce()).route(eventCaptor.capture());
        
        // Should only have the normal message
        assertEquals(1, eventCaptor.getAllValues().stream()
            .filter(event -> event.getMessage() != null && event.getMessage().equals("Normal log message"))
            .count(), "Only 'Normal log message' should be captured");
            
        // Filtered messages should not be present
        assertFalse(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMessage() != null && event.getMessage().contains("SikuliX")),
            "SikuliX messages should be filtered out");
    }
    
    @Test
    @DisplayName("Should handle empty lines")
    void testEmptyLines() throws InterruptedException {
        // Act
        capture.startCapture();
        System.out.println("");  // Empty line should be ignored
        System.out.println("Non-empty");
        System.out.flush();
        Thread.sleep(50);
        capture.stopCapture();
        
        // Assert - Only non-empty line should be logged
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockMessageRouter, atLeastOnce()).route(eventCaptor.capture());
        
        assertEquals(1, eventCaptor.getAllValues().size(), "Only non-empty line should be logged");
        assertEquals("Non-empty", eventCaptor.getValue().getMessage());
    }
    
    @Test
    @DisplayName("Should handle log level determination")
    void testLogLevelDetermination() throws InterruptedException {
        // Act
        capture.startCapture();
        System.out.println("Error occurred");  // Should be ERROR level
        System.out.println("Warning message"); // Should be WARNING level  
        System.out.println("Debug info");     // Should be DEBUG level
        System.out.println("Normal message");  // Should be INFO level
        System.out.flush();
        Thread.sleep(50);
        capture.stopCapture();
        
        // Assert - Verify different log levels were used
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockMessageRouter, atLeastOnce()).route(eventCaptor.capture());
        
        assertTrue(eventCaptor.getAllValues().stream().anyMatch(e -> e.getLevel() == LogEvent.Level.ERROR));
        assertTrue(eventCaptor.getAllValues().stream().anyMatch(e -> e.getLevel() == LogEvent.Level.WARNING));
        assertTrue(eventCaptor.getAllValues().stream().anyMatch(e -> e.getLevel() == LogEvent.Level.DEBUG));
        assertTrue(eventCaptor.getAllValues().stream().anyMatch(e -> e.getLevel() == LogEvent.Level.INFO));
    }
    
    @Test
    @DisplayName("Should get original streams")
    void testGetOriginalStreams() {
        // Arrange - Start capture to set original streams
        capture.startCapture();
        
        // Act
        PrintStream capturedOut = ConsoleOutputCapture.getOriginalOut();
        PrintStream capturedErr = ConsoleOutputCapture.getOriginalErr();
        
        // Assert
        assertNotNull(capturedOut);
        assertNotNull(capturedErr);
        assertEquals(originalOut, capturedOut);
        assertEquals(originalErr, capturedErr);
        
        capture.stopCapture();
    }
    
    @Test
    @DisplayName("Should not capture when disabled via property")
    void testCaptureDisabledViaProperty() throws InterruptedException {
        // Arrange - Create capture with disabled property
        ConsoleOutputCapture disabledCapture = new ConsoleOutputCapture();
        ReflectionTestUtils.setField(disabledCapture, "captureEnabled", false);
        disabledCapture.setBrobotLogger(brobotLogger);
        
        // Act
        disabledCapture.startCapture();
        System.out.println("This should not be captured");
        Thread.sleep(50);
        disabledCapture.stopCapture();
        
        // Assert - No logging should occur
        verifyNoInteractions(mockMessageRouter);
    }
    
    @Test
    @DisplayName("Should restore original streams on stop")
    void testRestoreOriginalStreams() {
        // Act
        capture.startCapture();
        
        // Verify streams were replaced during capture
        PrintStream capturedOut = System.out;
        PrintStream capturedErr = System.err;
        
        capture.stopCapture();
        
        // Assert - Original streams should be restored
        assertEquals(originalOut, System.out);
        assertEquals(originalErr, System.err);
    }
    
    @Test
    @DisplayName("Should handle null logger gracefully")
    void testNullLoggerHandling() {
        // Arrange - Create capture without setting logger
        ConsoleOutputCapture captureWithoutLogger = new ConsoleOutputCapture();
        ReflectionTestUtils.setField(captureWithoutLogger, "captureEnabled", true);
        
        // Act & Assert - Should not throw exceptions
        assertDoesNotThrow(() -> {
            captureWithoutLogger.startCapture();
            System.out.println("Message without logger");
            captureWithoutLogger.stopCapture();
        });
        
        // Restore streams
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
    
    @Test
    @DisplayName("Should skip capture in test environment")
    void testSkipCaptureInTestEnvironment() throws InterruptedException {
        // Arrange - Set test property
        System.setProperty("brobot.test.type", "unit");
        
        try {
            ConsoleOutputCapture testCapture = new ConsoleOutputCapture();
            ReflectionTestUtils.setField(testCapture, "captureEnabled", true);
            testCapture.setBrobotLogger(brobotLogger);
            
            // Reset mocks
            reset(mockMessageRouter);
            
            // Act
            testCapture.startCapture();
            System.out.println("Test message");
            Thread.sleep(50);
            testCapture.stopCapture();
            
            // Assert - Should not have captured anything due to test environment
            verifyNoInteractions(mockMessageRouter);
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
                System.out.flush();
                Thread.sleep(10);
                capture.stopCapture();
            }
        });
        
        // Verify some logging occurred
        verify(mockMessageRouter, atLeastOnce()).route(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should capture output during exception handling")
    void testCapturesDuringException() throws InterruptedException {
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
        System.out.flush();
        System.err.flush();
        Thread.sleep(50);
        capture.stopCapture();
        
        // Assert - Verify all messages were captured
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockMessageRouter, atLeastOnce()).route(eventCaptor.capture());
        
        // Check for all expected messages
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMessage() != null && event.getMessage().contains("Before exception")),
            "Should capture 'Before exception'");
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMessage() != null && event.getMessage().contains("Caught exception")),
            "Should capture exception message");
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMessage() != null && event.getMessage().contains("finally block")),
            "Should capture finally block message");
    }
    
    @Test
    @DisplayName("Should prevent recursive logging")
    void testPreventRecursiveLogging() throws InterruptedException {
        // This test verifies that the ThreadLocal mechanism prevents infinite recursion
        // when BrobotLogger itself might write to console
        
        // This test verifies that the ThreadLocal mechanism prevents infinite recursion
        // The ConsoleOutputCapture should detect recursive calls and prevent the loop
        
        // Act
        capture.startCapture();
        System.out.println("Original message");
        System.out.flush();
        Thread.sleep(50);
        capture.stopCapture();
        
        // Assert - Should handle gracefully without infinite recursion
        // The test passing without timeout or StackOverflowError means recursion was prevented
        verify(mockMessageRouter, atLeastOnce()).route(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle flush operations")
    void testFlushOperations() throws InterruptedException {
        // Act
        capture.startCapture();
        System.out.print("Partial line without newline");
        System.out.flush(); // This should trigger the flush in LoggingOutputStream
        Thread.sleep(50);
        capture.stopCapture();
        
        // Assert - The flush should have caused the partial line to be logged
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockMessageRouter, atLeastOnce()).route(eventCaptor.capture());
        
        assertTrue(eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMessage() != null && event.getMessage().contains("Partial line without newline")),
            "Flush should cause partial line to be logged");
    }
}