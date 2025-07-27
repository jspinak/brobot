package com.example.chaining.demos;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates advanced action chaining patterns including
 * dynamic chains, error recovery, and complex workflows.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdvancedChainingDemo {
    
    private final Action action;
    
    public void runDemos() {
        log.info("\n=== Advanced Action Chaining Demos ===");
        
        // Demo 1: Dynamic chain building
        demonstrateDynamicChain();
        
        // Demo 2: Error recovery chain
        demonstrateErrorRecoveryChain();
        
        // Demo 3: Complex workflow with validation
        demonstrateComplexWorkflow();
        
        // Demo 4: Parallel-like execution
        demonstrateParallelExecution();
    }
    
    /**
     * Demo 1: Dynamic chain building
     * Shows how to build chains based on runtime conditions
     */
    private void demonstrateDynamicChain() {
        log.info("\n--- Demo 1: Dynamic Chain Building ---");
        
        // Simulate dynamic data
        List<String> fieldsToFill = List.of("firstName", "lastName", "email", "phone");
        boolean requiresLogin = true;
        boolean saveAfterFill = true;
        
        // Start with base action
        PatternFindOptions.Builder chainBuilder = new PatternFindOptions.Builder()
            .withBeforeActionLog("Starting dynamic form process...")
            .withSuccessLog("Form ready");
            
        // Add login step if required
        if (requiresLogin) {
            chainBuilder = (PatternFindOptions.Builder) chainBuilder
                .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Login required - looking for login button...")
                    .withSuccessLog("Login button found")
                    .build())
                .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Clicking login...")
                    .withSuccessLog("Login dialog opened")
                    .build())
                // Add login sequence
                .then(createLoginChain());
        }
        
        // Dynamically add field operations
        for (String fieldName : fieldsToFill) {
            chainBuilder = (PatternFindOptions.Builder) chainBuilder
                .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Looking for " + fieldName + " field...")
                    .withSuccessLog("Found " + fieldName)
                    .build())
                .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Focusing " + fieldName + "...")
                    .build())
                .then(new TypeOptions.Builder()
                    .withBeforeActionLog("Entering " + fieldName + " data...")
                    .withSuccessLog(fieldName + " filled")
                    .build());
        }
        
        // Add save step if required
        if (saveAfterFill) {
            chainBuilder = (PatternFindOptions.Builder) chainBuilder
                .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Looking for save button...")
                    .withSuccessLog("Save button found")
                    .build())
                .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Saving form data...")
                    .withSuccessLog("Form saved successfully")
                    .withAfterActionLog("Dynamic workflow completed in {duration}ms")
                    .build());
        }
        
        ActionConfig dynamicChain = chainBuilder.build();
        log.info("Dynamic chain created with {} steps", fieldsToFill.size() + (requiresLogin ? 3 : 0) + (saveAfterFill ? 2 : 0));
    }
    
    /**
     * Demo 2: Error recovery chain
     * Shows how to handle failures within chains
     */
    private void demonstrateErrorRecoveryChain() {
        log.info("\n--- Demo 2: Error Recovery Chain ---");
        
        // Create a chain with recovery logic
        ActionConfig recoveryChain = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for primary target...")
            .withFailureLog("Primary target not found - attempting recovery")
            .setSuccessCriteria(result -> {
                if (!result.isSuccess()) {
                    log.info("Primary search failed - triggering recovery sequence");
                    // In real scenario, would trigger alternate search
                    return false;
                }
                return true;
            })
            // Recovery sequence
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Recovery: Looking for menu button...")
                .withSuccessLog("Menu button found - opening menu")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Opening menu for alternate navigation...")
                .withSuccessLog("Menu opened")
                .setPauseAfterEnd(0.5)
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for target in menu...")
                .withSuccessLog("Target found via menu")
                .withFailureLog("Recovery failed - target not accessible")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Clicking target...")
                .withSuccessLog("Successfully accessed target via recovery path")
                .build())
            .build();
            
        log.info("Error recovery chain created - will attempt alternate path on failure");
    }
    
    /**
     * Demo 3: Complex workflow with validation
     * Shows a multi-step process with validation checks
     */
    private void demonstrateComplexWorkflow() {
        log.info("\n--- Demo 3: Complex Workflow with Validation ---");
        
        // Create a file upload workflow with validation
        ActionChainOptions uploadWorkflow = new ActionChainOptions.Builder(
            new PatternFindOptions.Builder()
                .withBeforeActionLog("Starting file upload workflow...")
                .withSuccessLog("Upload interface ready")
                .build()
        )
        // Click upload button
        .then(new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for upload button...")
            .withSuccessLog("Upload button found")
            .build())
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Opening file selector...")
            .withSuccessLog("File selector opened")
            .setPauseAfterEnd(1.0)
            .build())
        // Type file path
        .then(new TypeOptions.Builder()
            .withBeforeActionLog("Entering file path...")
            .withSuccessLog("File path entered")
            .build())
        // Confirm selection
        .then(new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for Open/Select button...")
            .withSuccessLog("Select button found")
            .build())
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Confirming file selection...")
            .withSuccessLog("File selected")
            .build())
        // Validate file was loaded
        .then(new PatternFindOptions.Builder()
            .withBeforeActionLog("Validating file preview...")
            .withSuccessLog("File preview confirmed")
            .withFailureLog("File preview not found - upload may have failed")
            .setPauseBeforeBegin(1.0) // Wait for preview
            .build())
        // Add metadata
        .then(new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for description field...")
            .withSuccessLog("Description field found")
            .build())
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Focusing description field...")
            .build())
        .then(new TypeOptions.Builder()
            .withBeforeActionLog("Adding file description...")
            .withSuccessLog("Description added")
            .build())
        // Final upload
        .then(new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for final upload button...")
            .withSuccessLog("Upload button ready")
            .build())
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Uploading file...")
            .withSuccessLog("File uploaded successfully")
            .withAfterActionLog("Upload workflow completed in {duration}ms")
            .setPauseAfterEnd(2.0) // Wait for upload confirmation
            .build())
        // Verify success message
        .then(new PatternFindOptions.Builder()
            .withBeforeActionLog("Checking for success confirmation...")
            .withSuccessLog("Upload confirmed - workflow complete")
            .withFailureLog("No confirmation found - check upload status")
            .build())
        .build();
        
        log.info("Complex upload workflow created with validation steps");
    }
    
    /**
     * Demo 4: Parallel-like execution
     * Shows how to handle multiple independent action sequences
     */
    private void demonstrateParallelExecution() {
        log.info("\n--- Demo 4: Parallel-like Execution ---");
        
        // Create multiple independent chains
        List<ActionConfig> independentChains = new ArrayList<>();
        
        // Chain 1: Update profile picture
        ActionConfig updatePictureChain = new PatternFindOptions.Builder()
            .withBeforeActionLog("[Profile] Looking for profile picture...")
            .withSuccessLog("[Profile] Profile picture found")
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("[Profile] Opening picture options...")
                .withSuccessLog("[Profile] Picture options opened")
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("[Profile] Looking for upload option...")
                .withSuccessLog("[Profile] Upload option found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("[Profile] Initiating upload...")
                .withSuccessLog("[Profile] Picture updated")
                .build())
            .build();
        independentChains.add(updatePictureChain);
        
        // Chain 2: Update status message
        ActionConfig updateStatusChain = new PatternFindOptions.Builder()
            .withBeforeActionLog("[Status] Looking for status field...")
            .withSuccessLog("[Status] Status field found")
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("[Status] Clicking status field...")
                .build())
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("[Status] Updating status message...")
                .withSuccessLog("[Status] Status updated")
                .build())
            .build();
        independentChains.add(updateStatusChain);
        
        // Chain 3: Check notifications
        ActionConfig checkNotificationsChain = new PatternFindOptions.Builder()
            .withBeforeActionLog("[Notif] Looking for notification icon...")
            .withSuccessLog("[Notif] Notifications found")
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("[Notif] Opening notifications...")
                .withSuccessLog("[Notif] Notifications panel opened")
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("[Notif] Checking for new items...")
                .withSuccessLog("[Notif] Found {matchCount} new notifications")
                .build())
            .build();
        independentChains.add(checkNotificationsChain);
        
        log.info("Created {} independent chains for parallel-like execution", independentChains.size());
        log.info("In real execution, these would run sequentially but could be optimized");
        
        // Demonstrate execution order logging
        for (int i = 0; i < independentChains.size(); i++) {
            log.info("Chain {} would execute independently with its own logging context", i + 1);
        }
    }
    
    /**
     * Helper method to create a reusable login chain
     */
    private ActionConfig createLoginChain() {
        return new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for username field...")
            .withSuccessLog("Username field found")
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Focusing username...")
                .build())
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Entering username...")
                .withSuccessLog("Username entered")
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for password field...")
                .withSuccessLog("Password field found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Focusing password...")
                .build())
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Entering password...")
                .withSuccessLog("Password entered")
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for login submit button...")
                .withSuccessLog("Login button found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Submitting login...")
                .withSuccessLog("Login successful")
                .setPauseAfterEnd(2.0) // Wait for login processing
                .build())
            .build();
    }
}