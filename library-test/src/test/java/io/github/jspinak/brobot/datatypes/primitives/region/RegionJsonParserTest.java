package io.github.jspinak.brobot.datatypes.primitives.region;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.json.utils.JsonUtils;
import org.bytedeco.opencv.opencv_core.Rect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class RegionJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic Region from JSON
     */
    @Test
    public void testParseBasicRegion() throws ConfigurationException {
        String json = """
                {
                  "x": 100,
                  "y": 200,
                  "w": 300,
                  "h": 400
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Region region = jsonParser.convertJson(jsonNode, Region.class);

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
    public void testSerializeDeserializeRegion() throws ConfigurationException {
        // Create a region
        Region region = new Region(150, 250, 350, 450);

        // Serialize
        String json = jsonUtils.toJsonSafe(region);
        System.out.println("DEBUG: Serialized Region: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Region deserializedRegion = jsonParser.convertJson(jsonNode, Region.class);

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
    public void testRegionOperations() {
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
    public void testMinusOperation() {
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
    public void testSize() {
        Region region = new Region(0, 0, 100, 200);
        assertEquals(20000, region.size());
    }

    /**
     * Test X2 and Y2 getters
     */
    @Test
    public void testX2Y2() {
        Region region = new Region(10, 20, 30, 40);
        assertEquals(40, region.x2());
        assertEquals(60, region.y2());
    }

    /**
     * Test getJavaCVRect conversion
     */
    @Test
    public void testGetJavaCVRect() {
        Region region = new Region(10, 20, 30, 40);
        Rect rect = region.getJavaCVRect();

        assertEquals(10, rect.x());
        assertEquals(20, rect.y());
        assertEquals(30, rect.width());
        assertEquals(40, rect.height());
    }

    /**
     * Test grid functionality
     */
    @Test
    public void testGrid() {
        Region region = new Region(0, 0, 300, 200);

        // Create a 2x3 grid
        List<Region> gridRegions = region.getGridRegions(2, 3);

        // Should have 6 cells
        assertEquals(6, gridRegions.size());

        // First cell should be at (0,0) with width 100 and height 100
        assertEquals(0, gridRegions.getFirst().x());
        assertEquals(0, gridRegions.getFirst().y());
        assertEquals(100, gridRegions.getFirst().w());
        assertEquals(100, gridRegions.getFirst().h());
    }

    /**
     * Test isDefined method
     */
    @Test
    public void testIsDefined() {
        // A region with non-default values should be defined
        Region definedRegion = new Region(10, 20, 30, 40);
        assertTrue(definedRegion.isDefined());

        // A region with all default values should not be defined
        Region defaultRegion = new Region();
        assertFalse(defaultRegion.isDefined());
    }
}