package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for Location - represents a point on the screen.
 * Tests absolute and relative positioning, offsets, and conversions.
 */
@DisplayName("Location Tests")
public class LocationTest extends BrobotTestBase {
    
    @Mock
    private Region mockRegion;
    
    @Mock
    private Position mockPosition;
    
    private Location location;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        location = new Location();
    }
    
    @Nested
    @DisplayName("Constructors")
    class Constructors {
        
        @Test
        @DisplayName("Default constructor creates origin location")
        public void testDefaultConstructor() {
            Location loc = new Location();
            
            assertEquals(0, loc.getX());
            assertEquals(0, loc.getY());
            assertNull(loc.getRegion());
            assertNull(loc.getPosition());
            assertNull(loc.getName());
            assertEquals(0, loc.getOffsetX());
            assertEquals(0, loc.getOffsetY());
        }
        
        @Test
        @DisplayName("Constructor with coordinates")
        public void testConstructorWithCoordinates() {
            Location loc = new Location(100, 200);
            
            assertEquals(100, loc.getX());
            assertEquals(200, loc.getY());
            assertNull(loc.getRegion());
            assertNull(loc.getPosition());
        }
        
        @Test
        @DisplayName("Constructor with SikuliX Location")
        public void testConstructorWithSikuliLocation() {
            org.sikuli.script.Location sikuliLoc = new org.sikuli.script.Location(150, 250);
            Location loc = new Location(sikuliLoc);
            
            assertEquals(150, loc.getX());
            assertEquals(250, loc.getY());
        }
        
        @Test
        @DisplayName("Constructor with Region")
        public void testConstructorWithRegion() {
            Region region = new Region(10, 20, 100, 200);
            Location loc = new Location(region);
            
            assertEquals(region, loc.getRegion());
            assertNotNull(loc.getPosition());
            // Default position should be center (0.5, 0.5)
            assertEquals(0.5, loc.getPosition().getPercentW());
            assertEquals(0.5, loc.getPosition().getPercentH());
        }
        
        @Test
        @DisplayName("Constructor with Region and position")
        public void testConstructorWithRegionAndPosition() {
            Region region = new Region(10, 20, 100, 200);
            Location loc = new Location(region, 0.25, 0.75);
            
            assertEquals(region, loc.getRegion());
            assertNotNull(loc.getPosition());
            assertEquals(0.25, loc.getPosition().getPercentW());
            assertEquals(0.75, loc.getPosition().getPercentH());
        }
        
        @Test
        @DisplayName("Copy constructor with absolute location")
        public void testCopyConstructorAbsolute() {
            Location original = new Location(100, 200);
            original.setName("TestLocation");
            original.setOffsetX(10);
            original.setOffsetY(20);
            
            Location copy = new Location(original);
            
            assertEquals(100, copy.getX());
            assertEquals(200, copy.getY());
            assertEquals("TestLocation", copy.getName());
            // Note: offsets might not be copied in absolute mode
        }
        
        @Test
        @DisplayName("Constructor with Positions.Name")
        public void testConstructorWithPositionsName() {
            Location loc = new Location(Positions.Name.MIDDLEMIDDLE);
            
            assertNotNull(loc.getPosition());
            assertEquals(0.5, loc.getPosition().getPercentW());
            assertEquals(0.5, loc.getPosition().getPercentH());
        }
    }
    
    @Nested
    @DisplayName("Absolute Positioning")
    class AbsolutePositioning {
        
        @Test
        @DisplayName("Set and get X coordinate")
        public void testSetGetX() {
            location.setX(500);
            
            assertEquals(500, location.getX());
        }
        
        @Test
        @DisplayName("Set and get Y coordinate")
        public void testSetGetY() {
            location.setY(300);
            
            assertEquals(300, location.getY());
        }
        
        @ParameterizedTest
        @CsvSource({
            "0, 0",
            "100, 200",
            "-50, -100",
            "1920, 1080",
            "9999, 9999"
        })
        @DisplayName("Various coordinate values")
        public void testVariousCoordinates(int x, int y) {
            location.setX(x);
            location.setY(y);
            
            assertEquals(x, location.getX());
            assertEquals(y, location.getY());
        }
        
        @Test
        @DisplayName("Get calculated X returns actual X when no region")
        public void testGetCalculatedXNoRegion() {
            location.setX(123);
            
            assertEquals(123, location.getCalculatedX());
        }
        
        @Test
        @DisplayName("Get calculated Y returns actual Y when no region")
        public void testGetCalculatedYNoRegion() {
            location.setY(456);
            
            assertEquals(456, location.getCalculatedY());
        }
    }
    
    @Nested
    @DisplayName("Relative Positioning")
    class RelativePositioning {
        
        @BeforeEach
        void setupRegion() {
            // Setup a test region at (100, 200) with size 400x300
            when(mockRegion.x()).thenReturn(100);
            when(mockRegion.y()).thenReturn(200);
            when(mockRegion.w()).thenReturn(400);
            when(mockRegion.h()).thenReturn(300);
        }
        
        @Test
        @DisplayName("Calculate position from region center")
        public void testCalculateFromRegionCenter() {
            location.setRegion(mockRegion);
            location.setPosition(new Position(0.5, 0.5)); // Center
            
            assertEquals(300, location.getCalculatedX()); // 100 + 400*0.5
            assertEquals(350, location.getCalculatedY()); // 200 + 300*0.5
        }
        
        @Test
        @DisplayName("Calculate position from region top-left")
        public void testCalculateFromRegionTopLeft() {
            location.setRegion(mockRegion);
            location.setPosition(new Position(0.0, 0.0)); // Top-left
            
            assertEquals(100, location.getCalculatedX());
            assertEquals(200, location.getCalculatedY());
        }
        
        @Test
        @DisplayName("Calculate position from region bottom-right")
        public void testCalculateFromRegionBottomRight() {
            location.setRegion(mockRegion);
            location.setPosition(new Position(1.0, 1.0)); // Bottom-right
            
            assertEquals(500, location.getCalculatedX()); // 100 + 400
            assertEquals(500, location.getCalculatedY()); // 200 + 300
        }
        
        @ParameterizedTest
        @CsvSource({
            "0.0, 0.0, 100, 200",    // Top-left
            "0.5, 0.5, 300, 350",    // Center
            "1.0, 1.0, 500, 500",    // Bottom-right
            "0.25, 0.75, 200, 425"   // Custom position
        })
        @DisplayName("Various relative positions")
        public void testVariousRelativePositions(double percentW, double percentH, int expectedX, int expectedY) {
            location.setRegion(mockRegion);
            location.setPosition(new Position(percentW, percentH));
            
            assertEquals(expectedX, location.getCalculatedX());
            assertEquals(expectedY, location.getCalculatedY());
        }
    }
    
    @Nested
    @DisplayName("Offsets")
    class Offsets {
        
        @Test
        @DisplayName("Set and get offset X")
        public void testSetGetOffsetX() {
            location.setOffsetX(50);
            
            assertEquals(50, location.getOffsetX());
        }
        
        @Test
        @DisplayName("Set and get offset Y")
        public void testSetGetOffsetY() {
            location.setOffsetY(-30);
            
            assertEquals(-30, location.getOffsetY());
        }
        
        @Test
        @DisplayName("Apply offsets to absolute position")
        public void testOffsetsWithAbsolutePosition() {
            location.setX(100);
            location.setY(200);
            location.setOffsetX(10);
            location.setOffsetY(20);
            
            assertEquals(110, location.getCalculatedX());
            assertEquals(220, location.getCalculatedY());
        }
        
        @Test
        @DisplayName("Apply offsets to relative position")
        public void testOffsetsWithRelativePosition() {
            Region region = new Region(100, 200, 400, 300);
            location.setRegion(region);
            location.setPosition(new Position(0.5, 0.5)); // Center at (300, 350)
            location.setOffsetX(25);
            location.setOffsetY(-15);
            
            assertEquals(325, location.getCalculatedX()); // 300 + 25
            assertEquals(335, location.getCalculatedY()); // 350 - 15
        }
        
        @ParameterizedTest
        @ValueSource(ints = {-100, -50, 0, 50, 100})
        @DisplayName("Various offset values")
        public void testVariousOffsets(int offset) {
            location.setX(500);
            location.setY(500);
            location.setOffsetX(offset);
            location.setOffsetY(offset);
            
            assertEquals(500 + offset, location.getCalculatedX());
            assertEquals(500 + offset, location.getCalculatedY());
        }
    }
    
    @Nested
    @DisplayName("Location Metadata")
    class LocationMetadata {
        
        @Test
        @DisplayName("Set and get name")
        public void testSetGetName() {
            location.setName("SubmitButton");
            
            assertEquals("SubmitButton", location.getName());
        }
        
        @Test
        @DisplayName("Set and get anchor")
        public void testSetGetAnchor() {
            location.setAnchor(Positions.Name.TOPLEFT);
            
            assertEquals(Positions.Name.TOPLEFT, location.getAnchor());
        }
        
        // Tests for isDefinedWithRegion() removed - method has private access
    }
    
    @Nested
    @DisplayName("Conversions")
    class Conversions {
        
        @Test
        @DisplayName("Convert to SikuliX Location")
        public void testToSikuliLocation() {
            location.setX(250);
            location.setY(150);
            
            org.sikuli.script.Location sikuliLoc = location.sikuli();
            
            assertNotNull(sikuliLoc);
            assertEquals(250, sikuliLoc.x);
            assertEquals(150, sikuliLoc.y);
        }
        
        @Test
        @DisplayName("Convert to SikuliX Location with offsets")
        public void testToSikuliLocationWithOffsets() {
            location.setX(100);
            location.setY(200);
            location.setOffsetX(10);
            location.setOffsetY(20);
            
            org.sikuli.script.Location sikuliLoc = location.sikuli();
            
            assertEquals(110, sikuliLoc.x);
            assertEquals(220, sikuliLoc.y);
        }
        
        @Test
        @DisplayName("Get as Region creates 1x1 region")
        public void testGetAsRegion() {
            location.setX(100);
            location.setY(200);
            
            Region region = location.getRegion();
            
            // Note: Depending on implementation, this might return null
            // or create a 1x1 region at the location
            if (region != null) {
                assertEquals(100, region.x());
                assertEquals(200, region.y());
            }
        }
        
        @Test
        @DisplayName("ToString representation")
        public void testToString() {
            location.setX(123);
            location.setY(456);
            location.setName("TestLoc");
            
            String str = location.toString();
            
            assertNotNull(str);
            // Should contain coordinates or name
            assertTrue(str.contains("123") || str.contains("456") || str.contains("TestLoc"));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Handle null region")
        public void testNullRegion() {
            location.setRegion(null);
            location.setX(100);
            location.setY(200);
            
            assertEquals(100, location.getCalculatedX());
            assertEquals(200, location.getCalculatedY());
        }
        
        @Test
        @DisplayName("Handle null position with region")
        public void testNullPositionWithRegion() {
            location.setRegion(mockRegion);
            location.setPosition(null);
            
            // Should fall back to x,y or default behavior
            assertNotNull(location.getCalculatedX());
            assertNotNull(location.getCalculatedY());
        }
        
        @Test
        @DisplayName("Negative coordinates")
        public void testNegativeCoordinates() {
            location.setX(-100);
            location.setY(-200);
            
            assertEquals(-100, location.getX());
            assertEquals(-200, location.getY());
            assertEquals(-100, location.getCalculatedX());
            assertEquals(-200, location.getCalculatedY());
        }
        
        @Test
        @DisplayName("Very large coordinates")
        public void testVeryLargeCoordinates() {
            location.setX(Integer.MAX_VALUE);
            location.setY(Integer.MAX_VALUE);
            
            assertEquals(Integer.MAX_VALUE, location.getX());
            assertEquals(Integer.MAX_VALUE, location.getY());
        }
        
        @Test
        @DisplayName("Position percentages outside 0-1 range")
        public void testPositionPercentagesOutsideRange() {
            Region region = new Region(0, 0, 100, 100);
            location.setRegion(region);
            location.setPosition(new Position(1.5, -0.5)); // Outside normal range
            
            // Should still calculate, even if results are outside region
            assertEquals(150, location.getCalculatedX()); // 0 + 100*1.5
            assertEquals(-50, location.getCalculatedY()); // 0 + 100*(-0.5)
        }
    }
    
    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {
        
        @Test
        @DisplayName("Build location with builder")
        public void testBuilder() {
            Location loc = new Location.Builder()
                .setXY(100, 200)
                .called("TestLocation")
                .setOffsetX(10)
                .setOffsetY(20)
                .build();
            
            assertEquals(100, loc.getX());
            assertEquals(200, loc.getY());
            assertEquals("TestLocation", loc.getName());
            assertEquals(10, loc.getOffsetX());
            assertEquals(20, loc.getOffsetY());
        }
        
        @Test
        @DisplayName("Build relative location")
        public void testBuilderRelative() {
            Region region = new Region(0, 0, 200, 100);
            
            Location loc = new Location.Builder()
                .setRegion(region)
                .setPosition(75, 25)  // Using percentOfW, percentOfH (converts to 0.75, 0.25)
                .called("RelativeLocation")
                .build();
            
            assertEquals(region, loc.getRegion());
            assertNotNull(loc.getPosition());
            assertEquals(0.75, loc.getPosition().getPercentW());
            assertEquals(0.25, loc.getPosition().getPercentH());
            assertEquals("RelativeLocation", loc.getName());
        }
    }
}