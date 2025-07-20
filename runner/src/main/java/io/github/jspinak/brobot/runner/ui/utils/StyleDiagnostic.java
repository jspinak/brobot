package io.github.jspinak.brobot.runner.ui.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diagnostic utility to analyze and fix styling issues.
 */
public class StyleDiagnostic {
    private static final Logger logger = LoggerFactory.getLogger(StyleDiagnostic.class);
    
    /**
     * Recursively removes all white borders from nodes in a scene.
     */
    public static void removeWhiteBorders(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }
        
        removeWhiteBordersFromNode(scene.getRoot());
        logger.info("Removed white borders from scene");
    }
    
    /**
     * Recursively removes white borders from a node and its children.
     */
    private static void removeWhiteBordersFromNode(Node node) {
        if (node == null) {
            return;
        }
        
        // Remove any inline border styles
        String currentStyle = node.getStyle();
        if (currentStyle != null && currentStyle.contains("border")) {
            String newStyle = currentStyle.replaceAll("-fx-border[^;]*;?", "");
            node.setStyle(newStyle);
        }
        
        // For regions, explicitly set transparent borders
        if (node instanceof Region) {
            Region region = (Region) node;
            region.setStyle(region.getStyle() + "-fx-border-color: transparent;");
        }
        
        // Fix label text color
        if (node instanceof Label) {
            Label label = (Label) node;
            String style = label.getStyle();
            if (style == null || !style.contains("-fx-text-fill")) {
                label.setStyle(style + "-fx-text-fill: #c9d1d9;");
            }
        }
        
        // Recursively process children
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                removeWhiteBordersFromNode(child);
            }
        }
    }
    
    /**
     * Logs diagnostic information about a node's styling.
     */
    public static void logNodeStyle(Node node, String description) {
        if (node == null) {
            return;
        }
        
        logger.info("=== Style Diagnostic: {} ===", description);
        logger.info("Node class: {}", node.getClass().getSimpleName());
        logger.info("Style classes: {}", node.getStyleClass());
        logger.info("Inline style: {}", node.getStyle());
        
        if (node instanceof Region) {
            Region region = (Region) node;
            logger.info("Border: {}", region.getBorder());
            logger.info("Background: {}", region.getBackground());
        }
    }
}