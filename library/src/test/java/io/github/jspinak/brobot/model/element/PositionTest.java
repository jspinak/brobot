package io.github.jspinak.brobot.model.element;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Comprehensive tests for the Position class which represents
 * relative positions within rectangular areas using percentage coordinates.
 */
@DisplayName("Position Model Tests")
public class PositionTest extends BrobotTestBase {

    private Position position;
    private ObjectMapper objectMapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        position = new Position();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create Position with default constructor")
    void testDefaultConstructor() {
        // When
        Position defaultPos = new Position();
        
        // Then - Default is center (0.5, 0.5)
        assertEquals(0.5, defaultPos.getPercentW());
        assertEquals(0.5, defaultPos.getPercentH());
    }

    @Test
    @DisplayName("Should create Position with double coordinates")
    void testDoubleConstructor() {
        // When
        Position pos = new Position(0.25, 0.75);
        
        // Then
        assertEquals(0.25, pos.getPercentW());
        assertEquals(0.75, pos.getPercentH());
    }

    @Test
    @DisplayName("Should create Position with integer percentages")
    void testIntegerConstructor() {
        // When
        Position pos = new Position(25, 75);
        
        // Then - Converts to decimal (25/100 = 0.25)
        assertEquals(0.25, pos.getPercentW());
        assertEquals(0.75, pos.getPercentH());
    }

    @ParameterizedTest
    @CsvSource({
        "0,0,0.0,0.0",
        "50,50,0.5,0.5",
        "100,100,1.0,1.0",
        "25,75,0.25,0.75",
        "10,90,0.1,0.9"
    })
    @DisplayName("Should convert integer percentages correctly")
    void testIntegerPercentageConversion(int intW, int intH, double expectedW, double expectedH) {
        // When
        Position pos = new Position(intW, intH);
        
        // Then
        assertEquals(expectedW, pos.getPercentW(), 0.001);
        assertEquals(expectedH, pos.getPercentH(), 0.001);
    }

    @ParameterizedTest
    @EnumSource(Positions.Name.class)
    @DisplayName("Should create Position from named position")
    void testNamedPositionConstructor(Positions.Name name) {
        // When
        Position pos = new Position(name);
        
        // Then
        assertNotNull(pos);
        assertTrue(pos.getPercentW() >= 0.0);
        assertTrue(pos.getPercentW() <= 1.0);
        assertTrue(pos.getPercentH() >= 0.0);
        assertTrue(pos.getPercentH() <= 1.0);
    }

    @Test
    @DisplayName("Should create Position with named position and offset")
    void testNamedPositionWithOffset() {
        // When
        Position pos = new Position(Positions.Name.TOPLEFT, 0.1, 0.2);
        
        // Then - TOPLEFT is (0, 0) + offset
        assertEquals(0.1, pos.getPercentW());
        assertEquals(0.2, pos.getPercentH());
    }

    @Test
    @DisplayName("Should create Position with copy constructor")
    void testCopyConstructor() {
        // Given
        Position original = new Position(0.3, 0.7);
        
        // When
        Position copy = new Position(original);
        
        // Then
        assertEquals(original.getPercentW(), copy.getPercentW());
        assertEquals(original.getPercentH(), copy.getPercentH());
        assertNotSame(original, copy); // Different objects
    }

    @Test
    @DisplayName("Should add to percentW")
    void testAddPercentW() {
        // Given
        position.setPercentW(0.5);
        
        // When
        position.addPercentW(0.3);
        
        // Then
        assertEquals(0.8, position.getPercentW());
        assertEquals(0.5, position.getPercentH()); // H unchanged
    }

    @Test
    @DisplayName("Should add to percentH")
    void testAddPercentH() {
        // Given
        position.setPercentH(0.5);
        
        // When
        position.addPercentH(0.2);
        
        // Then
        assertEquals(0.7, position.getPercentH());
        assertEquals(0.5, position.getPercentW()); // W unchanged
    }

    @Test
    @DisplayName("Should multiply percentW")
    void testMultiplyPercentW() {
        // Given
        position.setPercentW(0.5);
        
        // When
        position.multiplyPercentW(2.0);
        
        // Then
        assertEquals(1.0, position.getPercentW());
        assertEquals(0.5, position.getPercentH()); // H unchanged
    }

    @Test
    @DisplayName("Should multiply percentH")
    void testMultiplyPercentH() {
        // Given
        position.setPercentH(0.4);
        
        // When
        position.multiplyPercentH(0.5);
        
        // Then
        assertEquals(0.2, position.getPercentH());
        assertEquals(0.5, position.getPercentW()); // W unchanged
    }

    @Test
    @DisplayName("Should provide formatted toString")
    void testToString() {
        // Given
        Position pos = new Position(0.25, 0.75);
        
        // When
        String result = pos.toString();
        
        // Then
        assertEquals("P[0.3 0.8]", result); // Formatted to 1 decimal place
    }

    @Test
    @DisplayName("Should handle values outside 0-1 range")
    void testValuesOutsideNormalRange() {
        // Positions can exceed 0-1 range for outside positions
        
        // When
        Position outside = new Position(-0.5, 1.5);
        
        // Then
        assertEquals(-0.5, outside.getPercentW());
        assertEquals(1.5, outside.getPercentH());
        
        // Add can push outside range
        Position pos = new Position(0.8, 0.9);
        pos.addPercentW(0.5);
        pos.addPercentH(0.3);
        
        assertEquals(1.3, pos.getPercentW());
        assertEquals(1.2, pos.getPercentH());
    }

    @TestFactory
    @DisplayName("Position usage scenarios")
    Stream<DynamicTest> testUsageScenarios() {
        return Stream.of(
            dynamicTest("Center click position", () -> {
                Position center = new Position();
                assertEquals(0.5, center.getPercentW());
                assertEquals(0.5, center.getPercentH());
            }),
            
            dynamicTest("Corner positions", () -> {
                Position topLeft = new Position(Positions.Name.TOPLEFT);
                assertEquals(0.0, topLeft.getPercentW());
                assertEquals(0.0, topLeft.getPercentH());
                
                Position bottomRight = new Position(Positions.Name.BOTTOMRIGHT);
                assertEquals(1.0, bottomRight.getPercentW());
                assertEquals(1.0, bottomRight.getPercentH());
            }),
            
            dynamicTest("Quarter positions", () -> {
                Position topQuarter = new Position(0.5, 0.25);
                assertEquals(0.5, topQuarter.getPercentW());
                assertEquals(0.25, topQuarter.getPercentH());
            }),
            
            dynamicTest("Offset from corner", () -> {
                // 10% offset from top-left
                Position offsetCorner = new Position(Positions.Name.TOPLEFT, 0.1, 0.1);
                assertEquals(0.1, offsetCorner.getPercentW());
                assertEquals(0.1, offsetCorner.getPercentH());
            }),
            
            dynamicTest("Dynamic adjustment", () -> {
                Position pos = new Position(0.3, 0.3);
                // Move 20% right and down
                pos.addPercentW(0.2);
                pos.addPercentH(0.2);
                assertEquals(0.5, pos.getPercentW());
                assertEquals(0.5, pos.getPercentH());
            })
        );
    }

    @Test
    @DisplayName("Should serialize and deserialize to/from JSON")
    void testJacksonSerialization() throws Exception {
        // Given
        Position original = new Position(0.33, 0.67);
        
        // When - Serialize
        String json = objectMapper.writeValueAsString(original);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"percentW\":0.33"));
        assertTrue(json.contains("\"percentH\":0.67"));
        
        // When - Deserialize
        Position deserialized = objectMapper.readValue(json, Position.class);
        
        // Then
        assertEquals(original.getPercentW(), deserialized.getPercentW());
        assertEquals(original.getPercentH(), deserialized.getPercentH());
    }

    @Test
    @DisplayName("Should handle method chaining for transformations")
    void testMethodChaining() {
        // Given
        Position pos = new Position(0.2, 0.2);
        
        // When - Chain operations
        pos.addPercentW(0.1);
        pos.addPercentH(0.1);
        pos.multiplyPercentW(2);
        pos.multiplyPercentH(2);
        
        // Then
        assertEquals(0.6, pos.getPercentW(), 0.001); // (0.2 + 0.1) * 2
        assertEquals(0.6, pos.getPercentH(), 0.001); // (0.2 + 0.1) * 2
    }

    @Test
    @DisplayName("Should handle edge percentage values")
    void testEdgePercentageValues() {
        // Test with 0% and 100%
        Position zeroPercent = new Position(0, 0);
        assertEquals(0.0, zeroPercent.getPercentW());
        assertEquals(0.0, zeroPercent.getPercentH());
        
        Position hundredPercent = new Position(100, 100);
        assertEquals(1.0, hundredPercent.getPercentW());
        assertEquals(1.0, hundredPercent.getPercentH());
    }

    @Test
    @DisplayName("Should handle negative operations")
    void testNegativeOperations() {
        // Given
        Position pos = new Position(0.5, 0.5);
        
        // When - Add negative values
        pos.addPercentW(-0.3);
        pos.addPercentH(-0.2);
        
        // Then
        assertEquals(0.2, pos.getPercentW());
        assertEquals(0.3, pos.getPercentH());
        
        // When - Multiply by negative
        pos.multiplyPercentW(-1);
        pos.multiplyPercentH(-2);
        
        // Then
        assertEquals(-0.2, pos.getPercentW());
        assertEquals(-0.6, pos.getPercentH());
    }

    @Test
    @DisplayName("Should handle zero multiplication")
    void testZeroMultiplication() {
        // Given
        Position pos = new Position(0.5, 0.5);
        
        // When
        pos.multiplyPercentW(0);
        pos.multiplyPercentH(0);
        
        // Then
        assertEquals(0.0, pos.getPercentW());
        assertEquals(0.0, pos.getPercentH());
    }

    @Test
    @DisplayName("Should maintain precision for calculations")
    void testPrecision() {
        // Given
        Position pos = new Position(1.0/3.0, 2.0/3.0);
        
        // Then
        assertEquals(0.3333333333333333, pos.getPercentW());
        assertEquals(0.6666666666666666, pos.getPercentH());
        
        // When - Add small values
        pos.addPercentW(0.0000001);
        pos.addPercentH(0.0000001);
        
        // Then - Precision maintained
        assertEquals(0.3333334333333333, pos.getPercentW());
        assertEquals(0.6666667666666666, pos.getPercentH());
    }

    @Test
    @DisplayName("Should verify equals and hashCode")
    void testEqualsAndHashCode() {
        // Given
        Position pos1 = new Position(0.5, 0.5);
        Position pos2 = new Position(0.5, 0.5);
        Position pos3 = new Position(0.3, 0.7);
        
        // Then - Equals
        assertEquals(pos1, pos1); // Reflexive
        assertEquals(pos1, pos2); // Same values
        assertNotEquals(pos1, pos3); // Different values
        assertNotEquals(pos1, null); // Null safety
        assertNotEquals(pos1, "not a Position"); // Type safety
        
        // HashCode
        assertEquals(pos1.hashCode(), pos2.hashCode());
    }

    @Test
    @DisplayName("Should handle large integer percentages")
    void testLargeIntegerPercentages() {
        // Values over 100% are valid for positions outside the area
        Position outside = new Position(150, 200);
        
        // Then
        assertEquals(1.5, outside.getPercentW());
        assertEquals(2.0, outside.getPercentH());
    }

    @Test
    @DisplayName("Should demonstrate resolution independence")
    void testResolutionIndependence() {
        // Position works same regardless of actual size
        Position relativePos = new Position(0.25, 0.75);
        
        // In 100x100 area: would be (25, 75)
        // In 1920x1080 area: would be (480, 810)
        // In 800x600 area: would be (200, 450)
        
        // Position stays the same
        assertEquals(0.25, relativePos.getPercentW());
        assertEquals(0.75, relativePos.getPercentH());
    }

    @Test
    @DisplayName("Should handle common named positions")
    void testCommonNamedPositions() {
        // Test all corners and center
        Position topLeft = new Position(Positions.Name.TOPLEFT);
        assertEquals(0.0, topLeft.getPercentW());
        assertEquals(0.0, topLeft.getPercentH());
        
        Position center = new Position(Positions.Name.MIDDLEMIDDLE);
        assertEquals(0.5, center.getPercentW());
        assertEquals(0.5, center.getPercentH());
        
        Position bottomRight = new Position(Positions.Name.BOTTOMRIGHT);
        assertEquals(1.0, bottomRight.getPercentW());
        assertEquals(1.0, bottomRight.getPercentH());
    }
}