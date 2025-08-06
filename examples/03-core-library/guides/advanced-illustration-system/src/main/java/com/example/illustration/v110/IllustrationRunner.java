package com.example.illustration.v110;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

/**
 * Runner for v1.1.0 illustration examples.
 * 
 * Run with different profiles:
 * - java -jar app.jar --spring.profiles.active=dev    (full illustrations)
 * - java -jar app.jar --spring.profiles.active=test   (selective illustrations)
 * - java -jar app.jar --spring.profiles.active=prod   (no illustrations)
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.illustration.v110", "io.github.jspinak.brobot"})
@RequiredArgsConstructor
@Slf4j
public class IllustrationRunner implements CommandLineRunner {
    
    private final SimpleIllustrationExample simpleExample;
    private final WorkingIllustrationExample workingExample;
    
    public static void main(String[] args) {
        SpringApplication.run(IllustrationRunner.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("================================================");
        log.info("Brobot v1.1.0 Illustration System Demo");
        log.info("================================================");
        
        // Show current illustration status
        simpleExample.checkIllustrationStatus();
        
        Thread.sleep(1000);
        
        // Demonstrate simple property-based illustrations
        log.info("\n>>> Simple Property-Based Examples <<<");
        simpleExample.demonstrateIllustrations();
        
        Thread.sleep(1000);
        
        // Show different scenarios
        simpleExample.demonstrateScenarios();
        
        Thread.sleep(1000);
        
        // Demonstrate IllustrationController API
        log.info("\n>>> IllustrationController API Examples <<<");
        workingExample.runAllExamples();
        
        log.info("\n================================================");
        log.info("Demo completed!");
        log.info("================================================");
        
        log.info("\nTo try different illustration modes:");
        log.info("1. Edit application.properties");
        log.info("2. Or run with profiles:");
        log.info("   --spring.profiles.active=dev");
        log.info("   --spring.profiles.active=test");
        log.info("   --spring.profiles.active=prod");
    }
}