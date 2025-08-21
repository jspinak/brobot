package io.github.jspinak.brobot.util.image.core;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sikuli.script.Image;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ImageConverter utilities.
 * Tests image format conversions and operations in mock mode.
 */
public class ImageConverterTest extends BrobotTestBase {

    private BufferedImage testImage;
    private Image sikuliImage;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Create a test image programmatically
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 50, 50);
        g.setColor(Color.BLUE);
        g.fillRect(50, 50, 50, 50);
        g.dispose();
        
        sikuliImage = new Image(testImage);
    }
    
    @Test
    void shouldConvertImageToByteArray() {
        // Test conversion from Image to byte array
        byte[] result = ImageConverter.getBytes(sikuliImage);
        
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
    
    @Test
    void shouldHandleNullImageToByteArray() {
        // Should throw exception or handle gracefully
        assertThrows(NullPointerException.class, () -> 
            ImageConverter.getBytes(null)
        );
    }
    
    @Test
    void shouldConvertByteArrayToImage() {
        // First convert to byte array
        byte[] byteArray = ImageConverter.getBytes(sikuliImage);
        
        // Then convert back
        Image result = ImageConverter.getImage(byteArray);
        
        assertNotNull(result);
        assertNotNull(result.get());
    }
    
    @Test
    void shouldHandleEmptyByteArrayToImage() {
        // Empty byte array should throw exception or return null
        assertThrows(Exception.class, () -> 
            ImageConverter.getImage(new byte[0])
        );
    }
    
    @Test
    void shouldHandleNullByteArrayToImage() {
        assertThrows(Exception.class, () -> 
            ImageConverter.getImage((byte[])null)
        );
    }
    
    @Test
    void shouldConvertMatToImage() {
        // Create a test Mat
        Mat mat = new Mat(100, 100, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        
        Image result = ImageConverter.getImage(mat);
        
        assertNotNull(result);
        assertNotNull(result.get());
    }
    
    @Test
    void shouldHandleNullMatToImage() {
        assertThrows(Exception.class, () -> 
            ImageConverter.getImage((Mat)null)
        );
    }
    
    @Test
    void shouldConvertImageToBgrMat() {
        Mat result = ImageConverter.getMatBGR(sikuliImage);
        
        assertNotNull(result);
        assertFalse(result.empty());
        // ImageConverter may return BGRA (4 channels) depending on source image
        assertTrue(result.channels() == 3 || result.channels() == 4);
    }
    
    @Test
    void shouldHandleNullImageToBgrMat() {
        assertThrows(NullPointerException.class, () -> 
            ImageConverter.getMatBGR((Image)null)
        );
    }
    
    @Test
    void shouldConvertBufferedImageToBgrMat() {
        Mat result = ImageConverter.getMatBGR(testImage);
        
        assertNotNull(result);
        assertFalse(result.empty());
        // ImageConverter may return BGRA (4 channels) depending on source image
        assertTrue(result.channels() == 3 || result.channels() == 4);
        assertEquals(100, result.rows());
        assertEquals(100, result.cols());
    }
    
    @Test
    void shouldHandleNullBufferedImageToBgrMat() {
        Mat result = ImageConverter.getMatBGR((BufferedImage)null);
        
        assertNotNull(result);
        assertTrue(result.empty());
    }
    
    @Test
    void shouldConvertImageToHsvMat() {
        Mat result = ImageConverter.getMatHSV(sikuliImage);
        
        assertNotNull(result);
        assertFalse(result.empty());
        assertEquals(3, result.channels()); // HSV has 3 channels
    }
    
    @Test
    void shouldHandleNullImageToHsvMat() {
        assertThrows(NullPointerException.class, () -> 
            ImageConverter.getMatHSV((Image)null)
        );
    }
    
    @Test
    void shouldConvertBufferedImageToHsvMat() {
        Mat result = ImageConverter.getMatHSV(testImage);
        
        assertNotNull(result);
        assertFalse(result.empty());
        assertEquals(3, result.channels()); // HSV has 3 channels
        assertEquals(100, result.rows());
        assertEquals(100, result.cols());
    }
    
    @Test
    void shouldHandleNullBufferedImageToHsvMat() {
        Mat result = ImageConverter.getMatHSV((BufferedImage)null);
        
        assertNotNull(result);
        assertTrue(result.empty());
    }
    
    @Test
    void shouldHandleRoundTripConversion() {
        // Test round-trip: Image -> byte[] -> Image
        byte[] byteArray = ImageConverter.getBytes(sikuliImage);
        Image convertedBack = ImageConverter.getImage(byteArray);
        
        assertNotNull(convertedBack);
        assertNotNull(convertedBack.get()); // Get BufferedImage
    }
    
    @Test
    void shouldHandleEmptyImage() {
        // Create an empty image
        BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Image emptySkImage = new Image(emptyImage);
        
        // Test various conversions with empty image
        byte[] byteArray = ImageConverter.getBytes(emptySkImage);
        assertNotNull(byteArray);
        assertTrue(byteArray.length > 0);
        
        Mat bgrMat = ImageConverter.getMatBGR(emptySkImage);
        assertNotNull(bgrMat);
        assertFalse(bgrMat.empty());
        assertEquals(1, bgrMat.rows());
        assertEquals(1, bgrMat.cols());
        
        // Test isEmpty method
        assertFalse(ImageConverter.isEmpty(emptySkImage));
    }
    
    @Test
    void shouldHandleLargeImage() {
        // Create a larger test image
        BufferedImage largeImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = largeImage.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 1920, 1080);
        g.dispose();
        Image largeSikuliImage = new Image(largeImage);
        
        // Test conversions with large image
        Mat bgrMat = ImageConverter.getMatBGR(largeSikuliImage);
        assertNotNull(bgrMat);
        assertFalse(bgrMat.empty());
        assertEquals(1080, bgrMat.rows());
        assertEquals(1920, bgrMat.cols());
        
        Mat hsvMat = ImageConverter.getMatHSV(largeSikuliImage);
        assertNotNull(hsvMat);
        assertFalse(hsvMat.empty());
        assertEquals(1080, hsvMat.rows());
        assertEquals(1920, hsvMat.cols());
    }
    
    @Test
    void shouldHandleDifferentImageTypes() {
        // Test with different BufferedImage types
        BufferedImage grayImage = new BufferedImage(50, 50, BufferedImage.TYPE_BYTE_GRAY);
        Mat grayMat = ImageConverter.getMatBGR(grayImage);
        assertNotNull(grayMat);
        assertFalse(grayMat.empty());
        
        BufferedImage argbImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Mat argbMat = ImageConverter.getMatBGR(argbImage);
        assertNotNull(argbMat);
        assertFalse(argbMat.empty());
    }
}