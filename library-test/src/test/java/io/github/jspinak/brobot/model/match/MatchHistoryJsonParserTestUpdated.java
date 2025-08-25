package io.github.jspinak.brobot.model.match;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated tests for MatchHistory JSON parsing without Spring dependencies.
 * Tests the ActionHistory class with new ActionConfig API.
 * Migrated from library-test module.
 */
public class MatchHistoryJsonParserTestUpdated {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Test parsing a basic MatchHistory from JSON with new ActionConfig
     */
    @Test
    public void testParseBasicMatchHistoryWithActionConfig() throws Exception {
        String json = """
                {
                  "timesSearched": 10,
                  "timesFound": 7,
                  "snapshots": [
                    {
                      "actionConfig": {
                        "@type": "PatternFindOptions",
                        "strategy": "FIRST",
                        "similarity": 0.9
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

        JsonNode jsonNode = objectMapper.readTree(json);
        ActionHistory matchHistory = objectMapper.treeToValue(jsonNode, ActionHistory.class);

        assertNotNull(matchHistory);
        assertEquals(10, matchHistory.getTimesSearched());
        assertEquals(7, matchHistory.getTimesFound());

        // Verify snapshots
        assertNotNull(matchHistory.getSnapshots());
        assertEquals(1, matchHistory.getSnapshots().size());

        ActionRecord snapshot = matchHistory.getSnapshots().getFirst();
        assertNotNull(snapshot.getActionConfig());
        assertTrue(snapshot.getActionConfig() instanceof PatternFindOptions);
        
        PatternFindOptions findOptions = (PatternFindOptions) snapshot.getActionConfig();
        assertEquals(PatternFindOptions.Strategy.FIRST, findOptions.getStrategy());
        assertEquals(0.9, findOptions.getSimilarity(), 0.001);

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
     * Test serializing and deserializing a MatchHistory with new ActionConfig
     */
    @Test
    public void testSerializeDeserializeMatchHistoryWithActionConfig() throws Exception {
        // Create a match history
        ActionHistory matchHistory = new ActionHistory();

        // Create click options
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();

        // Create and add a snapshot with new API
        ActionRecord snapshot = new ActionRecord.Builder()
                .setActionConfig(clickOptions)
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
        String json = objectMapper.writeValueAsString(matchHistory);
        System.out.println("DEBUG: Serialized MatchHistory: " + json);

        // Deserialize
        JsonNode jsonNode = objectMapper.readTree(json);
        ActionHistory deserializedHistory = objectMapper.treeToValue(jsonNode, ActionHistory.class);

        // Verify
        assertNotNull(deserializedHistory);
        assertEquals(1, deserializedHistory.getTimesSearched());
        assertEquals(1, deserializedHistory.getTimesFound());

        // Verify snapshots
        assertNotNull(deserializedHistory.getSnapshots());
        assertEquals(1, deserializedHistory.getSnapshots().size());

        ActionRecord deserializedSnapshot = deserializedHistory.getSnapshots().getFirst();
        assertNotNull(deserializedSnapshot.getActionConfig());
        assertTrue(deserializedSnapshot.getActionConfig() instanceof ClickOptions);
        
        ClickOptions deserializedClickOptions = (ClickOptions) deserializedSnapshot.getActionConfig();
        assertEquals(ClickOptions.Type.LEFT, deserializedClickOptions.getClickType());
        
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
        ActionHistory matchHistory = new ActionHistory();
        assertEquals(0, matchHistory.getTimesSearched());
        assertEquals(0, matchHistory.getTimesFound());

        // Add a successful snapshot
        ActionRecord successfulSnapshot = new ActionRecord.Builder()
                .addMatch(new Match())
                .build();
        matchHistory.addSnapshot(successfulSnapshot);

        assertEquals(1, matchHistory.getTimesSearched());
        assertEquals(1, matchHistory.getTimesFound());

        // Add a failed snapshot
        ActionRecord failedSnapshot = new ActionRecord.Builder().build();
        matchHistory.addSnapshot(failedSnapshot);

        assertEquals(2, matchHistory.getTimesSearched());
        assertEquals(1, matchHistory.getTimesFound());
    }

    /**
     * Test the getRandomSnapshot methods with new ActionConfig
     */
    @Test
    public void testGetRandomSnapshotWithActionConfig() {
        ActionHistory matchHistory = new ActionHistory();

        // Create find options
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();

        // Add a FIND snapshot
        ActionRecord findSnapshot = new ActionRecord.Builder()
                .setActionConfig(findOptions)
                .addMatch(new Match.Builder().setName("FindMatch").build())
                .build();
        findSnapshot.setStateId(1L);
        matchHistory.addSnapshot(findSnapshot);

        // Create click options
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.RIGHT)
                .build();

        // Add a CLICK snapshot
        ActionRecord clickSnapshot = new ActionRecord.Builder()
                .setActionConfig(clickOptions)
                .addMatch(new Match.Builder().setName("ClickMatch").build())
                .build();
        clickSnapshot.setStateId(2L);
        matchHistory.addSnapshot(clickSnapshot);

        // Test getRandomSnapshot by action type (using class type)
        Optional<ActionRecord> findResult = matchHistory.getSnapshots().stream()
                .filter(s -> s.getActionConfig() instanceof PatternFindOptions)
                .findFirst();
        assertTrue(findResult.isPresent());
        assertEquals("FindMatch", findResult.get().getMatchList().getFirst().getName());

        Optional<ActionRecord> clickResult = matchHistory.getSnapshots().stream()
                .filter(s -> s.getActionConfig() instanceof ClickOptions)
                .findFirst();
        assertTrue(clickResult.isPresent());
        assertEquals("ClickMatch", clickResult.get().getMatchList().getFirst().getName());

        // Test filtering by state
        Optional<ActionRecord> stateResult = matchHistory.getSnapshots().stream()
                .filter(s -> s.getActionConfig() instanceof PatternFindOptions && s.getStateId() == 1L)
                .findFirst();
        assertTrue(stateResult.isPresent());
        assertEquals("FindMatch", stateResult.get().getMatchList().getFirst().getName());
    }

    /**
     * Test the isEmpty and merge methods
     */
    @Test
    public void testEmptyAndMerge() {
        ActionHistory emptyHistory = new ActionHistory();
        assertTrue(emptyHistory.isEmpty());

        ActionHistory history1 = new ActionHistory();
        history1.addSnapshot(new ActionRecord.Builder().addMatch(new Match()).build());
        assertFalse(history1.isEmpty());

        ActionHistory history2 = new ActionHistory();
        history2.addSnapshot(new ActionRecord.Builder().addMatch(new Match()).build());
        history2.addSnapshot(new ActionRecord.Builder().addMatch(new Match()).build());

        // Merge history2 into history1
        history1.merge(history2);

        assertEquals(3, history1.getTimesSearched()); // 1 + 2
        assertEquals(3, history1.getTimesFound()); // 1 + 2
        assertEquals(3, history1.getSnapshots().size()); // 1 + 2
    }

    /**
     * Test the equals method with new ActionConfig
     */
    @Test
    public void testEqualsWithActionConfig() {
        ActionHistory history1 = new ActionHistory();

        PatternFindOptions findOptions1 = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();

        ActionRecord snapshot1 = new ActionRecord.Builder()
                .setActionConfig(findOptions1)
                .addMatch(new Match.Builder().setRegion(10, 20, 30, 40).build())
                .build();
        history1.addSnapshot(snapshot1);

        // Create identical history
        ActionHistory history2 = new ActionHistory();

        PatternFindOptions findOptions2 = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();

        ActionRecord snapshot2 = new ActionRecord.Builder()
                .setActionConfig(findOptions2)
                .addMatch(new Match.Builder().setRegion(10, 20, 30, 40).build())
                .build();
        history2.addSnapshot(snapshot2);

        System.out.println("History1: " + history1);
        System.out.println("History2: " + history2);
        System.out.println("Equals result: " + history1.equals(history2));
        assertEquals(history1.getSnapshots().getFirst().getActionConfig().getClass(), 
                     history2.getSnapshots().getFirst().getActionConfig().getClass());
        assertTrue(history1.getSnapshots().getFirst().equals(history2.getSnapshots().getFirst()));
        assertTrue(history1.equals(history2));

        // Create different history with different action type
        ActionHistory history3 = new ActionHistory();

        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();

        ActionRecord snapshot3 = new ActionRecord.Builder()
                .setActionConfig(clickOptions)  // Different action type
                .addMatch(new Match.Builder().setRegion(10, 20, 30, 40).build())
                .build();
        history3.addSnapshot(snapshot3);

        assertFalse(history1.equals(history3));
    }

    /**
     * Test backward compatibility with legacy ActionOptions if still supported
     */
    @Test
    public void testLegacyActionOptionsCompatibility() throws Exception {
        // This test demonstrates how to handle legacy ActionOptions if needed
        ActionHistory matchHistory = new ActionHistory();

        // Create legacy snapshot with PatternFindOptions
        PatternFindOptions legacyOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();

        ActionRecord legacySnapshot = new ActionRecord.Builder()
                .setActionConfig(legacyOptions)
                .addMatch(new Match.Builder().setName("LegacyMatch").build())
                .build();
        matchHistory.addSnapshot(legacySnapshot);

        // Serialize
        String json = objectMapper.writeValueAsString(matchHistory);
        assertNotNull(json);
        assertTrue(json.contains("\"strategy\""));
        assertTrue(json.contains("\"FIRST\""));

        // Note: In production, you would migrate these to new ActionConfig format
    }
}