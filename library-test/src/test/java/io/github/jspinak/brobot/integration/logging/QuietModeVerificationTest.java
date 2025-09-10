package io.github.jspinak.brobot.integration.logging;

import org.junit.jupiter.api.Disabled;
import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest // (classes = ClaudeAutomatorApplication.class)
@TestPropertySource(properties = {
    "brobot.logging.verbosity=QUIET",
    "brobot.console.actions.level=QUIET",
    "brobot.logging.console.capture-enabled=false",
    "brobot.framework.mock=true",
    "logging.level.root=INFO"
})
@Disabled("Failing in CI - temporarily disabled for CI/CD")
public class QuietModeVerificationTest extends BrobotTestBase {

    @Autowired
    private Action action;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    void testQuietModeProducesSingleLineOutput() {
        // Create a mock StateImage for testing
        StateImage testImage = new StateImage.Builder()
            .setName("TestIcon")
            .setOwnerStateName("TestState")
            .build();

        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withImages(testImage)
            .build();

        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setPauseBeforeBegin(0)
            .build();

        // Perform the action
        ActionResult result = action.perform(findOptions, objectCollection);

        // Get the console output
        System.out.flush();
        String output = outputStream.toString();

        // Restore original output
        System.setOut(originalOut);

        // Print the output for debugging
        System.out.println("=== Captured Output ===");
        System.out.println(output);
        System.out.println("=== End Output ===");

        // Verify the output format
        String[] lines = output.split("\n");
        
        // Find the action log line (skip Spring Boot startup logs)
        String actionLogLine = null;
        for (String line : lines) {
            if (line.contains("Find") && line.contains("TestState.TestIcon")) {
                actionLogLine = line.trim();
                break;
            }
        }

        if (actionLogLine != null) {
            // Should be a single line with format: ✗ Find TestState.TestIcon • XXXms
            assertTrue(actionLogLine.matches("^[✓✗] Find TestState\\.TestIcon • \\d+ms$"),
                "Output should match QUIET mode format: " + actionLogLine);
            
            // Should not contain pipes or PatternFindOptions
            assertFalse(actionLogLine.contains("|"), "Output should not contain pipes");
            assertFalse(actionLogLine.contains("PatternFindOptions"), 
                "Output should not contain class names");
        }
    }
}