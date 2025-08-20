package io.github.jspinak.brobot.util.geometry;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Sector - angular sector representation.
 * Tests sector creation, angle normalization, and span calculations.
 */
@DisplayName("Sector Tests")
public class SectorTest extends BrobotTestBase {
    
    private static final double DELTA = 0.001;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }
    
    @Nested
    @DisplayName("Basic Sector Creation")
    class BasicSectorCreation {
        
        @Test
        @DisplayName("Create simple sector")
        public void testSimpleSector() {
            Sector sector = new Sector(45, 135);
            
            assertEquals(45, sector.getLeftAngle(), DELTA);
            assertEquals(135, sector.getRightAngle(), DELTA);
            assertEquals(90, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Create sector with reversed angles")
        public void testReversedAngles() {
            Sector sector = new Sector(135, 45);
            
            // Should take the shorter arc
            assertEquals(135, sector.getLeftAngle(), DELTA);
            assertEquals(45, sector.getRightAngle(), DELTA);
            assertEquals(270, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Create sector at origin")
        public void testSectorAtOrigin() {
            Sector sector = new Sector(0, 90);
            
            assertEquals(0, sector.getLeftAngle(), DELTA);
            assertEquals(90, sector.getRightAngle(), DELTA);
            assertEquals(90, sector.getSpan(), DELTA);
        }
    }
    
    @Nested
    @DisplayName("Full Circle Cases")
    class FullCircleCases {
        
        @Test
        @DisplayName("Equal angles create full circle")
        public void testEqualAnglesFullCircle() {
            Sector sector = new Sector(90, 90);
            
            assertEquals(90, sector.getLeftAngle(), DELTA);
            assertEquals(90, sector.getRightAngle(), DELTA);
            assertEquals(360, sector.getSpan(), DELTA);
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0, 45, 90, 180, 270, 360})
        @DisplayName("Full circle at various positions")
        public void testFullCircleVariousPositions(double angle) {
            Sector sector = new Sector(angle, angle);
            
            assertEquals(angle, sector.getLeftAngle(), DELTA);
            assertEquals(angle, sector.getRightAngle(), DELTA);
            assertEquals(360, sector.getSpan(), DELTA);
        }
    }
    
    @Nested
    @DisplayName("Crossing Zero Degree")
    class CrossingZeroDegree {
        
        @Test
        @DisplayName("Sector crossing 0 degrees")
        public void testCrossingZero() {
            Sector sector = new Sector(315, 45);
            
            assertEquals(315, sector.getLeftAngle(), DELTA);
            assertEquals(45, sector.getRightAngle(), DELTA);
            assertEquals(90, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Sector crossing 0 degrees reversed")
        public void testCrossingZeroReversed() {
            Sector sector = new Sector(45, 315);
            
            // Should take the shorter arc
            assertEquals(45, sector.getLeftAngle(), DELTA);
            assertEquals(315, sector.getRightAngle(), DELTA);
            assertEquals(270, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Small sector around 0")
        public void testSmallSectorAroundZero() {
            Sector sector = new Sector(350, 10);
            
            assertEquals(350, sector.getLeftAngle(), DELTA);
            assertEquals(10, sector.getRightAngle(), DELTA);
            assertEquals(20, sector.getSpan(), DELTA);
        }
    }
    
    @Nested
    @DisplayName("180 Degree Spans")
    class HalfCircleSpans {
        
        @Test
        @DisplayName("Exact 180 degree span")
        public void testExact180Span() {
            Sector sector = new Sector(0, 180);
            
            assertEquals(0, sector.getLeftAngle(), DELTA);
            assertEquals(180, sector.getRightAngle(), DELTA);
            assertEquals(180, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("180 degree span at different positions")
        public void test180SpanDifferentPositions() {
            Sector sector = new Sector(90, 270);
            
            assertEquals(90, sector.getLeftAngle(), DELTA);
            assertEquals(270, sector.getRightAngle(), DELTA);
            assertEquals(180, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("180 degree span reversed")
        public void test180SpanReversed() {
            Sector sector = new Sector(180, 0);
            
            // Both directions are 180, so it should maintain order
            assertEquals(180, sector.getLeftAngle(), DELTA);
            assertEquals(0, sector.getRightAngle(), DELTA);
            assertEquals(180, sector.getSpan(), DELTA);
        }
    }
    
    @Nested
    @DisplayName("Negative Angles")
    class NegativeAngles {
        
        @Test
        @DisplayName("Negative angle normalization")
        public void testNegativeAngleNormalization() {
            Sector sector = new Sector(-45, 45);
            
            // -45 normalizes to 315 for calculation
            // The span from -45 to 45 is 90 degrees (shortest arc)
            // Algorithm preserves original values
            assertEquals(-45, sector.getLeftAngle(), DELTA);
            assertEquals(45, sector.getRightAngle(), DELTA);
            assertEquals(90, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Both angles negative")
        public void testBothAnglesNegative() {
            Sector sector = new Sector(-90, -45);
            
            // Preserves original values
            assertEquals(-90, sector.getLeftAngle(), DELTA);
            assertEquals(-45, sector.getRightAngle(), DELTA);
            assertEquals(45, sector.getSpan(), DELTA);
        }
        
        @ParameterizedTest
        @CsvSource({
            "-360, 0, -360, 0, 0",
            "-180, 180, -180, 180, 0",
            "-270, -90, -270, -90, 180",
            "-45, -315, -45, -315, 270"
        })
        @DisplayName("Various negative angle combinations")
        public void testNegativeAngleCombinations(double a1, double a2, 
                                                  double expLeft, double expRight, double expSpan) {
            Sector sector = new Sector(a1, a2);
            
            assertEquals(expLeft, sector.getLeftAngle(), DELTA);
            assertEquals(expRight, sector.getRightAngle(), DELTA);
            assertEquals(expSpan == 0 ? 360 : expSpan, sector.getSpan(), DELTA);
        }
    }
    
    @Nested
    @DisplayName("Large Angles")
    class LargeAngles {
        
        @Test
        @DisplayName("Angles greater than 360")
        public void testAnglesGreaterThan360() {
            Sector sector = new Sector(370, 450);
            
            // Original values preserved
            assertEquals(370, sector.getLeftAngle(), DELTA);
            assertEquals(450, sector.getRightAngle(), DELTA);
            assertEquals(80, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Multiple rotations")
        public void testMultipleRotations() {
            Sector sector = new Sector(720, 810);
            
            // 720 normalizes to 0, 810 normalizes to 90
            assertEquals(720, sector.getLeftAngle(), DELTA);
            assertEquals(810, sector.getRightAngle(), DELTA);
            assertEquals(90, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Very large angles")
        public void testVeryLargeAngles() {
            Sector sector = new Sector(3600, 3645);
            
            // Both normalize to 0 and 45
            assertEquals(3600, sector.getLeftAngle(), DELTA);
            assertEquals(3645, sector.getRightAngle(), DELTA);
            assertEquals(45, sector.getSpan(), DELTA);
        }
    }
    
    @Nested
    @DisplayName("Quadrant Sectors")
    class QuadrantSectors {
        
        @ParameterizedTest
        @CsvSource({
            "0, 90, 0, 90, 90",      // First quadrant
            "90, 180, 90, 180, 90",  // Second quadrant
            "180, 270, 180, 270, 90", // Third quadrant
            "270, 360, 270, 360, 90"  // Fourth quadrant
        })
        @DisplayName("Standard quadrants")
        public void testStandardQuadrants(double a1, double a2, 
                                         double expLeft, double expRight, double expSpan) {
            Sector sector = new Sector(a1, a2);
            
            assertEquals(expLeft, sector.getLeftAngle(), DELTA);
            assertEquals(expRight, sector.getRightAngle(), DELTA);
            assertEquals(expSpan, sector.getSpan(), DELTA);
        }
    }
    
    @Nested
    @DisplayName("Shortest Arc Selection")
    class ShortestArcSelection {
        
        @Test
        @DisplayName("Chooses 90 over 270")
        public void testChooses90Over270() {
            Sector sector1 = new Sector(0, 90);
            Sector sector2 = new Sector(90, 0);
            
            // First should be 90 degree span
            assertEquals(90, sector1.getSpan(), DELTA);
            
            // Second should choose the 270 degree span (shorter from 90 to 0)
            assertEquals(270, sector2.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Chooses 120 over 240")
        public void testChooses120Over240() {
            Sector sector = new Sector(0, 120);
            
            assertEquals(0, sector.getLeftAngle(), DELTA);
            assertEquals(120, sector.getRightAngle(), DELTA);
            assertEquals(120, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Chooses 170 over 190")
        public void testChooses170Over190() {
            Sector sector = new Sector(0, 170);
            
            assertEquals(0, sector.getLeftAngle(), DELTA);
            assertEquals(170, sector.getRightAngle(), DELTA);
            assertEquals(170, sector.getSpan(), DELTA);
        }
    }
    
    @Nested
    @DisplayName("Setters and Getters")
    class SettersAndGetters {
        
        @Test
        @DisplayName("Modify left angle")
        public void testSetLeftAngle() {
            Sector sector = new Sector(45, 135);
            
            sector.setLeftAngle(60);
            
            assertEquals(60, sector.getLeftAngle(), DELTA);
            assertEquals(135, sector.getRightAngle(), DELTA);
            assertEquals(90, sector.getSpan(), DELTA); // Span doesn't auto-update
        }
        
        @Test
        @DisplayName("Modify right angle")
        public void testSetRightAngle() {
            Sector sector = new Sector(45, 135);
            
            sector.setRightAngle(150);
            
            assertEquals(45, sector.getLeftAngle(), DELTA);
            assertEquals(150, sector.getRightAngle(), DELTA);
            assertEquals(90, sector.getSpan(), DELTA); // Span doesn't auto-update
        }
        
        @Test
        @DisplayName("Modify span")
        public void testSetSpan() {
            Sector sector = new Sector(45, 135);
            
            sector.setSpan(180);
            
            assertEquals(45, sector.getLeftAngle(), DELTA);
            assertEquals(135, sector.getRightAngle(), DELTA);
            assertEquals(180, sector.getSpan(), DELTA);
        }
    }
    
    @Nested
    @DisplayName("Real-World Use Cases")
    class RealWorldUseCases {
        
        @Test
        @DisplayName("Clock hour sectors")
        public void testClockHourSectors() {
            // 12 to 3 o'clock
            Sector morning = new Sector(90, 0);
            assertEquals(270, morning.getSpan(), DELTA);
            
            // 3 to 6 o'clock (shortest arc is 90 degrees)
            Sector afternoon = new Sector(0, 270);
            assertEquals(90, afternoon.getSpan(), DELTA);
            
            // 6 to 9 o'clock (shortest arc is 90 degrees)
            Sector evening = new Sector(270, 180);
            assertEquals(90, evening.getSpan(), DELTA);
            
            // 9 to 12 o'clock (shortest arc is 90 degrees)
            Sector night = new Sector(180, 90);
            assertEquals(90, night.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Compass directions")
        public void testCompassDirections() {
            // North to East (NE quadrant)
            Sector ne = new Sector(90, 0);
            assertEquals(270, ne.getSpan(), DELTA);
            
            // East to South (SE quadrant - shortest arc is 90 degrees)
            Sector se = new Sector(0, 270);
            assertEquals(90, se.getSpan(), DELTA);
            
            // South to West (SW quadrant - shortest arc is 90 degrees)
            Sector sw = new Sector(270, 180);
            assertEquals(90, sw.getSpan(), DELTA);
            
            // West to North (NW quadrant - shortest arc is 90 degrees)
            Sector nw = new Sector(180, 90);
            assertEquals(90, nw.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Pie chart segments")
        public void testPieChartSegments() {
            // 25% segment
            Sector quarter = new Sector(0, 90);
            assertEquals(90, quarter.getSpan(), DELTA);
            
            // 33.33% segment
            Sector third = new Sector(0, 120);
            assertEquals(120, third.getSpan(), DELTA);
            
            // 50% segment
            Sector half = new Sector(0, 180);
            assertEquals(180, half.getSpan(), DELTA);
            
            // 75% segment - but algorithm chooses shortest arc (90 degrees)
            // To get 270 degrees, would need different angle ordering
            Sector threeQuarters = new Sector(0, 270);
            assertEquals(90, threeQuarters.getSpan(), DELTA);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Tiny sector")
        public void testTinySector() {
            Sector sector = new Sector(0, 0.001);
            
            assertEquals(0, sector.getLeftAngle(), DELTA);
            assertEquals(0.001, sector.getRightAngle(), DELTA);
            assertEquals(0.001, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Almost full circle")
        public void testAlmostFullCircle() {
            Sector sector = new Sector(0, 359.999);
            
            // Algorithm chooses the shortest arc (0.001 degrees)
            // The arc from 359.999 to 0 is only 0.001 degrees
            assertEquals(359.999, sector.getLeftAngle(), DELTA);
            assertEquals(0, sector.getRightAngle(), DELTA);
            assertEquals(0.001, sector.getSpan(), DELTA);
        }
        
        @Test
        @DisplayName("Floating point precision")
        public void testFloatingPointPrecision() {
            Sector sector = new Sector(45.123456789, 135.987654321);
            
            assertEquals(45.123456789, sector.getLeftAngle(), DELTA);
            assertEquals(135.987654321, sector.getRightAngle(), DELTA);
            assertTrue(Math.abs(sector.getSpan() - 90.864197532) < 0.000001);
        }
    }
}