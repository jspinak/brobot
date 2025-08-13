package io.github.jspinak.brobot.startup;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * ApplicationContextInitializer that ensures physical resolution is configured
 * before ANY Spring beans are created or AWT classes are loaded.
 * 
 * This runs at the very beginning of Spring Boot application startup.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BrobotApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    static {
        // This runs as soon as this class is loaded
        PhysicalResolutionInitializer.forceInitialization();
    }
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // Double-check initialization
        PhysicalResolutionInitializer.forceInitialization();
        
        System.out.println("Brobot ApplicationContextInitializer: Physical resolution mode enabled");
    }
}