package com.example.usingcolor;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.find.histogram.HistogramFindOptions;
import io.github.jspinak.brobot.action.basic.find.HSVBinOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.AreaFilteringOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demonstrates color-based finding in Brobot
 * Based on documentation from /docs/03-core-library/guides/finding-objects/using-color.md
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ColorFindingExamples {
    
    private final Action action;
    
    /**
     * Demonstrates the three color analysis strategies
     */
    public void demonstrateColorStrategies() {
        log.info("=== Color Analysis Strategies ===");
        
        StateImage targetImage = new StateImage.Builder()
                .addPatterns("color-target")
                .setName("ColorTarget")
                .build();
        
        // 1. KMEANS - Find dominant colors using k-means clustering
        log.info("\n1. KMEANS Strategy - Finding dominant colors");
        ColorFindOptions kmeansColor = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.KMEANS)
                .setKmeans(3)  // Find 3 dominant colors
                .setDiameter(5)
                .setSimilarity(0.9)
                .setMaxMatchesToActOn(10)
                .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(targetImage)
                .build();
        
        ActionResult kmeansResult = action.perform(kmeansColor, objects);
        log.info("KMEANS found {} matches", kmeansResult.getMatchList().size());
        
        // 2. MU (Mean Color) - Default strategy
        log.info("\n2. MU Strategy - Using mean color statistics");
        ColorFindOptions meanColor = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.MU)
                .setDiameter(5)
                .setSimilarity(0.95)
                .setAreaFiltering(AreaFilteringOptions.builder()
                        .minArea(10)  // Filter out small noise
                        .build())
                .build();
        
        ActionResult muResult = action.perform(meanColor, objects);
        log.info("MU found {} matches", muResult.getMatchList().size());
        
        // 3. CLASSIFICATION - Multi-class classification
        log.info("\n3. CLASSIFICATION Strategy - Multi-class pixel classification");
        ColorFindOptions classification = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.CLASSIFICATION)
                .setSimilarity(0.8)
                .build();
        
        ActionResult classResult = action.perform(classification, objects);
        log.info("CLASSIFICATION found {} matches", classResult.getMatchList().size());
    }
    
    /**
     * Example: Finding Red Dots on a Minimap
     */
    public void findRedDotsOnMinimap() {
        log.info("=== Finding Red Dots on Minimap ===");
        
        // Create an image object for the red dot
        StateImage redDot = new StateImage.Builder()
                .setName("red_dot")
                .addPatterns("reddot1.png")
                .addPatterns("reddot2.png")  // Multiple samples improve accuracy
                .build();
        
        // Configure color finding
        ColorFindOptions findRedDots = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.MU)  // Use mean color
                .setDiameter(3)  // Minimum cluster size
                .setSimilarity(0.9)
                .setMaxMatchesToActOn(10)
                .setAreaFiltering(AreaFilteringOptions.builder()
                        .minArea(5)  // Filter out noise
                        .build())
                .build();
        
        // Execute the find operation
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(redDot)
                .build();
        
        ActionResult result = action.perform(findRedDots, objects);
        log.info("Found {} red dots on minimap", result.getMatchList().size());
    }
    
    /**
     * Example: Combining Color with Pattern Matching
     */
    public void combineColorWithPattern() {
        log.info("=== Combining Color with Pattern Matching ===");
        
        StateImage buttonImage = new StateImage.Builder()
                .addPatterns("button-shape")
                .setName("ButtonShape")
                .build();
        
        // First find patterns, then filter by color
        PatternFindOptions patternFind = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.7)
                .build();
        
        ColorFindOptions colorFilter = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.KMEANS)
                .setKmeans(2)
                .setSimilarity(0.85)
                .build();
        
        ActionChainOptions combineColorPattern = new ActionChainOptions.Builder(patternFind)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .then(colorFilter)
                .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(buttonImage)
                .build();
        
        ActionResult result = action.perform(combineColorPattern, objects);
        log.info("Found {} buttons with correct color", result.getMatchList().size());
    }
    
    /**
     * Example: Using Histograms for Complex Images
     */
    public void useHistogramFinding() {
        log.info("=== Histogram Finding for Complex Images ===");
        
        StateImage complexImage = new StateImage.Builder()
                .addPatterns("landscape-image")
                .setName("Landscape")
                .build();
        
        HistogramFindOptions histogramFind = new HistogramFindOptions.Builder()
                .setSimilarity(0.8)
                .setBinOptions(HSVBinOptions.builder()
                        .hueBins(90)
                        .saturationBins(2)
                        .valueBins(1))
                .setMaxMatchesToActOn(5)
                .setIllustrate(HistogramFindOptions.Illustrate.YES)  // Save visual results
                .build();
        
        ObjectCollection complexImages = new ObjectCollection.Builder()
                .withImages(complexImage)
                .build();
        
        ActionResult histogramResult = action.perform(histogramFind, complexImages);
        log.info("Histogram found {} similar regions", histogramResult.getMatchList().size());
    }
    
    /**
     * Example: Area Filtering with Color
     */
    public void colorWithAreaFiltering() {
        log.info("=== Area Filtering with Color ===");
        
        StateImage colorRegion = new StateImage.Builder()
                .addPatterns("color-region")
                .setName("ColorRegion")
                .build();
        
        // Find larger color regions only
        ColorFindOptions largeColorRegions = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.MU)
                .setDiameter(10)
                .setSimilarity(0.85)
                .setAreaFiltering(AreaFilteringOptions.builder()
                        .minArea(100)
                        .maxArea(5000)
                        .build())
                .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(colorRegion)
                .build();
        
        ActionResult result = action.perform(largeColorRegions, objects);
        log.info("Found {} large color regions", result.getMatchList().size());
    }
    
    /**
     * Example: Integration with Other Actions
     */
    public void colorFindAndClick() {
        log.info("=== Color Finding with Click Action ===");
        
        StateImage coloredButton = new StateImage.Builder()
                .addPatterns("colored-button")
                .setName("ColoredButton")
                .build();
        
        // Find colored button, then click it
        ColorFindOptions findColoredButton = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.MU)
                .setSimilarity(0.9)
                .setMaxMatchesToActOn(1)
                .build();
        
        ClickOptions clickButton = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();
        
        ActionChainOptions findAndClick = new ActionChainOptions.Builder(findColoredButton)
                .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .then(clickButton)
                .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(coloredButton)
                .build();
        
        ActionResult result = action.perform(findAndClick, objects);
        if (result.isSuccess()) {
            log.info("Successfully clicked colored button");
        }
    }
    
    /**
     * Demonstrates adjustable options: similarity, diameter, k-means
     */
    public void demonstrateAdjustableOptions() {
        log.info("=== Adjustable Color Options ===");
        
        StateImage target = new StateImage.Builder()
                .addPatterns("adjustable-target")
                .build();
        
        // Low similarity - more matches but possibly false positives
        ColorFindOptions lowSimilarity = new ColorFindOptions.Builder()
                .setSimilarity(0.6)
                .setDiameter(1)  // Single pixel matches
                .build();
        
        ActionResult lowSimResult = action.perform(lowSimilarity, 
                new ObjectCollection.Builder().withImages(target).build());
        log.info("Low similarity (0.6): {} matches", lowSimResult.getMatchList().size());
        
        // High similarity - fewer matches but more accurate
        ColorFindOptions highSimilarity = new ColorFindOptions.Builder()
                .setSimilarity(0.95)
                .setDiameter(5)  // 5x5 pixel clusters must match
                .build();
        
        ActionResult highSimResult = action.perform(highSimilarity,
                new ObjectCollection.Builder().withImages(target).build());
        log.info("High similarity (0.95): {} matches", highSimResult.getMatchList().size());
        
        // K-means with multiple colors
        ColorFindOptions multiColorKmeans = new ColorFindOptions.Builder()
                .setColorStrategy(ColorFindOptions.Color.KMEANS)
                .setKmeans(5)  // Look for 5 distinct colors
                .setSimilarity(0.8)
                .build();
        
        ActionResult kmeansResult = action.perform(multiColorKmeans,
                new ObjectCollection.Builder().withImages(target).build());
        log.info("K-means (5 colors): {} matches", kmeansResult.getMatchList().size());
    }
}