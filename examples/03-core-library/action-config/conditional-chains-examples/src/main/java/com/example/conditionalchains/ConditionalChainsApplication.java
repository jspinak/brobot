package com.example.conditionalchains;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Demonstrates ConditionalActionChain - Brobot's elegant API for conditional execution flows.
 * 
 * This example shows:
 * - Basic find → ifFoundClick → ifNotFoundLog patterns
 * - Multi-step workflows with error handling
 * - Custom logic with lambdas using ifFoundDo/ifNotFoundDo
 * - Combining conditional chains with other patterns
 * - Convenience methods for common operations (ifFoundClick, ifFoundType, etc.)
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.conditionalchains", "io.github.jspinak.brobot"})
public class ConditionalChainsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConditionalChainsApplication.class, args);
    }
}