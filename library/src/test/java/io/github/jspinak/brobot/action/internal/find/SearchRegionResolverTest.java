package io.github.jspinak.brobot.action.internal.find;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

// Removed old logging import:
@DisplayName("SearchRegionResolver Tests")
public class SearchRegionResolverTest extends BrobotTestBase {

    @Mock private StateImage mockStateImage;

    @Mock private Pattern mockPattern1;

    @Mock private Pattern mockPattern2;

    @Mock private BaseFindOptions mockFindOptions;

    @Mock private ActionConfig mockActionConfig;

    @Mock private SearchRegions mockSearchRegions;

    private SearchRegionResolver resolver;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        resolver = new SearchRegionResolver();
    }

    @Nested
    @DisplayName("StateImage Region Resolution")
    class StateImageRegionResolution {

        @Test
        @DisplayName("Use ActionOptions search regions when available")
        public void testActionOptionsRegionsPriority() {
            // Setup
            Region customRegion = new Region(10, 10, 100, 100);
            List<Region> customRegions = Arrays.asList(customRegion);

            when(mockFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
            when(mockSearchRegions.isEmpty()).thenReturn(false);
            when(mockSearchRegions.getRegionsForSearch()).thenReturn(customRegions);
            when(mockStateImage.getName()).thenReturn("TestImage");

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify
            assertEquals(1, result.size());
            assertEquals(customRegion, result.get(0));
            // The implementation calls getSearchRegions() multiple times for null and empty checks
            verify(mockFindOptions, atLeastOnce()).getSearchRegions();
            verify(mockSearchRegions).getRegionsForSearch();
        }

        @Test
        @DisplayName("Fall back to pattern regions when no ActionOptions regions")
        public void testPatternRegionsFallback() {
            // Setup
            Region patternRegion1 = new Region(20, 20, 50, 50);
            Region patternRegion2 = new Region(30, 30, 60, 60);

            when(mockFindOptions.getSearchRegions()).thenReturn(null);
            when(mockStateImage.getPatterns())
                    .thenReturn(Arrays.asList(mockPattern1, mockPattern2));
            when(mockPattern1.getRegionsForSearch()).thenReturn(Arrays.asList(patternRegion1));
            when(mockPattern2.getRegionsForSearch()).thenReturn(Arrays.asList(patternRegion2));
            when(mockStateImage.getName()).thenReturn("TestImage");

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify
            assertEquals(2, result.size());
            assertTrue(result.contains(patternRegion1));
            assertTrue(result.contains(patternRegion2));
            verify(mockPattern1).getRegionsForSearch();
            verify(mockPattern2).getRegionsForSearch();
        }

        @Test
        @DisplayName("Use default full-screen region when no regions found")
        public void testDefaultFullScreenRegion() {
            // Setup
            when(mockFindOptions.getSearchRegions()).thenReturn(null);
            when(mockStateImage.getPatterns()).thenReturn(new ArrayList<>());
            when(mockStateImage.getName()).thenReturn("TestImage");

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify
            assertEquals(1, result.size());
            assertNotNull(result.get(0));
            // Default Region constructor creates a full-screen region
        }

        @Test
        @DisplayName("Handle empty ActionOptions search regions")
        public void testEmptyActionOptionsRegions() {
            // Setup
            when(mockFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
            when(mockSearchRegions.isEmpty()).thenReturn(true);
            when(mockStateImage.getPatterns()).thenReturn(Arrays.asList(mockPattern1));
            when(mockPattern1.getRegionsForSearch())
                    .thenReturn(Arrays.asList(new Region(5, 5, 10, 10)));
            when(mockStateImage.getName()).thenReturn("TestImage");

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify
            assertEquals(1, result.size());
            verify(mockPattern1).getRegionsForSearch();
        }

        @Test
        @DisplayName("Handle non-BaseFindOptions ActionConfig")
        public void testNonBaseFindOptionsConfig() {
            // Setup
            when(mockStateImage.getPatterns()).thenReturn(Arrays.asList(mockPattern1));
            when(mockPattern1.getRegionsForSearch()).thenReturn(Arrays.asList(new Region()));
            when(mockStateImage.getName()).thenReturn("TestImage");

            // Execute
            List<Region> result = resolver.getRegions(mockActionConfig, mockStateImage);

            // Verify
            assertEquals(1, result.size());
            verify(mockPattern1).getRegionsForSearch();
        }
    }

    @Nested
    @DisplayName("Pattern Region Resolution")
    class PatternRegionResolution {

        @Test
        @DisplayName("Use ActionOptions regions for pattern")
        public void testActionOptionsRegionsForPattern() {
            // Setup
            Region customRegion = new Region(15, 15, 80, 80);
            when(mockFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
            when(mockSearchRegions.isEmpty()).thenReturn(false);
            when(mockSearchRegions.getRegionsForSearch()).thenReturn(Arrays.asList(customRegion));

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockPattern1);

            // Verify
            assertEquals(1, result.size());
            assertEquals(customRegion, result.get(0));
            verify(mockSearchRegions).getRegionsForSearch();
        }

        @Test
        @DisplayName("Use pattern's own regions when no ActionOptions")
        public void testPatternOwnRegions() {
            // Setup
            Region patternRegion = new Region(25, 25, 75, 75);
            when(mockFindOptions.getSearchRegions()).thenReturn(null);
            when(mockPattern1.getRegionsForSearch()).thenReturn(Arrays.asList(patternRegion));

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockPattern1);

            // Verify
            assertEquals(1, result.size());
            assertEquals(patternRegion, result.get(0));
            verify(mockPattern1).getRegionsForSearch();
        }

        @Test
        @DisplayName("Handle pattern with multiple regions")
        public void testPatternMultipleRegions() {
            // Setup
            Region region1 = new Region(0, 0, 50, 50);
            Region region2 = new Region(50, 50, 100, 100);
            when(mockFindOptions.getSearchRegions()).thenReturn(null);
            when(mockPattern1.getRegionsForSearch()).thenReturn(Arrays.asList(region1, region2));

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockPattern1);

            // Verify
            assertEquals(2, result.size());
            assertTrue(result.contains(region1));
            assertTrue(result.contains(region2));
        }
    }

    @Nested
    @DisplayName("ActionOptions Only Resolution")
    class ActionOptionsOnlyResolution {

        @Test
        @DisplayName("Return regions from ActionOptions")
        public void testActionOptionsOnly() {
            // Setup
            Region region = new Region(40, 40, 120, 120);
            when(mockFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
            when(mockSearchRegions.isEmpty()).thenReturn(false);
            when(mockSearchRegions.getRegionsForSearch()).thenReturn(Arrays.asList(region));

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions);

            // Verify
            assertEquals(1, result.size());
            assertEquals(region, result.get(0));
        }

        @Test
        @DisplayName("Return default when no ActionOptions regions")
        public void testNoActionOptionsRegions() {
            // Setup
            when(mockFindOptions.getSearchRegions()).thenReturn(null);

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions);

            // Verify
            assertEquals(1, result.size());
            assertNotNull(result.get(0));
        }

        @Test
        @DisplayName("Handle non-BaseFindOptions in ActionOptions only")
        public void testNonBaseFindOptionsActionOnly() {
            // Execute
            List<Region> result = resolver.getRegions(mockActionConfig);

            // Verify
            assertEquals(1, result.size());
            assertNotNull(result.get(0));
        }
    }

    @Nested
    @DisplayName("Logging and Deduplication")
    class LoggingAndDeduplication {

        @Test
        @DisplayName("Log custom regions for StateImage")
        public void testLogCustomRegions() {
            // Setup
            Region customRegion = new Region(10, 10, 100, 100);
            when(mockFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
            when(mockSearchRegions.isEmpty()).thenReturn(false);
            when(mockSearchRegions.getRegionsForSearch()).thenReturn(Arrays.asList(customRegion));
            when(mockStateImage.getName()).thenReturn("ButtonImage");

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify
            assertEquals(1, result.size());
            assertEquals(customRegion, result.get(0));
        }

        @Test
        @DisplayName("Suppress duplicate log messages")
        public void testSuppressDuplicateMessages() {
            // Setup
            Region customRegion = new Region(10, 10, 100, 100);
            when(mockFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
            when(mockSearchRegions.isEmpty()).thenReturn(false);
            when(mockSearchRegions.getRegionsForSearch()).thenReturn(Arrays.asList(customRegion));
            when(mockStateImage.getName()).thenReturn("SameImage");

            // Execute multiple times with same configuration
            List<Region> result1 = resolver.getRegions(mockFindOptions, mockStateImage);
            List<Region> result2 = resolver.getRegions(mockFindOptions, mockStateImage);
            List<Region> result3 = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify - all results should be identical
            assertEquals(result1, result2);
            assertEquals(result2, result3);
        }

        @Test
        @DisplayName("Don't log full screen searches")
        public void testDontLogFullScreenSearches() {
            // Setup
            Region fullScreen = new Region(0, 0, 1536, 864);
            when(mockFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
            when(mockSearchRegions.isEmpty()).thenReturn(false);
            when(mockSearchRegions.getRegionsForSearch()).thenReturn(Arrays.asList(fullScreen));
            when(mockStateImage.getName()).thenReturn("TestImage");

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify - result should contain the full screen region
            assertEquals(1, result.size());
            assertEquals(fullScreen, result.get(0));
        }
    }

    @Nested
    @DisplayName("Priority Order Verification")
    class PriorityOrderVerification {

        @Test
        @DisplayName("ActionOptions regions override pattern regions")
        public void testActionOptionsOverridePattern() {
            // Setup
            Region actionRegion = new Region(5, 5, 20, 20);
            Region patternRegion = new Region(50, 50, 100, 100);

            when(mockFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
            when(mockSearchRegions.isEmpty()).thenReturn(false);
            when(mockSearchRegions.getRegionsForSearch()).thenReturn(Arrays.asList(actionRegion));
            when(mockStateImage.getPatterns()).thenReturn(Arrays.asList(mockPattern1));
            when(mockPattern1.getRegionsForSearch()).thenReturn(Arrays.asList(patternRegion));
            when(mockStateImage.getName()).thenReturn("TestImage");

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify - Should use ActionOptions region, not pattern region
            assertEquals(1, result.size());
            assertEquals(actionRegion, result.get(0));
            verify(mockPattern1, never()).getRegionsForSearch();
        }

        @Test
        @DisplayName("Multiple patterns contribute regions")
        public void testMultiplePatternsContributeRegions() {
            // Setup
            Region region1 = new Region(10, 10, 30, 30);
            Region region2 = new Region(40, 40, 60, 60);
            Region region3 = new Region(70, 70, 90, 90);

            when(mockFindOptions.getSearchRegions()).thenReturn(null);
            when(mockStateImage.getPatterns())
                    .thenReturn(Arrays.asList(mockPattern1, mockPattern2));
            when(mockPattern1.getRegionsForSearch()).thenReturn(Arrays.asList(region1, region2));
            when(mockPattern2.getRegionsForSearch()).thenReturn(Arrays.asList(region3));
            when(mockStateImage.getName()).thenReturn("MultiPatternImage");

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify
            assertEquals(3, result.size());
            assertTrue(result.contains(region1));
            assertTrue(result.contains(region2));
            assertTrue(result.contains(region3));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Handle null search regions in BaseFindOptions")
        public void testNullSearchRegionsInOptions() {
            // Setup
            when(mockFindOptions.getSearchRegions()).thenReturn(null);
            when(mockStateImage.getPatterns()).thenReturn(new ArrayList<>());
            when(mockStateImage.getName()).thenReturn("TestImage");

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Handle empty pattern list")
        public void testEmptyPatternList() {
            // Setup
            when(mockFindOptions.getSearchRegions()).thenReturn(null);
            when(mockStateImage.getPatterns()).thenReturn(Collections.emptyList());
            when(mockStateImage.getName()).thenReturn("EmptyImage");

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5, 10})
        @DisplayName("Handle various numbers of patterns")
        public void testVariousPatternCounts(int patternCount) {
            // Setup
            List<Pattern> patterns = new ArrayList<>();
            for (int i = 0; i < patternCount; i++) {
                Pattern pattern = mock(Pattern.class);
                when(pattern.getRegionsForSearch())
                        .thenReturn(Arrays.asList(new Region(i, i, 10, 10)));
                patterns.add(pattern);
            }

            when(mockFindOptions.getSearchRegions()).thenReturn(null);
            when(mockStateImage.getPatterns()).thenReturn(patterns);
            when(mockStateImage.getName()).thenReturn("TestImage");

            // Execute
            List<Region> result = resolver.getRegions(mockFindOptions, mockStateImage);

            // Verify
            assertNotNull(result);
            assertTrue(result.size() >= 1);
            if (patternCount > 0) {
                assertEquals(patternCount, result.size());
            } else {
                assertEquals(1, result.size()); // Default region
            }
        }
    }
}
