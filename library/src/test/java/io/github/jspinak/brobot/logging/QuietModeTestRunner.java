package io.github.jspinak.brobot.logging;

import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.logging.unified.LoggingContext;
import io.github.jspinak.brobot.logging.unified.MessageRouter;
import io.github.jspinak.brobot.logging.unified.console.ConsoleFormatter;
import io.github.jspinak.brobot.tools.logging.ActionLogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple test runner to verify QUIET mode logging output.
 */
public class QuietModeTestRunner {
    
    public static void main(String[] args) {
        // Capture console output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        // Configure for QUIET mode
        LoggingVerbosityConfig verbosityConfig = new LoggingVerbosityConfig();
        verbosityConfig.setVerbosity(LoggingVerbosityConfig.VerbosityLevel.QUIET);
        
        // Create components
        ActionLogger actionLogger = new NoOpActionLogger();
        ConsoleFormatter formatter = new ConsoleFormatter(verbosityConfig);
        LoggingContext context = new LoggingContext();
        MessageRouter router = new MessageRouter(actionLogger, verbosityConfig, formatter);
        BrobotLogger logger = new BrobotLogger(context, router);
        
        QuietModeLoggingScorer scorer = new QuietModeLoggingScorer();
        
        // Test 1: Successful Find action
        System.out.println("\n=== Test 1: Successful Find ===");
        outputStream.reset();
        
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("FIND_COMPLETE")
            .target("Working.ClaudeIcon")
            .success(true)
            .metadata("duration", 234L)
            .log();
            
        String actual1 = getOutput(outputStream);
        String expected1 = "✓ Find Working.ClaudeIcon • 234ms";
        LoggingScore score1 = scorer.scoreOutput(actual1, expected1);
        
        System.setOut(originalOut);
        System.out.println("Expected: " + expected1);
        System.out.println("Actual: " + actual1);
        System.out.println("Score: " + score1);
        System.out.println("Perfect: " + score1.isPerfect());
        
        // Test 2: Failed Find action
        System.out.println("\n=== Test 2: Failed Find ===");
        System.setOut(new PrintStream(outputStream));
        outputStream.reset();
        
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("FIND_FAILED")
            .target("Prompt.ClaudePrompt")
            .success(false)
            .metadata("duration", 567L)
            .log();
            
        String actual2 = getOutput(outputStream);
        String expected2 = "✗ Find Prompt.ClaudePrompt • 567ms";
        LoggingScore score2 = scorer.scoreOutput(actual2, expected2);
        
        System.setOut(originalOut);
        System.out.println("Expected: " + expected2);
        System.out.println("Actual: " + actual2);
        System.out.println("Score: " + score2);
        System.out.println("Perfect: " + score2.isPerfect());
        
        // Test 3: Click action
        System.out.println("\n=== Test 3: Click Action ===");
        System.setOut(new PrintStream(outputStream));
        outputStream.reset();
        
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("CLICK_COMPLETE")
            .target("Prompt.SubmitButton")
            .success(true)
            .metadata("duration", 123L)
            .log();
            
        String actual3 = getOutput(outputStream);
        String expected3 = "✓ Click Prompt.SubmitButton • 123ms";
        LoggingScore score3 = scorer.scoreOutput(actual3, expected3);
        
        System.setOut(originalOut);
        System.out.println("Expected: " + expected3);
        System.out.println("Actual: " + actual3);
        System.out.println("Score: " + score3);
        System.out.println("Perfect: " + score3.isPerfect());
        
        // Test 4: START events should be skipped
        System.out.println("\n=== Test 4: START Event (should be empty) ===");
        System.setOut(new PrintStream(outputStream));
        outputStream.reset();
        
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("FIND_START")
            .target("Working.ClaudeIcon")
            .log();
            
        String actual4 = getOutput(outputStream);
        
        System.setOut(originalOut);
        System.out.println("Expected: (empty)");
        System.out.println("Actual: " + (actual4.isEmpty() ? "(empty)" : actual4));
        System.out.println("Correct: " + actual4.isEmpty());
        
        // Summary
        System.out.println("\n=== Summary ===");
        System.out.println("Test 1 (Successful Find): " + (score1.isPerfect() ? "PASS" : "FAIL"));
        System.out.println("Test 2 (Failed Find): " + (score2.isPerfect() ? "PASS" : "FAIL"));
        System.out.println("Test 3 (Click Action): " + (score3.isPerfect() ? "PASS" : "FAIL"));
        System.out.println("Test 4 (START Event): " + (actual4.isEmpty() ? "PASS" : "FAIL"));
        
        // Restore output
        System.setOut(originalOut);
    }
    
    private static String getOutput(ByteArrayOutputStream outputStream) {
        System.out.flush();
        return outputStream.toString().trim();
    }
}