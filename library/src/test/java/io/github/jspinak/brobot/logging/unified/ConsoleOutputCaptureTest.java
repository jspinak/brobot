package io.github.jspinak.brobot.logging.unified;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConsoleOutputCapture.
 * Tests console output capture, buffering, and thread safety.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsoleOutputCapture Tests")
public class ConsoleOutputCaptureTest extends BrobotTestBase {

    private ConsoleOutputCapture capture;
    private PrintStream originalOut;
    private PrintStream originalErr;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Save original streams
        originalOut = System.out;
        originalErr = System.err;
        
        capture = new ConsoleOutputCapture();
    }
    
    @Override
    protected void tearDown() {
        // Always restore original streams
        capture.stop();
        System.setOut(originalOut);
        System.setErr(originalErr);
        super.tearDown();
    }
    
    @Test
    @DisplayName("Should capture System.out output")
    void testCaptureSystemOut() {
        // Act
        capture.start();
        System.out.println("Test output line 1");
        System.out.println("Test output line 2");
        capture.stop();
        
        // Assert
        List<String> captured = capture.getCapturedOutput();
        assertEquals(2, captured.size());
        assertEquals("Test output line 1", captured.get(0));
        assertEquals("Test output line 2", captured.get(1));
    }
    
    @Test
    @DisplayName("Should capture System.err output")
    void testCaptureSystemErr() {
        // Act
        capture.start();
        System.err.println("Error message 1");
        System.err.println("Error message 2");
        capture.stop();
        
        // Assert
        List<String> captured = capture.getCapturedOutput();
        assertEquals(2, captured.size());
        assertEquals("Error message 1", captured.get(0));
        assertEquals("Error message 2", captured.get(1));
    }
    
    @Test
    @DisplayName("Should capture mixed output and error streams")
    void testCaptureMixedStreams() {
        // Act
        capture.start();
        System.out.println("Output 1");
        System.err.println("Error 1");
        System.out.println("Output 2");
        System.err.println("Error 2");
        capture.stop();
        
        // Assert
        List<String> captured = capture.getCapturedOutput();
        assertEquals(4, captured.size());
        assertTrue(captured.contains("Output 1"));
        assertTrue(captured.contains("Error 1"));
        assertTrue(captured.contains("Output 2"));
        assertTrue(captured.contains("Error 2"));
    }
    
    @Test
    @DisplayName("Should clear captured output")
    void testClearCapturedOutput() {
        // Arrange
        capture.start();
        System.out.println("Line 1");
        System.out.println("Line 2");
        
        // Act
        List<String> beforeClear = capture.getCapturedOutput();
        capture.clear();
        List<String> afterClear = capture.getCapturedOutput();
        
        // Assert
        assertEquals(2, beforeClear.size());
        assertEquals(0, afterClear.size());
        
        capture.stop();
    }
    
    @Test
    @DisplayName("Should handle multiple start/stop cycles")
    void testMultipleStartStopCycles() {
        // First cycle
        capture.start();
        System.out.println("Cycle 1");
        capture.stop();
        
        List<String> cycle1 = capture.getCapturedOutput();
        assertEquals(1, cycle1.size());
        assertEquals("Cycle 1", cycle1.get(0));
        
        // Clear and second cycle
        capture.clear();
        capture.start();
        System.out.println("Cycle 2");
        capture.stop();
        
        List<String> cycle2 = capture.getCapturedOutput();
        assertEquals(1, cycle2.size());
        assertEquals("Cycle 2", cycle2.get(0));
    }
    
    @Test
    @DisplayName("Should be thread-safe for concurrent output")
    void testThreadSafety() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        int messagesPerThread = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        capture.start();
        
        // Act - Create threads that write concurrently
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    for (int j = 0; j < messagesPerThread; j++) {
                        System.out.println("Thread-" + threadId + "-Message-" + j);
                    }
                    successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to complete
        assertTrue(endLatch.await(5, TimeUnit.SECONDS), "Threads did not complete in time");
        
        capture.stop();
        
        // Assert
        assertEquals(threadCount, successCount.get());
        List<String> captured = capture.getCapturedOutput();
        assertEquals(threadCount * messagesPerThread, captured.size());
        
        // Verify all messages are present
        for (int i = 0; i < threadCount; i++) {
            for (int j = 0; j < messagesPerThread; j++) {
                String expectedMessage = "Thread-" + i + "-Message-" + j;
                assertTrue(captured.contains(expectedMessage), 
                    "Missing message: " + expectedMessage);
            }
        }
    }
    
    @Test
    @DisplayName("Should handle empty lines")
    void testEmptyLines() {
        // Act
        capture.start();
        System.out.println("");
        System.out.println("Non-empty");
        System.out.println("");
        capture.stop();
        
        // Assert
        List<String> captured = capture.getCapturedOutput();
        assertEquals(3, captured.size());
        assertEquals("", captured.get(0));
        assertEquals("Non-empty", captured.get(1));
        assertEquals("", captured.get(2));
    }
    
    @Test
    @DisplayName("Should capture print without newline")
    void testPrintWithoutNewline() {
        // Act
        capture.start();
        System.out.print("Part 1 ");
        System.out.print("Part 2 ");
        System.out.println("Part 3");
        capture.stop();
        
        // Assert
        List<String> captured = capture.getCapturedOutput();
        assertEquals(1, captured.size());
        assertEquals("Part 1 Part 2 Part 3", captured.get(0));
    }
    
    @Test
    @DisplayName("Should handle special characters")
    void testSpecialCharacters() {
        // Act
        capture.start();
        System.out.println("Tab\tcharacter");
        System.out.println("Newline\nin middle"); // Will be split
        System.out.println("Special: ✓ ✗ →");
        capture.stop();
        
        // Assert
        List<String> captured = capture.getCapturedOutput();
        assertTrue(captured.contains("Tab\tcharacter"));
        assertTrue(captured.contains("Special: ✓ ✗ →"));
    }
    
    @Test
    @DisplayName("Should handle large output")
    void testLargeOutput() {
        // Arrange
        int lineCount = 10000;
        String longLine = "x".repeat(1000); // 1000 character line
        
        // Act
        capture.start();
        for (int i = 0; i < lineCount; i++) {
            System.out.println(longLine + i);
        }
        capture.stop();
        
        // Assert
        List<String> captured = capture.getCapturedOutput();
        assertEquals(lineCount, captured.size());
        assertEquals(longLine + "0", captured.get(0));
        assertEquals(longLine + (lineCount - 1), captured.get(lineCount - 1));
    }
    
    @Test
    @DisplayName("Should restore original streams on stop")
    void testRestoreOriginalStreams() {
        // Arrange
        ByteArrayOutputStream testOut = new ByteArrayOutputStream();
        ByteArrayOutputStream testErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
        System.setErr(new PrintStream(testErr));
        
        // Act
        capture.start();
        capture.stop();
        
        // Write to streams after stop
        System.out.println("After stop output");
        System.err.println("After stop error");
        
        // Assert - Output should go to our test streams, not capture
        assertTrue(testOut.toString().contains("After stop output"));
        assertTrue(testErr.toString().contains("After stop error"));
        
        List<String> captured = capture.getCapturedOutput();
        assertFalse(captured.contains("After stop output"));
        assertFalse(captured.contains("After stop error"));
    }
    
    @Test
    @DisplayName("Should handle null and empty output gracefully")
    void testNullAndEmptyHandling() {
        // Act
        capture.start();
        System.out.print(""); // Empty string
        System.out.flush();
        capture.stop();
        
        // Assert
        List<String> captured = capture.getCapturedOutput();
        assertNotNull(captured);
        // Empty print without newline doesn't create a line
        assertEquals(0, captured.size());
    }
    
    @Test
    @DisplayName("Should capture formatted output")
    void testFormattedOutput() {
        // Act
        capture.start();
        System.out.printf("Number: %d, String: %s, Float: %.2f%n", 42, "test", 3.14159);
        System.out.format("Formatted: [%10s]%n", "text");
        capture.stop();
        
        // Assert
        List<String> captured = capture.getCapturedOutput();
        assertEquals(2, captured.size());
        assertEquals("Number: 42, String: test, Float: 3.14", captured.get(0));
        assertEquals("Formatted: [      text]", captured.get(1));
    }
    
    @Test
    @DisplayName("Should handle rapid start/stop calls")
    void testRapidStartStop() {
        // Act & Assert - Should not throw exceptions
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                capture.start();
                System.out.println("Rapid test " + i);
                capture.stop();
                capture.clear();
            }
        });
    }
    
    @Test
    @DisplayName("Should capture output during exception handling")
    void testCapturesDuringException() {
        // Act
        capture.start();
        try {
            System.out.println("Before exception");
            throw new RuntimeException("Test exception");
        } catch (RuntimeException e) {
            System.err.println("Caught exception: " + e.getMessage());
        } finally {
            System.out.println("In finally block");
        }
        capture.stop();
        
        // Assert
        List<String> captured = capture.getCapturedOutput();
        assertEquals(3, captured.size());
        assertTrue(captured.contains("Before exception"));
        assertTrue(captured.contains("Caught exception: Test exception"));
        assertTrue(captured.contains("In finally block"));
    }
    
    @Test
    @DisplayName("Should get captured output as single string")
    void testGetCapturedAsString() {
        // Act
        capture.start();
        System.out.println("Line 1");
        System.out.println("Line 2");
        System.out.println("Line 3");
        capture.stop();
        
        // Assert
        String asString = capture.getCapturedAsString();
        assertEquals("Line 1\nLine 2\nLine 3", asString);
    }
    
    @Test
    @DisplayName("Should get last N lines")
    void testGetLastNLines() {
        // Act
        capture.start();
        for (int i = 1; i <= 10; i++) {
            System.out.println("Line " + i);
        }
        capture.stop();
        
        // Assert
        List<String> last3 = capture.getLastNLines(3);
        assertEquals(3, last3.size());
        assertEquals("Line 8", last3.get(0));
        assertEquals("Line 9", last3.get(1));
        assertEquals("Line 10", last3.get(2));
        
        // Request more than available
        List<String> last20 = capture.getLastNLines(20);
        assertEquals(10, last20.size());
        assertEquals("Line 1", last20.get(0));
    }
}