package io.github.jspinak.brobot.action.internal.region;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;

@DisplayName("ActionConfigRegionProvider Tests")
class ActionConfigRegionProviderTest extends BrobotTestBase {

    private ActionConfigRegionProvider regionProvider;

    @Mock private PatternFindOptions mockPatternFindOptions;

    @Mock private SearchRegions mockSearchRegions;

    @Mock private ActionConfig mockActionConfig;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        regionProvider = new ActionConfigRegionProvider();
    }

    @Test
    @DisplayName("Should return empty list when ActionConfig is null")
    void testGetRegionsFromActionConfig_NullConfig() {
        List<Region> result = regionProvider.getRegionsFromActionConfig(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when ActionConfig is not PatternFindOptions")
    void testGetRegionsFromActionConfig_NonPatternFindOptions() {
        List<Region> result = regionProvider.getRegionsFromActionConfig(mockActionConfig);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when PatternFindOptions has null search regions")
    void testGetRegionsFromActionConfig_NullSearchRegions() {
        when(mockPatternFindOptions.getSearchRegions()).thenReturn(null);

        List<Region> result = regionProvider.getRegionsFromActionConfig(mockPatternFindOptions);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return regions when PatternFindOptions has search regions")
    void testGetRegionsFromActionConfig_WithSearchRegions() {
        Region region1 = new Region(0, 0, 100, 100);
        Region region2 = new Region(100, 100, 200, 200);
        List<Region> expectedRegions = Arrays.asList(region1, region2);

        when(mockPatternFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
        when(mockSearchRegions.getRegions(true)).thenReturn(expectedRegions);

        List<Region> result = regionProvider.getRegionsFromActionConfig(mockPatternFindOptions);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedRegions, result);
    }

    @Test
    @DisplayName("Should convert PatternFindOptions regions to StateRegions")
    void testConvertToSearchRegions() {
        Region region1 = new Region(10, 10, 50, 50);
        Region region2 = new Region(60, 60, 100, 100);
        List<Region> regions = Arrays.asList(region1, region2);

        when(mockPatternFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
        when(mockSearchRegions.getRegions(true)).thenReturn(regions);

        List<StateRegion> result = regionProvider.convertToSearchRegions(mockPatternFindOptions);

        assertNotNull(result);
        assertEquals(2, result.size());

        StateRegion stateRegion1 = result.get(0);
        StateRegion stateRegion2 = result.get(1);

        assertEquals(region1, stateRegion1.getSearchRegion());
        assertEquals(region2, stateRegion2.getSearchRegion());
    }

    @Test
    @DisplayName("Should return empty list when converting null config")
    void testConvertToSearchRegions_NullConfig() {
        List<StateRegion> result = regionProvider.convertToSearchRegions(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return regions for pattern search with fallback to full screen")
    void testGetPatternSearchRegions_WithRegions() {
        Region region = new Region(20, 20, 80, 80);
        List<Region> regions = Collections.singletonList(region);

        when(mockPatternFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
        when(mockSearchRegions.getRegions(true)).thenReturn(regions);

        List<Region> result = regionProvider.getPatternSearchRegions(mockPatternFindOptions);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(region, result.get(0));
    }

    @Test
    @DisplayName("Should return full screen region when no search regions defined")
    void testGetPatternSearchRegions_NoRegions_ReturnsFullScreen() {
        when(mockPatternFindOptions.getSearchRegions()).thenReturn(null);

        List<Region> result = regionProvider.getPatternSearchRegions(mockPatternFindOptions);

        assertNotNull(result);
        assertEquals(1, result.size());

        Region fullScreen = result.get(0);
        assertNotNull(fullScreen);
        // In mock mode, screen dimensions might be 0 or default values
        assertTrue(fullScreen.w() >= 0);
        assertTrue(fullScreen.h() >= 0);
    }

    @Test
    @DisplayName("Should get state image search regions")
    void testGetStateImageSearchRegions() {
        StateRegion stateRegion1 = mock(StateRegion.class);
        StateRegion stateRegion2 = mock(StateRegion.class);
        Region region1 = new Region(0, 0, 50, 50);
        Region region2 = new Region(50, 50, 100, 100);

        when(stateRegion1.getSearchRegion()).thenReturn(region1);
        when(stateRegion2.getSearchRegion()).thenReturn(region2);

        List<StateRegion> stateRegions = Arrays.asList(stateRegion1, stateRegion2);

        when(mockPatternFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
        when(mockSearchRegions.getRegions(true)).thenReturn(Arrays.asList(region1, region2));

        List<Region> result = regionProvider.getStateImageSearchRegions(mockPatternFindOptions);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(region1));
        assertTrue(result.contains(region2));
    }

    @Test
    @DisplayName("Should check if ActionConfig has search regions")
    void testHasSearchRegions_True() {
        when(mockPatternFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
        when(mockSearchRegions.getRegions(true))
                .thenReturn(Arrays.asList(new Region(0, 0, 10, 10)));

        boolean result = regionProvider.hasSearchRegions(mockPatternFindOptions);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when ActionConfig has no search regions")
    void testHasSearchRegions_False() {
        when(mockPatternFindOptions.getSearchRegions()).thenReturn(null);

        boolean result = regionProvider.hasSearchRegions(mockPatternFindOptions);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false for null ActionConfig")
    void testHasSearchRegions_NullConfig() {
        boolean result = regionProvider.hasSearchRegions(null);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should merge regions without duplicates")
    void testMergeWithExistingRegions() {
        Region region1 = new Region(0, 0, 50, 50);
        Region region2 = new Region(50, 50, 100, 100);
        Region region3 = new Region(100, 100, 150, 150);

        when(mockPatternFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
        when(mockSearchRegions.getRegions(true)).thenReturn(Arrays.asList(region1, region2));

        List<Region> existingRegions = new ArrayList<>(Arrays.asList(region2, region3));

        List<Region> result =
                regionProvider.mergeWithExistingRegions(mockPatternFindOptions, existingRegions);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(region1));
        assertTrue(result.contains(region2));
        assertTrue(result.contains(region3));

        // Verify no duplicates
        long uniqueCount = result.stream().distinct().count();
        assertEquals(3, uniqueCount);
    }

    @Test
    @DisplayName("Should handle empty existing regions when merging")
    void testMergeWithExistingRegions_EmptyExisting() {
        Region region1 = new Region(0, 0, 50, 50);

        when(mockPatternFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
        when(mockSearchRegions.getRegions(true)).thenReturn(Collections.singletonList(region1));

        List<Region> result =
                regionProvider.mergeWithExistingRegions(mockPatternFindOptions, new ArrayList<>());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(region1, result.get(0));
    }

    @Test
    @DisplayName("Should handle null existing regions when merging")
    void testMergeWithExistingRegions_NullExisting() {
        Region region1 = new Region(0, 0, 50, 50);

        when(mockPatternFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
        when(mockSearchRegions.getRegions(true)).thenReturn(Collections.singletonList(region1));

        List<Region> result = regionProvider.mergeWithExistingRegions(mockPatternFindOptions, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(region1, result.get(0));
    }

    @Test
    @DisplayName("Should return existing regions when ActionConfig has no regions")
    void testMergeWithExistingRegions_NoConfigRegions() {
        Region region1 = new Region(0, 0, 50, 50);
        List<Region> existingRegions = new ArrayList<>(Collections.singletonList(region1));

        when(mockPatternFindOptions.getSearchRegions()).thenReturn(null);

        List<Region> result =
                regionProvider.mergeWithExistingRegions(mockPatternFindOptions, existingRegions);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(region1, result.get(0));
    }

    @Test
    @DisplayName("Should handle overlapping regions correctly")
    void testMergeWithExistingRegions_OverlappingRegions() {
        // Create regions that overlap but are not identical
        Region region1 = new Region(0, 0, 60, 60);
        Region region2 = new Region(40, 40, 100, 100);
        Region region3 = new Region(0, 0, 60, 60); // Duplicate of region1

        when(mockPatternFindOptions.getSearchRegions()).thenReturn(mockSearchRegions);
        when(mockSearchRegions.getRegions(true)).thenReturn(Arrays.asList(region1, region2));

        List<Region> existingRegions = new ArrayList<>(Arrays.asList(region2, region3));

        List<Region> result =
                regionProvider.mergeWithExistingRegions(mockPatternFindOptions, existingRegions);

        assertNotNull(result);
        // Should have region1, region2, and not duplicate region3
        assertEquals(2, result.size());
        assertTrue(result.contains(region1));
        assertTrue(result.contains(region2));
    }
}
