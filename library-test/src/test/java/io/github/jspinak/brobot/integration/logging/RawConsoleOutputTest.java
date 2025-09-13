package io.github.jspinak.brobot.integration.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

@SpringBootApplication
@Component
@Disabled("Not a JUnit test - has main method instead")
public class RawConsoleOutputTest extends BrobotTestBase {

    @Autowired private Action action;

    public static void main(String[] args) {
        // Set properties
        System.setProperty("brobot.logging.verbosity", "QUIET");
        System.setProperty("brobot.logging.console.capture-enabled", "false");
        System.setProperty("brobot.mock", "true");
        System.setProperty("logging.level.root", "WARN");
        System.setProperty("logging.level.io.github.jspinak.brobot.logging", "DEBUG");

        ConfigurableApplicationContext context =
                SpringApplication.run(RawConsoleOutputTest.class, args);

        try {
            RawConsoleOutputTest test = context.getBean(RawConsoleOutputTest.class);
            test.runTest();
        } finally {
            context.close();
        }
    }

    private void runTest() {
        System.out.println("\n=== Raw Console Output Test ===\n");

        // Capture console output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream capturingStream = new PrintStream(outputStream);
        System.setOut(capturingStream);

        try {
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

            // Perform the action
            ActionResult result = action.perform(findOptions, objectCollection);

            // Flush output
            capturingStream.flush();
            String output = outputStream.toString();

            // Restore output
            System.setOut(originalOut);

            // Analyze output line by line
            System.out.println("Captured output:");
            System.out.println("================");
            Scanner scanner = new Scanner(output);
            int lineNum = 1;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.printf("%3d: [%s]\n", lineNum++, line);

                // Check if this is a Find line
                if (line.contains("Find") || line.contains("✗") || line.contains("✓")) {
                    System.out.println("     ^ Action log line detected");
                    System.out.println("       Length: " + line.length());
                    System.out.println(
                            "       Bytes: " + java.util.Arrays.toString(line.getBytes()));
                }
            }
            scanner.close();

            System.out.println("\nAction Result Details:");
            System.out.println("  Success: " + result.isSuccess());
            System.out.println("  Duration: " + result.getDuration());
            System.out.println("  Description: " + result.getActionDescription());

        } catch (Exception e) {
            System.setOut(originalOut);
            e.printStackTrace();
        }
    }
}
