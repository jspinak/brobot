package io.github.jspinak.brobot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Initializes the static FrameworkSettings from BrobotProperties on startup.
 * 
 * <p>This component bridges the gap between the modern property-based configuration
 * and the legacy static fields. It runs after Spring context initialization to
 * ensure all properties are loaded and resolved.</p>
 * 
 * <p>This is a transitional component that will be removed once all code
 * migrates to using BrobotProperties directly instead of FrameworkSettings.</p>
 * 
 * @since 1.1.0
 */
@Component
@ConditionalOnBean(BrobotProperties.class)
@RequiredArgsConstructor
@Slf4j
public class BrobotPropertiesInitializer implements InitializingBean {
    
    private final BrobotProperties properties;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initializing FrameworkSettings from BrobotProperties");
        
        // Apply the loaded properties to the static settings
        properties.applyToFrameworkSettings();
        
        // Also set the SikuliX bundle path early for headless mode
        String imagePath = properties.getCore().getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            if (!env.shouldSkipSikuliX()) {
                log.info("Setting SikuliX bundle path early to: {}", imagePath);
                org.sikuli.script.ImagePath.setBundlePath(imagePath);
            }
        }
        
        log.debug("FrameworkSettings initialized with:");
        log.debug("  Image path: {}", properties.getCore().getImagePath());
        log.debug("  Mock mode: {}", properties.getCore().isMock());
        log.debug("  Headless: {}", properties.getCore().isHeadless());
        log.debug("  Save snapshots: {}", properties.getScreenshot().isSaveSnapshots());
        log.debug("  Save history: {}", properties.getScreenshot().isSaveHistory());
    }
}