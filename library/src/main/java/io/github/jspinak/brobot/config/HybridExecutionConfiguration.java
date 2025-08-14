package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.action.internal.text.HybridTextTyper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Configuration for hybrid execution mode that supports both profile-based
 * and runtime delegation architectures.
 * 
 * Phase 3: Enables mixed-mode execution where components can switch between
 * mock and live implementations dynamically within a single session.
 * 
 * This configuration is activated by setting:
 * brobot.hybrid.enabled=true
 * 
 * Use cases:
 * 1. Testing workflows that require both mock and live components
 * 2. Gradual migration from runtime checks to profile-based architecture
 * 3. Dynamic testing scenarios where mode switching is needed
 * 4. Development environments that need to toggle between modes
 */
@Configuration
@ConditionalOnProperty(name = "brobot.hybrid.enabled", havingValue = "true")
@Slf4j
public class HybridExecutionConfiguration {
    
    @Autowired
    private Environment environment;
    
    @PostConstruct
    public void logHybridModeActivation() {
        log.info("════════════════════════════════════════════════════════");
        log.info("  HYBRID EXECUTION MODE ACTIVATED");
        log.info("  Active Profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        log.info("  Mock Mode: {}", FrameworkSettings.mock);
        log.info("  Components can switch between mock/live at runtime");
        log.info("════════════════════════════════════════════════════════");
    }
    
    /**
     * Bean post-processor that configures hybrid components for mixed-mode execution.
     */
    @Bean
    public HybridComponentConfigurer hybridComponentConfigurer() {
        return new HybridComponentConfigurer();
    }
    
    /**
     * Configures hybrid components to enable runtime mode switching.
     */
    public static class HybridComponentConfigurer {
        
        @Autowired(required = false)
        private HybridTextTyper hybridTextTyper;
        
        @PostConstruct
        public void configureHybridComponents() {
            if (hybridTextTyper != null) {
                // Enable legacy mode for runtime switching
                hybridTextTyper.setLegacyMode(true);
                log.info("Configured HybridTextTyper for mixed-mode execution");
            }
            
            // Configure other hybrid components as they're added
            // e.g., HybridClickExecutor, HybridSceneProvider, etc.
        }
        
        /**
         * Utility method to switch all hybrid components to mock mode.
         */
        public void switchAllToMock() {
            log.info("Switching all hybrid components to MOCK mode");
            FrameworkSettings.mock = true;
            
            if (hybridTextTyper != null) {
                hybridTextTyper.switchToMock();
            }
            // Add other components as needed
        }
        
        /**
         * Utility method to switch all hybrid components to live mode.
         */
        public void switchAllToLive() {
            log.info("Switching all hybrid components to LIVE mode");
            FrameworkSettings.mock = false;
            
            if (hybridTextTyper != null) {
                hybridTextTyper.switchToLive();
            }
            // Add other components as needed
        }
    }
}