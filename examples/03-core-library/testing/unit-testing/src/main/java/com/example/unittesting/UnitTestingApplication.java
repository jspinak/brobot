package com.example.unittesting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Unit Testing Example for Brobot
 * 
 * This example demonstrates:
 * - Testing States and StateObjects
 * - Testing Actions and ActionResults
 * - Testing Transitions
 * - Testing with Mock mode
 * - Integration testing patterns
 * - Best practices for Brobot unit tests
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.unittesting"})
public class UnitTestingApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnitTestingApplication.class, args);
    }
}