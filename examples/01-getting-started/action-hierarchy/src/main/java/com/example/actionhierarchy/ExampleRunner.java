package com.example.actionhierarchy;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.actionhierarchy.states.ExampleState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Runner class that demonstrates the action hierarchy examples on application startup */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExampleRunner implements ApplicationRunner {

    private final ComplexActionExamples complexActionExamples;
    private final ExampleState exampleState;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Action Hierarchy Examples ===");
        log.info("This example demonstrates different ways to implement complex actions in Brobot");

        // Demonstrate different click-until-found approaches
        log.info("\n1. Traditional loop approach:");
        boolean result1 =
                complexActionExamples.clickUntilFound(
                        exampleState.getNextButton(), exampleState.getFinishButton(), 5);
        log.info("Traditional approach result: {}", result1);

        log.info("\n2. Fluent API approach:");
        boolean result2 =
                complexActionExamples.clickUntilFoundFluent(
                        exampleState.getNextButton(), exampleState.getFinishButton());
        log.info("Fluent API approach result: {}", result2);

        log.info("\n3. Reusable function approach (recommended pattern):");
        boolean result3 =
                complexActionExamples.clickUntilFound(
                        exampleState.getNextButton(), exampleState.getFinishButton(), 10, 1.0);
        log.info("Reusable function result: {}", result3);

        log.info("\n4. Usage example:");
        complexActionExamples.usageExample(
                exampleState.getNextButton(), exampleState.getFinishButton());

        log.info("\n=== Action Hierarchy Examples Complete ===");
        log.info("Note: ClickUntilOptions (previously Method 3) has been removed in Brobot 1.1.0+");
        log.info("The reusable function approach (Method 3 above) is now the recommended pattern.");
    }
}
