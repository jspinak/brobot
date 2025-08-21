package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Movement - represents directed movement from start to end.
 * Tests immutability, null safety, delta calculations, and equality/hashcode contracts.
 */
@DisplayName("Movement Tests")
public class MovementTest extends BrobotTestBase {
    
    private Location startLocation;
    private Location endLocation;
    private Movement movement;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        startLocation = new Location(100, 200);
        endLocation = new Location(300, 500);
        movement = new Movement(startLocation, endLocation);
    }
    
    @Nested
    @DisplayName("Constructor and Immutability")
    class ConstructorTests {
        
        @Test
        @DisplayName("Create movement with valid locations")
        public void testValidConstruction() {
            Movement m = new Movement(startLocation, endLocation);
            
            assertNotNull(m);
            assertEquals(startLocation, m.getStartLocation());
            assertEquals(endLocation, m.getEndLocation());
        }
        
        @Test
        @DisplayName("Null start location throws NullPointerException")
        public void testNullStartLocation() {
            assertThrows(NullPointerException.class, () -> {
                new Movement(null, endLocation);
            }, "Start location cannot be null");
        }
        
        @Test
        @DisplayName("Null end location throws NullPointerException")
        public void testNullEndLocation() {
            assertThrows(NullPointerException.class, () -> {
                new Movement(startLocation, null);
            }, "End location cannot be null");
        }
        
        @Test
        @DisplayName("Movement is immutable")
        public void testImmutability() {
            Location originalStart = new Location(10, 20);
            Location originalEnd = new Location(30, 40);
            Movement m = new Movement(originalStart, originalEnd);
            
            // Locations themselves are immutable, so Movement is immutable
            assertEquals(originalStart, m.getStartLocation());
            assertEquals(originalEnd, m.getEndLocation());
            
            // Create new locations with same values
            Location newStart = new Location(10, 20);
            Location newEnd = new Location(30, 40);
            
            // Original movement unchanged
            assertEquals(originalStart, m.getStartLocation());
            assertEquals(originalEnd, m.getEndLocation());
        }
        
        @Test
        @DisplayName("Same location for start and end creates zero movement")
        public void testZeroMovement() {
            Location samePoint = new Location(100, 100);
            Movement zeroMovement = new Movement(samePoint, samePoint);
            
            assertEquals(0, zeroMovement.getDeltaX());
            assertEquals(0, zeroMovement.getDeltaY());
        }
    }
    
    @Nested
    @DisplayName("Delta Calculations")
    class DeltaCalculations {
        
        @Test
        @DisplayName("Calculate positive deltas")
        public void testPositiveDeltas() {
            // Movement from (100,200) to (300,500)
            assertEquals(200, movement.getDeltaX());
            assertEquals(300, movement.getDeltaY());
        }
        
        @Test
        @DisplayName("Calculate negative deltas")
        public void testNegativeDeltas() {
            Movement reverseMovement = new Movement(endLocation, startLocation);
            
            assertEquals(-200, reverseMovement.getDeltaX());
            assertEquals(-300, reverseMovement.getDeltaY());
        }
        
        @Test
        @DisplayName("Calculate mixed deltas")
        public void testMixedDeltas() {
            Location start = new Location(500, 100);
            Location end = new Location(200, 400);
            Movement m = new Movement(start, end);
            
            assertEquals(-300, m.getDeltaX()); // Moving left
            assertEquals(300, m.getDeltaY());  // Moving down
        }
        
        @ParameterizedTest
        @DisplayName("Various delta calculations")
        @CsvSource({
            "0,0,100,100,100,100",
            "50,50,50,50,0,0",
            "100,200,50,100,-50,-100",
            "-50,-50,50,50,100,100",
            "0,0,0,100,0,100",
            "0,0,100,0,100,0"
        })
        public void testVariousDeltas(int x1, int y1, int x2, int y2, int expectedDx, int expectedDy) {
            Location start = new Location(x1, y1);
            Location end = new Location(x2, y2);
            Movement m = new Movement(start, end);
            
            assertEquals(expectedDx, m.getDeltaX());
            assertEquals(expectedDy, m.getDeltaY());
        }
    }
    
    @Nested
    @DisplayName("Movement Patterns")
    class MovementPatterns {
        
        @Test
        @DisplayName("Horizontal movement only")
        public void testHorizontalMovement() {
            Location start = new Location(100, 200);
            Location end = new Location(500, 200);
            Movement horizontal = new Movement(start, end);
            
            assertEquals(400, horizontal.getDeltaX());
            assertEquals(0, horizontal.getDeltaY());
        }
        
        @Test
        @DisplayName("Vertical movement only")
        public void testVerticalMovement() {
            Location start = new Location(100, 200);
            Location end = new Location(100, 600);
            Movement vertical = new Movement(start, end);
            
            assertEquals(0, vertical.getDeltaX());
            assertEquals(400, vertical.getDeltaY());
        }
        
        @Test
        @DisplayName("Diagonal movement")
        public void testDiagonalMovement() {
            Location start = new Location(100, 100);
            Location end = new Location(200, 200);
            Movement diagonal = new Movement(start, end);
            
            assertEquals(100, diagonal.getDeltaX());
            assertEquals(100, diagonal.getDeltaY());
            
            // Perfect diagonal - same X and Y delta
            assertEquals(diagonal.getDeltaX(), diagonal.getDeltaY());
        }
        
        @Test
        @DisplayName("Screen position-based movement")
        public void testScreenPositionMovement() {
            Location topLeft = new Location(Positions.Name.TOPLEFT);
            Location bottomRight = new Location(Positions.Name.BOTTOMRIGHT);
            Movement fullScreen = new Movement(topLeft, bottomRight);
            
            // Full screen diagonal movement
            assertTrue(fullScreen.getDeltaX() > 0);
            assertTrue(fullScreen.getDeltaY() > 0);
        }
    }
    
    @Nested
    @DisplayName("Common Use Cases")
    class CommonUseCases {
        
        @Test
        @DisplayName("Drag operation movement")
        public void testDragMovement() {
            // Simulating dragging from one point to another
            Location dragStart = new Location(200, 300);
            Location dragEnd = new Location(400, 350);
            Movement drag = new Movement(dragStart, dragEnd);
            
            assertEquals(200, drag.getDeltaX());
            assertEquals(50, drag.getDeltaY());
        }
        
        @Test
        @DisplayName("Swipe gesture movement")
        public void testSwipeMovement() {
            // Simulating a left swipe
            Location swipeStart = new Location(500, 400);
            Location swipeEnd = new Location(100, 400);
            Movement leftSwipe = new Movement(swipeStart, swipeEnd);
            
            assertEquals(-400, leftSwipe.getDeltaX());
            assertEquals(0, leftSwipe.getDeltaY());
            
            // Simulating an up swipe
            Location upSwipeStart = new Location(300, 600);
            Location upSwipeEnd = new Location(300, 200);
            Movement upSwipe = new Movement(upSwipeStart, upSwipeEnd);
            
            assertEquals(0, upSwipe.getDeltaX());
            assertEquals(-400, upSwipe.getDeltaY());
        }
        
        @Test
        @DisplayName("Circular movement pattern")
        public void testCircularPattern() {
            // Four movements forming a square (approximating circular motion)
            Movement right = new Movement(new Location(0, 0), new Location(100, 0));
            Movement down = new Movement(new Location(100, 0), new Location(100, 100));
            Movement left = new Movement(new Location(100, 100), new Location(0, 100));
            Movement up = new Movement(new Location(0, 100), new Location(0, 0));
            
            assertEquals(100, right.getDeltaX());
            assertEquals(0, right.getDeltaY());
            
            assertEquals(0, down.getDeltaX());
            assertEquals(100, down.getDeltaY());
            
            assertEquals(-100, left.getDeltaX());
            assertEquals(0, left.getDeltaY());
            
            assertEquals(0, up.getDeltaX());
            assertEquals(-100, up.getDeltaY());
            
            // Sum of all deltas should be zero (closed loop)
            int totalDx = right.getDeltaX() + down.getDeltaX() + left.getDeltaX() + up.getDeltaX();
            int totalDy = right.getDeltaY() + down.getDeltaY() + left.getDeltaY() + up.getDeltaY();
            
            assertEquals(0, totalDx);
            assertEquals(0, totalDy);
        }
    }
    
    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCode {
        
        @Test
        @DisplayName("Equals with same object")
        public void testEqualsSameObject() {
            assertTrue(movement.equals(movement));
        }
        
        @Test
        @DisplayName("Equals with null")
        public void testEqualsNull() {
            assertFalse(movement.equals(null));
        }
        
        @Test
        @DisplayName("Equals with different class")
        public void testEqualsDifferentClass() {
            assertFalse(movement.equals("Not a Movement"));
        }
        
        @Test
        @DisplayName("Equals with equivalent movement")
        public void testEqualsEquivalent() {
            Movement equivalent = new Movement(
                new Location(100, 200),
                new Location(300, 500)
            );
            
            assertTrue(movement.equals(equivalent));
            assertTrue(equivalent.equals(movement));
        }
        
        @Test
        @DisplayName("Not equals with different start")
        public void testNotEqualsDifferentStart() {
            Movement different = new Movement(
                new Location(50, 200),
                new Location(300, 500)
            );
            
            assertFalse(movement.equals(different));
        }
        
        @Test
        @DisplayName("Not equals with different end")
        public void testNotEqualsDifferentEnd() {
            Movement different = new Movement(
                new Location(100, 200),
                new Location(300, 600)
            );
            
            assertFalse(movement.equals(different));
        }
        
        @Test
        @DisplayName("HashCode consistency")
        public void testHashCodeConsistency() {
            int hash1 = movement.hashCode();
            int hash2 = movement.hashCode();
            
            assertEquals(hash1, hash2);
        }
        
        @Test
        @DisplayName("HashCode equals for equivalent movements")
        public void testHashCodeEquals() {
            Movement equivalent = new Movement(
                new Location(100, 200),
                new Location(300, 500)
            );
            
            assertEquals(movement.hashCode(), equivalent.hashCode());
        }
        
        @Test
        @DisplayName("HashCode different for different movements")
        public void testHashCodeDifferent() {
            Movement different = new Movement(
                new Location(0, 0),
                new Location(100, 100)
            );
            
            // Usually different, though not guaranteed by contract
            assertNotEquals(movement.hashCode(), different.hashCode());
        }
    }
    
    @Nested
    @DisplayName("ToString Representation")
    class ToStringTests {
        
        @Test
        @DisplayName("ToString contains both locations")
        public void testToStringFormat() {
            String str = movement.toString();
            
            assertNotNull(str);
            assertTrue(str.contains("Movement"));
            assertTrue(str.contains("from="));
            assertTrue(str.contains("to="));
            assertTrue(str.contains(startLocation.toString()));
            assertTrue(str.contains(endLocation.toString()));
        }
        
        @Test
        @DisplayName("ToString for zero movement")
        public void testToStringZeroMovement() {
            Location same = new Location(50, 50);
            Movement zero = new Movement(same, same);
            String str = zero.toString();
            
            assertTrue(str.contains("from="));
            assertTrue(str.contains("to="));
            // Both locations should be the same in the string
            assertTrue(str.contains(same.toString()));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Movement with extreme coordinates")
        public void testExtremeCoordinates() {
            Location maxStart = new Location(Integer.MAX_VALUE - 1000, Integer.MAX_VALUE - 1000);
            Location maxEnd = new Location(Integer.MAX_VALUE, Integer.MAX_VALUE);
            Movement maxMovement = new Movement(maxStart, maxEnd);
            
            assertEquals(1000, maxMovement.getDeltaX());
            assertEquals(1000, maxMovement.getDeltaY());
        }
        
        @Test
        @DisplayName("Movement with negative coordinates")
        public void testNegativeCoordinates() {
            Location negStart = new Location(-100, -200);
            Location negEnd = new Location(-50, -150);
            Movement negMovement = new Movement(negStart, negEnd);
            
            assertEquals(50, negMovement.getDeltaX());
            assertEquals(50, negMovement.getDeltaY());
        }
        
        @Test
        @DisplayName("Movement crossing coordinate origin")
        public void testCrossingOrigin() {
            Location negStart = new Location(-100, -100);
            Location posEnd = new Location(100, 100);
            Movement crossing = new Movement(negStart, posEnd);
            
            assertEquals(200, crossing.getDeltaX());
            assertEquals(200, crossing.getDeltaY());
        }
    }
    
    @Nested
    @DisplayName("Movement Chains")
    class MovementChains {
        
        @Test
        @DisplayName("Sequential movements")
        public void testSequentialMovements() {
            Location point1 = new Location(0, 0);
            Location point2 = new Location(100, 0);
            Location point3 = new Location(100, 100);
            
            Movement move1 = new Movement(point1, point2);
            Movement move2 = new Movement(point2, point3);
            
            // Total displacement
            int totalDx = move1.getDeltaX() + move2.getDeltaX();
            int totalDy = move1.getDeltaY() + move2.getDeltaY();
            
            // Should equal direct movement
            Movement direct = new Movement(point1, point3);
            assertEquals(direct.getDeltaX(), totalDx);
            assertEquals(direct.getDeltaY(), totalDy);
        }
        
        @Test
        @DisplayName("Reverse movement")
        public void testReverseMovement() {
            Movement forward = new Movement(startLocation, endLocation);
            Movement reverse = new Movement(endLocation, startLocation);
            
            assertEquals(-forward.getDeltaX(), reverse.getDeltaX());
            assertEquals(-forward.getDeltaY(), reverse.getDeltaY());
            
            // Round trip should have zero total displacement
            assertEquals(0, forward.getDeltaX() + reverse.getDeltaX());
            assertEquals(0, forward.getDeltaY() + reverse.getDeltaY());
        }
    }
}