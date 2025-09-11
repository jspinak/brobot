package io.github.jspinak.brobot.runner.ui.debug;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.ui.registry.UIComponentRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * UI debugging tool that helps identify component issues.
 *
 * <p>Features: - Debug mode with visual borders - Component tree logging - Duplicate detection -
 * Performance monitoring - Interactive highlighting
 */
@Slf4j
@Component
public class UIDebugger {

    @Autowired private UIComponentRegistry componentRegistry;

    private boolean debugMode = false;
    private final Map<Node, String> originalBorders = new ConcurrentHashMap<>();
    private final Set<Node> highlightedNodes = ConcurrentHashMap.newKeySet();

    // Debug border styles
    private static final Border DEBUG_BORDER =
            new Border(
                    new BorderStroke(
                            Color.RED,
                            BorderStrokeStyle.DASHED,
                            new CornerRadii(2),
                            new BorderWidths(2)));

    private static final Border HIGHLIGHT_BORDER =
            new Border(
                    new BorderStroke(
                            Color.BLUE,
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(2),
                            new BorderWidths(3)));

    /** Enables debug mode for all components. */
    public void enableDebugMode() {
        debugMode = true;
        log.info("UI Debug mode enabled");
    }

    /** Disables debug mode and removes debug borders. */
    public void disableDebugMode() {
        debugMode = false;
        removeAllDebugBorders();
        log.info("UI Debug mode disabled");
    }

    /** Toggles debug mode. */
    public void toggleDebugMode() {
        if (debugMode) {
            disableDebugMode();
        } else {
            enableDebugMode();
        }
    }

    /**
     * Adds debug borders to a component tree.
     *
     * @param root The root component
     */
    public void addDebugBorders(Node root) {
        if (!debugMode) return;

        traverseTree(
                root,
                node -> {
                    if (!originalBorders.containsKey(node)) {
                        // Store original border
                        if (node instanceof Parent) {
                            Parent parent = (Parent) node;
                            originalBorders.put(node, parent.getStyle());
                            parent.setStyle(
                                    parent.getStyle()
                                            + "; -fx-border-color: red; -fx-border-style: dashed;"
                                            + " -fx-border-width: 1;");
                        }

                        // Add tooltip with component info
                        addDebugTooltip(node);
                    }
                });
    }

    /** Removes all debug borders. */
    private void removeAllDebugBorders() {
        originalBorders.forEach(
                (node, originalStyle) -> {
                    if (node instanceof Parent) {
                        ((Parent) node).setStyle(originalStyle != null ? originalStyle : "");
                    }
                });
        originalBorders.clear();
        highlightedNodes.clear();
    }

    /** Adds a debug tooltip to a node. */
    private void addDebugTooltip(Node node) {
        String info =
                String.format(
                        "Type: %s\nID: %s\nClasses: %s\nBounds: %s",
                        node.getClass().getSimpleName(),
                        node.getId() != null ? node.getId() : "none",
                        String.join(", ", node.getStyleClass()),
                        formatBounds(node.getBoundsInParent()));

        Tooltip tooltip = new Tooltip(info);
        Tooltip.install(node, tooltip);
    }

    /**
     * Logs the component tree structure.
     *
     * @param root The root node
     */
    public void logComponentTree(Node root) {
        log.info("=== Component Tree ===");
        logComponentTreeRecursive(root, 0);
        log.info("=== End Component Tree ===");
    }

    private void logComponentTreeRecursive(Node node, int depth) {
        String indent = "  ".repeat(depth);
        String nodeInfo =
                String.format(
                        "%s%s [id=%s, classes=%s]",
                        indent,
                        node.getClass().getSimpleName(),
                        node.getId(),
                        String.join(",", node.getStyleClass()));

        log.info(nodeInfo);

        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                logComponentTreeRecursive(child, depth + 1);
            }
        }
    }

    /**
     * Highlights a specific component by ID.
     *
     * @param componentId The component ID to highlight
     */
    public void highlightComponent(String componentId) {
        componentRegistry
                .get(componentId)
                .ifPresent(
                        node -> {
                            if (node instanceof Region) {
                                Region region = (Region) node;
                                region.setBorder(HIGHLIGHT_BORDER);
                                highlightedNodes.add(node);
                                log.info("Highlighted component: {}", componentId);
                            }
                        });
    }

    /** Removes highlighting from all components. */
    public void clearHighlights() {
        highlightedNodes.forEach(
                node -> {
                    if (node instanceof Region) {
                        ((Region) node).setBorder(null);
                    }
                });
        highlightedNodes.clear();
    }

    /**
     * Detects duplicate labels in a component tree.
     *
     * @param root The root node
     * @return Map of duplicate label texts to their locations
     */
    public Map<String, List<String>> detectDuplicateLabels(Node root) {
        Map<String, List<String>> labelOccurrences = new HashMap<>();

        traverseTree(
                root,
                node -> {
                    if (node instanceof Label) {
                        Label label = (Label) node;
                        String text = label.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            String location = getNodePath(label);
                            labelOccurrences
                                    .computeIfAbsent(text, k -> new ArrayList<>())
                                    .add(location);
                        }
                    }
                });

        // Filter to only duplicates
        Map<String, List<String>> duplicates = new HashMap<>();
        labelOccurrences.forEach(
                (text, locations) -> {
                    if (locations.size() > 1) {
                        duplicates.put(text, locations);
                    }
                });

        return duplicates;
    }

    /**
     * Logs duplicate labels found in the component tree.
     *
     * @param root The root node
     */
    public void logDuplicateLabels(Node root) {
        Map<String, List<String>> duplicates = detectDuplicateLabels(root);

        if (duplicates.isEmpty()) {
            log.info("No duplicate labels found");
        } else {
            log.warn("Found {} duplicate labels:", duplicates.size());
            duplicates.forEach(
                    (text, locations) -> {
                        log.warn("  '{}' appears {} times at:", text, locations.size());
                        locations.forEach(location -> log.warn("    - {}", location));
                    });
        }
    }

    /** Gets the path to a node in the component tree. */
    private String getNodePath(Node node) {
        List<String> path = new ArrayList<>();
        Node current = node;

        while (current != null) {
            String nodeDesc = current.getClass().getSimpleName();
            if (current.getId() != null) {
                nodeDesc += "#" + current.getId();
            }
            path.add(0, nodeDesc);
            current = current.getParent();
        }

        return String.join(" > ", path);
    }

    /** Traverses the component tree and applies an action to each node. */
    private void traverseTree(Node node, java.util.function.Consumer<Node> action) {
        if (node == null) return;

        action.accept(node);

        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                traverseTree(child, action);
            }
        }
    }

    /** Formats bounds for display. */
    private String formatBounds(Bounds bounds) {
        return String.format(
                "x=%.1f, y=%.1f, w=%.1f, h=%.1f",
                bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Creates a debug report for the current UI state.
     *
     * @param root The root node
     * @return Debug report as a string
     */
    public String createDebugReport(Node root) {
        StringBuilder report = new StringBuilder();

        report.append("=== UI Debug Report ===\n");
        report.append("Timestamp: ").append(new Date()).append("\n\n");

        // Component registry info
        report.append("Component Registry:\n");
        report.append("  Registered components: ").append(componentRegistry.size()).append("\n");

        // Duplicate labels
        Map<String, List<String>> duplicates = detectDuplicateLabels(root);
        report.append("\nDuplicate Labels: ").append(duplicates.size()).append("\n");
        duplicates.forEach(
                (text, locations) -> {
                    report.append("  '")
                            .append(text)
                            .append("' (")
                            .append(locations.size())
                            .append(" occurrences)\n");
                });

        // Component counts
        Map<String, Integer> componentCounts = new HashMap<>();
        traverseTree(
                root,
                node -> {
                    String type = node.getClass().getSimpleName();
                    componentCounts.merge(type, 1, Integer::sum);
                });

        report.append("\nComponent Counts:\n");
        componentCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(
                        entry -> {
                            report.append("  ")
                                    .append(entry.getKey())
                                    .append(": ")
                                    .append(entry.getValue())
                                    .append("\n");
                        });

        report.append("\n=== End Debug Report ===");

        return report.toString();
    }
}
