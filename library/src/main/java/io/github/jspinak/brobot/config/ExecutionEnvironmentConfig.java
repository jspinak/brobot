package io.github.jspinak.brobot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

/**
 * Spring configuration for ExecutionEnvironment initialization.
 * Sets up the ExecutionEnvironment based on Spring properties and profiles.
 */
@Slf4j
@Configuration
public class ExecutionEnvironmentConfig {
    
    @Value("${brobot.framework.mock:false}")
    private boolean mockMode;
    
    @Value("${brobot.force.headless:#{null}}")
    private Boolean forceHeadless;
    
    @Value("${brobot.allow.screen.capture:true}")
    private boolean allowScreenCapture;
    
    private final Environment springEnvironment;
    
    public ExecutionEnvironmentConfig(Environment springEnvironment) {
        this.springEnvironment = springEnvironment;
    }
    
    @PostConstruct
    public void initializeExecutionEnvironment() {
        // Check active profiles
        String[] activeProfiles = springEnvironment.getActiveProfiles();
        boolean hasWindowsProfile = false;
        boolean hasLinuxProfile = false;
        
        for (String profile : activeProfiles) {
            if ("windows".equalsIgnoreCase(profile)) {
                hasWindowsProfile = true;
            } else if ("linux".equalsIgnoreCase(profile)) {
                hasLinuxProfile = true;
            }
        }
        
        // Build ExecutionEnvironment with profile awareness
        ExecutionEnvironment.Builder builder = ExecutionEnvironment.builder()
            .mockMode(mockMode)
            .allowScreenCapture(allowScreenCapture)
            .verboseLogging(true);
        
        // Override headless setting based on profile
        if (hasWindowsProfile) {
            builder.forceHeadless(false);
            log.info("Windows profile active - forcing non-headless mode");
        } else if (hasLinuxProfile) {
            builder.forceHeadless(true);
            log.info("Linux profile active - forcing headless mode");
        } else if (forceHeadless != null) {
            builder.forceHeadless(forceHeadless);
            log.info("Using configured headless mode: {}", forceHeadless);
        }
        
        // Also check from environment variables
        builder.fromEnvironment();
        
        // Build and set the instance
        ExecutionEnvironment env = builder.build();
        ExecutionEnvironment.setInstance(env);
        
        log.info("ExecutionEnvironment initialized: {}", env.getEnvironmentInfo());
        log.info("Active profiles: {}", String.join(", ", activeProfiles));
        log.info("Mock mode: {}, Has display: {}, Can capture screen: {}", 
                env.isMockMode(), env.hasDisplay(), env.canCaptureScreen());
    }
}