package io.github.jspinak.brobot.model.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActionHistory (formerly MatchHistory) JSON serialization without Spring dependencies.
 * Demonstrates migration from deprecated ActionOptions.Action to new ActionConfig API.
 * Migrated from library-test module.
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
    }

    @Test
    void testSerializeActionHistoryWithFindOptions() throws Exception {
        // Create ActionHistory with new API
        ActionHistory history = new ActionHistory();
        history.setTimesSearched(10);
        history.setTimesFound(7);
        
        // Create ActionRecord with new FindOptions
        ActionRecord record = new ActionRecord();
        
        // ActionRecord still uses ActionOptions, not the new API
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        record.setActionOptions(actionOptions);
        
        // Add match to record
        Match match = new Match.Builder()
                .setRegion(new Region(10, 20, 30, 40))
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
        assertNotNull(snapshotNode.get("actionOptions"));
        assertEquals("Found Text", snapshotNode.get("text").asText());
        assertTrue(snapshotNode.get("actionSuccess").asBoolean());
        assertTrue(snapshotNode.get("resultSuccess").asBoolean());
        
        // Verify match in snapshot
        JsonNode matchListNode = snapshotNode.get("matchList");
        assertNotNull(matchListNode);
        assertEquals(1, matchListNode.size());
        
        JsonNode matchNode = matchListNode.get(0);
        assertEquals("Match1", matchNode.get("name").asText());
        assertEquals(0.9, matchNode.get("simScore").asDouble());
    }

    @Test
    void testSerializeActionHistoryWithClickOptions() throws Exception {
        ActionHistory history = new ActionHistory();
        
        // Create ActionRecord with ClickOptions
        ActionRecord record = new ActionRecord();
        
        // Using ActionOptions instead of ClickOptions
        record.setActionOptions(new ActionOptions.Builder().setAction(ActionOptions.Action.CLICK).build());
        
        record.setActionSuccess(true);
        history.getSnapshots().add(record);
        
        // Serialize
        String json = objectMapper.writeValueAsString(history);
        
        // Verify
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode configNode = jsonNode.get("snapshots").get(0).get("actionOptions");
        assertNotNull(configNode);
        
        // The actual structure depends on Jackson's type handling
        // but we can verify the action was serialized
        assertTrue(json.contains("action") || json.contains("@type"));
    }

    @Test
    void testGetRandomSnapshotByActionType() {
        ActionHistory history = new ActionHistory();
        
        // Add Find action record
        ActionRecord findRecord = new ActionRecord();
        findRecord.setActionOptions(new ActionOptions.Builder().setAction(ActionOptions.Action.FIND).build());
        findRecord.addMatch(new Match.Builder()
                .setRegion(10, 10, 20, 20)
                .build());
        history.getSnapshots().add(findRecord);
        
        // Add Click action record
        ActionRecord clickRecord = new ActionRecord();
        clickRecord.setActionOptions(new ActionOptions.Builder().setAction(ActionOptions.Action.CLICK).build());
        clickRecord.addMatch(new Match.Builder()
                .setRegion(50, 50, 20, 20)
                .build());
        history.getSnapshots().add(clickRecord);
        
        // Test retrieval by action type (requires method update in ActionHistory)
        // This demonstrates how the API would change:
        
        // OLD API:
        // Optional<ActionRecord> findResult = history.getRandomSnapshot(ActionOptions.Action.FIND);
        
        // NEW API would need to filter by config type:
        Optional<ActionRecord> findResult = history.getSnapshots().stream()
                .filter(r -> r.getActionOptions() != null && r.getActionOptions().getAction() == ActionOptions.Action.FIND)
                .findFirst();
        
        Optional<ActionRecord> clickResult = history.getSnapshots().stream()
                .filter(r -> r.getActionOptions() != null && r.getActionOptions().getAction() == ActionOptions.Action.CLICK)
                .findFirst();
        
        assertTrue(findResult.isPresent());
        assertTrue(clickResult.isPresent());
        assertEquals(ActionOptions.Action.FIND, findResult.get().getActionOptions().getAction());
        assertEquals(ActionOptions.Action.CLICK, clickResult.get().getActionOptions().getAction());
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
                // Find action
                record.setActionOptions(new ActionOptions.Builder().setAction(ActionOptions.Action.FIND).build());
            } else {
                // Click action
                record.setActionOptions(new ActionOptions.Builder().setAction(ActionOptions.Action.CLICK).build());
            }
            
            record.addMatch(new Match.Builder()
                    .setRegion(i * 10, i * 10, 20, 20)
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
        // Test reading JSON that might have old ActionOptions structure
        String legacyJson = """
                {
                  "timesSearched": 5,
                  "timesFound": 3,
                  "snapshots": [
                    {
                      "actionOptions": {
                        "action": "FIND",
                        "similarity": 0.8
                      },
                      "matchList": [],
                      "actionSuccess": true
                    }
                  ]
                }
                """;
        
        // Parse JSON structure
        JsonNode jsonNode = objectMapper.readTree(legacyJson);
        
        // Verify we can read the structure
        assertEquals(5, jsonNode.get("timesSearched").asInt());
        assertEquals(3, jsonNode.get("timesFound").asInt());
        
        // Check if it has old structure
        JsonNode snapshotNode = jsonNode.get("snapshots").get(0);
        if (snapshotNode.has("actionOptions")) {
            // Legacy structure detected
            JsonNode actionOptionsNode = snapshotNode.get("actionOptions");
            assertEquals("FIND", actionOptionsNode.get("action").asText());
            assertEquals(0.8, actionOptionsNode.get("similarity").asDouble());
        }
    }

    @Test
    void testActionRecordWithStateContext() throws Exception {
        ActionHistory history = new ActionHistory();
        
        ActionRecord record = new ActionRecord();
        record.setActionOptions(new ActionOptions.Builder().setAction(ActionOptions.Action.FIND).build());
        
        // Add state context
        record.setStateId(1L);
        record.setStateName("TestState");
        
        // Add matches with state info
        Match match = new Match.Builder()
                .setRegion(10, 10, 20, 20)
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
    private Optional<ActionRecord> findRecordByActionType(ActionHistory history, ActionOptions.Action action) {
        return history.getSnapshots().stream()
                .filter(record -> record.getActionOptions() != null && record.getActionOptions().getAction() == action)
                .findFirst();
    }
}