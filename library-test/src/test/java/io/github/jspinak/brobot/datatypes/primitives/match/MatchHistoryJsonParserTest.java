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

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class MatchHistoryJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic MatchHistory from JSON
     */
    @Test
    public void testParseBasicMatchHistory() throws ConfigurationException {
        String json = """
                {
                  "timesSearched": 10,
                  "timesFound": 7,
                  "snapshots": [
                    {
                      "actionOptions": {
                        "action": "FIND",
                        "find": "FIRST"
                      },
                      "matchList": [
                        {
                          "score": 0.9,
                          "name": "Match1",
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
                      "text": "Found Text",
                      "actionSuccess": true,
                      "resultSuccess": true
                    }
                  ]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        MatchHistory matchHistory = jsonParser.convertJson(jsonNode, MatchHistory.class);

        assertNotNull(matchHistory);
        assertEquals(10, matchHistory.getTimesSearched());
        assertEquals(7, matchHistory.getTimesFound());

        // Verify snapshots
        assertNotNull(matchHistory.getSnapshots());
        assertEquals(1, matchHistory.getSnapshots().size());

        MatchSnapshot snapshot = matchHistory.getSnapshots().getFirst();
        assertEquals(ActionOptions.Action.FIND, snapshot.getActionOptions().getAction());
        assertEquals(ActionOptions.Find.FIRST, snapshot.getActionOptions().getFind());

        assertTrue(snapshot.isActionSuccess());
        assertTrue(snapshot.isResultSuccess());
        assertEquals("Found Text", snapshot.getText());

        // Verify match in snapshot
        assertFalse(snapshot.getMatchList().isEmpty());
        Match match = snapshot.getMatchList().getFirst();
        assertEquals("Match1", match.getName());
        assertEquals(0.9, match.getScore(), 0.001);

        // Verify region in match
        assertEquals(10, match.getRegion().x());
        assertEquals(20, match.getRegion().y());
        assertEquals(30, match.getRegion().w());
        assertEquals(40, match.getRegion().h());
    }

    /**
     * Test serializing and deserializing a MatchHistory
     */
    @Test
    public void testSerializeDeserializeMatchHistory() throws ConfigurationException {
        // Create a match history
        MatchHistory matchHistory = new MatchHistory();
        matchHistory.setTimesSearched(15);
        matchHistory.setTimesFound(12);

        // Create and add a snapshot
        MatchSnapshot snapshot = new MatchSnapshot.Builder()
                .setActionOptions(ActionOptions.Action.CLICK)
                .addMatch(new Match.Builder()
                        .setName("SerializedMatch")
                        .setSimScore(0.85)
                        .setRegion(50, 60, 70, 80)
                        .build())
                .setText("Serialized Text")
                .setActionSuccess(true)
                .build();
        matchHistory.addSnapshot(snapshot);

        // Serialize
        String json = jsonUtils.toJsonSafe(matchHistory);
        System.out.println("DEBUG: Serialized MatchHistory: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        MatchHistory deserializedHistory = jsonParser.convertJson(jsonNode, MatchHistory.class);

        // Verify
        assertNotNull(deserializedHistory);
        assertEquals(15, deserializedHistory.getTimesSearched());
        assertEquals(12, deserializedHistory.getTimesFound());

        // Verify snapshots
        assertNotNull(deserializedHistory.getSnapshots());
        assertEquals(1, deserializedHistory.getSnapshots().size());

        MatchSnapshot deserializedSnapshot = deserializedHistory.getSnapshots().getFirst();
        assertEquals(ActionOptions.Action.CLICK, deserializedSnapshot.getActionOptions().getAction());
        assertTrue(deserializedSnapshot.isActionSuccess());
        assertEquals("Serialized Text", deserializedSnapshot.getText());

        // Verify match in snapshot
        assertFalse(deserializedSnapshot.getMatchList().isEmpty());
        Match deserializedMatch = deserializedSnapshot.getMatchList().getFirst();
        assertEquals("SerializedMatch", deserializedMatch.getName());
        assertEquals(0.85, deserializedMatch.getScore(), 0.001);

        // Verify region in match
        assertEquals(50, deserializedMatch.getRegion().x());
        assertEquals(60, deserializedMatch.getRegion().y());
        assertEquals(70, deserializedMatch.getRegion().w());
        assertEquals(80, deserializedMatch.getRegion().h());
    }

    /**
     * Test adding snapshots and counting methods
     */
    @Test
    public void testAddSnapshot() {
        MatchHistory matchHistory = new MatchHistory();
        assertEquals(0, matchHistory.getTimesSearched());
        assertEquals(0, matchHistory.getTimesFound());

        // Add a successful snapshot
        MatchSnapshot successfulSnapshot = new MatchSnapshot.Builder()
                .addMatch(new Match())
                .build();
        matchHistory.addSnapshot(successfulSnapshot);

        assertEquals(1, matchHistory.getTimesSearched());
        assertEquals(1, matchHistory.getTimesFound());

        // Add a failed snapshot
        MatchSnapshot failedSnapshot = new MatchSnapshot.Builder().build();
        matchHistory.addSnapshot(failedSnapshot);

        assertEquals(2, matchHistory.getTimesSearched());
        assertEquals(1, matchHistory.getTimesFound());
    }

    /**
     * Test the getRandomSnapshot methods
     */
    @Test
    public void testGetRandomSnapshot() {
        MatchHistory matchHistory = new MatchHistory();

        // Add a FIND snapshot
        MatchSnapshot findSnapshot = new MatchSnapshot.Builder()
                .setActionOptions(ActionOptions.Action.FIND)
                .addMatch(new Match.Builder().setName("FindMatch").build())
                .build();
        findSnapshot.setStateId(1L);
        matchHistory.addSnapshot(findSnapshot);

        // Add a CLICK snapshot
        MatchSnapshot clickSnapshot = new MatchSnapshot.Builder()
                .setActionOptions(ActionOptions.Action.CLICK)
                .addMatch(new Match.Builder().setName("ClickMatch").build())
                .build();
        clickSnapshot.setStateId(2L);
        matchHistory.addSnapshot(clickSnapshot);

        // Test getRandomSnapshot by action
        Optional<MatchSnapshot> findResult = matchHistory.getRandomSnapshot(ActionOptions.Action.FIND);
        assertTrue(findResult.isPresent());
        assertEquals("FindMatch", findResult.get().getMatchList().getFirst().getName());

        Optional<MatchSnapshot> clickResult = matchHistory.getRandomSnapshot(ActionOptions.Action.CLICK);
        assertTrue(clickResult.isPresent());
        assertEquals("ClickMatch", clickResult.get().getMatchList().getFirst().getName());

        // Test getRandomSnapshot by action and state
        Optional<MatchSnapshot> stateResult = matchHistory.getRandomSnapshot(ActionOptions.Action.FIND, 1L);
        assertTrue(stateResult.isPresent());
        assertEquals("FindMatch", stateResult.get().getMatchList().getFirst().getName());

        // Test getRandomSnapshot by action and states array
        Optional<MatchSnapshot> multiStateResult = matchHistory.getRandomSnapshot(ActionOptions.Action.CLICK, 1L, 2L);
        assertTrue(multiStateResult.isPresent());
        assertEquals("ClickMatch", multiStateResult.get().getMatchList().getFirst().getName());

        // Test getRandomSnapshot by action and states set
        Optional<MatchSnapshot> setStateResult = matchHistory.getRandomSnapshot(ActionOptions.Action.CLICK, Set.of(2L));
        assertTrue(setStateResult.isPresent());
        assertEquals("ClickMatch", setStateResult.get().getMatchList().getFirst().getName());
    }

    /**
     * Test the isEmpty and merge methods
     */
    @Test
    public void testEmptyAndMerge() {
        MatchHistory emptyHistory = new MatchHistory();
        assertTrue(emptyHistory.isEmpty());

        MatchHistory history1 = new MatchHistory();
        history1.setTimesSearched(5);
        history1.setTimesFound(3);
        history1.addSnapshot(new MatchSnapshot.Builder().addMatch(new Match()).build());
        assertFalse(history1.isEmpty());

        MatchHistory history2 = new MatchHistory();
        history2.setTimesSearched(10);
        history2.setTimesFound(7);
        history2.addSnapshot(new MatchSnapshot.Builder().addMatch(new Match()).build());
        history2.addSnapshot(new MatchSnapshot.Builder().addMatch(new Match()).build());

        // Merge history2 into history1
        history1.merge(history2);

        assertEquals(15, history1.getTimesSearched()); // 5 + 10
        assertEquals(10, history1.getTimesFound()); // 3 + 7
        assertEquals(3, history1.getSnapshots().size()); // 1 + 2
    }

    /**
     * Test the equals method
     */
    @Test
    public void testEquals() {
        MatchHistory history1 = new MatchHistory();
        history1.setTimesSearched(5);
        history1.setTimesFound(3);

        MatchSnapshot snapshot1 = new MatchSnapshot.Builder()
                .setActionOptions(ActionOptions.Action.FIND)
                .addMatch(new Match.Builder().setRegion(10, 20, 30, 40).build())
                .build();
        history1.addSnapshot(snapshot1);

        // Create identical history
        MatchHistory history2 = new MatchHistory();
        history2.setTimesSearched(5);
        history2.setTimesFound(3);

        MatchSnapshot snapshot2 = new MatchSnapshot.Builder()
                .setActionOptions(ActionOptions.Action.FIND)
                .addMatch(new Match.Builder().setRegion(10, 20, 30, 40).build())
                .build();
        history2.addSnapshot(snapshot2);

        assertTrue(history1.equals(history2));

        // Create different history
        MatchHistory history3 = new MatchHistory();
        history3.setTimesSearched(5);
        history3.setTimesFound(4); // Different times found

        MatchSnapshot snapshot3 = new MatchSnapshot.Builder()
                .setActionOptions(ActionOptions.Action.FIND)
                .addMatch(new Match.Builder().setRegion(10, 20, 30, 40).build())
                .build();
        history3.addSnapshot(snapshot3);

        assertFalse(history1.equals(history3));
    }
}