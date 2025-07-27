package com.example.brobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.brobot",
    "io.github.jspinak.brobot"
})
public class QuickStartApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(QuickStartApplication.class, args);
        
        // Get the automation component and run an example
        SimpleAutomation automation = context.getBean(SimpleAutomation.class);
        
        System.out.println("=== Brobot Quick Start Example ===");
        System.out.println("This example demonstrates basic find and click operations.");
        System.out.println();
        
        // Run the examples
        System.out.println("1. Running simple button click example...");
        automation.clickButton();
        
        System.out.println("\n2. Running find examples with different strategies...");
        automation.findImagesExample();
        
        System.out.println("\nExample completed!");
    }
}