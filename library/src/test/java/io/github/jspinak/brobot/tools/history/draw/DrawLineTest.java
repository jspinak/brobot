package io.github.jspinak.brobot.tools.history.draw;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.bytedeco.opencv.global.opencv_imgproc.line;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for DrawLine functionality.
 * Tests line drawing operations on OpenCV Mat images.
 */
@ExtendWith(MockitoExtension.class)
public class DrawLineTest extends BrobotTestBase {

    private DrawLine drawLine;
    
    @Mock
    private Mat mockMat;
    
    private Scalar testColor;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        drawLine = new DrawLine();
        testColor = new Scalar(0, 255, 0, 0); // Green color
        
        // Setup default mock behavior
        when(mockMat.cols()).thenReturn(1920);
        when(mockMat.rows()).thenReturn(1080);
    }
    
    @Test
    @DisplayName("Should draw line between two points")
    void shouldDrawLineBetweenTwoPoints() {
        Point start = new Point(100, 200);
        Point end = new Point(300, 400);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, start, end, testColor, 1, 8, 0);
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), eq(start), eq(end), eq(testColor), eq(1), eq(8), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw line with custom thickness")
    void shouldDrawLineWithCustomThickness() {
        Point start = new Point(0, 0);
        Point end = new Point(100, 100);
        int thickness = 5;
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, start, end, testColor, thickness, 8, 0);
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), eq(start), eq(end), eq(testColor), eq(thickness), eq(8), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw line with custom line type")
    void shouldDrawLineWithCustomLineType() {
        Point start = new Point(50, 50);
        Point end = new Point(150, 150);
        int thickness = 2;
        int lineType = 8; // Anti-aliased line
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, start, end, testColor, thickness, lineType, 0);
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), eq(start), eq(end), eq(testColor), eq(thickness), eq(lineType), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw line with all parameters")
    void shouldDrawLineWithAllParameters() {
        Point start = new Point(10, 20);
        Point end = new Point(200, 300);
        int thickness = 3;
        int lineType = 4;
        int shift = 2;
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, start, end, testColor, thickness, lineType, shift);
            
            imgprocMock.verify(() -> 
                line(mockMat, start, end, testColor, thickness, lineType, shift)
            );
        }
    }
    
    @Test
    @DisplayName("Should draw line using Locations")
    void shouldDrawLineUsingLocations() {
        Location startLoc = new Location(100, 200);
        Location endLoc = new Location(400, 500);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, startLoc, endLoc, testColor, 1, 8, 0);
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), any(Point.class), any(Point.class), eq(testColor), eq(1), eq(8), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw line with zero thickness")
    void shouldDrawLineWithZeroThickness() {
        Point start = new Point(0, 0);
        Point end = new Point(100, 0);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, start, end, testColor, 0, 8, 0);
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), eq(start), eq(end), eq(testColor), eq(0), eq(8), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw very thick line")
    void shouldDrawVeryThickLine() {
        Point start = new Point(500, 500);
        Point end = new Point(700, 700);
        int thickness = 20;
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, start, end, testColor, thickness, 8, 0);
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), eq(start), eq(end), eq(testColor), eq(thickness), eq(8), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw horizontal line")
    void shouldDrawHorizontalLine() {
        Point start = new Point(0, 500);
        Point end = new Point(1920, 500);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, start, end, testColor, 1, 8, 0);
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), eq(start), eq(end), eq(testColor), eq(1), eq(8), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw vertical line")
    void shouldDrawVerticalLine() {
        Point start = new Point(960, 0);
        Point end = new Point(960, 1080);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, start, end, testColor, 1, 8, 0);
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), eq(start), eq(end), eq(testColor), eq(1), eq(8), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw diagonal line")
    void shouldDrawDiagonalLine() {
        Point start = new Point(0, 0);
        Point end = new Point(1920, 1080);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, start, end, testColor, 1, 8, 0);
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), eq(start), eq(end), eq(testColor), eq(1), eq(8), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw line with same start and end point")
    void shouldDrawLineWithSameStartAndEndPoint() {
        Point point = new Point(500, 500);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, point, point, testColor, 1, 8, 0);
            
            // Should still call line even if points are same (will draw a point)
            imgprocMock.verify(() -> 
                line(eq(mockMat), eq(point), eq(point), eq(testColor), eq(1), eq(8), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw line with negative coordinates")
    void shouldDrawLineWithNegativeCoordinates() {
        Point start = new Point(-100, -200);
        Point end = new Point(300, 400);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, start, end, testColor, 1, 8, 0);
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), eq(start), eq(end), eq(testColor), eq(1), eq(8), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw line outside image bounds")
    void shouldDrawLineOutsideImageBounds() {
        Point start = new Point(2000, 2000);
        Point end = new Point(3000, 3000);
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            drawLine.draw(mockMat, start, end, testColor, 1, 8, 0);
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), eq(start), eq(end), eq(testColor), eq(1), eq(8), eq(0))
            );
        }
    }
    
    @Test
    @DisplayName("Should draw lines with various colors")
    void shouldDrawLinesWithVariousColors() {
        Point start = new Point(100, 100);
        Point end = new Point(200, 200);
        
        Scalar[] colors = {
            new Scalar(255, 0, 0, 0),    // Blue
            new Scalar(0, 255, 0, 0),    // Green
            new Scalar(0, 0, 255, 0),    // Red
            new Scalar(255, 255, 0, 0),  // Cyan
            new Scalar(255, 0, 255, 0),  // Magenta
            new Scalar(0, 255, 255, 0),  // Yellow
            new Scalar(0, 0, 0, 0),      // Black
            new Scalar(255, 255, 255, 0) // White
        };
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            for (Scalar color : colors) {
                drawLine.draw(mockMat, start, end, color, 1, 8, 0);
            }
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), any(Point.class), any(Point.class), any(Scalar.class), eq(1), eq(8), eq(0)),
                times(colors.length)
            );
        }
    }
    
    @Test
    @DisplayName("Should draw multiple connected lines")
    void shouldDrawMultipleConnectedLines() {
        Point[] points = {
            new Point(100, 100),
            new Point(200, 150),
            new Point(300, 100),
            new Point(400, 200),
            new Point(500, 150)
        };
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            // Draw connected lines
            for (int i = 0; i < points.length - 1; i++) {
                drawLine.draw(mockMat, points[i], points[i + 1], testColor, 1, 8, 0);
            }
            
            imgprocMock.verify(() -> 
                line(eq(mockMat), any(Point.class), any(Point.class), eq(testColor), eq(1), eq(8), eq(0)),
                times(points.length - 1)
            );
        }
    }
    
    @Test
    @DisplayName("Should draw grid pattern")
    void shouldDrawGridPattern() {
        int gridSize = 100;
        int cols = 19; // 1920 / 100
        int rows = 10; // 1080 / 100
        
        try (MockedStatic<org.bytedeco.opencv.global.opencv_imgproc> imgprocMock = 
                mockStatic(org.bytedeco.opencv.global.opencv_imgproc.class)) {
            
            // Draw vertical lines
            for (int x = 0; x <= 1920; x += gridSize) {
                drawLine.draw(mockMat, new Point(x, 0), new Point(x, 1080), testColor, 1, 8, 0);
            }
            
            // Draw horizontal lines
            for (int y = 0; y <= 1080; y += gridSize) {
                drawLine.draw(mockMat, new Point(0, y), new Point(1920, y), testColor, 1, 8, 0);
            }
            
            // Verify appropriate number of lines were drawn
            imgprocMock.verify(() -> 
                line(eq(mockMat), any(Point.class), any(Point.class), eq(testColor), eq(1), eq(8), eq(0)),
                atLeast(cols + rows)
            );
        }
    }
}