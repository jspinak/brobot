package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Rect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for Region - represents a rectangular area on the screen.
 * Tests constructors, geometric operations, conversions, and region arithmetic.
 */
@DisplayName("Region Tests")
public class RegionTest extends BrobotTestBase {
    
    @Mock
    private Location mockLocation;
    
    @Mock
    private Match mockMatch;
    
    @Mock
    private org.sikuli.script.Match mockSikuliMatch;
    
    @Mock
    private org.sikuli.script.Region mockSikuliRegion;
    
    @Mock
    private org.sikuli.script.Location mockSikuliLocation;
    
    private Region region;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        region = new Region();
    }
    
    @Nested
    @DisplayName("Constructors")
    class Constructors {
        
        @Test
        @DisplayName("Default constructor creates screen-sized region")
        public void testDefaultConstructor() {
            Region r = new Region();
            
            assertEquals(0, r.getX());
            assertEquals(0, r.getY());
            assertTrue(r.getW() > 0, "Width should be positive");
            assertTrue(r.getH() > 0, "Height should be positive");
        }
        
        @Test
        @DisplayName("Constructor with coordinates")
        public void testCoordinateConstructor() {
            Region r = new Region(10, 20, 100, 50);
            
            assertEquals(10, r.getX());
            assertEquals(20, r.getY());
            assertEquals(100, r.getW());
            assertEquals(50, r.getH());
        }
        
        @Test
        @DisplayName("Constructor from two Locations")
        public void testTwoLocationConstructor() {
            Location loc1 = new Location(10, 20);
            Location loc2 = new Location(90, 70);
            Region r = new Region(loc1, loc2);
            
            assertEquals(10, r.getX());
            assertEquals(20, r.getY());
            assertEquals(80, r.getW()); // 90 - 10
            assertEquals(50, r.getH()); // 70 - 20
        }
        
        @Test
        @DisplayName("Constructor from Match")
        public void testMatchConstructor() {
            when(mockMatch.x()).thenReturn(30);
            when(mockMatch.y()).thenReturn(40);
            when(mockMatch.w()).thenReturn(70);
            when(mockMatch.h()).thenReturn(90);
            
            Region r = new Region(mockMatch);
            
            assertEquals(30, r.getX());
            assertEquals(40, r.getY());
            assertEquals(70, r.getW());
            assertEquals(90, r.getH());
        }
        
        @Test
        @DisplayName("Constructor from SikuliX Match")
        public void testSikuliMatchConstructor() {
            when(mockSikuliMatch.x).thenReturn(15);
            when(mockSikuliMatch.y).thenReturn(25);
            when(mockSikuliMatch.w).thenReturn(85);
            when(mockSikuliMatch.h).thenReturn(65);
            
            Region r = new Region(mockSikuliMatch);
            
            assertEquals(15, r.getX());
            assertEquals(25, r.getY());
            assertEquals(85, r.getW());
            assertEquals(65, r.getH());
        }
        
        @Test
        @DisplayName("Copy constructor")
        public void testCopyConstructor() {
            Region original = new Region(5, 10, 50, 30);
            Region copy = new Region(original);
            
            assertEquals(original.getX(), copy.getX());
            assertEquals(original.getY(), copy.getY());
            assertEquals(original.getW(), copy.getW());
            assertEquals(original.getH(), copy.getH());
            assertNotSame(original, copy);
        }
        
        @Test
        @DisplayName("Constructor from OpenCV Rect")
        public void testRectConstructor() {
            Rect rect = new Rect(20, 30, 80, 60);
            Region r = new Region(rect);
            
            assertEquals(20, r.getX());
            assertEquals(30, r.getY());
            assertEquals(80, r.getW());
            assertEquals(60, r.getH());
        }
    }
    
    @Nested
    @DisplayName("Accessor Methods")
    class AccessorMethods {
        
        @Test
        @DisplayName("x() method returns x coordinate")
        public void testXMethod() {
            Region r = new Region(15, 25, 100, 50);
            assertEquals(15, r.x());
        }
        
        @Test
        @DisplayName("y() method returns y coordinate")
        public void testYMethod() {
            Region r = new Region(15, 25, 100, 50);
            assertEquals(25, r.y());
        }
        
        @Test
        @DisplayName("w() method returns width")
        public void testWMethod() {
            Region r = new Region(15, 25, 100, 50);
            assertEquals(100, r.w());
        }
        
        @Test
        @DisplayName("h() method returns height")
        public void testHMethod() {
            Region r = new Region(15, 25, 100, 50);
            assertEquals(50, r.h());
        }
        
        @Test
        @DisplayName("x2() returns right edge")
        public void testX2Method() {
            Region r = new Region(10, 20, 100, 50);
            assertEquals(110, r.x2()); // x + w
        }
        
        @Test
        @DisplayName("y2() returns bottom edge")
        public void testY2Method() {
            Region r = new Region(10, 20, 100, 50);
            assertEquals(70, r.y2()); // y + h
        }
    }
    
    @Nested
    @DisplayName("Adjustment Methods")
    class AdjustmentMethods {
        
        @Test
        @DisplayName("Adjust all coordinates")
        public void testAdjust() {
            Region r = new Region(10, 20, 100, 50);
            r.adjust(5, -5, 10, -10);
            
            assertEquals(15, r.getX());
            assertEquals(15, r.getY());
            assertEquals(110, r.getW());
            assertEquals(40, r.getH());
        }
        
        @Test
        @DisplayName("Adjust X coordinate")
        public void testAdjustX() {
            Region r = new Region(10, 20, 100, 50);
            r.adjustX(25);
            
            assertEquals(25, r.getX());
            assertEquals(85, r.getW()); // width adjusted to maintain x2
        }
        
        @Test
        @DisplayName("Adjust Y coordinate")
        public void testAdjustY() {
            Region r = new Region(10, 20, 100, 50);
            r.adjustY(30);
            
            assertEquals(30, r.getY());
            assertEquals(40, r.getH()); // height adjusted to maintain y2
        }
        
        @Test
        @DisplayName("Adjust X2 (right edge)")
        public void testAdjustX2() {
            Region r = new Region(10, 20, 100, 50);
            r.adjustX2(120);
            
            assertEquals(10, r.getX());
            assertEquals(110, r.getW()); // 120 - 10
        }
        
        @Test
        @DisplayName("Adjust Y2 (bottom edge)")
        public void testAdjustY2() {
            Region r = new Region(10, 20, 100, 50);
            r.adjustY2(80);
            
            assertEquals(20, r.getY());
            assertEquals(60, r.getH()); // 80 - 20
        }
    }
    
    @Nested
    @DisplayName("Contains and Overlaps")
    class ContainsOverlaps {
        
        @Test
        @DisplayName("Region contains Location")
        public void testContainsLocation() {
            Region r = new Region(10, 10, 50, 50);
            
            assertTrue(r.contains(new Location(30, 30)));
            assertTrue(r.contains(new Location(10, 10))); // Top-left corner
            assertFalse(r.contains(new Location(5, 5)));
            assertFalse(r.contains(new Location(70, 70)));
        }
        
        @Test
        @DisplayName("Region contains another Region")
        public void testContainsRegion() {
            Region r1 = new Region(10, 10, 100, 100);
            Region r2 = new Region(20, 20, 30, 30);
            Region r3 = new Region(5, 5, 20, 20);
            
            assertTrue(r1.contains(r2));
            assertFalse(r1.contains(r3));
            assertFalse(r2.contains(r1));
        }
        
        @Test
        @DisplayName("Region contains Match")
        public void testContainsMatch() {
            Region r = new Region(10, 10, 100, 100);
            
            when(mockMatch.x()).thenReturn(20);
            when(mockMatch.y()).thenReturn(20);
            when(mockMatch.w()).thenReturn(30);
            when(mockMatch.h()).thenReturn(30);
            
            assertTrue(r.contains(mockMatch));
        }
        
        @Test
        @DisplayName("Region contains SikuliX Match")
        public void testContainsSikuliMatch() {
            Region r = new Region(10, 10, 100, 100);
            
            when(mockSikuliMatch.x).thenReturn(20);
            when(mockSikuliMatch.y).thenReturn(20);
            when(mockSikuliMatch.w).thenReturn(30);
            when(mockSikuliMatch.h).thenReturn(30);
            
            assertTrue(r.contains(mockSikuliMatch));
        }
        
        @Test
        @DisplayName("Region contains SikuliX Location")
        public void testContainsSikuliLocation() {
            Region r = new Region(10, 10, 50, 50);
            
            when(mockSikuliLocation.x).thenReturn(30);
            when(mockSikuliLocation.y).thenReturn(30);
            assertTrue(r.contains(mockSikuliLocation));
            
            when(mockSikuliLocation.x).thenReturn(70);
            when(mockSikuliLocation.y).thenReturn(70);
            assertFalse(r.contains(mockSikuliLocation));
        }
        
        @Test
        @DisplayName("Regions overlap")
        public void testOverlaps() {
            Region r1 = new Region(10, 10, 50, 50);
            Region r2 = new Region(30, 30, 50, 50);
            Region r3 = new Region(70, 70, 20, 20);
            
            assertTrue(r1.overlaps(r2));
            assertFalse(r1.overlaps(r3));
        }
        
        @Test
        @DisplayName("Region overlaps with Rect")
        public void testOverlapsRect() {
            Region r = new Region(10, 10, 50, 50);
            Rect overlapping = new Rect(30, 30, 50, 50);
            Rect nonOverlapping = new Rect(70, 70, 20, 20);
            
            assertTrue(r.overlaps(overlapping));
            assertFalse(r.overlaps(nonOverlapping));
        }
    }
    
    @Nested
    @DisplayName("Conversions")
    class Conversions {
        
        @Test
        @DisplayName("Convert to SikuliX Region")
        public void testToSikuli() {
            Region r = new Region(15, 25, 80, 60);
            org.sikuli.script.Region sikuliRegion = r.sikuli();
            
            assertNotNull(sikuliRegion);
            assertEquals(15, sikuliRegion.x);
            assertEquals(25, sikuliRegion.y);
            assertEquals(80, sikuliRegion.w);
            assertEquals(60, sikuliRegion.h);
        }
        
        @Test
        @DisplayName("Convert to Match")
        public void testToMatch() {
            Region r = new Region(20, 30, 70, 40);
            Match match = r.toMatch();
            
            assertNotNull(match);
            assertEquals(20, match.x());
            assertEquals(30, match.y());
            assertEquals(70, match.w());
            assertEquals(40, match.h());
        }
        
        @Test
        @DisplayName("Convert to StateRegion in null state")
        public void testInNullState() {
            Region r = new Region(10, 20, 100, 50);
            StateRegion stateRegion = r.inNullState();
            
            assertNotNull(stateRegion);
            assertNotNull(stateRegion.getSearchRegion());
            assertEquals(10, stateRegion.getSearchRegion().x());
            assertEquals(20, stateRegion.getSearchRegion().y());
        }
        
        @Test
        @DisplayName("Convert to ObjectCollection")
        public void testAsObjectCollection() {
            Region r = new Region(10, 20, 100, 50);
            var objectCollection = r.asObjectCollection();
            
            assertNotNull(objectCollection);
            assertNotNull(objectCollection.getStateRegions());
            assertEquals(1, objectCollection.getStateRegions().size());
        }
    }
    
    @Nested
    @DisplayName("Comparison and Equality")
    class ComparisonEquality {
        
        @Test
        @DisplayName("Compare regions by area")
        public void testCompareTo() {
            Region small = new Region(0, 0, 10, 10);
            Region medium = new Region(0, 0, 20, 20);
            Region large = new Region(0, 0, 30, 30);
            
            assertTrue(small.compareTo(medium) < 0);
            assertTrue(large.compareTo(medium) > 0);
            assertEquals(0, medium.compareTo(new Region(10, 10, 20, 20)));
        }
        
        @Test
        @DisplayName("Equals method")
        public void testEquals() {
            Region r1 = new Region(10, 20, 30, 40);
            Region r2 = new Region(10, 20, 30, 40);
            Region r3 = new Region(10, 20, 30, 41);
            
            assertTrue(r1.equals(r2));
            assertFalse(r1.equals(r3));
        }
        
        @Test
        @DisplayName("Size calculation")
        public void testSize() {
            Region r = new Region(10, 10, 100, 50);
            assertEquals(5000, r.size()); // w * h
        }
    }
    
    @Nested
    @DisplayName("Grid Operations")
    class GridOperations {
        
        @Test
        @DisplayName("Get grid regions")
        public void testGetGridRegions() {
            Region r = new Region(0, 0, 100, 100);
            List<Region> gridRegions = r.getGridRegions(2, 2);
            
            assertEquals(4, gridRegions.size());
            
            // Top-left
            assertEquals(0, gridRegions.get(0).getX());
            assertEquals(0, gridRegions.get(0).getY());
            assertEquals(50, gridRegions.get(0).getW());
            assertEquals(50, gridRegions.get(0).getH());
            
            // Top-right
            assertEquals(50, gridRegions.get(1).getX());
            assertEquals(0, gridRegions.get(1).getY());
            
            // Bottom-left
            assertEquals(0, gridRegions.get(2).getX());
            assertEquals(50, gridRegions.get(2).getY());
            
            // Bottom-right
            assertEquals(50, gridRegions.get(3).getX());
            assertEquals(50, gridRegions.get(3).getY());
        }
        
        @Test
        @DisplayName("Get grid number from location")
        public void testGetGridNumber() {
            Region r = new Region(0, 0, 100, 100);
            
            Optional<Integer> gridNum = r.getGridNumber(new Location(25, 25));
            assertTrue(gridNum.isPresent());
            assertEquals(0, gridNum.get()); // Top-left grid
            
            gridNum = r.getGridNumber(new Location(75, 75));
            assertTrue(gridNum.isPresent());
            assertEquals(11, gridNum.get()); // Bottom-right in 3x4 grid
        }
        
        @Test
        @DisplayName("Get grid region by number")
        public void testGetGridRegionByNumber() {
            Region r = new Region(0, 0, 120, 120);
            Region gridRegion = r.getGridRegion(0);
            
            assertNotNull(gridRegion);
            assertEquals(0, gridRegion.getX());
            assertEquals(0, gridRegion.getY());
            assertEquals(40, gridRegion.getW()); // 120/3
            assertEquals(30, gridRegion.getH()); // 120/4
        }
        
        @Test
        @DisplayName("Get grid region from location")
        public void testGetGridRegionFromLocation() {
            Region r = new Region(0, 0, 100, 100);
            Optional<Region> gridRegion = r.getGridRegion(new Location(25, 25));
            
            assertTrue(gridRegion.isPresent());
            Region grid = gridRegion.get();
            assertTrue(grid.contains(new Location(25, 25)));
        }
    }
    
    @Nested
    @DisplayName("Special Methods")
    class SpecialMethods {
        
        @Test
        @DisplayName("Get random location")
        public void testGetRandomLocation() {
            Region r = new Region(10, 20, 100, 50);
            Location randomLoc = r.getRandomLocation();
            
            assertNotNull(randomLoc);
            assertTrue(r.contains(randomLoc));
        }
        
        @Test
        @DisplayName("Is defined")
        public void testIsDefined() {
            Region defined = new Region(10, 10, 50, 50);
            assertTrue(defined.isDefined());
            
            Region undefined = new Region(0, 0, 0, 0);
            assertFalse(undefined.isDefined());
        }
        
        @Test
        @DisplayName("Set to Match")
        public void testSetToMatch() {
            Region r = new Region();
            when(mockMatch.x()).thenReturn(30);
            when(mockMatch.y()).thenReturn(40);
            when(mockMatch.w()).thenReturn(70);
            when(mockMatch.h()).thenReturn(90);
            
            r.setTo(mockMatch);
            
            assertEquals(30, r.getX());
            assertEquals(40, r.getY());
            assertEquals(70, r.getW());
            assertEquals(90, r.getH());
        }
        
        @Test
        @DisplayName("Set to Region")
        public void testSetToRegion() {
            Region r = new Region();
            Region source = new Region(15, 25, 85, 65);
            
            r.setTo(source);
            
            assertEquals(15, r.getX());
            assertEquals(25, r.getY());
            assertEquals(85, r.getW());
            assertEquals(65, r.getH());
        }
        
        @Test
        @DisplayName("Set XYWH")
        public void testSetXYWH() {
            Region r = new Region();
            r.setXYWH(12, 23, 88, 67);
            
            assertEquals(12, r.getX());
            assertEquals(23, r.getY());
            assertEquals(88, r.getW());
            assertEquals(67, r.getH());
        }
    }
    
    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {
        
        @Test
        @DisplayName("Builder creates region with all properties")
        public void testBuilder() {
            Region r = Region.builder()
                .withRegion(10, 20, 100, 50)
                .build();
            
            assertEquals(10, r.getX());
            assertEquals(20, r.getY());
            assertEquals(100, r.getW());
            assertEquals(50, r.getH());
        }
    }
    
    @Nested
    @DisplayName("String Representation")
    class StringRepresentation {
        
        @Test
        @DisplayName("toString format")
        public void testToString() {
            Region r = new Region(10, 20, 100, 50);
            String str = r.toString();
            
            assertNotNull(str);
            assertTrue(str.contains("10"));
            assertTrue(str.contains("20"));
            assertTrue(str.contains("100"));
            assertTrue(str.contains("50"));
        }
    }
}