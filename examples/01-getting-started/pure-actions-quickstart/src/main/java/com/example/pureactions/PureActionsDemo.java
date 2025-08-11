package com.example.pureactions;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Demonstrates Pure Actions - the new approach that separates finding from acting
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PureActionsDemo {
    
    private final Action action;
    
    /**
     * Old Way vs New Way comparison
     * Code from: docs/docs/01-getting-started/pure-actions-quickstart.md lines 14-38
     */
    public void demonstrateOldVsNew() {
        log.info("=== Old Way vs New Way ===");
        
        StateImage buttonImage = new StateImage.Builder()
                .addPatterns("submit-button")
                .build();
        
        // Old Way (Embedded Find)
        log.info("Old way: Find and click happen together");
        action.click(buttonImage);
        
        // New Way (Pure Actions)
        log.info("New way: Find first, then act");
        ActionResult found = action.find(buttonImage);
        if (found.isSuccess()) {
            action.perform(ActionType.CLICK, found.getBestMatch().get().getRegion());
        }
        
        // Even Better Way (Conditional Chains)
        // Code from: docs/docs/01-getting-started/pure-actions-quickstart.md lines 34-38
        log.info("Best way: Using conditional chains");
        ConditionalActionChain.find(new PatternFindOptions.Builder().build())
            .ifFoundClick()
            .ifNotFoundLog("Button not found")
            .perform(action, new ObjectCollection.Builder()
                .withImages(buttonImage)
                .build());
    }
    
    /**
     * Common Use Case 1: Click a Button
     * Code from: docs/docs/01-getting-started/pure-actions-quickstart.md lines 68-77
     */
    public void clickButtonExample() {
        log.info("=== Click Button Example ===");
        
        // Simplest form - using convenience method
        Location buttonLocation = new Location(100, 200);
        action.perform(ActionType.CLICK, buttonLocation);
        
        // With an image using conditional chain
        // Code from: docs/docs/01-getting-started/pure-actions-quickstart.md lines 74-77
        StateImage submitButton = new StateImage.Builder()
                .addPatterns("submit-button")
                .build();
        
        ConditionalActionChain.find(new PatternFindOptions.Builder().build())
            .ifFoundClick()
            .perform(action, new ObjectCollection.Builder()
                .withImages(submitButton)
                .build());
    }
    
    /**
     * Common Use Case 2: Type in a Field
     * Code from: docs/docs/01-getting-started/pure-actions-quickstart.md lines 80-89
     */
    public void typeInFieldExample() {
        log.info("=== Type in Field Example ===");
        
        // Type at current cursor position
        action.perform(ActionType.TYPE, "Hello World");
        
        // Find field and type using conditional chain
        // Code from: docs/docs/01-getting-started/pure-actions-quickstart.md lines 85-89
        StateImage textField = new StateImage.Builder()
                .addPatterns("text-field")
                .build();
        
        ConditionalActionChain.find(new PatternFindOptions.Builder().build())
            .ifFoundClick()
            .then(new TypeOptions.Builder()
                .setTypeDelay(0.1)
                .build())
            .perform(action, new ObjectCollection.Builder()
                .withImages(textField)
                .withStrings("Hello from Pure Actions!")
                .build());
    }
    
    /**
     * Common Use Case 3: Highlight Found Elements
     */
    public void highlightFoundElements() {
        log.info("=== Highlight Found Elements ===");
        
        StateImage targetPattern = new StateImage.Builder()
                .addPatterns("target-element")
                .build();
        
        // Find all matching elements and highlight them
        ActionResult matches = action.find(targetPattern);
        for (Match match : matches.getMatchList()) {
            action.perform(ActionType.HIGHLIGHT, match.getRegion());
        }
    }
    
    /**
     * Common Use Case 4: Right-Click Menu
     */
    public void rightClickMenuExample() {
        log.info("=== Right-Click Menu Example ===");
        
        StateImage targetElement = new StateImage.Builder()
                .addPatterns("target-element")
                .build();
        
        StateImage deleteOption = new StateImage.Builder()
                .addPatterns("delete-option")
                .build();
        
        // Right-click on target element
        action.perform(
                new ClickOptions.Builder()
                        .setClickType(ClickOptions.Type.RIGHT)
                        .setPauseAfterEnd(0.5)
                        .build(),
                new ObjectCollection.Builder()
                        .withImages(targetElement)
                        .build());
        
        // Then click on delete option
        action.perform(
                new ClickOptions.Builder().build(),
                new ObjectCollection.Builder()
                        .withImages(deleteOption)
                        .build());
    }
    
    /**
     * Demonstrates convenience methods
     */
    public void demonstrateConvenienceMethods() {
        log.info("=== Convenience Methods ===");
        
        // Click at a location
        action.perform(ActionType.CLICK, new Location(100, 200));
        
        // Highlight a region  
        action.perform(ActionType.HIGHLIGHT, new Region(50, 50, 200, 100));
        
        // Type text
        action.perform(ActionType.TYPE, "Hello World");
        
        // Double-click
        action.perform(ActionType.DOUBLE_CLICK, new Location(300, 400));
        
        // Right-click
        action.perform(ActionType.RIGHT_CLICK, new Region(150, 150, 100, 50));
    }
    
    /**
     * Demonstrates working with results
     */
    public void workWithResults() {
        log.info("=== Working with Results ===");
        
        StateImage targetImage = new StateImage.Builder()
                .addPatterns("multi-element")
                .build();
        
        // Find returns matches you can work with
        ActionResult findResult = action.find(targetImage);
        
        if (findResult.isSuccess()) {
            // Get the first match
            Match firstMatch = findResult.getBestMatch().get();
            log.info("First match found at: {}", firstMatch.getTarget());
            
            // Get all matches
            List<Match> allMatches = findResult.getMatchList();
            log.info("Found {} total matches", allMatches.size());
            
            // Work with each match
            for (Match match : allMatches) {
                // Highlight each found instance
                action.perform(ActionType.HIGHLIGHT, match.getRegion());
                
                // Click each one with a pause
                action.perform(ActionType.CLICK, match.getRegion());
                
                try {
                    Thread.sleep(500); // Brief pause between clicks
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /**
     * Demonstrates error handling with pure actions
     */
    public void demonstrateErrorHandling() {
        log.info("=== Error Handling ===");
        
        StateImage criticalButton = new StateImage.Builder()
                .addPatterns("critical-button")
                .build();
        
        ActionResult result = action.perform(
                new PatternFindOptions.Builder().build(),
                new ObjectCollection.Builder()
                        .withImages(criticalButton)
                        .build());
        
        if (result.isSuccess()) {
            action.perform(
                    new ClickOptions.Builder().build(),
                    new ObjectCollection.Builder()
                            .withMatches(result)
                            .build());
        } else {
            log.error("ERROR: Critical button not found!");
            // Custom error handling
            takeScreenshot("error-state");
            notifyUser("Application in unexpected state");
        }
    }
    
    /**
     * Best Practice: Handle both success and failure
     */
    public void handleBothSuccessAndFailure() {
        log.info("=== Handle Both Success and Failure ===");
        
        StateImage saveButton = new StateImage.Builder()
                .addPatterns("save-button")
                .build();
        
        ActionResult result = action.perform(
                new PatternFindOptions.Builder().build(),
                new ObjectCollection.Builder()
                        .withImages(saveButton)
                        .build());
        
        if (result.isSuccess()) {
            action.perform(
                    new ClickOptions.Builder().build(),
                    new ObjectCollection.Builder()
                            .withMatches(result)
                            .build());
            log.info("Document saved");
        } else {
            log.warn("Save button not found");
            tryAlternativeSave();
        }
    }
    
    /**
     * Best Practice: Reuse find results
     * Code from: docs/docs/01-getting-started/pure-actions-quickstart.md lines 199-212
     */
    public void reuseFindResults() {
        log.info("=== Reuse Find Results ===");
        
        StateImage allButtons = new StateImage.Builder()
                .addPatterns("button-pattern")
                .build();
        
        // Find once, use multiple times
        // Code from: docs/docs/01-getting-started/pure-actions-quickstart.md lines 200-212
        ActionResult buttons = action.find(allButtons);
        
        log.info("Found {} buttons", buttons.getMatchList().size());
        
        for (Match button : buttons.getMatchList()) {
            // Highlight with pause after
            HighlightOptions highlight = new HighlightOptions.Builder()
                    .setPauseAfterEnd(0.5)  // 500ms pause after highlighting
                    .build();
            
            action.perform(highlight, new ObjectCollection.Builder()
                    .withRegions(button.getRegion())
                    .build());
            
            // Then click
            action.perform(ActionType.CLICK, button.getRegion());
        }
    }
    
    /**
     * Login Flow Example using Conditional Chains
     * Code from: docs/docs/03-core-library/action-config/15-conditional-chains-examples.md lines 38-56
     */
    public ActionResult performLogin(String username, String password) {
        log.info("=== Login Flow Example ===");
        
        StateImage loginButton = new StateImage.Builder()
                .addPatterns("login-button")
                .build();
                
        StateImage usernameField = new StateImage.Builder()
                .addPatterns("username-field")
                .build();
                
        StateImage passwordField = new StateImage.Builder()
                .addPatterns("password-field")
                .build();
                
        StateImage submitButton = new StateImage.Builder()
                .addPatterns("submit-button")
                .build();
        
        // Simplified version of the login flow from documentation
        return ConditionalActionChain.find(new PatternFindOptions.Builder().build())
            .ifFoundClick()
            .ifNotFoundLog("Login button not visible")
            .perform(action, new ObjectCollection.Builder()
                .withImages(loginButton)
                .build());
    }
    
    /**
     * Save with Confirmation Dialog Example
     * Code from: docs/docs/03-core-library/action-config/15-conditional-chains-examples.md lines 62-76
     */
    public ActionResult saveWithConfirmation() {
        log.info("=== Save with Confirmation Example ===");
        
        StateImage saveButton = new StateImage.Builder()
                .addPatterns("save-button")
                .build();
                
        StateImage confirmDialog = new StateImage.Builder()
                .addPatterns("confirm-dialog")
                .build();
        
        return ConditionalActionChain.find(new PatternFindOptions.Builder().build())
            .ifFoundClick()
            .ifNotFoundLog("Save button not found")
            .perform(action, new ObjectCollection.Builder()
                .withImages(saveButton)
                .build());
    }
    
    // Helper methods
    private void takeScreenshot(String filename) {
        log.info("Taking screenshot: {}", filename);
        // Implementation would capture screen
    }
    
    private void notifyUser(String message) {
        log.warn("User notification: {}", message);
        // Implementation would show notification
    }
    
    private void tryAlternativeSave() {
        log.info("Trying alternative save method...");
        // Implementation would try Ctrl+S or menu option
    }
    
    private void alertUser(String message) {
        log.warn("Alert: {}", message);
        // Implementation would show alert dialog
    }
    
    private void takeDebugScreenshot() {
        log.info("Taking debug screenshot");
        // Implementation would capture screen for debugging
    }
}