package io.github.jspinak.brobot.model.match;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JSON serialization and deserialization of ActionHistory (MatchHistory).
 * 
 * Note: The original test used non-existent methods:
 * - ClickOptions.Type and setClickType/getClickType don't exist
 * - Click type is configured through MousePressOptions
 * - ActionRecord.Builder is used instead of direct construction
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
class MatchHistoryJsonParserTestUpdated extends BrobotIntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        super.setUpBrobotEnvironment();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
        }
    }

    @Test
    @DisplayName("Should serialize and deserialize empty ActionHistory")
    void testEmptyMatchHistory() throws Exception {
        // Given
        ActionHistory matchHistory = new ActionHistory();

        // When
        String json = objectMapper.writeValueAsString(matchHistory);
        ActionHistory deserializedHistory = objectMapper.readValue(json, ActionHistory.class);

        // Then
        assertNotNull(deserializedHistory);
        assertEquals(0, deserializedHistory.getTimesSearched());
        assertEquals(0, deserializedHistory.getTimesFound());
        assertTrue(deserializedHistory.getSnapshots().isEmpty());
    }

    @Test
    @DisplayName("Should serialize and deserialize ActionHistory with matches")
    void testMatchHistoryWithMatches() throws Exception {
        // Given
        ActionHistory matchHistory = new ActionHistory();
        
        // Create pattern find options
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();

        // Create matches
        Match match1 = new Match.Builder()
                .setName("Match1")
                .setSimScore(0.95)
                .setRegion(10, 20, 30, 40)
                .build();

        Match match2 = new Match.Builder()
                .setName("Match2")
                .setSimScore(0.89)
                .setRegion(50, 60, 70, 80)
                .build();

        // Create snapshot with matches
        ActionRecord snapshot = new ActionRecord.Builder()
                .setActionConfig(findOptions)
                .addMatch(match1)
                .addMatch(match2)
                .setActionSuccess(true)
                .setDuration(150.0)
                .build();
        
        matchHistory.addSnapshot(snapshot);

        // When
        String json = objectMapper.writeValueAsString(matchHistory);
        ActionHistory deserializedHistory = objectMapper.readValue(json, ActionHistory.class);

        // Then
        assertNotNull(deserializedHistory);
        assertEquals(1, deserializedHistory.getTimesSearched());
        assertEquals(1, deserializedHistory.getTimesFound());
        
        List<ActionRecord> snapshots = deserializedHistory.getSnapshots();
        assertEquals(1, snapshots.size());
        
        ActionRecord deserializedSnapshot = snapshots.get(0);
        assertEquals(2, deserializedSnapshot.getMatchList().size());
        assertTrue(deserializedSnapshot.isActionSuccess());
        assertEquals(150.0, deserializedSnapshot.getDuration(), 0.01);
    }

    @Test
    @DisplayName("Should serialize and deserialize ActionHistory with ClickOptions")
    void testMatchHistoryWithClickOptions() throws Exception {
        // Given
        ActionHistory matchHistory = new ActionHistory();

        // Create click options with mouse press options for button type
        MousePressOptions pressOptions = MousePressOptions.builder()
                .setButton(MouseButton.LEFT)
                .build();
                
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .setPressOptions(pressOptions)
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

        ActionRecord deserializedSnapshot = deserializedHistory.getSnapshots().get(0);
        assertNotNull(deserializedSnapshot.getActionConfig());
        assertTrue(deserializedSnapshot.getActionConfig() instanceof ClickOptions);
        
        ClickOptions deserializedClickOptions = (ClickOptions) deserializedSnapshot.getActionConfig();
        assertNotNull(deserializedClickOptions.getMousePressOptions());
        assertEquals(MouseButton.LEFT, deserializedClickOptions.getMousePressOptions().getButton());
        
        assertTrue(deserializedSnapshot.isActionSuccess());
        assertEquals("Serialized Text", deserializedSnapshot.getText());

        // Verify match in snapshot
        assertFalse(deserializedSnapshot.getMatchList().isEmpty());
        Match deserializedMatch = deserializedSnapshot.getMatchList().get(0);
        assertEquals("SerializedMatch", deserializedMatch.getName());
        assertEquals(0.85, deserializedMatch.getScore(), 0.001);
    }

    @Test
    @DisplayName("Should handle multiple snapshots in ActionHistory")
    void testMultipleSnapshots() throws Exception {
        // Given
        ActionHistory matchHistory = new ActionHistory();

        // Add multiple snapshots
        for (int i = 0; i < 3; i++) {
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                    .setStrategy(PatternFindOptions.Strategy.ALL)
                    .build();
                    
            ActionRecord snapshot = new ActionRecord.Builder()
                    .setActionConfig(findOptions)
                    .addMatch(new Match.Builder()
                            .setName("Match" + i)
                            .setSimScore(0.9 - (i * 0.1))
                            .build())
                    .setActionSuccess(i % 2 == 0)
                    .setDuration(100.0 + (i * 50))
                    .build();
            matchHistory.addSnapshot(snapshot);
        }

        // When
        String json = objectMapper.writeValueAsString(matchHistory);
        ActionHistory deserializedHistory = objectMapper.readValue(json, ActionHistory.class);

        // Then
        assertNotNull(deserializedHistory);
        assertEquals(3, deserializedHistory.getTimesSearched());
        assertEquals(2, deserializedHistory.getTimesFound()); // 2 successful (i=0,2)
        assertEquals(3, deserializedHistory.getSnapshots().size());

        // Verify each snapshot
        for (int i = 0; i < 3; i++) {
            ActionRecord snapshot = deserializedHistory.getSnapshots().get(i);
            assertEquals(i % 2 == 0, snapshot.isActionSuccess());
            assertEquals(100.0 + (i * 50), snapshot.getDuration(), 0.01);
        }
    }

    @Test
    @DisplayName("Should preserve timestamps in ActionHistory")
    void testTimestampPreservation() throws Exception {
        // Given
        ActionHistory matchHistory = new ActionHistory();
        LocalDateTime timestamp = LocalDateTime.now();

        ActionRecord snapshot = new ActionRecord.Builder()
                .setActionConfig(new PatternFindOptions.Builder().build())
                .setActionSuccess(true)
                .build();
        matchHistory.addSnapshot(snapshot);

        // When
        String json = objectMapper.writeValueAsString(matchHistory);
        ActionHistory deserializedHistory = objectMapper.readValue(json, ActionHistory.class);

        // Then
        assertNotNull(deserializedHistory);
        ActionRecord deserializedSnapshot = deserializedHistory.getSnapshots().get(0);
        assertNotNull(deserializedSnapshot.getTimeStamp());
        // Timestamp is automatically set when creating ActionRecord
    }

    @Test
    @DisplayName("Should handle null values in ActionHistory")
    void testNullHandling() throws Exception {
        // Given
        ActionHistory matchHistory = new ActionHistory();
        
        ActionRecord snapshot = new ActionRecord.Builder()
                .setActionConfig(null) // null config
                .setText(null) // null text
                .setActionSuccess(false)
                .build();
        matchHistory.addSnapshot(snapshot);

        // When
        String json = objectMapper.writeValueAsString(matchHistory);
        ActionHistory deserializedHistory = objectMapper.readValue(json, ActionHistory.class);

        // Then
        assertNotNull(deserializedHistory);
        ActionRecord deserializedSnapshot = deserializedHistory.getSnapshots().get(0);
        assertNull(deserializedSnapshot.getActionConfig());
        assertNull(deserializedSnapshot.getText());
        assertFalse(deserializedSnapshot.isActionSuccess());
    }

    @Test
    @DisplayName("Should handle custom JSON modifications")
    void testCustomJsonModifications() throws Exception {
        // Given
        ActionHistory matchHistory = new ActionHistory();
        ActionRecord snapshot = new ActionRecord.Builder()
                .setActionConfig(new PatternFindOptions.Builder().build())
                .setActionSuccess(true)
                .build();
        matchHistory.addSnapshot(snapshot);

        // Serialize to JSON and modify
        JsonNode jsonNode = objectMapper.valueToTree(matchHistory);
        ((ObjectNode) jsonNode).put("customField", "customValue");
        ((ObjectNode) jsonNode).put("timesSearched", 10);

        // When
        ActionHistory deserializedHistory = objectMapper.treeToValue(jsonNode, ActionHistory.class);

        // Then
        assertNotNull(deserializedHistory);
        assertEquals(10, deserializedHistory.getTimesSearched());
        // Custom fields are ignored due to @JsonIgnoreProperties(ignoreUnknown = true)
    }

    @Test
    @DisplayName("Should handle complex nested ActionConfig")
    void testComplexActionConfig() throws Exception {
        // Given
        ActionHistory matchHistory = new ActionHistory();
        
        // Create complex click options
        MousePressOptions pressOptions = MousePressOptions.builder()
                .setButton(MouseButton.RIGHT)
                .setPauseBeforeMouseDown(0.1)
                .setPauseAfterMouseDown(0.2)
                .build();
                
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .setPressOptions(pressOptions)
                .build();

        ActionRecord snapshot = new ActionRecord.Builder()
                .setActionConfig(clickOptions)
                .setActionSuccess(true)
                .build();
        matchHistory.addSnapshot(snapshot);

        // When
        String json = objectMapper.writeValueAsString(matchHistory);
        ActionHistory deserializedHistory = objectMapper.readValue(json, ActionHistory.class);

        // Then
        ActionRecord deserializedSnapshot = deserializedHistory.getSnapshots().get(0);
        ClickOptions deserializedOptions = (ClickOptions) deserializedSnapshot.getActionConfig();
        assertEquals(2, deserializedOptions.getNumberOfClicks());
        assertEquals(MouseButton.RIGHT, deserializedOptions.getMousePressOptions().getButton());
        assertEquals(0.1, deserializedOptions.getMousePressOptions().getPauseBeforeMouseDown(), 0.001);
    }
}