package io.github.jspinak.brobot.config.core;

import java.io.File;

import jakarta.annotation.PostConstruct;

import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Ensures image paths are initialized as early as possible in the Spring Boot lifecycle. This
 * component runs before state processing to ensure images can be found.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EarlyImagePathInitializer {

    @Value("${brobot.core.image-path:images}")
    private String primaryImagePath;

    @Autowired(required = false)
    private BrobotProperties brobotProperties;

    /**
     * Initialize image paths as early as possible after property resolution. This runs after @Value
     * injection but before most other beans are created.
     */
    @PostConstruct
    public void initializeImagePaths() {
        log.info("Early image path initialization starting...");

        // Get the configured image path
        String imagePath = primaryImagePath;
        if (brobotProperties != null && brobotProperties.getCore() != null) {
            String configuredPath = brobotProperties.getCore().getImagePath();
            if (configuredPath != null && !configuredPath.isEmpty()) {
                imagePath = configuredPath;
            }
        }

        // Ensure we have a valid path
        if (imagePath == null || imagePath.isEmpty()) {
            imagePath = "images";
        }

        // Remove trailing slash for consistency
        if (imagePath.endsWith("/") || imagePath.endsWith("\\")) {
            imagePath = imagePath.substring(0, imagePath.length() - 1);
        }

        log.info("Setting SikuliX ImagePath to: {}", imagePath);

        try {
            // Set the SikuliX bundle path immediately
            ImagePath.setBundlePath(imagePath);

            // Also add the path to ensure it's searchable
            ImagePath.add(imagePath);

            // Verify the path exists
            File imageDir = new File(imagePath);
            if (!imageDir.exists()) {
                log.warn(
                        "Image directory does not exist: {}. Creating it...",
                        imageDir.getAbsolutePath());
                imageDir.mkdirs();
            }

            // Log the actual paths being used
            log.info("SikuliX ImagePath configured:");
            log.info("  Bundle path: {}", ImagePath.getBundlePath());

        } catch (Exception e) {
            log.error("Failed to initialize SikuliX ImagePath: {}", e.getMessage(), e);
        }
    }

    /**
     * Additional initialization on context refresh to ensure paths are set even if the initial
     * attempt failed.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed() {
        // Double-check that paths are set
        String bundlePath = ImagePath.getBundlePath();
        if (bundlePath == null || bundlePath.isEmpty()) {
            log.warn("ImagePath not set after context refresh, attempting to set again...");
            initializeImagePaths();
        }
    }
}
