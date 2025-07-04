package io.github.jspinak.brobot.model.state;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.model.state.StateString;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class StateStringJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test basic serialization and deserialization of a StateString
     */
    @Test
    public void testBasicSerializationDeserialization() throws ConfigurationException {
        // Create a StateString
        StateString stateString = new StateString.Builder()
                .setName("TestString")
                .setOwnerStateName("TestState")
                .build("Hello World");

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateString);
        System.out.println("Serialized StateString: " + json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("\"name\" : \"TestString\""));
        assertTrue(json.contains("\"ownerStateName\" : \"TestState\""));
        assertTrue(json.contains("\"objectType\" : \"STRING\""));
        assertTrue(json.contains("\"string\" : \"Hello World\""));

        // Deserialize back to StateString
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateString deserializedString = jsonParser.convertJson(jsonNode, StateString.class);

        // Verify deserialized object
        assertNotNull(deserializedString);
        assertEquals("TestString", deserializedString.getName());
        assertEquals("TestState", deserializedString.getOwnerStateName());
        assertEquals("Hello World", deserializedString.getString());
    }

    /**
     * Test StateString with search region
     */
    @Test
    public void testStateStringWithSearchRegion() throws ConfigurationException {
        // Create a region
        Region searchRegion = new Region(50, 60, 150, 200);

        // Create a StateString with search region
        StateString stateString = new StateString.Builder()
                .setName("RegionString")
                .setOwnerStateName("RegionState")
                .setSearchRegion(searchRegion)
                .build("Search Region String");

        // Verify search region was set
        assertNotNull(stateString.getSearchRegion());
        assertEquals(50, stateString.getSearchRegion().x());
        assertEquals(60, stateString.getSearchRegion().y());
        assertEquals(150, stateString.getSearchRegion().w());
        assertEquals(200, stateString.getSearchRegion().h());

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateString);
        System.out.println("Serialized StateString with search region: " + json);

        // Verify JSON contains search region
        assertTrue(json.contains("\"searchRegion\""));
        assertTrue(json.contains("\"x\" : 50"));
        assertTrue(json.contains("\"y\" : 60"));
        assertTrue(json.contains("\"w\" : 150"));
        assertTrue(json.contains("\"h\" : 200"));

        // Deserialize back to StateString
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateString deserializedString = jsonParser.convertJson(jsonNode, StateString.class);

        // Verify deserialized search region
        assertNotNull(deserializedString.getSearchRegion());
        assertEquals(50, deserializedString.getSearchRegion().x());
        assertEquals(60, deserializedString.getSearchRegion().y());
        assertEquals(150, deserializedString.getSearchRegion().w());
        assertEquals(200, deserializedString.getSearchRegion().h());
    }

    /**
     * Test defined method
     */
    @Test
    public void testDefined() {
        // Create a StateString with a string value
        StateString definedString = new StateString.Builder()
                .setName("DefinedString")
                .build("Defined");

        // Test defined method
        assertTrue(definedString.defined());

        // Create an empty StateString
        StateString emptyString = new StateString();

        // Test defined method for empty string
        assertFalse(emptyString.defined());
    }

    /**
     * Test InNullState static helper
     */
    @Test
    public void testInNullState() throws ConfigurationException {
        // Create a StateString using InNullState helper
        StateString nullStateString = new StateString.InNullState().withString("Null State String");

        // Verify string is created correctly
        assertEquals("Null State String", nullStateString.getString());
        assertEquals("null", nullStateString.getOwnerStateName());

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(nullStateString);
        System.out.println("Serialized InNullState StateString: " + json);

        // Deserialize back to StateString
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateString deserializedString = jsonParser.convertJson(jsonNode, StateString.class);

        // Verify deserialized object
        assertEquals("Null State String", deserializedString.getString());
        assertEquals("null", deserializedString.getOwnerStateName());
    }

    /**
     * Test getId method
     */
    @Test
    public void testGetId() {
        // Create a StateString with name, region, and string
        StateString stateString = new StateString.Builder()
                .setName("IdString")
                .setSearchRegion(new Region(10, 20, 30, 40))
                .build("ID Test");

        // Get ID
        String id = stateString.getId();

        // Verify ID contains relevant information
        assertNotNull(id);
        assertTrue(id.contains("IdString"));
        assertTrue(id.contains("ID Test"));

        // Create another StateString with the same properties
        StateString identicalString = new StateString.Builder()
                .setName("IdString")
                .setSearchRegion(new Region(10, 20, 30, 40))
                .build("ID Test");

        // Verify IDs are equal
        assertEquals(id, identicalString.getId());

        // Create a StateString with different properties
        StateString differentString = new StateString.Builder()
                .setName("DifferentName")
                .build("Different Text");

        // Verify IDs are different
        assertNotEquals(id, differentString.getId());
    }

    /**
     * Test timesActedOn
     */
    @Test
    public void testTimesActedOn() {
        // Create a StateString
        StateString stateString = new StateString.Builder()
                .setName("ActionString")
                .build("Action Test");

        // Initial value should be 0
        assertEquals(0, stateString.getTimesActedOn());

        // Increment and verify
        stateString.addTimesActedOn();
        assertEquals(1, stateString.getTimesActedOn());

        stateString.addTimesActedOn();
        stateString.addTimesActedOn();
        assertEquals(3, stateString.getTimesActedOn());

        // Set directly and verify
        stateString.setTimesActedOn(10);
        assertEquals(10, stateString.getTimesActedOn());
    }
}