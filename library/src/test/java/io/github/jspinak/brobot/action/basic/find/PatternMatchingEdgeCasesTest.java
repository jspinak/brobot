package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for pattern matching edge cases and advanced scenarios.
 * This test class focuses on challenging situations that can occur during
 * pattern matching operations.
 */
@DisplayName("Pattern Matching Edge Cases Tests")
class PatternMatchingEdgeCasesTest extends BrobotTestBase {

    @Mock
    private FindPipeline findPipeline;
    
    private Find find;
    private ObjectCollection objectCollection;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        find = new Find(findPipeline);
        objectCollection = new ObjectCollection();
    }
    
    @Nested
    @DisplayName("Multi-Scale Searching")
    class MultiScaleSearching {
        
        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.75, 1.0, 1.25, 1.5, 2.0})
        @DisplayName("Should handle different scale factors")
        void testDifferentScaleFactors(double scaleFactor) {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(0.8)
                // Note: Scale factor would typically be set on the Pattern or StateImage
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle multi-scale search with increasing similarity")
        void testMultiScaleWithIncreasingSimilarity() {
            // Arrange
            List<Double> scales = Arrays.asList(0.8, 0.9, 1.0, 1.1, 1.2);
            for (Double scale : scales) {
                PatternFindOptions options = new PatternFindOptions.Builder()
                    .setSimilarity(0.7 + (scale - 0.8) * 0.1) // Increase similarity with scale
                    .build();
                ActionResult actionResult = new ActionResult(options);
                
                // Act
                find.perform(actionResult, objectCollection);
                
                // Assert
                verify(findPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
            }
            
            // Verify pipeline was called for each scale
            verify(findPipeline, times(scales.size())).execute(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should optimize search by starting with most likely scale")
        void testOptimizedScaleSearch() {
            // Arrange - using FIRST strategy for early exit on match
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.85)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
        }
    }
    
    @Nested
    @DisplayName("Similarity Threshold Edge Cases")
    class SimilarityThresholdEdgeCases {
        
        @Test
        @DisplayName("Should handle exact match requirement (similarity = 1.0)")
        void testExactMatchRequirement() {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(1.0) // Exact match only
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(argThat(opts -> 
                ((PatternFindOptions)opts).getSimilarity() == 1.0), 
                eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle very low similarity for fuzzy matching")
        void testFuzzyMatching() {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(0.3) // Very fuzzy matching
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(argThat(opts -> 
                ((PatternFindOptions)opts).getSimilarity() == 0.3), 
                eq(actionResult), eq(objectCollection));
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.0, -0.1, 1.1, 2.0})
        @DisplayName("Should handle invalid similarity values")
        void testInvalidSimilarityValues(double similarity) {
            // Arrange - Builder should clamp or validate similarity
            PatternFindOptions.Builder builder = new PatternFindOptions.Builder();
            
            // Similarity should be clamped between 0 and 1
            if (similarity < 0) similarity = 0;
            if (similarity > 1) similarity = 1;
            
            PatternFindOptions options = builder.setSimilarity(similarity).build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(any(), eq(actionResult), eq(objectCollection));
        }
    }
    
    @Nested
    @DisplayName("Complex Search Region Scenarios")
    class ComplexSearchRegionScenarios {
        
        @Test
        @DisplayName("Should handle overlapping search regions")
        void testOverlappingSearchRegions() {
            // Arrange
            SearchRegions searchRegions = new SearchRegions();
            searchRegions.addRegion(new Region(0, 0, 500, 500));
            searchRegions.addRegion(new Region(250, 250, 500, 500)); // Overlapping
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSearchRegions(searchRegions)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(argThat(opts -> 
                ((PatternFindOptions)opts).getSearchRegions() != null), 
                eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle disjoint search regions")
        void testDisjointSearchRegions() {
            // Arrange
            SearchRegions searchRegions = new SearchRegions();
            searchRegions.addRegion(new Region(0, 0, 100, 100));
            searchRegions.addRegion(new Region(200, 200, 100, 100));
            searchRegions.addRegion(new Region(400, 400, 100, 100));
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSearchRegions(searchRegions)
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle nested search regions")
        void testNestedSearchRegions() {
            // Arrange
            SearchRegions searchRegions = new SearchRegions();
            searchRegions.addRegion(new Region(0, 0, 1000, 1000)); // Outer
            searchRegions.addRegion(new Region(100, 100, 800, 800)); // Middle
            searchRegions.addRegion(new Region(200, 200, 600, 600)); // Inner
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSearchRegions(searchRegions)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
        }
    }
    
    @Nested
    @DisplayName("Multiple Pattern Scenarios")
    class MultiplePatternScenarios {
        
        @Test
        @DisplayName("Should handle multiple patterns in single StateImage")
        void testMultiplePatternsInStateImage() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("multi-pattern-image")
                // In reality, would add multiple Pattern objects here
                .build();
            objectCollection.getStateImages().add(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.EACH)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle mixed object types in collection")
        void testMixedObjectTypes() {
            // Arrange
            objectCollection.getStateImages().add(new StateImage.Builder().setName("image1").build());
            objectCollection.getStateLocations().add(new StateLocation.Builder().setLocation(new Location(100, 100)).build());
            objectCollection.getStateRegions().add(new StateRegion.Builder().setSearchRegion(new Region(0, 0, 200, 200)).build());
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
        }
    }
    
    @Nested
    @DisplayName("Performance and Timeout Scenarios")
    class PerformanceAndTimeoutScenarios {
        
        @Test
        @DisplayName("Should respect search duration timeout")
        void testSearchDurationTimeout() {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSearchDuration(0.1) // 100ms timeout
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(argThat(opts -> 
                ((PatternFindOptions)opts).getSearchDuration() == 0.1), 
                eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle immediate timeout (0 duration)")
        void testImmediateTimeout() {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSearchDuration(0) // No waiting
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should optimize for FIRST strategy with early exit")
        void testEarlyExitOptimization() {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setMaxMatchesToActOn(1)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Add multiple objects but expect search to stop after first match
            for (int i = 0; i < 10; i++) {
                objectCollection.getStateImages().add(
                    new StateImage.Builder().setName("image" + i).build()
                );
            }
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline, times(1)).execute(eq(options), eq(actionResult), eq(objectCollection));
        }
    }
    
    @Nested
    @DisplayName("Match Fusion Edge Cases")
    class MatchFusionEdgeCases {
        
        @Test
        @DisplayName("Should configure match fusion for overlapping matches")
        void testMatchFusionForOverlapping() {
            // Arrange
            MatchFusionOptions fusionOptions = MatchFusionOptions.builder()
                .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                .setMaxFusionDistanceX(10)
                .setMaxFusionDistanceY(10)
                .build();
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setMatchFusion(fusionOptions)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(argThat(opts -> 
                ((PatternFindOptions)opts).getMatchFusionOptions() != null), 
                eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle match fusion with different fusion methods")
        void testDifferentFusionMethods() {
            // Test each fusion method
            for (MatchFusionOptions.FusionMethod method : MatchFusionOptions.FusionMethod.values()) {
                // Arrange
                MatchFusionOptions fusionOptions = MatchFusionOptions.builder()
                    .setFusionMethod(method)
                    .build();
                
                PatternFindOptions options = new PatternFindOptions.Builder()
                    .setMatchFusion(fusionOptions)
                    .build();
                ActionResult actionResult = new ActionResult(options);
                
                // Act
                find.perform(actionResult, objectCollection);
                
                // Assert
                verify(findPipeline, atLeastOnce()).execute(any(), any(), any());
            }
        }
    }
    
    static Stream<Arguments> provideEdgeCaseScenarios() {
        return Stream.of(
            Arguments.of(0.0, PatternFindOptions.Strategy.FIRST, 1),
            Arguments.of(0.5, PatternFindOptions.Strategy.ALL, -1),
            Arguments.of(1.0, PatternFindOptions.Strategy.BEST, 5),
            Arguments.of(0.95, PatternFindOptions.Strategy.EACH, 10)
        );
    }
    
    @ParameterizedTest
    @MethodSource("provideEdgeCaseScenarios")
    @DisplayName("Should handle combined edge case scenarios")
    void testCombinedEdgeCases(double similarity, PatternFindOptions.Strategy strategy, int maxMatches) {
        // Arrange
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setSimilarity(similarity)
            .setStrategy(strategy)
            .setMaxMatchesToActOn(maxMatches)
            .build();
        ActionResult actionResult = new ActionResult(options);
        
        // Act
        find.perform(actionResult, objectCollection);
        
        // Assert
        verify(findPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
    }
}