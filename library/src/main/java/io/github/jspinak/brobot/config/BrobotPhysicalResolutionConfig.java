package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.screen.PhysicalResolutionScreen;
import org.sikuli.script.Screen;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration that makes physical resolution capture the default for Brobot.
 * 
 * This ensures all screen captures are at physical resolution (e.g., 1920x1080)
 * rather than logical resolution (e.g., 1536x864 with 125% DPI scaling).
 * 
 * This configuration makes Brobot behave exactly like the SikuliX IDE,
 * achieving the same high pattern matching scores (~0.99) without needing
 * Settings.AlwaysResize or other scaling workarounds.
 * 
 * Enable this configuration by setting: brobot.capture.physical-resolution=true
 */
@Configuration
@ConditionalOnProperty(name = "brobot.capture.physical-resolution", havingValue = "true", matchIfMissing = true)
public class BrobotPhysicalResolutionConfig {
    
    /**
     * Provides a PhysicalResolutionScreen as the primary Screen implementation.
     * This will be used by default for all screen operations.
     */
    @Bean
    @Primary
    public Screen physicalScreen() {
        System.out.println("=== Brobot Physical Resolution Mode ===");
        PhysicalResolutionScreen screen = new PhysicalResolutionScreen();
        
        if (screen.isPhysicalResolutionEnabled()) {
            System.out.println("✓ Physical resolution capture is ACTIVE");
        } else {
            System.out.println("⚠ Physical resolution may not be properly configured");
            System.out.println("  Ensure the application is started with: -Dsun.java2d.dpiaware=false");
        }
        
        System.out.println("Pattern matching will work like SikuliX IDE");
        System.out.println("=======================================\n");
        
        return screen;
    }
    
    /**
     * Factory method to create PhysicalResolutionScreen instances when needed.
     */
    @Bean
    public PhysicalScreenFactory physicalScreenFactory() {
        return new PhysicalScreenFactory();
    }
    
    /**
     * Factory class for creating PhysicalResolutionScreen instances.
     */
    public static class PhysicalScreenFactory {
        
        /**
         * Creates a new PhysicalResolutionScreen instance.
         */
        public PhysicalResolutionScreen create() {
            return new PhysicalResolutionScreen();
        }
        
        /**
         * Creates a Screen that captures at physical resolution.
         * Can be used as a drop-in replacement for new Screen().
         */
        public Screen createScreen() {
            return new PhysicalResolutionScreen();
        }
    }
}