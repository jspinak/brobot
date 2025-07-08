package io.github.jspinak.brobot.runner.ui.components;

import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import io.github.jspinak.brobot.runner.ui.navigation.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * A lazy-loading tab content wrapper that defers content creation until the tab is selected.
 */
public class LazyTabContent extends StackPane {
    private static final Logger logger = LoggerFactory.getLogger(LazyTabContent.class);
    
    private final Screen screen;
    private boolean contentLoaded = false;
    private Node actualContent = null;
    
    public LazyTabContent(Screen screen) {
        this.screen = screen;
        
        // Show a loading indicator initially
        ProgressIndicator loader = new ProgressIndicator();
        loader.setMaxSize(50, 50);
        getChildren().add(loader);
        
        getStyleClass().add("lazy-tab-content");
    }
    
    /**
     * Loads the actual content when needed.
     */
    public void loadContent() {
        if (!contentLoaded) {
            logger.info("Loading content for screen: {}", screen.getTitle());
            
            try {
                Optional<Node> contentOpt = screen.getContent(null);
                if (contentOpt.isPresent()) {
                    actualContent = contentOpt.get();
                    getChildren().clear();
                    getChildren().add(actualContent);
                    contentLoaded = true;
                    logger.info("Content loaded successfully for: {}", screen.getTitle());
                } else {
                    logger.warn("No content available for screen: {}", screen.getTitle());
                }
            } catch (Exception e) {
                logger.error("Error loading content for screen: {}", screen.getTitle(), e);
            }
        }
    }
    
    public boolean isContentLoaded() {
        return contentLoaded;
    }
}