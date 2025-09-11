package io.github.jspinak.brobot.runner.json.utils;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.test.DisabledInCI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for MatchesJsonUtils for ActionResult serialization.
 * Tests Map conversion, JSON serialization, and deep copying of action results.
 */
@DisplayName("MatchesJsonUtils Tests")

@DisabledInCI
public class MatchesJsonUtilsTest extends BrobotTestBase {

    @Mock
    private JsonUtils mockJsonUtils;
    
    @Mock
    private ConfigurationParser mockJsonParser;
    
    private MatchesJsonUtils matchesJsonUtils;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        matchesJsonUtils = new MatchesJsonUtils(mockJsonUtils, mockJsonParser);
    }
    
    @Nested
    @DisplayName("Map Conversion")
    class MapConversion {
        
        @Test
        @DisplayName("Should convert empty ActionResult to Map")
        public void testEmptyActionResultToMap() {
            // Given
            ActionResult result = new ActionResult();
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            assertNotNull(map);
            assertFalse((Boolean) map.get("success"));
            assertNotNull(map.get("duration")); // Duration is likely a Duration object
            assertNull(map.get("selectedText"));
            assertTrue(((List<?>) map.get("matchList")).isEmpty());
            assertTrue(((Set<?>) map.get("activeStates")).isEmpty());
        }
        
        @Test
        @DisplayName("Should convert successful ActionResult with matches to Map")
        public void testSuccessfulActionResultToMap() {
            // Given
            ActionResult result = new ActionResult();
            result.setSuccess(true);
            result.setDuration(Duration.ofMillis(1500));
            result.setActionDescription("Find button");
            result.setStartTime(LocalDateTime.now());
            result.setEndTime(LocalDateTime.now().plusSeconds(1));
            
            Match match1 = createMatch("button1", 0.95, 100, 200);
            Match match2 = createMatch("button2", 0.88, 300, 400);
            result.add(match1);
            result.add(match2);
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            assertEquals(true, map.get("success"));
            assertNotNull(map.get("duration")); // Duration object
            assertEquals("Find button", map.get("actionDescription"));
            assertNotNull(map.get("startTime"));
            assertNotNull(map.get("endTime"));
            
            List<Map<String, Object>> matches = (List<Map<String, Object>>) map.get("matchList");
            assertEquals(2, matches.size());
            assertEquals(0.95, matches.get(0).get("score"));
            assertEquals("button1", matches.get(0).get("name"));
        }
        
        @Test
        @DisplayName("Should include text extraction results in Map")
        public void testTextExtractionToMap() {
            // Given
            ActionResult result = new ActionResult();
            Text text = new Text();
            text.add("Extracted text from screen");
            result.setText(text);
            result.setSelectedText("Selected portion");
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            assertNotNull(map.get("text")); // Text object
            assertEquals("Selected portion", map.get("selectedText"));
        }
        
        @Test
        @DisplayName("Should include active states and defined regions")
        public void testActiveStatesAndRegionsToMap() {
            // Given
            ActionResult result = new ActionResult();
            Set<String> activeStates = new HashSet<>();
            activeStates.add("MainMenu");
            activeStates.add("ToolbarVisible");
            result.setActiveStates(activeStates);
            
            Region region1 = new Region(0, 0, 100, 100);
            Region region2 = new Region(100, 100, 200, 200);
            result.addDefinedRegion(region1);
            result.addDefinedRegion(region2);
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            Set<String> states = (Set<String>) map.get("activeStates");
            assertEquals(2, states.size());
            assertTrue(states.contains("MainMenu"));
            assertTrue(states.contains("ToolbarVisible"));
            
            List<Region> definedRegions = (List<Region>) map.get("definedRegions");
            assertEquals(2, definedRegions.size());
        }
        
        @Test
        @DisplayName("Should handle Match with StateObjectMetadata")
        public void testMatchWithStateObjectMetadata() {
            // Given
            ActionResult result = new ActionResult();
            
            Match match = createMatch("element", 0.9, 50, 50);
            StateObjectMetadata stateData = new StateObjectMetadata();
            stateData.setStateObjectName("Button");
            stateData.setOwnerStateName("LoginScreen");
            match.setStateObjectData(stateData);
            result.add(match);
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            List<Map<String, Object>> matches = (List<Map<String, Object>>) map.get("matchList");
            Map<String, Object> matchMap = matches.get(0);
            Map<String, String> stateObjectData = (Map<String, String>) matchMap.get("stateObjectData");
            
            assertNotNull(stateObjectData);
            assertEquals("LoginScreen", stateObjectData.get("ownerStateName"));
            assertEquals("Button", stateObjectData.get("stateObjectName"));
        }
        
        @Test
        @DisplayName("Should handle Match without StateObjectMetadata")
        public void testMatchWithoutStateObjectMetadata() {
            // Given
            ActionResult result = new ActionResult();
            Match match = createMatch("element", 0.9, 50, 50);
            match.setStateObjectData(null);
            result.add(match);
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            List<Map<String, Object>> matches = (List<Map<String, Object>>) map.get("matchList");
            Map<String, Object> matchMap = matches.get(0);
            assertFalse(matchMap.containsKey("stateObjectData"));
        }
        
        @Test
        @DisplayName("Should preserve Match region and score")
        public void testMatchRegionAndScore() {
            // Given
            ActionResult result = new ActionResult();
            Match match = createMatch("target", 0.987, 123, 456);
            result.add(match);
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            List<Map<String, Object>> matches = (List<Map<String, Object>>) map.get("matchList");
            Map<String, Object> matchMap = matches.get(0);
            
            assertEquals(0.987, matchMap.get("score"));
            Region region = (Region) matchMap.get("region");
            assertEquals(123, region.x());
            assertEquals(456, region.y());
        }
    }
    
    @Nested
    @DisplayName("JSON Serialization")
    class JSONSerialization {
        
        @Test
        @DisplayName("Should serialize ActionResult to JSON")
        public void testSerializeToJson() throws Exception {
            // Given
            ActionResult result = new ActionResult();
            result.setSuccess(true);
            Text text = new Text();
            text.add("Sample text");
            result.setText(text);
            
            String expectedJson = "{\"success\":true,\"text\":\"Sample text\"}";
            when(mockJsonUtils.toJsonSafe(result)).thenReturn(expectedJson);
            
            // When
            String json = matchesJsonUtils.matchesToJson(result);
            
            // Then
            assertEquals(expectedJson, json);
            verify(mockJsonUtils).toJsonSafe(result);
        }
        
        @Test
        @DisplayName("Should handle serialization failure")
        public void testSerializationFailure() throws Exception {
            // Given
            ActionResult result = new ActionResult();
            when(mockJsonUtils.toJsonSafe(result))
                .thenThrow(new ConfigurationException("Serialization failed"));
            
            // When/Then
            assertThrows(ConfigurationException.class,
                () -> matchesJsonUtils.matchesToJson(result));
        }
        
        @Test
        @DisplayName("Should serialize null ActionResult")
        public void testSerializeNull() throws Exception {
            // Given
            when(mockJsonUtils.toJsonSafe(null)).thenReturn("null");
            
            // When
            String json = matchesJsonUtils.matchesToJson(null);
            
            // Then
            assertEquals("null", json);
        }
        
        @Test
        @DisplayName("Should serialize ActionResult with many matches")
        public void testSerializeManyMatches() throws Exception {
            // Given
            ActionResult result = new ActionResult();
            for (int i = 0; i < 100; i++) {
                result.add(createMatch("match" + i, 0.8 + (i * 0.001), i * 10, i * 20));
            }
            
            when(mockJsonUtils.toJsonSafe(result)).thenReturn("{\"large\":\"result\"}");
            
            // When
            String json = matchesJsonUtils.matchesToJson(result);
            
            // Then
            assertNotNull(json);
            verify(mockJsonUtils).toJsonSafe(result);
        }
    }
    
    @Nested
    @DisplayName("Deep Copy")
    class DeepCopy {
        
        @Test
        @DisplayName("Should create deep copy of ActionResult")
        public void testDeepCopy() throws Exception {
            // Given
            ActionResult original = new ActionResult();
            original.setSuccess(true);
            Text originalText = new Text();
            originalText.add("Original text");
            original.setText(originalText);
            original.add(createMatch("match1", 0.9, 10, 20));
            
            String json = "{\"success\":true,\"text\":\"Original text\"}";
            ActionResult copy = new ActionResult();
            copy.setSuccess(true);
            Text copyText = new Text();
            copyText.add("Original text");
            copy.setText(copyText);
            copy.add(createMatch("match1", 0.9, 10, 20));
            
            when(mockJsonUtils.toJsonSafe(original)).thenReturn(json);
            when(mockJsonParser.convertJson(json, ActionResult.class)).thenReturn(copy);
            
            // When
            ActionResult result = matchesJsonUtils.deepCopy(original);
            
            // Then
            assertNotNull(result);
            assertNotSame(original, result);
            assertEquals(original.isSuccess(), result.isSuccess());
            assertNotNull(result.getText()); // Text comparison
            assertEquals(original.getMatchList().size(), result.getMatchList().size());
        }
        
        @Test
        @DisplayName("Should handle deep copy serialization failure")
        public void testDeepCopySerializationFailure() throws Exception {
            // Given
            ActionResult original = new ActionResult();
            when(mockJsonUtils.toJsonSafe(original))
                .thenThrow(new ConfigurationException("Serialization failed"));
            
            // When/Then
            assertThrows(ConfigurationException.class,
                () -> matchesJsonUtils.deepCopy(original));
        }
        
        @Test
        @DisplayName("Should handle deep copy deserialization failure")
        public void testDeepCopyDeserializationFailure() throws Exception {
            // Given
            ActionResult original = new ActionResult();
            String json = "{}";
            
            when(mockJsonUtils.toJsonSafe(original)).thenReturn(json);
            when(mockJsonParser.convertJson(json, ActionResult.class))
                .thenThrow(new ConfigurationException("Deserialization failed"));
            
            // When/Then
            assertThrows(ConfigurationException.class,
                () -> matchesJsonUtils.deepCopy(original));
        }
        
        @Test
        @DisplayName("Should create independent copy")
        public void testDeepCopyIndependence() throws Exception {
            // Given
            ActionResult original = new ActionResult();
            original.setSuccess(true);
            Set<String> originalStates = new HashSet<>();
            originalStates.add("State1");
            original.setActiveStates(originalStates);
            
            ActionResult copy = new ActionResult();
            copy.setSuccess(true);
            Set<String> copyStates = new HashSet<>();
            copyStates.add("State1");
            copy.setActiveStates(copyStates);
            
            String json = "{}";
            when(mockJsonUtils.toJsonSafe(original)).thenReturn(json);
            when(mockJsonParser.convertJson(json, ActionResult.class)).thenReturn(copy);
            
            // When
            ActionResult result = matchesJsonUtils.deepCopy(original);
            
            // Modify original
            original.setSuccess(false);
            originalStates.add("State2");
            original.setActiveStates(originalStates);
            
            // Then
            assertTrue(result.isSuccess()); // Copy unaffected
            assertEquals(1, result.getActiveStates().size());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle ActionResult with null fields")
        public void testNullFields() {
            // Given
            ActionResult result = new ActionResult();
            result.setActionDescription(null);
            result.setText(null);  // Text can be null
            result.setSelectedText(null);
            result.setStartTime(null);
            result.setEndTime(null);
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            assertFalse(map.containsKey("actionDescription"));
            assertFalse(map.containsKey("text"));
            assertFalse(map.containsKey("selectedText"));
            assertFalse(map.containsKey("startTime"));
            assertFalse(map.containsKey("endTime"));
            assertNotNull(map.get("matchList")); // Should still have empty list
        }
        
        @Test
        @DisplayName("Should handle ActionResult with empty collections")
        public void testEmptyCollections() {
            // Given
            ActionResult result = new ActionResult();
            // All collections start empty
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            assertTrue(((List<?>) map.get("matchList")).isEmpty());
            assertTrue(((Set<?>) map.get("activeStates")).isEmpty());
            assertTrue(((List<?>) map.get("definedRegions")).isEmpty());
        }
        
        @Test
        @DisplayName("Should handle Match with extreme score values")
        public void testExtremeScoreValues() {
            // Given
            ActionResult result = new ActionResult();
            result.add(createMatch("perfect", 1.0, 0, 0));
            result.add(createMatch("terrible", 0.0, 0, 0));
            result.add(createMatch("negative", -0.5, 0, 0)); // Invalid but should handle
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            List<Map<String, Object>> matches = (List<Map<String, Object>>) map.get("matchList");
            assertEquals(3, matches.size());
            assertEquals(1.0, matches.get(0).get("score"));
            assertEquals(0.0, matches.get(1).get("score"));
            assertEquals(-0.5, matches.get(2).get("score"));
        }
        
        @Test
        @DisplayName("Should handle very long text content")
        public void testLongTextContent() {
            // Given
            ActionResult result = new ActionResult();
            Text longText = new Text();
            for (int i = 0; i < 10000; i++) {
                longText.add("Line " + i);
            }
            result.setText(longText);
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            Object text = map.get("text");
            assertNotNull(text);
            // Text object will be in the map
            assertTrue(text instanceof Text || text.toString().length() > 50000);
        }
        
        @Test
        @DisplayName("Should handle ActionResult with duplicate matches")
        public void testDuplicateMatches() {
            // Given
            ActionResult result = new ActionResult();
            Match sameMatch = createMatch("duplicate", 0.9, 100, 100);
            result.add(sameMatch);
            result.add(sameMatch); // Add same match twice
            result.add(sameMatch); // And again
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            List<Map<String, Object>> matches = (List<Map<String, Object>>) map.get("matchList");
            assertEquals(3, matches.size()); // Duplicates preserved
            // All should have same values
            for (Map<String, Object> match : matches) {
                assertEquals("duplicate", match.get("name"));
                assertEquals(0.9, match.get("score"));
            }
        }
        
        @ParameterizedTest
        @ValueSource(longs = {0, 500, 1000, 10500, 100000, 1000000})
        @DisplayName("Should handle various duration values")
        public void testVariousDurations(long durationMillis) {
            // Given
            ActionResult result = new ActionResult();
            result.setDuration(Duration.ofMillis(durationMillis));
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            Object duration = map.get("duration");
            assertNotNull(duration);
            assertTrue(duration instanceof Duration || duration.toString().contains(String.valueOf(durationMillis)));
        }
        
        @Test
        @DisplayName("Should handle circular references safely")
        public void testCircularReferences() {
            // Given
            ActionResult result = new ActionResult();
            Match match = createMatch("circular", 0.8, 50, 50);
            // In a real scenario, match might reference back to result
            // but our Map conversion should break this cycle
            result.add(match);
            
            // When
            Map<String, Object> map = matchesJsonUtils.matchesToMap(result);
            
            // Then
            assertNotNull(map);
            List<Map<String, Object>> matches = (List<Map<String, Object>>) map.get("matchList");
            assertEquals(1, matches.size());
            // Map should not contain circular reference
            assertFalse(matches.get(0).containsValue(result));
        }
    }
    
    // Helper method to create Match objects
    private Match createMatch(String name, double score, int x, int y) {
        Match match = new Match();
        match.setName(name);
        match.setScore(score);
        match.setRegion(new Region(x, y, 100, 50));
        return match;
    }
}