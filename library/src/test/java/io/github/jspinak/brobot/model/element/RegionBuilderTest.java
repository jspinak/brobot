package io.github.jspinak.brobot.model.element;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static io.github.jspinak.brobot.model.element.Positions.Name.*;

/**
 * Tests for RegionBuilder functionality.
 */
public class RegionBuilderTest {
    
    private RegionBuilder builder;
    
    @BeforeEach
    public void setUp() {
        builder = new RegionBuilder();
    }
    
    @Test
    public void testBasicRegionCreation() {
        Region region = builder
            .withRegion(100, 100, 200, 150)
            .build();
        
        assertEquals(100, region.getX());
        assertEquals(100, region.getY());
        assertEquals(200, region.getW());
        assertEquals(150, region.getH());
    }
    
    @Test
    public void testPositionAndSizeSeparately() {
        Region region = builder
            .withPosition(50, 75)
            .withSize(300, 200)
            .build();
        
        assertEquals(50, region.getX());
        assertEquals(75, region.getY());
        assertEquals(300, region.getW());
        assertEquals(200, region.getH());
    }
    
    @Test
    public void testAdjustments() {
        Region region = builder
            .withRegion(100, 100, 200, 150)
            .adjustX(10)
            .adjustY(-20)
            .adjustWidth(50)
            .adjustHeight(30)
            .build();
        
        assertEquals(110, region.getX());
        assertEquals(80, region.getY());
        assertEquals(250, region.getW());
        assertEquals(180, region.getH());
    }
    
    @Test
    public void testExpand() {
        Region region = builder
            .withRegion(100, 100, 200, 150)
            .expand(10)
            .build();
        
        assertEquals(90, region.getX());
        assertEquals(90, region.getY());
        assertEquals(220, region.getW());
        assertEquals(170, region.getH());
    }
    
    @Test
    public void testFromExistingRegion() {
        Region original = new Region(50, 50, 100, 100);
        Region modified = builder
            .fromRegion(original)
            .adjustWidth(50)
            .adjustHeight(50)
            .build();
        
        assertEquals(50, modified.getX());
        assertEquals(50, modified.getY());
        assertEquals(150, modified.getW());
        assertEquals(150, modified.getH());
    }
    
    @Test
    public void testScreenPercentage() {
        // Assuming screen size is detected (will vary by system)
        // This test demonstrates the API usage
        Region region = builder
            .withScreenPercentage(0.25, 0.25, 0.5, 0.5)
            .build();
        
        assertNotNull(region);
        assertTrue(region.getW() > 0);
        assertTrue(region.getH() > 0);
    }
    
    @Test
    public void testDirectCoordinates() {
        // Test direct coordinate specification
        Region region = builder
            .withPosition(100, 100)
            .withSize(200, 150)
            .build();
        
        assertNotNull(region);
        assertEquals(100, region.getX());
        assertEquals(100, region.getY());
        assertEquals(200, region.getW());
        assertEquals(150, region.getH());
    }
    
    @Test
    public void testPredefinedRegions() {
        // Test top half
        Region topHalf = new RegionBuilder().topHalf().build();
        assertEquals(0, topHalf.getX());
        assertEquals(0, topHalf.getY());
        assertTrue(topHalf.getW() > 0);
        assertTrue(topHalf.getH() > 0);
        
        // Test bottom half
        Region bottomHalf = new RegionBuilder().bottomHalf().build();
        assertEquals(0, bottomHalf.getX());
        assertTrue(bottomHalf.getY() > 0);
        assertTrue(bottomHalf.getW() > 0);
        assertTrue(bottomHalf.getH() > 0);
        
        // Test left half
        Region leftHalf = new RegionBuilder().leftHalf().build();
        assertEquals(0, leftHalf.getX());
        assertEquals(0, leftHalf.getY());
        assertTrue(leftHalf.getW() > 0);
        assertTrue(leftHalf.getH() > 0);
        
        // Test right half
        Region rightHalf = new RegionBuilder().rightHalf().build();
        assertTrue(rightHalf.getX() > 0);
        assertEquals(0, rightHalf.getY());
        assertTrue(rightHalf.getW() > 0);
        assertTrue(rightHalf.getH() > 0);
    }
    
    @Test
    public void testCenterOnScreen() {
        Region centered = builder
            .withSize(200, 150)
            .centerOnScreen()
            .build();
        
        assertNotNull(centered);
        // Should be centered, so X and Y should be greater than 0
        assertTrue(centered.getX() >= 0);
        assertTrue(centered.getY() >= 0);
        assertEquals(200, centered.getW());
        assertEquals(150, centered.getH());
    }
    
    @Test
    public void testConstrainToScreen() {
        // Try to create a region that goes off-screen
        Region constrained = builder
            .withPosition(10000, 10000)  // Way off screen
            .withSize(200, 150)
            .constrainToScreen(true)
            .build();
        
        assertNotNull(constrained);
        assertTrue(constrained.getX() >= 0);
        assertTrue(constrained.getY() >= 0);
    }
    
    @Test
    public void testPositionIntegration() {
        // Test using Position for placement
        Position customPosition = new Position(0.3, 0.7);
        Region positioned = builder
            .withSize(200, 150)
            .withPosition(customPosition)
            .build();
        
        assertNotNull(positioned);
        // Position should be at 30% width, 70% height of screen
        assertTrue(positioned.getX() >= 0);
        assertTrue(positioned.getY() >= 0);
        
        // Test using named position
        Region bottomRight = builder
            .withSize(200, 150)
            .withPosition(BOTTOMRIGHT)
            .build();
        
        assertNotNull(bottomRight);
        assertTrue(bottomRight.getX() > 0);
        assertTrue(bottomRight.getY() > 0);
    }
    
    @Test
    public void testRelativePositioning() {
        // Create a reference region
        Region reference = new Region(100, 100, 400, 300);
        
        // Position a new region at the center of the reference
        Region centered = builder
            .withSize(50, 50)
            .positionRelativeTo(reference, MIDDLEMIDDLE)
            .build();
        
        // Should be at the center of reference region
        assertEquals(300, centered.getX()); // 100 + 400/2
        assertEquals(250, centered.getY()); // 100 + 300/2
        
        // Position at custom point within reference
        Region customPoint = builder
            .withSize(50, 50)
            .positionRelativeTo(reference, new Position(0.25, 0.75))
            .build();
        
        assertEquals(200, customPoint.getX()); // 100 + 400*0.25
        assertEquals(325, customPoint.getY()); // 100 + 300*0.75
    }
    
    @Test
    public void testAnchorWithPositions() {
        // Test anchoring with Positions.Name
        Region anchored = builder
            .withSize(200, 150)
            .withAnchor(MIDDLEMIDDLE)
            .build();
        
        assertNotNull(anchored);
        // Should be centered on screen
        assertTrue(anchored.getX() > 0);
        assertTrue(anchored.getY() > 0);
        
        // Test custom anchor position
        Region customAnchor = builder
            .withSize(200, 150)
            .withAnchor(new Position(0.75, 0.25))
            .build();
        
        assertNotNull(customAnchor);
        assertTrue(customAnchor.getX() > 0);
        assertTrue(customAnchor.getY() >= 0);
    }
    
    @Test
    public void testNamedPositionHelpers() {
        // Test all named position helper methods
        Region topCenter = new RegionBuilder().withSize(100, 100).topCenter().build();
        assertNotNull(topCenter);
        assertEquals(0, topCenter.getY());
        
        Region bottomCenter = new RegionBuilder().withSize(100, 100).bottomCenter().build();
        assertNotNull(bottomCenter);
        assertTrue(bottomCenter.getY() > 0);
        
        Region leftCenter = new RegionBuilder().withSize(100, 100).leftCenter().build();
        assertNotNull(leftCenter);
        assertEquals(0, leftCenter.getX());
        
        Region rightCenter = new RegionBuilder().withSize(100, 100).rightCenter().build();
        assertNotNull(rightCenter);
        assertTrue(rightCenter.getX() > 0);
    }
    
    /**
     * Example usage for documentation.
     */
    @Test
    public void exampleUsage() {
        // Example 1: Create a search area that's always in the top-right corner
        // regardless of screen size
        Region topRightSearch = Region.builder()
            .withScreenPercentageSize(0.3, 0.3)  // 30% of screen size
            .topRight()
            .build();
        
        // Example 2: Create a region with specific coordinates
        Region specificRegion = Region.builder()
            .withPosition(500, 300)
            .withSize(400, 200)
            .build();
        
        // Example 3: Create a centered dialog region
        Region dialogRegion = Region.builder()
            .withSize(600, 400)
            .centerOnScreen()
            .expand(-20)  // Add 20px margin inside
            .build();
        
        // Example 4: Adjust an existing region
        Region original = new Region(100, 100, 200, 150);
        Region adjusted = Region.builder()
            .fromRegion(original)
            .adjustX(50)
            .adjustY(-25)
            .adjustWidth(100)
            .adjustHeight(50)
            .build();
        
        // Example 5: Create a region as percentage of screen
        Region percentageRegion = Region.builder()
            .withScreenPercentage(0.1, 0.1, 0.8, 0.8)  // 10% margins, 80% size
            .build();
        
        // Example 6: Position using Position objects
        Region positionBased = Region.builder()
            .withSize(100, 100)
            .withPosition(new Position(0.7, 0.3))  // 70% right, 30% down
            .build();
        
        // Example 7: Position relative to another region
        Region tooltip = Region.builder()
            .withSize(200, 50)
            .positionRelativeTo(original, TOPMIDDLE)  // Above the original region
            .adjustY(-10)  // 10px gap
            .build();
        
        // Example 8: Use named positions for quick placement
        Region bottomRightButton = Region.builder()
            .withSize(120, 40)
            .bottomRight()
            .adjustX(-20)  // 20px margin from edge
            .adjustY(-20)
            .build();
        
        // All regions should be valid
        assertNotNull(topRightSearch);
        assertNotNull(specificRegion);
        assertNotNull(dialogRegion);
        assertNotNull(adjusted);
        assertNotNull(percentageRegion);
        assertNotNull(positionBased);
        assertNotNull(tooltip);
        assertNotNull(bottomRightButton);
    }
}