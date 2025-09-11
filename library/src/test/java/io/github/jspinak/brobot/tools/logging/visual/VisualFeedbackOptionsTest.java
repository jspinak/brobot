package io.github.jspinak.brobot.tools.logging.visual;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.test.BrobotTestBase;

@ExtendWith(MockitoExtension.class)
public class VisualFeedbackOptionsTest extends BrobotTestBase {

    @Mock private VisualFeedbackConfig globalConfig;

    @Mock private VisualFeedbackConfig.FindHighlightConfig findConfig;

    @Mock private VisualFeedbackConfig.SearchRegionHighlightConfig searchRegionConfig;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Setup mock config with lenient stubs since not all tests use all mocks
        lenient().when(globalConfig.isEnabled()).thenReturn(true);
        lenient().when(globalConfig.getFind()).thenReturn(findConfig);
        lenient().when(globalConfig.getSearchRegion()).thenReturn(searchRegionConfig);

        lenient().when(findConfig.getColorObject()).thenReturn(Color.GREEN);
        lenient().when(findConfig.getDuration()).thenReturn(2.0);

        lenient().when(searchRegionConfig.getColorObject()).thenReturn(Color.BLUE);
        lenient().when(searchRegionConfig.getDuration()).thenReturn(1.0);
    }

    @Test
    public void testDefaultBuilder() {
        VisualFeedbackOptions options = VisualFeedbackOptions.builder().build();

        assertTrue(options.isHighlightEnabled());
        assertTrue(options.isHighlightFinds());
        assertFalse(options.isHighlightSearchRegions());
        assertFalse(options.isHighlightErrors());
        assertFalse(options.isFlashHighlight());
        assertEquals(2, options.getFlashCount());
        assertFalse(options.isShowMatchScore());
        assertFalse(options.isPersistHighlight());
        assertNull(options.getHighlightLabel());
        assertNull(options.getFindHighlightColor());
        assertNull(options.getFindHighlightDuration());
        assertNull(options.getSearchRegionHighlightColor());
        assertNull(options.getSearchRegionHighlightDuration());
    }

    @Test
    public void testDefaultsFactoryMethod() {
        VisualFeedbackOptions options = VisualFeedbackOptions.defaults();

        assertTrue(options.isHighlightEnabled());
        assertTrue(options.isHighlightFinds());
        assertFalse(options.isHighlightSearchRegions());
        assertFalse(options.isHighlightErrors());
    }

    @Test
    public void testNoneFactoryMethod() {
        VisualFeedbackOptions options = VisualFeedbackOptions.none();

        assertFalse(options.isHighlightEnabled());
        // Other defaults should remain
        assertTrue(options.isHighlightFinds());
        assertFalse(options.isHighlightSearchRegions());
    }

    @Test
    public void testDebugFactoryMethod() {
        VisualFeedbackOptions options = VisualFeedbackOptions.debug();

        assertTrue(options.isHighlightEnabled());
        assertTrue(options.isHighlightFinds());
        assertTrue(options.isHighlightSearchRegions());
        assertTrue(options.isHighlightErrors());
        assertTrue(options.isShowMatchScore());
        assertEquals(3.0, options.getFindHighlightDuration());
        assertFalse(options.isFlashHighlight());
        assertFalse(options.isPersistHighlight());
    }

    @Test
    public void testFindsOnlyFactoryMethod() {
        VisualFeedbackOptions options = VisualFeedbackOptions.findsOnly();

        assertTrue(options.isHighlightEnabled());
        assertTrue(options.isHighlightFinds());
        assertFalse(options.isHighlightSearchRegions());
        assertFalse(options.isHighlightErrors());
    }

    @Test
    public void testBuilderWithAllProperties() {
        Color customFindColor = Color.RED;
        Color customSearchColor = Color.YELLOW;

        VisualFeedbackOptions options =
                VisualFeedbackOptions.builder()
                        .highlightEnabled(false)
                        .highlightFinds(false)
                        .highlightSearchRegions(true)
                        .highlightErrors(true)
                        .findHighlightColor(customFindColor)
                        .findHighlightDuration(5.0)
                        .searchRegionHighlightColor(customSearchColor)
                        .searchRegionHighlightDuration(3.0)
                        .flashHighlight(true)
                        .flashCount(5)
                        .showMatchScore(true)
                        .persistHighlight(true)
                        .highlightLabel("Test Label")
                        .build();

        assertFalse(options.isHighlightEnabled());
        assertFalse(options.isHighlightFinds());
        assertTrue(options.isHighlightSearchRegions());
        assertTrue(options.isHighlightErrors());
        assertEquals(customFindColor, options.getFindHighlightColor());
        assertEquals(5.0, options.getFindHighlightDuration());
        assertEquals(customSearchColor, options.getSearchRegionHighlightColor());
        assertEquals(3.0, options.getSearchRegionHighlightDuration());
        assertTrue(options.isFlashHighlight());
        assertEquals(5, options.getFlashCount());
        assertTrue(options.isShowMatchScore());
        assertTrue(options.isPersistHighlight());
        assertEquals("Test Label", options.getHighlightLabel());
    }

    @Test
    public void testMergeWithGlobal_NoCustomValues() {
        VisualFeedbackOptions options = VisualFeedbackOptions.defaults();

        VisualFeedbackOptions merged = options.mergeWithGlobal(globalConfig);

        // Should use global values when no custom values set
        assertTrue(merged.isHighlightEnabled());
        assertTrue(merged.isHighlightFinds());
        assertFalse(merged.isHighlightSearchRegions());
        assertEquals(Color.GREEN, merged.getFindHighlightColor());
        assertEquals(2.0, merged.getFindHighlightDuration());
        assertEquals(Color.BLUE, merged.getSearchRegionHighlightColor());
        assertEquals(1.0, merged.getSearchRegionHighlightDuration());
    }

    @Test
    public void testMergeWithGlobal_WithCustomValues() {
        Color customFindColor = Color.MAGENTA;
        Color customSearchColor = Color.CYAN;

        VisualFeedbackOptions options =
                VisualFeedbackOptions.builder()
                        .findHighlightColor(customFindColor)
                        .findHighlightDuration(10.0)
                        .searchRegionHighlightColor(customSearchColor)
                        .searchRegionHighlightDuration(5.0)
                        .highlightLabel("Custom")
                        .flashHighlight(true)
                        .flashCount(3)
                        .build();

        VisualFeedbackOptions merged = options.mergeWithGlobal(globalConfig);

        // Custom values should override global
        assertEquals(customFindColor, merged.getFindHighlightColor());
        assertEquals(10.0, merged.getFindHighlightDuration());
        assertEquals(customSearchColor, merged.getSearchRegionHighlightColor());
        assertEquals(5.0, merged.getSearchRegionHighlightDuration());
        assertEquals("Custom", merged.getHighlightLabel());
        assertTrue(merged.isFlashHighlight());
        assertEquals(3, merged.getFlashCount());
    }

    @Test
    public void testMergeWithGlobal_GlobalDisabled() {
        when(globalConfig.isEnabled()).thenReturn(false);

        VisualFeedbackOptions options =
                VisualFeedbackOptions.builder().highlightEnabled(true).build();

        VisualFeedbackOptions merged = options.mergeWithGlobal(globalConfig);

        // Should be disabled if global is disabled
        assertFalse(merged.isHighlightEnabled());
    }

    @Test
    public void testMergeWithGlobal_LocalDisabled() {
        VisualFeedbackOptions options =
                VisualFeedbackOptions.builder().highlightEnabled(false).build();

        VisualFeedbackOptions merged = options.mergeWithGlobal(globalConfig);

        // Should be disabled if local is disabled
        assertFalse(merged.isHighlightEnabled());
    }

    @Test
    public void testMergeWithGlobal_PreservesNonColorProperties() {
        VisualFeedbackOptions options =
                VisualFeedbackOptions.builder()
                        .highlightFinds(false)
                        .highlightSearchRegions(true)
                        .highlightErrors(true)
                        .showMatchScore(true)
                        .persistHighlight(true)
                        .build();

        VisualFeedbackOptions merged = options.mergeWithGlobal(globalConfig);

        // Non-color properties should be preserved
        assertFalse(merged.isHighlightFinds());
        assertTrue(merged.isHighlightSearchRegions());
        assertTrue(merged.isHighlightErrors());
        assertTrue(merged.isShowMatchScore());
        assertTrue(merged.isPersistHighlight());

        // Colors should come from global
        assertEquals(Color.GREEN, merged.getFindHighlightColor());
        assertEquals(Color.BLUE, merged.getSearchRegionHighlightColor());
    }

    @Test
    public void testSettersAndGetters() {
        VisualFeedbackOptions options = VisualFeedbackOptions.builder().build();

        options.setHighlightEnabled(false);
        assertFalse(options.isHighlightEnabled());

        options.setHighlightFinds(false);
        assertFalse(options.isHighlightFinds());

        options.setHighlightSearchRegions(true);
        assertTrue(options.isHighlightSearchRegions());

        options.setHighlightErrors(true);
        assertTrue(options.isHighlightErrors());

        Color testColor = Color.ORANGE;
        options.setFindHighlightColor(testColor);
        assertEquals(testColor, options.getFindHighlightColor());

        options.setFindHighlightDuration(7.5);
        assertEquals(7.5, options.getFindHighlightDuration());

        options.setSearchRegionHighlightColor(testColor);
        assertEquals(testColor, options.getSearchRegionHighlightColor());

        options.setSearchRegionHighlightDuration(4.5);
        assertEquals(4.5, options.getSearchRegionHighlightDuration());

        options.setFlashHighlight(true);
        assertTrue(options.isFlashHighlight());

        options.setFlashCount(10);
        assertEquals(10, options.getFlashCount());

        options.setShowMatchScore(true);
        assertTrue(options.isShowMatchScore());

        options.setPersistHighlight(true);
        assertTrue(options.isPersistHighlight());

        options.setHighlightLabel("Test");
        assertEquals("Test", options.getHighlightLabel());
    }

    @Test
    public void testEqualsAndHashCode() {
        VisualFeedbackOptions options1 =
                VisualFeedbackOptions.builder()
                        .highlightEnabled(true)
                        .highlightFinds(true)
                        .highlightLabel("Test")
                        .build();

        VisualFeedbackOptions options2 =
                VisualFeedbackOptions.builder()
                        .highlightEnabled(true)
                        .highlightFinds(true)
                        .highlightLabel("Test")
                        .build();

        VisualFeedbackOptions options3 =
                VisualFeedbackOptions.builder()
                        .highlightEnabled(false)
                        .highlightFinds(true)
                        .highlightLabel("Test")
                        .build();

        assertEquals(options1, options2);
        assertEquals(options1.hashCode(), options2.hashCode());
        assertNotEquals(options1, options3);
    }

    @Test
    public void testToString() {
        VisualFeedbackOptions options =
                VisualFeedbackOptions.builder().highlightLabel("TestLabel").build();

        String str = options.toString();
        assertNotNull(str);
        assertTrue(str.contains("highlightLabel=TestLabel"));
    }

    @Test
    public void testMergeWithGlobal_PartialCustomValues() {
        // Only set find color, not duration
        VisualFeedbackOptions options =
                VisualFeedbackOptions.builder()
                        .findHighlightColor(Color.PINK)
                        .searchRegionHighlightDuration(8.0)
                        .build();

        VisualFeedbackOptions merged = options.mergeWithGlobal(globalConfig);

        // Custom find color, global find duration
        assertEquals(Color.PINK, merged.getFindHighlightColor());
        assertEquals(2.0, merged.getFindHighlightDuration()); // From global

        // Global search color, custom search duration
        assertEquals(Color.BLUE, merged.getSearchRegionHighlightColor()); // From global
        assertEquals(8.0, merged.getSearchRegionHighlightDuration());
    }
}
