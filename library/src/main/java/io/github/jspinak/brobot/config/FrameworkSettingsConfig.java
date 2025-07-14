package io.github.jspinak.brobot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Spring configuration for FrameworkSettings initialization.
 * Sets up the static FrameworkSettings based on Spring properties.
 */
@Slf4j
@Configuration
public class FrameworkSettingsConfig {
    
    @Value("${brobot.framework.mock:false}")
    private boolean mockMode;
    
    @PostConstruct
    public void initializeFrameworkSettings() {
        // Set the static mock field
        FrameworkSettings.mock = mockMode;
        
        log.info("FrameworkSettings initialized: mock={}", FrameworkSettings.mock);
        
        // Log other important settings
        log.info("Framework configuration: mock={}, moveMouseDelay={}",
                FrameworkSettings.mock,
                FrameworkSettings.moveMouseDelay);
    }
}