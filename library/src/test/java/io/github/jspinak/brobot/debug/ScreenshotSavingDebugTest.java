package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.config.BrobotProperties;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test to verify screenshot saving functionality.
 * This test helps diagnose why screenshots are not being saved in claude-automator.
 */
@SpringBootTest(classes = io.github.jspinak.brobot.test.TestConfiguration.class, classes = io.github.jspinak.brobot.test.TestConfiguration.class)
@TestPropertySource(properties = {
    "brobot.screenshot.save-history=true",
    "brobot.screenshot.history-path=history/",
    "brobot.logging.verbosity=VERBOSE"
})
public class ScreenshotSavingDebugTest {

    @Autowired
    private BrobotProperties brobotProperties;
    
    @Autowired
    private IllustrationController illustrationController;
    
    @Autowired
    private LoggingVerbosityConfig loggingConfig;
    
    @BeforeEach
    public void setup() {
        // Ensure verbose logging is enabled
        loggingConfig.setVerbosity(LoggingVerbosityConfig.VerbosityLevel.VERBOSE);
    }
    
    @Test
    public void testConfigurationLoading() {
        System.out.println("\n=== Configuration Loading Test ===");
        
        // Test that properties are loaded correctly
        System.out.println("BrobotProperties.screenshot.saveHistory: " + 
            brobotProperties.getScreenshot().isSaveHistory());
        System.out.println("BrobotProperties.screenshot.historyPath: " + 
            brobotProperties.getScreenshot().getHistoryPath());
            
        // Test that FrameworkSettings are updated
        System.out.println("FrameworkSettings.saveHistory: " + FrameworkSettings.saveHistory);
        System.out.println("FrameworkSettings.historyPath: " + FrameworkSettings.historyPath);
        
        // Verify the values
        assertTrue(brobotProperties.getScreenshot().isSaveHistory(), 
            "BrobotProperties should have saveHistory=true");
        assertTrue(FrameworkSettings.saveHistory, 
            "FrameworkSettings should have saveHistory=true after initialization");
    }
    
    @Test
    public void testHistoryDirectoryExists() {
        System.out.println("\n=== History Directory Test ===");
        
        String historyPath = FrameworkSettings.historyPath;
        Path path = Paths.get(historyPath);
        
        System.out.println("History path: " + path.toAbsolutePath());
        System.out.println("Directory exists: " + Files.exists(path));
        System.out.println("Is directory: " + Files.isDirectory(path));
        System.out.println("Is writable: " + Files.isWritable(path));
        
        // Create directory if it doesn't exist
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("Created history directory");
            } catch (Exception e) {
                System.err.println("Failed to create history directory: " + e.getMessage());
            }
        }
    }
    
    @Test
    public void testIllustrationPermission() {
        System.out.println("\n=== Illustration Permission Test ===");
        
        // Create test action options
        ActionOptions actionOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setIllustrate(ActionOptions.Illustrate.MAYBE) // Use global setting
            .build();
            
        // Create test object collection
        // Create a simple object collection without StateObject
        ObjectCollection objectCollection = new ObjectCollection();
        
        // Test if illustration is allowed
        boolean allowed = illustrationController.okToIllustrate(actionOptions, objectCollection);
        
        System.out.println("Illustration allowed: " + allowed);
        System.out.println("This should be TRUE if saveHistory is properly set");
        
        assertTrue(allowed, "Illustration should be allowed when saveHistory=true");
    }
    
    @Test
    public void testIllustrationExecution() {
        System.out.println("\n=== Illustration Execution Test ===");
        
        // Force settings
        FrameworkSettings.saveHistory = true;
        System.out.println("Forced FrameworkSettings.saveHistory = true");
        
        // Create test data
        ActionResult actionResult = new ActionResult();
        actionResult.setSuccess(true);
        
        List<Region> searchRegions = new ArrayList<>();
        searchRegions.add(new Region(0, 0, 100, 100));
        
        ActionOptions actionOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setIllustrate(ActionOptions.Illustrate.YES) // Force illustration
            .build();
            
        // Create a simple object collection without StateObject
        ObjectCollection objectCollection = new ObjectCollection();
        
        // Test illustration
        System.out.println("Attempting to create illustration...");
        boolean illustrated = illustrationController.illustrateWhenAllowed(
            actionResult, searchRegions, actionOptions, objectCollection);
            
        System.out.println("Illustration created: " + illustrated);
        
        // Check if any files were created in history directory
        File historyDir = new File(FrameworkSettings.historyPath);
        if (historyDir.exists() && historyDir.isDirectory()) {
            File[] files = historyDir.listFiles();
            System.out.println("Files in history directory: " + 
                (files != null ? files.length : 0));
            if (files != null && files.length > 0) {
                for (File file : files) {
                    System.out.println("  - " + file.getName());
                }
            }
        }
    }
}