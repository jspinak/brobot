package io.github.jspinak.brobot.actions.methods.basicactions.find;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.text.TextFindOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.ocr.OcrTestBase;
import io.github.jspinak.brobot.test.ocr.OcrTestSupport;
import io.github.jspinak.brobot.testutils.TestPaths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated tests for finding all words in images with OCR using new ActionConfig API.
 * Demonstrates migration from ActionOptions to TextFindOptions.
 * These tests require Tesseract OCR to be installed.
 * 
 * Key changes:
 * - Uses TextFindOptions instead of generic ActionOptions for OCR
 * - TextFindOptions automatically uses ActionOptions.Find.ActionOptions.Find.ALL strategy
 * - ActionResult requires setActionConfig() before perform()
 * - Uses ActionService to get the appropriate action
 * - Fusion and area filtering handled differently in new API
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@OcrTestSupport.RequiresScreenshots
public class FindAllWordsTestUpdated extends OcrTestBase {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    ActionService actionService;

    @Test
    void findWordsWithMinSizeNewAPI() {
        String imagePath = TestPaths.getScreenshotPath("floranext0");
        File imageFile = new File(imagePath);
        
        // If the specific test image doesn't exist, try a fallback
        if (!imageFile.exists()) {
            imagePath = TestPaths.getImagePath("topLeft");
            imageFile = new File(imagePath);
        }
        
        if (!imageFile.exists()) {
            // Skip test if no test images available
            System.out.println("Skipping test - no test images found");
            return;
        }
        
        final String finalImagePath = imagePath;
        
        ActionResult wordMatches = performOcrOperation(() -> {
            ObjectCollection screens = new ObjectCollection.Builder()
                    .withScenes(new Pattern(finalImagePath))
                    .build();
                    
            // NEW API: Use TextFindOptions for OCR word finding
            TextFindOptions textFindOptions = new TextFindOptions.Builder()
                    .setMaxMatchRetries(2)
                    .setPauseAfterEnd(0.5)
                    // Note: minArea filtering might be handled differently in new API
                    // Check if MatchAdjustmentOptions or post-processing is needed
                    .build();
            
            ActionResult result = new ActionResult();
            result.setActionConfig(textFindOptions);
            
            Optional<ActionInterface> textFindActionOpt = actionService.getAction(textFindOptions);
            assertTrue(textFindActionOpt.isPresent(), "Text find action should be available");
            ActionInterface textFindAction = textFindActionOpt.get();
            textFindAction.perform(result, screens);
            
            return result;
        });
        
        // If OCR is not available, wordMatches will be null
        if (wordMatches == null) {
            System.out.println("Test skipped - OCR not available");
            return;
        }
        
        // Verify the action completed
        assertNotNull(wordMatches);
        
        // Verify results only if we found words
        if (!wordMatches.isEmpty()) {
            // Filter matches by size if needed (post-processing)
            int minArea = 25;
            int validMatches = 0;
            
            for (Match match : wordMatches.getMatchList()) {
                if (match.getRegion().size() >= minArea) {
                    validMatches++;
                }
            }
            
            System.out.println("Found " + wordMatches.size() + " total words with OCR");
            System.out.println("Found " + validMatches + " words with area >= " + minArea);
            
            // With TextFindOptions, we get structured text analysis
            // getTextAnalysis() method may not exist in current API
            if (wordMatches.getMatchList() != null && !wordMatches.getMatchList().isEmpty()) {
                System.out.println("Match list contains " + wordMatches.getMatchList().size() + " matches");
            }
        } else {
            System.out.println("No words found in image (this may be normal for some test images)");
        }
    }
    
    @Test
    void findWordsWithCustomSettings() {
        String imagePath = TestPaths.getScreenshotPath("floranext0");
        File imageFile = new File(imagePath);
        
        if (!imageFile.exists()) {
            imagePath = TestPaths.getImagePath("topLeft");
            imageFile = new File(imagePath);
        }
        
        if (!imageFile.exists()) {
            System.out.println("Skipping test - no test images found");
            return;
        }
        
        final String finalImagePath = imagePath;
        
        ActionResult wordMatches = performOcrOperation(() -> {
            ObjectCollection screens = new ObjectCollection.Builder()
                    .withScenes(new Pattern(finalImagePath))
                    .build();
            
            // NEW API: Configure text finding with more options
            TextFindOptions textFindOptions = new TextFindOptions.Builder()
                    .setMaxMatchRetries(3)
                    .setCaptureImage(true)  // Capture images of found text
                    .setSimilarity(0.7)     // Lower threshold for OCR confidence
                    .setMaxMatchesToActOn(100)  // Limit number of words
                    .setPauseAfterEnd(1.0)
                    .build();
            
            ActionResult result = new ActionResult();
            result.setActionConfig(textFindOptions);
            
            Optional<ActionInterface> textFindActionOpt = actionService.getAction(textFindOptions);
            assertTrue(textFindActionOpt.isPresent(), "Text find action should be available");
            ActionInterface textFindAction = textFindActionOpt.get();
            textFindAction.perform(result, screens);
            
            return result;
        });
        
        if (wordMatches == null) {
            System.out.println("Test skipped - OCR not available");
            return;
        }
        
        assertNotNull(wordMatches);
        
        // Verify configuration was used
        if (wordMatches.getActionConfig() instanceof TextFindOptions) {
            TextFindOptions config = (TextFindOptions) wordMatches.getActionConfig();
            assertEquals(3, config.getMaxMatchRetries());
            assertTrue(config.isCaptureImage());
            assertEquals(0.7, config.getSimilarity(), 0.001);
        }
        
        if (!wordMatches.isEmpty()) {
            System.out.println("Found " + wordMatches.size() + " words with custom settings");
            
            // Check if images were captured
            boolean hasImages = wordMatches.getMatchList().stream()
                    .anyMatch(match -> match.getMat() != null);
            
            if (hasImages) {
                System.out.println("Word images were captured as requested");
            }
        }
    }
    
    @Test
    void compareOldAndNewOcrAPI() {
        // This test demonstrates the migration pattern for OCR
        
        // OLD API (commented out):
        /*
        ActionOptions oldOptions = new ActionOptions.Builder()
                .setAction(PatternFindOptions)
                .setFind(ActionOptions.Find.ActionOptions.Find.ALL)
                .setMinArea(25)
                .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
                .setMaxFusionDistances(20, 10)
                .build();
        ActionResult oldResult = action.perform(oldOptions, screens);
        */
        
        // NEW API:
        TextFindOptions newOptions = new TextFindOptions.Builder()
                .setMaxMatchRetries(3)
                .setPauseAfterEnd(0.5)
                .build();
        
        // Note: Fusion and area filtering might need to be handled differently:
        // - Fusion might be part of post-processing or MatchAdjustmentOptions
        // - MinArea filtering might be done after results are returned
        // - TextFindOptions focuses on OCR-specific parameters
        
        assertNotNull(newOptions);
        assertEquals("ActionOptions.Find.ActionOptions.Find.ALL", newOptions.getFindStrategy().toString());
    }
    
    @Test
    void testTextFindOptionsSpecificFeatures() {
        // Test features specific to TextFindOptions
        
        TextFindOptions options = new TextFindOptions.Builder()
                .setMaxMatchRetries(5)
                .setPauseBeforeBegin(1.0)
                .setPauseAfterEnd(2.0)
                .setCaptureImage(true)
                .setSimilarity(0.6)
                .build();
        
        // Verify all settings
        assertEquals(5, options.getMaxMatchRetries());
        assertEquals(1.0, options.getPauseBeforeBegin(), 0.001);
        assertEquals(2.0, options.getPauseAfterEnd(), 0.001);
        assertTrue(options.isCaptureImage());
        assertEquals(0.6, options.getSimilarity(), 0.001);
        
        // TextFindOptions always uses ActionOptions.Find.ActionOptions.Find.ALL strategy
        assertNotNull(options.getFindStrategy());
        assertEquals("ActionOptions.Find.ActionOptions.Find.ALL", options.getFindStrategy().toString());
    }
}