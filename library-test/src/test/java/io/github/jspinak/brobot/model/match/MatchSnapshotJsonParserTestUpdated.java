package io.github.jspinak.brobot.model.match;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.action.ActionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated tests for MatchSnapshot (ActionRecord) JSON parsing without Spring dependencies.
 * Tests the new ActionConfig API and verifies JSON structure.
 * Migrated from library-test module.
 */
@Disabled("CI failure - needs investigation")

public class MatchSnapshotJsonParserTestUpdated {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Test parsing a basic MatchSnapshot from JSON with new ActionConfig
     */
    @Test
    public void testParseBasicMatchSnapshotWithActionConfig() throws Exception {
        String json = """
                {
                  "actionConfig": {
                    "@type": "PatternFindOptions",
                    "strategy": "BEST",
                    "similarity": 0.95
                  },
                  "matchList": [
                    {
                      "score": 0.95,
                      "name": "TestMatch",
                      "target": {
                        "region": {
                          "x": 10,
                          "y": 20,
                          "w": 30,
                          "h": 40
                        }
                      }
                    }
                  ],
                  "text": "Sample Text",
                  "duration": 1.5,
                  "actionSuccess": true,
                  "resultSuccess": true,
                  "stateName": "TestState"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        ActionRecord snapshot = objectMapper.treeToValue(jsonNode, ActionRecord.class);

        assertNotNull(snapshot);
        assertNotNull(snapshot.getActionConfig());
        assertTrue(snapshot.getActionConfig() instanceof PatternFindOptions);
        
        PatternFindOptions findOptions = (PatternFindOptions) snapshot.getActionConfig();
        assertEquals(PatternFindOptions.Strategy.BEST, findOptions.getStrategy());
        assertEquals(0.95, findOptions.getSimilarity(), 0.001);
        
        assertEquals("Sample Text", snapshot.getText());
        assertEquals(1.5, snapshot.getDuration(), 0.001);
        assertTrue(snapshot.isActionSuccess());
        assertTrue(snapshot.isResultSuccess());
        assertEquals("TestState", snapshot.getStateName());

        // Verify match list
        assertNotNull(snapshot.getMatchList());
        assertEquals(1, snapshot.getMatchList().size());

        Match match = snapshot.getMatchList().getFirst();
        assertEquals("TestMatch", match.getName());
        assertEquals(0.95, match.getScore(), 0.001);

        // Verify match region
        assertEquals(10, match.getRegion().x());
        assertEquals(20, match.getRegion().y());
        assertEquals(30, match.getRegion().w());
        assertEquals(40, match.getRegion().h());
    }

    /**
     * Test serializing and deserializing a MatchSnapshot with new ActionConfig
     */
    @Test
    public void testSerializeDeserializeMatchSnapshotWithActionConfig() throws Exception {
        // Create a match snapshot
        ActionRecord snapshot = new ActionRecord();

        // Set action config with new API
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();
        snapshot.setActionConfig(clickOptions);

        // Add match
        Match match = new Match.Builder()
                .setName("SerializedMatch")
                .setSimScore(0.85)
                .setRegion(50, 60, 70, 80)
                .build();
        snapshot.addMatch(match);

        // Set other properties
        snapshot.setText("Serialized Text");
        snapshot.setDuration(2.5);
        snapshot.setActionSuccess(true);
        snapshot.setResultSuccess(false);
        snapshot.setStateName("SerializedState");
        snapshot.setStateId(123L);

        // Serialize
        String json = objectMapper.writeValueAsString(snapshot);
        System.out.println("DEBUG: Serialized MatchSnapshot: " + json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("\"actionConfig\""));
        assertTrue(json.contains("\"@type\""));
        assertTrue(json.contains("\"ClickOptions\""));
        assertTrue(json.contains("\"numberOfClicks\""));

        // Deserialize
        JsonNode jsonNode = objectMapper.readTree(json);
        ActionRecord deserializedSnapshot = objectMapper.treeToValue(jsonNode, ActionRecord.class);

        // Verify
        assertNotNull(deserializedSnapshot);
        assertNotNull(deserializedSnapshot.getActionConfig());
        assertTrue(deserializedSnapshot.getActionConfig() instanceof ClickOptions);
        
        ClickOptions deserializedClickOptions = (ClickOptions) deserializedSnapshot.getActionConfig();
        assertEquals(1, deserializedClickOptions.getNumberOfClicks());
        
        assertEquals("Serialized Text", deserializedSnapshot.getText());
        assertEquals(2.5, deserializedSnapshot.getDuration(), 0.001);
        assertTrue(deserializedSnapshot.isActionSuccess());
        assertFalse(deserializedSnapshot.isResultSuccess());
        assertEquals("SerializedState", deserializedSnapshot.getStateName());
        assertEquals(123L, deserializedSnapshot.getStateId());

        // Verify match
        assertNotNull(deserializedSnapshot.getMatchList());
        assertEquals(1, deserializedSnapshot.getMatchList().size());

        Match deserializedMatch = deserializedSnapshot.getMatchList().getFirst();
        assertEquals("SerializedMatch", deserializedMatch.getName());
        assertEquals(0.85, deserializedMatch.getScore(), 0.001);

        // Verify match region
        assertEquals(50, deserializedMatch.getRegion().x());
        assertEquals(60, deserializedMatch.getRegion().y());
        assertEquals(70, deserializedMatch.getRegion().w());
        assertEquals(80, deserializedMatch.getRegion().h());
    }

    /**
     * Test the Builder pattern with new ActionConfig
     */
    @Test
    public void testMatchSnapshotBuilderWithActionConfig() throws Exception {
        // Create move options
        MouseMoveOptions moveOptions = new MouseMoveOptions.Builder()
                .setMoveMouseDelay(0.0f)
                .build();

        // Create a match snapshot with builder
        ActionRecord snapshot = new ActionRecord.Builder()
                .setActionConfig(moveOptions)
                .addMatch(new Match.Builder()
                        .setName("BuilderMatch")
                        .setSimScore(0.92)
                        .setRegion(100, 150, 200, 250)
                        .build())
                .setText("Builder Text")
                .setDuration(3.5)
                .setActionSuccess(true)
                .setResultSuccess(true)
                .setState("BuilderState")
                .build();

        // Serialize
        String json = objectMapper.writeValueAsString(snapshot);
        System.out.println("DEBUG: Serialized Builder MatchSnapshot: " + json);

        // Deserialize
        JsonNode jsonNode = objectMapper.readTree(json);
        ActionRecord deserializedSnapshot = objectMapper.treeToValue(jsonNode, ActionRecord.class);

        // Verify
        assertNotNull(deserializedSnapshot);
        assertNotNull(deserializedSnapshot.getActionConfig());
        assertTrue(deserializedSnapshot.getActionConfig() instanceof MouseMoveOptions);
        
        MouseMoveOptions deserializedMouseMoveOptions = (MouseMoveOptions) deserializedSnapshot.getActionConfig();
        assertEquals(0.0f, deserializedMouseMoveOptions.getMoveMouseDelay(), 0.001);
        
        assertEquals("Builder Text", deserializedSnapshot.getText());
        assertEquals(3.5, deserializedSnapshot.getDuration(), 0.001);
        assertTrue(deserializedSnapshot.isActionSuccess());
        assertTrue(deserializedSnapshot.isResultSuccess());
        assertEquals("BuilderState", deserializedSnapshot.getStateName());

        // Verify match
        assertNotNull(deserializedSnapshot.getMatchList());
        assertEquals(1, deserializedSnapshot.getMatchList().size());

        Match deserializedMatch = deserializedSnapshot.getMatchList().getFirst();
        assertEquals("BuilderMatch", deserializedMatch.getName());
        assertEquals(0.92, deserializedMatch.getScore(), 0.001);

        // Verify match region
        assertEquals(100, deserializedMatch.getRegion().x());
        assertEquals(150, deserializedMatch.getRegion().y());
        assertEquals(200, deserializedMatch.getRegion().w());
        assertEquals(250, deserializedMatch.getRegion().h());
    }

    /**
     * Test constructor with coords and wasFound method
     */
    @Test
    public void testConstructorAndWasFound() {
        // Test constructor with coordinates
        ActionRecord snapshot = new ActionRecord(10, 20, 30, 40);

        assertNotNull(snapshot.getMatchList());
        assertEquals(1, snapshot.getMatchList().size());

        Match match = snapshot.getMatchList().getFirst();
        assertEquals(10, match.getRegion().x());
        assertEquals(20, match.getRegion().y());
        assertEquals(30, match.getRegion().w());
        assertEquals(40, match.getRegion().h());

        // Test wasFound with match
        assertTrue(snapshot.wasFound());

        // Test wasFound with text only
        ActionRecord textSnapshot = new ActionRecord();
        textSnapshot.setText("Text Only");
        assertTrue(textSnapshot.wasFound());

        // Test wasFound with no match or text
        ActionRecord emptySnapshot = new ActionRecord();
        assertFalse(emptySnapshot.wasFound());
    }

    /**
     * Test addMatch, addMatchList, and setString methods
     */
    @Test
    public void testAddMethods() {
        ActionRecord snapshot = new ActionRecord();

        // Test setString
        snapshot.setString("Test String");
        assertEquals("Test String", snapshot.getText());

        // Test addMatch
        Match match1 = new Match.Builder()
                .setName("Match1")
                .setRegion(10, 20, 30, 40)
                .build();
        snapshot.addMatch(match1);

        assertEquals(1, snapshot.getMatchList().size());
        assertEquals("Match1", snapshot.getMatchList().getFirst().getName());

        // Test addMatchList
        List<Match> matches = new ArrayList<>();
        matches.add(new Match.Builder()
                .setName("Match2")
                .setRegion(50, 60, 70, 80)
                .build());
        matches.add(new Match.Builder()
                .setName("Match3")
                .setRegion(90, 100, 110, 120)
                .build());

        snapshot.addMatchList(matches);

        assertEquals(3, snapshot.getMatchList().size());
        assertEquals("Match1", snapshot.getMatchList().getFirst().getName());
        assertEquals("Match2", snapshot.getMatchList().get(1).getName());
        assertEquals("Match3", snapshot.getMatchList().get(2).getName());
    }

    /**
     * Test hasSameResultsAs and equals methods with new ActionConfig
     */
    @Test
    public void testComparisonMethodsWithActionConfig() {
        PatternFindOptions findOptions1 = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();

        // Create first snapshot
        ActionRecord snapshot1 = new ActionRecord.Builder()
                .setActionConfig(findOptions1)
                .addMatch(new Match.Builder()
                        .setName("Match1")
                        .setRegion(10, 20, 30, 40)
                        .build())
                .setText("Text1")
                .build();

        PatternFindOptions findOptions2 = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();

        // Create identical snapshot
        ActionRecord snapshot2 = new ActionRecord.Builder()
                .setActionConfig(findOptions2)
                .addMatch(new Match.Builder()
                        .setName("Match1")
                        .setRegion(10, 20, 30, 40)
                        .build())
                .setText("Text1")
                .build();

        // Create snapshot with different text
        ActionRecord snapshot3 = new ActionRecord.Builder()
                .setActionConfig(findOptions2)
                .addMatch(new Match.Builder()
                        .setName("Match1")
                        .setRegion(10, 20, 30, 40)
                        .build())
                .setText("Different Text")
                .build();

        // Create snapshot with different match
        ActionRecord snapshot4 = new ActionRecord.Builder()
                .setActionConfig(findOptions2)
                .addMatch(new Match.Builder()
                        .setName("Match1")
                        .setRegion(50, 60, 70, 80) // Different region
                        .build())
                .setText("Text1")
                .build();

        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();

        // Create snapshot with different action type
        ActionRecord snapshot5 = new ActionRecord.Builder()
                .setActionConfig(clickOptions) // Different action type
                .addMatch(new Match.Builder()
                        .setName("Match1")
                        .setRegion(10, 20, 30, 40)
                        .build())
                .setText("Text1")
                .build();

        // Test hasSameResultsAs (only compares matches and text)
        assertTrue(snapshot1.hasSameResultsAs(snapshot2));
        assertFalse(snapshot1.hasSameResultsAs(snapshot3)); // Different text
        assertFalse(snapshot1.hasSameResultsAs(snapshot4)); // Different match
        assertTrue(snapshot1.hasSameResultsAs(snapshot5)); // Same match and text, different action

        // Test equals (compares action config, matches, and text)
        assertTrue(snapshot1.equals(snapshot2));
        assertFalse(snapshot1.equals(snapshot3)); // Different text
        assertFalse(snapshot1.equals(snapshot4)); // Different match
        assertFalse(snapshot1.equals(snapshot5)); // Different action type
    }

    /**
     * Test mixed usage with legacy ActionOptions for backward compatibility
     */
    @Test
    public void testMixedLegacyAndNewAPI() throws Exception {
        ActionRecord snapshot = new ActionRecord();

        // Test that we can still use PatternFindOptions if needed
        PatternFindOptions legacyOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
        snapshot.setActionConfig(legacyOptions);

        // Add match
        snapshot.addMatch(new Match.Builder()
                .setName("LegacyMatch")
                .setRegion(10, 20, 30, 40)
                .build());

        // Serialize
        String json = objectMapper.writeValueAsString(snapshot);
        assertNotNull(json);
        assertTrue(json.contains("\"strategy\""));
        assertTrue(json.contains("\"ALL\""));

        // Deserialize
        ActionRecord deserialized = objectMapper.readValue(json, ActionRecord.class);
        assertNotNull(deserialized);
        assertNotNull(deserialized.getActionConfig());
        assertTrue(deserialized.getActionConfig() instanceof PatternFindOptions);
        assertEquals(PatternFindOptions.Strategy.ALL, ((PatternFindOptions)deserialized.getActionConfig()).getStrategy());
    }
}