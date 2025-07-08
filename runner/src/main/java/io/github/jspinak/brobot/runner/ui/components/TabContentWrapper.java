package io.github.jspinak.brobot.runner.ui.components;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Wrapper for tab content to ensure proper layout and prevent overlapping with tab headers.
 * This class provides a container that properly isolates tab content from the tab header area.
 */
public class TabContentWrapper extends VBox {
    
    private final StackPane contentContainer;
    
    public TabContentWrapper() {
        // Initialize with top padding to prevent overlap with tab headers
        setPadding(new javafx.geometry.Insets(45, 0, 0, 0)); // 45px top padding for tab headers
        setSpacing(0);
        
        // Create a content container that will hold the actual content
        contentContainer = new StackPane();
        contentContainer.getStyleClass().add("tab-content-wrapper");
        
        // Add the container to this wrapper
        getChildren().add(contentContainer);
        
        // Ensure the content container grows to fill available space
        VBox.setVgrow(contentContainer, javafx.scene.layout.Priority.ALWAYS);
        
        // Apply styling
        getStyleClass().add("tab-content-wrapper-root");
        setStyle("-fx-background-color: transparent;");
    }
    
    /**
     * Sets the content for this tab.
     * 
     * @param content The content node to display in the tab
     */
    public void setContent(Node content) {
        contentContainer.getChildren().clear();
        if (content != null) {
            contentContainer.getChildren().add(content);
            
            // Ensure the content fills the available space
            if (content instanceof javafx.scene.layout.Region) {
                javafx.scene.layout.Region region = (javafx.scene.layout.Region) content;
                StackPane.setAlignment(region, javafx.geometry.Pos.TOP_LEFT);
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
            }
        }
    }
    
    /**
     * Gets the current content of this tab wrapper.
     * 
     * @return The content node, or null if no content is set
     */
    public Node getContent() {
        return contentContainer.getChildren().isEmpty() ? null : contentContainer.getChildren().get(0);
    }
    
    /**
     * Creates a wrapped version of the given content.
     * This is a convenience method for wrapping content in a single call.
     * 
     * @param content The content to wrap
     * @return A new TabContentWrapper containing the content
     */
    public static TabContentWrapper wrap(Node content) {
        TabContentWrapper wrapper = new TabContentWrapper();
        wrapper.setContent(content);
        return wrapper;
    }
}