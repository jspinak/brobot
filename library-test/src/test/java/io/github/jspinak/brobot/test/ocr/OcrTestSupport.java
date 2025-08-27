package io.github.jspinak.brobot.test.ocr;

import java.lang.annotation.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.io.File;
import java.nio.file.Paths;

/**
 * Utility class for OCR-dependent test support.
 * Provides methods to detect Tesseract availability and support for tests using saved screenshots.
 * Tests no longer require live OCR since they use pre-saved FloraNext screenshots.
 */
public class OcrTestSupport {
    
    private static final AtomicBoolean tesseractChecked = new AtomicBoolean(false);
    private static final AtomicBoolean tesseractAvailable = new AtomicBoolean(false);
    private static final AtomicReference<String> tesseractVersion = new AtomicReference<>(null);
    private static final AtomicReference<String> tesseractError = new AtomicReference<>(null);
    private static File screenshotDirectory = null;
    
    /**
     * Gets the directory containing FloraNext screenshots.
     * 
     * @return File object for the screenshots directory, or null if not found
     */
    public static File getScreenshotDirectory() {
        if (screenshotDirectory == null) {
            String[] possiblePaths = {
                "library-test/screenshots",
                "screenshots",
                "library/screenshots",
                Paths.get("").toAbsolutePath().toString() + "/library-test/screenshots",
                Paths.get("").toAbsolutePath().toString() + "/library/screenshots"
            };
            
            for (String path : possiblePaths) {
                File dir = new File(path);
                if (dir.exists() && new File(dir, "floranext0.png").exists()) {
                    screenshotDirectory = dir;
                    break;
                }
            }
        }
        return screenshotDirectory;
    }
    
    /**
     * Checks if FloraNext screenshots are available for testing.
     * 
     * @return true if screenshots are available
     */
    public static boolean areScreenshotsAvailable() {
        File dir = getScreenshotDirectory();
        if (dir == null || !dir.exists()) {
            return false;
        }
        
        // Check for at least one FloraNext screenshot
        for (int i = 0; i <= 4; i++) {
            File screenshot = new File(dir, "floranext" + i + ".png");
            if (screenshot.exists()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if Tesseract OCR is available on the system.
     * This is optional for tests using saved screenshots.
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
     * Checks if tests can run with saved screenshots.
     * These tests don't require live OCR capability.
     * 
     * @return true if screenshot-based tests can run
     */
    public static boolean canRunScreenshotTests() {
        return areScreenshotsAvailable();
    }
    
    /**
     * Checks if tests requiring live OCR should run.
     * These tests need both Tesseract and appropriate permissions.
     * 
     * @return true if live OCR tests can run
     */
    public static boolean canRunLiveOcrTests() {
        // Check environment variable for CI/CD environments
        String envDisable = System.getenv("BROBOT_DISABLE_LIVE_OCR");
        if (envDisable != null && Boolean.parseBoolean(envDisable)) {
            return false;
        }
        
        // Check if running in headless mode
        if (Boolean.getBoolean("java.awt.headless")) {
            // In headless mode, only allow if screenshots are available
            return areScreenshotsAvailable();
        }
        
        // Otherwise check if Tesseract is available
        return isTesseractAvailable();
    }
    
    private static synchronized void checkTesseractAvailability() {
        if (tesseractChecked.get()) {
            return;
        }
        
        try {
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
            
            // Also check for tessdata
            checkTessdataAvailability();
            
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
    
    private static void checkTessdataAvailability() {
        // Check TESSDATA_PREFIX environment variable
        String tessDataPath = System.getenv("TESSDATA_PREFIX");
        
        if (tessDataPath == null || tessDataPath.isEmpty()) {
            // Check common locations
            String[] possiblePaths = {
                "/usr/share/tesseract-ocr/4.00/tessdata",
                "/usr/share/tesseract-ocr/tessdata",
                "/usr/local/share/tessdata",
                "/usr/share/tessdata"
            };
            
            for (String path : possiblePaths) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    // Check for English language data
                    File engData = new File(dir, "eng.traineddata");
                    if (engData.exists()) {
                        return; // Found tessdata
                    }
                }
            }
            
            // If we get here, tessdata wasn't found
            if (tesseractAvailable.get()) {
                tesseractError.set("Warning: Tesseract found but tessdata directory not located");
            }
        }
    }
    
    /**
     * Gets the path to tessdata directory.
     * 
     * @return Path to tessdata or null if not found
     */
    public static String getTessdataPath() {
        String tessDataPath = System.getenv("TESSDATA_PREFIX");
        if (tessDataPath != null && !tessDataPath.isEmpty()) {
            return tessDataPath;
        }
        
        // Check common locations
        String[] possiblePaths = {
            "/usr/share/tesseract-ocr/4.00/tessdata",
            "/usr/share/tesseract-ocr/tessdata",
            "/usr/local/share/tessdata",
            "/usr/share/tessdata"
        };
        
        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                File engData = new File(dir, "eng.traineddata");
                if (engData.exists()) {
                    return path;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Annotation to indicate tests that require FloraNext screenshots.
     * These tests can run in CI/CD environments.
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface RequiresScreenshots {
        String value() default "Test requires FloraNext screenshots to be available";
    }
    
    /**
     * Annotation to indicate tests that require live OCR capability.
     * These tests may not run in all environments.
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface RequiresLiveOcr {
        String value() default "Test requires live OCR capability with Tesseract";
    }
}