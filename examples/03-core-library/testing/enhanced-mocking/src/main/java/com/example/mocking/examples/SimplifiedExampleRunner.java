package com.example.mocking.examples;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** Simplified runner for enhanced mocking examples. */
@Component
@Profile("simple")
@Slf4j
public class SimplifiedExampleRunner implements CommandLineRunner {

    @Autowired private SimplifiedScenarioExample scenarioExample;

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
                        scenarioExample.testLoginUnderNetworkIssues();
                        break;
                    case "5":
                        scenarioExample.testRetryBehaviorWithCascadingFailures();
                        break;
                    case "6":
                        scenarioExample.testPerformanceUnderLoad();
                        break;
                    case "7":
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
        System.out.println("4. Basic Scenario Setup (Docs)");
        System.out.println("5. Advanced Failure Patterns (Docs)");
        System.out.println("6. Temporal Conditions (Docs)");
        System.out.println("7. Run All Examples");
        System.out.println("0. Exit");
        System.out.print("\nEnter your choice: ");
    }

    private void runAllExamples() {
        log.info("\n=== Running All Examples ===\n");

        log.info(">>> Original Examples <<<");
        scenarioExample.runSimpleDegradingPerformance();
        scenarioExample.runSimpleCascadingFailures();
        scenarioExample.runSimpleTimeBasedStates();

        log.info(">>> Documentation Examples <<<");
        scenarioExample.testLoginUnderNetworkIssues();
        scenarioExample.testRetryBehaviorWithCascadingFailures();
        scenarioExample.testPerformanceUnderLoad();

        log.info("\n=== All Examples Completed ===");
    }
}
