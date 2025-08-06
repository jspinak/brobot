package com.example.illustration.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Demonstrates performance optimization strategies for the illustration system.
 * Shows how to use IllustrationController efficiently in high-volume scenarios.
 */
@Component
@Slf4j
public class PerformanceOptimizationExample {
    
    private final Action action;
    private final IllustrationController illustrationController;
    private final Random random = new Random();
    
    // Sample UI elements
    private List<StateImage> dataItems;
    private StateImage searchBox;
    private StateImage resultArea;
    
    public PerformanceOptimizationExample(Action action, IllustrationController illustrationController) {
        this.action = action;
        this.illustrationController = illustrationController;
        initializeObjects();
    }
    
    private void initializeObjects() {
        dataItems = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            dataItems.add(new StateImage.Builder()
                .setName("DataItem" + i)
                .addPatterns("data/item-" + i)
                .build());
        }
        
        searchBox = new StateImage.Builder()
            .setName("SearchBox")
            .addPatterns("ui-elements/search-box")
            .build();
            
        resultArea = new StateImage.Builder()
            .setName("ResultArea")
            .addPatterns("ui-elements/result-area")
            .build();
    }
    
    /**
     * Demonstrate adaptive sampling for repetitive actions
     */
    public void demonstrateAdaptiveSampling() {
        log.info("=== Adaptive Sampling Example ===");
        
        // High-frequency mouse movements - most will not be illustrated
        log.info("Performing 100 mouse movements (only sampling some)...");
        
        for (int i = 0; i < 100; i++) {
            Region targetRegion = new Region(
                random.nextInt(1600) + 100,
                random.nextInt(800) + 100,
                50, 50
            );
            
            ObjectCollection moveTarget = new ObjectCollection.Builder()
                .withRegions(targetRegion)
                .build();
            
            ActionResult moveResult = action.perform(
                new MouseMoveOptions.Builder().build(),
                moveTarget
            );
            
            // IllustrationController will automatically sample based on configuration
            // Only illustrate every 10th movement for performance
            MouseMoveOptions moveOptions = new MouseMoveOptions.Builder().build();
            if (i % 10 == 0 && illustrationController.okToIllustrate(moveOptions, moveTarget)) {
                illustrationController.illustrateWhenAllowed(
                    moveResult,
                    new ArrayList<>(),
                    moveOptions,
                    moveTarget);
            }
        }
        
        log.info("Mouse movement sampling completed");
    }
    
    /**
     * Demonstrate batched operations for efficiency
     */
    public void demonstrateBatchedOperations() {
        log.info("=== Batched Operations Example ===");
        
        // Process data items in batches
        int batchSize = 5;
        
        for (int batch = 0; batch < dataItems.size() / batchSize; batch++) {
            log.info("Processing batch {} of {}", batch + 1, dataItems.size() / batchSize);
            
            boolean batchSuccess = true;
            List<ActionResult> batchResults = new ArrayList<>();
            
            // Process batch
            for (int i = batch * batchSize; i < (batch + 1) * batchSize && i < dataItems.size(); i++) {
                ObjectCollection itemCollection = new ObjectCollection.Builder()
                    .withImages(dataItems.get(i))
                    .build();
                    
                ActionResult result = action.perform(
                    new PatternFindOptions.Builder().build(),
                    itemCollection
                );
                
                batchResults.add(result);
                if (!result.isSuccess()) {
                    batchSuccess = false;
                }
            }
            
            // Only illustrate failed batches or every 3rd successful batch
            if (!batchSuccess || batch % 3 == 0) {
                for (int i = 0; i < batchResults.size(); i++) {
                    if (!batchResults.get(i).isSuccess() || batch % 3 == 0) {
                        ObjectCollection itemCollection = new ObjectCollection.Builder()
                            .withImages(dataItems.get(batch * batchSize + i))
                            .build();
                        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
                        if (illustrationController.okToIllustrate(findOptions, itemCollection)) {
                            illustrationController.illustrateWhenAllowed(
                                batchResults.get(i),
                                new ArrayList<>(),
                                findOptions,
                                itemCollection
                            );
                        }
                    }
                }
            }
            
            // Simulate processing delay
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        log.info("Batched processing completed");
    }
    
    /**
     * Demonstrate system-aware illustration decisions
     */
    public void demonstrateSystemAwareDecisions() {
        log.info("=== System-Aware Decisions Example ===");
        
        // Simulate different system load scenarios
        for (int scenario = 0; scenario < 3; scenario++) {
            String loadLevel = scenario == 0 ? "Low" : scenario == 1 ? "Medium" : "High";
            log.info("Simulating {} system load scenario", loadLevel);
            
            // Perform operations with load-aware illustration
            for (int i = 0; i < 10; i++) {
                ObjectCollection searchCollection = new ObjectCollection.Builder()
                    .withImages(searchBox)
                    .build();
                    
                ActionResult searchResult = action.perform(
                    new ClickOptions.Builder().build(),
                    searchCollection
                );
                
                // Reduce illustrations under high load
                boolean shouldIllustrate = false;
                switch (scenario) {
                    case 0: // Low load - illustrate most actions
                        shouldIllustrate = true;
                        break;
                    case 1: // Medium load - illustrate 50%
                        shouldIllustrate = i % 2 == 0;
                        break;
                    case 2: // High load - illustrate only failures
                        shouldIllustrate = !searchResult.isSuccess();
                        break;
                }
                
                ClickOptions clickOptions = new ClickOptions.Builder().build();
                if (shouldIllustrate && illustrationController.okToIllustrate(clickOptions, searchCollection)) {
                    illustrationController.illustrateWhenAllowed(
                        searchResult,
                        new ArrayList<>(),
                        clickOptions,
                        searchCollection);
                }
                
                // Simulate varying system load
                try {
                    Thread.sleep(50 * (scenario + 1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        log.info("System-aware illustration completed");
    }
    
    /**
     * Demonstrate smart caching and reuse
     */
    public void demonstrateSmartCaching() {
        log.info("=== Smart Caching Example ===");
        
        // Repeatedly check the same elements
        StateImage cachedElement = new StateImage.Builder()
            .setName("CachedElement")
            .addPatterns("ui-elements/cached-element")
            .build();
            
        ObjectCollection cachedCollection = new ObjectCollection.Builder()
            .withImages(cachedElement)
            .build();
        
        log.info("Performing repeated checks on the same element...");
        
        for (int i = 0; i < 20; i++) {
            ActionResult result = action.perform(
                new PatternFindOptions.Builder().build(),
                cachedCollection
            );
            
            // Only illustrate first occurrence, failures, and every 5th check
            boolean isFirstOccurrence = i == 0;
            boolean isFailed = !result.isSuccess();
            boolean isPeriodicCheck = i % 5 == 0;
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            if ((isFirstOccurrence || isFailed || isPeriodicCheck) && 
                illustrationController.okToIllustrate(findOptions, cachedCollection)) {
                log.debug("Illustrating check {} (first: {}, failed: {}, periodic: {})",
                    i, isFirstOccurrence, isFailed, isPeriodicCheck);
                illustrationController.illustrateWhenAllowed(
                    result,
                    new ArrayList<>(),
                    findOptions,
                    cachedCollection);
            }
        }
        
        log.info("Smart caching demonstration completed");
    }
    
    /**
     * Run all performance optimization examples
     */
    public void runExample() {
        log.info("=== Performance Optimization Examples ===");
        log.info("Note: Illustration settings are configured in application properties");
        
        // Adaptive sampling for high-frequency actions
        demonstrateAdaptiveSampling();
        log.info("");
        
        // Batched operations
        demonstrateBatchedOperations();
        log.info("");
        
        // System-aware decisions
        demonstrateSystemAwareDecisions();
        log.info("");
        
        // Smart caching
        demonstrateSmartCaching();
        
        log.info("Performance optimization examples completed");
        log.info("Key strategies demonstrated:");
        log.info("✓ Adaptive sampling reduces illustrations for repetitive actions");
        log.info("✓ Batching groups related operations for efficiency");
        log.info("✓ System-aware decisions adapt to current load");
        log.info("✓ Smart caching avoids redundant illustrations");
    }
}