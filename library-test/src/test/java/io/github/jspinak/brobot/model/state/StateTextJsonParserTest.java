package io.github.jspinak.brobot.model.state;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.model.state.special.StateText;

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
public class StateTextJsonParserTest {
    
    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test basic serialization and deserialization of a StateText
     */
    @Test
    public void testBasicSerializationDeserialization() throws ConfigurationException {
        // Create a StateText
        StateText stateText = new StateText.Builder()
                .setName("TestText")
                .setOwnerStateName("TestState")
                .setText("Hello World Text")
                .build();

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateText);
        System.out.println("Serialized StateText: " + json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("\"name\" : \"TestText\""));
        assertTrue(json.contains("\"ownerStateName\" : \"TestState\""));
        assertTrue(json.contains("\"objectType\" : \"TEXT\""));
        assertTrue(json.contains("\"text\" : \"Hello World Text\""));

        // Deserialize back to StateText
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateText deserializedText = jsonParser.convertJson(jsonNode, StateText.class);

        // Verify deserialized object
        assertNotNull(deserializedText);
        assertEquals("TestText", deserializedText.getName());
        assertEquals("TestState", deserializedText.getOwnerStateName());
        assertEquals("Hello World Text", deserializedText.getText());
    }

    /**
     * Test StateText with search region
     */
    @Test
    public void testStateTextWithSearchRegion() throws ConfigurationException {
        // Create a region
        Region searchRegion = new Region(70, 80, 170, 220);

        // Create a StateText with search region
        StateText stateText = new StateText.Builder()
                .setName("RegionText")
                .setOwnerStateName("RegionState")
                .setSearchRegion(searchRegion)
                .setText("Search Region Text")
                .build();

        // Verify search region was set
        assertNotNull(stateText.getSearchRegion());
        assertEquals(70, stateText.getSearchRegion().x());
        assertEquals(80, stateText.getSearchRegion().y());
        assertEquals(170, stateText.getSearchRegion().w());
        assertEquals(220, stateText.getSearchRegion().h());

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateText);
        System.out.println("Serialized StateText with search region: " + json);

        // Verify JSON contains search region
        assertTrue(json.contains("\"searchRegion\""));
        assertTrue(json.contains("\"x\" : 70"));
        assertTrue(json.contains("\"y\" : 80"));
        assertTrue(json.contains("\"w\" : 170"));
        assertTrue(json.contains("\"h\" : 220"));

        // Deserialize back to StateText
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateText deserializedText = jsonParser.convertJson(jsonNode, StateText.class);

        // Verify deserialized search region
        assertNotNull(deserializedText.getSearchRegion());
        assertEquals(70, deserializedText.getSearchRegion().x());
        assertEquals(80, deserializedText.getSearchRegion().y());
        assertEquals(170, deserializedText.getSearchRegion().w());
        assertEquals(220, deserializedText.getSearchRegion().h());
    }

    /**
     * Test defined method
     */
    @Test
    public void testDefined() {
        // Create a StateText with text
        StateText definedText = new StateText.Builder()
                .setName("DefinedText")
                .setText("Defined Text")
                .build();

        // Test defined method
        assertTrue(definedText.defined());

        // Create a StateText with empty text
        StateText emptyText = new StateText.Builder()
                .setName("EmptyText")
                .setText("")
                .build();

        // Test defined method for empty text
        assertFalse(emptyText.defined());

        // Create a StateText with null text
        StateText nullText = new StateText.Builder()
                .setName("NullText")
                .build();

        // Test defined method for null text
        assertFalse(nullText.defined());
    }

    /**
     * Test getId method
     */
    @Test
    public void testGetId() {
        // Create a StateText with name, region, and text
        StateText stateText = new StateText.Builder()
                .setName("IdText")
                .setSearchRegion(new Region(30, 40, 130, 140))
                .setText("ID Test Text")
                .build();

        // Get ID
        String id = stateText.getId();

        // Verify ID contains relevant information
        assertNotNull(id);
        assertTrue(id.contains("IdText"));
        assertTrue(id.contains("ID Test Text"));

        // Create another StateText with the same properties
        StateText identicalText = new StateText.Builder()
                .setName("IdText")
                .setSearchRegion(new Region(30, 40, 130, 140))
                .setText("ID Test Text")
                .build();

        // Verify IDs are equal
        assertEquals(id, identicalText.getId());

        // Create a StateText with different properties
        StateText differentText = new StateText.Builder()
                .setName("DifferentName")
                .setText("Different Text")
                .build();

        // Verify IDs are different
        assertNotEquals(id, differentText.getId());
    }

    /**
     * Test builder pattern
     */
    @Test
    public void testBuilder() throws ConfigurationException {
        // Create a complete StateText using the builder
        StateText stateText = new StateText.Builder()
                .setName("BuilderText")
                .setOwnerStateName("BuilderState")
                .setSearchRegion(new Region(90, 100, 190, 240))
                .setText("Builder Test Text")
                .build();

        // Verify all properties
        assertEquals("BuilderText", stateText.getName());
        assertEquals("BuilderState", stateText.getOwnerStateName());
        assertEquals("Builder Test Text", stateText.getText());
        assertNotNull(stateText.getSearchRegion());
        assertEquals(90, stateText.getSearchRegion().x());

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(stateText);
        System.out.println("Serialized Builder StateText: " + json);

        // Deserialize back to StateText
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateText deserializedText = jsonParser.convertJson(jsonNode, StateText.class);

        // Verify all properties are preserved
        assertEquals("BuilderText", deserializedText.getName());
        assertEquals("BuilderState", deserializedText.getOwnerStateName());
        assertEquals("Builder Test Text", deserializedText.getText());
        assertNotNull(deserializedText.getSearchRegion());
        assertEquals(90, deserializedText.getSearchRegion().x());
    }
}