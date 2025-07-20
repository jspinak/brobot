package io.github.jspinak.brobot.runner.ui.utils.verifiers;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ZOrderAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(ZOrderAnalyzer.class);
    
    public static class ZOrderIssue {
        public final Node frontNode;
        public final Node backNode;
        public final String description;
        
        public ZOrderIssue(Node frontNode, Node backNode) {
            this.frontNode = frontNode;
            this.backNode = backNode;
            this.description = String.format("%s appears on top of %s",
                NodeDescriptionUtil.getNodeDescription(frontNode), 
                NodeDescriptionUtil.getNodeDescription(backNode));
        }
    }
    
    public List<ZOrderIssue> analyzeZOrder(List<Node> nodes) {
        List<ZOrderIssue> issues = new ArrayList<>();
        
        // Group nodes by their scene coordinates
        Map<String, List<Node>> nodesByLocation = new HashMap<>();
        
        for (Node node : nodes) {
            if (!NodeVisibilityUtil.isNodeActuallyVisible(node)) continue;
            
            Bounds bounds = node.localToScene(node.getBoundsInLocal());
            String key = String.format("%.0f,%.0f", bounds.getMinX(), bounds.getMinY());
            nodesByLocation.computeIfAbsent(key, k -> new ArrayList<>()).add(node);
        }
        
        // Check for nodes at the same location but different z-order
        for (List<Node> nodesAtLocation : nodesByLocation.values()) {
            if (nodesAtLocation.size() > 1) {
                // Check if these nodes should be overlapping
                for (int i = 0; i < nodesAtLocation.size(); i++) {
                    for (int j = i + 1; j < nodesAtLocation.size(); j++) {
                        Node node1 = nodesAtLocation.get(i);
                        Node node2 = nodesAtLocation.get(j);
                        
                        if (shouldNotOverlap(node1, node2)) {
                            // Determine which is in front
                            Node front = isInFront(node1, node2) ? node1 : node2;
                            Node back = front == node1 ? node2 : node1;
                            
                            // Check if the z-order makes sense
                            if (!isValidZOrder(front, back)) {
                                issues.add(new ZOrderIssue(front, back));
                            }
                        }
                    }
                }
            }
        }
        
        return issues;
    }
    
    private boolean shouldNotOverlap(Node node1, Node node2) {
        // Parent-child relationships can overlap
        if (isParentOf(node1, node2) || isParentOf(node2, node1)) return false;
        
        // Check if they're in the same container
        Parent commonParent = findCommonParent(node1, node2);
        if (commonParent == null) return true;
        
        // Buttons and labels in the same container typically shouldn't overlap
        if ((node1 instanceof Button && node2 instanceof Label) ||
            (node1 instanceof Label && node2 instanceof Button)) {
            return true;
        }
        
        // Text nodes shouldn't overlap
        if ((node1 instanceof Text || node1 instanceof Label) &&
            (node2 instanceof Text || node2 instanceof Label)) {
            return true;
        }
        
        return false;
    }
    
    private boolean isInFront(Node node1, Node node2) {
        Parent commonParent = findCommonParent(node1, node2);
        if (commonParent == null) return false;
        
        List<Node> children = new ArrayList<>(commonParent.getChildrenUnmodifiable());
        int index1 = children.indexOf(node1);
        int index2 = children.indexOf(node2);
        
        // In JavaFX, later children appear on top
        return index1 > index2;
    }
    
    private boolean isValidZOrder(Node front, Node back) {
        // Dialogs and popup-like components should be in front
        if (front.getStyleClass().contains("dialog") || 
            front.getStyleClass().contains("popup")) return true;
        
        // Buttons should typically be in front of backgrounds
        if (front instanceof Button && !(back instanceof Button)) return true;
        
        // Tab content should be behind tab headers
        if (isTabContent(front) && isTabHeader(back)) return false;
        
        return true;
    }
    
    private boolean isParentOf(Node parent, Node child) {
        Node current = child.getParent();
        while (current != null) {
            if (current == parent) return true;
            current = current.getParent();
        }
        return false;
    }
    
    private Parent findCommonParent(Node node1, Node node2) {
        Set<Parent> parents1 = new HashSet<>();
        Parent p1 = node1.getParent();
        while (p1 != null) {
            parents1.add(p1);
            p1 = p1.getParent();
        }
        
        Parent p2 = node2.getParent();
        while (p2 != null) {
            if (parents1.contains(p2)) return p2;
            p2 = p2.getParent();
        }
        
        return null;
    }
    
    private boolean isTabContent(Node node) {
        return node.getStyleClass().contains("tab-content-area") ||
               node.getStyleClass().contains("tab-content");
    }
    
    private boolean isTabHeader(Node node) {
        return node.getStyleClass().contains("tab-header-area") ||
               node.getStyleClass().contains("tab-header");
    }
    
    public void logResults(List<ZOrderIssue> issues) {
        if (!issues.isEmpty()) {
            logger.error("Z-ORDER ISSUES: {}", issues.size());
            for (ZOrderIssue issue : issues) {
                logger.error("  - {}", issue.description);
            }
        }
    }
}