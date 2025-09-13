package io.github.jspinak.brobot.config.core;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Early initializer for image loading infrastructure. This ensures ImagePathManager is configured
 * before any State beans are created.
 */
@Slf4j
@Component
@Order(1) // Ensure this runs very early
@RequiredArgsConstructor
public class ImageLoadingInitializer {

    private final ImagePathManager imagePathManager;

    @Value("${brobot.screenshot.path:images}")
    private String imagePath;

    @PostConstruct
    public void initializeImageLoading() {
        log.info("=== EARLY IMAGE LOADING INITIALIZATION ===");
        log.info("Initializing ImagePathManager with path: {}", imagePath);

        // Initialize the ImagePathManager early
        imagePathManager.initialize(imagePath);

        // Store resolved path as system property for other components
        if (!imagePathManager.getConfiguredPaths().isEmpty()) {
            String resolvedPath = imagePathManager.getConfiguredPaths().iterator().next();
            System.setProperty("brobot.resolved.image.path", resolvedPath);
            log.info("Resolved image path set to: {}", resolvedPath);
        }

        log.info("Image loading infrastructure initialized");
        log.info("==========================================");
    }
}
