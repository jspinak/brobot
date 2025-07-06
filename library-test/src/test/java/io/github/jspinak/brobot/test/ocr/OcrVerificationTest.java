package io.github.jspinak.brobot.test.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify OCR functionality works with screenshots.
 */
public class OcrVerificationTest {

    @Test
    @DisabledIfSystemProperty(named = "brobot.tests.ocr.disable", matches = "true")
    void testOcrOnScreenshot() throws IOException, TesseractException {
        // Check if Tesseract is available
        assertTrue(OcrTestSupport.isTesseractAvailable(), 
            "Tesseract should be available. Version: " + OcrTestSupport.getTesseractVersion());
        
        // Try to find a screenshot file
        File screenshotDir = new File("library-test/screenshots");
        if (!screenshotDir.exists()) {
            screenshotDir = new File("screenshots");
        }
        
        assertTrue(screenshotDir.exists(), "Screenshot directory should exist at: " + screenshotDir.getAbsolutePath());
        
        File[] pngFiles = screenshotDir.listFiles((dir, name) -> name.endsWith(".png"));
        assertNotNull(pngFiles, "Should find PNG files");
        assertTrue(pngFiles.length > 0, "Should have at least one screenshot");
        
        // Try OCR on the first screenshot
        File screenshot = pngFiles[0];
        System.out.println("Testing OCR on: " + screenshot.getName());
        
        BufferedImage image = ImageIO.read(screenshot);
        assertNotNull(image, "Should be able to read image");
        
        Tesseract tesseract = new Tesseract();
        // Set data path for WSL environment
        String tessDataPath = System.getenv("TESSDATA_PREFIX");
        if (tessDataPath == null || tessDataPath.isEmpty()) {
            tessDataPath = "/usr/share/tesseract-ocr/4.00/tessdata";
        }
        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage("eng");
        
        String result = tesseract.doOCR(image);
        assertNotNull(result, "OCR should return a result");
        System.out.println("OCR Result preview: " + result.substring(0, Math.min(100, result.length())) + "...");
        
        // The result might be empty if the screenshot has no text, but it shouldn't be null
        assertTrue(result != null, "OCR should complete without errors");
    }
    
    @Test
    void testOcrSupportAvailability() {
        System.out.println("Tesseract available: " + OcrTestSupport.isTesseractAvailable());
        System.out.println("Tesseract version: " + OcrTestSupport.getTesseractVersion());
        
        if (!OcrTestSupport.isTesseractAvailable()) {
            System.out.println("Error: " + OcrTestSupport.getTesseractError());
        }
    }
}