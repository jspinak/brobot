package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.test.BrobotTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Location JSON parsing.
 * 
 * Key points:
 * - Location can be defined by x,y coordinates or region+position
 * - Supports offsets and anchors
 * - Has multiple constructors for different use cases
 */
@DisplayName("Location JSON Parser Tests")
class LocationJsonParserTest extends BrobotTestBase {

    private ObjectMapper objectMapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    @DisplayName("Should parse Location with x,y coordinates from JSON")
    void testParseLocationWithXY() throws Exception {
        String json = """
        {
            "name": "XYLocation",
            "x": 100,
            "y": 200,
            "offsetX": 5,
            "offsetY": 10
        }
        """;

        Location location = objectMapper.readValue(json, Location.class);

        assertNotNull(location);
        assertEquals("XYLocation", location.getName());
        assertEquals(105, location.getCalculatedX());
        assertEquals(210, location.getCalculatedY());
        assertEquals(5, location.getOffsetX());
        assertEquals(10, location.getOffsetY());
        assertNull(location.getRegion());
    }

    @Test
    @DisplayName("Should parse Location with region and position from JSON")
    void testParseLocationWithRegionAndPosition() throws Exception {
        String json = """
        {
            "name": "RegionLocation",
            "region": {
                "x": 100,
                "y": 200,
                "w": 300,
                "h": 400
            },
            "position": {
                "percentW": 0.25,
                "percentH": 0.75
            },
            "anchor": "MIDDLEMIDDLE"
        }
        """;

        Location location = objectMapper.readValue(json, Location.class);

        assertNotNull(location);
        assertEquals("RegionLocation", location.getName());
        assertNotNull(location.getRegion());
        assertEquals(100, location.getRegion().x());
        assertEquals(200, location.getRegion().y());
        assertEquals(300, location.getRegion().w());
        assertEquals(400, location.getRegion().h());
        assertNotNull(location.getPosition());
        assertEquals(0.25, location.getPosition().getPercentW(), 0.001);
        assertEquals(0.75, location.getPosition().getPercentH(), 0.001);
        assertEquals(Positions.Name.MIDDLEMIDDLE, location.getAnchor());
    }

    @Test
    @DisplayName("Should serialize and deserialize Location with x,y")
    void testSerializeDeserializeLocationXY() throws Exception {
        // Create a location defined by x,y
        Location location = new Location(150, 250);
        location.setName("SerializedXYLocation");
        location.setOffsetX(15);
        location.setOffsetY(25);

        // Serialize
        String json = objectMapper.writeValueAsString(location);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":") && json.contains("\"SerializedXYLocation\""));

        // Deserialize
        Location deserializedLocation = objectMapper.readValue(json, Location.class);

        // Verify
        assertNotNull(deserializedLocation);
        assertEquals("SerializedXYLocation", deserializedLocation.getName());
        assertEquals(165, deserializedLocation.getCalculatedX());
        assertEquals(275, deserializedLocation.getCalculatedY());
        assertEquals(15, deserializedLocation.getOffsetX());
        assertEquals(25, deserializedLocation.getOffsetY());
    }

    @Test
    @DisplayName("Should serialize and deserialize Location with region")
    void testSerializeDeserializeLocationRegion() throws Exception {
        // Create a location defined by region
        Region region = new Region(150, 250, 350, 450);
        Position position = new Position(0.33, 0.67);
        Location location = new Location(region, position);
        location.setName("SerializedRegionLocation");
        location.setAnchor(Positions.Name.TOPLEFT);

        // Serialize
        String json = objectMapper.writeValueAsString(location);
        assertNotNull(json);

        // Deserialize
        Location deserializedLocation = objectMapper.readValue(json, Location.class);

        // Verify
        assertNotNull(deserializedLocation);
        assertEquals("SerializedRegionLocation", deserializedLocation.getName());
        assertNotNull(deserializedLocation.getRegion());
        assertEquals(150, deserializedLocation.getRegion().x());
        assertEquals(250, deserializedLocation.getRegion().y());
        assertEquals(350, deserializedLocation.getRegion().w());
        assertEquals(450, deserializedLocation.getRegion().h());
        assertNotNull(deserializedLocation.getPosition());
        assertEquals(0.33, deserializedLocation.getPosition().getPercentW(), 0.001);
        assertEquals(0.67, deserializedLocation.getPosition().getPercentH(), 0.001);
        assertEquals(Positions.Name.TOPLEFT, deserializedLocation.getAnchor());
    }

    @Test
    @DisplayName("Should test Location constructors")
    void testLocationConstructors() {
        // Test basic x,y constructor
        Location loc1 = new Location(100, 200);
        assertEquals(100, loc1.getCalculatedX());
        assertEquals(200, loc1.getCalculatedY());

        // Test region constructor
        Region region = new Region(100, 200, 300, 400);
        Location loc2 = new Location(region);
        assertNotNull(loc2.getRegion());
        assertNotNull(loc2.getPosition());
        assertEquals(0.5, loc2.getPosition().getPercentW(), 0.001); // Default centered

        // Test region with position name constructor
        Location loc3 = new Location(region, Positions.Name.TOPLEFT);
        assertNotNull(loc3.getRegion());
        assertNotNull(loc3.getPosition());
        assertEquals(0.0, loc3.getPosition().getPercentW(), 0.001); // Top left
        assertEquals(0.0, loc3.getPosition().getPercentH(), 0.001);

        // Test region with percentages constructor
        Location loc4 = new Location(region, 0.25, 0.75);
        assertNotNull(loc4.getRegion());
        assertNotNull(loc4.getPosition());
        assertEquals(0.25, loc4.getPosition().getPercentW(), 0.001);
        assertEquals(0.75, loc4.getPosition().getPercentH(), 0.001);
    }

    @Test
    @DisplayName("Should test equals method")
    void testEquals() {
        // Create two identical locations
        Location loc1 = new Location(100, 200);
        Location loc2 = new Location(100, 200);

        // Should be equal
        assertEquals(loc1, loc2);

        // Create a different location
        Location loc3 = new Location(150, 250);

        // Should not be equal
        assertNotEquals(loc1, loc3);
    }

    @Test
    @DisplayName("Should test defined method")
    void testDefined() {
        // Create a defined location
        Location loc1 = new Location(100, 200);
        assertTrue(loc1.defined());

        // Location(0, 0) is considered defined (x >= 0, y >= 0)
        Location loc2 = new Location(0, 0);
        assertTrue(loc2.defined());

        // Create a location with negative coordinates
        Location loc3 = new Location(-1, -1);
        assertFalse(loc3.defined());

        // Create a location with a region
        Region region = new Region(100, 200, 300, 400);
        Location loc4 = new Location(region);
        assertTrue(loc4.defined());
    }

    @Test
    @DisplayName("Should test getOpposite method")
    void testGetOpposite() {
        // Create a location with region and position
        Region region = new Region(100, 200, 300, 400);
        Location loc = new Location(region, 0.25, 0.75);

        // Get opposite
        Location opposite = loc.getOpposite();

        // Should have complementary position percentages
        assertEquals(0.75, opposite.getPosition().getPercentW(), 0.001);
        assertEquals(0.25, opposite.getPosition().getPercentH(), 0.001);
    }

    @Test
    @DisplayName("Should test add method")
    void testAdd() {
        // Create two locations defined by x,y
        Location loc1 = new Location(100, 200);
        Location loc2 = new Location(50, 75);

        // Add loc2 to loc1
        loc1.add(loc2);

        // Should have summed coordinates
        assertEquals(150, loc1.getCalculatedX());
        assertEquals(275, loc1.getCalculatedY());
    }

    @Test
    @DisplayName("Should test Builder pattern")
    void testBuilder() throws Exception {
        // Create a location with builder
        Location location = new Location.Builder()
                .called("BuilderLocation")
                .setXY(100, 200)
                .setOffsetX(10)
                .setOffsetY(20)
                .setAnchor(Positions.Name.TOPLEFT)
                .build();

        // Serialize
        String json = objectMapper.writeValueAsString(location);
        assertNotNull(json);

        // Deserialize
        Location deserializedLocation = objectMapper.readValue(json, Location.class);

        // Verify
        assertNotNull(deserializedLocation);
        assertEquals("BuilderLocation", deserializedLocation.getName());
        assertEquals(110, deserializedLocation.getCalculatedX());
        assertEquals(220, deserializedLocation.getCalculatedY());
        assertEquals(10, deserializedLocation.getOffsetX());
        assertEquals(20, deserializedLocation.getOffsetY());
        assertEquals(Positions.Name.TOPLEFT, deserializedLocation.getAnchor());
    }

    @Test
    @DisplayName("Should parse Location with position name from JSON")
    void testParseLocationWithPositionName() throws Exception {
        // The Location class uses Positions.Name directly, not in Position object
        String json = """
        {
            "name": "PositionNameLocation",
            "anchor": "TOPLEFT"
        }
        """;

        Location location = objectMapper.readValue(json, Location.class);

        assertNotNull(location);
        assertEquals("PositionNameLocation", location.getName());
        assertEquals(Positions.Name.TOPLEFT, location.getAnchor());
        // When created with anchor only, position might be default (0.5, 0.5)
        // unless we create it with a region as well
    }

    @Test
    @DisplayName("Should use screen center location")
    void testScreenCenterLocation() {
        // Test creating location at screen center
        Location center = new Location(Positions.Name.MIDDLEMIDDLE);
        
        assertNotNull(center);
        assertNotNull(center.getPosition());
        assertEquals(0.5, center.getPosition().getPercentW(), 0.001);
        assertEquals(0.5, center.getPosition().getPercentH(), 0.001);
    }
}