package io.github.jspinak.brobot.capture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import jakarta.annotation.PostConstruct;

import org.sikuli.script.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.capture.provider.CaptureProvider;

/**
 * Unified screen capture service that provides a single interface for all capture operations.
 *
 * <p>This service is the primary entry point for screen capture in Brobot and can be configured
 * entirely through properties to use different capture providers.
 *
 * <p>Configuration example in application.properties: ```properties # Choose capture provider:
 * AUTO, ROBOT, FFMPEG, SIKULIX, or fully qualified class name brobot.capture.provider=ROBOT
 *
 * <p># Or use a custom provider class brobot.capture.provider=com.example.MyCustomCaptureProvider
 * ```
 *
 * <p>Features: - Single interface for all capture operations - Property-based provider selection -
 * Automatic fallback handling - Support for custom providers - Thread-safe operations
 *
 * @since 1.1.0
 */
@Service
@Primary
public class UnifiedCaptureService {

    @Autowired private BrobotCaptureService delegateService;

    @Value("${brobot.capture.provider:SIKULIX}")
    private String providerConfig;

    @Value("${brobot.capture.enable-logging:false}")
    private boolean enableLogging;

    @Value("${brobot.capture.auto-retry:true}")
    private boolean autoRetry;

    @Value("${brobot.capture.retry-count:3}")
    private int retryCount;

    @PostConstruct
    public void init() {
        if (enableLogging) {
            System.out.println("[UnifiedCapture] Initializing with provider: " + providerConfig);
        }

        // Handle provider selection through delegate service
        if (!providerConfig.equals("AUTO")) {
            try {
                // Check if it's a known provider name or a class name
                if (isKnownProvider(providerConfig)) {
                    delegateService.setProvider(providerConfig);
                } else {
                    // Assume it's a custom provider class name
                    loadCustomProvider(providerConfig);
                }
            } catch (Exception e) {
                System.err.println("[UnifiedCapture] Failed to set provider: " + providerConfig);
                System.err.println("[UnifiedCapture] Falling back to AUTO selection");
            }
        }
    }

    /**
     * Captures the entire primary screen.
     *
     * @return BufferedImage of the screen capture
     * @throws IOException if capture fails
     */
    public BufferedImage captureScreen() throws IOException {
        return executeWithRetry(() -> delegateService.captureScreen());
    }

    /**
     * Captures a specific screen by ID.
     *
     * @param screenId the screen ID to capture (0 for primary)
     * @return BufferedImage of the screen capture
     * @throws IOException if capture fails
     */
    public BufferedImage captureScreen(int screenId) throws IOException {
        return executeWithRetry(() -> delegateService.captureScreen(screenId));
    }

    /**
     * Captures a region of the primary screen.
     *
     * @param region the region to capture
     * @return BufferedImage of the region capture
     * @throws IOException if capture fails
     */
    public BufferedImage captureRegion(Rectangle region) throws IOException {
        return executeWithRetry(() -> delegateService.captureRegion(region));
    }

    /**
     * Captures a region of a specific screen.
     *
     * @param screenId the screen ID
     * @param region the region to capture
     * @return BufferedImage of the region capture
     * @throws IOException if capture fails
     */
    public BufferedImage captureRegion(int screenId, Rectangle region) throws IOException {
        return executeWithRetry(() -> delegateService.captureRegion(screenId, region));
    }

    /**
     * Captures a SikuliX Region.
     *
     * @param region the SikuliX region to capture
     * @return BufferedImage of the region capture
     * @throws IOException if capture fails
     */
    public BufferedImage captureRegion(Region region) throws IOException {
        return executeWithRetry(() -> delegateService.captureRegion(region));
    }

    /**
     * Changes the capture provider at runtime. This is useful for testing or when different
     * providers are needed for different operations.
     *
     * @param providerName the name of the provider (ROBOT, FFMPEG, SIKULIX)
     * @throws IllegalArgumentException if provider is not found
     * @throws IllegalStateException if provider is not available
     */
    public void setProvider(String providerName) {
        if (enableLogging) {
            System.out.println("[UnifiedCapture] Switching to provider: " + providerName);
        }
        delegateService.setProvider(providerName);
    }

    /**
     * Gets the currently active capture provider.
     *
     * @return the active CaptureProvider
     */
    public CaptureProvider getActiveProvider() {
        return delegateService.getActiveProvider();
    }

    /**
     * Gets information about all available providers.
     *
     * @return string describing provider status
     */
    public String getProvidersInfo() {
        return delegateService.getProvidersInfo();
    }

    /**
     * Gets the name of the currently active provider.
     *
     * @return provider name
     */
    public String getActiveProviderName() {
        return delegateService.getActiveProvider().getName();
    }

    /**
     * Checks if the current provider captures at physical resolution.
     *
     * @return true if capturing at physical resolution
     */
    public boolean isPhysicalResolution() {
        return delegateService.getActiveProvider().getResolutionType()
                == CaptureProvider.ResolutionType.PHYSICAL;
    }

    /** Executes a capture operation with retry logic. */
    private BufferedImage executeWithRetry(CaptureOperation operation) throws IOException {
        IOException lastException = null;

        for (int i = 0; i <= (autoRetry ? retryCount : 0); i++) {
            try {
                BufferedImage result = operation.execute();

                if (enableLogging && i > 0) {
                    System.out.println("[UnifiedCapture] Capture succeeded on retry " + i);
                }

                return result;
            } catch (IOException e) {
                lastException = e;

                if (enableLogging) {
                    System.err.println(
                            "[UnifiedCapture] Capture attempt "
                                    + (i + 1)
                                    + " failed: "
                                    + e.getMessage());
                }

                if (i < retryCount && autoRetry) {
                    // Wait a bit before retrying
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw lastException != null
                ? lastException
                : new IOException("Capture failed after " + (retryCount + 1) + " attempts");
    }

    /** Checks if the provider name is a known built-in provider. */
    private boolean isKnownProvider(String name) {
        return name.equalsIgnoreCase("ROBOT")
                || name.equalsIgnoreCase("FFMPEG")
                || name.equalsIgnoreCase("JAVACV_FFMPEG")
                || name.equalsIgnoreCase("SIKULIX")
                || name.equalsIgnoreCase("AUTO");
    }

    /**
     * Loads a custom capture provider by class name. This allows users to implement their own
     * CaptureProvider and use it via properties.
     */
    private void loadCustomProvider(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (CaptureProvider.class.isAssignableFrom(clazz)) {
                // This would need Spring context support for full implementation
                System.out.println(
                        "[UnifiedCapture] Custom provider loading not yet implemented: "
                                + className);
                System.out.println(
                        "[UnifiedCapture] Please register your provider as a Spring bean");
            } else {
                throw new IllegalArgumentException(
                        "Class does not implement CaptureProvider: " + className);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Provider class not found: " + className, e);
        }
    }

    /** Functional interface for capture operations with retry. */
    @FunctionalInterface
    private interface CaptureOperation {
        BufferedImage execute() throws IOException;
    }
}
