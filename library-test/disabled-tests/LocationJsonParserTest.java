package io.github.jspinak.brobot.model.location;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LocationJsonParserTest {
    
    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a Location defined by x,y coordinates from JSON
     */
    @Test
    public void testParseLocationWithXY() throws ConfigurationException {
        String json = """
                {
                  "name": "XYLocation",
                  "x": 100,
                  "y": 200,
                  "offsetX": 5,
                  "offsetY": 10
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Location location = jsonParser.convertJson(jsonNode, Location.class);

        assertNotNull(location);
        assertEquals("XYLocation", location.getName());
        assertEquals(105, location.getCalculatedX());
        assertEquals(210, location.getCalculatedY());
        assertEquals(5, location.getOffsetX());
        assertEquals(10, location.getOffsetY());
        assertNull(location.getRegion());
    }

    /**
     * Test parsing a Location defined by region and position from JSON
     */
    @Test
    public void testParseLocationWithRegionAndPosition() throws ConfigurationException {
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

        JsonNode jsonNode = jsonParser.parseJson(json);
        Location location = jsonParser.convertJson(jsonNode, Location.class);

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

    /**
     * Test serializing and deserializing a Location defined by x,y
     */
    @Test
    public void testSerializeDeserializeLocationXY() throws ConfigurationException {
        // Create a location defined by x,y
        Location location = new Location(150, 250);
        location.setName("SerializedXYLocation");
        location.setOffsetX(15);
        location.setOffsetY(25);

        // Serialize
        String json = jsonUtils.toJsonSafe(location);
        System.out.println("DEBUG: Serialized XY Location: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Location deserializedLocation = jsonParser.convertJson(jsonNode, Location.class);

        // Verify
        assertNotNull(deserializedLocation);
        assertEquals("SerializedXYLocation", deserializedLocation.getName());
        assertEquals(165, deserializedLocation.getCalculatedX());
        assertEquals(275, deserializedLocation.getCalculatedY());
        assertEquals(15, deserializedLocation.getOffsetX());
        assertEquals(25, deserializedLocation.getOffsetY());
    }

    /**
     * Test serializing and deserializing a Location defined by region
     */
    @Test
    public void testSerializeDeserializeLocationRegion() throws ConfigurationException {
        // Create a location defined by region
        Region region = new Region(150, 250, 350, 450);
        Position position = new Position(0.33, 0.67);
        Location location = new Location(region, position);
        location.setName("SerializedRegionLocation");
        location.setAnchor(Positions.Name.TOPLEFT);

        // Serialize
        String json = jsonUtils.toJsonSafe(location);
        System.out.println("DEBUG: Serialized Region Location: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Location deserializedLocation = jsonParser.convertJson(jsonNode, Location.class);

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

    /**
     * Test the various constructors
     */
    @Test
    public void testLocationConstructors() {
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

    /**
     * Test the equals method
     */
    @Test
    public void testEquals() {
        // Create two identical locations
        Location loc1 = new Location(100, 200);
        Location loc2 = new Location(100, 200);

        // Should be equal
        assertTrue(loc1.equals(loc2));

        // Create a different location
        Location loc3 = new Location(150, 250);

        // Should not be equal
        assertFalse(loc1.equals(loc3));
    }

    /**
     * Test the defined method
     */
    @Test
    public void testDefined() {
        // Create a defined location
        Location loc1 = new Location(100, 200);
        assertTrue(loc1.defined());

        // Create a location with default (not explicitly defined) coordinates
        Location loc2 = new Location(0, 0);
        assertFalse(loc2.defined());

        // Create a location with a region
        Region region = new Region(100, 200, 300, 400);
        Location loc3 = new Location(region);
        assertTrue(loc3.defined());
    }

    /**
     * Test getOpposite method
     */
    @Test
    public void testGetOpposite() {
        // Create a location with region and position
        Region region = new Region(100, 200, 300, 400);
        Location loc = new Location(region, 0.25, 0.75);

        // Get opposite
        Location opposite = loc.getOpposite();

        // Should have complementary position percentages
        assertEquals(0.75, opposite.getPosition().getPercentW(), 0.001);
        assertEquals(0.25, opposite.getPosition().getPercentH(), 0.001);
    }

    /**
     * Test add method
     */
    @Test
    public void testAdd() {
        // Create two locations defined by x,y
        Location loc1 = new Location(100, 200);
        Location loc2 = new Location(50, 75);

        // Add loc2 to loc1
        loc1.add(loc2);

        // Should have summed coordinates
        assertEquals(150, loc1.getCalculatedX());
        assertEquals(275, loc1.getCalculatedY());
    }

    /**
     * Test the Builder pattern
     */
    @Test
    public void testBuilder() throws ConfigurationException {
        // Create a location with builder
        Location location = new Location.Builder()
                .called("BuilderLocation")
                .setXY(100, 200)
                .setOffsetX(10)
                .setOffsetY(20)
                .setAnchor(Positions.Name.TOPLEFT)
                .build();

        // Serialize
        String json = jsonUtils.toJsonSafe(location);
        System.out.println("DEBUG: Serialized Builder Location: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Location deserializedLocation = jsonParser.convertJson(jsonNode, Location.class);

        // Verify
        assertNotNull(deserializedLocation);
        assertEquals("BuilderLocation", deserializedLocation.getName());
        assertEquals(110, deserializedLocation.getCalculatedX());
        assertEquals(220, deserializedLocation.getCalculatedY());
        assertEquals(10, deserializedLocation.getOffsetX());
        assertEquals(20, deserializedLocation.getOffsetY());
        assertEquals(Positions.Name.TOPLEFT, deserializedLocation.getAnchor());
    }
}