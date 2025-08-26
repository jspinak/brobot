package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.util.common.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.jspinak.brobot.model.element.Positions.Name.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Positions class, which defines standard relative positions within a rectangular area.
 */
@DisplayName("Positions Tests")
class PositionsTest extends BrobotTestBase {

    @Nested
    @DisplayName("Position Coordinate Tests")
    class PositionCoordinateTests {

        @Test
        @DisplayName("Should return correct coordinates for TOPLEFT")
        void shouldReturnCorrectCoordinatesForTopLeft() {
            Pair<Double, Double> coords = Positions.getCoordinates(TOPLEFT);
            assertEquals(0.0, coords.getKey());
            assertEquals(0.0, coords.getValue());
        }

        @Test
        @DisplayName("Should return correct coordinates for TOPMIDDLE")
        void shouldReturnCorrectCoordinatesForTopMiddle() {
            Pair<Double, Double> coords = Positions.getCoordinates(TOPMIDDLE);
            assertEquals(0.5, coords.getKey());
            assertEquals(0.0, coords.getValue());
        }

        @Test
        @DisplayName("Should return correct coordinates for TOPRIGHT")
        void shouldReturnCorrectCoordinatesForTopRight() {
            Pair<Double, Double> coords = Positions.getCoordinates(TOPRIGHT);
            assertEquals(1.0, coords.getKey());
            assertEquals(0.0, coords.getValue());
        }

        @Test
        @DisplayName("Should return correct coordinates for MIDDLELEFT")
        void shouldReturnCorrectCoordinatesForMiddleLeft() {
            Pair<Double, Double> coords = Positions.getCoordinates(MIDDLELEFT);
            assertEquals(0.0, coords.getKey());
            assertEquals(0.5, coords.getValue());
        }

        @Test
        @DisplayName("Should return correct coordinates for MIDDLEMIDDLE")
        void shouldReturnCorrectCoordinatesForMiddleMiddle() {
            Pair<Double, Double> coords = Positions.getCoordinates(MIDDLEMIDDLE);
            assertEquals(0.5, coords.getKey());
            assertEquals(0.5, coords.getValue());
        }

        @Test
        @DisplayName("Should return correct coordinates for MIDDLERIGHT")
        void shouldReturnCorrectCoordinatesForMiddleRight() {
            Pair<Double, Double> coords = Positions.getCoordinates(MIDDLERIGHT);
            assertEquals(1.0, coords.getKey());
            assertEquals(0.5, coords.getValue());
        }

        @Test
        @DisplayName("Should return correct coordinates for BOTTOMLEFT")
        void shouldReturnCorrectCoordinatesForBottomLeft() {
            Pair<Double, Double> coords = Positions.getCoordinates(BOTTOMLEFT);
            assertEquals(0.0, coords.getKey());
            assertEquals(1.0, coords.getValue());
        }

        @Test
        @DisplayName("Should return correct coordinates for BOTTOMMIDDLE")
        void shouldReturnCorrectCoordinatesForBottomMiddle() {
            Pair<Double, Double> coords = Positions.getCoordinates(BOTTOMMIDDLE);
            assertEquals(0.5, coords.getKey());
            assertEquals(1.0, coords.getValue());
        }

        @Test
        @DisplayName("Should return correct coordinates for BOTTOMRIGHT")
        void shouldReturnCorrectCoordinatesForBottomRight() {
            Pair<Double, Double> coords = Positions.getCoordinates(BOTTOMRIGHT);
            assertEquals(1.0, coords.getKey());
            assertEquals(1.0, coords.getValue());
        }
    }

    @Nested
    @DisplayName("Parameterized Tests")
    class ParameterizedTests {

        @ParameterizedTest
        @MethodSource("providePositionsAndCoordinates")
        @DisplayName("Should return correct coordinates for all positions")
        void shouldReturnCorrectCoordinatesForAllPositions(Positions.Name position, double expectedX, double expectedY) {
            Pair<Double, Double> coords = Positions.getCoordinates(position);
            assertEquals(expectedX, coords.getKey(), 0.001, "X coordinate mismatch for " + position);
            assertEquals(expectedY, coords.getValue(), 0.001, "Y coordinate mismatch for " + position);
        }

        private static Stream<Arguments> providePositionsAndCoordinates() {
            return Stream.of(
                Arguments.of(TOPLEFT, 0.0, 0.0),
                Arguments.of(TOPMIDDLE, 0.5, 0.0),
                Arguments.of(TOPRIGHT, 1.0, 0.0),
                Arguments.of(MIDDLELEFT, 0.0, 0.5),
                Arguments.of(MIDDLEMIDDLE, 0.5, 0.5),
                Arguments.of(MIDDLERIGHT, 1.0, 0.5),
                Arguments.of(BOTTOMLEFT, 0.0, 1.0),
                Arguments.of(BOTTOMMIDDLE, 0.5, 1.0),
                Arguments.of(BOTTOMRIGHT, 1.0, 1.0)
            );
        }
    }

    @Nested
    @DisplayName("Grid Layout Tests")
    class GridLayoutTests {

        @Test
        @DisplayName("Should have correct top row positions")
        void shouldHaveCorrectTopRowPositions() {
            // Top row should all have Y = 0.0
            assertEquals(0.0, Positions.getCoordinates(TOPLEFT).getValue());
            assertEquals(0.0, Positions.getCoordinates(TOPMIDDLE).getValue());
            assertEquals(0.0, Positions.getCoordinates(TOPRIGHT).getValue());
        }

        @Test
        @DisplayName("Should have correct middle row positions")
        void shouldHaveCorrectMiddleRowPositions() {
            // Middle row should all have Y = 0.5
            assertEquals(0.5, Positions.getCoordinates(MIDDLELEFT).getValue());
            assertEquals(0.5, Positions.getCoordinates(MIDDLEMIDDLE).getValue());
            assertEquals(0.5, Positions.getCoordinates(MIDDLERIGHT).getValue());
        }

        @Test
        @DisplayName("Should have correct bottom row positions")
        void shouldHaveCorrectBottomRowPositions() {
            // Bottom row should all have Y = 1.0
            assertEquals(1.0, Positions.getCoordinates(BOTTOMLEFT).getValue());
            assertEquals(1.0, Positions.getCoordinates(BOTTOMMIDDLE).getValue());
            assertEquals(1.0, Positions.getCoordinates(BOTTOMRIGHT).getValue());
        }

        @Test
        @DisplayName("Should have correct left column positions")
        void shouldHaveCorrectLeftColumnPositions() {
            // Left column should all have X = 0.0
            assertEquals(0.0, Positions.getCoordinates(TOPLEFT).getKey());
            assertEquals(0.0, Positions.getCoordinates(MIDDLELEFT).getKey());
            assertEquals(0.0, Positions.getCoordinates(BOTTOMLEFT).getKey());
        }

        @Test
        @DisplayName("Should have correct middle column positions")
        void shouldHaveCorrectMiddleColumnPositions() {
            // Middle column should all have X = 0.5
            assertEquals(0.5, Positions.getCoordinates(TOPMIDDLE).getKey());
            assertEquals(0.5, Positions.getCoordinates(MIDDLEMIDDLE).getKey());
            assertEquals(0.5, Positions.getCoordinates(BOTTOMMIDDLE).getKey());
        }

        @Test
        @DisplayName("Should have correct right column positions")
        void shouldHaveCorrectRightColumnPositions() {
            // Right column should all have X = 1.0
            assertEquals(1.0, Positions.getCoordinates(TOPRIGHT).getKey());
            assertEquals(1.0, Positions.getCoordinates(MIDDLERIGHT).getKey());
            assertEquals(1.0, Positions.getCoordinates(BOTTOMRIGHT).getKey());
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should return same coordinates for repeated calls")
        void shouldReturnSameCoordinatesForRepeatedCalls() {
            Pair<Double, Double> firstCall = Positions.getCoordinates(MIDDLEMIDDLE);
            Pair<Double, Double> secondCall = Positions.getCoordinates(MIDDLEMIDDLE);
            
            assertEquals(firstCall.getKey(), secondCall.getKey());
            assertEquals(firstCall.getValue(), secondCall.getValue());
        }

        @Test
        @DisplayName("Should return consistent coordinates across all positions")
        void shouldReturnConsistentCoordinatesAcrossAllPositions() {
            // Call each position twice and verify consistency
            for (Positions.Name position : Positions.Name.values()) {
                Pair<Double, Double> first = Positions.getCoordinates(position);
                Pair<Double, Double> second = Positions.getCoordinates(position);
                
                assertEquals(first.getKey(), second.getKey(), 
                    "X coordinate should be consistent for " + position);
                assertEquals(first.getValue(), second.getValue(), 
                    "Y coordinate should be consistent for " + position);
            }
        }
    }

    @Nested
    @DisplayName("Coverage Tests")
    class CoverageTests {

        @Test
        @DisplayName("Should have coordinates for all enum values")
        void shouldHaveCoordinatesForAllEnumValues() {
            for (Positions.Name position : Positions.Name.values()) {
                Pair<Double, Double> coords = Positions.getCoordinates(position);
                assertNotNull(coords, "Coordinates should not be null for " + position);
                assertNotNull(coords.getKey(), "X coordinate should not be null for " + position);
                assertNotNull(coords.getValue(), "Y coordinate should not be null for " + position);
            }
        }

        @Test
        @DisplayName("Should have exactly 9 positions")
        void shouldHaveExactlyNinePositions() {
            assertEquals(9, Positions.Name.values().length);
        }

        @Test
        @DisplayName("Should have coordinates in valid range")
        void shouldHaveCoordinatesInValidRange() {
            for (Positions.Name position : Positions.Name.values()) {
                Pair<Double, Double> coords = Positions.getCoordinates(position);
                
                // X and Y should be in [0.0, 1.0] range
                assertTrue(coords.getKey() >= 0.0 && coords.getKey() <= 1.0, 
                    "X coordinate should be in [0,1] for " + position);
                assertTrue(coords.getValue() >= 0.0 && coords.getValue() <= 1.0, 
                    "Y coordinate should be in [0,1] for " + position);
            }
        }
    }

    @Nested
    @DisplayName("Null Handling Tests")
    class NullHandlingTests {

        @Test
        @DisplayName("Should return null for null position")
        void shouldReturnNullForNullPosition() {
            Pair<Double, Double> coords = Positions.getCoordinates(null);
            assertNull(coords);
        }
    }
}