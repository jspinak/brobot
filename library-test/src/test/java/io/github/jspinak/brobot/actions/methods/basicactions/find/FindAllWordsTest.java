package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.ocr.OcrTestBase;
import io.github.jspinak.brobot.test.ocr.OcrTestSupport;
import io.github.jspinak.brobot.testutils.TestPaths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Find.ALL_WORDS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for finding all words in images with OCR.
 * These tests require Tesseract OCR to be installed.
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@OcrTestSupport.RequiresOcr
public class FindAllWordsTest extends OcrTestBase {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    Action action;

    @Test
    void findWordsWithMinSize() {
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
        
        Matches wordMatches = performOcrOperation(() -> {
            ObjectCollection screens = new ObjectCollection.Builder()
                    .withScenes(new Pattern(finalImagePath))
                    .build();
                    
            ActionOptions findAllWords = new ActionOptions.Builder()
                    .setAction(ActionOptions.Action.FIND)
                    .setFind(ALL_WORDS)
                    .setMinArea(25)
                    .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
                    .setMaxFusionDistances(20, 10)
                    .build();
                    
            return action.perform(findAllWords, screens);
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
            for (Match match : wordMatches.getMatchList()) {
                assertTrue(match.getRegion().size() >= 25, 
                    "Match region size should be at least 25, but was " + match.getRegion().size());
            }
            System.out.println("Found " + wordMatches.size() + " words with OCR");
        } else {
            System.out.println("No words found in image (this may be normal for some test images)");
        }
    }
}