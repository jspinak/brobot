package io.github.jspinak.brobot.test.ocr;

import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base class for OCR-dependent tests.
 * Automatically applies OCR test conditions and provides helper methods.
 */
@ExtendWith(OcrTestCondition.class)
public abstract class OcrTestBase extends BrobotIntegrationTestBase {
    
    protected boolean ocrAvailable = false;
    protected String ocrVersion = null;
    
    @BeforeEach
    protected void checkOcrAvailability() {
        ocrAvailable = OcrTestSupport.isTesseractAvailable();
        ocrVersion = OcrTestSupport.getTesseractVersion();
        
        if (ocrAvailable) {
            System.out.println("OCR tests enabled with " + ocrVersion);
        } else {
            System.out.println("OCR tests disabled: " + OcrTestSupport.getTesseractError());
        }
    }
    
    /**
     * Helper method to handle OCR operations with proper error handling.
     * 
     * @param operation The OCR operation to perform
     * @return The result of the operation, or null if OCR is not available
     */
    protected <T> T performOcrOperation(OcrOperation<T> operation) {
        if (!ocrAvailable) {
            System.out.println("Skipping OCR operation - Tesseract not available");
            return null;
        }
        
        try {
            return operation.perform();
        } catch (UnsatisfiedLinkError | NoClassDefFoundError | ExceptionInInitializerError e) {
            System.err.println("OCR library error: " + e.getMessage());
            return null;
        } catch (java.awt.HeadlessException e) {
            System.err.println("OCR operation failed in headless mode: " + e.getMessage());
            return null;
        } catch (Exception e) {
            if (isOcrRelatedError(e)) {
                System.err.println("OCR operation failed: " + e.getMessage());
                return null;
            }
            // Re-throw non-OCR related exceptions
            throw new RuntimeException("Unexpected error in OCR operation", e);
        }
    }
    
    /**
     * Checks if an exception is related to OCR functionality.
     * 
     * @param e The exception to check
     * @return true if the exception is OCR-related
     */
    protected boolean isOcrRelatedError(Exception e) {
        if (e.getMessage() == null) {
            return false;
        }
        
        String message = e.getMessage().toLowerCase();
        return message.contains("ocr") || 
               message.contains("tesseract") || 
               message.contains("headless") ||
               message.contains("text recognition") ||
               message.contains("word detection");
    }
    
    /**
     * Functional interface for OCR operations.
     */
    @FunctionalInterface
    protected interface OcrOperation<T> {
        T perform() throws Exception;
    }
}