package io.github.jspinak.brobot.model.analysis.color;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ColorInfo.
 * Tests color information storage and manipulation.
 */
@DisplayName("ColorInfo Tests")
public class ColorInfoTest extends BrobotTestBase {
    
    private ColorInfo colorInfo;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        colorInfo = new ColorInfo();
    }
    
    @Nested
    @DisplayName("RGB Color Values")
    class RGBColorValues {
        
        @Test
        @DisplayName("Should store RGB values")
        void shouldStoreRGBValues() {
            colorInfo.setRed(255);
            colorInfo.setGreen(128);
            colorInfo.setBlue(64);
            
            assertEquals(255, colorInfo.getRed());
            assertEquals(128, colorInfo.getGreen());
            assertEquals(64, colorInfo.getBlue());
        }
        
        @ParameterizedTest
        @CsvSource({
            "0, 0, 0",      // Black
            "255, 255, 255", // White
            "255, 0, 0",    // Red
            "0, 255, 0",    // Green
            "0, 0, 255",    // Blue
            "128, 128, 128" // Gray
        })
        @DisplayName("Should handle standard colors")
        void shouldHandleStandardColors(int r, int g, int b) {
            colorInfo.setRed(r);
            colorInfo.setGreen(g);
            colorInfo.setBlue(b);
            
            assertEquals(r, colorInfo.getRed());
            assertEquals(g, colorInfo.getGreen());
            assertEquals(b, colorInfo.getBlue());
        }
        
        @Test
        @DisplayName("Should clamp values to valid range")
        void shouldClampValuesToValidRange() {
            colorInfo.setRed(300);
            colorInfo.setGreen(-50);
            colorInfo.setBlue(128);
            
            assertTrue(colorInfo.getRed() <= 255);
            assertTrue(colorInfo.getGreen() >= 0);
            assertEquals(128, colorInfo.getBlue());
        }
    }
    
    @Nested
    @DisplayName("HSV Color Values")
    class HSVColorValues {
        
        @Test
        @DisplayName("Should store HSV values")
        void shouldStoreHSVValues() {
            colorInfo.setHue(180.0);
            colorInfo.setSaturation(0.5);
            colorInfo.setValue(0.75);
            
            assertEquals(180.0, colorInfo.getHue(), 0.001);
            assertEquals(0.5, colorInfo.getSaturation(), 0.001);
            assertEquals(0.75, colorInfo.getValue(), 0.001);
        }
        
        @ParameterizedTest
        @CsvSource({
            "0.0, 0.0, 0.0",    // Black
            "0.0, 0.0, 1.0",    // White
            "0.0, 1.0, 1.0",    // Red
            "120.0, 1.0, 1.0",  // Green
            "240.0, 1.0, 1.0",  // Blue
            "60.0, 1.0, 1.0"    // Yellow
        })
        @DisplayName("Should handle standard HSV colors")
        void shouldHandleStandardHSVColors(double h, double s, double v) {
            colorInfo.setHue(h);
            colorInfo.setSaturation(s);
            colorInfo.setValue(v);
            
            assertEquals(h, colorInfo.getHue(), 0.001);
            assertEquals(s, colorInfo.getSaturation(), 0.001);
            assertEquals(v, colorInfo.getValue(), 0.001);
        }
        
        @Test
        @DisplayName("Should validate HSV ranges")
        void shouldValidateHSVRanges() {
            // Hue wraps around at 360
            colorInfo.setHue(370.0);
            assertTrue(colorInfo.getHue() >= 0 && colorInfo.getHue() < 360);
            
            // Saturation and Value clamp to [0, 1]
            colorInfo.setSaturation(1.5);
            colorInfo.setValue(-0.5);
            
            assertTrue(colorInfo.getSaturation() >= 0 && colorInfo.getSaturation() <= 1);
            assertTrue(colorInfo.getValue() >= 0 && colorInfo.getValue() <= 1);
        }
    }
    
    @Nested
    @DisplayName("Color Statistics")
    class ColorStatistics {
        
        @Test
        @DisplayName("Should track color frequency")
        void shouldTrackColorFrequency() {
            colorInfo.setFrequency(100);
            assertEquals(100, colorInfo.getFrequency());
            
            colorInfo.incrementFrequency();
            assertEquals(101, colorInfo.getFrequency());
            
            colorInfo.incrementFrequency(10);
            assertEquals(111, colorInfo.getFrequency());
        }
        
        @Test
        @DisplayName("Should calculate percentage")
        void shouldCalculatePercentage() {
            colorInfo.setFrequency(50);
            colorInfo.setTotalPixels(200);
            
            assertEquals(25.0, colorInfo.getPercentage(), 0.001);
        }
        
        @Test
        @DisplayName("Should handle zero total pixels")
        void shouldHandleZeroTotalPixels() {
            colorInfo.setFrequency(50);
            colorInfo.setTotalPixels(0);
            
            // Should not throw divide by zero
            double percentage = colorInfo.getPercentage();
            assertTrue(Double.isNaN(percentage) || percentage == 0.0);
        }
    }
    
    @Nested
    @DisplayName("Color Distance and Similarity")
    class ColorDistanceAndSimilarity {
        
        @Test
        @DisplayName("Should calculate RGB distance")
        void shouldCalculateRGBDistance() {
            colorInfo.setRed(100);
            colorInfo.setGreen(150);
            colorInfo.setBlue(200);
            
            ColorInfo other = new ColorInfo();
            other.setRed(110);
            other.setGreen(140);
            other.setBlue(190);
            
            double distance = colorInfo.rgbDistanceTo(other);
            assertTrue(distance > 0);
            assertTrue(distance < 50); // Small difference
        }
        
        @Test
        @DisplayName("Should calculate HSV distance")
        void shouldCalculateHSVDistance() {
            colorInfo.setHue(120.0);
            colorInfo.setSaturation(0.5);
            colorInfo.setValue(0.7);
            
            ColorInfo other = new ColorInfo();
            other.setHue(130.0);
            other.setSaturation(0.6);
            other.setValue(0.8);
            
            double distance = colorInfo.hsvDistanceTo(other);
            assertTrue(distance > 0);
        }
        
        @Test
        @DisplayName("Should identify similar colors")
        void shouldIdentifySimilarColors() {
            colorInfo.setRed(100);
            colorInfo.setGreen(100);
            colorInfo.setBlue(100);
            
            ColorInfo similar = new ColorInfo();
            similar.setRed(105);
            similar.setGreen(95);
            similar.setBlue(102);
            
            ColorInfo different = new ColorInfo();
            different.setRed(255);
            different.setGreen(0);
            different.setBlue(0);
            
            assertTrue(colorInfo.isSimilarTo(similar, 10));
            assertFalse(colorInfo.isSimilarTo(different, 10));
        }
    }
    
    @Nested
    @DisplayName("Color Name and Description")
    class ColorNameAndDescription {
        
        @Test
        @DisplayName("Should set color name")
        void shouldSetColorName() {
            colorInfo.setName("Sky Blue");
            assertEquals("Sky Blue", colorInfo.getName());
        }
        
        @Test
        @DisplayName("Should generate color description")
        void shouldGenerateColorDescription() {
            colorInfo.setRed(255);
            colorInfo.setGreen(0);
            colorInfo.setBlue(0);
            colorInfo.setName("Red");
            
            String description = colorInfo.getDescription();
            assertNotNull(description);
            assertTrue(description.contains("Red") || description.contains("255"));
        }
        
        @Test
        @DisplayName("Should provide hex representation")
        void shouldProvideHexRepresentation() {
            colorInfo.setRed(255);
            colorInfo.setGreen(128);
            colorInfo.setBlue(0);
            
            String hex = colorInfo.toHex();
            assertEquals("#FF8000", hex.toUpperCase());
        }
    }
    
    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {
        
        @Test
        @DisplayName("Should build with RGB values")
        void shouldBuildWithRGBValues() {
            ColorInfo color = ColorInfo.builder()
                .red(128)
                .green(64)
                .blue(192)
                .build();
            
            assertEquals(128, color.getRed());
            assertEquals(64, color.getGreen());
            assertEquals(192, color.getBlue());
        }
        
        @Test
        @DisplayName("Should build with HSV values")
        void shouldBuildWithHSVValues() {
            ColorInfo color = ColorInfo.builder()
                .hue(240.0)
                .saturation(0.75)
                .value(0.5)
                .build();
            
            assertEquals(240.0, color.getHue(), 0.001);
            assertEquals(0.75, color.getSaturation(), 0.001);
            assertEquals(0.5, color.getValue(), 0.001);
        }
        
        @Test
        @DisplayName("Should build complete color info")
        void shouldBuildCompleteColorInfo() {
            ColorInfo color = ColorInfo.builder()
                .red(200)
                .green(100)
                .blue(50)
                .hue(20.0)
                .saturation(0.75)
                .value(0.78)
                .name("Orange")
                .frequency(150)
                .build();
            
            assertEquals(200, color.getRed());
            assertEquals("Orange", color.getName());
            assertEquals(150, color.getFrequency());
        }
    }
    
    @Nested
    @DisplayName("Equality and Comparison")
    class EqualityAndComparison {
        
        @Test
        @DisplayName("Should identify equal colors")
        void shouldIdentifyEqualColors() {
            colorInfo.setRed(100);
            colorInfo.setGreen(150);
            colorInfo.setBlue(200);
            
            ColorInfo other = new ColorInfo();
            other.setRed(100);
            other.setGreen(150);
            other.setBlue(200);
            
            assertEquals(colorInfo, other);
        }
        
        @Test
        @DisplayName("Should compare by frequency")
        void shouldCompareByFrequency() {
            colorInfo.setFrequency(100);
            
            ColorInfo moreFrequent = new ColorInfo();
            moreFrequent.setFrequency(200);
            
            ColorInfo lessFrequent = new ColorInfo();
            lessFrequent.setFrequency(50);
            
            assertTrue(colorInfo.compareTo(moreFrequent) < 0);
            assertTrue(colorInfo.compareTo(lessFrequent) > 0);
        }
        
        @Test
        @DisplayName("Should generate consistent hash code")
        void shouldGenerateConsistentHashCode() {
            colorInfo.setRed(100);
            colorInfo.setGreen(150);
            colorInfo.setBlue(200);
            
            ColorInfo other = new ColorInfo();
            other.setRed(100);
            other.setGreen(150);
            other.setBlue(200);
            
            assertEquals(colorInfo.hashCode(), other.hashCode());
        }
    }
}