package io.github.jspinak.brobot.model.element;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Comprehensive tests for the Anchor class which defines relative positioning constraints
 * between visual elements in Brobot.
 */
@DisplayName("Anchor Model Tests")
public class AnchorTest extends BrobotTestBase {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should create anchor with default constructor")
    void testDefaultConstructor() {
        // Given & When
        Anchor anchor = new Anchor();
        
        // Then
        assertNull(anchor.getAnchorInNewDefinedRegion());
        assertNull(anchor.getPositionInMatch());
    }

    @Test
    @DisplayName("Should create anchor with parameterized constructor")
    void testParameterizedConstructor() {
        // Given
        Positions.Name anchorPosition = Positions.Name.TOPLEFT;
        Position position = new Position(10, 20);
        
        // When
        Anchor anchor = new Anchor(anchorPosition, position);
        
        // Then
        assertEquals(anchorPosition, anchor.getAnchorInNewDefinedRegion());
        assertEquals(position, anchor.getPositionInMatch());
        assertSame(position, anchor.getPositionInMatch()); // Verify reference
    }

    @ParameterizedTest
    @EnumSource(Positions.Name.class)
    @DisplayName("Should handle all Positions.Name enum values")
    void testAllPositionsEnumValues(Positions.Name positionName) {
        // Given
        Position position = new Position(0, 0);
        
        // When
        Anchor anchor = new Anchor(positionName, position);
        
        // Then
        assertEquals(positionName, anchor.getAnchorInNewDefinedRegion());
        assertNotNull(anchor.toString());
        assertTrue(anchor.toString().contains(positionName.toString()));
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Given
        Position position1 = new Position(10, 20);
        Position position2 = new Position(10, 20);
        Position position3 = new Position(50, 60);
        
        Anchor anchor1 = new Anchor(Positions.Name.TOPLEFT, position1);
        Anchor anchor2 = new Anchor(Positions.Name.TOPLEFT, position2);
        Anchor anchor3 = new Anchor(Positions.Name.BOTTOMRIGHT, position1);
        Anchor anchor4 = new Anchor(Positions.Name.TOPLEFT, position3);
        Anchor anchor5 = new Anchor(Positions.Name.TOPLEFT, position1);
        
        // Then - Reflexive
        assertEquals(anchor1, anchor1);
        assertEquals(anchor1.hashCode(), anchor1.hashCode());
        
        // Symmetric
        assertEquals(anchor1, anchor2);
        assertEquals(anchor2, anchor1);
        assertEquals(anchor1.hashCode(), anchor2.hashCode());
        
        // Transitive
        assertEquals(anchor1, anchor2);
        assertEquals(anchor2, anchor5);
        assertEquals(anchor1, anchor5);
        
        // Not equal when different anchor position
        assertNotEquals(anchor1, anchor3);
        
        // Not equal when different position in match
        assertNotEquals(anchor1, anchor4);
        
        // Null safety
        assertNotEquals(anchor1, null);
        
        // Different class
        assertNotEquals(anchor1, "not an anchor");
    }

    @Test
    @DisplayName("Should serialize and deserialize to/from JSON correctly")
    void testJacksonSerialization() throws JsonProcessingException {
        // Given
        Position position = new Position(25, 35);
        Anchor original = new Anchor(Positions.Name.MIDDLERIGHT, position);
        
        // When - Serialize
        String json = objectMapper.writeValueAsString(original);
        
        // Then - JSON contains expected fields
        assertNotNull(json);
        assertTrue(json.contains("anchorInNewDefinedRegion"));
        assertTrue(json.contains("MIDDLERIGHT"));
        assertTrue(json.contains("positionInMatch"));
        
        // When - Deserialize
        Anchor deserialized = objectMapper.readValue(json, Anchor.class);
        
        // Then - Objects are equal
        assertEquals(original, deserialized);
        assertEquals(original.getAnchorInNewDefinedRegion(), deserialized.getAnchorInNewDefinedRegion());
        assertEquals(original.getPositionInMatch(), deserialized.getPositionInMatch());
        assertNotSame(original, deserialized); // Different instances
    }

    @Test
    @DisplayName("Should handle null values in serialization")
    void testNullSerialization() throws JsonProcessingException {
        // Given
        Anchor anchor = new Anchor();
        anchor.setAnchorInNewDefinedRegion(null);
        anchor.setPositionInMatch(null);
        
        // When
        String json = objectMapper.writeValueAsString(anchor);
        Anchor deserialized = objectMapper.readValue(json, Anchor.class);
        
        // Then
        assertNull(deserialized.getAnchorInNewDefinedRegion());
        assertNull(deserialized.getPositionInMatch());
    }

    @Test
    @DisplayName("Should handle JsonIgnoreProperties annotation")
    void testJsonIgnoreUnknownProperties() throws JsonProcessingException {
        // Given - JSON with unknown property
        String jsonWithUnknown = "{\"anchorInNewDefinedRegion\":\"TOPLEFT\"," +
                                 "\"positionInMatch\":{\"x\":10,\"y\":20,\"xOffsetPercent\":30,\"yOffsetPercent\":40}," +
                                 "\"unknownProperty\":\"unknown value\"}";
        
        // When - Should not throw exception due to @JsonIgnoreProperties
        Anchor anchor = objectMapper.readValue(jsonWithUnknown, Anchor.class);
        
        // Then
        assertEquals(Positions.Name.TOPLEFT, anchor.getAnchorInNewDefinedRegion());
        assertNotNull(anchor.getPositionInMatch());
    }

    @Test
    @DisplayName("Should generate meaningful toString output")
    void testToString() {
        // Given
        Position position = new Position(100, 50);
        Anchor anchor = new Anchor(Positions.Name.BOTTOMLEFT, position);
        
        // When
        String toString = anchor.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("Anchor"));
        assertTrue(toString.contains("anchorInNewDefinedRegion=BOTTOMLEFT"));
        assertTrue(toString.contains("positionInMatch="));
    }

    @ParameterizedTest
    @CsvSource({
        "TOPLEFT,0,0,100,100",
        "TOPRIGHT,100,0,100,100", 
        "BOTTOMLEFT,0,100,100,100",
        "BOTTOMRIGHT,100,100,100,100",
        "MIDDLELEFT,0,50,100,100",
        "MIDDLERIGHT,100,50,100,100",
        "TOPMIDDLE,50,0,100,100",
        "BOTTOMMIDDLE,50,100,100,100",
        "MIDDLEMIDDLE,50,50,100,100"
    })
    @DisplayName("Should work with common anchor positions")
    void testCommonAnchorScenarios(String positionName, int x, int y, int width, int height) {
        // Given
        Positions.Name anchorPosition = Positions.Name.valueOf(positionName);
        Position position = new Position(x, y);
        
        // When
        Anchor anchor = new Anchor(anchorPosition, position);
        
        // Then
        assertEquals(anchorPosition, anchor.getAnchorInNewDefinedRegion());
        assertEquals((double)x/100, anchor.getPositionInMatch().getPercentW());
        assertEquals((double)y/100, anchor.getPositionInMatch().getPercentH());
    }

    @TestFactory
    @DisplayName("Getter and Setter tests")
    Stream<DynamicTest> testGettersAndSetters() {
        return Stream.of(
            dynamicTest("Set and get anchorInNewDefinedRegion", () -> {
                Anchor anchor = new Anchor();
                anchor.setAnchorInNewDefinedRegion(Positions.Name.TOPLEFT);
                assertEquals(Positions.Name.TOPLEFT, anchor.getAnchorInNewDefinedRegion());
            }),
            dynamicTest("Set and get positionInMatch", () -> {
                Anchor anchor = new Anchor();
                Position position = new Position(10, 20);
                anchor.setPositionInMatch(position);
                assertEquals(position, anchor.getPositionInMatch());
            }),
            dynamicTest("Modify anchorInNewDefinedRegion after creation", () -> {
                Anchor anchor = new Anchor(Positions.Name.TOPLEFT, new Position());
                anchor.setAnchorInNewDefinedRegion(Positions.Name.BOTTOMRIGHT);
                assertEquals(Positions.Name.BOTTOMRIGHT, anchor.getAnchorInNewDefinedRegion());
            }),
            dynamicTest("Modify positionInMatch after creation", () -> {
                Position initial = new Position(10, 10);
                Position updated = new Position(20, 20);
                Anchor anchor = new Anchor(Positions.Name.TOPLEFT, initial);
                anchor.setPositionInMatch(updated);
                assertEquals(updated, anchor.getPositionInMatch());
            })
        );
    }

    @Test
    @DisplayName("Should handle typical GUI automation scenarios")
    void testTypicalAutomationScenarios() {
        // Scenario 1: Text field to the right of a label
        Position labelPosition = new Position(100, 50);
        Anchor textFieldAnchor = new Anchor(Positions.Name.MIDDLELEFT, labelPosition);
        assertEquals(Positions.Name.MIDDLELEFT, textFieldAnchor.getAnchorInNewDefinedRegion());
        
        // Scenario 2: Button below an image
        Position imagePosition = new Position(100, 100);
        Anchor buttonAnchor = new Anchor(Positions.Name.TOPMIDDLE, imagePosition);
        assertEquals(Positions.Name.TOPMIDDLE, buttonAnchor.getAnchorInNewDefinedRegion());
        
        // Scenario 3: Dropdown aligned with input
        Position inputPosition = new Position(50, 100);
        Anchor dropdownAnchor = new Anchor(Positions.Name.TOPLEFT, inputPosition);
        assertEquals(Positions.Name.TOPLEFT, dropdownAnchor.getAnchorInNewDefinedRegion());
    }

    @Test
    @DisplayName("Should handle boundary values")
    void testBoundaryValues() {
        // Zero coordinates
        Position zeroPosition = new Position(0, 0);
        Anchor zeroAnchor = new Anchor(Positions.Name.TOPLEFT, zeroPosition);
        assertEquals(0.0, zeroAnchor.getPositionInMatch().getPercentW());
        assertEquals(0.0, zeroAnchor.getPositionInMatch().getPercentH());
        
        // Large values
        Position largePosition = new Position(100.0, 100.0);
        Anchor largeAnchor = new Anchor(Positions.Name.BOTTOMRIGHT, largePosition);
        assertEquals(100.0, largeAnchor.getPositionInMatch().getPercentW());
        
        // Negative values (could occur with offsets)
        Position negativePosition = new Position(-0.1, -0.2);
        Anchor negativeAnchor = new Anchor(Positions.Name.MIDDLEMIDDLE, negativePosition);
        assertEquals(-0.1, negativeAnchor.getPositionInMatch().getPercentW());
        assertEquals(-0.2, negativeAnchor.getPositionInMatch().getPercentH());
    }
}