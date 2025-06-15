package io.github.jspinak.brobot.runner.ui.resources;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles loading and caching of application resources such as images,
 * icons, and localized messages.
 */
@Component
public class ResourceLoader {
    private static final Logger logger = LoggerFactory.getLogger(ResourceLoader.class);

    private final EventBus eventBus;

    // Cache for loaded images
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    // Message source for i18n
    private ResourceBundleMessageSource messageSource;
    private MessageSourceAccessor messageAccessor;

    @Autowired
    public ResourceLoader(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void initialize() {
        // Initialize message source for i18n
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageAccessor = new MessageSourceAccessor(messageSource, Locale.getDefault());

        logger.info("ResourceLoader initialized");
        eventBus.publish(LogEvent.info(this, "Resource loader initialized", "Resources"));
    }

    /**
     * Loads an image from the specified path.
     *
     * @param imagePath The path to the image resource
     * @return The loaded image, or null if the image could not be loaded
     */
    public Image loadImage(String imagePath) {
        return loadImage(imagePath, true);
    }

    /**
     * Loads an image from the specified path.
     *
     * @param imagePath The path to the image resource
     * @param cache Whether to cache the image
     * @return The loaded image, or null if the image could not be loaded
     */
    public Image loadImage(String imagePath, boolean cache) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        // Check cache first if caching is enabled
        if (cache && imageCache.containsKey(imagePath)) {
            return imageCache.get(imagePath);
        }

        try {
            // Handle both absolute paths and resource paths
            InputStream inputStream;
            if (imagePath.startsWith("/")) {
                inputStream = getClass().getResourceAsStream(imagePath);
            } else {
                inputStream = getClass().getResourceAsStream("/" + imagePath);
            }

            if (inputStream == null) {
                logger.warn("Image not found: {}", imagePath);
                return null;
            }

            Image image = new Image(inputStream);

            // Cache the image if caching is enabled
            if (cache && !image.isError()) {
                imageCache.put(imagePath, image);
            }

            return image;
        } catch (Exception e) {
            logger.error("Error loading image: {}", imagePath, e);
            eventBus.publish(LogEvent.error(this,
                    "Failed to load image: " + imagePath, "Resources", e));
            return null;
        }
    }

    /**
     * Gets a URL for a resource.
     *
     * @param resourcePath The path to the resource
     * @return The URL of the resource, or null if the resource could not be found
     */
    public URL getResourceUrl(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return null;
        }

        try {
            // Handle both absolute paths and resource paths
            if (resourcePath.startsWith("/")) {
                return getClass().getResource(resourcePath);
            } else {
                return getClass().getResource("/" + resourcePath);
            }
        } catch (Exception e) {
            logger.error("Error getting resource URL: {}", resourcePath, e);
            return null;
        }
    }

    /**
     * Gets a message from the message bundle.
     *
     * @param code The message code
     * @param defaultMessage The default message to return if the code is not found
     * @return The localized message
     */
    public String getMessage(String code, String defaultMessage) {
        try {
            return messageAccessor.getMessage(code, defaultMessage);
        } catch (Exception e) {
            logger.error("Error getting message: {}", code, e);
            return defaultMessage;
        }
    }

    /**
     * Gets a message from the message bundle.
     *
     * @param code The message code
     * @param args Arguments to substitute into the message
     * @param defaultMessage The default message to return if the code is not found
     * @return The localized message
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        try {
            return messageAccessor.getMessage(code, args, defaultMessage);
        } catch (Exception e) {
            logger.error("Error getting message: {}", code, e);
            return defaultMessage;
        }
    }

    /**
     * Gets the current locale.
     *
     * @return The current locale
     */
    public Locale getCurrentLocale() {
        return Locale.getDefault();
    }

    /**
     * Sets the current locale.
     *
     * @param locale The locale to set
     */
    public void setCurrentLocale(Locale locale) {
        if (locale != null) {
            Locale.setDefault(locale);
            messageAccessor = new MessageSourceAccessor(messageSource, locale);
            logger.info("Locale changed to {}", locale);
        }
    }

    /**
     * Clears the image cache.
     */
    public void clearImageCache() {
        imageCache.clear();
        logger.info("Image cache cleared");
    }

    /**
     * Gets the number of images in the cache.
     *
     * @return The number of cached images
     */
    public int getImageCacheSize() {
        return imageCache.size();
    }

    /**
     * Gets a list of available locales.
     *
     * @return List of available locales
     */
    public List<Locale> getAvailableLocales() {
        // This would typically be determined by the available resource bundles
        // For now, return a fixed list of common locales
        return Arrays.asList(
                Locale.ENGLISH,
                Locale.GERMAN,
                Locale.FRENCH,
                Locale.CHINESE,
                Locale.of("es")  // Spanish
        );
    }
}