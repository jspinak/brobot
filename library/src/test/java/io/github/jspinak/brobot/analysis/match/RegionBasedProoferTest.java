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
 * Comprehensive test suite for RegionBasedProofer. Tests validation of matches within designated
 * search regions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegionBasedProofer Tests")
public class RegionBasedProoferTest extends BrobotTestBase {

    @Mock private SearchRegionResolver searchRegionResolver;

    private RegionBasedProofer regionBasedProofer;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        regionBasedProofer = new RegionBasedProofer(searchRegionResolver);
    }

    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorAndInitialization {

        @Test
        @DisplayName("Should create proofer with search region resolver")
        void shouldCreateProoferWithResolver() {
            RegionBasedProofer proofer = new RegionBasedProofer(searchRegionResolver);
            assertNotNull(proofer);
        }
    }

    @Nested
    @DisplayName("Basic Region Validation")
    class BasicRegionValidation {

        @Test
        @DisplayName("Should accept match completely within region")
        void shouldAcceptMatchWithinRegion() {
            Region searchRegion = new Region(100, 100, 200, 200);
            Match match = createMatch(150, 150, 50, 50);

            boolean isValid =
                    regionBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertTrue(isValid, "Match completely within region should be valid");
        }

        @Test
        @DisplayName("Should reject match outside region")
        void shouldRejectMatchOutsideRegion() {
            Region searchRegion = new Region(100, 100, 200, 200);
            Match match = createMatch(400, 400, 50, 50);

            boolean isValid =
                    regionBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertFalse(isValid, "Match outside region should be invalid");
        }

        @Test
        @DisplayName("Should reject match partially overlapping region")
        void shouldRejectPartiallyOverlappingMatch() {
            Region searchRegion = new Region(100, 100, 200, 200);
            Match match = createMatch(250, 250, 100, 100); // Partially overlaps

            boolean isValid =
                    regionBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertFalse(isValid, "Match partially overlapping should be invalid");
        }

        @Test
        @DisplayName("Should accept match at region boundary")
        void shouldAcceptMatchAtBoundary() {
            Region searchRegion = new Region(100, 100, 200, 200);
            Match match = createMatch(100, 100, 200, 200); // Exact same as region

            boolean isValid =
                    regionBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertTrue(isValid, "Match exactly matching region should be valid");
        }
    }

    @Nested
    @DisplayName("Multiple Region Validation")
    class MultipleRegionValidation {

        @Test
        @DisplayName("Should accept match in any of multiple regions")
        void shouldAcceptMatchInAnyRegion() {
            List<Region> regions =
                    Arrays.asList(
                            new Region(0, 0, 100, 100),
                            new Region(200, 200, 100, 100),
                            new Region(400, 400, 100, 100));
            Match match = createMatch(220, 220, 60, 60); // In second region

            boolean isValid = regionBasedProofer.isInSearchRegions(match, regions);

            assertTrue(isValid, "Match in any region should be valid");
        }

        @Test
        @DisplayName("Should reject match spanning adjacent regions")
        void shouldRejectMatchSpanningRegions() {
            List<Region> regions =
                    Arrays.asList(
                            new Region(0, 0, 100, 100),
                            new Region(100, 0, 100, 100) // Adjacent to first
                            );
            Match match = createMatch(50, 25, 100, 50); // Spans both regions

            boolean isValid = regionBasedProofer.isInSearchRegions(match, regions);

            assertFalse(isValid, "Match spanning multiple regions should be invalid");
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

            boolean isValid = regionBasedProofer.isInSearchRegions(match, regions);

            assertTrue(isValid, "Match in overlapping area should be valid");
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

            boolean isValid = regionBasedProofer.isInSearchRegions(match, config, pattern);

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

            boolean isValid = regionBasedProofer.isInSearchRegions(match, config, pattern);

            assertFalse(isValid, "Match should be invalid with no search regions");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty region list")
        void shouldHandleEmptyRegionList() {
            Match match = createMatch(100, 100, 50, 50);

            boolean isValid = regionBasedProofer.isInSearchRegions(match, Collections.emptyList());

            assertFalse(isValid, "Match should be invalid with empty region list");
        }

        @Test
        @DisplayName("Should handle very small matches")
        void shouldHandleVerySmallMatches() {
            Region searchRegion = new Region(100, 100, 200, 200);
            Match match = createMatch(150, 150, 1, 1); // 1x1 pixel match

            boolean isValid =
                    regionBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertTrue(isValid, "Small match within region should be valid");
        }

        @Test
        @DisplayName("Should handle very large matches")
        void shouldHandleVeryLargeMatches() {
            Region searchRegion = new Region(0, 0, 1920, 1080); // Full screen
            Match match = createMatch(100, 100, 1000, 800); // Large match

            boolean isValid =
                    regionBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertTrue(isValid, "Large match within large region should be valid");
        }

        @Test
        @DisplayName("Should reject match larger than region")
        void shouldRejectMatchLargerThanRegion() {
            Region searchRegion = new Region(100, 100, 50, 50);
            Match match = createMatch(90, 90, 100, 100); // Larger than region

            boolean isValid =
                    regionBasedProofer.isInSearchRegions(
                            match, Collections.singletonList(searchRegion));

            assertFalse(isValid, "Match larger than region should be invalid");
        }
    }

    @Nested
    @DisplayName("Performance Considerations")
    class PerformanceConsiderations {

        @Test
        @DisplayName("Should short-circuit on first valid region")
        void shouldShortCircuitOnFirstValid() {
            List<Region> regions =
                    Arrays.asList(
                            new Region(100, 100, 200, 200), // Match is in this one
                            new Region(400, 400, 200, 200),
                            new Region(700, 700, 200, 200));
            Match match = createMatch(150, 150, 50, 50);

            boolean isValid = regionBasedProofer.isInSearchRegions(match, regions);

            assertTrue(isValid);
            // Implementation should stop checking after first match
        }

        @Test
        @DisplayName("Should handle many regions efficiently")
        void shouldHandleManyRegions() {
            // Create 100 non-overlapping regions
            List<Region> regions = new java.util.ArrayList<>();
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    regions.add(new Region(i * 100, j * 100, 50, 50));
                }
            }

            Match match = createMatch(525, 525, 25, 25); // In middle region

            boolean isValid = regionBasedProofer.isInSearchRegions(match, regions);

            assertTrue(isValid);
        }
    }

    // Helper methods

    private Match createMatch(int x, int y, int w, int h) {
        return new Match.Builder().setRegion(new Region(x, y, w, h)).build();
    }
}
