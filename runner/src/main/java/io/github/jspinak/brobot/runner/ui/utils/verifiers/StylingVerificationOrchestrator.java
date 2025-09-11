package io.github.jspinak.brobot.runner.ui.utils.verifiers;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StylingVerificationOrchestrator {
    private static final Logger logger =
            LoggerFactory.getLogger(StylingVerificationOrchestrator.class);

    private final DuplicateDetector duplicateDetector = new DuplicateDetector();
    private final TabIsolationVerifier tabIsolationVerifier = new TabIsolationVerifier();
    private final ZOrderAnalyzer zOrderAnalyzer = new ZOrderAnalyzer();
    private final ContainerBoundaryChecker containerBoundaryChecker =
            new ContainerBoundaryChecker();
    private final ContrastChecker contrastChecker = new ContrastChecker();
    private final StyleDivergenceAnalyzer styleDivergenceAnalyzer = new StyleDivergenceAnalyzer();

    public static class VerificationResult {
        public final List<DuplicateDetector.DuplicateIssue> duplicates;
        public final List<TabIsolationVerifier.TabIsolationIssue> tabIsolationIssues;
        public final List<ZOrderAnalyzer.ZOrderIssue> zOrderIssues;
        public final List<ContainerBoundaryChecker.ContainerViolation> containerViolations;
        public final List<ContrastChecker.TextVisibilityIssue> textVisibilityIssues;
        public final StyleDivergenceAnalyzer.StyleDivergenceResult styleDivergenceResult;

        public VerificationResult(
                List<DuplicateDetector.DuplicateIssue> duplicates,
                List<TabIsolationVerifier.TabIsolationIssue> tabIsolationIssues,
                List<ZOrderAnalyzer.ZOrderIssue> zOrderIssues,
                List<ContainerBoundaryChecker.ContainerViolation> containerViolations,
                List<ContrastChecker.TextVisibilityIssue> textVisibilityIssues,
                StyleDivergenceAnalyzer.StyleDivergenceResult styleDivergenceResult) {

            this.duplicates = duplicates;
            this.tabIsolationIssues = tabIsolationIssues;
            this.zOrderIssues = zOrderIssues;
            this.containerViolations = containerViolations;
            this.textVisibilityIssues = textVisibilityIssues;
            this.styleDivergenceResult = styleDivergenceResult;
        }

        public int getTotalIssues() {
            return duplicates.size()
                    + tabIsolationIssues.size()
                    + zOrderIssues.size()
                    + containerViolations.size()
                    + textVisibilityIssues.size()
                    + styleDivergenceResult.issues.size();
        }
    }

    public VerificationResult verify(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            return new VerificationResult(
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new StyleDivergenceAnalyzer.StyleDivergenceResult());
        }

        // Collect all visible nodes
        List<Node> allNodes = new ArrayList<>();
        collectVisibleNodes(scene.getRoot(), allNodes);

        // Run all verifications
        var duplicates = duplicateDetector.detectDuplicates(allNodes);
        var tabIsolationIssues = tabIsolationVerifier.verifyTabIsolation(scene);
        var zOrderIssues = zOrderAnalyzer.analyzeZOrder(allNodes);
        var containerViolations = containerBoundaryChecker.checkContainerViolations(allNodes);
        var textVisibilityIssues = contrastChecker.checkTextVisibility(allNodes);
        var styleDivergenceResult = styleDivergenceAnalyzer.analyzeStyleDivergence(allNodes);

        return new VerificationResult(
                duplicates,
                tabIsolationIssues,
                zOrderIssues,
                containerViolations,
                textVisibilityIssues,
                styleDivergenceResult);
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

    public void logVerificationResults(VerificationResult result) {
        logger.info("=== Comprehensive Styling Verification Report ===");
        logger.info("Total issues found: {}", result.getTotalIssues());

        // Log each type of issue
        duplicateDetector.logResults(result.duplicates);
        tabIsolationVerifier.logResults(result.tabIsolationIssues);
        zOrderAnalyzer.logResults(result.zOrderIssues);
        containerBoundaryChecker.logResults(result.containerViolations);
        contrastChecker.logResults(result.textVisibilityIssues);
        styleDivergenceAnalyzer.logResults(result.styleDivergenceResult);

        if (result.getTotalIssues() == 0) {
            logger.info("No styling issues detected!");
        }
    }
}
