package com.example.specialstates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot application for testing PreviousState and CurrentState transitions.
 *
 * <p>This example demonstrates: 1. PreviousState - Returning to hidden states when closing modal
 * dialogs 2. CurrentState - Self-transitions for refresh, pagination, etc. 3. Hidden state
 * management - How states are automatically tracked as hidden
 *
 * <p>The application runs in mock mode (configured in application.properties) to test the state
 * transition logic without requiring a real GUI.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.specialstates", "io.github.jspinak.brobot"})
public class SpecialStatesApplication {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("Special States Example - Testing Dynamic Transitions");
        System.out.println("===========================================");
        System.out.println("This example demonstrates:");
        System.out.println("1. PreviousState - Returning to hidden states");
        System.out.println("2. CurrentState - Self-transitions");
        System.out.println("3. Hidden state management");
        System.out.println("===========================================\n");

        SpringApplication.run(SpecialStatesApplication.class, args);
    }
}
