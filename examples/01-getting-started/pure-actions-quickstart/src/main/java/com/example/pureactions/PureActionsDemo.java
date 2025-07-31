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
import io.github.jspinak.brobot.model.region.Location;
import io.github.jspinak.brobot.model.region.Region;
import io.github.jspinak.brobot.model.state.StateImage;
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
            action.perform(ActionType.CLICK, found.getFirstMatch());
        }
        
        // Even Better Way (With action chaining)
        log.info("Best way: Using action chaining");
        PatternFindOptions findAndClick = new PatternFindOptions.Builder()
                .then(new ClickOptions.Builder().build())
                .build();
        ActionResult chainResult = action.perform(findAndClick, 
                new ObjectCollection.Builder()
                        .withImages(buttonImage)
                        .build());
    }
    
    /**
     * Common Use Case 1: Click a Button
     */
    public void clickButtonExample() {
        log.info("=== Click Button Example ===");
        
        // Simplest form - using convenience method
        Location buttonLocation = new Location(100, 200);
        action.perform(ActionType.CLICK, buttonLocation);
        
        // With an image
        StateImage submitButton = new StateImage.Builder()
                .addPatterns("submit-button")
                .build();
        
        action.perform(
                new PatternFindOptions.Builder()
                        .then(new ClickOptions.Builder().build())
                        .build(),
                new ObjectCollection.Builder()
                        .withImages(submitButton)
                        .build());
    }
    
    /**
     * Common Use Case 2: Type in a Field
     */
    public void typeInFieldExample() {
        log.info("=== Type in Field Example ===");
        
        // Type at current cursor position
        action.perform(ActionType.TYPE, "Hello World");
        
        // Find field and type
        StateImage textField = new StateImage.Builder()
                .addPatterns("text-field")
                .build();
        
        action.perform(
                new PatternFindOptions.Builder()
                        .then(new ClickOptions.Builder().build())
                        .then(new TypeOptions.Builder()
                                .setTypeDelay(0.1)
                                .build())
                        .build(),
                new ObjectCollection.Builder()
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
            Match firstMatch = findResult.getFirstMatch();
            log.info("First match found at: {}", firstMatch.getLocation());
            
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
                            .withMatches(result.getMatchList())
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
                            .withMatches(result.getMatchList())
                            .build());
            log.info("Document saved");
        } else {
            log.warn("Save button not found");
            tryAlternativeSave();
        }
    }
    
    /**
     * Best Practice: Reuse find results
     */
    public void reuseFindResults() {
        log.info("=== Reuse Find Results ===");
        
        StateImage allButtons = new StateImage.Builder()
                .addPatterns("button-pattern")
                .build();
        
        // Find once, use multiple times
        ActionResult buttons = action.find(allButtons);
        
        log.info("Found {} buttons", buttons.getMatchList().size());
        
        for (Match button : buttons.getMatchList()) {
            // Highlight with pause after
            HighlightOptions highlight = new HighlightOptions.Builder()
                    .setPauseAfterEnd(0.5)
                    .build();
            
            action.perform(highlight, new ObjectCollection.Builder()
                    .withMatches(button)
                    .build());
        }
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
}