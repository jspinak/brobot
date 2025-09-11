package io.github.jspinak.brobot.integration.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

@SpringBootApplication
@ComponentScan(basePackages = {"io.github.jspinak.brobot", "com.claude.automator"})
@Disabled("Not a JUnit test - has main method instead")
public class DirectLoggingDiagnosticTest extends BrobotTestBase {

    @Autowired private Action action;

    @Autowired private BrobotLogger brobotLogger;

    public static void main(String[] args) {
        // Set properties
        System.setProperty("brobot.logging.verbosity", "QUIET");
        System.setProperty("brobot.logging.console.capture-enabled", "false");
        System.setProperty("brobot.framework.mock", "true");
        System.setProperty("logging.level.root", "INFO");

        // Capture console output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        ConfigurableApplicationContext context =
                SpringApplication.run(DirectLoggingDiagnosticTest.class, args);

        try {
            DirectLoggingDiagnosticTest test = context.getBean(DirectLoggingDiagnosticTest.class);
            test.runDiagnostic(outputStream, originalOut);
        } finally {
            context.close();
        }
    }

    private void runDiagnostic(ByteArrayOutputStream outputStream, PrintStream originalOut) {
        System.out.println("=== Starting Direct Logging Diagnostic ===\n");

        // Test 1: Direct BrobotLogger test
        System.out.println("1. Testing direct BrobotLogger with full information:");
        outputStream.reset();

        brobotLogger
                .log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.INFO)
                .action("FIND_COMPLETE")
                .target("TestState.TestImage")
                .success(false)
                .duration(234L)
                .log();

        System.out.flush();
        String directLogOutput = outputStream.toString();
        System.setOut(originalOut);
        System.out.println("   Direct log output: " + directLogOutput.trim());

        // Test 2: Full action test
        System.out.println("\n2. Testing full action execution:");
        System.setOut(new PrintStream(outputStream));
        outputStream.reset();

        StateImage testImage =
                new StateImage.Builder().setName("TestIcon").setOwnerStateName("TestState").build();

        ObjectCollection objectCollection =
                new ObjectCollection.Builder().withImages(testImage).build();

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder().setPauseBeforeBegin(0).build();

        ActionResult result = action.perform(findOptions, objectCollection);

        System.out.flush();
        String actionOutput = outputStream.toString();
        System.setOut(originalOut);

        System.out.println("   Action output: " + actionOutput.trim());

        // Test 3: Check what the ActionResult contains
        System.out.println("\n3. ActionResult details:");
        System.out.println("   Success: " + result.isSuccess());
        System.out.println("   Duration: " + result.getDuration());
        System.out.println("   ActionConfig: " + result.getActionConfig());
        System.out.println("   Description: " + result.getActionDescription());

        // Test 4: Manual logging test to see if target info is preserved
        System.out.println("\n4. Testing manual action lifecycle logging:");
        System.setOut(new PrintStream(outputStream));
        outputStream.reset();

        // Simulate what ActionLifecycleAspect should do
        brobotLogger
                .log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.INFO)
                .action("FIND_START")
                .target("TestState.TestIcon")
                .log();

        brobotLogger
                .log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.INFO)
                .action("FIND_COMPLETE")
                .target("TestState.TestIcon")
                .success(false)
                .duration(567L)
                .log();

        System.out.flush();
        String manualOutput = outputStream.toString();
        System.setOut(originalOut);
        System.out.println("   Manual lifecycle output: " + manualOutput.trim());

        System.out.println("\n=== Diagnostic Complete ===");
    }
}
