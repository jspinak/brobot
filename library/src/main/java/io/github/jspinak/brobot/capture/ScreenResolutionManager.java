package io.github.jspinak.brobot.capture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import jakarta.annotation.PostConstruct;
import java.awt.*;

/**
 * Manages screen resolution information based on the active capture provider.
 * This ensures all components use consistent coordinate systems.
 * 
 * Key principle: Regions should be defined in the same coordinate space
 * as the images being captured and searched.
 */
@Slf4j
@Component
@Getter
public class ScreenResolutionManager {
    
    @Value("${brobot.capture.provider:JAVACV_FFMPEG}")
    private String captureProviderName;
    
    @Autowired(required = false)
    private UnifiedCaptureService captureService;
    
    private int screenWidth;
    private int screenHeight;
    private boolean isPhysicalResolution;
    private double dpiScaleX = 1.0;
    private double dpiScaleY = 1.0;
    
    // Cached values for common resolutions
    private static final int PHYSICAL_WIDTH_1080P = 1920;
    private static final int PHYSICAL_HEIGHT_1080P = 1080;
    private static final int LOGICAL_WIDTH_125_PERCENT = 1536;
    private static final int LOGICAL_HEIGHT_125_PERCENT = 864;
    
    @PostConstruct
    public void initialize() {
        detectResolution();
        
        // Only initialize ScreenDimensions if not already done by EarlyScreenDimensionsInitializer
        if (!ScreenDimensions.isInitialized()) {
            ScreenDimensions.initialize(captureProviderName, screenWidth, screenHeight);
            log.info("ScreenDimensions initialized by ScreenResolutionManager");
        } else {
            // Verify the dimensions match what we detected
            if (ScreenDimensions.getWidth() != screenWidth || ScreenDimensions.getHeight() != screenHeight) {
                log.warn("ScreenDimensions mismatch! Already initialized as {}x{}, but detected {}x{}",
                        ScreenDimensions.getWidth(), ScreenDimensions.getHeight(), 
                        screenWidth, screenHeight);
            }
        }
        
        log.info("=== Screen Resolution Manager Initialized ===");
        log.info("Capture Provider: {}", captureProviderName);
        log.info("Screen Resolution: {}x{}", screenWidth, screenHeight);
        log.info("Resolution Type: {}", isPhysicalResolution ? "PHYSICAL" : "LOGICAL");
        if (Math.abs(dpiScaleX - 1.0) > 0.01) {
            log.info("DPI Scale: {}x{}", String.format("%.2f", dpiScaleX), String.format("%.2f", dpiScaleY));
        }
        log.info("==========================================");
    }
    
    private void detectResolution() {
        String provider = captureProviderName.toUpperCase();
        
        // Check if we're in mock mode or headless environment
        if (FrameworkSettings.mock || provider.contains("MOCK") || GraphicsEnvironment.isHeadless()) {
            // Use default dimensions for mock/headless mode
            screenWidth = 1920;
            screenHeight = 1080;
            isPhysicalResolution = false;
            log.debug("Using mock/headless resolution: {}x{}", screenWidth, screenHeight);
            return;
        }
        
        // Determine resolution based on capture provider
        if (provider.contains("JAVACV_FFMPEG") || 
            provider.contains("FFMPEG") || 
            provider.contains("ROBOT")) {
            // These providers capture at physical resolution
            detectPhysicalResolution();
            isPhysicalResolution = true;
        } else {
            // SikuliX and AUTO capture at logical resolution
            detectLogicalResolution();
            isPhysicalResolution = false;
        }
    }
    
    private void detectPhysicalResolution() {
        // For physical resolution providers, we need to detect the actual screen size
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        
        // Try to get physical resolution
        // On Windows with DPI scaling, this might still return logical dimensions
        // So we check for known patterns
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        
        // Check if we're getting logical dimensions when we expect physical
        if (screenSize.width == LOGICAL_WIDTH_125_PERCENT && 
            screenSize.height == LOGICAL_HEIGHT_125_PERCENT) {
            // This is likely 125% DPI scaling - use known physical resolution
            screenWidth = PHYSICAL_WIDTH_1080P;
            screenHeight = PHYSICAL_HEIGHT_1080P;
            dpiScaleX = (double) PHYSICAL_WIDTH_1080P / LOGICAL_WIDTH_125_PERCENT;
            dpiScaleY = (double) PHYSICAL_HEIGHT_1080P / LOGICAL_HEIGHT_125_PERCENT;
            log.debug("Detected 125% DPI scaling, using physical resolution: {}x{}", 
                     screenWidth, screenHeight);
        } else {
            // Use detected dimensions
            screenWidth = screenSize.width;
            screenHeight = screenSize.height;
            
            // Try to detect DPI scale
            try {
                // This might work on some systems
                double scaleX = gd.getDefaultConfiguration().getDefaultTransform().getScaleX();
                double scaleY = gd.getDefaultConfiguration().getDefaultTransform().getScaleY();
                if (scaleX > 1.0 || scaleY > 1.0) {
                    // We have DPI scaling
                    screenWidth = (int) Math.round(screenSize.width * scaleX);
                    screenHeight = (int) Math.round(screenSize.height * scaleY);
                    dpiScaleX = scaleX;
                    dpiScaleY = scaleY;
                    log.debug("Detected DPI scale: {}x{}, adjusted to physical: {}x{}", 
                             scaleX, scaleY, screenWidth, screenHeight);
                }
            } catch (Exception e) {
                log.debug("Could not detect DPI scale: {}", e.getMessage());
            }
        }
    }
    
    private void detectLogicalResolution() {
        // For logical resolution, use standard AWT/Swing detection
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;
        
        // No DPI scaling for logical resolution
        dpiScaleX = 1.0;
        dpiScaleY = 1.0;
    }
    
    /**
     * Creates a region in the current capture coordinate space.
     * This ensures regions are always defined correctly for the active capture provider.
     */
    public Rectangle createRegion(int x, int y, int width, int height) {
        // Regions are created in the capture coordinate space
        // No conversion needed - just validation
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > screenWidth) width = screenWidth - x;
        if (y + height > screenHeight) height = screenHeight - y;
        
        return new Rectangle(x, y, width, height);
    }
    
    /**
     * Creates a region for the lower-left quarter of the screen.
     * Commonly used for finding UI elements like prompts.
     */
    public Rectangle getLowerLeftQuarter() {
        int halfWidth = screenWidth / 2;
        int halfHeight = screenHeight / 2;
        return new Rectangle(0, halfHeight, halfWidth, halfHeight);
    }
    
    /**
     * Creates a region for the lower-right quarter of the screen.
     */
    public Rectangle getLowerRightQuarter() {
        int halfWidth = screenWidth / 2;
        int halfHeight = screenHeight / 2;
        return new Rectangle(halfWidth, halfHeight, halfWidth, halfHeight);
    }
    
    /**
     * Creates a region for the upper-left quarter of the screen.
     */
    public Rectangle getUpperLeftQuarter() {
        int halfWidth = screenWidth / 2;
        int halfHeight = screenHeight / 2;
        return new Rectangle(0, 0, halfWidth, halfHeight);
    }
    
    /**
     * Creates a region for the upper-right quarter of the screen.
     */
    public Rectangle getUpperRightQuarter() {
        int halfWidth = screenWidth / 2;
        int halfHeight = screenHeight / 2;
        return new Rectangle(halfWidth, 0, halfWidth, halfHeight);
    }
    
    /**
     * Returns the full screen region.
     */
    public Rectangle getFullScreen() {
        return new Rectangle(0, 0, screenWidth, screenHeight);
    }
    
    /**
     * Checks if the current configuration uses physical resolution.
     */
    public boolean isUsingPhysicalResolution() {
        return isPhysicalResolution;
    }
    
    /**
     * Gets a descriptive string of the current resolution configuration.
     */
    public String getResolutionInfo() {
        return String.format("%dx%d (%s resolution, provider: %s)", 
            screenWidth, screenHeight,
            isPhysicalResolution ? "PHYSICAL" : "LOGICAL",
            captureProviderName);
    }
}