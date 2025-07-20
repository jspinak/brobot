package io.github.jspinak.brobot.runner.ui;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to detect and fix duplicate labels in the UI.
 * This is a temporary fix until proper refactoring is implemented.
 */
public class LabelDeduplicator {
    private static final Logger logger = LoggerFactory.getLogger(LabelDeduplicator.class);
    
    /**
     * Removes duplicate labels from a parent node
     */
    public static void deduplicateLabels(Parent parent) {
        if (parent == null) return;
        
        Set<String> seenTexts = new HashSet<>();
        Set<Node> toRemove = new HashSet<>();
        
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Label) {
                Label label = (Label) child;
                String text = label.getText();
                
                if (text != null && !text.isEmpty()) {
                    String key = text + "-" + label.getStyleClass().toString();
                    
                    if (seenTexts.contains(key)) {
                        logger.debug("Found duplicate label: {}", text);
                        toRemove.add(label);
                    } else {
                        seenTexts.add(key);
                    }
                }
            }
            
            // Recursively check children
            if (child instanceof Parent) {
                deduplicateLabels((Parent) child);
            }
        }
        
        // Remove duplicates
        if (!toRemove.isEmpty()) {
            // Only certain Parent types allow modification
            if (parent instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane) parent).getChildren().removeAll(toRemove);
                logger.info("Removed {} duplicate labels", toRemove.size());
            } else {
                logger.warn("Cannot remove duplicates from {} - not a modifiable container", 
                    parent.getClass().getSimpleName());
            }
        }
    }
    
    /**
     * Logs the label hierarchy for debugging
     */
    public static void logLabelHierarchy(Node node, int depth) {
        if (node == null) return;
        
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
        
        if (node instanceof Labeled) {
            Labeled labeled = (Labeled) node;
            logger.debug("{}[{}] {} - '{}' - Classes: {}", 
                indent, 
                node.getClass().getSimpleName(),
                node.getId() != null ? node.getId() : "no-id",
                labeled.getText(),
                node.getStyleClass()
            );
        }
        
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                logLabelHierarchy(child, depth + 1);
            }
        }
    }
    
    /**
     * Adds unique IDs to labels for easier tracking
     */
    public static void assignLabelIds(Parent parent, String prefix) {
        if (parent == null) return;
        
        int counter = 0;
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Label && child.getId() == null) {
                child.setId(prefix + "-label-" + counter++);
            }
            
            if (child instanceof Parent) {
                assignLabelIds((Parent) child, prefix);
            }
        }
    }
}