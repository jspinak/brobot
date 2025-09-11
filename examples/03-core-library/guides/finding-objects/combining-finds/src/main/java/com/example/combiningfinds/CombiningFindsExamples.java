package com.example.combiningfinds;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionChainBuilder;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Demonstrates combining multiple find operations using ActionChainOptions Based on documentation
 * from /docs/03-core-library/guides/finding-objects/combining-finds.md
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CombiningFindsExamples {

    private final Action action;

    /**
     * Demonstrates NESTED strategy - searching within previous results From:
     * /docs/03-core-library/guides/finding-objects/combining-finds.md
     */
    public void demonstrateNestedStrategy() {
        log.info("=== NESTED Strategy Example ===");
        log.info("Finding yellow elements WITHIN bar patterns");

        // Find pattern matches first, then search for color within those matches
        PatternFindOptions patternFind =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.8)
                        .build();

        ColorFindOptions colorFind =
                new ColorFindOptions.Builder()
                        .setColorStrategy(ColorFindOptions.Color.MU) // Use mean color statistics
                        .setDiameter(5)
                        .setSimilarity(0.9)
                        .build();

        ActionChainOptions nestedChain =
                new ActionChainOptions.Builder(patternFind)
                        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                        .then(colorFind)
                        .build();

        // Create test images
        StateImage barImage =
                new StateImage.Builder().addPatterns("bar-pattern.png").setName("BarImage").build();

        StateImage yellowColorSample =
                new StateImage.Builder()
                        .addPatterns("yellow-sample.png")
                        .setName("YellowColorSample")
                        .build();

        ObjectCollection objectCollection =
                new ObjectCollection.Builder().withImages(barImage, yellowColorSample).build();

        // Execute the chain
        ActionResult result = action.perform(nestedChain, objectCollection);
        log.info("Found {} yellow regions inside bar patterns", result.getMatchList().size());
    }

    /**
     * Demonstrates CONFIRM strategy - validating results with second search From:
     * /docs/03-core-library/guides/finding-objects/combining-finds.md
     */
    public void demonstrateConfirmStrategy() {
        log.info("=== CONFIRM Strategy Example ===");
        log.info("Finding patterns and confirming with color");

        // Find pattern matches and confirm with color
        PatternFindOptions patternFind =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.8)
                        .build();

        ColorFindOptions colorConfirm =
                new ColorFindOptions.Builder()
                        .setColorStrategy(ColorFindOptions.Color.MU)
                        .setDiameter(5)
                        .setSimilarity(0.85)
                        .build();

        ActionChainOptions confirmedChain =
                new ActionChainOptions.Builder(patternFind)
                        .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                        .then(colorConfirm)
                        .build();

        StateImage barImage =
                new StateImage.Builder().addPatterns("bar-pattern.png").setName("BarImage").build();

        StateImage yellowColorSample =
                new StateImage.Builder()
                        .addPatterns("yellow-sample.png")
                        .setName("YellowColorSample")
                        .build();

        ObjectCollection objectCollection =
                new ObjectCollection.Builder().withImages(barImage, yellowColorSample).build();

        ActionResult result = action.perform(confirmedChain, objectCollection);
        log.info("Found {} confirmed patterns with correct color", result.getMatchList().size());
    }

    /** Complex example: Find specific colored text within UI panels */
    public void findColoredTextInPanels() {
        log.info("=== Complex Example: Colored Text in Panels ===");

        // Step 1: Find all UI panels
        PatternFindOptions findPanels =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.75)
                        .build();

        // Step 2: Within panels, find text regions
        PatternFindOptions findTextRegions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.7)
                        .build();

        // Step 3: Confirm text is red
        ColorFindOptions confirmRedText =
                new ColorFindOptions.Builder()
                        .setColorStrategy(ColorFindOptions.Color.MU)
                        .setSimilarity(0.9)
                        .build();

        // Build the chain: panels -> text regions -> red color
        ActionChainOptions findRedTextInPanels =
                ActionChainBuilder.of(findPanels)
                        .then(findTextRegions)
                        .then(confirmRedText)
                        .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                        .build();

        StateImage panelImage = new StateImage.Builder().addPatterns("ui/panel-border").build();

        StateImage textPattern = new StateImage.Builder().addPatterns("ui/text-region").build();

        StateImage redColor = new StateImage.Builder().addPatterns("colors/red-text").build();

        ObjectCollection objects =
                new ObjectCollection.Builder()
                        .withImages(panelImage, textPattern, redColor)
                        .build();

        ActionResult result = action.perform(findRedTextInPanels, objects);
        log.info("Found {} red text regions within panels", result.getMatchList().size());
    }

    /** Example: Interactive element detection (button with hover state) */
    public void detectInteractiveElements() {
        log.info("=== Interactive Element Detection ===");

        // Find button shape
        PatternFindOptions findButtonShape =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.8)
                        .build();

        // Confirm it has hover highlight color
        ColorFindOptions confirmHoverColor =
                new ColorFindOptions.Builder()
                        .setColorStrategy(ColorFindOptions.Color.KMEANS)
                        .setKmeans(2) // Button color + highlight color
                        .setSimilarity(0.85)
                        .build();

        // Click if confirmed
        ClickOptions clickButton = new ClickOptions.Builder().setNumberOfClicks(1).build();

        // Chain: find shape -> confirm hover state -> click
        ActionChainOptions interactiveChain =
                new ActionChainOptions.Builder(findButtonShape)
                        .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                        .then(confirmHoverColor)
                        .then(clickButton)
                        .build();

        StateImage buttonShape =
                new StateImage.Builder().addPatterns("interactive/button-shape").build();

        StateImage hoverColor =
                new StateImage.Builder().addPatterns("interactive/hover-highlight").build();

        ObjectCollection objects =
                new ObjectCollection.Builder().withImages(buttonShape, hoverColor).build();

        ActionResult result = action.perform(interactiveChain, objects);
        if (result.isSuccess()) {
            log.info("Successfully clicked interactive button");
        }
    }

    /** Comparison: Sequential vs Nested vs Confirmed */
    public void compareStrategies() {
        log.info("=== Strategy Comparison ===");

        PatternFindOptions findShape =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.75)
                        .build();

        ColorFindOptions findColor =
                new ColorFindOptions.Builder()
                        .setColorStrategy(ColorFindOptions.Color.MU)
                        .setSimilarity(0.85)
                        .build();

        StateImage shapeImage = new StateImage.Builder().addPatterns("comparison/shape").build();

        StateImage colorImage = new StateImage.Builder().addPatterns("comparison/color").build();

        ObjectCollection objects =
                new ObjectCollection.Builder().withImages(shapeImage, colorImage).build();

        // 1. Sequential (default) - independent searches
        log.info("\n1. SEQUENTIAL Strategy:");
        ActionChainOptions sequential =
                new ActionChainOptions.Builder(findShape).then(findColor).build();

        ActionResult seqResult = action.perform(sequential, objects);
        log.info("Sequential: {} total matches", seqResult.getMatchList().size());

        // 2. Nested - search within results
        log.info("\n2. NESTED Strategy:");
        ActionChainOptions nested =
                new ActionChainOptions.Builder(findShape)
                        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                        .then(findColor)
                        .build();

        ActionResult nestedResult = action.perform(nested, objects);
        log.info("Nested: {} matches (color within shape)", nestedResult.getMatchList().size());

        // 3. Confirm - validate results
        log.info("\n3. CONFIRM Strategy:");
        ActionChainOptions confirm =
                new ActionChainOptions.Builder(findShape)
                        .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                        .then(findColor)
                        .build();

        ActionResult confirmResult = action.perform(confirm, objects);
        log.info(
                "Confirm: {} matches (shapes with correct color)",
                confirmResult.getMatchList().size());
    }

    /** Best practice: Multi-stage filtering */
    public void multiStageFiltering() {
        log.info("=== Multi-Stage Filtering Example ===");

        // Stage 1: Find all candidate regions (broad search)
        PatternFindOptions broadSearch =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.6) // Low threshold
                        .build();

        // Stage 2: Filter by shape (medium precision)
        PatternFindOptions shapeFilter =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.8) // Higher threshold
                        .build();

        // Stage 3: Confirm by color (high precision)
        ColorFindOptions colorConfirm =
                new ColorFindOptions.Builder()
                        .setColorStrategy(ColorFindOptions.Color.MU)
                        .setSimilarity(0.9) // Strict threshold
                        .build();

        // Build multi-stage chain
        ActionChainOptions multiStage =
                ActionChainBuilder.of(broadSearch)
                        .then(shapeFilter)
                        .then(colorConfirm)
                        .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                        .pauseBeforeBegin(0.5)
                        .pauseAfterEnd(0.5)
                        .build();

        StateImage candidate =
                new StateImage.Builder().addPatterns("filter/candidate-region").build();

        StateImage targetShape =
                new StateImage.Builder().addPatterns("filter/target-shape").build();

        StateImage targetColor =
                new StateImage.Builder().addPatterns("filter/target-color").build();

        ObjectCollection objects =
                new ObjectCollection.Builder()
                        .withImages(candidate, targetShape, targetColor)
                        .build();

        ActionResult result = action.perform(multiStage, objects);
        log.info(
                "Multi-stage filtering: {} final matches from broad->narrow search",
                result.getMatchList().size());
    }

    /**
     * Finding Yellow Health Bars - Example from documentation From:
     * /docs/03-core-library/guides/finding-objects/combining-finds.md
     */
    public void findYellowHealthBars() {
        log.info("=== Finding Yellow Health Bars ===");

        // First find all bar-shaped patterns
        PatternFindOptions barPatterns =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.7) // Lower similarity to catch all bars
                        .build();

        // Then filter for yellow color
        ColorFindOptions yellowFilter =
                new ColorFindOptions.Builder()
                        .setColorStrategy(ColorFindOptions.Color.MU)
                        .setDiameter(10) // Larger diameter for solid color areas
                        .setSimilarity(0.9) // High similarity for color matching
                        .build();

        // Create nested chain to find yellow bars
        ActionChainOptions findYellowBars =
                new ActionChainOptions.Builder(barPatterns)
                        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                        .then(yellowFilter)
                        .setPauseAfterEnd(0.5) // Add pause after finding
                        .build();

        // Execute
        StateImage barImage =
                new StateImage.Builder()
                        .setName("health_bar")
                        .addPatterns("bar_pattern.png")
                        .build();

        ObjectCollection objects = new ObjectCollection.Builder().withImages(barImage).build();

        ActionResult yellowBars = action.perform(findYellowBars, objects);
        log.info("Found {} yellow health bars", yellowBars.getMatchList().size());
    }

    /**
     * Confirming UI Elements - Example from documentation From:
     * /docs/03-core-library/guides/finding-objects/combining-finds.md
     */
    public void confirmUIElements() {
        log.info("=== Confirming UI Elements ===");

        // Find buttons by pattern, confirm by color to reduce false positives
        PatternFindOptions buttonPattern =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.8)
                        .build();

        ColorFindOptions buttonColor =
                new ColorFindOptions.Builder()
                        .setColorStrategy(ColorFindOptions.Color.CLASSIFICATION)
                        .setSimilarity(0.85)
                        .build();

        ActionChainOptions confirmButtons =
                new ActionChainOptions.Builder(buttonPattern)
                        .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                        .then(buttonColor)
                        .build();

        StateImage buttonImage =
                new StateImage.Builder()
                        .setName("button_pattern")
                        .addPatterns("button_pattern.png")
                        .build();

        StateImage colorSample =
                new StateImage.Builder()
                        .setName("button_color")
                        .addPatterns("button_color.png")
                        .build();

        ObjectCollection objects =
                new ObjectCollection.Builder().withImages(buttonImage, colorSample).build();

        ActionResult confirmedButtons = action.perform(confirmButtons, objects);
        log.info("Found {} confirmed UI buttons", confirmedButtons.getMatchList().size());
    }
}
