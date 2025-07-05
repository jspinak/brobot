package io.github.jspinak.brobot.model.image;

import io.github.jspinak.brobot.BrobotTestApplication;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Image;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
@TestPropertySource(properties = {"java.awt.headless=false", "brobot.mock.enabled=true"})
public class PatternJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic Pattern from JSON
     */
    @Test
    public void testParseBasicPattern() throws ConfigurationException {
        String json = """
                {
                  "name": "TestPattern",
                  "fixed": true,
                  "dynamic": false,
                  "imgpath": "test/path.png",
                  "searchRegions": {
                    "fixedRegion": {
                      "x": 10,
                      "y": 20,
                      "w": 100,
                      "h": 50
                    }
                  },
                  "targetPosition": {
                    "percentW": 0.5,
                    "percentH": 0.5
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Pattern pattern = jsonParser.convertJson(jsonNode, Pattern.class);

        assertNotNull(pattern);
        assertEquals("TestPattern", pattern.getName());
        assertTrue(pattern.isFixed());
        assertFalse(pattern.isDynamic());
        assertEquals("test/path.png", pattern.getImgpath());

        // Verify search regions
        assertNotNull(pattern.getSearchRegions());
        assertNotNull(pattern.getSearchRegions().getFixedRegion());
        assertEquals(10, pattern.getSearchRegions().getFixedRegion().x());
        assertEquals(20, pattern.getSearchRegions().getFixedRegion().y());
        assertEquals(100, pattern.getSearchRegions().getFixedRegion().w());
        assertEquals(50, pattern.getSearchRegions().getFixedRegion().h());

        // Verify target position
        assertNotNull(pattern.getTargetPosition());
        assertEquals(0.5, pattern.getTargetPosition().getPercentW(), 0.001);
        assertEquals(0.5, pattern.getTargetPosition().getPercentH(), 0.001);
    }

    /**
     * Test serializing and deserializing a Pattern
     */
    @Test
    public void testSerializeDeserializePattern() throws ConfigurationException {
        // Create a pattern
        Pattern pattern = new Pattern();
        pattern.setName("SerializedPattern");
        pattern.setFixed(true);
        pattern.setDynamic(false);
        pattern.setImgpath("test/serialized.png");

        // Set search regions
        pattern.getSearchRegions().setFixedRegion(new Region(50, 60, 200, 100));

        // Set target position
        pattern.setTargetPosition(new Position(0.75, 0.25));

        // Create an image (but it won't serialize properly by default)
        pattern.setImage(new Image(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB), "PatternImage"));

        // Serialize
        String json = jsonUtils.toJsonSafe(pattern);
        System.out.println("DEBUG: Serialized Pattern: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Pattern deserializedPattern = jsonParser.convertJson(jsonNode, Pattern.class);

        // Verify
        assertNotNull(deserializedPattern);
        assertEquals("SerializedPattern", deserializedPattern.getName());
        assertTrue(deserializedPattern.isFixed());
        assertFalse(deserializedPattern.isDynamic());
        assertEquals("test/serialized.png", deserializedPattern.getImgpath());

        // Verify search regions
        assertNotNull(deserializedPattern.getSearchRegions());
        assertNotNull(deserializedPattern.getSearchRegions().getFixedRegion());
        assertEquals(50, deserializedPattern.getSearchRegions().getFixedRegion().x());
        assertEquals(60, deserializedPattern.getSearchRegions().getFixedRegion().y());
        assertEquals(200, deserializedPattern.getSearchRegions().getFixedRegion().w());
        assertEquals(100, deserializedPattern.getSearchRegions().getFixedRegion().h());

        // Verify target position
        assertNotNull(deserializedPattern.getTargetPosition());
        assertEquals(0.75, deserializedPattern.getTargetPosition().getPercentW(), 0.001);
        assertEquals(0.25, deserializedPattern.getTargetPosition().getPercentH(), 0.001);
    }

    /**
     * Test the Builder pattern
     */
    @Test
    public void testPatternBuilder() throws ConfigurationException {
        // Create a pattern with builder
        Pattern pattern = new Pattern.Builder()
                .setName("BuilderPattern")
                .setFixed(true)
                .setFixedRegion(new Region(100, 150, 250, 120))
                .setTargetPosition(new Position(0.4, 0.6))
                .setDynamic(true)
                .build();

        // Serialize
        String json = jsonUtils.toJsonSafe(pattern);
        System.out.println("DEBUG: Serialized Builder Pattern: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Pattern deserializedPattern = jsonParser.convertJson(jsonNode, Pattern.class);

        // Verify
        assertNotNull(deserializedPattern);
        assertEquals("BuilderPattern", deserializedPattern.getName());
        assertTrue(deserializedPattern.isFixed());
        assertTrue(deserializedPattern.isDynamic());

        // Verify search regions
        assertNotNull(deserializedPattern.getSearchRegions());
        assertNotNull(deserializedPattern.getSearchRegions().getFixedRegion());
        assertEquals(100, deserializedPattern.getSearchRegions().getFixedRegion().x());
        assertEquals(150, deserializedPattern.getSearchRegions().getFixedRegion().y());
        assertEquals(250, deserializedPattern.getSearchRegions().getFixedRegion().w());
        assertEquals(120, deserializedPattern.getSearchRegions().getFixedRegion().h());

        // Verify target position
        assertNotNull(deserializedPattern.getTargetPosition());
        assertEquals(0.4, deserializedPattern.getTargetPosition().getPercentW(), 0.001);
        assertEquals(0.6, deserializedPattern.getTargetPosition().getPercentH(), 0.001);
    }

    /**
     * Test the isDefined method
     */
    @Test
    public void testIsDefined() {
        // Create a pattern with fixed region
        Pattern fixedPattern = new Pattern();
        fixedPattern.setFixed(true);
        fixedPattern.getSearchRegions().setFixedRegion(new Region(0, 0, 100, 100));

        // Should be defined
        assertTrue(fixedPattern.isDefined());

        // Create a pattern with search regions but not fixed
        Pattern searchPattern = new Pattern();
        searchPattern.setFixed(false);
        searchPattern.getSearchRegions().addSearchRegions(new Region(0, 0, 100, 100));

        // Should be defined
        assertTrue(searchPattern.isDefined());

        // Create a pattern with no regions
        Pattern undefinedPattern = new Pattern();

        // Should not be defined
        assertFalse(undefinedPattern.isDefined());
    }

    /**
     * Test getRegion and getRegions methods
     */
    @Test
    public void testGetRegionMethods() {
        // Create a pattern with fixed region
        Pattern fixedPattern = new Pattern();
        fixedPattern.setFixed(true);
        Region fixedRegion = new Region(10, 20, 30, 40);
        fixedPattern.getSearchRegions().setFixedRegion(fixedRegion);

        // getRegion should return the fixed region
        assertEquals(fixedRegion, fixedPattern.getRegion());

        // Pattern no longer has getStateObjects() method in new API

        // Create a pattern with multiple search regions
        Pattern searchPattern = new Pattern();
        searchPattern.setFixed(false);
        Region region1 = new Region(100, 200, 300, 400);
        Region region2 = new Region(500, 600, 700, 800);
        searchPattern.getSearchRegions().addSearchRegions(region1);
        searchPattern.getSearchRegions().addSearchRegions(region2);

        // getRegion should return one of the regions (can be either since it's random selection)
        Region returnedRegion = searchPattern.getRegion();
        assertTrue(returnedRegion.equals(region1) || returnedRegion.equals(region2));

        // Pattern no longer has getStateObjects() method in new API
        // We can verify the search regions were added
        assertNotNull(searchPattern.getSearchRegions());
    }
}