package com.example.quickstart;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs the quick start examples on application startup.
 * Demonstrates both direct Action API and state-based approaches.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuickStartRunner implements ApplicationRunner {

    private final SimpleAutomation simpleAutomation;
    private final LoginAutomation loginAutomation;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Brobot Quick Start Examples ===");
        log.info("This demo showcases two approaches to GUI automation:");
        log.info("  A. Direct Action API (SimpleAutomation)");
        log.info("  B. State-Based Architecture (LoginAutomation)\n");

        // Part A: Direct Action API Examples
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("PART A: DIRECT ACTION API EXAMPLES");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        log.info("1. Full version with explicit steps:");
        simpleAutomation.clickButton();

        log.info("\n2. Simplified version using convenience methods:");
        simpleAutomation.clickButtonSimplified();

        log.info("\n3. Various convenience methods demonstration:");
        simpleAutomation.demonstrateConvenienceMethods();

        log.info("\n4. Production-ready example with error handling:");
        boolean success = simpleAutomation.submitForm("testuser", "testpass");
        log.info("Form submission result: {}", success);

        log.info("\n5. Type-safe configuration examples:");
        simpleAutomation.demonstrateTypeSafeConfiguration();

        log.info("\n6. Common action patterns:");
        simpleAutomation.demonstrateCommonActions();

        log.info("\n7. Proper pause usage in Brobot:");
        simpleAutomation.demonstrateProperPauseUsage();

        // Part B: State-Based Architecture Examples
        log.info("\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("PART B: STATE-BASED ARCHITECTURE (RECOMMENDED)");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        log.info("8. Automatic state navigation with StateNavigator:");
        log.info("   This demonstrates Brobot's automatic pathfinding between states.");
        log.info("   StateNavigator will:");
        log.info("   - Detect current state via @IncomingTransition methods");
        log.info("   - Find path to target state via @OutgoingTransition annotations");
        log.info("   - Execute necessary transitions automatically\n");

        boolean navigationSuccess = loginAutomation.navigateToDashboard();
        log.info("Navigation result: {}", navigationSuccess ? "✓ SUCCESS" : "✗ FAILED");

        // Summary
        log.info("\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("SUMMARY");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("\n✓ Examples Complete!");
        log.info("\nKey Principles:");
        log.info("  • Use type-safe configuration builders (PatternFindOptions, ClickOptions)");
        log.info("  • Never use Thread.sleep() - use built-in pause options instead");
        log.info("  • Always check ActionResult.isSuccess() for proper error handling");
        log.info("  • Leverage convenience methods (action.click, action.find) for simpler code");
        log.info("  • Use @State and @Transition for maintainable complex automations");
        log.info("\nRecommendation:");
        log.info("  → Use Direct Actions (Part A) for quick scripts and simple tasks");
        log.info("  → Use State-Based Architecture (Part B) for complex applications");
    }
}
