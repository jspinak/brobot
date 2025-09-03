package io.github.jspinak.brobot.startup.orchestration;

import org.sikuli.basics.Settings;
import org.sikuli.script.ImagePath;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.awt.GraphicsEnvironment;
import java.io.File;

/**
 * ApplicationContextInitializer that ensures critical Brobot settings are configured
 * before ANY Spring beans are created.
 * 
 * This runs at the very beginning of Spring Boot application startup and handles:
 * - ImagePath configuration (prevents "not found" errors during State construction)
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApplicationContextInitializer implements org.springframework.context.ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.out.println("=== Brobot Early Initialization ===");
        
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        
        // CRITICAL: Initialize DPI settings BEFORE ImagePath to ensure patterns are loaded correctly
        initializeDPISettings(env);
        
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
    
    private void initializeDPISettings(ConfigurableEnvironment env) {
        System.out.println("[Brobot] Configuring DPI settings...");
        
        // Get DPI configuration from properties
        String resizeFactor = env.getProperty("brobot.dpi.resize-factor", "auto");
        double similarityThreshold = Double.parseDouble(env.getProperty("brobot.action.similarity", "0.70"));
        
        // Configure similarity threshold
        Settings.MinSimilarity = similarityThreshold;
        System.out.println("[Brobot] Similarity threshold: " + similarityThreshold);
        
        // Configure DPI scaling
        float targetResize = 1.0f;
        
        if ("auto".equalsIgnoreCase(resizeFactor)) {
            System.out.println("[Brobot] DPI auto-detection enabled");
            
            // Detect display scaling
            double scaleX = 1.0;
            double scaleY = 1.0;
            
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                if (ge != null && ge.getDefaultScreenDevice() != null) {
                    scaleX = ge.getDefaultScreenDevice()
                        .getDefaultConfiguration()
                        .getDefaultTransform()
                        .getScaleX();
                    scaleY = ge.getDefaultScreenDevice()
                        .getDefaultConfiguration()
                        .getDefaultTransform()
                        .getScaleY();
                }
            } catch (Exception e) {
                System.out.println("[Brobot] Could not detect display scaling: " + e.getMessage());
            }
            
            System.out.println("[Brobot] Display scaling detected: " + (int)(scaleX * 100) + "% x " + (int)(scaleY * 100) + "%");
            
            // Calculate appropriate resize factor based on scaling
            if (Math.abs(scaleX - 1.25) < 0.01) {
                targetResize = 0.8f; // 125% scaling
                System.out.println("[Brobot] 125% DPI scaling detected - pattern scale factor: 0.8");
            } else if (Math.abs(scaleX - 1.5) < 0.01) {
                targetResize = 0.67f; // 150% scaling
                System.out.println("[Brobot] 150% DPI scaling detected - pattern scale factor: 0.67");
            } else if (Math.abs(scaleX - 2.0) < 0.01) {
                targetResize = 0.5f; // 200% scaling
                System.out.println("[Brobot] 200% DPI scaling detected - pattern scale factor: 0.5");
            } else if (Math.abs(scaleX - 1.0) < 0.01) {
                targetResize = 1.0f; // No scaling
                System.out.println("[Brobot] No DPI scaling detected - pattern scale factor: 1.0");
            } else {
                // Calculate appropriate resize factor for non-standard scaling
                targetResize = (float)(1.0 / scaleX);
                System.out.println("[Brobot] Non-standard scaling " + (int)(scaleX * 100) + "% detected - pattern scale factor: " + targetResize);
            }
        } else {
            // Manual configuration
            try {
                targetResize = Float.parseFloat(resizeFactor);
                System.out.println("[Brobot] Using manual resize factor from configuration: " + targetResize);
            } catch (NumberFormatException e) {
                System.err.println("[Brobot] Invalid resize factor '" + resizeFactor + "', defaulting to 1.0 (no scaling)");
                targetResize = 1.0f;
            }
        }
        
        // Apply the DPI configuration
        Settings.AlwaysResize = targetResize;
        Settings.CheckLastSeen = true; // Performance optimization
        
        System.out.println("[Brobot] DPI Configuration Applied:");
        System.out.println("  Settings.AlwaysResize = " + Settings.AlwaysResize);
        System.out.println("  Settings.MinSimilarity = " + Settings.MinSimilarity);
        
        if (Settings.AlwaysResize != 1.0f) {
            System.out.println("  Pattern scaling: " + 
                (Settings.AlwaysResize < 1.0f ? "DOWNSCALE" : "UPSCALE") +
                " (patterns will be resized by " + Settings.AlwaysResize + "x during matching)");
        }
    }
}