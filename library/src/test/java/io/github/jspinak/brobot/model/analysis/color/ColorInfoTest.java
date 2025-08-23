package io.github.jspinak.brobot.model.analysis.color;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static io.github.jspinak.brobot.model.analysis.color.ColorInfo.ColorStat.*;

/**
 * Comprehensive test suite for ColorInfo.
 * Tests statistical color channel information storage and operations.
 */
@DisplayName("ColorInfo Tests")
public class ColorInfoTest extends BrobotTestBase {
    
    private ColorInfo colorInfo;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        colorInfo = new ColorInfo(ColorSchema.ColorValue.HUE);
    }
    
    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create color info with color value")
        void shouldCreateColorInfo() {
            assertNotNull(colorInfo);
            assertEquals(ColorSchema.ColorValue.HUE, colorInfo.getColorValue());
            assertNotNull(colorInfo.getStats());
        }
        
        @ParameterizedTest
        @EnumSource(ColorSchema.ColorValue.class)
        @DisplayName("Should create with all color values")
        void shouldCreateWithAllColorValues(ColorSchema.ColorValue colorValue) {
            ColorInfo info = new ColorInfo(colorValue);
            assertEquals(colorValue, info.getColorValue());
            assertNotNull(info.getStats());
        }
    }
    
    @Nested
    @DisplayName("Statistics Management")
    class StatisticsTests {
        
        @Test
        @DisplayName("Should set all statistics")
        void shouldSetAllStatistics() {
            colorInfo.setAll(10.0, 200.0, 105.0, 45.5);
            
            assertEquals(10.0, colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
            assertEquals(200.0, colorInfo.getStat(ColorInfo.ColorStat.MAX), 0.001);
            assertEquals(105.0, colorInfo.getStat(ColorInfo.ColorStat.MEAN), 0.001);
            assertEquals(45.5, colorInfo.getStat(ColorInfo.ColorStat.STDDEV), 0.001);
        }
        
        @Test
        @DisplayName("Should retrieve individual statistics")
        void shouldRetrieveIndividualStatistics() {
            colorInfo.setAll(0.0, 255.0, 127.5, 30.0);
            
            assertEquals(0.0, colorInfo.getStat(ColorInfo.ColorStat.MIN));
            assertEquals(255.0, colorInfo.getStat(ColorInfo.ColorStat.MAX));
            assertEquals(127.5, colorInfo.getStat(ColorInfo.ColorStat.MEAN));
            assertEquals(30.0, colorInfo.getStat(ColorInfo.ColorStat.STDDEV));
        }
        
        @Test
        @DisplayName("Should update individual statistics")
        void shouldUpdateIndividualStatistics() {
            colorInfo.getStats().put(ColorInfo.ColorStat.MIN, 50.0);
            colorInfo.getStats().put(ColorInfo.ColorStat.MAX, 200.0);
            
            assertEquals(50.0, colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
            assertEquals(200.0, colorInfo.getStat(ColorInfo.ColorStat.MAX), 0.001);
        }
        
        @ParameterizedTest
        @CsvSource({
            "0.0, 255.0, 127.5, 30.0",
            "10.0, 100.0, 55.0, 15.5",
            "-10.0, 10.0, 0.0, 5.0"
        })
        @DisplayName("Should handle various statistic values")
        void shouldHandleVariousValues(double min, double max, double mean, double stddev) {
            colorInfo.setAll(min, max, mean, stddev);
            
            assertEquals(min, colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
            assertEquals(max, colorInfo.getStat(ColorInfo.ColorStat.MAX), 0.001);
            assertEquals(mean, colorInfo.getStat(ColorInfo.ColorStat.MEAN), 0.001);
            assertEquals(stddev, colorInfo.getStat(ColorInfo.ColorStat.STDDEV), 0.001);
        }
    }
    
    @Nested
    @DisplayName("Color Channel Specific Tests")
    class ColorChannelTests {
        
        @Test
        @DisplayName("BGR channels should handle 0-255 range")
        void shouldHandleBGRRange() {
            ColorInfo blue = new ColorInfo(ColorSchema.ColorValue.BLUE);
            blue.setAll(0.0, 255.0, 128.0, 50.0);
            
            assertTrue(blue.getStat(ColorInfo.ColorStat.MIN) >= 0);
            assertTrue(blue.getStat(ColorInfo.ColorStat.MAX) <= 255);
        }
        
        @Test
        @DisplayName("HSV Hue should handle 0-179 range")
        void shouldHandleHueRange() {
            ColorInfo hue = new ColorInfo(ColorSchema.ColorValue.HUE);
            hue.setAll(0.0, 179.0, 90.0, 30.0);
            
            assertTrue(hue.getStat(ColorInfo.ColorStat.MAX) <= 179); // OpenCV HSV convention
        }
        
        @Test
        @DisplayName("Saturation/Value should handle 0-255 range")
        void shouldHandleSaturationValueRange() {
            ColorInfo saturation = new ColorInfo(ColorSchema.ColorValue.SATURATION);
            saturation.setAll(0.0, 255.0, 200.0, 20.0);
            
            ColorInfo value = new ColorInfo(ColorSchema.ColorValue.VALUE);
            value.setAll(0.0, 255.0, 180.0, 25.0);
            
            assertTrue(saturation.getStat(ColorInfo.ColorStat.MAX) <= 255);
            assertTrue(value.getStat(ColorInfo.ColorStat.MAX) <= 255);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle uniform color (min == max)")
        void shouldHandleUniformColor() {
            colorInfo.setAll(128.0, 128.0, 128.0, 0.0);
            
            assertEquals(colorInfo.getStat(ColorInfo.ColorStat.MIN), colorInfo.getStat(ColorInfo.ColorStat.MAX));
            assertEquals(0.0, colorInfo.getStat(ColorInfo.ColorStat.STDDEV), 0.001);
        }
        
        @Test
        @DisplayName("Should handle zero values")
        void shouldHandleZeroValues() {
            colorInfo.setAll(0.0, 0.0, 0.0, 0.0);
            
            assertEquals(0.0, colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
            assertEquals(0.0, colorInfo.getStat(ColorInfo.ColorStat.MAX), 0.001);
            assertEquals(0.0, colorInfo.getStat(ColorInfo.ColorStat.MEAN), 0.001);
            assertEquals(0.0, colorInfo.getStat(ColorInfo.ColorStat.STDDEV), 0.001);
        }
        
        @Test
        @DisplayName("Should handle negative values")
        void shouldHandleNegativeValues() {
            colorInfo.setAll(-50.0, -10.0, -30.0, 10.0);
            
            assertEquals(-50.0, colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
            assertEquals(-10.0, colorInfo.getStat(ColorInfo.ColorStat.MAX), 0.001);
            assertEquals(-30.0, colorInfo.getStat(ColorInfo.ColorStat.MEAN), 0.001);
        }
    }
    
    @Nested
    @DisplayName("Output and Display")
    class OutputTests {
        
        @Test
        @DisplayName("Should print formatted output without exception")
        void shouldPrintFormattedOutput() {
            colorInfo.setAll(50.0, 150.0, 100.0, 25.0);
            assertDoesNotThrow(() -> colorInfo.print());
        }
        
        @Test
        @DisplayName("Should handle print with extreme values")
        void shouldPrintExtremeValues() {
            colorInfo.setAll(Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 1000.0);
            assertDoesNotThrow(() -> colorInfo.print());
        }
    }
    
    @Nested
    @DisplayName("Use Cases")
    class UseCases {
        
        @Test
        @DisplayName("Should create red color profile")
        void shouldCreateRedProfile() {
            ColorInfo red = new ColorInfo(ColorSchema.ColorValue.RED);
            red.setAll(200.0, 255.0, 230.0, 15.0);
            
            assertTrue(red.getStat(ColorInfo.ColorStat.MIN) >= 200);
            assertTrue(red.getStat(ColorInfo.ColorStat.MEAN) > 200);
        }
        
        @Test
        @DisplayName("Should create grayscale profile")
        void shouldCreateGrayscaleProfile() {
            ColorInfo gray = new ColorInfo(ColorSchema.ColorValue.BLUE);
            gray.setAll(120.0, 130.0, 125.0, 3.0);
            
            double range = gray.getStat(ColorInfo.ColorStat.MAX) - gray.getStat(ColorInfo.ColorStat.MIN);
            assertTrue(range <= 10); // Small range for grayscale
            assertTrue(gray.getStat(ColorInfo.ColorStat.STDDEV) < 5); // Low variation
        }
    }
}