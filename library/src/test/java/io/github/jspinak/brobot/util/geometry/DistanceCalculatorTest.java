package io.github.jspinak.brobot.util.geometry;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
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

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for DistanceCalculator - geometric calculations utility.
 * Tests distance, angle, and spatial relationship calculations.
 */
@DisplayName("DistanceCalculator Tests")
public class DistanceCalculatorTest extends BrobotTestBase {
    
    private DistanceCalculator calculator;
    private static final double DELTA = 0.001;
    
    @Mock
    private Match mockMatch1;
    @Mock
    private Match mockMatch2;
    @Mock
    private Region mockRegion1;
    @Mock
    private Region mockRegion2;
    @Mock
    private org.sikuli.script.Region sikuliRegion1;
    @Mock
    private org.sikuli.script.Region sikuliRegion2;
    @Mock
    private org.sikuli.script.Location sikuliCenter1;
    @Mock
    private org.sikuli.script.Location sikuliCenter2;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        calculator = new DistanceCalculator();
        
        // Setup mock matches with regions
        when(mockMatch1.getRegion()).thenReturn(mockRegion1);
        when(mockMatch2.getRegion()).thenReturn(mockRegion2);
        when(mockRegion1.sikuli()).thenReturn(sikuliRegion1);
        when(mockRegion2.sikuli()).thenReturn(sikuliRegion2);
        when(sikuliRegion1.getCenter()).thenReturn(sikuliCenter1);
        when(sikuliRegion2.getCenter()).thenReturn(sikuliCenter2);
    }
    
    @Nested
    @DisplayName("Distance Calculations")
    class DistanceCalculations {
        
        @Test
        @DisplayName("Distance between two locations")
        public void testDistanceBetweenLocations() {
            Location loc1 = new Location(0, 0);
            Location loc2 = new Location(3, 4);
            
            double distance = calculator.getDistance(loc1, loc2);
            
            assertEquals(5.0, distance, DELTA); // 3-4-5 triangle
        }
        
        @Test
        @DisplayName("Euclidean distance")
        public void testEuclideanDistance() {
            Location loc1 = new Location(1, 1);
            Location loc2 = new Location(4, 5);
            
            double distance = calculator.euclidean(loc1, loc2);
            
            assertEquals(5.0, distance, DELTA); // sqrt((4-1)^2 + (5-1)^2) = sqrt(9+16) = 5
        }
        
        @Test
        @DisplayName("Distance to same location is zero")
        public void testDistanceToSelf() {
            Location loc = new Location(100, 200);
            
            double distance = calculator.getDistance(loc, loc);
            
            assertEquals(0.0, distance, DELTA);
        }
        
        @Test
        @DisplayName("Distance between matches")
        public void testDistanceBetweenMatches() {
            when(sikuliCenter1.getX()).thenReturn(100);
            when(sikuliCenter1.getY()).thenReturn(100);
            when(sikuliCenter2.getX()).thenReturn(200);
            when(sikuliCenter2.getY()).thenReturn(200);
            
            double distance = calculator.getDistance(mockMatch1, mockMatch2);
            
            assertEquals(141.421, distance, 0.01); // sqrt(100^2 + 100^2)
        }
        
        @ParameterizedTest
        @CsvSource({
            "0,0,0,0,0.0",
            "0,0,1,0,1.0",
            "0,0,0,1,1.0",
            "0,0,1,1,1.414",
            "-1,-1,1,1,2.828",
            "10,10,13,14,5.0"
        })
        @DisplayName("Various distance calculations")
        public void testVariousDistances(int x1, int y1, int x2, int y2, double expected) {
            Location loc1 = new Location(x1, y1);
            Location loc2 = new Location(x2, y2);
            
            double distance = calculator.getDistance(loc1, loc2);
            
            assertEquals(expected, distance, 0.01);
        }
    }
    
    @Nested
    @DisplayName("Angle Calculations")
    class AngleCalculations {
        
        @Test
        @DisplayName("Angle to East (0 degrees)")
        public void testAngleEast() {
            Location loc1 = new Location(0, 0);
            Location loc2 = new Location(10, 0);
            
            double angle = calculator.getAngle(loc1, loc2);
            
            assertEquals(0.0, angle, DELTA);
        }
        
        @Test
        @DisplayName("Angle to North (90 degrees in screen coordinates)")
        public void testAngleNorth() {
            Location loc1 = new Location(0, 10);
            Location loc2 = new Location(0, 0);
            
            double angle = calculator.getAngle(loc1, loc2);
            
            assertEquals(90.0, angle, DELTA); // Y increases downward
        }
        
        @Test
        @DisplayName("Angle to West (180/-180 degrees)")
        public void testAngleWest() {
            Location loc1 = new Location(10, 0);
            Location loc2 = new Location(0, 0);
            
            double angle = calculator.getAngle(loc1, loc2);
            
            assertEquals(180.0, Math.abs(angle), DELTA);
        }
        
        @Test
        @DisplayName("Angle to South (-90 degrees in screen coordinates)")
        public void testAngleSouth() {
            Location loc1 = new Location(0, 0);
            Location loc2 = new Location(0, 10);
            
            double angle = calculator.getAngle(loc1, loc2);
            
            assertEquals(-90.0, angle, DELTA); // Y increases downward
        }
        
        @Test
        @DisplayName("Angle at 45 degrees")
        public void testAngle45Degrees() {
            Location loc1 = new Location(0, 10);
            Location loc2 = new Location(10, 0);
            
            double angle = calculator.getAngle(loc1, loc2);
            
            assertEquals(45.0, angle, DELTA);
        }
        
        @Test
        @DisplayName("Angle from origin")
        public void testAngleFromOrigin() {
            Location loc = new Location(10, -10); // Screen Y inverted
            
            double angle = calculator.getAngleFrom00(loc);
            
            assertEquals(45.0, angle, DELTA);
        }
        
        @ParameterizedTest
        @CsvSource({
            "0,0,1,0,0.0",
            "0,0,1,1,-45.0",
            "0,0,0,1,-90.0",
            "0,0,-1,1,-135.0",
            "0,0,-1,0,180.0",
            "0,0,-1,-1,135.0",
            "0,0,0,-1,90.0",
            "0,0,1,-1,45.0"
        })
        @DisplayName("Angles in all octants")
        public void testAnglesAllOctants(int x1, int y1, int x2, int y2, double expected) {
            Location loc1 = new Location(x1, y1);
            Location loc2 = new Location(x2, y2);
            
            double angle = calculator.getAngle(loc1, loc2);
            
            assertEquals(expected, angle, DELTA);
        }
    }
    
    @Nested
    @DisplayName("Angle Between Vectors")
    class AngleBetweenVectors {
        
        @Test
        @DisplayName("Angle between perpendicular vectors")
        public void testPerpendicularVectors() {
            Location center = new Location(0, 0);
            Location loc1 = new Location(10, 0);
            Location loc2 = new Location(0, 10);
            
            double angleBetween = calculator.getDegreesBetween(center, loc1, loc2);
            
            assertEquals(90.0, Math.abs(angleBetween), DELTA);
        }
        
        @Test
        @DisplayName("Angle between parallel vectors")
        public void testParallelVectors() {
            Location center = new Location(0, 0);
            Location loc1 = new Location(10, 0);
            Location loc2 = new Location(20, 0);
            
            double angleBetween = calculator.getDegreesBetween(center, loc1, loc2);
            
            assertEquals(0.0, angleBetween, DELTA);
        }
        
        @Test
        @DisplayName("Angle between opposite vectors")
        public void testOppositeVectors() {
            Location center = new Location(0, 0);
            Location loc1 = new Location(10, 0);
            Location loc2 = new Location(-10, 0);
            
            double angleBetween = calculator.getDegreesBetween(center, loc1, loc2);
            
            assertEquals(180.0, Math.abs(angleBetween), DELTA);
        }
    }
    
    @Nested
    @DisplayName("Median Angle Calculations")
    class MedianAngleCalculations {
        
        @Test
        @DisplayName("Median of two angles")
        public void testMedianOfTwoAngles() {
            double median = calculator.getMedianAngle(0.0, 90.0);
            
            assertEquals(45.0, median, DELTA);
        }
        
        @Test
        @DisplayName("Median across discontinuity")
        public void testMedianAcrossDiscontinuity() {
            double median = calculator.getMedianAngle(170.0, -170.0);
            
            assertEquals(180.0, Math.abs(median), DELTA);
        }
        
        @Test
        @DisplayName("Median of multiple angles")
        public void testMedianMultipleAngles() {
            List<Double> angles = Arrays.asList(0.0, 30.0, 60.0, 90.0);
            
            double median = calculator.getMedianAngle(angles);
            
            assertTrue(median > 30 && median < 60);
        }
        
        @Test
        @DisplayName("Median angle from locations")
        public void testMedianAngleFromLocations() {
            Location start = new Location(0, 0);
            Location loc1 = new Location(10, 0);
            Location loc2 = new Location(0, -10); // Y inverted
            
            double median = calculator.getMedianAngle(start, loc1, loc2);
            
            assertEquals(45.0, median, DELTA);
        }
    }
    
    @Nested
    @DisplayName("Opposite Angle")
    class OppositeAngle {
        
        @ParameterizedTest
        @CsvSource({
            "0.0, 180.0",
            "90.0, -90.0",
            "180.0, 0.0",
            "-90.0, 90.0",
            "45.0, -135.0",
            "-45.0, 135.0"
        })
        @DisplayName("Calculate opposite angles")
        public void testOppositeAngles(double angle, double expected) {
            double opposite = calculator.getOppositeAngle(angle);
            
            assertEquals(expected, opposite, DELTA);
        }
    }
    
    @Nested
    @DisplayName("Location from Angle and Distance")
    class LocationFromAngleDistance {
        
        @Test
        @DisplayName("Location at 0 degrees")
        public void testLocationAt0Degrees() {
            Location start = new Location(0, 0);
            
            Location result = calculator.getLocation(0, 100, start);
            
            assertEquals(100, result.getCalculatedX());
            assertEquals(0, result.getCalculatedY());
        }
        
        @Test
        @DisplayName("Location at 90 degrees")
        public void testLocationAt90Degrees() {
            Location start = new Location(0, 0);
            
            Location result = calculator.getLocation(90, 100, start);
            
            assertEquals(0, result.getCalculatedX());
            assertEquals(-100, result.getCalculatedY()); // Y inverted
        }
        
        @Test
        @DisplayName("Location at 45 degrees")
        public void testLocationAt45Degrees() {
            Location start = new Location(100, 100);
            
            Location result = calculator.getLocation(45, 141.421, start);
            
            assertEquals(200, result.getCalculatedX(), 1);
            assertEquals(0, result.getCalculatedY(), 1);
        }
        
        @ParameterizedTest
        @CsvSource({
            "0, 100, 100, 0",
            "90, 100, 0, -100",
            "180, 100, -100, 0",
            "270, 100, 0, 100",
            "45, 141.421, 100, -100"
        })
        @DisplayName("Various angle and distance combinations")
        public void testVariousAngleDistance(double angle, double distance, int expectedX, int expectedY) {
            Location start = new Location(0, 0);
            
            Location result = calculator.getLocation(angle, distance, start);
            
            assertEquals(expectedX, result.getCalculatedX(), 1);
            assertEquals(expectedY, result.getCalculatedY(), 1);
        }
    }
    
    @Nested
    @DisplayName("Angle Scale Conversion")
    class AngleScaleConversion {
        
        @Test
        @DisplayName("Convert angles to same scale")
        public void testConvertAnglesToSameScale() {
            List<Double> angles = Arrays.asList(170.0, -170.0, -160.0);
            
            List<Double> converted = calculator.convertAnglesToSameScale(angles);
            
            // Should adjust to avoid discontinuity
            assertFalse(converted.contains(-170.0) && converted.contains(170.0));
        }
        
        @Test
        @DisplayName("Undo angle conversion")
        public void testUndoConversion() {
            double angle = 190.0;
            
            double normalized = calculator.undueConversion(angle);
            
            assertEquals(-170.0, normalized, DELTA);
        }
        
        @Test
        @DisplayName("Undo conversion for normal angle")
        public void testUndoConversionNormal() {
            double angle = 45.0;
            
            double normalized = calculator.undueConversion(angle);
            
            assertEquals(45.0, normalized, DELTA);
        }
    }
    
    @Nested
    @DisplayName("Displacement Vector")
    class DisplacementVector {
        
        @Test
        @DisplayName("Displacement between locations")
        public void testDisplacement() {
            Location loc1 = new Location(10, 20);
            Location loc2 = new Location(30, 50);
            
            Location displacement = calculator.getLocation(loc1, loc2);
            
            assertEquals(20, displacement.getCalculatedX());
            assertEquals(30, displacement.getCalculatedY());
        }
        
        @Test
        @DisplayName("Zero displacement")
        public void testZeroDisplacement() {
            Location loc = new Location(100, 200);
            
            Location displacement = calculator.getLocation(loc, loc);
            
            assertEquals(0, displacement.getCalculatedX());
            assertEquals(0, displacement.getCalculatedY());
        }
        
        @Test
        @DisplayName("Negative displacement")
        public void testNegativeDisplacement() {
            Location loc1 = new Location(50, 50);
            Location loc2 = new Location(10, 20);
            
            Location displacement = calculator.getLocation(loc1, loc2);
            
            assertEquals(-40, displacement.getCalculatedX());
            assertEquals(-30, displacement.getCalculatedY());
        }
    }
    
    @Nested
    @DisplayName("Standard Location")
    class StandardLocation {
        
        @Test
        @DisplayName("Standard location at 0 degrees")
        public void testStandardLocationAt0() {
            Location loc = calculator.getStandardLocation(0);
            
            assertEquals(1000, loc.getCalculatedX());
            assertEquals(0, loc.getCalculatedY());
        }
        
        @Test
        @DisplayName("Standard location at 90 degrees")
        public void testStandardLocationAt90() {
            Location loc = calculator.getStandardLocation(90);
            
            assertEquals(0, loc.getCalculatedX());
            assertEquals(-1000, loc.getCalculatedY());
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0, 45, 90, 135, 180, -45, -90, -135})
        @DisplayName("Standard locations at various angles")
        public void testStandardLocationsVariousAngles(double angle) {
            Location loc = calculator.getStandardLocation(angle);
            
            // Distance should always be 1000
            double distance = calculator.getDistance(new Location(0, 0), loc);
            assertEquals(1000, distance, 1);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Very large coordinates")
        public void testVeryLargeCoordinates() {
            Location loc1 = new Location(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
            Location loc2 = new Location(Integer.MAX_VALUE / 2 + 100, Integer.MAX_VALUE / 2);
            
            double distance = calculator.getDistance(loc1, loc2);
            
            assertEquals(100, distance, DELTA);
        }
        
        @Test
        @DisplayName("Negative coordinates")
        public void testNegativeCoordinates() {
            Location loc1 = new Location(-100, -200);
            Location loc2 = new Location(-50, -150);
            
            double distance = calculator.getDistance(loc1, loc2);
            
            assertEquals(70.711, distance, 0.01);
        }
        
        @Test
        @DisplayName("Empty angle list")
        public void testEmptyAngleList() {
            List<Double> empty = new ArrayList<>();
            
            double span = calculator.getSpan(empty);
            
            assertEquals(0.0, span, DELTA);
        }
    }
}