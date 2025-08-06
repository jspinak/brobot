package com.example.illustration.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates quality-based filtering for illustrations.
 * Shows how to focus on meaningful, high-quality visualizations in Brobot v1.1.0.
 */
@Component
@Slf4j
public class QualityFilteringExample {
    
    private final Action action;
    private final IllustrationController illustrationController;
    
    // Example objects with varying quality expectations
    private StateImage highQualityButton;
    private StateImage lowQualityText;
    private StateImage dynamicElement;
    private List<StateImage> criticalElements;
    
    public QualityFilteringExample(Action action, IllustrationController illustrationController) {
        this.action = action;
        this.illustrationController = illustrationController;
        initializeObjects();
    }
    
    private void initializeObjects() {
        highQualityButton = new StateImage.Builder()
            .setName("HighQualityButton")
            .addPatterns("ui-elements/high-quality-button")
            .build();
            
        lowQualityText = new StateImage.Builder()
            .setName("LowQualityText")
            .addPatterns("ui-elements/low-quality-text")
            .build();
            
        dynamicElement = new StateImage.Builder()
            .setName("DynamicElement")
            .addPatterns("ui-elements/dynamic-element")
            .build();
            
        // Critical elements that should always be illustrated
        criticalElements = new ArrayList<>();
        criticalElements.add(new StateImage.Builder()
            .setName("SaveButton")
            .addPatterns("ui-elements/save-button")
            .build());
        criticalElements.add(new StateImage.Builder()
            .setName("DeleteButton")
            .addPatterns("ui-elements/delete-button")
            .build());
        criticalElements.add(new StateImage.Builder()
            .setName("ConfirmDialog")
            .addPatterns("ui-elements/confirm-dialog")
            .build());
    }
    
    /**
     * Demonstrate quality threshold filtering
     */
    public void demonstrateQualityThresholds() {
        log.info("=== Quality Threshold Filtering ===");
        
        // Test elements with different expected quality scores
        StateImage[] testElements = {highQualityButton, lowQualityText, dynamicElement};
        String[] descriptions = {"High Quality", "Low Quality", "Dynamic"};
        
        for (int i = 0; i < testElements.length; i++) {
            log.info("Testing {} element", descriptions[i]);
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(testElements[i])
                .build();
                
            // Use different similarity thresholds
            double[] thresholds = {0.95, 0.80, 0.60};
            
            for (double threshold : thresholds) {
                PatternFindOptions findOptions = new PatternFindOptions.Builder()
                    .setSimilarity(threshold)
                    .build();
                    
                ActionResult result = action.perform(findOptions, collection);
                
                if (result.isSuccess()) {
                    // Check match quality
                    result.getBestMatch().ifPresent(match -> {
                        double matchScore = match.getScore();
                        log.info("Found with threshold {}: score = {}", threshold, matchScore);
                        
                        // Only illustrate high-quality matches
                        if (matchScore >= 0.85 && illustrationController.okToIllustrate(findOptions, collection)) {
                            log.info("High-quality match - illustrating");
                            illustrationController.illustrateWhenAllowed(
                                result,
                                new ArrayList<>(),
                                findOptions,
                                collection);
                        } else {
                            log.debug("Low-quality match - skipping illustration");
                        }
                    });
                } else {
                    log.debug("Not found with threshold {}", threshold);
                }
            }
            
            log.info("");
        }
    }
    
    /**
     * Demonstrate critical element prioritization
     */
    public void demonstrateCriticalElements() {
        log.info("=== Critical Element Prioritization ===");
        
        // Always illustrate critical elements regardless of quality
        for (StateImage critical : criticalElements) {
            log.info("Testing critical element: {}", critical.getName());
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(critical)
                .build();
                
            // Use lower threshold for critical elements
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.7)  // Lower threshold for critical elements
                .build();
                
            ActionResult result = action.perform(findOptions, collection);
            
            if (result.isSuccess()) {
                // Always illustrate critical elements
                if (illustrationController.okToIllustrate(findOptions, collection)) {
                    log.info("Critical element found - always illustrating");
                    illustrationController.illustrateWhenAllowed(
                        result,
                        new ArrayList<>(),
                        findOptions,
                        collection);
                }
                
                // Perform critical action
                ClickOptions clickOptions = new ClickOptions.Builder()
                    .setPauseBeforeBegin(0.5)  // Extra caution for critical actions
                    .build();
                    
                ActionResult clickResult = action.perform(clickOptions, collection);
                
                // Illustrate the click result too
                if (illustrationController.okToIllustrate(clickOptions, collection)) {
                    illustrationController.illustrateWhenAllowed(
                        clickResult,
                        new ArrayList<>(),
                        clickOptions,
                        collection);
                }
            } else {
                log.error("Critical element {} not found!", critical.getName());
                // Still try to illustrate the failed search for debugging
                if (illustrationController.okToIllustrate(findOptions, collection)) {
                    illustrationController.illustrateWhenAllowed(
                        result,
                        new ArrayList<>(),
                        findOptions,
                        collection);
                }
            }
        }
    }
    
    /**
     * Demonstrate context-aware quality filtering
     */
    public void demonstrateContextAwareFiltering() {
        log.info("=== Context-Aware Quality Filtering ===");
        
        // Create elements that appear in different contexts
        StateImage contextualElement = new StateImage.Builder()
            .setName("ContextualElement")
            .addPatterns("ui-elements/contextual-element")
            .build();
            
        // Simulate different contexts
        String[] contexts = {"Normal Operation", "Error State", "Loading State"};
        
        for (String context : contexts) {
            log.info("Testing in context: {}", context);
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(contextualElement)
                .build();
                
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.8)
                .build();
                
            ActionResult result = action.perform(findOptions, collection);
            
            if (result.isSuccess()) {
                boolean shouldIllustrate = false;
                
                // Context-based decision
                switch (context) {
                    case "Normal Operation":
                        // Only illustrate if match quality is very high
                        shouldIllustrate = result.getBestMatch()
                            .map(match -> match.getScore() > 0.9)
                            .orElse(false);
                        break;
                        
                    case "Error State":
                        // Always illustrate errors
                        shouldIllustrate = true;
                        break;
                        
                    case "Loading State":
                        // Rarely illustrate loading states
                        shouldIllustrate = Math.random() < 0.1;  // 10% chance
                        break;
                }
                
                if (shouldIllustrate && illustrationController.okToIllustrate(findOptions, collection)) {
                    log.info("Context '{}' requires illustration", context);
                    illustrationController.illustrateWhenAllowed(
                        result,
                        new ArrayList<>(),
                        findOptions,
                        collection);
                } else {
                    log.debug("Context '{}' does not require illustration", context);
                }
            }
        }
    }
    
    /**
     * Demonstrate multi-match quality analysis
     */
    public void demonstrateMultiMatchAnalysis() {
        log.info("=== Multi-Match Quality Analysis ===");
        
        // Search for multiple instances of an element
        StateImage repeatingElement = new StateImage.Builder()
            .setName("RepeatingElement")
            .addPatterns("ui-elements/repeating-element")
            .build();
            
        ObjectCollection collection = new ObjectCollection.Builder()
            .withImages(repeatingElement)
            .build();
            
        // Find all instances
        PatternFindOptions findAllOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.7)
            // Find up to 10 instances - this method doesn't exist in v1.1.0
            // PatternFindOptions uses Strategy enum instead
            .build();
            
        ActionResult result = action.perform(findAllOptions, collection);
        
        if (result.isSuccess()) {
            List<Match> allMatches = result.getMatchList();
            log.info("Found {} instances", allMatches.size());
            
            // Analyze quality distribution
            double totalScore = 0;
            double maxScore = 0;
            double minScore = 1.0;
            
            for (Match match : allMatches) {
                double score = match.getScore();
                totalScore += score;
                maxScore = Math.max(maxScore, score);
                minScore = Math.min(minScore, score);
            }
            
            double avgScore = totalScore / allMatches.size();
            
            log.info("Quality statistics:");
            log.info("- Average score: {}", String.format("%.3f", avgScore));
            log.info("- Max score: {}", String.format("%.3f", maxScore));
            log.info("- Min score: {}", String.format("%.3f", minScore));
            
            // Only illustrate if quality variance is significant
            double variance = maxScore - minScore;
            if (variance > 0.2 && illustrationController.okToIllustrate(findAllOptions, collection)) {
                log.info("Significant quality variance detected - illustrating");
                illustrationController.illustrateWhenAllowed(
                    result,
                    new ArrayList<>(),
                    findAllOptions,
                    collection);
            }
        }
    }
    
    /**
     * Run all quality filtering examples
     */
    public void runExample() {
        log.info("=== Quality Filtering Examples ===");
        log.info("Demonstrating intelligent quality-based illustration filtering");
        
        // Quality threshold filtering
        demonstrateQualityThresholds();
        log.info("");
        
        // Critical element prioritization
        demonstrateCriticalElements();
        log.info("");
        
        // Context-aware filtering
        demonstrateContextAwareFiltering();
        log.info("");
        
        // Multi-match analysis
        demonstrateMultiMatchAnalysis();
        
        log.info("Quality filtering examples completed");
        log.info("Key concepts demonstrated:");
        log.info("✓ Quality thresholds focus on high-confidence matches");
        log.info("✓ Critical elements are always illustrated");
        log.info("✓ Context influences illustration decisions");
        log.info("✓ Multi-match analysis reveals quality patterns");
    }
}