package io.github.jspinak.brobot.tools.logging.visual;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.github.jspinak.brobot.test.BrobotTestBase;

@ExtendWith(MockitoExtension.class)
public class VisualFeedbackConfigTest extends BrobotTestBase {

    private VisualFeedbackConfig config;
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(TestConfiguration.class);

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        config = new VisualFeedbackConfig();
    }

    @Test
    public void testDefaultConfiguration() {
        // Assert default values
        assertTrue(config.isEnabled());
        assertTrue(config.isAutoHighlightFinds());
        assertFalse(config.isAutoHighlightSearchRegions());

        // Test find config defaults
        VisualFeedbackConfig.FindHighlightConfig findConfig = config.getFind();
        assertNotNull(findConfig);
        assertEquals("#00FF00", findConfig.getColor());
        assertEquals(2.0, findConfig.getDuration());
        assertEquals(3, findConfig.getBorderWidth());
        assertFalse(findConfig.isFlash());
        assertEquals(2, findConfig.getFlashCount());
        assertEquals(300L, findConfig.getFlashInterval());

        // Test search region config defaults
        VisualFeedbackConfig.SearchRegionHighlightConfig searchConfig = config.getSearchRegion();
        assertNotNull(searchConfig);
        assertEquals("#0000FF", searchConfig.getColor());
        assertEquals(1.0, searchConfig.getDuration());
        assertEquals(2, searchConfig.getBorderWidth());
        assertEquals(0.3, searchConfig.getOpacity());
        assertFalse(searchConfig.isFilled());
        assertFalse(searchConfig.isShowDimensions());

        // Test error config defaults
        VisualFeedbackConfig.ErrorHighlightConfig errorConfig = config.getError();
        assertNotNull(errorConfig);
        assertFalse(errorConfig.isEnabled());
        assertEquals("#FF0000", errorConfig.getColor());
        assertEquals(3.0, errorConfig.getDuration());
        assertTrue(errorConfig.isShowCrossMark());

        // Test click config defaults
        VisualFeedbackConfig.ClickHighlightConfig clickConfig = config.getClick();
        assertNotNull(clickConfig);
        assertTrue(clickConfig.isEnabled());
        assertEquals("#FFFF00", clickConfig.getColor());
        assertEquals(0.5, clickConfig.getDuration());
        assertEquals(20, clickConfig.getRadius());
        assertTrue(clickConfig.isRippleEffect());
    }

    @Test
    public void testSettersAndGetters() {
        // Test main config
        config.setEnabled(false);
        assertFalse(config.isEnabled());

        config.setAutoHighlightFinds(false);
        assertFalse(config.isAutoHighlightFinds());

        config.setAutoHighlightSearchRegions(true);
        assertTrue(config.isAutoHighlightSearchRegions());
    }

    @Test
    public void testFindHighlightConfig_ColorConversion() {
        VisualFeedbackConfig.FindHighlightConfig findConfig = config.getFind();

        // Test valid color
        findConfig.setColor("#FF0000");
        Color color = findConfig.getColorObject();
        assertEquals(Color.RED, color);

        // Test another valid color
        findConfig.setColor("#00FF00");
        color = findConfig.getColorObject();
        assertEquals(Color.GREEN, color);

        // Test invalid color (should default to green)
        findConfig.setColor("invalid");
        color = findConfig.getColorObject();
        assertEquals(Color.GREEN, color);
    }

    @Test
    public void testSearchRegionHighlightConfig_ColorConversion() {
        VisualFeedbackConfig.SearchRegionHighlightConfig searchConfig = config.getSearchRegion();

        // Test valid color without opacity
        searchConfig.setColor("#FF0000");
        searchConfig.setFilled(false);
        Color color = searchConfig.getColorObject();
        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
        assertEquals(255, color.getAlpha());

        // Test with opacity
        searchConfig.setFilled(true);
        searchConfig.setOpacity(0.5);
        color = searchConfig.getColorObject();
        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
        assertEquals(127, color.getAlpha(), 1);

        // Test invalid color (should default to blue)
        searchConfig.setColor("invalid");
        color = searchConfig.getColorObject();
        assertEquals(Color.BLUE, color);
    }

    @Test
    public void testErrorHighlightConfig_ColorConversion() {
        VisualFeedbackConfig.ErrorHighlightConfig errorConfig = config.getError();

        // Test valid color
        errorConfig.setColor("#FFFF00");
        Color color = errorConfig.getColorObject();
        assertEquals(Color.YELLOW, color);

        // Test invalid color (should default to red)
        errorConfig.setColor("invalid");
        color = errorConfig.getColorObject();
        assertEquals(Color.RED, color);
    }

    @Test
    public void testClickHighlightConfig_ColorConversion() {
        VisualFeedbackConfig.ClickHighlightConfig clickConfig = config.getClick();

        // Test valid color
        clickConfig.setColor("#00FFFF");
        Color color = clickConfig.getColorObject();
        assertEquals(Color.CYAN, color);

        // Test invalid color (should default to yellow)
        clickConfig.setColor("invalid");
        color = clickConfig.getColorObject();
        assertEquals(Color.YELLOW, color);
    }

    @Test
    public void testFindHighlightConfig_AllProperties() {
        VisualFeedbackConfig.FindHighlightConfig findConfig = config.getFind();

        findConfig.setColor("#123456");
        findConfig.setDuration(5.5);
        findConfig.setBorderWidth(10);
        findConfig.setFlash(true);
        findConfig.setFlashCount(5);
        findConfig.setFlashInterval(500L);

        assertEquals("#123456", findConfig.getColor());
        assertEquals(5.5, findConfig.getDuration());
        assertEquals(10, findConfig.getBorderWidth());
        assertTrue(findConfig.isFlash());
        assertEquals(5, findConfig.getFlashCount());
        assertEquals(500L, findConfig.getFlashInterval());
    }

    @Test
    public void testSearchRegionHighlightConfig_AllProperties() {
        VisualFeedbackConfig.SearchRegionHighlightConfig searchConfig = config.getSearchRegion();

        searchConfig.setColor("#654321");
        searchConfig.setDuration(3.3);
        searchConfig.setBorderWidth(5);
        searchConfig.setOpacity(0.75);
        searchConfig.setFilled(true);
        searchConfig.setShowDimensions(true);

        assertEquals("#654321", searchConfig.getColor());
        assertEquals(3.3, searchConfig.getDuration());
        assertEquals(5, searchConfig.getBorderWidth());
        assertEquals(0.75, searchConfig.getOpacity());
        assertTrue(searchConfig.isFilled());
        assertTrue(searchConfig.isShowDimensions());
    }

    @Test
    public void testErrorHighlightConfig_AllProperties() {
        VisualFeedbackConfig.ErrorHighlightConfig errorConfig = config.getError();

        errorConfig.setEnabled(true);
        errorConfig.setColor("#AABBCC");
        errorConfig.setDuration(10.0);
        errorConfig.setShowCrossMark(false);

        assertTrue(errorConfig.isEnabled());
        assertEquals("#AABBCC", errorConfig.getColor());
        assertEquals(10.0, errorConfig.getDuration());
        assertFalse(errorConfig.isShowCrossMark());
    }

    @Test
    public void testClickHighlightConfig_AllProperties() {
        VisualFeedbackConfig.ClickHighlightConfig clickConfig = config.getClick();

        clickConfig.setEnabled(false);
        clickConfig.setColor("#DDEEFF");
        clickConfig.setDuration(1.5);
        clickConfig.setRadius(50);
        clickConfig.setRippleEffect(false);

        assertFalse(clickConfig.isEnabled());
        assertEquals("#DDEEFF", clickConfig.getColor());
        assertEquals(1.5, clickConfig.getDuration());
        assertEquals(50, clickConfig.getRadius());
        assertFalse(clickConfig.isRippleEffect());
    }

    @Test
    public void testConfigurationProperties_Integration() {
        contextRunner
                .withPropertyValues(
                        "brobot.highlight.enabled=false",
                        "brobot.highlight.auto-highlight-finds=false",
                        "brobot.highlight.auto-highlight-search-regions=true",
                        "brobot.highlight.find.color=#112233",
                        "brobot.highlight.find.duration=3.5",
                        "brobot.highlight.find.border-width=5",
                        "brobot.highlight.find.flash=true",
                        "brobot.highlight.search-region.color=#445566",
                        "brobot.highlight.search-region.duration=2.5",
                        "brobot.highlight.search-region.filled=true",
                        "brobot.highlight.search-region.opacity=0.6",
                        "brobot.highlight.error.enabled=true",
                        "brobot.highlight.error.color=#778899",
                        "brobot.highlight.click.enabled=false",
                        "brobot.highlight.click.radius=30")
                .run(
                        context -> {
                            // The test runner context should process the configuration
                            assertNotNull(context);
                        });
    }

    @Test
    public void testSearchRegionConfig_OpacityBounds() {
        VisualFeedbackConfig.SearchRegionHighlightConfig searchConfig = config.getSearchRegion();

        // Test opacity at bounds
        searchConfig.setFilled(true);
        searchConfig.setColor("#FF0000");

        // Opacity 0
        searchConfig.setOpacity(0.0);
        Color color = searchConfig.getColorObject();
        assertEquals(0, color.getAlpha());

        // Opacity 1
        searchConfig.setOpacity(1.0);
        searchConfig.setFilled(true);
        color = searchConfig.getColorObject();
        assertEquals(255, color.getAlpha());

        // Opacity 0.25
        searchConfig.setOpacity(0.25);
        color = searchConfig.getColorObject();
        assertEquals(63, color.getAlpha(), 1);
    }

    @Test
    public void testColorParsing_HexFormats() {
        VisualFeedbackConfig.FindHighlightConfig findConfig = config.getFind();

        // Test 6-digit hex
        findConfig.setColor("#FFFFFF");
        assertEquals(Color.WHITE, findConfig.getColorObject());

        findConfig.setColor("#000000");
        Color black = findConfig.getColorObject();
        assertEquals(0, black.getRed());
        assertEquals(0, black.getGreen());
        assertEquals(0, black.getBlue());

        // Test without # prefix - will fail and return default
        findConfig.setColor("FF0000");
        assertEquals(Color.GREEN, findConfig.getColorObject()); // Default for find config
    }

    @Test
    public void testAllConfigsIndependence() {
        // Modify find config
        config.getFind().setColor("#111111");
        config.getFind().setDuration(10.0);

        // Verify other configs unchanged
        assertEquals("#0000FF", config.getSearchRegion().getColor());
        assertEquals("#FF0000", config.getError().getColor());
        assertEquals("#FFFF00", config.getClick().getColor());

        assertEquals(1.0, config.getSearchRegion().getDuration());
        assertEquals(3.0, config.getError().getDuration());
        assertEquals(0.5, config.getClick().getDuration());
    }

    @EnableConfigurationProperties(VisualFeedbackConfig.class)
    static class TestConfiguration {
        // Configuration class for testing property binding
    }
}
