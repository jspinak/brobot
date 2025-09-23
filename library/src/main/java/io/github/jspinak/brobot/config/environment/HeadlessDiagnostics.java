package io.github.jspinak.brobot.config.environment;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.sikuli.script.Screen;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Diagnostics component for headless mode detection issues. Provides detailed information about why
 * the system might be running in headless mode.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HeadlessDiagnostics {

    private final BrobotLogger brobotLogger;

    @EventListener(ApplicationStartedEvent.class)
    public void diagnoseHeadlessMode() {
        log.debug("=== Headless Mode Diagnostics ===");

        // Check various headless indicators
        boolean awtHeadless = GraphicsEnvironment.isHeadless();
        String headlessProperty = System.getProperty("java.awt.headless");
        String display = System.getenv("DISPLAY");

        brobotLogger
                .builder(LogCategory.SYSTEM)
                .message("Headless Mode Diagnostics")
                .context("GraphicsEnvironment.isHeadless()", awtHeadless)
                .context(
                        "java.awt.headless property",
                        headlessProperty != null ? headlessProperty : "not set")
                .context("DISPLAY env variable", display != null ? display : "not set")
                .context("OS", System.getProperty("os.name"))
                .context("OS version", System.getProperty("os.version"))
                .log();

        // Try to get graphics devices
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = ge.getScreenDevices();

            brobotLogger
                    .builder(LogCategory.SYSTEM)
                    .message("Graphics Devices")
                    .context("deviceCount", devices.length)
                    .context("defaultScreenDevice", ge.getDefaultScreenDevice().getIDstring())
                    .log();

            for (int i = 0; i < devices.length; i++) {
                GraphicsDevice device = devices[i];
                DisplayMode dm = device.getDisplayMode();
                log.debug(
                        "Display {}: {}x{} @ {}Hz",
                        i,
                        dm.getWidth(),
                        dm.getHeight(),
                        dm.getRefreshRate());
            }
        } catch (Exception e) {
            log.error("Failed to enumerate graphics devices: {}", e.getMessage());
            brobotLogger
                    .builder(LogCategory.SYSTEM)
                    .level(LogLevel.ERROR)
                    .error(e)
                    .message("Failed to enumerate graphics devices")
                    .log();
        }

        // Test screen capture capability
        testScreenCapture();

        // Check macOS specific permissions
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            checkMacOSPermissions();
        }

        log.debug("=== End Headless Diagnostics ===");
    }

    private void testScreenCapture() {
        log.debug("Testing screen capture capability...");

        // Check which capture provider is being used
        String captureProvider = System.getProperty("brobot.capture.provider", "AUTO");

        if (captureProvider.toUpperCase().contains("FFMPEG")
                || captureProvider.toUpperCase().contains("JAVACV")) {
            log.debug("Using FFmpeg capture provider - skipping SikuliX screen capture test");
            brobotLogger
                    .builder(LogCategory.SYSTEM)
                    .message("FFmpeg capture provider active - SikuliX test skipped");
            return;
        }

        try {
            Screen screen = new Screen();

            // Try to capture a small region
            org.sikuli.script.Region testRegion = new org.sikuli.script.Region(0, 0, 1, 1);
            BufferedImage testCapture = screen.capture(testRegion).getImage();

            if (testCapture != null) {
                log.debug("✓ Screen capture test PASSED - SikuliX can capture screen");
                brobotLogger.builder(LogCategory.SYSTEM).message("Screen capture test passed");
            } else {
                log.warn("✗ Screen capture returned null");
                brobotLogger.builder(LogCategory.SYSTEM).message("Screen capture returned null");
            }
        } catch (Exception e) {
            log.error(
                    "✗ Screen capture test FAILED - {}: {}",
                    e.getClass().getSimpleName(),
                    e.getMessage());
            brobotLogger
                    .builder(LogCategory.SYSTEM)
                    .level(LogLevel.ERROR)
                    .error(e)
                    .message("Screen capture failed")
                    .log();
        }
    }

    private void checkMacOSPermissions() {
        log.debug("Checking macOS specific settings...");

        // Check if running via SSH or remote session
        String sshConnection = System.getenv("SSH_CONNECTION");
        if (sshConnection != null) {
            log.warn("Running via SSH connection - this may cause headless mode");
            brobotLogger
                    .builder(LogCategory.SYSTEM)
                    .message("SSH connection detected: " + sshConnection);
        }

        // Check Apple specific properties
        String uiElement = System.getProperty("apple.awt.UIElement");
        String appName = System.getProperty("apple.awt.application.name");

        brobotLogger
                .builder(LogCategory.SYSTEM)
                .message("macOS Settings")
                .context("apple.awt.UIElement", uiElement)
                .context("apple.awt.application.name", appName)
                .context("SSH_CONNECTION", sshConnection)
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

            brobotLogger
                    .builder(LogCategory.SYSTEM)
                    .message("macOS Permission Guidance")
                    .context("action", "Grant Screen Recording permission")
                    .context(
                            "path",
                            "System Preferences > Security & Privacy > Privacy > Screen Recording")
                    .log();
        }
    }
}
