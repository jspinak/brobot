package com.example.movement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates Brobot's movement actions.
 * 
 * This example shows:
 * - Mouse movement with different patterns
 * - Drag and drop operations
 * - Mouse scrolling
 * - Custom drag sequences
 * - Movement timing and control
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.movement", "io.github.jspinak.brobot"})
public class MovementApplication {
    private static final Logger log = LoggerFactory.getLogger(MovementApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MovementApplication.class, args);
    }
}