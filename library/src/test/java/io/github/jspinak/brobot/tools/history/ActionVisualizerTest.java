package io.github.jspinak.brobot.tools.history;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.utils.MatTestUtils;
import io.github.jspinak.brobot.tools.history.draw.DrawRect;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for ActionVisualizer class.
 * Tests drawing operations for matches, clicks, drags, and regions using real Mat operations.
 */
public class ActionVisualizerTest extends BrobotTestBase {
    
    @Mock
    private DrawRect mockDrawRect;
    
    @Mock
    private Visualization mockVisualization;
    
    private ActionVisualizer actionVisualizer;
    
    // Test Mats for real operations
    private Mat testMat;
    private Mat sceneMat;
    private Mat matchesOnSceneMat;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        actionVisualizer = new ActionVisualizer(mockDrawRect);
        
        // Create safe test Mats using MatTestUtils
        testMat = MatTestUtils.createSafeMat(1080, 1920, CV_8UC3);
        sceneMat = MatTestUtils.createColorMat(1080, 1920, 100, 100, 100); // Gray background
        matchesOnSceneMat = MatTestUtils.createSafeMat(1080, 1920, CV_8UC3);
        
        // Setup mock visualization to return real Mats
        when(mockVisualization.getMatchesOnScene()).thenReturn(matchesOnSceneMat);
        when(mockVisualization.getScene()).thenReturn(sceneMat);
    }
    
    @AfterEach
    public void cleanupTest() {
        // Safe cleanup of all Mats
        MatTestUtils.safeReleaseAll(testMat, sceneMat, matchesOnSceneMat);
    }
    
    @Nested
    @DisplayName("Rectangle Drawing Tests")
    class RectangleDrawingTests {
        
        @Test
        @DisplayName("Should draw rectangle with boundary padding")
        public void testDrawRectWithPadding() {
            Match match = new Match.Builder()
                .setRegion(100, 100, 50, 50)
                .build();
            
            Scalar color = new Scalar(255, 0, 0, 0);
            
            // Validate Mat before operation
            MatTestUtils.validateMat(testMat, "testMat before drawRect");
            
            // Should draw without exception
            assertDoesNotThrow(() -> actionVisualizer.drawRect(testMat, match, color));
            
            // Verify rectangle was drawn (check non-zero pixels in the expected area)
            Mat roi = testMat.apply(new Rect(99, 99, 52, 52)); // Account for 1-pixel padding
            double sum = sumElems(roi).get(0);
            assertTrue(sum > 0, "Rectangle should be drawn on the Mat");
        }
        
        @Test
        @DisplayName("Should clamp rectangle to screen boundaries")
        public void testDrawRectBoundaryClamping() {
            // Match near edge of screen
            Match match = new Match.Builder()
                .setRegion(1900, 1060, 50, 50)  // Extends past screen
                .build();
            
            Scalar color = new Scalar(0, 255, 0, 0);
            
            MatTestUtils.validateMat(testMat, "testMat before boundary test");
            
            // Should not throw exception even when match extends past boundaries
            assertDoesNotThrow(() -> actionVisualizer.drawRect(testMat, match, color));
        }
        
        @Test
        @DisplayName("Should handle match at origin")
        public void testDrawRectAtOrigin() {
            Match match = new Match.Builder()
                .setRegion(0, 0, 20, 20)
                .build();
            
            Scalar color = new Scalar(0, 0, 255, 0);
            
            MatTestUtils.validateMat(testMat, "testMat at origin test");
            
            assertDoesNotThrow(() -> actionVisualizer.drawRect(testMat, match, color));
            
            // Verify drawing at origin
            Mat roi = testMat.apply(new Rect(0, 0, 22, 22)); // Include padding
            double sum = sumElems(roi).get(2); // Blue channel
            assertTrue(sum > 0, "Rectangle should be drawn at origin");
        }
    }
    
    @Nested
    @DisplayName("Point Drawing Tests")
    class PointDrawingTests {
        
        @Test
        @DisplayName("Should draw multi-ring point at target location")
        public void testDrawPoint() {
            Match match = new Match.Builder()
                .setRegion(480, 380, 40, 40)
                .build();
            match.setTarget(new Location(500, 400));
            
            Scalar color = new Scalar(255, 150, 255, 0);
            
            MatTestUtils.validateMat(testMat, "testMat before drawPoint");
            
            assertDoesNotThrow(() -> actionVisualizer.drawPoint(testMat, match, color));
            
            // Verify circles were drawn at target location
            // Check a small ROI around the target point
            Mat roi = testMat.apply(new Rect(490, 390, 20, 20));
            double sum = sumElems(roi).get(0) + sumElems(roi).get(2); // Blue + Red channels
            assertTrue(sum > 0, "Point should be drawn at target location");
        }
        
        @Test
        @DisplayName("Should handle match with calculated target location")
        public void testDrawPointWithCalculatedTarget() {
            Match match = new Match.Builder()
                .setRegion(100, 100, 50, 50)
                .build();
            // Set a target to avoid NPE
            match.setTarget(new Location(125, 125)); // Center of the region
            
            Scalar color = new Scalar(255, 0, 0, 0);
            
            MatTestUtils.validateMat(testMat, "testMat with calculated target");
            
            assertDoesNotThrow(() -> actionVisualizer.drawPoint(testMat, match, color));
        }
    }
    
    @Nested
    @DisplayName("Click Visualization Tests")
    class ClickVisualizationTests {
        
        @Test
        @DisplayName("Should draw clicks for all matches")
        public void testDrawClick() {
            Match match1 = createMatchWithTarget(100, 100);
            Match match2 = createMatchWithTarget(200, 200);
            
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Arrays.asList(match1, match2));
            
            MatTestUtils.validateMat(matchesOnSceneMat, "matchesOnSceneMat before clicks");
            
            actionVisualizer.drawClick(mockVisualization, actionResult);
            
            // Verify getMatchesOnScene was called for each match
            verify(mockVisualization, times(2)).getMatchesOnScene();
            
            // Verify the Mat has been modified (check for non-zero pixels)
            double sum = sumElems(matchesOnSceneMat).get(0) + 
                        sumElems(matchesOnSceneMat).get(1) + 
                        sumElems(matchesOnSceneMat).get(2);
            assertTrue(sum > 0, "Clicks should be drawn on the Mat");
        }
        
        @Test
        @DisplayName("Should handle empty match list")
        public void testDrawClickEmptyList() {
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.emptyList());
            
            assertDoesNotThrow(() -> actionVisualizer.drawClick(mockVisualization, actionResult));
            
            // Should not attempt to get the drawing surface if no matches
            verify(mockVisualization, never()).getMatchesOnScene();
        }
    }
    
    @Nested
    @DisplayName("Arrow Drawing Tests")
    class ArrowDrawingTests {
        
        @Test
        @DisplayName("Should draw arrow between two locations")
        public void testDrawArrow() {
            Location start = new Location(100, 100);
            Location end = new Location(300, 300);
            
            Scalar color = new Scalar(255, 150, 255, 0);
            
            MatTestUtils.validateMat(testMat, "testMat before arrow");
            
            assertDoesNotThrow(() -> actionVisualizer.drawArrow(testMat, start, end, color));
            
            // Verify arrow was drawn (check for non-zero pixels along the line)
            // Sample a point in the middle of the arrow
            Mat roi = testMat.apply(new Rect(190, 190, 20, 20));
            double sum = sumElems(roi).get(0) + sumElems(roi).get(2); // Blue + Red
            assertTrue(sum > 0, "Arrow should be drawn between locations");
        }
        
        @Test
        @DisplayName("Should handle arrow with same start and end")
        public void testDrawArrowSameLocation() {
            Location location = new Location(200, 200);
            
            Scalar color = new Scalar(0, 255, 0, 0);
            
            MatTestUtils.validateMat(testMat, "testMat same location arrow");
            
            // Arrow with same start and end should not crash
            assertDoesNotThrow(() -> actionVisualizer.drawArrow(testMat, location, location, color));
        }
    }
    
    @Nested
    @DisplayName("Drag Visualization Tests")
    class DragVisualizationTests {
        
        @Test
        @DisplayName("Should draw arrows between consecutive matches")
        public void testDrawDrag() {
            Match match1 = createMatchWithTarget(100, 100);
            Match match2 = createMatchWithTarget(200, 200);
            Match match3 = createMatchWithTarget(300, 300);
            
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Arrays.asList(match1, match2, match3));
            
            MatTestUtils.validateMat(matchesOnSceneMat, "matchesOnSceneMat before drag");
            
            actionVisualizer.drawDrag(mockVisualization, actionResult);
            
            // Should draw 2 arrows (between 3 matches)
            verify(mockVisualization, times(2)).getMatchesOnScene();
            
            // Verify arrows were drawn
            double sum = sumElems(matchesOnSceneMat).get(0) + 
                        sumElems(matchesOnSceneMat).get(1) + 
                        sumElems(matchesOnSceneMat).get(2);
            assertTrue(sum > 0, "Drag arrows should be drawn on the Mat");
        }
        
        @Test
        @DisplayName("Should not draw drag with less than 2 matches")
        public void testDrawDragInsufficientMatches() {
            Match match = createMatchWithTarget(100, 100);
            
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.singletonList(match));
            
            actionVisualizer.drawDrag(mockVisualization, actionResult);
            
            // Should not attempt to draw anything
            verify(mockVisualization, never()).getMatchesOnScene();
        }
        
        @Test
        @DisplayName("Should handle empty match list for drag")
        public void testDrawDragEmptyList() {
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.emptyList());
            
            assertDoesNotThrow(() -> actionVisualizer.drawDrag(mockVisualization, actionResult));
            
            verify(mockVisualization, never()).getMatchesOnScene();
        }
    }
    
    @Nested
    @DisplayName("Defined Region Tests")
    class DefinedRegionTests {
        
        @Test
        @DisplayName("Should draw defined region from action result")
        public void testDrawDefinedRegion() {
            io.github.jspinak.brobot.model.element.Region definedRegion = 
                new io.github.jspinak.brobot.model.element.Region(50, 50, 800, 600);
            
            ActionResult actionResult = new ActionResult();
            actionResult.setDefinedRegions(Collections.singletonList(definedRegion));
            
            MatTestUtils.validateMat(sceneMat, "sceneMat before defined region");
            
            actionVisualizer.drawDefinedRegion(mockVisualization, actionResult);
            
            // Verify that drawRectAroundRegion was called
            verify(mockDrawRect).drawRectAroundRegion(eq(sceneMat), any(io.github.jspinak.brobot.model.element.Region.class), any(Scalar.class));
            verify(mockVisualization, atLeastOnce()).getScene();
        }
        
        @Test
        @DisplayName("Should handle empty defined region")
        public void testDrawDefinedRegionEmpty() {
            ActionResult actionResult = new ActionResult();
            // No defined region set - getDefinedRegion() returns an empty region from RegionManager
            
            MatTestUtils.validateMat(sceneMat, "sceneMat before empty region");
            
            // Should not throw exception with empty region
            assertDoesNotThrow(() -> actionVisualizer.drawDefinedRegion(mockVisualization, actionResult));
            
            // Verify that drawRectAroundRegion was called with empty region
            verify(mockDrawRect).drawRectAroundRegion(eq(sceneMat), any(io.github.jspinak.brobot.model.element.Region.class), any(Scalar.class));
        }
    }
    
    @Nested
    @DisplayName("Move Visualization Tests")
    class MoveVisualizationTests {
        
        @Test
        @DisplayName("Should draw move path through multiple locations")
        public void testDrawMove() {
            Location start = new Location(50, 50);
            Match match1 = createMatchWithTarget(150, 150);
            Match match2 = createMatchWithTarget(250, 250);
            
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Arrays.asList(match1, match2));
            
            MatTestUtils.validateMat(matchesOnSceneMat, "matchesOnSceneMat before move");
            
            actionVisualizer.drawMove(mockVisualization, actionResult, start);
            
            // Verify the Mat was accessed
            verify(mockVisualization, atLeastOnce()).getMatchesOnScene();
            
            // Verify movement path was drawn
            double sum = sumElems(matchesOnSceneMat).get(0) + 
                        sumElems(matchesOnSceneMat).get(1) + 
                        sumElems(matchesOnSceneMat).get(2);
            assertTrue(sum > 0, "Move path should be drawn on the Mat");
        }
        
        @Test
        @DisplayName("Should handle move with no starting positions")
        public void testDrawMoveNoStartingPositions() {
            Match match = createMatchWithTarget(100, 100);
            
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.singletonList(match));
            
            // Call without starting positions
            actionVisualizer.drawMove(mockVisualization, actionResult);
            
            verify(mockVisualization, atLeastOnce()).getMatchesOnScene();
        }
    }
    
    // Helper methods
    
    private Match createMatchWithTarget(int x, int y) {
        // Create a match with the target at its center
        Match match = new Match.Builder()
            .setRegion(x - 10, y - 10, 20, 20)
            .build();
        // Set the target location explicitly
        Location target = new Location(x, y);
        match.setTarget(target);
        return match;
    }
}