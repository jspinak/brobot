package com.example.chaining.demos;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demonstrates basic action chaining patterns with automatic logging.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BasicChainingDemo {
    
    private final Action action;
    
    public void runDemos() {
        log.info("\n=== Basic Action Chaining Demos ===");
        
        // Demo 1: Simple sequential chain
        demonstrateSimpleChain();
        
        // Demo 2: Search workflow chain
        demonstrateSearchWorkflow();
        
        // Demo 3: Form filling chain
        demonstrateFormChain();
        
        // Demo 4: Navigation chain
        demonstrateNavigationChain();
    }
    
    /**
     * Demo 1: Simple sequential chain
     * Shows basic find -> click -> type sequence
     */
    private void demonstrateSimpleChain() {
        log.info("\n--- Demo 1: Simple Sequential Chain ---");
        
        // Create a simple chain: find input -> click -> type
        PatternFindOptions findInput = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for input field...")
            .withSuccessLog("Found input field")
            .withFailureLog("Input field not found")
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Clicking to focus input...")
                .withSuccessLog("Input field focused")
                .build())
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Typing text...")
                .withSuccessLog("Text entered successfully")
                .withAfterActionLog("Input sequence completed in {duration}ms")
                .build())
            .build();
            
        StateImage inputField = new StateImage.Builder()
            .setName("input-field")
            .addPattern("images/input-field.png")
            .build();
            
        // Create ObjectCollection with both the image and the text
        ObjectCollection targets = new ObjectCollection.Builder()
            .withImages(inputField)
            .withStrings("Hello, Brobot!")
            .build();
            
        ActionResult result = action.perform(findInput, targets);
        
        if (result.isSuccess()) {
            log.info("✓ Simple chain executed successfully");
        }
    }
    
    /**
     * Demo 2: Search workflow chain
     * Shows a complete search interaction flow
     */
    private void demonstrateSearchWorkflow() {
        log.info("\n--- Demo 2: Search Workflow Chain ---");
        
        // Build search workflow chain
        ActionChainOptions searchChain = new ActionChainOptions.Builder(
            new PatternFindOptions.Builder()
                .withBeforeActionLog("Opening search interface...")
                .withSuccessLog("Search interface ready")
                .build()
        )
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Clicking search box...")
            .withSuccessLog("Search box activated")
            .build())
        .then(new TypeOptions.Builder()
            .setTypeDelay(0.05)
            .withBeforeActionLog("Entering search query...")
            .withSuccessLog("Query entered")
            .build())
        .then(new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for search button...")
            .withSuccessLog("Search button found")
            .build())
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Initiating search...")
            .withSuccessLog("Search submitted")
            .withAfterActionLog("Search workflow completed in {duration}ms")
            .setPauseAfterEnd(2.0) // Wait for results
            .build())
        .build();
        
        // Define the UI elements
        StateImage searchBox = new StateImage.Builder()
            .setName("search-box")
            .addPattern("images/search-box.png")
            .build();
            
        StateImage searchButton = new StateImage.Builder()
            .setName("search-button")
            .addPattern("images/search-button.png")
            .build();
            
        // Execute the chain with all necessary data
        // In a real scenario, this would be:
        // ActionResult result = action.perform(searchChain, searchBox, "example query", searchButton);
        
        log.info("Search workflow chain created - would search for 'example query'");
    }
    
    /**
     * Demo 3: Form filling chain
     * Shows chaining for form automation
     */
    private void demonstrateFormChain() {
        log.info("\n--- Demo 3: Form Filling Chain ---");
        
        // Create form field definitions
        StateImage firstNameField = new StateImage.Builder()
            .setName("first-name")
            .addPattern("images/first-name-field.png")
            .build();
            
        StateImage lastNameField = new StateImage.Builder()
            .setName("last-name")
            .addPattern("images/last-name-field.png")
            .build();
            
        StateImage emailField = new StateImage.Builder()
            .setName("email")
            .addPattern("images/email-field.png")
            .build();
            
        StateImage submitButton = new StateImage.Builder()
            .setName("submit")
            .addPattern("images/submit-button.png")
            .build();
        
        // Build the form filling chain
        ActionConfig formChain = new PatternFindOptions.Builder()
            .withBeforeActionLog("Starting form fill process...")
            .withSuccessLog("First name field located")
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Focusing first name field...")
                .build())
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Entering first name...")
                .withSuccessLog("First name entered")
                .build())
            // Tab to next field
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Tabbing to last name field...")
                .build())
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Entering last name...")
                .withSuccessLog("Last name entered")
                .build())
            // Tab to email
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Tabbing to email field...")
                .build())
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Entering email address...")
                .withSuccessLog("Email entered")
                .build())
            // Find and click submit
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for submit button...")
                .withSuccessLog("Submit button found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Submitting form...")
                .withSuccessLog("Form submitted successfully")
                .withAfterActionLog("Form completion took {duration}ms")
                .build())
            .build();
        
        log.info("Form filling chain created - would fill: John Doe, john.doe@example.com");
    }
    
    /**
     * Demo 4: Navigation chain
     * Shows menu navigation with hover actions
     */
    private void demonstrateNavigationChain() {
        log.info("\n--- Demo 4: Navigation Chain ---");
        
        // Create a navigation chain with nested execution
        ActionChainOptions navChain = new ActionChainOptions.Builder(
            new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for menu button...")
                .withSuccessLog("Menu button found")
                .build()
        )
        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Opening menu...")
            .withSuccessLog("Menu opened")
            .setPauseAfterEnd(0.5) // Wait for menu animation
            .build())
        .then(new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for Settings option in menu...")
            .withSuccessLog("Settings option found")
            .build())
        .then(new MouseMoveOptions.Builder()
            .withBeforeActionLog("Hovering over Settings...")
            .withSuccessLog("Submenu displayed")
            .setPauseAfterEnd(0.3)
            .setMoveMouseDelay(0.5f) // Smooth hover movement
            .build())
        .then(new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for Profile in submenu...")
            .withSuccessLog("Profile option found")
            .build())
        .then(new ClickOptions.Builder()
            .withBeforeActionLog("Clicking Profile...")
            .withSuccessLog("Navigated to Profile page")
            .withAfterActionLog("Navigation completed in {duration}ms")
            .build())
        .build();
        
        log.info("Navigation chain created - would navigate: Menu -> Settings -> Profile");
        
        // Log execution strategy info
        log.info("Using NESTED strategy - each action searches within previous results");
    }
}