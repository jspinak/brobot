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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        drawRect = new DrawRect();
        testColor = new Scalar(255, 0, 0, 0); // Blue color
        
        // Setup default mock behavior
        when(mockMat.cols()).thenReturn(1920);
        when(mockMat.rows()).thenReturn(1080);
    }
    
    @Test
    @DisplayName("Should draw rectangle around match with padding")
    void shouldDrawRectAroundMatchWithPadding() {
        when(mockMatch.x()).thenReturn(100);
        when(mockMatch.y()).thenReturn(200);
        when(mockMatch.w()).thenReturn(50);
        when(mockMatch.h()).thenReturn(60);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRectAroundMatch(mockMat, mockMatch, testColor);
            
            // Verify rectangle was drawn with 1-pixel padding
            imgprocMock.verify(() -> 
                rectangle(eq(mockMat), any(Rect.class), eq(testColor))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw rectangle on match without padding")
    void shouldDrawRectOnMatchWithoutPadding() {
        when(mockMatch.x()).thenReturn(100);
        when(mockMatch.y()).thenReturn(200);
        when(mockMatch.w()).thenReturn(50);
        when(mockMatch.h()).thenReturn(60);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRectOnMatch(mockMat, mockMatch, testColor);
            
            imgprocMock.verify(() -> 
                rectangle(eq(mockMat), any(Rect.class), eq(testColor))
            );
        }
    }
    
    @Test
    @DisplayName("Should handle match at image boundary")
    void shouldHandleMatchAtImageBoundary() {
        // Match at top-left corner
        when(mockMatch.x()).thenReturn(0);
        when(mockMatch.y()).thenReturn(0);
        when(mockMatch.w()).thenReturn(50);
        when(mockMatch.h()).thenReturn(60);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRectAroundMatch(mockMat, mockMatch, testColor);
            
            imgprocMock.verify(() -> 
                rectangle(eq(mockMat), any(Rect.class), eq(testColor))
            );
        }
    }
    
    @Test
    @DisplayName("Should clip rectangle to image bounds")
    void shouldClipRectangleToImageBounds() {
        // Match extends beyond image bounds
        when(mockMatch.x()).thenReturn(1900);
        when(mockMatch.y()).thenReturn(1050);
        when(mockMatch.w()).thenReturn(100);
        when(mockMatch.h()).thenReturn(100);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRectOnMatch(mockMat, mockMatch, testColor);
            
            imgprocMock.verify(() -> 
                rectangle(eq(mockMat), any(Rect.class), eq(testColor))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw rectangle with OpenCV Rect")
    void shouldDrawRectangleWithOpenCVRect() {
        Rect testRect = new Rect(50, 50, 100, 100);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRect(mockMat, testRect, testColor);
            
            imgprocMock.verify(() -> 
                rectangle(eq(mockMat), any(Rect.class), eq(testColor))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw rectangle around OpenCV Rect with padding")
    void shouldDrawRectangleAroundOpenCVRectWithPadding() {
        Rect testRect = new Rect(50, 50, 100, 100);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRectAroundMatch(mockMat, testRect, testColor);
            
            imgprocMock.verify(() -> 
                rectangle(eq(mockMat), any(Rect.class), eq(testColor))
            );
        }
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
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRectAroundRegions(mockMat, regions, testColor);
            
            // Should draw 3 rectangles
            imgprocMock.verify(() -> 
                rectangle(eq(mockMat), any(Rect.class), eq(testColor)),
                times(3)
            );
        }
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
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRectAroundRegion(mockMat, region, testColor);
            
            imgprocMock.verify(() -> 
                rectangle(eq(mockMat), any(Rect.class), eq(testColor))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw on visualization layers")
    void shouldDrawOnVisualizationLayers() {
        Mat sceneMat = mock(Mat.class);
        Mat classesMat = mock(Mat.class);
        
        when(sceneMat.cols()).thenReturn(1920);
        when(sceneMat.rows()).thenReturn(1080);
        when(classesMat.cols()).thenReturn(1920);
        when(classesMat.rows()).thenReturn(1080);
        
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
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRectAroundMatch(mockVisualization, regions, testColor);
            
            // Should draw on both layers
            imgprocMock.verify(() -> 
                rectangle(any(Mat.class), any(Rect.class), eq(testColor)),
                times(2)
            );
        }
    }
    
    @Test
    @DisplayName("Should handle empty region list")
    void shouldHandleEmptyRegionList() {
        List<Region> emptyRegions = new ArrayList<>();
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRectAroundRegions(mockMat, emptyRegions, testColor);
            
            // Should not draw any rectangles
            imgprocMock.verify(() -> 
                rectangle(any(Mat.class), any(Rect.class), any(Scalar.class)),
                never()
            );
        }
    }
    
    @Test
    @DisplayName("Should handle negative coordinates")
    void shouldHandleNegativeCoordinates() {
        when(mockMatch.x()).thenReturn(-10);
        when(mockMatch.y()).thenReturn(-20);
        when(mockMatch.w()).thenReturn(50);
        when(mockMatch.h()).thenReturn(60);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRectAroundMatch(mockMat, mockMatch, testColor);
            
            // Should clamp negative coordinates to 0
            imgprocMock.verify(() -> 
                rectangle(eq(mockMat), any(Rect.class), eq(testColor))
            );
        }
    }
    
    @Test
    @DisplayName("Should handle large dimensions")
    void shouldHandleLargeDimensions() {
        when(mockMatch.x()).thenReturn(0);
        when(mockMatch.y()).thenReturn(0);
        when(mockMatch.w()).thenReturn(5000);
        when(mockMatch.h()).thenReturn(3000);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawRect.drawRectOnMatch(mockMat, mockMatch, testColor);
            
            // Should clamp to image bounds
            imgprocMock.verify(() -> 
                rectangle(eq(mockMat), any(Rect.class), eq(testColor))
            );
        }
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
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            for (Scalar color : colors) {
                drawRect.drawRectOnMatch(mockMat, mockMatch, color);
            }
            
            // Should draw with each color
            imgprocMock.verify(() -> 
                rectangle(eq(mockMat), any(Rect.class), any(Scalar.class)),
                times(colors.length)
            );
        }
    }
}