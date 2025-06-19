package io.github.jspinak.brobot.datatypes.state.state;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.json.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;

import java.util.HashSet;
import java.util.Set;

/**
 * Special diagnostic test to identify JSON serialization issues with State objects.
 */
@SpringBootTest
public class StateDiagnosticTest extends BrobotIntegrationTestBase {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    @Test
    public void inspectStateJsonStructure() throws ConfigurationException {
        // Create a minimal state with basic properties
        State state = new State();
        state.setId(100L);
        state.setName("DiagnosticState");

        // Add state text
        Set<String> stateText = new HashSet<>();
        stateText.add("Text1");
        state.setStateText(stateText);

        // Add one state image
        StateImage image = new StateImage.Builder()
                .setName("DiagnosticImage")
                .setOwnerStateName("DiagnosticState")
                .build();

        // Try both ways of adding a state image
        state.addStateImage(image);

        // Add one state region
        StateRegion region = new StateRegion.Builder()
                .setName("DiagnosticRegion")
                .setOwnerStateName("DiagnosticState")
                .build();
        state.addStateRegion(region);

        // Add one state location
        StateLocation location = new StateLocation.Builder()
                .setName("DiagnosticLocation")
                .setOwnerStateName("DiagnosticState")
                .build();
        state.addStateLocation(location);

        // Add one state string
        StateString string = new StateString.Builder()
                .setName("DiagnosticString")
                .setOwnerStateName("DiagnosticState")
                .build("Diagnostic Text");
        state.addStateString(string);

        // Verify objects were added (using direct getters)
        System.out.println("STATE BEFORE SERIALIZATION:");
        System.out.println("State ID: " + state.getId());
        System.out.println("State Name: " + state.getName());
        System.out.println("State Text: " + state.getStateText());
        System.out.println("StateImages size: " + state.getStateImages().size());
        System.out.println("StateRegions size: " + state.getStateRegions().size());
        System.out.println("StateLocations size: " + state.getStateLocations().size());
        System.out.println("StateStrings size: " + state.getStateStrings().size());

        // Dump out the first object in each collection
        if (!state.getStateImages().isEmpty()) {
            StateImage firstImage = state.getStateImages().iterator().next();
            System.out.println("First StateImage: " + firstImage.getName());
        }

        if (!state.getStateRegions().isEmpty()) {
            StateRegion firstRegion = state.getStateRegions().iterator().next();
            System.out.println("First StateRegion: " + firstRegion.getName());
        }

        if (!state.getStateLocations().isEmpty()) {
            StateLocation firstLocation = state.getStateLocations().iterator().next();
            System.out.println("First StateLocation: " + firstLocation.getName());
        }

        if (!state.getStateStrings().isEmpty()) {
            StateString firstString = state.getStateStrings().iterator().next();
            System.out.println("First StateString: " + firstString.getName());
        }

        // Serialize to JSON
        String json = jsonUtils.toJsonSafe(state);
        System.out.println("\nJSON OUTPUT:");
        System.out.println(json);

        // Search for various field names in the JSON
        System.out.println("\nFIELD NAME ANALYSIS:");
        checkFieldInJson(json, "id");
        checkFieldInJson(json, "name");
        checkFieldInJson(json, "stateText");
        checkFieldInJson(json, "stateImages");
        checkFieldInJson(json, "images");  // Alternative field name
        checkFieldInJson(json, "stateRegions");
        checkFieldInJson(json, "regions");  // Alternative field name
        checkFieldInJson(json, "stateLocations");
        checkFieldInJson(json, "locations");  // Alternative field name
        checkFieldInJson(json, "stateStrings");
        checkFieldInJson(json, "strings");  // Alternative field name

        // Try to parse it back
        JsonNode jsonNode = jsonParser.parseJson(json);
        State deserializedState = jsonParser.convertJson(jsonNode, State.class);

        System.out.println("\nSTATE AFTER DESERIALIZATION:");
        System.out.println("State ID: " + deserializedState.getId());
        System.out.println("State Name: " + deserializedState.getName());
        System.out.println("State Text: " + deserializedState.getStateText());
        System.out.println("StateImages size: " + deserializedState.getStateImages().size());
        System.out.println("StateRegions size: " + deserializedState.getStateRegions().size());
        System.out.println("StateLocations size: " + deserializedState.getStateLocations().size());
        System.out.println("StateStrings size: " + deserializedState.getStateStrings().size());

        // Check for class annotations
        System.out.println("\nCLASS ANNOTATION ANALYSIS:");
        System.out.println("State class annotations: " + State.class.getAnnotations().length);
        for (java.lang.annotation.Annotation annotation : State.class.getAnnotations()) {
            System.out.println("  " + annotation.toString());
        }

        // Check field annotations for the collections
        try {
            java.lang.reflect.Field stateImagesField = State.class.getDeclaredField("stateImages");
            System.out.println("stateImages annotations: " + stateImagesField.getAnnotations().length);
            for (java.lang.annotation.Annotation annotation : stateImagesField.getAnnotations()) {
                System.out.println("  " + annotation.toString());
            }
        } catch (NoSuchFieldException e) {
            System.out.println("stateImages field not found");
        }
    }

    private void checkFieldInJson(String json, String fieldName) {
        boolean contains = json.contains("\"" + fieldName + "\"");
        System.out.println("JSON contains \"" + fieldName + "\": " + contains);
    }
}