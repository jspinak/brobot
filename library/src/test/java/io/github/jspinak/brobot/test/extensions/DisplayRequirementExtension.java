package io.github.jspinak.brobot.test.extensions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import io.github.jspinak.brobot.test.EnvironmentDetector;
import io.github.jspinak.brobot.test.annotations.RequiresDisplay;

/** JUnit extension that evaluates whether tests requiring display should run. */
public class DisplayRequirementExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        // Check for @RequiresDisplay annotation
        boolean requiresDisplay =
                AnnotationSupport.isAnnotated(context.getElement(), RequiresDisplay.class);

        if (!requiresDisplay) {
            return ConditionEvaluationResult.enabled("Test does not require display");
        }

        // Get the annotation to retrieve custom message if provided
        String reason =
                context.getElement()
                        .flatMap(
                                element ->
                                        AnnotationSupport.findAnnotation(
                                                element, RequiresDisplay.class))
                        .map(RequiresDisplay::value)
                        .orElse("Requires real display for screen capture");

        // Check if we can run display-dependent tests
        if (EnvironmentDetector.canCaptureScreen()) {
            return ConditionEvaluationResult.enabled("Display available for screen capture");
        }

        // Build detailed skip reason
        StringBuilder skipReason = new StringBuilder();
        skipReason.append("Test skipped: ").append(reason).append(" | ");
        skipReason.append(EnvironmentDetector.getEnvironmentDescription());

        return ConditionEvaluationResult.disabled(skipReason.toString());
    }
}
