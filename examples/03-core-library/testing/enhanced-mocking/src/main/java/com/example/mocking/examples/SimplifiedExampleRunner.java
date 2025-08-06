package com.example.mocking.examples;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.Scanner;

/**
 * Simplified runner for enhanced mocking examples.
 */
@Component
@Profile("simple")
@Slf4j
public class SimplifiedExampleRunner implements CommandLineRunner {
    
    @Autowired
    private SimplifiedScenarioExample scenarioExample;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("\n====================================");
        log.info("Enhanced Mocking Examples (Simplified)");
        log.info("====================================\n");
        
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        while (running) {
            printMenu();
            String choice = scanner.nextLine();
            
            try {
                switch (choice) {
                    case "1":
                        scenarioExample.runSimpleDegradingPerformance();
                        break;
                    case "2":
                        scenarioExample.runSimpleCascadingFailures();
                        break;
                    case "3":
                        scenarioExample.runSimpleTimeBasedStates();
                        break;
                    case "4":
                        runAllExamples();
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        log.info("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                log.error("Error running example: {}", e.getMessage(), e);
            }
            
            if (running) {
                log.info("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
        
        log.info("Thank you for exploring Enhanced Mocking!");
    }
    
    private void printMenu() {
        System.out.println("\n=== Enhanced Mocking Examples ===");
        System.out.println("1. Degrading Performance");
        System.out.println("2. Cascading Failures");
        System.out.println("3. Time-Based States");
        System.out.println("4. Run All Examples");
        System.out.println("0. Exit");
        System.out.print("\nEnter your choice: ");
    }
    
    private void runAllExamples() {
        log.info("\n=== Running All Examples ===\n");
        
        scenarioExample.runSimpleDegradingPerformance();
        scenarioExample.runSimpleCascadingFailures();
        scenarioExample.runSimpleTimeBasedStates();
        
        log.info("\n=== All Examples Completed ===");
    }
}