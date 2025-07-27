package com.example.states;

import com.example.states.automation.StateNavigationDemo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * State Transitions Example Application
 * 
 * Demonstrates state-based navigation with comprehensive automatic logging
 * for every transition and action.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.states",
    "io.github.jspinak.brobot"
})
@RequiredArgsConstructor
@Slf4j
public class StateTransitionsApplication implements CommandLineRunner {
    
    private final StateNavigationDemo navigationDemo;
    
    public static void main(String[] args) {
        SpringApplication.run(StateTransitionsApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== State Transitions Example ===");
        log.info("This example demonstrates automatic logging for state-based navigation");
        log.info("");
        
        // Example 1: Simple direct transition
        navigationDemo.demonstrateDirectTransition();
        
        // Example 2: Multi-step navigation
        navigationDemo.demonstrateMultiStepNavigation();
        
        // Example 3: Conditional navigation
        navigationDemo.demonstrateConditionalNavigation();
        
        // Example 4: Error recovery
        navigationDemo.demonstrateErrorRecovery();
        
        // Example 5: Complex workflow
        navigationDemo.demonstrateComplexWorkflow();
        
        log.info("");
        log.info("State transitions example completed!");
    }
}