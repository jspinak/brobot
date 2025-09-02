package io.github.jspinak.brobot.startup;

import org.sikuli.script.ImagePath;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;

/**
 * ApplicationContextInitializer that ensures critical Brobot settings are configured
 * before ANY Spring beans are created or AWT classes are loaded.
 * 
 * This runs at the very beginning of Spring Boot application startup and handles:
 * - Physical resolution initialization
 * - ImagePath configuration (prevents "not found" errors during State construction)
 * - DPI settings
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BrobotApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    static {
        // This runs as soon as this class is loaded
        PhysicalResolutionInitializer.forceInitialization();
    }
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.out.println("=== Brobot Early Initialization ===");
        
        // Double-check physical resolution initialization
        PhysicalResolutionInitializer.forceInitialization();
        System.out.println("Brobot ApplicationContextInitializer: Physical resolution mode enabled");
        
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        
        // Initialize ImagePath very early to prevent errors during State construction
        initializeImagePath(env);
        
        // Initialize DPI settings
        initializeDPISettings(env);
        
        System.out.println("=== Brobot Early Initialization Complete ===");
    }
    
    private void initializeImagePath(ConfigurableEnvironment env) {
        // Get the image path from properties
        String imagePath = env.getProperty("brobot.core.image-path", "images");
        
        // Remove trailing slash for consistency
        if (imagePath.endsWith("/") || imagePath.endsWith("\\")) {
            imagePath = imagePath.substring(0, imagePath.length() - 1);
        }
        
        System.out.println("[Brobot] Setting ImagePath bundle to: " + imagePath);
        
        try {
            // Set the SikuliX bundle path very early
            ImagePath.setBundlePath(imagePath);
            
            // Also add the path to ensure it's searchable
            ImagePath.add(imagePath);
            
            // Verify the path exists
            File imageDir = new File(imagePath);
            if (!imageDir.exists()) {
                System.out.println("[Brobot] Image directory does not exist: " + imageDir.getAbsolutePath() + ". Creating it...");
                imageDir.mkdirs();
            }
            
            System.out.println("[Brobot] ImagePath configured. Bundle path: " + ImagePath.getBundlePath());
            
        } catch (Exception e) {
            System.err.println("[Brobot] Failed to initialize ImagePath: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeDPISettings(ConfigurableEnvironment env) {
        // Check if DPI scaling should be disabled
        boolean disableDPIScaling = env.getProperty("brobot.dpi.disable-scaling", Boolean.class, false);
        
        if (disableDPIScaling) {
            System.out.println("[Brobot] Disabling DPI scaling");
            System.setProperty("sun.java2d.dpiaware", "false");
            System.setProperty("sun.java2d.uiScale", "1.0");
            System.setProperty("sun.java2d.win.uiScaleX", "1.0");
            System.setProperty("sun.java2d.win.uiScaleY", "1.0");
            
            // Set SikuliX to not resize patterns
            org.sikuli.basics.Settings.AlwaysResize = 1.0f;
            System.out.println("[Brobot] Settings.AlwaysResize set to 1.0 (no scaling)");
            
            // Set a system property so BrobotDPIConfiguration knows scaling was disabled early
            System.setProperty("brobot.dpi.scaling.disabled", "true");
        }
    }
}