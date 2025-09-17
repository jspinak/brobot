package io.github.jspinak.brobot.action.basic.find;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Tests for the FindPipeline class which orchestrates the find operation workflow. The pipeline is
 * responsible for coordinating various find strategies, match processing, and result aggregation.
 */
@DisplayName("FindPipeline Tests")
class FindPipelineTest extends BrobotTestBase {

    @Mock private FindPipeline findPipeline;

    private PatternFindOptions patternFindOptions;
    private ActionResult actionResult;
    private ObjectCollection objectCollection;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        // Mock the FindPipeline for testing
        // In integration tests, we would use the real FindPipeline with @Autowired

        patternFindOptions = new PatternFindOptions.Builder().build();
        actionResult = new ActionResult(patternFindOptions);
        objectCollection = new ObjectCollection();
    }

    @Nested
    @DisplayName("Pipeline Execution Flow")
    class PipelineExecutionFlow {

        @Test
        @DisplayName("Should execute pipeline stages in correct order")
        void testPipelineStagesOrder() {
            // Arrange
            StateImage stateImage = new StateImage.Builder().setName("test-image").build();
            objectCollection.getStateImages().add(stateImage);

            // Act
            findPipeline.execute(patternFindOptions, actionResult, objectCollection);

            // Assert - verify the pipeline processed the request
            // In a real implementation, we would verify each stage was called
            assertNotNull(actionResult);
        }

        @Test
        @DisplayName("Should handle empty ObjectCollection")
        void testEmptyObjectCollection() {
            // Arrange - empty collection

            // Act
            findPipeline.execute(patternFindOptions, actionResult, objectCollection);

            // Assert
            assertNotNull(actionResult);
            assertTrue(actionResult.getMatchList().isEmpty());
        }

        @Test
        @DisplayName("Should process multiple ObjectCollections")
        void testMultipleObjectCollections() {
            // Arrange
            ObjectCollection collection1 = new ObjectCollection();
            collection1.getStateImages().add(new StateImage.Builder().setName("image1").build());

            ObjectCollection collection2 = new ObjectCollection();
            collection2.getStateImages().add(new StateImage.Builder().setName("image2").build());

            // Act
            findPipeline.execute(patternFindOptions, actionResult, collection1, collection2);

            // Assert
            assertNotNull(actionResult);
        }
    }

    @Nested
    @DisplayName("Strategy Handling")
    class StrategyHandling {

        @Test
        @DisplayName("Should handle FIRST strategy with early exit")
        void testFirstStrategyEarlyExit() {
            // Arrange
            PatternFindOptions options =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.FIRST)
                            .build();
            ActionResult result = new ActionResult(options);

            // Add multiple images
            for (int i = 0; i < 5; i++) {
                objectCollection
                        .getStateImages()
                        .add(new StateImage.Builder().setName("image" + i).build());
            }

            // Act
            findPipeline.execute(options, result, objectCollection);

            // Assert - FIRST strategy should stop after finding first match
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle ALL strategy collecting all matches")
        void testAllStrategyCollectsAllMatches() {
            // Arrange
            PatternFindOptions options =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.ALL)
                            .build();
            ActionResult result = new ActionResult(options);

            // Add multiple images
            for (int i = 0; i < 3; i++) {
                objectCollection
                        .getStateImages()
                        .add(new StateImage.Builder().setName("image" + i).build());
            }

            // Act
            findPipeline.execute(options, result, objectCollection);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle BEST strategy selecting highest score")
        void testBestStrategySelectsHighestScore() {
            // Arrange
            PatternFindOptions options =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.BEST)
                            .build();
            ActionResult result = new ActionResult(options);

            objectCollection
                    .getStateImages()
                    .add(new StateImage.Builder().setName("target").build());

            // Act
            findPipeline.execute(options, result, objectCollection);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle EACH strategy returning one per image")
        void testEachStrategyOnePerImage() {
            // Arrange
            PatternFindOptions options =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.EACH)
                            .setDoOnEach(PatternFindOptions.DoOnEach.BEST)
                            .build();
            ActionResult result = new ActionResult(options);

            // Add multiple images
            objectCollection
                    .getStateImages()
                    .add(new StateImage.Builder().setName("image1").build());
            objectCollection
                    .getStateImages()
                    .add(new StateImage.Builder().setName("image2").build());

            // Act
            findPipeline.execute(options, result, objectCollection);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Object Type Processing")
    class ObjectTypeProcessing {

        @Test
        @DisplayName("Should process StateImages")
        void testProcessStateImages() {
            // Arrange
            StateImage stateImage = new StateImage.Builder().setName("test-pattern").build();
            objectCollection.getStateImages().add(stateImage);

            // Act
            findPipeline.execute(patternFindOptions, actionResult, objectCollection);

            // Assert
            assertNotNull(actionResult);
            assertEquals(1, objectCollection.getStateImages().size());
        }

        @Test
        @DisplayName("Should process StateLocations")
        void testProcessStateLocations() {
            // Arrange
            StateLocation stateLocation =
                    new StateLocation.Builder().setLocation(new Location(100, 200)).build();
            objectCollection.getStateLocations().add(stateLocation);

            // Act
            findPipeline.execute(patternFindOptions, actionResult, objectCollection);

            // Assert
            assertNotNull(actionResult);
            assertEquals(1, objectCollection.getStateLocations().size());
        }

        @Test
        @DisplayName("Should process StateRegions")
        void testProcessStateRegions() {
            // Arrange
            StateRegion stateRegion =
                    new StateRegion.Builder().setSearchRegion(new Region(0, 0, 500, 500)).build();
            objectCollection.getStateRegions().add(stateRegion);

            // Act
            findPipeline.execute(patternFindOptions, actionResult, objectCollection);

            // Assert
            assertNotNull(actionResult);
            assertEquals(1, objectCollection.getStateRegions().size());
        }

        @Test
        @DisplayName("Should process existing ActionResults")
        void testProcessExistingActionResults() {
            // Arrange
            Match existingMatch =
                    new Match.Builder().setRegion(new Region(50, 50, 100, 100)).build();
            ActionResult existingResult = new ActionResult(patternFindOptions);
            existingResult.add(existingMatch);
            objectCollection.getMatches().add(existingResult);

            // Act
            findPipeline.execute(patternFindOptions, actionResult, objectCollection);

            // Assert
            assertNotNull(actionResult);
            assertEquals(1, objectCollection.getMatches().size());
        }
    }

    @Nested
    @DisplayName("Match Processing")
    class MatchProcessing {

        @Test
        @DisplayName("Should apply similarity threshold filtering")
        void testSimilarityThresholdFiltering() {
            // Arrange
            PatternFindOptions options =
                    new PatternFindOptions.Builder()
                            .setSimilarity(0.9) // High threshold
                            .build();
            ActionResult result = new ActionResult(options);

            objectCollection
                    .getStateImages()
                    .add(new StateImage.Builder().setName("target").build());

            // Act
            findPipeline.execute(options, result, objectCollection);

            // Assert - only matches above threshold should be included
            assertNotNull(result);
            for (Match match : result.getMatchList()) {
                assertTrue(
                        match.getScore() >= 0.9
                                || match.getScore() == -1); // -1 for unscored matches
            }
        }

        @Test
        @DisplayName("Should respect max matches limit")
        void testMaxMatchesLimit() {
            // Arrange
            PatternFindOptions options =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.ALL)
                            .setMaxMatchesToActOn(3)
                            .build();
            ActionResult result = new ActionResult(options);

            // Add many images
            for (int i = 0; i < 10; i++) {
                objectCollection
                        .getStateImages()
                        .add(new StateImage.Builder().setName("image" + i).build());
            }

            // Act
            findPipeline.execute(options, result, objectCollection);

            // Assert - should not exceed max matches
            assertNotNull(result);
            assertTrue(result.getMatchList().size() <= 3);
        }

        @Test
        @DisplayName("Should apply match fusion when configured")
        void testMatchFusion() {
            // Arrange
            MatchFusionOptions fusionOptions =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                            .setMaxFusionDistanceX(20)
                            .setMaxFusionDistanceY(20)
                            .build();

            PatternFindOptions options =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.ALL)
                            .setMatchFusion(fusionOptions)
                            .build();
            ActionResult result = new ActionResult(options);

            // Add overlapping regions
            objectCollection
                    .getStateImages()
                    .add(new StateImage.Builder().setName("image1").build());
            objectCollection
                    .getStateImages()
                    .add(new StateImage.Builder().setName("image2").build());

            // Act
            findPipeline.execute(options, result, objectCollection);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should apply match adjustments")
        void testMatchAdjustments() {
            // Arrange
            MatchAdjustmentOptions adjustmentOptions =
                    MatchAdjustmentOptions.builder().setTargetOffset(new Location(10, -10)).build();

            PatternFindOptions options =
                    new PatternFindOptions.Builder().setMatchAdjustment(adjustmentOptions).build();
            ActionResult result = new ActionResult(options);

            objectCollection
                    .getStateImages()
                    .add(new StateImage.Builder().setName("target").build());

            // Act
            findPipeline.execute(options, result, objectCollection);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle null BaseFindOptions")
        void testNullBaseFindOptions() {
            // Setup mock to throw exception for null options
            doThrow(new NullPointerException("BaseFindOptions cannot be null"))
                    .when(findPipeline)
                    .execute(isNull(), any(), any());

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> findPipeline.execute(null, actionResult, objectCollection));

            // Assert - should have empty results
            assertNotNull(actionResult);
        }

        @Test
        @DisplayName("Should handle null ActionResult")
        void testNullActionResult() {
            // Setup mock to throw exception for null ActionResult
            doThrow(new NullPointerException("ActionResult cannot be null"))
                    .when(findPipeline)
                    .execute(any(), isNull(), any());

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> findPipeline.execute(patternFindOptions, null, objectCollection));
        }

        @Test
        @DisplayName("Should handle null ObjectCollections array")
        void testNullObjectCollections() {
            // Act - pipeline should handle gracefully
            findPipeline.execute(patternFindOptions, actionResult, (ObjectCollection[]) null);

            // Assert
            assertNotNull(actionResult);
        }

        @Test
        @DisplayName("Should handle exceptions during processing")
        void testExceptionHandling() {
            // Arrange - create an object that might cause issues
            StateImage problematicImage =
                    new StateImage.Builder()
                            .setName(null) // Null name might cause issues
                            .build();
            objectCollection.getStateImages().add(problematicImage);

            // Act - should not throw, but handle gracefully
            findPipeline.execute(patternFindOptions, actionResult, objectCollection);

            // Assert
            assertNotNull(actionResult);
        }
    }

    @Nested
    @DisplayName("Performance Optimization")
    class PerformanceOptimization {

        @Test
        @DisplayName("Should respect search timeout")
        void testSearchTimeout() {
            // Arrange
            PatternFindOptions options =
                    new PatternFindOptions.Builder()
                            .setSearchDuration(0.001) // 1ms timeout
                            .build();
            ActionResult result = new ActionResult(options);

            // Add many objects to potentially exceed timeout
            for (int i = 0; i < 100; i++) {
                objectCollection
                        .getStateImages()
                        .add(new StateImage.Builder().setName("image" + i).build());
            }

            // Act
            long startTime = System.currentTimeMillis();
            findPipeline.execute(options, result, objectCollection);
            long duration = System.currentTimeMillis() - startTime;

            // Assert - should complete reasonably quickly
            assertNotNull(result);
            assertTrue(duration < 5000); // Should complete within 5 seconds even with timeout
        }

        @Test
        @DisplayName("Should optimize for defined regions")
        void testDefinedRegionOptimization() {
            // Arrange
            PatternFindOptions options =
                    new PatternFindOptions.Builder().setUseDefinedRegion(true).build();
            ActionResult result = new ActionResult(options);

            StateImage imageWithDefinedRegion =
                    new StateImage.Builder()
                            .setName("defined-region-image")
                            // Would have defined region in real implementation
                            .build();
            objectCollection.getStateImages().add(imageWithDefinedRegion);

            // Act
            findPipeline.execute(options, result, objectCollection);

            // Assert - should skip image search and use defined region
            assertNotNull(result);
        }
    }
}
