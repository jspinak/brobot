package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.json.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class StateLocationJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test basic serialization and deserialization of a StateLocation
     */
    @Test
    public void testBasicSerializationDeserialization() throws ConfigurationException {
        // Create a StateLocation
        StateLocation stateLocation = new StateLocation.Builder()
                .setName("TestLocation")
                .setOwnerStateName("TestState")
                .setLocation(100, 200)
                .build();

        // Verify the initial X,Y values before serialization
        assertEquals(100, stateLocation.getLocation().getCalculatedX());
        assertEquals(200, stateLocation.getLocation().getCalculatedY());

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateLocation);
        System.out.println("Serialized StateLocation: " + json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("\"name\":\"TestLocation\""));
        assertTrue(json.contains("\"ownerStateName\":\"TestState\""));
        assertTrue(json.contains("\"objectType\":\"LOCATION\""));
        assertTrue(json.contains("\"location\""));

        // Note: X,Y are computed by getX() and getY() methods which are annotated with @JsonIgnore
        // So they won't be in the JSON, but the underlying x,y fields should be
        assertTrue(json.contains("\"x\":100") || json.contains("\"x\": 100"),
                "JSON should contain the raw x coordinate");
        assertTrue(json.contains("\"y\":200") || json.contains("\"y\": 200"),
                "JSON should contain the raw y coordinate");

        // Deserialize back to StateLocation
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateLocation deserializedLocation = jsonParser.convertJson(jsonNode, StateLocation.class);

        // Verify deserialized object
        assertNotNull(deserializedLocation);
        assertEquals("TestLocation", deserializedLocation.getName());
        assertEquals("TestState", deserializedLocation.getOwnerStateName());
        assertEquals(StateObject.Type.LOCATION, deserializedLocation.getObjectType());

        // Verify the location properties
        // Since getX() and getY() are annotated with @JsonIgnore, we can't expect them to be
        // reconstructed exactly during deserialization, but we can check the underlying fields
        assertNotNull(deserializedLocation.getLocation());
        assertEquals(100, deserializedLocation.getLocation().getX());
        assertEquals(200, deserializedLocation.getLocation().getY());
    }

    /**
     * Test StateLocation with additional properties
     */
    @Test
    public void testStateLocationWithProperties() throws ConfigurationException {
        // Create a Location
        Location location = new Location(150, 250);

        // Create a StateLocation with additional properties
        StateLocation stateLocation = new StateLocation.Builder()
                .setName("PropertiesLocation")
                .setOwnerStateName("PropertiesState")
                .setLocation(location)
                .setStaysVisibleAfterClicked(300)
                .setProbabilityExists(85)
                .setPosition(Positions.Name.BOTTOMRIGHT)
                .build();

        // Verify properties were set
        assertEquals("PropertiesLocation", stateLocation.getName());
        assertEquals(300, stateLocation.getStaysVisibleAfterClicked());
        assertEquals(85, stateLocation.getProbabilityExists());
        assertEquals(1.0, stateLocation.getPosition().getPercentW(), 0.001); // BOTTOMRIGHT is (1.0, 1.0)
        assertEquals(1.0, stateLocation.getPosition().getPercentH(), 0.001);
        assertEquals(150, stateLocation.getLocation().getX()); // Raw field values
        assertEquals(250, stateLocation.getLocation().getY());

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateLocation);
        System.out.println("Serialized StateLocation with properties: " + json);

        // Verify JSON contains additional properties
        assertTrue(json.contains("\"staysVisibleAfterClicked\":300"));
        assertTrue(json.contains("\"probabilityExists\":85"));
        assertTrue(json.contains("\"position\""));

        // Deserialize back to StateLocation
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateLocation deserializedLocation = jsonParser.convertJson(jsonNode, StateLocation.class);

        // Verify deserialized properties
        assertEquals(300, deserializedLocation.getStaysVisibleAfterClicked());
        assertEquals(85, deserializedLocation.getProbabilityExists());
        assertNotNull(deserializedLocation.getPosition());
        assertEquals(1.0, deserializedLocation.getPosition().getPercentW(), 0.001);
        assertEquals(1.0, deserializedLocation.getPosition().getPercentH(), 0.001);

        // Verify the raw x,y fields were preserved
        assertEquals(150, deserializedLocation.getLocation().getX());
        assertEquals(250, deserializedLocation.getLocation().getY());
    }

    /**
     * Test defined method
     */
    @Test
    public void testDefined() {
        // Create a StateLocation with location
        StateLocation definedLocation = new StateLocation.Builder()
                .setName("DefinedLocation")
                .setLocation(200, 300)
                .build();

        // Test defined method
        assertTrue(definedLocation.defined());

        // Create a StateLocation without location
        StateLocation undefinedLocation = new StateLocation.Builder()
                .setName("UndefinedLocation")
                .build();

        // Test defined method for undefined location
        assertFalse(undefinedLocation.defined());
    }

    /**
     * Test asObjectCollection method
     */
    @Test
    public void testAsObjectCollection() {
        // Create a StateLocation
        StateLocation stateLocation = new StateLocation.Builder()
                .setName("CollectionLocation")
                .setLocation(250, 350)
                .build();

        // Convert to ObjectCollection
        ObjectCollection collection = stateLocation.asObjectCollection();

        // Verify collection
        assertNotNull(collection);
        assertFalse(collection.getStateLocations().isEmpty());
        assertEquals(1, collection.getStateLocations().size());
        assertTrue(collection.getStateLocations().contains(stateLocation));
    }

    /**
     * Test StateLocation as StateObject interface
     */
    @Test
    public void testStateObjectInterface() {
        // Create a StateLocation
        StateLocation stateLocation = new StateLocation.Builder()
                .setName("InterfaceLocation")
                .setOwnerStateName("InterfaceState")
                .setLocation(300, 400)
                .build();

        // Cast to StateObject
        StateObject stateObject = stateLocation;

        // Test interface methods
        assertEquals("InterfaceLocation", stateObject.getName());
        assertEquals("InterfaceState", stateObject.getOwnerStateName());
        assertEquals(StateObject.Type.LOCATION, stateObject.getObjectType());
        assertNotNull(stateObject.getIdAsString());

        // Test addTimesActedOn
        assertEquals(0, stateLocation.getTimesActedOn());
        stateObject.addTimesActedOn();
        assertEquals(1, stateLocation.getTimesActedOn());
    }

    /**
     * Test StateLocation with different position types
     */
    @Test
    public void testDifferentPositionTypes() throws ConfigurationException {
        // Create a StateLocation with percentages
        StateLocation percentLocation = new StateLocation.Builder()
                .setName("PercentLocation")
                .setLocation(350, 450)
                .setPosition(25, 75) // 25%, 75%
                .build();

        assertEquals(0.25, percentLocation.getPosition().getPercentW(), 0.001);
        assertEquals(0.75, percentLocation.getPosition().getPercentH(), 0.001);

        // Serialize and deserialize
        String json = jsonUtils.toJsonSafe(percentLocation);
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateLocation deserializedPercent = jsonParser.convertJson(jsonNode, StateLocation.class);

        // Test the percentages in the position, not the computed X,Y values
        assertEquals(0.25, deserializedPercent.getPosition().getPercentW(), 0.001);
        assertEquals(0.75, deserializedPercent.getPosition().getPercentH(), 0.001);

        // Create a StateLocation with named position
        StateLocation namedLocation = new StateLocation.Builder()
                .setName("NamedLocation")
                .setLocation(400, 500)
                .setPosition(Positions.Name.TOPLEFT)
                .build();

        assertEquals(0.0, namedLocation.getPosition().getPercentW(), 0.001);
        assertEquals(0.0, namedLocation.getPosition().getPercentH(), 0.001);

        // Serialize and deserialize
        json = jsonUtils.toJsonSafe(namedLocation);
        jsonNode = jsonParser.parseJson(json);
        StateLocation deserializedNamed = jsonParser.convertJson(jsonNode, StateLocation.class);

        // Test the percentages in the position, not the computed X,Y values
        assertEquals(0.0, deserializedNamed.getPosition().getPercentW(), 0.001);
        assertEquals(0.0, deserializedNamed.getPosition().getPercentH(), 0.001);
    }
}