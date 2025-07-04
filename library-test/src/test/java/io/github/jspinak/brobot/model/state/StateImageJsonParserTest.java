package io.github.jspinak.brobot.model.state;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateImage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class StateImageJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test basic serialization and deserialization of a StateImage
     */
    @Test
    public void testBasicSerializationDeserialization() throws ConfigurationException {
        // Create a StateImage
        StateImage stateImage = new StateImage.Builder()
                .setName("TestImage")
                .setOwnerStateName("TestState")
                .build();

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateImage);
        System.out.println("Serialized StateImage: " + json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("\"name\" : \"TestImage\""));
        assertTrue(json.contains("\"ownerStateName\" : \"TestState\""));
        assertTrue(json.contains("\"objectType\" : \"IMAGE\""));

        // Deserialize back to StateImage
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateImage deserializedImage = jsonParser.convertJson(jsonNode, StateImage.class);

        // Verify deserialized object
        assertNotNull(deserializedImage);
        assertEquals("TestImage", deserializedImage.getName());
        assertEquals("TestState", deserializedImage.getOwnerStateName());
        assertEquals(StateObject.Type.IMAGE, deserializedImage.getObjectType());
    }

    /**
     * Test StateImage with patterns
     */
    @Test
    public void testStateImageWithPatterns() throws ConfigurationException {
        // Create a Pattern
        Pattern pattern = new Pattern.Builder()
                .setName("TestPattern")
                .setFixedRegion(new Region(10, 20, 100, 200))
                .build();

        // Create a StateImage with the pattern
        StateImage stateImage = new StateImage.Builder()
                .setName("PatternImage")
                .setOwnerStateName("PatternState")
                .addPattern(pattern)
                .build();

        // Verify pattern was added
        assertEquals(1, stateImage.getPatterns().size());
        assertEquals("TestPattern", stateImage.getPatterns().getFirst().getName());

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateImage);
        System.out.println("Serialized StateImage with Pattern: " + json);

        // Verify JSON contains pattern info
        assertTrue(json.contains("\"patterns\""));
        assertTrue(json.contains("\"TestPattern\""));

        // Deserialize back to StateImage
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateImage deserializedImage = jsonParser.convertJson(jsonNode, StateImage.class);

        // Verify deserialized object has patterns
        assertNotNull(deserializedImage);
        assertFalse(deserializedImage.getPatterns().isEmpty());
        assertEquals("TestPattern", deserializedImage.getPatterns().getFirst().getName());
    }

    /**
     * Test setting position for all patterns
     */
    @Test
    public void testSetPositionForAllPatterns() throws ConfigurationException {
        // Create patterns
        Pattern pattern1 = new Pattern.Builder()
                .setName("Pattern1")
                .build();

        Pattern pattern2 = new Pattern.Builder()
                .setName("Pattern2")
                .build();

        // Create a StateImage with the patterns
        StateImage stateImage = new StateImage.Builder()
                .setName("MultiPatternImage")
                .addPattern(pattern1)
                .addPattern(pattern2)
                .build();

        // Set position for all patterns
        Position position = new Position(0.25, 0.75);
        stateImage.setPositionForAllPatterns(position);

        // Verify position was set for all patterns
        for (Pattern pattern : stateImage.getPatterns()) {
            assertEquals(0.25, pattern.getTargetPosition().getPercentW(), 0.001);
            assertEquals(0.75, pattern.getTargetPosition().getPercentH(), 0.001);
        }

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateImage);
        System.out.println("Serialized StateImage with positions: " + json);

        // Deserialize back to StateImage
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateImage deserializedImage = jsonParser.convertJson(jsonNode, StateImage.class);

        // Verify positions are preserved
        for (Pattern pattern : deserializedImage.getPatterns()) {
            assertEquals(0.25, pattern.getTargetPosition().getPercentW(), 0.001);
            assertEquals(0.75, pattern.getTargetPosition().getPercentH(), 0.001);
        }
    }

    /**
     * Test StateImage as StateObject interface
     */
    @Test
    public void testStateObjectInterface() {
        // Create a StateImage
        StateImage stateImage = new StateImage.Builder()
                .setName("InterfaceImage")
                .setOwnerStateName("InterfaceState")
                .build();

        // Test interface methods
        assertEquals("InterfaceImage", ((StateObject) stateImage).getName());
        assertEquals("InterfaceState", ((StateObject) stateImage).getOwnerStateName());
        assertEquals(StateObject.Type.IMAGE, ((StateObject) stateImage).getObjectType());
        assertNotNull(((StateObject) stateImage).getIdAsString());

        // Test addTimesActedOn
        assertEquals(0, stateImage.getTimesActedOn());
        ((StateObject) stateImage).addTimesActedOn();
        assertEquals(1, stateImage.getTimesActedOn());
    }

    /**
     * Test search regions methods
     */
    @Test
    public void testSearchRegions() {
        // Create a StateImage with patterns with search regions
        Pattern pattern1 = new Pattern.Builder()
                .setName("RegionPattern1")
                .setFixedRegion(new Region(10, 20, 100, 200))
                .build();

        Pattern pattern2 = new Pattern.Builder()
                .setName("RegionPattern2")
                .addSearchRegion(new Region(50, 60, 150, 200))
                .build();

        StateImage stateImage = new StateImage.Builder()
                .setName("RegionImage")
                .addPattern(pattern1)
                .addPattern(pattern2)
                .build();

        // Test getAllSearchRegions
        List<Region> allRegions = stateImage.getAllSearchRegions();
        assertNotNull(allRegions);
        allRegions.forEach(System.out::println);
        // The exact size may depend on implementation details, but should be at least 2
        assertTrue(allRegions.size() == 1); // There should be one region: fixed regions are not added to search regions.

        // Test getDefinedFixedRegions
        List<Region> fixedRegions = stateImage.getDefinedFixedRegions();
        assertEquals(1, fixedRegions.size());
        assertEquals(10, fixedRegions.getFirst().x());
        assertEquals(20, fixedRegions.getFirst().y());

        // Test getLargestDefinedFixedRegionOrNewRegion
        Region largestRegion = stateImage.getLargestDefinedFixedRegionOrNewRegion();
        assertNotNull(largestRegion);
        assertEquals(10, largestRegion.x());
        assertEquals(20, largestRegion.y());
    }
}