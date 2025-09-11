package io.github.jspinak.brobot.test.ocr;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 condition that evaluates whether OCR tests should be executed. This condition checks
 * multiple factors including system properties, environment variables, and actual Tesseract
 * availability.
 */
public class OcrTestCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        // Check if this is a screenshot-based test
        boolean isScreenshotTest =
                context.getElement()
                        .map(
                                element ->
                                        element.isAnnotationPresent(
                                                OcrTestSupport.RequiresScreenshots.class))
                        .orElse(false);

        if (!isScreenshotTest) {
            // Check for legacy live OCR tests
            boolean isLiveOcrTest =
                    context.getElement()
                            .map(
                                    element ->
                                            element.isAnnotationPresent(
                                                    OcrTestSupport.RequiresLiveOcr.class))
                            .orElse(false);

            if (!isLiveOcrTest) {
                // Not an OCR-related test, proceed normally
                return ConditionEvaluationResult.enabled("Not an OCR-dependent test");
            }
        }

        // For screenshot-based tests, only check if screenshots are available
        if (isScreenshotTest) {
            if (!OcrTestSupport.areScreenshotsAvailable()) {
                return ConditionEvaluationResult.disabled("FloraNext screenshots not available");
            }
            return ConditionEvaluationResult.enabled("Screenshot-based test enabled");
        }

        // Check if Tesseract is available
        if (!OcrTestSupport.isTesseractAvailable()) {
            String error = OcrTestSupport.getTesseractError();
            return ConditionEvaluationResult.disabled(
                    "Tesseract OCR not available: " + (error != null ? error : "Unknown error"));
        }

        // Check for headless environment issues
        if (Boolean.parseBoolean(System.getProperty("java.awt.headless", "false"))) {
            // In headless mode, OCR might still work but with limitations
            String version = OcrTestSupport.getTesseractVersion();
            return ConditionEvaluationResult.enabled(
                    "OCR test enabled in headless mode with " + version);
        }

        return ConditionEvaluationResult.enabled("OCR test enabled");
    }
}
