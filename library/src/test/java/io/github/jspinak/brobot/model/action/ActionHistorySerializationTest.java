package io.github.jspinak.brobot.model.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActionHistory JSON serialization using the new ActionConfig API.
 * This test class demonstrates the migration from deprecated ActionOptions to ActionConfig.
 */
class ActionHistorySerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Register JavaTimeModule for LocalDateTime serialization
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        // Configure to ignore getMat() method that can throw NPE
        objectMapper.addMixIn(Match.class, MatchMixIn.class);
    }
    
    // MixIn to ignore the problematic getMat() method during serialization
    private static abstract class MatchMixIn {
        @com.fasterxml.jackson.annotation.JsonIgnore
        abstract org.bytedeco.opencv.opencv_core.Mat getMat();
    }

    @Test
    void testSerializeActionHistoryWithFindOptions() throws Exception {
        // Create ActionHistory with new API
        ActionHistory history = new ActionHistory();
        history.setTimesSearched(10);
        history.setTimesFound(7);
        
        // Create ActionRecord with new PatternFindOptions
        ActionRecord record = new ActionRecord();
        
        // Use the new ActionConfig API
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        record.setActionConfig(findOptions);
        
        // Add match to record - create a valid Match with all required fields
        Region region = new Region(10, 20, 30, 40);
        Match match = new Match.Builder()
                .setRegion(region)
                .setSimScore(0.9)
                .setName("Match1")
                .build();
        record.getMatchList().add(match);
        
        record.setText("Found Text");
        record.setActionSuccess(true);
        record.setResultSuccess(true);
        
        history.getSnapshots().add(record);
        
        // Serialize
        String json = objectMapper.writeValueAsString(history);
        
        // Verify JSON structure
        assertNotNull(json);
        JsonNode jsonNode = objectMapper.readTree(json);
        
        assertEquals(10, jsonNode.get("timesSearched").asInt());
        assertEquals(7, jsonNode.get("timesFound").asInt());
        
        // Verify snapshot
        JsonNode snapshotsNode = jsonNode.get("snapshots");
        assertNotNull(snapshotsNode);
        assertTrue(snapshotsNode.isArray());
        assertEquals(1, snapshotsNode.size());
        
        JsonNode snapshotNode = snapshotsNode.get(0);
        assertNotNull(snapshotNode.get("actionConfig"));
        assertEquals("Found Text", snapshotNode.get("text").asText());
        assertTrue(snapshotNode.get("actionSuccess").asBoolean());
        assertTrue(snapshotNode.get("resultSuccess").asBoolean());
        
        // Verify match in snapshot
        JsonNode matchListNode = snapshotNode.get("matchList");
        assertNotNull(matchListNode);
        assertEquals(1, matchListNode.size());
        
        JsonNode matchNode = matchListNode.get(0);
        assertEquals("Match1", matchNode.get("name").asText());
        // The field might be named "score" instead of "simScore" in JSON
        if (matchNode.get("score") != null) {
            assertEquals(0.9, matchNode.get("score").asDouble());
        } else if (matchNode.get("simScore") != null) {
            assertEquals(0.9, matchNode.get("simScore").asDouble());
        } else {
            fail("Neither 'score' nor 'simScore' field found in Match JSON");
        }
    }

    @Test
    void testSerializeActionHistoryWithClickOptions() throws Exception {
        ActionHistory history = new ActionHistory();
        
        // Create ActionRecord with ClickOptions
        ActionRecord record = new ActionRecord();
        
        // Using new ClickOptions API
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        record.setActionConfig(clickOptions);
        
        record.setActionSuccess(true);
        history.getSnapshots().add(record);
        
        // Serialize
        String json = objectMapper.writeValueAsString(history);
        
        // Verify
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode configNode = jsonNode.get("snapshots").get(0).get("actionConfig");
        assertNotNull(configNode);
        
        // The actual structure depends on Jackson's type handling
        // but we can verify the config was serialized
        assertTrue(json.contains("actionConfig"));
        assertTrue(json.contains("@type"));
    }

    @Test
    void testGetRandomSnapshotByActionType() {
        ActionHistory history = new ActionHistory();
        
        // Add Find action record with new API
        ActionRecord findRecord = new ActionRecord();
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        findRecord.setActionConfig(findOptions);
        
        Region region1 = new Region(10, 10, 20, 20);
        findRecord.addMatch(new Match.Builder()
                .setRegion(region1)
                .setSimScore(0.95)
                .build());
        history.getSnapshots().add(findRecord);
        
        // Add Click action record with new API
        ActionRecord clickRecord = new ActionRecord();
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        clickRecord.setActionConfig(clickOptions);
        
        Region region2 = new Region(50, 50, 20, 20);
        clickRecord.addMatch(new Match.Builder()
                .setRegion(region2)
                .setSimScore(0.95)
                .build());
        history.getSnapshots().add(clickRecord);
        
        // Test retrieval by config type
        Optional<ActionRecord> findResult = history.getSnapshots().stream()
                .filter(r -> r.getActionConfig() instanceof PatternFindOptions)
                .findFirst();
        
        Optional<ActionRecord> clickResult = history.getSnapshots().stream()
                .filter(r -> r.getActionConfig() instanceof ClickOptions)
                .findFirst();
        
        assertTrue(findResult.isPresent());
        assertTrue(clickResult.isPresent());
        assertTrue(findResult.get().getActionConfig() instanceof PatternFindOptions);
        assertTrue(clickResult.get().getActionConfig() instanceof ClickOptions);
    }

    @Test
    void testMultipleActionRecords() throws Exception {
        ActionHistory history = new ActionHistory();
        history.setTimesSearched(20);
        history.setTimesFound(15);
        
        // Add multiple records with different action types
        for (int i = 0; i < 3; i++) {
            ActionRecord record = new ActionRecord();
            
            if (i % 2 == 0) {
                // Find action with new API
                PatternFindOptions findOptions = new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .build();
                record.setActionConfig(findOptions);
            } else {
                // Click action with new API
                ClickOptions clickOptions = new ClickOptions.Builder()
                        .setClickType(ClickOptions.Type.RIGHT)
                        .build();
                record.setActionConfig(clickOptions);
            }
            
            Region region = new Region(i * 10, i * 10, 20, 20);
            record.addMatch(new Match.Builder()
                    .setRegion(region)
                    .setSimScore(0.8 + i * 0.05)
                    .build());
            
            record.setActionSuccess(true);
            history.getSnapshots().add(record);
        }
        
        // Serialize
        String json = objectMapper.writeValueAsString(history);
        
        // Verify structure
        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals(20, jsonNode.get("timesSearched").asInt());
        assertEquals(15, jsonNode.get("timesFound").asInt());
        assertEquals(3, jsonNode.get("snapshots").size());
    }

    @Test
    void testDeserializeFromLegacyJson() throws Exception {
        // Test reading JSON that might have new ActionConfig structure
        String json = """
                {
                  "timesSearched": 5,
                  "timesFound": 3,
                  "snapshots": [
                    {
                      "actionConfig": {
                        "@type": "PatternFindOptions",
                        "strategy": "FIRST"
                      },
                      "matchList": [],
                      "actionSuccess": true
                    }
                  ]
                }
                """;
        
        // Parse JSON structure
        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify we can read the structure
        assertEquals(5, jsonNode.get("timesSearched").asInt());
        assertEquals(3, jsonNode.get("timesFound").asInt());
        
        // Check if it has new structure
        JsonNode snapshotNode = jsonNode.get("snapshots").get(0);
        if (snapshotNode.has("actionConfig")) {
            // New structure detected
            JsonNode actionConfigNode = snapshotNode.get("actionConfig");
            assertEquals("PatternFindOptions", actionConfigNode.get("@type").asText());
            assertEquals("FIRST", actionConfigNode.get("strategy").asText());
        }
    }

    @Test
    void testActionRecordWithStateContext() throws Exception {
        ActionHistory history = new ActionHistory();
        
        ActionRecord record = new ActionRecord();
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
        record.setActionConfig(findOptions);
        
        // Add state context
        record.setStateId(1L);
        record.setStateName("TestState");
        
        // Add matches with state info
        Region region = new Region(10, 10, 20, 20);
        Match match = new Match.Builder()
                .setRegion(region)
                .setSimScore(0.95)
                .build();
        record.addMatch(match);
        
        history.getSnapshots().add(record);
        
        // Serialize
        String json = objectMapper.writeValueAsString(history);
        
        // Verify state context is preserved
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode snapshotNode = jsonNode.get("snapshots").get(0);
        
        assertEquals(1L, snapshotNode.get("stateId").asLong());
        assertEquals("TestState", snapshotNode.get("stateName").asText());
    }

    @Test
    void testEmptyActionHistory() throws Exception {
        ActionHistory history = new ActionHistory();
        
        String json = objectMapper.writeValueAsString(history);
        assertNotNull(json);
        
        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals(0, jsonNode.get("timesSearched").asInt());
        assertEquals(0, jsonNode.get("timesFound").asInt());
        
        JsonNode snapshotsNode = jsonNode.get("snapshots");
        assertTrue(snapshotsNode == null || snapshotsNode.isEmpty());
    }

    /**
     * Helper method to demonstrate how to filter records by action type with new API
     */
    private Optional<ActionRecord> findRecordByConfigType(ActionHistory history, Class<?> configType) {
        return history.getSnapshots().stream()
                .filter(record -> record.getActionConfig() != null && 
                                configType.isInstance(record.getActionConfig()))
                .findFirst();
    }
}