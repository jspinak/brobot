package com.example.illustration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Demonstrates Brobot's Advanced Illustration System.
 *
 * <p>This example shows: - Context-aware illustration decisions - Performance optimization with
 * adaptive sampling - Quality-based filtering - Granular configuration for different environments -
 * Resource management to prevent system overload
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.illustration", "io.github.jspinak.brobot"})
public class AdvancedIllustrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdvancedIllustrationApplication.class, args);
    }
}
