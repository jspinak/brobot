package io.github.jspinak.brobot.runner.ui.utils.verifiers;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class StyleDivergenceAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(StyleDivergenceAnalyzer.class);
    
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
    
    public static class StyleDivergenceResult {
        public final List<StyleDivergenceIssue> issues = new ArrayList<>();
        public double overallDivergenceScore = 0.0;
        public int totalDivergenceIssues = 0;
    }
    
    public StyleDivergenceResult analyzeStyleDivergence(List<Node> nodes) {
        StyleDivergenceResult result = new StyleDivergenceResult();
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
                        if (stroke.getTopStroke() != null && stroke.getTopStroke() instanceof Color) {
                            Color color = (Color) stroke.getTopStroke();
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
                            result.issues.add(new StyleDivergenceIssue(
                                node,
                                "Excessive Border",
                                String.format("%s has unexpected border styling", NodeDescriptionUtil.getNodeDescription(node)),
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
                            result.issues.add(new StyleDivergenceIssue(
                                node,
                                "Inline Border Style",
                                String.format("%s has inline border styling: %s", NodeDescriptionUtil.getNodeDescription(node), style),
                                0.7
                            ));
                            totalSeverity += 0.7;
                            issueCount++;
                        }
                    }
                    
                    // Check for excessive shadows
                    if (style.contains("-fx-effect") && style.contains("dropshadow")) {
                        result.issues.add(new StyleDivergenceIssue(
                            node,
                            "Excessive Shadow",
                            String.format("%s has drop shadow effect", NodeDescriptionUtil.getNodeDescription(node)),
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
                        if (!(fill.getFill() instanceof Color)) {
                            result.issues.add(new StyleDivergenceIssue(
                                node,
                                "Non-standard Background",
                                String.format("%s uses non-solid background fill", NodeDescriptionUtil.getNodeDescription(node)),
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
        
        return result;
    }
    
    private boolean isExpectedToHaveBorder(Node node) {
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
    
    public void logResults(StyleDivergenceResult result) {
        if (!result.issues.isEmpty()) {
            logger.error("STYLE DIVERGENCE FROM ATLANTAFX: {} issues", result.issues.size());
            logger.error("Overall divergence score: {:.2f} (0=perfect, 1=worst)", result.overallDivergenceScore);
            for (StyleDivergenceIssue issue : result.issues) {
                logger.error("  - [{}] {} (severity: {:.1f})", issue.issueType, issue.description, issue.severity);
            }
        }
    }
}