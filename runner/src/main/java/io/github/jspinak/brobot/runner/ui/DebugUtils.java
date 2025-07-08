package io.github.jspinak.brobot.runner.ui;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for debugging UI issues
 */
public class DebugUtils {
    private static final Logger logger = LoggerFactory.getLogger(DebugUtils.class);
    
    /**
     * Prints the scene graph hierarchy
     */
    public static void printSceneGraph(Node node, int depth) {
        if (node == null) return;
        
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
        
        String nodeInfo = node.getClass().getSimpleName();
        if (node instanceof Labeled) {
            nodeInfo += " [" + ((Labeled) node).getText() + "]";
        }
        
        logger.debug("{}{} - visible: {}, disabled: {}, opacity: {}", 
            indent, nodeInfo, node.isVisible(), node.isDisabled(), node.getOpacity());
        
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                printSceneGraph(child, depth + 1);
            }
        }
    }
    
    /**
     * Checks if a node is truly visible in the scene graph
     */
    public static boolean isNodeReallyVisible(Node node) {
        if (node == null) return false;
        
        Node current = node;
        while (current != null) {
            if (!current.isVisible() || current.getOpacity() == 0) {
                return false;
            }
            current = current.getParent();
        }
        return true;
    }
}