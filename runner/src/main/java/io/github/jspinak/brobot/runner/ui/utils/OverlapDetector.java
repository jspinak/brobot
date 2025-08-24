package io.github.jspinak.brobot.runner.ui.utils;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects overlapping UI elements, particularly text and labels.
 * This helps identify spacing and layout issues in the UI.
 */
public class OverlapDetector {
    private static final Logger logger = LoggerFactory.getLogger(OverlapDetector.class);
    
    public static class OverlapInfo {
        public final Node node1;
        public final Node node2;
        public final Bounds intersection;
        public final String description;
        
        public OverlapInfo(Node node1, Node node2, Bounds intersection) {
            this.node1 = node1;
            this.node2 = node2;
            this.intersection = intersection;
            this.description = String.format("%s overlaps with %s", 
                getNodeDescription(node1), 
                getNodeDescription(node2));
        }
        
        private static String getNodeDescription(Node node) {
            String desc = node.getClass().getSimpleName();
            if (node instanceof Labeled) {
                desc += ": " + ((Labeled) node).getText();
            } else if (node instanceof Text) {
                desc += ": " + ((Text) node).getText();
            }
            return desc;
        }
    }
    
    /**
     * Detects all overlapping elements in a scene.
     * 
     * @param scene The scene to analyze
     * @return List of overlap information
     */
    public static List<OverlapInfo> detectOverlaps(Scene scene) {
        List<OverlapInfo> overlaps = new ArrayList<>();
        if (scene == null || scene.getRoot() == null) {
            return overlaps;
        }
        
        List<Node> allNodes = new ArrayList<>();
        collectVisibleNodes(scene.getRoot(), allNodes);
        
        // Check all pairs of nodes for overlaps
        for (int i = 0; i < allNodes.size(); i++) {
            for (int j = i + 1; j < allNodes.size(); j++) {
                Node node1 = allNodes.get(i);
                Node node2 = allNodes.get(j);
                
                // Skip if one is a parent of the other
                if (isParentOf(node1, node2) || isParentOf(node2, node1)) {
                    continue;
                }
                
                // Skip button-internal overlaps (button with its own label/text)
                if (isButtonInternalOverlap(node1, node2)) {
                    continue;
                }
                
                Bounds bounds1 = node1.localToScene(node1.getBoundsInLocal());
                Bounds bounds2 = node2.localToScene(node2.getBoundsInLocal());
                
                if (boundsOverlap(bounds1, bounds2)) {
                    Bounds intersection = getIntersection(bounds1, bounds2);
                    // Only report significant overlaps (more than 1 pixel)
                    if (intersection.getWidth() > 1 && intersection.getHeight() > 1) {
                        overlaps.add(new OverlapInfo(node1, node2, intersection));
                    }
                }
            }
        }
        
        return overlaps;
    }
    
    /**
     * Logs all detected overlaps with details.
     */
    public static void logOverlaps(Scene scene) {
        List<OverlapInfo> overlaps = detectOverlaps(scene);
        
        logger.info("=== Overlap Detection Report ===");
        logger.info("Total overlaps detected: {}", overlaps.size());
        
        for (int i = 0; i < overlaps.size(); i++) {
            OverlapInfo overlap = overlaps.get(i);
            logger.info("Overlap #{}: {}", i + 1, overlap.description);
            logger.info("  Intersection: x={}, y={}, width={}, height={}", 
                overlap.intersection.getMinX(), 
                overlap.intersection.getMinY(),
                overlap.intersection.getWidth(), 
                overlap.intersection.getHeight());
        }
        
        if (overlaps.isEmpty()) {
            logger.info("No overlapping elements detected!");
        }
    }
    
    /**
     * Creates visual markers for overlapping areas.
     * Adds red rectangles to the scene showing overlap areas.
     * 
     * @param scene The scene to mark
     * @return List of marker rectangles added
     */
    public static List<Rectangle> markOverlaps(Scene scene) {
        List<Rectangle> markers = new ArrayList<>();
        List<OverlapInfo> overlaps = detectOverlaps(scene);
        
        if (scene.getRoot() instanceof Parent) {
            Parent root = (Parent) scene.getRoot();
            
            for (OverlapInfo overlap : overlaps) {
                Rectangle marker = new Rectangle(
                    overlap.intersection.getMinX(),
                    overlap.intersection.getMinY(),
                    overlap.intersection.getWidth(),
                    overlap.intersection.getHeight()
                );
                marker.setFill(Color.rgb(255, 0, 0, 0.3)); // Semi-transparent red
                marker.setStroke(Color.RED);
                marker.setStrokeWidth(2);
                marker.setMouseTransparent(true); // Don't interfere with UI
                
                // Add to root if it's a container that supports children
                if (root instanceof javafx.scene.Group) {
                    ((javafx.scene.Group) root).getChildren().add(marker);
                    markers.add(marker);
                } else if (root instanceof javafx.scene.layout.Pane) {
                    ((javafx.scene.layout.Pane) root).getChildren().add(marker);
                    markers.add(marker);
                }
            }
        }
        
        logger.info("Added {} overlap markers to scene", markers.size());
        return markers;
    }
    
    /**
     * Collects all visible nodes that might contain text.
     */
    private static void collectVisibleNodes(Node node, List<Node> nodes) {
        if (node == null || !node.isVisible() || node.getOpacity() == 0) {
            return;
        }
        
        // Only collect nodes that typically contain text or are interactive
        if (node instanceof Label || node instanceof Text || 
            node instanceof javafx.scene.control.Button ||
            node instanceof javafx.scene.control.TextField ||
            node instanceof javafx.scene.control.TextArea) {
            nodes.add(node);
        }
        
        // Recursively collect from children
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectVisibleNodes(child, nodes);
            }
        }
    }
    
    /**
     * Checks if two bounds overlap.
     */
    private static boolean boundsOverlap(Bounds b1, Bounds b2) {
        return b1.getMinX() < b2.getMaxX() && b1.getMaxX() > b2.getMinX() &&
               b1.getMinY() < b2.getMaxY() && b1.getMaxY() > b2.getMinY();
    }
    
    /**
     * Gets the intersection of two bounds.
     */
    private static Bounds getIntersection(Bounds b1, Bounds b2) {
        double minX = Math.max(b1.getMinX(), b2.getMinX());
        double minY = Math.max(b1.getMinY(), b2.getMinY());
        double maxX = Math.min(b1.getMaxX(), b2.getMaxX());
        double maxY = Math.min(b1.getMaxY(), b2.getMaxY());
        
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }
    
    /**
     * Checks if node1 is a parent of node2.
     */
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
    
    /**
     * Checks if this is a button-internal overlap (button with its own label/text).
     * This prevents false positives from JavaFX's internal button structure.
     */
    private static boolean isButtonInternalOverlap(Node node1, Node node2) {
        // Check class names for better detection
        String class1 = node1.getClass().getName();
        String class2 = node2.getClass().getName();
        
        // Skip BrobotButton internal overlaps
        if ((node1 instanceof javafx.scene.control.Button || class1.contains("BrobotButton")) && 
            (node2 instanceof Label || node2 instanceof Text)) {
            // Check if they share a common parent (likely the button skin)
            Node parent1 = node1.getParent();
            Node parent2 = node2.getParent();
            while (parent1 != null && parent2 != null) {
                if (parent1 == parent2) return true;
                // If one is a parent of the other, it's internal
                if (parent1 == node2 || parent2 == node1) return true;
                parent1 = parent1.getParent();
                parent2 = parent2.getParent();
            }
        }
        
        if ((node2 instanceof javafx.scene.control.Button || class2.contains("BrobotButton")) && 
            (node1 instanceof Label || node1 instanceof Text)) {
            // Check if they share a common parent (likely the button skin)
            Node parent1 = node1.getParent();
            Node parent2 = node2.getParent();
            while (parent1 != null && parent2 != null) {
                if (parent1 == parent2) return true;
                // If one is a parent of the other, it's internal
                if (parent1 == node2 || parent2 == node1) return true;
                parent1 = parent1.getParent();
                parent2 = parent2.getParent();
            }
        }
        
        // Skip tab-related overlaps
        if (class1.contains("Tab") || class2.contains("Tab")) {
            // Check if it's a tab header or tab button
            if (class1.contains("TabPaneSkin") || class2.contains("TabPaneSkin") ||
                class1.contains("TabHeaderSkin") || class2.contains("TabHeaderSkin") ||
                class1.contains("TabContentRegion") || class2.contains("TabContentRegion")) {
                return true;
            }
        }
        
        // Check for labeled controls with their internal labels
        if (node1 instanceof Labeled && node2 instanceof Label) {
            // Labeled controls often have internal label nodes
            return isParentOf(node1, node2);
        }
        if (node2 instanceof Labeled && node1 instanceof Label) {
            // Labeled controls often have internal label nodes
            return isParentOf(node2, node1);
        }
        
        // Check for LabeledText which is often inside buttons
        if (class1.contains("LabeledText") || class2.contains("LabeledText")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Analyzes spacing issues between elements.
     * Reports elements that are too close together.
     */
    public static void analyzeSpacing(Scene scene, double minSpacing) {
        logger.info("=== Spacing Analysis (minimum: {} pixels) ===", minSpacing);
        
        List<Node> allNodes = new ArrayList<>();
        collectVisibleNodes(scene.getRoot(), allNodes);
        
        int spacingIssues = 0;
        
        for (int i = 0; i < allNodes.size(); i++) {
            for (int j = i + 1; j < allNodes.size(); j++) {
                Node node1 = allNodes.get(i);
                Node node2 = allNodes.get(j);
                
                if (isParentOf(node1, node2) || isParentOf(node2, node1)) {
                    continue;
                }
                
                // Skip button-internal spacing checks
                if (isButtonInternalOverlap(node1, node2)) {
                    continue;
                }
                
                Bounds bounds1 = node1.localToScene(node1.getBoundsInLocal());
                Bounds bounds2 = node2.localToScene(node2.getBoundsInLocal());
                
                // Calculate minimum distance between bounds
                double distance = getMinDistance(bounds1, bounds2);
                
                if (distance < minSpacing && distance >= 0) {
                    spacingIssues++;
                    logger.warn("Spacing issue: {} and {} are only {} pixels apart", 
                        getNodeDescription(node1), 
                        getNodeDescription(node2), 
                        String.format("%.1f", distance));
                }
            }
        }
        
        logger.info("Total spacing issues found: {}", spacingIssues);
    }
    
    /**
     * Calculates minimum distance between two bounds.
     * Returns 0 if they overlap, otherwise the minimum gap.
     */
    private static double getMinDistance(Bounds b1, Bounds b2) {
        if (boundsOverlap(b1, b2)) {
            return 0;
        }
        
        double dx = 0;
        if (b1.getMaxX() < b2.getMinX()) {
            dx = b2.getMinX() - b1.getMaxX();
        } else if (b2.getMaxX() < b1.getMinX()) {
            dx = b1.getMinX() - b2.getMaxX();
        }
        
        double dy = 0;
        if (b1.getMaxY() < b2.getMinY()) {
            dy = b2.getMinY() - b1.getMaxY();
        } else if (b2.getMaxY() < b1.getMinY()) {
            dy = b1.getMinY() - b2.getMaxY();
        }
        
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    private static String getNodeDescription(Node node) {
        String desc = node.getClass().getSimpleName();
        if (node instanceof Labeled) {
            String text = ((Labeled) node).getText();
            if (text != null && !text.isEmpty()) {
                desc += ": \"" + (text.length() > 30 ? text.substring(0, 30) + "..." : text) + "\"";
            }
        } else if (node instanceof Text) {
            String text = ((Text) node).getText();
            if (text != null && !text.isEmpty()) {
                desc += ": \"" + (text.length() > 30 ? text.substring(0, 30) + "..." : text) + "\"";
            }
        }
        return desc;
    }
}