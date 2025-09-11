package io.github.jspinak.brobot.runner.ui.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Analyzes and logs the UI component hierarchy to understand structure issues. */
public class UIStructureAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(UIStructureAnalyzer.class);

    /** Analyzes the scene and logs the complete UI hierarchy. */
    public static void analyzeScene(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            logger.warn("Cannot analyze null scene or root");
            return;
        }

        logger.info("=== UI Structure Analysis ===");
        analyzeNode(scene.getRoot(), 0);
        logger.info("=== End UI Structure Analysis ===");
    }

    /** Recursively analyzes a node and its children. */
    private static void analyzeNode(Node node, int depth) {
        if (node == null || !node.isVisible()) {
            return;
        }

        String indent = "  ".repeat(depth);
        String nodeInfo = getNodeInfo(node);

        logger.info("{}{}", indent, nodeInfo);

        // If it's a parent node, analyze children
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                analyzeNode(child, depth + 1);
            }
        }

        // Special handling for TabPane
        if (node instanceof TabPane) {
            TabPane tabPane = (TabPane) node;
            for (Tab tab : tabPane.getTabs()) {
                logger.info(
                        "{}  Tab: '{}' (selected: {})", indent, tab.getText(), tab.isSelected());
                if (tab.getContent() != null) {
                    analyzeNode(tab.getContent(), depth + 2);
                }
            }
        }
    }

    /** Gets a descriptive string for a node. */
    private static String getNodeInfo(Node node) {
        StringBuilder info = new StringBuilder();
        info.append(node.getClass().getSimpleName());

        // Add ID if present
        if (node.getId() != null && !node.getId().isEmpty()) {
            info.append("#").append(node.getId());
        }

        // Add primary style class
        if (!node.getStyleClass().isEmpty()) {
            info.append(".").append(node.getStyleClass().get(0));
        }

        // Add text content for known types
        String text = extractText(node);
        if (text != null && !text.isEmpty()) {
            info.append(" \"")
                    .append(text.length() > 30 ? text.substring(0, 30) + "..." : text)
                    .append("\"");
        }

        // Add bounds info
        info.append(" [")
                .append(
                        String.format(
                                "%.0fx%.0f",
                                node.getBoundsInLocal().getWidth(),
                                node.getBoundsInLocal().getHeight()))
                .append("]");

        // Add visibility info if not visible
        if (!node.isVisible()) {
            info.append(" (hidden)");
        }

        return info.toString();
    }

    /** Extracts text from various node types. */
    private static String extractText(Node node) {
        if (node instanceof Label) {
            return ((Label) node).getText();
        } else if (node instanceof Text) {
            return ((Text) node).getText();
        } else if (node instanceof Button) {
            return ((Button) node).getText();
        } else if (node instanceof TitledPane) {
            return ((TitledPane) node).getText();
        } else if (node instanceof TextField) {
            TextField tf = (TextField) node;
            return tf.getText().isEmpty() ? tf.getPromptText() : tf.getText();
        } else if (node instanceof TextArea) {
            TextArea ta = (TextArea) node;
            return ta.getText().isEmpty() ? ta.getPromptText() : ta.getText();
        }
        return null;
    }

    /** Finds all nodes with duplicate text content. */
    public static void findDuplicateTextNodes(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }

        logger.info("=== Duplicate Text Analysis ===");

        // Collect all text nodes
        java.util.Map<String, java.util.List<Node>> textToNodes = new java.util.HashMap<>();
        collectTextNodes(scene.getRoot(), textToNodes);

        // Report duplicates with their locations
        for (java.util.Map.Entry<String, java.util.List<Node>> entry : textToNodes.entrySet()) {
            if (entry.getValue().size() > 1) {
                logger.warn("Text '{}' appears {} times:", entry.getKey(), entry.getValue().size());
                for (Node node : entry.getValue()) {
                    logger.warn(
                            "  - {} at path: {}",
                            node.getClass().getSimpleName(),
                            getNodePath(node));
                }
            }
        }

        logger.info("=== End Duplicate Text Analysis ===");
    }

    private static void collectTextNodes(
            Node node, java.util.Map<String, java.util.List<Node>> textToNodes) {
        if (node == null || !node.isVisible()) {
            return;
        }

        String text = extractText(node);
        if (text != null && !text.trim().isEmpty() && text.length() > 3) {
            textToNodes.computeIfAbsent(text, k -> new java.util.ArrayList<>()).add(node);
        }

        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectTextNodes(child, textToNodes);
            }
        }
    }

    /** Gets the path from root to node. */
    private static String getNodePath(Node node) {
        java.util.List<String> path = new java.util.ArrayList<>();
        Node current = node;

        while (current != null) {
            String nodeDesc = current.getClass().getSimpleName();
            if (current.getId() != null && !current.getId().isEmpty()) {
                nodeDesc += "#" + current.getId();
            } else if (!current.getStyleClass().isEmpty()) {
                nodeDesc += "." + current.getStyleClass().get(0);
            }
            path.add(0, nodeDesc);
            current = current.getParent();
        }

        return String.join(" > ", path);
    }
}
