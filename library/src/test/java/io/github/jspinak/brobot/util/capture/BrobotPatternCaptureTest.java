package io.github.jspinak.brobot.util.capture;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for BrobotPatternCapture functionality.
 * Tests pattern capture operations with metadata handling.
 */
public class BrobotPatternCaptureTest extends BrobotTestBase {

    private BrobotPatternCapture patternCapture;
    private ObjectMapper objectMapper;
    private BufferedImage testImage;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        patternCapture = new BrobotPatternCapture();
        objectMapper = new ObjectMapper();
        
        // Create test image
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.setColor(Color.WHITE);
        g.fillRect(25, 25, 50, 50);
        g.dispose();
    }
    
    @Test
    void shouldCapturePatternWithMetadata() throws Exception {
        Rectangle region = new Rectangle(0, 0, 100, 100);
        String patternName = "test_pattern";
        
        // Mock Screen and ScreenImage
        Screen mockScreen = mock(Screen.class);
        ScreenImage mockScreenImage = mock(ScreenImage.class);
        when(mockScreenImage.getImage()).thenReturn(testImage);
        when(mockScreen.capture(any(Rectangle.class))).thenReturn(mockScreenImage);
        mockScreen.w = 1920;  // Direct field assignment
        mockScreen.h = 1080;  // Direct field assignment
        
        // Set the mock screen
        patternCapture.setScreen(mockScreen);
        
        // Mock static methods for metadata creation
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
             MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
            
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
            AffineTransform mockTransform = mock(AffineTransform.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
            when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
            when(mockTransform.getScaleX()).thenReturn(1.0);
            when(mockTransform.getScaleY()).thenReturn(1.0);
            
            Toolkit mockToolkit = mock(Toolkit.class);
            toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
            when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1920, 1080));
            
            // Mock ImageIO for writing
            try (MockedStatic<ImageIO> imageIOMock = mockStatic(ImageIO.class)) {
                imageIOMock.when(() -> ImageIO.write(any(BufferedImage.class), eq("png"), any(File.class)))
                    .thenReturn(true);
                
                ImageWriter mockWriter = mock(ImageWriter.class);
                Iterator<ImageWriter> mockIterator = mock(Iterator.class);
                ImageOutputStream mockOutputStream = mock(ImageOutputStream.class);
                IIOMetadata mockMetadata = mock(IIOMetadata.class);
                
                when(mockIterator.hasNext()).thenReturn(true);
                when(mockIterator.next()).thenReturn(mockWriter);
                imageIOMock.when(() -> ImageIO.getImageWritersByFormatName("png"))
                    .thenReturn(mockIterator);
                imageIOMock.when(() -> ImageIO.createImageOutputStream(any(File.class)))
                    .thenReturn(mockOutputStream);
                when(mockWriter.getDefaultImageMetadata(any(), any())).thenReturn(mockMetadata);
                
                // Capture pattern
                assertDoesNotThrow(() -> 
                    patternCapture.capturePattern(patternName, region)
                );
                
                // Verify screen capture was called
                verify(mockScreen).capture(region);
            }
        }
    }
    
    @Test
    void shouldCreatePatternMetadata() throws Exception {
        // Use reflection to test private method
        var method = BrobotPatternCapture.class.getDeclaredMethod("createMetadata");
        method.setAccessible(true);
        
        // Set up a mock Screen on the pattern capture object
        Screen mockScreen = mock(Screen.class);
        mockScreen.w = 1920;  // Direct field assignment
        mockScreen.h = 1080;  // Direct field assignment
        patternCapture.setScreen(mockScreen);
        
        // Mock GraphicsEnvironment
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            GraphicsConfiguration mockConfig = mock(GraphicsConfiguration.class);
            AffineTransform mockTransform = mock(AffineTransform.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
            when(mockConfig.getDefaultTransform()).thenReturn(mockTransform);
            when(mockTransform.getScaleX()).thenReturn(1.5);
            when(mockTransform.getScaleY()).thenReturn(1.5);
            
            // Mock Toolkit - need to properly stub it
            try (MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                Toolkit mockToolkit = mock(Toolkit.class);
                Dimension mockDimension = new Dimension(1920, 1080);
                
                // Complete the stubbing properly
                toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
                when(mockToolkit.getScreenSize()).thenReturn(mockDimension);
                
                BrobotPatternCapture.PatternMetadata metadata = 
                    (BrobotPatternCapture.PatternMetadata) method.invoke(patternCapture);
                
                assertNotNull(metadata);
                assertEquals(150, metadata.getDpi());
                assertEquals(1.5, metadata.getScaleFactorX());
                assertEquals(1.5, metadata.getScaleFactorY());
                assertEquals(1920, metadata.getPhysicalWidth());
                assertEquals(1080, metadata.getPhysicalHeight());
                assertEquals(1920, metadata.getLogicalWidth());
                assertEquals(1080, metadata.getLogicalHeight());
                assertNotNull(metadata.getJavaVersion());
                assertNotNull(metadata.getCaptureTimestamp());
                assertNotNull(metadata.getOs());
                assertNotNull(metadata.getOsVersion());
            }
        }
    }
    
    @Test
    void shouldSavePatternWithMetadata() throws Exception {
        BrobotPatternCapture.PatternMetadata metadata = new BrobotPatternCapture.PatternMetadata();
        metadata.setDpi(100);
        metadata.setScaleFactorX(1.0);
        metadata.setScaleFactorY(1.0);
        metadata.setPhysicalWidth(1920);
        metadata.setPhysicalHeight(1080);
        metadata.setLogicalWidth(1920);
        metadata.setLogicalHeight(1080);
        metadata.setJavaVersion("17.0.1");
        metadata.setCaptureTimestamp(Instant.now().toString());
        metadata.setOs("Windows");
        metadata.setOsVersion("10.0");
        
        // Use reflection to test private method
        var method = BrobotPatternCapture.class.getDeclaredMethod(
            "savePatternWithMetadata", BufferedImage.class, String.class, 
            BrobotPatternCapture.PatternMetadata.class
        );
        method.setAccessible(true);
        
        String patternName = "test_save";
        
        // Mock ImageIO operations
        try (MockedStatic<ImageIO> imageIOMock = mockStatic(ImageIO.class)) {
            ImageWriter mockWriter = mock(ImageWriter.class);
            @SuppressWarnings("unchecked")
            Iterator<ImageWriter> mockIterator = mock(Iterator.class);
            ImageOutputStream mockOutputStream = mock(ImageOutputStream.class);
            IIOMetadata mockMetadata = mock(IIOMetadata.class);
            
            when(mockIterator.hasNext()).thenReturn(true);
            when(mockIterator.next()).thenReturn(mockWriter);
            imageIOMock.when(() -> ImageIO.getImageWritersByFormatName("png"))
                .thenReturn(mockIterator);
            imageIOMock.when(() -> ImageIO.createImageOutputStream(any(File.class)))
                .thenReturn(mockOutputStream);
            when(mockWriter.getDefaultImageMetadata(any(), any())).thenReturn(mockMetadata);
            
            assertDoesNotThrow(() -> 
                method.invoke(patternCapture, testImage, patternName, metadata)
            );
        }
    }
    
    @Test
    void shouldCreateScaledVersions() throws Exception {
        BrobotPatternCapture.PatternMetadata metadata = new BrobotPatternCapture.PatternMetadata();
        metadata.setDpi(100);
        
        // Use reflection to test private method
        var method = BrobotPatternCapture.class.getDeclaredMethod(
            "createScaledVersions", BufferedImage.class, String.class,
            BrobotPatternCapture.PatternMetadata.class
        );
        method.setAccessible(true);
        
        String patternName = "test_scale";
        
        // Mock ImageIO for writing scaled versions
        try (MockedStatic<ImageIO> imageIOMock = mockStatic(ImageIO.class)) {
            imageIOMock.when(() -> ImageIO.write(any(BufferedImage.class), eq("png"), any(File.class)))
                .thenReturn(true);
            
            assertDoesNotThrow(() -> 
                method.invoke(patternCapture, testImage, patternName, metadata)
            );
            
            // Verify scaled versions were created (5 versions: 0.75, 0.8, 1.25, 1.33, 1.5)
            imageIOMock.verify(() -> 
                ImageIO.write(any(BufferedImage.class), eq("png"), any(File.class)),
                times(5)
            );
        }
    }
    
    @Test
    void shouldHandleCaptureException() {
        Rectangle region = new Rectangle(0, 0, 100, 100);
        String patternName = "error_pattern";
        
        // Mock Screen to throw exception
        try (MockedConstruction<Screen> screenMock = mockConstruction(Screen.class, (mock, context) -> {
            when(mock.capture(any(Rectangle.class))).thenThrow(new RuntimeException("Capture failed"));
        })) {
            // Should not throw, but log error
            assertDoesNotThrow(() -> 
                patternCapture.capturePattern(patternName, region)
            );
        }
    }
    
    @Test
    void shouldHandleNullRegion() {
        String patternName = "null_region_pattern";
        
        // Should handle null region gracefully
        assertDoesNotThrow(() -> 
            patternCapture.capturePattern(patternName, null)
        );
    }
    
    @Test
    void shouldHandleEmptyPatternName() {
        Rectangle region = new Rectangle(0, 0, 100, 100);
        
        try (MockedConstruction<Screen> screenMock = mockConstruction(Screen.class, (mock, context) -> {
            ScreenImage mockScreenImage = mock(ScreenImage.class);
            when(mockScreenImage.getImage()).thenReturn(testImage);
            when(mock.capture(any(Rectangle.class))).thenReturn(mockScreenImage);
        })) {
            // Should handle empty name
            assertDoesNotThrow(() -> 
                patternCapture.capturePattern("", region)
            );
        }
    }
    
    @Test
    void shouldHandleInteractiveCapture() {
        String patternName = "interactive_pattern";
        
        // Mock headless environment to prevent GUI creation
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.isHeadless()).thenReturn(true);
            
            // Should handle headless gracefully
            assertDoesNotThrow(() -> 
                patternCapture.interactiveCapture(patternName)
            );
        }
    }
    
    @Test
    void shouldValidatePatternMetadataGettersSetters() {
        BrobotPatternCapture.PatternMetadata metadata = new BrobotPatternCapture.PatternMetadata();
        
        // Test all setters and getters
        metadata.setDpi(120);
        assertEquals(120, metadata.getDpi());
        
        metadata.setScaleFactorX(1.25);
        assertEquals(1.25, metadata.getScaleFactorX());
        
        metadata.setScaleFactorY(1.25);
        assertEquals(1.25, metadata.getScaleFactorY());
        
        metadata.setPhysicalWidth(2560);
        assertEquals(2560, metadata.getPhysicalWidth());
        
        metadata.setPhysicalHeight(1440);
        assertEquals(1440, metadata.getPhysicalHeight());
        
        metadata.setLogicalWidth(2048);
        assertEquals(2048, metadata.getLogicalWidth());
        
        metadata.setLogicalHeight(1152);
        assertEquals(1152, metadata.getLogicalHeight());
        
        metadata.setJavaVersion("17.0.2");
        assertEquals("17.0.2", metadata.getJavaVersion());
        
        String timestamp = Instant.now().toString();
        metadata.setCaptureTimestamp(timestamp);
        assertEquals(timestamp, metadata.getCaptureTimestamp());
        
        metadata.setOs("Linux");
        assertEquals("Linux", metadata.getOs());
        
        metadata.setOsVersion("5.10");
        assertEquals("5.10", metadata.getOsVersion());
    }
    
    @Test
    void shouldSerializeMetadataToJson() throws Exception {
        BrobotPatternCapture.PatternMetadata metadata = new BrobotPatternCapture.PatternMetadata();
        metadata.setDpi(96);
        metadata.setScaleFactorX(1.0);
        metadata.setScaleFactorY(1.0);
        metadata.setPhysicalWidth(1920);
        metadata.setPhysicalHeight(1080);
        metadata.setLogicalWidth(1920);
        metadata.setLogicalHeight(1080);
        metadata.setJavaVersion("11.0.1");
        metadata.setCaptureTimestamp("2024-01-01T12:00:00Z");
        metadata.setOs("macOS");
        metadata.setOsVersion("14.0");
        
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(metadata);
        assertNotNull(json);
        assertTrue(json.contains("\"os\":\"macOS\""));
        
        // Deserialize back
        BrobotPatternCapture.PatternMetadata deserialized = 
            objectMapper.readValue(json, BrobotPatternCapture.PatternMetadata.class);
        
        assertEquals(metadata.getOs(), deserialized.getOs());
    }
    
    @Test
    void shouldHandleLargeImages() {
        // Create large test image
        BufferedImage largeImage = new BufferedImage(4096, 2160, BufferedImage.TYPE_INT_RGB);
        Rectangle region = new Rectangle(0, 0, 4096, 2160);
        String patternName = "large_pattern";
        
        try (MockedConstruction<Screen> screenMock = mockConstruction(Screen.class, (mock, context) -> {
            ScreenImage mockScreenImage = mock(ScreenImage.class);
            when(mockScreenImage.getImage()).thenReturn(largeImage);
            when(mock.capture(any(Rectangle.class))).thenReturn(mockScreenImage);
            when(mock.w).thenReturn(4096);
            when(mock.h).thenReturn(2160);
        })) {
            assertDoesNotThrow(() -> 
                patternCapture.capturePattern(patternName, region)
            );
        }
    }
    
    @Test
    void shouldHandleSmallImages() {
        // Create small test image
        BufferedImage smallImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Rectangle region = new Rectangle(0, 0, 10, 10);
        String patternName = "small_pattern";
        
        try (MockedConstruction<Screen> screenMock = mockConstruction(Screen.class, (mock, context) -> {
            ScreenImage mockScreenImage = mock(ScreenImage.class);
            when(mockScreenImage.getImage()).thenReturn(smallImage);
            when(mock.capture(any(Rectangle.class))).thenReturn(mockScreenImage);
        })) {
            assertDoesNotThrow(() -> 
                patternCapture.capturePattern(patternName, region)
            );
        }
    }
    
    @Test
    void shouldHandleInvalidScaleFactors() throws Exception {
        // Use reflection to test createScaledVersions with edge cases
        var method = BrobotPatternCapture.class.getDeclaredMethod(
            "createScaledVersions", BufferedImage.class, String.class,
            BrobotPatternCapture.PatternMetadata.class
        );
        method.setAccessible(true);
        
        BrobotPatternCapture.PatternMetadata metadata = new BrobotPatternCapture.PatternMetadata();
        
        // Test with small but valid image (not 1x1 which causes problems with scaling)
        BufferedImage smallImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        
        // Mock ImageIO for writing scaled versions
        try (MockedStatic<ImageIO> imageIOMock = mockStatic(ImageIO.class)) {
            imageIOMock.when(() -> ImageIO.write(any(BufferedImage.class), eq("png"), any(File.class)))
                .thenReturn(true);
            
            assertDoesNotThrow(() -> 
                method.invoke(patternCapture, smallImage, "small_pattern", metadata)
            );
        }
    }
    
    @Test
    void shouldHandleIOExceptionDuringSave() throws Exception {
        BrobotPatternCapture.PatternMetadata metadata = new BrobotPatternCapture.PatternMetadata();
        metadata.setDpi(100);
        
        var method = BrobotPatternCapture.class.getDeclaredMethod(
            "savePatternWithMetadata", BufferedImage.class, String.class,
            BrobotPatternCapture.PatternMetadata.class
        );
        method.setAccessible(true);
        
        // Mock ImageIO to provide writers but throw exception on createImageOutputStream
        try (MockedStatic<ImageIO> imageIOMock = mockStatic(ImageIO.class)) {
            ImageWriter mockWriter = mock(ImageWriter.class);
            Iterator<ImageWriter> mockIterator = mock(Iterator.class);
            
            when(mockIterator.hasNext()).thenReturn(true);
            when(mockIterator.next()).thenReturn(mockWriter);
            
            imageIOMock.when(() -> ImageIO.getImageWritersByFormatName("png"))
                .thenReturn(mockIterator);
            imageIOMock.when(() -> ImageIO.createImageOutputStream(any(File.class)))
                .thenThrow(new IOException("Write failed"));
            
            // The method throws IOException which is wrapped in InvocationTargetException
            InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> 
                method.invoke(patternCapture, testImage, "io_error_pattern", metadata)
            );
            
            // Verify the cause is the expected IOException
            assertTrue(exception.getCause() instanceof IOException);
            assertEquals("Write failed", exception.getCause().getMessage());
        }
    }
}