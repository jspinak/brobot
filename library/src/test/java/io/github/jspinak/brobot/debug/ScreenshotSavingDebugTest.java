package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.config.BrobotProperties;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
// Note: This test requires Spring Boot context which may not be available
// It's provided as a debugging tool for screenshot saving functionality
public class ScreenshotSavingDebugTest {

    // These would normally be autowired in a Spring context
    private BrobotProperties brobotProperties;
    private IllustrationController illustrationController;
    private LoggingVerbosityConfig loggingConfig;
    
    @BeforeEach
    public void setup() {
        // Initialize components (would normally be done by Spring)
        // For now, tests will need to handle null checks
    }
    
    @Test
    public void testConfigurationLoading() {
        System.out.println("\n=== Configuration Loading Test ===");
        
        if (brobotProperties != null) {
            // Test that properties are loaded correctly
            System.out.println("BrobotProperties.screenshot.saveHistory: " + 
                brobotProperties.getScreenshot().isSaveHistory());
            System.out.println("BrobotProperties.screenshot.historyPath: " + 
                brobotProperties.getScreenshot().getHistoryPath());
        } else {
            System.out.println("BrobotProperties not available (requires Spring context)");
        }
            
        // Test that FrameworkSettings are updated
        System.out.println("FrameworkSettings.saveHistory: " + FrameworkSettings.saveHistory);
        System.out.println("FrameworkSettings.historyPath: " + FrameworkSettings.historyPath);
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
        
        if (illustrationController == null) {
            System.out.println("IllustrationController not available (requires Spring context)");
            return;
        }
        
        // Create test action config
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .build();
            
        // Create test object collection
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();
        
        // Test if illustration is allowed
        boolean allowed = illustrationController.okToIllustrate(findOptions, objectCollection);
        
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
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .build();
            
        // Create test object collection
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();
        
        // Test illustration
        System.out.println("Attempting to create illustration...");
        if (illustrationController != null) {
            boolean illustrated = illustrationController.illustrateWhenAllowed(
                actionResult, searchRegions, findOptions, objectCollection);
            System.out.println("Illustration created: " + illustrated);
        } else {
            System.out.println("IllustrationController not available (requires Spring context)");
        }
        
        // Check if any files were created in history directory
        File historyDir = new File(FrameworkSettings.historyPath);
        if (historyDir.exists() && historyDir.isDirectory()) {
            File[] files = historyDir.listFiles();
            System.out.println("Files in history directory: " + 
                (files != null ? files.length : 0));
            if (files != null) {
                for (File file : files) {
                    System.out.println("  - " + file.getName());
                }
            }
        }
    }
    
    @Test
    public void testIllustrationWithMockSettings() {
        System.out.println("\n=== Illustration with Mock Settings Test ===");
        
        // Test with mock mode
        boolean originalMock = FrameworkSettings.mock;
        FrameworkSettings.mock = true;
        
        ActionResult actionResult = new ActionResult();
        actionResult.setSuccess(true);
        
        List<Region> searchRegions = new ArrayList<>();
        searchRegions.add(new Region(0, 0, 100, 100));
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .build();
            
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();
        
        System.out.println("Mock mode: " + FrameworkSettings.mock);
        if (illustrationController != null) {
            boolean illustrated = illustrationController.illustrateWhenAllowed(
                actionResult, searchRegions, findOptions, objectCollection);
            System.out.println("Illustration in mock mode: " + illustrated);
        } else {
            System.out.println("IllustrationController not available (requires Spring context)");
        }
        
        // Restore original mock setting
        FrameworkSettings.mock = originalMock;
    }
    
    @Test
    public void testVerbosityLevels() {
        System.out.println("\n=== Verbosity Levels Test ===");
        
        // Test different verbosity levels
        LoggingVerbosityConfig.VerbosityLevel[] levels = {
            LoggingVerbosityConfig.VerbosityLevel.QUIET,
            LoggingVerbosityConfig.VerbosityLevel.NORMAL,
            LoggingVerbosityConfig.VerbosityLevel.VERBOSE
        };
        
        for (LoggingVerbosityConfig.VerbosityLevel level : levels) {
            if (loggingConfig != null) {
                loggingConfig.setVerbosity(level);
            }
            System.out.println("Verbosity set to: " + level);
            System.out.println("  saveHistory: " + FrameworkSettings.saveHistory);
        }
    }
    
    @Test
    public void testActionConfigIllustration() {
        System.out.println("\n=== ActionConfig Illustration Test ===");
        
        // Test with different ActionConfig implementations
        ActionConfig[] configs = {
            new PatternFindOptions.Builder().build(),
            new io.github.jspinak.brobot.action.basic.click.ClickOptions.Builder().build(),
            new io.github.jspinak.brobot.action.basic.type.TypeOptions.Builder().build()
        };
        
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();
        
        for (ActionConfig config : configs) {
            System.out.println("Testing with: " + config.getClass().getSimpleName());
            if (illustrationController != null) {
                boolean allowed = illustrationController.okToIllustrate(config, objectCollection);
                System.out.println("  Illustration allowed: " + allowed);
            } else {
                System.out.println("  IllustrationController not available");
            }
        }
    }
}