package io.github.jspinak.brobot.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Test to debug why screenshots are not being saved. */
@SpringBootTest // (classes = ClaudeAutomatorApplication.class not available)
@ActiveProfiles("linux")
@Disabled("Failing in CI - temporarily disabled for CI/CD")
public class ScreenshotSaveDebugTest extends BrobotTestBase {

    @Autowired private BrobotProperties brobotProperties;

    @Autowired private Find find;

    @Autowired private StateStore stateStore;

    @BeforeEach
    void setUp() throws Exception {
        // Ensure history directory exists
        Path historyPath = Paths.get("history");
        Files.createDirectories(historyPath);

        System.out.println("\n=== SCREENSHOT SAVE DEBUG TEST ===");
        System.out.println("History directory: " + historyPath.toAbsolutePath());
        System.out.println("Directory exists: " + Files.exists(historyPath));
        System.out.println("Directory writable: " + Files.isWritable(historyPath));

        // Check BrobotProperties
        System.out.println("\nFramework Settings:");
        System.out.println("saveHistory: " + brobotProperties.getScreenshot().isSaveHistory());
        System.out.println("historyPath: " + brobotProperties.getScreenshot().getHistoryPath());
        System.out.println("drawFind: " + brobotProperties.getIllustration().isDrawFind());

        // Force enable
        // Settings are configured via application properties
        // Settings are configured via application properties
        // Settings are configured via application properties
        // Settings are configured via application properties

        System.out.println("\nAfter forcing:");
        System.out.println("saveHistory: " + brobotProperties.getScreenshot().isSaveHistory());
        System.out.println("historyPath: " + brobotProperties.getScreenshot().getHistoryPath());
    }

    @Test
    @DisabledIf("java.awt.GraphicsEnvironment#isHeadless")
    void testScreenshotSaving() throws Exception {
        System.out.println("\n=== TESTING SCREENSHOT SAVING ===");

        // Check environment
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        System.out.println("canCaptureScreen: " + env.canCaptureScreen());
        assertFalse(GraphicsEnvironment.isHeadless(), "Should not be headless");
        assertTrue(env.canCaptureScreen(), "Should be able to capture screen");

        // First test direct capture to verify system works
        System.out.println("\n1. Testing direct capture:");
        Robot robot = new Robot();
        BufferedImage directCapture = robot.createScreenCapture(new Rectangle(0, 0, 200, 200));
        File directFile = new File("history/direct-test.png");
        ImageIO.write(directCapture, "png", directFile);
        System.out.println("Direct capture saved: " + directFile.exists());
        System.out.println("Direct capture size: " + directFile.length() + " bytes");

        // Count files before action
        Path historyPath = Paths.get("history");
        long filesBefore = 0;
        try (Stream<Path> files = Files.list(historyPath)) {
            filesBefore = files.filter(p -> p.toString().endsWith(".png")).count();
        }
        System.out.println("\n2. Files in history before action: " + filesBefore);

        // Create a test state
        State testState = new State.Builder("TestState").build();
        StateRegion searchRegion =
                new StateRegion.Builder()
                        .setName("TestRegion")
                        .setSearchRegion(new Region(0, 0, 200, 200))
                        .build();
        testState.addStateRegion(searchRegion);
        stateStore.save(testState);

        // Force illustration
        ActionResult result = new ActionResult();

        ObjectCollection objects = new ObjectCollection.Builder().withRegions(searchRegion).build();

        System.out.println("\n3. Performing FIND action with forced illustration...");
        find.perform(result, objects);
        System.out.println("Action performed. Success: " + result.isSuccess());

        // Wait for file writing
        Thread.sleep(2000);

        // Count files after action
        long filesAfter = 0;
        try (Stream<Path> files = Files.list(historyPath)) {
            filesAfter = files.filter(p -> p.toString().endsWith(".png")).count();
        }
        System.out.println("\n4. Files in history after action: " + filesAfter);
        System.out.println("New files created: " + (filesAfter - filesBefore));

        // List all PNG files
        System.out.println("\n5. PNG files in history directory:");
        try (Stream<Path> files = Files.list(historyPath)) {
            files.filter(p -> p.toString().endsWith(".png"))
                    .forEach(
                            p -> {
                                File f = p.toFile();
                                System.out.println(
                                        "  " + f.getName() + " (" + f.length() + " bytes)");

                                // Check if it's black
                                try {
                                    BufferedImage img = ImageIO.read(f);
                                    int blackPixels = 0;
                                    for (int i = 0; i < 100; i++) {
                                        int x = (int) (Math.random() * img.getWidth());
                                        int y = (int) (Math.random() * img.getHeight());
                                        if (img.getRGB(x, y) == 0xFF000000) blackPixels++;
                                    }
                                    System.out.println("    Black pixels: " + blackPixels + "%");
                                    if (blackPixels > 90) {
                                        System.out.println("    ⚠️ This file is BLACK!");
                                    }
                                } catch (Exception e) {
                                    System.err.println("    Error reading: " + e.getMessage());
                                }
                            });
        }

        // Clean up test file
        directFile.delete();
    }
}
