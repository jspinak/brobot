package io.github.jspinak.brobot.runner.ui.utils.verifiers;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TabIsolationVerifier {
    private static final Logger logger = LoggerFactory.getLogger(TabIsolationVerifier.class);
    
    public static class TabIsolationIssue {
        public final Tab activeTab;
        public final Tab leakingTab;
        public final Node leakingNode;
        public final String description;
        
        public TabIsolationIssue(Tab activeTab, Tab leakingTab, Node leakingNode) {
            this.activeTab = activeTab;
            this.leakingTab = leakingTab;
            this.leakingNode = leakingNode;
            this.description = String.format("Content from tab '%s' visible while tab '%s' is active: %s",
                leakingTab.getText(), activeTab.getText(), NodeDescriptionUtil.getNodeDescription(leakingNode));
        }
    }
    
    public List<TabIsolationIssue> verifyTabIsolation(Scene scene) {
        List<TabIsolationIssue> issues = new ArrayList<>();
        
        if (scene == null || scene.getRoot() == null) {
            return issues;
        }
        
        List<TabPane> tabPanes = NodeFinderUtil.findAllNodesOfType(scene.getRoot(), TabPane.class);
        
        for (TabPane tabPane : tabPanes) {
            Tab activeTab = tabPane.getSelectionModel().getSelectedItem();
            if (activeTab == null) continue;
            
            // Get bounds of the active tab's content area
            Node activeContent = activeTab.getContent();
            if (activeContent == null) continue;
            
            Bounds activeContentBounds = activeContent.localToScene(activeContent.getBoundsInLocal());
            
            // Check all other tabs
            for (Tab tab : tabPane.getTabs()) {
                if (tab == activeTab || tab.getContent() == null) continue;
                
                // Find all nodes in the inactive tab
                List<Node> inactiveTabNodes = new ArrayList<>();
                collectVisibleNodes(tab.getContent(), inactiveTabNodes);
                
                // Check if any nodes from inactive tab are visible in active tab area
                for (Node node : inactiveTabNodes) {
                    if (NodeVisibilityUtil.isNodeActuallyVisible(node)) {
                        Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());
                        if (nodeBounds.intersects(activeContentBounds)) {
                            issues.add(new TabIsolationIssue(activeTab, tab, node));
                        }
                    }
                }
            }
        }
        
        return issues;
    }
    
    private void collectVisibleNodes(Node node, List<Node> nodes) {
        if (node == null || !node.isVisible()) return;
        
        nodes.add(node);
        
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectVisibleNodes(child, nodes);
            }
        }
    }
    
    public void logResults(List<TabIsolationIssue> issues) {
        if (!issues.isEmpty()) {
            logger.error("TAB ISOLATION ISSUES: {}", issues.size());
            for (TabIsolationIssue issue : issues) {
                logger.error("  - {}", issue.description);
            }
        }
    }
}