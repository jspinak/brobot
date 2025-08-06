package com.example.illustration;

import com.example.illustration.examples.LoginWorkflowExample;
import com.example.illustration.examples.PerformanceOptimizationExample;
import com.example.illustration.examples.QualityFilteringExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Runs all advanced illustration system examples.
 */
@Component
@Slf4j
public class AdvancedIllustrationRunner implements CommandLineRunner {
    
    private final LoginWorkflowExample loginExample;
    private final PerformanceOptimizationExample performanceExample;
    private final QualityFilteringExample qualityExample;
    
    public AdvancedIllustrationRunner(LoginWorkflowExample loginExample,
                                     PerformanceOptimizationExample performanceExample,
                                     QualityFilteringExample qualityExample) {
        this.loginExample = loginExample;
        this.performanceExample = performanceExample;
        this.qualityExample = qualityExample;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("================================================");
        log.info("Advanced Illustration System Examples");
        log.info("================================================");
        log.info("");
        
        // Login workflow with state-aware illustrations
        log.info(">>> Running Login Workflow Example <<<");
        loginExample.runExample();
        log.info("");
        
        Thread.sleep(1000);
        
        // Performance optimization strategies
        log.info(">>> Running Performance Optimization Example <<<");
        performanceExample.runExample();
        log.info("");
        
        Thread.sleep(1000);
        
        // Quality-based filtering
        log.info(">>> Running Quality Filtering Example <<<");
        qualityExample.runExample();
        log.info("");
        
        log.info("================================================");
        log.info("All examples completed!");
        log.info("================================================");
        
        log.info("");
        log.info("Key takeaways:");
        log.info("✓ Context-aware decisions reduce noise while capturing important events");
        log.info("✓ Performance optimization prevents system overload");
        log.info("✓ Quality filtering focuses on meaningful visualizations");
        log.info("✓ State-based priorities ensure critical actions are documented");
        log.info("✓ Adaptive sampling adjusts to system conditions");
        log.info("✓ Custom quality calculations enable domain-specific filtering");
        
        log.info("");
        log.info("Check the illustrations directory for generated visualizations!");
    }
}