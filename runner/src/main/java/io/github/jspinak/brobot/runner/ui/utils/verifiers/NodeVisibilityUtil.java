package io.github.jspinak.brobot.runner.ui.utils.verifiers;

import javafx.scene.Node;
import javafx.scene.Parent;

public class NodeVisibilityUtil {
    
    public static boolean isNodeActuallyVisible(Node node) {
        if (node == null || !node.isVisible()) return false;
        
        // Check opacity
        if (node.getOpacity() < 0.01) return false;
        
        // Check if any parent is invisible
        Parent parent = node.getParent();
        while (parent != null) {
            if (!parent.isVisible() || parent.getOpacity() < 0.01) return false;
            parent = parent.getParent();
        }
        
        // Check bounds
        var bounds = node.getBoundsInLocal();
        return bounds.getWidth() > 0 && bounds.getHeight() > 0;
    }
}