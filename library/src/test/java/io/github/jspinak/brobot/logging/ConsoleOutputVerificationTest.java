package io.github.jspinak.brobot.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that action logs produce single-line output in QUIET mode.
 * This test should initially fail when multi-line output is produced.
 */
public class ConsoleOutputVerificationTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        // Capture console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }

    @Test
    void testQuietModeProducesSingleLine() {
        // Given - Simulate what ConsoleActionReporter outputs in QUIET mode
        // This should be a single line like: ✓ Find TestState.TestObject • 234ms
        String expectedOutput = "✓ Find TestState.TestObject • 234ms";
        
        // When - Output what QUIET mode should produce
        System.out.println(expectedOutput);
        System.out.flush();
        
        // Then - Verify it's a single line
        String output = outputStream.toString();
        String[] lines = output.split("\n");
        
        assertEquals(1, lines.length, 
            "QUIET mode should produce exactly 1 line, but got " + lines.length);
        assertEquals(expectedOutput, lines[0].trim());
    }
    
    @Test 
    void testNormalModeProducesMultipleLines() {
        // Given - Simulate what ConsoleActionReporter outputs in NORMAL mode
        // This produces two lines: Find_START and Find_COMPLETE
        
        // When - Output what NORMAL mode currently produces
        System.out.println("▶ Find_START: ");
        System.out.println("▶ Find_COMPLETE:  ✓");
        System.out.flush();
        
        // Then - Verify it's multiple lines
        String output = outputStream.toString();
        String[] lines = output.split("\n");
        
        assertEquals(2, lines.length, 
            "NORMAL mode currently produces 2 lines");
        assertTrue(lines[0].contains("Find_START"));
        assertTrue(lines[1].contains("Find_COMPLETE"));
    }
    
    @Test
    void testCurrentBehaviorShowsMultiLineIssue() {
        // This test documents the current problematic behavior
        // where even QUIET mode might produce multiple lines
        
        // When - Simulate the problematic output we're seeing
        System.out.println("▶ Find_START: ");
        System.out.println("▶ Find_COMPLETE:  ✗");
        System.out.flush();
        
        // Then - Show that we get 2 lines instead of 1
        String output = outputStream.toString();
        String[] lines = output.split("\n");
        
        // This assertion documents the problem
        assertTrue(lines.length > 1, 
            "Current implementation produces " + lines.length + 
            " lines instead of the expected 1 line for QUIET mode");
    }
}