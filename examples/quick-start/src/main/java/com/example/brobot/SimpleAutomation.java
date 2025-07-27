package com.example.brobot;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Simple automation example from the Brobot quick start guide.
 * This demonstrates the core concepts of finding and clicking elements
 * with automatic logging for better observability.
 * 
 * The examples show various logging patterns:
 * - Before action: Log what the automation is about to attempt
 * - Success: Log when an action completes successfully
 * - Failure: Log when an action fails with helpful context
 * - After action: Log when an action completes (regardless of outcome)
 */
@Component
public class SimpleAutomation {
    
    @Autowired
    private Action action;
    
    public void clickButton() {
        // 1. Define what to look for
        StateImage buttonImage = new StateImage.Builder()
                .addPattern("submit-button")
                .build();
        
        // 2. Configure how to find it with automatic logging
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.9)
                .withBeforeActionLog("Searching for submit button...")
                .withSuccessLog("Submit button found")
                .withFailureLog("Submit button not found - check if page loaded correctly")
                .withAfterActionLog("Search completed")
                .build();
        
        // 3. Find the button
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(buttonImage)
                .build();
        
        ActionResult findResult = action.perform(findOptions, objects);
        
        // 4. Click the found button with automatic logging
        if (findResult.isSuccess()) {
            ClickOptions clickOptions = new ClickOptions.Builder()
                    .setClickType(ClickOptions.Type.LEFT)
                    .withBeforeActionLog("Attempting to click submit button...")
                    .withSuccessLog("Successfully clicked the submit button")
                    .withFailureLog("Failed to click submit button at {target}")
                    .withAfterActionLog("Click action completed in {duration}ms")
                    .build();
            
            ObjectCollection clickObjects = new ObjectCollection.Builder()
                    .withMatches(findResult.getMatchList())
                    .build();
            
            action.perform(clickOptions, clickObjects);
            // No need for manual success/failure logging - it's handled automatically!
        }
    }
    
    /**
     * Example using fluent find presets
     */
    public void findImagesExample() {
        StateImage targetImage = new StateImage.Builder()
                .setName("target")
                .addPattern("target.png")
                .build();
        
        // Quick search - optimized for speed
        PatternFindOptions quickSearch = PatternFindOptions.forQuickSearch();
        performFind(quickSearch, targetImage, "Quick Search");
        
        // Precise search - optimized for accuracy
        PatternFindOptions preciseSearch = PatternFindOptions.forPreciseSearch();
        performFind(preciseSearch, targetImage, "Precise Search");
        
        // Custom search with detailed logging
        PatternFindOptions customSearch = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.85)
                .setMaxMatchesToActOn(5)
                .withLogging(logging -> logging
                        .beforeActionMessage("Starting exhaustive search for {target}...")
                        .successMessage("Found {matchCount} instances of {target} in {duration}ms")
                        .failureMessage("Could not find {target} with similarity >= 0.85")
                        .afterActionMessage("Exhaustive search completed")
                        .logBeforeAction(true)
                        .logOnSuccess(true)
                        .logOnFailure(true)
                        .logAfterAction(true))
                .build();
        performFind(customSearch, targetImage, "Custom Search");
    }
    
    private void performFind(PatternFindOptions options, StateImage image, String searchType) {
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        
        ActionResult result = action.perform(options, objects);
        System.out.println(searchType + " found " + result.getMatchList().size() + " matches");
    }
}