package com.example.combiningfinds;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runs the combining finds examples on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CombiningFindsRunner implements ApplicationRunner {
    
    private final CombiningFindsExamples combiningFindsExamples;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Combining Finds Examples ===");
        log.info("Demonstrating how to combine multiple find operations");
        
        // Core strategy demonstrations
        log.info("\n--- Core Strategies ---");
        combiningFindsExamples.demonstrateNestedStrategy();
        combiningFindsExamples.demonstrateConfirmStrategy();
        
        // Practical examples
        log.info("\n--- Practical Examples ---");
        combiningFindsExamples.findColoredTextInPanels();
        combiningFindsExamples.detectInteractiveElements();
        
        // Strategy comparison
        log.info("\n--- Strategy Comparison ---");
        combiningFindsExamples.compareStrategies();
        
        // Advanced example
        log.info("\n--- Advanced Example ---");
        combiningFindsExamples.multiStageFiltering();
        
        log.info("\n=== Combining Finds Complete ===");
        log.info("Key concepts:");
        log.info("- NESTED: Search within previous results (refinement)");
        log.info("- CONFIRM: Validate results with additional criteria");
        log.info("- SEQUENTIAL: Independent searches (default)");
        log.info("- Use ActionChainBuilder for complex multi-step chains");
        log.info("- Multi-stage filtering improves accuracy");
    }
}