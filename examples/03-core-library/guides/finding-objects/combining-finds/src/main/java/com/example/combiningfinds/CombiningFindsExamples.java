package com.example.combiningfinds;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionChainBuilder;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demonstrates combining multiple find operations using ActionChainOptions
 * Based on documentation from /docs/03-core-library/guides/finding-objects/combining-finds.md
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CombiningFindsExamples {
    
    private final Action action;
    
    /**
     * Demonstrates NESTED strategy - searching within previous results
     */
    public void demonstrateNestedStrategy() {
        log.info("=== NESTED Strategy Example ===");
        log.info("Finding yellow elements WITHIN bar patterns");
        
        // First, find all bars
        PatternFindOptions findBars = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.7)  // Lower to catch all bars
                .build();
        
        // Then find yellow color within those bars
        ColorFindOptions findYellow = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.MU)
                .setDiameter(10)
                .setSimilarity(0.9)
                .build();
        
        // Create nested chain
        ActionChainOptions nestedFind = new ActionChainOptions.Builder(findBars)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .then(findYellow)
                .build();
        
        // Create test images
        StateImage barPattern = new StateImage.Builder()
                .addPatterns("patterns/bar-pattern")
                .setName("BarPattern")
                .build();
        
        StateImage yellowColor = new StateImage.Builder()
                .addPatterns("colors/yellow-sample")
                .setName("YellowColor")
                .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(barPattern, yellowColor)
                .build();
        
        // Execute nested find
        ActionResult yellowRegions = action.perform(nestedFind, objects);
        log.info("Found {} yellow regions inside bar patterns", 
                yellowRegions.getMatchList().size());
    }
    
    /**
     * Demonstrates CONFIRM strategy - validating results with second search
     */
    public void demonstrateConfirmStrategy() {
        log.info("=== CONFIRM Strategy Example ===");
        log.info("Finding buttons and confirming they have the right color");
        
        // Find all button patterns
        PatternFindOptions findButtons = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.8)
                .build();
        
        // Confirm they have the expected color
        ColorFindOptions confirmColor = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.MU)
                .setSimilarity(0.85)
                .build();
        
        // Create confirmed chain
        ActionChainOptions confirmedFind = new ActionChainOptions.Builder(findButtons)
                .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                .then(confirmColor)
                .build();
        
        StateImage buttonPattern = new StateImage.Builder()
                .addPatterns("ui/button-shape")
                .setName("ButtonShape")
                .build();
        
        StateImage expectedColor = new StateImage.Builder()
                .addPatterns("ui/button-color")
                .setName("ExpectedButtonColor")
                .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(buttonPattern, expectedColor)
                .build();
        
        // Execute confirmed find
        ActionResult confirmedButtons = action.perform(confirmedFind, objects);
        log.info("Found {} buttons with correct color (confirmed)", 
                confirmedButtons.getMatchList().size());
    }
    
    /**
     * Complex example: Find specific colored text within UI panels
     */
    public void findColoredTextInPanels() {
        log.info("=== Complex Example: Colored Text in Panels ===");
        
        // Step 1: Find all UI panels
        PatternFindOptions findPanels = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.75)
                .build();
        
        // Step 2: Within panels, find text regions
        PatternFindOptions findTextRegions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.7)
                .build();
        
        // Step 3: Confirm text is red
        ColorFindOptions confirmRedText = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.MU)
                .setSimilarity(0.9)
                .build();
        
        // Build the chain: panels -> text regions -> red color
        ActionChainOptions findRedTextInPanels = ActionChainBuilder
                .of(findPanels)
                .then(findTextRegions)
                .then(confirmRedText)
                .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .build();
        
        StateImage panelImage = new StateImage.Builder()
                .addPatterns("ui/panel-border")
                .build();
        
        StateImage textPattern = new StateImage.Builder()
                .addPatterns("ui/text-region")
                .build();
        
        StateImage redColor = new StateImage.Builder()
                .addPatterns("colors/red-text")
                .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(panelImage, textPattern, redColor)
                .build();
        
        ActionResult result = action.perform(findRedTextInPanels, objects);
        log.info("Found {} red text regions within panels", 
                result.getMatchList().size());
    }
    
    /**
     * Example: Interactive element detection (button with hover state)
     */
    public void detectInteractiveElements() {
        log.info("=== Interactive Element Detection ===");
        
        // Find button shape
        PatternFindOptions findButtonShape = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.8)
                .build();
        
        // Confirm it has hover highlight color
        ColorFindOptions confirmHoverColor = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.KMEANS)
                .setKmeans(2)  // Button color + highlight color
                .setSimilarity(0.85)
                .build();
        
        // Click if confirmed
        ClickOptions clickButton = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();
        
        // Chain: find shape -> confirm hover state -> click
        ActionChainOptions interactiveChain = new ActionChainOptions.Builder(findButtonShape)
                .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                .then(confirmHoverColor)
                .then(clickButton)
                .build();
        
        StateImage buttonShape = new StateImage.Builder()
                .addPatterns("interactive/button-shape")
                .build();
        
        StateImage hoverColor = new StateImage.Builder()
                .addPatterns("interactive/hover-highlight")
                .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(buttonShape, hoverColor)
                .build();
        
        ActionResult result = action.perform(interactiveChain, objects);
        if (result.isSuccess()) {
            log.info("Successfully clicked interactive button");
        }
    }
    
    /**
     * Comparison: Sequential vs Nested vs Confirmed
     */
    public void compareStrategies() {
        log.info("=== Strategy Comparison ===");
        
        PatternFindOptions findShape = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.75)
                .build();
        
        ColorFindOptions findColor = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.MU)
                .setSimilarity(0.85)
                .build();
        
        StateImage shapeImage = new StateImage.Builder()
                .addPatterns("comparison/shape")
                .build();
        
        StateImage colorImage = new StateImage.Builder()
                .addPatterns("comparison/color")
                .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(shapeImage, colorImage)
                .build();
        
        // 1. Sequential (default) - independent searches
        log.info("\n1. SEQUENTIAL Strategy:");
        ActionChainOptions sequential = new ActionChainOptions.Builder(findShape)
                .then(findColor)
                .build();
        
        ActionResult seqResult = action.perform(sequential, objects);
        log.info("Sequential: {} total matches", seqResult.getMatchList().size());
        
        // 2. Nested - search within results
        log.info("\n2. NESTED Strategy:");
        ActionChainOptions nested = new ActionChainOptions.Builder(findShape)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .then(findColor)
                .build();
        
        ActionResult nestedResult = action.perform(nested, objects);
        log.info("Nested: {} matches (color within shape)", nestedResult.getMatchList().size());
        
        // 3. Confirm - validate results
        log.info("\n3. CONFIRM Strategy:");
        ActionChainOptions confirm = new ActionChainOptions.Builder(findShape)
                .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                .then(findColor)
                .build();
        
        ActionResult confirmResult = action.perform(confirm, objects);
        log.info("Confirm: {} matches (shapes with correct color)", confirmResult.getMatchList().size());
    }
    
    /**
     * Best practice: Multi-stage filtering
     */
    public void multiStageFiltering() {
        log.info("=== Multi-Stage Filtering Example ===");
        
        // Stage 1: Find all candidate regions (broad search)
        PatternFindOptions broadSearch = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.6)  // Low threshold
                .build();
        
        // Stage 2: Filter by shape (medium precision)
        PatternFindOptions shapeFilter = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.8)  // Higher threshold
                .build();
        
        // Stage 3: Confirm by color (high precision)
        ColorFindOptions colorConfirm = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.MU)
                .setSimilarity(0.9)  // Strict threshold
                .build();
        
        // Build multi-stage chain
        ActionChainOptions multiStage = ActionChainBuilder
                .of(broadSearch)
                .then(shapeFilter)
                .then(colorConfirm)
                .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .pauseBeforeBegin(0.5)
                .pauseAfterEnd(0.5)
                .build();
        
        StateImage candidate = new StateImage.Builder()
                .addPatterns("filter/candidate-region")
                .build();
        
        StateImage targetShape = new StateImage.Builder()
                .addPatterns("filter/target-shape")
                .build();
        
        StateImage targetColor = new StateImage.Builder()
                .addPatterns("filter/target-color")
                .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(candidate, targetShape, targetColor)
                .build();
        
        ActionResult result = action.perform(multiStage, objects);
        log.info("Multi-stage filtering: {} final matches from broad->narrow search", 
                result.getMatchList().size());
    }
}