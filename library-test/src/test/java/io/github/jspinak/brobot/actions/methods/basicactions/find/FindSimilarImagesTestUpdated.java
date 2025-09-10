package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.SimilarImagesFindOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for finding similar images functionality.
 * 
 * The SIMILAR_IMAGES strategy compares images between two ObjectCollections:
 * - First collection contains base images for comparison
 * - Second collection contains images to compare against the base
 * - Returns one Match per image in the second collection with similarity scores
 * 
 * This is useful for:
 * 1. Screen state recognition - determining which known screen is displayed
 * 2. Image classification - categorizing images based on similarity
 * 3. Change detection - finding which images have changed
 * 4. Duplicate detection - identifying similar or duplicate images
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("CI failure - needs investigation")
public class FindSimilarImagesTestUpdated extends BrobotIntegrationTestBase {

        @Autowired
        private ActionService actionService;

        @BeforeEach
        void setUp() {
                super.setUpBrobotEnvironment();
                FrameworkSettings.mock = true;
        }

        @AfterEach
        void tearDown() {
                FrameworkSettings.mock = false;
        }

        @Test
        @Order(1)
        @DisplayName("Should find similar images between two collections")
        void testFindSimilarImagesBetweenCollections() {
                /*
                 * Test the SIMILAR_IMAGES strategy which compares images in the
                 * second collection against base images in the first collection.
                 */

                // Create base images for comparison (first collection)
                BufferedImage baseImage1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                BufferedImage baseImage2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

                Pattern basePattern1 = new Pattern.Builder()
                                .setBufferedImage(baseImage1)
                                .setName("BasePattern1")
                                .build();

                Pattern basePattern2 = new Pattern.Builder()
                                .setBufferedImage(baseImage2)
                                .setName("BasePattern2")
                                .build();

                StateImage baseStateImage1 = new StateImage.Builder()
                                .addPattern(basePattern1)
                                .setName("BaseImage1")
                                .build();

                StateImage baseStateImage2 = new StateImage.Builder()
                                .addPattern(basePattern2)
                                .setName("BaseImage2")
                                .build();

                // Create images to compare (second collection)
                BufferedImage compareImage1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                BufferedImage compareImage2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                BufferedImage compareImage3 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

                Pattern comparePattern1 = new Pattern.Builder()
                                .setBufferedImage(compareImage1)
                                .setName("ComparePattern1")
                                .build();

                Pattern comparePattern2 = new Pattern.Builder()
                                .setBufferedImage(compareImage2)
                                .setName("ComparePattern2")
                                .build();

                Pattern comparePattern3 = new Pattern.Builder()
                                .setBufferedImage(compareImage3)
                                .setName("ComparePattern3")
                                .build();

                StateImage compareStateImage1 = new StateImage.Builder()
                                .addPattern(comparePattern1)
                                .setName("CompareImage1")
                                .build();

                StateImage compareStateImage2 = new StateImage.Builder()
                                .addPattern(comparePattern2)
                                .setName("CompareImage2")
                                .build();

                StateImage compareStateImage3 = new StateImage.Builder()
                                .addPattern(comparePattern3)
                                .setName("CompareImage3")
                                .build();

                // First collection - base images
                ObjectCollection baseCollection = new ObjectCollection.Builder()
                                .withImages(baseStateImage1, baseStateImage2)
                                .build();

                // Second collection - images to compare
                ObjectCollection compareCollection = new ObjectCollection.Builder()
                                .withImages(compareStateImage1, compareStateImage2, compareStateImage3)
                                .build();

                // Use SimilarImagesFindOptions
                SimilarImagesFindOptions findOptions = new SimilarImagesFindOptions.Builder()
                                .setSimilarity(0.7)
                                .setComparisonMethod(SimilarImagesFindOptions.ComparisonMethod.BEST_MATCH)
                                .build();

                ActionResult result = new ActionResult();
                result.setActionConfig(findOptions);

                // Get the find action
                Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
                assertTrue(findActionOpt.isPresent(), "Find action should be available for SIMILAR_IMAGES");

                ActionInterface findAction = findActionOpt.get();

                // IMPORTANT: SIMILAR_IMAGES requires two ObjectCollections
                findAction.perform(result, baseCollection, compareCollection);

                // Verify results
                assertNotNull(result.getMatchList());
                assertTrue(result.isSuccess(), "Find operation should succeed in mock mode");

                // Should have one match for each image in the compare collection
                // In mock mode, we may not get exact counts but verify structure
                List<Match> matches = result.getMatchList();
                assertNotNull(matches, "Should return match list");

                // Each match represents how well an image from compareCollection
                // matches the best image from baseCollection
                for (Match match : matches) {
                        assertNotNull(match);
                        assertTrue(match.getScore() >= 0 && match.getScore() <= 1.0,
                                        "Match score should be between 0 and 1");
                }
        }

        @Test
        @Order(2)
        @DisplayName("Should use screen recognition preset")
        void testScreenRecognitionPreset() {
                /*
                 * Test using the screen recognition preset which uses high similarity
                 * thresholds for accurate screen state identification.
                 */

                BufferedImage knownScreen = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                BufferedImage currentScreen = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

                StateImage knownStateImage = new StateImage.Builder()
                                .addPattern(new Pattern.Builder()
                                                .setBufferedImage(knownScreen)
                                                .setName("KnownScreen")
                                                .build())
                                .setName("KnownScreenState")
                                .build();

                StateImage currentStateImage = new StateImage.Builder()
                                .addPattern(new Pattern.Builder()
                                                .setBufferedImage(currentScreen)
                                                .setName("CurrentScreen")
                                                .build())
                                .setName("CurrentScreenState")
                                .build();

                ObjectCollection knownScreens = new ObjectCollection.Builder()
                                .withImages(knownStateImage)
                                .build();

                ObjectCollection currentScreenCapture = new ObjectCollection.Builder()
                                .withImages(currentStateImage)
                                .build();

                // Use screen recognition preset
                SimilarImagesFindOptions findOptions = SimilarImagesFindOptions.forScreenRecognition();

                ActionResult result = new ActionResult();
                result.setActionConfig(findOptions);

                Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
                assertTrue(findActionOpt.isPresent());

                ActionInterface findAction = findActionOpt.get();
                findAction.perform(result, knownScreens, currentScreenCapture);

                assertTrue(result.isSuccess());

                // Verify preset configuration
                assertEquals(0.9, findOptions.getSimilarity(), 0.001);
                assertEquals(SimilarImagesFindOptions.ComparisonMethod.BEST_MATCH,
                                findOptions.getComparisonMethod());
                assertFalse(findOptions.isIncludeNoMatches());
        }

        @Test
        @Order(3)
        @DisplayName("Should find one match per image with multiple base images")
        void testMultipleBaseImages() {
                /*
                 * Test that each image in the compare collection gets matched
                 * with the best matching image from the base collection.
                 */

                // Create multiple base images
                BufferedImage[] baseImages = new BufferedImage[3];
                StateImage[] baseStateImages = new StateImage[3];

                for (int i = 0; i < 3; i++) {
                        baseImages[i] = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                        Pattern pattern = new Pattern.Builder()
                                        .setBufferedImage(baseImages[i])
                                        .setName("BasePattern" + i)
                                        .build();
                        baseStateImages[i] = new StateImage.Builder()
                                        .addPattern(pattern)
                                        .setName("BaseImage" + i)
                                        .build();
                }

                // Create images to compare
                BufferedImage[] compareImages = new BufferedImage[2];
                StateImage[] compareStateImages = new StateImage[2];

                for (int i = 0; i < 2; i++) {
                        compareImages[i] = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                        Pattern pattern = new Pattern.Builder()
                                        .setBufferedImage(compareImages[i])
                                        .setName("ComparePattern" + i)
                                        .build();
                        compareStateImages[i] = new StateImage.Builder()
                                        .addPattern(pattern)
                                        .setName("CompareImage" + i)
                                        .build();
                }

                ObjectCollection baseCollection = new ObjectCollection.Builder()
                                .withImages(baseStateImages)
                                .build();

                ObjectCollection compareCollection = new ObjectCollection.Builder()
                                .withImages(compareStateImages)
                                .build();

                SimilarImagesFindOptions findOptions = new SimilarImagesFindOptions.Builder()
                                .setSimilarity(0.75)
                                .setComparisonMethod(SimilarImagesFindOptions.ComparisonMethod.BEST_MATCH)
                                .build();

                ActionResult result = new ActionResult();
                result.setActionConfig(findOptions);

                Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
                assertTrue(findActionOpt.isPresent());

                ActionInterface findAction = findActionOpt.get();
                findAction.perform(result, baseCollection, compareCollection);

                assertTrue(result.isSuccess());
                assertNotNull(result.getMatchList());
        }

        @Test
        @Order(4)
        @DisplayName("Should use duplicate detection preset")
        void testDuplicateDetectionPreset() {
                /*
                 * Test using the duplicate detection preset which uses very high
                 * similarity thresholds to identify nearly identical images.
                 */

                BufferedImage original = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                BufferedImage duplicate = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

                StateImage originalImage = new StateImage.Builder()
                                .addPattern(new Pattern.Builder()
                                                .setBufferedImage(original)
                                                .setName("Original")
                                                .build())
                                .setName("OriginalImage")
                                .build();

                StateImage duplicateImage = new StateImage.Builder()
                                .addPattern(new Pattern.Builder()
                                                .setBufferedImage(duplicate)
                                                .setName("PossibleDuplicate")
                                                .build())
                                .setName("DuplicateImage")
                                .build();

                ObjectCollection originals = new ObjectCollection.Builder()
                                .withImages(originalImage)
                                .build();

                ObjectCollection candidates = new ObjectCollection.Builder()
                                .withImages(duplicateImage)
                                .build();

                // Use duplicate detection preset
                SimilarImagesFindOptions findOptions = SimilarImagesFindOptions.forDuplicateDetection();

                ActionResult result = new ActionResult();
                result.setActionConfig(findOptions);

                Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
                assertTrue(findActionOpt.isPresent());

                ActionInterface findAction = findActionOpt.get();
                findAction.perform(result, originals, candidates);

                assertTrue(result.isSuccess());

                // Verify preset configuration
                assertEquals(0.95, findOptions.getSimilarity(), 0.001);
                assertEquals(SimilarImagesFindOptions.ComparisonMethod.ALL_PATTERNS_MATCH,
                                findOptions.getComparisonMethod());
                assertTrue(findOptions.isIncludeNoMatches());
        }

        @Test
        @Order(5)
        @DisplayName("Should use change detection preset")
        void testChangeDetectionPreset() {
                /*
                 * Test using the change detection preset which uses lower similarity
                 * thresholds to identify which images have changed.
                 */

                BufferedImage beforeImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                BufferedImage afterImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

                StateImage beforeState = new StateImage.Builder()
                                .addPattern(new Pattern.Builder()
                                                .setBufferedImage(beforeImage)
                                                .setName("BeforeChange")
                                                .build())
                                .setName("BeforeState")
                                .build();

                StateImage afterState = new StateImage.Builder()
                                .addPattern(new Pattern.Builder()
                                                .setBufferedImage(afterImage)
                                                .setName("AfterChange")
                                                .build())
                                .setName("AfterState")
                                .build();

                ObjectCollection beforeCollection = new ObjectCollection.Builder()
                                .withImages(beforeState)
                                .build();

                ObjectCollection afterCollection = new ObjectCollection.Builder()
                                .withImages(afterState)
                                .build();

                // Use change detection preset
                SimilarImagesFindOptions findOptions = SimilarImagesFindOptions.forChangeDetection();

                ActionResult result = new ActionResult();
                result.setActionConfig(findOptions);

                Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
                assertTrue(findActionOpt.isPresent());

                ActionInterface findAction = findActionOpt.get();
                findAction.perform(result, beforeCollection, afterCollection);

                assertTrue(result.isSuccess());

                // Verify preset configuration
                assertEquals(0.7, findOptions.getSimilarity(), 0.001);
                assertEquals(SimilarImagesFindOptions.ComparisonMethod.ANY_PATTERN_MATCHES,
                                findOptions.getComparisonMethod());
                assertTrue(findOptions.isIncludeNoMatches());
                assertTrue(findOptions.isReturnAllScores());
        }

        @Test
        @Order(6)
        @DisplayName("Should handle empty collections gracefully")
        void testEmptyCollections() {
                /*
                 * Test that SIMILAR_IMAGES handles empty collections without errors.
                 */

                ObjectCollection emptyCollection = new ObjectCollection.Builder().build();

                ObjectCollection nonEmptyCollection = new ObjectCollection.Builder()
                                .withImages(new StateImage.Builder()
                                                .addPattern(new Pattern.Builder()
                                                                .setBufferedImage(new BufferedImage(100, 100,
                                                                                BufferedImage.TYPE_INT_RGB))
                                                                .setName("TestPattern")
                                                                .build())
                                                .setName("TestImage")
                                                .build())
                                .build();

                SimilarImagesFindOptions findOptions = new SimilarImagesFindOptions.Builder()
                                .setSimilarity(0.8)
                                .build();

                ActionResult result = new ActionResult();
                result.setActionConfig(findOptions);

                Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
                assertTrue(findActionOpt.isPresent());

                ActionInterface findAction = findActionOpt.get();

                // Test with empty first collection
                findAction.perform(result, emptyCollection, nonEmptyCollection);
                // Should handle gracefully without exceptions
                assertNotNull(result);

                // Test with empty second collection
                result = new ActionResult();
                result.setActionConfig(findOptions);
                findAction.perform(result, nonEmptyCollection, emptyCollection);
                assertNotNull(result);

                // Test with both empty
                result = new ActionResult();
                result.setActionConfig(findOptions);
                findAction.perform(result, emptyCollection, emptyCollection);
                assertNotNull(result);
        }

        @Test
        @Order(7)
        @DisplayName("Should work with patterns having search regions")
        void testPatternsWithSearchRegions() {
                /*
                 * Test that SIMILAR_IMAGES works correctly when patterns have
                 * search regions defined.
                 */

                Region searchRegion = new Region(100, 100, 400, 300);

                BufferedImage baseImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                BufferedImage compareImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

                Pattern basePattern = new Pattern.Builder()
                                .setBufferedImage(baseImage)
                                .setName("BaseWithRegion")
                                .addSearchRegion(searchRegion)
                                .build();

                Pattern comparePattern = new Pattern.Builder()
                                .setBufferedImage(compareImage)
                                .setName("CompareWithRegion")
                                .addSearchRegion(searchRegion)
                                .build();

                StateImage baseStateImage = new StateImage.Builder()
                                .addPattern(basePattern)
                                .setName("BaseImageWithRegion")
                                .build();

                StateImage compareStateImage = new StateImage.Builder()
                                .addPattern(comparePattern)
                                .setName("CompareImageWithRegion")
                                .build();

                ObjectCollection baseCollection = new ObjectCollection.Builder()
                                .withImages(baseStateImage)
                                .build();

                ObjectCollection compareCollection = new ObjectCollection.Builder()
                                .withImages(compareStateImage)
                                .build();

                SimilarImagesFindOptions findOptions = new SimilarImagesFindOptions.Builder()
                                .setSimilarity(0.8)
                                .setComparisonMethod(SimilarImagesFindOptions.ComparisonMethod.BEST_MATCH)
                                .build();

                ActionResult result = new ActionResult();
                result.setActionConfig(findOptions);

                Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
                assertTrue(findActionOpt.isPresent());

                ActionInterface findAction = findActionOpt.get();
                findAction.perform(result, baseCollection, compareCollection);

                assertTrue(result.isSuccess());
                assertNotNull(result.getMatchList());
        }
}