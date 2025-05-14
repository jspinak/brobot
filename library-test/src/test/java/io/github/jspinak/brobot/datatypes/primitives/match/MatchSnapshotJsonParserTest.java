package io.github.jspinak.brobot.datatypes.primitives.match;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.json.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class MatchSnapshotJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic MatchSnapshot from JSON
     */
    @Test
    public void testParseBasicMatchSnapshot() throws ConfigurationException {
        String json = """
                {
                  "actionOptions": {
                    "action": "FIND",
                    "find": "BEST"
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

        JsonNode jsonNode = jsonParser.parseJson(json);
        MatchSnapshot snapshot = jsonParser.convertJson(jsonNode, MatchSnapshot.class);

        assertNotNull(snapshot);
        assertEquals(ActionOptions.Action.FIND, snapshot.getActionOptions().getAction());
        assertEquals(ActionOptions.Find.BEST, snapshot.getActionOptions().getFind());
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
     * Test serializing and deserializing a MatchSnapshot
     */
    @Test
    public void testSerializeDeserializeMatchSnapshot() throws ConfigurationException {
        // Create a match snapshot
        MatchSnapshot snapshot = new MatchSnapshot();

        // Set action options
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setFind(ActionOptions.Find.FIRST)
                .build();
        snapshot.setActionOptions(actionOptions);

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
        String json = jsonUtils.toJsonSafe(snapshot);
        System.out.println("DEBUG: Serialized MatchSnapshot: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        MatchSnapshot deserializedSnapshot = jsonParser.convertJson(jsonNode, MatchSnapshot.class);

        // Verify
        assertNotNull(deserializedSnapshot);
        assertEquals(ActionOptions.Action.CLICK, deserializedSnapshot.getActionOptions().getAction());
        assertEquals(ActionOptions.Find.FIRST, deserializedSnapshot.getActionOptions().getFind());
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
     * Test the Builder pattern
     */
    @Test
    public void testMatchSnapshotBuilder() throws ConfigurationException {
        // Create a match snapshot with builder
        MatchSnapshot snapshot = new MatchSnapshot.Builder()
                .setActionOptions(new ActionOptions.Builder()
                        .setAction(ActionOptions.Action.MOVE)
                        .setFind(ActionOptions.Find.ALL)
                        .build())
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
        String json = jsonUtils.toJsonSafe(snapshot);
        System.out.println("DEBUG: Serialized Builder MatchSnapshot: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        MatchSnapshot deserializedSnapshot = jsonParser.convertJson(jsonNode, MatchSnapshot.class);

        // Verify
        assertNotNull(deserializedSnapshot);
        assertEquals(ActionOptions.Action.MOVE, deserializedSnapshot.getActionOptions().getAction());
        assertEquals(ActionOptions.Find.ALL, deserializedSnapshot.getActionOptions().getFind());
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
        MatchSnapshot snapshot = new MatchSnapshot(10, 20, 30, 40);

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
        MatchSnapshot textSnapshot = new MatchSnapshot();
        textSnapshot.setText("Text Only");
        assertTrue(textSnapshot.wasFound());

        // Test wasFound with no match or text
        MatchSnapshot emptySnapshot = new MatchSnapshot();
        assertFalse(emptySnapshot.wasFound());
    }

    /**
     * Test addMatch, addMatchList, and setString methods
     */
    @Test
    public void testAddMethods() {
        MatchSnapshot snapshot = new MatchSnapshot();

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
     * Test hasSameResultsAs and equals methods
     */
    @Test
    public void testComparisonMethods() {
        // Create first snapshot
        MatchSnapshot snapshot1 = new MatchSnapshot.Builder()
                .setActionOptions(ActionOptions.Action.FIND)
                .addMatch(new Match.Builder()
                        .setName("Match1")
                        .setRegion(10, 20, 30, 40)
                        .build())
                .setText("Text1")
                .build();

        // Create identical snapshot
        MatchSnapshot snapshot2 = new MatchSnapshot.Builder()
                .setActionOptions(ActionOptions.Action.FIND)
                .addMatch(new Match.Builder()
                        .setName("Match1")
                        .setRegion(10, 20, 30, 40)
                        .build())
                .setText("Text1")
                .build();

        // Create snapshot with different text
        MatchSnapshot snapshot3 = new MatchSnapshot.Builder()
                .setActionOptions(ActionOptions.Action.FIND)
                .addMatch(new Match.Builder()
                        .setName("Match1")
                        .setRegion(10, 20, 30, 40)
                        .build())
                .setText("Different Text")
                .build();

        // Create snapshot with different match
        MatchSnapshot snapshot4 = new MatchSnapshot.Builder()
                .setActionOptions(ActionOptions.Action.FIND)
                .addMatch(new Match.Builder()
                        .setName("Match1")
                        .setRegion(50, 60, 70, 80) // Different region
                        .build())
                .setText("Text1")
                .build();

        // Create snapshot with different action
        MatchSnapshot snapshot5 = new MatchSnapshot.Builder()
                .setActionOptions(ActionOptions.Action.CLICK) // Different action
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

        // Test equals (compares action, find, matches, and text)
        assertTrue(snapshot1.equals(snapshot2));
        assertFalse(snapshot1.equals(snapshot3)); // Different text
        assertFalse(snapshot1.equals(snapshot4)); // Different match
        assertFalse(snapshot1.equals(snapshot5)); // Different action
    }
}