package io.github.jspinak.brobot.runner.ui.utils.verifiers;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerBoundaryChecker {
    private static final Logger logger = LoggerFactory.getLogger(ContainerBoundaryChecker.class);

    public static class ContainerViolation {
        public final Node node;
        public final Node container;
        public final String description;

        public ContainerViolation(Node node, Node container) {
            this.node = node;
            this.container = container;
            this.description =
                    String.format(
                            "%s extends outside its container %s",
                            NodeDescriptionUtil.getNodeDescription(node),
                            NodeDescriptionUtil.getNodeDescription(container));
        }
    }

    public List<ContainerViolation> checkContainerViolations(List<Node> nodes) {
        List<ContainerViolation> violations = new ArrayList<>();

        for (Node node : nodes) {
            if (!NodeVisibilityUtil.isNodeActuallyVisible(node)) continue;

            Parent parent = node.getParent();
            if (parent == null) continue;

            // Skip certain parent types that are expected to have overflow
            if (parent instanceof ScrollPane) continue;

            Bounds nodeBounds = node.localToParent(node.getBoundsInLocal());
            Bounds parentBounds = parent.getBoundsInLocal();

            // Check if node extends outside parent
            if (nodeBounds.getMinX() < 0
                    || nodeBounds.getMinY() < 0
                    || nodeBounds.getMaxX() > parentBounds.getWidth()
                    || nodeBounds.getMaxY() > parentBounds.getHeight()) {

                // Some overflow is acceptable for certain components
                if (!isAcceptableOverflow(node, parent)) {
                    violations.add(new ContainerViolation(node, parent));
                }
            }
        }

        return violations;
    }

    private boolean isAcceptableOverflow(Node node, Parent parent) {
        // Shadows and effects can extend outside
        if (node.getEffect() != null) return true;

        // Tab headers can extend outside their container
        if (node.getStyleClass().contains("tab-header")) return true;

        // Tooltips and popup-like elements can overflow
        if (node.getStyleClass().contains("tooltip") || node.getStyleClass().contains("popup"))
            return true;

        return false;
    }

    public void logResults(List<ContainerViolation> violations) {
        if (!violations.isEmpty()) {
            logger.error("CONTAINER BOUNDARY VIOLATIONS: {}", violations.size());
            for (ContainerViolation issue : violations) {
                logger.error("  - {}", issue.description);
            }
        }
    }
}
