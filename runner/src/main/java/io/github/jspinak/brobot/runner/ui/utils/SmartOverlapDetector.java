package io.github.jspinak.brobot.runner.ui.utils;

import java.util.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Smart overlap detector that intelligently filters out false positives and only reports genuine UI
 * layout issues.
 */
public class SmartOverlapDetector {
    private static final Logger logger = LoggerFactory.getLogger(SmartOverlapDetector.class);

    /** Node pair to track relationships */
    private static class NodePair {
        final Node node1;
        final Node node2;

        NodePair(Node n1, Node n2) {
            // Always put smaller hashcode first for consistent ordering
            if (n1.hashCode() < n2.hashCode()) {
                node1 = n1;
                node2 = n2;
            } else {
                node1 = n2;
                node2 = n1;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodePair nodePair = (NodePair) o;
            return Objects.equals(node1, nodePair.node1) && Objects.equals(node2, nodePair.node2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(node1, node2);
        }
    }

    /** Detects only genuine overlaps, filtering out all false positives. */
    public static List<OverlapDetector.OverlapInfo> detectGenuineOverlaps(Scene scene) {
        List<OverlapDetector.OverlapInfo> overlaps = new ArrayList<>();
        if (scene == null || scene.getRoot() == null) {
            return overlaps;
        }

        // Collect all visible nodes
        List<Node> allNodes = new ArrayList<>();
        collectVisibleNodes(scene.getRoot(), allNodes);

        // Track which pairs we've already checked
        Set<NodePair> checkedPairs = new HashSet<>();

        // Group nodes by their container
        Map<Parent, List<Node>> nodesByParent = groupNodesByParent(allNodes);

        // Only check nodes that are siblings (same parent) or in different branches
        for (Map.Entry<Parent, List<Node>> entry : nodesByParent.entrySet()) {
            List<Node> siblings = entry.getValue();

            // Check siblings against each other
            for (int i = 0; i < siblings.size(); i++) {
                for (int j = i + 1; j < siblings.size(); j++) {
                    Node node1 = siblings.get(i);
                    Node node2 = siblings.get(j);

                    NodePair pair = new NodePair(node1, node2);
                    if (checkedPairs.contains(pair)) continue;
                    checkedPairs.add(pair);

                    if (shouldCheckOverlap(node1, node2)) {
                        Bounds bounds1 = node1.localToScene(node1.getBoundsInLocal());
                        Bounds bounds2 = node2.localToScene(node2.getBoundsInLocal());

                        if (boundsOverlap(bounds1, bounds2)) {
                            Bounds intersection = getIntersection(bounds1, bounds2);
                            if (intersection.getWidth() > 1 && intersection.getHeight() > 1) {
                                overlaps.add(
                                        new OverlapDetector.OverlapInfo(
                                                node1, node2, intersection));
                            }
                        }
                    }
                }
            }
        }

        return overlaps;
    }

    /** Groups nodes by their immediate parent. */
    private static Map<Parent, List<Node>> groupNodesByParent(List<Node> nodes) {
        Map<Parent, List<Node>> result = new HashMap<>();
        for (Node node : nodes) {
            Parent parent = node.getParent();
            if (parent != null) {
                result.computeIfAbsent(parent, k -> new ArrayList<>()).add(node);
            }
        }
        return result;
    }

    /**
     * Determines if two nodes should be checked for overlap. This method contains all the
     * intelligence about what constitutes a "real" overlap.
     */
    private static boolean shouldCheckOverlap(Node node1, Node node2) {
        // Never check a node against itself
        if (node1 == node2) return false;

        // Never check parent-child relationships
        if (isParentOf(node1, node2) || isParentOf(node2, node1)) return false;

        // Skip if both are internal components of the same control
        if (areInternalComponents(node1, node2)) return false;

        // Skip tab-related internal overlaps
        if (isTabInternalOverlap(node1, node2)) return false;

        // Skip button-label overlaps (buttons contain labels internally)
        if (isButtonLabelOverlap(node1, node2)) return false;

        // Skip scroll pane internal overlaps
        if (isScrollPaneInternal(node1, node2)) return false;

        // Skip table internal overlaps
        if (isTableInternal(node1, node2)) return false;

        // Skip toolbar/button bar internal overlaps
        if (isToolBarInternal(node1, node2)) return false;

        // Skip overlaps between different tab contents
        if (areDifferentTabContents(node1, node2)) return false;

        // All checks passed - this is a legitimate overlap to check
        return true;
    }

    /** Checks if nodes are internal components of the same control. */
    private static boolean areInternalComponents(Node node1, Node node2) {
        // Find the first "significant" ancestor for each node
        Node control1 = findControlAncestor(node1);
        Node control2 = findControlAncestor(node2);

        // If they share the same control ancestor, they're internal
        return control1 != null && control1 == control2;
    }

    /** Finds the first significant control ancestor. */
    private static Node findControlAncestor(Node node) {
        Node current = node;
        while (current != null) {
            if (current instanceof Control
                    || current instanceof TitledPane
                    || current.getClass().getSimpleName().contains("Cell")
                    || current.getClass().getSimpleName().contains("Skin")) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    /** Checks for tab-related internal overlaps. */
    private static boolean isTabInternalOverlap(Node node1, Node node2) {
        String class1 = node1.getClass().getName();
        String class2 = node2.getClass().getName();

        // Check if both are part of tab structure
        boolean isTab1 = class1.contains("Tab") || hasTabAncestor(node1);
        boolean isTab2 = class2.contains("Tab") || hasTabAncestor(node2);

        if (isTab1 && isTab2) {
            // Check if they're from the same tab pane
            TabPane tabPane1 = findTabPaneAncestor(node1);
            TabPane tabPane2 = findTabPaneAncestor(node2);
            return tabPane1 != null && tabPane1 == tabPane2;
        }

        return false;
    }

    private static boolean hasTabAncestor(Node node) {
        Node current = node.getParent();
        while (current != null) {
            if (current.getClass().getName().contains("Tab")) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private static TabPane findTabPaneAncestor(Node node) {
        Node current = node.getParent();
        while (current != null) {
            if (current instanceof TabPane) {
                return (TabPane) current;
            }
            current = current.getParent();
        }
        return null;
    }

    /** Checks for button-label overlaps. */
    private static boolean isButtonLabelOverlap(Node node1, Node node2) {
        boolean isButton1 =
                node1 instanceof ButtonBase || node1.getClass().getName().contains("Button");
        boolean isButton2 =
                node2 instanceof ButtonBase || node2.getClass().getName().contains("Button");
        boolean isLabel1 =
                node1 instanceof Label
                        || node1 instanceof Text
                        || node1.getClass().getName().contains("Label")
                        || node1.getClass().getName().contains("Text");
        boolean isLabel2 =
                node2 instanceof Label
                        || node2 instanceof Text
                        || node2.getClass().getName().contains("Label")
                        || node2.getClass().getName().contains("Text");

        return (isButton1 && isLabel2) || (isButton2 && isLabel1);
    }

    /** Checks for scroll pane internal overlaps. */
    private static boolean isScrollPaneInternal(Node node1, Node node2) {
        ScrollPane sp1 = findScrollPaneAncestor(node1);
        ScrollPane sp2 = findScrollPaneAncestor(node2);
        return sp1 != null && sp1 == sp2;
    }

    private static ScrollPane findScrollPaneAncestor(Node node) {
        Node current = node.getParent();
        while (current != null) {
            if (current instanceof ScrollPane) {
                return (ScrollPane) current;
            }
            current = current.getParent();
        }
        return null;
    }

    /** Checks for table internal overlaps. */
    private static boolean isTableInternal(Node node1, Node node2) {
        TableView<?> table1 = findTableViewAncestor(node1);
        TableView<?> table2 = findTableViewAncestor(node2);
        return table1 != null && table1 == table2;
    }

    private static TableView<?> findTableViewAncestor(Node node) {
        Node current = node.getParent();
        while (current != null) {
            if (current instanceof TableView) {
                return (TableView<?>) current;
            }
            current = current.getParent();
        }
        return null;
    }

    /** Checks for toolbar/button bar internal overlaps. */
    private static boolean isToolBarInternal(Node node1, Node node2) {
        Node toolbar1 = findToolBarAncestor(node1);
        Node toolbar2 = findToolBarAncestor(node2);
        return toolbar1 != null && toolbar1 == toolbar2;
    }

    private static Node findToolBarAncestor(Node node) {
        Node current = node.getParent();
        while (current != null) {
            if (current instanceof ToolBar
                    || current instanceof ButtonBar
                    || current.getStyleClass().contains("action-bar")
                    || current.getStyleClass().contains("button-bar")
                    || current.getStyleClass().contains("tool-bar")) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    /** Checks if nodes are in different tab contents. */
    private static boolean areDifferentTabContents(Node node1, Node node2) {
        Tab tab1 = findTabAncestor(node1);
        Tab tab2 = findTabAncestor(node2);

        // If they're in different tabs, don't check overlap
        return tab1 != null && tab2 != null && tab1 != tab2;
    }

    private static Tab findTabAncestor(Node node) {
        // Tabs don't directly contain nodes, so we need a different approach
        // This method would need access to the TabPane to properly find the tab
        // For now, return null as tabs can't be found this way
        return null;
    }

    /** Collects all visible nodes in the scene graph. */
    private static void collectVisibleNodes(Node node, List<Node> nodes) {
        if (node == null || !node.isVisible() || node.isDisabled()) {
            return;
        }

        // Only add nodes that could potentially overlap
        if (shouldIncludeNode(node)) {
            nodes.add(node);
        }

        // Recursively collect children
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectVisibleNodes(child, nodes);
            }
        }
    }

    /** Determines if a node should be included in overlap checking. */
    private static boolean shouldIncludeNode(Node node) {
        // Skip invisible or very small nodes
        Bounds bounds = node.getBoundsInLocal();
        if (bounds.getWidth() < 1 || bounds.getHeight() < 1) {
            return false;
        }

        // Skip pure containers that don't render content
        if (node instanceof Pane && !(node instanceof Control)) {
            // But include styled panes
            if (node.getStyleClass().isEmpty() && node.getStyle().isEmpty()) {
                return false;
            }
        }

        // Skip shapes used for styling
        if (node.getClass().getName().contains("Shape")
                && !node.getClass().getName().contains("Text")) {
            return false;
        }

        return true;
    }

    /** Checks if two bounds overlap. */
    private static boolean boundsOverlap(Bounds b1, Bounds b2) {
        return b1.intersects(b2);
    }

    /** Calculates the intersection of two bounds. */
    private static Bounds getIntersection(Bounds b1, Bounds b2) {
        double minX = Math.max(b1.getMinX(), b2.getMinX());
        double minY = Math.max(b1.getMinY(), b2.getMinY());
        double maxX = Math.min(b1.getMaxX(), b2.getMaxX());
        double maxY = Math.min(b1.getMaxY(), b2.getMaxY());

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    /** Checks if node1 is a parent of node2. */
    private static boolean isParentOf(Node parent, Node child) {
        Node current = child.getParent();
        while (current != null) {
            if (current == parent) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /** Logs overlaps using the smart detection. */
    public static void logSmartOverlaps(Scene scene) {
        List<OverlapDetector.OverlapInfo> overlaps = detectGenuineOverlaps(scene);
        logger.info("=== Smart Overlap Detection Report ===");
        logger.info("Total genuine overlaps detected: {}", overlaps.size());

        for (int i = 0; i < overlaps.size(); i++) {
            OverlapDetector.OverlapInfo info = overlaps.get(i);
            logger.info("Overlap #{}: {}", i + 1, info.description);
            logger.info(
                    "  Intersection: x={}, y={}, width={}, height={}",
                    info.intersection.getMinX(),
                    info.intersection.getMinY(),
                    info.intersection.getWidth(),
                    info.intersection.getHeight());
        }
    }
}
