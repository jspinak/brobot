package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateRegion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class StateRegionJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test basic serialization and deserialization of a StateRegion
     */
    @Test
    public void testBasicSerializationDeserialization() throws ConfigurationException {
        // Create a StateRegion
        StateRegion stateRegion = new StateRegion.Builder()
                .setName("TestRegion")
                .setOwnerStateName("TestState")
                .setSearchRegion(new Region(10, 20, 100, 200))
                .build();

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateRegion);
        System.out.println("Serialized StateRegion: " + json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("\"name\" : \"TestRegion\""));
        assertTrue(json.contains("\"ownerStateName\" : \"TestState\""));
        assertTrue(json.contains("\"objectType\" : \"REGION\""));
        assertTrue(json.contains("\"searchRegion\""));
        assertTrue(json.contains("\"x\" : 10"));
        assertTrue(json.contains("\"y\" : 20"));
        assertTrue(json.contains("\"w\" : 100"));
        assertTrue(json.contains("\"h\" : 200"));

        // Deserialize back to StateRegion
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateRegion deserializedRegion = jsonParser.convertJson(jsonNode, StateRegion.class);

        // Verify deserialized object
        assertNotNull(deserializedRegion);
        assertEquals("TestRegion", deserializedRegion.getName());
        assertEquals("TestState", deserializedRegion.getOwnerStateName());
        assertEquals(StateObject.Type.REGION, deserializedRegion.getObjectType());

        // Verify region properties
        assertNotNull(deserializedRegion.getSearchRegion());
        assertEquals(10, deserializedRegion.getSearchRegion().x());
        assertEquals(20, deserializedRegion.getSearchRegion().y());
        assertEquals(100, deserializedRegion.getSearchRegion().w());
        assertEquals(200, deserializedRegion.getSearchRegion().h());
    }

    /**
     * Test StateRegion with additional properties
     */
    @Test
    public void testStateRegionWithProperties() throws ConfigurationException {
        // Create a StateRegion with additional properties
        StateRegion stateRegion = new StateRegion.Builder()
                .setName("PropertiesRegion")
                .setOwnerStateName("PropertiesState")
                .setSearchRegion(new Region(30, 40, 300, 400))
                .setStaysVisibleAfterClicked(500)
                .setProbabilityExists(75)
                .setPosition(new Position(0.25, 0.75))
                .setMockText("Mock region text")
                .build();

        // Verify properties were set
        assertEquals("PropertiesRegion", stateRegion.getName());
        assertEquals(500, stateRegion.getStaysVisibleAfterClicked());
        assertEquals(75, stateRegion.getProbabilityExists());
        assertEquals(0.25, stateRegion.getPosition().getPercentW(), 0.001);
        assertEquals(0.75, stateRegion.getPosition().getPercentH(), 0.001);
        assertEquals("Mock region text", stateRegion.getMockText());

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateRegion);
        System.out.println("Serialized StateRegion with properties: " + json);

        // Verify JSON contains additional properties
        assertTrue(json.contains("\"staysVisibleAfterClicked\" : 500"));
        assertTrue(json.contains("\"probabilityExists\" : 75"));
        assertTrue(json.contains("\"position\""));
        assertTrue(json.contains("\"mockText\" : \"Mock region text\""));

        // Deserialize back to StateRegion
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateRegion deserializedRegion = jsonParser.convertJson(jsonNode, StateRegion.class);

        // Verify deserialized properties
        assertEquals(500, deserializedRegion.getStaysVisibleAfterClicked());
        assertEquals(75, deserializedRegion.getProbabilityExists());
        assertEquals(0.25, deserializedRegion.getPosition().getPercentW(), 0.001);
        assertEquals(0.75, deserializedRegion.getPosition().getPercentH(), 0.001);
        assertEquals("Mock region text", deserializedRegion.getMockText());
    }

    /**
     * Test coordinate convenience methods
     */
    @Test
    public void testCoordinateMethods() {
        // Create a StateRegion
        StateRegion stateRegion = new StateRegion.Builder()
                .setName("CoordinateRegion")
                .setSearchRegion(50, 60, 150, 200)
                .build();

        // Test coordinate methods
        assertEquals(50, stateRegion.x());
        assertEquals(60, stateRegion.y());
        assertEquals(150, stateRegion.w());
        assertEquals(200, stateRegion.h());

        // Test defined method
        assertTrue(stateRegion.defined());

        // Test with undefined region
        StateRegion undefinedRegion = new StateRegion.Builder()
                .setName("UndefinedRegion")
                .build();

        assertFalse(undefinedRegion.defined());
    }

    /**
     * Test asObjectCollection method
     */
    @Test
    public void testAsObjectCollection() {
        // Create a StateRegion
        StateRegion stateRegion = new StateRegion.Builder()
                .setName("CollectionRegion")
                .setSearchRegion(70, 80, 170, 210)
                .build();

        // Convert to ObjectCollection
        ObjectCollection collection = stateRegion.asObjectCollection();

        // Verify collection
        assertNotNull(collection);
        assertFalse(collection.getStateRegions().isEmpty());
        assertEquals(1, collection.getStateRegions().size());
        assertTrue(collection.getStateRegions().contains(stateRegion));
    }

    /**
     * Test StateRegion as StateObject interface
     */
    @Test
    public void testStateObjectInterface() {
        // Create a StateRegion
        StateRegion stateRegion = new StateRegion.Builder()
                .setName("InterfaceRegion")
                .setOwnerStateName("InterfaceState")
                .setSearchRegion(15, 25, 115, 215)
                .build();

        // Cast to StateObject
        StateObject stateObject = stateRegion;

        // Test interface methods
        assertEquals("InterfaceRegion", stateObject.getName());
        assertEquals("InterfaceState", stateObject.getOwnerStateName());
        assertEquals(StateObject.Type.REGION, stateObject.getObjectType());
        assertNotNull(stateObject.getIdAsString());

        // Test addTimesActedOn
        assertEquals(0, stateRegion.getTimesActedOn());
        stateObject.addTimesActedOn();
        assertEquals(1, stateRegion.getTimesActedOn());
    }
}