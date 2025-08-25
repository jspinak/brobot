package io.github.jspinak.brobot.analysis.match;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for RelativeSizeFusionDecider.
 * Tests fusion strategy using size-relative distance thresholds for match grouping.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RelativeSizeFusionDecider Tests")
public class RelativeSizeFusionDeciderTest extends BrobotTestBase {
    
    @Mock
    private AbsoluteSizeFusionDecider absoluteSizeFusionDecider;
    
    private RelativeSizeFusionDecider fusionDecider;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        fusionDecider = new RelativeSizeFusionDecider(absoluteSizeFusionDecider);
    }
    
    @Nested
    @DisplayName("Basic Relative Fusion Logic")
    class BasicRelativeFusionLogic {
        
        @Test
        @DisplayName("Should calculate fusion distance based on smaller match height")
        void shouldCalculateFusionDistanceBasedOnSmallerHeight() {
            Match smallMatch = createMatch(100, 100, 50, 20); // Height: 20
            Match largeMatch = createMatch(200, 100, 100, 80); // Height: 80
            
            // With 50% threshold, should use 50% of smaller height (20)
            fusionDecider.isSameMatchGroup(smallMatch, largeMatch, 50, 50);
            
            // Expected distances: 10px (50% of 20px)
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(smallMatch), eq(largeMatch), eq(10), eq(10)
            );
        }
        
        @Test
        @DisplayName("Should handle equal-sized matches")
        void shouldHandleEqualSizedMatches() {
            Match match1 = createMatch(100, 100, 60, 40);
            Match match2 = createMatch(200, 100, 60, 40);
            
            // With equal heights, min height equals both heights
            fusionDecider.isSameMatchGroup(match1, match2, 25, 25);
            
            // Expected distances: 10px (25% of 40px)
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(match1), eq(match2), eq(10), eq(10)
            );
        }
        
        @Test
        @DisplayName("Should use minimum height when sizes differ")
        void shouldUseMinimumHeightWhenSizesDiffer() {
            Match tallMatch = createMatch(100, 100, 50, 100);
            Match shortMatch = createMatch(200, 100, 50, 30);
            
            fusionDecider.isSameMatchGroup(tallMatch, shortMatch, 100, 100);
            
            // Should use 100% of smaller height (30px)
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(tallMatch), eq(shortMatch), eq(30), eq(30)
            );
        }
    }
    
    @Nested
    @DisplayName("Percentage Calculations")
    class PercentageCalculations {
        
        @Test
        @DisplayName("Should handle 0% threshold")
        void shouldHandleZeroPercentThreshold() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(160, 100, 50, 50);
            
            fusionDecider.isSameMatchGroup(match1, match2, 0, 0);
            
            // 0% of height should result in 0 pixel distance
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(match1), eq(match2), eq(0), eq(0)
            );
        }
        
        @Test
        @DisplayName("Should handle 100% threshold")
        void shouldHandleOneHundredPercentThreshold() {
            Match match1 = createMatch(100, 100, 50, 40);
            Match match2 = createMatch(200, 100, 50, 60);
            
            fusionDecider.isSameMatchGroup(match1, match2, 100, 100);
            
            // 100% of smaller height (40px)
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(match1), eq(match2), eq(40), eq(40)
            );
        }
        
        @Test
        @DisplayName("Should handle percentages greater than 100%")
        void shouldHandlePercentagesGreaterThan100() {
            Match match1 = createMatch(100, 100, 50, 30);
            Match match2 = createMatch(200, 100, 50, 30);
            
            fusionDecider.isSameMatchGroup(match1, match2, 200, 150);
            
            // 200% of 30px = 60px, 150% of 30px = 45px
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(match1), eq(match2), eq(60), eq(45)
            );
        }
        
        @Test
        @DisplayName("Should handle different X and Y percentages")
        void shouldHandleDifferentXAndYPercentages() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(200, 100, 50, 50);
            
            fusionDecider.isSameMatchGroup(match1, match2, 20, 80);
            
            // 20% of 50px = 10px for X, 80% of 50px = 40px for Y
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(match1), eq(match2), eq(10), eq(40)
            );
        }
    }
    
    @Nested
    @DisplayName("Size Variation Scenarios")
    class SizeVariationScenarios {
        
        @Test
        @DisplayName("Should handle very small matches")
        void shouldHandleVerySmallMatches() {
            Match tiny1 = createMatch(100, 100, 5, 5);
            Match tiny2 = createMatch(110, 100, 5, 5);
            
            fusionDecider.isSameMatchGroup(tiny1, tiny2, 50, 50);
            
            // 50% of 5px = 2px (integer division)
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(tiny1), eq(tiny2), eq(2), eq(2)
            );
        }
        
        @Test
        @DisplayName("Should handle very large matches")
        void shouldHandleVeryLargeMatches() {
            Match large1 = createMatch(0, 0, 500, 400);
            Match large2 = createMatch(450, 0, 500, 400);
            
            fusionDecider.isSameMatchGroup(large1, large2, 10, 10);
            
            // 10% of 400px = 40px
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(large1), eq(large2), eq(40), eq(40)
            );
        }
        
        @Test
        @DisplayName("Should handle extreme size differences")
        void shouldHandleExtremeSizeDifferences() {
            Match tiny = createMatch(100, 100, 10, 10);
            Match huge = createMatch(200, 100, 500, 500);
            
            fusionDecider.isSameMatchGroup(tiny, huge, 50, 50);
            
            // Should use tiny match's height (10px), 50% = 5px
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(tiny), eq(huge), eq(5), eq(5)
            );
        }
        
        @Test
        @DisplayName("Should handle matches with different aspect ratios")
        void shouldHandleMatchesWithDifferentAspectRatios() {
            Match wideMatch = createMatch(100, 100, 200, 20); // Wide
            Match tallMatch = createMatch(300, 100, 20, 200); // Tall
            
            fusionDecider.isSameMatchGroup(wideMatch, tallMatch, 75, 75);
            
            // Should use smaller height (20px), 75% = 15px
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(wideMatch), eq(tallMatch), eq(15), eq(15)
            );
        }
    }
    
    @Nested
    @DisplayName("Delegation Behavior")
    class DelegationBehavior {
        
        @Test
        @DisplayName("Should delegate to absolute decider with calculated distances")
        void shouldDelegateToAbsoluteDecider() {
            Match match1 = createMatch(100, 100, 50, 60);
            Match match2 = createMatch(200, 100, 50, 80);
            
            when(absoluteSizeFusionDecider.isSameMatchGroup(any(), any(), anyInt(), anyInt()))
                .thenReturn(true);
            
            boolean result = fusionDecider.isSameMatchGroup(match1, match2, 50, 25);
            
            assertTrue(result);
            // Should calculate: X = 60 * 50 / 100 = 30, Y = 60 * 25 / 100 = 15
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(match1), eq(match2), eq(30), eq(15)
            );
        }
        
        @Test
        @DisplayName("Should return false when absolute decider returns false")
        void shouldReturnFalseWhenAbsoluteDeciderReturnsFalse() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(200, 100, 50, 50);
            
            when(absoluteSizeFusionDecider.isSameMatchGroup(any(), any(), anyInt(), anyInt()))
                .thenReturn(false);
            
            boolean result = fusionDecider.isSameMatchGroup(match1, match2, 50, 50);
            
            assertFalse(result);
        }
        
        @Test
        @DisplayName("Should return true when absolute decider returns true")
        void shouldReturnTrueWhenAbsoluteDeciderReturnsTrue() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(150, 100, 50, 50);
            
            when(absoluteSizeFusionDecider.isSameMatchGroup(any(), any(), anyInt(), anyInt()))
                .thenReturn(true);
            
            boolean result = fusionDecider.isSameMatchGroup(match1, match2, 30, 30);
            
            assertTrue(result);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle matches with height of 1")
        void shouldHandleMatchesWithHeightOf1() {
            Match match1 = createMatch(100, 100, 50, 1);
            Match match2 = createMatch(200, 100, 50, 1);
            
            fusionDecider.isSameMatchGroup(match1, match2, 100, 100);
            
            // 100% of 1px = 1px
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(match1), eq(match2), eq(1), eq(1)
            );
        }
        
        @Test
        @DisplayName("Should handle negative percentages")
        void shouldHandleNegativePercentages() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(150, 100, 50, 50);
            
            fusionDecider.isSameMatchGroup(match1, match2, -50, -50);
            
            // Negative percentages result in negative distances
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(match1), eq(match2), eq(-25), eq(-25)
            );
        }
        
        @Test
        @DisplayName("Should handle fractional percentage results")
        void shouldHandleFractionalPercentageResults() {
            Match match1 = createMatch(100, 100, 50, 33);
            Match match2 = createMatch(200, 100, 50, 33);
            
            fusionDecider.isSameMatchGroup(match1, match2, 30, 30);
            
            // 30% of 33px = 9.9px, should truncate to 9px (integer division)
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(match1), eq(match2), eq(9), eq(9)
            );
        }
    }
    
    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {
        
        @Test
        @DisplayName("Should handle button grouping")
        void shouldHandleButtonGrouping() {
            // Small buttons in a toolbar
            Match button1 = createMatch(100, 50, 30, 25);
            Match button2 = createMatch(135, 50, 30, 25);
            
            when(absoluteSizeFusionDecider.isSameMatchGroup(any(), any(), anyInt(), anyInt()))
                .thenReturn(true);
            
            // 20% threshold for small UI elements
            boolean result = fusionDecider.isSameMatchGroup(button1, button2, 20, 20);
            
            assertTrue(result);
            // 20% of 25px = 5px
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(button1), eq(button2), eq(5), eq(5)
            );
        }
        
        @Test
        @DisplayName("Should handle mixed-size UI elements")
        void shouldHandleMixedSizeUIElements() {
            // Small icon next to large panel
            Match icon = createMatch(100, 100, 16, 16);
            Match panel = createMatch(120, 100, 200, 150);
            
            // Conservative threshold for mixed sizes
            fusionDecider.isSameMatchGroup(icon, panel, 30, 30);
            
            // 30% of smaller height (16px) = 4px
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(icon), eq(panel), eq(4), eq(4)
            );
        }
        
        @Test
        @DisplayName("Should handle responsive UI scaling")
        void shouldHandleResponsiveUIScaling() {
            // Same logical button at different scales
            Match smallButton = createMatch(100, 100, 60, 30);
            Match largeButton = createMatch(200, 200, 120, 60);
            
            // Use percentage that scales with size
            fusionDecider.isSameMatchGroup(smallButton, largeButton, 40, 40);
            
            // 40% of smaller height (30px) = 12px
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(smallButton), eq(largeButton), eq(12), eq(12)
            );
        }
    }
    
    @Nested
    @DisplayName("Algorithm Verification")
    class AlgorithmVerification {
        
        @Test
        @DisplayName("Should correctly calculate minimum height")
        void shouldCorrectlyCalculateMinimumHeight() {
            Match match1 = createMatch(0, 0, 100, 75);
            Match match2 = createMatch(0, 0, 100, 125);
            
            fusionDecider.isSameMatchGroup(match1, match2, 40, 60);
            
            // Min height is 75, distances should be 30 and 45
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(match1), eq(match2), eq(30), eq(45)
            );
        }
        
        @Test
        @DisplayName("Should use integer division for percentage calculation")
        void shouldUseIntegerDivisionForPercentageCalculation() {
            Match match1 = createMatch(0, 0, 50, 17); // Odd height
            Match match2 = createMatch(0, 0, 50, 20);
            
            fusionDecider.isSameMatchGroup(match1, match2, 33, 33);
            
            // 17 * 33 / 100 = 5.61, should truncate to 5
            verify(absoluteSizeFusionDecider).isSameMatchGroup(
                eq(match1), eq(match2), eq(5), eq(5)
            );
        }
    }
    
    // Helper method
    private Match createMatch(int x, int y, int w, int h) {
        return new Match.Builder()
            .setRegion(new Region(x, y, w, h))
            .build();
    }
}