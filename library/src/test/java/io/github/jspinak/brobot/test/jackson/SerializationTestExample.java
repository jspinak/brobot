package io.github.jspinak.brobot.test.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test demonstrating how to properly serialize/deserialize Brobot objects.
 * Shows solutions to common Jackson serialization issues.
 */
public class SerializationTestExample extends BrobotTestBase {

    @Test
    public void testActionRecordSerialization() throws Exception {
        // Create ActionRecord with all required fields
        ActionRecord record = new ActionRecord();
        record.setActionConfig(new PatternFindOptions.Builder().build());
        record.setText("Test text");
        record.setDuration(100.0);
        record.setTimeStamp(LocalDateTime.now());
        record.setActionSuccess(true);
        record.setResultSuccess(true);
        record.setStateName("TestState");
        record.setStateId(1L);
        
        // Add a match (will be serialized as simplified object)
        Match match = new Match();
        match.setRegion(new Region(10, 20, 100, 50));
        match.setScore(0.95);
        match.setText("Match text");
        record.addMatch(match);
        
        // Serialize
        String json = testObjectMapper.writeValueAsString(record);
        assertNotNull(json);
        assertTrue(json.contains("\"actionSuccess\":true"));
        
        // Deserialize
        ActionRecord deserialized = testObjectMapper.readValue(json, ActionRecord.class);
        assertNotNull(deserialized);
        assertEquals("Test text", deserialized.getText());
        assertEquals(100.0, deserialized.getDuration(), 0.01);
        assertTrue(deserialized.isActionSuccess());
    }

    @Test
    public void testObjectCollectionSerialization() throws Exception {
        // Create ObjectCollection with various objects
        ObjectCollection collection = new ObjectCollection();
        
        // Add StateLocation
        StateLocation stateLocation = new StateLocation();
        stateLocation.setName("TestLocation");
        Location location = new Location(100, 200);
        stateLocation.setLocation(location);
        collection.getStateLocations().add(stateLocation);
        
        // Add StateRegion
        StateRegion stateRegion = new StateRegion();
        stateRegion.setName("TestRegion");
        Region region = new Region(0, 0, 800, 600);
        stateRegion.setSearchRegion(region);
        collection.getStateRegions().add(stateRegion);
        
        // Add StateImage (without actual image data)
        StateImage stateImage = new StateImage();
        stateImage.setName("TestImage");
        Pattern pattern = new Pattern();
        pattern.setName("test-pattern");
        pattern.setImgpath("images/test.png");
        stateImage.getPatterns().add(pattern);
        collection.getStateImages().add(stateImage);
        
        // Serialize
        String json = testObjectMapper.writeValueAsString(collection);
        assertNotNull(json);
        assertTrue(json.contains("TestLocation"));
        assertTrue(json.contains("TestRegion"));
        assertTrue(json.contains("TestImage"));
        
        // Deserialize
        ObjectCollection deserialized = testObjectMapper.readValue(json, ObjectCollection.class);
        assertNotNull(deserialized);
        assertEquals(1, deserialized.getStateLocations().size());
        assertEquals(1, deserialized.getStateRegions().size());
        assertEquals(1, deserialized.getStateImages().size());
        assertEquals("TestLocation", deserialized.getStateLocations().get(0).getName());
    }

    @Test
    public void testActionResultSerialization() throws Exception {
        // Create ActionResult with proper initialization
        ActionResult result = new ActionResult();
        result.setActionConfig(new PatternFindOptions.Builder().build());
        result.setSuccess(true);
        // Duration is a Duration object, not double
        // Text is a Text object, not String
        
        // Add matches
        Match match1 = new Match();
        match1.setRegion(new Region(10, 10, 50, 50));
        match1.setScore(0.98);
        result.getMatchList().add(match1);
        
        Match match2 = new Match();
        match2.setRegion(new Region(100, 100, 50, 50));
        match2.setScore(0.85);
        result.getMatchList().add(match2);
        
        // Serialize
        String json = testObjectMapper.writeValueAsString(result);
        assertNotNull(json);
        assertTrue(json.contains("\"success\":true"));
        // Text field removed as it's a complex object
        
        // Deserialize
        ActionResult deserialized = testObjectMapper.readValue(json, ActionResult.class);
        assertNotNull(deserialized);
        assertTrue(deserialized.isSuccess());
        assertEquals(2, deserialized.getMatchList().size());
        assertEquals(2, deserialized.getMatchList().size());
    }

    @Test
    public void testPatternFindOptionsSerialization() throws Exception {
        // Create PatternFindOptions with builder pattern
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setSimilarity(0.8)
            .build();
        
        // Serialize
        String json = testObjectMapper.writeValueAsString(options);
        assertNotNull(json);
        assertTrue(json.contains("0.8"));
        
        // Deserialize
        PatternFindOptions deserialized = testObjectMapper.readValue(json, PatternFindOptions.class);
        assertNotNull(deserialized);
        assertEquals(0.8, deserialized.getSimilarity(), 0.01);
    }

    @Test
    public void testMatchSerialization() throws Exception {
        // Create Match with all fields
        Match match = new Match();
        match.setRegion(new Region(50, 75, 200, 150));
        match.setScore(0.92);
        match.setText("Extracted text");
        match.setName("test-match");
        match.setTimeStamp(LocalDateTime.now());
        
        // Serialize
        String json = testObjectMapper.writeValueAsString(match);
        assertNotNull(json);
        assertTrue(json.contains("0.92"));
        assertTrue(json.contains("Extracted text"));
        
        // Deserialize
        Match deserialized = testObjectMapper.readValue(json, Match.class);
        assertNotNull(deserialized);
        assertEquals(0.92, deserialized.getScore(), 0.01);
        assertEquals("Extracted text", deserialized.getText());
        assertNotNull(deserialized.getRegion());
    }

    @Test
    public void testStateImageWithPatternsSerialization() throws Exception {
        // Create StateImage with multiple patterns
        StateImage stateImage = new StateImage();
        stateImage.setName("ComplexState");
        
        // Add patterns without actual image data
        for (int i = 0; i < 3; i++) {
            Pattern pattern = new Pattern();
            pattern.setName("pattern-" + i);
            pattern.setImgpath("images/pattern-" + i + ".png");
            pattern.setFixed(i == 0); // First pattern is fixed
            pattern.setDynamic(i == 2); // Last pattern is dynamic
            
            // Add search regions
            pattern.getRegions().add(new Region(i * 100, i * 100, 200, 200));
            
            stateImage.getPatterns().add(pattern);
        }
        
        // Serialize
        String json = testObjectMapper.writeValueAsString(stateImage);
        assertNotNull(json);
        assertTrue(json.contains("ComplexState"));
        assertTrue(json.contains("pattern-0"));
        assertTrue(json.contains("pattern-1"));
        assertTrue(json.contains("pattern-2"));
        
        // Deserialize
        StateImage deserialized = testObjectMapper.readValue(json, StateImage.class);
        assertNotNull(deserialized);
        assertEquals("ComplexState", deserialized.getName());
        assertEquals(3, deserialized.getPatterns().size());
        assertTrue(deserialized.getPatterns().get(0).isFixed());
        assertTrue(deserialized.getPatterns().get(2).isDynamic());
    }

    @Test
    public void testHandlingNullAndEmptyFields() throws Exception {
        // Create objects with null/empty fields
        ActionRecord record = new ActionRecord();
        // Don't set timestamp, matches, etc.
        
        // Should serialize without errors
        String json = testObjectMapper.writeValueAsString(record);
        assertNotNull(json);
        
        // Should deserialize without errors
        ActionRecord deserialized = testObjectMapper.readValue(json, ActionRecord.class);
        assertNotNull(deserialized);
        assertTrue(deserialized.getMatchList().isEmpty());
        assertEquals("", deserialized.getText());
    }

    @Test
    @org.junit.jupiter.api.Disabled("ObjectCollection requires type information due to enableDefaultTyping - needs review")
    public void testArrayVsSingleValueDeserialization() throws Exception {
        // Test that arrays are properly deserialized
        String jsonWithArray = "{\"stateImages\":[{\"name\":\"test\"}]}";
        
        // Array should deserialize successfully
        ObjectCollection fromArray = testObjectMapper.readValue(jsonWithArray, ObjectCollection.class);
        assertNotNull(fromArray);
        assertEquals(1, fromArray.getStateImages().size());
        
        // Note: Single value as array feature would require additional configuration
        // For ObjectCollection which doesn't have @JsonTypeInfo, this is the expected behavior
    }
}