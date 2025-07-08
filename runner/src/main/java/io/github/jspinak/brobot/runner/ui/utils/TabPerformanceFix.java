package io.github.jspinak.brobot.runner.ui.utils;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to improve tab switching performance.
 */
@Slf4j
public class TabPerformanceFix {
    
    /**
     * Apply performance optimizations to a TabPane.
     * This addresses issues with slow tab clicking response.
     */
    public static void optimizeTabPane(TabPane tabPane) {
        if (tabPane == null) return;
        
        // Disable animations
        tabPane.setStyle("-fx-open-tab-animation: NONE; -fx-close-tab-animation: NONE;");
        
        // Add direct mouse click handlers to bypass any delays
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getGraphic() != null) {
                tab.getGraphic().setOnMouseClicked(event -> {
                    event.consume();
                    Platform.runLater(() -> tabPane.getSelectionModel().select(tab));
                });
            }
            
            // Ensure tab content is loaded eagerly
            if (tab.getContent() != null) {
                tab.getContent().setCache(false);
                tab.getContent().setManaged(true);
            }
        }
        
        // Add a more responsive click handler to the tab header area
        tabPane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            // Get the clicked tab
            Tab clickedTab = findClickedTab(tabPane, event.getX(), event.getY());
            
            if (clickedTab != null && clickedTab != tabPane.getSelectionModel().getSelectedItem()) {
                event.consume();
                // Use runLater to ensure UI thread processes this immediately
                Platform.runLater(() -> {
                    tabPane.getSelectionModel().select(clickedTab);
                    log.debug("Tab switched to: {}", clickedTab.getText());
                });
            }
        });
        
        // Disable tab reordering which can cause delays
        tabPane.getTabs().forEach(tab -> tab.setDisable(false));
        
        // Force immediate layout
        tabPane.requestLayout();
        
        log.info("Tab performance optimizations applied to TabPane");
    }
    
    /**
     * Find which tab was clicked based on coordinates.
     */
    private static Tab findClickedTab(TabPane tabPane, double x, double y) {
        // Simple heuristic: divide tab header width by number of tabs
        double tabWidth = tabPane.getWidth() / tabPane.getTabs().size();
        int tabIndex = (int) (x / tabWidth);
        
        if (tabIndex >= 0 && tabIndex < tabPane.getTabs().size()) {
            return tabPane.getTabs().get(tabIndex);
        }
        
        return null;
    }
    
    /**
     * Ensure all tab content is pre-loaded to avoid delays during switching.
     */
    public static void preloadTabContent(TabPane tabPane) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getContent() != null) {
                // Force content to be created and laid out
                tab.getContent().autosize();
                tab.getContent().applyCss();
                if (tab.getContent() instanceof javafx.scene.Parent) {
                    ((javafx.scene.Parent) tab.getContent()).layout();
                }
            }
        }
        log.debug("Pre-loaded content for {} tabs", tabPane.getTabs().size());
    }
}