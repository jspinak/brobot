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
 * Simple OCR test without Spring context to verify Tesseract configuration.
 */
public class SimpleOcrTest {

    @Test
    @DisabledIfSystemProperty(named = "brobot.tests.ocr.disable", matches = "true")
    void testSimpleOcr() throws IOException, TesseractException {
        System.out.println("Starting simple OCR test...");
        
        // Find a screenshot
        File screenshotDir = new File("library-test/screenshots");
        if (!screenshotDir.exists()) {
            screenshotDir = new File("screenshots");
        }
        
        File[] pngFiles = screenshotDir.listFiles((dir, name) -> name.endsWith(".png"));
        assertNotNull(pngFiles, "Should find PNG files");
        assertTrue(pngFiles.length > 0, "Should have at least one screenshot");
        
        File screenshot = pngFiles[0];
        System.out.println("Using screenshot: " + screenshot.getAbsolutePath());
        
        // Configure Tesseract
        Tesseract tesseract = new Tesseract();
        
        // Set data path
        String tessDataPath = System.getenv("TESSDATA_PREFIX");
        if (tessDataPath == null || tessDataPath.isEmpty()) {
            tessDataPath = "/usr/share/tesseract-ocr/4.00/tessdata";
        }
        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage("eng");
        
        System.out.println("TESSDATA_PREFIX: " + tessDataPath);
        
        // Perform OCR
        BufferedImage image = ImageIO.read(screenshot);
        String result = tesseract.doOCR(image);
        
        System.out.println("OCR Result length: " + result.length());
        assertNotNull(result, "OCR should return a result");
        
        // Print first 200 characters of result
        String preview = result.length() > 200 ? result.substring(0, 200) + "..." : result;
        System.out.println("OCR Result preview: " + preview);
    }
    
    @Test
    void testTesseractConfiguration() {
        System.out.println("Tesseract configuration test:");
        System.out.println("- Tesseract available: " + OcrTestSupport.isTesseractAvailable());
        System.out.println("- Tesseract version: " + OcrTestSupport.getTesseractVersion());
        System.out.println("- TESSDATA_PREFIX env: " + System.getenv("TESSDATA_PREFIX"));
        System.out.println("- brobot.tests.ocr.disable property: " + System.getProperty("brobot.tests.ocr.disable"));
        
        assertTrue(OcrTestSupport.isTesseractAvailable(), "Tesseract should be available");
    }
}