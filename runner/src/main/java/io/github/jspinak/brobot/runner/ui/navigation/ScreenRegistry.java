package io.github.jspinak.brobot.runner.ui.navigation;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;

/**
 * Registry for all screens in the application.
 * Manages screen creation and lifecycle.
 */
@Component
public class ScreenRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ScreenRegistry.class);

    private final EventBus eventBus;

    // Map of registered screens
    private final Map<String, Screen> screens = new HashMap<>();

    // Map of screen factories
    private final Map<String, Function<NavigationContext, Node>> screenFactories = new HashMap<>();

    @Autowired
    public ScreenRegistry(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void initialize() {
        logger.info("ScreenRegistry initialized");
    }

    /**
     * Registers a screen.
     *
     * @param screen The screen to register
     */
    public void registerScreen(Screen screen) {
        if (screen == null || screen.getId() == null) {
            logger.warn("Attempted to register null screen or screen with null ID");
            return;
        }

        if (screens.containsKey(screen.getId())) {
            logger.warn("Screen with ID {} already registered, overwriting", screen.getId());
        }

        screens.put(screen.getId(), screen);
        logger.debug("Registered screen: {}", screen.getId());
    }

    /**
     * Registers a screen factory.
     *
     * @param screenId The ID of the screen
     * @param title The title of the screen
     * @param factory The factory function to create the screen content
     */
    public void registerScreenFactory(String screenId, String title, Function<NavigationContext, Node> factory) {
        if (screenId == null || factory == null) {
            logger.warn("Attempted to register null screen ID or factory");
            return;
        }

        // Create a screen that uses the factory to create its content
        Screen screen = new Screen() {
            @Override
            public String getId() {
                return screenId;
            }

            @Override
            public String getTitle() {
                return title != null ? title : screenId;
            }

            @Override
            public Optional<Node> getContent(NavigationContext context) {
                try {
                    Node content = factory.apply(context);
                    return Optional.ofNullable(content);
                } catch (Exception e) {
                    logger.error("Error creating content for screen: {}", screenId, e);
                    eventBus.publish(LogEvent.error(this,
                            "Failed to create screen content: " + screenId, "Navigation", e));
                    return Optional.empty();
                }
            }
        };

        registerScreen(screen);
        screenFactories.put(screenId, factory);
    }

    /**
     * Gets a screen by ID.
     *
     * @param screenId The ID of the screen
     * @return An Optional containing the screen, or empty if not found
     */
    public Optional<Screen> getScreen(String screenId) {
        return Optional.ofNullable(screens.get(screenId));
    }

    /**
     * Gets all registered screens.
     *
     * @return A list of all registered screens
     */
    public List<Screen> getAllScreens() {
        return new ArrayList<>(screens.values());
    }

    /**
     * Gets all registered screen IDs.
     *
     * @return A set of all registered screen IDs
     */
    public Set<String> getAllScreenIds() {
        return new HashSet<>(screens.keySet());
    }

    /**
     * Unregisters a screen.
     *
     * @param screenId The ID of the screen to unregister
     */
    public void unregisterScreen(String screenId) {
        screens.remove(screenId);
        screenFactories.remove(screenId);
        logger.debug("Unregistered screen: {}", screenId);
    }

    /**
     * Checks if a screen is registered.
     *
     * @param screenId The ID of the screen to check
     * @return true if the screen is registered, false otherwise
     */
    public boolean isScreenRegistered(String screenId) {
        return screens.containsKey(screenId);
    }
}