package io.github.jspinak.brobot.runner.ui.utils;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive styling verification system that detects:
 * 1. Duplicate rendering of components
 * 2. Tab content isolation issues
 * 3. Z-order and layering problems
 * 4. Container boundary violations
 * 5. Text visibility and contrast issues
 */
public class ComprehensiveStylingVerifier {
    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveStylingVerifier.class);
    
    // Minimum contrast ratios for WCAG compliance
    private static final double MIN_CONTRAST_RATIO_NORMAL = 4.5; // WCAG AA for normal text
    private static final double MIN_CONTRAST_RATIO_LARGE = 3.0;  // WCAG AA for large text (18pt+)
    
    public static class VerificationResult {
        public final List<DuplicateIssue> duplicates = new ArrayList<>();
        public final List<TabIsolationIssue> tabIsolationIssues = new ArrayList<>();
        public final List<ZOrderIssue> zOrderIssues = new ArrayList<>();
        public final List<ContainerViolation> containerViolations = new ArrayList<>();
        public final List<TextVisibilityIssue> textVisibilityIssues = new ArrayList<>();
        public final List<StyleDivergenceIssue> styleDivergenceIssues = new ArrayList<>();
        public double overallDivergenceScore = 0.0;
        public int totalDivergenceIssues = 0;
        
        public int getTotalIssues() {
            return duplicates.size() + tabIsolationIssues.size() + 
                   zOrderIssues.size() + containerViolations.size() + 
                   textVisibilityIssues.size() + styleDivergenceIssues.size();
        }
    }
    
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
                leakingTab.getText(), activeTab.getText(), getNodeDescription(leakingNode));
        }
    }
    
    public static class ZOrderIssue {
        public final Node frontNode;
        public final Node backNode;
        public final String description;
        
        public ZOrderIssue(Node frontNode, Node backNode) {
            this.frontNode = frontNode;
            this.backNode = backNode;
            this.description = String.format("%s appears on top of %s",
                getNodeDescription(frontNode), getNodeDescription(backNode));
        }
    }
    
    public static class ContainerViolation {
        public final Node node;
        public final Node container;
        public final String description;
        
        public ContainerViolation(Node node, Node container) {
            this.node = node;
            this.container = container;
            this.description = String.format("%s extends outside its container %s",
                getNodeDescription(node), getNodeDescription(container));
        }
    }
    
    public static class TextVisibilityIssue {
        public final Node node;
        public final String text;
        public final javafx.scene.paint.Color textColor;
        public final javafx.scene.paint.Color backgroundColor;
        public final double contrastRatio;
        public final String description;
        
        public TextVisibilityIssue(Node node, String text, javafx.scene.paint.Color textColor, 
                                  javafx.scene.paint.Color backgroundColor, double contrastRatio) {
            this.node = node;
            this.text = text;
            this.textColor = textColor;
            this.backgroundColor = backgroundColor;
            this.contrastRatio = contrastRatio;
            this.description = String.format(
                "Text '%s' has poor contrast (%.2f:1) - %s on %s",
                text.length() > 30 ? text.substring(0, 30) + "..." : text,
                contrastRatio,
                formatColor(textColor),
                formatColor(backgroundColor)
            );
        }
        
        private static String formatColor(javafx.scene.paint.Color color) {
            if (color == null) return "transparent";
            return String.format("rgb(%d,%d,%d)", 
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
        }
    }
    
    public static class StyleDivergenceIssue {
        public final Node node;
        public final String issueType;
        public final String description;
        public final double severity; // 0.0 to 1.0
        
        public StyleDivergenceIssue(Node node, String issueType, String description, double severity) {
            this.node = node;
            this.issueType = issueType;
            this.description = description;
            this.severity = severity;
        }
    }
    
    /**
     * Performs comprehensive styling verification on the scene
     */
    public static VerificationResult verify(Scene scene) {
        VerificationResult result = new VerificationResult();
        
        if (scene == null || scene.getRoot() == null) {
            return result;
        }
        
        // Collect all visible nodes
        List<Node> allNodes = new ArrayList<>();
        collectVisibleNodes(scene.getRoot(), allNodes);
        
        // 1. Check for duplicate text rendering
        checkDuplicateRendering(allNodes, result);
        
        // 2. Check tab content isolation
        checkTabContentIsolation(scene, result);
        
        // 3. Check z-order issues
        checkZOrderIssues(allNodes, result);
        
        // 4. Check container boundary violations
        checkContainerViolations(allNodes, result);
        
        // 5. Check text visibility and contrast
        checkTextVisibility(allNodes, result);
        
        // 6. Check style divergence from AtlantaFX
        checkStyleDivergence(allNodes, result);
        
        return result;
    }
    
    /**
     * Detects duplicate rendering of text
     */
    private static void checkDuplicateRendering(List<Node> nodes, VerificationResult result) {
        Map<String, List<Node>> textOccurrences = new HashMap<>();
        
        for (Node node : nodes) {
            // Skip LabeledText nodes that are children of Label nodes (JavaFX internal structure)
            if (node.getClass().getName().contains("LabeledText") && 
                node.getParent() != null && 
                node.getParent() instanceof javafx.scene.control.Label) {
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
                List<Node> visibleDuplicates = entry.getValue().stream()
                    .filter(n -> isNodeActuallyVisible(n))
                    .collect(Collectors.toList());
                    
                if (visibleDuplicates.size() > 1) {
                    result.duplicates.add(new DuplicateIssue(entry.getKey(), visibleDuplicates));
                }
            }
        }
    }
    
    /**
     * Checks if content from inactive tabs is visible
     */
    private static void checkTabContentIsolation(Scene scene, VerificationResult result) {
        List<TabPane> tabPanes = findAllNodesOfType(scene.getRoot(), TabPane.class);
        
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
                    if (isNodeActuallyVisible(node)) {
                        Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());
                        if (nodeBounds.intersects(activeContentBounds)) {
                            result.tabIsolationIssues.add(new TabIsolationIssue(activeTab, tab, node));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Checks for z-order issues where elements appear on top incorrectly
     */
    private static void checkZOrderIssues(List<Node> nodes, VerificationResult result) {
        // Group nodes by their scene coordinates
        Map<String, List<Node>> nodesByLocation = new HashMap<>();
        
        for (Node node : nodes) {
            if (!isNodeActuallyVisible(node)) continue;
            
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
                                result.zOrderIssues.add(new ZOrderIssue(front, back));
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Checks for nodes extending outside their containers
     */
    private static void checkContainerViolations(List<Node> nodes, VerificationResult result) {
        for (Node node : nodes) {
            if (!isNodeActuallyVisible(node)) continue;
            
            Parent parent = node.getParent();
            if (parent == null) continue;
            
            // Skip certain parent types that are expected to have overflow
            if (parent instanceof ScrollPane) continue;
            
            Bounds nodeBounds = node.localToParent(node.getBoundsInLocal());
            Bounds parentBounds = parent.getBoundsInLocal();
            
            // Check if node extends outside parent
            if (nodeBounds.getMinX() < 0 || nodeBounds.getMinY() < 0 ||
                nodeBounds.getMaxX() > parentBounds.getWidth() ||
                nodeBounds.getMaxY() > parentBounds.getHeight()) {
                
                // Some overflow is acceptable for certain components
                if (!isAcceptableOverflow(node, parent)) {
                    result.containerViolations.add(new ContainerViolation(node, parent));
                }
            }
        }
    }
    
    /**
     * Collects all visible nodes recursively
     */
    private static void collectVisibleNodes(Node node, List<Node> nodes) {
        if (node == null || !node.isVisible()) return;
        
        nodes.add(node);
        
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectVisibleNodes(child, nodes);
            }
        }
    }
    
    /**
     * Extracts text from various node types
     */
    private static String extractText(Node node) {
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
    
    /**
     * Checks if text is common UI text that might appear multiple times
     */
    private static boolean isCommonUIText(String text) {
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
    
    /**
     * Checks if a node is actually visible (not just visible property)
     */
    private static boolean isNodeActuallyVisible(Node node) {
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
        Bounds bounds = node.getBoundsInLocal();
        return bounds.getWidth() > 0 && bounds.getHeight() > 0;
    }
    
    /**
     * Finds all nodes of a specific type
     */
    private static <T> List<T> findAllNodesOfType(Node root, Class<T> type) {
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
    
    /**
     * Checks if two nodes should not overlap
     */
    private static boolean shouldNotOverlap(Node node1, Node node2) {
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
    
    /**
     * Determines if node1 is in front of node2
     */
    private static boolean isInFront(Node node1, Node node2) {
        Parent commonParent = findCommonParent(node1, node2);
        if (commonParent == null) return false;
        
        List<Node> children = new ArrayList<>(commonParent.getChildrenUnmodifiable());
        int index1 = children.indexOf(node1);
        int index2 = children.indexOf(node2);
        
        // In JavaFX, later children appear on top
        return index1 > index2;
    }
    
    /**
     * Checks if the z-order is valid
     */
    private static boolean isValidZOrder(Node front, Node back) {
        // Dialogs and popup-like components should be in front
        if (front.getStyleClass().contains("dialog") || 
            front.getStyleClass().contains("popup")) return true;
        
        // Buttons should typically be in front of backgrounds
        if (front instanceof Button && !(back instanceof Button)) return true;
        
        // Tab content should be behind tab headers
        if (isTabContent(front) && isTabHeader(back)) return false;
        
        return true;
    }
    
    /**
     * Checks if overflow is acceptable for this node/parent combination
     */
    private static boolean isAcceptableOverflow(Node node, Parent parent) {
        // Shadows and effects can extend outside
        if (node.getEffect() != null) return true;
        
        // Tab headers can extend outside their container
        if (node.getStyleClass().contains("tab-header")) return true;
        
        // Tooltips and popup-like elements can overflow
        if (node.getStyleClass().contains("tooltip") || 
            node.getStyleClass().contains("popup")) return true;
        
        return false;
    }
    
    /**
     * Gets a description of the node for error messages
     */
    private static String getNodeDescription(Node node) {
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
    
    /**
     * Checks if node1 is a parent of node2
     */
    private static boolean isParentOf(Node parent, Node child) {
        Node current = child.getParent();
        while (current != null) {
            if (current == parent) return true;
            current = current.getParent();
        }
        return false;
    }
    
    /**
     * Finds the common parent of two nodes
     */
    private static Parent findCommonParent(Node node1, Node node2) {
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
    
    /**
     * Checks if node is tab content
     */
    private static boolean isTabContent(Node node) {
        return node.getStyleClass().contains("tab-content-area") ||
               node.getStyleClass().contains("tab-content");
    }
    
    /**
     * Checks if node is a tab header
     */
    private static boolean isTabHeader(Node node) {
        return node.getStyleClass().contains("tab-header-area") ||
               node.getStyleClass().contains("tab-header");
    }
    
    /**
     * Checks for text visibility and contrast issues
     */
    private static void checkTextVisibility(List<Node> nodes, VerificationResult result) {
        for (Node node : nodes) {
            if (!isNodeActuallyVisible(node)) continue;
            
            String text = extractText(node);
            if (text == null || text.trim().isEmpty()) continue;
            
            // Get text color
            javafx.scene.paint.Color textColor = getTextColor(node);
            if (textColor == null) continue;
            
            // Get effective background color
            javafx.scene.paint.Color backgroundColor = getEffectiveBackgroundColor(node);
            if (backgroundColor == null) {
                // If we can't determine background, assume white for light theme, dark for dark theme
                backgroundColor = javafx.scene.paint.Color.WHITE; // Default assumption
            }
            
            // Calculate contrast ratio
            double contrastRatio = calculateContrastRatio(textColor, backgroundColor);
            
            // Check if contrast meets WCAG standards
            boolean isLargeText = isLargeText(node);
            double minRatio = isLargeText ? MIN_CONTRAST_RATIO_LARGE : MIN_CONTRAST_RATIO_NORMAL;
            
            if (contrastRatio < minRatio) {
                result.textVisibilityIssues.add(
                    new TextVisibilityIssue(node, text, textColor, backgroundColor, contrastRatio)
                );
            }
        }
    }
    
    /**
     * Checks for style divergence from AtlantaFX clean design
     */
    private static void checkStyleDivergence(List<Node> nodes, VerificationResult result) {
        double totalSeverity = 0.0;
        int issueCount = 0;
        
        for (Node node : nodes) {
            if (!node.isVisible() || !node.isManaged()) continue;
            
            // Check for excessive borders
            if (node instanceof Region) {
                Region region = (Region) node;
                
                // Check border properties
                if (region.getBorder() != null && !region.getBorder().getStrokes().isEmpty()) {
                    boolean hasVisibleBorder = false;
                    for (var stroke : region.getBorder().getStrokes()) {
                        if (stroke.getTopStroke() != null && stroke.getTopStroke() instanceof javafx.scene.paint.Color) {
                            javafx.scene.paint.Color color = (javafx.scene.paint.Color) stroke.getTopStroke();
                            if (color.getOpacity() > 0) {
                                hasVisibleBorder = true;
                                break;
                            }
                        }
                    }
                    
                    if (hasVisibleBorder) {
                        // Check if this is an expected border (buttons, inputs) or excessive
                        boolean isExpectedBorder = isExpectedToHaveBorder(node);
                        if (!isExpectedBorder) {
                            result.styleDivergenceIssues.add(new StyleDivergenceIssue(
                                node,
                                "Excessive Border",
                                String.format("%s has unexpected border styling", getNodeDescription(node)),
                                0.8
                            ));
                            totalSeverity += 0.8;
                            issueCount++;
                        }
                    }
                }
                
                // Check inline styles for border specifications
                String style = region.getStyle();
                if (style != null) {
                    if (style.contains("-fx-border-") && !style.contains("-fx-border-color: transparent")) {
                        boolean hasVisibleBorder = !style.contains("-fx-border-width: 0");
                        if (hasVisibleBorder && !isExpectedToHaveBorder(node)) {
                            result.styleDivergenceIssues.add(new StyleDivergenceIssue(
                                node,
                                "Inline Border Style",
                                String.format("%s has inline border styling: %s", getNodeDescription(node), style),
                                0.7
                            ));
                            totalSeverity += 0.7;
                            issueCount++;
                        }
                    }
                    
                    // Check for excessive shadows
                    if (style.contains("-fx-effect") && style.contains("dropshadow")) {
                        result.styleDivergenceIssues.add(new StyleDivergenceIssue(
                            node,
                            "Excessive Shadow",
                            String.format("%s has drop shadow effect", getNodeDescription(node)),
                            0.3
                        ));
                        totalSeverity += 0.3;
                        issueCount++;
                    }
                }
                
                // Check for non-standard backgrounds
                if (region.getBackground() != null && !region.getBackground().getFills().isEmpty()) {
                    // Check if using gradients or patterns instead of solid colors
                    for (var fill : region.getBackground().getFills()) {
                        if (!(fill.getFill() instanceof javafx.scene.paint.Color)) {
                            result.styleDivergenceIssues.add(new StyleDivergenceIssue(
                                node,
                                "Non-standard Background",
                                String.format("%s uses non-solid background fill", getNodeDescription(node)),
                                0.5
                            ));
                            totalSeverity += 0.5;
                            issueCount++;
                        }
                    }
                }
            }
        }
        
        // Calculate overall divergence score
        double divergenceScore = issueCount > 0 ? totalSeverity / issueCount : 0.0;
        if (divergenceScore > 0.5) {
            logger.error("HIGH STYLE DIVERGENCE: Score {:.2f} with {} issues detected", divergenceScore, issueCount);
        } else if (divergenceScore > 0.3) {
            logger.warn("Moderate style divergence: Score {:.2f} with {} issues", divergenceScore, issueCount);
        }
        
        // Add to result for display
        result.overallDivergenceScore = divergenceScore;
        result.totalDivergenceIssues = issueCount;
    }
    
    /**
     * Determines if a node is expected to have a border based on AtlantaFX design
     */
    private static boolean isExpectedToHaveBorder(Node node) {
        // Buttons typically have borders in AtlantaFX
        if (node instanceof Button || node instanceof ButtonBase) return true;
        
        // Text fields and inputs have borders
        if (node instanceof TextField || node instanceof TextArea) return true;
        if (node instanceof ComboBox || node instanceof ChoiceBox) return true;
        
        // Tables and lists may have borders
        if (node instanceof TableView || node instanceof ListView) return true;
        
        // Check style classes for expected bordered components
        List<String> styleClasses = node.getStyleClass();
        for (String styleClass : styleClasses) {
            if (styleClass.contains("button") || styleClass.contains("field") ||
                styleClass.contains("input") || styleClass.contains("bordered")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets the text color of a node
     */
    private static javafx.scene.paint.Color getTextColor(Node node) {
        if (node instanceof Text) {
            return (javafx.scene.paint.Color) ((Text) node).getFill();
        } else if (node instanceof Label) {
            return (javafx.scene.paint.Color) ((Label) node).getTextFill();
        } else if (node instanceof Button) {
            return (javafx.scene.paint.Color) ((Button) node).getTextFill();
        }
        
        // Try to get from style
        String style = node.getStyle();
        if (style != null && style.contains("-fx-text-fill:")) {
            String colorStr = extractStyleValue(style, "-fx-text-fill");
            return parseColor(colorStr);
        }
        
        return null;
    }
    
    /**
     * Gets the effective background color of a node (traversing up the tree if needed)
     */
    private static javafx.scene.paint.Color getEffectiveBackgroundColor(Node node) {
        Node current = node;
        
        while (current != null) {
            javafx.scene.paint.Color color = getBackgroundColor(current);
            if (color != null && color.getOpacity() > 0) {
                return color;
            }
            current = current.getParent();
        }
        
        return null;
    }
    
    /**
     * Gets the background color of a node
     */
    private static javafx.scene.paint.Color getBackgroundColor(Node node) {
        if (node instanceof Region) {
            Region region = (Region) node;
            if (region.getBackground() != null && !region.getBackground().getFills().isEmpty()) {
                javafx.scene.paint.Paint fill = region.getBackground().getFills().get(0).getFill();
                if (fill instanceof javafx.scene.paint.Color) {
                    return (javafx.scene.paint.Color) fill;
                }
            }
        }
        
        // Try to get from style
        String style = node.getStyle();
        if (style != null && style.contains("-fx-background-color:")) {
            String colorStr = extractStyleValue(style, "-fx-background-color");
            return parseColor(colorStr);
        }
        
        return null;
    }
    
    /**
     * Checks if text is large (18pt+ or 14pt+ bold)
     */
    private static boolean isLargeText(Node node) {
        double fontSize = 12; // Default
        boolean isBold = false;
        
        // Get font size from style
        String style = node.getStyle();
        if (style != null && style.contains("-fx-font-size:")) {
            String sizeStr = extractStyleValue(style, "-fx-font-size");
            if (sizeStr != null) {
                sizeStr = sizeStr.replace("px", "").replace("pt", "").trim();
                try {
                    fontSize = Double.parseDouble(sizeStr);
                } catch (NumberFormatException e) {
                    // Use default
                }
            }
        }
        
        // Check if bold
        if (style != null && (style.contains("-fx-font-weight: bold") || style.contains("-fx-font-weight:bold"))) {
            isBold = true;
        }
        
        // WCAG large text: 18pt+ or 14pt+ bold
        return fontSize >= 18 || (fontSize >= 14 && isBold);
    }
    
    /**
     * Calculates contrast ratio between two colors
     */
    private static double calculateContrastRatio(javafx.scene.paint.Color color1, javafx.scene.paint.Color color2) {
        double l1 = getRelativeLuminance(color1);
        double l2 = getRelativeLuminance(color2);
        
        double lighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);
        
        return (lighter + 0.05) / (darker + 0.05);
    }
    
    /**
     * Calculates relative luminance of a color
     */
    private static double getRelativeLuminance(javafx.scene.paint.Color color) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        
        // Apply gamma correction
        r = r <= 0.03928 ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = g <= 0.03928 ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = b <= 0.03928 ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
        
        // Calculate luminance
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }
    
    /**
     * Extracts a style value from a style string
     */
    private static String extractStyleValue(String style, String property) {
        int index = style.indexOf(property);
        if (index == -1) return null;
        
        int start = index + property.length();
        while (start < style.length() && (style.charAt(start) == ':' || style.charAt(start) == ' ')) {
            start++;
        }
        
        int end = style.indexOf(';', start);
        if (end == -1) end = style.length();
        
        return style.substring(start, end).trim();
    }
    
    /**
     * Parses a color string into a Color object
     */
    private static javafx.scene.paint.Color parseColor(String colorStr) {
        if (colorStr == null) return null;
        
        try {
            // Remove any quotes
            colorStr = colorStr.replace("'", "").replace("\"", "").trim();
            
            // Handle hex colors
            if (colorStr.startsWith("#")) {
                return javafx.scene.paint.Color.web(colorStr);
            }
            
            // Handle rgb/rgba
            if (colorStr.startsWith("rgb")) {
                return javafx.scene.paint.Color.web(colorStr);
            }
            
            // Handle named colors
            return javafx.scene.paint.Color.web(colorStr);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Logs the verification results
     */
    public static void logVerificationResults(VerificationResult result) {
        logger.info("=== Comprehensive Styling Verification Report ===");
        logger.info("Total issues found: {}", result.getTotalIssues());
        
        // Log duplicate rendering issues
        if (!result.duplicates.isEmpty()) {
            logger.error("DUPLICATE RENDERING ISSUES: {}", result.duplicates.size());
            for (DuplicateIssue issue : result.duplicates) {
                logger.error("  - {}", issue.description);
            }
        }
        
        // Log tab isolation issues
        if (!result.tabIsolationIssues.isEmpty()) {
            logger.error("TAB ISOLATION ISSUES: {}", result.tabIsolationIssues.size());
            for (TabIsolationIssue issue : result.tabIsolationIssues) {
                logger.error("  - {}", issue.description);
            }
        }
        
        // Log z-order issues
        if (!result.zOrderIssues.isEmpty()) {
            logger.error("Z-ORDER ISSUES: {}", result.zOrderIssues.size());
            for (ZOrderIssue issue : result.zOrderIssues) {
                logger.error("  - {}", issue.description);
            }
        }
        
        // Log container violations
        if (!result.containerViolations.isEmpty()) {
            logger.error("CONTAINER BOUNDARY VIOLATIONS: {}", result.containerViolations.size());
            for (ContainerViolation issue : result.containerViolations) {
                logger.error("  - {}", issue.description);
            }
        }
        
        // Log text visibility issues
        if (!result.textVisibilityIssues.isEmpty()) {
            logger.error("TEXT VISIBILITY ISSUES: {}", result.textVisibilityIssues.size());
            for (TextVisibilityIssue issue : result.textVisibilityIssues) {
                logger.error("  - {}", issue.description);
            }
        }
        
        // Log style divergence issues
        if (!result.styleDivergenceIssues.isEmpty()) {
            logger.error("STYLE DIVERGENCE FROM ATLANTAFX: {} issues", result.styleDivergenceIssues.size());
            logger.error("Overall divergence score: {:.2f} (0=perfect, 1=worst)", result.overallDivergenceScore);
            for (StyleDivergenceIssue issue : result.styleDivergenceIssues) {
                logger.error("  - [{}] {} (severity: {:.1f})", issue.issueType, issue.description, issue.severity);
            }
        }
        
        if (result.getTotalIssues() == 0) {
            logger.info("No styling issues detected!");
        }
    }
}