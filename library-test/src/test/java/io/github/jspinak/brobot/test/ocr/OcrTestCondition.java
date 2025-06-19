package io.github.jspinak.brobot.test.ocr;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 condition that evaluates whether OCR tests should be executed.
 * This condition checks multiple factors including system properties,
 * environment variables, and actual Tesseract availability.
 */
public class OcrTestCondition implements ExecutionCondition {
    
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        // Check if this is an OCR-dependent test
        boolean isOcrTest = context.getElement()
            .map(element -> element.isAnnotationPresent(OcrTestSupport.RequiresOcr.class))
            .orElse(false);
        
        if (!isOcrTest) {
            // Not an OCR test, proceed normally
            return ConditionEvaluationResult.enabled("Not an OCR-dependent test");
        }
        
        // Check if OCR tests should be disabled
        if (OcrTestSupport.shouldDisableOcrTests()) {
            String reason = OcrTestSupport.getTesseractError();
            if (reason == null) {
                reason = "OCR tests disabled";
            }
            return ConditionEvaluationResult.disabled("OCR test disabled: " + reason);
        }
        
        // Check if Tesseract is available
        if (!OcrTestSupport.isTesseractAvailable()) {
            String error = OcrTestSupport.getTesseractError();
            return ConditionEvaluationResult.disabled(
                "Tesseract OCR not available: " + (error != null ? error : "Unknown error")
            );
        }
        
        // Check for headless environment issues
        if (Boolean.parseBoolean(System.getProperty("java.awt.headless", "false"))) {
            // In headless mode, OCR might still work but with limitations
            String version = OcrTestSupport.getTesseractVersion();
            return ConditionEvaluationResult.enabled(
                "OCR test enabled in headless mode with " + version
            );
        }
        
        return ConditionEvaluationResult.enabled("OCR test enabled");
    }
}