package io.github.jspinak.brobot.util.image.core;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified test suite for BufferedImageUtilities that works with Brobot's mock mode.
 * Extends BrobotTestBase for proper mock configuration.
 */
@DisplayName("BufferedImageUtilities Simple Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
public class BufferedImageUtilitiesSimpleTest extends BrobotTestBase {
    
    private BufferedImageUtilities imageUtils;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // Enables mock mode via BrobotTestBase
        imageUtils = new BufferedImageUtilities();
    }
    
    @Nested
    @DisplayName("Base64 Conversions")
    class Base64Conversions {
        
        @Test
        @DisplayName("Convert image to Base64 and back")
        public void testImageToBase64AndBack() throws IOException {
            // Create a simple test image
            BufferedImage originalImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = originalImage.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 100, 100);
            g.dispose();
            
            // Convert to Base64
            String base64 = BufferedImageUtilities.bufferedImageToStringBase64(originalImage);
            assertNotNull(base64);
            assertFalse(base64.isEmpty());
            
            // Convert back to image
            BufferedImage decodedImage = BufferedImageUtilities.base64StringToImage(base64);
            assertNotNull(decodedImage);
            assertEquals(100, decodedImage.getWidth());
            assertEquals(100, decodedImage.getHeight());
        }
        
        @Test
        @DisplayName("Convert null image returns null")
        public void testNullImageToBase64() {
            String result = BufferedImageUtilities.bufferedImageToStringBase64(null);
            assertNull(result);
        }
        
        @Test
        @DisplayName("Convert valid Base64 to byte array")
        public void testBase64ToByteArray() throws IOException {
            // Create a simple image
            BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            String base64 = BufferedImageUtilities.bufferedImageToStringBase64(image);
            
            // Convert to byte array
            byte[] bytes = BufferedImageUtilities.base64StringToByteArray(base64);
            assertNotNull(bytes);
            assertTrue(bytes.length > 0);
        }
        
        @Test
        @DisplayName("Handle empty Base64 string")
        public void testEmptyBase64String() {
            byte[] result = BufferedImageUtilities.base64StringToByteArray("");
            assertNull(result);
        }
        
        @Test
        @DisplayName("Handle null Base64 string")
        public void testNullBase64String() {
            byte[] result = BufferedImageUtilities.base64StringToByteArray(null);
            assertNull(result);
        }
        
        @Test
        @DisplayName("Handle invalid Base64 string")
        public void testInvalidBase64String() {
            // Test with invalid base64 string
            BufferedImage result = BufferedImageUtilities.base64StringToImage("not-a-valid-base64!");
            assertNull(result, "Should return null for invalid base64");
            
            // Test with partially valid base64 that doesn't decode to an image
            result = BufferedImageUtilities.base64StringToImage("YWJjZGVmZw=="); // "abcdefg" in base64
            assertNull(result, "Should return null when base64 doesn't decode to valid image");
        }
    }
    
    @Nested
    @DisplayName("Byte Array Operations")
    class ByteArrayOperations {
        
        @Test
        @DisplayName("Convert image to byte array")
        public void testImageToByteArray() {
            BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            byte[] bytes = BufferedImageUtilities.toByteArray(image);
            
            assertNotNull(bytes);
            assertTrue(bytes.length > 0);
        }
        
        @Test
        @DisplayName("Convert null image to byte array")
        public void testNullImageToByteArray() {
            byte[] bytes = BufferedImageUtilities.toByteArray(null);
            assertNull(bytes);
        }
        
        @Test
        @DisplayName("Convert byte array to image")
        public void testByteArrayToImage() throws IOException {
            // Create a test image
            BufferedImage original = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = original.createGraphics();
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, 50, 50);
            g.dispose();
            
            // Convert to bytes
            byte[] bytes = BufferedImageUtilities.toByteArray(original);
            
            // Convert back to image
            BufferedImage decoded = BufferedImageUtilities.fromByteArray(bytes);
            assertNotNull(decoded);
            assertEquals(50, decoded.getWidth());
            assertEquals(50, decoded.getHeight());
        }
        
        @Test
        @DisplayName("Handle null byte array")
        public void testNullByteArray() {
            BufferedImage result = BufferedImageUtilities.fromByteArray(null);
            assertNull(result);
        }
        
        @Test
        @DisplayName("Handle empty byte array")
        public void testEmptyByteArray() {
            BufferedImage result = BufferedImageUtilities.fromByteArray(new byte[0]);
            assertNull(result);
        }
    }
    
    @Nested
    @DisplayName("Sub-Image Operations")
    class SubImageOperations {
        
        @Test
        @DisplayName("Get sub-image from BufferedImage")
        public void testGetSubImage() {
            // Create a test image
            BufferedImage original = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = original.createGraphics();
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, 200, 200);
            g.setColor(Color.RED);
            g.fillRect(50, 50, 100, 100);
            g.dispose();
            
            // Get sub-image
            BufferedImage subImage = BufferedImageUtilities.getSubImage(original, 50, 50, 100, 100);
            
            assertNotNull(subImage);
            assertEquals(100, subImage.getWidth());
            assertEquals(100, subImage.getHeight());
        }
        
        @Test
        @DisplayName("Get sub-image with adjusted bounds")
        public void testGetSubImageAdjustedBounds() {
            BufferedImage original = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            
            // Try to get sub-image that extends outside bounds
            // The method should adjust the bounds to fit
            BufferedImage subImage = BufferedImageUtilities.getSubImage(original, 50, 50, 100, 100);
            
            assertNotNull(subImage);
            // Bounds should be adjusted to fit within the original
            assertEquals(50, subImage.getWidth());  // 100 - 50 = 50
            assertEquals(50, subImage.getHeight()); // 100 - 50 = 50
        }
    }
    
    @Nested
    @DisplayName("Image Type Conversions")
    class ImageTypeConversions {
        
        @Test
        @DisplayName("Convert to 3-byte BGR type")
        public void testConvertTo3ByteBGR() {
            BufferedImage original = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            BufferedImageUtilities utils = new BufferedImageUtilities();
            BufferedImage converted = utils.convertTo3ByteBGRType(original);
            
            assertNotNull(converted);
            assertEquals(BufferedImage.TYPE_3BYTE_BGR, converted.getType());
            assertEquals(100, converted.getWidth());
            assertEquals(100, converted.getHeight());
        }
        
        @Test
        @DisplayName("Convert already BGR image")
        public void testConvertAlreadyBGR() {
            BufferedImage original = new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR);
            BufferedImageUtilities utils = new BufferedImageUtilities();
            BufferedImage converted = utils.convertTo3ByteBGRType(original);
            
            // The method always creates a new image, so they won't be the same object
            assertNotSame(original, converted);
            // But they should have the same type and dimensions
            assertEquals(BufferedImage.TYPE_3BYTE_BGR, converted.getType());
            assertEquals(100, converted.getWidth());
            assertEquals(100, converted.getHeight());
        }
    }
}