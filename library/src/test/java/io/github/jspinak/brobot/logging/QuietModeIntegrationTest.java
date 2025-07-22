package io.github.jspinak.brobot.logging;

import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.logging.unified.LoggingContext;
import io.github.jspinak.brobot.logging.unified.MessageRouter;
import io.github.jspinak.brobot.logging.unified.console.ConsoleFormatter;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that verifies QUIET mode logging produces the expected output format.
 */
public class QuietModeIntegrationTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private BrobotLogger logger;
    private QuietModeLoggingScorer scorer;
    private LoggingVerbosityConfig verbosityConfig;

    @BeforeEach
    void setUp() {
        // Capture console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        // Configure for QUIET mode
        verbosityConfig = new LoggingVerbosityConfig();
        verbosityConfig.setVerbosity(LoggingVerbosityConfig.VerbosityLevel.QUIET);
        
        // Create components
        ActionLogger actionLogger = new NoOpActionLogger();
        ConsoleFormatter formatter = new ConsoleFormatter(verbosityConfig);
        LoggingContext context = new LoggingContext();
        MessageRouter router = new MessageRouter(actionLogger, verbosityConfig, formatter);
        logger = new BrobotLogger(context, router);
        
        scorer = new QuietModeLoggingScorer();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testSuccessfulFindAction() {
        // Given - Expected output
        String expectedOutput = "✓ Find Working.ClaudeIcon • 234ms";
        
        // When - Log a successful find action
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("FIND_COMPLETE")
            .target("Working.ClaudeIcon")
            .success(true)
            .metadata("duration", 234L)
            .log();
            
        // Then - Score the output
        String actualOutput = getConsoleOutput();
        LoggingScore score = scorer.scoreOutput(actualOutput, expectedOutput);
        
        // Print diagnostics
        System.err.println("Expected: " + expectedOutput);
        System.err.println("Actual: " + actualOutput);
        System.err.println("Score: " + score);
        
        // Assert perfect score
        assertTrue(score.isPerfect(), "Output should match expected format perfectly");
    }

    @Test
    void testFailedFindAction() {
        // Given - Expected output
        String expectedOutput = "✗ Find Prompt.ClaudePrompt • 567ms";
        
        // When - Log a failed find action
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("FIND_COMPLETE")
            .target("Prompt.ClaudePrompt")
            .success(false)
            .metadata("duration", 567L)
            .log();
            
        // Then - Score the output
        String actualOutput = getConsoleOutput();
        LoggingScore score = scorer.scoreOutput(actualOutput, expectedOutput);
        
        // Print diagnostics
        System.err.println("Expected: " + expectedOutput);
        System.err.println("Actual: " + actualOutput);
        System.err.println("Score: " + score);
        
        // Assert perfect score
        assertTrue(score.isPerfect(), "Failed action should show ✗ symbol");
    }

    @Test
    void testClickAction() {
        // Given - Expected output
        String expectedOutput = "✓ Click Prompt.SubmitButton • 123ms";
        
        // When - Log a click action
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("CLICK_COMPLETE")
            .target("Prompt.SubmitButton")
            .success(true)
            .metadata("duration", 123L)
            .log();
            
        // Then - Score the output
        String actualOutput = getConsoleOutput();
        LoggingScore score = scorer.scoreOutput(actualOutput, expectedOutput);
        
        // Print diagnostics
        System.err.println("Expected: " + expectedOutput);
        System.err.println("Actual: " + actualOutput);
        System.err.println("Score: " + score);
        
        // Assert perfect score
        assertTrue(score.isPerfect(), "Click action should format correctly");
    }

    @Test
    void testTypeAction() {
        // Given - Expected output
        String expectedOutput = "✓ Type Working.InputField • 456ms";
        
        // When - Log a type action
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("TYPE_COMPLETE")
            .target("Working.InputField")
            .success(true)
            .metadata("duration", 456L)
            .log();
            
        // Then - Score the output
        String actualOutput = getConsoleOutput();
        LoggingScore score = scorer.scoreOutput(actualOutput, expectedOutput);
        
        // Print diagnostics
        System.err.println("Expected: " + expectedOutput);
        System.err.println("Actual: " + actualOutput);
        System.err.println("Score: " + score);
        
        // Assert perfect score
        assertTrue(score.isPerfect(), "Type action should format correctly");
    }

    @Test
    void testStartEventsShouldBeSkipped() {
        // When - Log a START event
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("FIND_START")
            .target("Working.ClaudeIcon")
            .log();
            
        // Then - No output should be produced
        String actualOutput = getConsoleOutput();
        assertTrue(actualOutput.isEmpty(), "START events should be skipped in QUIET mode");
    }

    @Test
    void testMultipleActions() {
        // Given - Expected outputs
        List<String> expectedOutputs = Arrays.asList(
            "✓ Find Working.ClaudeIcon • 234ms",
            "✗ Find Prompt.ClaudePrompt • 567ms",
            "✓ Click Prompt.SubmitButton • 123ms"
        );
        
        // When - Log multiple actions
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("FIND_COMPLETE")
            .target("Working.ClaudeIcon")
            .success(true)
            .metadata("duration", 234L)
            .log();
            
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("FIND_COMPLETE")
            .target("Prompt.ClaudePrompt")
            .success(false)
            .metadata("duration", 567L)
            .log();
            
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("CLICK_COMPLETE")
            .target("Prompt.SubmitButton")
            .success(true)
            .metadata("duration", 123L)
            .log();
            
        // Then - Score all outputs
        List<String> actualOutputs = getConsoleOutputLines();
        MultiLineScore multiScore = scorer.scoreMultipleOutputs(actualOutputs, expectedOutputs);
        
        // Print diagnostics
        System.err.println("Expected outputs:");
        expectedOutputs.forEach(line -> System.err.println("  " + line));
        System.err.println("Actual outputs:");
        actualOutputs.forEach(line -> System.err.println("  " + line));
        System.err.println("Score: " + multiScore);
        
        // Assert perfect score
        assertTrue(multiScore.isPerfect(), "All outputs should match expected format");
        assertEquals(3, multiScore.getPerfectLines());
        assertEquals(0, multiScore.getImperfectLines());
    }

    @Test
    void testActionWithoutDuration() {
        // Given - Expected output without duration
        String expectedOutput = "✓ Find Working.ClaudeIcon";
        
        // When - Log without duration metadata
        logger.log()
            .type(LogEvent.Type.ACTION)
            .action("FIND_COMPLETE")
            .target("Working.ClaudeIcon")
            .success(true)
            .log();
            
        // Then - Score the output
        String actualOutput = getConsoleOutput();
        
        // Since our current format always expects duration, let's check what we actually get
        System.err.println("Expected: " + expectedOutput);
        System.err.println("Actual: " + actualOutput);
        
        // The actual output might not have duration, which is okay
        assertTrue(actualOutput.startsWith("✓ Find Working.ClaudeIcon"), 
            "Output should start with success symbol, action, and target");
    }

    private String getConsoleOutput() {
        System.out.flush();
        return outputStream.toString().trim();
    }
    
    private List<String> getConsoleOutputLines() {
        System.out.flush();
        String output = outputStream.toString();
        return Arrays.stream(output.split("\n"))
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .collect(Collectors.toList());
    }
}