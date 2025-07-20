package io.github.jspinak.brobot.runner.ui.icons;

import lombok.Data;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import jakarta.annotation.PostConstruct;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages application icons and provides easy access to them.
 */
@Component
@Data
public class IconRegistry {
    private static final Logger logger = LoggerFactory.getLogger(IconRegistry.class);

    private final EventBus eventBus;
    private final ModernIconGenerator iconGenerator;

    // Paths to icon resources
    private static final String ICON_BASE_PATH = "/icons/";

    // Map of loaded icons
    private final Map<String, Image> iconCache = new HashMap<>();

    // Available sizes
    private static final int[] ICON_SIZES = { 16, 24, 32, 48, 64 };

    @Autowired
    public IconRegistry(EventBus eventBus, ModernIconGenerator iconGenerator) {
        this.eventBus = eventBus;
        this.iconGenerator = iconGenerator;
    }

    @PostConstruct
    public void initialize() {
        // Don't preload icons in PostConstruct - wait until FX thread is ready
        // Icons will be loaded on-demand when first requested from FX thread
        
        logger.info("IconRegistry initialized");
        eventBus.publish(LogEvent.info(this, "Icon registry initialized", "Resources"));
        
        // Preload common icons on FX thread
        if (javafx.application.Platform.isFxApplicationThread()) {
            preloadCommonIcons();
        } else {
            javafx.application.Platform.runLater(this::preloadCommonIcons);
        }
    }

    /**
     * Preloads common icons into the cache.
     */
    private void preloadCommonIcons() {
        logger.info("Preloading common icons on thread: {}", Thread.currentThread().getName());
        
        // Tab icons
        loadIcon("settings", 16);    // Configuration tab
        loadIcon("play", 16);        // Automation tab
        loadIcon("grid", 16);        // Resources tab
        loadIcon("list", 16);        // Logs tab
        loadIcon("chart", 16);       // Showcase tab
        
        // Button icons
        loadIcon("add", 16);
        loadIcon("folder", 16);
        loadIcon("folder-open", 16);
        loadIcon("refresh", 16);
        loadIcon("pause", 16);
        loadIcon("stop", 16);
        loadIcon("window", 16);
        loadIcon("keyboard", 16);
        loadIcon("moon", 16);
        loadIcon("sun", 16);
        
        // Common action icons
        loadIcon("edit", 16);
        loadIcon("delete", 16);
        loadIcon("save", 16);
        loadIcon("cancel", 16);
        loadIcon("search", 16);

        // Status icons
        loadIcon("success", 16);
        loadIcon("warning", 16);
        loadIcon("error", 16);
        loadIcon("info", 16);
        loadIcon("bug", 16);

        logger.info("Preloaded {} common icons", iconCache.size());
    }

    /**
     * Loads an icon into the cache.
     *
     * @param iconName The name of the icon (without extension)
     * @param size The size of the icon
     * @return true if the icon was loaded successfully, false otherwise
     */
    public boolean loadIcon(String iconName, int size) {
        String key = getIconKey(iconName, size);

        if (iconCache.containsKey(key)) {
            return true;
        }

        try {
            String path = ICON_BASE_PATH + size + "/" + iconName + ".png";
            InputStream is = getClass().getResourceAsStream(path);

            if (is == null) {
                // Try without size subdirectory
                path = ICON_BASE_PATH + iconName + ".png";
                is = getClass().getResourceAsStream(path);
                
                if (is == null) {
                    // Try SVG if PNG not found
                    path = ICON_BASE_PATH + "svg/" + iconName + ".svg";
                    is = getClass().getResourceAsStream(path);

                    if (is == null) {
                        // Try to generate the icon programmatically
                        logger.debug("Icon not found in resources, generating: {}", iconName);
                        Image generatedIcon = iconGenerator.getIcon(iconName, size);
                        if (generatedIcon != null) {
                            iconCache.put(key, generatedIcon);
                            logger.info("Successfully generated icon: {} at size {}", iconName, size);
                            return true;
                        } else {
                            logger.warn("Failed to generate icon: {} at size {}", iconName, size);
                            return false;
                        }
                    }
                }
            }

            Image icon = new Image(is);
            iconCache.put(key, icon);

            logger.debug("Loaded icon: {}", key);
            return true;
        } catch (Exception e) {
            logger.error("Error loading icon: {}", iconName, e);
            return false;
        }
    }

    /**
     * Gets an icon from the registry.
     *
     * @param iconName The name of the icon
     * @param size The size of the icon
     * @return The icon, or null if not found
     */
    public Image getIcon(String iconName, int size) {
        String key = getIconKey(iconName, size);

        // Check if icon is already loaded
        Image icon = iconCache.get(key);

        // If not loaded, try to load it
        if (icon == null) {
            if (loadIcon(iconName, size)) {
                icon = iconCache.get(key);
            } else {
                // If icon not found, try to find the closest size
                icon = findClosestSizeIcon(iconName, size);
            }
        }

        return icon;
    }

    /**
     * Creates an ImageView for an icon.
     *
     * @param iconName The name of the icon
     * @param size The size of the icon
     * @return The ImageView, or null if the icon was not found
     */
    public ImageView getIconView(String iconName, int size) {
        logger.debug("Getting icon view for '{}' at size {}", iconName, size);
        Image icon = getIcon(iconName, size);

        if (icon != null) {
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            logger.debug("Created ImageView for icon '{}' at size {}", iconName, size);
            return imageView;
        }

        logger.warn("No icon found for '{}' at size {}", iconName, size);
        return null;
    }

    /**
     * Finds the closest available size for an icon.
     *
     * @param iconName The name of the icon
     * @param targetSize The target size
     * @return The icon with the closest available size, or null if not found
     */
    private Image findClosestSizeIcon(String iconName, int targetSize) {
        int closestSize = -1;
        int minDiff = Integer.MAX_VALUE;

        for (int size : ICON_SIZES) {
            int diff = Math.abs(size - targetSize);
            if (diff < minDiff) {
                String key = getIconKey(iconName, size);
                if (iconCache.containsKey(key) || loadIcon(iconName, size)) {
                    closestSize = size;
                    minDiff = diff;
                }
            }
        }

        if (closestSize > 0) {
            return iconCache.get(getIconKey(iconName, closestSize));
        }

        return null;
    }

    /**
     * Gets a unique key for an icon.
     *
     * @param iconName The name of the icon
     * @param size The size of the icon
     * @return The key
     */
    private String getIconKey(String iconName, int size) {
        return iconName + "_" + size;
    }

    /**
     * Gets the set of all loaded icon names.
     *
     * @return The set of icon names
     */
    public Set<String> getLoadedIconNames() {
        Set<String> iconNames = new java.util.HashSet<>();

        for (String key : iconCache.keySet()) {
            String[] parts = key.split("_");
            if (parts.length > 0) {
                iconNames.add(parts[0]);
            }
        }

        return Collections.unmodifiableSet(iconNames);
    }

    /**
     * Gets the application icon in multiple sizes for window icons.
     *
     * @return A list of application icons in different sizes
     */
    public java.util.List<Image> getAppIcons() {
        java.util.List<Image> icons = new java.util.ArrayList<>();

        for (int size : new int[] { 16, 32, 48, 64, 128 }) {
            Image icon = getIcon("brobot-icon", size);
            if (icon != null) {
                icons.add(icon);
            }
        }

        return icons;
    }

    /**
     * Clears the icon cache.
     */
    public void clearCache() {
        iconCache.clear();
        logger.debug("Icon cache cleared");
    }
}