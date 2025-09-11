package io.github.jspinak.brobot.runner.ui.navigation;

import java.util.Optional;
import javafx.scene.Node;

/**
 * Interface representing a screen in the application. A screen is a top-level UI component that can
 * be navigated to.
 */
public interface IScreen {

    /**
     * Gets the unique identifier for this screen.
     *
     * @return The screen ID
     */
    String getId();

    /**
     * Gets the display title for this screen.
     *
     * @return The screen title
     */
    String getTitle();

    /**
     * Creates and returns the content for this screen.
     *
     * @param context The navigation context
     * @return An Optional containing the screen content, or empty if the content could not be
     *     created
     */
    Optional<Node> getContent(NavigationContext context);
}
