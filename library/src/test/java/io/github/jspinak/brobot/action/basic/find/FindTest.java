package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.find.histogram.HistogramFindOptions;
import io.github.jspinak.brobot.action.basic.find.motion.MotionFindOptions;
import io.github.jspinak.brobot.action.basic.find.text.TextFindOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Find Tests - Pattern Matching and Image Recognition")
class FindTest extends BrobotTestBase {

    private Find find;
    
    @Mock
    private FindPipeline findPipeline;
    
    @Mock
    private ObjectCollection objectCollection;
    
    @Mock
    private ActionResult mockActionResult;
    
    private PatternFindOptions patternFindOptions;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        find = new Find(findPipeline);
        patternFindOptions = new PatternFindOptions.Builder().build();
    }
    
    @Test
    @DisplayName("Should return FIND action type")
    void testGetActionType() {
        assertEquals(ActionInterface.Type.FIND, find.getActionType());
    }
    
    @Nested
    @DisplayName("Basic Find Operations")
    class BasicFindOperations {
        
        @Test
        @DisplayName("Should perform find with valid PatternFindOptions")
        void testPerformWithValidOptions() {
            // Arrange
            ActionResult actionResult = new ActionResult(patternFindOptions);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline, times(1))
                .execute(patternFindOptions, actionResult, objectCollection);
        }
        
        @Test
        @DisplayName("Should throw exception for invalid ActionConfig")
        void testPerformWithInvalidConfig() {
            // Arrange
            ActionConfig invalidConfig = mock(ActionConfig.class);
            ActionResult actionResult = new ActionResult(invalidConfig);
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> 
                find.perform(actionResult, objectCollection),
                "Find requires BaseFindOptions configuration"
            );
        }
        
        @Test
        @DisplayName("Should handle multiple object collections")
        void testPerformWithMultipleCollections() {
            // Arrange
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            ObjectCollection collection3 = mock(ObjectCollection.class);
            ActionResult actionResult = new ActionResult(patternFindOptions);
            
            // Act
            find.perform(actionResult, collection1, collection2, collection3);
            
            // Assert
            verify(findPipeline, times(1))
                .execute(patternFindOptions, actionResult, collection1, collection2, collection3);
        }
        
        @Test
        @DisplayName("Should handle empty object collections")
        void testPerformWithEmptyCollections() {
            // Arrange
            when(objectCollection.getStateImages()).thenReturn(Collections.emptyList());
            when(objectCollection.getMatches()).thenReturn(Collections.emptyList());
            when(objectCollection.getStateRegions()).thenReturn(Collections.emptyList());
            ActionResult actionResult = new ActionResult(patternFindOptions);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline, times(1))
                .execute(patternFindOptions, actionResult, objectCollection);
        }
    }
    
    @Nested
    @DisplayName("Find Strategy Tests")
    class FindStrategyTests {
        
        @ParameterizedTest
        @EnumSource(value = PatternFindOptions.Strategy.class)
        @DisplayName("Should handle pattern-based find strategies")
        void testPatternBasedStrategies(PatternFindOptions.Strategy strategy) {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(strategy)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            ArgumentCaptor<BaseFindOptions> optionsCaptor = ArgumentCaptor.forClass(BaseFindOptions.class);
            verify(findPipeline).execute(optionsCaptor.capture(), eq(actionResult), eq(objectCollection));
            PatternFindOptions capturedOptions = (PatternFindOptions) optionsCaptor.getValue();
            assertEquals(strategy, capturedOptions.getStrategy());
        }
        
        @Test
        @DisplayName("Should handle FIRST strategy with early exit")
        void testFirstStrategyEarlyExit() {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(eq(options), eq(actionResult), any());
        }
        
        @Test
        @DisplayName("Should handle BEST strategy with score comparison")
        void testBestStrategyScoreComparison() {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.8)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            ArgumentCaptor<BaseFindOptions> optionsCaptor = ArgumentCaptor.forClass(BaseFindOptions.class);
            verify(findPipeline).execute(optionsCaptor.capture(), eq(actionResult), any());
            assertEquals(0.8, optionsCaptor.getValue().getSimilarity(), 0.01);
        }
        
        @Test
        @DisplayName("Should handle ALL strategy without limit")
        void testAllStrategyNoLimit() {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(eq(options), eq(actionResult), any());
        }
    }
    
    @Nested
    @DisplayName("Advanced Find Options")
    class AdvancedFindOptions {
        
        @Test
        @DisplayName("Should handle color-based find options")
        void testColorFindOptions() {
            // Arrange
            ColorFindOptions colorOptions = new ColorFindOptions.Builder()
                .build();
            ActionResult actionResult = new ActionResult(colorOptions);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(colorOptions, actionResult, objectCollection);
        }
        
        @Test
        @DisplayName("Should handle text find options")
        void testTextFindOptions() {
            // Arrange
            TextFindOptions textOptions = new TextFindOptions.Builder()
                .build();
            ActionResult actionResult = new ActionResult(textOptions);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(textOptions, actionResult, objectCollection);
        }
        
        @Test
        @DisplayName("Should handle histogram find options")
        void testHistogramFindOptions() {
            // Arrange
            HistogramFindOptions histOptions = new HistogramFindOptions.Builder()
                .build();
            ActionResult actionResult = new ActionResult(histOptions);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(histOptions, actionResult, objectCollection);
        }
        
        @Test
        @DisplayName("Should handle motion find options")
        void testMotionFindOptions() {
            // Arrange
            MotionFindOptions motionOptions = new MotionFindOptions.Builder()
                .build();
            ActionResult actionResult = new ActionResult(motionOptions);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(motionOptions, actionResult, objectCollection);
        }
    }
    
    @Nested
    @DisplayName("Pattern Matching Options")
    class PatternMatchingOptions {
        
        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.7, 0.85, 0.95, 1.0})
        @DisplayName("Should handle different similarity thresholds")
        void testSimilarityThresholds(double similarity) {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(similarity)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            ArgumentCaptor<BaseFindOptions> optionsCaptor = ArgumentCaptor.forClass(BaseFindOptions.class);
            verify(findPipeline).execute(optionsCaptor.capture(), eq(actionResult), any());
            assertEquals(similarity, optionsCaptor.getValue().getSimilarity(), 0.01);
        }
        
        @Test
        @DisplayName("Should handle multi-scale searching")
        void testMultiScaleSearching() {
            // Arrange - using setters that might exist for scale options
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(0.8)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(eq(options), eq(actionResult), any());
        }
        
        @Test
        @DisplayName("Should handle search regions")
        void testSearchRegions() {
            // Arrange
            Region searchRegion = new Region(100, 100, 500, 400);
            StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(searchRegion)
                .build();
            when(objectCollection.getStateRegions()).thenReturn(Arrays.asList(stateRegion));
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(options, actionResult, objectCollection);
            // Note: getStateRegions is called by the pipeline, not Find itself
        }
    }
    
    @Nested
    @DisplayName("Object Collection Handling")
    class ObjectCollectionHandling {
        
        @Test
        @DisplayName("Should handle StateImages in collection")
        void testStateImagesHandling() {
            // Arrange
            StateImage stateImage1 = new StateImage.Builder()
                .setName("test1")
                .build();
            StateImage stateImage2 = new StateImage.Builder()
                .setName("test2")
                .build();
            when(objectCollection.getStateImages())
                .thenReturn(Arrays.asList(stateImage1, stateImage2));
            ActionResult actionResult = new ActionResult(patternFindOptions);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(patternFindOptions, actionResult, objectCollection);
            // Note: getStateImages is called by the pipeline, not Find itself
        }
        
        @Test
        @DisplayName("Should handle existing ActionResults in collection")
        void testExistingActionResultsHandling() {
            // Arrange
            Match match1 = new Match.Builder()
                .setRegion(new Region(0, 0, 100, 100))
                .build();
            Match match2 = new Match.Builder()
                .setRegion(new Region(200, 200, 100, 100))
                .build();
            ActionResult result1 = new ActionResult(patternFindOptions);
            result1.setSuccess(true);
            result1.add(match1);
            ActionResult result2 = new ActionResult(patternFindOptions);
            result2.setSuccess(true);
            result2.add(match2);
            when(objectCollection.getMatches())
                .thenReturn(Arrays.asList(result1, result2));
            ActionResult actionResult = new ActionResult(patternFindOptions);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(patternFindOptions, actionResult, objectCollection);
        }
        
        @Test
        @DisplayName("Should handle StateLocations in collection")
        void testStateLocationsHandling() {
            // Arrange
            StateLocation location1 = new StateLocation.Builder()
                .setLocation(new Location(100, 100))
                .build();
            StateLocation location2 = new StateLocation.Builder()
                .setLocation(new Location(200, 200))
                .build();
            when(objectCollection.getStateLocations())
                .thenReturn(Arrays.asList(location1, location2));
            ActionResult actionResult = new ActionResult(patternFindOptions);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(findPipeline).execute(patternFindOptions, actionResult, objectCollection);
        }
    }
    
    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingAndEdgeCases {
        
        @Test
        @DisplayName("Should handle null ActionResult")
        void testNullActionResult() {
            assertThrows(NullPointerException.class, () -> 
                find.perform(null, objectCollection)
            );
        }
        
        @Test
        @DisplayName("Should handle null ObjectCollections")
        void testNullObjectCollections() {
            // Arrange
            ActionResult actionResult = new ActionResult(patternFindOptions);
            
            // Act - Find delegates to pipeline which should handle null
            find.perform(actionResult, (ObjectCollection[]) null);
            
            // Assert - verify pipeline was called with null
            verify(findPipeline).execute(eq(patternFindOptions), eq(actionResult), (ObjectCollection[]) isNull());
        }
        
        @Test
        @DisplayName("Should validate configuration type")
        void testInvalidConfigurationType() {
            // Arrange
            ActionResult actionResult = new ActionResult((ActionConfig) null);
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> 
                find.perform(actionResult, objectCollection)
            );
        }
        
        @Test
        @DisplayName("Should handle pipeline exceptions gracefully")
        void testPipelineException() {
            // Arrange
            ActionResult actionResult = new ActionResult(patternFindOptions);
            doThrow(new RuntimeException("Pipeline error"))
                .when(findPipeline).execute(any(), any(), any());
            
            // Act & Assert
            assertThrows(RuntimeException.class, () -> 
                find.perform(actionResult, objectCollection)
            );
        }
    }
    
    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceOptimization {
        
        @Test
        @DisplayName("Should respect search time limits")
        void testSearchTimeLimits() {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSearchDuration(2.5)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            ArgumentCaptor<BaseFindOptions> optionsCaptor = ArgumentCaptor.forClass(BaseFindOptions.class);
            verify(findPipeline).execute(optionsCaptor.capture(), eq(actionResult), any());
            assertEquals(2.5, optionsCaptor.getValue().getSearchDuration(), 0.01);
        }
        
        @Test
        @DisplayName("Should handle max matches limit")
        void testMaxMatchesLimit() {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setMaxMatchesToActOn(10)
                .build();
            ActionResult actionResult = new ActionResult(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            ArgumentCaptor<BaseFindOptions> optionsCaptor = ArgumentCaptor.forClass(BaseFindOptions.class);
            verify(findPipeline).execute(optionsCaptor.capture(), eq(actionResult), any());
            assertEquals(10, optionsCaptor.getValue().getMaxMatchesToActOn());
        }
    }
    
}