package io.github.jspinak.brobot.tools.logging;

import io.github.jspinak.brobot.tools.logging.ansi.AnsiColor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class MessageFormatterTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;
    private PrintStream testOut;
    
    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        testOut = new PrintStream(outputStream);
        System.setOut(testOut);
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }
    
    @Test
    void testPrintColor_SingleColor() {
        // Execute
        MessageFormatter.printColor("Test message", AnsiColor.RED);
        
        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains(AnsiColor.RED));
        assertTrue(output.contains("Test message"));
        assertTrue(output.contains(AnsiColor.RESET));
        assertFalse(output.contains(System.lineSeparator()));
    }
    
    @Test
    void testPrintColor_MultipleColors() {
        // Execute
        MessageFormatter.printColor("Multi-color text", AnsiColor.YELLOW_BACKGROUND, AnsiColor.BLACK_BOLD);
        
        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains(AnsiColor.YELLOW_BACKGROUND));
        assertTrue(output.contains(AnsiColor.BLACK_BOLD));
        assertTrue(output.contains("Multi-color text"));
        assertTrue(output.contains(AnsiColor.RESET));
        
        // Verify order - colors should come before message
        int yellowIndex = output.indexOf(AnsiColor.YELLOW_BACKGROUND);
        int blackIndex = output.indexOf(AnsiColor.BLACK_BOLD);
        int messageIndex = output.indexOf("Multi-color text");
        int resetIndex = output.indexOf(AnsiColor.RESET);
        
        assertTrue(yellowIndex < messageIndex);
        assertTrue(blackIndex < messageIndex);
        assertTrue(messageIndex < resetIndex);
    }
    
    @Test
    void testPrintColor_NoColors() {
        // Execute
        MessageFormatter.printColor("Plain text");
        
        // Verify
        String output = outputStream.toString();
        assertEquals("Plain text" + AnsiColor.RESET, output);
    }
    
    @Test
    void testPrintColorLn_SingleColor() {
        // Execute
        MessageFormatter.printColorLn("Line message", AnsiColor.GREEN);
        
        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains(AnsiColor.GREEN));
        assertTrue(output.contains("Line message"));
        assertTrue(output.contains(AnsiColor.RESET));
        assertTrue(output.endsWith(System.lineSeparator()));
    }
    
    @Test
    void testPrintColorLn_MultipleColors() {
        // Execute
        MessageFormatter.printColorLn("Error message", AnsiColor.RED_BACKGROUND, AnsiColor.WHITE_BOLD);
        
        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains(AnsiColor.RED_BACKGROUND));
        assertTrue(output.contains(AnsiColor.WHITE_BOLD));
        assertTrue(output.contains("Error message"));
        assertTrue(output.contains(AnsiColor.RESET));
        assertTrue(output.endsWith(System.lineSeparator()));
    }
    
    @Test
    void testCheckSymbol() {
        // Verify check symbol
        assertEquals("✓", MessageFormatter.check);
        
        // Test using check symbol in output
        MessageFormatter.printColor(MessageFormatter.check + " Success", AnsiColor.GREEN);
        String output = outputStream.toString();
        assertTrue(output.contains("✓ Success"));
    }
    
    @Test
    void testFailSymbol() {
        // Verify fail symbol
        assertEquals("✘", MessageFormatter.fail);
        
        // Test using fail symbol in output
        MessageFormatter.printColor(MessageFormatter.fail + " Failed", AnsiColor.RED);
        String output = outputStream.toString();
        assertTrue(output.contains("✘ Failed"));
    }
    
    @Test
    void testEmptyMessage() {
        // Execute
        MessageFormatter.printColor("", AnsiColor.BLUE);
        
        // Verify
        String output = outputStream.toString();
        assertEquals(AnsiColor.BLUE + AnsiColor.RESET, output);
    }
    
    @Test
    void testSpecialCharacters() {
        // Execute
        String specialMessage = "Special chars: \n\t\"'<>&";
        MessageFormatter.printColorLn(specialMessage, AnsiColor.CYAN);
        
        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains(specialMessage));
        assertTrue(output.contains(AnsiColor.CYAN));
        assertTrue(output.contains(AnsiColor.RESET));
    }
    
    @Test
    void testMultipleConsecutiveCalls() {
        // Execute multiple calls
        MessageFormatter.printColor("First ", AnsiColor.RED);
        MessageFormatter.printColor("Second ", AnsiColor.GREEN);
        MessageFormatter.printColorLn("Third", AnsiColor.BLUE);
        
        // Verify
        String output = outputStream.toString();
        
        // Check all messages are present
        assertTrue(output.contains("First "));
        assertTrue(output.contains("Second "));
        assertTrue(output.contains("Third"));
        
        // Check all colors are present
        assertTrue(output.contains(AnsiColor.RED));
        assertTrue(output.contains(AnsiColor.GREEN));
        assertTrue(output.contains(AnsiColor.BLUE));
        
        // Count RESET occurrences - should be 3
        int resetCount = 0;
        int index = 0;
        while ((index = output.indexOf(AnsiColor.RESET, index)) != -1) {
            resetCount++;
            index += AnsiColor.RESET.length();
        }
        assertEquals(3, resetCount);
    }
    
    @Test
    void testColorArrayOrder() {
        // Execute with specific color order
        String[] colors = {AnsiColor.YELLOW, AnsiColor.YELLOW_BOLD, AnsiColor.YELLOW_UNDERLINED};
        MessageFormatter.printColor("Styled text", colors);
        
        // Verify
        String output = outputStream.toString();
        
        // Check colors appear in the correct order
        int yellowIndex = output.indexOf(AnsiColor.YELLOW);
        int boldIndex = output.indexOf(AnsiColor.YELLOW_BOLD);
        int underlineIndex = output.indexOf(AnsiColor.YELLOW_UNDERLINED);
        int textIndex = output.indexOf("Styled text");
        
        assertTrue(yellowIndex < boldIndex);
        assertTrue(boldIndex < underlineIndex);
        assertTrue(underlineIndex < textIndex);
    }
}