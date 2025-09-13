package io.github.jspinak.brobot.integration.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

@SpringBootApplication
@ComponentScan(basePackages = {"io.github.jspinak.brobot", "com.claude.automator"})
@Disabled("Not a JUnit test - has main method instead")
public class DirectActionInvocationTest extends BrobotTestBase {

    @Autowired private Find findAction;

    @Autowired private ActionResultFactory actionResultFactory;

    @Autowired private BrobotLogger brobotLogger;

    public static void main(String[] args) {
        // Set properties
        System.setProperty("brobot.logging.verbosity", "QUIET");
        System.setProperty("brobot.logging.console.capture-enabled", "false");
        System.setProperty("brobot.mock", "true");
        System.setProperty("logging.level.root", "WARN");

        ConfigurableApplicationContext context =
                SpringApplication.run(DirectActionInvocationTest.class, args);

        try {
            DirectActionInvocationTest test = context.getBean(DirectActionInvocationTest.class);
            test.runTest();
        } finally {
            context.close();
        }
    }

    private void runTest() {
        System.out.println("\n=== Direct Action Invocation Test ===\n");

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

            // Create ActionResult manually
            ActionResult actionResult =
                    actionResultFactory.init(findOptions, "Test Find Action", objectCollection);

            System.out.println("Before perform() call...");
            System.out.flush();

            // Call perform directly on the Find action
            findAction.perform(actionResult, objectCollection);

            System.out.println("After perform() call...");
            System.out.flush();

            // Get captured output
            String output = outputStream.toString();

            // Restore output
            System.setOut(originalOut);

            // Analyze output
            System.out.println("Captured output during perform():");
            System.out.println("================");
            System.out.println(output);
            System.out.println("================");

            System.out.println("\nAction Result Details:");
            System.out.println("  Success: " + actionResult.isSuccess());
            System.out.println("  Duration: " + actionResult.getDuration());
            System.out.println("  Description: " + actionResult.getActionDescription());

            // Try manual logging to see expected output
            System.out.println("\nExpected output (manual log):");
            outputStream.reset();
            System.setOut(capturingStream);

            brobotLogger
                    .log()
                    .type(LogEvent.Type.ACTION)
                    .level(LogEvent.Level.INFO)
                    .action("FIND_COMPLETE")
                    .target("TestState.TestIcon")
                    .success(false)
                    .duration(234L)
                    .log();

            System.out.flush();
            String expectedOutput = outputStream.toString();
            System.setOut(originalOut);

            System.out.println(expectedOutput.trim());

        } catch (Exception e) {
            System.setOut(originalOut);
            e.printStackTrace();
        }
    }
}
