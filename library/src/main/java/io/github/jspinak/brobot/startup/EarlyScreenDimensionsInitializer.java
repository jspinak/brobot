package io.github.jspinak.brobot.startup;

import io.github.jspinak.brobot.capture.ScreenDimensions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.awt.*;

/**
 * Initializes ScreenDimensions very early in the Spring Boot startup process.
 * This runs before beans are created, ensuring that Region creation uses the 
 * correct dimensions based on the capture provider.
 * 
 * This class is registered via META-INF/spring.factories to run during
 * application context initialization.
 */
@Slf4j
@Component
public class EarlyScreenDimensionsInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        String captureProviderName = env.getProperty("brobot.capture.provider", "JAVACV_FFMPEG");
        
        initializeScreenDimensions(captureProviderName);
    }
    
    /**
     * Initialize screen dimensions based on the capture provider.
     * This method can also be called directly for non-Spring applications.
     */
    public static void initializeScreenDimensions(String captureProviderName) {
        log.info("=== Early Screen Dimensions Initialization ===");
        
        String provider = captureProviderName.toUpperCase();
        int screenWidth;
        int screenHeight;
        
        // Determine resolution based on capture provider
        if (provider.contains("JAVACV_FFMPEG") || 
            provider.contains("FFMPEG") || 
            provider.contains("ROBOT")) {
            // These providers capture at physical resolution
            // For Windows with 125% DPI scaling: 1920x1080 physical
            screenWidth = detectPhysicalResolution();
            screenHeight = detectPhysicalHeight();
            log.info("Using PHYSICAL resolution for provider: {}", provider);
        } else {
            // SikuliX and AUTO capture at logical resolution
            // Get logical dimensions from AWT
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            screenWidth = screenSize.width;
            screenHeight = screenSize.height;
            log.info("Using LOGICAL resolution for provider: {}", provider);
        }
        
        // Initialize the static ScreenDimensions
        ScreenDimensions.initialize(captureProviderName, screenWidth, screenHeight);
        
        log.info("Screen Dimensions initialized: {}x{} for provider: {}", 
                 screenWidth, screenHeight, captureProviderName);
        log.info("============================================");
    }
    
    private static int detectPhysicalResolution() {
        // Try to detect physical resolution
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        
        // Check for known DPI scaling patterns
        if (screenSize.width == 1536 && screenSize.height == 864) {
            // 125% DPI scaling detected - return physical resolution
            return 1920;
        }
        
        // Try to get DPI scale from graphics configuration
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            double scaleX = gc.getDefaultTransform().getScaleX();
            if (scaleX > 1.0) {
                return (int) Math.round(screenSize.width * scaleX);
            }
        } catch (Exception e) {
            log.debug("Could not detect DPI scale: {}", e.getMessage());
        }
        
        // Default to detected size or 1920
        return screenSize.width > 0 ? screenSize.width : 1920;
    }
    
    private static int detectPhysicalHeight() {
        // Try to detect physical resolution
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        
        // Check for known DPI scaling patterns
        if (screenSize.width == 1536 && screenSize.height == 864) {
            // 125% DPI scaling detected - return physical resolution
            return 1080;
        }
        
        // Try to get DPI scale from graphics configuration
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            double scaleY = gc.getDefaultTransform().getScaleY();
            if (scaleY > 1.0) {
                return (int) Math.round(screenSize.height * scaleY);
            }
        } catch (Exception e) {
            log.debug("Could not detect DPI scale: {}", e.getMessage());
        }
        
        // Default to detected size or 1080
        return screenSize.height > 0 ? screenSize.height : 1080;
    }
}