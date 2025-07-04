package io.github.jspinak.brobot.model.element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.bytedeco.opencv.opencv_core.Rect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Region JSON serialization/deserialization without Spring dependencies.
 * Migrated from library-test module.
 */
class RegionJsonSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Test parsing a basic Region from JSON
     */
    @Test
    void testParseBasicRegion() throws Exception {
        String json = """
                {
                  "x": 100,
                  "y": 200,
                  "w": 300,
                  "h": 400
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        Region region = objectMapper.treeToValue(jsonNode, Region.class);

        assertNotNull(region);
        assertEquals(100, region.x());
        assertEquals(200, region.y());
        assertEquals(300, region.w());
        assertEquals(400, region.h());
    }

    /**
     * Test serializing and deserializing a Region
     */
    @Test
    void testSerializeDeserializeRegion() throws Exception {
        // Create a region
        Region region = new Region(150, 250, 350, 450);

        // Serialize
        String json = objectMapper.writeValueAsString(region);
        assertNotNull(json);

        // Deserialize
        Region deserializedRegion = objectMapper.readValue(json, Region.class);

        // Verify
        assertNotNull(deserializedRegion);
        assertEquals(150, deserializedRegion.x());
        assertEquals(250, deserializedRegion.y());
        assertEquals(350, deserializedRegion.w());
        assertEquals(450, deserializedRegion.h());
    }

    /**
     * Test basic operations of Region
     */
    @Test
    void testRegionOperations() {
        // Create regions
        Region r1 = new Region(100, 100, 200, 200);
        Region r2 = new Region(200, 200, 200, 200);
        Region r3 = new Region(400, 400, 200, 200);

        // Test overlap
        assertTrue(r1.overlaps(r2));
        assertFalse(r1.overlaps(r3));

        // Test contains
        Region smallRegion = new Region(150, 150, 50, 50);
        assertTrue(r1.contains(smallRegion));
        assertFalse(r2.contains(smallRegion));

        // Test getOverlappingRegion
        Optional<Region> overlap = r1.getOverlappingRegion(r2);
        assertTrue(overlap.isPresent());
        assertEquals(200, overlap.get().x());
        assertEquals(200, overlap.get().y());
        assertEquals(100, overlap.get().w());
        assertEquals(100, overlap.get().h());

        // Test getUnion
        Region union = r1.getUnion(r2);
        assertEquals(100, union.x());
        assertEquals(100, union.y());
        assertEquals(300, union.w());
        assertEquals(300, union.h());
    }

    /**
     * Test the minus operation
     */
    @Test
    void testMinusOperation() {
        // Create regions
        Region r1 = new Region(100, 100, 300, 300);
        Region r2 = new Region(200, 200, 100, 100);

        // Get the non-overlapping parts of r1
        List<Region> result = r1.minus(r2);

        // Should get 4 regions (L shape around r2)
        assertEquals(4, result.size());

        // The total area should be r1's area minus the overlap area
        int totalArea = result.stream().mapToInt(Region::size).sum();
        int r1Area = r1.size();
        int overlapArea = 100 * 100; // r2 is completely inside r1
        assertEquals(r1Area - overlapArea, totalArea);
    }

    /**
     * Test the size calculation
     */
    @Test
    void testSize() {
        Region region = new Region(0, 0, 100, 200);
        assertEquals(20000, region.size());
    }

    /**
     * Test X2 and Y2 getters
     */
    @Test
    void testX2Y2() {
        Region region = new Region(10, 20, 30, 40);
        assertEquals(40, region.x2());
        assertEquals(60, region.y2());
    }

    /**
     * Test getJavaCVRect conversion
     */
    @Test
    void testGetJavaCVRect() {
        Region region = new Region(10, 20, 30, 40);
        Rect rect = region.getJavaCVRect();

        assertEquals(10, rect.x());
        assertEquals(20, rect.y());
        assertEquals(30, rect.width());
        assertEquals(40, rect.height());
    }

    /**
     * Test isDefined method
     */
    @Test
    void testIsDefined() {
        // A region with non-default values should be defined
        Region definedRegion = new Region(10, 20, 30, 40);
        assertTrue(definedRegion.isDefined());

        // A region with all default values should not be defined
        Region defaultRegion = new Region();
        assertFalse(defaultRegion.isDefined());
    }

    /**
     * Test JSON with nested regions (as part of complex objects)
     */
    @Test
    void testNestedRegionSerialization() throws Exception {
        // Create a complex object with a region
        TestRegionContainer container = new TestRegionContainer();
        container.setName("Container1");
        container.setRegion(new Region(50, 60, 70, 80));

        // Serialize
        String json = objectMapper.writeValueAsString(container);
        assertNotNull(json);
        assertTrue(json.contains("\"name\" : \"Container1\""));
        assertTrue(json.contains("\"x\" : 50"));

        // Deserialize
        TestRegionContainer deserialized = objectMapper.readValue(json, TestRegionContainer.class);
        assertNotNull(deserialized);
        assertEquals("Container1", deserialized.getName());
        assertNotNull(deserialized.getRegion());
        assertEquals(50, deserialized.getRegion().x());
        assertEquals(60, deserialized.getRegion().y());
        assertEquals(70, deserialized.getRegion().w());
        assertEquals(80, deserialized.getRegion().h());
    }

    // Helper class for testing nested serialization
    static class TestRegionContainer {
        private String name;
        private Region region;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Region getRegion() {
            return region;
        }

        public void setRegion(Region region) {
            this.region = region;
        }
    }
}