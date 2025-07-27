package com.example.chaining;

import com.example.chaining.demos.BasicChainingDemo;
import com.example.chaining.demos.AdvancedChainingDemo;
import com.example.chaining.demos.ConditionalChainingDemo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Action Chaining Example Application
 * 
 * Demonstrates the power of Brobot's fluent API for creating
 * readable, maintainable automation chains with automatic logging.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.chaining",
    "io.github.jspinak.brobot"
})
@RequiredArgsConstructor
@Slf4j
public class ActionChainingApplication implements CommandLineRunner {
    
    private final BasicChainingDemo basicDemo;
    private final AdvancedChainingDemo advancedDemo;
    private final ConditionalChainingDemo conditionalDemo;
    
    public static void main(String[] args) {
        SpringApplication.run(ActionChainingApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== Action Chaining Example ===");
        log.info("Demonstrating fluent action chains with automatic logging");
        log.info("");
        
        // Basic chaining examples
        basicDemo.runDemos();
        
        // Advanced chaining patterns
        advancedDemo.runDemos();
        
        // Conditional chaining
        conditionalDemo.runDemos();
        
        log.info("");
        log.info("Action chaining examples completed!");
    }
}