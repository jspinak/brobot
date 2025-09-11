package io.github.jspinak.brobot.capture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.capture.provider.CaptureProvider;
import io.github.jspinak.brobot.capture.provider.FFmpegCaptureProvider;
import io.github.jspinak.brobot.capture.provider.RobotCaptureProvider;
import io.github.jspinak.brobot.capture.provider.SikuliXCaptureProvider;

/**
 * Central service for screen capture operations in Brobot.
 *
 * <p>This service manages different capture providers and automatically selects the best one based
 * on configuration and availability.
 *
 * <p>Priority order: 1. User-configured provider (if available) 2. FFmpeg (if available) - for
 * physical resolution 3. SikuliX (fallback) - may be logical resolution in Java 21
 *
 * @since 1.1.0
 */
@Service
public class BrobotCaptureService {

    @Autowired(required = false)
    private List<CaptureProvider> availableProviders;

    @Value("${brobot.capture.provider:AUTO}")
    private String configuredProvider;

    @Value("${brobot.capture.prefer-physical:true}")
    private boolean preferPhysicalResolution;

    @Value("${brobot.capture.fallback-enabled:true}")
    private boolean fallbackEnabled;

    private CaptureProvider activeProvider;
    private Map<String, CaptureProvider> providerMap;

    @PostConstruct
    public void init() {
        providerMap = new HashMap<>();

        if (availableProviders != null) {
            for (CaptureProvider provider : availableProviders) {
                providerMap.put(provider.getName().toUpperCase(), provider);
            }
        }

        selectProvider();
        reportConfiguration();
    }

    /** Captures the entire primary screen. */
    public BufferedImage captureScreen() throws IOException {
        return getActiveProvider().captureScreen();
    }

    /** Captures a specific screen. */
    public BufferedImage captureScreen(int screenId) throws IOException {
        return getActiveProvider().captureScreen(screenId);
    }

    /** Captures a region of the primary screen. */
    public BufferedImage captureRegion(Rectangle region) throws IOException {
        return getActiveProvider().captureRegion(region);
    }

    /** Captures a region of a specific screen. */
    public BufferedImage captureRegion(int screenId, Rectangle region) throws IOException {
        return getActiveProvider().captureRegion(screenId, region);
    }

    /** Captures a region defined by a Brobot/SikuliX Region object. */
    public BufferedImage captureRegion(org.sikuli.script.Region region) throws IOException {
        Rectangle rect = new Rectangle(region.x, region.y, region.w, region.h);
        // Determine which screen contains this region
        int screenId = getScreenForRegion(rect);
        return captureRegion(screenId, rect);
    }

    /** Gets the currently active capture provider. */
    public CaptureProvider getActiveProvider() {
        if (activeProvider == null) {
            throw new IllegalStateException("No capture provider available");
        }
        return activeProvider;
    }

    /** Switches to a different capture provider. */
    public void setProvider(String providerName) {
        CaptureProvider provider = providerMap.get(providerName.toUpperCase());

        if (provider == null) {
            throw new IllegalArgumentException("Provider not found: " + providerName);
        }

        if (!provider.isAvailable()) {
            throw new IllegalStateException("Provider not available: " + providerName);
        }

        activeProvider = provider;
        System.out.println("[BrobotCapture] Switched to provider: " + provider.getName());
    }

    /** Selects the best available provider based on configuration and availability. */
    private void selectProvider() {
        selectProvider(true);
    }

    /**
     * Selects the best available provider based on configuration and availability.
     *
     * @param allowCreateDefaults if true, will create default providers if none exist
     */
    private void selectProvider(boolean allowCreateDefaults) {
        // Try configured provider first
        if (!"AUTO".equalsIgnoreCase(configuredProvider)) {
            CaptureProvider provider = providerMap.get(configuredProvider.toUpperCase());
            if (provider != null && provider.isAvailable()) {
                activeProvider = provider;
                return;
            }
            System.err.println(
                    "[BrobotCapture] Configured provider not available: " + configuredProvider);
        }

        // Auto-select based on preferences
        if (preferPhysicalResolution) {
            // Prefer Robot for physical resolution (through scaling)
            CaptureProvider robot = providerMap.get("ROBOT");
            if (robot != null && robot.isAvailable()) {
                activeProvider = robot;
                return;
            }

            // Then try FFmpeg for true physical resolution
            CaptureProvider ffmpeg = providerMap.get("FFMPEG");
            if (ffmpeg != null && ffmpeg.isAvailable()) {
                activeProvider = ffmpeg;
                return;
            }
        }

        // Try any available provider (prefer JavaCV FFmpeg for physical resolution)
        String[] preferredOrder = {"JAVACV_FFMPEG", "ROBOT", "FFMPEG", "SIKULIX"};
        for (String name : preferredOrder) {
            CaptureProvider provider = providerMap.get(name);
            if (provider != null && provider.isAvailable()) {
                activeProvider = provider;
                return;
            }
        }

        // If no providers in Spring context, create defaults (only once)
        if (activeProvider == null && fallbackEnabled && allowCreateDefaults) {
            createDefaultProviders();
            selectProvider(false); // Retry without allowing another create
        }
    }

    /** Creates default providers if none were found via Spring. */
    private void createDefaultProviders() {
        if (providerMap.isEmpty()) {
            // Add SikuliX as primary provider for compatibility
            SikuliXCaptureProvider sikuli = new SikuliXCaptureProvider();
            providerMap.put("SIKULIX", sikuli);

            // Add Robot as secondary provider
            RobotCaptureProvider robot = new RobotCaptureProvider();
            providerMap.put("ROBOT", robot);

            // Try FFmpeg if available
            FFmpegCaptureProvider ffmpeg = new FFmpegCaptureProvider();
            if (ffmpeg.isAvailable()) {
                providerMap.put("FFMPEG", ffmpeg);
            }
        }
    }

    /** Determines which screen contains the given region. */
    private int getScreenForRegion(Rectangle region) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();

        Point center = new Point(region.x + region.width / 2, region.y + region.height / 2);

        for (int i = 0; i < devices.length; i++) {
            Rectangle bounds = devices[i].getDefaultConfiguration().getBounds();
            if (bounds.contains(center)) {
                return i;
            }
        }

        // Default to primary screen
        return 0;
    }

    /** Reports the current capture configuration. */
    private void reportConfiguration() {
        System.out.println("\n=== Brobot Capture Service Configuration ===");
        System.out.println("Configured Provider: " + configuredProvider);
        System.out.println("Prefer Physical Resolution: " + preferPhysicalResolution);
        System.out.println("Fallback Enabled: " + fallbackEnabled);

        System.out.println("\nAvailable Providers:");
        for (Map.Entry<String, CaptureProvider> entry : providerMap.entrySet()) {
            CaptureProvider provider = entry.getValue();
            String status = provider.isAvailable() ? "✓" : "✗";
            String resolution = provider.getResolutionType().toString();
            System.out.println(
                    String.format(
                            "  %s %s (%s resolution)", status, provider.getName(), resolution));
        }

        if (activeProvider != null) {
            System.out.println(
                    "\nActive Provider: "
                            + activeProvider.getName()
                            + " ("
                            + activeProvider.getResolutionType()
                            + " resolution)");
        } else {
            System.err.println("\n⚠ WARNING: No capture provider available!");
        }

        System.out.println("=========================================\n");
    }

    /** Gets information about all available providers. */
    public String getProvidersInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Capture Providers Status:\n");

        for (CaptureProvider provider : providerMap.values()) {
            info.append(
                    String.format(
                            "- %s: %s (%s resolution)\n",
                            provider.getName(),
                            provider.isAvailable() ? "Available" : "Not Available",
                            provider.getResolutionType()));
        }

        if (activeProvider != null) {
            info.append("\nActive: ").append(activeProvider.getName());
        }

        return info.toString();
    }
}
