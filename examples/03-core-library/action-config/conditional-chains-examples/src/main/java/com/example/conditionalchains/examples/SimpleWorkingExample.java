package com.example.conditionalchains.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Simple working examples using the real Brobot API.
 * 
 * This class demonstrates how to use the actual Brobot API for conditional execution patterns,
 * without the non-existent ConditionalActionChain methods from the documentation.
 */
@Component
public class SimpleWorkingExample {
    private static final Logger log = LoggerFactory.getLogger(SimpleWorkingExample.class);
    
    @Autowired
    private Action action;
    
    private StateImage buttonImage;
    
    public SimpleWorkingExample() {
        initializeImages();
    }
    
    private void initializeImages() {
        buttonImage = new StateImage.Builder()
            .setName("ButtonImage")
            .addPattern("button.png")
            .build();
    }
    
    /**
     * Simple find and click pattern using real API.
     */
    public void simpleFindAndClick() {
        log.info("=== Simple Find and Click with Real API ===");
        
        // Create the find options
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.8)
            .build();
        
        // Create object collection
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(buttonImage)
            .build();
        
        // Find the button
        ActionResult findResult = action.perform(findOptions, objects);
        
        if (findResult.isSuccess()) {
            log.info("Button found, clicking it");
            
            // Click the button
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setPauseAfterEnd(0.5)
                .build();
            
            ActionResult clickResult = action.perform(clickOptions, objects);
            
            if (clickResult.isSuccess()) {
                log.info("Successfully clicked button");
            } else {
                log.warn("Failed to click button");
            }
        } else {
            log.warn("Button not found");
        }
    }
    
    /**
     * Retry pattern using real API.
     */
    public ActionResult clickWithRetry(StateImage target, int maxRetries) {
        log.info("=== Click with Retry Pattern ===");
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.7)
            .build();
        
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(target)
            .build();
        
        ActionResult result = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("Attempt {} of {}", attempt, maxRetries);
            
            // Try to find the target
            result = action.perform(findOptions, objects);
            
            if (result.isSuccess()) {
                // Found it, now click
                result = action.perform(clickOptions, objects);
                
                if (result.isSuccess()) {
                    log.info("Successfully clicked on attempt {}", attempt);
                    return result;
                }
            }
            
            // Wait before retry
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.error("Failed after {} attempts", maxRetries);
        if (result == null) {
            result = new ActionResult();
            result.setSuccess(false);
        }
        return result;
    }
    
    /**
     * Demonstrates all working examples.
     */
    public void runAllExamples() {
        log.info("Running simple working examples with real Brobot API\n");
        
        simpleFindAndClick();
        log.info("");
        
        clickWithRetry(buttonImage, 3);
        log.info("");
        
        log.info("All examples completed!");
    }
}