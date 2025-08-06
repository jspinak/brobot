package com.example.conditionalchains;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Demonstrates ConditionalActionChain - Brobot's elegant API for conditional execution flows.
 * 
 * This example shows:
 * - Basic find → ifFound → ifNotFound patterns
 * - Multi-step workflows with error handling
 * - Custom logic with lambdas
 * - Combining conditional chains with other patterns
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.conditionalchains", "io.github.jspinak.brobot"})
public class ConditionalChainsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConditionalChainsApplication.class, args);
    }
}