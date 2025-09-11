package com.example.pureactions;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Runs the pure actions examples on application startup */
@Component
@RequiredArgsConstructor
@Slf4j
public class PureActionsRunner implements ApplicationRunner {

    private final PureActionsDemo pureActionsDemo;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Pure Actions Quick Start Examples ===");
        log.info("Demonstrating the new approach that separates finding from acting");

        // Demonstrate the evolution from old to new
        pureActionsDemo.demonstrateOldVsNew();

        // Show common use cases
        log.info("\n--- Common Use Cases ---");
        pureActionsDemo.clickButtonExample();
        pureActionsDemo.typeInFieldExample();
        pureActionsDemo.highlightFoundElements();
        pureActionsDemo.rightClickMenuExample();

        // Demonstrate convenience methods
        log.info("\n--- Convenience Methods ---");
        pureActionsDemo.demonstrateConvenienceMethods();

        // Working with results
        log.info("\n--- Working with Results ---");
        pureActionsDemo.workWithResults();

        // Error handling
        log.info("\n--- Error Handling ---");
        pureActionsDemo.demonstrateErrorHandling();

        // Best practices
        log.info("\n--- Best Practices ---");
        pureActionsDemo.handleBothSuccessAndFailure();
        pureActionsDemo.reuseFindResults();

        // Real-world examples with conditional chains
        log.info("\n--- Real-World Examples ---");
        pureActionsDemo.performLogin("testuser", "testpass");
        pureActionsDemo.saveWithConfirmation();

        log.info("\n=== Pure Actions Examples Complete ===");
        log.info("Pure actions provide:");
        log.info("- Clear separation of concerns");
        log.info("- Better error handling");
        log.info("- More testable code");
        log.info("- Greater flexibility");
        log.info("- Conditional action chains for complex workflows");
    }
}
