package io.github.jspinak.brobot.util.image.constants;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for BgrColorConstants.
 * Tests predefined BGR color constants for OpenCV operations.
 */
@DisplayName("BgrColorConstants Tests")
public class BgrColorConstantsTest extends BrobotTestBase {
    
    @Nested
    @DisplayName("Color Value Verification")
    class ColorValueVerification {
        
        @Test
        @DisplayName("BLUE has correct BGR values")
        public void testBlueColorValues() {
            Scalar blue = BgrColorConstants.BLUE.getScalar();
            
            assertNotNull(blue);
            // In BGR format: Blue=255, Green=0, Red=0, Alpha=255
            assertEquals(255.0, blue.get(0), 0.01); // Blue channel
            assertEquals(0.0, blue.get(1), 0.01);   // Green channel
            assertEquals(0.0, blue.get(2), 0.01);   // Red channel
            assertEquals(255.0, blue.get(3), 0.01); // Alpha channel
        }
        
        @Test
        @DisplayName("GREEN has correct BGR values")
        public void testGreenColorValues() {
            Scalar green = BgrColorConstants.GREEN.getScalar();
            
            assertNotNull(green);
            // In BGR format: Blue=0, Green=255, Red=0, Alpha=255
            assertEquals(0.0, green.get(0), 0.01);   // Blue channel
            assertEquals(255.0, green.get(1), 0.01); // Green channel
            assertEquals(0.0, green.get(2), 0.01);   // Red channel
            assertEquals(255.0, green.get(3), 0.01); // Alpha channel
        }
        
        @Test
        @DisplayName("RED has correct BGR values")
        public void testRedColorValues() {
            Scalar red = BgrColorConstants.RED.getScalar();
            
            assertNotNull(red);
            // In BGR format: Blue=0, Green=0, Red=255, Alpha=255
            assertEquals(0.0, red.get(0), 0.01);   // Blue channel
            assertEquals(0.0, red.get(1), 0.01);   // Green channel
            assertEquals(255.0, red.get(2), 0.01); // Red channel
            assertEquals(255.0, red.get(3), 0.01); // Alpha channel
        }
    }
    
    @Nested
    @DisplayName("Alpha Channel")
    class AlphaChannel {
        
        @Test
        @DisplayName("All colors have full opacity")
        public void testAllColorsHaveFullOpacity() {
            for (BgrColorConstants color : BgrColorConstants.values()) {
                Scalar scalar = color.getScalar();
                assertEquals(255.0, scalar.get(3), 0.01, 
                    color.name() + " should have full opacity (alpha=255)");
            }
        }
        
        @ParameterizedTest
        @EnumSource(BgrColorConstants.class)
        @DisplayName("Each color has alpha channel set")
        public void testEachColorHasAlpha(BgrColorConstants color) {
            Scalar scalar = color.getScalar();
            
            assertNotNull(scalar);
            // Alpha should be 255 (fully opaque)
            assertEquals(255.0, scalar.get(3), 0.01);
        }
    }
    
    @Nested
    @DisplayName("Scalar Properties")
    class ScalarProperties {
        
        @Test
        @DisplayName("Scalars are not null")
        public void testScalarsNotNull() {
            assertNotNull(BgrColorConstants.BLUE.getScalar());
            assertNotNull(BgrColorConstants.GREEN.getScalar());
            assertNotNull(BgrColorConstants.RED.getScalar());
        }
        
        @Test
        @DisplayName("Scalars have 4 channels")
        public void testScalarsHaveFourChannels() {
            // Scalar should have 4 values (B, G, R, A)
            for (BgrColorConstants color : BgrColorConstants.values()) {
                Scalar scalar = color.getScalar();
                
                // Test we can access all 4 channels
                assertDoesNotThrow(() -> {
                    scalar.get(0); // Blue
                    scalar.get(1); // Green
                    scalar.get(2); // Red
                    scalar.get(3); // Alpha
                });
            }
        }
        
        @Test
        @DisplayName("Scalar values are in valid range")
        public void testScalarValuesInRange() {
            for (BgrColorConstants color : BgrColorConstants.values()) {
                Scalar scalar = color.getScalar();
                
                for (int i = 0; i < 4; i++) {
                    double value = scalar.get(i);
                    assertTrue(value >= 0.0 && value <= 255.0,
                        String.format("%s channel %d should be in range [0, 255], but was %f", 
                            color.name(), i, value));
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Enum Operations")
    class EnumOperations {
        
        @Test
        @DisplayName("Can get all color constants")
        public void testGetAllColorConstants() {
            BgrColorConstants[] colors = BgrColorConstants.values();
            
            assertNotNull(colors);
            assertEquals(3, colors.length); // Currently 3 colors defined
            
            // Check all expected colors are present
            boolean hasBlue = false;
            boolean hasGreen = false;
            boolean hasRed = false;
            
            for (BgrColorConstants color : colors) {
                if (color == BgrColorConstants.BLUE) hasBlue = true;
                if (color == BgrColorConstants.GREEN) hasGreen = true;
                if (color == BgrColorConstants.RED) hasRed = true;
            }
            
            assertTrue(hasBlue, "Should have BLUE constant");
            assertTrue(hasGreen, "Should have GREEN constant");
            assertTrue(hasRed, "Should have RED constant");
        }
        
        @Test
        @DisplayName("valueOf returns correct constant")
        public void testValueOf() {
            assertEquals(BgrColorConstants.BLUE, BgrColorConstants.valueOf("BLUE"));
            assertEquals(BgrColorConstants.GREEN, BgrColorConstants.valueOf("GREEN"));
            assertEquals(BgrColorConstants.RED, BgrColorConstants.valueOf("RED"));
        }
        
        @Test
        @DisplayName("valueOf throws for invalid name")
        public void testValueOfInvalid() {
            assertThrows(IllegalArgumentException.class, () -> 
                BgrColorConstants.valueOf("INVALID_COLOR")
            );
        }
    }
    
    @Nested
    @DisplayName("Color Uniqueness")
    class ColorUniqueness {
        
        @Test
        @DisplayName("Each color has unique values")
        public void testColorsAreUnique() {
            Scalar blue = BgrColorConstants.BLUE.getScalar();
            Scalar green = BgrColorConstants.GREEN.getScalar();
            Scalar red = BgrColorConstants.RED.getScalar();
            
            // Blue should be different from Green
            assertNotEquals(blue.get(0), green.get(0));
            assertNotEquals(blue.get(1), green.get(1));
            
            // Blue should be different from Red
            assertNotEquals(blue.get(0), red.get(0));
            assertNotEquals(blue.get(2), red.get(2));
            
            // Green should be different from Red
            assertNotEquals(green.get(1), red.get(1));
            assertNotEquals(green.get(2), red.get(2));
        }
        
        @Test
        @DisplayName("Primary colors have single channel at maximum")
        public void testPrimaryColorChannels() {
            Scalar blue = BgrColorConstants.BLUE.getScalar();
            Scalar green = BgrColorConstants.GREEN.getScalar();
            Scalar red = BgrColorConstants.RED.getScalar();
            
            // Blue: only blue channel should be 255
            assertEquals(255.0, blue.get(0), 0.01);
            assertEquals(0.0, blue.get(1), 0.01);
            assertEquals(0.0, blue.get(2), 0.01);
            
            // Green: only green channel should be 255
            assertEquals(0.0, green.get(0), 0.01);
            assertEquals(255.0, green.get(1), 0.01);
            assertEquals(0.0, green.get(2), 0.01);
            
            // Red: only red channel should be 255
            assertEquals(0.0, red.get(0), 0.01);
            assertEquals(0.0, red.get(1), 0.01);
            assertEquals(255.0, red.get(2), 0.01);
        }
    }
    
    @Nested
    @DisplayName("Use Cases")
    class UseCases {
        
        @Test
        @DisplayName("Colors suitable for OpenCV drawing")
        public void testColorsForDrawing() {
            // Verify colors are in correct format for OpenCV drawing functions
            for (BgrColorConstants color : BgrColorConstants.values()) {
                Scalar scalar = color.getScalar();
                
                // All values should be non-negative for drawing
                assertTrue(scalar.get(0) >= 0);
                assertTrue(scalar.get(1) >= 0);
                assertTrue(scalar.get(2) >= 0);
                assertTrue(scalar.get(3) >= 0);
                
                // All values should be <= 255
                assertTrue(scalar.get(0) <= 255);
                assertTrue(scalar.get(1) <= 255);
                assertTrue(scalar.get(2) <= 255);
                assertTrue(scalar.get(3) <= 255);
            }
        }
        
        @Test
        @DisplayName("Can be used in loops")
        public void testIterateOverColors() {
            int colorCount = 0;
            
            for (BgrColorConstants color : BgrColorConstants.values()) {
                assertNotNull(color);
                assertNotNull(color.getScalar());
                colorCount++;
            }
            
            assertEquals(3, colorCount);
        }
        
        @Test
        @DisplayName("Scalar objects are reusable")
        public void testScalarReusability() {
            // Get the same scalar multiple times
            Scalar red1 = BgrColorConstants.RED.getScalar();
            Scalar red2 = BgrColorConstants.RED.getScalar();
            
            // Should return the same Scalar object (not create new ones)
            assertSame(red1, red2, "Should return the same Scalar instance");
        }
    }
}