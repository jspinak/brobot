package com.example.quickstart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runs the quick start examples on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuickStartRunner implements ApplicationRunner {
    
    private final SimpleAutomation simpleAutomation;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Brobot Quick Start Examples ===");
        
        log.info("\n1. Full version with explicit steps:");
        simpleAutomation.clickButton();
        
        log.info("\n2. Simplified version using convenience methods:");
        simpleAutomation.clickButtonSimplified();
        
        log.info("\n3. Various convenience methods demonstration:");
        simpleAutomation.demonstrateConvenienceMethods();
        
        log.info("\n4. Production-ready example with error handling:");
        boolean success = simpleAutomation.submitForm("testuser", "testpass");
        log.info("Form submission result: {}", success);
        
        log.info("\n=== Quick Start Examples Complete ===");
        log.info("Check the logs above to understand how each approach works!");
    }
}