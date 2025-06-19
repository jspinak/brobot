package io.github.jspinak.brobot.test.ocr;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.annotation.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for OCR-dependent test support.
 * Provides methods to detect Tesseract availability and annotations for conditional test execution.
 */
public class OcrTestSupport {
    
    private static final AtomicBoolean tesseractChecked = new AtomicBoolean(false);
    private static final AtomicBoolean tesseractAvailable = new AtomicBoolean(false);
    private static final AtomicReference<String> tesseractVersion = new AtomicReference<>(null);
    private static final AtomicReference<String> tesseractError = new AtomicReference<>(null);
    
    /**
     * Checks if Tesseract OCR is available on the system.
     * This method caches the result to avoid repeated checks.
     * 
     * @return true if Tesseract is available and functioning
     */
    public static boolean isTesseractAvailable() {
        if (!tesseractChecked.get()) {
            checkTesseractAvailability();
        }
        return tesseractAvailable.get();
    }
    
    /**
     * Gets the Tesseract version if available.
     * 
     * @return Tesseract version string or null if not available
     */
    public static String getTesseractVersion() {
        if (!tesseractChecked.get()) {
            checkTesseractAvailability();
        }
        return tesseractVersion.get();
    }
    
    /**
     * Gets the error message if Tesseract is not available.
     * 
     * @return Error message or null if Tesseract is available
     */
    public static String getTesseractError() {
        if (!tesseractChecked.get()) {
            checkTesseractAvailability();
        }
        return tesseractError.get();
    }
    
    /**
     * Checks if OCR tests should be disabled based on system properties.
     * 
     * @return true if OCR tests should be disabled
     */
    public static boolean shouldDisableOcrTests() {
        // Check system property first
        if (Boolean.parseBoolean(System.getProperty("brobot.tests.ocr.disable", "false"))) {
            return true;
        }
        
        // Check environment variable
        String envDisable = System.getenv("BROBOT_DISABLE_OCR_TESTS");
        if (envDisable != null && Boolean.parseBoolean(envDisable)) {
            return true;
        }
        
        // Check if Tesseract is available
        return !isTesseractAvailable();
    }
    
    private static synchronized void checkTesseractAvailability() {
        if (tesseractChecked.get()) {
            return;
        }
        
        try {
            // First check if we should skip OCR tests entirely
            if (Boolean.parseBoolean(System.getProperty("brobot.tests.ocr.disable", "false"))) {
                tesseractAvailable.set(false);
                tesseractError.set("OCR tests disabled by system property");
                tesseractChecked.set(true);
                return;
            }
            
            // Try to execute tesseract command to check availability
            ProcessBuilder pb = new ProcessBuilder("tesseract", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // Read version info
                try (var reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
                    String line = reader.readLine();
                    if (line != null && line.contains("tesseract")) {
                        tesseractVersion.set(line);
                        tesseractAvailable.set(true);
                    }
                }
            } else {
                tesseractError.set("Tesseract command failed with exit code: " + exitCode);
            }
        } catch (java.io.IOException e) {
            tesseractError.set("Tesseract not found in PATH: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            tesseractError.set("Tesseract check interrupted: " + e.getMessage());
        } catch (Exception e) {
            tesseractError.set("Error checking Tesseract: " + e.getMessage());
        } finally {
            tesseractChecked.set(true);
        }
    }
    
    /**
     * Annotation to disable tests when OCR is not available.
     * This combines system property check with runtime Tesseract detection.
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @DisabledIfSystemProperty(named = "brobot.tests.ocr.disable", matches = "true", 
                              disabledReason = "OCR tests disabled by configuration")
    public @interface RequiresOcr {
    }
    
    /**
     * Annotation to enable tests only when OCR is explicitly enabled.
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @EnabledIfSystemProperty(named = "brobot.tests.ocr.enable", matches = "true",
                            disabledReason = "OCR tests not explicitly enabled")
    public @interface EnabledIfOcrAvailable {
    }
}