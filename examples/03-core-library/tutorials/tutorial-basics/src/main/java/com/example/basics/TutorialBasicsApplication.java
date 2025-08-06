package com.example.basics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Tutorial Basics - Introduction to Brobot fundamentals.
 * 
 * This example demonstrates:
 * - State management with @State annotation
 * - Transitions between states with @Transition
 * - StateImage and StateRegion objects
 * - Basic action execution
 * - Mock mode for testing
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.basics", "io.github.jspinak.brobot"})
public class TutorialBasicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutorialBasicsApplication.class, args);
    }
}