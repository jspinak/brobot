package io.github.jspinak.brobot.startup.orchestration;

import java.io.File;

import org.sikuli.basics.Settings;
import org.sikuli.script.ImagePath;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import io.github.jspinak.brobot.dpi.DPIScalingStrategy;

/**
 * ApplicationContextInitializer that ensures critical Brobot settings are configured before ANY
 * Spring beans are created.
 *
 * <p>This runs at the very beginning of Spring Boot application startup and handles: - ImagePath
 * configuration (prevents "not found" errors during State construction)
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApplicationContextInitializer
        implements org.springframework.context.ApplicationContextInitializer<
                ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.out.println("=== Brobot Early Initialization ===");

        ConfigurableEnvironment env = applicationContext.getEnvironment();

        // CRITICAL: Initialize DPI settings BEFORE ImagePath to ensure patterns are loaded
        // correctly
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
                System.out.println(
                        "[Brobot] Image directory does not exist: "
                                + imageDir.getAbsolutePath()
                                + ". Creating it...");
                imageDir.mkdirs();
            }

            System.out.println(
                    "[Brobot] ImagePath configured. Bundle path: " + ImagePath.getBundlePath());

        } catch (Exception e) {
            System.err.println("[Brobot] Failed to initialize ImagePath: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeDPISettings(ConfigurableEnvironment env) {
        System.out.println("[Brobot] Configuring DPI settings...");

        // Get DPI configuration from properties
        String resizeFactor = env.getProperty("brobot.dpi.resize-factor", "auto");
        String patternSourceStr = env.getProperty("brobot.dpi.pattern.source", "SIKULI_IDE");
        double similarityThreshold =
                Double.parseDouble(env.getProperty("brobot.action.similarity", "0.70"));

        // Configure similarity threshold
        Settings.MinSimilarity = similarityThreshold;
        System.out.println("[Brobot] Pattern source: " + patternSourceStr);
        System.out.println("[Brobot] Similarity threshold: " + similarityThreshold);

        // Configure DPI scaling
        float targetResize = 1.0f;

        if ("auto".equalsIgnoreCase(resizeFactor)) {
            System.out.println("[Brobot] DPI auto-detection enabled");

            // Determine pattern source
            DPIScalingStrategy.PatternSource patternSource;
            try {
                patternSource =
                        DPIScalingStrategy.PatternSource.valueOf(patternSourceStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                patternSource = DPIScalingStrategy.PatternSource.SIKULI_IDE;
                System.out.println(
                        "[Brobot] Unknown pattern source '"
                                + patternSourceStr
                                + "', defaulting to SIKULI_IDE");
            }

            // Use DPIScalingStrategy for optimal configuration
            double displayScale = DPIScalingStrategy.detectDisplayScaling();

            System.out.println(
                    "[Brobot] Display scaling detected: " + (int) (displayScale * 100) + "%");

            // Calculate appropriate resize factor using strategy
            targetResize = DPIScalingStrategy.getOptimalResizeFactor(patternSource);
            System.out.println("[Brobot] Calculated pattern scale factor: " + targetResize);

            // Provide detailed diagnostics based on display scaling
            if (Math.abs(displayScale - 1.25) < 0.01) {
                System.out.println("[Brobot] 125% DPI scaling detected");
                System.out.println("  - Logical 100px → Physical 125px on screen");
                System.out.println("  - Pattern scale factor 0.8 will compensate");
            } else if (Math.abs(displayScale - 1.5) < 0.01) {
                System.out.println("[Brobot] 150% DPI scaling detected");
                System.out.println("  - Logical 100px → Physical 150px on screen");
                System.out.println("  - Pattern scale factor 0.67 will compensate");
            } else if (Math.abs(displayScale - 2.0) < 0.01) {
                System.out.println("[Brobot] 200% DPI scaling detected");
                System.out.println("  - Logical 100px → Physical 200px on screen");
                System.out.println("  - Pattern scale factor 0.5 will compensate");
            } else if (Math.abs(displayScale - 1.0) < 0.01) {
                System.out.println("[Brobot] No DPI scaling detected - patterns match directly");
            } else {
                System.out.println(
                        "[Brobot] Non-standard "
                                + (int) (displayScale * 100)
                                + "% scaling detected");
            }

            // Warn about pattern source implications
            if (patternSource == DPIScalingStrategy.PatternSource.WINDOWS_TOOL) {
                System.out.println("[Brobot] ⚠ Windows tool patterns detected:");
                System.out.println("  - These are in logical pixels");
                System.out.println("  - May need different handling than SikuliX patterns");
            }
        } else {
            // Manual configuration
            try {
                targetResize = Float.parseFloat(resizeFactor);
                System.out.println(
                        "[Brobot] Using manual resize factor from configuration: " + targetResize);
            } catch (NumberFormatException e) {
                System.err.println(
                        "[Brobot] Invalid resize factor '"
                                + resizeFactor
                                + "', defaulting to 1.0 (no scaling)");
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
            System.out.println(
                    "  Pattern scaling: "
                            + (Settings.AlwaysResize < 1.0f ? "DOWNSCALE" : "UPSCALE")
                            + " (patterns will be resized by "
                            + Settings.AlwaysResize
                            + "x during matching)");
        }

        // Print diagnostic information if debug is enabled
        String debugEnabled = env.getProperty("brobot.dpi.debug", "false");
        if ("true".equalsIgnoreCase(debugEnabled)) {
            DPIScalingStrategy.printDiagnostics();
        }
    }
}
