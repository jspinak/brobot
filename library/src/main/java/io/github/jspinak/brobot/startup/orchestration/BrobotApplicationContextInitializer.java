package io.github.jspinak.brobot.startup.orchestration;

import org.sikuli.script.ImagePath;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;

/**
 * ApplicationContextInitializer that ensures critical Brobot settings are configured
 * before ANY Spring beans are created.
 * 
 * This runs at the very beginning of Spring Boot application startup and handles:
 * - ImagePath configuration (prevents "not found" errors during State construction)
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BrobotApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.out.println("=== Brobot Early Initialization ===");
        
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        
        // Initialize ImagePath very early to prevent errors during State construction
        initializeImagePath(env);
        
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
}