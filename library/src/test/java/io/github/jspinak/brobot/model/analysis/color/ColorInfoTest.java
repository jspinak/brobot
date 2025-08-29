package io.github.jspinak.brobot.model.analysis.color;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ColorInfo.
 * Tests statistical color channel information for pattern matching.
 */
@DisplayName("ColorInfo Tests")
public class ColorInfoTest extends BrobotTestBase {
    
    private ColorInfo colorInfo;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Initialize with a specific color value
        colorInfo = new ColorInfo(ColorSchema.ColorValue.HUE);
    }
    
    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorAndInitialization {
        
        @Test
        @DisplayName("Should initialize with specified color value")
        void shouldInitializeWithColorValue() {
            ColorInfo info = new ColorInfo(ColorSchema.ColorValue.BLUE);
            assertEquals(ColorSchema.ColorValue.BLUE, info.getColorValue());
            assertNotNull(info.getStats());
            assertTrue(info.getStats().isEmpty());
        }
        
        @ParameterizedTest
        @EnumSource(ColorSchema.ColorValue.class)
        @DisplayName("Should accept all color value types")
        void shouldAcceptAllColorValueTypes(ColorSchema.ColorValue colorValue) {
            ColorInfo info = new ColorInfo(colorValue);
            assertEquals(colorValue, info.getColorValue());
        }
    }
    
    @Nested
    @DisplayName("Statistical Values")
    class StatisticalValues {
        
        @Test
        @DisplayName("Should set all statistical values")
        void shouldSetAllStatisticalValues() {
            colorInfo.setAll(10.0, 250.0, 128.5, 45.3);
            
            assertEquals(10.0, colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
            assertEquals(250.0, colorInfo.getStat(ColorInfo.ColorStat.MAX), 0.001);
            assertEquals(128.5, colorInfo.getStat(ColorInfo.ColorStat.MEAN), 0.001);
            assertEquals(45.3, colorInfo.getStat(ColorInfo.ColorStat.STDDEV), 0.001);
        }
        
        @Test
        @DisplayName("Should get individual statistics")
        void shouldGetIndividualStatistics() {
            Map<ColorInfo.ColorStat, Double> stats = colorInfo.getStats();
            stats.put(ColorInfo.ColorStat.MIN, 5.0);
            stats.put(ColorInfo.ColorStat.MAX, 255.0);
            
            assertEquals(5.0, colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
            assertEquals(255.0, colorInfo.getStat(ColorInfo.ColorStat.MAX), 0.001);
        }
        
        @ParameterizedTest
        @CsvSource({
            "0.0, 255.0, 127.5, 30.0",     // Full range
            "100.0, 200.0, 150.0, 10.0",   // Mid-range
            "0.0, 0.0, 0.0, 0.0",           // All zeros
            "255.0, 255.0, 255.0, 0.0",    // Max values, no deviation
            "50.5, 150.5, 100.5, 25.25"    // Decimal values
        })
        @DisplayName("Should handle various statistical ranges")
        void shouldHandleVariousStatisticalRanges(double min, double max, double mean, double stddev) {
            colorInfo.setAll(min, max, mean, stddev);
            
            assertEquals(min, colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
            assertEquals(max, colorInfo.getStat(ColorInfo.ColorStat.MAX), 0.001);
            assertEquals(mean, colorInfo.getStat(ColorInfo.ColorStat.MEAN), 0.001);
            assertEquals(stddev, colorInfo.getStat(ColorInfo.ColorStat.STDDEV), 0.001);
        }
        
        @Test
        @DisplayName("Should handle negative values")
        void shouldHandleNegativeValues() {
            // Though color values are typically positive, stats might include negative values
            colorInfo.setAll(-10.0, 260.0, 125.0, 50.0);
            
            assertEquals(-10.0, colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
            assertEquals(260.0, colorInfo.getStat(ColorInfo.ColorStat.MAX), 0.001);
        }
    }
    
    @Nested
    @DisplayName("Color Value Types")
    class ColorValueTypes {
        
        @Test
        @DisplayName("Should handle BGR color channels")
        void shouldHandleBGRColorChannels() {
            ColorInfo blue = new ColorInfo(ColorSchema.ColorValue.BLUE);
            ColorInfo green = new ColorInfo(ColorSchema.ColorValue.GREEN);
            ColorInfo red = new ColorInfo(ColorSchema.ColorValue.RED);
            
            // BGR values range from 0-255
            blue.setAll(0, 255, 128, 30);
            green.setAll(0, 255, 100, 25);
            red.setAll(0, 255, 150, 40);
            
            assertEquals(ColorSchema.ColorValue.BLUE, blue.getColorValue());
            assertEquals(ColorSchema.ColorValue.GREEN, green.getColorValue());
            assertEquals(ColorSchema.ColorValue.RED, red.getColorValue());
        }
        
        @Test
        @DisplayName("Should handle HSV color channels")
        void shouldHandleHSVColorChannels() {
            ColorInfo hue = new ColorInfo(ColorSchema.ColorValue.HUE);
            ColorInfo saturation = new ColorInfo(ColorSchema.ColorValue.SATURATION);
            ColorInfo value = new ColorInfo(ColorSchema.ColorValue.VALUE);
            
            // OpenCV HSV ranges: H(0-179), S(0-255), V(0-255)
            hue.setAll(0, 179, 90, 30);
            saturation.setAll(0, 255, 128, 40);
            value.setAll(0, 255, 200, 25);
            
            assertEquals(ColorSchema.ColorValue.HUE, hue.getColorValue());
            assertEquals(ColorSchema.ColorValue.SATURATION, saturation.getColorValue());
            assertEquals(ColorSchema.ColorValue.VALUE, value.getColorValue());
        }
    }
    
    @Nested
    @DisplayName("Statistical Patterns")
    class StatisticalPatterns {
        
        @Test
        @DisplayName("Should represent uniform color distribution")
        void shouldRepresentUniformColorDistribution() {
            // Uniform distribution has same min/max, zero stddev
            colorInfo.setAll(128.0, 128.0, 128.0, 0.0);
            
            assertEquals(colorInfo.getStat(ColorInfo.ColorStat.MIN), 
                        colorInfo.getStat(ColorInfo.ColorStat.MAX), 0.001);
            assertEquals(0.0, colorInfo.getStat(ColorInfo.ColorStat.STDDEV), 0.001);
        }
        
        @Test
        @DisplayName("Should represent high variance distribution")
        void shouldRepresentHighVarianceDistribution() {
            // High variance with large stddev
            colorInfo.setAll(0.0, 255.0, 127.5, 80.0);
            
            assertTrue(colorInfo.getStat(ColorInfo.ColorStat.STDDEV) > 50.0);
            assertEquals(255.0, 
                colorInfo.getStat(ColorInfo.ColorStat.MAX) - 
                colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
        }
        
        @Test
        @DisplayName("Should represent skewed distribution")
        void shouldRepresentSkewedDistribution() {
            // Skewed with mean closer to min than max
            colorInfo.setAll(10.0, 250.0, 50.0, 30.0);
            
            double range = colorInfo.getStat(ColorInfo.ColorStat.MAX) - 
                          colorInfo.getStat(ColorInfo.ColorStat.MIN);
            double meanOffset = colorInfo.getStat(ColorInfo.ColorStat.MEAN) - 
                               colorInfo.getStat(ColorInfo.ColorStat.MIN);
            
            assertTrue(meanOffset < range / 2); // Mean is in lower half
        }
    }
    
    @Nested
    @DisplayName("Setter and Getter Methods")
    class SetterAndGetterMethods {
        
        @Test
        @DisplayName("Should set and get color value")
        void shouldSetAndGetColorValue() {
            colorInfo.setColorValue(ColorSchema.ColorValue.SATURATION);
            assertEquals(ColorSchema.ColorValue.SATURATION, colorInfo.getColorValue());
            
            colorInfo.setColorValue(ColorSchema.ColorValue.GREEN);
            assertEquals(ColorSchema.ColorValue.GREEN, colorInfo.getColorValue());
        }
        
        @Test
        @DisplayName("Should set and get stats map")
        void shouldSetAndGetStatsMap() {
            Map<ColorInfo.ColorStat, Double> originalStats = colorInfo.getStats();
            assertNotNull(originalStats);
            
            // Modify stats directly
            originalStats.put(ColorInfo.ColorStat.MIN, 42.0);
            assertEquals(42.0, colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
        }
        
        @Test
        @DisplayName("Should handle missing statistics gracefully")
        void shouldHandleMissingStatisticsGracefully() {
            // Clear all stats
            colorInfo.getStats().clear();
            
            // Getting a missing stat should return null
            assertNull(colorInfo.getStats().get(ColorInfo.ColorStat.MEAN));
        }
    }
    
    @Nested
    @DisplayName("Use Cases for Pattern Matching")
    class PatternMatchingUseCases {
        
        @Test
        @DisplayName("Should store color profile for template matching")
        void shouldStoreColorProfileForTemplateMatching() {
            // Simulate a blue sky color profile
            ColorInfo skyBlue = new ColorInfo(ColorSchema.ColorValue.BLUE);
            skyBlue.setAll(200.0, 255.0, 235.0, 15.0); // High blue values, low variance
            
            assertTrue(skyBlue.getStat(ColorInfo.ColorStat.MIN) > 150);
            assertTrue(skyBlue.getStat(ColorInfo.ColorStat.STDDEV) < 20);
        }
        
        @Test
        @DisplayName("Should store tolerance ranges for color matching")
        void shouldStoreToleranceRangesForColorMatching() {
            // Use stddev to determine tolerance
            colorInfo.setAll(100.0, 200.0, 150.0, 25.0);
            
            double tolerance = colorInfo.getStat(ColorInfo.ColorStat.STDDEV) * 2;
            double lowerBound = colorInfo.getStat(ColorInfo.ColorStat.MEAN) - tolerance;
            double upperBound = colorInfo.getStat(ColorInfo.ColorStat.MEAN) + tolerance;
            
            assertTrue(lowerBound >= colorInfo.getStat(ColorInfo.ColorStat.MIN));
            assertTrue(upperBound <= colorInfo.getStat(ColorInfo.ColorStat.MAX));
        }
        
        @Test
        @DisplayName("Should support multi-channel color analysis")
        void shouldSupportMultiChannelColorAnalysis() {
            // Create profiles for each BGR channel
            ColorInfo blueChannel = new ColorInfo(ColorSchema.ColorValue.BLUE);
            ColorInfo greenChannel = new ColorInfo(ColorSchema.ColorValue.GREEN);
            ColorInfo redChannel = new ColorInfo(ColorSchema.ColorValue.RED);
            
            blueChannel.setAll(50.0, 150.0, 100.0, 20.0);
            greenChannel.setAll(100.0, 200.0, 150.0, 25.0);
            redChannel.setAll(150.0, 250.0, 200.0, 30.0);
            
            // Each channel has distinct statistics
            assertNotEquals(blueChannel.getStat(ColorInfo.ColorStat.MEAN),
                           greenChannel.getStat(ColorInfo.ColorStat.MEAN));
            assertNotEquals(greenChannel.getStat(ColorInfo.ColorStat.MEAN),
                           redChannel.getStat(ColorInfo.ColorStat.MEAN));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesAndBoundaries {
        
        @Test
        @DisplayName("Should handle extreme values")
        void shouldHandleExtremeValues() {
            colorInfo.setAll(Double.MIN_VALUE, Double.MAX_VALUE, 0.0, Double.MAX_VALUE);
            
            assertEquals(Double.MIN_VALUE, colorInfo.getStat(ColorInfo.ColorStat.MIN));
            assertEquals(Double.MAX_VALUE, colorInfo.getStat(ColorInfo.ColorStat.MAX));
        }
        
        @Test
        @DisplayName("Should handle NaN and Infinity")
        void shouldHandleNaNAndInfinity() {
            colorInfo.setAll(Double.NaN, Double.POSITIVE_INFINITY, 
                           Double.NEGATIVE_INFINITY, Double.NaN);
            
            assertTrue(Double.isNaN(colorInfo.getStat(ColorInfo.ColorStat.MIN)));
            assertTrue(Double.isInfinite(colorInfo.getStat(ColorInfo.ColorStat.MAX)));
        }
        
        @Test
        @DisplayName("Should maintain precision for small differences")
        void shouldMaintainPrecisionForSmallDifferences() {
            colorInfo.setAll(127.123456789, 127.123456790, 127.1234567895, 0.0000000005);
            
            double min = colorInfo.getStat(ColorInfo.ColorStat.MIN);
            double max = colorInfo.getStat(ColorInfo.ColorStat.MAX);
            
            assertNotEquals(min, max);
            assertEquals(0.000000001, max - min, 1e-10);
        }
    }
}