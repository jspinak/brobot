package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Diagnostics component for headless mode detection issues.
 * Provides detailed information about why the system might be running in headless mode.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HeadlessDiagnostics {
    
    private final BrobotLogger brobotLogger;
    
    @EventListener(ApplicationStartedEvent.class)
    public void diagnoseHeadlessMode() {
        log.info("=== Headless Mode Diagnostics ===");
        
        // Check various headless indicators
        boolean awtHeadless = GraphicsEnvironment.isHeadless();
        String headlessProperty = System.getProperty("java.awt.headless");
        String display = System.getenv("DISPLAY");
        
        brobotLogger.log()
            .observation("Headless Mode Diagnostics")
            .metadata("GraphicsEnvironment.isHeadless()", awtHeadless)
            .metadata("java.awt.headless property", headlessProperty)
            .metadata("DISPLAY env variable", display)
            .metadata("OS", System.getProperty("os.name"))
            .metadata("OS version", System.getProperty("os.version"))
            .log();
        
        // Try to get graphics devices
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = ge.getScreenDevices();
            
            brobotLogger.log()
                .observation("Graphics Devices")
                .metadata("deviceCount", devices.length)
                .metadata("defaultScreenDevice", ge.getDefaultScreenDevice().getIDstring())
                .log();
                
            for (int i = 0; i < devices.length; i++) {
                GraphicsDevice device = devices[i];
                DisplayMode dm = device.getDisplayMode();
                log.info("Display {}: {}x{} @ {}Hz", i, dm.getWidth(), dm.getHeight(), dm.getRefreshRate());
            }
        } catch (Exception e) {
            log.error("Failed to enumerate graphics devices: {}", e.getMessage());
            brobotLogger.error("Failed to enumerate graphics devices", e);
        }
        
        // Test screen capture capability
        testScreenCapture();
        
        // Check macOS specific permissions
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            checkMacOSPermissions();
        }
        
        log.info("=== End Headless Diagnostics ===");
    }
    
    private void testScreenCapture() {
        log.info("Testing screen capture capability...");
        
        try {
            Robot robot = new Robot();
            
            // Try to capture a 1x1 pixel region
            BufferedImage testCapture = robot.createScreenCapture(new Rectangle(0, 0, 1, 1));
            
            if (testCapture != null) {
                log.info("✓ Screen capture test PASSED - Robot can capture screen");
                brobotLogger.observation("Screen capture test passed");
            } else {
                log.warn("✗ Screen capture returned null");
                brobotLogger.observation("Screen capture returned null");
            }
        } catch (AWTException e) {
            log.error("✗ Screen capture test FAILED - AWTException: {}", e.getMessage());
            brobotLogger.error("Screen capture failed with AWTException", e);
        } catch (SecurityException e) {
            log.error("✗ Screen capture test FAILED - SecurityException (missing permissions): {}", e.getMessage());
            brobotLogger.error("Screen capture failed - missing permissions", e);
        } catch (Exception e) {
            log.error("✗ Screen capture test FAILED - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            brobotLogger.error("Screen capture failed", e);
        }
    }
    
    private void checkMacOSPermissions() {
        log.info("Checking macOS specific settings...");
        
        // Check if running via SSH or remote session
        String sshConnection = System.getenv("SSH_CONNECTION");
        if (sshConnection != null) {
            log.warn("Running via SSH connection - this may cause headless mode");
            brobotLogger.observation("SSH connection detected: " + sshConnection);
        }
        
        // Check Apple specific properties
        String uiElement = System.getProperty("apple.awt.UIElement");
        String appName = System.getProperty("apple.awt.application.name");
        
        brobotLogger.log()
            .observation("macOS Settings")
            .metadata("apple.awt.UIElement", uiElement)
            .metadata("apple.awt.application.name", appName)
            .metadata("SSH_CONNECTION", sshConnection)
            .log();
        
        // Provide guidance
        if (GraphicsEnvironment.isHeadless()) {
            log.warn("=== macOS Screen Recording Permission Required ===");
            log.warn("To grant permission:");
            log.warn("1. Open System Preferences > Security & Privacy > Privacy");
            log.warn("2. Select 'Screen Recording' from the left panel");
            log.warn("3. Add your Java/Terminal application to the list");
            log.warn("4. Restart the application");
            log.warn("================================================");
            
            brobotLogger.log()
                .observation("macOS Permission Guidance")
                .metadata("action", "Grant Screen Recording permission")
                .metadata("path", "System Preferences > Security & Privacy > Privacy > Screen Recording")
                .log();
        }
    }
}