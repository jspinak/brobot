package io.github.jspinak.brobot.examples.logging;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.logging.enhanced.EnhancedActionLogger;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessMonitor;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.awt.Color;
import java.util.List;

/**
 * Demonstration of the enhanced logging features in Brobot.
 * 
 * <p>This example shows:</p>
 * <ul>
 *   <li>Console action reporting with visual indicators</li>
 *   <li>Visual highlighting of finds and search regions</li>
 *   <li>GUI access detection and problem reporting</li>
 *   <li>Performance tracking and warnings</li>
 *   <li>Custom visual feedback options</li>
 * </ul>
 * 
 * <p>Run with different profiles to see different behaviors:</p>
 * <pre>
 * # Maximum visibility for debugging
 * java -jar app.jar --spring.profiles.active=visual-debug
 * 
 * # Normal operation
 * java -jar app.jar
 * 
 * # Quiet mode for CI/CD
 * java -jar app.jar --spring.profiles.active=ci
 * </pre>
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "io.github.jspinak.brobot",
    "io.github.jspinak.brobot.examples"
})
@RequiredArgsConstructor
@Slf4j
public class EnhancedLoggingDemo implements CommandLineRunner {
    
    private final BrobotLogger logger;
    private final EnhancedActionLogger actionLogger;
    private final GuiAccessMonitor guiAccessMonitor;
    private final HighlightManager highlightManager;
    private final Action action;
    
    public static void main(String[] args) {
        SpringApplication.run(EnhancedLoggingDemo.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Enhanced Logging Demo");
        
        try (var session = logger.session("demo-session-" + System.currentTimeMillis())) {
            
            // 1. Check GUI Access
            demonstrateGuiAccessCheck();
            
            // 2. Basic Console Action Reporting
            demonstrateConsoleReporting();
            
            // 3. Visual Highlighting
            demonstrateVisualHighlighting();
            
            // 4. Performance Warnings
            demonstratePerformanceWarnings();
            
            // 5. Custom Visual Feedback
            demonstrateCustomVisualFeedback();
            
            // 6. State Transitions
            demonstrateStateTransitions();
            
            // 7. Automatic Action Logging
            demonstrateAutomaticActionLogging();
            
            logger.observation("Enhanced Logging Demo completed successfully");
        }
    }
    
    /**
     * Demonstrates GUI access checking and problem reporting.
     */
    private void demonstrateGuiAccessCheck() {
        logger.log()
            .observation("=== GUI Access Check Demo ===")
            .console("\n=== GUI Access Check Demo ===\n")
            .log();
            
        boolean guiAccessible = guiAccessMonitor.checkGuiAccess();
        
        if (!guiAccessible) {
            logger.observation("GUI access problems detected - see console output for solutions");
        } else {
            logger.observation("GUI access check passed - display is available");
        }
    }
    
    /**
     * Demonstrates basic console action reporting.
     */
    private void demonstrateConsoleReporting() {
        logger.log()
            .observation("=== Console Action Reporting Demo ===")
            .console("\n=== Console Action Reporting Demo ===\n")
            .log();
            
        // Create mock state objects for demonstration
        StateImage loginButton = new StateImage.Builder()
            .setName("login-button")
            .addPattern("images/login-button.png")
            .build();
            
        StateImage submitButton = new StateImage.Builder()
            .setName("submit-button")
            .addPattern("images/submit-button.png")
            .build();
            
        // Simulate successful find
        logger.observation("Simulating successful find...");
        ActionResult successResult = createMockResult(true, 234, List.of(
            createMockMatch(450, 320, 100, 40, 0.985)
        ));
        
        actionLogger.logAction("FIND", 
            new ObjectCollection.Builder().withImages(loginButton).build(), 
            successResult);
            
        // Simulate failed find
        logger.observation("Simulating failed find...");
        ActionResult failedResult = createMockResult(false, 2003, List.of());
        
        actionLogger.logAction("FIND",
            new ObjectCollection.Builder().withImages(submitButton).build(),
            failedResult);
            
        // Simulate click action
        logger.observation("Simulating click action...");
        ActionResult clickResult = createMockResult(true, 156, List.of());
        
        actionLogger.logAction("CLICK",
            new ObjectCollection.Builder().withImages(loginButton).build(),
            clickResult);
    }
    
    /**
     * Demonstrates visual highlighting features.
     */
    private void demonstrateVisualHighlighting() {
        logger.log()
            .observation("=== Visual Highlighting Demo ===")
            .console("\n=== Visual Highlighting Demo ===\n")
            .log();
            
        // Highlight a successful match
        logger.observation("Highlighting successful match area...");
        Region matchRegion = new Region(450, 320, 100, 40);
        highlightManager.highlightMatches(List.of(
            createMockMatch(matchRegion.x(), matchRegion.y(), 
                          matchRegion.w(), matchRegion.h(), 0.98)
        ));
        
        // Highlight search regions
        logger.observation("Highlighting search regions...");
        List<Region> searchRegions = List.of(
            new Region(100, 100, 400, 300),
            new Region(500, 100, 400, 300)
        );
        highlightManager.highlightSearchRegions(searchRegions);
        
        // Highlight a click location
        logger.observation("Highlighting click location...");
        highlightManager.highlightClick(500, 350);
        
        // Highlight an error region
        logger.observation("Highlighting error region...");
        highlightManager.highlightError(new Region(200, 400, 300, 200));
    }
    
    /**
     * Demonstrates performance warning features.
     */
    private void demonstratePerformanceWarnings() {
        logger.log()
            .observation("=== Performance Warnings Demo ===")
            .console("\n=== Performance Warnings Demo ===\n")
            .log();
            
        StateImage slowElement = new StateImage.Builder()
            .setName("slow-loading-element")
            .build();
            
        // Simulate slow action that triggers warning
        logger.observation("Simulating slow action...");
        ActionResult slowResult = createMockResult(true, 1500, List.of());
        
        actionLogger.logAction("FIND",
            new ObjectCollection.Builder().withImages(slowElement).build(),
            slowResult);
            
        // Simulate very slow action that triggers error threshold
        logger.observation("Simulating very slow action...");
        ActionResult verySlowResult = createMockResult(false, 5500, List.of());
        
        actionLogger.logAction("FIND",
            new ObjectCollection.Builder().withImages(slowElement).build(),
            verySlowResult);
    }
    
    /**
     * Demonstrates custom visual feedback options.
     */
    private void demonstrateCustomVisualFeedback() {
        logger.log()
            .observation("=== Custom Visual Feedback Demo ===")
            .console("\n=== Custom Visual Feedback Demo ===\n")
            .log();
            
        StateImage importantButton = new StateImage.Builder()
            .setName("important-button")
            .build();
            
        // Debug mode - everything visible
        logger.observation("Using debug visual feedback...");
        actionLogger.logActionWithVisuals(
            "FIND",
            new ObjectCollection.Builder().withImages(importantButton).build(),
            createMockResult(true, 150, List.of(createMockMatch(600, 400, 150, 50, 0.99))),
            VisualFeedbackOptions.debug()
        );
        
        // Custom options with yellow highlight and longer duration
        logger.observation("Using custom visual feedback...");
        VisualFeedbackOptions customOptions = VisualFeedbackOptions.builder()
            .highlightFinds(true)
            .findHighlightColor(Color.YELLOW)
            .findHighlightDuration(5.0)
            .flashHighlight(true)
            .flashCount(3)
            .showMatchScore(true)
            .highlightLabel("Important Button Found!")
            .build();
            
        actionLogger.logActionWithVisuals(
            "CLICK",
            new ObjectCollection.Builder().withImages(importantButton).build(),
            createMockResult(true, 100, List.of()),
            customOptions
        );
    }
    
    /**
     * Demonstrates state transition logging.
     */
    private void demonstrateStateTransitions() {
        logger.log()
            .observation("=== State Transition Demo ===")
            .console("\n=== State Transition Demo ===\n")
            .log();
            
        // Simulate successful transition
        logger.observation("Simulating successful state transition...");
        actionLogger.logTransition("LoginPage", "Dashboard", true, 425);
        
        // Simulate failed transition
        logger.observation("Simulating failed state transition...");
        actionLogger.logTransition("Dashboard", "Settings", false, 3000);
    }
    
    /**
     * Demonstrates the new automatic action logging capabilities.
     * Shows how automatic logging reduces boilerplate while providing better observability.
     */
    private void demonstrateAutomaticActionLogging() {
        logger.log()
            .observation("=== Automatic Action Logging Demo ===")
            .console("\n=== Automatic Action Logging Demo ===\n")
            .log();
            
        StateImage searchBox = new StateImage.Builder()
            .setName("search-box")
            .addPattern("images/search-box.png")
            .build();
            
        StateImage searchButton = new StateImage.Builder()
            .setName("search-button")
            .addPattern("images/search-button.png")
            .build();
            
        // 1. Simple automatic logging
        logger.observation("1. Simple automatic logging:");
        PatternFindOptions simpleFind = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for search box...")
            .withSuccessLog("Search box found")
            .withFailureLog("Search box not found")
            .withAfterActionLog("Search completed")
            .build();
            
        // Simulate action (in real usage, would use action.perform())
        actionLogger.logAction("FIND",
            new ObjectCollection.Builder().withImages(searchBox).build(),
            createMockResult(true, 125, List.of(createMockMatch(300, 200, 200, 30, 0.95))));
            
        // 2. Logging with placeholders
        logger.observation("\n2. Logging with dynamic placeholders:");
        ClickOptions clickWithPlaceholders = new ClickOptions.Builder()
            .withBeforeActionLog("Clicking {target}...")
            .withSuccessLog("Successfully clicked {target} at location ({matchCount} matches)")
            .withFailureLog("Failed to click {target} after {duration}ms")
            .withAfterActionLog("Click operation took {duration}ms")
            .build();
            
        // Simulate action with placeholders
        actionLogger.logAction("CLICK",
            new ObjectCollection.Builder().withImages(searchButton).build(),
            createMockResult(true, 85, List.of(createMockMatch(550, 200, 80, 30, 0.99))));
            
        // 3. Advanced logging configuration
        logger.observation("\n3. Advanced logging configuration:");
        TypeOptions advancedType = new TypeOptions.Builder()
            .withLogging(logging -> logging
                .beforeActionMessage("Starting to type search query...")
                .successMessage("Query typed successfully in {duration}ms")
                .failureMessage("Failed to type query - check if field is focused")
                .afterActionMessage("Typing completed")
                .logBeforeAction(true)
                .logOnSuccess(true)
                .logOnFailure(true)
                .logAfterAction(true))
            .build();
            
        // Simulate typing action
        actionLogger.logAction("TYPE",
            new ObjectCollection.Builder().withStrings("example search query").build(),
            createMockResult(true, 750, List.of()));
            
        // 4. Conditional logging based on environment
        logger.observation("\n4. Environment-specific logging:");
        boolean isDebugMode = log.isDebugEnabled();
        
        PatternFindOptions conditionalFind = new PatternFindOptions.Builder()
            .withLogging(logging -> {
                logging.successMessage("Element found")
                      .failureMessage("Element not found");
                
                if (isDebugMode) {
                    logging.beforeActionMessage("DEBUG: Starting search with similarity 0.9")
                          .afterActionMessage("DEBUG: Search completed, {matchCount} matches")
                          .logBeforeAction(true)
                          .logAfterAction(true);
                }
                
                return logging;
            })
            .build();
            
        // 5. Chained actions with logging
        logger.observation("\n5. Chained actions with automatic logging:");
        logger.observation("This would execute: find -> click -> type sequence");
        logger.observation("Each action logs its progress automatically");
        
        // In real usage:
        // PatternFindOptions chainedFind = new PatternFindOptions.Builder()
        //     .withBeforeActionLog("Finding search field...")
        //     .withSuccessLog("Search field located")
        //     .then(new ClickOptions.Builder()
        //         .withBeforeActionLog("Clicking to focus...")
        //         .withSuccessLog("Field focused")
        //         .build())
        //     .then(new TypeOptions.Builder()
        //         .withBeforeActionLog("Typing search query...")
        //         .withSuccessLog("Query entered")
        //         .build())
        //     .build();
    }
    
    // Helper methods for creating mock objects
    
    private ActionResult createMockResult(boolean success, long durationMs, List<Match> matches) {
        ActionResult result = new ActionResult();
        result.setSuccess(success);
        result.setDuration(java.time.Duration.ofMillis(durationMs));
        result.setMatchList(matches);
        result.setActionDescription(success ? "Action completed successfully" : "Action failed");
        return result;
    }
    
    private Match createMockMatch(int x, int y, int w, int h, double score) {
        Match match = new Match();
        match.setRegion(new Region(x, y, w, h));
        match.setScore(score);
        return match;
    }
}