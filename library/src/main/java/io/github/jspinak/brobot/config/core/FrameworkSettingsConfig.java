package io.github.jspinak.brobot.config.core;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import io.github.jspinak.brobot.config.mock.MockProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring configuration for FrameworkSettings initialization. Sets up the static FrameworkSettings
 * based on Spring properties.
 */
@Slf4j
@Configuration
public class FrameworkSettingsConfig {

    @Autowired
    private MockProperties mockProperties;

    @PostConstruct
    public void initializeFrameworkSettings() {
        // Set the static mock field from the new MockProperties
        FrameworkSettings.mock = mockProperties.isEnabled();
        
        // Set the action success probability (store in FrameworkSettings for now)
        // This will be used by mock action implementations
        FrameworkSettings.mockActionSuccessProbability = mockProperties.getAction().getSuccessProbability();

        log.info("FrameworkSettings initialized: mock={}, actionSuccessProbability={}", 
                FrameworkSettings.mock, 
                FrameworkSettings.mockActionSuccessProbability);

        // Log other important settings
        log.info(
                "Framework configuration: mock={}, moveMouseDelay={}",
                FrameworkSettings.mock,
                FrameworkSettings.moveMouseDelay);
    }
}
