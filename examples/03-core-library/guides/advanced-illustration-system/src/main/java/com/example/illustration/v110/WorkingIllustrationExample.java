package com.example.illustration.v110;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

/**
 * Demonstrates real IllustrationController usage in Brobot v1.1.0.
 * 
 * The IllustrationController in v1.1.0 provides:
 * - okToIllustrate() - Check if illustration should happen
 * - illustrateWhenAllowed() - Create illustration if allowed
 * - No setConfig() method - configuration is via application properties
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkingIllustrationExample {
    
    private final Action action;
    private final IllustrationController illustrationController;
    
    /**
     * Demonstrates checking illustration permissions
     */
    public void demonstrateIllustrationChecking() {
        log.info("=== Illustration Permission Checking ===");
        
        // Create test objects
        StateImage button = new StateImage.Builder()
            .setName("TestButton")
            .addPatterns("button")
            .build();
            
        ObjectCollection buttonCollection = new ObjectCollection.Builder()
            .withImages(button)
            .build();
        
        // Check if find operations should be illustrated
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.9)
            .build();
            
        boolean shouldIllustrateFInd = illustrationController.okToIllustrate(
            findOptions, buttonCollection
        );
        
        log.info("Should illustrate FIND operation: {}", shouldIllustrateFInd);
        
        // Check if click operations should be illustrated
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .build();
            
        boolean shouldIllustrateClick = illustrationController.okToIllustrate(
            clickOptions, buttonCollection
        );
        
        log.info("Should illustrate CLICK operation: {}", shouldIllustrateClick);
    }
    
    /**
     * Demonstrates manual illustration control
     */
    public void demonstrateManualIllustration() {
        log.info("=== Manual Illustration Control ===");
        
        StateImage loginButton = new StateImage.Builder()
            .setName("LoginButton")
            .addPatterns("login_button")
            .build();
            
        ObjectCollection loginCollection = new ObjectCollection.Builder()
            .withImages(loginButton)
            .build();
        
        // Perform action
        ActionResult result = action.perform(
            new PatternFindOptions.Builder().build(),
            loginCollection);
        
        // Manually control illustration
        if (result.isSuccess()) {
            // Define search regions (optional)
            List<Region> searchRegions = new ArrayList<>();
            searchRegions.add(new Region(0, 0, 1920, 1080)); // Full screen
            
            // Create illustration using the controller
            PatternFindOptions findConfig = new PatternFindOptions.Builder()
                .setSimilarity(0.9)
                .build();
                
            boolean illustrated = illustrationController.illustrateWhenAllowed(
                result,
                searchRegions,
                findConfig,
                loginCollection
            );
            
            if (illustrated) {
                log.info("Created illustration for login button find");
            } else {
                log.info("Illustration was filtered (likely duplicate)");
            }
        }
    }
    
    /**
     * Demonstrates illustration filtering behavior
     */
    public void demonstrateIllustrationFiltering() {
        log.info("=== Illustration Filtering ===");
        
        StateImage element = new StateImage.Builder()
            .setName("RepeatedElement")
            .addPatterns("element")
            .build();
            
        ObjectCollection elementCollection = new ObjectCollection.Builder()
            .withImages(element)
            .build();
        
        // Perform same action multiple times
        for (int i = 0; i < 5; i++) {
            log.info("Attempt {}: Finding element", i + 1);
            
            ActionResult result = action.perform(
                new PatternFindOptions.Builder().build(),
                elementCollection);
            
            // Check if this would be illustrated
            PatternFindOptions findConfig = new PatternFindOptions.Builder().build();
            boolean wouldIllustrate = illustrationController.okToIllustrate(
                findConfig, elementCollection
            );
            
            log.info("  Would illustrate: {}", wouldIllustrate);
            
            // Note: First occurrence usually gets illustrated,
            // subsequent identical actions are filtered
        }
    }
    
    /**
     * Demonstrates working with different action types
     */
    public void demonstrateActionTypes() {
        log.info("=== Different Action Types ===");
        
        // Check illustration permissions for different actions
        ObjectCollection dummyCollection = new ObjectCollection.Builder().build();
        
        // These checks tell you what's configured in application.properties
        log.info("Illustration permissions by action type:");
        log.info("(Controlled by brobot.illustration.draw-* properties)");
        
        // The actual permissions are checked internally by the controller
        // based on the ActionConfig type you pass
    }
    
    /**
     * Best practices for illustration in v1.1.0
     */
    public void demonstrateBestPractices() {
        log.info("=== Illustration Best Practices ===");
        
        // 1. Let the framework handle illustrations automatically
        log.info("1. Automatic illustration:");
        StateImage autoButton = new StateImage.Builder()
            .setName("AutoButton")
            .addPatterns("auto_button")
            .build();
            
        // This will automatically illustrate based on settings
        action.perform(
            new ClickOptions.Builder().build(),
            new ObjectCollection.Builder()
                .withImages(autoButton)
                .build());
        
        // 2. Use ActionConfig.illustrate for explicit control
        log.info("\n2. Explicit control with ActionConfig:");
        ClickOptions forceIllustrate = new ClickOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.YES)  // Force illustration
            .build();
            
        action.perform(forceIllustrate, new ObjectCollection.Builder()
            .withImages(autoButton)
            .build());
        
        // 3. Disable illustration for specific actions
        log.info("\n3. Disable illustration:");
        ClickOptions noIllustrate = new ClickOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.NO)  // Prevent illustration
            .build();
            
        action.perform(noIllustrate, new ObjectCollection.Builder()
            .withImages(autoButton)
            .build());
    }
    
    /**
     * Understanding illustration state
     */
    public void demonstrateIllustrationState() {
        log.info("=== Illustration State ===");
        
        // The controller tracks last action to prevent duplicates
        log.info("Last action type: {}", illustrationController.getLastAction());
        log.info("Last find type: {}", illustrationController.getLastFind());
        log.info("Last collections count: {}", 
            illustrationController.getLastCollections().size());
        
        // Action permissions from configuration
        log.info("\nAction permissions:");
        illustrationController.getActionPermissions().forEach((action, allowed) -> 
            log.info("  {} : {}", action, allowed)
        );
    }
    
    /**
     * Run all examples
     */
    public void runAllExamples() {
        demonstrateIllustrationChecking();
        log.info("");
        
        demonstrateManualIllustration();
        log.info("");
        
        demonstrateIllustrationFiltering();
        log.info("");
        
        demonstrateActionTypes();
        log.info("");
        
        demonstrateBestPractices();
        log.info("");
        
        demonstrateIllustrationState();
    }
}