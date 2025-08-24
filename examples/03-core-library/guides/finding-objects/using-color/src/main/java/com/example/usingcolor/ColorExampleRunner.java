package com.example.usingcolor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runs the color finding examples on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ColorExampleRunner implements ApplicationRunner {
    
    private final ColorFindingExamples colorFindingExamples;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Using Color Examples ===");
        log.info("Demonstrating color-based object detection in Brobot");
        
        // Demonstrate the three color strategies
        colorFindingExamples.demonstrateColorStrategies();
        
        // Practical examples
        log.info("\n--- Practical Examples ---");
        colorFindingExamples.findRedDotsOnMinimap();
        colorFindingExamples.combineColorWithPattern();
        colorFindingExamples.useHistogramFinding();
        
        // Advanced features
        log.info("\n--- Advanced Features ---");
        colorFindingExamples.colorWithAreaFiltering();
        colorFindingExamples.colorFindAndClick();
        
        // Adjustable options demonstration
        log.info("\n--- Adjustable Options ---");
        colorFindingExamples.demonstrateAdjustableOptions();
        
        log.info("\n=== Color Examples Complete ===");
        log.info("Key takeaways:");
        log.info("- KMEANS: Best for multi-colored objects");
        log.info("- MU (Mean): Default, works well for single-color objects");
        log.info("- CLASSIFICATION: For pixel-level classification");
        log.info("- Combine with pattern matching for better accuracy");
        log.info("- Use area filtering to remove noise");
        log.info("- Adjust similarity and diameter based on your needs");
    }
}