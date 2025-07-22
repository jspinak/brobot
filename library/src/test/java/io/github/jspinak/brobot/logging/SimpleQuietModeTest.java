package io.github.jspinak.brobot.logging;

import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.logging.unified.console.ConsoleFormatter;


/**
 * Simple test to verify QUIET mode formatting.
 */
public class SimpleQuietModeTest {
    
    public static void main(String[] args) {
        // Configure for QUIET mode
        LoggingVerbosityConfig verbosityConfig = new LoggingVerbosityConfig();
        verbosityConfig.setVerbosity(LoggingVerbosityConfig.VerbosityLevel.QUIET);
        
        // Create formatter
        ConsoleFormatter formatter = new ConsoleFormatter(verbosityConfig);
        
        System.out.println("=== Testing QUIET Mode Formatting ===\n");
        
        // Test 1: Successful Find
        LogEvent event1 = new LogEvent.Builder()
            .type(LogEvent.Type.ACTION)
            .action("FIND_COMPLETE")
            .target("Working.ClaudeIcon")
            .success(true)
            .metadata("duration", 234L)
            .build();
        
        String result1 = formatter.format(event1);
        System.out.println("Test 1 - Successful Find:");
        System.out.println("Expected: ✓ Find Working.ClaudeIcon • 234ms");
        System.out.println("Actual:   " + result1);
        System.out.println("Match:    " + "✓ Find Working.ClaudeIcon • 234ms".equals(result1));
        System.out.println();
        
        // Test 2: Failed Find
        LogEvent event2 = new LogEvent.Builder()
            .type(LogEvent.Type.ACTION)
            .action("FIND_FAILED")
            .target("Prompt.ClaudePrompt")
            .success(false)
            .metadata("duration", 567L)
            .build();
        
        String result2 = formatter.format(event2);
        System.out.println("Test 2 - Failed Find:");
        System.out.println("Expected: ✗ Find Prompt.ClaudePrompt • 567ms");
        System.out.println("Actual:   " + result2);
        System.out.println("Match:    " + "✗ Find Prompt.ClaudePrompt • 567ms".equals(result2));
        System.out.println();
        
        // Test 3: Click action
        LogEvent event3 = new LogEvent.Builder()
            .type(LogEvent.Type.ACTION)
            .action("CLICK_COMPLETE")
            .target("Prompt.SubmitButton")
            .success(true)
            .metadata("duration", 123L)
            .build();
        
        String result3 = formatter.format(event3);
        System.out.println("Test 3 - Click Action:");
        System.out.println("Expected: ✓ Click Prompt.SubmitButton • 123ms");
        System.out.println("Actual:   " + result3);
        System.out.println("Match:    " + "✓ Click Prompt.SubmitButton • 123ms".equals(result3));
        System.out.println();
        
        // Test 4: START event (should be null)
        LogEvent event4 = new LogEvent.Builder()
            .type(LogEvent.Type.ACTION)
            .action("FIND_START")
            .target("Working.ClaudeIcon")
            .build();
        
        String result4 = formatter.format(event4);
        System.out.println("Test 4 - START Event (should be null):");
        System.out.println("Expected: null");
        System.out.println("Actual:   " + result4);
        System.out.println("Match:    " + (result4 == null));
        System.out.println();
        
        // Summary
        System.out.println("=== Summary ===");
        int passed = 0;
        if ("✓ Find Working.ClaudeIcon • 234ms".equals(result1)) passed++;
        if ("✗ Find Prompt.ClaudePrompt • 567ms".equals(result2)) passed++;
        if ("✓ Click Prompt.SubmitButton • 123ms".equals(result3)) passed++;
        if (result4 == null) passed++;
        
        System.out.println("Tests passed: " + passed + "/4");
        
        if (passed < 4) {
            // Debug output format
            System.out.println("\n=== Debug Info ===");
            if (result1 != null) {
                System.out.println("Result 1 bytes: " + java.util.Arrays.toString(result1.getBytes()));
            }
        }
    }
}