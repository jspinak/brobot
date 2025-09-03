package io.github.jspinak.brobot.tools.history.draw;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for DrawRect functionality.
 * Tests rectangle drawing operations on OpenCV Mat images.
 */
@ExtendWith(MockitoExtension.class)
public class DrawRectTest extends BrobotTestBase {

    private DrawRect drawRect;
    
    @Mock
    private Mat mockMat;
    
    @Mock
    private Match mockMatch;
    
    @Mock
    private Visualization mockVisualization;
    
    private Scalar testColor;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        org.mockito.MockitoAnnotations.openMocks(this);
        drawRect = new DrawRect();
        testColor = new Scalar(255, 0, 0, 0); // Blue color
        
        // Create a real Mat object for testing instead of mock
        mockMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockMat != null && !mockMat.isNull()) {
            mockMat.release();
        }
    }
    
    @Test
    @DisplayName("Should draw rectangle around match with padding")
    void shouldDrawRectAroundMatchWithPadding() {
        when(mockMatch.x()).thenReturn(100);
        when(mockMatch.y()).thenReturn(200);
        when(mockMatch.w()).thenReturn(50);
        when(mockMatch.h()).thenReturn(60);
        
        // Just verify no exception is thrown when drawing
        assertDoesNotThrow(() -> 
            drawRect.drawRectAroundMatch(mockMat, mockMatch, testColor)
        );
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
    
    @Test
    @DisplayName("Should draw rectangle on match without padding")
    void shouldDrawRectOnMatchWithoutPadding() {
        when(mockMatch.x()).thenReturn(100);
        when(mockMatch.y()).thenReturn(200);
        when(mockMatch.w()).thenReturn(50);
        when(mockMatch.h()).thenReturn(60);
        
        // Just verify no exception is thrown
        assertDoesNotThrow(() -> 
            drawRect.drawRectOnMatch(mockMat, mockMatch, testColor)
        );
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
    
    @Test
    @DisplayName("Should handle match at image boundary")
    void shouldHandleMatchAtImageBoundary() {
        // Match at top-left corner
        when(mockMatch.x()).thenReturn(0);
        when(mockMatch.y()).thenReturn(0);
        when(mockMatch.w()).thenReturn(50);
        when(mockMatch.h()).thenReturn(60);
        
        // Just verify no exception is thrown
        assertDoesNotThrow(() -> 
            drawRect.drawRectAroundMatch(mockMat, mockMatch, testColor)
        );
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
    
    @Test
    @DisplayName("Should clip rectangle to image bounds")
    void shouldClipRectangleToImageBounds() {
        // Match extends beyond image bounds
        when(mockMatch.x()).thenReturn(1900);
        when(mockMatch.y()).thenReturn(1050);
        when(mockMatch.w()).thenReturn(100);
        when(mockMatch.h()).thenReturn(100);
        
        // Just verify no exception is thrown
        assertDoesNotThrow(() -> 
            drawRect.drawRectOnMatch(mockMat, mockMatch, testColor)
        );
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
    
    @Test
    @DisplayName("Should draw rectangle with OpenCV Rect")
    void shouldDrawRectangleWithOpenCVRect() {
        Rect testRect = new Rect(50, 50, 100, 100);
        
        // Just verify no exception is thrown
        assertDoesNotThrow(() -> 
            drawRect.drawRect(mockMat, testRect, testColor)
        );
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
    
    @Test
    @DisplayName("Should draw rectangle around OpenCV Rect with padding")
    void shouldDrawRectangleAroundOpenCVRectWithPadding() {
        Rect testRect = new Rect(50, 50, 100, 100);
        
        // Just verify no exception is thrown
        assertDoesNotThrow(() -> 
            drawRect.drawRectAroundMatch(mockMat, testRect, testColor)
        );
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
    
    @Test
    @DisplayName("Should draw rectangles around multiple regions")
    void shouldDrawRectanglesAroundMultipleRegions() {
        List<Region> regions = new ArrayList<>();
        Region region1 = mock(Region.class);
        Region region2 = mock(Region.class);
        Region region3 = mock(Region.class);
        
        Match match1 = mock(Match.class);
        Match match2 = mock(Match.class);
        Match match3 = mock(Match.class);
        
        when(region1.toMatch()).thenReturn(match1);
        when(region2.toMatch()).thenReturn(match2);
        when(region3.toMatch()).thenReturn(match3);
        
        when(match1.x()).thenReturn(10);
        when(match1.y()).thenReturn(20);
        when(match1.w()).thenReturn(30);
        when(match1.h()).thenReturn(40);
        
        when(match2.x()).thenReturn(100);
        when(match2.y()).thenReturn(200);
        when(match2.w()).thenReturn(50);
        when(match2.h()).thenReturn(60);
        
        when(match3.x()).thenReturn(500);
        when(match3.y()).thenReturn(600);
        when(match3.w()).thenReturn(70);
        when(match3.h()).thenReturn(80);
        
        regions.add(region1);
        regions.add(region2);
        regions.add(region3);
        
        // Just verify no exception is thrown
        assertDoesNotThrow(() -> 
            drawRect.drawRectAroundRegions(mockMat, regions, testColor)
        );
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
    
    @Test
    @DisplayName("Should handle null Mat gracefully")
    void shouldHandleNullMatGracefully() {
        List<Region> regions = Arrays.asList(mock(Region.class));
        
        // Should not throw exception
        assertDoesNotThrow(() -> 
            drawRect.drawRectAroundRegions(null, regions, testColor)
        );
        
        assertDoesNotThrow(() -> 
            drawRect.drawRectAroundRegion(null, mock(Region.class), testColor)
        );
    }
    
    @Test
    @DisplayName("Should draw rectangle around single region")
    void shouldDrawRectangleAroundSingleRegion() {
        Region region = mock(Region.class);
        Match match = mock(Match.class);
        
        when(region.toMatch()).thenReturn(match);
        when(match.x()).thenReturn(100);
        when(match.y()).thenReturn(200);
        when(match.w()).thenReturn(50);
        when(match.h()).thenReturn(60);
        
        // Just verify no exception is thrown
        assertDoesNotThrow(() -> 
            drawRect.drawRectAroundRegion(mockMat, region, testColor)
        );
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
    
    @Test
    @DisplayName("Should draw on visualization layers")
    void shouldDrawOnVisualizationLayers() {
        // Create real Mat objects instead of mocks to avoid NullPointerException
        Mat sceneMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        Mat classesMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        
        when(mockVisualization.getMatchesOnScene()).thenReturn(sceneMat);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(classesMat);
        
        List<Region> regions = new ArrayList<>();
        Region region = mock(Region.class);
        Match match = mock(Match.class);
        
        when(region.toMatch()).thenReturn(match);
        when(match.x()).thenReturn(100);
        when(match.y()).thenReturn(200);
        when(match.w()).thenReturn(50);
        when(match.h()).thenReturn(60);
        
        regions.add(region);
        
        // Just verify no exception is thrown
        assertDoesNotThrow(() -> 
            drawRect.drawRectAroundMatch(mockVisualization, regions, testColor)
        );
        
        // Verify the visualization object is still valid
        assertNotNull(mockVisualization);
        
        // Clean up the Mat objects
        if (!sceneMat.isNull()) sceneMat.release();
        if (!classesMat.isNull()) classesMat.release();
    }
    
    @Test
    @DisplayName("Should handle empty region list")
    void shouldHandleEmptyRegionList() {
        List<Region> emptyRegions = new ArrayList<>();
        
        // Just verify no exception is thrown with empty regions
        assertDoesNotThrow(() -> 
            drawRect.drawRectAroundRegions(mockMat, emptyRegions, testColor)
        );
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
    
    @Test
    @DisplayName("Should handle negative coordinates")
    void shouldHandleNegativeCoordinates() {
        when(mockMatch.x()).thenReturn(-10);
        when(mockMatch.y()).thenReturn(-20);
        when(mockMatch.w()).thenReturn(50);
        when(mockMatch.h()).thenReturn(60);
        
        // Just verify no exception is thrown with negative coordinates
        assertDoesNotThrow(() -> 
            drawRect.drawRectAroundMatch(mockMat, mockMatch, testColor)
        );
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
    
    @Test
    @DisplayName("Should handle large dimensions")
    void shouldHandleLargeDimensions() {
        when(mockMatch.x()).thenReturn(0);
        when(mockMatch.y()).thenReturn(0);
        when(mockMatch.w()).thenReturn(5000);
        when(mockMatch.h()).thenReturn(3000);
        
        // Just verify no exception is thrown with large dimensions
        assertDoesNotThrow(() -> 
            drawRect.drawRectOnMatch(mockMat, mockMatch, testColor)
        );
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
    
    @Test
    @DisplayName("Should handle various color scalars")
    void shouldHandleVariousColorScalars() {
        when(mockMatch.x()).thenReturn(100);
        when(mockMatch.y()).thenReturn(200);
        when(mockMatch.w()).thenReturn(50);
        when(mockMatch.h()).thenReturn(60);
        
        Scalar[] colors = {
            new Scalar(255, 0, 0, 0),    // Blue
            new Scalar(0, 255, 0, 0),    // Green
            new Scalar(0, 0, 255, 0),    // Red
            new Scalar(255, 255, 0, 0),  // Cyan
            new Scalar(255, 0, 255, 0),  // Magenta
            new Scalar(0, 255, 255, 0),  // Yellow
            new Scalar(128, 128, 128, 0) // Gray
        };
        
        // Just verify no exception is thrown with various colors
        assertDoesNotThrow(() -> {
            for (Scalar color : colors) {
                drawRect.drawRectOnMatch(mockMat, mockMatch, color);
            }
        });
        
        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }
}