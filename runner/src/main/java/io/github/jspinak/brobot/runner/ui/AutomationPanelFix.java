package io.github.jspinak.brobot.runner.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary fix for AutomationPanel label duplication issues.
 * This should be applied until proper refactoring is implemented.
 */
@Component
public class AutomationPanelFix {
    private static final Logger logger = LoggerFactory.getLogger(AutomationPanelFix.class);
    private final Map<String, Label> categoryLabelCache = new HashMap<>();
    
    /**
     * Fixes the refreshAutomationButtons method to prevent duplicate labels
     */
    public void fixRefreshButtons(AutomationPanel panel) {
        // Add logging to track when refresh is called
        logger.debug("AutomationPanel refresh called");
        
        // Clear the cache when panel is cleared
        panel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Add periodic deduplication
                Platform.runLater(() -> {
                    deduplicatePanel(panel);
                });
            }
        });
    }
    
    /**
     * Removes duplicate labels from the panel
     */
    private void deduplicatePanel(Pane panel) {
        Map<String, Node> seenLabels = new HashMap<>();
        
        // Iterate through children in reverse to safely remove
        for (int i = panel.getChildren().size() - 1; i >= 0; i--) {
            Node child = panel.getChildren().get(i);
            
            if (child instanceof VBox) {
                VBox vbox = (VBox) child;
                deduplicateVBox(vbox);
            }
            
            // Check for duplicate top-level labels
            if (child instanceof Label) {
                Label label = (Label) child;
                String key = label.getText() + "-" + label.getStyleClass().toString();
                
                if (seenLabels.containsKey(key)) {
                    logger.debug("Removing duplicate label: {}", label.getText());
                    panel.getChildren().remove(i);
                } else {
                    seenLabels.put(key, label);
                }
            }
        }
    }
    
    /**
     * Removes duplicate labels from a VBox (category box)
     */
    private void deduplicateVBox(VBox vbox) {
        Map<String, Node> seenLabels = new HashMap<>();
        
        for (int i = vbox.getChildren().size() - 1; i >= 0; i--) {
            Node child = vbox.getChildren().get(i);
            
            if (child instanceof Label) {
                Label label = (Label) child;
                String key = label.getText() + "-" + label.getStyleClass().toString();
                
                if (seenLabels.containsKey(key)) {
                    logger.debug("Removing duplicate label in VBox: {}", label.getText());
                    vbox.getChildren().remove(i);
                } else {
                    seenLabels.put(key, label);
                }
            }
        }
    }
    
    /**
     * Creates or reuses a category label to prevent duplicates
     */
    public Label getOrCreateCategoryLabel(String category) {
        return categoryLabelCache.computeIfAbsent(category, k -> {
            Label label = new Label(k);
            label.getStyleClass().add("category-label");
            label.setStyle("-fx-font-weight: bold;");
            label.setId("category-label-" + k.hashCode());
            return label;
        });
    }
    
    /**
     * Clears the label cache
     */
    public void clearCache() {
        categoryLabelCache.clear();
    }
}