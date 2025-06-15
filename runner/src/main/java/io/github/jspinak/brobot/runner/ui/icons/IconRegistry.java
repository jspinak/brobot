package io.github.jspinak.brobot.runner.ui.icons;

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
public class IconRegistry {
    private static final Logger logger = LoggerFactory.getLogger(IconRegistry.class);

    private final EventBus eventBus;

    // Paths to icon resources
    private static final String ICON_BASE_PATH = "/icons/";

    // Map of loaded icons
    private final Map<String, Image> iconCache = new HashMap<>();

    // Available sizes
    private static final int[] ICON_SIZES = { 16, 24, 32, 48, 64 };

    @Autowired
    public IconRegistry(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void initialize() {
        // Preload common icons
        preloadCommonIcons();

        logger.info("IconRegistry initialized");
        eventBus.publish(LogEvent.info(this, "Icon registry initialized", "Resources"));
    }

    /**
     * Preloads common icons into the cache.
     */
    private void preloadCommonIcons() {
        // App icons
        loadIcon("app_icon", 32);

        // Common action icons
        loadIcon("add", 16);
        loadIcon("edit", 16);
        loadIcon("delete", 16);
        loadIcon("save", 16);
        loadIcon("cancel", 16);
        loadIcon("refresh", 16);
        loadIcon("search", 16);
        loadIcon("settings", 16);

        // Navigation icons
        loadIcon("home", 16);
        loadIcon("back", 16);
        loadIcon("forward", 16);

        // Status icons
        loadIcon("success", 16);
        loadIcon("warning", 16);
        loadIcon("error", 16);
        loadIcon("info", 16);

        logger.debug("Preloaded {} common icons", iconCache.size());
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
                // Try SVG if PNG not found
                path = ICON_BASE_PATH + "svg/" + iconName + ".svg";
                is = getClass().getResourceAsStream(path);

                if (is == null) {
                    logger.warn("Icon not found: {}", iconName);
                    return false;
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
        Image icon = getIcon(iconName, size);

        if (icon != null) {
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            return imageView;
        }

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
            Image icon = getIcon("app_icon", size);
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