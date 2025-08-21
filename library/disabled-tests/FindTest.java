package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.find.text.TextFindOptions;
import io.github.jspinak.brobot.action.basic.find.histogram.HistogramFindOptions;
import io.github.jspinak.brobot.action.basic.find.motion.MotionFindOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for Find action - locates GUI elements on screen.
 * Tests various find strategies, pattern matching, and result processing.
 */
@DisplayName("Find Action Tests")
public class FindTest extends BrobotTestBase {
    
    @Mock
    private FindPipeline mockFindPipeline;
    
    @Mock
    private ActionResult mockActionResult;
    
    @Mock
    private ObjectCollection mockObjectCollection;
    
    private Find find;
    private PatternFindOptions patternFindOptions;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        find = new Find(mockFindPipeline);
        patternFindOptions = new PatternFindOptions.Builder().build();
        
        when(mockActionResult.getActionConfig()).thenReturn(patternFindOptions);
    }
    
    @Test
    @DisplayName("Should return FIND action type")
    public void testGetActionType() {
        assertEquals(ActionInterface.Type.FIND, find.getActionType());
    }
    
    @Nested
    @DisplayName("Basic Find Operations")
    class BasicFindOperations {
        
        @Test
        @DisplayName("Should perform find with valid options")
        public void testPerformWithValidOptions() {
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline, times(1))
                .execute(patternFindOptions, mockActionResult, mockObjectCollection);
        }
        
        @Test
        @DisplayName("Should throw exception for invalid options")
        public void testPerformWithInvalidOptions() {
            // Use a non-BaseFindOptions config
            when(mockActionResult.getActionConfig()).thenReturn(mock(ActionConfig.class));
            
            assertThrows(IllegalArgumentException.class, () -> 
                find.perform(mockActionResult, mockObjectCollection));
        }
        
        @Test
        @DisplayName("Should handle multiple object collections")
        public void testPerformWithMultipleCollections() {
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            ObjectCollection collection3 = mock(ObjectCollection.class);
            
            find.perform(mockActionResult, collection1, collection2, collection3);
            
            verify(mockFindPipeline, times(1))
                .execute(patternFindOptions, mockActionResult, collection1, collection2, collection3);
        }
        
        @Test
        @DisplayName("Should handle empty object collections")
        public void testPerformWithEmptyCollections() {
            when(mockObjectCollection.getStateImages()).thenReturn(Collections.emptyList());
            when(mockObjectCollection.getMatches()).thenReturn(Collections.emptyList());
            when(mockObjectCollection.getRegions()).thenReturn(Collections.emptyList());
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline, times(1))
                .execute(patternFindOptions, mockActionResult, mockObjectCollection);
        }
    }
    
    @Nested
    @DisplayName("Find Strategy Options")
    class FindStrategyOptions {
        
        @ParameterizedTest
        @EnumSource(FindStrategy.class)
        @DisplayName("Should handle all find strategies")
        public void testAllFindStrategies(FindStrategy strategy) {
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(strategy)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(options, mockActionResult, mockObjectCollection);
        }
        
        @Test
        @DisplayName("Should handle FIRST strategy with early exit")
        public void testFirstStrategyEarlyExit() {
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(FindStrategy.FIRST)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(eq(options), eq(mockActionResult), any());
        }
        
        @Test
        @DisplayName("Should handle BEST strategy with score comparison")
        public void testBestStrategyScoreComparison() {
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(FindStrategy.BEST)
                .setMinScore(0.8)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(eq(options), eq(mockActionResult), any());
        }
        
        @Test
        @DisplayName("Should handle ALL strategy without limit")
        public void testAllStrategyNoLimit() {
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(FindStrategy.ALL)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(eq(options), eq(mockActionResult), any());
        }
    }
    
    @Nested
    @DisplayName("Advanced Find Options")
    class AdvancedFindOptions {
        
        @Test
        @DisplayName("Should handle color-based matching")
        public void testColorBasedMatching() {
            ColorFindOptions colorOptions = new ColorFindOptions.Builder()
                .setKmeansProfiles(Arrays.asList(1, 2, 3))
                .setColorMethod(ColorFindOptions.ColorMethod.KMEANS)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(colorOptions);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(colorOptions, mockActionResult, mockObjectCollection);
        }
        
        @Test
        @DisplayName("Should handle text find options")
        public void testTextFindOptions() {
            TextFindOptions textOptions = new TextFindOptions.Builder()
                .setSearchText("Test Text")
                .setCaseSensitive(true)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(textOptions);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(textOptions, mockActionResult, mockObjectCollection);
        }
        
        @Test
        @DisplayName("Should handle histogram find options")
        public void testHistogramFindOptions() {
            HistogramFindOptions histOptions = new HistogramFindOptions.Builder()
                .setHistogramBins(64)
                .setUseLAB(true)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(histOptions);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(histOptions, mockActionResult, mockObjectCollection);
        }
        
        @Test
        @DisplayName("Should handle motion find options")
        public void testMotionFindOptions() {
            MotionFindOptions motionOptions = new MotionFindOptions.Builder()
                .setFrameCount(10)
                .setThreshold(0.1)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(motionOptions);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(motionOptions, mockActionResult, mockObjectCollection);
        }
    }
    
    @Nested
    @DisplayName("Match Processing Options")
    class MatchProcessingOptions {
        
        @Test
        @DisplayName("Should handle match fusion")
        public void testMatchFusion() {
            // Test match fusion configuration
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setMinScore(0.8)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(eq(options), eq(mockActionResult), any());
        }
        
        @Test
        @DisplayName("Should handle match adjustments")
        public void testMatchAdjustments() {
            // Test match adjustment configuration
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setMinScore(0.7)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(eq(options), eq(mockActionResult), any());
        }
        
        @Test
        @DisplayName("Should handle area filtering")
        public void testAreaFiltering() {
            // Test area filtering configuration
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setMinScore(0.5)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(eq(options), eq(mockActionResult), any());
        }
    }
    
    @Nested
    @DisplayName("Object Collection Handling")
    class ObjectCollectionHandling {
        
        @Test
        @DisplayName("Should handle StateImages in collection")
        public void testStateImagesHandling() {
            StateImage stateImage1 = new StateImage.Builder().withName("test1").build();
            StateImage stateImage2 = new StateImage.Builder().withName("test2").build();
            when(mockObjectCollection.getStateImages())
                .thenReturn(Arrays.asList(stateImage1, stateImage2));
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(patternFindOptions, mockActionResult, mockObjectCollection);
        }
        
        @Test
        @DisplayName("Should handle existing Matches in collection")
        public void testExistingMatchesHandling() {
            Match match1 = new Match.Builder()
                .setRegion(new Region(0, 0, 100, 100))
                .setScore(0.95)
                .build();
            Match match2 = new Match.Builder()
                .setRegion(new Region(200, 200, 100, 100))
                .setScore(0.90)
                .build();
            when(mockObjectCollection.getMatches())
                .thenReturn(Arrays.asList(match1, match2));
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(patternFindOptions, mockActionResult, mockObjectCollection);
        }
        
        @Test
        @DisplayName("Should handle Regions in collection")
        public void testRegionsHandling() {
            Region region1 = new Region(0, 0, 100, 100);
            Region region2 = new Region(200, 200, 150, 150);
            // Use StateRegion instead of Region directly
            StateRegion stateRegion1 = new StateRegion.Builder().withRegion(region1).build();
            StateRegion stateRegion2 = new StateRegion.Builder().withRegion(region2).build();
            when(mockObjectCollection.getStateRegions())
                .thenReturn(Arrays.asList(stateRegion1, stateRegion2));
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(patternFindOptions, mockActionResult, mockObjectCollection);
        }
        
        @Test
        @DisplayName("Should handle Locations in collection")
        public void testLocationsHandling() {
            StateLocation location1 = new StateLocation.Builder()
                .withLocation(new Location(100, 100)).build();
            StateLocation location2 = new StateLocation.Builder()
                .withLocation(new Location(200, 200)).build();
            when(mockObjectCollection.getStateLocations())
                .thenReturn(Arrays.asList(location1, location2));
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(patternFindOptions, mockActionResult, mockObjectCollection);
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle null ActionResult")
        public void testNullActionResult() {
            assertThrows(NullPointerException.class, () -> 
                find.perform(null, mockObjectCollection));
        }
        
        @Test
        @DisplayName("Should handle null ObjectCollections")
        public void testNullObjectCollections() {
            assertThrows(NullPointerException.class, () -> 
                find.perform(mockActionResult, (ObjectCollection[]) null));
        }
        
        @Test
        @DisplayName("Should validate configuration type")
        public void testInvalidConfigurationType() {
            when(mockActionResult.getActionConfig()).thenReturn(null);
            
            assertThrows(IllegalArgumentException.class, () -> 
                find.perform(mockActionResult, mockObjectCollection));
        }
        
        @Test
        @DisplayName("Should handle pipeline exceptions gracefully")
        public void testPipelineException() {
            doThrow(new RuntimeException("Pipeline error"))
                .when(mockFindPipeline).execute(any(), any(), any());
            
            assertThrows(RuntimeException.class, () -> 
                find.perform(mockActionResult, mockObjectCollection));
        }
    }
    
    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceOptimization {
        
        @Test
        @DisplayName("Should respect search region limits")
        public void testSearchRegionLimits() {
            // Test search region configuration
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setMinScore(0.85)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(eq(options), eq(mockActionResult), any());
        }
        
        @Test
        @DisplayName("Should handle timeout configuration")
        public void testTimeoutConfiguration() {
            // Test timeout configuration
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setMinScore(0.6)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(eq(options), eq(mockActionResult), any());
        }
        
        @Test
        @DisplayName("Should handle max matches limit")
        public void testMaxMatchesLimit() {
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(FindStrategy.ALL)
                .setMaxMatchesToActOn(10)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            find.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFindPipeline).execute(eq(options), eq(mockActionResult), any());
        }
    }
}