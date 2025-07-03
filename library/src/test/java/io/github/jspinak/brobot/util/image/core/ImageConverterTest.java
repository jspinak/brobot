package io.github.jspinak.brobot.util.image.core;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.sikuli.script.Image;

import java.awt.image.BufferedImage;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ImageConverterTest {
    
    private Image mockImage;
    private BufferedImage mockBufferedImage;
    private Mat mockMat;
    
    @BeforeEach
    void setUp() {
        mockImage = mock(Image.class);
        mockBufferedImage = mock(BufferedImage.class);
        mockMat = mock(Mat.class);
    }
    
    @Test
    void testGetBytes_FromImage() {
        // Setup
        byte[] expectedBytes = new byte[]{1, 2, 3, 4};
        when(mockImage.get()).thenReturn(mockBufferedImage);
        
        try (MockedStatic<BufferedImageUtilities> mockedUtil = mockStatic(BufferedImageUtilities.class)) {
            mockedUtil.when(() -> BufferedImageUtilities.toByteArray(mockBufferedImage))
                      .thenReturn(expectedBytes);
            
            // Execute
            byte[] result = ImageConverter.getBytes(mockImage);
            
            // Verify
            assertArrayEquals(expectedBytes, result);
            mockedUtil.verify(() -> BufferedImageUtilities.toByteArray(mockBufferedImage));
        }
    }
    
    @Test
    void testGetImage_FromBytes() {
        // Setup
        byte[] inputBytes = new byte[]{1, 2, 3, 4};
        
        try (MockedStatic<BufferedImageUtilities> mockedUtil = mockStatic(BufferedImageUtilities.class)) {
            mockedUtil.when(() -> BufferedImageUtilities.fromByteArray(inputBytes))
                      .thenReturn(mockBufferedImage);
            
            // Execute
            Image result = ImageConverter.getImage(inputBytes);
            
            // Verify
            assertNotNull(result);
            mockedUtil.verify(() -> BufferedImageUtilities.fromByteArray(inputBytes));
        }
    }
    
    @Test
    void testGetImage_FromMat() {
        // Setup
        try (MockedStatic<BufferedImageUtilities> mockedUtil = mockStatic(BufferedImageUtilities.class)) {
            mockedUtil.when(() -> BufferedImageUtilities.fromMat(mockMat))
                      .thenReturn(mockBufferedImage);
            
            // Execute
            Image result = ImageConverter.getImage(mockMat);
            
            // Verify
            assertNotNull(result);
            mockedUtil.verify(() -> BufferedImageUtilities.fromMat(mockMat));
        }
    }
    
    @Test
    void testGetMatBGR_FromImage() {
        // Setup
        when(mockImage.get()).thenReturn(mockBufferedImage);
        
        try (MockedStatic<MatrixUtilities> mockedMatrix = mockStatic(MatrixUtilities.class)) {
            mockedMatrix.when(() -> MatrixUtilities.bufferedImageToMat(mockBufferedImage))
                        .thenReturn(Optional.of(mockMat));
            
            // Execute
            Mat result = ImageConverter.getMatBGR(mockImage);
            
            // Verify
            assertEquals(mockMat, result);
            mockedMatrix.verify(() -> MatrixUtilities.bufferedImageToMat(mockBufferedImage));
        }
    }
    
    @Test
    void testGetMatBGR_FromBufferedImage() {
        // Setup
        try (MockedStatic<MatrixUtilities> mockedMatrix = mockStatic(MatrixUtilities.class)) {
            mockedMatrix.when(() -> MatrixUtilities.bufferedImageToMat(mockBufferedImage))
                        .thenReturn(Optional.of(mockMat));
            
            // Execute
            Mat result = ImageConverter.getMatBGR(mockBufferedImage);
            
            // Verify
            assertEquals(mockMat, result);
            mockedMatrix.verify(() -> MatrixUtilities.bufferedImageToMat(mockBufferedImage));
        }
    }
    
    @Test
    void testGetMatBGR_ConversionFailure_ReturnsEmptyMat() {
        // Setup
        when(mockImage.get()).thenReturn(mockBufferedImage);
        
        try (MockedStatic<MatrixUtilities> mockedMatrix = mockStatic(MatrixUtilities.class)) {
            mockedMatrix.when(() -> MatrixUtilities.bufferedImageToMat(mockBufferedImage))
                        .thenReturn(Optional.empty());
            
            // Execute
            Mat result = ImageConverter.getMatBGR(mockImage);
            
            // Verify
            assertNotNull(result);
            // Note: Can't verify it's empty without actually creating Mat
        }
    }
    
    @Test
    void testGetMatHSV_FromImage() {
        // Setup
        Mat hsvMat = mock(Mat.class);
        when(mockImage.get()).thenReturn(mockBufferedImage);
        
        try (MockedStatic<MatrixUtilities> mockedMatrix = mockStatic(MatrixUtilities.class)) {
            mockedMatrix.when(() -> MatrixUtilities.bufferedImageToMat(mockBufferedImage))
                        .thenReturn(Optional.of(mockMat));
            mockedMatrix.when(() -> MatrixUtilities.BGRtoHSV(mockMat))
                        .thenReturn(hsvMat);
            
            // Execute
            Mat result = ImageConverter.getMatHSV(mockImage);
            
            // Verify
            assertEquals(hsvMat, result);
            mockedMatrix.verify(() -> MatrixUtilities.bufferedImageToMat(mockBufferedImage));
            mockedMatrix.verify(() -> MatrixUtilities.BGRtoHSV(mockMat));
        }
    }
    
    @Test
    void testGetMatHSV_FromBufferedImage() {
        // Setup
        Mat hsvMat = mock(Mat.class);
        
        try (MockedStatic<MatrixUtilities> mockedMatrix = mockStatic(MatrixUtilities.class)) {
            mockedMatrix.when(() -> MatrixUtilities.bufferedImageToMat(mockBufferedImage))
                        .thenReturn(Optional.of(mockMat));
            mockedMatrix.when(() -> MatrixUtilities.BGRtoHSV(mockMat))
                        .thenReturn(hsvMat);
            
            // Execute
            Mat result = ImageConverter.getMatHSV(mockBufferedImage);
            
            // Verify
            assertEquals(hsvMat, result);
            mockedMatrix.verify(() -> MatrixUtilities.bufferedImageToMat(mockBufferedImage));
            mockedMatrix.verify(() -> MatrixUtilities.BGRtoHSV(mockMat));
        }
    }
    
    @Test
    void testGetMatHSV_ConversionFailure_ReturnsEmptyMat() {
        // Setup
        when(mockImage.get()).thenReturn(mockBufferedImage);
        
        try (MockedStatic<MatrixUtilities> mockedMatrix = mockStatic(MatrixUtilities.class)) {
            mockedMatrix.when(() -> MatrixUtilities.bufferedImageToMat(mockBufferedImage))
                        .thenReturn(Optional.empty());
            
            // Execute
            Mat result = ImageConverter.getMatHSV(mockImage);
            
            // Verify
            assertNotNull(result);
            mockedMatrix.verify(() -> MatrixUtilities.bufferedImageToMat(mockBufferedImage));
            mockedMatrix.verify(() -> MatrixUtilities.BGRtoHSV(any()), never());
        }
    }
    
    @Test
    void testIsEmpty_NullBufferedImage() {
        // Setup
        when(mockImage.get()).thenReturn(null);
        
        // Execute
        boolean result = ImageConverter.isEmpty(mockImage);
        
        // Verify
        assertTrue(result);
    }
    
    @Test
    void testIsEmpty_NonNullBufferedImage() {
        // Setup
        when(mockImage.get()).thenReturn(mockBufferedImage);
        
        // Execute
        boolean result = ImageConverter.isEmpty(mockImage);
        
        // Verify
        assertFalse(result);
    }
}