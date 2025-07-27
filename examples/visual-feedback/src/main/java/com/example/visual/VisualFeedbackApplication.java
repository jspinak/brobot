package com.example.visual;

import com.example.visual.demos.IllustrationDemo;
import com.example.visual.demos.AnalysisDemo;
import com.example.visual.demos.PerformanceDemo;
import com.example.visual.demos.ReportingDemo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Visual Feedback Example Application
 * 
 * Demonstrates Brobot's visual feedback, illustration, and analysis capabilities
 * for monitoring automation execution and generating visual documentation.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.visual",
    "io.github.jspinak.brobot"
})
@RequiredArgsConstructor
@Slf4j
public class VisualFeedbackApplication implements CommandLineRunner {
    
    private final IllustrationDemo illustrationDemo;
    private final AnalysisDemo analysisDemo;
    private final PerformanceDemo performanceDemo;
    private final ReportingDemo reportingDemo;
    
    public static void main(String[] args) {
        SpringApplication.run(VisualFeedbackApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== Visual Feedback & Illustration Example ===");
        log.info("Demonstrating visual monitoring and analysis capabilities");
        log.info("");
        
        // Basic illustration examples
        illustrationDemo.runDemos();
        
        // Result analysis examples
        analysisDemo.runDemos();
        
        // Performance monitoring
        performanceDemo.runDemos();
        
        // Report generation
        reportingDemo.runDemos();
        
        log.info("");
        log.info("Visual feedback examples completed!");
        log.info("Check 'build/illustrations/' for generated visual documentation");
    }
}