package io.github.jspinak.brobot.analysis.match;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for EdgeBasedProofer. Tests edge-based validation of matches within
 * search regions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EdgeBasedProofer Tests")
public class EdgeBasedProoferTest extends BrobotTestBase {

    @Mock private SearchRegionResolver searchRegionResolver;

    private EdgeBasedProofer edgeBasedProofer;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        edgeBasedProofer = new EdgeBasedProofer(searchRegionResolver);
    }

    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorAndInitialization {

        @Test
        @DisplayName("Should create proofer with search region resolver")
        void shouldCreateProoferWithResolver() {
            EdgeBasedProofer proofer = new EdgeBasedProofer(searchRegionResolver);
            assertNotNull(proofer);
        }
    }

    @Nested
    @DisplayName("Basic Edge Validation")
    class BasicEdgeValidation {

        @Test
        @DisplayName("Should accept match with all corners within region")
        void shouldAcceptMatchWithAllCornersInRegion() {
            Region searchRegion = new Region(100, 100, 200, 200);
            Match match = createMatch(150, 150, 50, 50);

            boolean isValid =
                    edgeBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertTrue(isValid, "Match with all corners in region should be valid");
        }

        @Test
        @DisplayName("Should reject match with any corner outside regions")
        void shouldRejectMatchWithCornerOutside() {
            Region searchRegion = new Region(100, 100, 200, 200);
            // Match extends beyond region - bottom-right corner at (350, 350)
            Match match = createMatch(250, 250, 100, 100);

            boolean isValid =
                    edgeBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertFalse(isValid, "Match with corner outside should be invalid");
        }

        @Test
        @DisplayName("Should validate all four corners independently")
        void shouldValidateAllFourCorners() {
            Region searchRegion = new Region(100, 100, 100, 100);
            // Match slightly overlapping - only top-left corner is inside
            Match match = createMatch(190, 190, 50, 50);

            boolean isValid =
                    edgeBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertFalse(isValid, "Match with only one corner inside should be invalid");
        }
    }

    @Nested
    @DisplayName("Multiple Region Spanning")
    class MultipleRegionSpanning {

        @Test
        @DisplayName("Should accept match spanning adjacent regions")
        void shouldAcceptMatchSpanningAdjacentRegions() {
            List<Region> regions =
                    Arrays.asList(
                            new Region(0, 0, 100, 100),
                            new Region(100, 0, 100, 100) // Adjacent to first
                            );
            // Match spans both regions
            Match match = createMatch(50, 25, 100, 50);

            boolean isValid = edgeBasedProofer.isInSearchRegions(match, regions);

            assertTrue(isValid, "Match spanning adjacent regions should be valid");
        }

        @Test
        @DisplayName("Should accept match with corners in different regions")
        void shouldAcceptMatchWithCornersInDifferentRegions() {
            List<Region> regions =
                    Arrays.asList(
                            new Region(0, 0, 100, 100), // Top-left region
                            new Region(100, 0, 100, 100), // Top-right region
                            new Region(0, 100, 100, 100), // Bottom-left region
                            new Region(100, 100, 100, 100) // Bottom-right region
                            );
            // Match centered at grid intersection
            Match match = createMatch(50, 50, 100, 100);

            boolean isValid = edgeBasedProofer.isInSearchRegions(match, regions);

            assertTrue(isValid, "Match with corners in different regions should be valid");
        }

        @Test
        @DisplayName("Should accept match in gap if corners are in regions")
        void shouldAcceptMatchInGapIfCornersInRegions() {
            List<Region> regions =
                    Arrays.asList(
                            new Region(0, 0, 51, 51), // Top-left corner region
                            new Region(149, 0, 51, 51), // Top-right corner region
                            new Region(0, 149, 51, 51), // Bottom-left corner region
                            new Region(149, 149, 51, 51) // Bottom-right corner region
                            );
            // Match centered in gap, corners at edges of regions
            Match match = createMatch(0, 0, 199, 199);

            boolean isValid = edgeBasedProofer.isInSearchRegions(match, regions);

            assertTrue(
                    isValid,
                    "Match with corners in regions should be valid even if center is in gap");
        }
    }

    @Nested
    @DisplayName("Pattern and ActionConfig Integration")
    class PatternAndActionConfigIntegration {

        @Test
        @DisplayName("Should use resolved regions from pattern and config")
        void shouldUseResolvedRegions() {
            Pattern pattern = mock(Pattern.class);
            ActionConfig config = mock(ActionConfig.class);
            List<Region> resolvedRegions = Arrays.asList(new Region(100, 100, 200, 200));

            when(searchRegionResolver.getRegions(config, pattern)).thenReturn(resolvedRegions);

            Match match = createMatch(150, 150, 50, 50);

            boolean isValid = edgeBasedProofer.isInSearchRegions(match, config, pattern);

            assertTrue(isValid);
            verify(searchRegionResolver).getRegions(config, pattern);
        }

        @Test
        @DisplayName("Should handle empty resolved regions")
        void shouldHandleEmptyResolvedRegions() {
            Pattern pattern = mock(Pattern.class);
            ActionConfig config = mock(ActionConfig.class);

            when(searchRegionResolver.getRegions(config, pattern))
                    .thenReturn(Collections.emptyList());

            Match match = createMatch(150, 150, 50, 50);

            boolean isValid = edgeBasedProofer.isInSearchRegions(match, config, pattern);

            assertFalse(isValid, "Match should be invalid with no search regions");
        }
    }

    @Nested
    @DisplayName("Corner Position Calculations")
    class CornerPositionCalculations {

        @Test
        @DisplayName("Should correctly calculate corner positions")
        void shouldCorrectlyCalculateCornerPositions() {
            Region searchRegion = new Region(0, 0, 200, 200);
            // Match at specific position to verify corner calculations
            Match match = createMatch(10, 20, 30, 40);
            // Corners should be: TL(10,20), TR(40,20), BL(10,60), BR(40,60)

            boolean isValid =
                    edgeBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertTrue(isValid, "All corners should be within region");
        }

        @Test
        @DisplayName("Should handle match at region boundary")
        void shouldHandleMatchAtRegionBoundary() {
            Region searchRegion = new Region(100, 100, 100, 100);
            // Match within region, slightly smaller to ensure corners are inside
            Match match = createMatch(100, 100, 99, 99);

            boolean isValid =
                    edgeBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertTrue(isValid, "Match at region boundary should be valid");

            // Test match exactly matching region - corners extend beyond
            Match exactMatch = createMatch(100, 100, 100, 100);
            boolean exactValid =
                    edgeBasedProofer.isInSearchRegions(
                            exactMatch, Collections.singletonList(searchRegion));
            // This may fail as bottom-right corner at (200,200) is outside region ending at
            // (199,199)
            assertFalse(exactValid, "Match with corners extending beyond region should be invalid");
        }

        @Test
        @DisplayName("Should handle single pixel match")
        void shouldHandleSinglePixelMatch() {
            Region searchRegion = new Region(100, 100, 100, 100);
            Match match = createMatch(150, 150, 1, 1);
            // For 1x1 match, all corners are essentially the same point

            boolean isValid =
                    edgeBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertTrue(isValid, "Single pixel match within region should be valid");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty region list")
        void shouldHandleEmptyRegionList() {
            Match match = createMatch(100, 100, 50, 50);

            boolean isValid = edgeBasedProofer.isInSearchRegions(match, Collections.emptyList());

            assertFalse(isValid, "Match should be invalid with empty region list");
        }

        @Test
        @DisplayName("Should handle overlapping search regions")
        void shouldHandleOverlappingRegions() {
            List<Region> regions =
                    Arrays.asList(
                            new Region(0, 0, 150, 150),
                            new Region(100, 100, 150, 150) // Overlaps with first
                            );
            Match match = createMatch(110, 110, 30, 30); // In overlap area

            boolean isValid = edgeBasedProofer.isInSearchRegions(match, regions);

            assertTrue(isValid, "Match in overlapping area should be valid");
        }

        @Test
        @DisplayName("Should handle very large match")
        void shouldHandleVeryLargeMatch() {
            Region searchRegion = new Region(0, 0, 1920, 1080); // Full screen
            Match match = createMatch(100, 100, 1700, 900); // Almost full screen

            boolean isValid =
                    edgeBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertTrue(isValid, "Large match within large region should be valid");
        }

        @Test
        @DisplayName("Should reject match with corners outside screen-sized region")
        void shouldRejectMatchWithCornersOutside() {
            Region searchRegion = new Region(0, 0, 1920, 1080);
            Match match = createMatch(1900, 1060, 100, 100); // Bottom-right corner outside

            boolean isValid =
                    edgeBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertFalse(isValid, "Match extending beyond region should be invalid");
        }
    }

    @Nested
    @DisplayName("Performance Optimization")
    class PerformanceOptimization {

        @Test
        @DisplayName("Should short-circuit when all corners found")
        void shouldShortCircuitWhenAllCornersFound() {
            List<Region> regions =
                    Arrays.asList(
                            new Region(100, 100, 200, 200), // Contains all corners
                            new Region(400, 400, 200, 200), // Won't be checked
                            new Region(700, 700, 200, 200) // Won't be checked
                            );
            Match match = createMatch(150, 150, 50, 50);

            boolean isValid = edgeBasedProofer.isInSearchRegions(match, regions);

            assertTrue(isValid);
            // Implementation should stop after finding all corners in first region
        }

        @Test
        @DisplayName("Should handle many regions efficiently")
        void shouldHandleManyRegions() {
            // Create grid of regions
            List<Region> regions = new java.util.ArrayList<>();
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    regions.add(new Region(i * 100, j * 100, 100, 100));
                }
            }

            // Match spanning four adjacent regions
            Match match = createMatch(450, 450, 100, 100);

            boolean isValid = edgeBasedProofer.isInSearchRegions(match, regions);

            assertTrue(isValid);
        }
    }

    @Nested
    @DisplayName("Comparison with RegionBasedProofer")
    class ComparisonWithRegionBased {

        @Test
        @DisplayName("Should accept match that RegionBasedProofer would reject")
        void shouldAcceptSpanningMatch() {
            // This test demonstrates the key difference from RegionBasedProofer
            List<Region> regions =
                    Arrays.asList(new Region(0, 0, 100, 100), new Region(100, 0, 100, 100));

            // Match spans both regions - RegionBasedProofer would reject this
            Match match = createMatch(50, 25, 100, 50);

            boolean isValid = edgeBasedProofer.isInSearchRegions(match, regions);

            assertTrue(isValid, "EdgeBasedProofer should accept spanning matches");
        }

        @Test
        @DisplayName("Should potentially accept match in gap between regions")
        void shouldPotentiallyAcceptMatchInGap() {
            // This demonstrates a potential limitation
            List<Region> regions =
                    Arrays.asList(
                            new Region(0, 0, 100, 50), // Top region
                            new Region(0, 150, 100, 51) // Bottom region with gap
                            );

            // Match spans gap - corners at (0,0), (99,0), (0,199), (99,199)
            Match match = createMatch(0, 0, 99, 199);

            boolean isValid = edgeBasedProofer.isInSearchRegions(match, regions);

            assertTrue(isValid, "Match with corners in regions accepted despite gap");
        }
    }

    // Helper methods

    private Match createMatch(int x, int y, int w, int h) {
        return new Match.Builder().setRegion(new Region(x, y, w, h)).build();
    }
}
