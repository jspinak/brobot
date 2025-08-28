package io.github.jspinak.brobot.model.element;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Comprehensive tests for the Anchors class which manages collections of Anchor objects
 * for complex relative positioning constraints.
 */
@DisplayName("Anchors Collection Tests")
public class AnchorsTest extends BrobotTestBase {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Anchors anchors;
    private Anchor testAnchor1;
    private Anchor testAnchor2;
    private Anchor testAnchor3;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        anchors = new Anchors();
        testAnchor1 = new Anchor(Positions.Name.TOPLEFT, new Position(10, 20));
        testAnchor2 = new Anchor(Positions.Name.BOTTOMRIGHT, new Position(50, 60));
        testAnchor3 = new Anchor(Positions.Name.MIDDLEMIDDLE, new Position(50, 50));
    }

    @Test
    @DisplayName("Should create empty anchors collection")
    void testDefaultConstructor() {
        // Given & When
        Anchors emptyAnchors = new Anchors();
        
        // Then
        assertNotNull(emptyAnchors);
        assertNotNull(emptyAnchors.getAnchorList());
        assertEquals(0, emptyAnchors.size());
        assertTrue(emptyAnchors.getAnchorList().isEmpty());
    }

    @Test
    @DisplayName("Should add single anchor to collection")
    void testAddSingleAnchor() {
        // When
        anchors.add(testAnchor1);
        
        // Then
        assertEquals(1, anchors.size());
        assertEquals(testAnchor1, anchors.getAnchorList().get(0));
        assertSame(testAnchor1, anchors.getAnchorList().get(0));
    }

    @Test
    @DisplayName("Should add multiple anchors to collection")
    void testAddMultipleAnchors() {
        // When
        anchors.add(testAnchor1);
        anchors.add(testAnchor2);
        anchors.add(testAnchor3);
        
        // Then
        assertEquals(3, anchors.size());
        assertEquals(testAnchor1, anchors.getAnchorList().get(0));
        assertEquals(testAnchor2, anchors.getAnchorList().get(1));
        assertEquals(testAnchor3, anchors.getAnchorList().get(2));
    }

    @Test
    @DisplayName("Should handle adding null anchor")
    void testAddNullAnchor() {
        // When
        anchors.add(null);
        
        // Then - null is added to the list
        assertEquals(1, anchors.size());
        assertNull(anchors.getAnchorList().get(0));
    }

    @Test
    @DisplayName("Should allow duplicate anchors")
    void testAddDuplicateAnchors() {
        // When
        anchors.add(testAnchor1);
        anchors.add(testAnchor1);
        anchors.add(testAnchor1);
        
        // Then
        assertEquals(3, anchors.size());
        assertSame(testAnchor1, anchors.getAnchorList().get(0));
        assertSame(testAnchor1, anchors.getAnchorList().get(1));
        assertSame(testAnchor1, anchors.getAnchorList().get(2));
    }

    @Test
    @DisplayName("Should serialize and deserialize to/from JSON correctly")
    void testJacksonSerialization() throws JsonProcessingException {
        // Given
        anchors.add(testAnchor1);
        anchors.add(testAnchor2);
        
        // When - Serialize
        String json = objectMapper.writeValueAsString(anchors);
        
        // Then - JSON contains expected structure
        assertNotNull(json);
        assertTrue(json.contains("anchorList"));
        assertTrue(json.contains("TOPLEFT"));
        assertTrue(json.contains("BOTTOMRIGHT"));
        
        // When - Deserialize
        Anchors deserialized = objectMapper.readValue(json, Anchors.class);
        
        // Then - Collections are equal
        assertEquals(anchors.size(), deserialized.size());
        assertEquals(anchors.getAnchorList().size(), deserialized.getAnchorList().size());
        assertEquals(anchors.getAnchorList().get(0), deserialized.getAnchorList().get(0));
        assertEquals(anchors.getAnchorList().get(1), deserialized.getAnchorList().get(1));
    }

    @Test
    @DisplayName("Should handle empty collection serialization")
    void testEmptyCollectionSerialization() throws JsonProcessingException {
        // Given - empty anchors
        
        // When
        String json = objectMapper.writeValueAsString(anchors);
        Anchors deserialized = objectMapper.readValue(json, Anchors.class);
        
        // Then
        assertEquals(0, deserialized.size());
        assertTrue(deserialized.getAnchorList().isEmpty());
    }

    @Test
    @DisplayName("Should handle JsonIgnoreProperties annotation")
    void testJsonIgnoreUnknownProperties() throws JsonProcessingException {
        // Given - JSON with unknown property
        String jsonWithUnknown = "{\"anchorList\":[" +
            "{\"anchorInNewDefinedRegion\":\"TOPLEFT\"," +
            "\"positionInMatch\":{\"x\":10,\"y\":20,\"xOffsetPercent\":30,\"yOffsetPercent\":40}}]," +
            "\"unknownProperty\":\"unknown value\"}";
        
        // When - Should not throw exception
        Anchors deserialized = objectMapper.readValue(jsonWithUnknown, Anchors.class);
        
        // Then
        assertEquals(1, deserialized.size());
        assertEquals(Positions.Name.TOPLEFT, deserialized.getAnchorList().get(0).getAnchorInNewDefinedRegion());
    }

    @Test
    @DisplayName("Should generate meaningful toString output")
    void testToString() {
        // Given
        anchors.add(testAnchor1);
        anchors.add(testAnchor2);
        
        // When
        String toString = anchors.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("Anchors:"));
        assertTrue(toString.contains("TOPLEFT"));
        assertTrue(toString.contains("BOTTOMRIGHT"));
        assertTrue(toString.contains("Anchor:"));
    }

    @Test
    @DisplayName("Should handle toString with empty collection")
    void testToStringEmpty() {
        // When
        String toString = anchors.toString();
        
        // Then
        assertNotNull(toString);
        assertEquals("Anchors:", toString);
    }

    @Test
    @DisplayName("Should handle toString with null anchor")
    void testToStringWithNull() {
        // Given
        anchors.add(null);
        anchors.add(testAnchor1);
        
        // When - Should not throw exception
        String toString = anchors.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("Anchors:"));
        assertTrue(toString.contains("null"));
        assertTrue(toString.contains("TOPLEFT"));
    }

    @Test
    @DisplayName("Should provide direct access to anchor list")
    void testGetAndSetAnchorList() {
        // Given
        List<Anchor> newList = new ArrayList<>();
        newList.add(testAnchor1);
        newList.add(testAnchor2);
        
        // When
        anchors.setAnchorList(newList);
        
        // Then
        assertEquals(2, anchors.size());
        assertSame(newList, anchors.getAnchorList());
        
        // Modification through getter affects the collection
        anchors.getAnchorList().add(testAnchor3);
        assertEquals(3, anchors.size());
    }

    @Test
    @DisplayName("Should handle null anchor list")
    void testSetNullAnchorList() {
        // When
        anchors.setAnchorList(null);
        
        // Then
        assertNull(anchors.getAnchorList());
        // size() will throw NPE if called
        assertThrows(NullPointerException.class, () -> anchors.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 50, 100})
    @DisplayName("Should handle collections of various sizes")
    void testVariousCollectionSizes(int size) {
        // Given & When
        for (int i = 0; i < size; i++) {
            Position pos = new Position(i, i);
            Anchor anchor = new Anchor(Positions.Name.TOPLEFT, pos);
            anchors.add(anchor);
        }
        
        // Then
        assertEquals(size, anchors.size());
        assertEquals(size, anchors.getAnchorList().size());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Given
        Anchors anchors1 = new Anchors();
        anchors1.add(testAnchor1);
        anchors1.add(testAnchor2);
        
        Anchors anchors2 = new Anchors();
        anchors2.add(testAnchor1);
        anchors2.add(testAnchor2);
        
        Anchors anchors3 = new Anchors();
        anchors3.add(testAnchor1);
        
        Anchors anchors4 = new Anchors();
        anchors4.add(testAnchor2);
        anchors4.add(testAnchor1); // Different order
        
        // Then - Reflexive
        assertEquals(anchors1, anchors1);
        assertEquals(anchors1.hashCode(), anchors1.hashCode());
        
        // Symmetric
        assertEquals(anchors1, anchors2);
        assertEquals(anchors2, anchors1);
        assertEquals(anchors1.hashCode(), anchors2.hashCode());
        
        // Not equal when different size
        assertNotEquals(anchors1, anchors3);
        
        // Not equal when different order (List maintains order)
        assertNotEquals(anchors1, anchors4);
        
        // Null safety
        assertNotEquals(anchors1, null);
        
        // Different class
        assertNotEquals(anchors1, "not anchors");
    }

    @TestFactory
    @DisplayName("Complex positioning scenarios")
    Stream<DynamicTest> testComplexPositioningScenarios() {
        return Stream.of(
            dynamicTest("Form field bounded by multiple elements", () -> {
                Anchors formAnchors = new Anchors();
                // Label's right edge defines field's left boundary
                Anchor labelAnchor = new Anchor(Positions.Name.MIDDLELEFT, 
                    new Position(50, 50));
                // Submit button's top edge defines field's bottom boundary  
                Anchor buttonAnchor = new Anchor(Positions.Name.BOTTOMMIDDLE,
                    new Position(70, 100));
                formAnchors.add(labelAnchor);
                formAnchors.add(buttonAnchor);
                assertEquals(2, formAnchors.size());
            }),
            
            dynamicTest("Four anchors for precise boundary definition", () -> {
                Anchors boundaryAnchors = new Anchors();
                boundaryAnchors.add(new Anchor(Positions.Name.TOPLEFT, new Position(0, 0)));
                boundaryAnchors.add(new Anchor(Positions.Name.TOPRIGHT, new Position(100, 0)));
                boundaryAnchors.add(new Anchor(Positions.Name.BOTTOMLEFT, new Position(0, 100)));
                boundaryAnchors.add(new Anchor(Positions.Name.BOTTOMRIGHT, new Position(100, 100)));
                assertEquals(4, boundaryAnchors.size());
            }),
            
            dynamicTest("Multiple fallback anchors", () -> {
                Anchors fallbackAnchors = new Anchors();
                // Primary anchor
                fallbackAnchors.add(new Anchor(Positions.Name.MIDDLEMIDDLE, new Position(50, 50)));
                // Fallback anchors if primary is not found
                fallbackAnchors.add(new Anchor(Positions.Name.TOPLEFT, new Position(0, 0)));
                fallbackAnchors.add(new Anchor(Positions.Name.BOTTOMRIGHT, new Position(80, 80)));
                assertEquals(3, fallbackAnchors.size());
            })
        );
    }

    @Test
    @DisplayName("Should handle large collections efficiently")
    void testLargeCollection() {
        // Given - 1000 anchors
        int largeSize = 1000;
        
        // When
        IntStream.range(0, largeSize).forEach(i -> {
            Position pos = new Position(i, i);
            Anchor anchor = new Anchor(Positions.Name.TOPLEFT, pos);
            anchors.add(anchor);
        });
        
        // Then
        assertEquals(largeSize, anchors.size());
        assertEquals(largeSize, anchors.getAnchorList().size());
        
        // toString should complete without stack overflow
        String toString = anchors.toString();
        assertNotNull(toString);
        assertTrue(toString.length() > 0);
    }

    @Test
    @DisplayName("Should preserve insertion order")
    void testInsertionOrderPreserved() {
        // Given
        List<Anchor> insertionOrder = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Anchor anchor = new Anchor(Positions.Name.TOPLEFT, new Position(i, i));
            insertionOrder.add(anchor);
            anchors.add(anchor);
        }
        
        // Then
        for (int i = 0; i < 10; i++) {
            assertSame(insertionOrder.get(i), anchors.getAnchorList().get(i));
        }
    }

    @Test
    @DisplayName("Should handle concurrent modifications safely")
    void testConcurrentModificationHandling() {
        // Given
        anchors.add(testAnchor1);
        anchors.add(testAnchor2);
        anchors.add(testAnchor3);
        
        // When - Get the list and modify original
        List<Anchor> list = anchors.getAnchorList();
        anchors.add(new Anchor(Positions.Name.TOPLEFT, new Position()));
        
        // Then - Both are affected since getAnchorList returns direct reference
        assertEquals(4, list.size());
        assertEquals(4, anchors.size());
    }
}