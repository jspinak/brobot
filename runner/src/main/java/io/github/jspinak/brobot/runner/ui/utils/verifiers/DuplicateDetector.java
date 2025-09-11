package io.github.jspinak.brobot.runner.ui.utils.verifiers;

import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DuplicateDetector {
    private static final Logger logger = LoggerFactory.getLogger(DuplicateDetector.class);

    public static class DuplicateIssue {
        public final String text;
        public final List<Node> nodes;
        public final String description;

        public DuplicateIssue(String text, List<Node> nodes) {
            this.text = text;
            this.nodes = new ArrayList<>(nodes);
            this.description = String.format("Text '%s' appears %d times", text, nodes.size());
        }
    }

    public List<DuplicateIssue> detectDuplicates(List<Node> nodes) {
        List<DuplicateIssue> duplicates = new ArrayList<>();
        Map<String, List<Node>> textOccurrences = new HashMap<>();

        for (Node node : nodes) {
            // Skip LabeledText nodes that are children of Label nodes (JavaFX internal structure)
            if (node.getClass().getName().contains("LabeledText")
                    && node.getParent() != null
                    && node.getParent() instanceof Label) {
                continue;
            }

            String text = extractText(node);
            if (text != null && !text.trim().isEmpty() && text.length() > 3) {
                // Ignore very short text and common UI elements
                if (!isCommonUIText(text)) {
                    textOccurrences.computeIfAbsent(text, k -> new ArrayList<>()).add(node);
                }
            }
        }

        // Report duplicates
        for (Map.Entry<String, List<Node>> entry : textOccurrences.entrySet()) {
            if (entry.getValue().size() > 1) {
                // Check if nodes are actually visible at the same time
                List<Node> visibleDuplicates =
                        entry.getValue().stream()
                                .filter(this::isNodeActuallyVisible)
                                .collect(Collectors.toList());

                if (visibleDuplicates.size() > 1) {
                    duplicates.add(new DuplicateIssue(entry.getKey(), visibleDuplicates));
                }
            }
        }

        return duplicates;
    }

    private String extractText(Node node) {
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

    private boolean isCommonUIText(String text) {
        String[] commonTexts = {
            "OK", "Cancel", "Apply", "Close", "Save", "Delete", "Edit",
            "Yes", "No", "...", "Browse", "Select", "Open", "New",
            "+", "-", "x", "X", ":", "|", "/"
        };

        for (String common : commonTexts) {
            if (text.equalsIgnoreCase(common)) return true;
        }

        return false;
    }

    private boolean isNodeActuallyVisible(Node node) {
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

    public void logResults(List<DuplicateIssue> issues) {
        if (!issues.isEmpty()) {
            logger.error("DUPLICATE RENDERING ISSUES: {}", issues.size());
            for (DuplicateIssue issue : issues) {
                logger.error("  - {}", issue.description);
            }
        }
    }
}
