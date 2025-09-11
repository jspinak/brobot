package io.github.jspinak.brobot.runner.ui.navigation;

import java.util.*;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;

import lombok.Setter;

/**
 * Manages navigation between screens in the application.
 *
 * <p>The NavigationManager provides a centralized navigation system for the JavaFX application,
 * supporting screen transitions with animations, navigation history, and event notifications. It
 * implements a browser-like navigation model with back/forward functionality.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Screen registration and lifecycle management
 *   <li>Animated transitions between screens (fade, slide)
 *   <li>Navigation history with back/forward support
 *   <li>Navigation event notifications for observers
 *   <li>Context passing between screens
 *   <li>Breadcrumb trail generation
 * </ul>
 *
 * <p>Navigation flow:
 *
 * <ol>
 *   <li>Screen requests navigation via navigateTo()
 *   <li>Current screen is animated out
 *   <li>New screen is created with context
 *   <li>New screen is animated in
 *   <li>History is updated
 *   <li>Listeners are notified
 * </ol>
 *
 * @see IScreen
 * @see NavigationContext
 * @see ScreenRegistry
 * @see INavigationListener
 */
@Component
public class NavigationManager {
    private static final Logger logger = LoggerFactory.getLogger(NavigationManager.class);

    private final EventBus eventBus;
    private final ScreenRegistry screenRegistry;

    // The container where screens will be displayed
    @Setter private Pane contentContainer;

    // Current screen property
    private final ObjectProperty<Screen> currentScreen = new SimpleObjectProperty<>();

    // Navigation history
    private final Deque<Screen> history = new LinkedList<>();
    private final Deque<Screen> forwardHistory = new LinkedList<>();

    // Navigation listeners
    private final List<INavigationListener> navigationListeners = new ArrayList<>();

    @Autowired
    public NavigationManager(EventBus eventBus, ScreenRegistry screenRegistry) {
        this.eventBus = eventBus;
        this.screenRegistry = screenRegistry;
    }

    @PostConstruct
    public void initialize() {
        logger.info("NavigationManager initialized");
    }

    /**
     * Navigates to the specified screen.
     *
     * @param screenId The ID of the screen to navigate to
     * @return true if navigation was successful, false otherwise
     */
    public boolean navigateTo(String screenId) {
        return navigateTo(screenId, NavigationContext.empty(), TransitionType.FADE);
    }

    /**
     * Navigates to the specified screen with a custom transition.
     *
     * @param screenId The ID of the screen to navigate to
     * @param transitionType The type of transition to use
     * @return true if navigation was successful, false otherwise
     */
    public boolean navigateTo(String screenId, TransitionType transitionType) {
        return navigateTo(screenId, NavigationContext.empty(), transitionType);
    }

    /**
     * Navigates to the specified screen with context data.
     *
     * @param screenId The ID of the screen to navigate to
     * @param context Context data for the navigation
     * @return true if navigation was successful, false otherwise
     */
    public boolean navigateTo(String screenId, NavigationContext context) {
        return navigateTo(screenId, context, TransitionType.FADE);
    }

    /**
     * Navigates to the specified screen with context data and a custom transition.
     *
     * @param screenId The ID of the screen to navigate to
     * @param context Context data for the navigation
     * @param transitionType The type of transition to use
     * @return true if navigation was successful, false otherwise
     */
    public boolean navigateTo(
            String screenId, NavigationContext context, TransitionType transitionType) {
        if (contentContainer == null) {
            logger.error("Content container not set");
            return false;
        }

        // Get the requested screen
        Optional<Screen> screenOpt = screenRegistry.getScreen(screenId);
        if (screenOpt.isEmpty()) {
            logger.error("Screen not found: {}", screenId);
            return false;
        }

        Screen screen = screenOpt.get();

        // Check if we're already on this screen
        if (currentScreen.get() != null && currentScreen.get().getId().equals(screenId)) {
            logger.debug("Already on screen: {}", screenId);
            return true;
        }

        // Get the content node for the screen
        Optional<Node> contentOpt = screen.getContent(context);
        if (contentOpt.isEmpty()) {
            logger.error("Failed to get content for screen: {}", screenId);
            return false;
        }

        Node content = contentOpt.get();

        // Update navigation history
        if (currentScreen.get() != null) {
            history.push(currentScreen.get());
        }
        forwardHistory.clear();

        // Store the previous screen for the transition
        Screen previousScreen = currentScreen.get();

        // Update the current screen
        currentScreen.set(screen);

        // Perform the transition
        performTransition(previousScreen, screen, content, context, transitionType);

        // Log the navigation
        logger.info("Navigated to screen: {}", screenId);
        eventBus.publish(LogEvent.info(this, "Navigated to: " + screen.getTitle(), "Navigation"));

        // Notify listeners
        for (INavigationListener listener : navigationListeners) {
            listener.onNavigated(previousScreen, screen, context);
        }

        return true;
    }

    /**
     * Navigates back to the previous screen.
     *
     * @return true if navigation was successful, false otherwise
     */
    public boolean navigateBack() {
        if (history.isEmpty()) {
            logger.debug("No previous screen to navigate back to");
            return false;
        }

        // Get the previous screen
        Screen previousScreen = history.pop();

        // Add current screen to forward history
        if (currentScreen.get() != null) {
            forwardHistory.push(currentScreen.get());
        }

        // Navigate to the previous screen
        return navigateTo(
                previousScreen.getId(), NavigationContext.empty(), TransitionType.SLIDE_RIGHT);
    }

    /**
     * Navigates forward to the next screen in the history.
     *
     * @return true if navigation was successful, false otherwise
     */
    public boolean navigateForward() {
        if (forwardHistory.isEmpty()) {
            logger.debug("No forward screen to navigate to");
            return false;
        }

        // Get the next screen
        Screen nextScreen = forwardHistory.pop();

        // Navigate to the next screen
        return navigateTo(nextScreen.getId(), NavigationContext.empty(), TransitionType.SLIDE_LEFT);
    }

    /**
     * Checks if backward navigation is possible.
     *
     * @return true if backward navigation is possible, false otherwise
     */
    public boolean canNavigateBack() {
        return !history.isEmpty();
    }

    /**
     * Checks if forward navigation is possible.
     *
     * @return true if forward navigation is possible, false otherwise
     */
    public boolean canNavigateForward() {
        return !forwardHistory.isEmpty();
    }

    /**
     * Gets the current screen.
     *
     * @return The current screen, or null if no screen is displayed
     */
    public Screen getCurrentScreen() {
        return currentScreen.get();
    }

    /**
     * Gets the current screen property.
     *
     * @return The current screen property
     */
    public ObjectProperty<Screen> currentScreenProperty() {
        return currentScreen;
    }

    /**
     * Gets the navigation history.
     *
     * @return The navigation history
     */
    public List<Screen> getHistory() {
        return new ArrayList<>(history);
    }

    /**
     * Adds a navigation listener.
     *
     * @param listener The listener to add
     */
    public void addNavigationListener(INavigationListener listener) {
        if (listener != null && !navigationListeners.contains(listener)) {
            navigationListeners.add(listener);
        }
    }

    /**
     * Removes a navigation listener.
     *
     * @param listener The listener to remove
     */
    public void removeNavigationListener(INavigationListener listener) {
        navigationListeners.remove(listener);
    }

    /**
     * Performs the transition between screens.
     *
     * @param previousScreen The previous screen
     * @param newScreen The new screen
     * @param newContent The content of the new screen
     * @param context The navigation context
     * @param transitionType The type of transition to use
     */
    private void performTransition(
            Screen previousScreen,
            Screen newScreen,
            Node newContent,
            NavigationContext context,
            TransitionType transitionType) {
        // If there's no previous screen, just show the new content
        if (previousScreen == null) {
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(newContent);
            return;
        }

        // Create final reference for lambda expressions
        final Pane container = this.contentContainer;

        // Get the old content
        final Node oldContent;
        if (!container.getChildren().isEmpty()) {
            Node tempContent;
            try {
                tempContent = container.getChildren().get(0);
            } catch (IndexOutOfBoundsException e) {
                // List became empty during access, set to null
                tempContent = null;
            }
            oldContent = tempContent;
        } else {
            oldContent = null;
        }

        switch (transitionType) {
            case NONE:
                // Just replace the content
                container.getChildren().clear();
                container.getChildren().add(newContent);
                break;

            case FADE:
                // Fade out old content, fade in new content
                if (oldContent != null) {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(150), oldContent);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(
                            e -> {
                                container.getChildren().clear();
                                container.getChildren().add(newContent);
                                newContent.setOpacity(0.0);

                                FadeTransition fadeIn =
                                        new FadeTransition(Duration.millis(150), newContent);
                                fadeIn.setFromValue(0.0);
                                fadeIn.setToValue(1.0);
                                fadeIn.play();
                            });
                    fadeOut.play();
                } else {
                    container.getChildren().clear();
                    container.getChildren().add(newContent);
                    newContent.setOpacity(0.0);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(150), newContent);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                }
                break;

            case SLIDE_LEFT:
                // Slide old content out to the left, slide new content in from the right
                container.getChildren().add(newContent);
                newContent.setTranslateX(container.getWidth());

                TranslateTransition slideOutLeft =
                        new TranslateTransition(Duration.millis(200), oldContent);
                slideOutLeft.setToX(-container.getWidth());

                TranslateTransition slideInRight =
                        new TranslateTransition(Duration.millis(200), newContent);
                slideInRight.setToX(0);

                ParallelTransition transition = new ParallelTransition(slideOutLeft, slideInRight);
                transition.setOnFinished(
                        e -> {
                            if (oldContent != null
                                    && container.getChildren().contains(oldContent)) {
                                container.getChildren().remove(oldContent);
                            }
                        });
                transition.play();
                break;

            case SLIDE_RIGHT:
                // Slide old content out to the right, slide new content in from the left
                container.getChildren().add(newContent);
                newContent.setTranslateX(-container.getWidth());

                TranslateTransition slideOutRight =
                        new TranslateTransition(Duration.millis(200), oldContent);
                slideOutRight.setToX(container.getWidth());

                TranslateTransition slideInLeft =
                        new TranslateTransition(Duration.millis(200), newContent);
                slideInLeft.setToX(0);

                ParallelTransition transition2 = new ParallelTransition(slideOutRight, slideInLeft);
                transition2.setOnFinished(
                        e -> {
                            if (oldContent != null
                                    && container.getChildren().contains(oldContent)) {
                                container.getChildren().remove(oldContent);
                            }
                        });
                transition2.play();
                break;
        }
    }

    /** Types of transitions for screen navigation. */
    public enum TransitionType {
        NONE, // No transition
        FADE, // Fade out old screen, fade in new screen
        SLIDE_LEFT, // Slide out to the left, slide in from the right
        SLIDE_RIGHT // Slide out to the right, slide in from the left
    }

    /** Listener interface for navigation events. */
    public interface INavigationListener {
        void onNavigated(Screen previousScreen, Screen newScreen, NavigationContext context);
    }
}
