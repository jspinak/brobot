package io.github.jspinak.brobot.runner.ui.utils;

import io.github.jspinak.brobot.runner.ui.utils.verifiers.*;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive styling verification system that detects:
 * 1. Duplicate rendering of components
 * 2. Tab content isolation issues
 * 3. Z-order and layering problems
 * 4. Container boundary violations
 * 5. Text visibility and contrast issues
 * 
 * This class now delegates to specialized verifiers for better maintainability.
 */
public class ComprehensiveStylingVerifier {
    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveStylingVerifier.class);
    
    private static final StylingVerificationOrchestrator orchestrator = new StylingVerificationOrchestrator();
    
    public static class VerificationResult {
        public final List<DuplicateIssue> duplicates;
        public final List<TabIsolationIssue> tabIsolationIssues;
        public final List<ZOrderIssue> zOrderIssues;
        public final List<ContainerViolation> containerViolations;
        public final List<TextVisibilityIssue> textVisibilityIssues;
        public final List<StyleDivergenceIssue> styleDivergenceIssues;
        public double overallDivergenceScore = 0.0;
        public int totalDivergenceIssues = 0;
        
        // Adapter constructor to convert from orchestrator result
        private VerificationResult(StylingVerificationOrchestrator.VerificationResult orchestratorResult) {
            this.duplicates = new ArrayList<>();
            for (var duplicate : orchestratorResult.duplicates) {
                this.duplicates.add(new DuplicateIssue(duplicate.text, duplicate.nodes));
            }
            
            this.tabIsolationIssues = new ArrayList<>();
            for (var issue : orchestratorResult.tabIsolationIssues) {
                this.tabIsolationIssues.add(new TabIsolationIssue(issue.activeTab, issue.leakingTab, issue.leakingNode));
            }
            
            this.zOrderIssues = new ArrayList<>();
            for (var issue : orchestratorResult.zOrderIssues) {
                this.zOrderIssues.add(new ZOrderIssue(issue.frontNode, issue.backNode));
            }
            
            this.containerViolations = new ArrayList<>();
            for (var violation : orchestratorResult.containerViolations) {
                this.containerViolations.add(new ContainerViolation(violation.node, violation.container));
            }
            
            this.textVisibilityIssues = new ArrayList<>();
            for (var issue : orchestratorResult.textVisibilityIssues) {
                this.textVisibilityIssues.add(new TextVisibilityIssue(
                    issue.node, issue.text, issue.textColor, issue.backgroundColor, issue.contrastRatio));
            }
            
            this.styleDivergenceIssues = new ArrayList<>();
            for (var issue : orchestratorResult.styleDivergenceResult.issues) {
                this.styleDivergenceIssues.add(new StyleDivergenceIssue(
                    issue.node, issue.issueType, issue.description, issue.severity));
            }
            
            this.overallDivergenceScore = orchestratorResult.styleDivergenceResult.overallDivergenceScore;
            this.totalDivergenceIssues = orchestratorResult.styleDivergenceResult.totalDivergenceIssues;
        }
        
        public int getTotalIssues() {
            return duplicates.size() + tabIsolationIssues.size() + 
                   zOrderIssues.size() + containerViolations.size() + 
                   textVisibilityIssues.size() + styleDivergenceIssues.size();
        }
    }
    
    public static class DuplicateIssue {
        public final String text;
        public final List<javafx.scene.Node> nodes;
        public final String description;
        
        public DuplicateIssue(String text, List<javafx.scene.Node> nodes) {
            this.text = text;
            this.nodes = new ArrayList<>(nodes);
            this.description = String.format("Text '%s' appears %d times", text, nodes.size());
        }
    }
    
    public static class TabIsolationIssue {
        public final javafx.scene.control.Tab activeTab;
        public final javafx.scene.control.Tab leakingTab;
        public final javafx.scene.Node leakingNode;
        public final String description;
        
        public TabIsolationIssue(javafx.scene.control.Tab activeTab, javafx.scene.control.Tab leakingTab, javafx.scene.Node leakingNode) {
            this.activeTab = activeTab;
            this.leakingTab = leakingTab;
            this.leakingNode = leakingNode;
            this.description = String.format("Content from tab '%s' visible while tab '%s' is active: %s",
                leakingTab.getText(), activeTab.getText(), NodeDescriptionUtil.getNodeDescription(leakingNode));
        }
    }
    
    public static class ZOrderIssue {
        public final javafx.scene.Node frontNode;
        public final javafx.scene.Node backNode;
        public final String description;
        
        public ZOrderIssue(javafx.scene.Node frontNode, javafx.scene.Node backNode) {
            this.frontNode = frontNode;
            this.backNode = backNode;
            this.description = String.format("%s appears on top of %s",
                NodeDescriptionUtil.getNodeDescription(frontNode), NodeDescriptionUtil.getNodeDescription(backNode));
        }
    }
    
    public static class ContainerViolation {
        public final javafx.scene.Node node;
        public final javafx.scene.Node container;
        public final String description;
        
        public ContainerViolation(javafx.scene.Node node, javafx.scene.Node container) {
            this.node = node;
            this.container = container;
            this.description = String.format("%s extends outside its container %s",
                NodeDescriptionUtil.getNodeDescription(node), NodeDescriptionUtil.getNodeDescription(container));
        }
    }
    
    public static class TextVisibilityIssue {
        public final javafx.scene.Node node;
        public final String text;
        public final javafx.scene.paint.Color textColor;
        public final javafx.scene.paint.Color backgroundColor;
        public final double contrastRatio;
        public final String description;
        
        public TextVisibilityIssue(javafx.scene.Node node, String text, javafx.scene.paint.Color textColor, 
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
        public final javafx.scene.Node node;
        public final String issueType;
        public final String description;
        public final double severity; // 0.0 to 1.0
        
        public StyleDivergenceIssue(javafx.scene.Node node, String issueType, String description, double severity) {
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
        var orchestratorResult = orchestrator.verify(scene);
        return new VerificationResult(orchestratorResult);
    }
    
    /**
     * Logs the verification results
     */
    public static void logVerificationResults(VerificationResult result) {
        orchestrator.logVerificationResults(new StylingVerificationOrchestrator.VerificationResult(
            // Convert back to orchestrator types for logging
            result.duplicates.stream().map(d -> 
                new DuplicateDetector.DuplicateIssue(d.text, d.nodes)).toList(),
            result.tabIsolationIssues.stream().map(i -> 
                new TabIsolationVerifier.TabIsolationIssue(i.activeTab, i.leakingTab, i.leakingNode)).toList(),
            result.zOrderIssues.stream().map(z -> 
                new ZOrderAnalyzer.ZOrderIssue(z.frontNode, z.backNode)).toList(),
            result.containerViolations.stream().map(c -> 
                new ContainerBoundaryChecker.ContainerViolation(c.node, c.container)).toList(),
            result.textVisibilityIssues.stream().map(t -> 
                new ContrastChecker.TextVisibilityIssue(t.node, t.text, t.textColor, t.backgroundColor, t.contrastRatio)).toList(),
            new StyleDivergenceAnalyzer.StyleDivergenceResult() {{
                issues.addAll(result.styleDivergenceIssues.stream().map(s -> 
                    new StyleDivergenceAnalyzer.StyleDivergenceIssue(s.node, s.issueType, s.description, s.severity)).toList());
                overallDivergenceScore = result.overallDivergenceScore;
                totalDivergenceIssues = result.totalDivergenceIssues;
            }}
        ));
    }
}