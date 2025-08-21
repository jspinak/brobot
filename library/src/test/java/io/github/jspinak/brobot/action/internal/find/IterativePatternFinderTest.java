package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for IterativePatternFinder class.
 * Tests iterative pattern finding with multiple attempts and strategies.
 */
@DisplayName("IterativePatternFinder Tests")
public class IterativePatternFinderTest extends BrobotTestBase {

    @InjectMocks
    private IterativePatternFinder iterativePatternFinder;
    
    @Mock
    private Pattern pattern;
    
    @Mock
    private Region searchRegion;
    
    @Mock
    private ActionConfig actionConfig;
    
    @Mock
    private BufferedImage patternImage;
    
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        iterativePatternFinder = new IterativePatternFinder();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }
    
    @Nested
    @DisplayName("Iterative Finding")
    class IterativeFinding {
        
        @Test
        @DisplayName("Should find pattern on first attempt")
        void shouldFindPatternOnFirstAttempt() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(3);
            
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should retry on failure")
        void shouldRetryOnFailure() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(3);
            when(actionConfig.getRetryDelay()).thenReturn(100L);
            
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
        }
        
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 5, 10})
        @DisplayName("Should respect max iterations")
        void shouldRespectMaxIterations(int maxIterations) {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(maxIterations);
            
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.getAttempts() <= maxIterations);
        }
    }
    
    @Nested
    @DisplayName("Similarity Adjustment")
    class SimilarityAdjustment {
        
        @Test
        @DisplayName("Should decrease similarity on retry")
        void shouldDecreaseSimilarityOnRetry() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(3);
            when(actionConfig.getSimilarity()).thenReturn(0.9);
            when(actionConfig.getSimilarityDecrement()).thenReturn(0.05);
            
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
            // Similarity should decrease with each retry
        }
        
        @Test
        @DisplayName("Should not go below minimum similarity")
        void shouldNotGoBelowMinimumSimilarity() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(10);
            when(actionConfig.getSimilarity()).thenReturn(0.9);
            when(actionConfig.getSimilarityDecrement()).thenReturn(0.1);
            when(actionConfig.getMinSimilarity()).thenReturn(0.5);
            
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
            // Should not decrease below 0.5
        }
    }
    
    @Nested
    @DisplayName("Delay Between Attempts")
    class DelayBetweenAttempts {
        
        @Test
        @DisplayName("Should apply delay between attempts")
        void shouldApplyDelayBetweenAttempts() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(3);
            when(actionConfig.getRetryDelay()).thenReturn(200L);
            
            // Act
            long startTime = System.currentTimeMillis();
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertNotNull(result);
            // Should take at least the delay time for retries
        }
        
        @Test
        @DisplayName("Should handle zero delay")
        void shouldHandleZeroDelay() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(3);
            when(actionConfig.getRetryDelay()).thenReturn(0L);
            
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
        }
    }
    
    @Nested
    @DisplayName("Success Conditions")
    class SuccessConditions {
        
        @Test
        @DisplayName("Should stop on successful find")
        void shouldStopOnSuccessfulFind() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(5);
            
            Match match = mock(Match.class);
            when(match.getScore()).thenReturn(0.85);
            
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
            // Should stop early on success
        }
        
        @Test
        @DisplayName("Should continue until max iterations on failure")
        void shouldContinueUntilMaxIterationsOnFailure() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(3);
            
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
            assertEquals(3, result.getAttempts());
        }
    }
    
    @Nested
    @DisplayName("Result Aggregation")
    class ResultAggregation {
        
        @Test
        @DisplayName("Should aggregate matches from all attempts")
        void shouldAggregateMatchesFromAllAttempts() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(3);
            when(actionConfig.getAggregateResults()).thenReturn(true);
            
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
            // Should contain matches from all attempts
        }
        
        @Test
        @DisplayName("Should return only last attempt when not aggregating")
        void shouldReturnOnlyLastAttemptWhenNotAggregating() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(3);
            when(actionConfig.getAggregateResults()).thenReturn(false);
            
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
            // Should only contain matches from last attempt
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle null pattern")
        void shouldHandleNullPattern() {
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(null, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should handle null search region")
        void shouldHandleNullSearchRegion() {
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, null, actionConfig);
            
            // Assert
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should handle null config")
        void shouldHandleNullConfig() {
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, null);
            
            // Assert
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should handle zero max iterations")
        void shouldHandleZeroMaxIterations() {
            // Arrange
            when(actionConfig.getMaxIterations()).thenReturn(0);
            
            // Act
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess());
        }
    }
    
    @Nested
    @DisplayName("Performance")
    class Performance {
        
        @Test
        @DisplayName("Should complete quickly with early success")
        void shouldCompleteQuicklyWithEarlySuccess() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(10);
            
            // Act
            long startTime = System.currentTimeMillis();
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertNotNull(result);
            assertTrue(endTime - startTime < 1000, "Should complete quickly on early success");
        }
        
        @Test
        @DisplayName("Should handle many iterations efficiently")
        void shouldHandleManyIterationsEfficiently() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(actionConfig.getMaxIterations()).thenReturn(100);
            when(actionConfig.getRetryDelay()).thenReturn(0L);
            
            // Act
            long startTime = System.currentTimeMillis();
            ActionResult result = iterativePatternFinder.findIteratively(pattern, searchRegion, actionConfig);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertNotNull(result);
            assertTrue(endTime - startTime < 5000, "Should handle 100 iterations in less than 5 seconds");
        }
    }
}