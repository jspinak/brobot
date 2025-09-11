package io.github.jspinak.brobot.runner.ui.navigation;

import java.util.Optional;
import javafx.scene.Node;

/**
 * Represents a screen in the application. Each screen has a unique ID, a title, and can provide its
 * content.
 */
public interface Screen {

    /**
     * Gets the unique identifier for this screen.
     *
     * @return The screen ID
     */
    String getId();

    /**
     * Gets the title of this screen.
     *
     * @return The screen title
     */
    String getTitle();

    /**
     * Gets the content of this screen.
     *
     * @param context The navigation context
     * @return An Optional containing the screen content, or empty if content cannot be created
     */
    Optional<Node> getContent(NavigationContext context);
}
