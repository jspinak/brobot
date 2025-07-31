package com.example.actionhierarchy;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.RepetitionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.composite.repeat.ClickUntilOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demonstrates various approaches to implementing complex actions in Brobot,
 * specifically showing different ways to implement "click until found" behavior.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ComplexActionExamples {
    
    private final Action action;
    
    /**
     * Method 1: Traditional Loop Approach
     * Using individual actions with retry logic
     */
    public boolean clickUntilFound(StateImage clickTarget, StateImage findTarget, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            // Click on target with pause after action
            ClickOptions click = new ClickOptions.Builder()
                    .setPauseAfterEnd(1.0)  // 1 second pause after click
                    .build();
            action.perform(click, new ObjectCollection.Builder()
                    .withImages(clickTarget).build());
            
            // Check if pattern appeared
            PatternFindOptions find = PatternFindOptions.forQuickSearch();
            ActionResult result = action.perform(find, new ObjectCollection.Builder()
                    .withImages(findTarget).build());
            
            if (result.isSuccess()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Method 2: Fluent API with Action Chaining
     * Using the fluent API to chain click and find operations
     */
    public boolean clickUntilFoundFluent(StateImage clickTarget, StateImage findTarget) {
        // For simple chaining, we can use then() to combine actions
        // Note: This performs click then find once, not repeatedly
        PatternFindOptions findFirst = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        
        ClickOptions clickAndCheck = new ClickOptions.Builder()
                .setPauseAfterEnd(1.0)  // Wait after click
                .then(new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .build())
                .build();
        
        // Execute the chained action
        ObjectCollection targets = new ObjectCollection.Builder()
                .withImages(clickTarget, findTarget)
                .build();
        
        // In practice, you would implement repetition logic yourself
        for (int i = 0; i < 10; i++) {
            ActionResult result = action.perform(clickAndCheck, targets);
            if (result.isSuccess()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Method 3: Using the Built-in ClickUntilOptions (Deprecated but Available)
     * Using Brobot's built-in ClickUntil composite action
     */
    public boolean clickUntilFoundBuiltIn(StateImage clickTarget, StateImage findTarget) {
        // Create ClickUntilOptions configured to click until objects appear
        ClickUntilOptions clickUntil = new ClickUntilOptions.Builder()
                .setCondition(ClickUntilOptions.Condition.OBJECTS_APPEAR)
                .setPauseBeforeBegin(0.5)
                .setPauseAfterEnd(1.0)
                .build();
        
        // Create ObjectCollections
        // If using 1 collection: clicks objects until they appear
        // If using 2 collections: clicks collection 1 until collection 2 appears
        ObjectCollection clickCollection = new ObjectCollection.Builder()
                .withImages(clickTarget)
                .build();
        ObjectCollection appearCollection = new ObjectCollection.Builder()
                .withImages(findTarget)
                .build();
        
        // Execute with two collections - click first until second appears
        ActionResult result = action.perform(clickUntil, clickCollection, appearCollection);
        return result.isSuccess();
    }
    
    /**
     * Method 4: Creating a Reusable Click-Until-Found Function
     * Creating a clean, reusable function that combines the best approaches
     */
    public boolean clickUntilFoundReusable(StateImage clickTarget, StateImage findTarget, 
                                   int maxAttempts, double pauseBetween) {
        // Simple reusable pattern
        for (int i = 0; i < maxAttempts; i++) {
            // First find and click the target
            ActionResult clickResult = action.perform(
                    new ClickOptions.Builder()
                            .setPauseAfterEnd(pauseBetween)
                            .build(),
                    new ObjectCollection.Builder()
                            .withImages(clickTarget)
                            .build());
            
            if (!clickResult.isSuccess()) {
                log.warn("Click target not found on attempt {}", i + 1);
                continue;
            }
            
            // Check if the find target appeared
            ActionResult findResult = action.perform(
                    new PatternFindOptions.Builder()
                            .setSearchDuration(0.5) // Quick check
                            .build(),
                    new ObjectCollection.Builder()
                            .withImages(findTarget)
                            .build());
            
            if (findResult.isSuccess()) {
                log.info("Target appeared after {} attempts", i + 1);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Demonstrates basic action usage
     */
    public void demonstrateBasicActions(StateImage targetImage) {
        log.info("=== Demonstrating Basic Actions ===");
        
        // Find Actions - Locate images, text, or patterns on screen
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.9)
                .build();
        
        ActionResult findResult = action.perform(findOptions, 
                new ObjectCollection.Builder().withImages(targetImage).build());
        log.info("Find action result: {}", findResult.isSuccess());
        
        // Click Actions - Single, double, or right clicks at specific locations
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        
        ActionResult clickResult = action.perform(clickOptions,
                new ObjectCollection.Builder().withImages(targetImage).build());
        log.info("Click action result: {}", clickResult.isSuccess());
        
        // Type Actions - Keyboard input and key combinations
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setTypeDelay(0.1)
                .setPauseBeforeBegin(0.5)
                .build();
        
        ActionResult typeResult = action.perform(typeOptions,
                new ObjectCollection.Builder()
                        .withStrings("Hello Brobot!")
                        .build());
        log.info("Type action result: {}", typeResult.isSuccess());
        
        // Move Actions - Mouse movements and hover operations
        MouseMoveOptions moveOptions = new MouseMoveOptions.Builder()
                .setPauseAfterEnd(1.0)
                .build();
        
        ActionResult moveResult = action.perform(moveOptions,
                new ObjectCollection.Builder().withImages(targetImage).build());
        log.info("Move action result: {}", moveResult.isSuccess());
    }
}