package io.github.jspinak.brobot.test.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple OCR test using saved FloraNext screenshots.
 * Works in headless/CI environments since it uses pre-saved images.
 */
public class SimpleOcrTest {

    private static File screenshotDir;
    
    @BeforeAll
    static void setupScreenshotDirectory() {
        // Find the screenshots directory
        String[] possiblePaths = {
            "library-test/screenshots",
            "screenshots",
            Paths.get("").toAbsolutePath().toString() + "/library-test/screenshots"
        };
        
        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && new File(dir, "floranext0.png").exists()) {
                screenshotDir = dir;
                break;
            }
        }
        
        assertNotNull(screenshotDir, "Screenshots directory with FloraNext images should exist");
        System.out.println("Using screenshots from: " + screenshotDir.getAbsolutePath());
    }

    @Test
    void testOcrOnFloraNextScreenshot() throws IOException, TesseractException {
        System.out.println("Starting OCR test with FloraNext screenshots...");
        
        // Use the first FloraNext screenshot
        File screenshot = new File(screenshotDir, "floranext0.png");
        assertTrue(screenshot.exists(), "floranext0.png should exist");
        System.out.println("Using screenshot: " + screenshot.getAbsolutePath());
        
        // Configure Tesseract
        Tesseract tesseract = new Tesseract();
        
        // Set data path - try multiple locations
        String tessDataPath = System.getenv("TESSDATA_PREFIX");
        if (tessDataPath == null || tessDataPath.isEmpty()) {
            // Try common locations
            String[] possiblePaths = {
                "/usr/share/tesseract-ocr/4.00/tessdata",
                "/usr/share/tesseract-ocr/tessdata", 
                "/usr/local/share/tessdata"
            };
            
            for (String path : possiblePaths) {
                File tessDir = new File(path);
                if (tessDir.exists()) {
                    tessDataPath = path;
                    break;
                }
            }
        }
        
        if (tessDataPath != null && !tessDataPath.isEmpty()) {
            tesseract.setDatapath(tessDataPath);
            System.out.println("Using TESSDATA_PREFIX: " + tessDataPath);
        } else {
            System.out.println("Warning: TESSDATA_PREFIX not set, using default");
        }
        
        tesseract.setLanguage("eng");
        
        // Perform OCR
        BufferedImage image = ImageIO.read(screenshot);
        assertNotNull(image, "Image should be loaded successfully");
        System.out.println("Image dimensions: " + image.getWidth() + "x" + image.getHeight());
        
        String result = tesseract.doOCR(image);
        
        System.out.println("OCR Result length: " + result.length());
        assertNotNull(result, "OCR should return a result");
        
        // Print first 200 characters of result
        String preview = result.length() > 200 ? result.substring(0, 200) + "..." : result;
        System.out.println("OCR Result preview: " + preview);
        
        // FloraNext screenshots should contain some text
        assertTrue(result.length() > 0, "OCR should extract some text from FloraNext screenshot");
    }
    
    @Test
    void testOcrOnMultipleFloraNextScreenshots() throws IOException, TesseractException {
        System.out.println("Testing OCR on multiple FloraNext screenshots...");
        
        Tesseract tesseract = new Tesseract();
        configureTesseract(tesseract);
        
        int successfulReads = 0;
        
        // Test on all available FloraNext screenshots
        for (int i = 0; i <= 4; i++) {
            File screenshot = new File(screenshotDir, "floranext" + i + ".png");
            if (screenshot.exists()) {
                System.out.println("\nProcessing: " + screenshot.getName());
                
                BufferedImage image = ImageIO.read(screenshot);
                String result = tesseract.doOCR(image);
                
                if (result != null && result.length() > 0) {
                    successfulReads++;
                    System.out.println("- Extracted " + result.length() + " characters");
                    
                    // Show a sample of extracted text
                    String[] lines = result.split("\n");
                    if (lines.length > 0) {
                        System.out.println("- First line: " + (lines[0].length() > 50 ? 
                            lines[0].substring(0, 50) + "..." : lines[0]));
                    }
                }
            }
        }
        
        assertTrue(successfulReads > 0, "Should successfully read at least one FloraNext screenshot");
        System.out.println("\nSuccessfully processed " + successfulReads + " screenshots");
    }
    
    @Test
    void testTesseractConfiguration() {
        System.out.println("Tesseract configuration test:");
        
        // Check if Tesseract is available
        boolean tesseractAvailable = isTesseractAvailable();
        System.out.println("- Tesseract available: " + tesseractAvailable);
        
        // Get Tesseract version
        String version = getTesseractVersion();
        System.out.println("- Tesseract version: " + version);
        
        // Check environment variables
        System.out.println("- TESSDATA_PREFIX env: " + System.getenv("TESSDATA_PREFIX"));
        
        // Check for tessdata directory
        String[] possiblePaths = {
            "/usr/share/tesseract-ocr/4.00/tessdata",
            "/usr/share/tesseract-ocr/tessdata",
            "/usr/local/share/tessdata"
        };
        
        boolean tessdataFound = false;
        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists()) {
                System.out.println("- Found tessdata at: " + path);
                tessdataFound = true;
            }
        }
        
        if (!tessdataFound) {
            System.out.println("- Warning: No tessdata directory found in standard locations");
        }
        
        assertTrue(tesseractAvailable, "Tesseract should be available");
    }
    
    @Test
    void verifyFloraNextScreenshotsExist() {
        System.out.println("Verifying FloraNext screenshots...");
        
        int foundCount = 0;
        for (int i = 0; i <= 4; i++) {
            File screenshot = new File(screenshotDir, "floranext" + i + ".png");
            if (screenshot.exists()) {
                foundCount++;
                System.out.println("- Found: " + screenshot.getName() + 
                                 " (" + screenshot.length() + " bytes)");
            }
        }
        
        assertTrue(foundCount > 0, "Should find at least one FloraNext screenshot");
        System.out.println("Total FloraNext screenshots found: " + foundCount);
    }
    
    private void configureTesseract(Tesseract tesseract) {
        String tessDataPath = System.getenv("TESSDATA_PREFIX");
        if (tessDataPath == null || tessDataPath.isEmpty()) {
            String[] possiblePaths = {
                "/usr/share/tesseract-ocr/4.00/tessdata",
                "/usr/share/tesseract-ocr/tessdata",
                "/usr/local/share/tessdata"
            };
            
            for (String path : possiblePaths) {
                if (new File(path).exists()) {
                    tessDataPath = path;
                    break;
                }
            }
        }
        
        if (tessDataPath != null && !tessDataPath.isEmpty()) {
            tesseract.setDatapath(tessDataPath);
        }
        tesseract.setLanguage("eng");
    }
    
    private boolean isTesseractAvailable() {
        try {
            Tesseract tesseract = new Tesseract();
            configureTesseract(tesseract);
            // Try a simple operation to verify it works
            BufferedImage testImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            tesseract.doOCR(testImage);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String getTesseractVersion() {
        try {
            Process process = Runtime.getRuntime().exec("tesseract --version");
            process.waitFor();
            byte[] output = process.getInputStream().readAllBytes();
            String versionOutput = new String(output);
            if (versionOutput.length() > 0) {
                String[] lines = versionOutput.split("\n");
                if (lines.length > 0) {
                    return lines[0];
                }
            }
            return "Unknown";
        } catch (Exception e) {
            return "Not available: " + e.getMessage();
        }
    }
}