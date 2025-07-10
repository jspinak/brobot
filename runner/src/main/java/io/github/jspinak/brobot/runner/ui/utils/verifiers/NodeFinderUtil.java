package io.github.jspinak.brobot.runner.ui.utils.verifiers;

import javafx.scene.Node;
import javafx.scene.Parent;

import java.util.ArrayList;
import java.util.List;

public class NodeFinderUtil {
    
    public static <T> List<T> findAllNodesOfType(Node root, Class<T> type) {
        List<T> results = new ArrayList<>();
        findAllNodesOfTypeRecursive(root, type, results);
        return results;
    }
    
    private static <T> void findAllNodesOfTypeRecursive(Node node, Class<T> type, List<T> results) {
        if (node == null) return;
        
        if (type.isInstance(node)) {
            results.add(type.cast(node));
        }
        
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                findAllNodesOfTypeRecursive(child, type, results);
            }
        }
    }
}