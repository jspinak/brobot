package com.example.quickstart;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Demonstrates different approaches to GUI automation with Brobot
 */
@Component
@Slf4j  // Add logging support
public class SimpleAutomation {
    
    @Autowired
    private Action action;
    
    /**
     * Full version showing all the steps explicitly
     */
    public void clickButton() {
        // 1. Define what to look for
        StateImage buttonImage = new StateImage.Builder()
                .setName("submit-button")
                .addPatterns("submit-button")
                .build();
        
        // 2. Configure how to find it
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9)
                .build();
        
        // 3. Add the button to the objects to find
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(buttonImage)
                .build();
        
        // 4. Find the button
        ActionResult findResult = action.perform(findOptions, objects);
        
        // 5. Click the found button
        if (findResult.isSuccess()) {
            ClickOptions clickOptions = new ClickOptions.Builder()
                    .setClickType(ClickOptions.ClickType.LEFT)
                    .build();
            
            ObjectCollection clickObjects = new ObjectCollection.Builder()
                    .withMatches(findResult.getMatchList())
                    .build();
            
            ActionResult clickResult = action.perform(clickOptions, clickObjects);
            log.info("Click result: {}", clickResult.isSuccess());
        }
    }
    
    /**
     * Simplified version using convenience methods
     */
    public void clickButtonSimplified() {
        // 1. Define the button image
        StateImage buttonImage = new StateImage.Builder()
                .setName("submit-button")
                .addPatterns("submit-button")
                .build();
        
        // 2. Find and click in one line
        action.click(buttonImage);
        
        // That's it! 🎉
    }
    
    /**
     * Demonstrates various convenience methods
     */
    public void demonstrateConvenienceMethods() {
        StateImage submitButton = new StateImage.Builder()
                .addPatterns("submit-button")
                .build();
        
        // Find an image on screen
        ActionResult found = action.find(submitButton);
        log.info("Found submit button: {}", found.isSuccess());
        
        // Click an image (finds it first automatically)
        action.click(submitButton);
        
        // Type text (with automatic focus)
        action.type(new ObjectCollection.Builder()
                .withStrings("Hello World")
                .build());
        
        // Chain find and click with fluent API
        PatternFindOptions findAndClick = new PatternFindOptions.Builder()
                .then(new ClickOptions.Builder().build())
                .build();
        
        action.perform(findAndClick, new ObjectCollection.Builder()
                .withImages(submitButton)
                .build());
    }
    
    /**
     * Production-ready example with proper error handling and logging
     */
    public boolean submitForm(String username, String password) {
        log.info("Starting form submission for user: {}", username);
        
        // Define all UI elements
        StateImage usernameField = new StateImage.Builder()
                .setName("username-field")
                .addPatterns("form/username-field")
                .build();
        
        StateImage passwordField = new StateImage.Builder()
                .setName("password-field")
                .addPatterns("form/password-field")
                .build();
        
        StateImage submitButton = new StateImage.Builder()
                .setName("submit-button")
                .addPatterns("form/submit-button")
                .build();
        
        try {
            // Click username field and type
            ActionResult userResult = action.click(usernameField);
            if (!userResult.isSuccess()) {
                log.error("Failed to find username field");
                return false;
            }
            
            action.type(new ObjectCollection.Builder()
                    .withStrings(username)
                    .build());
            
            // Click password field and type
            ActionResult passResult = action.click(passwordField);
            if (!passResult.isSuccess()) {
                log.error("Failed to find password field");
                return false;
            }
            
            action.type(new ObjectCollection.Builder()
                    .withStrings(password)
                    .build());
            
            // Submit the form
            ActionResult submitResult = action.click(submitButton);
            if (submitResult.isSuccess()) {
                log.info("Form submitted successfully");
                return true;
            } else {
                log.error("Failed to click submit button");
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error during form submission", e);
            return false;
        }
    }
}