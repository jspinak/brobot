package io.github.jspinak.brobot.analysis.match;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for AbsoluteSizeFusionDecider. Tests fusion strategy using fixed pixel
 * distance thresholds for match grouping.
 */
@DisplayName("AbsoluteSizeFusionDecider Tests")
public class AbsoluteSizeFusionDeciderTest extends BrobotTestBase {

    private AbsoluteSizeFusionDecider fusionDecider;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        fusionDecider = new AbsoluteSizeFusionDecider();
    }

    @Nested
    @DisplayName("Basic Fusion Logic")
    class BasicFusionLogic {

        @Test
        @DisplayName("Should fuse matches with overlapping expanded regions")
        void shouldFuseMatchesWithOverlappingExpandedRegions() {
            // Create two close matches
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(140, 100, 50, 50);

            // With expansion of 10 pixels, regions should overlap
            boolean shouldFuse = fusionDecider.isSameMatchGroup(match1, match2, 10, 10);

            assertTrue(shouldFuse, "Close matches should be fused when expanded regions overlap");
        }

        @Test
        @DisplayName("Should not fuse matches with non-overlapping expanded regions")
        void shouldNotFuseMatchesWithNonOverlappingExpandedRegions() {
            // Create two distant matches
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(200, 200, 50, 50);

            // Even with 10 pixel expansion, these shouldn't overlap
            boolean shouldFuse = fusionDecider.isSameMatchGroup(match1, match2, 10, 10);

            assertFalse(shouldFuse, "Distant matches should not be fused");
        }

        @Test
        @DisplayName("Should fuse exactly adjacent matches with any expansion")
        void shouldFuseExactlyAdjacentMatchesWithAnyExpansion() {
            // Create exactly adjacent matches (touching edges)
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(150, 100, 50, 50); // Exactly adjacent horizontally

            // Even with 1 pixel expansion, should overlap
            boolean shouldFuse = fusionDecider.isSameMatchGroup(match1, match2, 1, 1);

            assertTrue(shouldFuse, "Adjacent matches should fuse with minimal expansion");
        }
    }

    @Nested
    @DisplayName("Distance Threshold Tests")
    class DistanceThresholdTests {

        @Test
        @DisplayName("Should respect horizontal distance threshold")
        void shouldRespectHorizontalDistanceThreshold() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(170, 100, 50, 50); // 20 pixels apart horizontally

            // Should not fuse with 9 pixel expansion (total gap of 20 > 2*9)
            assertFalse(fusionDecider.isSameMatchGroup(match1, match2, 9, 100));

            // Should fuse with 11 pixel expansion (total expansion 22 > gap of 20)
            assertTrue(fusionDecider.isSameMatchGroup(match1, match2, 11, 100));
        }

        @Test
        @DisplayName("Should respect vertical distance threshold")
        void shouldRespectVerticalDistanceThreshold() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(100, 170, 50, 50); // 20 pixels apart vertically

            // Should not fuse with 9 pixel expansion
            assertFalse(fusionDecider.isSameMatchGroup(match1, match2, 100, 9));

            // Should fuse with 11 pixel expansion
            assertTrue(fusionDecider.isSameMatchGroup(match1, match2, 100, 11));
        }

        @Test
        @DisplayName("Should handle different X and Y thresholds independently")
        void shouldHandleDifferentThresholdsIndependently() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(160, 155, 50, 50); // 10 pixels apart in X, 5 in Y

            // Large X threshold, small Y threshold - should not fuse
            assertFalse(fusionDecider.isSameMatchGroup(match1, match2, 20, 2));

            // Small X threshold, large Y threshold - should not fuse
            assertFalse(fusionDecider.isSameMatchGroup(match1, match2, 4, 20));

            // Both thresholds sufficient - should fuse
            assertTrue(fusionDecider.isSameMatchGroup(match1, match2, 6, 3));
        }
    }

    @Nested
    @DisplayName("Diagonal and Corner Cases")
    class DiagonalAndCornerCases {

        @Test
        @DisplayName("Should fuse diagonally positioned matches when in range")
        void shouldFuseDiagonallyPositionedMatches() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(160, 160, 50, 50); // Diagonal offset

            // Calculate if expansion creates overlap
            // Gap is 10 pixels in both X and Y
            boolean shouldFuse = fusionDecider.isSameMatchGroup(match1, match2, 6, 6);

            assertTrue(
                    shouldFuse,
                    "Diagonally positioned matches should fuse when expanded regions overlap");
        }

        @Test
        @DisplayName("Should handle corner-to-corner positioning")
        void shouldHandleCornerToCornerPositioning() {
            Match match1 = createMatch(100, 100, 50, 50);
            // match2's top-left is at match1's bottom-right
            Match match2 = createMatch(150, 150, 50, 50);

            // Exactly touching at corners, any expansion should cause fusion
            boolean shouldFuse = fusionDecider.isSameMatchGroup(match1, match2, 1, 1);

            assertTrue(shouldFuse, "Corner-touching matches should fuse with minimal expansion");
        }
    }

    @Nested
    @DisplayName("Zero and Negative Thresholds")
    class ZeroAndNegativeThresholds {

        @Test
        @DisplayName("Should handle zero thresholds")
        void shouldHandleZeroThresholds() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(149, 100, 50, 50); // 1 pixel overlap

            // With zero expansion, only overlapping matches should fuse
            boolean shouldFuse = fusionDecider.isSameMatchGroup(match1, match2, 0, 0);

            assertTrue(shouldFuse, "Overlapping matches should fuse with zero threshold");

            // Exactly adjacent matches should not fuse with zero threshold (no overlap)
            Match match3 = createMatch(150, 100, 50, 50); // Exactly adjacent
            assertFalse(fusionDecider.isSameMatchGroup(match1, match3, 0, 0));

            // Non-touching matches should not fuse
            Match match4 = createMatch(151, 100, 50, 50); // 1 pixel gap
            assertFalse(fusionDecider.isSameMatchGroup(match1, match4, 0, 0));
        }

        @Test
        @DisplayName("Should handle negative thresholds")
        void shouldHandleNegativeThresholds() {
            Match match1 = createMatch(100, 100, 60, 60);
            Match match2 = createMatch(140, 100, 60, 60); // 20 pixel overlap

            // Negative threshold shrinks the regions
            boolean shouldFuse = fusionDecider.isSameMatchGroup(match1, match2, -5, -5);

            // With -5 threshold, effective regions are smaller, might still overlap
            assertTrue(
                    shouldFuse,
                    "Overlapping matches should still fuse with negative threshold if overlap"
                            + " remains");

            // Large negative threshold should prevent fusion
            assertFalse(fusionDecider.isSameMatchGroup(match1, match2, -15, -15));
        }
    }

    @Nested
    @DisplayName("Large Threshold Scenarios")
    class LargeThresholdScenarios {

        @Test
        @DisplayName("Should fuse distant matches with large thresholds")
        void shouldFuseDistantMatchesWithLargeThresholds() {
            Match match1 = createMatch(0, 0, 50, 50);
            Match match2 = createMatch(200, 200, 50, 50);

            // Very large thresholds should fuse even distant matches
            boolean shouldFuse = fusionDecider.isSameMatchGroup(match1, match2, 100, 100);

            assertTrue(shouldFuse, "Large thresholds should fuse distant matches");
        }

        @Test
        @DisplayName("Should handle screen-spanning thresholds")
        void shouldHandleScreenSpanningThresholds() {
            Match match1 = createMatch(0, 0, 10, 10);
            Match match2 = createMatch(1900, 1000, 10, 10);

            // Extremely large thresholds
            boolean shouldFuse = fusionDecider.isSameMatchGroup(match1, match2, 1000, 1000);

            assertTrue(shouldFuse, "Screen-spanning thresholds should fuse any matches");
        }
    }

    @Nested
    @DisplayName("Match Size Variations")
    class MatchSizeVariations {

        @Test
        @DisplayName("Should handle matches of different sizes")
        void shouldHandleMatchesOfDifferentSizes() {
            Match smallMatch = createMatch(100, 100, 20, 20);
            Match largeMatch = createMatch(125, 100, 100, 100);

            // The expansion is absolute, not relative to size
            // With 5 pixel expansion:
            // smallMatch expanded: (95, 95) to (125, 125)
            // largeMatch expanded: (120, 95) to (230, 205)
            // These overlap from x=120 to x=125
            boolean shouldFuse = fusionDecider.isSameMatchGroup(smallMatch, largeMatch, 5, 5);

            assertTrue(
                    shouldFuse, "Different sized matches should fuse based on absolute distance");

            // Test with no overlap even after expansion
            Match distantLarge = createMatch(131, 100, 100, 100);
            assertFalse(
                    fusionDecider.isSameMatchGroup(smallMatch, distantLarge, 5, 5),
                    "Matches too far apart should not fuse");
        }

        @Test
        @DisplayName("Should handle very small matches")
        void shouldHandleVerySmallMatches() {
            Match tiny1 = createMatch(100, 100, 1, 1);
            Match tiny2 = createMatch(105, 100, 1, 1);

            // 4 pixels apart, should not fuse with 1 pixel expansion
            assertFalse(fusionDecider.isSameMatchGroup(tiny1, tiny2, 1, 1));

            // Should fuse with 3 pixel expansion
            assertTrue(fusionDecider.isSameMatchGroup(tiny1, tiny2, 3, 3));
        }

        @Test
        @DisplayName("Should handle very large matches")
        void shouldHandleVeryLargeMatches() {
            Match large1 = createMatch(0, 0, 500, 500);
            Match large2 = createMatch(400, 400, 500, 500);

            // Already overlapping, should fuse even with zero threshold
            assertTrue(fusionDecider.isSameMatchGroup(large1, large2, 0, 0));

            // Should still fuse with negative threshold if overlap remains
            assertTrue(fusionDecider.isSameMatchGroup(large1, large2, -10, -10));
        }
    }

    @Nested
    @DisplayName("Boundary Conditions")
    class BoundaryConditions {

        @Test
        @DisplayName("Should handle matches at screen boundaries")
        void shouldHandleMatchesAtScreenBoundaries() {
            Match topLeft = createMatch(0, 0, 50, 50);
            Match nearTopLeft = createMatch(10, 10, 50, 50);

            // Overlapping at screen edge
            boolean shouldFuse = fusionDecider.isSameMatchGroup(topLeft, nearTopLeft, 5, 5);

            assertTrue(shouldFuse, "Matches at screen boundaries should fuse normally");
        }

        @Test
        @DisplayName("Should handle matches with negative coordinates after expansion")
        void shouldHandleNegativeCoordinatesAfterExpansion() {
            Match match1 = createMatch(5, 5, 50, 50);
            Match match2 = createMatch(20, 20, 50, 50);

            // Expansion of 10 would create negative coordinates for match1
            boolean shouldFuse = fusionDecider.isSameMatchGroup(match1, match2, 10, 10);

            assertTrue(shouldFuse, "Should handle negative coordinates in expanded regions");
        }
    }

    @Nested
    @DisplayName("Symmetry and Consistency")
    class SymmetryAndConsistency {

        @Test
        @DisplayName("Should be symmetric - order shouldn't matter")
        void shouldBeSymmetric() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(160, 100, 50, 50);

            boolean result1 = fusionDecider.isSameMatchGroup(match1, match2, 10, 10);
            boolean result2 = fusionDecider.isSameMatchGroup(match2, match1, 10, 10);

            assertEquals(result1, result2, "Fusion decision should be symmetric");
        }

        @Test
        @DisplayName("Should be consistent across multiple calls")
        void shouldBeConsistentAcrossMultipleCalls() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(145, 100, 50, 50);

            boolean firstCall = fusionDecider.isSameMatchGroup(match1, match2, 8, 8);
            boolean secondCall = fusionDecider.isSameMatchGroup(match1, match2, 8, 8);
            boolean thirdCall = fusionDecider.isSameMatchGroup(match1, match2, 8, 8);

            assertEquals(firstCall, secondCall);
            assertEquals(secondCall, thirdCall);
        }
    }

    // Helper method
    private Match createMatch(int x, int y, int w, int h) {
        return new Match.Builder().setRegion(new Region(x, y, w, h)).build();
    }
}
