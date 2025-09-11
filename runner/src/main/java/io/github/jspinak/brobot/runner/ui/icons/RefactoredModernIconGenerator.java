package io.github.jspinak.brobot.runner.ui.icons;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.scene.image.Image;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.ui.icons.services.IconCacheService;
import io.github.jspinak.brobot.runner.ui.icons.services.IconDrawingService;
import io.github.jspinak.brobot.runner.ui.icons.services.IconRendererService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Refactored icon generator that orchestrates between services. Simplified from 610 lines to a thin
 * orchestrator.
 */
@Slf4j
@Component
@Data
@RequiredArgsConstructor
public class RefactoredModernIconGenerator {

    private final IconCacheService cacheService;
    private final IconDrawingService drawingService;
    private final IconRendererService rendererService;

    /** Configuration for icon generation. */
    public static class IconGeneratorConfiguration {
        private boolean cacheEnabled = true;
        private long renderTimeout = 5000; // milliseconds
        private boolean logPerformance = false;

        public static IconGeneratorConfigurationBuilder builder() {
            return new IconGeneratorConfigurationBuilder();
        }

        public static class IconGeneratorConfigurationBuilder {
            private IconGeneratorConfiguration config = new IconGeneratorConfiguration();

            public IconGeneratorConfigurationBuilder cacheEnabled(boolean enabled) {
                config.cacheEnabled = enabled;
                return this;
            }

            public IconGeneratorConfigurationBuilder renderTimeout(long timeout) {
                config.renderTimeout = timeout;
                return this;
            }

            public IconGeneratorConfigurationBuilder logPerformance(boolean log) {
                config.logPerformance = log;
                return this;
            }

            public IconGeneratorConfiguration build() {
                return config;
            }
        }
    }

    private IconGeneratorConfiguration configuration = IconGeneratorConfiguration.builder().build();

    /** Sets the configuration. */
    public void setConfiguration(IconGeneratorConfiguration configuration) {
        this.configuration = configuration;
        log.info("Icon generator configured");
    }

    /** Gets or generates an icon. */
    public Image getIcon(String iconName, int size) {
        String key = IconCacheService.generateKey(iconName, size);

        // Check cache first
        if (configuration.cacheEnabled) {
            Image cached = cacheService.get(key);
            if (cached != null) {
                log.trace("Icon cache hit for: {}", key);
                return cached;
            }
        }

        // Generate icon
        if (Platform.isFxApplicationThread()) {
            // On FX thread - can render synchronously
            return generateIconSync(iconName, size, key);
        } else {
            // Off FX thread - must render asynchronously
            return generateIconAsync(iconName, size, key);
        }
    }

    /** Generates an icon synchronously (must be on FX thread). */
    private Image generateIconSync(String iconName, int size, String key) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("Generating icon '{}' synchronously", iconName);

            // Get the drawer for this icon
            IconRendererService.IconDrawer drawer = drawingService.getDrawer(iconName);

            // Render the icon
            Image icon = rendererService.renderIconSync(size, drawer);

            // Cache it
            if (configuration.cacheEnabled && icon != null) {
                cacheService.put(key, icon);
            }

            if (configuration.logPerformance) {
                long duration = System.currentTimeMillis() - startTime;
                log.debug("Icon '{}' generated in {} ms", iconName, duration);
            }

            return icon;

        } catch (Exception e) {
            log.error("Error generating icon: {}", iconName, e);
            return rendererService.createPlaceholderIcon(size);
        }
    }

    /** Generates an icon asynchronously. */
    private Image generateIconAsync(String iconName, int size, String key) {
        log.warn("Icon generation requested off FX thread for '{}'", iconName);

        try {
            // Get the drawer
            IconRendererService.IconDrawer drawer = drawingService.getDrawer(iconName);

            // Render asynchronously
            CompletableFuture<Image> future = rendererService.renderIcon(size, drawer);

            // Try to get the result with timeout
            try {
                Image icon = future.get(configuration.renderTimeout, TimeUnit.MILLISECONDS);

                // Cache it
                if (configuration.cacheEnabled && icon != null) {
                    cacheService.put(key, icon);
                }

                return icon;

            } catch (TimeoutException e) {
                log.warn("Icon generation timed out for '{}', using placeholder", iconName);
                future.cancel(true);
                return rendererService.createPlaceholderIcon(size);
            }

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error generating icon asynchronously: {}", iconName, e);
            return rendererService.createPlaceholderIcon(size);
        }
    }

    /** Preloads a set of commonly used icons. */
    public CompletableFuture<Void> preloadIcons(String[] iconNames, int[] sizes) {
        return CompletableFuture.runAsync(
                () -> {
                    Platform.runLater(
                            () -> {
                                for (String iconName : iconNames) {
                                    for (int size : sizes) {
                                        try {
                                            getIcon(iconName, size);
                                        } catch (Exception e) {
                                            log.warn(
                                                    "Failed to preload icon: {} at size {}",
                                                    iconName,
                                                    size,
                                                    e);
                                        }
                                    }
                                }
                                log.info(
                                        "Preloaded {} icon types in {} sizes",
                                        iconNames.length,
                                        sizes.length);
                            });
                });
    }

    /** Clears the icon cache. */
    public void clearCache() {
        cacheService.clear();
        log.info("Icon cache cleared");
    }

    /** Gets cache statistics. */
    public IconCacheService.CacheStatistics getCacheStatistics() {
        return cacheService.getStatistics();
    }

    /** Checks if an icon type is supported. */
    public boolean isIconSupported(String iconName) {
        return drawingService.hasDrawer(iconName);
    }

    /** Gets the current cache size. */
    public int getCacheSize() {
        return cacheService.size();
    }
}
