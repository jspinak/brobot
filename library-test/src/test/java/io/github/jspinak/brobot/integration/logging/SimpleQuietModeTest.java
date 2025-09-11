package io.github.jspinak.brobot.integration.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Disabled;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

@Disabled("Not a JUnit test - has main method instead")
public class SimpleQuietModeTest extends BrobotTestBase {

    public static void main(String[] args) {
        // Mock mode is set via properties

        // Capture console output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            // Create Spring context manually
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.scan("io.github.jspinak.brobot", "com.claude.automator");

            // Configure verbosity before refresh
            LoggingVerbosityConfig verbosityConfig = new LoggingVerbosityConfig();
            verbosityConfig.setVerbosity(LoggingVerbosityConfig.VerbosityLevel.QUIET);
            context.registerBean(LoggingVerbosityConfig.class, () -> verbosityConfig);

            context.refresh();

            // Get Action bean
            Action action = context.getBean(Action.class);

            // Create test objects
            StateImage testImage =
                    new StateImage.Builder()
                            .setName("TestIcon")
                            .setOwnerStateName("TestState")
                            .build();

            ObjectCollection objectCollection =
                    new ObjectCollection.Builder().withImages(testImage).build();

            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setPauseBeforeBegin(0).build();

            // Clear output before action
            outputStream.reset();

            // Perform the action
            ActionResult result = action.perform(findOptions, objectCollection);

            // Get the output
            System.out.flush();
            String output = outputStream.toString();

            // Restore output
            System.setOut(originalOut);

            // Analyze the output
            System.out.println("=== QUIET Mode Test Results ===");
            System.out.println("\nRaw output:");
            System.out.println(output);

            // Find action log lines
            String[] lines = output.split("\n");
            System.out.println("\nAction log lines:");
            boolean foundActionLog = false;
            for (String line : lines) {
                if (line.contains("Find") || line.contains("✓") || line.contains("✗")) {
                    System.out.println("  " + line.trim());
                    foundActionLog = true;

                    // Check format
                    if (line.contains("PatternFindOptions") || line.contains("|")) {
                        System.out.println("  ❌ FAIL: Found legacy format indicators");
                    } else if (line.trim().matches("^[✓✗] Find TestState\\.TestIcon • \\d+ms$")) {
                        System.out.println("  ✅ PASS: Correct QUIET mode format");
                    }
                }
            }

            if (!foundActionLog) {
                System.out.println("  No action log lines found");
            }

            context.close();

        } catch (Exception e) {
            System.setOut(originalOut);
            e.printStackTrace();
        }
    }
}
