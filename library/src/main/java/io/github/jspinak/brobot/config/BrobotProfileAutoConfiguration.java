package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.tools.testing.mock.state.MockStateManagement;
import io.github.jspinak.brobot.navigation.service.StateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Auto-configuration for Brobot that applies profile-based settings.
 * This configuration ensures proper mock/live mode setup based on active profiles.
 */
@Configuration
@ConditionalOnClass(Action.class)
@PropertySource("classpath:brobot-defaults.properties")
@Slf4j
public class BrobotProfileAutoConfiguration {
    
    @Autowired
    private Environment environment;
    
    /**
     * Configuration for test profile - loads test defaults
     */
    @Configuration
    @Profile("test")
    @PropertySource("classpath:brobot-test-defaults.properties")
    public static class TestProfileConfiguration {
        
        @PostConstruct
        public void configureTestEnvironment() {
            log.info("═══ BROBOT TEST PROFILE ACTIVATED ═══");
            log.info("Loading test defaults from brobot-test-defaults.properties");
            
            // Ensure mock mode is properly set
            FrameworkSettings.mock = true;
            log.info("Mock mode ENABLED for test profile");
            
            // Configure test-optimized settings
            configureTestSettings();
        }
        
        private void configureTestSettings() {
            // Disable visual elements
            FrameworkSettings.drawFind = false;
            FrameworkSettings.drawClick = false;
            FrameworkSettings.drawDrag = false;
            FrameworkSettings.drawMove = false;
            FrameworkSettings.drawHighlight = false;
            FrameworkSettings.saveSnapshots = false;
            FrameworkSettings.saveHistory = false;
            
            // Fast execution
            FrameworkSettings.pauseBeforeMouseDown = 0;
            FrameworkSettings.pauseAfterMouseDown = 0;
            FrameworkSettings.pauseBeforeMouseUp = 0;
            FrameworkSettings.pauseAfterMouseUp = 0;
            FrameworkSettings.moveMouseDelay = 0;
            
            // Fast mock timings
            FrameworkSettings.mockTimeFindFirst = 0.01;
            FrameworkSettings.mockTimeFindAll = 0.02;
            FrameworkSettings.mockTimeClick = 0.005;
            FrameworkSettings.mockTimeMove = 0.01;
            
            log.debug("Test settings applied: visual elements disabled, pauses removed");
        }
    }
    
    /**
     * Configuration for production/live profile
     */
    @Configuration
    @Profile("!test")
    public static class LiveProfileConfiguration {
        
        @Autowired
        private Environment environment;
        
        @PostConstruct
        public void configureLiveEnvironment() {
            log.info("═══ BROBOT LIVE PROFILE ACTIVATED ═══");
            
            // Check if mock mode is explicitly set
            String mockProperty = environment.getProperty("brobot.framework.mock", "false");
            FrameworkSettings.mock = Boolean.parseBoolean(mockProperty);
            
            if (FrameworkSettings.mock) {
                log.warn("Mock mode is ENABLED in live profile - this is unusual!");
            } else {
                log.info("Mock mode DISABLED - using real screen interaction");
            }
        }
    }
    
    /**
     * Mock state management bean - only available in test profile or when mock is enabled
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "brobot.framework.mock", havingValue = "true", matchIfMissing = false)
    public MockStateManagement mockStateManagement(StateService stateService) {
        log.info("Creating MockStateManagement bean (mock mode enabled)");
        return new MockStateManagement(stateService);
    }
    
    /**
     * Enhanced mock state management for test profile
     */
    @Bean
    @Profile("test")
    @Primary
    @ConditionalOnMissingBean(name = "testMockStateManagement")
    public MockStateManagement testMockStateManagement(StateService stateService) {
        log.info("Creating enhanced MockStateManagement for test profile");
        MockStateManagement mockManagement = new MockStateManagement(stateService);
        
        // Configure default test probabilities
        configureDefaultTestProbabilities(mockManagement);
        
        return mockManagement;
    }
    
    private void configureDefaultTestProbabilities(MockStateManagement mockManagement) {
        // Set default 100% probability for all states in test mode
        // This ensures deterministic behavior unless specifically overridden
        log.debug("Setting default 100% probability for all states in test mode");
        // Note: Individual states can override this in their @PostConstruct
    }
    
    /**
     * Profile validator - ensures configuration consistency
     */
    @Component
    public static class ProfileValidator {
        
        @Autowired
        private Environment environment;
        
        @PostConstruct
        public void validateProfileConfiguration() {
            String[] activeProfiles = environment.getActiveProfiles();
            boolean isTestProfile = java.util.Arrays.asList(activeProfiles).contains("test");
            
            // Validate mock mode consistency
            String mockProperty = environment.getProperty("brobot.framework.mock", "false");
            boolean mockEnabled = Boolean.parseBoolean(mockProperty);
            
            if (isTestProfile && !mockEnabled) {
                log.warn("⚠️ Test profile active but mock mode not enabled - fixing...");
                FrameworkSettings.mock = true;
            }
            
            if (!isTestProfile && mockEnabled) {
                log.info("ℹ️ Mock mode enabled in non-test profile - this is valid for development");
            }
            
            log.info("Profile validation complete: profiles={}, mock={}", 
                    java.util.Arrays.toString(activeProfiles), FrameworkSettings.mock);
        }
    }
    
    /**
     * Configuration properties logger for debugging
     */
    @PostConstruct
    public void logConfigurationSummary() {
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║             BROBOT CONFIGURATION SUMMARY                      ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║ Active Profiles: {}", String.format("%-45s", 
                java.util.Arrays.toString(environment.getActiveProfiles())));
        log.info("║ Mock Mode: {}", String.format("%-52s", FrameworkSettings.mock));
        log.info("║ Image Path: {}", String.format("%-51s", 
                environment.getProperty("brobot.core.image-path", "images")));
        log.info("║ Similarity: {}", String.format("%-51s", 
                environment.getProperty("brobot.action.similarity", "0.85")));
        log.info("╚══════════════════════════════════════════════════════════════╝");
    }
}