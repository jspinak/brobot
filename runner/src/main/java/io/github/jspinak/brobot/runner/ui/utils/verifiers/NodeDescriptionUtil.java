package io.github.jspinak.brobot.runner.ui.utils.verifiers;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class NodeDescriptionUtil {
    
    public static String getNodeDescription(Node node) {
        if (node == null) return "null";
        
        String text = extractText(node);
        String className = node.getClass().getSimpleName();
        
        if (text != null && !text.isEmpty()) {
            return String.format("%s: '%s'", className, text);
        }
        
        if (node.getId() != null && !node.getId().isEmpty()) {
            return String.format("%s#%s", className, node.getId());
        }
        
        if (!node.getStyleClass().isEmpty()) {
            return String.format("%s.%s", className, node.getStyleClass().get(0));
        }
        
        return className;
    }
    
    public static String extractText(Node node) {
        if (node instanceof Label) {
            return ((Label) node).getText();
        } else if (node instanceof Text) {
            return ((Text) node).getText();
        } else if (node instanceof Button) {
            return ((Button) node).getText();
        } else if (node instanceof TextField) {
            return ((TextField) node).getPromptText();
        } else if (node instanceof TextArea) {
            return ((TextArea) node).getPromptText();
        } else if (node instanceof TitledPane) {
            return ((TitledPane) node).getText();
        }
        return null;
    }
}