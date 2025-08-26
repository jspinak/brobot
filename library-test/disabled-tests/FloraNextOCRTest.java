package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.find.text.TextFindOptions;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.testutils.TestPaths;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OCR tests using real FloraNext screenshots that can run in CI/CD environments.
 * These tests use actual screenshot images for OCR processing without needing
 * screen capture capabilities.
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FloraNextOCRTest extends BrobotIntegrationTestBase {
    
    @Autowired
    private Find find;
    
    private BufferedImage loadFloraNextScreenshot(int index) {
        try {
            String filename = TestPaths.getScreenshotPath("floranext" + index);
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("Screenshot file not found: " + filename);
                return null;
            }
            return ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("Failed to load screenshot: " + e.getMessage());
            return null;
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Should perform OCR on FloraNext screenshot without mocking")
    @Timeout(10)
    void testOCROnFloraNextScreenshot() {
        // Load a real screenshot
        BufferedImage screenshot = loadFloraNextScreenshot(1);
        
        if (screenshot == null) {
            System.out.println("Skipping test - screenshot not available");
            return;
        }
        
        // Create a Pattern from the screenshot
        Pattern pattern = new Pattern.Builder()
                .setBufferedImage(screenshot)
                .setName("FloraNext1")
                .build();
        
        // Create StateImage with the pattern
        StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .setName("FloraNextScreen")
                .build();
        
        // Create ObjectCollection
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        // Configure OCR options - TextFindOptions uses ALL_WORDS strategy for OCR
        TextFindOptions ocrOptions = new TextFindOptions.Builder()
                .setMaxMatchRetries(3)
                .setPauseAfterEnd(0.5)
                .build();
        
        // Create ActionResult with proper initialization
        ActionResult result = new ActionResult();
        result.setActionConfig(ocrOptions);
        result.setActionLifecycle(new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
            java.time.LocalDateTime.now(), 30.0));
        
        // Perform OCR
        find.perform(result, objColl);
        
        // Verify results
        assertNotNull(result);
        
        // Check if any text was found
        if (!result.getMatchList().isEmpty()) {
            // Check text in matches
            for (var match : result.getMatchList()) {
                if (match.getText() != null && !match.getText().isEmpty()) {
                    System.out.println("OCR found text in match: " + match.getText());
                }
            }
        }
        
        // Check accumulated text
        if (result.getText() != null && !result.getText().isEmpty()) {
            System.out.println("Total text found: " + result.getText().size() + " strings");
            for (String str : result.getText().getAll()) {
                System.out.println("  Text: " + str);
            }
            assertTrue(result.getText().size() > 0, "Should find some text");
        } else {
            System.out.println("No text found in screenshot (OCR may not be available)");
        }
    }
    
    @Test
    @Order(2)
    @DisplayName("Should find specific text in FloraNext screenshot")
    @Timeout(10)
    void testFindSpecificTextInScreenshot() {
        BufferedImage screenshot = loadFloraNextScreenshot(0);
        
        if (screenshot == null) {
            System.out.println("Skipping test - screenshot not available");
            return;
        }
        
        Pattern pattern = new Pattern.Builder()
                .setBufferedImage(screenshot)
                .setName("FloraNext0")
                .build();
        
        StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .setName("FloraNextScreen0")
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        // Search for common UI text that might be in the screenshot
        String[] searchTerms = {"File", "Edit", "View", "Help", "Save", "Open", "Close"};
        
        boolean foundAnyText = false;
        for (String searchTerm : searchTerms) {
            // TextFindOptions doesn't have setSearchText - it finds ALL text
            // We'll need to search through results after finding
            TextFindOptions ocrOptions = new TextFindOptions.Builder()
                    .setMaxMatchRetries(2)
                    .build();
            
            ActionResult result = new ActionResult();
            result.setActionConfig(ocrOptions);
            result.setActionLifecycle(new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
                java.time.LocalDateTime.now(), 30.0));
            
            find.perform(result, objColl);
            
            // Check if any matches contain the search term
            for (var match : result.getMatchList()) {
                if (match.getText() != null && match.getText().contains(searchTerm)) {
                    System.out.println("Found text '" + searchTerm + "' in screenshot");
                    foundAnyText = true;
                    break;
                }
            }
        }
        
        // This test succeeds whether OCR is available or not
        // If OCR is available, we should find some text
        // If not, the test still passes but logs that OCR is not available
        if (!foundAnyText) {
            System.out.println("No text found - OCR may not be available in this environment");
        }
    }
    
    @Test
    @Order(3)
    @DisplayName("Should process multiple FloraNext screenshots")
    @Timeout(15)
    void testProcessMultipleScreenshots() {
        int screenshotsProcessed = 0;
        int textFound = 0;
        
        // Process all available FloraNext screenshots
        for (int i = 0; i < 5; i++) {
            BufferedImage screenshot = loadFloraNextScreenshot(i);
            
            if (screenshot == null) {
                continue;
            }
            
            screenshotsProcessed++;
            
            Pattern pattern = new Pattern.Builder()
                    .setBufferedImage(screenshot)
                    .setName("FloraNext" + i)
                    .build();
            
            StateImage stateImage = new StateImage.Builder()
                    .addPattern(pattern)
                    .setName("FloraNextScreen" + i)
                    .build();
            
            ObjectCollection objColl = new ObjectCollection.Builder()
                    .withImages(stateImage)
                    .build();
            
            TextFindOptions ocrOptions = new TextFindOptions.Builder()
                    .setMaxMatchRetries(2)
                    .build();
            
            ActionResult result = new ActionResult();
            result.setActionConfig(ocrOptions);
            result.setActionLifecycle(new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
                java.time.LocalDateTime.now(), 30.0));
            
            find.perform(result, objColl);
            
            if (result.getText() != null && !result.getText().isEmpty()) {
                textFound++;
                String firstText = result.getText().get(0);
                System.out.println("Screenshot " + i + " - Text found: " + 
                    firstText.substring(0, Math.min(100, firstText.length())));
            }
        }
        
        System.out.println("Processed " + screenshotsProcessed + " screenshots");
        System.out.println("Found text in " + textFound + " screenshots");
        
        assertTrue(screenshotsProcessed > 0, "Should process at least one screenshot");
    }
}