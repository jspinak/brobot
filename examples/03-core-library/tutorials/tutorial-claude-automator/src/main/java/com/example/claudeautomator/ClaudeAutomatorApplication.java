package com.example.claudeautomator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Tutorial example demonstrating:
 * - State-based automation with Brobot
 * - Declarative search region definition
 * - Cross-state object dependencies
 * 
 * This simplified version of claude-automator shows how to monitor
 * Claude AI's interface states and react to changes.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.claudeautomator", "io.github.jspinak.brobot"})
public class ClaudeAutomatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaudeAutomatorApplication.class, args);
    }
}