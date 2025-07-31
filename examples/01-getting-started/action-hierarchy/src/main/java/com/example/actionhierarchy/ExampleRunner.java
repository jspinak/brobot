package com.example.actionhierarchy;

import com.example.actionhierarchy.states.ExampleState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runner class that demonstrates the action hierarchy examples on application startup
 */
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
        
        // Demonstrate basic actions first
        log.info("\n1. Demonstrating basic actions:");
        complexActionExamples.demonstrateBasicActions(exampleState.getNextButton());
        
        // Demonstrate different click-until-found approaches
        log.info("\n2. Traditional loop approach:");
        boolean result1 = complexActionExamples.clickUntilFound(
                exampleState.getNextButton(), 
                exampleState.getFinishButton(), 
                5
        );
        log.info("Traditional approach result: {}", result1);
        
        log.info("\n3. Fluent API approach:");
        boolean result2 = complexActionExamples.clickUntilFoundFluent(
                exampleState.getNextButton(),
                exampleState.getFinishButton()
        );
        log.info("Fluent API approach result: {}", result2);
        
        log.info("\n4. Built-in ClickUntilOptions approach:");
        boolean result3 = complexActionExamples.clickUntilFoundBuiltIn(
                exampleState.getNextButton(),
                exampleState.getFinishButton()
        );
        log.info("Built-in approach result: {}", result3);
        
        log.info("\n5. Reusable function approach:");
        boolean result4 = complexActionExamples.clickUntilFoundReusable(
                exampleState.getNextButton(),
                exampleState.getFinishButton(),
                10,
                1.0
        );
        log.info("Reusable function result: {}", result4);
        
        log.info("\n=== Action Hierarchy Examples Complete ===");
    }
}